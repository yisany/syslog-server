#! /bin/bash

trap 'logMsgToConfigSysLog "INFO" "Aborting the script."; exit 1' INT

#################### Param ##########################

RSYSLOG_DIR=/var/spool/rsyslog
RSYSLOG_SERVICE=rsyslog
RSYSLOGD=rsyslogd
MIN_RSYSLOG_VERSION=5.8.0
RSYSLOG_VERSION=

HOST_NAME=
LINUX_DIST=

CONF_FILE="/etc/rsyslog.d/dtstack.conf"
SYSLOG_SERVER_HOST=
PORT="9898"
PROTOCOL=

URL_PEM=

contentTemplate="\$template DTStackFormat,\"unique %syslogpriority% %timestamp% %hostname% %syslogtag% %msg%\""

LINUX_ENV_VALIDATED="false"

#################### Method ##########################

logMsgToConfigSysLog()
{
  if [ $1 == "INFO" ]; then
    echo -e "\033[1;36m[$(date +"%Y-%m-%d %H:%M:%S")] $@\033[m" >&2
  elif [ $1 == "WARN" ]; then
    echo -e "\033[1;33m[$(date +"%Y-%m-%d %H:%M:%S")] $@\033[m" >&2
  elif [ $1 == "ERROR" ]; then
    echo -e "\033[1;31m[$(date +"%Y-%m-%d %H:%M:%S")] $@\033[m" >&2
  else
    echo -e "\033[1;36m[$(date +"%Y-%m-%d %H:%M:%S")] $@\033[m" >&2
  fi
}

check()
{
  checkIfUserHasRootPrivileges
  checkIfSupportedOS
  checkIfHostnameOK
  checkIfRsyslogConfiguredAsService
  checkIfMinVersionOfRsyslog
  checkIfSelinuxServiceEnforced
  checkUrlAndOpenSsl

  LINUX_ENV_VALIDATED="true"
}

write()
{
  if [[ $PROTOCOL == "UDP" || $PROTOCOL == "udp" ]]; then
    writeUDP
  elif [[ $PROTOCOL == "TCP" || $PROTOCOL == "tcp" ]]; then
    writeTCP
  elif [[ $PROTOCOL == "TLS" || $PROTOCOL == "tls" ]]; then
    writeTLS
  fi
}

writeUDP()
{
  rmFile
  contentSend="*.*  @${SYSLOG_SERVER_HOST}:${PORT};DTStackFormat"
  touch $CONF_FILE
  echo "$contentTemplate" >> $CONF_FILE
  echo "$contentSend" >> $CONF_FILE
}

writeTCP()
{
  rmFile
  contentSend="*.*  @@${SYSLOG_SERVER_HOST}:${PORT};DTStackFormat"
  touch $CONF_FILE
  echo "$contentTemplate" >> $CONF_FILE
  echo "$contentSend" >> $CONF_FILE
}

writeTLS()
{
  rmFile
  contentSend="*.*  @@${SYSLOG_SERVER_HOST}:${PORT};DTStackFormat"
  touch $CONF_FILE
  PEM_DIR="/etc/rsyslog.d/pem"

  if [ ! -d "${PEM_DIR}" ];then
    mkdir ${PEM_DIR}
  else
    if [ `ls ${PEM_DIR} | wc -l` != 0 ];then
      rm -f ${PEM_DIR}/*
    fi
  fi

  sudo mv ${URL_PEM}/server.crt $PEM_DIR
  sudo yum install -y rsyslog-gnutls

  echo "\$DefaultNetstreamDriver gtls" >> $CONF_FILE
  echo "\$DefaultNetstreamDriverCAFile ${PEM_DIR}/server.crt" >> $CONF_FILE
  echo "\$ActionSendStreamDriverAuthMode anon" >> $CONF_FILE
  echo "\$ActionSendStreamDriverMode 1" >> $CONF_FILE
  echo "$contentTemplate" >> $CONF_FILE
  echo "$contentSend" >> $CONF_FILE
}

# 检查dtstack.coonf文件是否已经存在
rmFile()
{
  if [ `ls /etc/rsyslog.d | grep dtstack.conf |wc -l`  == 1 ];then
    rm -f /etc/rsyslog.d/dtstack.conf
  fi
}

checkIfUserHasRootPrivileges()
{
  if [[ $EUID -ne 0 ]]; then
    logMsgToConfigSysLog "ERROR" "This script must be run with root privilege"
    exit 1
  else
    logMsgToConfigSysLog "INFO" "checkIfUserHasRootPrivileges is OK"
  fi
}

checkIfSupportedOS()
{
  getOS

  LINUX_DIST_IN_LOWER_CASE=$(echo $LINUX_DIST | tr "[:upper:]" "[:lower:]")

  case "$LINUX_DIST_IN_LOWER_CASE" in
    *"ubuntu"* )
    logMsgToConfigSysLog "INFO" "Operating system is Ubuntu."
    ;;
    *"redhat"* )
    logMsgToConfigSysLog "INFO" "Operating system is Red Hat."
    ;;
    *"centos"* )
    logMsgToConfigSysLog "INFO" "Operating system is CentOS."
    ;;
    *"darwin"* )
    logMsgToConfigSysLog "ERROR" "This script is for Linux systems, and Darwin or Mac OSX are not currently supported"
    logMsgToConfigSysLog "ERROR" "You can config rsyslog manually"
    exit 1
    ;;
    * )
    logMsgToConfigSysLog "WARN" "The linux distribution '$LINUX_DIST' has not been previously tested with Rizhiyi."
    while true; do
      read -p "Would you like to continue anyway? (yes/no)" yn
      case $yn in
        [Yy]* )
        break;;
        [Nn]* )
        exit 1
        ;;
        * ) echo "Please answer yes or no.";;
      esac
    done
    ;;
  esac

  logMsgToConfigSysLog "INFO" "CheckIfSupportedOS is OK"
}

getOS()
{
  UNAME=$(uname | tr "[:upper:]" "[:lower:]")
  if [ "$UNAME" == "linux" ]; then
    # /etc/issue always exit (Ubuntu, Debian, CentOS, RHEL, Arch, OpenSUSE)
    if [ -f /etc/lsb-release -o -d /etc/lsb-release.d ]; then
      LINUX_DIST=$(lsb_release -i | cut -d: -f2 | sed s/'^\t'//)
    elif [ -f /etc/system-release ]; then
      LINUX_DIST=$(cat /etc/system-release  | cut -f 1 -d  " ")
    else
      LINUX_DIST=$(ls -d /etc/[A-Za-z]*[_-][rv]e[lr]* | grep -v "lsb" | cut -d'/' -f3 | cut -d'-' -f1 | cut -d'_' -f1)
    fi
  fi

  if [ "$LINUX_DIST" == "" ]; then
    LINUX_DIST=$(uname)
  fi
}

checkIfHostnameOK()
{
  HOSTNAME=$(hostname)
  IS_VALID_HOSTNAME="NO"
  if [ "x$HOSTNAME" == "x" ]; then
    logMsgToConfigSysLog "ERROR" "Bad Hostname [$HOSTNAME]"
  elif [ "$HOSTNAME" == "localhost" ]; then
    logMsgToConfigSysLog "ERROR" "Bad Hostname [$HOSTNAME]"
  elif [ "$HOSTNAME" == "127.0.0.1" ]; then
    logMsgToConfigSysLog "ERROR" "Bad Hostname [$HOSTNAME]"
  else
    IS_VALID_HOSTNAME="YES"
  fi

  if [ "$IS_VALID_HOSTNAME" == "NO" ]; then
    logMsgToConfigSysLog "ERROR" "Please Config Your Hostname First"
    exit 1
  fi
  logMsgToConfigSysLog "INFO" "checkIfHostnameOK is OK"
}

checkLinuxVersion()
{
    LINUX_VERSION=`awk '{print $(NF-1)}' /etc/redhat-release 2> /dev/null`
    LINUX_INT_VERSION=${LINUX_VERSION:0:1}
}

checkIfRsyslogConfiguredAsService()
{
  checkLinuxVersion
  if [ -f /etc/init.d/$RSYSLOG_SERVICE ] || [[ -f /usr/lib/systemd/system/rsyslog.service ]]; then
    logMsgToConfigSysLog "INFO" "$RSYSLOG_SERVICE is present as service."
  else
    logMsgToConfigSysLog "ERROR" "$RSYSLOG_SERVICE is not present as service."
    exit 1
  fi

  if [ $(ps -ef | grep -v grep | grep "$RSYSLOG_SERVICE" | wc -l) -eq 0 ]; then
    logMsgToConfigSysLog "INFO" "$RSYSLOG_SERVICE is not running. Attempting to start service."
    if [[ ${LINUX_INT_VERSION} -ge 7 ]]; then
      sudo systemctl start ${RSYSLOG_SERVICE}.service
    else
      sudo service $RSYSLOG_SERVICE start
    fi
  fi
}

compareVersions ()
{
  typeset    IFS='.'
  typeset -a v1=( $1 )
  typeset -a v2=( $2 )
  typeset    n diff

  for (( n=0; n<$3; n+=1 )); do
  diff=$((v1[n]-v2[n]))
  if [ $diff -ne 0 ] ; then
    [ $diff -le 0 ] && echo '-1' || echo '1'
    return
  fi
  done
  echo  '0'
}

checkIfMinVersionOfRsyslog()
{
  RSYSLOG_VERSION=$(sudo $RSYSLOGD -version | grep "$RSYSLOGD")
  RSYSLOG_VERSION=${RSYSLOG_VERSION#* }
  RSYSLOG_VERSION=${RSYSLOG_VERSION%,*}
  RSYSLOG_VERSION=$RSYSLOG_VERSION | tr -d " "
  if [ $(compareVersions $RSYSLOG_VERSION $MIN_RSYSLOG_VERSION 3) -lt 0 ]; then
    logMsgToConfigSysLog "ERROR" "Min rsyslog version required is ${MIN_RSYSLOG_VERSION}"
    exit 1
  fi
  logMsgToConfigSysLog "INFO" "checkIfMinVersionOfRsyslog is OK"
}

checkIfSelinuxServiceEnforced()
{
  if [[ "$LINUX_DIST" != *"Ubuntu"* ]];then
    isSelinuxInstalled=$(getenforce -ds 2>/dev/null)
    if [ `sudo getenforce | grep "Disabled" | wc -l` -eq 0 ]; then
      logMsgToConfigSysLog "ERROR" "selinux status is not enforced.Please stop it."
      exit 1
    elif [ $(sudo getenforce | grep "Enforcing" | wc -l) -gt 0 ]; then
      logMsgToConfigSysLog "ERROR" "selinux status is 'Enforcing'. Please disable it and start the rsyslog daemon manually."
      exit 1
    fi
  fi
  logMsgToConfigSysLog "INFO" "checkIfSelinuxServiceEnforced is OK"
}

checkUrlAndOpenSsl()
{
  if [ $(ping -c 1 www.baidu.com | grep "1 packets transmitted, 1 received, 0% packet loss" | wc -l) == 1 ]; then
    logMsgToConfigSysLog "INFO" "The Internet is reachable."
  else
      logMsgToConfigSysLog "WARN" "The Internet is not reachable via ping."
      exit 1
  fi
  logMsgToConfigSysLog "INFO" "Checking if curl is existed."
  if [ $(which curl | wc -l) == 0 ]; then
    logMsgToConfigSysLog "ERROR" "We need \"curl\" to check , but it's not found"
    exit 1
  fi
  if [ $(which openssl | wc -l) == 0 ]; then
    logMsgToConfigSysLog "ERROR" "We need \"openssl\" to check , but it's not found"
    exit 1
  fi
  logMsgToConfigSysLog "INFO" "checkUrlAndOpenSsl is OK."
}



#################### Main ##########################

if [ "$LINUX_ENV_VALIDATED" == "false" ]; then
  check
fi
write
ehco "Success!!! Syslog Config is finished."



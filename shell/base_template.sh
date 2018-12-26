#! /bin/bash

url="http://192.168.92.1:8855/open/api/v2/syslog/downloadSyslog"

ipArray=
serverIp=
protocol=
tenantId=

unique=

# 获取本机ip
machineIp=`ifconfig | grep inet | grep -v inet6 | grep -v 127 | sed 's/^[ \t]*//g' | cut -d ' ' -f2`
read -a mIps <<< $machineIp

# iip代表每一台机器需要匹配的ip
#
 for iip in ${ipArray[@]}
 do
    # ip代表本机ip
    for ip in ${mIps[@]}
    do
        if [[ $ip == $iip ]]; then
            # unique赋值
            unique=${iip}"_"${tenantId}
        fi
    done
done

if [[ $unique == "" ]]; then
    echo "error!!!"
    echo "The ip does not exist"
    exit
fi

param="unique="${unique}"&serverIp="${serverIp}"&protocol="${protocol}
if [ `ls ./ | grep syslog.sh | wc -l`  == 1 ]; then
    rm -f ./syslog.sh
fi
# 得到一个sh文件，再执行
curl -d ${param} ${url} -o syslog.sh
echo "Now is setting syslog.service. waitting..."
sh syslog.sh
echo "Now please manual restart service"


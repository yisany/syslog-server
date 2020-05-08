package com.yis.syslog.output.impl.file;

import com.alibaba.fastjson.JSON;
import com.yis.syslog.domain.OutputOptions;
import com.yis.syslog.output.Output;
import com.yis.syslog.comm.BizException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

/**
 * @author milu
 * @Description 输出到文件
 * @createTime 2019年07月10日 13:49:00
 */
public class FileOutput implements Output {

    private static final Logger logger = LogManager.getLogger(FileOutput.class);

    // 单次写入缓冲区大小: 40k
    public static final Integer BUFFER_SIZE = 1024 * 40;
    // 是否追加写入文件
    public static final boolean FILE_APPEND = true;
    // 系统换行符
    public static final String LINE_BREAK = System.getProperty("line.separator");

    private String lineBreak;
    private String dir;
    private FileOutputStream fos = null;
    private FileChannel oChannel = null;
    private ByteBuffer buf = null;
    private boolean isFirst;

    public FileOutput(OutputOptions.FileOption option) {
        this.dir = option.getPath();
        this.isFirst = true;
        this.lineBreak = LINE_BREAK;
    }


    @Override
    public void prepare() {

    }

    @Override
    public void release() {
        try {
            fos.close();
            oChannel.close();
        } catch (IOException e) {
            logger.error("FileOutput release error, e={}", e);
            throw new BizException("关闭File输出失败");
        }
    }

    @Override
    public void process(Map<String, Object> event) {
        String content = JSON.toJSONString(event);
        // fileChannel 写入
        appendLog(content);
    }

    private void appendLog(String content) {
        try {
            if (isFirst) {
                buf = ByteBuffer.allocate(BUFFER_SIZE);

                File fileTemp = new File(dir);
                // 判断父文件夹是否存在
                File parent = fileTemp.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                // 判断要写入文件是否存在
                if (!fileTemp.exists()) {
                    fileTemp.createNewFile();
                }

                fos = new FileOutputStream(dir, FILE_APPEND);

                isFirst = false;
            }

            oChannel = fos.getChannel();

            buf.put(content.getBytes());
            buf.put(lineBreak.getBytes());
            buf.flip();

            while (buf.hasRemaining()) {
                oChannel.write(buf);
            }

            // buf压缩
            buf.compact();
        } catch (IOException e) {
            logger.error("FileOutput appendLog error, e={}", e);
        }
    }

}

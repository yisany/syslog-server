package org.yis.export.ipml;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yis.comm.Config;
import org.yis.domain.Const;
import org.yis.export.Caller;
import org.yis.export.Export;
import org.yis.util.BizException;

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
public class FileExport implements Export {

    private static final Logger logger = LogManager.getLogger(FileExport.class);

    private String lineBreak;
    private String dir;
    private FileOutputStream fos = null;
    private FileChannel oChannel = null;
    private ByteBuffer buf = null;
    private boolean isFirst;

    public FileExport() {
        init();
    }

    @Override
    public void init() {
        this.dir = Config.path;
        this.isFirst = true;
        this.lineBreak = Const.LINE_BREAK;
    }

    @Override
    public void send(Caller caller) {
        Map<String, Object> event = caller.convert();
        String content = JSON.toJSONString(event);
        // fileChannel 写入
        appendLog(content);
    }

    @Override
    public void release() {
        try {
            fos.close();
            oChannel.close();
        } catch (IOException e) {
            logger.error("FileExport release error, e={}", e);
            throw new BizException("关闭File输出失败");
        }
    }

    private void appendLog(String content) {
        try {
            if (isFirst) {
                buf = ByteBuffer.allocate(Const.BUFFER_SIZE);

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

                fos = new FileOutputStream(dir, Const.FILE_APPEND);

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
            logger.error("FileExport appendLog error, e={}", e);
        }
    }

}

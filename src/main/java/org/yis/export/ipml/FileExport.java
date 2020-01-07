package org.yis.export.ipml;

import com.alibaba.fastjson.JSON;
import org.yis.comm.Config;
import org.yis.comm.Const;
import org.yis.export.Caller;
import org.yis.export.Export;
import org.yis.util.DateUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @author milu
 * @Description 输出到文件
 * @createTime 2019年07月10日 13:49:00
 */
public class FileExport implements Export {

    private String dir;

    public FileExport() {
        init();
    }

    @Override
    public void init() {
        this.dir = Config.path;
    }

    @Override
    public void send(Caller caller) {
        Map<String, Object> event = caller.convert();
        String str = JSON.toJSONString(event);
        // TODO 双缓冲区机制, 单位时间内缓冲区没满, 清空缓冲区写入文件
//        String file = String.format(Const.INDEX, dir, DateUtil.getDate());
//        File log = new File(file);
//        appendLog(log, str);
    }

    private void appendLog(File log, String str) {
        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            if (!log.exists()) {
                // 判断目录是否存在
                judgeDirExist(log);
                log.createNewFile();
                fw = new FileWriter(log);
            } else {
                fw = new FileWriter(log, true);
            }
            pw = new PrintWriter(fw);
            pw.println(str);
            pw.flush();
            Thread.sleep(500);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                fw.flush();
                pw.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void judgeDirExist(File log) {
        File parentDir = new File(log.getParent());
        if (!parentDir.exists()) {
            parentDir.mkdir();
        }
    }


}

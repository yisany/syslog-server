package org.yis.export;

import com.alibaba.fastjson.JSON;
import org.yis.entity.Const;
import org.yis.entity.MessageQueue;
import org.yis.util.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author milu
 * @Description 输出到文件
 * @createTime 2019年07月10日 13:49:00
 */
public class ExportFile {

    public void write2File(String path) {
        try {
            while (true) {
                String str = MessageQueue.getInstance().take().toString();
                String file = String.format(Const.INDEX, path, Utils.getDate());
                File log=new File(file);
                appendLog(log, str);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

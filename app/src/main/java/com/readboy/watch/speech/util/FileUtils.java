package com.readboy.watch.speech.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author oubin
 * @date 2016/11/4
 */

public class FileUtils {
    private static final String TAG = "FileUtils";

    private FileUtils() {
        throw new UnsupportedOperationException("u can't create me ...");
    }

    /**
     * @param file 文件夹
     */
    public static boolean createOrExistsDir(File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    public static boolean createNewFile(File file) {
        if (file == null) {
            return false;
        }
        if (file.exists() && !file.delete()) {
            Log.e(TAG, "createNewFile: file is exist and file can not delete.");
            return false;
        }
        if (!createOrExistsDir(file.getParentFile())) {
            Log.e(TAG, "createNewFile: can not create dir, dir = " + file.getAbsolutePath());
            return false;
        }

        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "createNewFile: e = " + e.toString());
            return false;
        }
    }

    public static boolean createNewFile(String fileName) {
        return createNewFile(new File(fileName));
    }

    public static boolean createNewFile(String dir, String fileName) {
        return createNewFile(new File(dir, fileName));
    }

    public static boolean writeFileFromIS(File file, InputStream is, boolean append) {
        if (file == null || is == null) {
            return false;
        }
        if (!createNewFile(file)) {
            return false;
        }
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, append));
            byte buffer[] = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeIO(is);
            closeIO(os);
        }
    }

    public static void closeIO(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void closeIO(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean copyAssets(Context context, String oldPath, String newPath) {
        Log.e(TAG, "copyAssets: start.newPath = " + newPath);
        // 获取assets目录下的所有文件及目录名
        String newTempPath = newPath + ".temp";
        File file = new File(newTempPath);
        //如果临时文件存在，删除
        if (file.exists() && file.isFile()) {
            if (!file.delete()) {
                return false;
            }
        }
        if (file.isDirectory()) {
            Log.e(TAG, "copyAssets: isDirectory.");
            // 如果是目录
            if (!file.mkdirs()) {
                return false;
            }
            // 如果文件夹不存在，则递归
            String[] fileNames = file.list();
            for (String fileName : fileNames) {
                copyAssets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
            }
        } else {
            // 如果是文件
            Log.e(TAG, "copyAssets: is file.");
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = context.getAssets().open(oldPath);
                if (!createOrExistsDir(file.getParentFile())) {
                    Log.e(TAG, "copyAssets: fail. can not create dir, dir = " + file.getParent());
                    return false;
                }
                fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {
                    // 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);
                    // 将读取的输入流写入到输出流
                }
                // 刷新缓冲区
                fos.flush();
                //复制完成才修改正确文件名
                file.renameTo(new File(newPath));
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "copyAssets: e: " + e.toString());
                return false;
            } finally {
                closeIO(is);
                closeIO(fos);
            }
        }
        Log.e(TAG, "copyAssets: end.");
        return true;
    }

    public static File pullXml(InputStream is, String fileName) throws IOException {
        int sumLength = is.available();
        BufferedInputStream bis = new BufferedInputStream(is);
        bis.mark(sumLength);
        byte[] bytes = new byte[512];
        int len = bis.read(bytes, 0, 256);
        String string = new String(bytes, 0, len);
        if (!string.contains("Success")) {
            //TODO 可能是服务器异常，
            Log.e(TAG, "pullXml: string = " + string);
            return null;
        }
        String mark = "</ResponseInfo>";
        int index = string.indexOf(mark);
        int textLength = string.substring(0, index + mark.length()).getBytes().length;
        File file = new File(fileName);
        bis.reset();
        int sum = textLength;
        while (sum > 0) {
            len = bis.read(bytes, 0, sum);
            sum = sum - len;
        }
        if (!writeFileFromIS(file, bis, false)) {
            Log.e(TAG, "pullXml: write file fail!");
        }
        closeIO(is);
        return file;
    }

    public static void appendLog(String filePath, String text, boolean addTime) {
        RandomAccessFile raf = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                createNewFile(file);
            }
            raf = new RandomAccessFile(filePath, "rw");
            long length = raf.length();
            Log.d(TAG, "addLog: length = " + length);
            raf.seek(length);
            raf.write("\r\n".getBytes());
            if (addTime) {
                String time = DateUtils.getCurDateString() + "  ";
                raf.write(time.getBytes());
            }
            raf.write(text.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "addLog: e = " + e.toString() + ", filePath = " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "addLog: e = " + e.toString() + ", filePath = " + filePath);
        } finally {
            closeIO(raf);
        }
    }
}

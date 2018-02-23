package com.readboy.watch.speech.util;

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

/**
 * @author oubin
 * @date 2016/11/4
 */

public class FileUtils {
    private static final String TAG = "FileUtils";

    private FileUtils() {
        throw new UnsupportedOperationException("u can't create me ...");
    }

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

    public static void addLog(String filePath, String text, boolean addTime) {
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
            raf.write("\n".getBytes());
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

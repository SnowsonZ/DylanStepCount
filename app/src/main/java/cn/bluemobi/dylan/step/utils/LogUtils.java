package cn.bluemobi.dylan.step.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.amap.api.maps.model.LatLng;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by snowson on 17-12-12.
 */

public class LogUtils {

    public static final String DEBUG_DIR = "debug_info";
    public static final String DEBUG_FILE_NAME = "debug.txt";

    public static void saveDebugInfoToLocal(Context context, String info, String targetDir, String fileName) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || context == null
                || TextUtils.isEmpty(info)) {
            return;
        }
        if (TextUtils.isEmpty(targetDir)) {
            targetDir = DEBUG_DIR;
        }
        if (TextUtils.isEmpty(fileName)) {
            fileName = DEBUG_FILE_NAME;
        }
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + targetDir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        new Thread(new FileThread(file, fileName, info)).start();
    }

    public static void saveLogcatToLocal(Context context, String targetDir) {

    }

    private static class FileThread implements Runnable {

        final File target_file;
        String info;

        public FileThread(File file, String fileName, String info) {
            this.info = info;
            target_file = new File(file.getAbsolutePath(), fileName);
        }

        @Override
        public void run() {
            synchronized (target_file) {
                FileOutputStream fos = null;
                BufferedOutputStream bos = null;
                try {
                    fos = new FileOutputStream(target_file, true);
                    bos = new BufferedOutputStream(fos);
                    byte[] bytes = info.getBytes(Charset.forName("UTF-8"));
                    bos.flush();
                    bos.write(bytes, 0, bytes.length);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        bos.close();
                        fos.close();
                        bos = null;
                        fos = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    public static StringBuffer textLogStyle(
            StringBuffer sb, LatLng curLatLng, LatLng lastLatLng, float distance) {
        if (sb == null || curLatLng == null || lastLatLng == null) {
            return null;
        }
        sb.append("Current Lat: " + curLatLng.latitude + ", Current Lon: " + curLatLng.longitude
                + "\n");
        sb.append("Last Lat: " + lastLatLng.latitude + ", Last Lon: " + lastLatLng.longitude
                + "\n");
        sb.append("本次行走距离: " + distance + "米\n");
        sb.append("----------------------------------------------------------------\n");

        return sb;
    }

    public static StringBuffer textError(StringBuffer sb, int errorCode, String errorMsg) {
        if (sb == null) {
            return null;
        }
        sb.append("ErrorCode: " + errorCode + ", Error: " + errorMsg + "\n");
        sb.append("----------------------------------------------------------------\n");

        return sb;
    }

    public static StringBuffer locationIgnore(StringBuffer sb) {
        if (sb == null) {
            return null;
        }
        sb.append("网络定位请求低于1秒、或两次定位之间设备位置变化非常小时返回，设备位移通过传感器感知\n");
        sb.append("----------------------------------------------------------------\n");

        return sb;
    }
}

package cn.bluemobi.dylan.step.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by snowson on 17-12-11.
 */

public class AssetsCopy {

    public static void copyAssets(Context context, String assetsPath, String targetPath) {
        if (TextUtils.isEmpty(targetPath) || context == null) {
            return;
        } else if (targetPath.endsWith("/")) {
            targetPath = targetPath.substring(0, targetPath.length() - 1);
        }
        if (TextUtils.isEmpty(assetsPath) || assetsPath.equals("/")) {
            assetsPath = "";
        } else if (assetsPath.endsWith("/")) {
            assetsPath = assetsPath.substring(0, assetsPath.length() - 1);
        }

        AssetManager am = context.getAssets();
        try {
            String[] files = am.list(assetsPath);
            if (files.length > 0) {
                for (String fileName : files) {
                    if (!TextUtils.isEmpty(fileName)) {
                        fileName = assetsPath + File.separator + fileName;
                        String[] childFiles = am.list(fileName);
                        if (fileName.length() > 0) {
                            copyAssets(context, fileName, targetPath);
                        } else {
                            InputStream is = am.open(fileName);
                            writeFile(targetPath + File.separator + fileName, is);
                        }
                    }
                }
            } else {
                InputStream is = am.open("/" + assetsPath);
                writeFile(targetPath + File.separator + assetsPath, is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean writeFile(String fileName, InputStream in) throws IOException {
        boolean bRet = true;
        try {
            OutputStream os = new FileOutputStream(fileName);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            in.close();
            in = null;
            os.flush();
            os.close();
            os = null;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            bRet = false;
        }
        return bRet;
    }
}

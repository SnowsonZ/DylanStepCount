package cn.bluemobi.dylan.step.app;

import android.app.Application;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import java.io.File;

import cn.bluemobi.dylan.step.utils.AssetsCopy;

/**
 * Created by yuandl on 2016-10-18.
 */

public class MyApplication extends Application {

    private WakeLock mWakeLock;
    public static final String MAP_THEME_DATA = "map_theme/config_theme.data";

    @Override
    public void onCreate() {
        super.onCreate();
        acquireSystemLock();
        AssetsCopy.copyAssets(this, MAP_THEME_DATA,
                getFilesDir().getPath() + File.separator + MAP_THEME_DATA);
    }

    /**
     * 获取电源锁，避免部分机型锁屏后传感器和定位失效
     */
    private void acquireSystemLock() {
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
            mWakeLock.acquire();
        }
    }

    public void releaseSystemLock() {
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
}

package cn.bluemobi.dylan.step.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by snowson on 17-12-8.
 */

public class LocationUpdateService extends Service implements AMapLocationListener {

    private AMapLocationClient mLocationClient;
    private OnUpdateLocationInfoListener mListener;
    private List<LatLng> positions = null;
    private int index = 0;
    private LocationUpdateService instance;

    public class LocationBinder extends Binder {
        public LocationUpdateService getService() {
            return getInstance();
        }
    }

    private LocationUpdateService getInstance() {
        if (instance == null) {
            instance = this;
        }
        return instance;
    }

    public interface OnUpdateLocationInfoListener {
        boolean onUpdate(List<LatLng> locations);
    }

    public void setOnUpdateLocationListener(OnUpdateLocationInfoListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocationBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
            LatLng curPosition = new LatLng(aMapLocation.getLatitude() + 0.001 * index++, aMapLocation.getLongitude() + 0.001 * index++);
            if (positions == null) {
                positions = new ArrayList<LatLng>();
            }
            positions.add(curPosition);
            //累计点数大于5个后开始绘制该段路径
            if (positions.size() >= 5 && mListener != null) {
                if (mListener.onUpdate(positions)) {
                    positions.clear();
                    positions.add(curPosition);
                }
            }
        } else {
            Toast.makeText(this, "当前GPS信号弱,轨迹精确度可能会下降！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationClient = new AMapLocationClient(this);
        mLocationClient.setLocationOption(getLocationOption());
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();
    }

    private AMapLocationClientOption getLocationOption() {
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

        return option;
    }
}

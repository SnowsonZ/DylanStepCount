package cn.bluemobi.dylan.step.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceLocation;
import com.amap.api.trace.TraceStatusListener;

import java.util.ArrayList;
import java.util.List;

import cn.bluemobi.dylan.step.R;

public class MapActivity extends Activity implements TraceStatusListener {

    private MapView mMapContent;
    private AMap mAMap;
    private static final int LOCATION_PERMISSION_CODE = 1;
    private String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = null;

    private List<LatLng> positions = null;
    private boolean isSet = false;
    private MyLocationStyle myLocationStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mMapContent = (MapView) findViewById(R.id.map_content);
        //创建地图
        mMapContent.onCreate(savedInstanceState);
        if (mAMap == null) {
            mAMap = mMapContent.getMap();
            mAMap.getUiSettings().setRotateGesturesEnabled(false);
            mAMap.getUiSettings().setZoomControlsEnabled(false);
            mAMap.setMapCustomEnable(true);
            mAMap.setCustomMapStylePath("/sdcard/config_theme.data");
            mAMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(final Location location) {
                    //设置地图初始zoomlevel和中心点
                    if (isSet) {
                        return;
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                    new CameraPosition.Builder().zoom(15).target(new LatLng(
                                            location.getLatitude(), location.getLongitude()
                                    )).build()
                            ));
                            isSet = true;
                        }
                    }, 50);
                }
            });
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
            } else {
                locateCurrent();
            }
        }
        initLocationConfig();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            locateCurrent();
        }
    }

    private void locateCurrent() {
        // 自定义定位蓝点图标
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(
                BitmapDescriptorFactory.fromResource(R.drawable.gps_point));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
        // 自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(0);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
        //设置首次定位模式
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
        // 将自定义的 myLocationStyle 对象添加到地图上
        myLocationStyle.showMyLocation(true);
        mAMap.setMyLocationStyle(myLocationStyle);
        mAMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种

    }

    private AMapLocationClientOption getLocationOption() {
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

        return option;
    }

    private void initLocationConfig() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationOption(getLocationOption());
        mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                    LatLng curPosition = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                    if (positions == null) {
                        positions = new ArrayList<LatLng>();
                    }
                    positions.add(curPosition);
                    //累计点数大于5个后开始绘制该段路径
                    if (positions.size() >= 5) {
                        Polyline polyline = mAMap.addPolyline(new PolylineOptions().
                                addAll(positions).width(14).color(Color.argb(255, 1, 1, 1)));
                        if (polyline != null) {
                            positions.clear();
                            positions.add(curPosition);
                        }
                    }
                } else {
                    Toast.makeText(MapActivity.this, "当前GPS信号弱,轨迹精确度可能会下降！", Toast.LENGTH_SHORT).show();
                }
            }
        };
        mLocationClient.setLocationListener(mLocationListener);
        mLocationClient.startLocation();

        if (lbsTraceClient == null) {
            lbsTraceClient = LBSTraceClient.getInstance(this);
        }
        lbsTraceClient.startTrace(this);
    }

    private LBSTraceClient lbsTraceClient = null;

    @Override
    protected void onResume() {
        super.onResume();
        mMapContent.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapContent.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapContent.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapContent.onDestroy();
    }

    @Override
    public void onTraceStatus(List<TraceLocation> list, List<LatLng> list1, String s) {
        if (list1 != null && !s.equalsIgnoreCase("引擎返回数据异常")) {
            Toast.makeText(this, "轨迹纠偏成功", Toast.LENGTH_SHORT).show();
        }
    }
}

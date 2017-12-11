package cn.bluemobi.dylan.step.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.List;

import cn.bluemobi.dylan.step.R;
import cn.bluemobi.dylan.step.app.MyApplication;
import cn.bluemobi.dylan.step.fragment.StepFragment;
import cn.bluemobi.dylan.step.step.UpdateUiCallBack;
import cn.bluemobi.dylan.step.step.service.StepService;
import cn.bluemobi.dylan.step.utils.trancesmooth.PathSmoothTool;

/**
 * 记步主页
 */
public class MainActivity extends FragmentActivity {
    private SupportMapFragment mapFragment;
    private StepFragment stepFragment;
    private AMap mAMap;
    private boolean isSet;
    private MyLocationStyle myLocationStyle;
    private MyApplication mAppContext;
    private Intent intent_service;

    private static int RES_READY = 1;
    private SoftReference<MainActivity> mContext;
    private static final int LOCATION_PERMISSION_CODE = 1;
    private String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        mAppContext = (MyApplication) getApplicationContext();
        mContext = new SoftReference<MainActivity>(this);
        initFragment();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
            } else {
                initMap();
                locateCurrent();
                setupService();
            }
        } else {
            initMap();
            locateCurrent();
            setupService();
        }
    }

    private AMapOptions getMapOptions() {
        AMapOptions options = new AMapOptions();
        options.rotateGesturesEnabled(false);
        options.zoomControlsEnabled(false);

        return options;
    }

    private void initFragment() {
        mapFragment = SupportMapFragment.newInstance(getMapOptions());
        stepFragment = new StepFragment();
        try {
            MapsInitializer.initialize(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        getSupportFragmentManager().beginTransaction().add(R.id.layout_content, mapFragment,
                mapFragment.getClass().getName()).add(R.id.layout_content, stepFragment,
                stepFragment.getClass().getName()).show(stepFragment).commit();
    }

    private void initMap() {
        if (mAMap == null) {
            mAMap = mapFragment.getMap();
        }
        mAMap.getUiSettings().setRotateGesturesEnabled(false);
        mAMap.getUiSettings().setZoomControlsEnabled(false);
        mAMap.setMapCustomEnable(true);
        mAMap.setCustomMapStylePath(getFilesDir().getPath() + File.separator + MyApplication.MAP_THEME_DATA);
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
//        myLocationStyle.radiusFillColor(Color.argb(88, 24, 172, 255));
        //设置首次定位模式
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
        // 将自定义的 myLocationStyle 对象添加到地图上
        myLocationStyle.showMyLocation(true);
        mAMap.setMyLocationStyle(myLocationStyle);
        mAMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
    }

    private boolean isBind = false;

    /**
     * 开启计步服务
     */
    private void setupService() {
        intent_service = new Intent(this, StepService.class);
        isBind = bindService(intent_service, conn, Context.BIND_AUTO_CREATE);
        startService(intent_service);
    }

    /**
     * 用于查询应用服务（application Service）的状态的一种interface，
     * 更详细的信息可以参考Service 和 context.bindService()中的描述，
     * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
     */
    ServiceConnection conn = new ServiceConnection() {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         * @param name 实际所连接到的Service组件名称
         * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StepService stepService = ((StepService.StepBinder) service).getService();
            //设置初始化数据
            if (stepFragment != null) {
                stepFragment.setStepCount(stepService.getStepCount());
            }

            //设置步数监听回调
            stepService.registerCallback(new UpdateUiCallBack() {
                @Override
                public void updateUi(int stepCount) {
                    if (stepFragment != null) {
                        stepFragment.setStepCount(stepCount);
                    }
                }

                @Override
                public boolean onUpdate(List<LatLng> locations) {
                    if (locations != null && locations.size() > 0) {
                        if (stepFragment != null) {
                            stepFragment.setWaringVisiable(View.GONE);
                        }

//                        PathSmoothTool pst = new PathSmoothTool();
//                        pst.setIntensity(4);
//                        List<LatLng> optimizeList = pst.pathOptimize(locations);

                        PolylineOptions po = new PolylineOptions();
                        po.addAll(locations);
                        po.width(40);
                        //设置纹理
                        po.setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.map_alr));
                        Polyline polyline = mAMap.addPolyline(po);
                        if (polyline != null) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void onLocationSignalWeak() {
                    if (stepFragment != null) {
                        stepFragment.setWaringVisiable(View.VISIBLE);
                    }
                }
            });
        }

        /**
         * 当与Service之间的连接丢失的时候会调用该方法，
         * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
         * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
         * @param name 丢失连接的组件名称
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onBackPressed() {
        if (curFragment == mapFragment) {
            changeToStepPage();
            return;
        } else {
            //进入后台，返回桌面
            moveTaskToBack(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBind) {
            this.unbindService(conn);
            stopService(intent_service);
        }
        if (mAppContext != null) {
            mAppContext.releaseSystemLock();
        }
    }

    private Fragment curFragment = stepFragment;

    public void changeToTargetPage() {
        getSupportFragmentManager().beginTransaction().show(mapFragment).hide(stepFragment).commit();
        curFragment = mapFragment;
    }

    private void changeToStepPage() {
        getSupportFragmentManager().beginTransaction().show(stepFragment).hide(mapFragment).commit();
        curFragment = stepFragment;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initMap();
        locateCurrent();
        setupService();
    }
}

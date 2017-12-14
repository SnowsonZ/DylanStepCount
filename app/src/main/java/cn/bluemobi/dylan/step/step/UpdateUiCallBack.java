package cn.bluemobi.dylan.step.step;

import com.amap.api.maps.model.LatLng;

import java.util.List;

/**
 * 步数更新回调
 * Created by dylan on 16/9/27.
 */
public interface UpdateUiCallBack {
    /**
     * 更新UI步数
     *
     * @param stepCount 步数
     */
    void updateUi(int stepCount);

    boolean onUpdate(List<LatLng> locations);

    void onLocationSignalWeak(String waringMsg);
}

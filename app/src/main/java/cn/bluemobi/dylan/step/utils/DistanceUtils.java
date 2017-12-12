package cn.bluemobi.dylan.step.utils;

import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.DPoint;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

/**
 * Created by snowson on 17-12-12.
 */

public class DistanceUtils {

    public static float caculateDistance(LatLng startP, LatLng endP) {
        if (startP == null || endP == null) {
            return -1f;
        }
        return AMapUtils.calculateLineDistance(startP, endP);
    }

    public static boolean isGaoDeUsed(DPoint point) {

        return CoordinateConverter.isAMapDataAvailable(point.getLatitude(), point.getLongitude());
    }
}

package cn.bluemobi.dylan.step.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.bluemobi.dylan.step.R;
import cn.bluemobi.dylan.step.activity.MainActivity;
import cn.bluemobi.dylan.step.step.utils.SharedPreferencesUtils;
import cn.bluemobi.dylan.step.view.CircleImageView;
import cn.bluemobi.dylan.step.view.StepArcView;

/**
 * Created by snowson on 17-12-8.
 */

public class StepFragment extends Fragment {

    private StepArcView cc;
    private SharedPreferencesUtils sp;
    private CircleImageView iv_show_map;
    private TextView tv_isSupport, tv_waring;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewContent = inflater.inflate(R.layout.fragment_step, null);
        cc = (StepArcView) viewContent.findViewById(R.id.cc);
        tv_isSupport = (TextView) viewContent.findViewById(R.id.tv_isSupport);
        iv_show_map = (CircleImageView) viewContent.findViewById(R.id.iv_show_map);
        tv_waring = (TextView) viewContent.findViewById(R.id.tv_info);
        initData();
        initListener();
        return viewContent;
    }

    private void initListener() {
        iv_show_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).changeToTargetPage();
                }
            }
        });
    }

    private void initData() {
        sp = new SharedPreferencesUtils(getActivity());
        //获取用户设置的计划锻炼步数，没有设置过的话默认7000
        String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "7000");
        //设置当前步数为0
        cc.setCurrentCount(Integer.parseInt(planWalk_QTY), 0);
        tv_isSupport.setText("计步中...");
    }

    public void setStepCount(int stepCount) {
        String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "7000");
        cc.setCurrentCount(Integer.parseInt(planWalk_QTY), stepCount);
    }

    public void setWaringVisiable(int visiable) {
        tv_waring.setVisibility(visiable);
    }
}

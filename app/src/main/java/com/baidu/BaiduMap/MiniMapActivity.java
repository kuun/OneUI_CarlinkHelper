package com.baidu.BaiduMap;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;

import com.baidu.BaiduMap.ConstantData.CarLinkData;
import com.baidu.BaiduMap.Utils.DiagLog;
import com.baidu.BaiduMap.Utils.ApplicationUtil;
import com.baidu.BaiduMap.carlifeapplauncher.adapter.FakeStart;
import com.baidu.BaiduMap.carlifeapplauncher.adapter.OpenProvider;
import com.baidu.BaiduMap.deeplink.DeepLinkService;

import java.util.Objects;

public class MiniMapActivity extends AppCompatActivity {
    private static MiniMapActivity instance;

    //优先返回地图小窗(CarLifeMiniDisplay)上的Context，使地图启动到小窗所在的虚拟屏，
    //而不是悬浮窗所在的整屏，否则高德会按整屏宽度渲染并被左右覆盖层挡住
    public static Context launchContext(Context fallback) {
        MiniMapActivity a = instance;
        if (a == null || a.isFinishing() || a.isDestroyed()) {
            return fallback;
        }
        Display display = a.getDisplay();
        if (display == null || !display.isValid() || display.getDisplayId() == Display.DEFAULT_DISPLAY) {
            return fallback;
        }
        return a;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DiagLog.log(this, "MiniMap.onCreate");
        instance = this;
        //setContentView(R.layout.activity_mini_map);
        ApplicationUtil.initAppList(this);
        ApplicationUtil.initSettings(this);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onResume() {
        super.onResume();
        DiagLog.log(this, "MiniMap.onResume intent=" + getIntent());
        if(Objects.equals(CarLinkData.getStringFromList(this, CarLinkData.sp_dock_map_pkg), CarLinkData.pkgNameAmap)){
            FakeStart.StartMiniMap(this);
        }else {
            FakeStart.StartCommon(this,CarLinkData.getStringFromList(this,CarLinkData.sp_dock_map_pkg));
        }



        if (OpenProvider.isConnected(this)) {
            if (!DeepLinkService.isAlive()) {
                //断连后残留的实例(已销毁/被迁到主屏/虚拟屏已失效)先清掉，再重新拉起
                DeepLinkService.release();
                String uri = "banqumusic://deeplinkservice/";
                Uri parse = Uri.parse("samsungcarlink://link/----" + uri);
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(parse);
                intent.addFlags(0x10104000);
                startActivity(intent);
            }else {
                DeepLinkService.getInstance().resume();
            }
        }
    }

    @Override
    protected void onDestroy() {
        DiagLog.log(this, "MiniMap.onDestroy");
        if (instance == this) {
            instance = null;
        }
        super.onDestroy();
    }

}

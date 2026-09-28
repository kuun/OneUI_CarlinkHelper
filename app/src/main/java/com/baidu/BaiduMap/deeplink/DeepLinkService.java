package com.baidu.BaiduMap.deeplink;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.view.Display;

import com.baidu.BaiduMap.ConstantData.CarLinkData;
import com.baidu.BaiduMap.Utils.DiagLog;
import com.baidu.BaiduMap.R;
import com.baidu.BaiduMap.Utils.PreferenceUtil;
import com.baidu.BaiduMap.Utils.ToastUtil;
import com.baidu.BaiduMap.Widget.FloatView.NavBar;
import com.baidu.BaiduMap.Widget.FloatView.WidgetRight;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Objects;

public class DeepLinkService extends AppCompatActivity {
    public static DeepLinkService deepLinkService;
    private Context mContex;
    //悬浮窗所在的虚拟屏id，用于判断重连后悬浮窗是否已失效
    private int overlayDisplayId = Display.INVALID_DISPLAY;
    //悬浮窗已被release()清理，onDestroy不再重复清理(避免误删新实例的悬浮窗)
    private boolean released = false;
    private final DisplayManager.DisplayListener displayListener = new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayAdded(int displayId) {
        }

        @Override
        public void onDisplayRemoved(int displayId) {
            if (displayId == overlayDisplayId && deepLinkService == DeepLinkService.this) {
                release();
            }
        }

        @Override
        public void onDisplayChanged(int displayId) {
        }
    };

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DiagLog.log(this, "DeepLinkService.onCreate");
        //setContentView(R.layout.activity_mini_map);
        deepLinkService = this;
        mContex = this;
        getSystemService(DisplayManager.class).registerDisplayListener(displayListener, null);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onResume() {
        super.onResume();
        DiagLog.log(this, "DeepLinkService.onResume intent=" + getIntent());
        resume();
        Intent i = new Intent("carlife.intent.action.openpage");
        i.setClassName("com.baidu.carlife", "com.baidu.carlife.CarlifeActivity");
        i.putExtra("pageid", 1);
        i.addFlags(0x10104000);
        startActivity(i);
    }

    public static DeepLinkService getInstance() {
        return deepLinkService;
    }

    //实例存在、未销毁，且仍在有效的车机虚拟屏上
    public static boolean isAlive() {
        DeepLinkService s = deepLinkService;
        if (s == null || s.isFinishing() || s.isDestroyed()) {
            return false;
        }
        Display display = s.getDisplay();
        return display != null && display.isValid() && display.getDisplayId() != Display.DEFAULT_DISPLAY;
    }

    //清理失效的实例及其悬浮窗
    public static void release() {
        DeepLinkService s = deepLinkService;
        if (s == null) {
            return;
        }
        s.destroyAllService();
        s.released = true;
        deepLinkService = null;
        if (!s.isFinishing() && !s.isDestroyed()) {
            s.finish();
        }
    }

    public void resume() {
        Display display = getDisplay();
        if (display == null || display.getDisplayId() == Display.DEFAULT_DISPLAY) {
            return;
        }
        //重连后虚拟屏id会变，旧屏上的悬浮窗已被系统移除，需销毁后重建
        if (overlayDisplayId != display.getDisplayId()) {
            destroyAllService();
        }
        XXPermissions.with(this).permission(Permission.SYSTEM_ALERT_WINDOW).permission(Permission.BIND_NOTIFICATION_LISTENER_SERVICE).permission(Permission.READ_PHONE_STATE).permission(Permission.ACCESS_COARSE_LOCATION).permission(Permission.ACCESS_FINE_LOCATION).request(new OnPermissionCallback() {
            @Override
            public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                overlayDisplayId = Objects.requireNonNull(mContex.getDisplay()).getDisplayId();
                if (NavBar.getInstance() == null && Objects.requireNonNull(mContex.getDisplay()).getDisplayId() != Display.DEFAULT_DISPLAY&& CarLinkData.getBoolean(mContex,CarLinkData.sp_enable_overlay,true)) {
                    NavBar.createInstance(mContex).onStart();
                }
                if (WidgetRight.getInstance() == null && Objects.requireNonNull(mContex.getDisplay()).getDisplayId() != Display.DEFAULT_DISPLAY&& CarLinkData.getBoolean(mContex,CarLinkData.sp_enable_overlay_right,true)) {
                    WidgetRight.createInstance(mContex).onStart();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        DiagLog.log(this, "DeepLinkService.onDestroy");
        getSystemService(DisplayManager.class).unregisterDisplayListener(displayListener);
        if (!released) {
            destroyAllService();
        }
        if (deepLinkService == this) {
            deepLinkService = null;
        }
        super.onDestroy();
    }

    public void destroyAllService() {
        if (NavBar.getInstance() != null) {
            NavBar.getInstance().onDestroy();
        }
        if (WidgetRight.getInstance() != null) {
            WidgetRight.getInstance().onDestroy();
        }
        overlayDisplayId = Display.INVALID_DISPLAY;
    }
}
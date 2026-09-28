package com.baidu.BaiduMap.Widget.FloatView;

import android.content.Context;
import android.graphics.Rect;
import android.view.WindowManager;

import com.baidu.BaiduMap.ConstantData.CarLinkData;

/**
 * 按Carlife三星版主页的布局规则计算覆盖层尺寸，使左右覆盖窗口与中间的地图卡片严丝合缝:
 * Dock宽80dp，剩余宽度 地图卡片:右侧卡片列 = 3:2，地图卡片四周1dp边距，
 * 卡片列右/下12dp边距，导航卡片:音乐卡片 = 0.3:0.7，间隔12dp
 */
public class OverlayMetrics {
    private static final float DOCK_WIDTH_DP = 80f;
    private static final float MAP_MARGIN_DP = 1f;
    private static final float CARD_MARGIN_DP = 12f;
    public static final float NAVI_CARD_WEIGHT = 0.3f;
    public static final float MUSIC_CARD_WEIGHT = 0.7f;
    //原作者的像素值是在density=2.0的虚拟屏上调的
    private static final float LEGACY_DENSITY = 2f;

    public final int screenWidth;
    public final int screenHeight;
    public final float density;
    public final int dockWidth;
    public final int rightWidth;
    public final int cardMargin;

    private OverlayMetrics(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Rect bounds = wm.getCurrentWindowMetrics().getBounds();
        screenWidth = bounds.width();
        screenHeight = bounds.height();
        density = context.getResources().getDisplayMetrics().density;
        cardMargin = Math.round(CARD_MARGIN_DP * density);

        int autoDock = Math.round(DOCK_WIDTH_DP * density);
        float weighted = screenWidth - autoDock - (2 * MAP_MARGIN_DP + CARD_MARGIN_DP) * density;
        int mapRight = autoDock + (int) Math.floor(weighted * 3 / 5 + 2 * MAP_MARGIN_DP * density);
        int autoRight = screenWidth - mapRight;

        if (CarLinkData.getBoolean(context, CarLinkData.sp_overlay_auto_size, true)) {
            dockWidth = autoDock;
            rightWidth = autoRight;
        } else {
            dockWidth = CarLinkData.getIntFromString(context, CarLinkData.sp_overlay_left_width, autoDock);
            rightWidth = CarLinkData.getIntFromString(context, CarLinkData.sp_overlay_right_width, autoRight);
        }
    }

    public static OverlayMetrics from(Context context) {
        return new OverlayMetrics(context);
    }

    //把原来按density=2.0写死的像素值换算到当前虚拟屏
    public int scale(int legacyPx) {
        return Math.round(legacyPx * density / LEGACY_DENSITY);
    }
}

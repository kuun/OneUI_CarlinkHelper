package com.baidu.BaiduMap.Utils;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 诊断日志: 记录各Activity生命周期与地图启动时所在的虚拟屏，用于排查小窗/大屏地图错位。
 * 同时写logcat(tag=CLHelper)与 Android/data/包名/files/diag.log (adb pull可取)
 */
public class DiagLog {
    private static final String TAG = "CLHelper";
    private static final long MAX_SIZE = 512 * 1024;

    public static void log(Context context, String event) {
        String line = event + " | " + describeDisplay(context);
        Log.i(TAG, line);
        try {
            File dir = context.getExternalFilesDir(null);
            if (dir == null) {
                return;
            }
            File file = new File(dir, "diag.log");
            boolean append = file.length() < MAX_SIZE;
            try (FileWriter writer = new FileWriter(file, append)) {
                writer.write(new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US).format(new Date()) + " " + line + "\n");
            }
        } catch (Exception ignored) {
        }
    }

    private static String describeDisplay(Context context) {
        try {
            Display display = context.getDisplay();
            if (display == null) {
                return "display=null";
            }
            Point size = new Point();
            display.getRealSize(size);
            DisplayMetrics dm = new DisplayMetrics();
            display.getRealMetrics(dm);
            return "display=" + display.getDisplayId() + "(" + display.getName() + ") " + size.x + "x" + size.y + " dpi=" + dm.densityDpi + " valid=" + display.isValid();
        } catch (Exception e) {
            return "display=? " + e;
        }
    }
}

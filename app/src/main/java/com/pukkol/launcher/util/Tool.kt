package com.pukkol.launcher.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.*
import android.util.TypedValue
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.pukkol.launcher.Setup
import com.pukkol.launcher.data.model.App
import java.util.*

object Tool {
    /**
     * Filtering followers by search char sequence
     * @param list source follower list
     * @param charString searching char sequence
     * @return filtered follower list
     */
    fun searchAppsFilter(list: List<App?>, charString: String): ArrayList<App> {
        val filteredTempList = ArrayList<App>()
        for (app in list) {
            if (app != null) {
                // Filter by user name and user id
                if (containsIgnoreCase(app.label, charString)
                        || containsIgnoreCase(app.packageName, charString)) {
                    filteredTempList.add(app)
                }
            }
        }
        return filteredTempList
    }

    /**
     * Search if substring has char sequence in source string ignore case
     * @param src source string
     * @param charString substring for searching
     * @return true if has coincidence
     */
    fun containsIgnoreCase(src: String, charString: String): Boolean {
        val length = charString.length
        if (length == 0) {
            return true
        }
        for (i in src.length - length downTo 0) {
            if (src.regionMatches(i, charString, 0, length, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    fun visibleViews(duration: Long, vararg views: View?) {
        if (views == null) return
        for (view in views) {
            if (view == null) continue
            view.animate().alpha(1f).setDuration(duration).setInterpolator(AccelerateDecelerateInterpolator()).withStartAction { view.visibility = View.VISIBLE }
        }
    }

    fun invisibleViews(duration: Long, vararg views: View?) {
        if (views == null) return
        for (view in views) {
            if (view == null) continue
            view.animate().alpha(0f).setDuration(duration).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction { view.visibility = View.INVISIBLE }
        }
    }

    fun goneViews(duration: Long, vararg views: View?) {
        if (views == null) return
        for (view in views) {
            if (view == null) continue
            view.animate().alpha(0f).setDuration(duration).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction { view.visibility = View.GONE }
        }
    }

    fun viewToBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(
                view.width,
                view.height,
                Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun createScaleInScaleOutAnim(view: View, followedAction: () -> Unit) {
        val animTime: Int = Setup.deviceSettings().animationSpeed * 4
        val animateScaleIn = view.animate().scaleX(0.85f).scaleY(0.85f).setDuration(animTime.toLong())
        animateScaleIn.interpolator = AccelerateDecelerateInterpolator()
        Handler().postDelayed({
            val animateScaleOut = view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(animTime.toLong())
            animateScaleOut.interpolator = AccelerateDecelerateInterpolator()
            Handler().postDelayed(followedAction , animTime.toLong())
        }, animTime.toLong())
    }

    fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun clampInt(target: Int, min: Int, max: Int): Int {
        return Math.max(min, Math.min(max, target))
    }

    fun clampFloat(target: Float, min: Float, max: Float): Float {
        return Math.max(min, Math.min(max, target))
    }

    fun getAverageColorValue(color: Color): Int {
        val r = color.toArgb() shr 16 and 0xFF
        val g = color.toArgb() shr 8 and 0xFF
        val b = color.toArgb() and 0xFF
        return (r + g + b) / 3
    }

    fun getOppositeColor(color: Color): Color {
        val value = getAverageColorValue(color)
        return if (value > 125) {
            Color.valueOf(0f, 0f, 0f)
        } else Color.valueOf(255f, 255f, 255f)
    }

    fun getIntOppositeColor(color: Color): Int {
        val value = getAverageColorValue(color)
        return if (value > 125) {
            Color.BLACK
        } else Color.WHITE
    }

    fun getIntColor(color: Color): Int {
        val r = color.toArgb() shr 16 and 0xFF
        val g = color.toArgb() shr 8 and 0xFF
        val b = color.toArgb() and 0xFF
        return Color.argb(255, r, g, b)
    }

    fun averageBitmapColor(bitmap: Bitmap): Int {
        var redColors: Long = 0
        var greenColors: Long = 0
        var blueColors: Long = 0
        var pixelCount: Long = 0
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                pixelCount++
                redColors += Color.red(pixel).toLong()
                greenColors += Color.green(pixel).toLong()
                blueColors += Color.blue(pixel).toLong()
            }
        }
        // calculate average of bitmap r,g,b values
        val red = (redColors / pixelCount).toInt()
        val green = (greenColors / pixelCount).toInt()
        val blue = (blueColors / pixelCount).toInt()
        return Color.argb(255, red, green, blue)
    }

    fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable
            if (bitmapDrawable.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }
        val bitmap: Bitmap
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            // single color bitmap will be created
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    fun resizeBitmap(source: Bitmap, width: Int, height: Int): Bitmap {
        var source = source
        if (source.height == height && source.width == width) return source
        val biggestValue = Math.min(width, height)
        val sourceWidth = source.width
        val sourceHeight = source.height
        source = source.copy(source.config, true)
        return if (sourceHeight <= sourceWidth) {
            if (sourceHeight <= biggestValue) { // if image already smaller than the required height
                return Bitmap.createScaledBitmap(source, sourceWidth, sourceHeight, true)
            }
            val aspectRatio = sourceWidth.toDouble() / sourceHeight.toDouble()
            val targetWidth = (biggestValue * aspectRatio).toInt()
            Bitmap.createScaledBitmap(source, targetWidth, biggestValue, true)
        } else {
            if (sourceWidth <= biggestValue) { // if image already smaller than the required width
                return Bitmap.createScaledBitmap(source, sourceWidth, sourceHeight, true)
            }
            val aspectRatio = sourceHeight.toDouble() / sourceWidth.toDouble()
            val targetHeight = (biggestValue * aspectRatio).toInt()
            Bitmap.createScaledBitmap(source, biggestValue, targetHeight, true)
        }
    }

    fun setDrawableColor(context: Context?, icon: Drawable?, becomeAttrId: Int) {
        if (context == null || icon == null) return
        val color = TypedValue()
        context.theme.resolveAttribute(becomeAttrId, color, true)
        val colorValue = ContextCompat.getColor(context, color.resourceId)
        icon.mutate().setColorFilter(colorValue, PorterDuff.Mode.SRC_IN)
    }

    fun RoundedRect(width: Float, height: Float, radius: Float): Path {
        return RoundedRect(0f, 0f, width, height, radius, radius, true, true, true, true)
    }

    fun RoundedRect(left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float, tl: Boolean, tr: Boolean, br: Boolean, bl: Boolean): Path {
        var rx = rx
        var ry = ry
        val path = Path()
        if (rx < 0) rx = 0f
        if (ry < 0) ry = 0f
        val width = right - left
        val height = bottom - top
        if (rx > width / 2) rx = width / 2
        if (ry > height / 2) ry = height / 2
        val widthMinusCorners = width - 2 * rx
        val heightMinusCorners = height - 2 * ry
        path.moveTo(right, top + ry)
        if (tr) path.rQuadTo(0f, -ry, -rx, -ry) //top-right corner
        else {
            path.rLineTo(0f, -ry)
            path.rLineTo(-rx, 0f)
        }
        path.rLineTo(-widthMinusCorners, 0f)
        if (tl) path.rQuadTo(-rx, 0f, -rx, ry) //top-left corner
        else {
            path.rLineTo(-rx, 0f)
            path.rLineTo(0f, ry)
        }
        path.rLineTo(0f, heightMinusCorners)
        if (bl) path.rQuadTo(0f, ry, rx, ry) //bottom-left corner
        else {
            path.rLineTo(0f, ry)
            path.rLineTo(rx, 0f)
        }
        path.rLineTo(widthMinusCorners, 0f)
        if (br) path.rQuadTo(rx, 0f, rx, -ry) //bottom-right corner
        else {
            path.rLineTo(rx, 0f)
            path.rLineTo(0f, -ry)
        }
        path.rLineTo(0f, -heightMinusCorners)
        path.close() //Given close, last lineto can be removed.
        return path
    }

    //    public static Point convertPoint(Point fromPoint, View fromView, View toView) {
    //        int[] fromCoordinate = new int[2];
    //        int[] toCoordinate = new int[2];
    //        fromView.getLocationOnScreen(fromCoordinate);
    //        toView.getLocationOnScreen(toCoordinate);
    //
    //        Point toPoint = new Point(fromCoordinate[0] - toCoordinate[0] + fromPoint.x, fromCoordinate[1] - toCoordinate[1] + fromPoint.y);
    //        return toPoint;
    //    }
    //
    //    public static boolean isIntentActionAvailable(Context context, String action) {
    //        final PackageManager packageManager = context.getPackageManager();
    //        final Intent intent = new Intent(action);
    //        List resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    //        return resolveInfo.size() > 0;
    //    }
    fun getIntentAsString(intent: Intent?): String {
        return if (intent == null) {
            ""
        } else {
            intent.toUri(0)
        }
    }

    fun getIntentFromString(string: String?): Intent? {
        return try {
            Intent.parseUri(string, 0)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getIntentFromApp(app: App): Intent {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.setClassName(app.packageName, app.className)
        return intent
    }

    //    public static Drawable getIcon(Context context, String filename) {
    //        Bitmap bitmap = BitmapFactory.decodeFile(context.getFilesDir() + "/icons/" + filename + ".png");
    //        if (bitmap != null) return new BitmapDrawable(context.getResources(), bitmap);
    //        return null;
    //    }
    //
    //    public static void saveIcon(Context context, Bitmap icon, String filename) {
    //        File directory = new File(context.getFilesDir() + "/icons/");
    //        if (!directory.exists()) directory.mkdir();
    //        File file = new File(directory + filename + ".png");
    //        try {
    //            file.createNewFile();
    //            FileOutputStream out = new FileOutputStream(file);
    //            icon.compress(Bitmap.CompressFormat.PNG, 100, out);
    //            out.close();
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //    }
    //
    //    public static void removeIcon(Context context, String filename) {
    //        File file = new File(context.getFilesDir() + "/icons/" + filename + ".png");
    //        if (file.exists()) {
    //            try {
    //                file.delete();
    //            } catch (Exception e) {
    //                e.printStackTrace();
    //            }
    //        }
    //    }
    object Time {
        const val MILLI_SEC_1: Long = 1
        const val MILLI_SEC_1000: Long = 1000
        const val SEC_1 = MILLI_SEC_1000
        const val MIN_1 = SEC_1 * 60
        const val MIN_5 = MIN_1 * 5
        const val HOUR_1 = MIN_1 * 60
        const val DAY_1 = HOUR_1 * 24
    }
}
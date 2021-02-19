package com.pukkol.launcher.viewutil

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import java.util.*

object BitmapEditor {
    /**
     * Stack Blur v1.0 from
     * http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
     * Java Author: Mario Klingemann <mario at quasimondo.com>
     * http://incubator.quasimondo.com
     *
     *
     * created Feburary 29, 2004
     * Android port : Yahel Bouaziz <yahel at kayenko.com>
     * http://www.kayenko.com
     * ported april 5th, 2012
     *
     *
     * This is A compromise between Gaussian Blur and Box blur
     * It creates much better looking blurs than Box Blur, but is
     * 7x faster than my Gaussian Blur implementation.
     *
     *
     * I called it Stack Blur because this describes best how this
     * filter works internally: it creates A kind of moving stack
     * of colors whilst scanning through the image. Thereby it
     * just has to add one new block of color to the right side
     * of the stack and removeFromParent the leftmost color. The remaining
     * colors on the topmost layer of the stack are either added on
     * or reduced by one, depending on if they are on the right or
     * on the x side of the stack.
     *
     *
     * If you are using this algorithm in your code please add
     * the following line:
     * Stack Blur Algorithm by Mario Klingemann <mario></mario>@quasimondo.com>
    </yahel></mario> */
    fun FastBlurSupportAlpha(sentBitmap: Bitmap, scale: Float, radius: Int): Bitmap? {
        var sentBitmap = sentBitmap
        val width = Math.round(sentBitmap.width * scale)
        val height = Math.round(sentBitmap.height * scale)
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false)
        val bitmap = sentBitmap.copy(sentBitmap.config, true)
        if (radius < 1) {
            return null
        }
        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)
        //  Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1
        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        val a = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var asum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(Math.max(w, h))
        var divsum = div + 1 shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }
        yi = 0
        yw = yi
        val stack = Array(div) { IntArray(4) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var aoutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int
        var ainsum: Int
        y = 0
        while (y < h) {
            asum = 0
            bsum = asum
            gsum = bsum
            rsum = gsum
            aoutsum = rsum
            boutsum = aoutsum
            goutsum = boutsum
            routsum = goutsum
            ainsum = routsum
            binsum = ainsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                sir[3] = 0xff and (p shr 24)
                rbs = r1 - Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                asum += sir[3] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                    ainsum += sir[3]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                    aoutsum += sir[3]
                }
                i++
            }
            stackpointer = radius
            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                a[yi] = dv[asum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                asum -= aoutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                aoutsum -= sir[3]
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                sir[3] = 0xff and (p shr 24)
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                ainsum += sir[3]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                asum += ainsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                aoutsum += sir[3]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                ainsum -= sir[3]
                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            asum = 0
            bsum = asum
            gsum = bsum
            rsum = gsum
            aoutsum = rsum
            boutsum = aoutsum
            goutsum = boutsum
            routsum = goutsum
            ainsum = routsum
            binsum = ainsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                sir[3] = a[yi]
                rbs = r1 - Math.abs(i)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                asum += a[yi] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                    ainsum += sir[3]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                    aoutsum += sir[3]
                }
                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                pix[yi] = dv[asum] shl 24 or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                asum -= aoutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                aoutsum -= sir[3]
                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w
                }
                p = x + vmin[y]
                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]
                sir[3] = a[p]
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                ainsum += sir[3]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                asum += ainsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                aoutsum += sir[3]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                ainsum -= sir[3]
                yi += w
                y++
            }
            x++
        }

        //   Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }

    fun PerceivedBrightness(will_White: Int, c: IntArray): Boolean {
        val TBT = Math.sqrt(c[0] * c[0] * .241 + c[1] * c[1] * .691 + c[2] * c[2] * .068)
        //    Log.d("themee",TBT+"");
        return TBT <= will_White
    }

    fun manipulateColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = Math.round(Color.red(color) * factor)
        val g = Math.round(Color.green(color) * factor)
        val b = Math.round(Color.blue(color) * factor)
        return Color.argb(a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255))
    }

    @ColorInt
    fun darkenColor(@ColorInt color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] *= 0.9f
        return Color.HSVToColor(hsv)
    }

    fun isDark(bitmap: Bitmap): Boolean {
        var dark = false
        val darkThreshold = bitmap.width * bitmap.height * 0.45f
        var darkPixels = 0
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (pixel in pixels) {
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            val luminance = 0.299 * r + 0.0f + 0.587 * g + 0.0f + 0.114 * b + 0.0f
            if (luminance < 150) {
                darkPixels++
            }
        }
        if (darkPixels >= darkThreshold) {
            dark = true
        }
        return dark
    }

    fun getAverageColorRGB(bitmap: Bitmap): IntArray {
        val width = bitmap.width
        val height = bitmap.height
        var size = width * height
        var pixelColor: Int
        var r: Int
        var g: Int
        var b: Int
        b = 0
        g = b
        r = g
        for (x in 0 until width) {
            for (y in 0 until height) {
                pixelColor = bitmap.getPixel(x, y)
                if (pixelColor == 0) {
                    size--
                    continue
                }
                r += Color.red(pixelColor)
                g += Color.green(pixelColor)
                b += Color.blue(pixelColor)
            }
        }
        r /= size
        g /= size
        b /= size
        return intArrayOf(
                r, g, b
        )
    }

    fun getMostPopularColor(bitmap: Bitmap?): Int {
        return 0
    }

    fun getDominantColor2(bitmap: Bitmap?): Int {
        if (bitmap == null) throw NullPointerException()
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height
        val pixels = IntArray(size)
        val bitmap2 = bitmap.copy(Bitmap.Config.ARGB_4444, false)
        bitmap2.getPixels(pixels, 0, width, 0, 0, width, height)
        val colorMap = HashMap<Int, Int>()
        var color = 0
        var count: Int? = 0
        for (i in pixels.indices) {
            color = pixels[i]
            if (Color.alpha(color) <= 25) continue
            count = colorMap[color]
            if (count == null) count = 0
            colorMap[color] = ++count
        }
        var dominantColor = 0
        var max = 0
        for ((key, value) in colorMap) {
            if (value > max) {
                max = value
                dominantColor = key
            }
        }
        return dominantColor
    }

    fun getDominantAndMostColor(bitmap: Bitmap?): IntArray {
        if (bitmap == null) return intArrayOf(Color.WHITE, Color.WHITE)
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height
        val pixels = IntArray(size)
        val colorMap = HashMap<Int, Int>()
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        var color: Int
        var r = 0
        var g = 0
        var b = 0
        var a: Int
        var count = 0
        var mostCount: Int? = 0
        for (i in pixels.indices) {
            color = pixels[i]
            a = Color.alpha(color)
            if (a > 0) {
                r += Color.red(color)
                g += Color.green(color)
                b += Color.blue(color)
                count++
            }
            if (a > 25) {
                mostCount = colorMap[color]
                if (mostCount == null) mostCount = 0
                colorMap[color] = ++mostCount
            }
        }
        r /= count
        g /= count
        b /= count
        r = r shl 16 and 0x00FF0000
        g = g shl 8 and 0x0000FF00
        b = b and 0x000000FF
        color = -0x1000000 or r or g or b
        if (color == 0) color = Color.WHITE
        var mostColor = Color.WHITE
        var max = 0
        for ((key, value) in colorMap) {
            if (value > max) {
                max = value
                mostColor = key
            }
        }
        return intArrayOf(color, mostColor)
    }

    fun getLuminance(argb: Int): Int {
        return 77 * (argb shr 16 and 255) + 150 * (argb shr 8 and 255) + 29 * (argb and 255) shr 8
    }

    fun getDominantColor(bitmap: Bitmap?): Int {
        if (bitmap == null) {
            return Color.WHITE
        }
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height
        val pixels = IntArray(size)
        //Bitmap bitmap2 = bitmap.copy(Bitmap.Config.ARGB_4444, false);
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        var color: Int
        var r = 0
        var g = 0
        var b = 0
        var a: Int
        var count = 0
        for (i in pixels.indices) {
            color = pixels[i]
            a = Color.alpha(color)
            if (a > 0) {
                r += Color.red(color)
                g += Color.green(color)
                b += Color.blue(color)
                count++
            }
        }
        r /= count
        g /= count
        b /= count
        r = r shl 16 and 0x00FF0000
        g = g shl 8 and 0x0000FF00
        b = b and 0x000000FF
        color = -0x1000000 or r or g or b
        return color
    }

    fun updateSat(src: Bitmap, settingSat: Float): Bitmap {
        val w = src.width
        val h = src.height
        val bitmapResult = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        var canvasResult: Canvas? = Canvas(bitmapResult)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(settingSat)
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter
        canvasResult!!.drawBitmap(src, 0f, 0f, paint)
        canvasResult.setBitmap(null)
        canvasResult = null
        return bitmapResult
    }

    /**
     * Stack Blur v1.0 from
     * http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
     * Java Author: Mario Klingemann <mario at quasimondo.com>
     * http://incubator.quasimondo.com
     *
     * created Feburary 29, 2004
     * Android port : Yahel Bouaziz <yahel at kayenko.com>
     * http://www.kayenko.com
     * ported april 5th, 2012
     *
     * This is A compromise between Gaussian Blur and Box blur
     * It creates much better looking blurs than Box Blur, but is
     * 7x faster than my Gaussian Blur implementation.
     *
     * I called it Stack Blur because this describes best how this
     * filter works internally: it creates A kind of moving stack
     * of colors whilst scanning through the image. Thereby it
     * just has to add one new block of color to the right side
     * of the stack and removeFromParent the leftmost color. The remaining
     * colors on the topmost layer of the stack are either added on
     * or reduced by one, depending on if they are on the right or
     * on the x side of the stack.
     *
     * If you are using this algorithm in your code please add
     * the following line:
     * Stack Blur Algorithm by Mario Klingemann <mario></mario>@quasimondo.com>
    </yahel></mario> */
    fun fastblur(sentBitmap: Bitmap, scale: Float, radius: Int): Bitmap {
        val afterscaleSentBitmap: Bitmap
        val bitmap: Bitmap
        if (scale != 1f) {
            val width = Math.round(sentBitmap.width * scale) //lấy chiều rộng làm tròn
            val height = Math.round(sentBitmap.height * scale) // lấy chiều cao làm tròn
            afterscaleSentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false) // tạo bitmap scaled
            bitmap = afterscaleSentBitmap.copy(afterscaleSentBitmap.config, true)
            afterscaleSentBitmap.recycle()
        } else {
            bitmap = sentBitmap.copy(sentBitmap.config, true) // đơn giản chỉ copy
        }
        if (radius < 1) {
            return sentBitmap.copy(sentBitmap.config, true)
        }
        val w = bitmap.width //  w is the width of sample bitmap
        val h = bitmap.height // h is the height of sample bitmap
        val pix = IntArray(w * h) // pix is the arrary of all bitmap pixel
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1
        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(Math.max(w, h))
        var divsum = div + 1 shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }
        yi = 0
        yw = yi
        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int
        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius
            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - Math.abs(i)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {

                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w
                }
                p = x + vmin[y]
                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi += w
                y++
            }
            x++
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }

    fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap
                .height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        //    final ScreenSize rectF = new ScreenSize(rect);
        val roundPx = pixels.toFloat()
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        //   canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawPath(RoundedRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), roundPx, roundPx, false), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    /** getResizedBitmap method is used to Resized the Image according to custom width and height
     * @param image
     * @param newHeight (new desired height)
     * @param newWidth (new desired Width)
     * @return image (new resized image)
     */
    fun getResizedBitmap(image: Bitmap, newHeight: Int, newWidth: Int): Bitmap {
        val width = image.width
        val height = image.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // create A matrix for the manipulation
        val matrix = Matrix()
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight)
        // recreate the new Bitmap
        return Bitmap.createBitmap(image, 0, 0, width, height,
                matrix, false)
    }

    fun TrueIfBitmapBigger(bitmap: Bitmap, size: Int): Boolean {
        val sizeBitmap = if (bitmap.height > bitmap.width) bitmap.height else bitmap.width
        return if (sizeBitmap > size) true else false
    }

    fun GetRoundedBitmapWithBlurShadow(original: Bitmap, paddingTop: Int, paddingBottom: Int, paddingLeft: Int, paddingRight: Int): Bitmap? {
        val original_width = original.width
        val orginal_height = original.height
        val bitmap_width = original_width + paddingLeft + paddingRight
        val bitmap_height = orginal_height + paddingTop + paddingBottom
        val bitmap = Bitmap.createBitmap(bitmap_width, bitmap_height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.style = Paint.Style.FILL
        //paint.setAlpha(60);
        //   canvas.drawRect(0,0,bitmap_width,bitmap_height,paint);
        paint.isAntiAlias = true
        canvas.drawBitmap(original, paddingLeft.toFloat(), paddingTop.toFloat(), paint)
        val blurred_bitmap = getBlurredWithGoodPerformance(bitmap, 1, 6, 4)
        canvas.setBitmap(null)
        bitmap.recycle()
        return blurred_bitmap
    }

    //                                                                            Activity.
    //                                                                                |            Original bitmap.
    //                                                                                 |                      |              To make the blur background, the original must to padding.
    //                                                                                  |                      |                          |                |                            |                       |
    //                                                                                   V                     V                         V              V                           V                      V
    fun GetRoundedBitmapWithBlurShadow(context: Context?, original: Bitmap, paddingTop: Int, paddingBottom: Int, paddingLeft: Int, paddingRight: Int,
                                       TopBack: Int // this value makes the overview bitmap is higher or  belower the background.
                                       , alphaBlurBackground: Int // this is the alpha of the background Bitmap, you need A number between 0 -> 255, the value recommend is 180.
                                       , valueBlurBackground: Int // this is the value used to blur the background Bitmap, the recommended one is 12.
                                       , valueSaturationBlurBackground: Int // this is the value used to background Bitmap more colorful, if valueBlur is 12, the valudeSaturation should be 2.
    ): Bitmap {
        val original_width = original.width
        val orginal_height = original.height
        val bitmap_width = original_width + paddingLeft + paddingRight
        val bitmap_height = orginal_height + paddingTop + paddingBottom
        val bitmap = Bitmap.createBitmap(bitmap_width, bitmap_height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        canvas.drawBitmap(original, paddingLeft.toFloat(), paddingTop.toFloat(), paint)
        val blurred_bitmap = getBlurredWithGoodPerformance(context, bitmap, 1, valueBlurBackground, valueSaturationBlurBackground.toFloat())
        //   Bitmap blurred_bitmap= getBlurredWithGoodPerformance(context, bitmap,1,15,3);
        val end_bitmap = Bitmap.createBitmap(bitmap_width, bitmap_height, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(end_bitmap)
        paint.alpha = alphaBlurBackground
        canvas.drawBitmap(blurred_bitmap, Rect(0, 0, blurred_bitmap.width, blurred_bitmap.height), Rect(0, 0, bitmap_width, bitmap_height), paint)
        paint.alpha = 255
        canvas.drawBitmap(bitmap, 0f, TopBack.toFloat(), paint) // drawVisualWave cái lớn
        canvas.setBitmap(null)
        blurred_bitmap.recycle()
        bitmap.recycle()
        return end_bitmap
    }

    fun setBitmapforImageView(imv: ImageView, apply: Bitmap?) {
        val old = (imv.drawable as BitmapDrawable).bitmap
        imv.setImageBitmap(apply)
        old?.recycle()
    }

    fun getBlurredWithGoodPerformance(bitmap: Bitmap, scale: Int, radius: Int, saturation: Int): Bitmap? {
        val options = BitmapFactory.Options()
        val bitmap1 = getResizedBitmap(bitmap, 50, 50)
        val updateSatBitmap = updateSat(bitmap1, saturation.toFloat())
        val blurredBitmap = FastBlurSupportAlpha(updateSatBitmap, scale.toFloat(), radius)
        updateSatBitmap.recycle()
        bitmap1.recycle()
        return blurredBitmap
    }

    fun RoundedRect(left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float, conformToOriginalPost: Boolean): Path {
        var rx = rx
        var ry = ry
        val path = Path()
        if (rx < 0) rx = 0f
        if (ry < 0) ry = 0f
        val width = right - left
        val height = bottom - top
        if (rx > width / 2) rx = width / 2
        if (ry > height / 2) ry = height / 2
        val widthMinusCorners = width - 2 * rx // do dai phan "thang" cua chieu rong
        val heightMinusCorners = height - 2 * ry // do dai phan "thang" cua chieu dai
        path.moveTo(right, top + ry) // bat dau tu  day
        path.rQuadTo(0f, -ry, -rx, -ry) //y-right corner
        path.rLineTo(-widthMinusCorners, 0f)
        path.rQuadTo(-rx, 0f, -rx, ry) //y-x corner
        path.rLineTo(0f, heightMinusCorners)
        if (conformToOriginalPost) {
            path.rLineTo(0f, ry)
            path.rLineTo(width, 0f)
            path.rLineTo(0f, -ry)
        } else {
            path.rQuadTo(0f, ry, rx, ry) //bottom-x corner
            path.rLineTo(widthMinusCorners, 0f)
            path.rQuadTo(rx, 0f, rx, -ry) //bottom-right corner
        }
        path.rLineTo(0f, -heightMinusCorners)
        path.close() //Given close, last lineto can be removed.
        return path
    }

    fun mixTwoColors(color1: Int, color2: Int, amount: Float): Int {
        val ALPHA_CHANNEL: Byte = 24
        val RED_CHANNEL: Byte = 16
        val GREEN_CHANNEL: Byte = 8
        val BLUE_CHANNEL: Byte = 0
        val inverseAmount = 1.0f - amount
        val a = ((color1 shr ALPHA_CHANNEL.toInt() and 0xff).toFloat() * amount +
                (color2 shr ALPHA_CHANNEL.toInt() and 0xff).toFloat() * inverseAmount).toInt() and 0xff
        val r = ((color1 shr RED_CHANNEL.toInt() and 0xff).toFloat() * amount +
                (color2 shr RED_CHANNEL.toInt() and 0xff).toFloat() * inverseAmount).toInt() and 0xff
        val g = ((color1 shr GREEN_CHANNEL.toInt() and 0xff).toFloat() * amount +
                (color2 shr GREEN_CHANNEL.toInt() and 0xff).toFloat() * inverseAmount).toInt() and 0xff
        val b = ((color1 and 0xff).toFloat() * amount +
                (color2 and 0xff).toFloat() * inverseAmount).toInt() and 0xff
        return a shl ALPHA_CHANNEL.toInt() or (r shl RED_CHANNEL.toInt()) or (g shl GREEN_CHANNEL.toInt()) or (b shl BLUE_CHANNEL.toInt())
    }

    fun getBlurredWithGoodPerformance(context: Context?, bitmap: Bitmap, scale: Int, radius: Int, saturation: Float): Bitmap {
        val bitmap1 = getResizedBitmap(bitmap, 150, 150)
        val updateSatBimap = updateSat(bitmap1, saturation)
        val blurredBitmap = BlurBitmapWithRenderScript(context, updateSatBimap, radius.toFloat())
        updateSatBimap.recycle()
        bitmap1.recycle()
        return blurredBitmap
    }

    fun getBlurredBimapWithRenderScript(context: Context?, bitmapOriginal: Bitmap, radius: Float): Bitmap {
        //define this only once if blurring multiple times
        val rs = RenderScript.create(context)

//this will blur the bitmapOriginal with A radius of 8 and save it in bitmapOriginal
        val input = Allocation.createFromBitmap(rs, bitmapOriginal) //use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
        val output = Allocation.createTyped(rs, input.type)
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        script.setRadius(radius)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(bitmapOriginal)
        return bitmapOriginal
    }

    fun BlurBitmapWithRenderScript(context: Context?, bitmap: Bitmap, radius: Float): Bitmap {
        //Let's create an empty bitmap with the same size of the bitmap we want to blur
        val outBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

        //Instantiate A new Renderscript
        val rs = RenderScript.create(context)

        //Create an Intrinsic Blur Script using the Renderscript
        val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
        val allIn = Allocation.createFromBitmap(rs, bitmap)
        val allOut = Allocation.createFromBitmap(rs, outBitmap)
        //Set the radius of the blur
        blurScript.setRadius(radius)

        //Perform the Renderscript
        blurScript.setInput(allIn)
        blurScript.forEach(allOut)

        //Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap)

        //recycle the original bitmap

        //After finishing everything, we destroy the Renderscript.
        rs.destroy()
        return outBitmap
    }

    fun covertBitmapToDrawable(context: Context, bitmap: Bitmap?): Drawable {
        return BitmapDrawable(context.resources, bitmap)
    }

    fun convertDrawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun changeBitmapColor(sourceBitmap: Bitmap, color: Int): Bitmap {
        val resultBitmap = sourceBitmap.copy(sourceBitmap.config, true)
        val paint = Paint()
        val filter: ColorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        paint.colorFilter = filter
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(resultBitmap, 0f, 0f, paint)
        return resultBitmap
    }

    /**
     *
     * @param mode
     * @return
     * 0 : CLEAR
     * <br></br> 1 : SRC
     * <br></br> 2 : DST
     * <br></br> 3 : SRC_OVER
     * <br></br> 4 : DST_OVER
     * <br></br> 5 : SRC_IN
     * <br></br> 6 : DST_IN
     * <br></br> 7 : SRC_OUT
     * <br></br> 8 : DST_OUT
     * <br></br> 9 : SRC_ATOP
     * <br></br>10 : DST_ATOP
     * <br></br>11 : XOR
     * <br></br>12 : ADD
     * <br></br>13 : MULTIPLY
     * <br></br>14 : SCREEN
     * <br></br>15 : OVERLAY
     * <br></br>16 : DARKEN
     * <br></br>17 : LIGHTEN
     */
    fun getPorterMode(mode: Int): PorterDuff.Mode {
        return when (mode) {
            0 -> PorterDuff.Mode.CLEAR
            1 -> PorterDuff.Mode.SRC
            2 -> PorterDuff.Mode.DST
            3 -> PorterDuff.Mode.SRC_OVER
            4 -> PorterDuff.Mode.DST_OVER
            5 -> PorterDuff.Mode.SRC_IN
            6 -> PorterDuff.Mode.DST_IN
            7 -> PorterDuff.Mode.SRC_OUT
            8 -> PorterDuff.Mode.DST_OUT
            9 -> PorterDuff.Mode.SRC_ATOP
            10 -> PorterDuff.Mode.DST_ATOP
            11 -> PorterDuff.Mode.XOR
            16 -> PorterDuff.Mode.DARKEN
            17 -> PorterDuff.Mode.LIGHTEN
            13 -> PorterDuff.Mode.MULTIPLY
            14 -> PorterDuff.Mode.SCREEN
            12 -> PorterDuff.Mode.ADD
            15 -> PorterDuff.Mode.OVERLAY
            else -> PorterDuff.Mode.CLEAR
        }
    }

    fun applyNewColor4Bitmap(context: Context, idBitmaps: IntArray, imageViews: Array<ImageView>, color: Int, alpha: Float) {
        val resource = context.resources
        val size = idBitmaps.size
        var usingBitmap: Bitmap
        var resultBitmap: Bitmap
        for (i in 0 until size) {
            usingBitmap = BitmapFactory.decodeResource(resource, idBitmaps[i])
            resultBitmap = changeBitmapColor(usingBitmap, color)
            imageViews[i].setImageBitmap(resultBitmap)
            imageViews[i].alpha = alpha
        }
    }

    fun applyNewColor4Bitmap(context: Context, idBitmap: Int, applyView: ImageView, color: Int, alpha: Float) {
        val resource = context.resources
        val usingBitmap = BitmapFactory.decodeResource(resource, idBitmap)
        val resultBitmap = changeBitmapColor(usingBitmap, color)
        applyView.setImageBitmap(resultBitmap)
        applyView.alpha = alpha
    }

    fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        view.layout(view.left, view.top, view.right, view.bottom)
        view.draw(c)
        return bitmap
    }

    fun getBitmapFromView(view: View, left: Int, top: Int, right: Int, bottom: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        view.layout(left, top, right, bottom)
        view.draw(c)
        return bitmap
    }

    fun getBackgroundBitmapAViewWithParent(childView: View, parentView: View): Bitmap {
        val pos_child = IntArray(2)
        childView.getLocationOnScreen(pos_child)
        return getBitmapFromView(parentView, pos_child[0], pos_child[1], parentView.right, parentView.bottom)
    }

    fun getBackgroundBlurAViewWithParent(activity: Activity?, childView: View, parentView: View): Bitmap {
        val b1 = getBackgroundBitmapAViewWithParent(childView, parentView)
        val b2 = getBlurredWithGoodPerformance(activity, b1, 1, 8, 2f)
        b1.recycle()
        return b2
    }

    /***
     * Trim an image, removing transparent borders.
     * @param bitmap image to crop
     * @return square bitmap with the cropped image
     */
    fun crop(bitmap: Bitmap): Bitmap {
        val height = bitmap.height
        val width = bitmap.width
        var empty = IntArray(width)
        var buffer = IntArray(width)
        Arrays.fill(empty, 0)
        var top = 0
        var left = 0
        var bottom = height
        var right = width
        for (y in 0 until height) {
            bitmap.getPixels(buffer, 0, width, 0, y, width, 1)
            if (!Arrays.equals(empty, buffer)) {
                top = y
                break
            }
        }
        for (y in height - 1 downTo top + 1) {
            bitmap.getPixels(buffer, 0, width, 0, y, width, 1)
            if (!Arrays.equals(empty, buffer)) {
                bottom = y
                break
            }
        }
        val bufferSize = bottom - top + 1
        empty = IntArray(bufferSize)
        buffer = IntArray(bufferSize)
        Arrays.fill(empty, 0)
        for (x in 0 until width) {
            bitmap.getPixels(buffer, 0, 1, x, top + 1, 1, bufferSize)
            if (!Arrays.equals(empty, buffer)) {
                left = x
                break
            }
        }
        Arrays.fill(empty, 0)
        for (x in width - 1 downTo left + 1) {
            bitmap.getPixels(buffer, 0, 1, x, top + 1, 1, bufferSize)
            if (!Arrays.equals(empty, buffer)) {
                right = x
                break
            }
        }
        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }

    fun CropBitmapTransparency(sourceBitmap: Bitmap): Bitmap? {
        var minX = sourceBitmap.width
        var minY = sourceBitmap.height
        var maxX = -1
        var maxY = -1
        for (y in 0 until sourceBitmap.height) {
            for (x in 0 until sourceBitmap.width) {
                val alpha = sourceBitmap.getPixel(x, y) shr 24 and 255
                if (alpha > 0) // pixel is not 100% transparent
                {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }
        return if (maxX < minX || maxY < minY) null else Bitmap.createBitmap(sourceBitmap, minX, minY, maxX - minX + 1, maxY - minY + 1) // Bitmap is entirely transparent

        // crop bitmap to non-transparent area and return:
    }
}
package com.pukkol.launcher.ui.home.desktop.edit.wallpaper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pukkol.launcher.R
import com.pukkol.launcher.interfaces.IWallpaperListener
import com.pukkol.launcher.ui.home.desktop.edit.wallpaper.WallpaperOptionAdapter.ViewHolder.onClickListener
import com.pukkol.launcher.util.Display
import java.io.IOException
import java.io.InputStream
import java.util.*

class WallpaperOptionView @SuppressLint("ClickableViewAccessibility") constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr), View.OnTouchListener, View.OnClickListener, onClickListener {
    // ui
    private val mImageWallpaper: ImageView
    private val mRecyclerView: RecyclerView
    private val mToolbar: Toolbar
    private val mLayoutWallpaper: ConstraintLayout

    // methods
    private var mAdapter: WallpaperOptionAdapter? = null
    private var mWPListener: IWallpaperListener? = null
    private var mFingerMode = FingerAction.NONE
    private var mWallpaperMatrix = Matrix()
    private var mWallpaperSavedMatrix = Matrix()
    private val mDisplaySize: Size
    private var mCurrentWallpaper: Bitmap? = null

    // variables
    var oldDist = 1.0
    private var mOriginalWidth = -1
    private var mOriginalHeight = -1
    private var mWallpaperScale = 1.0f // 100%
    private var mResizedSize = 1.0000 // 100%
    private var mNewSize = 1.0000 // 100%
    private var mPositionX = 0.0000 // 0%
    private var mPositionY = 0.0000 // 0%

    // todo(later): possible to set to %
    private var mStartDrag = PointF()
    private var mFingersZoom = PointF()
    private var mMoving = PointF()

    constructor(context: Context) : this(context, null, 0) {}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    fun init() {
        if (isInEditMode) return
        mAdapter = WallpaperOptionAdapter(this.context, this)
        setupAdapter()
        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mRecyclerView.layoutManager = linearLayoutManager
        mRecyclerView.adapter = mAdapter
    }

    private fun setupAdapter() {
        val assetManager = this.context.assets
        var is1: InputStream? = null
        var is2: InputStream? = null
        var is3: InputStream? = null
        var is4: InputStream? = null
        var is5: InputStream? = null
        try {
            is1 = assetManager.open("wallpapers/wallpaper-1.jpg")
            is2 = assetManager.open("wallpapers/wallpaper-2.jpg")
            is3 = assetManager.open("wallpapers/wallpaper-3.jpg")
            is4 = assetManager.open("wallpapers/wallpaper-4.jpg")
            is5 = assetManager.open("wallpapers/wallpaper-5.jpg")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val imageWidth = Math.ceil(mDisplaySize.width / 3.5).toInt()
        val imageHeight = Math.ceil(imageWidth.toDouble()).toInt()

        // set as default
        val defaultItem = WallpaperOptionContainer(context, BitmapFactory.decodeStream(is1), Size(imageWidth, imageHeight))
        val items: MutableList<WallpaperOptionContainer> = ArrayList()
        items.add(WallpaperOptionContainer(context, null, Size(imageWidth, imageHeight)))
        items.add(defaultItem)
        items.add(WallpaperOptionContainer(context, BitmapFactory.decodeStream(is2), Size(imageWidth, imageHeight)))
        items.add(WallpaperOptionContainer(context, BitmapFactory.decodeStream(is3), Size(imageWidth, imageHeight)))
        items.add(WallpaperOptionContainer(context, BitmapFactory.decodeStream(is4), Size(imageWidth, imageHeight)))
        items.add(WallpaperOptionContainer(context, BitmapFactory.decodeStream(is5), Size(imageWidth, imageHeight)))

        // set default ones
        mAdapter!!.items = items
        mAdapter!!.updateUsages(1, false)
        setWallpaper(defaultItem.bitmap!!)
    }

    private fun showSettings(show: Boolean) {
        if (show) {
            mRecyclerView.visibility = VISIBLE
            mToolbar.visibility = VISIBLE
        } else {
            mRecyclerView.visibility = GONE
            mToolbar.visibility = GONE
        }
    }

    fun setOnWallpaperListener(wallpaperListener: IWallpaperListener?) {
        mWPListener = wallpaperListener
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.layout_wallpaper_save -> {
                prepareWallpaper(mAdapter!!.currentWallpaper)
            }
            R.id.layout_choose_wallpaper -> {
                mLayoutWallpaper.visibility = GONE
            }
            R.id.card_wallpaper_homeScreen -> {
                mWPListener!!.onWallpaperChanged(mCurrentWallpaper!!, true, false)
                mLayoutWallpaper.visibility = GONE
            }
            R.id.card_wallpaper_lockScreen -> {
                mWPListener!!.onWallpaperChanged(mCurrentWallpaper!!, false, true)
                mLayoutWallpaper.visibility = GONE
            }
            R.id.card_wallpaper_both -> {
                mWPListener!!.onWallpaperChanged(mCurrentWallpaper!!, true, true)
                mLayoutWallpaper.visibility = GONE
            }
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val view = v as ImageView
        view.scaleType = ImageView.ScaleType.MATRIX
        var currentWidth = mOriginalWidth * mNewSize
        var currentHeight = mOriginalHeight * mNewSize
        var currentPointX = currentWidth * mPositionX
        var currentPointY = currentHeight * mPositionY
        if (event != null) {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    showSettings(false)
                    mFingerMode = FingerAction.DRAG
                    displayWallpaper(currentWidth, currentHeight, currentPointX, currentPointY, event)
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    oldDist = spacing(event)
                    Log.d(TAG, "oldDist=$oldDist")
                    if (oldDist > 5.0000) {
                        mFingerMode = FingerAction.ZOOM
                        displayWallpaper(currentWidth, currentHeight, currentPointX, currentPointY, event)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mFingerMode == FingerAction.DRAG) {
                        mFingerMode = FingerAction.MOVE_DRAG
                    } else if (mFingerMode == FingerAction.ZOOM) {
                        mFingerMode = FingerAction.MOVE_ZOOM
                    }
                    if (mFingerMode == FingerAction.MOVE_DRAG) {
                        displayWallpaper(currentWidth, currentHeight, currentPointX, currentPointY, event)
                    } else if (mFingerMode == FingerAction.MOVE_ZOOM) {
                        displayWallpaper(currentWidth, currentHeight, currentPointX, currentPointY, event)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    showSettings(true)
                    mFingerMode = FingerAction.NONE

                    // zoom in/out
                    currentWidth *= mWallpaperScale.toDouble()
                    currentHeight *= mWallpaperScale.toDouble()
                    currentPointX = mMoving.x + currentWidth * mPositionX
                    currentPointY = mMoving.y + currentHeight * mPositionY

                    // zoom save & reset
                    mNewSize = mResizedSize

                    // drag save
                    mPositionX = currentPointX / currentWidth
                    mPositionY = currentPointY / currentHeight
                    Log.d(TAG, "mode = NONE")
                    Log.d(TAG, "position X = $mPositionX, Y = $mPositionY")
                    Log.d(TAG, "resized W = " + (mOriginalWidth.toDouble() * mResizedSize).toInt() + ", H = " + (mOriginalHeight.toDouble() * mResizedSize).toInt() + ", Scaled at: " + mWallpaperScale)
                    mMoving = PointF(0f, 0f)
                    mWallpaperScale = 1.00f
                }
                else -> {
                    showSettings(true)
                    mFingerMode = FingerAction.NONE
                    currentWidth *= mWallpaperScale.toDouble()
                    currentHeight *= mWallpaperScale.toDouble()
                    currentPointX = mMoving.x + currentWidth * mPositionX
                    currentPointY = mMoving.y + currentHeight * mPositionY
                    mNewSize = mResizedSize
                    mPositionX = currentPointX / currentWidth
                    mPositionY = currentPointY / currentHeight
                    Log.d(TAG, "mode = NONE")
                    Log.d(TAG, "position X = $mPositionX, Y = $mPositionY")
                    Log.d(TAG, "resized W = " + (mOriginalWidth.toDouble() * mResizedSize).toInt() + ", H = " + (mOriginalHeight.toDouble() * mResizedSize).toInt() + ", Scaled at: " + mWallpaperScale)
                    mMoving = PointF(0f, 0f)
                    mWallpaperScale = 1.00f
                }
            }
        }
        view.imageMatrix = mWallpaperMatrix
        return true // indicate event was handled
    }

    override fun onItemClickListener(item: WallpaperOptionContainer?, position: Int) {
        if (position == 0) {
            mAdapter!!.updateUsages(position, true)
            mWPListener!!.onWallpaperPick()
        } else {
            mAdapter!!.updateUsages(position, false)
            setWallpaper(item!!.bitmap!!)
        }
    }

    // todo(later): set zoom to finger point
    //  and not to the corners from the device
    private fun displayWallpaper(currentWidth: Double, currentHeight: Double, currentPointX: Double, currentPointY: Double, event: MotionEvent) {
        val finger = PointF(event.x, event.y)
        val maxZoomW = mOriginalWidth * MAX_SIZE
        val maxZoomH = mOriginalHeight * MAX_SIZE
        val maxPointX = -(currentWidth - mDisplaySize.width)
        val maxPointY = -(currentHeight - mDisplaySize.height)
        val drag = PointF((finger.x - mStartDrag.x) * DRAG_TURBO.toFloat(), (finger.y - mStartDrag.y) * DRAG_TURBO.toFloat())
        val position = PointF(currentPointX.toFloat() + drag.x, currentPointY.toFloat() + drag.y)

        // define drag mode
        if (mFingerMode == FingerAction.DRAG) {
            mWallpaperSavedMatrix.set(mWallpaperMatrix)
            mStartDrag[finger.x] = finger.y
            Log.d(TAG, "mode=DRAG")
        } else if (mFingerMode == FingerAction.MOVE_DRAG) {
            // horizontal
            if (position.x < 0f && position.x > maxPointX) {
                mMoving.x = drag.x
            } else if (position.x > 0f && mMoving.x < 0f) {
                mMoving.x = Math.abs(currentPointX.toFloat())
            }

            // vertical
            if (position.y < 0f && position.y > maxPointY) {
                mMoving.y = drag.y
            } else if (position.y > 0f && mMoving.y < 0f) {
                mMoving.y = Math.abs(currentPointY.toFloat())
            }
            mWallpaperMatrix.set(mWallpaperSavedMatrix)
            mWallpaperMatrix.postTranslate(mMoving.x, mMoving.y)
            Log.d(TAG, " position X = " + position.x + ", Y = " + position.y + " | wallpaper size X = " + currentWidth + ", Y = " + currentHeight)
        } else if (mFingerMode == FingerAction.ZOOM) {
            mWallpaperSavedMatrix.set(mWallpaperMatrix)
            zoomPoint(mFingersZoom, event)
            Log.d(TAG, "mode=ZOOM")
        } else if (mFingerMode == FingerAction.MOVE_ZOOM) {
            // pinch zooming
            val newDist = spacing(event)
            if (newDist > 5.00f) {
                val scale = (newDist / oldDist).toFloat()
                val newWidth = (currentWidth.toFloat() * scale).toInt()
                val newHeight = (currentHeight.toFloat() * scale).toInt()
                val screenWidth = mDisplaySize.width.toDouble()
                val screenHeight = mDisplaySize.height.toDouble()

                // scale limitation:
                // - max = 90% zoom in
                // - min = screen size
                if (newWidth > screenWidth && newWidth < maxZoomW && newHeight > screenHeight && newHeight < maxZoomH) {
                    mResizedSize = newWidth.toDouble() / mOriginalWidth.toDouble()
                    mWallpaperScale = scale

                    // zoom in/out overflow protection (x-left & y-bottom)
                    run {
                        val width = screenWidth * mWallpaperScale
                        val height = screenHeight * mWallpaperScale
                        val centerX = -(mOriginalWidth * mWallpaperScale / 2).toDouble()
                        val centerY = -(mOriginalWidth * mWallpaperScale / 2).toDouble()
                        if (position.x < centerX) {
                            mMoving.x = Math.max((screenWidth - width).toFloat(), 0.00f)
                        }
                        if (position.y < centerY) {
                            mMoving.y = Math.max((screenHeight - height).toFloat(), 0.00f)
                        }
                    }
                }
                mWallpaperMatrix.set(mWallpaperSavedMatrix)
                mWallpaperMatrix.postScale(mWallpaperScale, mWallpaperScale) // mFingersZoom.x, mFingersZoom.y);

                // zoom in/out overflow protection
                if (mMoving.x != 0.00f || mMoving.y != 0.00f) {
                    mWallpaperMatrix.postTranslate(mMoving.x, mMoving.y)
                }
            }
        }
    }

    private fun spacing(event: MotionEvent): Double {
        val x = (event.getX(0) - event.getX(1)).toDouble()
        val y = (event.getY(0) - event.getY(1)).toDouble()
        return Math.sqrt(x * x + y * y)
    }

    private fun zoomPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2.00f] = y / 2.00f
    }

    fun setWallpaper(bitmap: Bitmap) {
        refreshWallpaper()
        mOriginalWidth = bitmap.width
        mOriginalHeight = bitmap.height

        // resize wallpaper
        run {
            val ratio: Double
            if (mOriginalHeight > mOriginalWidth) {
                if (mOriginalWidth < mDisplaySize.width) {
                    ratio = mDisplaySize.width.toDouble() / mOriginalWidth.toDouble()
                    mOriginalWidth = mDisplaySize.width
                    mOriginalHeight = Math.ceil(mOriginalHeight.toDouble() * ratio).toInt()
                }
            } else if (mOriginalWidth > mOriginalHeight) {
                if (mOriginalHeight < mDisplaySize.height) {
                    ratio = mDisplaySize.height.toDouble() / mOriginalHeight.toDouble()
                    mOriginalHeight = mDisplaySize.height
                    mOriginalWidth = Math.ceil(mOriginalWidth.toDouble() * ratio).toInt()
                }
            }
            mNewSize = 1.0000 // 100.00%
            mResizedSize = 1.0000 // 100.00%
        }

        // set new wallpaper image
        val wallpaper = Bitmap.createScaledBitmap(bitmap, mOriginalWidth, mOriginalHeight, true)
        mImageWallpaper.setImageBitmap(wallpaper)
        mImageWallpaper.scaleType = ImageView.ScaleType.MATRIX
        mImageWallpaper.imageMatrix = mWallpaperMatrix
    }

    fun refreshWallpaper() {
        mFingerMode = FingerAction.NONE
        mStartDrag = PointF()
        mFingersZoom = PointF()
        mPositionX = 0.0000 // 0%
        mPositionY = 0.0000 // 0%
        mMoving = PointF()
        mWallpaperMatrix = Matrix()
        mWallpaperSavedMatrix = Matrix()
        mWallpaperScale = 1.0f // 100%
    }

    // todo(later): possible to edit location and zoom
    //  on a picked image from the user
    fun prepareWallpaper(wallpaper: Bitmap?) {
        var wallpaper = wallpaper
        val currentWidth = (mOriginalWidth * mNewSize).toInt()
        val currentHeight = (mOriginalHeight * mNewSize).toInt()
        var currentPointX = (currentWidth * Math.abs(mPositionX)).toInt()
        val currentPointY = (currentHeight * Math.abs(mPositionY)).toInt()

        // set space scroll animation
        currentPointX = Math.max(0, currentPointX - mDisplaySize.height / 2)
        if (currentWidth < currentPointX + mDisplaySize.height) {
            currentPointX = currentWidth - mDisplaySize.height
        }

        // create new bitmap
        wallpaper = Bitmap.createScaledBitmap(wallpaper!!, currentWidth, currentHeight, false)
        mCurrentWallpaper = Bitmap.createBitmap(wallpaper, currentPointX, currentPointY, mDisplaySize.height, mDisplaySize.height)
        mLayoutWallpaper.visibility = VISIBLE
    }

    enum class FingerAction {
        NONE, DRAG, ZOOM, MOVE_DRAG, MOVE_ZOOM
    }

    companion object {
        // static & final
        private val TAG = WallpaperOptionView::class.java.simpleName
        private const val DRAG_TURBO = 2.5000 // speed up drag finger
        private const val MAX_SIZE = 1.9000 // max 190.00% zoom
    }

    init {
        (getContext() as Activity).layoutInflater
                .inflate(R.layout.view_wallpaper_option, this, true)
        mImageWallpaper = findViewById(R.id.image_wallpaper_set)
        mRecyclerView = findViewById(R.id.recycler_wallpaper_item)
        mToolbar = findViewById(R.id.toolbar_wallpaper_option)
        mLayoutWallpaper = findViewById(R.id.layout_choose_wallpaper)
        val buttonSave = findViewById<LinearLayout>(R.id.layout_wallpaper_save)
        val buttonOnlyHome: CardView = findViewById(R.id.card_wallpaper_homeScreen)
        val buttonOnlyLock: CardView = findViewById(R.id.card_wallpaper_lockScreen)
        val buttonBoth: CardView = findViewById(R.id.card_wallpaper_both)
        mDisplaySize = Display.DISPLAY_SIZE!!

        // below status-bar and above nav-bar
        mToolbar.setPadding(0, Display.STATUSBAR_HEIGHT, 0, 0)
        mRecyclerView.setPadding(0, 0, 0, Display.NAVBAR_HEIGHT)
        buttonSave.setOnClickListener(this)
        buttonOnlyHome.setOnClickListener(this)
        buttonOnlyLock.setOnClickListener(this)
        buttonBoth.setOnClickListener(this)
        mImageWallpaper.setOnTouchListener(this)
        mLayoutWallpaper.setOnClickListener(this)
        init()
    }
}
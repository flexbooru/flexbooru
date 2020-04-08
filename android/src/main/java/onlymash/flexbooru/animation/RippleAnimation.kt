/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package onlymash.flexbooru.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

// https://github.com/Ifxcyr/RippleAnimation/
@SuppressLint("ViewConstructor")
class RippleAnimation private constructor(
    context: Context,
    private val startX: Float,
    private val startY: Float,
    private val startRadius: Int
) : View(context) {

    private var background: Bitmap? = null//屏幕截图
    private var paint: Paint? = null
    private var maxRadius = 0
    private var currentRadius: Int = 0
    private var isStarted = false
    private var duration: Long = 0
    private var rootView: ViewGroup? = null//DecorView
    private var onAnimationEndListener: OnAnimationEndListener? = null
    private var animatorListener: Animator.AnimatorListener? = null
    private var animatorUpdateListener: ValueAnimator.AnimatorUpdateListener? = null

    private val animator: ValueAnimator
        get() {
            val valueAnimator = ValueAnimator.ofFloat(0f, maxRadius.toFloat()).setDuration(duration)
            valueAnimator.addUpdateListener(animatorUpdateListener)
            valueAnimator.addListener(animatorListener)
            return valueAnimator
        }

    init {
        //获取activity的根视图,用来添加本View
        rootView = getActivityFromContext(context).window.decorView as ViewGroup
        paint = Paint()
        paint!!.isAntiAlias = true
        //设置为擦除模式
        paint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        updateMaxRadius()
        initListener()
    }

    /**
     * try get host activity from view.
     * views hosted on floating window like dialog and toast will sure return null.
     *
     * @return host activity
     */
    private fun getActivityFromContext(context: Context): Activity {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        throw RuntimeException("Activity not found!")
    }

    /**
     * 开始播放动画
     */
    fun start() {
        if (!isStarted) {
            isStarted = true
            updateBackground()
            attachToRootView()
            animator.start()
        }
    }

    /**
     * 设置动画时长
     */
    fun setDuration(duration: Long): RippleAnimation {
        this.duration = duration
        return this
    }

    private fun initListener() {
        animatorListener = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (onAnimationEndListener != null) {
                    onAnimationEndListener!!.onAnimationEnd()
                }
                isStarted = false
                //动画播放完毕, 移除本View
                detachFromRootView()
            }
        }
        animatorUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
            //更新圆的半径
            currentRadius = (animation.animatedValue as Float).toInt() + startRadius
            postInvalidate()
        }
    }

    /**
     * 根据起始点将屏幕分成4个小矩形,maxRadius就是取它们中最大的矩形的对角线长度
     * 这样的话, 无论起始点在屏幕中的哪一个位置上, 我们绘制的圆形总是能覆盖屏幕
     */
    private fun updateMaxRadius() {
        val root = rootView ?: return
        //将屏幕分成4个小矩形
        val leftTop = RectF(0f, 0f, startX + startRadius, startY + startRadius)
        val rightTop = RectF(leftTop.right, 0f, root.right.toFloat(), leftTop.bottom)
        val leftBottom = RectF(0f, leftTop.bottom, leftTop.right, root.bottom.toFloat())
        val rightBottom = RectF(leftBottom.right, leftTop.bottom, root.right.toFloat(), leftBottom.bottom)
        //分别获取对角线长度
        val leftTopHypotenuse =
            sqrt(leftTop.width().toDouble().pow(2.0) + leftTop.height().toDouble().pow(2.0))
        val rightTopHypotenuse =
            sqrt(rightTop.width().toDouble().pow(2.0) + rightTop.height().toDouble().pow(2.0))
        val leftBottomHypotenuse =
            sqrt(leftBottom.width().toDouble().pow(2.0) + leftBottom.height().toDouble().pow(2.0))
        val rightBottomHypotenuse =
            sqrt(rightBottom.width().toDouble().pow(2.0) + rightBottom.height().toDouble().pow(2.0))
        //取最大值
        maxRadius = max(
            max(leftTopHypotenuse, rightTopHypotenuse),
            max(leftBottomHypotenuse, rightBottomHypotenuse)
        ).toInt()
    }

    /**
     * 添加到根视图
     */
    private fun attachToRootView() {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        rootView?.addView(this)
    }

    /**
     * 从根视图中移除并释放资源
     */
    private fun detachFromRootView() {
        rootView?.removeView(this).also { 
            rootView = null
        }
        if (background != null) {
            if (!background!!.isRecycled) {
                background!!.recycle()
            }
            background = null
        }
        if (paint != null) {
            paint = null
        }
    }

    /**
     * 更新屏幕截图
     */
    private fun updateBackground() {
        if (background != null && !background!!.isRecycled) {
            background!!.recycle()
        }
        background = getBitmapFromView(rootView ?: return)
    }

    override fun onDraw(canvas: Canvas) {
        //在新的图层上面绘制
        val layer = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        canvas.drawBitmap(background!!, 0f, 0f, null)
        canvas.drawCircle(startX, startY, currentRadius.toFloat(), paint!!)
        canvas.restoreToCount(layer)
    }

    /**
     * 设置动画结束监听器
     */
    fun setOnAnimationEndListener(listener: OnAnimationEndListener): RippleAnimation {
        onAnimationEndListener = listener
        return this
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return true
    }

    interface OnAnimationEndListener {
        fun onAnimationEnd()
    }

    companion object {

        fun create(onClickView: View): RippleAnimation {
            val context = onClickView.context
            val newWidth = onClickView.width / 2
            val newHeight = onClickView.height / 2
            //计算起点位置
            val startX = getAbsoluteX(onClickView) + newWidth
            val startY = getAbsoluteY(onClickView) + newHeight
            //起始半径
            //因为我们要避免遮挡按钮
            val radius = max(newWidth, newHeight)
            return RippleAnimation(context, startX, startY, radius)
        }

        /**
         * 由canvas更新背景截图（drawingCache已废弃）
         */
        private fun getBitmapFromView(view: View): Bitmap {
            view.measure(
                MeasureSpec.makeMeasureSpec(view.layoutParams.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(view.layoutParams.height, MeasureSpec.EXACTLY)
            )
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap
        }

        /**
         * 获取view在屏幕中的绝对x坐标
         */
        private fun getAbsoluteX(view: View): Float {
            var x = view.x
            val parent = view.parent
            if (parent is View) {
                x += getAbsoluteX(parent)
            }
            return x
        }

        /**
         * 获取view在屏幕中的绝对y坐标
         */
        private fun getAbsoluteY(view: View): Float {
            var y = view.y
            val parent = view.parent
            if (parent is View) {
                y += getAbsoluteY(parent)
            }
            return y
        }
    }
}
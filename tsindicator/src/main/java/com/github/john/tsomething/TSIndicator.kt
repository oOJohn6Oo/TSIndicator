package com.github.john.tsomething

import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.item_si.view.*

class TSIndicator(context: Context, attrs: AttributeSet?, defStyle: Int) :
    LinearLayout(context, attrs, defStyle) {
    private var density: Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        1f,
        context.resources.displayMetrics
    )
    private var dotCount: Int = 0
    private var dotDiameter: Float
    private var currentIndex: Int = 0
    private var viewPager: ViewPager? = null
    private var onPageChangeListener: ViewPager.OnPageChangeListener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1)

    init {
        val typedArray = context.obtainStyledAttributes(attrs,R.styleable.TSIndicator,defStyle,0)
        dotDiameter=typedArray.getDimension(R.styleable.TSIndicator_dot_diameter,10f)
        typedArray.recycle()
    }

    /**
     * @param position same as viewPager, 0 -> 1《==》0, 1 ->0 《==》0
     * @param positionOffset same as viewPager, 0 -> 1 <==> 0f -> 1f, 1 -> 0 《==》1f -> 0f
     *
     * Which means I can use ONE Int value to control both of changing items
     */
    fun startScroll(position: Int, positionOffset: Float) {
        if (position + 1 >= dotCount) return
        val currentItem = (getChildAt(position) as FrameLayout).inner_si
        val nextItem = (getChildAt(position + 1) as FrameLayout).inner_si

        currentItem.layoutParams = (currentItem.layoutParams as FrameLayout.LayoutParams).also {
            it.marginEnd = 0
            it.marginStart = (2*dotDiameter * density * positionOffset).toInt()
        }
        nextItem.layoutParams = (nextItem.layoutParams as FrameLayout.LayoutParams).also {
            it.marginEnd = (2*dotDiameter * density * (1 - positionOffset)).toInt()
            it.marginStart = 0
        }
    }

    /**
     * 设置直径（外圆）
     */
    fun setDotDiameter(diameterInDp:Float):TSIndicator{
        dotDiameter = diameterInDp
        return this
    }

    /**
     * 与viewPager链接
     */
    fun connect(viewPager: ViewPager):TSIndicator {
        this.viewPager = viewPager
        return this
    }

    /**
     * 与 connect() 配合使用
     */
    fun setOnPageChangeListener(onPageChangeListener: ViewPager.OnPageChangeListener): TSIndicator {
        this.onPageChangeListener = onPageChangeListener
        return this
    }

    fun setDotCount(count: Int): TSIndicator {
        dotCount = count
        return this
    }

    fun build(){
        //初始化 小圆点
        for (i in 0 until dotCount) {
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_si, this, false)
            itemView.layoutParams = with(itemView.layoutParams as LayoutParams){
                width = (2*dotDiameter * density).toInt()
                this
            }
            itemView.outer_si.layoutParams = (itemView.outer_si.layoutParams as FrameLayout.LayoutParams).also {
                it.width = (dotDiameter * density).toInt()
                it.height = (dotDiameter * density).toInt()
            }
            itemView.inner_si.layoutParams = (itemView.inner_si.layoutParams as FrameLayout.LayoutParams).apply {
                width = (dotDiameter * density).toInt()
                height = (dotDiameter * density).toInt()
                marginEnd = (2 * dotDiameter * density).toInt()
            }
            itemView.clipToOutline = true
            itemView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline?) {
                    outline?.setOval(
                        (density * dotDiameter/2).toInt(),
                        0,
                        (density * dotDiameter *1.5f).toInt(),
                        (density * dotDiameter).toInt()
                    )
                }
            }
            addView(itemView)
        }

        // 监听事件
        viewPager?.apply{
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    onPageChangeListener?.onPageScrollStateChanged(state)
                    if (state == ViewPager.SCROLL_STATE_DRAGGING) {

                        currentIndex = currentItem


                        this@TSIndicator.getChildAt(currentIndex).inner_si.apply {
                            scaleX = 1f
                            scaleY = 1f
                        }


                    }
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
                        currentIndex = currentItem


                        this@TSIndicator.getChildAt(currentIndex).inner_si.apply {
                            scaleX = 1.2f
                            scaleY = 1.2f
                        }
                    }
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    onPageChangeListener?.onPageScrolled(
                        position,
                        positionOffset,
                        positionOffsetPixels
                    )
                    if (position !in 0 until dotCount) return
                    else startScroll(position, positionOffset)
                }

                override fun onPageSelected(position: Int) {
                    onPageChangeListener?.onPageSelected(position)
                }

            })
        }
    }

}
package com.brotherd.bannerlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brotherd.bannerlibrary.impl.ImageLoader;
import com.brotherd.bannerlibrary.inter.OnBannerClickListener;
import com.brotherd.bannerlibrary.transformer.BGAPageTransformer;
import com.brotherd.bannerlibrary.transformer.TransitionEffect;
import com.brotherd.bannerlibrary.util.ScreenUtil;

import java.util.List;


/**
 * Created by dumingwei on 2016/10/22.
 * 参考开源库https://github.com/bingoogolapple/BGABanner-Android
 * 简单的图片轮播
 */

public class SimpleBanner extends RelativeLayout implements ViewPager.OnPageChangeListener {

    private static final String numberStyleFormat = "%1$d/%2$d";
    private static final int RMP = LayoutParams.MATCH_PARENT;
    private static final int RWC = LayoutParams.WRAP_CONTENT;
    private static final int LWC = LinearLayout.LayoutParams.WRAP_CONTENT;
    private String tag = SimpleBanner.class.getSimpleName();
    private int count;//图片轮播的数量
    private List imageUrls;//轮播的图片的加载地址
    private List<String> titles;//轮播的标题
    private BannerViewPager viewPager;
    private int duration = 800;//viewpager 切换页面的时间
    private BannerPagerAdapter adapter;
    private LinearLayout llIndicator;//使用线性布局放置轮播小圆点
    private int mPointGravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private OnBannerClickListener mOnBannerClickListener;
    //当前显示图片
    private int nowSelect = 0;
    private TextView textNumIndicator;
    private TextView mTipTv;//轮播的文字
    private Context context;
    private Handler handler = new Handler();
    private int delayTime = 4000;//默认轮播时间4000毫秒
    private boolean isAutoPlay;//默认自动轮播为false
    private boolean isNumIndicator;//标志是否是数字指示
    private int mIndicatorMargin;
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private TransitionEffect transitionEffect;
    private int mTipTextColor;//轮播文字的颜色
    private int mTipTextSize;//轮播标题的字体大小
    private int numIndicatorTextColor;//数字指示器的文字的颜色
    private int numIndicatorTextSize;//数字指示器的文字的大小
    private Drawable pointContainerBackground;//圆点指示器的背景
    private Drawable numIndicatorBackground;//数字指示器的背景
    private int mPointDrawableResId;
    private ImageLoader imageLoader;    //图片加载器
    private boolean cyclePlay;//是否循环播放
    private boolean abortAnimation = true;

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        this.mOnPageChangeListener = onPageChangeListener;
    }

    public void setOnBannerClickListener(OnBannerClickListener mOnBannerClickListener) {
        this.mOnBannerClickListener = mOnBannerClickListener;
    }

    public SimpleBanner(Context context) {
        this(context, null);
    }

    public SimpleBanner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        mTipTextSize = ScreenUtil.sp2px(context, 16);
        numIndicatorTextSize = ScreenUtil.sp2px(context, 16);
        mIndicatorWidth = ScreenUtil.dp2px(context, 6);
        mIndicatorHeight = ScreenUtil.dp2px(context, 6);
        mIndicatorMargin = ScreenUtil.dp2px(context, 4);
        //初始化属性
        initTypedArray(context, attrs);
        initView(context);
    }

    private void initTypedArray(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleBanner);
        mIndicatorWidth = typedArray.getDimensionPixelSize(R.styleable.SimpleBanner_indicator_width, mIndicatorWidth);
        mIndicatorHeight = typedArray.getDimensionPixelSize(R.styleable.SimpleBanner_indicator_height, mIndicatorHeight);
        mIndicatorMargin = typedArray.getDimensionPixelSize(R.styleable.SimpleBanner_indicator_margin, mIndicatorMargin);
        delayTime = typedArray.getInt(R.styleable.SimpleBanner_delay_time, delayTime);
        isAutoPlay = typedArray.getBoolean(R.styleable.SimpleBanner_is_auto_play, isAutoPlay);
        isNumIndicator = typedArray.getBoolean(R.styleable.SimpleBanner_is_num_indicator, false);
        numIndicatorTextColor = typedArray.getColor(R.styleable.SimpleBanner_num_indicator_text_color, Color.WHITE);
        numIndicatorTextSize = typedArray.getDimensionPixelSize(R.styleable.SimpleBanner_num_indicator_text_size, numIndicatorTextSize);
        numIndicatorBackground = typedArray.getDrawable(R.styleable.SimpleBanner_num_indicator_bg);
        pointContainerBackground = typedArray.getDrawable(R.styleable.SimpleBanner_point_container_background);
        mPointDrawableResId = typedArray.getResourceId(R.styleable.SimpleBanner_point_drawable, R.drawable.bga_banner_selector_point_solid);
        mTipTextColor = typedArray.getColor(R.styleable.SimpleBanner_tip_text_color, Color.WHITE);
        mTipTextSize = typedArray.getDimensionPixelSize(R.styleable.SimpleBanner_tip_text_size, mTipTextSize);
        mPointGravity = typedArray.getInt(R.styleable.SimpleBanner_point_gravity, mPointGravity);
        int ordinal = typedArray.getInt(R.styleable.SimpleBanner_transition_effect, TransitionEffect.Default.ordinal());
        transitionEffect = TransitionEffect.values()[ordinal];
        cyclePlay = typedArray.getBoolean(R.styleable.SimpleBanner_banner_cycle, true);
        typedArray.recycle();
    }

    private void initView(Context context) {
        RelativeLayout pointContainerRl = new RelativeLayout(context);
        if (Build.VERSION.SDK_INT >= 16) {
            pointContainerRl.setBackground(pointContainerBackground);
        } else {
            pointContainerRl.setBackgroundDrawable(pointContainerBackground);
        }
        LayoutParams pointContainerLp = new LayoutParams(RMP, RWC);
        // 处理圆点在顶部还是底部
        if ((mPointGravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.TOP) {
            pointContainerLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        } else {
            pointContainerLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        addView(pointContainerRl, pointContainerLp);

        LayoutParams indicatorLp = new LayoutParams(RWC, RWC);
        indicatorLp.addRule(CENTER_VERTICAL);
        if (isNumIndicator) {
            textNumIndicator = new TextView(getContext());
            textNumIndicator.setId(R.id.banner_indicator_id);
            textNumIndicator.setGravity(Gravity.CENTER_VERTICAL);
            textNumIndicator.setSingleLine();
            textNumIndicator.setEllipsize(TextUtils.TruncateAt.END);
            textNumIndicator.setTextColor(numIndicatorTextColor);
            textNumIndicator.setTextSize(TypedValue.COMPLEX_UNIT_PX, numIndicatorTextSize);
            textNumIndicator.setVisibility(INVISIBLE);
            if (numIndicatorBackground != null) {
                if (Build.VERSION.SDK_INT >= 16) {
                    textNumIndicator.setBackground(numIndicatorBackground);
                } else {
                    textNumIndicator.setBackgroundDrawable(numIndicatorBackground);
                }
            }
            pointContainerRl.addView(textNumIndicator, indicatorLp);
        } else {
            llIndicator = new LinearLayout(context);
            llIndicator.setId(R.id.banner_indicator_id);
            llIndicator.setOrientation(LinearLayout.HORIZONTAL);
            pointContainerRl.addView(llIndicator, indicatorLp);
        }
        //处理轮播标题
        LayoutParams tipLp = new LayoutParams(RMP, RWC);
        tipLp.addRule(CENTER_VERTICAL);
        tipLp.leftMargin = mIndicatorMargin;
        tipLp.rightMargin = mIndicatorMargin;
        mTipTv = new TextView(context);
        mTipTv.setGravity(Gravity.CENTER_VERTICAL);
        mTipTv.setSingleLine(true);
        mTipTv.setEllipsize(TextUtils.TruncateAt.END);
        mTipTv.setTextColor(mTipTextColor);
        mTipTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTipTextSize);
        pointContainerRl.addView(mTipTv, tipLp);

        int horizontalGravity = mPointGravity & Gravity.HORIZONTAL_GRAVITY_MASK;

        // 处理指示器在左边、右边还是水平居中
        if (horizontalGravity == Gravity.LEFT) {
            indicatorLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tipLp.addRule(RelativeLayout.RIGHT_OF, R.id.banner_indicator_id);
            mTipTv.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        } else if (horizontalGravity == Gravity.RIGHT) {
            indicatorLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            tipLp.addRule(RelativeLayout.LEFT_OF, R.id.banner_indicator_id);
        } else {
            indicatorLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            tipLp.addRule(RelativeLayout.LEFT_OF, R.id.banner_indicator_id);
        }
    }

    public SimpleBanner setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }

    public SimpleBanner isAutoPlay(boolean isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
        return this;
    }

    /**
     * 设置从一张图片切换到下一张的延迟时间
     *
     * @param delayTime
     * @return
     */
    public SimpleBanner setDelayTime(int delayTime) {
        this.delayTime = delayTime;
        return this;
    }

    /**
     * 设置ViewPager切换的时间
     *
     * @param duration
     * @return
     */
    public SimpleBanner setViewPagerChangeDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public SimpleBanner setTitles(List<String> titles) {
        if (imageUrls != null && titles.size() != imageUrls.size()) {
            throw new IllegalArgumentException("the size of titles must be same as imageUrls's size");
        }
        this.titles = titles;
        return this;
    }

    public SimpleBanner setImages(List<?> imageUrls) {
        if (titles != null && titles.size() != imageUrls.size()) {
            throw new IllegalArgumentException("the size of imageUrls must be same as titles's size");
        }
        this.imageUrls = imageUrls;
        return this;
    }

    public SimpleBanner setTransitionEffect(TransitionEffect transitionEffect) {
        this.transitionEffect = transitionEffect;
        return this;
    }

    /**
     * 如果在RecyclerView中使用的话，应该设置 abortAnimation 为false
     *
     * @param abortAnimation
     * @return
     */
    public SimpleBanner setAbortAnimation(boolean abortAnimation) {
        this.abortAnimation = abortAnimation;
        return this;
    }

    public SimpleBanner setCyclePlay(boolean cyclePlay) {
        this.cyclePlay = cyclePlay;
        return this;
    }

    public void start() {
        if (imageUrls == null || imageUrls.size() <= 0) {
            throw new IllegalStateException("when start imageUrls can not be null or empty");
        }
        if (imageLoader == null) {
            throw new NullPointerException("when start the imageLoader can not be null");
        }
        count = imageUrls.size();
        if (count > 1) {
            initIndicator();
        }
        initViewPager();
    }

    /**
     * 添加轮播图片底部的小圆点，初始化数字指示器
     */
    private void initIndicator() {
        if (llIndicator != null) {
            llIndicator.removeAllViews();
            for (int i = 0; i < count; i++) {
                ImageView imageView = new ImageView(context);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LWC, LWC);
                lp.width = mIndicatorWidth;
                lp.height = mIndicatorHeight;
                lp.leftMargin = mIndicatorMargin;
                lp.rightMargin = mIndicatorMargin;
                imageView.setLayoutParams(lp);
                imageView.setImageResource(mPointDrawableResId);
                llIndicator.addView(imageView);
            }
        }
        if (textNumIndicator != null) {
            textNumIndicator.setVisibility(VISIBLE);
        }
    }

    private void initViewPager() {
        if (viewPager != null && this.equals(viewPager.getParent())) {
            this.removeView(viewPager);
            viewPager = null;
        }
        viewPager = new BannerViewPager(getContext());
        viewPager.setOverScrollMode(OVER_SCROLL_ALWAYS);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setFocusable(true);
        viewPager.setAbortAnimation(abortAnimation);
        adapter = new BannerPagerAdapter(imageUrls);
        viewPager.setAdapter(adapter);
        if (count > 1) {
            viewPager.addOnPageChangeListener(this);
            viewPager.setPageTransformer(true, BGAPageTransformer.getPageTransformer(transitionEffect));
            viewPager.setScrollable(true);
            //设置页面切换的时间
            if (duration >= 0 && duration <= 2000) {
                viewPager.setPageChangeDuration(duration);
            }
            changeLoopPoint(nowSelect);
        } else {
            //如果只有一张，则设置viewPager不可滑动
            viewPager.setScrollable(false);
        }
        addView(viewPager, 0, new LayoutParams(RMP, RMP));
    }

    private void changeLoopPoint(int position) {
        nowSelect = position;
        if (isNumIndicator) {
            textNumIndicator.setText(String.format(numberStyleFormat, nowSelect + 1, count));
        } else {
            for (int i = 0; i < llIndicator.getChildCount(); i++) {
                llIndicator.getChildAt(i).setEnabled(false);
            }
            llIndicator.getChildAt(nowSelect).setEnabled(true);
        }

        if (mTipTv != null && titles != null) {
            mTipTv.setText(titles.get(nowSelect));
        }
    }

    private void resume() {
        if (isAutoPlay) {
            handler.removeCallbacks(task);
            handler.postDelayed(task, delayTime);
        }
    }

    private void stop() {
        handler.removeCallbacks(task);
    }

    /**
     * 处理触摸事件
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isAutoPlay) {
            int action = ev.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL
                    || action == MotionEvent.ACTION_OUTSIDE) {
                resume();
            } else if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                stop();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        Log.e(tag, "onPageSelected position=" + position);
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(position);
        }
        position %= count;
        //改变相应的指示器
        if (count > 1) {
            changeLoopPoint(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private final Runnable task = new Runnable() {

        @Override
        public void run() {
            int num = adapter.getCount();
            if (num > 2) {
                int index = viewPager.getCurrentItem();
                index = index % (num - 2) + 1;
                viewPager.setCurrentItem(index);
            }
            handler.postDelayed(task, delayTime);
        }
    };

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            resume();
        } else if (visibility == INVISIBLE) {
            stop();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    /**
     * ViewPager 的适配器
     */
    class BannerPagerAdapter extends PagerAdapter {

        /**
         * 这个size一定要比较大才行，默认为轮播图片张数的30倍。
         */
        private final int FAKE_BANNER_SIZE = count * 30;
        //轮播图片的地址
        private List imgUrls;

        public BannerPagerAdapter(List imgUrls) {
            this.imgUrls = imgUrls;
        }

        @Override
        public int getCount() {
            if (count == 1) {
                return 1;
            }
            if (cyclePlay) {
                return FAKE_BANNER_SIZE;
            } else {
                return count;
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            position %= count;
            final int pos = position;
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageLoader.displayImage(context, imgUrls.get(position), imageView);
            if (mOnBannerClickListener != null) {
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnBannerClickListener.OnBannerClick(pos);
                    }
                });
            }
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            if (cyclePlay) {
                if (count > 1) {
                    int position = viewPager.getCurrentItem();
                    Log.e(tag, "finishUpdate" + position);
                    if (position == 0) {
                        position = count;
                        viewPager.setCurrentItem(position, false);
                    } else if (position == FAKE_BANNER_SIZE - 1) {
                        position = count - 1;
                        viewPager.setCurrentItem(position, false);
                    }
                }
            }
        }
    }
}

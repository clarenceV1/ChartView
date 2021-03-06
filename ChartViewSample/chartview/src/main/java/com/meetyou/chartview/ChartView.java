package com.meetyou.chartview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * 图表控件
 * Created by lwh on 2015/9/21.
 */
public class ChartView extends View {
    private static final String TAG = "ChartView";
    private Context mContext;
    private int mDensity;
    //Scroller用于手势滚动和弹性
    private Scroller mScroller;
    //当前停留的位置
    private int mScreenIndex;

    //图表配置
    private ChartViewConfig chartViewConfig;

    //格子画笔
    private Paint mPaintGrid;

    //竖向刻度画笔 字体大的
    private Paint mPaintLable;
    //竖向刻度画笔 字体小的
    private Paint mPaintLableSub;
    //竖向刻度单位画笔
    private Paint mPaintLableUnit;
    //竖向刻度额外填充x值
    private int verical_unit_extral_x_space;

    //横向向刻度单位画笔 字体大的
    private Paint mPaintHorizontalLable;
    //横向向刻度单位画笔 字体小的
    private Paint mPaintHorizontalLableSub;

    //线的path
    private Path[] mPathSet;
    //画点线与X轴的区域
    private Path[] mPathSetRegion;
    private Path mPath;
    //线的画笔
    private Paint mPaintPath;
    private Path mPathRegion;
    //画电线连接与X轴形成的区域Path
    private Path mPathConnectRegion;
    private Paint mPaintPathConnectRegion;
    private  List<PointValue> listRegionTemp = new ArrayList<>();
    //线的画笔
    private Paint mPaintPathRegion;
    //点的内圆和外圆 画笔
    private Paint mPaintCircle, mPaintCircleOutSide;
    //游标画笔
    private Paint mPaintIndicator;
    //游标标题画笔
    private Paint mPaintIndicatorTitle;
    private Paint mPaintIndicatorSubTitle;
    private Paint mPaintIndicatorTitleUnit;
    //游标画笔上部分连线
    private Paint mPaintIndicatorLineTop;
    //游标画笔下部分连线
    private Paint mPaintIndicatorLineBottom;

    //手势参数
    private VelocityTracker mVelocityTracker = null;
    private static final int TOUCH_STATE_REST = 0;
    public static int SNAP_VELOCITY = 600;
    private float mLastionMotionX = 0;
    private float mLastionMotionY = 0;
    private int mLastScrollX = 0;
    protected boolean mIsPressd = false;
    //游标资源
    private Bitmap mBitmapIndicator;

    //是否计算值
    private boolean mIsCaculateValue = true;

    //滚动监听
    private OnChartViewChangeListener mListener;
    public void setOnChartViewChangeListener(OnChartViewChangeListener listener){
        mListener = listener;
    }

    public ChartView(Context context) {
        super(context);
        mContext = context;
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public ChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

    }

    /**
     * 初始化
     *
     * @param config
     */
    public void init(ChartViewConfig config) {
        mDensity = (int) getResources().getDisplayMetrics().density;

        mScroller = new Scroller(mContext);
        //global config
        chartViewConfig = config;
        //格子线和颜色
        mPaintGrid = new Paint();
        verical_unit_extral_x_space = 20;
        //竖向大刻度文本画笔
        mPaintLable = new Paint();
        if (config.getVerical_unit_lable_color() > 0)
            mPaintLable.setColor(getResources().getColor(config.getVerical_unit_lable_color()));
        mPaintLable.setTextSize(12 * mDensity);
        mPaintLable.setAntiAlias(true);
        //竖向小刻度文本画笔
        mPaintLableSub = new Paint();
        if (config.getVerical_unit_lable_sub_color() > 0)
            mPaintLableSub.setColor(getResources().getColor(config.getVerical_unit_lable_sub_color()));
        mPaintLableSub.setTextSize(10 * mDensity);
        mPaintLableSub.setAntiAlias(true);
        //竖向单位文本画笔
        mPaintLableUnit = new Paint();
        if (config.getVerical_unit_color() > 0)
            mPaintLableUnit.setColor(getResources().getColor(config.getVerical_unit_color()));
        mPaintLableUnit.setTextSize(15 * mDensity);
        mPaintLableUnit.setAntiAlias(true);
        //横向向刻度单位画笔 字体大的
        mPaintHorizontalLable = new Paint();
        if (config.getVerical_unit_lable_color() > 0)
            mPaintHorizontalLable.setColor(getResources().getColor(config.getVerical_unit_lable_color()));
        mPaintHorizontalLable.setTextSize(12 * mDensity);
        mPaintHorizontalLable.setAntiAlias(true);
        //横向向刻度单位画笔 字体小的
        mPaintHorizontalLableSub = new Paint();
        if (config.getVerical_unit_lable_sub_color() > 0)
            mPaintHorizontalLableSub.setColor(getResources().getColor(config.getVerical_unit_lable_sub_color()));
        mPaintHorizontalLableSub.setTextSize(10 * mDensity);
        mPaintHorizontalLableSub.setAntiAlias(true);

        //画线
        mPaintPath = new Paint();
        mPaintPath.setStyle(Paint.Style.STROKE);
        mPaintPath.setStrokeWidth(mDensity * 1.5f);
        if (config.getPath_line_color() > 0)
            mPaintPath.setColor(getResources().getColor(config.getPath_line_color()));
        mPaintPath.setAntiAlias(true);

        //区域画笔
        mPaintPathRegion = new Paint();
        mPaintPathRegion.setStyle(Paint.Style.FILL);
        //mPaintPathRegion.setStrokeWidth(mDensity * 1.5f);
        mPaintPathRegion.setAntiAlias(true);
        if(chartViewConfig.getRegion_color()>0){
            mPaintPathRegion.setColor(getResources().getColor(config.getRegion_color()));
        }
        mPaintPathRegion.setAlpha(80);

        mPaintPathConnectRegion = new Paint();
        mPaintPathConnectRegion.setStyle(Paint.Style.FILL);
        mPaintPathConnectRegion.setAntiAlias(true);
        if(chartViewConfig.getRegion_connect_color()>0){
            mPaintPathConnectRegion.setColor(getResources().getColor(config.getRegion_connect_color()));
        }
        mPaintPathConnectRegion.setAlpha(80);

        //点的内圆
        mPaintCircle = new Paint();
        mPaintCircle.setStyle(Paint.Style.FILL);
        mPaintCircle.setStrokeWidth(mDensity * 0.5f);
        if (config.getPoint_circle_color_interval() > 0)
            mPaintCircle.setColor(getResources().getColor(config.getPoint_circle_color_interval()));
        mPaintCircle.setTextSize(15 * mDensity);
        mPaintCircle.setAntiAlias(true);

        //点的外圆
        mPaintCircleOutSide = new Paint();
        mPaintCircleOutSide.setStyle(Paint.Style.STROKE);
        mPaintCircleOutSide.setStrokeWidth(mDensity * 0.5f);
        if (config.getPoint_circle_color_outside() > 0)
            mPaintCircleOutSide.setColor(getResources().getColor(config.getPoint_circle_color_outside()));
        mPaintCircleOutSide.setAntiAlias(true);

        //游标圆的画笔
        mPaintIndicator = new Paint();
        mPaintIndicator.setStyle(Paint.Style.FILL);
        mPaintIndicator.setStrokeWidth(mDensity * 2.0f);
        if (config.getIndicator_color() > 0)
            mPaintIndicator.setColor(getResources().getColor(config.getIndicator_color()));
        mPaintIndicator.setAntiAlias(true);

        //游标连线
        mPaintIndicatorLineTop = new Paint();
        mPaintIndicatorLineTop.setStyle(Paint.Style.FILL);
        mPaintIndicatorLineTop.setStrokeWidth(mDensity * 2.0f);
        if (config.getIndicator_color() > 0)
            mPaintIndicatorLineTop.setColor(getResources().getColor(config.getIndicator_color()));
        mPaintIndicatorLineTop.setAntiAlias(true);
        //游标连线
        mPaintIndicatorLineBottom = new Paint();
        mPaintIndicatorLineBottom.setStyle(Paint.Style.FILL);
        mPaintIndicatorLineBottom.setStrokeWidth(mDensity * 1.0f);
        if (config.getIndicator_color() > 0)
            mPaintIndicatorLineBottom.setColor(getResources().getColor(config.getIndicator_color()));
        mPaintIndicatorLineBottom.setAntiAlias(true);
        //游标标题
        mPaintIndicatorTitle = new Paint();
        mPaintIndicatorTitle.setTextSize(14 * mDensity);
        if (config.getIndicator_title_color() > 0)
            mPaintIndicatorTitle.setColor(getResources().getColor(chartViewConfig.getIndicator_title_color()));
        //游标标题单位
        mPaintIndicatorTitleUnit = new Paint();
        mPaintIndicatorTitleUnit.setTextSize(10 * mDensity);
        if (config.getIndicator_title_color() > 0)
            mPaintIndicatorTitleUnit.setColor(getResources().getColor(chartViewConfig.getIndicator_title_color()));
        //游标子标题
        mPaintIndicatorSubTitle = new Paint();
        mPaintIndicatorSubTitle.setTextSize(10 * mDensity);
        if (config.getIndicator_title_color() > 0)
            mPaintIndicatorSubTitle.setColor(getResources().getColor(chartViewConfig.getIndicator_title_color()));


        invalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //画格子
        drawGrid(canvas);
        //画横向刻度和单位
        drawHorizontalUnit(canvas);
        //计算点线坐标
        caculatePointValue();
        //点线区域是否需要闭合渲染
        drawFillPointConnectRegion(canvas);
        //画区域
        drawPointRegion(canvas);
        //画点和线
        drawPointAndPath(canvas);
        //画顶部游标
        drawIndicator(canvas);
        //画竖向刻度和单位,放在最后是为了避免画区域部分覆盖了这个刻度
        drawVericalUnit(canvas);
        //选中初始位置
        setSelection();

        //当前x刻度
        mScreenIndex = getScrollX() / chartViewConfig.getItem_width();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    /**
     * 画指示器
     *
     * @param canvas
     */
    protected void drawIndicator(Canvas canvas) {

        //indicator坐标
        //x轴中间值
        int indicator_x = getWidth() / 2 + getScrollX();
        //游标默认y轴位置
        int indicator_y =chartViewConfig.getIndicator_radius() * 3 / 2;
        //游标半径
        //------ 计算上部分连线的Y方式的结束值
        int radius=50;
        if(chartViewConfig.getIndicatorBgRes()>0) {
            if (mBitmapIndicator == null) {
                mBitmapIndicator = BitmapFactory.decodeResource(getResources(), chartViewConfig.getIndicatorBgRes());
            }
            if(mBitmapIndicator!=null)
                 radius = mBitmapIndicator.getWidth()/2;
        }else{
            radius = chartViewConfig.getIndicator_radius();
        }
        //上部分连接线坐标
        //上部分连线x开始和结束
        float line_top_x_start = indicator_x;
        float line_top_x_end = indicator_x;
        //上部分连线y 默认开始值
        float line_top_y_start = indicator_y + radius;
        //上部分连线y 默认结束值
        float line_top_y_end = indicator_y + radius;

        //当前点位置
        int index = 0;
        boolean isAtPoint = false;
        for (int i = chartViewConfig.getListPoint().size() - 1; i >= 0; i--) {
            Log.d(TAG,"-->i:"+i+"--point x:"+chartViewConfig.getListPoint().get(i).x+"-->indicator_x:"+indicator_x);
            float x = chartViewConfig.getListPoint().get(i).x;
            if (indicator_x+5 >=(int)x) {
                 index = i;
                break;
            }
            if (indicator_x+5 == (int)x) {
                isAtPoint = true;
            }
        }
        //------ 计算上部分连线的Y方式的结束值 start
        //计算左右边界x值，防止画超出范围
        getMinAndManScrollerValue();
        //超过最小，取最小
        if (getScrollX() <= minX) {
            line_top_y_end = chartViewConfig.getListPoint().get(0).y;
            //超过最大，取最大
        } else if (getScrollX() >= maxX) {
            line_top_y_end = chartViewConfig.getListPoint().get(chartViewConfig.getListPoint().size() - 1).y;
            //中间按比例取值
        } else {
            //防止越界
            if (index + 1 < chartViewConfig.getListPoint().size()) {
                //与下一个点的x和y方向的差值
                int width_x = (int) (chartViewConfig.getListPoint().get(index + 1).x - chartViewConfig.getListPoint().get(index).x);
                int width_y = (int) (chartViewConfig.getListPoint().get(index + 1).y - chartViewConfig.getListPoint().get(index).y);
                //计算出比例
                int cha_x = indicator_x - (int) chartViewConfig.getListPoint().get(index).x;
                float progress = cha_x / (width_x * 1.0f);
                float cha_y = progress * width_y;
                //最终值
                line_top_y_end = chartViewConfig.getListPoint().get(index).y + cha_y;
                Log.d(TAG, "progress:" + progress + "-->cha_y:" + cha_y + "--chax:" + cha_x + "-->line_top_y_end:" + line_top_y_end + "-->index:" + index);
            } else {
                line_top_y_end = chartViewConfig.getListPoint().get(chartViewConfig.getListPoint().size() - 1).y;
            }
        }
        //------ 计算上部分连线的Y方式的结束值 end

        //绘制上半部分的连线
        if(!chartViewConfig.isIndicatorMoveWithPoint()){
            canvas.drawLine(line_top_x_start, line_top_y_start, line_top_x_end, line_top_y_end, mPaintIndicatorLineTop);
        }else{
            line_top_y_start = line_top_y_end-radius-50;
            canvas.drawLine(line_top_x_start, line_top_y_start, line_top_x_end, line_top_y_end, mPaintIndicatorLineTop);
        }


        //绘制游标,自定义游标背景
        if(chartViewConfig.getIndicatorBgRes()>0){
            float left = indicator_x - radius;
            if(mBitmapIndicator!=null){
                if(!chartViewConfig.isIndicatorMoveWithPoint()){
                    float top = line_top_y_start-radius;
                    canvas.drawBitmap(mBitmapIndicator, left, top, mPaintIndicator);
                }else{
                    float top = line_top_y_start-radius;
                    canvas.drawBitmap(mBitmapIndicator,left,top,mPaintIndicator);
                }
            }
         //默认游标，圆形
        }else{
            if(!chartViewConfig.isIndicatorMoveWithPoint()){
                line_top_y_start-=radius;
                canvas.drawCircle(indicator_x,line_top_y_start , radius, mPaintIndicator);
            }else{
                canvas.drawCircle(indicator_x, line_top_y_start, radius, mPaintIndicator);
            }
        }


        //下部分连线坐标
        int line_bottom_x_end = indicator_x;
        int line_bottom_y_end = chartViewConfig.getRow() * chartViewConfig.getItem_height();
        canvas.drawLine(line_top_x_end, line_top_y_end, line_bottom_x_end, line_bottom_y_end, mPaintIndicatorLineBottom);

        //绘制上半部分连线和下半部分连线的小圆圈,是节点就大圆，非节点就小圆
        //canvas.drawCircle(line_top_x_end, line_top_y_end,mDensity*3, mPaintIndicatorLineTop);
        if (isAtPoint) {
            canvas.drawCircle(line_top_x_end, line_top_y_end, mDensity * 5, mPaintIndicatorLineTop);
        } else {
            canvas.drawCircle(line_top_x_end, line_top_y_end, mDensity * 3, mPaintIndicatorLineTop);
        }

        //绘制游标标题
        String tilte = "无";
        if (index < chartViewConfig.getListPoint().size() && index >= 0) {
            tilte = chartViewConfig.getListPoint().get(index).title;
        }
        if (TextUtils.isEmpty(tilte)) {
            tilte = "无";
        }
        Rect rectTitle = new Rect();
        mPaintIndicatorTitle.getTextBounds(tilte, 0, tilte.length(), rectTitle);
        canvas.drawText(tilte, indicator_x - (rectTitle.width() / 2), line_top_y_start, mPaintIndicatorTitle);

        //绘制游标标题单位
        String title_unit = chartViewConfig.getIndicator_title_unit();
        Rect rectTitleUnit = new Rect();
        mPaintIndicatorTitleUnit.getTextBounds(title_unit, 0, title_unit.length(), rectTitleUnit);
        canvas.drawText(title_unit, indicator_x - (rectTitle.width() / 2) + rectTitle.width() + 10, line_top_y_start, mPaintIndicatorTitleUnit);

        //绘制游标子标题
        String tilteSub = "子标题";
        if (index < chartViewConfig.getListPoint().size() && index >= 0) {
            tilteSub = chartViewConfig.getListPoint().get(index).title_sub;
        }
        if (TextUtils.isEmpty(tilteSub)) {
            tilteSub = "子标题";
        }
        Rect rectTitleSub = new Rect();
        mPaintIndicatorSubTitle.getTextBounds(tilteSub, 0, tilteSub.length(), rectTitleSub);
        canvas.drawText(tilteSub, indicator_x - (rectTitleSub.width() / 2), line_top_y_start + rectTitle.height(), mPaintIndicatorSubTitle);


    }

    /**
     * 画格子
     *
     * @param canvas
     */
    protected void drawGrid(Canvas canvas) {
        if(chartViewConfig.isShowGridLine()){
            if (chartViewConfig.getGrid_line_color() > 0)
                mPaintGrid.setColor(getResources().getColor(chartViewConfig.getGrid_line_color()));
            mPaintGrid.setStrokeWidth(mDensity * 0.5f);
            //画横线 ,左边预留getCloumn/2个网格
            int len = 16;
            len = chartViewConfig.getCloumn() * 2 + chartViewConfig.getListHorizontalKedu().size() - 1;
            for (int i = 0; i < chartViewConfig.getRow(); i++) {
                float startX = -chartViewConfig.getItem_width() * (chartViewConfig.getCloumn() / 2);
                float startY = i * chartViewConfig.getItem_height();
                float stopX = (len - chartViewConfig.getCloumn() / 2) * chartViewConfig.getItem_width();//(chartViewConfig.getListHorizontalKeduValue().size() +chartViewConfig.getCloumn())* chartViewConfig.getItem_width();
                float stopY = i * chartViewConfig.getItem_height();
                canvas.drawLine(startX, startY, stopX, stopY, mPaintGrid);
            }
            //画竖线
            for (int i = 0; i < len + 1; i++) {
                float startX = (i - chartViewConfig.getCloumn() / 2) * chartViewConfig.getItem_width();
                float startY = 0;
                float stopX = (i - chartViewConfig.getCloumn() / 2) * chartViewConfig.getItem_width();
                float stopY = chartViewConfig.getRow() * chartViewConfig.getItem_height();
                canvas.drawLine(startX, startY, stopX, stopY, mPaintGrid);
            }
        }
    }

    /**
     * 绘制竖向刻度 和单位
     * Draw unit & incremental lables
     *
     * @param canvas
     */
    protected void drawVericalUnit(Canvas canvas) {

        //画lable line 竖线
        int bottomY = chartViewConfig.getRow() * chartViewConfig.getItem_height();
        float x_ = getScrollX() + chartViewConfig.getVerical_kedu_leftmargin();
        canvas.drawLine(x_, 0, x_, bottomY, mPaintGrid);

        //每五个格子为一个大刻度
        int levelCount = 5;
        //刻度文案
        String unit_text = "";
        //格子数目
        final int count = (int) (chartViewConfig.getVerical_unit_end() - chartViewConfig.getVerical_unit_start()) / (int) chartViewConfig.getVerical_unit_incremetal();

        //从下往上画
        for (int i = 0; i < count + 1; i++) {
            //value
            float big_value = chartViewConfig.getVerical_unit_start() + i * chartViewConfig.getVerical_unit_incremetal();
            float value = i * chartViewConfig.getVerical_unit_incremetal();
            //取mode
            if (chartViewConfig.getVerical_unit_incremetal() < 1) {
                value = value % 1;
            } else if (chartViewConfig.getVerical_unit_incremetal() < 10) {
                value = value % 10;
            } else if (chartViewConfig.getVerical_unit_incremetal() < 100) {
                value = value % 100;
            }
            Rect rect = new Rect();
            //需要分大小刻度
            if (chartViewConfig.isVerical_need_to_fragment()) {
                //绘制大刻度
                if (i % levelCount == 0) {
                    //skip first one
                    if (i == 0)
                        continue;
                    if (chartViewConfig.verical_lable_use_integer) {
                        unit_text = String.valueOf((int) big_value);
                    } else if (chartViewConfig.verical_lable_use_integer) {
                        unit_text = String.valueOf(big_value);
                    } else {
                        unit_text = String.valueOf(big_value);
                    }
                    mPaintLable.getTextBounds(unit_text, 0, unit_text.length(), rect);
                    float x = getScrollX() + chartViewConfig.getVerical_kedu_leftmargin() - rect.width() - verical_unit_extral_x_space;
                    float y = (chartViewConfig.getRow() - i) * chartViewConfig.getItem_height();
                    canvas.drawText(unit_text, x, y, mPaintLable);
                    //绘制小刻度
                } else {
                    //skip first value
                    if (value == 0) {
                        continue;
                    } else {
                        if (chartViewConfig.verical_lable_use_integer) {
                            unit_text = String.valueOf((int) value);
                        } else {
                            unit_text = String.valueOf(value);
                        }
                    }
                    mPaintLableSub.getTextBounds(unit_text, 0, unit_text.length(), rect);
                    //draw text
                    float x = getScrollX() + chartViewConfig.getVerical_kedu_leftmargin() - rect.width() - verical_unit_extral_x_space;
                    //1 的宽度太小，造成不协调,在此特殊处理
                    if (unit_text.equals("1")) {
                        x -= (2 * mDensity);
                    }
                    float y = (chartViewConfig.getRow() - i) * chartViewConfig.getItem_height();
                    canvas.drawText(unit_text, x, y, mPaintLableSub);
                }
            } else {
                if (i == 0)
                    continue;
                if (chartViewConfig.verical_lable_use_integer) {
                    unit_text = String.valueOf((int) big_value);
                } else if (chartViewConfig.verical_lable_use_integer) {
                    unit_text = String.valueOf(big_value);
                } else {
                    unit_text = String.valueOf(big_value);
                }
                mPaintLable.getTextBounds(unit_text, 0, unit_text.length(), rect);
                float x = getScrollX() + chartViewConfig.getVerical_kedu_leftmargin() - rect.width() - verical_unit_extral_x_space;
                float y = (chartViewConfig.getRow() - i) * chartViewConfig.getItem_height();
                canvas.drawText(unit_text, x, y, mPaintLable);

                //是否需要刻度线
                if(chartViewConfig.isVerical_kedu_line_show()){
                    float startX = getScrollX() + chartViewConfig.getVerical_kedu_leftmargin();
                    float stopX = startX+15;
                    float startY =(chartViewConfig.getRow() - i) * chartViewConfig.getItem_height();
                    float stopY = (chartViewConfig.getRow() - i) * chartViewConfig.getItem_height();
                    canvas.drawLine(startX, startY, stopX, stopY, mPaintGrid);
                }

            }
        }

        //绘制刻度单位
        Rect rectUnit = new Rect();
        unit_text = chartViewConfig.getVerical_unit_text();
        mPaintLableUnit.getTextBounds(unit_text, 0, unit_text.length(), rectUnit);
        float x = getScrollX() + chartViewConfig.getVerical_kedu_leftmargin() - rectUnit.width() - verical_unit_extral_x_space;
        float y = (chartViewConfig.getRow() - count - 1) * chartViewConfig.getItem_height() - verical_unit_extral_x_space;
        canvas.drawText(unit_text, x, y, mPaintLableUnit);
    }


    /**
     * 绘制横向刻度和单位,以及横刻度的值
     *
     * @param canvas
     */
    private void drawHorizontalUnit(Canvas canvas) {
        //刻度文案
        String unit_text = "";
        //格子数目
        final int count = chartViewConfig.getListHorizontalKedu().size();
        //文案y轴底部
        int bottomY = chartViewConfig.getRow() * chartViewConfig.getItem_height() + 40;
        for (int i = 0; i < count; i++) {
            if (i >= mScreenIndex - chartViewConfig.getCloumn() / 2 && i <= mScreenIndex + chartViewConfig.getCloumn()) {
                KeduValue model = chartViewConfig.getListHorizontalKedu().get(i);
                unit_text = TextUtils.isEmpty(model.display_value) ? model.value : model.display_value;
                if (!TextUtils.isEmpty(unit_text)) {
                    Rect rect = new Rect();
                    mPaintHorizontalLable.getTextBounds(unit_text, 0, unit_text.length(), rect);
                    float x = (i + chartViewConfig.getCloumn() / 2) * chartViewConfig.getItem_width() - rect.width() / 2;//- getScrollX();
                    model.current_x = x + rect.width() / 2;
                    Log.d(TAG, "-->mScreenIndex:" + mScreenIndex + "-->i:" + i + "-->x:" + x + "-->getScrollX():" + getScrollX());
                    canvas.drawText(unit_text, x, bottomY, mPaintHorizontalLable);
                }
                unit_text = model.value_unit;
                if (!TextUtils.isEmpty(unit_text)) {
                    Rect rect = new Rect();
                    mPaintHorizontalLableSub.getTextBounds(unit_text, 0, unit_text.length(), rect);
                    float x = (i + chartViewConfig.getCloumn() / 2) * chartViewConfig.getItem_width() - rect.width() / 2;//- getScrollX();
                    canvas.drawText(unit_text, x, bottomY + 40, mPaintHorizontalLableSub);
                }
            }
        }


        if (chartViewConfig.getGrid_line_kedu_color() > 0)
            mPaintGrid.setColor(getResources().getColor(chartViewConfig.getGrid_line_kedu_color()));
        mPaintGrid.setStrokeWidth(mDensity * 0.8f);
        bottomY = chartViewConfig.getRow() * chartViewConfig.getItem_height();
        //画lable line 横线
        canvas.drawLine(getScrollX(), bottomY, getScrollX() + getWidth(), bottomY, mPaintGrid);

    }

    /**
     * 绘制点和线
     *
     * @param canvas
     */
    protected void drawPointAndPath(Canvas canvas) {

        if(chartViewConfig.getListPoint().size()>=4 && chartViewConfig.isSmoothPoint()){
            if (mPathSet == null) {
                int size = chartViewConfig.getListPoint().size();
                PointValue[] mBezierControls = new PointValue[4];
                mPathSet = new Path[size];
                for (int i = 0; i < size; i++) {
                    Path mPathTrends = new Path();
                    //构造控制点的四个点；
                    PointValue l = null;
                    PointValue a = chartViewConfig.getListPoint().get(i);
                    int nextIndex = i+1<=size-1?i+1:size-1;
                    int nextNextIndex = i+2<=size-1?i+2:size-1;
                    PointValue b = chartViewConfig.getListPoint().get(nextIndex);
                    PointValue n = chartViewConfig.getListPoint().get(nextNextIndex);
                   //超过限制，break;
                    if(i+1>size-1){
                        mPathSet[i]=mPathTrends;
                        break;
                    }
                    //构造最后一个，为了最后一个点能平滑过渡
                    if(i+1==size-1){
                        PointValue nn = new PointValue(b.x+50,b.y);
                        n=nn;
                    }
                    //构造第一个的前一个，为了第一个点和第二个点能够平滑过渡
                    if(i==0){
                        PointValue ll = new PointValue(a.x-50,a.y);
                        l = ll;
                        mPathTrends.moveTo(a.x,b.y);
                    }else{
                        l = chartViewConfig.getListPoint().get(i-1);
                    }
                    //构造控制点
                    ChartViewHelper.caculateController(a, b, l, n, mBezierControls);
                    mPathTrends.moveTo(a.x, a.y);
                    mPathTrends.cubicTo(mBezierControls[1].x,mBezierControls[1].y,mBezierControls[2].x,mBezierControls[2].y,b.x,b.y);
                    mPathSet[i]=mPathTrends;
                }
            }
        }else{
            //画线
            if (mPathSet == null) {
                mPathSet = new Path[chartViewConfig.getListPoint().size()];
                for (int i = 0; i < chartViewConfig.getListPoint().size(); i++) {
                    PointValue point = chartViewConfig.getListPoint().get(i);
                    PointValue pointLast = null;
                    if (i > 0) {
                        pointLast = chartViewConfig.getListPoint().get(i - 1);
                    }
                    Path mPathTrends = new Path();
                    if (i == 0) {
                        mPathTrends.moveTo(point.x, point.y);

                    } else {
                        mPathTrends.moveTo(pointLast.x, pointLast.y);
                        mPathTrends.lineTo(point.x, point.y);
                    }
                    Log.d(TAG, "path -->point.x:" + point.x + "-->point.y:" + point.y);
                    mPathSet[i] = mPathTrends;
                }
            }
        }
        int indicator_x = getWidth() / 2 + getScrollX();
        Log.d(TAG, "----indicator_x:" + indicator_x);
        for (int i = 0; i < chartViewConfig.getListPoint().size(); i++) {
            PointValue point = chartViewConfig.getListPoint().get(i);
            if (point.x >= (mScreenIndex - chartViewConfig.getCloumn() / 2) * chartViewConfig.getItem_width() && point.x <= (mScreenIndex + chartViewConfig.getCloumn() * 2) * chartViewConfig.getItem_width()) {
                //画线
                canvas.drawPath(mPathSet[i], mPaintPath);
                //画点
                if(indicator_x+5>=point.x && indicator_x-5<=point.x){
                    canvas.drawCircle(point.x, point.y, mDensity * 5f, mPaintCircle);
                }else{
                    canvas.drawCircle(point.x, point.y, mDensity * 4f, mPaintCircle);
                }
            }
        }
    }


    /**
     * 画区域
     * @param canvas
     */
    protected void drawPointRegion(Canvas canvas) {
        if(chartViewConfig.getListPointRegion()==null){
            return;
        }

        //画笔重置
        if (mPathRegion == null) {
            mPathRegion  = new Path();
        }
        mPathRegion.reset();
        //可见范围内，线的链接
        boolean bFirst = true;
        for (int i = 0; i < chartViewConfig.getListPointRegion().size(); i++) {
            PointValue point = chartViewConfig.getListPointRegion().get(i);
            if (point.x >= (mScreenIndex - chartViewConfig.getCloumn() / 2) * chartViewConfig.getItem_width() && point.x <= (mScreenIndex + chartViewConfig.getCloumn() * 2) * chartViewConfig.getItem_width()) {
                PointValue value = chartViewConfig.getListPointRegion().get(i);
                if(bFirst){
                    mPathRegion.moveTo(value.x,value.y);
                    bFirst =false;
                }else{
                    mPathRegion.lineTo(value.x,value.y);
                }
            }
        }
        //path闭合，填充
        mPathRegion.close();
        canvas.drawPath(mPathRegion, mPaintPathRegion);
    }

    //画点与点链接，与X轴行程的区域
    protected void drawFillPointConnectRegion(Canvas canvas) {
        if(!chartViewConfig.isFillPointRegion())
            return;
        if(chartViewConfig.getListPoint().size()>=4 && chartViewConfig.isSmoothPoint()){
            int bottomY = chartViewConfig.getRow() * chartViewConfig.getItem_height();
            if (mPathSetRegion == null) {
                int size = chartViewConfig.getListPoint().size();
                PointValue[] mBezierControls = new PointValue[4];
                mPathSetRegion = new Path[size];
                for (int i = 0; i < size; i++) {
                    Path mPathTrends = new Path();
                    //构造控制点的四个点；
                    PointValue l = null;
                    PointValue a = chartViewConfig.getListPoint().get(i);
                    int nextIndex = i+1<=size-1?i+1:size-1;
                    int nextNextIndex = i+2<=size-1?i+2:size-1;
                    PointValue b = chartViewConfig.getListPoint().get(nextIndex);
                    PointValue n = chartViewConfig.getListPoint().get(nextNextIndex);
                    //超过限制，break;
                    if(i+1>size-1){
                        mPathSetRegion[i]=mPathTrends;
                        break;
                    }
                    //构造最后一个，为了最后一个点能平滑过渡
                    if(i+1==size-1){
                        PointValue nn = new PointValue(b.x+50,b.y);
                        n=nn;
                    }
                    //构造第一个的前一个，为了第一个点和第二个点能够平滑过渡
                    if(i==0){
                        PointValue ll = new PointValue(a.x-50,a.y);
                        l = ll;
                        mPathTrends.moveTo(a.x,b.y);
                    }else{
                        l = chartViewConfig.getListPoint().get(i-1);
                    }
                    //构造控制点
                    ChartViewHelper.caculateController(a, b, l, n, mBezierControls);
                    mPathTrends.moveTo(a.x, a.y);
                    mPathTrends.cubicTo(mBezierControls[1].x,mBezierControls[1].y,mBezierControls[2].x,mBezierControls[2].y,b.x,b.y);
                    //闭合区域
                    if(nextIndex>=1 && nextIndex<=size-1){
                        PointValue pointValueBottomRight = new PointValue(b.x,bottomY);
                        PointValue pointValueBottomLeft = new PointValue(a.x,bottomY);
                        mPathTrends.lineTo(pointValueBottomRight.x,pointValueBottomRight.y);
                        mPathTrends.lineTo(pointValueBottomLeft.x,pointValueBottomLeft.y);
                        mPathTrends.close();
                    }
                    mPathSetRegion[i]=mPathTrends;
                }
            }
            for (int i = 0; i < chartViewConfig.getListPoint().size(); i++) {
                PointValue point = chartViewConfig.getListPoint().get(i);
                if (point.x >= (mScreenIndex - chartViewConfig.getCloumn() / 2) * chartViewConfig.getItem_width() && point.x <= (mScreenIndex + chartViewConfig.getCloumn() * 2) * chartViewConfig.getItem_width()) {
                    //画线
                    canvas.drawPath(mPathSetRegion[i], mPaintPathConnectRegion);
                }
            }

        }else{
            //画笔重置
            if (mPathConnectRegion == null) {
                mPathConnectRegion  = new Path();
            }
            mPathConnectRegion.reset();

            listRegionTemp.clear();
            listRegionTemp.addAll(chartViewConfig.getListPoint());
            //加入第一点
            PointValue firstValue = chartViewConfig.getListPoint().get(0);
            int bottomY = chartViewConfig.getRow() * chartViewConfig.getItem_height();
            PointValue firstBottomValue = new PointValue(firstValue.x,bottomY);
            listRegionTemp.add(0,firstBottomValue);

            PointValue lastValue = chartViewConfig.getListPoint().get( chartViewConfig.getListPoint().size()-1);
            PointValue lastBottomValue = new PointValue(lastValue.x,bottomY);
            listRegionTemp.add(lastBottomValue);
            //可见范围内，线的链接
            boolean bFirst = true;
            for (int i = 0; i < listRegionTemp.size(); i++) {
                PointValue point = listRegionTemp.get(i);
                if (point.x >= (mScreenIndex - chartViewConfig.getCloumn() / 2) * chartViewConfig.getItem_width() && point.x <= (mScreenIndex + chartViewConfig.getCloumn() * 2) * chartViewConfig.getItem_width()) {
                    PointValue value = listRegionTemp.get(i);
                    if(bFirst){
                        mPathConnectRegion.moveTo(value.x,value.y);
                        bFirst =false;
                    }else{
                        mPathConnectRegion.lineTo(value.x,value.y);
                    }
                }
            }
            //path闭合，填充
            mPathConnectRegion.close();
            canvas.drawPath(mPathConnectRegion,mPaintPathConnectRegion);
        }

    }

    /**
     * 计算控制点
     * @param result
     * @param value1
     * @param value2
     * @param multiplier
     */
    private void calc(PointValue result, PointValue value1, PointValue value2, final float multiplier) {
        float diffX = value2.x - value1.x;
        float diffY = value2.y - value2.y;
        result.x = value1.x+ (diffX * multiplier);
        result.y =value1.y + (diffY * multiplier);


    }

    //通过刻度值获取横向刻度值的索引
    private int getHorizontalKeduIndex(String value){
        int size = chartViewConfig.getListHorizontalKedu().size();
        for(int i=0;i<size;i++){
            KeduValue keduValue  = chartViewConfig.getListHorizontalKedu().get(i);
            if(chartViewConfig.horizontal_lable_use_integer){
                int valueSrc = Integer.valueOf(keduValue.value);
                if(valueSrc==Integer.valueOf(value)){
                    return i;
                    /*
                    //第一点返回-1说明没有间隔,从第二点开始才有间隔的概念
                    if(i==0){
                        return -1;
                    }else {
                        return i-1;
                    }
                    */
                }
            }
            if(chartViewConfig.horizontal_lable_use_float){
                float valueSrc = Float.valueOf(keduValue.value);
                if(valueSrc==Float.valueOf(value)){
                   /* //第一点返回-1说明没有间隔,从第二点开始才有间隔的概念
                    if(i==0){
                        return -1;
                    }else {
                        return i-1;
                    }*/
                    return i;
                }
            }
           /* if(chartViewConfig.horizontal_lable_use_calendar){

               *//* int valueSrc = Integer.valueOf(keduValue.value);
                if(valueSrc==Integer.valueOf(value)){
                    //第一点返回-1说明没有间隔,从第二点开始才有间隔的概念
                    if(i==0){
                        return -1;
                    }else {
                        return i-1;
                    }
                }*//*
            }*/
        }
        return -1;
    }
    /**
     * 计算每个点的坐标值
     */
    private void caculatePointValue() {
        if(mIsCaculateValue){
            //计算区域坐标
            if(chartViewConfig.getListPointRegion()!=null && chartViewConfig.getListPointRegion().size()>0){
                caculatePointRegionValue(chartViewConfig.getListPointRegion());
            }
            //计算点的坐标
            caculatePointRegionValue(chartViewConfig.getListPoint());
            mIsCaculateValue = false;
        }
    }

    /**
     * 更新视图
     */
    public void update(){
        mIsCaculateValue = true;
        invalidate();
    }

    private void caculatePointRegionValue(List<PointValue> listPoint) {
        //计算可见点的X轴坐标
     /*   List<KeduValue> listHorizontalKedu = new ArrayList<>();
        final int count_horizontal = chartViewConfig.getListHorizontalKedu().size();
        //取出可见范围的水平刻度
        for (int i = 0; i < count_horizontal; i++) {
            if (i >= mScreenIndex - chartViewConfig.getCloumn() / 2 && i <= mScreenIndex + chartViewConfig.getCloumn()) {
                KeduValue kedu = chartViewConfig.getListHorizontalKedu().get(i);
                listHorizontalKedu.add(kedu);
            }
        }*/
        //遍历在这个刻度范围内的点，计算出x值
         KeduValue firstKeduValue = chartViewConfig.getListHorizontalKedu().get(0);
         for (PointValue point : listPoint) {
             if (chartViewConfig.horizontal_lable_use_integer) {
                 //当前点的横刻度值
                 int horizontal_value = Integer.valueOf(String.valueOf(point.horizontal_value));
                 //找出当前点在第几个横刻度（非精确，因为是画区域），取余数
                 int horizontalKeduIndex = (horizontal_value-Integer.valueOf(firstKeduValue.value))/(int)chartViewConfig.horizontal_kedu_interval[0];
                 //该横刻度的值
                 int horizontalKeduIndex_value = horizontalKeduIndex*(int)chartViewConfig.horizontal_kedu_interval[0];
                 //该横刻度的x值
                 int horizontalKeduIndex_x = (horizontalKeduIndex + chartViewConfig.getCloumn() / 2) * chartViewConfig.getItem_width();

                 //计算出当前点与当前横刻度的差
                 int value = horizontal_value - horizontalKeduIndex_value;
                 //计算差值占用间隔的百分比
                 float percent = value / (chartViewConfig.horizontal_kedu_interval[0] * 1.0f);
                 //通过百分比和横刻度的x值算出当前点的x值
                 point.x = horizontalKeduIndex_x + percent * chartViewConfig.getItem_width();

             }else if (chartViewConfig.horizontal_lable_use_float) {

                 //当前点的横刻度值
                 float horizontal_value = Float.valueOf(String.valueOf(point.horizontal_value));
                 //找出当前点在第几个横刻度（非精确，因为是画区域），取余数
                 float horizontalKeduIndex = (horizontal_value-Integer.valueOf(firstKeduValue.value))/(int)chartViewConfig.horizontal_kedu_interval[0];
                 //该横刻度的值
                 float horizontalKeduIndex_value = horizontalKeduIndex*(int)chartViewConfig.horizontal_kedu_interval[0];
                 //该横刻度的x值
                 float horizontalKeduIndex_x = (horizontalKeduIndex + chartViewConfig.getCloumn() / 2) * chartViewConfig.getItem_width();

                 //计算出当前点与当前横刻度的差
                 float value = horizontal_value - horizontalKeduIndex_value;
                 //计算差值占用间隔的百分比
                 float percent = value / (chartViewConfig.horizontal_kedu_interval[0] * 1.0f);
                 //通过百分比和横刻度的x值算出当前点的x值
                 point.x = horizontalKeduIndex_x + percent * chartViewConfig.getItem_width();
             }
       /* for (PointValue point : listPoint) {
            int horizontal_value = Integer.valueOf(String.valueOf(point.horizontal_value));
            //Log.d(TAG, "--horizontal_value:" + horizontal_value);
            //每个点与可见的横刻度降序对比，一旦大于就break;
            for (int i = listHorizontalKedu.size() - 1; i >= 0; i--) {
                KeduValue kedu = listHorizontalKedu.get(i);
                if (chartViewConfig.horizontal_lable_use_integer) {
                    if (horizontal_value >= Integer.valueOf(String.valueOf(kedu.value))) {
                        int horizontalKeduIndex = getHorizontalKeduIndex(kedu.value);
                        if(horizontalKeduIndex>=0 && chartViewConfig.horizontal_kedu_interval[horizontalKeduIndex]!=0){
                            Integer value = horizontal_value - Integer.valueOf(String.valueOf(kedu.value));
                            float percent = value / (chartViewConfig.horizontal_kedu_interval[horizontalKeduIndex] * 1.0f);
                            point.x = kedu.current_x + percent * chartViewConfig.getItem_width();
                            Log.d(TAG, "point.x:" + point.x + "--kedu.current_x:" + kedu.current_x + "--percent:" + percent + "-->horizontal_kedu_interval:"
                                    + chartViewConfig.horizontal_kedu_interval[horizontalKeduIndex] + "-->horizontal_value:" + horizontal_value + "-->kedu.value:" + kedu.value+"-->i:"+i);
                        }else{
                            point.x = kedu.current_x;
                        }
                        break;
                    }
                } else if (chartViewConfig.horizontal_lable_use_float) {
                    //float horizontal_value = Float.valueOf(String.valueOf(point.horizontal_value));
                    if (horizontal_value >= Float.valueOf(String.valueOf(kedu.value))) {
                        int horizontalKeduIndex = getHorizontalKeduIndex(kedu.value);
                        if(horizontalKeduIndex>=0  && chartViewConfig.horizontal_kedu_interval[horizontalKeduIndex]!=0){
                            Float value = horizontal_value - Float.valueOf(String.valueOf(kedu.value));
                            float percent = value / (chartViewConfig.horizontal_kedu_interval[horizontalKeduIndex] * 1.0f);
                            point.x = kedu.current_x + percent * chartViewConfig.getItem_width();
                            //Log.d(TAG, "point.x:" + point.x + "--kedu.current_x:" + kedu.current_x + "--percent:" + percent + "-->horizontal_kedu_interval:" + chartViewConfig.horizontal_kedu_interval + "-->horizontal_value:" + horizontal_value + "-->kedu.value:" + kedu.value);

                        }else{
                            point.x = kedu.current_x;
                        }
                        break;
                    }
                }
            }*/

        }

        //遍历在这个刻度范围内的点，计算出y值
        final int count_verical = (int) (chartViewConfig.getVerical_unit_end() - chartViewConfig.getVerical_unit_start()) / (int) chartViewConfig.getVerical_unit_incremetal();
        for (PointValue point : listPoint) {
            for (int i = count_verical; i >= 0; i--) {
                //从最大值开始
                float value = chartViewConfig.getVerical_unit_start() + i * chartViewConfig.getVerical_unit_incremetal();
                if (chartViewConfig.verical_lable_use_integer) {
                    float verical_value = Float.valueOf(String.valueOf(point.verical_value));
                    if (verical_value >= (int) value) {
                        //超过最高，取最高
                        if (verical_value >= (int) (chartViewConfig.getVerical_unit_end())) {
                            point.y = (chartViewConfig.getRow() - count_verical) * chartViewConfig.getItem_height();
                            //超过最最低，取最低
                        } else if (verical_value <= (int) (chartViewConfig.getVerical_unit_start())) {
                            point.y = (chartViewConfig.getRow()) * chartViewConfig.getItem_height();
                        } else {
                            float cha = verical_value - (int) value;
                            float percent = cha / (chartViewConfig.getVerical_unit_incremetal() * 1.0f);
                            int y = (chartViewConfig.getRow() - i) * chartViewConfig.getItem_height();
                            point.y = y - percent * chartViewConfig.getItem_height();
                        }
                    }
                } else if (chartViewConfig.verical_lable_use_float) {
                    float verical_value = Float.valueOf(String.valueOf(point.verical_value));
                    if (verical_value >= Float.valueOf(String.valueOf(value))) {
                        //超过最高，取最高
                        if (verical_value >= Float.valueOf(String.valueOf(chartViewConfig.getVerical_unit_end()))) {
                            point.y = (chartViewConfig.getRow() - i) * chartViewConfig.getItem_height();
                            //超过最最低，取最低
                        } else if (verical_value <= Float.valueOf(String.valueOf(chartViewConfig.getVerical_unit_start()))) {
                            point.y = (chartViewConfig.getRow()) * chartViewConfig.getItem_height();
                        } else {
                            float cha = verical_value - Float.valueOf(String.valueOf(value));
                            float percent = cha / (chartViewConfig.getVerical_unit_incremetal() * 1.0f);
                            int y = (chartViewConfig.getRow() - i) * chartViewConfig.getItem_height();
                            point.y = y - percent * chartViewConfig.getItem_height();
                        }
                    }
                }
            }
        }

    }

    private boolean isFirst = true;

    private void setSelection() {
        if (isFirst) {
            isFirst = false;
            int selection = chartViewConfig.getItemSelection();
            if(selection<=0){
                mScroller.setFinalX((int) minX);
            }else{
                boolean has =false;
               for(int i=0;i<chartViewConfig.getListPoint().size();i++){
                   if(selection==i){
                       mScroller.setFinalX(getScrollX()+(int)chartViewConfig.getListPoint().get(i).x-getWidth()/2);
                       has =true;
                       break;
                   }
               }
                if(!has){
                    mScroller.setFinalX((int) maxX);
                }
            }
            invalidate();
        }
        /*
        //只有一个选中第一个
        if (chartViewConfig.getListPoint().size() == 1) {
            mScroller.setFinalX((int)minX);
            scrollToNearllyPoint();
            //多个则选择最后一个
        } else {
            mScroller.setFinalX((int)maxX);
        }*/


    }


    /**
     * 调用postInvalidate()方法重新绘制界面，
     * postInvalidate()方法会调用invalidate()方法，
     * invalidate()方法又会调用computeScroll方法，
     * 就这样周而复始的相互调用，直到mScroller.computeScrollOffset() 返回false才会停止界面的重绘动作
     */
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
        super.computeScroll();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(event);
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:{
                    getParent().requestDisallowInterceptTouchEvent(true);
                    //mLastFingerPosition = event.getX();
                    mIsPressd = true;
                    mLastScrollX=0;
                    mLastionMotionX = x;
                    mLastionMotionY = y;
                    mScroller.forceFinished(true);
                    invalidate();
                    break;
                }
                case MotionEvent.ACTION_MOVE:{
                    int detaX = (int) (mLastionMotionX - x);
                    scrollBy(detaX, 0);
                    Log.d(TAG, "getScrollX:" + getScrollX());
                    int detaY = (int) (mLastionMotionY - y);
                    //纵向消息交给父控件
                    if (Math.abs(detaY) > 50) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    //触发监听
                    if(mLastScrollX!=0 && mListener!=null){
                        int delta = getScrollX()-mLastScrollX;
                        mListener.onChartViewScrolled(delta);
                    }else{
                        mLastScrollX = getScrollX();
                    }
                    mLastionMotionX = x;
                    break;
                }
                case MotionEvent.ACTION_UP:{
                    getParent().requestDisallowInterceptTouchEvent(false);
                    /*if (mOnPressedListener != null) {
                        if (Math.abs(mLastFingerPosition - event.getX()) < mPressedSensitive) {
                            if (event.getX() >= x1 && event.getX() <= x2 && event.getY() >= y1 && event.getY() <= y2) {
                                if (null != mOnBalloonClickListener) {
                                    int index = mCurrentIndex - 1;
                                    if (index == -1) {
                                        index = 0;
                                    }
                                    mOnBalloonClickListener.OnClick(index);
                                }
                            }
                            mOnPressedListener.OnPressed();
                        }
                    }*/
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000);
                    int velocityX = (int) velocityTracker.getXVelocity();
                    int velocityY = (int) velocityTracker.getYVelocity();
                    Log.d(TAG, "velocityX:" + velocityX + "velocityY:" + velocityY + "--SNAP_VELOCITY:" + SNAP_VELOCITY);
                    //Use.trace(TAG, "---velocityX---" + velocityX);

                    //往右边滑
                    if (velocityX > SNAP_VELOCITY) {
                        snapToScreen(false, velocityX, velocityY);
                        if(mListener!=null){
                            mListener.onChartViewScrollDirection(ScrollDirection.RIGHT);
                        }
                    }
                    //往左边滑
                    else if (velocityX < -SNAP_VELOCITY) {
                        snapToScreen(true, velocityX, velocityY);
                        if(mListener!=null){
                            mListener.onChartViewScrollDirection(ScrollDirection.LEFT);
                        }
                    } else {
                        snapToDestination();
                    }

                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    //mTouchState = TOUCH_STATE_REST;
                    mIsPressd = false;
                    invalidate();
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                    snapToDestination();
                    //mTouchState = TOUCH_STATE_REST;
                    break;
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void snapToDestination() {
        int nowScrollX = getScrollX();
        getMinAndManScrollerValue();
        Log.d(TAG, "-->minX:" + minX + "-->maxX:" + maxX + "-->nowScrollX:" + nowScrollX);
        maxX = Math.max(maxX, 0);
        if (nowScrollX < minX) {
            int dx = minX - nowScrollX;
            mScroller.startScroll(getScrollX(), 0, dx, 0, Math.abs(dx));
            invalidate();
        } else if (nowScrollX > maxX) {
            int dx = maxX - nowScrollX;
            //数据过少的时候
            if (maxX == 0) {
                dx = -nowScrollX + minX;
            }
            mScroller.startScroll(getScrollX(), 0, dx, 0, Math.abs(dx));
            invalidate();
        } else if ((nowScrollX <= maxX) && (nowScrollX >= minX)) {
            scrollToNearby();
        }


    }

    private void scrollToNearby() {
        //当前点位置
        int index = 0;
        int indicator_x = getWidth() / 2 + getScrollX();
        for (int i = chartViewConfig.getListPoint().size() - 1; i >= 0; i--) {
            if (indicator_x > chartViewConfig.getListPoint().get(i).x) {
                index = i;
                break;
            }
        }
        //与后边的点的差
        int width = 0;
        if (index + 1 < chartViewConfig.getListPoint().size()) {
            width = (int) (chartViewConfig.getListPoint().get(index + 1).x - chartViewConfig.getListPoint().get(index).x);
        }
        int cha = indicator_x - (int) chartViewConfig.getListPoint().get(index).x;
        Log.v(TAG, "--cha:" + cha + "-->indicator_x:" + indicator_x + "-->index x:" + (int) chartViewConfig.getListPoint().get(index).x);
        //处于右半部分，往右边偏移
        if (Math.abs(cha) >= width / 2) {
            int dx = width - Math.abs(cha);
            mScroller.startScroll(getScrollX(), 0, dx, 0, 250);
            invalidate();
        } else {
            int dx = -Math.abs(cha);
            mScroller.startScroll(getScrollX(), 0, dx, 0, 250);
            invalidate();
        }
    }


    private void snapToScreen(boolean bScrollToLeft, int velocityX, int velocityY) {
        int nowScrollX = getScrollX();
        Log.d(TAG, "--snapToScreen nowScrollX:" + nowScrollX);
        getMinAndManScrollerValue();
        //滑动距离在:
        if ((nowScrollX <= maxX) && (nowScrollX >= minX)) {
            /**
             * 基于甩动手势开始滚动处理。根据甩动的初始速率来决定滚动的距离。
             参数：
             startX  滚动的起始点（X）
             startY  滚动的起始点（Y）
             velocityX 以像素/每秒为单位，测量所得X轴的初始甩动速率
             velocityY以像素/每秒为单位，测量所得Y轴的初始甩动速率
             minX  最小的X轴值，滚动器不能滚动经过这个点
             maxX  最大的X轴值，滚动器不能滚动经过这个点
             minY  最小的Y轴值，滚动器不能滚动经过这个点
             maxY  最大的Y轴值，滚动器不能滚动经过这个点
             */
            mScroller.fling(getScrollX(), getScrollY(), -velocityX, -velocityY, minX, maxX, 0, 0);
            final int mDurationTime = 2000;
            this.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //滚动完成后，定位
                    if (!mScroller.computeScrollOffset()) {
                        scrollToNearllyPoint();
                    } else {

                    }
                }
            }, mDurationTime);

        }
        //超过左边，回滚
        else if (nowScrollX < minX) {
            int dx = minX - nowScrollX;
            int duration = Math.abs(dx);
            mScroller.startScroll(getScrollX(), 0, dx, 0, duration);

            //超过右边，回滚
        } else if (nowScrollX > maxX) {
            int dx = maxX - nowScrollX;
            int duration = Math.abs(dx);
            mScroller.startScroll(getScrollX(), 0, dx, 0, duration);
        }

        invalidate();
    }

    private void scrollToNearllyPoint() {
        getMinAndManScrollerValue();
        int nowScrollX = getScrollX();
        if ((nowScrollX <= maxX) && (nowScrollX >= minX)) {
            scrollToNearby();
        }
    }

    private int minX;
    private int maxX;

    /**
     * 计算左右x的界限值
     */
    private void getMinAndManScrollerValue() {
        //int mGridWidth = chartViewConfig.getItem_width();
        //minX =0;
        //maxX = (chartViewConfig.getListHorizontalKedu().size() - 1) * mGridWidth;
        minX = (int) chartViewConfig.getListPoint().get(0).x - chartViewConfig.getCloumn() * chartViewConfig.getItem_width() / 2;
        maxX = (int) chartViewConfig.getListPoint().get(chartViewConfig.getListPoint().size() - 1).x - chartViewConfig.getCloumn() * chartViewConfig.getItem_width() / 2;
        Log.d(TAG, "--getMinAndManScrollerValue minX:" + minX + "--maxX:" + maxX);
    }


}

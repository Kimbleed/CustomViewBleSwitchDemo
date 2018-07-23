package com.example.mysmall.newelasticballview.elastic;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.example.mysmall.newelasticballview.CanvasUtils;
import com.example.mysmall.newelasticballview.R;

/**
 * Created by 49479 on 2018/7/18.
 */

public class YDBleSwitch extends View {

    private static final String TAG = YDBleSwitch.class.getName();

    /**
     * --------------  BallSwitch控件 整体属性 -- 开始
     */
    private float mWidth;
    private float mHeight;

    //画图形
    private Paint mPaint;
    //画文字 和 图片
    private Paint mPaintSecond;

    //操作状态
    private int mBallSwitchGoingState = SWITCH_STATE_CONNECTING;
    private int mBallSwitchState = SWITCH_STATE_CONNECTING;

    public static final int SWITCH_STATE_CONNECTING = 11;
    public static final int SWITCH_STATE_CONNECTED = 12;
    public static final int SWITCH_STATE_DISCONNECTED = 13;
    public static final int SWITCH_STATE_OPEN = 14;

    public String toString(int state) {
        String strState = "";
        switch (state) {
            case SWITCH_STATE_CONNECTING:
                strState = "SWITCH_STATE_CONNECTING";
                break;
            case SWITCH_STATE_CONNECTED:
                strState = "SWITCH_STATE_CONNECTED";
                break;
            case SWITCH_STATE_DISCONNECTED:
                strState = "SWITCH_STATE_DISCONNECTED";
                break;
            case SWITCH_STATE_OPEN:
                strState = "SWITCH_STATE_OPEN";
                break;
            case DRAW_STATE_CONNECTING:
                strState = "DRAW_STATE_CONNECTING";
                break;
            case DRAW_STATE_CONNECTED:
                strState = "DRAW_STATE_CONNECTED";
                break;
            case DRAW_STATE_DISCONNECTED:
                strState = "DRAW_STATE_DISCONNECTED ";
                break;
            case DRAW_STATE_CONNECTING_TO_CONNECTED:
                strState = "DRAW_STATE_CONNECTING_TO_CONNECTED";
                break;
            case DRAW_STATE_CONNECTING_TO_DISCONNECTED:
                strState = "DRAW_STATE_CONNECTING_TO_DISCONNECTED";
                break;
            case DRAW_STATE_CONNECTED_TO_DISCONNECTED:
                strState = "DRAW_STATE_CONNECTED_TO_DISCONNECTED";
                break;
            case DRAW_STATE_TO_OPENING:
                strState = "DRAW_STATE_TO_OPENING";
                break;
            case DRAW_STATE_OPENING:
                strState = "DRAW_STATE_OPENING";
                break;
            case DRAW_STATE_OPEN_SUCCESS:
                strState = "DRAW_STATE_OPEN_SUCCESS";
                break;
        }
        return strState;
    }

    private boolean isCancel = false;

    //画面状态
    private int mBallSwitchDrawState = DRAW_STATE_CONNECTING;
    public static final int DRAW_STATE_CONNECTING = 1;
    public static final int DRAW_STATE_CONNECTED = 2;
    public static final int DRAW_STATE_DISCONNECTED = 3;
    public static final int DRAW_STATE_CONNECTING_TO_CONNECTED = 4;
    public static final int DRAW_STATE_CONNECTING_TO_DISCONNECTED = 5;
    public static final int DRAW_STATE_CONNECTED_TO_DISCONNECTED = 6;
    public static final int DRAW_STATE_TO_OPENING = 7;
    public static final int DRAW_STATE_OPENING = 8;
    public static final int DRAW_STATE_OPEN_SUCCESS = 9;

    private PointF locatePointArr[] = new PointF[3];

    //弹性球本体
    PullBall
            mPullBall;

    Ball mLocateBall;

    //弹性球在Canvas中的体现
    Path mPullBallPath;

    //弹性球的目的坐标
    PointF mPullBallTargetPoint;

    private int mPullBallMoveDuration = 800;

    //球条PullBall状态
    private int mPullBallState = PULL_STATE_STATIC;
    public static final int PULL_STATE_STATIC = 0x0001;     //球条状态：收起
    public static final int PULL_STATE_EXPAND = 0x0002;     //球条状态：展开

    //球条PullBall半径
    private int mPullBallRadius = 200;
    //球条PullBall与包裹Container的间距
    private int mPullBallMargin = 20;

    //基础色调
    private int mEnableColor = 0xFFffffff;
    private int mDisableColor = 0xFFa5ecd9;

    //球条PullBall颜色
    private int mPullBallColor = mEnableColor;
    //包裹Container颜色
    private int mContainerColor = mEnableColor;

    //球属性
    private float mScaleMaxRadius = 300;
    private float mScaleMinRadius = mPullBallRadius;
    private float mConnectedRadius = 150;

    //动画时间
    private int scaleDuration = 600;
    private int narrowDuration = 400;
    private int expandDuration = 200;

    //动画集
    private AnimatorSet mAnimSet;

    //主图标 (跟随球条的右球位置 PullBall.mCurBall)
    private Bitmap mMainIcon;
    private int mMainIconSize = -1;
    private int mMainIconAlpha = 0xff;
    private float mMainIconY;

    //次图标 (始终在包裹Container 展开时，处于右球locatePointArr[2]的位置)
    private Bitmap mSecondaryIcon;
    private int mSecondaryIconSize = -1;
    private int mSecondaryIconAlpha = 0xff;
    private float mSecondaryIconY;


    private String TXT_CONNECTED = "连接中";
    private String TXT_OPENING = "开锁中";
    private String TXT_DRAG_OPEN = "右滑开锁";

    //PullBall 右球locatePointArr[2] 上的文字
    private String mPullBallTxtInCurBall = TXT_CONNECTED;
    private int mPullBallTxtSizeInCurBall;
    private int mPullBallTxtColorInCurBall = 0xFF96ebd3;

    //PullBall 中间的文字
    private String mPullBallInCenter = TXT_OPENING;
    private int mPullBallTxtSizeInCenter;
    private int mPullBallTxtColorInCenter;

    //Container 中间的文字
    private String mContainerTxt = TXT_DRAG_OPEN;
    private int mContainerTxtSize;
    private int mContainerTxtColor = mPullBallColor;

    //是否onLayout 过
    private boolean onLayout = false;


    //正在开锁 的圆点
    private static final int mOpeningMaxRadius = 20;
    private static final int mOpeningMinRadius = 0;
    private int mOpeningCircleRadius = mOpeningMinRadius;

    private ValueAnimator mOpeningAnim;

    public YDBleSwitch(Context context) {
        super(context);
        init();

    }

    public YDBleSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public YDBleSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mPullBallColor);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mPaintSecond = new Paint();
        mPaintSecond.setAntiAlias(true);

        setClickable(true);
        mMainIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_lock_locked);
        mMainIconSize = mMainIcon.getWidth() / 2;

        mSecondaryIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_lock_open);
        mSecondaryIconSize = mSecondaryIcon.getWidth() / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (onLayout)
            return;
        onLayout = true;

        //获取布局数据
        mWidth = getWidth();
        mHeight = getHeight();

        //定点
        locatePointArr[0] = new PointF(mWidth / 5, mHeight / 2);
        locatePointArr[1] = new PointF(mWidth / 2, mHeight / 2);
        locatePointArr[2] = new PointF(mWidth / 5 * 4, mHeight / 2);

        //初始化 ElasticBall
        mPullBall = new PullBall(locatePointArr[1].x, locatePointArr[1].y, mPullBallRadius);
        mPullBall.setDuration(mPullBallMoveDuration);

        mLocateBall = new Ball(locatePointArr[1].x, locatePointArr[1].y, mPullBallRadius);

        mPullBallTargetPoint = new PointF(locatePointArr[2].x, locatePointArr[2].y);
        mPullBall.setTarget(mPullBallTargetPoint, new PullBall.DragBallInterface() {
            @Override
            public void onChange(Path path) {
                mPullBallState = PULL_STATE_EXPAND;
                mPullBallPath = path;
                postInvalidate();
            }

            @Override
            public void onFinish(float percent) {
                //拉动到右边，触发开关
                if (percent == 1.0f) {
                    mBallSwitchDrawState = DRAW_STATE_TO_OPENING;
                    postInvalidate();
                } else {
                    mPullBallState = PULL_STATE_STATIC;
                }
            }
        });
        mPullBallPath = mPullBall.drawPath();
        mPullBallTxtSizeInCurBall = (int) mWidth / 30;
        mContainerTxtSize = (int) mWidth / 20;
        mMainIconY = mPullBall.mCurBall.y - mPullBallTxtSizeInCurBall;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        switch (mBallSwitchDrawState) {
            case DRAW_STATE_CONNECTING:                     //画面状态：连接中
            case DRAW_STATE_CONNECTING_TO_DISCONNECTED:     //画面状态：连接中 到 断开连接
                drawConnecting(canvas);
                break;
            case DRAW_STATE_DISCONNECTED:                   //画面状态：断开连接
                drawDisconnected(canvas);
                break;
            case DRAW_STATE_CONNECTING_TO_CONNECTED:        //画面状态：连接中 到 已连接
            case DRAW_STATE_CONNECTED:                      //画面状态：已连接
                drawConnectingToConnected(canvas);
                break;
            case DRAW_STATE_CONNECTED_TO_DISCONNECTED:      //画面状态：已连接 到 断开连接
                drawConnectedToDisconnected(canvas);
                break;
            case DRAW_STATE_TO_OPENING:                     //画面状态：过渡到 正在开锁
                drawToOpening(canvas);
                break;
            case DRAW_STATE_OPENING:                        //画面状态：正在开锁
                drawOpening(canvas);
                break;
            case DRAW_STATE_OPEN_SUCCESS:                   //画面状态：开锁成功
                drawOpenSuccess(canvas);
                break;
            default:
                break;
        }

        super.onDraw(canvas);
    }

    private float firstX, firstY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (mPullBall.getPercent() > 0.9f) {
            return super.onTouchEvent(event);
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                firstX = event.getX();
                firstY = event.getY();
                if (mBallSwitchState != SWITCH_STATE_CONNECTED || mPullBallState == PULL_STATE_EXPAND)
                    break;

                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getX() > firstX) {
                    float percent = (event.getX() - firstX) / (locatePointArr[2].x - locatePointArr[0].x);
                    mPullBall.setPercent(percent);
                    if (mPullBall.getPercent() > 0.9) {
                        vibrator();
                        mPullBall.startDragAnim(1.0f);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mPullBall.getPercent() > 0.9) {
                    mPullBall.startDragAnim(1.0f);
                } else {
                    mPullBall.startDragAnim(0.0f);
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 画图形  PullBall  可拖动球条
     *
     * @param canvas
     */
    private void drawPullBall(Canvas canvas) {
        //外形
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mPullBallColor);
        mPaint.setStrokeWidth(mPullBall.mOriginBall.radius * 1.5f);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawPath(mPullBallPath, mPaint);


        //文字 与 图标
        RectF rectf = rectf = new RectF((int) mPullBall.mCurBall.x - mMainIconSize, mMainIconY - mMainIconSize, mPullBall.mCurBall.x + mMainIconSize, mMainIconY + mMainIconSize);
        // 以下情况PullBall 中没有文字，mIcon居中
        if (mBallSwitchDrawState == DRAW_STATE_CONNECTING_TO_CONNECTED          //画面状态：连接中 到 已连接
                || mBallSwitchDrawState == DRAW_STATE_CONNECTED                 //画面状态：已连接
                || mBallSwitchDrawState == DRAW_STATE_CONNECTED_TO_DISCONNECTED //画面状态：已连接 到 断开连接
                || mBallSwitchDrawState == DRAW_STATE_OPENING                   //画面状态：正在开锁
                || mBallSwitchDrawState == DRAW_STATE_TO_OPENING                //画面状态：过渡到正在开锁
                || mBallSwitchDrawState == DRAW_STATE_OPEN_SUCCESS)             //画面状态：已开锁成功
        {

        }
        //其余画面状态有文字
        else {
            CanvasUtils.initPaintForTxt(mPaintSecond, mPullBallTxtColorInCurBall, 255, mPullBallTxtSizeInCurBall);
            CanvasUtils.drawText(canvas, mPaintSecond, mPullBallTxtInCurBall, mPullBall.mOriginBall.x, mPullBall.mCurBall.y + mPullBallTxtSizeInCurBall + mMainIconSize);
        }
        mPaint.setAlpha(mMainIconAlpha);
        canvas.drawBitmap(mMainIcon, null, rectf, mPaint);
        mPaint.setAlpha(0xff);

    }

    /**
     * 画图形 Container 外层包裹(跑道形状)
     *
     * @param canvas
     */
    private void drawContainer(Canvas canvas) {

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mPullBallMargin);
        mPaint.setColor(mContainerColor);

        //环形跑道形状 -- start
        Ball ball = new Ball(mWidth - mLocateBall.x, mLocateBall.y, mLocateBall.radius);

        Path path = new Path();

        path.moveTo(mLocateBall.topX, mLocateBall.topY - mPullBallMargin);
        path.lineTo(ball.topX, ball.topY - mPullBallMargin);

        RectF rectF1 = new RectF(ball.leftX - mPullBallMargin, ball.topY - mPullBallMargin, ball.rightX + mPullBallMargin, ball.bottomY + mPullBallMargin);
        path.arcTo(rectF1, -90, 180, false);

        path.lineTo(mLocateBall.bottomX, mLocateBall.bottomY + mPullBallMargin);

        RectF rectF2 = new RectF(mLocateBall.leftX - mPullBallMargin, mLocateBall.topY - mPullBallMargin, mLocateBall.rightX + mPullBallMargin, mLocateBall.bottomY + mPullBallMargin);
        path.addArc(rectF2, 90, 180);
        path.close();

        canvas.drawPath(path, mPaint);
        //环形跑道形状 -- end


        //文字 -- start
        CanvasUtils.initPaintForTxt(mPaintSecond, mContainerTxtColor, 255, mContainerTxtSize);
        CanvasUtils.drawText(canvas, mPaintSecond, mContainerTxt, locatePointArr[1].x, locatePointArr[1].y);
        //文字 -- end

        // 右图标 -- start
        if (ball.rightX + mPullBallMargin > locatePointArr[2].x + mSecondaryIconSize) {
            RectF rectF = new RectF(locatePointArr[2].x - mSecondaryIconSize, locatePointArr[2].y - mSecondaryIconSize, locatePointArr[2].x + mSecondaryIconSize, locatePointArr[2].y + mSecondaryIconSize);
            canvas.drawBitmap(mSecondaryIcon, null, rectF, mPaintSecond);
        }
        // 右图标 -- end

    }

    /**
     * 画图形 小圆点
     *
     * @param canvas
     */
    private void drawOpeningCircle(Canvas canvas) {
        mPaint.setColor(Color.parseColor("#FF96ebd3"));
        mPaint.setStrokeWidth(0);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(locatePointArr[2].x, locatePointArr[2].y + 20, mOpeningCircleRadius, mPaint);
    }


    /**
     * 画图形
     * 画基础图形  PullBall 和 Container
     *
     * @param canvas
     */
    private void drawBasic(Canvas canvas) {
        int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);

        //Container
        drawContainer(canvas);

        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        //球
        drawPullBall(canvas);

        mPaint.setXfermode(null);
        canvas.restoreToCount(sc);
    }

    /**
     * 动画 ：mPullBall.radius 呼吸  动画
     *
     * @return
     */
    private ValueAnimator getScaleRepeatAnim() {
        ValueAnimator anim = ValueAnimator.ofFloat(mScaleMinRadius, mScaleMaxRadius);
        anim.setDuration(scaleDuration);
        anim.setRepeatCount(-1);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mLocateBall.refresh(mLocateBall.x, mLocateBall.y, value);
                postInvalidate();
            }
        });
        return anim;
    }

    /**
     * 动画：Opening 小圆 呼吸动画
     *
     * @return
     */
    private ValueAnimator getOpeningScaleRepeatAnim() {
        ValueAnimator anim = ValueAnimator.ofInt(mOpeningMinRadius, mOpeningMaxRadius);
        anim.setDuration(scaleDuration);
        anim.setRepeatCount(-1);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                mOpeningCircleRadius = value;
                postInvalidate();
            }
        });
        return anim;
    }

    /**
     * 动画：强制缩小
     *
     * @return
     */
    private ValueAnimator getScaleOneTimeAnim(float narrowToSize) {
        ValueAnimator anim = ValueAnimator.ofFloat((int) mLocateBall.radius, narrowToSize);
        anim.setDuration(narrowDuration);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mLocateBall.refresh(mLocateBall.x, mLocateBall.y, value);
                postInvalidate();
            }
        });
        return anim;
    }


    /**
     * 动画：mPullBall.x 平移  动画
     *
     * @return
     */
    private ValueAnimator getDragBallTranslateAnim(final float translateToX) {
        ValueAnimator anim = ValueAnimator.ofFloat(mPullBall.mOriginBall.x, translateToX);
        float percent = Math.abs((mPullBall.mOriginBall.x - translateToX) / Math.abs(locatePointArr[1].x - locatePointArr[0].x));
        anim.setDuration((int) (scaleDuration * percent));
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mPullBall.refresh(value, mPullBall.mOriginBall.y, mPullBall.mOriginBall.radius);
                mPullBallPath = mPullBall.drawPath();
                mLocateBall.refresh(value, mLocateBall.y, mLocateBall.radius);
                postInvalidate();
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (translateToX == locatePointArr[1].x)
                    mBallSwitchDrawState = DRAW_STATE_DISCONNECTED;
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return anim;
    }

    /**
     * 动画：container color 渐变
     *
     * @param toColor
     * @return
     */
    private ValueAnimator getContainerColorAnim(int toColor) {
        final ValueAnimator anim = ValueAnimator.ofInt(mContainerColor, toColor);
        anim.setDuration(scaleDuration);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                mContainerColor = value;
                postInvalidate();
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mBallSwitchDrawState == DRAW_STATE_CONNECTING_TO_CONNECTED)
                    mBallSwitchDrawState = DRAW_STATE_CONNECTED;
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return anim;
    }


    /**
     * 动画：mPullBall.radius 扩展
     *
     * @return
     */
    private ValueAnimator getExpandForOpenSuccessAnim() {
        ValueAnimator anim = ValueAnimator.ofFloat(mPullBall.mOriginBall.radius, mPullBallRadius + 40);
        anim.setDuration(expandDuration);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mPullBall.refresh(mPullBall.mOriginBall.x, mPullBall.mOriginBall.y, value);
                postInvalidate();
            }
        });
        return anim;
    }

    /**
     * 动画：mPullBall 强制缩小
     *
     * @return
     */
    private ValueAnimator getPullBallNarrowForceAnim() {
        ValueAnimator anim = ValueAnimator.ofFloat(mPullBall.mOriginBall.radius, mPullBallRadius);
        anim.setDuration(400);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mPullBall.refresh(mPullBall.mOriginBall.x, mPullBall.mOriginBall.y, value);
                postInvalidate();
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mPullBall.startDragAnim(0.0f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return anim;
    }

    /**
     * 动画：MainIcon 透明度
     *
     * @param fromAlpha
     * @param toAlpha
     * @param resId
     * @return
     */
    private ValueAnimator getMainIconAlphaAnim(int fromAlpha, int toAlpha, final int resId, final boolean isVerticalCenter) {
        ValueAnimator anim = ValueAnimator.ofInt(fromAlpha, toAlpha);
        anim.setDuration(narrowDuration);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                mMainIconAlpha = value;
                postInvalidate();
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (resId != -1) {
                    mMainIcon = BitmapFactory.decodeResource(getResources(), resId);
                    if (isVerticalCenter) {
                        mMainIconY = mPullBall.mCurBall.y;
                    } else {
                        mMainIconY = mPullBall.mCurBall.y - mPullBallTxtSizeInCurBall;
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return anim;
    }

    /**
     * 动画：主Icon 放大 in OpenSuccess
     *
     * @return
     */
    private ValueAnimator getMainIconScaleAnim() {
        ValueAnimator anim = ValueAnimator.ofInt(0, mMainIconSize + 20, mMainIconSize);
        anim.setDuration(scaleDuration);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                mMainIconSize = value;
                postInvalidate();
            }
        });
        return anim;
    }

    /**
     * 绘制状态： 连接中状态
     *
     * @param canvas
     */
    private void drawConnecting(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        if (mAnimSet == null && mBallSwitchDrawState == DRAW_STATE_CONNECTING) {
            AnimatorSet connectingSet = new AnimatorSet();
            connectingSet.play(getScaleRepeatAnim());
            mAnimSet = connectingSet;
            mAnimSet.start();
        }
        drawContainer(canvas);
        drawPullBall(canvas);
    }

    /**
     * 绘制状态： 从连接中到已连接  的过程
     *
     * @param canvas
     */
    private void drawConnectingToConnected(Canvas canvas) {
        if (mAnimSet == null && mBallSwitchDrawState == DRAW_STATE_CONNECTING_TO_CONNECTED) {
            AnimatorSet connectingToConnectedSet = new AnimatorSet();

//            Animator animForce = getScaleOneTimeAnim(mScaleMinRadius);
            Animator animTranslate = getDragBallTranslateAnim(locatePointArr[0].x);
            Animator animColor = getContainerColorAnim(mDisableColor);
            Animator animScale = getScaleOneTimeAnim(mConnectedRadius);
            Animator animIconAlphaDisappear = getMainIconAlphaAnim(0xff, 0x00, R.mipmap.icon_ble, true);
            Animator animIconAlphaAppear = getMainIconAlphaAnim(0x00, 0xff, -1, true);

            // animScale & animIconAlphaDisappear -> animIconAlphaAppear & animTranslate -> animColor
            connectingToConnectedSet.play(animScale).with(animIconAlphaDisappear);
            connectingToConnectedSet.play(animTranslate).after(animScale);
            connectingToConnectedSet.play(animIconAlphaAppear).with(animTranslate);
            connectingToConnectedSet.play(animColor).after(animTranslate);
            connectingToConnectedSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isCancel) {
                        mBallSwitchState = SWITCH_STATE_CONNECTED;
                        mBallSwitchDrawState = DRAW_STATE_CONNECTED;
                    } else {

                    }
                    mAnimSet = null;
                    postInvalidate();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    isCancel = true;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mAnimSet = connectingToConnectedSet;
            mAnimSet.start();
        }

        drawBasic(canvas);
    }

    /**
     * 绘制状态： 已连接 到 断开 的过程
     *
     * @param canvas
     */
    private void drawConnectedToDisconnected(Canvas canvas) {
        if (mAnimSet == null && mBallSwitchDrawState == DRAW_STATE_CONNECTED_TO_DISCONNECTED) {
            if (mPullBall.mCurBall.x != mPullBall.mOriginBall.x) {
                mPullBall.startDragAnim(0.0f);
                mPullBall.refresh(mPullBall.mOriginBall.x, mPullBall.mOriginBall.y, mPullBallRadius);
            }
            AnimatorSet connectedToDisconnectedSet = new AnimatorSet();

            Animator animTranslate = getDragBallTranslateAnim(locatePointArr[1].x);
            Animator animColor = getContainerColorAnim(mEnableColor);
            Animator animScale = getScaleOneTimeAnim(mScaleMinRadius);
            Animator animIconAlphaDisappear = getMainIconAlphaAnim(0xff, 0x00, R.mipmap.icon_lock_locked, false);
            Animator animIconAlphaAppear = getMainIconAlphaAnim(0x00, 0xff, -1, false);

            // animColor & animIconAlphaDisappear-> animScale & animTranslate & animIconAlphaAppear
            connectedToDisconnectedSet.play(animColor).with(animIconAlphaDisappear);
            connectedToDisconnectedSet.play(animScale).after(animIconAlphaDisappear);
            connectedToDisconnectedSet.play(animTranslate).with(animScale);
            connectedToDisconnectedSet.play(animIconAlphaAppear).with(animTranslate);

            connectedToDisconnectedSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mBallSwitchState = SWITCH_STATE_DISCONNECTED;
                    mBallSwitchDrawState = DRAW_STATE_DISCONNECTED;
                    mAnimSet = null;
                    postInvalidate();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            mAnimSet = connectedToDisconnectedSet;
            mAnimSet.start();
        }

        drawBasic(canvas);
    }

    /**
     * 绘制状态：  到 正在开锁中 的状态
     *
     * @param canvas
     */
    private void drawToOpening(Canvas canvas) {
        if (mAnimSet == null && mBallSwitchDrawState == DRAW_STATE_TO_OPENING) {
            AnimatorSet toOpeningSet = new AnimatorSet();
            Animator animExpand = getExpandForOpenSuccessAnim();
            Animator animIconScale = getMainIconScaleAnim();
            Animator animIconAlphaDisappear = getMainIconAlphaAnim(0xff, 0x00, R.mipmap.icon_lock_locked, true);
            Animator animIconAlphaAppear = getMainIconAlphaAnim(0x00, 0xff, -1, true);
            animIconAlphaDisappear.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mOpeningAnim = getOpeningScaleRepeatAnim();
                    mOpeningAnim.start();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mBallSwitchDrawState = DRAW_STATE_OPENING;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            //animExpand -> animIconAlphaDisappear -> animIconAlphaAppear & animIconScale
            toOpeningSet.play(animExpand).before(animIconAlphaDisappear);
            toOpeningSet.play(animIconAlphaDisappear).before(animIconAlphaAppear);
            toOpeningSet.play(animIconAlphaAppear).with(animIconScale);

            toOpeningSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAnimSet = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mAnimSet = toOpeningSet;
            mAnimSet.start();
        }
        drawBasic(canvas);

    }

    /**
     * 绘制状态：正在开锁的状态
     *
     * @param canvas
     */
    public void drawOpening(Canvas canvas) {
        drawBasic(canvas);
        //小圆
        drawOpeningCircle(canvas);
    }

    public void drawOpenSuccess(Canvas canvas) {
        if (mAnimSet == null && mBallSwitchDrawState == DRAW_STATE_OPEN_SUCCESS) {
            AnimatorSet openSuccessSet = new AnimatorSet();
            Animator animIconAlphaDisappear = getMainIconAlphaAnim(0xff, 0x00, R.mipmap.icon_ble, true);
            animIconAlphaDisappear.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mPullBall.startDragAnim(0.0f);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            Animator animIconAlphaAppear = getMainIconAlphaAnim(0x00, 0xff, -1, true);
            Animator animNarrow = getPullBallNarrowForceAnim();
            openSuccessSet.play(animNarrow).after(animIconAlphaDisappear);
            openSuccessSet.play(animIconAlphaDisappear).before(animIconAlphaAppear);
            openSuccessSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mBallSwitchDrawState = DRAW_STATE_CONNECTED;
                    mAnimSet = null;
                    if (mOpeningAnim != null)
                        mOpeningAnim.cancel();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mAnimSet = openSuccessSet;
            mAnimSet.start();
        }
        drawBasic(canvas);
    }

    private void drawDisconnected(Canvas canvas) {
        drawBasic(canvas);
    }

    /**
     * 设置BallSwitch 状态
     */
    public void setSwitchState(int state) {
        //从连接中  ->  已连接
        Log.i(TAG, "setSwitchState - start || mBallSwitch:" + toString(mBallSwitchState) + "   mBallSwitchGoing:" + toString(mBallSwitchGoingState) + "   state:" + toString(state));
        if (mBallSwitchDrawState == DRAW_STATE_CONNECTING && state == SWITCH_STATE_CONNECTED) {

            mBallSwitchGoingState = state;
            mBallSwitchDrawState = DRAW_STATE_CONNECTING_TO_CONNECTED;

            if (mAnimSet != null) {
                mAnimSet.cancel();
                mAnimSet = null;
            }
            postInvalidate();
        }
        //从 连接中过渡到已连接  -> 连接断开
        else if ((mBallSwitchDrawState == DRAW_STATE_CONNECTING_TO_CONNECTED
                || mBallSwitchDrawState == DRAW_STATE_CONNECTED
                || mBallSwitchDrawState == DRAW_STATE_OPEN_SUCCESS
                || mBallSwitchDrawState == DRAW_STATE_OPENING
                || mBallSwitchDrawState == DRAW_STATE_TO_OPENING)
                && state == SWITCH_STATE_DISCONNECTED) {
            mBallSwitchGoingState = state;
            mBallSwitchDrawState = DRAW_STATE_CONNECTED_TO_DISCONNECTED;
            if (mAnimSet != null) {
                mAnimSet.cancel();
                mAnimSet = null;
            }
            postInvalidate();
        }
        //从已连接  -> 连接断开
        else if (mBallSwitchDrawState == DRAW_STATE_CONNECTED && state == SWITCH_STATE_DISCONNECTED) {

            mBallSwitchGoingState = state;
            if (mBallSwitchState == SWITCH_STATE_CONNECTED) {
                mBallSwitchDrawState = DRAW_STATE_CONNECTED_TO_DISCONNECTED;
            } else {
                mBallSwitchDrawState = DRAW_STATE_DISCONNECTED;
            }

            if (mAnimSet != null) {
                mAnimSet.cancel();
                mAnimSet = null;
            }

            postInvalidate();

        }
        //从连接断开 ->  连接中
        else if (mBallSwitchDrawState == DRAW_STATE_DISCONNECTED && state == SWITCH_STATE_CONNECTING) {
            mBallSwitchState = state;
            mBallSwitchGoingState = state;
            mBallSwitchDrawState = DRAW_STATE_CONNECTING;
            postInvalidate();
        }
        //从 连接中 -> 连接顿单开
        else if (mBallSwitchDrawState == DRAW_STATE_CONNECTING && state == SWITCH_STATE_DISCONNECTED) {

            mBallSwitchGoingState = state;
            mBallSwitchDrawState = DRAW_STATE_DISCONNECTED;

            if (mAnimSet != null) {
                mAnimSet.cancel();
                mAnimSet = null;
            }

            postInvalidate();

        } else if (mBallSwitchDrawState == DRAW_STATE_OPENING && state == SWITCH_STATE_OPEN) {
            mBallSwitchGoingState = state;
            mBallSwitchDrawState = DRAW_STATE_OPEN_SUCCESS;
            postInvalidate();
        }
        Log.i(TAG, "setSwitchState - end || mBallSwitch:" + toString(mBallSwitchState) + "   mBallSwitchGoing:" + toString(mBallSwitchGoingState) + "   state:" + toString(state));
    }

    /**
     * 震动
     */
    private void vibrator() {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(getContext().VIBRATOR_SERVICE);
        vibrator.vibrate(120);
    }


}

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
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.example.mysmall.newelasticballview.CanvasUtils;
import com.example.mysmall.newelasticballview.R;

import java.lang.ref.WeakReference;

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

    private Paint mPaint;
    private Paint mPaintBitmap;

    private PointF mCenterPoint;

    private int mBallSwitchGoingState = SWITCH_STATE_CONNECTING;

    private int mBallSwitchState = SWITCH_STATE_CONNECTING;
    public static final int SWITCH_STATE_CONNECTING = 11;
    public static final int SWITCH_STATE_CONNECTED = 12;
    public static final int SWITCH_STATE_DISCONNECTED = 13;

    private boolean isOpening = false;


    private int mBallSwitchDrawState = DRAW_STATE_CONNECTING;
    public static final int DRAW_STATE_CONNECTING = 1;
    public static final int DRAW_STATE_CONNECTED = 2;
    public static final int DRAW_STATE_DISCONNECTED = 3;
    public static final int DRAW_STATE_CONNECTING_TO_CONNECTED = 4;
    public static final int DRAW_STATE_CONNECTING_TO_DISCONNECTED = 5;
    public static final int DRAW_STATE_CONNECTED_TO_DISCONNECTED = 6;

    private PointF locatePointArr[] = new PointF[3];


    /**
     * --------------  BallSwitch控件 整体属性 -- 结束
     */



    /**
     * --------------  弹性球相关属性 -- 开始
     */
    //弹性球本体
    PullBall mPullBall;

    Ball mLocateBall;

    //弹性球在Canvas中的体现
    Path mPullBallPath;

    //弹性球的目的坐标
    PointF mPullBallTargetPoint;

    private int mPullBallMoveDuration = 800;

    //弹性球状态
    private int mElasticBallState = ELASTIC_STATE_STATIC;
    public static final int ELASTIC_STATE_STATIC = 0x0001;
    public static final int ELASTIC_STATE_CHANGING = 0x0002;

    //弹性球半径
    private int mPullBallRadius = 200;
    private int mPullBallMargin = 20;

    //弹性球颜色
    private int mEnableColor = 0xFFffffff;
    private int mDisableColor = 0xFFa5ecd9;

    private int mPullBallColor = mEnableColor;
    private int mContainerColor = mEnableColor;

    //球属性
    private int mScaleCircleRadius;
    private float mScaleMaxRadius = 300;
    private float mScaleMinRadius = mPullBallRadius;
    private float mConnectedRadius = 150;

    //球缩小放大动画
    private ValueAnimator mAnimScale;
    private int scaleDuration = 600;
    private int narrowDuration = 400;
    private int expandDuration = 3000;

    //动画集
    private AnimatorSet mAnimTransferSet;

    //开门成功动画
    private ValueAnimator mOpenSuccessAnim;

    private Bitmap mIcon;
    private int mIconSize = -1;
    private int mIconAlpha = 0xff;
    private float mIconY;
    private int txtSize;

    private String mPullBallTxt = "连接中";
    private int mPullBallTxtColor = 0xFF3fb57d;
    private PointF mPullBalTxtPointF;

    private String mContainerTxt = "右滑开锁";
    private int mContainerTxtColor = mPullBallColor;

    private boolean onLayout = false;

    private UIHandler mUIHandler = new UIHandler(this);
    private static final int UI_MSG_SWITCH_BACK = 0x1000;

    /**
     * --------------  弹性球相关属性 -- 结束
     */

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

    public void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mPullBallColor);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mPaintBitmap = new Paint();
        mPaintBitmap.setAntiAlias(true);

        setClickable(true);
        mIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_lock_locked);
        mIconSize = mIcon.getWidth() / 2;
    }

    private static class UIHandler extends Handler {

        private final WeakReference<View> mView;

        public UIHandler(View view) {
            mView = new WeakReference<View>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            YDBleSwitch view = (YDBleSwitch) mView.get();
            super.handleMessage(msg);
            switch (msg.what) {
                case UI_MSG_SWITCH_BACK:
                    view.mPullBall.startDragAnim(0.0f);
                    break;
            }
        }
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
//        mIconSize = (int)mWidth/12;
        Log.i(TAG, "width:" + mWidth + "\theight" + mHeight);
        mCenterPoint = new PointF(mWidth / 2, mHeight / 2);

        //初始化 ElasticBall
        mPullBall = new PullBall(mCenterPoint.x, mCenterPoint.y, mPullBallRadius);
        mPullBall.setDuration(mPullBallMoveDuration);

        mLocateBall = new Ball(mCenterPoint.x, mCenterPoint.y, mPullBallRadius);

        mPullBallTargetPoint = new PointF(mWidth / 3 * 2, mLocateBall.y);
        mPullBall.setTarget(mPullBallTargetPoint, new PullBall.DragBallInterface() {
            @Override
            public void onChange(Path path) {
                mElasticBallState = ELASTIC_STATE_CHANGING;
                mPullBallPath = path;
                postInvalidate();
            }

            @Override
            public void onFinish(float percent) {
                if (percent == 1.0f) {
                    mOpenSuccessAnim = getExpandForOpenSuccessAnim();
                    mOpenSuccessAnim.start();
                }
            }
        });
        mPullBallPath = mPullBall.drawPath();
        txtSize = (int) mWidth / 30;
        mIconY = mPullBall.mCurBall.y - txtSize;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.parseColor("#FF3FB57D"));
        switch (mBallSwitchDrawState) {
            case DRAW_STATE_CONNECTING:
                drawConnecting(canvas);
                break;
            case DRAW_STATE_DISCONNECTED:
                drawDisconnected(canvas);
                break;
            case DRAW_STATE_CONNECTED:
            case DRAW_STATE_CONNECTING_TO_CONNECTED:
                drawConnectingToConnected(canvas);
                break;
            case DRAW_STATE_CONNECTING_TO_DISCONNECTED:
                drawConnecting(canvas);
                break;
            case DRAW_STATE_CONNECTED_TO_DISCONNECTED:
                drawConnectedToDisconnected(canvas);
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
                if (mBallSwitchState != SWITCH_STATE_CONNECTED || mElasticBallState == ELASTIC_STATE_CHANGING)
                    break;

                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getX() > firstX) {
                    float percent = (event.getX() - firstX) / (mWidth - 2 * mLocateBall.x);
                    mPullBall.setPercent(percent);
                    if (mPullBall.getPercent() > 0.9) {
                        vibrator();
                        isOpening = true;
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
     * 画开关本体 PullBall
     *
     * @param canvas
     */
    private void drawPullBall(Canvas canvas) {
        //外形
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mPullBallColor);
        mPaint.setStrokeWidth(mPullBall.ball.radius * 1.5f);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawPath(mPullBallPath, mPaint);


        //文字 与 图标
        RectF rectf = null;
        // 以下情况PullBall 中没有文字，mIcon居中
        // 连接中到已连接、已连接、已连接到已断开
        if (mBallSwitchDrawState == DRAW_STATE_CONNECTING_TO_CONNECTED
                || mBallSwitchDrawState == DRAW_STATE_CONNECTED
                || mBallSwitchDrawState == DRAW_STATE_CONNECTED_TO_DISCONNECTED) {
            rectf = new RectF((int) mPullBall.mCurBall.x - mIconSize, mIconY - mIconSize, mPullBall.mCurBall.x + mIconSize, mIconY + mIconSize);
        }
        //其余情况有文字
        else {
            rectf = new RectF((int) mPullBall.mCurBall.x - mIconSize, mIconY - mIconSize, mPullBall.mCurBall.x + mIconSize, mIconY + mIconSize);
            CanvasUtils.initPaintForTxt(mPaintBitmap, mPullBallTxtColor, 255, txtSize);
            CanvasUtils.drawText(canvas, mPaintBitmap, mPullBallTxt, mPullBall.ball.x, mPullBall.mCurBall.y + txtSize + mIconSize);
        }
        mPaint.setAlpha(mIconAlpha);
        canvas.drawBitmap(mIcon, null, rectf, mPaint);
        mPaint.setAlpha(0xff);

    }

    /**
     * 画 外层包裹
     *
     * @param canvas
     */
    private void drawContainer(Canvas canvas) {

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mPullBallMargin);
        mPaint.setColor(mContainerColor);

        //环形跑道形状
        //中轴镜像球
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

        CanvasUtils.initPaintForTxt(mPaintBitmap, mContainerTxtColor , 255, txtSize);
        CanvasUtils.drawText(canvas, mPaintBitmap, mContainerTxt, mWidth/2,mHeight);

    }

    /**
     * mPullBall.radius 缩放  动画
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
     * 缩小 动画
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
     * mPullBall.x 平移  动画
     *
     * @return
     */
    private ValueAnimator getDragBallTranslateAnim(float translateToX) {
        ValueAnimator anim = ValueAnimator.ofFloat(mPullBall.ball.x, translateToX);
        float percent = Math.abs((mPullBall.ball.x - translateToX) / Math.abs(mWidth / 2.f - mWidth / 3.f));
        anim.setDuration((int) (scaleDuration * percent));
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mPullBall.refresh(value, mPullBall.ball.y, mPullBall.ball.radius);
                mPullBallPath = mPullBall.drawPath();
                mLocateBall.refresh(value, mLocateBall.y, mLocateBall.radius);
                postInvalidate();
            }
        });
        return anim;
    }

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
        return anim;
    }


    /**
     * mPullBall.radius 扩展
     *
     * @return
     */
    private ValueAnimator getExpandForOpenSuccessAnim() {
        ValueAnimator anim = ValueAnimator.ofFloat(mPullBall.ball.radius, mWidth, mPullBallRadius);
        anim.setDuration(expandDuration);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mPullBall.refresh(mPullBall.ball.x, mPullBall.ball.y, value);
                postInvalidate();
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mOpenSuccessAnim = null;
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


    private ValueAnimator getNarrowForceAnim() {
        ValueAnimator anim = ValueAnimator.ofFloat(mPullBall.ball.radius, mPullBallRadius);
        anim.setDuration(400);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mPullBall.refresh(mPullBall.ball.x, mPullBall.ball.y, value);
                postInvalidate();
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mOpenSuccessAnim = null;
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

    private ValueAnimator getIconAlphaAnim(int fromAlpha, int toAlpha, final int resId) {
        ValueAnimator anim = ValueAnimator.ofInt(fromAlpha, toAlpha);
        anim.setDuration(narrowDuration);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                mIconAlpha = value;
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
                    mIcon = BitmapFactory.decodeResource(getResources(), resId);
                    if (R.mipmap.icon_ble != resId) {
                        mIconY = mPullBall.mCurBall.y - txtSize;
                    } else {
                        mIconY = mPullBall.mCurBall.y;
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
     * 绘制 连接中状态
     *
     * @param canvas
     */
    private void drawConnecting(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        if (mAnimScale == null) {

            mAnimScale = getScaleRepeatAnim();
            mAnimScale.start();
        }
        drawContainer(canvas);
        drawPullBall(canvas);
    }

    /**
     * 绘制 从连接中到已连接  的过程
     *
     * @param canvas
     */
    private void drawConnectingToConnected(Canvas canvas) {
        if (mAnimTransferSet == null && mBallSwitchDrawState == DRAW_STATE_CONNECTING_TO_CONNECTED) {
            AnimatorSet animatorSet = new AnimatorSet();

            Animator animTranslate = getDragBallTranslateAnim(mWidth / 3);
            Animator animColor = getContainerColorAnim(mDisableColor);
            Animator animScale = getScaleOneTimeAnim(mConnectedRadius);
            Animator animIconAlphaDisappear = getIconAlphaAnim(0xff, 0x00, R.mipmap.icon_ble);
            Animator animIconAlphaAppear = getIconAlphaAnim(0x00, 0xff, -1);

            // animScale & animIconAlphaDisappear -> animIconAlphaAppear & animTranslate -> animColor
            animatorSet.play(animScale).with(animIconAlphaDisappear);
            animatorSet.play(animTranslate).after(animScale);
            animatorSet.play(animIconAlphaAppear).with(animTranslate);
            animatorSet.play(animColor).after(animTranslate);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mBallSwitchState = SWITCH_STATE_CONNECTED;
                    mBallSwitchDrawState = DRAW_STATE_CONNECTED;
                    mAnimTransferSet = null;
                    postInvalidate();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mAnimTransferSet = animatorSet;
            mAnimTransferSet.start();
        }

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
     * 绘制 已连接 到 断开 的过程
     *
     * @param canvas
     */
    private void drawConnectedToDisconnected(Canvas canvas) {
        if (mAnimTransferSet == null && mBallSwitchDrawState == DRAW_STATE_CONNECTED_TO_DISCONNECTED) {
            if (mOpenSuccessAnim != null) {
                mOpenSuccessAnim.cancel();
                mOpenSuccessAnim = getNarrowForceAnim();
                mOpenSuccessAnim.start();
            }
            AnimatorSet animatorSet = new AnimatorSet();
            Animator animTranslate = getDragBallTranslateAnim(mWidth / 2);
            Animator animColor = getContainerColorAnim(mEnableColor);
            Animator animScale = getScaleOneTimeAnim(mScaleMinRadius);
            Animator animIconAlphaDisappear = getIconAlphaAnim(0xff, 0x00, R.mipmap.icon_lock_locked);
            Animator animIconAlphaAppear = getIconAlphaAnim(0x00, 0xff, -1);

            // animColor & animIconAlphaDisappear-> animScale & animTranslate & animIconAlphaAppear
            animatorSet.play(animIconAlphaAppear).with(animTranslate);
            animatorSet.play(animIconAlphaDisappear).with(animColor);
            animatorSet.play(animScale).with(animTranslate);
            animatorSet.play(animTranslate).after(animColor);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mBallSwitchState = SWITCH_STATE_DISCONNECTED;
                    mBallSwitchDrawState = DRAW_STATE_DISCONNECTED;
                    mAnimTransferSet = null;
                    postInvalidate();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            mAnimTransferSet = animatorSet;
            mAnimTransferSet.start();
        }

        int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);

        //Container
        drawContainer(canvas);

        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        drawPullBall(canvas);

        mPaint.setXfermode(null);
        canvas.restoreToCount(sc);

    }

    /**
     * 绘制 已连接状态
     *
     * @param canvas
     */
    private void drawConnected(Canvas canvas) {

    }

    /**
     * 绘制 断开连接状态
     *
     * @param canvas
     */
    private void drawDisconnected(Canvas canvas) {
        drawContainer(canvas);
        drawPullBall(canvas);
    }

    /**
     * 设置BallSwitch 状态
     */
    public void setSwitchState(int state) {
        //从连接中  ->  已连接
        if (mBallSwitchState == SWITCH_STATE_CONNECTING && mBallSwitchGoingState != SWITCH_STATE_CONNECTED && state == SWITCH_STATE_CONNECTED) {
            mBallSwitchGoingState = state;
            if (mAnimScale != null) {
                mAnimScale.cancel();
                mAnimScale = getScaleOneTimeAnim(mScaleMinRadius);
                mAnimScale.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimScale = null;
                        mBallSwitchDrawState = DRAW_STATE_CONNECTING_TO_CONNECTED;
                        postInvalidate();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                mAnimScale.start();
            }
        }
        //从已连接  -> 连接断开
        else if ((mBallSwitchState == SWITCH_STATE_CONNECTED || mBallSwitchGoingState == SWITCH_STATE_CONNECTED) &&
                mBallSwitchGoingState != SWITCH_STATE_DISCONNECTED && state == SWITCH_STATE_DISCONNECTED) {
            mBallSwitchGoingState = state;
            if (mAnimTransferSet != null) {
                mAnimTransferSet.cancel();
                mAnimTransferSet = null;
            }
            if (mBallSwitchState == SWITCH_STATE_CONNECTED) {
                mBallSwitchDrawState = DRAW_STATE_CONNECTED_TO_DISCONNECTED;
            } else {
                mBallSwitchDrawState = DRAW_STATE_DISCONNECTED;
            }
            postInvalidate();

        }
        //从连接断开 ->  连接中
        else if (mBallSwitchState == SWITCH_STATE_DISCONNECTED && state == SWITCH_STATE_CONNECTING) {
            mBallSwitchState = state;
            mBallSwitchGoingState = state;
            mBallSwitchDrawState = DRAW_STATE_CONNECTING;
            postInvalidate();
        } else if (mBallSwitchState == SWITCH_STATE_CONNECTING && mBallSwitchGoingState != SWITCH_STATE_DISCONNECTED && state == SWITCH_STATE_DISCONNECTED) {
            mBallSwitchGoingState = state;
            if (mAnimScale != null) {
                mAnimScale.cancel();
                mAnimScale = getScaleOneTimeAnim(mScaleMinRadius);
                mAnimScale.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimScale = null;
                        mBallSwitchState = SWITCH_STATE_DISCONNECTED;
                        mBallSwitchDrawState = DRAW_STATE_DISCONNECTED;
                        postInvalidate();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                mAnimScale.start();
            }
        }
    }

    public void vibrator() {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(getContext().VIBRATOR_SERVICE);
        vibrator.vibrate(120);
    }


}

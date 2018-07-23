package com.example.mysmall.newelasticballview.elastic;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;

/**
 * Created by 49479 on 2018/7/18.
 */

public class YDElasticRoundRectBleSwitch extends View {

    private static final String TAG = YDElasticRoundRectBleSwitch.class.getName();

    /**
     * --------------  BallSwitch控件 整体属性 -- 开始
     */
    private float mWidth;
    private float mHeight;

    private Paint mPaint;

    private PointF mCenterPoint;

    private int mBallSwitchGoingState = SWITCH_STATE_CONNECTING;

    private int mBallSwitchState = SWITCH_STATE_CONNECTING;
    public static final int SWITCH_STATE_CONNECTING = 11;
    public static final int SWITCH_STATE_CONNECTED = 12;
    public static final int SWITCH_STATE_DISCONNECTED = 13;


    private int mBallSwitchDrawState = DRAW_STATE_CONNECTING;
    public static final int DRAW_STATE_CONNECTING = 1;
    public static final int DRAW_STATE_CONNECTED = 2;
    public static final int DRAW_STATE_DISCONNECTED = 3;
    public static final int DRAW_STATE_CONNECTING_TO_CONNECTED = 4;
    public static final int DRAW_STATE_CONNECTING_TO_DISCONNECTED = 5;
    public static final int DRAW_STATE_CONNECTED_TO_DISCONNECTED = 6;


    /**
     * --------------  BallSwitch控件 整体属性 -- 结束
     */


    /**
     * --------------  普通球相关属性 -- 开始
     */
    /**
     * --------------  普通球相关属性 -- 结束
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
    private int mDragBallRadius = 200;

    //弹性球颜色
    private int mElasticBallColor = 0xFFFFFFFF;

    //球属性
    private int mScaleCircleRadius;
    private float mScaleMaxRadius = 300;
    private float mScaleMinRadius = mDragBallRadius;

    //球缩小放大动画
    private ValueAnimator mAnimScale;
    private int scaleDuration = 600;
    private int narrowDuration = 400;
    private int expandDuration = 1200;

    //动画集
    private AnimatorSet mAnimTransferSet;

    //弧角度
    private float mArcDegree = 0;

    //弧颜色
    private int mArcColor = 0xFF777777;

    //弧宽
    private int mArcWidth = 22;


    /**
     * --------------  弹性球相关属性 -- 结束
     */

    public YDElasticRoundRectBleSwitch(Context context) {
        super(context);
        init();

    }

    public YDElasticRoundRectBleSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public YDElasticRoundRectBleSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mElasticBallColor);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        setClickable(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //获取布局数据
        mWidth = getWidth();
        mHeight = getHeight();
        Log.i(TAG, "width:" + mWidth + "\theight" + mHeight);
        mCenterPoint = new PointF(mWidth / 2, mHeight / 2);

        //初始化 ElasticBall
        mPullBall = new PullBall(mCenterPoint.x, mCenterPoint.y, mDragBallRadius);
        mPullBall.setDuration(mPullBallMoveDuration);

        mLocateBall = new Ball(mCenterPoint.x, mCenterPoint.y, mDragBallRadius);

        mPullBallTargetPoint = new PointF(mWidth /3*2, mLocateBall.y);
        mPullBall.setTarget(mPullBallTargetPoint, new PullBall.DragBallInterface() {
            @Override
            public void onChange(Path path) {
                mElasticBallState = ELASTIC_STATE_CHANGING;
                mPullBallPath = path;
                postInvalidate();
            }

            @Override
            public void onFinish() {

            }
        });

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
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
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mPullBall.getPercent() > 0.7) {
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
    private void drawElasticBall(Canvas canvas) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mElasticBallColor);
        mPaint.setStrokeWidth(mPullBall.ball.radius * 2);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawPath(mPullBallPath, mPaint);
    }

    /**
     * 画扇形
     */
    private void drawArc(Canvas canvas) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mArcWidth);
        mPaint.setColor(mArcColor);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        Path path = new Path();

        path.moveTo(mLocateBall.topX, mLocateBall.topY - mArcWidth / 2);

        RectF rectF = new RectF(mLocateBall.leftX - mArcWidth / 2, mLocateBall.topY - mArcWidth / 2, mLocateBall.rightX + mArcWidth / 2, mLocateBall.bottomY + mArcWidth / 2);
        path.arcTo(rectF, -90, mArcDegree, false);

        Log.i(TAG, "arc degree:" + mArcDegree);
        canvas.drawPath(path, mPaint);
    }

    /**
     * 画 外层包裹
     *
     * @param canvas
     */
    private void drawContainer(Canvas canvas) {

        if (mArcDegree > 355) {
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(mArcWidth);
            mPaint.setColor(mArcColor);

            //中轴镜像球
            Ball ball = new Ball(mWidth - mLocateBall.x, mLocateBall.y, mLocateBall.radius);

            Path path = new Path();

            path.moveTo(mLocateBall.topX, mLocateBall.topY - mArcWidth / 2);
            path.lineTo(ball.topX, ball.topY - mArcWidth / 2);

            RectF rectF1 = new RectF(ball.leftX - mArcWidth / 2, ball.topY - mArcWidth / 2, ball.rightX + mArcWidth / 2, ball.bottomY + mArcWidth / 2);
            path.arcTo(rectF1, -90, 180, false);

            path.lineTo(mLocateBall.bottomX, mLocateBall.bottomY + mArcWidth / 2);

            RectF rectF2 = new RectF(mLocateBall.leftX - mArcWidth / 2, mLocateBall.topY - mArcWidth / 2, mLocateBall.rightX + mArcWidth / 2, mLocateBall.bottomY + mArcWidth / 2);
            path.addArc(rectF2, 90, 180);
            path.close();

            canvas.drawPath(path, mPaint);
        }

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mBallSwitchState == SWITCH_STATE_CONNECTED ? mArcColor : mElasticBallColor);
        canvas.drawCircle(mLocateBall.x, mLocateBall.y, mLocateBall.radius, mPaint);

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
                mLocateBall.radius = value;
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
    private ValueAnimator getNarrowAnim() {
        ValueAnimator anim = ValueAnimator.ofFloat((int) mLocateBall.radius, mScaleMinRadius);
        anim.setDuration(narrowDuration);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mLocateBall.radius = value;
                postInvalidate();
            }
        });
        return anim;
    }

    /**
     * mArcDegree 0度 到 359度 动画
     *
     * @return
     */
    private ValueAnimator getArcAnim(final int toDegree) {
        ValueAnimator animArc = ValueAnimator.ofFloat(mArcDegree, toDegree);
        float percent = Math.abs(mArcDegree - toDegree) / 359;
        animArc.setDuration((int) (scaleDuration * percent));
        animArc.setInterpolator(new AccelerateDecelerateInterpolator());
        animArc.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mArcDegree = value;
                postInvalidate();
            }
        });
        animArc.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

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
        return animArc;
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
        anim.setInterpolator(new BounceInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mPullBall.setXY(value, mPullBall.ball.y);
                mPullBallPath = mPullBall.drawPath();
                mLocateBall = new Ball(value, mLocateBall.y, mLocateBall.radius);
                postInvalidate();
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
    }

    /**
     * 绘制 从连接中到已连接  的过程
     *
     * @param canvas
     */
    private void drawConnectingToConnected(Canvas canvas) {
        if (mAnimTransferSet == null && mBallSwitchDrawState == DRAW_STATE_CONNECTING_TO_CONNECTED) {
            AnimatorSet animatorSet = new AnimatorSet();

            Animator animArc = getArcAnim(359);
            Animator animTranslate = getDragBallTranslateAnim(mWidth / 3);

            animatorSet.play(animArc).before(animTranslate);
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

        //弧
        drawArc(canvas);

        int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);

        //Container
        drawContainer(canvas);

        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        //球
        if (mBallSwitchState == SWITCH_STATE_CONNECTED)
            drawElasticBall(canvas);

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
            AnimatorSet animatorSet = new AnimatorSet();
            Animator animTranslate = getDragBallTranslateAnim(mWidth / 2);
            Animator animArc = getArcAnim(0);
            if (mArcDegree > 355) {
                animatorSet.play(animTranslate).before(animArc);
            } else {
                animatorSet.play(animArc);
            }
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

        //弧
        drawArc(canvas);

        int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);

        //Container
        drawContainer(canvas);

        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        //球
        if (mBallSwitchState == SWITCH_STATE_CONNECTED)
            drawElasticBall(canvas);

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
                mAnimScale = getNarrowAnim();
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
                mAnimScale = getNarrowAnim();
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

}

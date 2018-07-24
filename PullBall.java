package com.example.mysmall.newelasticballview.elastic;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PointF;

/**
 * Created by 49479 on 2018/7/19.
 */

public class PullBall {

    public Ball mOriginBall;

    private Path mDragPath;

    private int toTargetDuration = 100;

    private PointF mTarget;

    public Ball mCurBall;

    private float percent = 0;

    private DragBallInterface mDragBallInterface;

    public interface DragBallInterface {
        void onChange(Path path);

        void onFinish(float percent);
    }

    public PullBall(float x, float y, float radius) {
        mOriginBall = new Ball(x, y, radius);
        mCurBall = new Ball(x, y, radius / 2);
        mTarget = new PointF(x, y);
    }

    public void setToTargetDuration(int toTargetDuration) {
        this.toTargetDuration = toTargetDuration;
    }

    public void startDragAnim(final float TargetPercent) {

        ValueAnimator animator = ValueAnimator.ofFloat(percent, TargetPercent);
        animator.setDuration((int) (Math.abs(TargetPercent - percent) * toTargetDuration));

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                percent = value;
                if (mDragBallInterface != null)
                    mDragBallInterface.onChange(drawPath());
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mDragBallInterface != null)
                    mDragBallInterface.onFinish(TargetPercent);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.start();
    }

    public void setTarget(PointF target, DragBallInterface dragBallInterface) {
        mDragBallInterface = dragBallInterface;
        mTarget = target;
    }

    public void setPercent(float percent) {
        if (percent <= 1.0) {
            this.percent = percent;
            mDragBallInterface.onChange(drawPath());
        }
    }

    public float getPercent() {
        return percent;
    }

    public Path drawPath() {
        Path path = new Path();
        path.moveTo(mOriginBall.x, mOriginBall.y);
        path.lineTo(mOriginBall.x + percent * (mTarget.x - mOriginBall.x), mOriginBall.y + percent * (mTarget.y - mOriginBall.y));
        mCurBall.refresh(mOriginBall.x + percent * (mTarget.x - mOriginBall.x), mOriginBall.y + percent * (mTarget.y - mOriginBall.y), mCurBall.radius);
        return path;
    }

    public void refresh(float x, float y, float radius) {
        mOriginBall.refresh(x, y, radius);
    }

}

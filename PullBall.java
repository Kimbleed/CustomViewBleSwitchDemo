package com.example.mysmall.newelasticballview.elastic;


import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PointF;

/**
 * Created by 49479 on 2018/7/19.
 */

public class PullBall {

    public Ball ball;

    private Path mDragPath;

    private int duration = 800;

    private PointF mTarget;

    private float percent = 0;

    private DragBallInterface mDragBallInterface;

    public interface DragBallInterface {
        void onChange(Path path);

        void onFinish();
    }

    public PullBall(float x, float y, float radius) {
        ball = new Ball(x, y, radius);
        mTarget = new PointF(x, y);
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void startDragAnim(float TargetPercent) {

        ValueAnimator animator = ValueAnimator.ofFloat(percent, TargetPercent);
        animator.setDuration((int)(Math.abs(TargetPercent - percent) * duration));

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                percent = value;
                mDragBallInterface.onChange(drawPath());
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
        path.moveTo(ball.x, ball.y);
        path.lineTo(ball.x + percent * (mTarget.x - ball.x), ball.y + percent * (mTarget.y - ball.y));
        return path;
    }

    public void setXY(float x, float y) {
        ball = new Ball(x, y, ball.radius);
    }

}

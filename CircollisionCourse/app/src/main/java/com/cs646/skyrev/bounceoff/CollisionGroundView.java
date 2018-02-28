package com.cs646.skyrev.bounceoff;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import java.util.LinkedHashSet;

/**
 * Created by skyrev on 2/25/18.
 */

public class CollisionGroundView extends View implements View.OnTouchListener {

    boolean scaling;
    int screenWidth;
    int screenHeight;
    int circleCount;
    float currentX;
    float currentY;
    float veloX;
    float veloY;
    float dampingFactor = -0.9f;

    Circle currentCircle;
    VelocityTracker velocityTracker;
    LinkedHashSet<Circle> circles = new LinkedHashSet<>();

    static Paint paint;
    static {
        paint = new Paint();
        paint.setColor(Color.parseColor("#E12D53"));
    }

    public CollisionGroundView(Context context) {
        super(context);
    }

    public CollisionGroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        setOnTouchListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(scaling && currentCircle != null) {
            canvas.drawCircle(currentCircle.getCentreX(), currentCircle.getCentreY(),
                    currentCircle.getRadius(), paint);
            scaleCircle();
        }
        moveCircle();
        for(Circle circle : circles) {
            if (circle != null)
                canvas.drawCircle(circle.getCentreX(), circle.getCentreY(), circle.getRadius(), paint);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        switch (actionCode) {
            case MotionEvent.ACTION_DOWN:
                return handleActionDown(event);

            case MotionEvent.ACTION_MOVE:
                return handleActionMove(event);

            case MotionEvent.ACTION_UP:
                return handleActionUp(event);

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                return false;
        }
        return false;
    }

    private boolean handleActionDown(MotionEvent event) {
        if(velocityTracker == null)
            velocityTracker = VelocityTracker.obtain();
        currentX = event.getX();
        currentY = event.getY();
        if(circleCount < 15 && !isOverlapping(currentX, currentY)) {
            scaling = true;
            currentCircle = new Circle(currentX, currentY);
            scaleCircle();
        }
        else
            scaling = false;
        return true;
    }

    private boolean handleActionMove(MotionEvent event) {
        currentX = event.getX();
        currentY = event.getY();
        velocityTracker.addMovement(event);
        velocityTracker.computeCurrentVelocity(10);
        veloX = velocityTracker.getXVelocity();
        veloY = velocityTracker.getYVelocity();

        if(isOverlapping(currentX, currentY)) {
            currentCircle = getSelectedCircle();
            if (currentCircle != null) {
                currentCircle.setVeloX(veloX);
                currentCircle.setVeloY(veloY);
                moveCircle();
            }
        }
        return true;
    }

    private boolean handleActionUp(MotionEvent event) {
        if(scaling && circleCount < 15) {
            circles.add(currentCircle);
            circleCount = circles.size();
        }
        scaling = false;

        return true;
    }

    // checks if the centre of the circle being drawn is inside an existing circle
    private boolean isOverlapping(float x, float y) {
        for(Circle circle : circles) {
            if(circle != null) {
                double distance = Math.sqrt(
                        Math.pow(x - circle.getCentreX(), 2)
                                + Math.pow(y - circle.getCentreY(), 2)
                );
                if (distance < circle.getRadius())
                    return true;
            }
        }
        return false;
    }

    // checks if the circle being drawn collides with an existing circle
    private boolean isCollidingWithOtherCircle(float x, float y, float r) {
        for (Circle circle : circles) {
            if(circle != null) {
                double distance = Math.sqrt(
                        Math.pow(x - circle.getCentreX(), 2)
                                + Math.pow(y - circle.getCentreY(), 2)
                );
                double difference = Math.floor(Math.abs(distance - (circle.getRadius() + r)));
                if (difference >= 0 && difference <= 2)
                    return true;
            }
        }
        return false;
    }

    // checks if the circle being drawn collides with the vertical edges of the screen
    private boolean isXOutOfBounds(float x, float r) {
        return (x + r >= screenWidth || x - r <= 0.0f);
    }

    // checks if the circle being drawn collides with the horizontal edges of the screen
    private boolean isYOutOfBounds(float y, float r) {
        return (y + r >= screenHeight || y - r <= 0);
    }

    // returns the selected circle
    private Circle getSelectedCircle() {
        for(Circle circle : circles) {
            if(circle != null) {
                double distance = Math.sqrt(
                        Math.pow(currentX - circle.getCentreX(), 2)
                                + Math.pow(currentY - circle.getCentreY(), 2)
                );
                if (distance < circle.getRadius())
                    return circle;
            }
        }
        return null;
    }

    // scales the circle being drawn
    private void scaleCircle() {
        if(scaling && circleCount < 15 && currentCircle != null) {
            float x = currentCircle.getCentreX();
            float y = currentCircle.getCentreY();
            float r = currentCircle.getRadius();
            if (!isOverlapping(x, y)
                    && !(isXOutOfBounds(x, r) || isYOutOfBounds(y, r))
                    && !isCollidingWithOtherCircle(x, y, r)) {
                currentCircle.setRadius(currentCircle.getRadius() + 2);
            }
            invalidate();
        }
    }

    // moves the circle in the direction of the swipe/collision
    private void moveCircle() {
        for(Circle circle : circles) {
            if(circle != null) {
                float x = circle.getCentreX();
                float y = circle.getCentreY();
                float r = circle.getRadius();
                float vx = circle.getVeloX();
                float vy = circle.getVeloY();

                if (!isXOutOfBounds(x, r) && !isXOutOfBounds(x + vx, r))
                    circle.setCentreX(x + vx);
                else
                    circle.setVeloX(vx * dampingFactor);

                if (!isYOutOfBounds(y, r) && !isYOutOfBounds(y + vy, r))
                    circle.setCentreY(y + vy);
                else
                    circle.setVeloY(vy * dampingFactor);
            }
        }
        invalidate();
    }

    // reset view
    public void reset() {
        scaling = false;
        currentCircle = null;
        currentX = 0.0f;
        currentY = 0.0f;
        veloX = 0.0f;
        veloY = 0.0f;
        circles.clear();
        circleCount = 0;
        velocityTracker = null;
    }

}

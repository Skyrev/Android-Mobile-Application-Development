package com.cs646.skyrev.circollisioncourse;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedHashSet;

/**
 * Created by skyrev on 2/25/18.
 */

public class CollisionGroundView extends View implements View.OnTouchListener {

    Circle currentCircle;
    LinkedHashSet<Circle> circles = new LinkedHashSet<>();
    boolean scaling;
    int screenWidth;
    int screenHeight;
    int circleCount;
    float currentX;
    float currentY;

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
        if(scaling) {
            canvas.drawCircle(currentCircle.getCentreX(), currentCircle.getCentreY(),
                    currentCircle.getRadius(), CollisionGroundView.paint);
            scaleCircle();
        }
        else {
            moveCircle();
        }
        for(Circle circle : circles) {
            canvas.drawCircle(circle.getCentreX(), circle.getCentreY(), circle.getRadius(),
                    CollisionGroundView.paint);
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
        currentX = event.getX();
        currentY = event.getY();
        if(circleCount < 15 && !isOverlapping(currentX, currentY)) {
            scaling = true;
            currentCircle = new Circle(currentX, currentY);
            scaleCircle();
        }
        else {
            scaling = false;
        }
        return true;
    }

    private boolean handleActionMove(MotionEvent event) {
        currentX = event.getX();
        currentY = event.getY();
        if(isOverlapping(currentX, currentY)) {
            scaling = false;
            moveCircle();
        }
        return true;
    }

    private boolean handleActionUp(MotionEvent event) {
        scaling = false;
        if(circleCount < 15) {
            circles.add(currentCircle);
            circleCount = circles.size();
        }
        return true;
    }

    // checks if the centre of the circle being drawn is inside an existing circle
    private boolean isOverlapping(float x, float y) {
        for(Circle circle : circles) {
            double distance = Math.sqrt(
                                        Math.pow(x - circle.getCentreX(), 2)
                                                + Math.pow(y - circle.getCentreY(), 2)
                                        );
            if(distance < circle.getRadius())
                return true;
        }
        return false;
    }

    // checks if the circle being drawn collides with the screen edge and/or an existing circle
    private boolean isColliding(float x, float y, float r) {
        boolean isOutOfBounds = isXOutOfBounds(x, r) || isYOutOfBounds(y, r);

        // check if the circle being drawn collides with the screen edges
        if(isOutOfBounds)
            return true;
        else {
            // check if the circle being drawn collides with an existing circle
            for (Circle circle : circles) {
                double distance = Math.sqrt(
                        Math.pow(x - circle.getCentreX(), 2)
                                + Math.pow(y - circle.getCentreY(), 2)
                );
                double difference = Math.abs(distance - (circle.getRadius() + r));
                if (difference >= 0 && difference <= 1)
                    return true;
            }
        }
        return false;
    }

    // checks if the circle being drawn collides with the vertical edges of the screen
    private boolean isXOutOfBounds(float x, float r) {
        if(x + r > screenWidth || x - r < 0)
            return true;
        return false;
    }

    // checks if the circle being drawn collides with the horizontal edges of the screen
    private boolean isYOutOfBounds(float y, float r) {
        if(y + r > screenHeight || y - r < 0)
            return true;
        return false;
    }

    // returns the selected circle
    private Circle getSelectedCircle() {
        for(Circle circle : circles) {
            double distance = Math.sqrt(
                    Math.pow(currentX - circle.getCentreX(), 2)
                            + Math.pow(currentY - circle.getCentreY(), 2)
            );
            if(distance < circle.getRadius())
                return circle;
        }
        return null;
    }

    // scales the circle being drawn
    private void scaleCircle() {
        if(currentCircle != null) {
            float x = currentCircle.getCentreX();
            float y = currentCircle.getCentreY();
            float r = currentCircle.getRadius();
            if (circleCount < 15
                    && scaling
                    && !isOverlapping(x, y)
                    && !isColliding(x, y, r)) {

                currentCircle.setRadius(currentCircle.getRadius() + 1);
            }
            invalidate();
        }
    }

    // sets the selected circle in motion with velocity of the swipe
    private void moveCircle() {
        Circle selectedCircle = getSelectedCircle();
        if(selectedCircle != null && !isColliding(currentX, currentY, selectedCircle.getRadius())) {
            selectedCircle.setCentreX(currentX);
            selectedCircle.setCentreY(currentY);
            invalidate();
        }
    }

}

/*
 * The MIT License (MIT)
 *
 * FXGL - JavaFX Game Library
 *
 * Copyright (c) 2015-2017 AlmasB (almaslvl@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.almasb.fxglgames.pong;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import com.almasb.fxgl.dsl.FXGL;

import static com.almasb.fxgl.dsl.FXGL.*;
import static java.lang.Math.abs;
import static java.lang.Math.signum;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class BallComponent extends Component {

    private PhysicsComponent physics;
    public boolean respawn = false;

    @Override
    public void onUpdate(double tpf) {
        // Don't handle ball movement until a player has moved.
        if (FXGL.getGameWorld().getEntitiesByComponent(BatComponent.class).get(0).getX() != 320 || FXGL.getGameWorld().getEntitiesByComponent(BatComponent.class).get(0).getY() != 380 || FXGL.getGameWorld().getEntitiesByComponent(BatComponent.class).get(1).getX() != 940 || FXGL.getGameWorld().getEntitiesByComponent(BatComponent.class).get(1).getY() != 380) {
            slowDownBall();
            screenBoundsCollision();
            checkOffscreen();
            resetPosition();
        }

    }


    // this is a hack:
    // we use a physics engine, so it is possible to push the ball through a wall to outside of the screen
    private void checkOffscreen() {
        if (getEntity().getBoundingBoxComponent().isOutside(getGameScene().getViewport().getVisibleArea())) {
            physics.overwritePosition(new Point2D(
                    getAppWidth() / 2,
                    getAppHeight() / 2
            ));
        }
    }

    public void resetPosition() {
        if (respawn){
        physics.overwritePosition(new Point2D(
                getAppWidth() / 2 ,
                getAppHeight() / 2 - 5 + 40
        ));
        respawn = false;
        }
    }

    public void setVelocity(int yVel, boolean direction, double xVelocity) {
        physics.setVelocityY(yVel * 60);
        physics.setVelocityX(xVelocity * 60 > 0 ? 540 : (xVelocity * 60 < 0 ? -540 : physics.getVelocityX() * -1));
    }

    public void screenBoundsCollision(){
        // Handles top and bottom collision
        if (entity.getBottomY() - entity.getHeight() <= 80){
            physics.setVelocityY(60);
        }
        if (entity.getBottomY() >= getAppHeight() - 10){
            physics.setVelocityY(-60);
        }

        // Handles left and right collision
        if (entity.getRightX() - entity.getWidth() <= 50) {
            physics.setVelocityX(60);
        }
        if (entity.getRightX() >= getAppWidth() - 50) {
            physics.setVelocityX(-60);
        }
    }

    // Need to handle jittery speed up movement when ball slows down
    public void slowDownBall() {
        if (abs(physics.getVelocityX()) >= 180) {
            physics.setVelocityX(physics.getVelocityX() * 0.92);
        }
       else if (abs(physics.getVelocityX()) >= 60) {
            physics.setVelocityX(physics.getVelocityX() * 0.99);
        } else {
            physics.setVelocityX(physics.getVelocityX() > 0 ? 60 : -60);
        }

        if (abs(physics.getVelocityY()) > 60) {
            physics.setVelocityY(physics.getVelocityY() * 0.99);
        }
    }

}

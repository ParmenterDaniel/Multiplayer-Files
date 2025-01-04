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

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import javafx.geometry.Point2D;

import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class BatComponent extends Component {

    private static final double BAT_SPEED = 180;
    private static final double BAT_SPEED_X = 180;

    public boolean respawn = false;
    //public int id = 0;

    //////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////

    protected PhysicsComponent physics;

    public void up() {
        if (entity.getBottomY() >= 134) {
            physics.setVelocityY(-BAT_SPEED);
            //System.out.println("Y Pos: " + entity.getPosition().getY());
        }else
            stop();
    }

    public void down() {
        if (entity.getBottomY() <= FXGL.getAppHeight() - (BAT_SPEED / 60) - 7)
            physics.setVelocityY(BAT_SPEED);
        else
            stop();
    }

    public void left() {
        if (entity.getX() >= 5 - (BAT_SPEED_X/60) )
            physics.setVelocityX(-BAT_SPEED_X);
        else
            stop();
    }

    public void right() {
        if (entity.getRightX() <= FXGL.getAppWidth() - (BAT_SPEED_X/60))
            physics.setVelocityX(BAT_SPEED_X);
        else
            stop();
    }

    public void stop() {
        physics.setLinearVelocity(0, 0);
    }

    public double getXVel() {
        return physics.getVelocityX();
    }

    public double getYVel() {
        return physics.getVelocityY();
    }

    public void resetPosition(int player) {
        if (player == 1){
            physics.overwritePosition(new Point2D(
                    getAppWidth() / 4, getAppHeight() / 2 + 20
            ));
        } else if (player == 2){
            physics.overwritePosition(new Point2D(
                    3 * getAppWidth() / 4 - 20, getAppHeight() / 2 + 20
            ));
        }
    }

    /* Handle bats colliding with goal.
    @Override
    public void onUpdate(double tpf) {
        double BallX = FXGL.getGameWorld().getEntitiesByComponent(BallComponent.class).get(0).getX();
        double BallY = FXGL.getGameWorld().getEntitiesByComponent(BallComponent.class).get(0).getY();
        moveAI(BallX,BallY);
    }*/
}

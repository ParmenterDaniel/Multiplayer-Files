package com.almasb.fxglgames.pong;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.scene.paint.Color;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import static com.almasb.fxgl.dsl.FXGL.*;
import static java.lang.Math.abs;
import static java.lang.Math.signum;
import com.almasb.fxgl.time.TimerAction;

public class AIComponent extends Component {
    public enum Role{
        Team_1,
        Team_2
    }

    public enum State{
        IDLE,
        SWITCH_HALF,
        KEEP_APART
    }

    private State state = State.IDLE;
    private Role role;
    private PhysicsComponent physics;

    private double lastX = 0;
    private double lastY = 0;
    private double lastUpdateTime = 0;
    double predictedBallX = 0;
    double predictedBallY = 0;

    public AIComponent(Role role) {
        this.role = role;
    }

    @Override
    public void onUpdate(double tpf) {
        // Don't handle AI movement until a player has moved
        if (FXGL.getGameWorld().getEntitiesByComponent(BatComponent.class).get(0).getX() != getAppWidth() / 4) {
            handleAIMovement();
        }
        /*checkHitScreenBounds();
        double BallX = FXGL.getGameWorld().getEntitiesByComponent(BallComponent.class).get(0).getX();
        double BallY = FXGL.getGameWorld().getEntitiesByComponent(BallComponent.class).get(0).getY();
        //System.out.println(FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(0).getPosition()); THIS GETS POSITION OF TEAM 1 AI
        Point2D AiTeam1 = FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(0).getPosition();
        Point2D AiTeam2 = FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(1).getPosition();
        //System.out.println(AiTeam1.distance(AiTeam2));
        //physics.setVelocityY(10);
       if (AiTeam1.distance(AiTeam2) > 100) {
            moveAI(BallX, BallY);
        } else {
            moveAway();
        }*/
    }

    private void handleAIMovement() {
        // Get AI positions
        Point2D AiTeam1 = FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(0).getPosition();
        Point2D AiTeam2 = FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(1).getPosition();

        // Get co-ordinates of AI player
        double x = entity.getX();
        double y = entity.getY();

        // Check if AI is too close to screen edge.
        // Move away if too close, handle normal movement otherwise
        if (y < 680 && y > 90 && x > 80 && x < 1200) {
            handleHalfSwitch();
            if (AiTeam1.distance(AiTeam2) > 40 && state!= State.SWITCH_HALF) {
                moveTowardsBall();
                //moveAI();
            } else if (state != State.SWITCH_HALF) {
                moveAway();
            }
        } else {
            if (x <= 80) {
                physics.setVelocityX(1);
            } else if (x >= 1200) {
                physics.setVelocityX(-1);
            } else if (y <= 90) {
                physics.setVelocityY(1);
            } else if (y >= 680) {
                physics.setVelocityY(-1);
            }
        }
    }

    private void handleHalfSwitch() {
        // Handle for Team1 AI
        if (role == Role.Team_1) {
            if(FXGL.getGameWorld().getEntitiesByComponent(BatComponent.class).get(0).getX() <= 640 && FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(0).getX() <= 640){
                state = State.SWITCH_HALF;
                physics.setVelocityX(80);
            }
            else if (FXGL.getGameWorld().getEntitiesByComponent(BatComponent.class).get(0).getX() > 640 && FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(0).getX() > 640) {
                physics.setVelocityX(-80);
                state = State.SWITCH_HALF;
            } else {
                state = State.IDLE;
            }
        }

        // Handle for Team2 AI
        if (role == Role.Team_2) {
            if (FXGL.getGameWorld().getEntitiesByComponent(BatComponent.class).get(1).getX() <= 640 && FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(1).getX() <= 640) {
                state = State.SWITCH_HALF;
                physics.setVelocityX(80);
            } else if (FXGL.getGameWorld().getEntitiesByComponent(BatComponent.class).get(1).getX() > 640 && FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(1).getX() > 640) {
                state = State.SWITCH_HALF;
                physics.setVelocityX(-80);
            } else {
                state = State.IDLE;
            }
        }
    }

    private void moveTowardsBall() {

        double BallX = FXGL.getGameWorld().getEntitiesByComponent(BallComponent.class).get(0).getX();
        double BallY = FXGL.getGameWorld().getEntitiesByComponent(BallComponent.class).get(0).getY();

        double currentTime = FXGL.getGameTimer().getNow();

        if (state != State.SWITCH_HALF) {
            if (lastX != 0 && lastY != 0) {
                double ballVelocityX = (BallX - lastX) * 60;
                double ballVelocityY = (BallY - lastY) * 60;

                System.out.println("Ball velocity - X: " + ballVelocityX + ", Y: " + ballVelocityY);

                // Only recalculate if its been 3 seconds since last
                if (currentTime - lastUpdateTime >= 3) {
                    lastUpdateTime = currentTime;
                    predictedBallX = BallX + (ballVelocityX * 2);
                    predictedBallY = BallY + (ballVelocityY * 2);
                }

                if (Math.abs(entity.getX() - predictedBallX) > 20) {
                    if (predictedBallX > entity.getX()) {
                        physics.setVelocityX(80);
                    } else if (predictedBallX < entity.getX()){
                        physics.setVelocityX(-80);
                    } else {
                        physics.setVelocityX(0);
                    }
                } else {
                    physics.setVelocityX(0);
                }

                if (Math.abs(entity.getY() - predictedBallY) > 20) {
                    if (predictedBallY > entity.getY()) {
                        physics.setVelocityY(80);
                    } else if (predictedBallY < entity.getY()) {
                        physics.setVelocityY(-80);
                    } else {
                        physics.setVelocityY(0);
                    }
                } else {
                    physics.setVelocityY(0);
                }
            }
        }
        lastX = BallX;
        lastY = BallY;

    }

    private void moveAI(){
        double BallX = FXGL.getGameWorld().getEntitiesByComponent(BallComponent.class).get(0).getX();
        double BallY = FXGL.getGameWorld().getEntitiesByComponent(BallComponent.class).get(0).getY();
        // Set state of AI to keep away from player teammate
        // AI player should always be in opposite half of pitch to player teammate
        // Handled for Team 1 AI
        // Handle keeping Team 1 AI in opposite half to Team 1 Player
        if (role == Role.Team_1) {
            if (FXGL.getGameWorld().getEntitiesByComponent(BatComponent.class).get(0).getX() <= 640 && FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(0).getX() <= 640) {
                    physics.setVelocityX(100);
            } else if (FXGL.getGameWorld().getEntitiesByComponent(BatComponent.class).get(0).getX() > 640 && FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(0).getX() > 640) {
                    physics.setVelocityX(-100);
            } else {
                    moveToBall(BallX, BallY);
            }
        }

        // Handled for Team 2 AI
        // Handle keeping Team 2 AI in opposite half to Team 2 Player
        if (role == Role.Team_2) {
            if (FXGL.getGameWorld().getEntitiesByComponent(BatComponent.class).get(1).getX() <= 640 && FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(1).getX() <= 640) {
                physics.setVelocityX(100);
            } else if (FXGL.getGameWorld().getEntitiesByComponent(BatComponent.class).get(1).getX() > 640 && FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(1).getX() > 640) {
                physics.setVelocityX(-100);
            } else {
                moveToBall(BallX, BallY);
            }
        }

    }

    public void moveToBall(double x, double y) {
        // Need to change to move to where the ball will be so AI hits the ball more often
        // These 2 statements move the AI players towards the ball
        if(Math.abs(entity.getX() - x) < 300 && Math.abs(entity.getY() - y ) < 300){
            if (x > entity.getX()) {
                physics.setVelocityX(20);
            } else {
                physics.setVelocityX(-20);
            }
        } else {
            physics.setVelocityX(0);
        }

        if(Math.abs(entity.getY() - y ) < 300 && Math.abs(entity.getX() - x) < 300){
            if (y > entity.getY()) {
                physics.setVelocityY(20);
            } else {
                physics.setVelocityY(-20);
            }
        } else {
            physics.setVelocityY(0);
        }
    }

    private void moveAway(){
        // Get Positions
        Point2D AiTeam1Pos = FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(0).getPosition();
        Point2D AiTeam2Pos = FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(1).getPosition();

        if (role == Role.Team_1) {
            if (AiTeam1Pos.getX() < AiTeam2Pos.getX()) {
                physics.setVelocityX(60);
            } else if (AiTeam1Pos.getX() > AiTeam2Pos.getX()) {
                physics.setVelocityX(-60);
            } else {
                physics.setVelocityX(60);
            }
        }

        if (role == Role.Team_1) {
            if (AiTeam1Pos.getY() < AiTeam2Pos.getY()) {
                physics.setVelocityY(60);
            } else if (AiTeam1Pos.getY() > AiTeam2Pos.getY()) {
                physics.setVelocityY(-60);
            } else {
                physics.setVelocityY(60);
            }
        }

        if (role == Role.Team_2) {
            if (AiTeam2Pos.getX() < AiTeam1Pos.getX()) {
                physics.setVelocityX(60);
            } else if (AiTeam2Pos.getX() > AiTeam1Pos.getX()) {
                physics.setVelocityX(-60);
            } else {
                physics.setVelocityX(-60);
            }
        }

        if (role == Role.Team_2) {
            if (AiTeam2Pos.getY() < AiTeam1Pos.getY()) {
                physics.setVelocityY(60);
            } else if (AiTeam2Pos.getY() > AiTeam1Pos.getY()) {
                physics.setVelocityY(-60);
            } else {
                physics.setVelocityY(-60);
            }
        }

        state = State.KEEP_APART;
        physics.setLinearVelocity(physics.getVelocityX() * -1,physics.getVelocityY() * -1);
    }

    private void checkHitScreenBounds() {
        System.out.println(entity.getBottomY());
        System.out.println(getAppHeight() - (entity.getHeight()));
        if(entity.getY() >= getAppHeight() - (entity.getHeight()) - 50) {
            //entity.setY(FXGL.getAppHeight() - entity.getHeight());
            physics.setVelocityY(0);
            //entity.setY(getAppHeight() - entity.getHeight() - 1);
            System.out.println("Hit bottom");
        }
    }

    public void resetPosition(int player) {
        if (player == 1){
            physics.overwritePosition(new Point2D(
                    getAppWidth() / 2 - 100, 180
            ));
        } else if (player == 2){
            physics.overwritePosition(new Point2D(
                    getAppWidth() / 2 + 100, 580
            ));
        }
    }
}


/* MORE EFFICIENT CODE TO BE IMPLEMENTED
private void handleTeamMovement(Role role, double BallX, double BallY) {
    // Determine the team index (0 for Team 1, 1 for Team 2)
    int teamIndex = (role == Role.Team_1) ? 0 : 1;

    // Get Bat and AI components for the specific team
    BatComponent bat = FXGL.getGameWorld().getEntitiesByComponent(BatComponent.class).get(teamIndex);
    AIComponent ai = FXGL.getGameWorld().getEntitiesByComponent(AIComponent.class).get(teamIndex);

    // Check positions for both Bat and AI to determine movement
    if (bat.getX() <= 640 && ai.getX() <= 640) {
        physics.setVelocityX(100);
    } else if (bat.getX() > 640 && ai.getX() > 640) {
        physics.setVelocityX(-100);
    } else {
        moveToBall(BallX, BallY);
    }
}

@Override
public void onUpdate(double tpf) {
    double BallX = FXGL.getGameWorld().getEntitiesByComponent(BallComponent.class).get(0).getX();
    double BallY = FXGL.getGameWorld().getEntitiesByComponent(BallComponent.class).get(0).getY();

    if (role == Role.Team_1) {
        handleTeamMovement(Role.Team_1, BallX, BallY);
    } else if (role == Role.Team_2) {
        handleTeamMovement(Role.Team_2, BallX, BallY);
    }
}*/
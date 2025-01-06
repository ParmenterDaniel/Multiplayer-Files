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

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.net.*;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.ui.UI;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
//
import java.util.List;
import java.util.ArrayList;


import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGL.getPhysicsWorld;
import static com.almasb.fxglgames.pong.NetworkMessages.*;

/**
 * A simple clone of Pong.
 * Sounds from https://freesound.org/people/NoiseCollector/sounds/4391/ under CC BY 3.0.
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class PongApp extends GameApplication implements MessageHandler<String> {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Striker");
        settings.setVersion("1.1");
        settings.setFontUI("pong.ttf");
        settings.setApplicationMode(ApplicationMode.DEBUG);
        settings.setWidth(1280);
        settings.setHeight(720);
    }

    private Entity player1;
    private Entity player2;
    private Entity ball;
    private BatComponent player1Bat;
    private BatComponent player2Bat;
    private BallComponent ballComp;
    private Entity goalLine1;
    private Entity goalLine2;
    private Entity goalNet1;
    private Entity goalNet2;
    private Entity topUiContainer;
    private Entity aiTeam1;
    private Entity aiTeam2;
    private AIComponent aiTeam1Comp;
    private AIComponent aiTeam2Comp;

    // Lobby Handling
    private boolean allowInput = true;
    private boolean gameStarted = false;
    private int connectedPlayers;
    private boolean gameEnded = false;

    private Server<String> server;

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Up1") {
            @Override
            protected void onAction() {
                if (!allowInput) return;
                player1Bat.up();
            }

            @Override
            protected void onActionEnd() {
                if (!allowInput) return;
                player1Bat.stop();
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Down1") {
            @Override
            protected void onAction() {
                if (!allowInput) return;
                player1Bat.down();
            }

            @Override
            protected void onActionEnd() {
                if (!allowInput) return;
                player1Bat.stop();
            }
        }, KeyCode.S);

        getInput().addAction(new UserAction("Left1") {
            @Override
            protected void onAction() {
                if (!allowInput) return;
                player1Bat.left();
            }

            @Override
            protected void onActionEnd() {
                if (!allowInput) return;
                player1Bat.stop();
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("Right1") {
            @Override
            protected void onAction() {
                if (!allowInput) return;
                player1Bat.right();
            }

            @Override
            protected void onActionEnd() {
                if (!allowInput) return;
                player1Bat.stop();
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("Up2") {
            @Override
            protected void onAction() {
                if (!allowInput) return;
                player2Bat.up();
            }

            @Override
            protected void onActionEnd() {
                if (!allowInput) return;
                player2Bat.stop();
            }
        }, KeyCode.I);

        getInput().addAction(new UserAction("Down2") {
            @Override
            protected void onAction() {
                if (!allowInput) return;
                player2Bat.down();
            }

            @Override
            protected void onActionEnd() {
                if (!allowInput) return;
                player2Bat.stop();
            }
        }, KeyCode.K);

        getInput().addAction(new UserAction("left2") {
            @Override
            protected void onAction() {
                if (!allowInput) return;
                player2Bat.left();
            }

            @Override
            protected void onActionEnd() {
                if (!allowInput) return;
                player2Bat.stop();
            }
        }, KeyCode.J);

        getInput().addAction(new UserAction("right2") {
            @Override
            protected void onAction() {
                if (!allowInput) return;
                player2Bat.right();
            }

            @Override
            protected void onActionEnd() {
                if (!allowInput) return;
                player2Bat.stop();
            }
        }, KeyCode.L);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("player1score", 0);
        vars.put("player2score", 0);
        vars.put("connectedClients",0);
    }
// Original pre lobby screen
  /* @Override
   protected void initGame() {
        Writers.INSTANCE.addTCPWriter(String.class, outputStream -> new MessageWriterS(outputStream));
        Readers.INSTANCE.addTCPReader(String.class, in -> new MessageReaderS(in));

        server = getNetService().newTCPServer(55555, new ServerConfig<>(String.class));

        server.setOnConnected(connection -> {
            connection.addMessageHandlerFX(this);
        });

        getGameWorld().addEntityFactory(new PongFactory());
        getGameScene().setBackgroundColor(Color.rgb(50, 50, 50));

        initScreenBounds();
        initGameObjects();

        var t = new Thread(server.startTask()::run);
        t.setDaemon(true);
        t.start();
    }*/

//////////////////////////////////////

    @Override
    protected void initGame() {
        // Initialize networking and message handlers
        Writers.INSTANCE.addTCPWriter(String.class, outputStream -> new MessageWriterS(outputStream));
        Readers.INSTANCE.addTCPReader(String.class, in -> new MessageReaderS(in));

        // Create and configure the server
        server = getNetService().newTCPServer(55555, new ServerConfig<>(String.class));

        // Set up connection handler
        server.setOnConnected(connection -> {
            if (connectedPlayers >= 2) {
                System.out.println("Connection attempt rejected: Maximum players reached.");
                connection.terminate();
            } else if(gameEnded) {
                System.out.println("Connection attempt rejected: Server Closed.");
                connection.terminate();
            } else {
                connection.addMessageHandlerFX(this);
                // Increment the connected client count
                connectedPlayers++;


                // Log the number of clients connected
                System.out.println("Client connected. Total clients: " + "," + connectedPlayers);
                connection.send("PLAYER_JOIN" + connectedPlayers);

                connection.getLocalSessionData().setValue("HeartBeatTime", System.currentTimeMillis() + 2000);
                connection.getLocalSessionData().setValue("ID", connectedPlayers);
                connection.getLocalSessionData().setValue("Active", true);

                // Once two clients are connected, set gameStarted to true and start the game
                if (connectedPlayers >= 2 && !gameStarted) {
                    //connection.addMessageHandlerFX(this);
                    gameStarted = true;  // Mark game as started
                    System.out.println("Game Started");
                }
            }
        });


        // Handle Disconnects //////////////////////////////////////////////////////////////////////
        server.setOnDisconnected(connection -> {
            System.out.println("CLIENT DISCONNECT");
        });
        //////////////////////////////////////////////////////////////////////////////////////////////

        // Set up the game scene (but don’t initialize game objects yet)
        getGameScene().setBackgroundColor(Color.rgb(50, 50, 50));

        // Optionally log or notify that the game hasn't started yet
        if (!gameStarted) {
            System.out.println("Waiting for players to connect...");
        }

        getGameWorld().addEntityFactory(new PongFactory());
        initScreenBounds();
        initGameObjects();

        // Start the server thread after starting the game
        var t = new Thread(server.startTask()::run);
        t.setDaemon(true);
        t.start();
    }

    ////////////////////////////////////////////////////////////////////
    @Override
    protected void initPhysics() {
        getPhysicsWorld().setGravity(0, 0);

        CollisionHandler ballBatHandler = new CollisionHandler(EntityType.BALL, EntityType.PLAYER_BAT) {
            @Override
            protected void onCollisionBegin(Entity a, Entity bat) {
                boolean moveRight;
                playHitAnimation(bat);

                server.broadcast(bat == player1 ? BALL_HIT_BAT1 : BALL_HIT_BAT2);
                server.broadcast("AUDIO, 2");
                //System.out.println(bat == player1 || bat == aiTeam1 ? "Hit team 1" : "bauer");

                // Ball needs to go right if team 1 hit it, left if team 2 hit it.
                moveRight = bat == player1 || bat == aiTeam1;

                double batTopY = bat.getY();

                double ballY = ball.getBottomY() - (ball.getHeight() / 2);

                double upperQuarter = batTopY + 9;
                double bottomQuarter = batTopY + 27;

                if (ballY < upperQuarter){
                    ballComp.setVelocity(-2, moveRight, bat == player1 ? player1Bat.getXVel() : player2Bat.getXVel());
                } else if (ballY > bottomQuarter){
                    ballComp.setVelocity(2, moveRight, bat == player1 ? player1Bat.getXVel() : player2Bat.getXVel());
                } else {
                    ballComp.setVelocity(0, moveRight, bat == player1 ? player1Bat.getXVel() : player2Bat.getXVel());
                }

            }
        };

        CollisionHandler AiBallHandler = new CollisionHandler(EntityType.BALL, EntityType.AI_PLAYER_2) {};
        getPhysicsWorld().addCollisionHandler(AiBallHandler);

        // goalLine Collisions
        CollisionHandler goalLineHandler = new CollisionHandler(EntityType.BALL, EntityType.GOAL_LINE){
            @Override
            protected void onCollisionBegin(Entity a, Entity goalLine) {
                //server.broadcast(goalLine == goalLine1 ? "true": "false");
                inc(goalLine == goalLine1 ? "player2score": "player1score", +1);
                server.broadcast("SCORES," + geti("player1score") + "," + geti("player2score"));
                //ballComp.respawn = true;
                resetObjects();
                if (geti("player1score") == 2 || geti("player2score") == 2) {
                    endGameState();
                }
            }

        };
        getPhysicsWorld().addCollisionHandler(goalLineHandler);

        getPhysicsWorld().addCollisionHandler(ballBatHandler);
        getPhysicsWorld().addCollisionHandler(ballBatHandler.copyFor(EntityType.BALL, EntityType.ENEMY_BAT));
        getPhysicsWorld().addCollisionHandler(ballBatHandler.copyFor(EntityType.BALL, EntityType.AI_PLAYER_1));
    }

    @Override
    protected void initUI() {
        MainUIController controller = new MainUIController();
        UI ui = getAssetLoader().loadUI("main.fxml", controller);

        /*var brickTexture = getAssetLoader().loadTexture("Untitled.jpg");
        brickTexture.setTranslateY(80);

        getGameScene().addUINode(brickTexture);*/

        controller.getLabelScorePlayer().textProperty().bind(getip("player1score").asString());
        controller.getLabelScoreEnemy().textProperty().bind(getip("player2score").asString());

        getGameScene().addUI(ui);
    }


    @Override
    protected void onUpdate(double tpf) {
        if (!server.getConnections().isEmpty()) {
            if (gameEnded) {
                return; // Skip updates if the game has ended
            }
            // Check if both players are initialized
            if (gameStarted) {
                // Ensure the game data message is constructed only if both players exist
                var message = "GAME_DATA," + player1.getY() + "," + player2.getY() + "," + ball.getX() + "," + ball.getY() + "," + player1.getX() + "," + player2.getX() + "," + aiTeam1.getX() + "," + aiTeam1.getY() + "," + aiTeam2.getX() + "," + aiTeam2.getY();

                // Print the message to the server console
                System.out.println("Server Message: " + message);

                // Broadcast the game data to all clients
                server.broadcast(message);
            }


            // Heartbeat Handling
            for(Connection connection: server.getConnections())
            {
                if (connection.getLocalSessionData().getValue("Active")) {
                    long lastHeartBeatTime = connection.getLocalSessionData().getValue("HeartBeatTime");

                    if (System.currentTimeMillis() > lastHeartBeatTime + 3000) {
                        int clientID = connection.getLocalSessionData().getValue("ID");
                        connection.terminate();
                        gameEnded = true;
                        System.out.println("Client " + clientID + " disconnected!");
                        connection.getLocalSessionData().setValue("Active", false);
                        gameOverDisconnect(clientID);
                    }
                }

            }
        }
    }

    /*
    @Override
    protected void onUpdate(double tpf) {
        if (!server.getConnections().isEmpty())
        {
            if(!gameStarted){
                processLobbyInformation();
                return;
            }

            var message = "GAME_DATA," + player1.getY() + "," + player2.getY() + "," + ball.getX() + "," + ball.getY() + "," + player1.getX() + "," + player2.getX() + "," + aiTeam1.getX() + "," + aiTeam1.getY() + "," + aiTeam2.getX() + "," + aiTeam2.getY();

            // Print the message to the server console
            System.out.println("Server Message: " + message);

            // Broadcast the game data to all clients
            server.broadcast(message);

            //TODO replace with heartbeat function
            //For all client connections (active and inactive)
            for(Connection connection: server.getConnections())
            {
                //Checks
                if(!connection.isConnected())
                    continue;

                if(!(boolean)connection.getLocalSessionData().getValue("Connected"))
                    continue;

                //Setup
                long lastHeartBeatTime = connection.getLocalSessionData().getValue("HeartBeatTime");

                //Check last Signal Time from client
                if(System.currentTimeMillis() > lastHeartBeatTime + 3000){
                    //connection.terminate();
                    int connectionID = connection.getLocalSessionData().getValue("ID");
                    System.out.println("Client " + connectionID + " disconnected!");
                    connection.getLocalSessionData().setValue("Connected", false);
                    inc("connectedClients", -1);

                    //If not a spectator, make linked player disconnect
                    if(connectionID != -1)
                        connectedPlayers.get(connectionID).getComponent(BatComponent.class).connected = false;
                }
            }

            //Add spectators to game if one of the main clients disconnect
            for(int i = 0; i < connectedPlayers.size(); i++)
            {
                if(!connectedPlayers.get(i).getComponent(BatComponent.class).connected)
                {
                    for(Connection connection : server.getConnections())
                    {
                        int id = connection.getLocalSessionData().getValue("ID");
                        if((boolean)connection.getLocalSessionData().getValue("Connected")  && id == -1)
                        {
                            connection.getLocalSessionData().setValue("ID", i);
                            connection.send("ID," + i);
                            connectedPlayers.get(i).getComponent(BatComponent.class).connected = true;
                        }
                    }
                }
            }
        }

        if(server.getConnections().size() < 2){
            //add AI?
            //TODO
        }
    }
    */

    private void resetObjects() {
        FXGL.runOnce(() -> {
            ballComp.respawn = true;
            player1Bat.resetPosition(1);
            player2Bat.resetPosition(2);
            aiTeam1Comp.resetPosition(1);
            aiTeam2Comp.resetPosition(2);
        }, Duration.ZERO);
    }

    private void endGameState() {
        gameEnded = true;
        allowInput = false;

        new ArrayList<>(getGameWorld().getEntities()).forEach(Entity::removeFromWorld);
        getGameTimer().clear();
        getPhysicsWorld().clearCollisionHandlers();

        getInput().clearAll();

        getPhysicsWorld().clearCollisionHandlers();
        System.out.println(geti("player1score") == 1 ? "Player 1 wins" : (geti("player2score") == 1 ? "Player 2 wins" : ""));
        var result = geti("player1score") == 1 ? "1" : (geti("player2score") == 1 ? "2" : "");
        server.broadcast("GAME_ENDED," + result);

        getInput().addAction(new UserAction("CloseServer") {
            @Override
            protected void onAction() {
                FXGL.getGameController().exit();
            }
        }, KeyCode.BACK_SPACE);
    }

    private void initScreenBounds() {
        Entity walls = entityBuilder()
                .type(EntityType.WALL)
                .collidable()
                .buildScreenBounds(150);

        getGameWorld().addEntity(walls);
    }

    private void initGameObjects() {
        ball = spawn("ball", getAppWidth() / 2, getAppHeight() / 2 - 5 + 40);
        player1 = spawn("bat", new SpawnData(getAppWidth() / 4, getAppHeight() / 2 + 20).put("isPlayer", true));
        player2 = spawn("bat", new SpawnData(3 * getAppWidth() / 4 - 20, getAppHeight() / 2 + 20).put("isPlayer", false));
        goalLine1 = spawn("goalLine", 50,(getAppHeight()-80 )/2 + 40);
        goalLine2 = spawn("goalLine", getAppWidth() - 50 - 5,(getAppHeight()-80 )/2 + 40);
        goalNet1 = spawn("goalNet",  10,(getAppHeight()-80 )/2 + 40);
        goalNet2 = spawn("goalNet",  getAppWidth() - 50,(getAppHeight()-80 )/2 + 40);
        topUiContainer = spawn("topUiContainer", 0, 78);
        aiTeam1 = spawn("aiPlayer", new SpawnData(getAppWidth() / 2 - 100, 180).put("isTeamA", true));
        aiTeam2 = spawn("aiPlayer", new SpawnData(getAppWidth() / 2 + 100, 580).put("isTeamA", false));

        player1Bat = player1.getComponent(BatComponent.class);
        player2Bat = player2.getComponent(BatComponent.class);
        ballComp = ball.getComponent(BallComponent.class); 
        aiTeam1Comp = aiTeam1.getComponent(AIComponent.class);
        aiTeam2Comp = aiTeam2.getComponent(AIComponent.class);
    }

    private void playHitAnimation(Entity bat) {
        animationBuilder()
                .autoReverse(true)
                .duration(Duration.seconds(0.5))
                .interpolator(Interpolators.BOUNCE.EASE_OUT())
                .rotate(bat)
                .from(FXGLMath.random(-25, 25))
                .to(0)
                .buildAndPlay();
    }

    /////////////////////////////////////////////////////////////////////////////

    private void processLobbyInformation()
    {
        //checkHeartBeats();
        //swapPlayer1();
        //spectatorsToPlayers();
    }

    /*
    private void spectatorsToPlayers()
    {
        for(int i = 0; i < connectedPlayers.size(); i++)
        {
            if(!connectedPlayers.get(i).getComponent(BatComponent.class).connected)
            {
                for(Connection connection : server.getConnections())
                {
                    int id = connection.getLocalSessionData().getValue("ID");
                    if((boolean)connection.getLocalSessionData().getValue("Connected")  && id == -1)
                    {
                        connection.getLocalSessionData().setValue("ID", i);
                        connection.send("ID," + i);
                        connectedPlayers.get(i).getComponent(BatComponent.class).connected = true;
                    }
                }
            }
        }
    }*/

/*
    private void checkHeartBeats()
    {
        if (server.getConnections().isEmpty())
            return;

        //For all client connections (active and inactive)
        for(Connection connection: server.getConnections())
        {
            //Checks
            if(!connection.isConnected())
                continue;

            if(!(boolean)connection.getLocalSessionData().getValue("Connected"))
                continue;

            //Setup
            long lastHeartBeatTime = connection.getLocalSessionData().getValue("HeartBeatTime");

            //Check last Signal Time from client
            if(System.currentTimeMillis() > lastHeartBeatTime + 4000){
                //connection.terminate();
                int connectionID = connection.getLocalSessionData().getValue("ID");
                System.out.println("Client " + connectionID + " disconnected!");
                connection.getLocalSessionData().setValue("Connected", false);
                inc("connectedClients", -1);
                server.broadcast("CONNECTEVENT," + geti("connectedClients"));

                //If not a spectator, make linked player disconnect
                if(connectionID != -1)
                    connectedPlayers.get(connectionID).getComponent(BatComponent.class).connected = false;

            }
        }
    }
 */

    private void gameOverDisconnect(int loserClientID){
        for (Connection connection : server.getConnections()) {
            int clientId = connection.getLocalSessionData().getValue("ID");

            if (clientId != loserClientID) {
                // Send the message to the client with the matching ID
                connection.send("OPPONENT_DISCONNECT");
                System.out.println("Message sent to client " + clientId);
                break; // Exit the loop once the message has been sent
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////

    @Override
    public void onReceive(Connection<String> connection, String message) {
        var tokens = message.split(",");

        // Check if the message starts with "HEARTBEAT"
        if (tokens.length > 1 && tokens[1].trim().equals("HEARTBEAT")) {
            System.out.println("Heartbeat received");
            connection.getLocalSessionData().setValue("HeartBeatTime", System.currentTimeMillis());
            var timeofbeat = connection.getLocalSessionData().getValue("HeartBeatTime");
            System.out.println(timeofbeat);
        }

        if (gameStarted && !gameEnded) {
            //var tokens = message.split(",");

            Arrays.stream(tokens).skip(1).forEach(key -> {
                if (key.endsWith("_DOWN")) {
                    getInput().mockKeyPress(KeyCode.valueOf(key.substring(0, 1)));
                } else if (key.endsWith("_UP")) {
                    getInput().mockKeyRelease(KeyCode.valueOf(key.substring(0, 1)));
                }
            });
        }
    }

    static class MessageWriterS implements TCPMessageWriter<String> {

        private OutputStream os;
        private PrintWriter out;

        MessageWriterS(OutputStream os) {
            this.os = os;
            out = new PrintWriter(os, true);
        }

        @Override
        public void write(String s) throws Exception {
            out.print(s.toCharArray());
            out.flush();
        }
    }

    static class MessageReaderS implements TCPMessageReader<String> {

        private BlockingQueue<String> messages = new ArrayBlockingQueue<>(50);

        private InputStreamReader in;

        MessageReaderS(InputStream is) {
            in =  new InputStreamReader(is);

            var t = new Thread(() -> {
                try {

                    char[] buf = new char[36];

                    int len;

                    while ((len = in.read(buf)) > 0) {
                        var message = new String(Arrays.copyOf(buf, len));

                        System.out.println("Recv message: " + message);

                        messages.put(message);
                    }

                }catch (SocketException e) {
                    // Handle socket reset or disconnect case
                    System.err.println("SocketException: Connection reset or client disconnected: " + e.getMessage());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            t.setDaemon(true);
            t.start();
        }

        @Override
        public String read() throws Exception {
            return messages.take();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

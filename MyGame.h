#ifndef __MY_GAME_H__
#define __MY_GAME_H__

#include <iostream>
#include <vector>
#include <string>

#include "SDL.h"
#include "SDL_ttf.h"
#include "SDL_image.h"
#include <SDL_mixer.h>

static struct GameData {
    int player1Y = 0;
    int player2Y = 0;
    int aiTeam1X = 0;
    int aiTeam1Y = 0;
    int aiTeam2X = 0;
    int aiTeam2Y = 0;
    int ballX = 0;
    int ballY = 0;
    int player1X = 0;
    int player2X = 0;
    int team1Score = 0;
    int team2Score = 0;
    int player = 0;
} game_data;

class Player {
private:
    SDL_Rect rect; // Player's position and size
    SDL_Texture* texture;   // Player's texture (sprite)
    bool isPlayerControlled;  // Set to true for this client's player only

    // Movement
    float Speed = 240;
    float xVelocity;
    float yVelocity;

public:
    ///////////////////////////////////////////////////////////////////
    // Constructor to initialize the player
    Player(int x, int y, int width, int height)
        : rect({ x, y, width, height }), texture(nullptr), isPlayerControlled(false), xVelocity(0.0f), yVelocity(0.0f) {
    }

    // Destructor to clean up the texture
    ~Player() {
        if (texture) {
            SDL_DestroyTexture(texture);
        }
    }

    // Load texture for the player
    void loadTexture(SDL_Renderer* renderer, const char* filePath) {
        SDL_Surface* surface = IMG_Load(filePath);
        if (!surface) {
            SDL_Log("Failed to load player image: %s, %s", filePath, IMG_GetError());
            return;
        }
        texture = SDL_CreateTextureFromSurface(renderer, surface);
        SDL_FreeSurface(surface);
        if (!texture) {
            SDL_Log("Failed to create texture: %s", SDL_GetError());
        }
    }

    // Render the player
    void render(SDL_Renderer* renderer) {
        if (texture) {
            SDL_RenderCopy(renderer, texture, nullptr, &rect);
        }
        else {
            // Optionally render a placeholder if no texture is loaded
            SDL_SetRenderDrawColor(renderer, 255, 0, 0, 255); // Red
            SDL_RenderFillRect(renderer, &rect);
        }
    }
    ////////////////////////////////////////////////////////////////

    // Handle setting player 1 or player 2 ////////////////////////////////

    // Set whether this is the player being controlled
    void setPlayerControlled(bool isControlled) {
        isPlayerControlled = isControlled;
    }

    // Check if this player is controlled by the client
    bool isControlled() const {
        return isPlayerControlled;
    }

    //////////////////////////////////////////////////////////////////////

    // Handle movement client side - for client-side prediction ///////////

    void up() {
        yVelocity = -Speed;
    }

    void down() {
        yVelocity = Speed;
    }

    void left() {
        xVelocity = -Speed;
    }

    void right() {
        xVelocity = Speed;
    }

    void stopY() {
        yVelocity = 0.0f;
    }

    void stopX() {
        xVelocity = 0.0f;
    }

    void update(float deltaTime) {
        rect.x += static_cast<int>(xVelocity * deltaTime);
        rect.y += static_cast<int>(yVelocity * deltaTime);
    }

    //////////////////////////////////////////////////////////////////////

    // Accessor for the SDL_Rect
    SDL_Rect getRect() const {
        return rect;
    }

    // For now, this just updates the SDL_Rect position (could be extended later)
    void setPosition(int x, int y) {
        rect.x = x;
        rect.y = y;
    }
};

class AIPlayer {
private:
    SDL_Rect rect; // Player's position and size
    SDL_Texture* texture;   // Player's texture (sprite)

    // Movement
    float Speed = 240;
    float xVelocity;
    float yVelocity;

public:
    ///////////////////////////////////////////////////////////////////
    // Constructor to initialize the player
    AIPlayer(int x, int y, int width, int height)
        : rect({ x, y, width, height }), texture(nullptr), xVelocity(0.0f), yVelocity(0.0f) {
    }

    // Destructor to clean up the texture
    ~AIPlayer() {
        if (texture) {
            SDL_DestroyTexture(texture);
        }
    }

    // Load texture for the player
    void loadTexture(SDL_Renderer* renderer, const char* filePath) {
        SDL_Surface* surface = IMG_Load(filePath);
        if (!surface) {
            SDL_Log("Failed to load AI Player image: %s, %s", filePath, IMG_GetError());
            return;
        }
        texture = SDL_CreateTextureFromSurface(renderer, surface);
        SDL_FreeSurface(surface);
        if (!texture) {
            SDL_Log("Failed to create texture: %s", SDL_GetError());
        }
    }

    // Render the player
    void render(SDL_Renderer* renderer) {
        if (texture) {
            SDL_RenderCopy(renderer, texture, nullptr, &rect);
        }
        else {
            // Optionally render a placeholder if no texture is loaded
            SDL_SetRenderDrawColor(renderer, 255, 0, 0, 255); // Red
            SDL_RenderFillRect(renderer, &rect);
        }
    }
    ////////////////////////////////////////////////////////////////

    // Accessor for the SDL_Rect
    SDL_Rect getRect() const {
        return rect;
    }

    // For now, this just updates the SDL_Rect position (could be extended later)
    void setPosition(int x, int y) {
        rect.x = x;
        rect.y = y;
    }
};

class Ball {
private:
    SDL_Rect rect; // Player's position and size
    SDL_Texture* texture;   // Player's texture (sprite)

    // Movement
    float Speed = 240;
    float xVelocity;
    float yVelocity;

public:
    ///////////////////////////////////////////////////////////////////
    // Constructor to initialize the player
    Ball(int x, int y, int width, int height)
        : rect({ x, y, width, height }), texture(nullptr), xVelocity(0.0f), yVelocity(0.0f) {
    }

    // Destructor to clean up the texture
    ~Ball() {
        if (texture) {
            SDL_DestroyTexture(texture);
        }
    }

    // Load texture for the player
    void loadTexture(SDL_Renderer* renderer, const char* filePath) {
        SDL_Surface* surface = IMG_Load(filePath);
        if (!surface) {
            SDL_Log("Failed to load AI Player image: %s, %s", filePath, IMG_GetError());
            return;
        }
        texture = SDL_CreateTextureFromSurface(renderer, surface);
        SDL_FreeSurface(surface);
        if (!texture) {
            SDL_Log("Failed to create texture: %s", SDL_GetError());
        }
    }

    // Render the player
    void render(SDL_Renderer* renderer) {
        if (texture) {
            SDL_RenderCopy(renderer, texture, nullptr, &rect);
        }
        else {
            // Optionally render a placeholder if no texture is loaded
            SDL_SetRenderDrawColor(renderer, 255, 0, 0, 255); // Red
            SDL_RenderFillRect(renderer, &rect);
        }
    }
    ////////////////////////////////////////////////////////////////

    // Accessor for the SDL_Rect
    SDL_Rect getRect() const {
        return rect;
    }

    // For now, this just updates the SDL_Rect position (could be extended later)
    void setPosition(int x, int y) {
        rect.x = x;
        rect.y = y;
    }
};

class Goal {
private:
    SDL_Rect rect; // Player's position and size
    SDL_Texture* texture;   // Player's texture (sprite)

public:
    ///////////////////////////////////////////////////////////////////
    // Constructor to initialize the player
    Goal(int x, int y, int width, int height)
        : rect({ x, y, width, height }), texture(nullptr) {
    }

    // Destructor to clean up the texture
    ~Goal() {
        if (texture) {
            SDL_DestroyTexture(texture);
        }
    }

    // Load texture for the player
    void loadTexture(SDL_Renderer* renderer, const char* filePath) {
        SDL_Surface* surface = IMG_Load(filePath);
        if (!surface) {
            SDL_Log("Failed to load Goal image: %s, %s", filePath, IMG_GetError());
            return;
        }
        texture = SDL_CreateTextureFromSurface(renderer, surface);
        SDL_FreeSurface(surface);
        if (!texture) {
            SDL_Log("Failed to create texture: %s", SDL_GetError());
        }
    }

    // Render the player
    void render(SDL_Renderer* renderer) {
        if (texture) {
            SDL_RenderCopy(renderer, texture, nullptr, &rect);
        }
        else {
            // Render a placeholder if no texture is loaded
            SDL_SetRenderDrawColor(renderer, 255, 255, 255, 255); // white
            SDL_RenderFillRect(renderer, &rect);
        }
    }
    ////////////////////////////////////////////////////////////////

    // Accessor for the SDL_Rect
    SDL_Rect getRect() const {
        return rect;
    }
};

class MyGame {

    private:
        //SDL_Rect player1 = { 20, 0, 10, 36 };
        Player player1{ 320, 550, 10, 36 };
        //SDL_Rect aiTeam1 = { 80, 400, 10 ,36 };
        AIPlayer aiTeam1{ 80, 400, 10 ,36 };
        //SDL_Rect player2 = { 600, 0, 10, 36 };
        Player player2{ 940, 550, 10, 36 };
        //SDL_Rect aiTeam2 = { 800, 400, 10 ,36 };
        AIPlayer aiTeam2{ 800, 400, 10 ,36 };
        //SDL_Rect ball = { 400, 0, 10, 10 };
        Ball ball = { 400, 360, 10, 10 };
        //SDL_Rect goalLine1{ 42, 360, 36, 80 };
        Goal goalLine1{ 14, 360, 36, 80 };
        //SDL_Rect goalLine2{ 1225, 360, 36, 80 };
        Goal goalLine2{ 1225, 360, 36, 80 };
        int team1Score, team2Score = 0;
        bool gameOver = false;
        bool serverLost = false;
        bool opponentDisconnect = false;
        bool isWinner;
        std::string winner;

        SDL_Texture* backgroundTexture = nullptr;
        SDL_Rect backgroundRect;

        SDL_Texture* scoreboardTexture = nullptr;
        SDL_Rect scoreboardRect;

        SDL_Texture* indicatorTexture = nullptr;
        SDL_Rect indicatorRect;

        Mix_Chunk* goalSound;
        Mix_Chunk* kickSound;

    public:
        std::vector<std::string> messages;
        std::vector<std::string> heartbeatMessages;
        Uint32 nextSendTime = SDL_GetTicks();

        void on_receive(std::string message, std::vector<std::string>& args);
        void send(std::string message);
        void input(SDL_Event& event);
        void update();
        void render(SDL_Renderer* renderer);
        void renderText(SDL_Renderer* renderer, TTF_Font* font, const std::string& text,
            int x, int y, SDL_Color color);

        // Image handling
        // Function declaration to load the background texture
        void loadBackgroundTexture(SDL_Renderer* renderer, const char* filePath);
        void loadScoreboardContainer(SDL_Renderer* renderer, const char* filePath);
        void loadIndicatorContainer(SDL_Renderer* renderer, const char* filePath);

        // Function declaration to render the background texture
        void renderBackground(SDL_Renderer* renderer);
        void renderScoreboard(SDL_Renderer* renderer);
        void renderIndicator(SDL_Renderer* renderer);

        // Function to cleanup background texture
        void cleanupBackground();

        ///////////////////////////////////////////////////////////////////////////
        void loadPlayerTextures(SDL_Renderer* renderer);
        ///////////////////////////////////////////////////////////////////////////

        // Handle Connection loss on server close//////////////////////////////////
        void handleServerClose(SDL_Renderer* renderer);
        //////////////////////////////////////////////////////////////////////////

        // Handle heartbeats /////////////////////////////////////////////////////
        void prepareHeartbeat();
        void sendHeartbeat(std::string message);
        //////////////////////////////////////////////////////////////////////////

        // Audio Handling ////////////////////////////////////////////////////////
        void loadAudio();
        void playSoundEffect(const std::string& soundName);
        //////////////////////////////////////////////////////////////////////////
};





#endif
#ifndef __MY_GAME_H__
#define __MY_GAME_H__

#include <iostream>
#include <vector>
#include <string>

#include "SDL.h"
#include "SDL_ttf.h"
#include "SDL_image.h"

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

class MyGame {

    private:
        SDL_Rect player1 = { 20, 0, 10, 36 };
        SDL_Rect aiTeam1 = { 80, 400, 10 ,36 };
        SDL_Rect player2 = { 600, 0, 10, 36 };
        SDL_Rect aiTeam2 = { 800, 400, 10 ,36 };
        SDL_Rect ball = { 400, 0, 10, 10 };
        SDL_Rect goalLine1{ 42, 360, 8, 80 };
        SDL_Rect goalLine2{ 1225, 360, 5, 80 };
        int team1Score, team2Score = 0;

        SDL_Texture* backgroundTexture = nullptr;
        SDL_Rect backgroundRect;

    public:
        std::vector<std::string> messages;

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

        // Function declaration to render the background texture
        void renderBackground(SDL_Renderer* renderer);

        // Function to cleanup background texture
        void cleanupBackground();
};

#endif
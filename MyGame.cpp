#include "MyGame.h"

void MyGame::on_receive(std::string cmd, std::vector<std::string>& args) {
    if (cmd == "GAME_DATA") {
        if (args.size() == 10) {
            game_data.player1Y = stoi(args.at(0));
            game_data.player1X = stoi(args.at(4));
            game_data.player2Y = stoi(args.at(1));
            game_data.player2X = stoi(args.at(5));
            game_data.ballX = stoi(args.at(2));
            game_data.ballY = stoi(args.at(3));
            game_data.aiTeam1X = stoi(args.at(6));
            game_data.aiTeam1Y = stoi(args.at(7));
            game_data.aiTeam2X = stoi(args.at(8));
            game_data.aiTeam2Y = stoi(args.at(9));
        }
    }
    else if (cmd == "SCORES") {
        std::cout << "Player1 Score = " << stoi(args.at(0)) << "    " << "Player2 Score = " << stoi(args.at(1));
        if (args.size() == 2) {
            game_data.team1Score = stoi(args.at(0));
        }
    }
    else {
        std::cout << "Received: " << cmd << std::endl;
    }
}

void MyGame::send(std::string message) {
    messages.push_back(message);
}

void MyGame::input(SDL_Event& event) {
    switch (event.key.keysym.sym) {
        case SDLK_w:
            send(event.type == SDL_KEYDOWN ? "W_DOWN" : "W_UP");
            break;
        case SDLK_s:
            send(event.type == SDL_KEYDOWN ? "S_DOWN" : "S_UP");
            break;
        case SDLK_a:
            send(event.type == SDL_KEYDOWN ? "A_DOWN" : "A_UP");
            break;
        case SDLK_d:
            send(event.type == SDL_KEYDOWN ? "D_DOWN" : "D_UP");
            break;
        case SDLK_i:
            send(event.type == SDL_KEYDOWN ? "I_DOWN" : "I_UP");
            break;
        case SDLK_k: 
            send(event.type == SDL_KEYDOWN ? "K_DOWN" : "K_UP");
            break;
        case SDLK_j:
            send(event.type == SDL_KEYDOWN ? "J_DOWN" : "J_UP");
            break;
        case SDLK_l:
            send(event.type == SDL_KEYDOWN ? "L_DOWN" : "L_UP");
            break;
    }
}

void MyGame::update() {
    player1.y = game_data.player1Y;
    player1.x = game_data.player1X;
    aiTeam1.x = game_data.aiTeam1X;
    aiTeam1.y = game_data.aiTeam1Y;
    player2.y = game_data.player2Y;
    player2.x = game_data.player2X;
    aiTeam2.x = game_data.aiTeam2X;
    aiTeam2.y = game_data.aiTeam2Y;
    ball.x = game_data.ballX;
    ball.y = game_data.ballY;
    team1Score = game_data.team1Score;
    team2Score = game_data.team2Score;
}

// Text Function
void MyGame::renderText(SDL_Renderer* renderer, TTF_Font* font, const std::string& text,
    int x, int y, SDL_Color color) {
    SDL_Surface* surface = TTF_RenderText_Solid(font, text.c_str(), color);
    if (!surface) {
        std::cerr << "Failed to create surface: " << TTF_GetError() << std::endl;
        return;
    }

    SDL_Texture* texture = SDL_CreateTextureFromSurface(renderer, surface);
    if (!texture) {
        std::cerr << "Failed to create texture: " << SDL_GetError() << std::endl;
        SDL_FreeSurface(surface);
        return;
    }

    SDL_Rect destRect = { x, y, surface->w, surface->h };
    SDL_FreeSurface(surface);

    SDL_RenderCopy(renderer, texture, nullptr, &destRect);
    SDL_DestroyTexture(texture);
}

void MyGame::render(SDL_Renderer* renderer) {
    // Render Images
    renderBackground(renderer);

    // Team 1 White
    SDL_SetRenderDrawColor(renderer, 255, 255, 255, 255);
    SDL_RenderFillRect(renderer, &player1);
    SDL_RenderFillRect(renderer, &aiTeam1);

    // Team 2 Grey
    SDL_SetRenderDrawColor(renderer, 50, 255, 255, 255);
    SDL_RenderFillRect(renderer, &player2);
    SDL_RenderFillRect(renderer, &aiTeam2);

    // Ball Blue
    SDL_SetRenderDrawColor(renderer, 0, 0, 255, 50);
    SDL_RenderFillRect(renderer, &ball);

    // Other Objects 
    SDL_SetRenderDrawColor(renderer, 200, 200, 200, 255);
    SDL_RenderFillRect(renderer, &goalLine1);
    SDL_RenderFillRect(renderer, &goalLine2);

    // Text Handling
    // Set up font and score text
    TTF_Font* font = TTF_OpenFont("assets/fonts/Arial.ttf", 24);
    if (!font) {
        std::cerr << "Failed to load font: " << TTF_GetError() << std::endl;
        return;
    }

    SDL_Color white = { 255, 255, 255, 255 }; // White color
    std::string scoreText = "Team 1 " + std::to_string(team1Score) + " - " + std::to_string(team2Score) + " Team 2";

    // Render the score at position (10, 10)
    renderText(renderer, font, scoreText, 50, 30, white);

    TTF_CloseFont(font);
}

// Image Handling
// Function to load the background texture
void MyGame::loadBackgroundTexture(SDL_Renderer* renderer, const char* filePath) {
    // Load image as a texture
    backgroundTexture = IMG_LoadTexture(renderer, filePath);
    if (backgroundTexture == nullptr) {
        printf("Failed to load background texture: %s\n", SDL_GetError());
        return;
    }

    // Get the texture width and height to set the destination rect
    int width, height;
    SDL_QueryTexture(backgroundTexture, NULL, NULL, &width, &height);
    backgroundRect = { 50, 80, width, height };  // Set rect to the size of the texture
}

// Function to render the background texture
void MyGame::renderBackground(SDL_Renderer* renderer) {
    if (backgroundTexture != nullptr) {
        SDL_RenderCopy(renderer, backgroundTexture, NULL, &backgroundRect);
    }
}

// Function to cleanup background texture when done
void MyGame::cleanupBackground() {
    if (backgroundTexture != nullptr) {
        SDL_DestroyTexture(backgroundTexture);
        backgroundTexture = nullptr;
    }
}

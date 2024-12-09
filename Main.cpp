#include "SDL_net.h"

#include <SDL.h>
#include <SDL_ttf.h>
#include <stdio.h>
#include <string>
#include <Windows.h>

#include "MyGame.h"

using namespace std;

const char* IP_NAME = "localhost";
const Uint16 PORT = 55555;

bool is_running = true;
bool startedGame = false;
bool is_connecting = true;
bool has_made_connection = true;
bool numConnections = 1;
bool gameActive = false;


struct connectionData
{
    boolean success;
    const char* message;

    connectionData(boolean a, const char* b) : success(a), message(b) {}
};

MyGame* game = new MyGame();
TCPsocket overallSocket;

static int on_receive(void* socket_ptr) {
    TCPsocket socket = (TCPsocket)socket_ptr;

    const int message_length = 1024;

    char message[message_length];
    int received;

    // TODO: while(), rather than do
    do {
        received = SDLNet_TCP_Recv(socket, message, message_length);
        message[received] = '\0';

        char* pch = strtok(message, ",");

        // get the command, which is the first string in the message
        string cmd(pch);

        // then get the arguments to the command
        vector<string> args;

        while (pch != NULL) {
            pch = strtok(NULL, ",");

            if (pch != NULL) {
                args.push_back(string(pch));
            }
        }

        if (cmd == "exit") {
            break;
        }

        if (cmd == "CONNECTEVENT") {
            numConnections = stoi(args.at(0));
        }

        if (cmd == "GAMESTATE" || cmd == "GAMESTATEGAME_DATA") { //Sometimes server gets confused. This is a hack.
            std::cout << "GameState Recieved" << std::endl;
            gameActive = true; //allows starting in lobby                                                    
        }

        if (cmd == "GAME_START") {
        startedGame = true; //exits lobby loop, lets program know to kill threads when game is closed
        }

        game->on_receive(cmd, args);


    } while (received > 0 && is_running);

    return 0;
}

static int on_send(void* socket_ptr) {
    TCPsocket socket = (TCPsocket)socket_ptr;

    while (is_running) {
        if (game->messages.size() > 0) {
            string message = "CLIENT_DATA";

            for (auto m : game->messages) {
                message += "," + m;
            }

            game->messages.clear();

            cout << "Sending_TCP: " << message << endl;

            SDLNet_TCP_Send(socket, message.c_str(), message.length());
        }

        SDL_Delay(1);
    }

    return 0;
}

void renderImages(SDL_Renderer* renderer) {
    game->loadPlayerTextures(renderer);
    game->loadBackgroundTexture(renderer, "assets/textures/Pitch.png");
}

SDL_Texture* renderText(const char* message, TTF_Font* font, SDL_Color color, SDL_Renderer* renderer) {
    SDL_Surface* surface = TTF_RenderText_Solid(font, message, color);
    SDL_Texture* texture = SDL_CreateTextureFromSurface(renderer, surface);
    SDL_FreeSurface(surface);
    return texture;
}

void loop(SDL_Renderer* renderer) {
    SDL_Event event;

    renderImages(renderer);

    while (is_running) {
        // input
        while (SDL_PollEvent(&event)) {
            if ((event.type == SDL_KEYDOWN || event.type == SDL_KEYUP) && event.key.repeat == 0) {
                game->input(event);

                switch (event.key.keysym.sym) {
                    case SDLK_ESCAPE:
                        is_running = false;
                        break;

                    default:
                        break;
                }
            }

            if (event.type == SDL_QUIT) {
                is_running = false;
            }
        }

        SDL_SetRenderDrawColor(renderer, 0, 0, 0, 255);
        SDL_RenderClear(renderer);

        game->update();

        game->render(renderer);

        SDL_RenderPresent(renderer);

        SDL_Delay(17);
    }
}

int run_game() {
    SDL_Window* window = SDL_CreateWindow(
        "Striker",
        SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED,
        1280, 720,
        SDL_WINDOW_SHOWN
    );



    if (nullptr == window) {
        std::cout << "Failed to create window" << SDL_GetError() << std::endl;
        return -1;
    }

    SDL_Renderer* renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED);

    if (nullptr == renderer) {
        std::cout << "Failed to create renderer" << SDL_GetError() << std::endl;
        return -1;
    }

    loop(renderer);

    return 0;
}

connectionData tryConnection(char* p_ip, char* port) {
    IPaddress ip;

    std::string newStr = std::string(port);

    int newInt = stoi(newStr);

    Uint16 newUint16 = static_cast<UINT16>(newInt);

    if (SDLNet_ResolveHost(&ip, p_ip, newUint16) == -1) {
        printf("SDLNet_ResolveHost: %s\n", SDLNet_GetError());
        printf("No Server found.");
        return connectionData(false, SDLNet_GetError());
    }

    overallSocket = SDLNet_TCP_Open(&ip);

    if (!overallSocket) {
        printf("SDLNet_TCP_Open: %s\n", SDLNet_GetError());
        return connectionData(false, SDLNet_GetError());
    }

    return connectionData(true, " ");
}

void load_connection_screen(SDL_Renderer* renderer, TTF_Font* font) {
    SDL_Color white = { 255, 255, 255, 255 };
    //SDL_Color green = { 0, 255, 0, 255 };
    SDL_Color red = { 255, 0 , 0, 255 };
    SDL_Color navy = { 0, 0, 128, 255 };
    SDL_Color black = { 0, 0, 0, 255 };
    SDL_Color lightGold = { 255, 215, 0, 255 };

    char ipBuffer[40] = "localhost";
    char portBuffer[6] = "55555";
    int ipLength = strlen(ipBuffer); //max 39
    int portLength = strlen(portBuffer); //max 5
    bool typingIP = true;  // Player Initially Enters IP
    bool hasErrored = false;
    std::string connectionString = " ";

    SDL_StartTextInput(); // Let User Enter Text

    bool quit = false;
    SDL_Event e;

    while (!quit)
    {
        //Take input
        while (SDL_PollEvent(&e))
        {
            if (e.type == SDL_QUIT) {
                quit = true;
                break;
            }

            if (e.type == SDL_TEXTINPUT) {
                if (typingIP && ipLength < sizeof(ipBuffer) - 1) {
                    strcat(ipBuffer, e.text.text);
                    ipLength += strlen(e.text.text);
                }
                else if (!typingIP && portLength < sizeof(portBuffer) - 1) {
                    if (isdigit(e.text.text[0])) {
                        strcat(portBuffer, e.text.text);
                        portLength += strlen(e.text.text);
                    }
                }
            }
            if (e.type == SDL_KEYDOWN) {
                if (e.key.keysym.sym == SDLK_BACKSPACE) {
                    if (typingIP && ipLength > 0) {
                        ipBuffer[--ipLength] = '\0';
                    }
                    else if (!typingIP && portLength > 0) {
                        portBuffer[--portLength] = '\0';
                    }
                }
                if (e.key.keysym.sym == SDLK_TAB) {
                    typingIP = !typingIP;
                }
                if (e.key.keysym.sym == SDLK_RETURN) {
                    // Attempt connection with entered IP
                    connectionData result = tryConnection(ipBuffer, portBuffer);
                    if (result.success) {
                        quit = true;
                        hasErrored = false;
                        connectionString = "Server found! Connecting.";
                        has_made_connection = true;
                    }
                    else {
                        hasErrored = true;
                        connectionString = result.message;
                        if (connectionString.empty())
                            connectionString = "Server not found";
                    }
                }
            }
        }

        // Draw Background
        SDL_RenderClear(renderer);
        for (int y = 0; y < 720; y++) {
            float interpolationFactor = (float)y / 720;

            Uint8 r = (Uint8)(navy.r + interpolationFactor * (black.r - navy.r));
            Uint8 g = (Uint8)(navy.g + interpolationFactor * (black.g - navy.g));
            Uint8 b = (Uint8)(navy.b + interpolationFactor * (black.b - navy.b));

            SDL_SetRenderDrawColor(renderer, r, g, b, 255);

            SDL_Rect lineRect = { 0, y, 1280, 1 };
            SDL_RenderFillRect(renderer, &lineRect);
        }
         
        //SDL_SetRenderDrawColor(renderer, 255, 0, 0, 255);
        //SDL_RenderClear(renderer);
        //////////////////////////////////

        // Display the IP and Port input fields
        SDL_Texture* topUiText = renderText("Enter Details to Connect to Server: ", font, white, renderer);
        SDL_Texture* ipLabel = renderText("IP Address: ", font, white, renderer);
        SDL_Texture* ipText = renderText(ipBuffer, font, typingIP ? lightGold : white, renderer);
        SDL_Texture* portLabel = renderText("Port: ", font, white, renderer);
        SDL_Texture* portText = renderText(portBuffer, font, typingIP ? white : lightGold, renderer);
        SDL_Texture* connectText = renderText("Hit Enter to Join", font, lightGold, renderer); 
        SDL_Texture* errorText = renderText(connectionString.c_str(), font, hasErrored ? red : lightGold, renderer);

        //int widthAddress = 175;
        //int widthPort = 150;

        //Change width of text boxes based on input length
        int widthAddress = 0;
        int widthPort = 0;

        for (int i = 0; i < strlen(ipBuffer); i++) {
            widthAddress += 15;
        }

        for (int i = 0; i < strlen(portBuffer); i++) {
            widthPort += 20;
        }

        // Text Centering //////////////////////////////////////////////////////////////

        

        ///////////////////////////////////////////////////////////////////////////////

        SDL_Rect topUiTextRect = { 400, 80, 500, 50 };
        SDL_Rect ipLabelRect = { 400, 180, 200, 50 };
        SDL_Rect ipTextRect = { 640, 180, widthAddress, 50 };
        SDL_Rect portLabelRect = { 400, 280, 100, 50 };
        SDL_Rect portTextRect = { 640, 280, widthPort, 50 };
        SDL_Rect connectRect = { 400, 380, 400, 50 };
        SDL_Rect errorRect = { 400, 480, 400, 60 };

        SDL_RenderCopy(renderer, topUiText, NULL, &topUiTextRect);
        SDL_RenderCopy(renderer, ipLabel, NULL, &ipLabelRect);
        SDL_RenderCopy(renderer, ipText, NULL, &ipTextRect);
        SDL_RenderCopy(renderer, portLabel, NULL, &portLabelRect);
        SDL_RenderCopy(renderer, portText, NULL, &portTextRect);
        SDL_RenderCopy(renderer, connectText, NULL, &connectRect);
        SDL_RenderCopy(renderer, errorText, NULL, &errorRect);

        SDL_RenderPresent(renderer);

        SDL_DestroyTexture(topUiText);
        SDL_DestroyTexture(ipLabel);
        SDL_DestroyTexture(ipText);
        SDL_DestroyTexture(portLabel);
        SDL_DestroyTexture(portText);
        SDL_DestroyTexture(connectText);
        SDL_DestroyTexture(errorText);
    }

    SDL_StopTextInput();  // Stop text input  
}

void load_lobby(SDL_Renderer* renderer, TTF_Font* font)
{
    std::cout << "Joined Lobby";

    startedGame = true;
    
}

int main(int argc, char** argv) {
    // Initialize SDL
    if (SDL_Init(0) == -1) {
        printf("SDL_Init: %s\n", SDL_GetError());
        exit(1);
    }

    // Initialise SDL_ttf
    if (TTF_Init() == -1) {
        printf("TTF_Init: %s\n", SDL_GetError());
        exit(1);
    }

    TTF_Font* font = TTF_OpenFont("assets/fonts/NeueHaasDisplay.ttf", 36);
    if (!font) {
        std::cerr << "Failed to load font: " << TTF_GetError() << std::endl;
        return -1;
    }

    // Initialize SDL_net
    if (SDLNet_Init() == -1) {
        printf("SDLNet_Init: %s\n", SDLNet_GetError());
        exit(2);
    }

    // Create window
    SDL_Window* window = SDL_CreateWindow("Striker!", SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, 1280, 720, SDL_WINDOW_SHOWN);
    if (!window) {
        printf("Window could not be created! SDL_Error: %s\n", SDL_GetError());
        exit(4);
    }

    // Create renderer
    SDL_Renderer* renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED);
    if (!renderer) {
        printf("Renderer could not be created! SDL_Error: %s\n", SDL_GetError());
        exit(5);
    }

    load_connection_screen(renderer, font);

    if (has_made_connection)
    {
        SDL_Thread* recvThread = SDL_CreateThread(on_receive, "ConnectionReceiveThread", (void*)overallSocket);
        SDL_Thread* sendThread = SDL_CreateThread(on_send, "ConnectionSendThread", (void*)overallSocket);

        //SDL_SetWindowTitle(window, "Lobby");
        load_lobby(renderer, font);

        if (startedGame) {
            SDL_DestroyWindow(window); //destroy screen for game screen
            SDL_DestroyRenderer(renderer); //destroy renderer as new one is created in run_game
            run_game();
        }

        is_running = false;
        int threadReturnValue;
        std::cout << "Waiting for threads to exit...";
        SDL_WaitThread(recvThread, &threadReturnValue);
        SDL_WaitThread(sendThread, &threadReturnValue);
    }

    delete game;

    // Close connection to the server
    SDLNet_TCP_Close(overallSocket);

    // Shutdown SDL_net
    SDLNet_Quit();

    // Shutdown SDL
    SDL_Quit();

    /*
    IPaddress ip;

    // Resolve host (ip name + port) into an IPaddress type
    if (SDLNet_ResolveHost(&ip, IP_NAME, PORT) == -1) {
        printf("SDLNet_ResolveHost: %s\n", SDLNet_GetError());
        exit(3);
    }

    // Open the connection to the server
    TCPsocket socket = SDLNet_TCP_Open(&ip);

    if (!socket) {
        printf("SDLNet_TCP_Open: %s\n", SDLNet_GetError());
        exit(4);
    }

    SDL_CreateThread(on_receive, "ConnectionReceiveThread", (void*)socket);
    SDL_CreateThread(on_send, "ConnectionSendThread", (void*)socket);

    run_game();

    delete game;

    // Close connection to the server
    SDLNet_TCP_Close(socket);

    // Shutdown SDL_net
    SDLNet_Quit();

    //Clear Textures
    game->cleanupBackground();

    // Shutdown TTF
    TTF_Quit();

    // Shutdown SDL
    SDL_Quit();
    */

    return 0;
}
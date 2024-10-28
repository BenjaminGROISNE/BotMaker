#ifndef GAME_H
#define GAME_H

#include <iostream>
#include <array>
#include <memory>
#include <ctime>
#include <filesystem>
#include <atomic>	
#include <chrono>
#include <thread>
#include <stdexcept>
#include <cstdio>
#include <algorithm>
#include "fopencv.h"
#include "fadb.h"
#include "sysCommands.h"
#include "BotConfig.h"
#include "paths.h"
#include <functional>
#include "Activities.h"
#include "Interpretor.h"

class game : public Activities
{
public:
	game();
	bool startBot();
	void initActivities();
	void selectActivity();
	std::string selectPreset();
	void selectFavoritePreset();
	void botLoop();
	void activityLoop();
	void explainPresetChoice();
	bool chronoPresetMenu(std::chrono::time_point<std::chrono::steady_clock> start, int duration, int& lastelapsed);
	bool botEnd();
	void startGame();
	void getAdbId();
	bool restartBot;
	bool firstBoot;
	bool endBot;
	int dimX, dimY, dpi;
	std::string package;
	std::string activity;
	std::string favoritePreset;
	std::string adbId;
	Activities allAct;
	Interpretor myIt;
	BotConfig bc; 


};


#endif

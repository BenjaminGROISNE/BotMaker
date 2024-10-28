
#pragma once
#include "MyFrame.h"
#include "game.h"
#include <TlHelp32.h>
#include <fstream>
#include <string>
#include <iostream>
#include <ctime>
#include <wtypes.h>
#include "BotConfig.h"
#include "LDPlayer.h"


class WXManager :public wxApp
{
public:
	bool OnInit() override;
};




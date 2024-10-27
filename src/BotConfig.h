#pragma once
#include <fstream>
#include <vector>
#include <string>
#include <iostream>
#include "BlueStacks.h"
#include "LDPlayer.h"
#include <limits>


#include "TextManager.h"
#include <variant>
#include <cstdint>
#include <utility>



struct AllEmulators {
	AllEmulators();
	AllEmulators(const std::string gamePackage, int dimX, int dimY,int dpi);
	void setAllInstances();
	void fillBluestacksInstances();
	void fillLdPlayerInstances();
	void fillAllInstances();
	void showAllInstances();
	void showInstance(int index);
	void showInstance(Instance *inst);
	Instance* getInstance(int index);
	bool isLdPlayerInstance(int index);
	bool isBluestacksInstance(int index);
	bool isBluestacksInstance(Instance* inst);
	bool isLdPlayerInstance(Instance* inst);
	bool isBluestacks(Emulator* emu);
	bool isLdPlayer(Emulator* emu);
	Bluestacks* transformBluestacks(Emulator* emu);
	LDPlayer* transformLdPlayer(Emulator* emu);
	Instance* getInstance(std::string globalId);
	int getFavoriteInstanceIndex(std::string globalId);
	BluestacksInstance* getBluestacksInstance(int index);
	LDPlayerInstance* getLDPlayerInstance(int index);
	void setPlayingEmulator(Instance* inst);
	void clearAllInstances();
	void clearBluestacksInstances();
	void clearLdPlayerInstances();

	std::vector<Instance*> LdPlayerInstances;
	std::vector<Instance*> BluestackInstances;
	Instance* playingInstance;
	Emulator* playingEmulator;
	std::vector <std::pair<int, Instance*>> allInstances;
	Bluestacks blue;
	LDPlayer ld;
};

class BotConfig
{
public:
	BotConfig();
	BotConfig(std::string gamePackage, std::string gameActivity, int dimX, int dimY,int dpi);
	std::string readString(const std::string& filename, const std::string& lineParameter, const std::string& first, const std::string& last);
	int readInt(const std::string& filename, const std::string& lineParameter, const std::string& first, const std::string& last);
	bool readBool(const std::string& filename, const std::string& lineParameter, const std::string& first, const std::string& last);

	int readIntBotFile(const std::string& lineParameter);
	bool readBoolBotFile(const std::string& lineParameter);
	std::string readStringBotFile(const std::string& lineParameter);
	bool anyPlayableInstance();


	//Choose Instance
	void choosePlayingInstance();
	bool chronoInstanceMenu(std::chrono::time_point< std::chrono::steady_clock> start, int duration, int& lastelapsed);
	bool interactUserInstance(int totalInstances, int& nbRunningInstance);
	void interactUserFavoriteInstance(int totalInstances);
	int handleFavoriteInstance();
	void explainInstanceChoice();


	//Read Data
	bool readDataCollected();
	bool readDataBluestacks();
	bool readDataLDPlayer();
	std::string readLineBotFile(const std::string& lineparameter);
	std::string readFavoriteInstance();
	std::string readFavoritePreset();
	void read();
	void readBluestacks();
	std::string readBluestacksFolder();
	std::string readBluestacksConfig();
	std::string readLdPlayerFolder();
	void readLdPlayer();
	bool readSearchAll();
	bool readSearchBluestacks();
	bool readSearchLdPlayer();


	//Update Data
	void updateString(const std::string& filename, const std::string& lineParameter, const std::string& content, const std::string& first, const std::string& last);
	void updateStringBotFile(const std::string& lineParameter, const std::string& content, const std::string& first, const std::string& last);
	void updateInt(const std::string& filename, const std::string& lineParameter, const int& content, const std::string& first, const std::string& last);
	void updateIntBotFile(const std::string& lineParameter, const int& content, const std::string& first, const std::string& last);
	void updateBool(const std::string& filename, const std::string& lineParameter, const bool& content, const std::string& first, const std::string& last);
	void updateBoolBotFile(const std::string& lineParameter, const bool& content, const std::string& first, const std::string& last);
	void updateFavoriteInstance(Instance* inst);
	void updateFavoritePreset(const std::string& preset);
	void updateInit();
	void updateInitBluestacks();
	void updateInitLdPlayer();
	void updateDataAll(bool search);
	void updateDataBluestacks(bool search);
	void updateDataLdPlayer(bool search);
	void updateBluestacksFolder(const std::string& folder);
	void updateBluestacksConfig(const std::string& folder);
	void updateLdPlayerFolder(const std::string& folder);

	void waitBootDevice();
	//Write Data
	void writeInit();
	std::string writeInitBluestacks();
	std::string writeInitLdPlayer();

	std::string textInitBluestacks();
	std::string textInitLdPlayer();
	std::string getBotFilecontent();


	void setParameters();
	void getAllPlayableInstances();
	void setSearchEmulators(bool reset=false);
	void initBotFile();
	void checkPlayingInstanceParameters();
	bool initBotConfig(bool reset=false);
	void initEmulators();
	void initBluestacks();
	void initLdPlayer();
	void setDim(int dimx, int dimy);
	bool verifyParameters();
	bool verifyBluestacksParameters(const std::string& botFile);
	bool verifyLdPlayerParameters(const std::string& botFile);
	bool verifyfavoriteParameters(const std::string& botFile);

	Instance* getPlayingInstance();
	bool doWriteInit();

	bool verifyEmulatorFolder(const std::string& emulatorFolder, bool& emulatorinstalled,bool& search);
	void startPlayingInstance();
	bool blueStacksInstalled,ldPlayerInstalled;
	bool searchAll,searchBluestacks,searchLdPlayer;
	std::string getAdbKey();
	std::string favPresetParam, favInstanceParam,emulatorFolder,data, bluestacksFolder,bluestacksConfigFolder,ldPlayerFolder,dataBluestacks,dataLDPlayer,dataCollected,NOTINSTALLED,TOSEARCH,end,quote;
	std::string gamePackage, gameActivity;
	std::string BotFile;
	std::string favoritePreset, favoriteInstance;
	std::string adbKey;
	int desiredX, desiredY,desiredDpi;
	AllEmulators allEmu;
};

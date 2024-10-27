
#ifndef BLUESTACKS_H
#define BLUESTACKS_H
#include <iostream>
#include <vector>
#include "fadb.h"
#include "sysCommands.h"
#include "BluestacksInstance.h"
#include "Emulator.h"

class Bluestacks :public Emulator {
public:
	Bluestacks();
	void setListPlayableInstances();
	Bluestacks(const std::string& gamePackage, int reqDimX, int reqDimY,int dpi);
	void startEmulator()override;
	void startInstance(Instance* inst)override;
	void stopInstance(Instance* inst)override;
	void showInstance(Instance* Inst)override;
	void getAdbKey(Instance* inst)override;
	void stopEmulator()override;
	bool searchEmulator()override;
	void searchAllInstances()override;
	void searchInstance();
	bool initEmulator(bool searchFiles)override;
	void initInstances()override;
	bool init(bool searchFiles)override;
	bool hasGame(Instance* inst)override;
	void setSubFolders()override;
	bool isInstanceRunning(Instance* inst)override;
	void waitForInstance(Instance* inst)override;
	void clearAllInstances()override;
	bool isInstanceLoaded(Instance* inst)override;
	void setRequiredDimensions(Instance* inst)override;
	bool adbActivated(Instance* inst)override;
	void activateAdb(Instance* inst)override;
	void receiveFolders(std::string hdPlayerFolder,std::string configFolder);
	BluestacksInstance* transformInstance(Instance* inst);
	bool startPlayingInstance();
	std::string configFolder,hdPlayerFolder;
};
#endif


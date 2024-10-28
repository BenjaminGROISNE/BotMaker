#pragma once
#include <vector>
#include "Emulator.h"
#include "LDPlayerInstance.h"

class LDPlayer : public Emulator
{
	public:
		LDPlayer();
		LDPlayer(std::string gamePackage, int reqDimX, int reqDimY,int dpi);
		void startEmulator()override;
		void startInstance(Instance* inst)override;
		void stopInstance(Instance* inst)override;
		void stopEmulator()override;
		bool searchEmulator()override;
		void searchAllInstances()override;
		bool initEmulator(bool searchFiles)override;
		void initInstances()override;
		void showInstance(Instance* inst)override;
		bool hasGame(Instance* inst)override;
		bool init(bool searchFiles)override;
		void setListPlayableInstances()override;
		void getAdbKey(Instance* inst)override;
		void setSubFolders()override;
		void clearAllInstances()override;
		bool isInstanceLoaded(Instance* inst)override;
		void waitForInstance(Instance* inst)override;
		void setRequiredDimensions(Instance* inst)override;
		bool adbActivated(Instance* inst)override;
		void activateAdb(Instance* inst)override;
		std::string getConfigFile(Instance* inst);
		bool isInstanceRunning(Instance* inst);
		void receiveFolders(std::string ldPlayerFolder);
		LDPlayerInstance* transformInstance(Instance* inst);
		bool selectedPlayingInstance();
		std::string getPlayingInstanceAdbKey();
		bool startPlayingInstance();
		void searchInstanceConfig(std::string engineName);
		void searchAllInstancesConfig();
		void showAllInstances();
		void showAllPlayableInstances();
		std::string formalEngineName;
		std::string configFolder;
		std::string vmsFolder;
		std::string ldPlayerFolder;
		std::vector<std::string> allEngines;
private:

};


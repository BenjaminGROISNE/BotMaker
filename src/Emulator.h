#pragma once
#include "Instance.h"
#include "sysCommands.h"
#include "TextManager.h"
#include <vector>
#include "fadb.h"


class Emulator
{
	public:
		Emulator();
		~Emulator();
		virtual void startEmulator()=0;
		virtual void startInstance(Instance* inst)=0;
		virtual void stopInstance(Instance* inst)=0;
		virtual void stopEmulator()=0;
		virtual bool searchEmulator()=0;
		virtual void searchAllInstances()=0;
		virtual bool initEmulator(bool searchFiles)=0;
		virtual void initInstances()=0;
		virtual bool init(bool searchFiles)=0;
		virtual void setListPlayableInstances()=0;
		virtual bool startPlayingInstance()=0;
		virtual bool hasGame(Instance* inst) = 0;
		virtual void showInstance(Instance* Inst)=0;
		virtual void getAdbKey(Instance* inst)=0;
		virtual bool isInstanceRunning(Instance* inst)=0;
		virtual void addInstance(Instance* inst);
		virtual void setSubFolders()=0;
		virtual void clearAllInstances();
		virtual bool isInstanceLoaded(Instance* inst)=0;
		virtual void waitForInstance(Instance* inst)=0;
		virtual void setRequiredDimensions(Instance*inst)=0;
		virtual bool adbActivated(Instance* inst) = 0;
		virtual void activateAdb(Instance* inst) = 0;
		void checkAdb(Instance* inst);
		void checkDimensions(Instance* inst);
		bool hasRequiredDimensions(Instance* inst);
		
		virtual EmulatorType getEmulatorType();
		EmulatorType myType;
		bool searchEmulatorDrive(std::string&emulatorFolder,const std::string& diskLetter);
		bool searchEmulatorAllDrives(std::string& emulatorFolder, const std::vector<std::string>& allDiskLetter);
		virtual std::vector<Instance*>& getListInstances();
		virtual std::vector<Instance*>& getListPlayableInstances();
		bool selectedPlayingInstance();
		std::string getPlayingInstanceAdbKey();
		Instance* playingInstance;
		std::string gamePackage;
		std::string emulatorName;
		std::string executableName;
		bool isInstalled;
		int nbInstances;
		std::vector<Instance*>listPlayableInstances;
		std::vector<Instance*> listInstances;
		int reqDimX, reqDimY,reqDpi;
};


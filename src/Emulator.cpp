#include "Emulator.h"

Emulator::Emulator()
{
	emulatorName = "BasicEmulator";
	executableName.clear();
	gamePackage.clear();
	isInstalled = false;
	nbInstances = 0;
	myType = OTHER;
	playingInstance = nullptr;
	reqDimX = 0;
	reqDimY = 0;
	reqDpi = 0;
}

Emulator::~Emulator()
{
	for (Instance* inst : listInstances) {
		delete inst;
	}
}

void Emulator::addInstance(Instance* inst)
{
	listInstances.push_back(inst);
}

void Emulator::clearAllInstances()
{
	for (Instance* inst : listInstances) {
		delete inst;
	}
	listInstances.clear();
	listPlayableInstances.clear();
	playingInstance = nullptr;
	nbInstances = 0;
}


void Emulator::checkAdb(Instance* inst)
{
	if (!adbActivated(inst)) {
		activateAdb(inst);
	}
}

void Emulator::checkDimensions(Instance* inst)
{
	if (!hasRequiredDimensions(inst)) {
		std::cout << "Dimensions of instance " << inst->instanceName << " are not the required ones\n";
		setRequiredDimensions(inst);
	}
}

bool Emulator::hasRequiredDimensions(Instance* inst)
{
	return inst->width==reqDimX&&inst->height==reqDimY&&inst->dpi==reqDpi;
}

EmulatorType Emulator::getEmulatorType()
{
	return myType;
}

bool Emulator::searchEmulatorDrive(std::string& emulatorFolder, const std::string& diskLetter)
{
	std::string fold = diskLetter + ":\\";
	emulatorFolder = findPathFolder(fold, executableName);
	return !emulatorFolder.empty();
}
bool Emulator::searchEmulatorAllDrives(std::string& emulatorFolder, const std::vector<std::string>& allDiskLetter)
{
	for (const std::string& diskLetter : allDiskLetter) {
		if (searchEmulatorDrive(emulatorFolder, diskLetter))return true;
	}
	return false;
}

std::vector<Instance*>& Emulator::getListInstances()
{
	return listInstances;
}
std::vector<Instance*>& Emulator::getListPlayableInstances()
{
	return listPlayableInstances;
}

bool Emulator::selectedPlayingInstance()
{
	return playingInstance != nullptr;
}

std::string Emulator::getPlayingInstanceAdbKey()
{
	if (selectedPlayingInstance()) {
		return playingInstance->getAdbKey();
	}
	return std::string();
}

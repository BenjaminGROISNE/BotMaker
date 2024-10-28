#include "LDPlayer.h"

LDPlayer::LDPlayer():Emulator()
{
    allEngines.clear();
	vmsFolder.clear();
	ldPlayerFolder.clear();
    formalEngineName = "leidian";
	emulatorName = "LDPlayer";
	executableName = "ldconsole.exe";
    playingInstance = nullptr;
    myType = LDPLAYER;
    reqDimX = 0;
    reqDimY = 0;
}
LDPlayer::LDPlayer(std::string gamePackage, int reqDimX, int reqDimY,int dpi): LDPlayer()
{
    this->gamePackage = gamePackage;
    this->reqDimX = reqDimX;
    this->reqDimY = reqDimY;
    this->reqDpi = dpi;

}

void LDPlayer::startEmulator()
{
    std::string args = executableName + " launch --index 0";
    moveAndExecute(ldPlayerFolder, args);
}

void LDPlayer::startInstance(Instance* inst)
{
    LDPlayerInstance* newInst = transformInstance(inst);
	std::string args= executableName+" launch --index " + std::to_string(newInst->ldId);
	moveAndExecute(ldPlayerFolder, args);
}

void LDPlayer::stopInstance(Instance* inst)
{
    LDPlayerInstance* newInst = transformInstance(inst);
	std::string args=executableName+" quit --index " + std::to_string(newInst->ldId);
	moveAndExecute(ldPlayerFolder, args);
}

void LDPlayer::stopEmulator()
{
	std::string args = executableName + " quitall";
	moveAndExecute(ldPlayerFolder, args);
}

bool LDPlayer::searchEmulator()
{
    std::vector<std::string>allDiskLetters = returnAllDrivesLetter();
    std::cout << "\nAll disks found:\n";
    for (std::string letter : allDiskLetters) {
        std::cout << letter << "\n";
    }
    if (searchEmulatorAllDrives(ldPlayerFolder, allDiskLetters)){
        std::cout << "============================================================================================\n";
        std::cout << "END OF SEARCH\n\n";
        std::cout << emulatorName<<" Found at "<<ldPlayerFolder << "\n\n";
        setSubFolders();
        isInstalled = true;
    }
    else {
        std::cout << emulatorName << "Not installed \n";
    }
    return isInstalled;
}


void LDPlayer::searchAllInstances()
{
    nbInstances = 0;
    while (true) {
        std::string engineName = formalEngineName + std::to_string(nbInstances);
        std::string folder = vmsFolder + "\\"+engineName;
        if (pathExists(folder)) {
            ++nbInstances;
            allEngines.push_back(engineName);
        }
        else break;
    }
}

bool LDPlayer::initEmulator(bool searchFiles)
{
    if (searchFiles) {
       return searchEmulator();
    }
    return true;
}

void LDPlayer::initInstances() {
    searchAllInstances();
    searchAllInstancesConfig();
    setListPlayableInstances();
}

bool LDPlayer::init(bool searchFiles)
{
    if(!initEmulator(searchFiles))return false;
    initInstances();
    return true;
}

void LDPlayer::setListPlayableInstances()
{
    for (Instance* instance : getListInstances()) {
        if (hasGame(instance)) {
            getListPlayableInstances().push_back(instance);
        }
    }
}

LDPlayerInstance* LDPlayer::transformInstance(Instance* inst)
{
    return dynamic_cast<LDPlayerInstance*>(inst);
}

void LDPlayer::setSubFolders()
{
    vmsFolder = ldPlayerFolder + "\\vms";
    configFolder = vmsFolder + "\\config";
}

bool LDPlayer::selectedPlayingInstance()
{
    return playingInstance!=nullptr;
}

void LDPlayer::receiveFolders(std::string ldPlayerFolder)
{
    	this->ldPlayerFolder = ldPlayerFolder;
        setSubFolders();
}

bool LDPlayer::isInstanceRunning(Instance* inst)
{
    LDPlayerInstance* newInst = transformInstance(inst);
    std::string args = executableName + " isrunning --index " + std::to_string(newInst->ldId);
    std::string result=moveAndExecute(ldPlayerFolder, args);
    return findContent(result, "running");
}

void LDPlayer::clearAllInstances()
{
    Emulator::clearAllInstances();
	allEngines.clear();
}

std::string LDPlayer::getPlayingInstanceAdbKey()
{
    if (selectedPlayingInstance()) {
        return playingInstance->getAdbKey();
    }
    return std::string();
}

void LDPlayer::waitForInstance(Instance* inst)
{
    if (inst != nullptr) {
        std::cout << "Waiting for device to be Loaded\n";
        while (!isDeviceConnected(inst->getAdbKey())) {
            reconnect();
		}
        waitForDevice(inst->getAdbKey());
    }
}

bool LDPlayer::isInstanceLoaded(Instance* inst)
{
	return false;
}

bool LDPlayer::startPlayingInstance()
{
    if (selectedPlayingInstance()) {
        startInstance(playingInstance);
        return true;
    }
    return false;
}

void LDPlayer::getAdbKey(Instance* inst)
{
    if (LDPlayerInstance* newInst = transformInstance(inst)) {
        newInst->adbKey = "emulator-" + std::to_string(5554 + 2 * newInst->ldId);
    }
}

void LDPlayer::searchInstanceConfig(std::string engineName)
{
    std::string file= configFolder + "\\" + engineName;
    LDPlayerInstance* newInstance=new LDPlayerInstance;
    std::string configText = getFileContent(file);
    std::string instanceNameParameter = "\"statusSettings.playerName\": ";
    std::string resolutionParameter = "\"advancedSettings.resolution\": {";
    std::string instanceWidthParameter = "\"width\": ";
    std::string instanceHeightParameter = "\"height\": ";
    std::string instanceDpiParameter = "\"advancedSettings.resolutionDpi\": ";
    newInstance->instanceName = extractLineContent(configText, instanceNameParameter,": \"", "\"");
    newInstance->dpi = stoi(extractLineContent(configText, instanceDpiParameter, instanceDpiParameter, ","));
    std::string resolution = extractContent(configText, resolutionParameter, "}");
    newInstance->width= stoi(extractLineContent(resolution, instanceWidthParameter, ": ", ","));
    std::string heightline = getContentAfter(resolution, instanceHeightParameter);
    newInstance->height = stoi(getContentBefore(heightline, "\n"));
    newInstance->ldId = stoi(extractContent(engineName, formalEngineName, ".config"));
    newInstance->engineName = getContentBefore(engineName,".config");
    newInstance->setGlobalId();
    newInstance->instanceFolder = vmsFolder + "\\" + newInstance->engineName + "\\";
    hasGame(newInstance);
    getAdbKey(newInstance);
    addInstance(newInstance);
}

void LDPlayer::searchAllInstancesConfig()
{
    for (const std::string& engine : allEngines) {
        std::string engineFileName = engine + ".config";
        searchInstanceConfig(engineFileName);
    }
}

void LDPlayer::showInstance(Instance* inst)
{
    if (LDPlayerInstance* ldInst = transformInstance(inst)) {
		ldInst->showInstance();
	}
}

void LDPlayer::showAllInstances()
{
    std::cout << "\n\nList Of All LDPlayer instances:\n\n";
    for (Instance* instance : getListInstances()) {
        showInstance(instance);
    }
}

void LDPlayer::showAllPlayableInstances()
{
    std::cout << "\n\nList Of All LDPlayer instances that have "+ gamePackage+" installed:\n\n";
    for (Instance* instance : getListPlayableInstances()) {
        showInstance(instance);
    }
}

std::string LDPlayer::getConfigFile(Instance* inst)
{
    LDPlayerInstance* newInst = transformInstance(inst);
    return configFolder + "\\" + formalEngineName+std::to_string(newInst->ldId)+".config";
}

bool LDPlayer::hasGame(Instance* inst)
{
   
    if (LDPlayerInstance* newInst = transformInstance(inst)) {
        std::string reportText = getFileContent(inst->instanceFolder + newInst->packageInfoFileName);
        if (findContent(reportText, gamePackage)) {
            newInst->hasGame = true;
        }
        else newInst->hasGame = false;
    }
    return inst->hasGame;
}

void LDPlayer::setRequiredDimensions(Instance* inst) {
    if (isInstanceRunning(inst)) {
        std::cout << "Stopping instance " << inst->instanceName << " to change its resolution\n";
        stopInstance(inst);
    }

    LDPlayerInstance* newInst = transformInstance(inst);
    std::string command = "ldconsole.exe modify --index " + std::to_string(newInst->ldId) + " --resolution " + std::to_string(reqDimX) + "," + std::to_string(reqDimY)+","+std::to_string(reqDpi);
    moveAndExecute(ldPlayerFolder, command);
}


bool LDPlayer::adbActivated(Instance* inst)
{
    if (LDPlayerInstance* newInst = transformInstance(inst)) {
        std::string file=getConfigFile(inst);
        std::string configText = getFileContent(file);
        std::string adbParameter = "\"basicSettings.adbDebug\": ";
        std::string adbValue = extractLineContent(configText, adbParameter, ": ", ",");
        return adbValue == "2";
	}
    return false;
}

void LDPlayer::activateAdb(Instance* inst)
{
    if (isInstanceRunning(inst))stopInstance(inst);
    if (LDPlayerInstance* newInst = transformInstance(inst)) {
		std::string file = getConfigFile(inst);
		std::string configText = getFileContent(file);
		std::string adbParameter = "\"basicSettings.adbDebug\": ";
        std::string adbValue = "2";  
        std::string newText=updateContent(configText, adbValue, adbParameter,",");
        writeToFile(file, newText,OVERWRITE);
	}
}

#include "BlueStacks.h"

/*
Bluestacks::Bluestacks()
{
    isInstalled = false;
    nbInstances = 0;
    hdPlayerPath.clear();
    Engines.clear();
    blueStacksConfigPath.clear();
    hdPlayerFolder.clear();
    blueStacksConfigFolder.clear();
    BluestacksInstances.clear();
    gamepackage.clear();
    exename = "HD-Player.exe";
    confname = "bluestacks.conf";
}

Bluestacks::Bluestacks(std::string Gamepackage)
{
    isInstalled = false;
    nbInstances = 0;
    hdPlayerPath.clear();
    Engines.clear();
    blueStacksConfigPath.clear();
    hdPlayerFolder.clear();
    blueStacksConfigFolder.clear();
    BluestacksInstances.clear();
    gamepackage = Gamepackage;
    exename = "HD-Player.exe";
    confname = "bluestacks.conf";
}

void Bluestacks::sethdPlayerPath()
{
    hdPlayerPath = hdPlayerFolder + "\\HD-player.exe";
}

void Bluestacks::setConfigPath()
{
    blueStacksConfigPath = blueStacksConfigFolder + "\\bluestacks.conf";
}


void Bluestacks::findBlueStacks()
{
    std::vector<std::string>letters = returnAllDrivesLetter();
    std::cout << "\nAll disks found:" << std::endl;
    for (std::string let : letters) {
        std::cout << let << std::endl;
    }
    std::string programDataBlue = ExpandEnvironmentVariables("%ProgramData%") + "\\BlueStacks_nxt";
    std::string programData = ExpandEnvironmentVariables("%ProgramData%");
    std::string programFiles = ExpandEnvironmentVariables("%ProgramFiles%");
    std::string programFilesBlue = ExpandEnvironmentVariables("%ProgramFiles%") + "\\BlueStacks_nxt";
    std::string programFiles86 = ExpandEnvironmentVariables("%ProgramFiles(x86)%");

    hdPlayerFolder = findPathFolder(programFilesBlue, "HD-Player.exe");
    if (hdPlayerFolder.empty()) {
        hdPlayerFolder = findPathFolder(programFiles, "HD-Player.exe");
        if (hdPlayerFolder.empty()) {
            hdPlayerFolder = findPathFolder(programFiles86, "HD-Player.exe");
            if (hdPlayerFolder.empty()) {
                for (std::string letter : letters) {
                    std::string fold = letter + ":\\";
                    hdPlayerFolder = findPathFolder(fold, "HD-Player.exe");
                    if (!hdPlayerFolder.empty())break;
                }
            }
        }
    }

    blueStacksConfigFolder = findPathFolder(programDataBlue, "bluestacks.conf");
    if (blueStacksConfigFolder.empty()) {
        blueStacksConfigFolder = findPathFolder(programData, "bluestacks.conf");
        if (blueStacksConfigFolder.empty()) {
            for (std::string letter : letters) {
                std::string fold = letter + ":\\";
                blueStacksConfigFolder = findPathFolder(fold, "bluestacks.conf");
                if (!blueStacksConfigFolder.empty())break;
            }
        }
    }
    std::cout << "============================================================================================" << std::endl;
    std::cout << "END OF SEARCH" << std::endl << std::endl;

    if (!blueStacksConfigFolder.empty() && !hdPlayerFolder.empty()) {
        setConfigPath();
        sethdPlayerPath();
        std::cout << "HD-Player Folder : " << hdPlayerFolder << std::endl;
        std::cout << "Config Folder : " << blueStacksConfigFolder << std::endl;
        isInstalled = true;
    }
    else throw 40;
}

bool Bluestacks::findGamePackage(const std::string& path, const std::string& packagename)
{
    std::string nameimg = packagename + ".png";
    std::string gamepath = path + "\\AppCache";
    if (findFileFolder(gamepath, nameimg))return true;
    else return false;
}

void Bluestacks::findAllInstancesGame()
{
    for (Instance& inst : BluestacksInstances) {
        if (findGamePackage(inst.path, gamepackage)) {
            inst.hasgame = true;
        }
        else  inst.hasgame = false;
    }

}

void Bluestacks::findInstancePath(Instance& inst)
{
    std::string instancepath = blueStacksConfigFolder + "\\Engine\\" + inst.EngineName;
    if (pathExists(instancepath)) {
        inst.path = instancepath;
    }
}

void Bluestacks::findAllInstancesPath()
{
    for (Instance& inst : BluestacksInstances) {
        findInstancePath(inst);
    }
}

void Bluestacks::startInstance(Instance inst)
{
    if (!IsInstanceRunning(inst.InstanceName)) {
        std::cout << "PreviousBootTime: " << inst.boot << " milliseconds" << std::endl;
        launchBluestacksInstance(hdPlayerFolder, inst.EngineName);
        wait(inst.boot * 2 + 5000);
    }


}

void Bluestacks::wait(int milliSeconds)
{
    std::this_thread::sleep_for(std::chrono::milliseconds(milliSeconds));
}
*/

Bluestacks::Bluestacks():Emulator()
{
    myType = BLUESTACKS;
    playingInstance = nullptr;

}
void Bluestacks::setListPlayableInstances()
{
    for (Instance*& instance : getListInstances()) {
        if (hasGame(instance)) {
            getListPlayableInstances().push_back(instance);
        }
    }
}

Bluestacks::Bluestacks(const std::string& gamePackage, int reqDimX, int reqDimY, int dpi): Bluestacks()
{
    this->gamePackage = gamePackage;
    this->reqDimX = reqDimX;
    this->reqDimY = reqDimY;
    this->reqDpi = dpi;
}

void Bluestacks::startEmulator()
{
}

void Bluestacks::startInstance(Instance* inst)
{
}

void Bluestacks::stopInstance(Instance* inst)
{
}

void Bluestacks::showInstance(Instance* Inst)
{
    if (BluestacksInstance* blueInst = dynamic_cast<BluestacksInstance*>(Inst)) {
		blueInst->showInstance();
	}
}

void Bluestacks::getAdbKey(Instance* inst)
{
}

void Bluestacks::stopEmulator()
{
}

bool Bluestacks::searchEmulator()
{
    return false;
}

void Bluestacks::searchAllInstances()
{
}
void Bluestacks::searchInstance() {

    //BluestacksInstance newInstance;
    //std::string before = "bst.BluestacksInstance." + engineName + ".";
    //newInstance.engineName = engineName;
    //newInstance.instanceName = readString(filename, before + "display_name", "\"", "\"");
    //newInstance.width = readInt(filename, before + "fb_width", "\"", "\"");
    //newInstance.height = readInt(filename, before + "fb_height", "\"", "\"");
    //newInstance.bootTime = readInt(filename, before + "boot_duration", "\"", "\"");
    //newInstance.adbKey = readString(filename, before + "status.adbKey", "\"", "\"");
    //newInstance.id = readString(filename, before + "android_id", "\"", "\"");
    //blue.getListInstances().push_back(newInstance);

    
}

bool Bluestacks::initEmulator(bool searchFiles)
{
	return false;
}

void Bluestacks::initInstances()
{
}

bool Bluestacks::init(bool searchFiles)
{
	return false;
}

bool Bluestacks::hasGame(Instance* inst)
{
    	return false;
}

void Bluestacks::setSubFolders()
{
}

bool Bluestacks::isInstanceRunning(Instance* inst)
{
    return false;
}

void Bluestacks::waitForInstance(Instance* inst)
{
}

void Bluestacks::clearAllInstances()
{
   Emulator::clearAllInstances();
}

bool Bluestacks::isInstanceLoaded(Instance* inst)
{
	return false;
}

void Bluestacks::setRequiredDimensions(Instance* inst)
{
}

bool Bluestacks::adbActivated(Instance* inst)
{
    return false;
}

void Bluestacks::activateAdb(Instance* inst)
{
}



void Bluestacks::receiveFolders(std::string hdPlayerFolder, std::string configFolder)
{
	this->hdPlayerFolder = hdPlayerFolder;
	this->configFolder = configFolder;
}

BluestacksInstance* Bluestacks::transformInstance(Instance* inst)
{
    return dynamic_cast<BluestacksInstance*>(inst);
}


bool Bluestacks::startPlayingInstance()
{
    if (selectedPlayingInstance()) {
        startInstance(playingInstance);
        return true;
    }
    return false;
}



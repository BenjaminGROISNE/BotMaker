#include "BotConfig.h"

BotConfig::BotConfig()
{
    gamePackage.clear();
    favoritePreset.clear();
    favoriteInstance.clear();
    blueStacksInstalled = false;
    ldPlayerInstalled = false;
    setParameters();
    desiredX = desiredY = desiredDpi=0;

}
BotConfig::BotConfig(std::string gamePackage, std::string gameActivity, int dimX,int dimY,int dpi) :BotConfig()
{
    this->gamePackage = gamePackage;
    this->gameActivity = gameActivity;
    this->desiredX = dimX;
    this->desiredY = dimY;
    this->desiredDpi = dpi;
    allEmu = AllEmulators(gamePackage,desiredX,desiredY,desiredDpi);
}

AllEmulators::AllEmulators()
{
	LdPlayerInstances.clear();
	BluestackInstances.clear();
	allInstances.clear();
    playingInstance=nullptr;
    playingEmulator = nullptr;
}

AllEmulators::AllEmulators(const std::string gamePackage,int dimX,int dimY,int dpi) :AllEmulators()
{
	blue = Bluestacks(gamePackage, dimX, dimY,dpi);
	ld = LDPlayer(gamePackage, dimX, dimY,dpi);
}   



std::string BotConfig::readString(const std::string& filename, const std::string& lineParameter, const std::string& first, const std::string& last) {
    std::string text = getFileContent(filename);
    std::string line = getLine(text, lineParameter);
    return extractBetween(line, first, last);
}

int BotConfig::readInt(const std::string& filename, const std::string& lineParameter, const std::string& first, const std::string& last) {

    return stoi(readString(filename, lineParameter, first, last));

}

bool BotConfig::readBool(const std::string& filename, const std::string& lineParameter, const std::string& first, const std::string& last) {

    std::string result = readString(filename, lineParameter, first, last);
    return (result == "1" || result == "true");
}

int BotConfig::readIntBotFile(const std::string& lineParameter)
{
    return readInt(BotFile, lineParameter, quote, quote);
}

bool BotConfig::readBoolBotFile(const std::string& lineParameter)
{
    return readBool(BotFile, lineParameter, quote, quote);
}

std::string BotConfig::readStringBotFile(const std::string& lineParameter)
{
    return readString(BotFile, lineParameter, quote, quote);
}

bool BotConfig::anyPlayableInstance()
{
    if (allEmu.allInstances.size() == 0) {
        std::cout << "No Playable Instance detected\n\n";
        return false;
    }
    else return true;
}

void BotConfig::updateString(const std::string& filename, const std::string& lineParameter, const std::string& content, const std::string& first, const std::string& last)
{
    std::string linetochange;
    std::string text = getFileContent(filename);
    linetochange = getLine(text, lineParameter);
    std::string firstpart, secondpart;
    firstpart = getContentBefore(text, linetochange);
    secondpart = getContentAfter(text, linetochange);
    text = firstpart + updateContent(linetochange, content, first, last) + secondpart;
    writeToFile(filename, text, OVERWRITE);

}

void BotConfig::updateStringBotFile(const std::string& lineParameter, const std::string& content, const std::string& first, const std::string& last)
{
    	updateString(BotFile, lineParameter, content, first, last);
}

void BotConfig::updateInt(const std::string& filename, const std::string& lineParameter, const int& content, const std::string& first, const std::string& last)
{
    updateString(filename, lineParameter, std::to_string(content), first, last);
}

void BotConfig::updateIntBotFile(const std::string& lineParameter, const int& content, const std::string& first, const std::string& last)
{
    updateInt(BotFile, lineParameter, content, first, last);
}

void BotConfig::updateBool(const std::string& filename, const std::string& lineParameter, const bool& content, const std::string& first, const std::string& last)
{
    updateString(filename, lineParameter, std::to_string(content), first, last);
}

void BotConfig::updateBoolBotFile(const std::string& lineParameter, const bool& content, const std::string& first, const std::string& last)
{
    updateBool(BotFile, lineParameter, content, first, last);
}

bool BotConfig::readDataCollected()
{
    return readBoolBotFile(dataCollected);
}

bool BotConfig::readDataBluestacks()
{
    return readBoolBotFile(dataBluestacks);
}
bool BotConfig::readDataLDPlayer()
{
	return readBoolBotFile(dataLDPlayer);
}

std::string BotConfig::readLineBotFile(const std::string& lineparameter)
{
    return readString(BotFile, lineparameter, quote, quote);
}

std::string BotConfig::readFavoriteInstance()
{
    favoriteInstance = readStringBotFile(favInstanceParam);
    return favoriteInstance;
}

std::string BotConfig::readFavoritePreset()
{
    favoritePreset = readStringBotFile(favPresetParam);
    return favoritePreset;
}


std::string BotConfig::getBotFilecontent()
{
    return getFileContent(BotFile);
}



void BotConfig::writeInit()
{
    std::string text;
    text += dataCollected+end;
    text+=textInitBluestacks();
    text+=textInitLdPlayer();
    text += favInstanceParam+end + favPresetParam+end;
    writeToFile(BotFile, text, OVERWRITE);
}

std::string BotConfig::writeInitBluestacks()
{
    return "";
}
std::string BotConfig::writeInitLdPlayer()
{
    return "";
}

std::string BotConfig::textInitBluestacks()
{
    return dataBluestacks + end + bluestacksFolder + end + bluestacksConfigFolder + end;
}

std::string BotConfig::textInitLdPlayer()
{
    return dataLDPlayer + end + ldPlayerFolder + end;
}


void BotConfig::setParameters()
{   //basic
    searchAll = false;
    searchBluestacks = false;
    searchLdPlayer = false;
    TOSEARCH="TOSEARCH";
    NOTINSTALLED="NOTINSTALLED";
    end = "=\"\"\n";
    quote="\"";
    emulatorFolder = "folderEmulator";
    data = "data";
    BotFile = "BotConfig.txt";

    //real parameters
    dataCollected = data+"All";
    favPresetParam = "favoritePreset";
    favInstanceParam = "favoriteId";
    ldPlayerFolder = emulatorFolder+"Ldplayer";
    bluestacksFolder=emulatorFolder+"Bluestacks";
    bluestacksConfigFolder= emulatorFolder +"BluestacksConfig";
    dataLDPlayer=data+"Ldplayer";
    dataBluestacks=data+"Bluestacks";
}

void BotConfig::getAllPlayableInstances()
{
    allEmu.setAllInstances();
    allEmu.fillAllInstances();
}

void BotConfig::setSearchEmulators(bool reset)
{
    if (!reset) {
        if (readSearchAll()) {
            searchBluestacks = true;
            searchLdPlayer = true;
        }
        else
        {
            searchBluestacks = readSearchBluestacks();
            searchLdPlayer = readSearchLdPlayer();
        }
	}
    else {
		searchAll = true;
		searchBluestacks = true;
		searchLdPlayer = true;
        blueStacksInstalled = false;
        ldPlayerInstalled = false;
    }

}

void BotConfig::initBotFile()
{
    if(doWriteInit())updateInit();
}

void BotConfig::checkPlayingInstanceParameters()
{
    allEmu.playingEmulator->checkDimensions(allEmu.playingInstance);
    allEmu.playingEmulator->checkAdb(allEmu.playingInstance);
}



void BotConfig::updateFavoriteInstance(Instance* inst)
{
    updateString(BotFile, favInstanceParam, inst->getGlobalId(), quote, quote);
}

void BotConfig::updateFavoritePreset(const std::string& preset)
{
    updateString(BotFile, favPresetParam, preset, quote, quote);
}

void BotConfig::choosePlayingInstance()
{
    bool choseInstance = false;
    int duration = 30;
    int totalInstances = (int)allEmu.allInstances.size();
    int nbRunningInstance=0;
    if (totalInstances == 1) {
        std::cout << "Only one Playable Instance detected selecting automatically\n\n";
        choseInstance = true;
        nbRunningInstance = 0;
    }
    allEmu.showAllInstances();
    auto start = std::chrono::steady_clock::now();
    int lastelapsed = -1;
    explainInstanceChoice();

    do {
        if(chronoInstanceMenu(start,duration,lastelapsed))break;
       /* if (fsfml::isKeyPressed(sf::Keyboard::C)) {
            if (interactUserInstance(totalInstances, nbRunningInstance)) {
                choseInstance = true;
                break;
            }
        }
        if (fsfml::isKeyPressed(sf::Keyboard::R)) {
            initBotConfig(true);
        }
        if (fsfml::isKeyPressed(sf::Keyboard::F)) {
            interactUserFavoriteInstance(totalInstances);
        }
        if (fsfml::isKeyPressed(sf::Keyboard::S))break;*/

    } while (true);
    if (!choseInstance) {
        nbRunningInstance= handleFavoriteInstance();
    }
    allEmu.playingInstance = allEmu.getInstance(nbRunningInstance);
    std::cout << "\n\nCHOSEN INSTANCE: \n\n";
    allEmu.playingInstance->showInstance();
    allEmu.setPlayingEmulator(getPlayingInstance());
}


void BotConfig::explainInstanceChoice() {
    std::cout << "Type C to enable Instance choice else favorite Instance will be chosen\n";
    std::cout << "Type F to select Favorite Instance\n";
    std::cout << "Type R to reset Configuration\n";
    std::cout << "Type S to Skip\n";
    std::cout << "Validate your input with Enter\n\n";
    std::cout << "Seconds left: ";
}

bool BotConfig::chronoInstanceMenu(std::chrono::time_point<std::chrono::steady_clock> start, int duration,int& lastelapsed) {
    std::string textseconds = "Seconds left: ";
    int maxchar = 0;
    auto now = std::chrono::steady_clock::now();
    long long int elapsed = std::chrono::duration_cast<std::chrono::seconds>(now - start).count();
    int timeleft =(int) duration - elapsed;
    if (lastelapsed != elapsed) {
        std::string time = std::to_string(timeleft);
        int nbchar =(int) time.size();
        for (int j = 0; j < nbchar + textseconds.length(); ++j) {
            std::cout << "\b";
        }
        std::cout << textseconds;
        std::cout << time;
        lastelapsed = elapsed;
    }
    return timeleft < 0;
}

bool BotConfig::interactUserInstance(int totalInstances,int&nbRunningInstance)
{
    bool hasChosen = false;
	std::string strnb;
    std::cout << "\n\nType the number of the Instance\n";
    while (!hasChosen) {
		std::cout << "Nb between 0 and " << totalInstances - 1 << ": ";
		std::cin >> strnb;
        for (int i = 0; i < strnb.size(); ++i) {
            if (isdigit(strnb.at(i))) {
				nbRunningInstance = strnb.at(i) - '0';
                if ((nbRunningInstance) >= 0 && nbRunningInstance < totalInstances) {
                    hasChosen = true;
					break;
				}
			}
		}
	}
    return true;
}


void BotConfig::interactUserFavoriteInstance(int totalInstances)
{
    bool hasChosen = false;
    int nbFavInstance;
    std::string strnb;
    std::cout << "\n\nType the number of the Instance\n";
    while (!hasChosen) {
        std::cout << "Nb between 0 and " << totalInstances - 1 << ": ";
        std::cin >> strnb;
        for (int i = 0; i < strnb.size(); ++i) {
            if (isdigit(strnb.at(i))) {
                nbFavInstance = strnb.at(i) - '0';
                if ((nbFavInstance) >= 0 && nbFavInstance < totalInstances) {
                    hasChosen = true;
                    break;
                }
            }
        }
    }
    updateFavoriteInstance(allEmu.getInstance(nbFavInstance));
}

void BotConfig::setDim(int dimx, int dimy)
{
    desiredX = dimx;
    desiredY = dimy;
}

bool BotConfig::verifyParameters()
{
    std::string text = getBotFilecontent();
    return verifyBluestacksParameters(text)&&verifyLdPlayerParameters(text)&&verifyfavoriteParameters(text);
}

bool BotConfig::verifyBluestacksParameters(const std::string& botFile)
{
    return botFile.find(dataBluestacks) && botFile.find(bluestacksFolder)&& botFile.find(bluestacksConfigFolder);
}

bool BotConfig::verifyLdPlayerParameters(const std::string& botFile)
{
    return botFile.find(dataLDPlayer)&& botFile.find(ldPlayerFolder);
}

bool BotConfig::verifyfavoriteParameters(const std::string& botFile)
{
    return botFile.find(favInstanceParam) && botFile.find(favPresetParam);
}

int BotConfig::handleFavoriteInstance()
{
    if (!readFavoriteInstance().empty()) {
        Instance* favInstance = allEmu.getInstance(favoriteInstance);
        if (favInstance != nullptr) {
            if (favInstance->hasGame) {
                return allEmu.getFavoriteInstanceIndex(favoriteInstance);
            }
        }
    }
    return 0;
}

void BotConfig::read()
{

}



void BotConfig::readBluestacks()
{

}

std::string BotConfig::readBluestacksFolder()
{
    return readStringBotFile(bluestacksFolder);
}

std::string BotConfig::readBluestacksConfig()
{
    return readStringBotFile(bluestacksConfigFolder);
}

std::string BotConfig::readLdPlayerFolder()
{
    return readStringBotFile(ldPlayerFolder);
}

void BotConfig::readLdPlayer()
{
}

void BotConfig::updateInit()
{
    updateDataAll(false);
    updateInitBluestacks();
	updateInitLdPlayer();
}

void BotConfig::updateInitBluestacks()
{
    updateDataBluestacks(false);
}

void BotConfig::updateInitLdPlayer()
{
	updateDataLdPlayer(false);
}

void BotConfig::updateDataAll(bool search)
{
	updateBoolBotFile(dataCollected, search, quote, quote);
}

void BotConfig::updateDataBluestacks(bool search)
{
	updateBoolBotFile(dataBluestacks, search, quote, quote);
}

void BotConfig::updateDataLdPlayer(bool search)
{
	updateBoolBotFile(dataLDPlayer, search, quote, quote);
}

void BotConfig::updateBluestacksFolder(const std::string& folder)
{
	updateStringBotFile(bluestacksFolder, folder, quote, quote);
}
void BotConfig::updateBluestacksConfig(const std::string& folder)
{
	updateStringBotFile(bluestacksConfigFolder, folder, quote, quote);
}

void BotConfig::updateLdPlayerFolder(const std::string& folder)
{
	updateStringBotFile(ldPlayerFolder, folder, quote, quote);
}

void BotConfig::waitBootDevice()
{
    allEmu.playingEmulator->waitForInstance(allEmu.playingInstance);
}


bool BotConfig::readSearchAll()
{
    if(!readDataCollected())return true;
    else if(readSearchLdPlayer()&&readSearchLdPlayer())return true;
    return false;
}

bool BotConfig::readSearchBluestacks()
{
    if(!readDataBluestacks())return true;
	else if (verifyEmulatorFolder(bluestacksFolder,blueStacksInstalled,searchBluestacks)&&verifyEmulatorFolder(bluestacksConfigFolder, blueStacksInstalled, searchBluestacks))return true;
    return false;
}

bool BotConfig::readSearchLdPlayer()
{
    if (!readDataLDPlayer())return true;
    else if (verifyEmulatorFolder(ldPlayerFolder,ldPlayerInstalled,searchLdPlayer))return true;
    return false;
}

Instance* BotConfig::getPlayingInstance()
{
    return allEmu.playingInstance;
}

bool BotConfig::doWriteInit()
{
    bool write = false; 
    if (searchAll || searchBluestacks || searchLdPlayer)write = true;
    else if(!verifyParameters())write=true;
    if (write) {
        writeInit();
    }
    return write;
}

bool BotConfig::verifyEmulatorFolder(const std::string& emulatorFolder,bool& emulatorinstalled,bool&search)
{
    search = false;
    emulatorinstalled = false;
    std::string folder= readStringBotFile(emulatorFolder);
    if (folder == NOTINSTALLED) {
        search = false;
        emulatorinstalled = false;
    }
    else if (pathExists(folder)) {
        search = false;
        emulatorinstalled = true;
    }
    else {
        emulatorinstalled = false;
        search = true;
    }
    return search;
}


bool BotConfig::initBotConfig(bool reset)
{
    allEmu.clearAllInstances();
    setSearchEmulators(reset);
    initBotFile();
    initEmulators();
    getAllPlayableInstances();
    if(!anyPlayableInstance())return false;
    choosePlayingInstance();
    checkPlayingInstanceParameters();
    return true;
}



void BotConfig::initEmulators()
{
    initBluestacks();
    initLdPlayer();
    updateDataAll(true);
}

void BotConfig::initBluestacks()
{
    if (blueStacksInstalled) {
        allEmu.blue.receiveFolders(readBluestacksFolder(),readBluestacksConfig());
        allEmu.blue.init(false);
    }
    else if (allEmu.blue.init(searchBluestacks)) {
        updateBluestacksConfig(allEmu.blue.configFolder);
        updateBluestacksFolder(allEmu.blue.hdPlayerFolder);
        blueStacksInstalled = true;
    }
    else {
        updateBluestacksConfig(NOTINSTALLED);
        updateBluestacksFolder(NOTINSTALLED);
    }
    updateDataBluestacks(true);
}

void BotConfig::initLdPlayer()
{
    if (ldPlayerInstalled) {
        allEmu.ld.receiveFolders(readLdPlayerFolder());
        allEmu.ld.init(false);
    }
    else if (allEmu.ld.init(searchLdPlayer)) {
        ldPlayerInstalled = true;
        updateLdPlayerFolder(allEmu.ld.ldPlayerFolder);
    }
    else {
        updateLdPlayerFolder(NOTINSTALLED);
    }
    updateDataLdPlayer(true);
}

void BotConfig::startPlayingInstance()
{
    allEmu.playingEmulator->startInstance(getPlayingInstance());
}

std::string BotConfig::getAdbKey()
{
    if (allEmu.playingInstance != nullptr) {
        return allEmu.playingInstance->getAdbKey();
    }
    return "";
}


void AllEmulators::setAllInstances()
{
   this->LdPlayerInstances = ld.getListPlayableInstances();
   this->BluestackInstances = blue.getListPlayableInstances();
}

void AllEmulators::fillBluestacksInstances()
{
    for (Instance* ldInstance : LdPlayerInstances) {
		allInstances.push_back(std::make_pair(allInstances.size(), ldInstance));
	}
}
void AllEmulators::fillLdPlayerInstances()
{
    for (Instance* blInstance : BluestackInstances) {
		allInstances.push_back(std::make_pair(allInstances.size(), blInstance));
	}
}

void AllEmulators::fillAllInstances()
{
    fillLdPlayerInstances();
    fillBluestacksInstances();
}

void AllEmulators::showAllInstances()
{
    for (std::pair<int, Instance*> inst : allInstances) {
		std::cout <<"ID CHOICE: " << inst.first << "\n";
        inst.second->showInstance();
        std::cout<< "\n\n";
	}
}

void AllEmulators::showInstance(int index)
{
    index = abs(index);
    if (index < allInstances.size()) {
        if(isBluestacksInstance(index))
		blue.showInstance(getBluestacksInstance(index));
        else if (isLdPlayerInstance(index)) {
        ld.showInstance(getLDPlayerInstance(index));
		}
	}
}

void AllEmulators::showInstance(Instance* inst)
{
    if (isBluestacksInstance(inst)) {
		blue.showInstance(inst);
	}
    else if (isLdPlayerInstance(inst)) {
		ld.showInstance(inst);
	}
}

Instance* AllEmulators::getInstance(int index)
{
    index = abs(index);
    if (index < allInstances.size()) {
        return allInstances.at(index).second;
    }
    else return nullptr;
}

bool AllEmulators::isLdPlayerInstance(int index)
{
    return dynamic_cast<LDPlayerInstance*>(getInstance(index))!=nullptr;
}


bool AllEmulators::isBluestacksInstance(int index)
{
	return dynamic_cast<BluestacksInstance*>(getInstance(index)) != nullptr;
}

bool AllEmulators::isLdPlayerInstance(Instance* inst)
{
    return dynamic_cast<LDPlayerInstance*>(inst) != nullptr;
}

bool AllEmulators::isBluestacks(Emulator* emu)
{
    return dynamic_cast<Bluestacks*>(emu) != nullptr;
}

bool AllEmulators::isLdPlayer(Emulator* emu)
{
    return dynamic_cast<LDPlayer*>(emu) != nullptr;
}

Bluestacks* AllEmulators::transformBluestacks(Emulator* emu)
{
    return dynamic_cast<Bluestacks*>(emu);
}

LDPlayer* AllEmulators::transformLdPlayer(Emulator* emu)
{
    return dynamic_cast<LDPlayer*>(emu);
}


Instance* AllEmulators::getInstance(std::string globalId)
{
    for (int i = 0;i<allInstances.size();++i) {
        Instance*temp= getInstance(i);
        if (temp->getGlobalId() == globalId) {
			return temp;
		}
	}
	return nullptr;
}

int AllEmulators::getFavoriteInstanceIndex(std::string globalId)
{
    for (int i = 0; i < allInstances.size(); ++i) {
		Instance*temp = getInstance(i);
        if (temp->getGlobalId() == globalId) {
			return i;
		}
	}
	return -1;
}

bool AllEmulators::isBluestacksInstance(Instance* inst)
{
    return dynamic_cast<BluestacksInstance*>(inst) != nullptr;
}


BluestacksInstance* AllEmulators::getBluestacksInstance(int index)
{
    return dynamic_cast<BluestacksInstance*>(getInstance(index));
}

LDPlayerInstance* AllEmulators::getLDPlayerInstance(int index)
{
	return dynamic_cast<LDPlayerInstance*>(getInstance(index));
}

void AllEmulators::setPlayingEmulator(Instance*inst)
{
    if (isBluestacksInstance(inst)) {
        playingEmulator = &blue;   
    }
    else if (isLdPlayerInstance(inst)) {
		playingEmulator = &ld;
	}
}

void AllEmulators::clearAllInstances()
{
    clearBluestacksInstances();
    clearLdPlayerInstances();
    allInstances.clear();
}

void AllEmulators::clearBluestacksInstances()
{
    blue.clearAllInstances();
	BluestackInstances.clear();
}

void AllEmulators::clearLdPlayerInstances()
{
    ld.clearAllInstances();
	LdPlayerInstances.clear();
}





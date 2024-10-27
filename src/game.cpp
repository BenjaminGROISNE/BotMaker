#include "game.h"


void game::botLoop() {

    while (!endBot) {
        switch (restartBot) {
        case true:
            startGame();
            break;
        case false:
            goHome();
            if (botEnd()) {
				endBot=true;
				break;
            }
            activityLoop();
        }
    }
    std::cout << "End of Bot!" << std::endl;
}

void game::activityLoop()
{
   // Activity todo=allAct.getOnActivity();
   // todo.doActivity();
   // *todo.activate = false;
}



void game::startGame() {
    try {
        std::cout << "RESTART" << std::endl;
        restartBot = false;
        int packageTime= 2;
        if (onPackage(package,adbId));
        else {
            firstBoot = false;
            while (!onPackage(package,adbId)) {
                quitPackage(package,adbId);
                launchPackage(activity,adbId);
                inte.wait(1000 * packageTime);
                packageTime++;
			}
            launchGame;
        }
    }
    catch (RebootException e) {

        return;
    }
}

void game::getAdbId()
{
    adbId= bc.getAdbKey();
}

game::game() {
    dimX = 1080;
    dimY = 1920;
    dpi = 320;
    package = "com.ltgames.android.m71.sea";
    activity = "com.ltgames.android.m71.sea/com.gbits.hook.HookUnityPlayerActivity";
    std::string currentPath= "C:\\Users\\bgroi\\OneDrive - Université De Technologie De Belfort - Montbeliard\\Documents\\Coding\\C++\\Bots\\BotOVMT";
    myIt = Interpretor(currentPath+"\\Activities");
    bc = BotConfig(package, activity, dimX, dimY, dpi);
    myIt.readActivityFile("ranking");
    createpaths(templates);
    firstBoot = true;
    restartBot = true;
    endBot = false;
}

void game::initActivities()
{
    

}


void game::selectActivity() {
    int duration = 30;
    std::string choix;
    auto start = std::chrono::steady_clock::now();
    int lastelapsed = 0;
    bool chosePreset = false;
    explainPresetChoice();
    do {
        auto now = std::chrono::steady_clock::now();
        int skipelapsed = std::chrono::duration_cast<std::chrono::seconds>(now - start).count();
        if (chronoPresetMenu(start, duration, lastelapsed))break;
       /* if (fsfml::isKeyPressed(sf::Keyboard::P)) {
            choix = selectPreset();
            chosePreset = true;
            break;
        }
        if (fsfml::isKeyPressed(sf::Keyboard::F)) {
            selectFavoritePreset();
        }
        if (fsfml::isKeyPressed(sf::Keyboard::S) && skipelapsed > 0.5f)break;*/

    } while (true);

    if (!chosePreset) {
        choix = bc.readFavoritePreset();
    }
  //  allAct.activate(choix);
   // allAct.showChoices();

}

std::string game::selectPreset() {
    std::string choix;
    std::cout << "\nType Preset: ";
    std::cin >> choix;
    return choix;
}

void game::selectFavoritePreset() {
    std::string fav;
    std::cout << "\nType favorite Preset: ";
    std::cin >> fav;
    bc.updateFavoritePreset(fav);
}

void game::explainPresetChoice() {
    std::cout << "\nChoose your preset:\n\n";
    //allAct.showShortcuts();
    std::cout << "\n";
   // allAct.showActivities();
    std::cout << "Favorite Preset: " << bc.readFavoritePreset() << "\n\n";
    std::cout << "Type the associated character:\n\n";
    std::cout << "For example : abjdlm or ABJghDd or 0\n";
    std::cout << "Type P to choose preset\n";
    std::cout << "Type F to choose favorite Preset\n";
    std::cout << "Type S to Skip\n";
    std::cout << "Validate your input with Enter\n";
}

bool game::chronoPresetMenu(std::chrono::time_point<std::chrono::steady_clock> start, int duration, int& lastelapsed) {
    std::string textseconds = "Seconds left: ";
    int maxchar = 0;
    auto now = std::chrono::steady_clock::now();
    int elapsed = std::chrono::duration_cast<std::chrono::seconds>(now - start).count();
    int timeleft = (int)duration - elapsed;
    if (lastelapsed != elapsed) {
        std::string time = std::to_string(timeleft);
        int nbchar = (int)time.size();
        for (int j = 0; j < nbchar + textseconds.length(); ++j) {
            std::cout << "\b";
        }
        std::cout << textseconds;
        std::cout << time;
        lastelapsed = elapsed;
    }
    return timeleft < 0;
}


bool game::botEnd() {
   // return allAct.noActivityLeft();
    return false;
}

bool game::startBot()
{
    bc.setDim(dimX, dimY);
    if (!bc.initBotConfig())return false;
    initActivities();
    selectActivity();
    createpaths(templates);
    bc.startPlayingInstance();
    bc.waitBootDevice();
    std::cout << "Instance loaded\n";
    getAdbId();
    std::cout << "Loading templates\n";
    allAct.inte.loadAllTemplates();
    std::cout << "Templates loaded\n";
    return true;
}



























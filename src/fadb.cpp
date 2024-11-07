#include "fadb.h"

void input(const std::string& inputcmd, const std::string adbId) {
    runAdbLocalhostShell("input " + inputcmd,adbId);
}

void waitForDevice(const std::string adbId)
{
	runAdbLocalhost("wait-for-device", adbId);
}

void touch(int x, int y, const std::string adbId) {
    std::string x_str = std::to_string(x);
    std::string y_str = std::to_string(y);
    std::string command = "tap " + x_str + " " + y_str;
    input(command, adbId);
}


void swipe(int x1, int y1, int x2, int y2, int delayMilliseconds, const std::string adbId) {
    std::string x1_str = std::to_string(x1);
    std::string y1_str = std::to_string(y1);
    std::string x2_str = std::to_string(x2);
    std::string y2_str = std::to_string(y2);
    std::string delay_str = std::to_string(delayMilliseconds);
    std::string command = "swipe " + x1_str + " " + y1_str + " " + x2_str + " " + y2_str + " " + delay_str;
    input(command, adbId);
}

int getPidLogcat(const std::string adbId) {
    return getPid("logcat", adbId);
}

std::string runAdbLocalhostShell(const std::string& command, const std::string adbId)
{
    return runAdbLocalhost("shell " + command, adbId);
}


std::string runAdbLocalhost(const std::string& command, const std::string adbId)
{
    std::string fullCommand;
    if(!adbId.empty())fullCommand+="-s "+adbId+" ";
    fullCommand+=command;
    return runAdb(fullCommand);
}

std::string runAdb(const std::string& command)
{
    std::string fullCommand = "adb " + command;
    return receivePipe(fullCommand);
}

void getEmulatorDimensions(int& DIMX, int& DIMY, const std::string adbId) {
    std::string output = runAdbLocalhostShell("wm size",adbId);
    size_t xbegpos, xendpos, ybegpos, yendpos;
    std::string size = "size: ";
    xbegpos = output.find(size) + size.length();
    xendpos = output.find("x") - 1;
    ybegpos = output.find("x") + 1;
    yendpos = output.find('\n') - 1;
    if (xbegpos != std::string::npos && xendpos != std::string::npos && ybegpos != std::string::npos && yendpos != std::string::npos) {
        DIMX = stoi(output.substr(xbegpos, xendpos));
        DIMY = stoi(output.substr(ybegpos, yendpos));
    }
}

void launchPackage(std::string namepckg, const std::string adbId) {
    runAdbLocalhostShell("am start -n " + namepckg, adbId);
}
void quitPackage(std::string namepckg, const std::string adbId) {
    runAdbLocalhostShell("am force-stop " + namepckg, adbId);

}

void adbHome(const std::string adbId) {
    input("keyevent KEYCODE_HOME", adbId);
}
void adbBack(const std::string adbId) {
    input("keyevent KEYCODE_BACK", adbId);
}
void adbMenu(const std::string adbId) {
	input("keyevent KEYCODE_MENU",adbId);
}
void takeScreenshot(const std::string& filename, const std::string adbId) {
    std::string command = "exec-out screencap -p > ./assets/liveScreenshot/" + filename;
    runAdbLocalhost(command, adbId);
}

void screenshot(const std::string adbId) {
    takeScreenshot("screenshot.png", adbId);
}
std::string devices() {
    return runAdb("devices");
}

bool isDeviceConnected(const std::string adbId)
{
    std::string text = devices();
    std::string name = extractBetween(text, adbId, "\n");
    return name.find("device");
}

void reconnect()
{
	runAdb("reconnect");
}

void startServer()
{
	runAdb("start-server");
}

void connectLocalhost(const std::string adbId)
{
    runAdb("connect " + adbId);
}


void disconnectLocalhost(const std::string adbId) {
    runAdb("disconnect " + adbId);
}

void setResolution(int x, int y, int dpi, const std::string adbId)
{
    std::string command1 = "wm size " + std::to_string(x) + "x" + std::to_string(y);
    std::string command2 = "wm density " + std::to_string(dpi);
    runAdbLocalhostShell(command1, adbId);
    runAdbLocalhostShell(command2, adbId);
}


void killAdb() {
    runAdb("kill-server");
}

int getPid(const std::string& process, const std::string adbId)
{
    std::string command = "pidof " + process;
    std::string result = runAdbLocalhostShell(command, adbId);
    if (result.empty())return -1;
    else return stoi(result);
}

std::vector<int> getMultPid(const std::string& process, const std::string adbId)
{
    std::string command = "pidof " + process;
    std::string result = runAdbLocalhostShell(command, adbId);
    std::vector<int>allpid;
    std::string nb;
    if (!result.empty()) {
        for (int i = 0; i < result.size(); ++i) {
            if (result.at(i) != ' ' && result.at(i) != '\n') {
                nb.push_back(result.at(i));
            }
            else if (!nb.empty()) {
                allpid.push_back(std::stoi(nb));
                nb.clear();
                if (result.at(i) == '\n')break;
            }
        }
    }
    std::cout << "stop";
    return allpid;
}

std::string currentFocus(const std::string adbId)
{
    std::string command = "\"dumpsys window windows | grep mCurrentFocus\"";
    return runAdbLocalhostShell(command, adbId);
}

void killPid(int pid, const std::string adbId)
{
    runAdbLocalhostShell("kill " + std::to_string(pid), adbId);
}

void killLogcat(const std::string adbId)
{
    std::vector<int> pids = getMultPid("logcat", adbId);
    for (int i : pids) {
        std::cout << "Pid: " << i << std::endl;
        killPid(i, adbId);
    }
}


bool packageRunning(std::string package, const std::string adbId)
{
    int pid = getPid(package, adbId);
    if (pid != -1)return true;
    else return false;

}

bool onPackage(std::string package, const std::string adbId)
{
    std::string focus = currentFocus(adbId);
    return (focus.find(package) != std::string::npos);
}





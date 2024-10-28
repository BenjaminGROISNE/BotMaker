
#ifndef FADB_H
#define FADB_H
#define NOMINMAX
#include <Windows.h>
#include <iostream>
#include <array>
#include <memory>
#include <ctime>
#include <filesystem>
#include <atomic>
#include <chrono>
#include <thread>
#include <stdexcept>
#include <cstdio>
#include "sysCommands.h"
#include "TextManager.h"

std::string runAdbLocalhostShell(const std::string& command,const std::string adbId="");
std::string runAdbLocalhost(const std::string& command, const std::string adbId = "");
std::string runAdb(const std::string& command);
void input(const std::string& inputcmd,const std::string adbId = "");
void waitForDevice(const std::string adbId = "");
void touch(int x, int y, const std::string adbId = "");
void swipe(int x1, int y1, int x2, int y2, int delayMilliseconds, const std::string adbId = "");
void getEmulatorDimensions(int& DIMX, int& DIMY, const std::string adbId = "");
void launchPackage(std::string namepckg, const std::string adbId = "");
int  getPidLogcat(const std::string adbId = "");
void quitPackage(std::string namepckg, const std::string adbId = "");
void adbHome(const std::string adbId = "");
void adbBack(const std::string adbId = "");
void adbMenu(const std::string adbId = "");
std::string devices();
bool isDeviceConnected(const std::string adbId = "");
void reconnect();
void startServer();
void takeScreenshot(const std::string& filename, const std::string adbId = "");
void screenshot(const std::string adbId = "");
void connectLocalhost(const std::string adbId = "");
void disconnectLocalhost(const std::string adbId = "");
void setResolution(int x, int y, int dpi, const std::string adbId = "");
void killAdb();
int getPid(const std::string& processn, const std::string adbId = "");
std::vector<int> getMultPid(const std::string& process, const std::string adbId = "");
void killPid(int pid, const std::string adbId = "");
void killLogcat(const std::string adbId = "");
bool packageRunning(std::string package, const std::string adbId = "");
bool onPackage(std::string package, const std::string adbId = "");
std::string currentFocus(const std::string adbId = "");

#endif


#pragma once
#define NOMINMAX

#include <Windows.h>
#include <iostream>
#include <array>
#include <memory>
#include <ctime>
#include <vector>
#include <atomic>
#include <chrono>
#include <thread>
#include <stdexcept>
#include <cstdio>
#include <cstdlib>
#include <TlHelp32.h>
#include <direct.h>
#include <fstream>
#include <Netlistmgr.h>
#include <bitset>
#include <string>
#include <io.h>
#include <filesystem>

std::vector<std::string> returnAllDrivesLetter();
std::string moveAndExecute(const std::string& folder, const std::string command = "");
void moveAndStartProgram(const std::string& programFolder, const std::string& exeName,const std::string args="");
void startProgram(const std::string& programPath, const std::string args = "");
void killProgram(const std::string& exeName,const std::string args="");
void killProgramWindow(const std::string& exeName,const std::string& windowName);
std::string ExpandEnvironmentVariables(const std::string& inputPath);
std::string findPathFolder(const std::string& folder, const std::string& namefile);
bool findFileFolder(const std::string& folder, const std::string& namefile);
void writeText(const std::string& text, const std::string& filename);
bool pathExists(const std::string& path);
std::chrono::time_point<std::chrono::steady_clock> startChrono();
long int endChrono(const std::chrono::time_point<std::chrono::steady_clock>& startTime);

bool openPipe(const std::string& command, FILE*& pipe);
void closePipe(FILE*& pipe);
void doPipe(const std::string& command);
std::string receivePipe(const std::string& command);
std::string doPipeNoclose(const std::string& command);
bool findPipe(const std::string& command, const std::string& parameter);
bool findPipeTime(const std::string& command, const std::string& parameter, int seconds);
bool findPipeTimeNoclose(const std::string& command, const std::string& parameter, int seconds, FILE*& pipe);
void findPathFolderRec(std::filesystem::path folderpath, const std::string& namefile, std::string& correctpath, bool& found);
std::string findNameFolder(const std::string& folder);
std::string findNameAncestorFolder(const std::string& folder, int number);
std::string getParentDirectory();
void createpaths(std::string rootFolder);
void GetRelativeResolution(int& width, int& height);

void writeFilesDirectory(std::filesystem::directory_iterator path, std::ofstream& fileContent, std::ofstream& fileDeclaration);
void bringWindowToFront(HWND hwnd);




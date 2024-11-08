#pragma once
#include <string>
#include <iostream>
#include <fstream>
#include <vector>
#include <algorithm>
#include <filesystem>

enum WriteMode {
	PREPEND,
	APPEND,
	OVERWRITE
};

std::string getFileContent(const std::string& path);
std::string getCorrectPath(const std::string& path);
std::string appendToFolder(const std::string& folderName, const std::string& fileName);
std::ofstream getOfstreamFile(const std::string& path,WriteMode	method=PREPEND);
void writeToFile(const std::string& path, const std::string& content,WriteMode method= PREPEND);
void clearFile(const std::string& path);
std::string extractBetween(const std::string& fullContent, const std::string& begin, const std::string& end);
std::string extractLineContent(const std::string& fullContent, const std::string& lineParameter, const std::string& begin, const std::string& end);
std::string updateContent(std::string fullContent, std::string newContent, const std::string& begin, const std::string& end);
std::string getLine(const std::string& text, const std::string& lineParameter);
std::string getStringBefore(const std::string& text, const std::string& content);
std::string getStringUntil(const std::string& text, const size_t& pos);
std::string getStringStarting(const std::string& text, const size_t& pos);
std::string getStringUntil(const std::string& text, const std::string& content);
std::string getStringAfter(const std::string& text, const std::string& content);
size_t getPosAfter(const std::string& text, const std::string& content, const size_t& offset=0);
size_t getPosEnd(const std::string& text, const std::string& content, const size_t& offset=0);
std::string getStringStarting(const std::string& text, const std::string& content);
bool isBeforeStrict(const std::string& text, const std::string& before, const std::string& after);
bool isBefore(const std::string& text, const std::string& before, const std::string& after);
std::string getStringInsideBorders(const std::string& text, const std::string& open,const std::string&close);
std::string getStringBeforeLastMatch(const std::string& text, const std::string& match);
std::string reverseString(const std::string& text);
int countMatches(const std::string& text, const std::string& content);
int countMatchesBefore(const std::string& text, const std::string& before, const std::string& end);

void skipSpace(std::string& text);
bool beginsBySpace(const std::string& text);
bool beginsBySequence(const std::string& text, const std::string& sequence);
void skipAnySequence(std::string& text, const std::string& sequence);
void skipSequence(std::string& text, const std::string& sequence);
bool containsSequence(const std::string& text, const std::string& content, const size_t& offset=0);
size_t getSequencePos(const std::string& text, const std::string& seq, const size_t& offset = 0);
size_t getSequencePosR(const std::string& text, const std::string& seq, const size_t& offset=std::string::npos);
void printFileContent(const std::string& path);
std::string eraseLastMatch(const std::string& text, const std::string& content);
std::vector<std::string> getAllLines(const std::string& text, const std::string& lineParameter);
std::string removeContent(const std::string& text, const std::string& content);
bool to_bool(std::string const& s);
std::string removeCharacter(const std::string& text, const char c);
std::string removeCharacter(const std::string& text, const std::vector<char>& c);



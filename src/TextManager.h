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
std::string getContentBefore(const std::string& text, const std::string& content);
std::string getContentBefore(const std::string& text, const size_t& pos);
std::string getContentAfter(const std::string& text, const size_t& pos);
std::string getContentUntil(const std::string& text, const size_t& pos);
std::string getContentStarting(const std::string& text, const size_t& pos);
std::string getContentUntil(const std::string& text, const std::string& content);
std::string getContentAfter(const std::string& text, const std::string& content);
size_t getPosAfter(const std::string& text, const std::string& content);
std::string getContentStarting(const std::string& text, const std::string& content);
bool isBeforeStrict(const std::string& text, const std::string& before, const std::string& after);
bool isBefore(const std::string& text, const std::string& before, const std::string& after);
int countOccurrencesBefore(const std::string& text, const std::string& before, const std::string& end);
std::string getContentUntilEqual(const std::string& text, const std::string& open,const std::string&close);
std::string getContentUntilEqualUntil(const std::string& text, const std::string& open, const std::string& close, const std::string& stop);
std::string getContentUntilOutside(const std::string& text, const std::string& inside, const std::string& outside);
std::string getContentInsideUntil(const std::string& text, const std::string& inside, const std::string& outside,const std::string& stop);
std::string getContentBeforeLastOccurence(const std::string& text, const std::string& occurence);
std::string reverseString(const std::string& text);
int countMatches(const std::string& text, const std::string& content);
void skipSpace(std::string& text);
void skipSequence(std::string& text, const std::string& sequence);
int countOccurrences(const std::string& text, const std::string& content);
bool containsSequence(const std::string& text, const std::string& content, const size_t& offset=0);
size_t getSequencePos(const std::string& text, const std::string& seq, const size_t& offset = 0);
void printFileContent(std::string path);
std::string removeLastOccurence(const std::string& text, const std::string& content);
std::vector<std::string> getAllLines(const std::string& text, const std::string& lineParameter);
std::string removeContent(const std::string& text, const std::string& content);
bool to_bool(std::string const& s);
std::string removeCharacter(const std::string& text, const char c);
std::string removeCharacter(const std::string& text, const std::vector<char>& c);



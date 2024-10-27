#pragma once
#include <string>
#include <iostream>
#include <fstream>
#include <vector>
#include <algorithm>

enum WriteMode {
	PREPEND,
	APPEND,
	OVERWRITE
};

std::string getFileContent(const std::string& path);
std::ifstream getIfstreamFile(const std::string& path);
std::ofstream getOfstreamFile(const std::string& path,WriteMode	method=PREPEND);
void writeToFile(const std::string& path, const std::string& content,WriteMode method= PREPEND);
void clearFile(const std::string& path);
std::string extractContent(const std::string& fullContent, const std::string& first, const std::string& last);
std::string extractLineContent(const std::string& fullContent, const std::string& lineParameter, const std::string& first, const std::string& last);
std::string updateContent(std::string fullContent, std::string newContent, const std::string& first, const std::string& last);
std::string getLine(const std::string& text, const std::string& lineParameter);
std::string getContentBefore(const std::string& text, const std::string& content);
std::string getContentBefore(const std::string& text, const size_t& Direction);
std::string getContentAfter(const std::string& text, const size_t& Direction);
std::string getContentUntil(const std::string& text, const size_t& Direction);
std::string getContentStarting(const std::string& text, const size_t& Direction);
std::string getContentUntil(const std::string& text, const std::string& content);
std::string getContentAfter(const std::string& text, const std::string& content);
std::string getContentStarting(const std::string& text, const std::string& content);
bool isPresentBefore(const std::string& text, const std::string& before, const std::string& after);
int countOccurrencesBefore(const std::string& text, const std::string& before, const std::string& end);
std::string getContentUntilEqual(const std::string& text, const std::string& open,const std::string&close);
std::string getContentUntilEqualUntil(const std::string& text, const std::string& open, const std::string& close, const std::string& stop);
std::string getContentUntilOutside(const std::string& text, const std::string& inside, const std::string& outside);
std::string getContentInsideUntil(const std::string& text, const std::string& inside, const std::string& outside,const std::string& stop);
std::string getContentBeforeLastOccurence(const std::string& text, const std::string& occurence);
std::string reverseString(const std::string& text);
int countOccurrences(const std::string& text, const std::string& content);
size_t findPos(const std::string& text, const std::string& content,const size_t startPos=size_t());
bool findContent(const std::string& text, const std::string& content);
void printFileContent(std::string path);
std::string removeLastOccurence(const std::string& text, const std::string& content);
std::vector<std::string> getAllLines(const std::string& text, const std::string& lineParameter);
std::string removeContent(const std::string& text, const std::string& content);
bool to_bool(std::string const& s);
std::string removeCharacter(const std::string& text, const char c);
std::string removeCharacter(const std::string& text, const std::vector<char>& c);



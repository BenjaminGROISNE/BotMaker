#include "TextManager.h"


std::string getFileContent(const std::string& path) {
    std::string line;
    std::string text;
    std::ifstream file(path);
    if (file) {
        while (getline(file, line)) {
            text += line + "\n";
        }
    }
    else std::cerr << "Cannot read File" + path;
    file.close();
    return text;
}

std::string getCorrectPath(const std::string& path)
{
	return std::filesystem::path(path).string();
}

std::string appendToFolder(const std::string& folderName,const std::string& fileName)
{
    std::filesystem::path path(folderName);
	return (path / fileName).string();
}


std::ofstream getOfstreamFile(const std::string& path,WriteMode method)
{
    switch (method) {
    case PREPEND:
        return std::ofstream(path);
    case APPEND:
        return std::ofstream(path,std::ios_base::app);
    case OVERWRITE:
        return std::ofstream(path,std::ios_base::trunc);
    default:
        return std::ofstream(path);
        break;
    }
}

void writeToFile(const std::string& path, const std::string& content,WriteMode method)
{
	std::ofstream file = getOfstreamFile(path,method);
    if (file) file << content;
    else std::cerr << "Cannot write to File";
	file.close();
}

void clearFile(const std::string& path)
{
	std::ofstream file = getOfstreamFile(path);
    if (file) file.clear();
    else std::cout << "Cannot write to File";
	file.close();
}

std::string extractBetween(const std::string& text, const std::string& begin, const std::string& end)
{
	return getStringBefore(getStringAfter(text, begin), end);
}

std::size_t getOccurencePosBefore(const std::string& text, const std::string& begin, const std::string& end) {
    return text.rfind(begin, getSequencePos(text, end));
}

std::string extractBetweenReverse(const std::string& text, const std::string& begin, const std::string& end) {
    return getStringAfter(getStringBefore(text, end), begin);
}

std::string extractLineContent(const std::string& fullContent, const std::string& lineParameter, const std::string& begin, const std::string& end)
{
    return extractBetween(getLine(fullContent, lineParameter), begin, end);
}

std::string updateContent(std::string fullContent, std::string newContent, const std::string& begin, const std::string& end)
{
	return getStringBefore(fullContent, begin) + newContent + getStringAfter(fullContent, end);
}

std::size_t getFirstOccurencePosBefore(const std::string& text,const std::string& occ, const std::string& beforeParameter) {
	return text.rfind(occ, getSequencePos(text, beforeParameter));
}




std::string getLine(const std::string& text, const std::string& lineParameter)
{
	return extractBetweenReverse(text,"\n", lineParameter) + lineParameter+extractBetween(text, lineParameter,"\n");
}

std::string removeContent(const std::string& text, const std::string& content)
{
    std::string first = getStringBefore(text, content);
    std::string second = getStringAfter(text, content);
    return first + second;
}

std::string getStringBefore(const std::string& text, const std::string& content)
{
    size_t pos = getSequencePos(text, content);
    if (pos != std::string::npos) return text.substr(0, pos);
}

std::string getStringUntil(const std::string& text, const std::string& content)
{
    return getStringUntil(text, getPosAfter(text, content));
}

std::string getStringAfter(const std::string& text, const std::string& content)
{
    return getStringStarting(text, getPosAfter(text, content));
}

size_t getPosAfter(const std::string& text, const std::string& content,const size_t& offset)
{
    std::size_t pos = getSequencePos(text, content);
    return pos==std::string::npos ? text.size() : pos+content.size() ;
}
size_t getPosEnd(const std::string& text, const std::string& content, const size_t& offset)
{
    std::size_t pos = getSequencePos(text, content);
    return pos == std::string::npos ? text.size() : pos + content.size()-1;
}



std::string getStringStarting(const std::string& text, const std::string& content)
{
    return getStringStarting(text, getSequencePos(text, content));
}

//only compares the starting point of the two strings not the whole string
bool isBeforeStrict(const std::string& text, const std::string& before, const std::string& after)
{
    size_t pos1 = getSequencePos(text, before);
    size_t pos2 = getSequencePos(text, after);
	return pos1 != std::string::npos && pos1 < pos2;
}

//only compares the starting point of the two strings not the whole string
bool isBefore(const std::string& text, const std::string& before, const std::string& after)
{
    size_t pos1 = getSequencePos(text, before);
    size_t pos2 = getSequencePos(text, after);
    return pos1 != std::string::npos && pos1 <= pos2;
}



std::string getStringInsideBorders(const std::string& text, const std::string& open, const std::string& close) {
    int nbOpen = 0;
    int nbClose = 0;
    std::string copy(text);
    std::string save;
    size_t lastClosePos = std::string::npos;
    size_t firstOpenPos = getSequencePos(copy, open);
    if (firstOpenPos != std::string::npos) {
        while (true) {
            lastClosePos = getSequencePos(copy, close);
            if (lastClosePos != std::string::npos) {
                if (nbOpen == 0 && firstOpenPos > lastClosePos) {
                    firstOpenPos = 0;
                    lastClosePos = text.size();
                    break;
                }
                nbClose++;
                save = getStringUntil(copy, close);
                nbOpen += countMatches(save, open);
                skipSequence(copy, save);
                if (nbClose == nbOpen) {
                    firstOpenPos += open.length();
                    lastClosePos = getSequencePosR(text, copy)- close.length();
                    break;
                }
            }
            else break;
        }
    }
    return getStringStarting(getStringUntil(text, lastClosePos),firstOpenPos);
}

std::string getStringBeforeLastMatch(const std::string& text, const std::string& match)
{
    return getStringUntil(text, getSequencePosR(text, match));
}

std::string reverseString(const std::string& text) {
    return std::string(text.rbegin(), text.rend());
}

int countMatches(const std::string& text, const std::string& match)
{
    int count = 0;
    std::string temp = text;
    while (!temp.empty()) {
        if (containsSequence(temp, match))count++;
        skipSequence(temp, getStringUntil(temp, match));
    }
    return count;
}

int countMatchesBefore(const std::string& text, const std::string& match, const std::string& end)
{
    return countMatches(getStringBefore(text, end),match);
}



void skipSpace(std::string& text) {
    skipAnySequence(text, "\b\t\n\r ");
}

void skipAnySequence(std::string& text, const std::string& sequence)
{
    size_t pos = text.find_first_not_of(sequence);
    text.erase(0, pos);
}

void skipSequence(std::string& text, const std::string& sequence)
{
    size_t pos = text.find_first_of(sequence);
    if(pos==0) text.erase(0, getPosAfter(text,sequence));
}

std::string getStringUntil(const std::string& text, const size_t& pos)
{
    size_t size = text.length();
    if (size >= pos)size = pos;
    return text.substr(0, size);
}

std::string getStringStarting(const std::string& text, const size_t& pos)
{
    size_t start = 0;
    if (text.length() > pos)start = pos;
    return text.substr(start);
}

std::string tolower(const std::string& s) {
    std::string newString;
    for (char c : s) {
        newString.push_back(tolower(c));
    }
    return newString;
}

bool to_bool(const std::string& s) {
    return s == "1" || tolower(s)=="true";
}

bool containsSequence(const std::string& text, const std::string& seq, const size_t& offset)
{
    return text.find(seq, offset)!=std::string::npos;
}

size_t getSequencePos(const std::string& text, const std::string& seq, const size_t& offset)
{
    return text.find(seq, offset);
}

size_t getSequencePosR(const std::string& text, const std::string& seq, const size_t& offset)
{
    size_t pos = text.rfind(seq, offset);
    return pos==std::string::npos ? std::string::npos : pos;
}

void printFileContent(const std::string& path)
{
    std::cout << getFileContent(path);
}

std::string eraseLastMatch(const std::string& text, const std::string& content)
{
    return getStringUntil(text, getSequencePosR(text, content));
}

std::vector<std::string> getAllLines(const std::string& text, const std::string& lineParameter)
{
    std::vector<std::string> allLinesTab;
    std::string newtext = text;
    while (!newtext.empty()) {
        std::string line = getLine(newtext, lineParameter);
        allLinesTab.push_back(line);
        skipSequence(newtext, line);
    }
    return allLinesTab;
}

std::string removeCharacter(const std::string& text, const char c)
{
    std::string result;
    for (char l : text) {
        if (l != c) {
            result += l;
        }
    }
    return result;
}
std::string removeCharacter(const std::string& text, const std::vector<char>& cList)
{
    std::string temp = text;
    for (char c : cList) {
        temp = removeCharacter(temp, c);
    }
    return temp;
}
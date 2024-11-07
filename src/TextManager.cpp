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
	return getContentBefore(getContentAfter(text, begin), end);
}

std::size_t getOccurencePosBefore(const std::string& text, const std::string& begin, const std::string& end) {
    return text.rfind(begin, getSequencePos(text, end));
}

std::string extractBetweenReverse(const std::string& text, const std::string& begin, const std::string& end) {
    return getContentAfter(getContentBefore(text, end), begin);
}

std::string extractLineContent(const std::string& fullContent, const std::string& lineParameter, const std::string& begin, const std::string& end)
{
    return extractBetween(getLine(fullContent, lineParameter), begin, end);
}

std::string updateContent(std::string fullContent, std::string newContent, const std::string& begin, const std::string& end)
{
	return getContentBefore(fullContent, begin) + newContent + getContentAfter(fullContent, end);
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
    std::string first = getContentBefore(text, content);
    std::string second = getContentAfter(text, content);
    return first + second;
}

std::string getContentBefore(const std::string& text, const std::string& content)
{
    size_t pos = getSequencePos(text, content);
    if (pos != std::string::npos) return text.substr(0, pos);
}

std::string getContentUntil(const std::string& text, const std::string& content)
{
    return getContentUntil(text, getSequencePos(text, content));
}

std::string getContentAfter(const std::string& text, const std::string& content)
{
    return getContentAfter(text, getPosAfter(text, content));
}

size_t getPosAfter(const std::string& text, const std::string& content)
{
    return getSequencePos(text, content) + content.size();
}

std::string getContentStarting(const std::string& text, const std::string& content)
{
    return getContentStarting(text, getSequencePos(text, content));
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

std::string getContentUntilEqual(const std::string& text, const std::string& open, const std::string& close)
{
    size_t pos =-1;
    std::string newtext;
    int nbOpen = 1;
    int nbClose = 0;
    while (nbOpen != nbClose) {
		pos = getSequencePos(text, close,pos+1);
        if (pos != std::string::npos) {
            newtext = getContentUntil(text, pos+close.length());
			nbOpen = countMatches(newtext, open);
			nbClose = countMatches(newtext, close);
		}
        else {
			return "";
		}
	}
    return newtext;
}

std::string getContentUntilEqualUntil(const std::string& text, const std::string& open, const std::string& close, const std::string& stop)
{
    size_t pos = -1;
    std::string newtext;
    int nbOpen = 1;
    int nbClose = 0;
    while (nbOpen != nbClose) {
        pos = getSequencePos(text, stop, pos+1);
        if (pos != std::string::npos) {
            newtext = getContentUntil(text, pos+stop.length());
            nbOpen = countMatches(newtext, open);
            nbClose = countMatches(newtext, close);
        }
        else {
            return "";
        }
    }
    return newtext;
}
std::string getContentUntilOutside(const std::string& text, const std::string& inside, const std::string& outside)
{
    size_t pos = -1;
    std::string newtext;
    int nbOpen = 0;
    int nbClose = 0;
    while (nbOpen+1 != nbClose) {
        pos = getSequencePos(text, outside, pos+1);
        if (pos != std::string::npos) {
            newtext = getContentUntil(text, pos+outside.length());
            nbOpen = countMatches(newtext, inside);
            nbClose = countMatches(newtext, outside);
        }
        else {
            return "";
        }
    }
    return newtext;
}

std::string getContentInsideUntil(const std::string& text, const std::string& inside, const std::string& outside, const std::string& stop)
{
    size_t pos = -1;
    std::string newtext;
    int nbOpen = 0;
    int nbClose = 0;
    while (nbOpen != nbClose+1) {
        pos = getSequencePos(text, stop, pos+1);
        if (pos != std::string::npos) {
            newtext = getContentUntil(text, pos+stop.length());
            nbOpen = countMatches(newtext, inside);
            nbClose = countMatches(newtext, outside);
        }
        else {
            return "";
        }
    }
    return newtext;
}

std::string getContentBeforeLastOccurence(const std::string& text, const std::string& occurence)
{
    size_t pos = text.find_last_of(occurence)-occurence.length()+1;
    if (pos != std::string::npos) {
		return text.substr(0, pos);
	}
	else return "";
}

std::string reverseString(const std::string& text)
{
    std::string newtext = text;
    std::reverse(newtext.begin(), newtext.end());
    return newtext;
}

int countMatches(const std::string& text, const std::string& match)
{
    int count = 0;
    std::string temp = text;
    while (!temp.empty()) {
        if (containsSequence(temp, match))count++;
        skipSequence(temp, getContentUntil(temp, match));
    }
    return count;
}



void skipSpace(std::string& text) {
	skipSequence(text, "\b\t\n\r ");
}

void skipSequence(std::string& text, const std::string& sequence)
{
    size_t pos = text.find_first_not_of(sequence);
    if (pos != std::string::npos) {
        text.erase(0, pos);
    }
}

std::string getContentBefore(const std::string& text, const size_t& pos)
{
    size_t size = text.length();
    if (size >= pos - 1) size = pos - 1;
    return text.substr(0, size);
}

std::string getContentUntil(const std::string& text, const size_t& pos)
{
    size_t size = text.length();
    if (size >= pos)size = pos;
    return text.substr(0, size);
}

std::string getContentAfter(const std::string& text, const size_t& pos)
{
	size_t start = 0;
    if (text.length() > pos+1)start = pos+1;
	return text.substr(start);
}

std::string getContentStarting(const std::string& text, const size_t& pos)
{
    size_t start = 0;
    if (text.length() > pos)start = pos;
    return text.substr(start);
}

bool to_bool(const std::string& s) {
    return s == "1" || s=="true";
}

bool containsSequence(const std::string& text, const std::string& seq, const size_t& offset)
{
    return text.find(seq, offset)!=std::string::npos;
}

size_t getSequencePos(const std::string& text, const std::string& seq, const size_t& offset)
{
    return text.find(seq, offset);
}

void printFileContent(std::string path)
{
    std::cout << getFileContent(path);
}

std::string removeLastOccurence(const std::string& text, const std::string& content)
{
    size_t pos =text.find_last_of(content)-content.length()+1;
    if (pos != std::string::npos) {
		return text.substr(0, pos);
	}
}

std::vector<std::string> getAllLines(const std::string& text, const std::string& lineParameter)
{
    std::vector<std::string> allocc;
    size_t pos = 0;
    size_t endPos = 0;
    std::string newtext = text;
    while (pos != std::string::npos) {
        pos = getSequencePos(newtext, lineParameter);
        if (pos != std::string::npos) {
            newtext = getContentStarting(newtext, pos);
            std::string line = getLine(newtext, lineParameter);
            allocc.push_back(line);
        }
    }
    return allocc;
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
#include "TextManager.h"


std::string getFileContent(const std::string& path) {
    std::string line;
    std::string text;
    std::ifstream file=getIfstreamFile(path);
    if (file) {
        while (getline(file, line)) {
            text += line + "\n";
        }
    }
    else {
        std::cout << "Cannot read File";
    }
    file.close();
    return text;
}

std::ifstream getIfstreamFile(const std::string& path)
{
    return std::ifstream(path);
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
    if (file) {
		file << content;
	}
    else {
		std::cout << "Cannot write to File";
	}
	file.close();
}

void clearFile(const std::string& path)
{
	std::ofstream file = getOfstreamFile(path);
    if (file) {
		file << "";
	}
    else {
		std::cout << "Cannot write to File";
	}
	file.close();
}

std::string extractContent(const std::string& fullContent, const std::string& first, const std::string& last)
{
    size_t pos, endpos;
    pos = findPos(fullContent, first) + first.length();
    if (pos != std::string::npos) {
        endpos = findPos(fullContent,last,pos);
        if (endpos != std::string::npos) {
            return fullContent.substr(pos, endpos - pos);
        }
    }
    return "";
}

std::string extractLineContent(const std::string& fullContent, const std::string& lineParameter, const std::string& first, const std::string& last)
{
    return extractContent(getLine(fullContent, lineParameter), first, last);
}

std::string updateContent(std::string fullContent, std::string newContent, const std::string& first, const std::string& last)
{
    size_t pos, endpos;
    std::string firstpart, secondpart;
    pos = findPos(fullContent,first)+first.length();
    if (pos != std::string::npos) {
        endpos =findPos(fullContent,last,pos);
        if (endpos != std::string::npos) {
            firstpart = fullContent.substr(0, pos);
            secondpart = fullContent.substr(endpos, std::string::npos);
            return firstpart + newContent + secondpart;
        }
    }
    return "";
}

std::string getLine(const std::string& text, const std::string& lineParameter)//doesn't return before lineParameter
{
    size_t pos = findPos(text, lineParameter);
    if (pos != std::string::npos) {
        size_t endPos = findPos(text,"\n",pos);
        if (endPos != std::string::npos) {
            std::string result = text.substr(pos, endPos - pos);
            return result;
        }
    }
    return "";
}

std::string removeContent(const std::string& text, const std::string& content)
{
    std::string first = getContentBefore(text, content);
    std::string second = getContentAfter(text, content);
    return first + second;
}

std::string getContentBefore(const std::string& text, const std::string& content)
{
    size_t pos = findPos(text, content);
    if (pos != std::string::npos) {
        return text.substr(0, pos);
    }
    return "";
}

std::string getContentUntil(const std::string& text, const std::string& content)
{
    return getContentUntil(text, findPos(text, content));
}

std::string getContentAfter(const std::string& text, const std::string& content)
{
    return getContentAfter(text, findPos(text, content)+content.size());
}

std::string getContentStarting(const std::string& text, const std::string& content)
{
    size_t pos = findPos(text,content);
    return getContentStarting(text, pos);
}

bool isPresentBefore(const std::string& text, const std::string& before, const std::string& after)
{
    size_t pos1 = findPos(text, before);
    size_t pos2 = findPos(text, after);
    if ( pos1 != std::string::npos) {
		return pos1 < pos2;
	}
    return false;
}

int countOccurrencesBefore(const std::string& text, const std::string& before, const std::string& end)
{
	size_t posEnd= findPos(text, end);
    if (posEnd != std::string::npos) {
        std::string newtext = getContentUntil(text, posEnd);
        return countOccurrences(newtext, before);
    }
	return 0;
}

std::string getContentUntilEqual(const std::string& text, const std::string& open, const std::string& close)
{
    size_t pos =-1;
    std::string newtext;
    int nbOpen = 1;
    int nbClose = 0;
    while (nbOpen != nbClose) {
		pos = findPos(text, close,pos+1);
        if (pos != std::string::npos) {
            newtext = getContentUntil(text, pos+close.length());
			nbOpen = countOccurrences(newtext, open);
			nbClose = countOccurrences(newtext, close);
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
        pos = findPos(text, stop, pos+1);
        if (pos != std::string::npos) {
            newtext = getContentUntil(text, pos+stop.length());
            nbOpen = countOccurrences(newtext, open);
            nbClose = countOccurrences(newtext, close);
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
        pos = findPos(text, outside, pos+1);
        if (pos != std::string::npos) {
            newtext = getContentUntil(text, pos+outside.length());
            nbOpen = countOccurrences(newtext, inside);
            nbClose = countOccurrences(newtext, outside);
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
        pos = findPos(text, stop, pos+1);
        if (pos != std::string::npos) {
            newtext = getContentUntil(text, pos+stop.length());
            nbOpen = countOccurrences(newtext, inside);
            nbClose = countOccurrences(newtext, outside);
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

int countOccurrences(const std::string& text, const std::string& content)
{
    int count = 0;
    std::string::const_iterator iter = text.begin();

    while (iter != text.end()) {
        iter = std::search(iter, text.end(), content.begin(), content.end());
        if (iter != text.end()) {
            ++count;
            iter += content.size();
        }
    }

    return count;
}

std::string getContentBefore(const std::string& text, const size_t& pos)
{
    if (text.length() >= pos-1) {
		return text.substr(0, pos-1);
    }
    else return "";
}

std::string getContentUntil(const std::string& text, const size_t& pos)
{
    if (text.length() >= pos) {
		return text.substr(0, pos);
	}
    else return "";
}

std::string getContentAfter(const std::string& text, const size_t& pos)
{
    if (text.length() >= pos) {
		return text.substr(pos, std::string::npos);
	}
	else return "";
}

bool to_bool(const std::string& s) {
    return s == "1" || s=="true";
}

std::string getContentStarting(const std::string& text, const size_t& pos)
{
    if (text.length() >= pos) {
		return text.substr(pos, std::string::npos);
	}
}

size_t findPos(const std::string& text, const std::string& content,const size_t startPos) {
    return text.find(content,startPos);
}

bool findContent(const std::string& text, const std::string& content)
{
    return findPos(text,content)!=std::string::npos;
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
        pos = findPos(newtext, lineParameter);
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
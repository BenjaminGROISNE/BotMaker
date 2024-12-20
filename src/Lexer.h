#pragma once
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include "Token.h"



class Lexer {
public:
	Lexer();
	Lexer(const std::string& text);
	std::string nameToken;
	std::string totalContent;
	std::string updatedContent;
	std::shared_ptr<Token> getToken(const std::string& text);
	std::shared_ptr<Token> getToken(const TokenVALUE& tValue, const std::string& text);
	void extractStringLiteral(std::string& newText, std::string& nextTokenString);
	std::vector<std::shared_ptr<Token>>  extractTokens(const std::string& text);
	static TokenVALUE getTokenValue(const std::string& text);
	std::string getNextPunctuationToken(const std::string& str);
	std::string getNextTokenString(const std::string& text);
	void skipTokenString(std::string& text, const std::string tokenText);
	void skipStringLiteral(std::string& text, const std::string tokenText);
	TokenVALUE getNextTokenValue(const std::string& text);
	std::string showAllTokens(std::vector<std::shared_ptr<Token>>);
protected:
	std::vector<std::shared_ptr<Token>> listTokens;
};
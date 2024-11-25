#pragma once

#include <iostream>
#include <fstream>
#include <string>
#include <memory>
#include <vector>
#include "Interactions.h"
#include "tags.h"
#include "Lexer.h"

enum class CS{TOKEN,SYNTAX,TAG};

class Interpretor {

	public:

	Interpretor();
	Interpretor(const std::string& folder);
	~Interpretor();
	std::shared_ptr<Tag> readActivityFile(const std::string& ActivityName);
	std::string copyActivity(const std::string& ActivityName);	
	IteratorList<Token> getTokens(std::string& text);
	std::shared_ptr<Token> executeTokens(IteratorList<Token>& tl);
	std::shared_ptr<Tag> executeTags(std::shared_ptr<Token> mainToken);

	std::shared_ptr<Token> compileTokens(std::string& text);
	std::shared_ptr<Tag> compileTags(std::string& text);
	void doUnitTests();
	std::string ActivityFolder;
};


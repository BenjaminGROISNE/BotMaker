#pragma once

#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include "Interactions.h"
#include "tags.h"
#include "Lexer.h"

class Interpretor {

	public:

	Interpretor();
	Interpretor(const std::string& folder);
	~Interpretor();
	void readActivityFile(const std::string& ActivityName);
	std::string copyActivity(const std::string& ActivityName);
	std::shared_ptr<Tag> getActivityTag();
	void compileScript(const std::string& text);
	std::shared_ptr<MainToken> mainToken;
	std::shared_ptr<MainTag> mainTag;
	std::shared_ptr<Lexer> mainStack;
	UserVariables uv;
	std::shared_ptr<TokenResult> tr;
	IteratorList<Token> tl;
	Lexer lex;
	std::string ActivityFolder;
};


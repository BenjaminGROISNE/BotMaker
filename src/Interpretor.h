#pragma once

#include <iostream>
#include <fstream>
#include <string>
#include <memory>
#include <vector>
#include "Interactions.h"
#include "tags.h"
#include "Lexer.h"


class Interpretor {

	public:

	Interpretor();
	Interpretor(const std::string& folder);
	~Interpretor();
	std::shared_ptr<Tag> readActivityFile(const std::string& ActivityName);
	std::string copyActivity(const std::string& ActivityName);	
	IteratorList<Token> getTokens(std::string& text);
	std::shared_ptr<Token> executeTokens(IteratorList<Token>& tl,TokenResult& tRes);
	std::shared_ptr<Tag> executeTags(std::shared_ptr<Token> mainToken);

	std::shared_ptr<Token> compileTokens(std::string& text, TokenResult& tRes);
	std::shared_ptr<Tag> compileTags(std::string& text);
	void doUnitTests();
	bool unitTest(std::string& text, TokenResult& tRes);
	std::string ActivityFolder;

	//Unit tests
	// 
	//UPKTokens
	bool intTest();
	bool floatTest();
	bool stringTest();
	bool boolTest();
	bool andTest();
	bool printTest();
	bool orTest();
	bool notTest();
	bool coordTest();
	bool zoneTest();
	bool directionTest();
	
	//FlowCKToken
	bool loopTest();
	bool ifTest();
	bool elseTest();
	bool elifTest();
	bool doloopTest();

	//PKToken
	bool waitTest();

	bool compareTest();

};


#include "Interpretor.h"

IteratorList<Token> Interpretor::getTokens(std::string& text)
{
	Lexer lex;
	return lex.extractTokens(text);
}

std::shared_ptr<Token> Interpretor::executeTokens(IteratorList<Token>& tl)
{
	std::shared_ptr<Token> mainToken;
	std::shared_ptr<Tag> mainTag;
	TokenResult tr;
	if (!tl.empty()) {
		mainToken = tl.getFirst();
		if (tl.size() > 1) {
			if (mainToken)mainToken->addTokens(tl, tr);
		}
	}
	return mainToken;
}

std::shared_ptr<Tag> Interpretor::executeTags(std::shared_ptr<Token> mainToken)
{
	if (mainToken)return mainToken->execute();
}

std::shared_ptr<Token> Interpretor::compileTokens(std::string& text)
{
	auto listTokens = getTokens(text);
	return executeTokens(listTokens);
}

std::shared_ptr<Tag> Interpretor::compileTags(std::string& text)
{
	auto listTokens = getTokens(text);
	auto compiledTokens= executeTokens(listTokens);
	return executeTags(compiledTokens);
}






Interpretor::Interpretor()
{
}

Interpretor::Interpretor(const std::string& folder)
{
	ActivityFolder = folder; 
}

Interpretor::~Interpretor()
{ 
}

void Interpretor::doUnitTests()
{
	std::string intToken1 = "int(3)";
	auto rInt1=compileTokens(intToken1);
	rInt1->showTokenTree(0);	
	std::cout << "\n";
	std::string intToken2= "int()";
	auto rInt2=compileTokens(intToken2);
	rInt2->showTokenTree(0);
	std::cout << "\n";
	std::string stringToken1 = "string(string(\"huhu\"))";
	auto rString1 = compileTokens(stringToken1);
	rString1->showTokenTree(0);
	std::cout << "\n";
	std::string stringToken2 = "string()";
	auto rString2 = compileTokens(stringToken2);
	rString2->showTokenTree(0);
	std::cout << "\n";

	std::string ct1 = "coord()";
	auto rc1 = compileTokens(ct1);
	rc1->showTokenTree(0);
	std::cout << "\n";
	std::string ct2 = "coord(int(5),6)";
	auto rc2 = compileTokens(ct2);
	rc2->showTokenTree(0);
	std::cout << "\n";

	std::string zt1 = "zone()";
	auto zc1 = compileTokens(zt1);
	zc1->showTokenTree(0);
	std::cout << "\n";

	std::string zt2 = "zone(zone(coord(3,int(5)),coord()))";
	auto zc2 = compileTokens(zt2);
	zc2->showTokenTree(0);
	std::cout << "\n";

	std::string bt1 = "bool()";
	auto bc1 = compileTokens(bt1);
	bc1->showTokenTree(0);
	std::cout << "\n";

	std::string bt2 = "bool(bool(true))";
	auto bc2 = compileTokens(bt2);
	bc2->showTokenTree(0);
	std::cout << "\n";

	std::string andt1 = "and()";
	auto andc1 = compileTokens(andt1);
	andc1->showTokenTree(0);
	std::cout << "\n";

	std::string andt2 = "and(and(),and(true,and(false,true)))";
	auto andc2 = compileTokens(andt2);
	andc2->showTokenTree(0);
	std::cout << "\n";

	std::string ort1 = "or()";
	auto orc1 = compileTokens(ort1);
	orc1->showTokenTree(0);
	std::cout << "\n";

	std::string ort2 = "or(or(),or(true,or(false,true)))";
	auto orc2 = compileTokens(ort2);
	orc2->showTokenTree(0);
	std::cout << "\n";

	volatile int stop = 0;
}

std::shared_ptr<Tag> Interpretor::readActivityFile(const std::string& ActivityName)
{
	auto path = appendToFolder(ActivityFolder, ActivityName);
	auto text = copyActivity(path);
	return compileTags(text);
}

std::string Interpretor::copyActivity(const std::string& ActivityPath)
{
	return getFileContent(ActivityPath);
}





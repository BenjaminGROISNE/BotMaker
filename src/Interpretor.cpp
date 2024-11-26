#include "Interpretor.h"

IteratorList<Token> Interpretor::getTokens(std::string& text)
{
	Lexer lex;
	return lex.extractTokens(text);
}

std::shared_ptr<Token> Interpretor::executeTokens(IteratorList<Token>& tl,TokenResult& tRes)
{
	std::shared_ptr<Token> mainToken;
	if (!tl.empty()) {
		mainToken = tl.getFirst();
		if (mainToken)mainToken->addTokens(tl, tRes);
	}
	return mainToken;
}

std::shared_ptr<Tag> Interpretor::executeTags(std::shared_ptr<Token> mainToken)
{
	if (mainToken)return mainToken->execute();
}

std::shared_ptr<Token> Interpretor::compileTokens(std::string& text, TokenResult& tRes)
{
	auto listTokens = getTokens(text);
	return executeTokens(listTokens, tRes);
}

std::shared_ptr<Tag> Interpretor::compileTags(std::string& text)
{
	TokenResult tRes;
	auto listTokens = getTokens(text);
	auto compiledTokens= executeTokens(listTokens, tRes);
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
	assert(intTest());
	assert(floatTest());
	assert(boolTest());
	assert(andTest());
	assert(orTest());
	assert(coordTest());
	assert(zoneTest());
	assert(directionTest());
	assert(printTest());
	assert(loopTest());
	assert(ifTest());
	assert(doloopTest());
	assert(elifTest());
	volatile int stop = 0;
}

bool Interpretor::unitTest(std::string& text,TokenResult&tRes){
	std::cout << "Base text: "+text+"  \n  ";
	auto comp1 = compileTokens(text, tRes);
	comp1->showTokenTree(0);
	std::cout << "\n";
	return tRes.success();
}

bool Interpretor::intTest()
{
	TokenResult tRes;
	std::string str1 = "int(3)";
	unitTest(str1, tRes);
	std::string str2 = "int()";
	unitTest(str2, tRes);
	return tRes.success();
}

bool Interpretor::floatTest()
{
	TokenResult tRes;
	std::string str1 = "float(4)";
	unitTest(str1, tRes);
	std::string str2 = "float()";
	unitTest(str2, tRes);
	return tRes.success();
}


bool Interpretor::stringTest()
{
	TokenResult tRes;
	std::string str1 = "string(string(\"coucou\"))";
	unitTest(str1, tRes);
	std::string str2 = "string()";
	unitTest(str2, tRes);
	return tRes.success();
}
bool Interpretor::boolTest()
{
	TokenResult tRes;
	std::string str1 = "bool()";
	unitTest(str1, tRes);
	std::string str2 = "bool(bool(true))";
	unitTest(str2, tRes);
	return tRes.success();
}

bool Interpretor::andTest()
{
	TokenResult tRes;
	std::string str1 = "and()";
	unitTest(str1, tRes);
	std::string str2 = "and(and(),and(true,and(false,true)))";
	unitTest(str2, tRes);
	return tRes.success();
}
bool Interpretor::printTest()
{
	TokenResult tRes;
	std::string str1 = "print()";
	unitTest(str1, tRes);
	std::string str2 = "print(\"ezezd\")";
	unitTest(str2, tRes);
	std::string str3 = "print(\"a\",\"b\",\"c\")";
	unitTest(str3, tRes);
	return tRes.success();
}

bool Interpretor::orTest()
{
	TokenResult tRes;
	std::string str1 = "or()";
	unitTest(str1, tRes);
	std::string str2 = "or(or(),or(true,or(false,true)))";
	unitTest(str2, tRes);
	return tRes.success();
}

bool Interpretor::notTest()
{
	TokenResult tRes;
	std::string str1 = "not()";
	unitTest(str1, tRes);
	std::string str2 = "not(not(true))";
	unitTest(str2, tRes);
	return tRes.success();
}

bool Interpretor::coordTest()
{

	TokenResult tRes;
	std::string str1 = "coord()";
	unitTest(str1, tRes);
	std::string str2 = "coord(int(5),6)";
	unitTest(str2, tRes);
	return tRes.success();
}

bool Interpretor::zoneTest()
{
	TokenResult tRes;
	std::string str1 = "zone()";
	unitTest(str1, tRes);
	std::string str2 = "zone(zone(coord(3,5),coord(1,1)))";
	unitTest(str2, tRes);
	return tRes.success();
}

bool Interpretor::directionTest()
{
	TokenResult tRes;
	std::string str1 = "direction()";
	unitTest(str1, tRes);
	std::string str2 = "direction(direction(NORTH))";
	unitTest(str2, tRes);
	std::string str3 = "direction(SOUTH)";
	unitTest(str3, tRes);
	std::string str4 = "direction(SOUTHW)";
	unitTest(str4, tRes);
	std::string str5 = "direction(SOUTHE)";
	unitTest(str5, tRes);
	std::string str6 = "direction(NORTHE)";
	unitTest(str6, tRes);
	std::string str7 = "direction(NORTHW)";
	unitTest(str7, tRes);
	return tRes.success();
}

bool Interpretor::loopTest()
{
	TokenResult tRes;
	std::string str1 = "loop()";
	assert(!unitTest(str1, tRes));
	TokenResult tRes2;
	std::string str2 = "loop(true){and(or(true,false))print(\"cee\") }";
	unitTest(str2, tRes2);
	return !tRes.success()&& tRes2.success();
}

bool Interpretor::ifTest()
{
	TokenResult tRes;
	std::string str1 = "if()";
	assert(!unitTest(str1, tRes));
	TokenResult tRes2;
	std::string str2 = "if(false){and(or(true,false))print(\"cee\") }";
	unitTest(str2, tRes2);
	return !tRes.success() && tRes2.success();
}

bool Interpretor::doloopTest()
{
	TokenResult tRes;
	std::string str1 = "doloop()";
	assert(!unitTest(str1, tRes));
	TokenResult tRes2;
	std::string str2 = "doloop(false){and(or(true,false))print(\"cee\") }";
	unitTest(str2, tRes2);
	return !tRes.success() && tRes2.success();
}
bool Interpretor::elifTest()
{
	TokenResult tRes;
	std::string str1 = "elif()";
	assert(!unitTest(str1, tRes));
	TokenResult tRes2;
	std::string str2 = "elif(false){and(or(true,false))print(\"cee\") }";
	unitTest(str2, tRes2);
	return !tRes.success() && tRes2.success();
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





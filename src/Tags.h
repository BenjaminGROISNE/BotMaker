#pragma once

#include <iostream>
#include <string>
#include <vector>
#include "Units.h"
#include <thread>
#include <typeinfo>
#include <algorithm>

bool isTokenString(const std::string& text);
bool isKeywordString(const std::string& text);
bool isLiteralString(const std::string& text);
bool isPunctuationString(const std::string& text);
#define TOKEN_DEF(var,str) static const std::string var = str;
#define TOKEN_CAT(catName) static const std::vector<std::string> catName
// Punctuation Tokens string
TOKEN_DEF(commaP, ",");
TOKEN_DEF(openParenthesisP, "(");
TOKEN_DEF(closeParenthesisP, ")");
TOKEN_DEF(openBracketsP, "{");
TOKEN_DEF(closeBracketsP, "}");
TOKEN_DEF(openAngleBracketsP, "<");
TOKEN_DEF(closeAngleBracketsP, ">");
TOKEN_DEF(quotation, "\"");
TOKEN_DEF(whitespace, " ");
TOKEN_DEF(tabulation, "\t");
TOKEN_DEF(backspace, "\b");
TOKEN_DEF(newline, "\n");
TOKEN_DEF(carriagereturn, "\r");
TOKEN_DEF(allPunctuations, " <>\",(){}\t\b\n\r");

TOKEN_CAT(allPunctuationsTokensStrings)= { openAngleBracketsP, closeAngleBracketsP, quotation, carriagereturn, newline, whitespace, tabulation, backspace, 
commaP, openParenthesisP, closeParenthesisP, openBracketsP, closeBracketsP };

// Literal Tokens string
TOKEN_DEF(trueL, "true");
TOKEN_DEF(falseL, "false");
TOKEN_DEF(secondL, "SECOND");
TOKEN_DEF(millisecondL, "MILLISECOND");
TOKEN_DEF(minuteL, "MINUTE");
TOKEN_DEF(northL, "NORTH");
TOKEN_DEF(southL, "SOUTH");
TOKEN_DEF(northwL, "NORTHW");
TOKEN_DEF(northeL, "NORTHE");
TOKEN_DEF(southwL, "SOUTHW");
TOKEN_DEF(southeL, "SOUTHE");
TOKEN_DEF(intL, "INT");
TOKEN_DEF(floatL, "FLOAT");
TOKEN_DEF(coordL, "COORD");
TOKEN_DEF(zoneL, "ZONE");
TOKEN_DEF(boolL, "BOOL");
TOKEN_DEF(timetypeL, "TIMETYPE");
TOKEN_DEF(directionL, "DIRECTION");
TOKEN_DEF(stringL, "STRING");
TOKEN_DEF(datatypeL, "DATATYPE");
TOKEN_DEF(comparetypeL, "COMPARETYPE");
TOKEN_DEF(greaterL, "GREATER");
TOKEN_DEF(lesserL, "LESSER");
TOKEN_DEF(equalL, "EQUAL");
TOKEN_DEF(notequalL, "NOTEQUAL");
TOKEN_DEF(greaterequalL, "GREATEREQUAL");
TOKEN_DEF(lesserequalL, "LESSEREQUAL");

TOKEN_CAT(allLiteralsTokensStrings)= { trueL, falseL, secondL, millisecondL, minuteL, northL, southL, northwL, northeL, southwL, southeL, intL, 
floatL, coordL, zoneL, boolL, timetypeL, directionL, stringL, datatypeL };

// Keyword Tokens string
TOKEN_DEF(storeK, "store");
TOKEN_DEF(clickK, "click");
TOKEN_DEF(swipeK, "swipe");
TOKEN_DEF(waitK, "wait");
TOKEN_DEF(ifK, "if");
TOKEN_DEF(elseK, "else");
TOKEN_DEF(elifK, "elif");
TOKEN_DEF(notK, "not");
TOKEN_DEF(floatK, "float");
TOKEN_DEF(intK, "int");
TOKEN_DEF(coordK, "coord");
TOKEN_DEF(zoneK, "zone");
TOKEN_DEF(andK, "and");
TOKEN_DEF(orK, "or");
TOKEN_DEF(loopK, "loop");
TOKEN_DEF(listK, "list");
TOKEN_DEF(stringK, "string");
TOKEN_DEF(directionK, "direction");
TOKEN_DEF(findswipeK, "findswipe");
TOKEN_DEF(findclickK, "findclick");
TOKEN_DEF(findK, "find");
TOKEN_DEF(breakK, "break");
TOKEN_DEF(continueK, "continue");
TOKEN_DEF(doloopK, "doloop");
TOKEN_DEF(functionK, "function");
TOKEN_DEF(switchK, "switch");
TOKEN_DEF(caseK, "case");
TOKEN_DEF(defaultK, "default");
TOKEN_DEF(compareK, "compare");
TOKEN_DEF(mainK, "main");
TOKEN_DEF(boolK, "bool");
TOKEN_DEF(printK, "print");
TOKEN_DEF(returnK, "return");
TOKEN_DEF(voidK, "void");
TOKEN_DEF(addK, "add");
TOKEN_DEF(subK, "sub");
TOKEN_DEF(multK, "mult");

TOKEN_CAT(allKeywordsTokensString)= { storeK, clickK, swipeK, waitK, ifK, 
elseK, elifK, notK, floatK, intK, coordK, zoneK, andK, orK, loopK, listK, stringK, 
directionK, findswipeK, findclickK, findK, breakK, continueK, doloopK,
functionK, switchK, caseK, defaultK, compareK, mainK, boolK, printK, returnK, voidK };

// Concatenate all token vectors
static const std::vector<std::string> allTokensStrings = [] {
	std::vector<std::string> result;

	result.insert(result.end(), allKeywordsTokensString.begin(), allKeywordsTokensString.end());
	result.insert(result.end(), allPunctuationsTokensStrings.begin(), allPunctuationsTokensStrings.end());
	result.insert(result.end(), allLiteralsTokensStrings.begin(), allLiteralsTokensStrings.end());

	return result;
	}();

enum class DataType {
	NONE, COORD, ZONE, STRING, DIRECTION, FLOAT, INT, BOOL, TIMETYPE, COMPARETYPE, DATATYPE, IDENTIFIER
};

enum class TokenVALUE {
	NOT, TOKEN, UNKNOWN, QUOTATION, OPENANGLEBRACKETS, CLOSEANGLEBRACKETS, FLOW, COMMA, SEMICOLON, NUMERIC, IDENTIFIER,
	CLOSEBRACKETS, OPENBRACKETS, OPENPARENTHESIS, CLOSEPARENTHESIS, STRINGLITERAL, TRUELITERAL, FALSELITERAL, SECOND, WHITESPACE,
	MINUTE, MILLISECOND, INTEGER, WAIT, FLOAT, BOOL, AND, OR, COMPARE, STRING, COORD, DIRECTION, ZONE, LIST, IF, LOOP, DOLOOP,
	SWITCH, DEFAULT, ELSE, ELIF, BREAK, CONTINUE, CASE, STORE, MAIN, PRINT, NORTH, SOUTH, EAST, WEST, NORTHW, NORTHE, SOUTHW, SOUTHE,
	TEMPLATE, STRINGTYPE, INTTYPE, FLOATTYPE, COORDTYPE, ZONETYPE, BOOLTYPE, DIRECTIONTYPE, TIMETYPE, DATATYPE,
	COMPARETYPE, GREATER, LESSER, GREATEREQUAL, LESSEREQUAL, EQUAL, NOTEQUAL
};
// Define the map to store the mappings between token strings and TokenVALUE enums
static const std::unordered_map<std::string, TokenVALUE> StrTokenValueMap = {
	{mainK, TokenVALUE::MAIN},
	{loopK, TokenVALUE::LOOP},
	{boolK, TokenVALUE::BOOL},
	{storeK, TokenVALUE::STORE},
	{stringK, TokenVALUE::STRING},
	{coordK, TokenVALUE::COORD},
	{listK, TokenVALUE::LIST},
	{intK, TokenVALUE::INTEGER},
	{floatK, TokenVALUE::FLOAT},
	{compareK, TokenVALUE::COMPARE},
	{zoneK, TokenVALUE::ZONE},
	{ifK, TokenVALUE::IF},
	{elseK, TokenVALUE::ELSE},
	{elifK, TokenVALUE::ELIF},
	{doloopK, TokenVALUE::DOLOOP},
	{andK, TokenVALUE::AND},
	{notK, TokenVALUE::NOT},
	{orK, TokenVALUE::OR},
	{directionK, TokenVALUE::DIRECTION},
	{switchK, TokenVALUE::SWITCH},
	{defaultK, TokenVALUE::DEFAULT},
	{breakK, TokenVALUE::BREAK},
	{continueK, TokenVALUE::CONTINUE},
	{caseK, TokenVALUE::CASE},
	{printK, TokenVALUE::PRINT},
	{waitK, TokenVALUE::WAIT},
	{commaP, TokenVALUE::COMMA},
	{openBracketsP, TokenVALUE::OPENBRACKETS},
	{closeBracketsP, TokenVALUE::CLOSEBRACKETS},
	{openAngleBracketsP, TokenVALUE::OPENANGLEBRACKETS},
	{closeAngleBracketsP, TokenVALUE::CLOSEANGLEBRACKETS},
	{openParenthesisP, TokenVALUE::OPENPARENTHESIS},
	{closeParenthesisP, TokenVALUE::CLOSEPARENTHESIS},
	{falseL, TokenVALUE::FALSELITERAL},
	{trueL, TokenVALUE::TRUELITERAL},
	{millisecondL, TokenVALUE::MILLISECOND},
	{secondL, TokenVALUE::SECOND},
	{minuteL, TokenVALUE::MINUTE},
	{northL, TokenVALUE::NORTH},
	{northwL, TokenVALUE::NORTHW},
	{northeL, TokenVALUE::NORTHE},
	{southL, TokenVALUE::SOUTH},
	{southwL, TokenVALUE::SOUTHW},
	{southeL, TokenVALUE::SOUTHE},
	{boolL, TokenVALUE::BOOLTYPE},
	{intL, TokenVALUE::INTTYPE},
	{floatL, TokenVALUE::FLOATTYPE},
	{coordL, TokenVALUE::COORDTYPE},
	{zoneL, TokenVALUE::ZONETYPE},
	{directionL, TokenVALUE::DIRECTIONTYPE},
	{timetypeL, TokenVALUE::TIMETYPE},
	{stringL, TokenVALUE::STRINGTYPE},
	{comparetypeL, TokenVALUE::COMPARETYPE},
	{greaterL, TokenVALUE::GREATER},
	{lesserL, TokenVALUE::LESSER},
	{greaterequalL, TokenVALUE::GREATEREQUAL},
	{lesserequalL, TokenVALUE::LESSEREQUAL},
	{equalL, TokenVALUE::EQUAL},
	{notequalL, TokenVALUE::NOTEQUAL},
	{quotation, TokenVALUE::QUOTATION}
};

// Define the map to store the mappings between TokenVALUE enums and their corresponding strings
static const std::unordered_map<TokenVALUE, std::string> TokenValueStrMap = {
{TokenVALUE::MAIN, mainK},
{TokenVALUE::LOOP, loopK},
{TokenVALUE::BOOL, boolK},
{TokenVALUE::STORE, storeK},
{TokenVALUE::STRING, stringK},
{TokenVALUE::COORD, coordK},
{TokenVALUE::LIST, listK},
{TokenVALUE::INTEGER, intK},
{TokenVALUE::FLOAT, floatK},
{TokenVALUE::COMPARE, compareK},
{TokenVALUE::ZONE, zoneK},
{TokenVALUE::IF, ifK},
{ TokenVALUE::ELSE, elseK },
{ TokenVALUE::ELIF, elifK },
{ TokenVALUE::DOLOOP, doloopK },
{ TokenVALUE::AND, andK },
{ TokenVALUE::NOT, notK },
{ TokenVALUE::OR, orK },
{ TokenVALUE::DIRECTION, directionK },
{ TokenVALUE::SWITCH, switchK },
{ TokenVALUE::DEFAULT, defaultK },
{ TokenVALUE::BREAK, breakK },
{ TokenVALUE::CONTINUE, continueK },
{ TokenVALUE::CASE, caseK },
{ TokenVALUE::PRINT, printK },
{ TokenVALUE::WAIT, waitK },
{ TokenVALUE::COMMA, commaP },
{ TokenVALUE::OPENBRACKETS, openBracketsP },
{ TokenVALUE::CLOSEBRACKETS, closeBracketsP },
{ TokenVALUE::OPENANGLEBRACKETS, openAngleBracketsP },
{ TokenVALUE::CLOSEANGLEBRACKETS, closeAngleBracketsP },
{ TokenVALUE::OPENPARENTHESIS, openParenthesisP },
{ TokenVALUE::CLOSEPARENTHESIS, closeParenthesisP },
{ TokenVALUE::FALSELITERAL, falseL },
{ TokenVALUE::TRUELITERAL, trueL },
{ TokenVALUE::MILLISECOND, millisecondL },
{ TokenVALUE::SECOND, secondL },
{ TokenVALUE::MINUTE, minuteL },
{ TokenVALUE::NORTH, northL },
{ TokenVALUE::NORTHW, northwL },
{ TokenVALUE::NORTHE, northeL },
{ TokenVALUE::SOUTH, southL },
{ TokenVALUE::SOUTHW, southwL },
{ TokenVALUE::SOUTHE, southeL },
{ TokenVALUE::BOOLTYPE, boolL },
{ TokenVALUE::INTTYPE, intL },
{ TokenVALUE::FLOATTYPE, floatL },
{ TokenVALUE::COORDTYPE, coordL },
{ TokenVALUE::ZONETYPE, zoneL },
{ TokenVALUE::DIRECTIONTYPE, directionL },
{ TokenVALUE::TIMETYPE, timetypeL },
{ TokenVALUE::STRINGTYPE, stringL },
{ TokenVALUE::COMPARETYPE, comparetypeL },
{ TokenVALUE::GREATER, greaterL },
{ TokenVALUE::LESSER, lesserL },
{ TokenVALUE::GREATEREQUAL, greaterequalL },
{ TokenVALUE::LESSEREQUAL, lesserequalL },
{ TokenVALUE::EQUAL, equalL },
{ TokenVALUE::NOTEQUAL, notequalL },
{ TokenVALUE::QUOTATION, quotation }
};




enum TagType {
	TAG,FLOWTAG,DATATYPETAG,COMPARETYPETAG,TIMETYPETAG,DIMENSIONTAG, INTEGERTAG, FLOATTAG, BOOLTAG, ANDTAG, NOTTAG, ORTAG, COMPARETAG,WAITTAG,
	STRINGTAG,COORDSTAG,DIRECTIONTAG,ZONETAG,LISTTAG,IFTAG,LOOPTAG, DOLOOPTAG, SWITCHTAG, DEFAULTTAG, ELSETAG, ELIFTAG, BREAKTAG, CONTINUETAG,
	CASETAG,LOADTAG,STORETAG,MAINTAG,PRINTTAG
};

enum class TimeType {
	MINUTE, SECOND, MILLISECOND
};

enum class CompareType {
	NONE,EQUAL, NOTEQUAL, GREATER, LESSER, GREATEREQUAL, LESSEREQUAL
};



struct UserVariables {
	std::map<std::string, void*> variables;

	template<typename T>
	void addVariable(std::string name, T value);

	template<typename T>
	T getValue(std::string name);
};

template<typename T>
void UserVariables::addVariable(std::string name, T value) {
	variables[name] = new T(value);
}

template<typename T>
T UserVariables::getValue(std::string name) {
	auto it = variables.find(name);
	if (it != variables.end()) {
		return *static_cast<T*>(it->second);
	}
	return T();
}


class Tag {

public:
	Tag();
	Tag(const Tag& toCopy);
	std::shared_ptr<Tag> clone();
	virtual void execute();
	std::string tagText;
	int nbParents;
	bool doExecute;
	TagType myType;
};

class FlowTag :public Tag {

public:
	FlowTag();
	FlowTag(const FlowTag& toCopy);
	std::shared_ptr<FlowTag> clone();
	void showTag();
	bool checkCondition();
	void linkTags();
	bool canReboot;
	bool executed;
	bool forcedExit;
	bool rebootLoop;
	std::shared_ptr<Tag> condition;
	FlowTag* previousTag;
	FlowTag* parentFlowTag;
	std::vector<std::shared_ptr<Tag>> nestedTags;
	std::vector<FlowTag*> listPreviousFlowTags;
};



class MainTag : public FlowTag {
public:
	MainTag();
	MainTag(const MainTag& toCopy);
	MainTag(std::shared_ptr<Tag> innerTag);
	MainTag(const std::vector<std::shared_ptr<Tag>>&tags);
	std::vector<std::shared_ptr<Tag>> argvTag;
	std::shared_ptr<MainTag> clone();
	void execute()override;
};

static bool isFlowTag(std::shared_ptr<Tag> tag) {
	if (tag) {
		return std::dynamic_pointer_cast<FlowTag>(tag) != nullptr;
	}
	else return false;
}

//Flow Tags
class DoLoopTag :public FlowTag {
public:
	DoLoopTag();
	DoLoopTag(const DoLoopTag& toCopy);
	DoLoopTag(const std::shared_ptr<Tag>& condition,const std::vector<std::shared_ptr<Tag>>&nestedTags);
	std::shared_ptr<DoLoopTag> clone();
	void execute()override;
};

class IfTag :public FlowTag {
public:
	IfTag();
	IfTag(const IfTag& toCopy);
	IfTag(const std::shared_ptr<Tag>& condition, const std::vector<std::shared_ptr<Tag>>& listTags);
	std::shared_ptr<IfTag> clone();
	void execute()override;
};

class ElseTag :public FlowTag {
public:
	ElseTag();
	ElseTag(const ElseTag& toCopy);
	ElseTag(const std::vector<std::shared_ptr<Tag>>& listTags);
	std::shared_ptr<ElseTag> clone();
	void execute()override;
};
class ElifTag :public FlowTag {
public:
	ElifTag();
	ElifTag(const ElifTag& toCopy);
	ElifTag(const std::shared_ptr<Tag>& condition,const std::vector<std::shared_ptr<Tag>>& listTags);
	std::shared_ptr<ElifTag> clone();
	void execute()override;
};

class LoopTag :public FlowTag {
public:
	LoopTag();
	LoopTag(const std::shared_ptr<Tag>conditionTag);
	LoopTag(const std::shared_ptr<Tag>conditionTag, const std::vector<std::shared_ptr<Tag>>& bodyTags);
	LoopTag(const LoopTag& toCopy);
	LoopTag(const std::shared_ptr<Tag>& condition,const std::vector<std::shared_ptr<Tag>>& listTags);
	std::shared_ptr<LoopTag> clone();
	void execute()override;
};

class CaseTag :public FlowTag {
public:
	CaseTag();
	//virtual void execute()override;
};

class SwitchTag :public FlowTag {
public:
	SwitchTag();
	//virtual void execute()override;
};

class BreakTag :public FlowTag {
public:
	BreakTag();
	BreakTag(const BreakTag& toCopy);
	std::shared_ptr<BreakTag> clone();
	void execute()override;
};

class ContinueTag :public FlowTag {
public:
	ContinueTag();
	ContinueTag(const ContinueTag& toCopy);
	std::shared_ptr<ContinueTag> clone();
	void execute()override;
};





//Action Tags
template <typename T>
class StoreTag :public Tag {
public:
	StoreTag();
	StoreTag(const StoreTag& toCopy);
	StoreTag(const std::string& name,const std::shared_ptr<Tag>& value,UserVariables* uv);
	std::shared_ptr<StoreTag> clone();
	std::string varName;
	std::shared_ptr<Tag> valueTag;
	void execute()override;
	UserVariables* myVariables;
};

template <typename T>
class StoreTag<std::vector<T>> :public Tag {
public:
	StoreTag();
	StoreTag(const StoreTag& toCopy);
	StoreTag(const std::string& name,const std::shared_ptr<Tag>& value,UserVariables* uv);
	std::shared_ptr<StoreTag> clone();
	std::string varName;
	std::vector<std::shared_ptr<Tag>> listValueTags;
	void execute()override;
	UserVariables* myVariables;
};
template class StoreTag<int>;
template class StoreTag<float>;
template class StoreTag<std::string>;
template class StoreTag<bool>;
template class StoreTag<Coord>;
template class StoreTag<Zone>;
template class StoreTag<Direction>;
template class StoreTag<std::vector<int>>;
template class StoreTag<std::vector<float>>;
template class StoreTag<std::vector<std::string>>;
template class StoreTag<std::vector<bool>>;
template class StoreTag<std::vector<Coord>>;
template class StoreTag<std::vector<Zone>>;
template class StoreTag<std::vector<Direction>>;

template<typename T>
class LoadTag :public Tag {
public:
	LoadTag();
	LoadTag(const LoadTag& toCopy);
	LoadTag(const std::string& name, UserVariables* uv);
	std::shared_ptr<LoadTag> clone();
	std::string varName;
	UserVariables* myVariables;
	void execute()override;
	T value;
};
template class LoadTag<int>;
template class LoadTag<float>;
template class LoadTag<std::string>;
template class LoadTag<bool>;
template class LoadTag<Coord>;
template class LoadTag<Zone>;
template class LoadTag<Direction>;
template class LoadTag<std::vector<int>>;
template class LoadTag<std::vector<float>>;
template class LoadTag<std::vector<std::string>>;
template class LoadTag<std::vector<bool>>;
template class LoadTag<std::vector<Coord>>;
template class LoadTag<std::vector<Zone>>;
template class LoadTag<std::vector<Direction>>;


class FunctionTag :public Tag {
public:
	FunctionTag();
};

class ClickTag :public Tag {
public:
	ClickTag();
};

class SwipeTag :public Tag {
public:
	SwipeTag();
	//virtual void execute()override;
};

class TimeTypeTag : public Tag {
public:
	TimeTypeTag();
	TimeTypeTag(const TimeTypeTag& toCopy);
	TimeTypeTag(TimeType type);
	std::shared_ptr<TimeTypeTag> clone();
	TimeType myTime;
};

class CompareTypeTag : public Tag {
public:
	CompareTypeTag();
	CompareTypeTag(const CompareTypeTag& toCopy);
	CompareTypeTag(CompareType type);
	std::shared_ptr<Tag> compareTypeTag;
	std::shared_ptr<CompareTypeTag> clone();
	CompareType myComp;
};

class FindTag :public Tag {
public:
	FindTag();
};

class FindClickTag :public Tag {
public:
	FindClickTag();

};

class FindSwipeTag :public Tag {
public:
	FindSwipeTag();

};


template<typename T>
class ListTag :public Tag {
	public:
	ListTag();
	ListTag(const ListTag& toCopy);
	ListTag(const std::vector<std::shared_ptr<Tag>>& parameters);
	std::shared_ptr<ListTag> clone();
	std::vector<T> myList;
	std::vector<std::shared_ptr<Tag>> listTags;
	void executeSimpleTag(std::shared_ptr<Tag> tag);
	void execute()override;
};



class DimXTag :public Tag {
public:
	DimXTag();
};

class DimYTag :public Tag {
public:
	DimYTag();
};

class WaitTag :public Tag {
public:
	WaitTag();
	WaitTag(const WaitTag& toCopy);
	WaitTag(const std::shared_ptr<Tag>& parameter,const TimeType& waitType);
	WaitTag(int time, TimeType waitType);
	std::shared_ptr<WaitTag> clone();
	std::shared_ptr<Tag> timeTag;
	TimeType type;
	int time;
	void execute()override;
};

template <typename T>
class PrintTag : public Tag {
public:
	PrintTag();
	PrintTag(const PrintTag& toCopy);
	PrintTag(const std::shared_ptr<Tag>& tag);
	std::shared_ptr<Tag> toPrintTag;
	std::shared_ptr<PrintTag> clone();
	void execute()override;
};

//Data Tags
class BoolTag : public Tag {
public:
	BoolTag();
	BoolTag(const BoolTag& toCopy);
	BoolTag(const std::shared_ptr<Tag>& parameter);
	BoolTag(bool v);
	std::shared_ptr<Tag> boolTag;
	std::shared_ptr<BoolTag> clone();
	bool myBool;
	void execute()override;
};



class IntTag :public Tag {
public:
	IntTag();
	IntTag(const IntTag& toCopy);
	IntTag(const std::shared_ptr<Tag>& parameter);
	IntTag(int nb);
	std::shared_ptr<Tag> intTag;
	std::shared_ptr<IntTag> clone();
	int myInt;
	void execute()override;
};

class FloatTag :public Tag {
public:
	FloatTag();
	FloatTag(const FloatTag& toCopy);
	FloatTag(const std::shared_ptr<Tag>& parameter);
	FloatTag(float nb);
	std::shared_ptr<Tag> floatTag;
	std::shared_ptr<FloatTag> clone();
	float myFloat;
	void execute()override;
};

class NotTag :public BoolTag {
public:
	NotTag();
	NotTag(const NotTag& toCopy);
	NotTag(const std::shared_ptr<Tag>& parameter);
	NotTag(bool b);
	std::shared_ptr<NotTag> clone();
	void execute()override;
};

class AndTag :public BoolTag {
public:
	AndTag();
	AndTag(const AndTag& toCopy);
	AndTag(const std::vector<std::shared_ptr<Tag>>& parameters);
	std::vector<std::shared_ptr<Tag>> listTags;
	std::shared_ptr<AndTag> clone();
	void execute()override;
};

class OrTag :public BoolTag {
public:
	OrTag();
	OrTag(const OrTag& toCopy);
	OrTag(const std::vector<std::shared_ptr<Tag>>& parameters);
	std::vector<std::shared_ptr<Tag>> listTags;
	std::shared_ptr<OrTag> clone();
	void execute()override;
};


template<typename T>
class CompareTag :public BoolTag {
public:
	CompareTag();
	CompareTag(const CompareTag& toCopy);
	CompareTag(const std::vector<std::shared_ptr<Tag>>& parameters,const CompareType& cType);
	std::shared_ptr<CompareTag> clone();
	std::vector<std::shared_ptr<Tag>> listTags;
	CompareType compType;
	void execute()override;
};



class CoordTag :public Tag {
public:
	CoordTag();
	CoordTag(const CoordTag& toCopy);
	CoordTag(const std::shared_ptr<Tag>& parameters);
	CoordTag(const std::shared_ptr<Tag>& xPoint, const std::shared_ptr<Tag>& yPoint);
	std::shared_ptr<CoordTag> clone();
	std::shared_ptr<Tag> coordsTag;
	Coord myCoords;
	void execute() override;
};

class ZoneTag :public Tag {
public:
	ZoneTag();
	ZoneTag(const ZoneTag& toCopy);
	ZoneTag(const std::shared_ptr<Tag>& parameters);
	std::shared_ptr<Tag> zoneTag;
	std::shared_ptr<ZoneTag> clone();
	Zone myZone;
	void execute() override;
};

class DirectionTag :public Tag {
public:
	DirectionTag();
	DirectionTag(const DirectionTag& toCopy);
	DirectionTag(const std::shared_ptr<Tag>& parameter);
	DirectionTag(Direction parameter);
	std::shared_ptr<Tag> dirTag;
	std::shared_ptr<DirectionTag> clone();
	Direction myDir;
	void execute() override;
};

class StringTag :public Tag {
public:
	StringTag();
	StringTag(const StringTag& toCopy);
	StringTag(const std::shared_ptr<Tag>& parameter);
	StringTag(std::string v);
	std::shared_ptr<Tag> stringTag;
	std::shared_ptr<StringTag> clone();
	std::string myString;
	void execute() override;
};

int executeInt(const std::shared_ptr<Tag>& parameter);
float executeFloat(const std::shared_ptr<Tag>& parameter);
std::string executeString(const std::shared_ptr<Tag>& parameter);
Zone executeZone(const std::shared_ptr<Tag>& parameter);
Coord executeCoord(const std::shared_ptr<Tag>& parameter);
Direction executeDirection(const std::shared_ptr<Tag>& parameter);
bool executeBool(const std::shared_ptr<Tag>& parameter);

template <typename T>
std::vector<T> executeList(std::vector<std::shared_ptr<Tag>>listTags);


std::shared_ptr < FloatTag> getFloatTag(std::shared_ptr<Tag> tag);
std::shared_ptr < IntTag> getIntTag(std::shared_ptr<Tag> tag);
std::shared_ptr<StringTag> getStringTag(std::shared_ptr<Tag> tag);
std::shared_ptr<DirectionTag> getDirectionTag(std::shared_ptr<Tag> tag);
std::shared_ptr < CoordTag> getCoordTag(std::shared_ptr<Tag> tag);
std::shared_ptr < ZoneTag> getZoneTag(std::shared_ptr<Tag> tag);
std::shared_ptr < FlowTag> getFlowTag(std::shared_ptr<Tag> tag);


template <typename T>
std::shared_ptr <CompareTag<T>> getCompareTag(std::shared_ptr<Tag> tag);

std::shared_ptr < BoolTag> getBoolTag(std::shared_ptr<Tag> tag);

Direction getDirection(std::string d);
std::string getDirection(Direction d);






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

//Punctuation Tokens string
static const std::string commaP = ",";
static const std::string openParenthesisP = "(";
static const std::string closeParenthesisP = ")";
static const std::string openBracketsP = "{";
static const std::string closeBracketsP = "}";
static const std::string openAngleBracketsP = "<";
static const std::string closeAngleBracketsP = ">";
static const std::string quotation = "\"";
static const std::string whitespace = " ";
static const std::string tabulation = "\t";
static const std::string backspace = "\b";
static const std::string newline = "\n";
static const std::string carriagereturn = "\r";
static const std::string allPunctuations = " <>\",(){}\t\b\n\r";

static const std::vector<std::string> allPunctuationsTokensStrings = { openAngleBracketsP,closeAngleBracketsP, quotation,carriagereturn, newline, whitespace,tabulation,backspace, commaP, openParenthesisP, closeParenthesisP, openBracketsP, closeBracketsP };

//Literal Tokens string
static const std::string trueL = "true";
static const std::string falseL = "false";
static const std::string secondL = "SECOND";
static const std::string millisecondL = "MILLISECOND";
static const std::string minuteL = "MINUTE";
static const std::string northL = "NORTH";
static const std::string southL = "SOUTH";
static const std::string northwL = "NORTHW";
static const std::string northeL = "NORTHE";
static const std::string southwL = "SOUTHW";
static const std::string southeL = "SOUTHE";
static const std::string intL = "INT";
static const std::string floatL = "FLOAT";
static const std::string coordL = "COORD";
static const std::string zoneL = "ZONE";
static const std::string boolL = "BOOL";
static const std::string timetypeL = "TIMETYPE";
static const std::string directionL = "DIRECTION";
static const std::string stringL = "STRING";
static const std::string datatypeL = "DATATYPE";
static const std::string comparetypeL = "COMPARETYPE";
static const std::string greaterL = "GREATER";
static const std::string lesserL = "LESSER";
static const std::string equalL = "EQUAL";
static const std::string notequalL = "NOTEQUAL";
static const std::string greaterequalL = "GREATEREQUAL";
static const std::string lesserequalL = "LESSEREQUAL";






static const std::vector<std::string> allLiteralsTokensStrings =
{ trueL,falseL,secondL,millisecondL,minuteL,northL,southL,northwL,northeL,southwL,southeL,intL,floatL,coordL,zoneL,boolL,timetypeL,directionL,stringL,datatypeL };

static std::string storeK = "store";
static std::string clickK = "click";
static std::string swipeK = "swipe";
static std::string waitK = "wait";
static std::string ifK = "if";
static std::string elseK = "else";
static std::string elifK = "elif";
static std::string notK = "not";
static std::string floatK = "float";
static std::string intK = "int";
static std::string coordK = "coord";
static std::string zoneK = "zone";
static std::string andK = "and";
static std::string orK = "or";
static std::string loopK = "loop";
static std::string listK = "list";
static std::string stringK = "string";
static std::string directionK = "direction";
static std::string findswipeK = "findswipe";
static std::string findclickK = "findclick";
static std::string findK = "find";
static std::string breakK = "break";
static std::string continueK = "continue";
static std::string doloopK = "doloop";
static std::string functionK = "function";
static std::string switchK = "switch";
static std::string caseK = "case";
static std::string defaultK = "default";
static std::string compareK = "compare";
static std::string mainK = "main";
static std::string boolK = "bool";
static std::string printK = "print";
static std::string returnK = "return";
static std::string voidK = "void";
static std::string addK = "add";
static std::string subK = "sub";
static std::string multK = "mult";


static const std::vector<std::string> allKeywordsTokensString = { storeK,clickK,swipeK,waitK,ifK,elseK,elifK,notK,floatK,intK,coordK,zoneK,andK,orK,loopK,listK,stringK,directionK,findswipeK,findclickK,findK,breakK,continueK,doloopK,functionK,switchK,caseK,defaultK,compareK,mainK,boolK,printK,returnK,voidK, };

// Concatenate all token vectors
static const std::vector<std::string> allTokensStrings = [] {
	std::vector<std::string> result;
	result.reserve(allKeywordsTokensString.size() +
		allPunctuationsTokensStrings.size() +
		allLiteralsTokensStrings.size());

	result.insert(result.end(), allKeywordsTokensString.begin(), allKeywordsTokensString.end());
	result.insert(result.end(), allPunctuationsTokensStrings.begin(), allPunctuationsTokensStrings.end());
	result.insert(result.end(), allLiteralsTokensStrings.begin(), allLiteralsTokensStrings.end());

	return result;
	}();




enum Dimensions {
	ONEDIM,TWODIM
};

enum TagType {
	TAG,FLOWTAG,DATATYPETAG,COMPARETYPETAG,TIMETYPETAG,DIMENSIONTAG, INTEGERTAG, FLOATTAG, BOOLTAG, ANDTAG, NOTTAG, ORTAG, COMPARETAG,WAITTAG, STRINGTAG,COORDSTAG,DIRECTIONTAG,ZONETAG,LISTTAG,IFTAG,LOOPTAG, DOLOOPTAG, SWITCHTAG, DEFAULTTAG, ELSETAG, ELIFTAG, BREAKTAG, CONTINUETAG, CASETAG,LOADTAG,STORETAG,MAINTAG,PRINTTAG
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






#pragma once

#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include "Interactions.h"
#include "tags.h"
#include <unordered_set>
//Punctuation Tokens string
static const std::string commaP				=",";
static const std::string openParenthesisP	="(";
static const std::string closeParenthesisP	=")";
static const std::string openBracketsP		="{";
static const std::string closeBracketsP		="}";
static const std::string openAngleBracketsP		="<";
static const std::string closeAngleBracketsP = ">";
static const std::string quotation	="\"";
static const std::string whitespace = " ";
static const std::string tabulation = "\t";
static const std::string backspace = "\b";
static const std::string newline = "\n";
static const std::string carriagereturn = "\r";
static const std::string allPunctuations = " <>\",(){}\t\b\n\r";

static const std::vector<std::string> allPunctuationsTokensStrings = { openAngleBracketsP,closeAngleBracketsP, quotation,carriagereturn, newline, whitespace,tabulation,backspace, commaP, openParenthesisP, closeParenthesisP, openBracketsP, closeBracketsP };

//Operators Tokens 
//string
static const std::string plusO				= "+";
static const std::string minusO				= "-";
static const std::string divideO			= "/";
static const std::string multiplyO			= "*";
static const std::string biggerO			= ">";
static const std::string biggerEqualO		= ">=";
static const std::string lesserO			= "<";
static const std::string lesserEqualO		= "<=";

static const std::vector<std::string> allOperatorsTokensStrings = { plusO, minusO, divideO, multiplyO, biggerO, biggerEqualO, lesserO, lesserEqualO };


//Literal Tokens string
static const std::string trueL				= "true";
static const std::string falseL				= "false";
static const std::string secondL			= "SECOND";
static const std::string millisecondL		= "MILLISECOND";
static const std::string minuteL		= "MINUTE";
static const std::vector<std::string> allLiteralsTokensStrings = { trueL,falseL,secondL,millisecondL,minuteL };


// Concatenate all token vectors
static const std::vector<std::string> allTokensStrings = [] {
	std::vector<std::string> result;
	result.reserve(allKeywordsTokensString.size() +
		allPunctuationsTokensStrings.size() +
		allOperatorsTokensStrings.size() +
		allLiteralsTokensStrings.size());

	result.insert(result.end(), allKeywordsTokensString.begin(), allKeywordsTokensString.end());
	result.insert(result.end(), allPunctuationsTokensStrings.begin(), allPunctuationsTokensStrings.end());
	result.insert(result.end(), allOperatorsTokensStrings.begin(), allOperatorsTokensStrings.end());
	result.insert(result.end(), allLiteralsTokensStrings.begin(), allLiteralsTokensStrings.end());

	return result;
	}();


static bool isTokenString(const std::string& text);
static bool isKeywordString(const std::string& text);
static bool isLiteralString(const std::string& text);
static bool isPunctuationString(const std::string& text);
static bool isOperatorString(const std::string& text);

static bool isTokenStringContained(const std::string& text);


enum class TokenVALUE {
	NOT,TOKEN,UNKNOWN, QUOTATION,OPENANGLEBRACKETS, CLOSEANGLEBRACKETS, FLOW,COMMA,SEMICOLON,NUMERIC,IDENTIFIER, CLOSEBRACKETS, OPENBRACKETS, OPENPARENTHESIS, CLOSEPARENTHESIS, STRINGLITERAL,TRUELITERAL,FALSELITERAL, SECOND,WHITESPACE, MINUTE, MILLISECOND, INTEGER, WAIT, FLOAT, BOOL, AND, OR, COMPARE, STRING, COORD, DIRECTION, ZONE, LIST, IF, LOOP, DOLOOP, SWITCH, DEFAULT, ELSE, ELIF, BREAK, CONTINUE, CASE, STORE, MAIN, PRINT
};

enum class DataType {
	NONE,COORD, ZONE, STRING, DIRECTION, FLOAT, INT, BOOL, TIMETYPE
};



static TokenVALUE getTokenValue(const std::string& text);
static std::string getTokenString(TokenVALUE value);
std::string getNextPunctuationToken(const std::string& str);
static std::string getNextTokenString(const std::string& text);
static TokenVALUE getNextTokenValue(const std::string& text);
bool isWhitespace(const char& c);

enum class ErrorType{MISSING,UNEXPECTED,REPLACED};

class Error {
public:
	Error();
	Error(TokenVALUE ev,ErrorType et,int el);
	Error(DataType dType,ErrorType et,int el);
	void showError();
	ErrorType errorType;
	TokenVALUE errorValue;
	DataType dType;
	int errorLine;
};

class TokenResult{
public:
	TokenResult();
	TokenResult(TokenVALUE value,int l);
	void showErrors();
	void addError(TokenVALUE value,ErrorType et, int l);
	void addError(DataType type, ErrorType et,int l);
	void addError(const std::shared_ptr<TokenResult>& nestedToken);
	void addVar(const std::string& name, const DataType& type);
	bool isSuccess();
	std::shared_ptr<std::map<std::string, DataType>> varTable;
	std::vector<std::shared_ptr<Error>> listErrors;
};



template <typename T>
class IteratorList{
public:
	IteratorList() {
		current = 0;
	}
	IteratorList(std::vector<std::shared_ptr<T>> nextTokens) :IteratorList() {
		this->nextTokens = nextTokens;
	}
	void next() {
		++current;
	}
	bool ended() {
		return current >= nextTokens.size();
	}
	int size()const {
		return nextTokens.size();
	}
	std::shared_ptr<T> currentToken() {
		return nextTokens.at(current);
	}
	bool empty()const {
		return nextTokens.empty();
	}
	std::vector<std::shared_ptr<T>> nextTokens;
	int current;
};

class Token {
public:
	Token();
	std::string tokenText;
	TokenVALUE tValue;
	std::shared_ptr<TokenResult> tRes;
	std::shared_ptr<TokenResult> updateRes(std::shared_ptr<TokenResult> tRes);
	std::shared_ptr<TokenResult> addError(TokenVALUE value, ErrorType et=ErrorType::MISSING);
	std::shared_ptr<TokenResult> addError(const std::shared_ptr <TokenResult>& tr);
	std::shared_ptr<TokenResult> addError(DataType type, ErrorType et=ErrorType::REPLACED);
	int line;

	virtual DataType getDataType();
	virtual std::shared_ptr<TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	virtual std::shared_ptr<Tag> execute();
};

bool isBooleanToken(const std::shared_ptr<Token>& token);


class OToken :public Token {
public:
};
class PToken :public Token {
public:
};

class LToken :public Token {
public:
};

class KToken :public Token {
public:

};

class KPToken :public KToken {
protected:
	KPToken();
	std::shared_ptr<TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::shared_ptr<Token> addNumber(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	std::shared_ptr<Token> addInteger(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	std::shared_ptr<Token> addFloat(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	std::shared_ptr<Token> addCoord(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	std::shared_ptr<Token> addString(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	std::shared_ptr<Token> addComma(IteratorList<Token>& tl);
	std::shared_ptr<Token> addTimeType(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	std::shared_ptr<Token> addOp(IteratorList<Token>& tl);
    std::shared_ptr<Token> addCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	virtual std::shared_ptr<Token> handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
};

class FlowKToken :public KToken {
protected:
	FlowKToken();
	std::vector<std::shared_ptr<Token>> nestedTokens;
	std::shared_ptr<Token> addOb(IteratorList<Token>& tl);
	std::shared_ptr<Token> addCb(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	std::shared_ptr<TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
};

class FlowKPToken :public KPToken {
protected:
	FlowKPToken();
	std::vector<std::shared_ptr<Token>> nestedTokens;
	std::shared_ptr<Token> addOb(IteratorList<Token>& tl);
	std::shared_ptr<Token> addCb(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	std::shared_ptr<TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
};

class FlowKCToken :public FlowKPToken {
protected:
	FlowKCToken();
	std::shared_ptr<Token> condition;
	std::shared_ptr<Token> addCondition(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	std::shared_ptr < TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
};

class IdentifierToken :public Token {
public:
	IdentifierToken(const std::string& varName);
	DataType getDataType()override;
	std::shared_ptr < TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
};

class UnknownToken : public Token {
public:
	UnknownToken();
};

class MainToken :public FlowKPToken {
public:
	MainToken();
	std::vector<std::shared_ptr<Token>> argvTokens;
    std::shared_ptr<TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::shared_ptr<Tag> execute()override;
};

class DoLoopToken :public FlowKPToken {
public:
	DoLoopToken();
	std::shared_ptr<TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::shared_ptr<Tag> execute()override;
};

class IfToken :public FlowKCToken {
public:
	IfToken();
	std::shared_ptr<TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::shared_ptr<Tag> execute()override;
};

class ElseToken :public FlowKToken {
public:
	ElseToken();
	std::shared_ptr<Tag> execute()override;
};

class ElifToken :public FlowKCToken {
public:
	ElifToken();
	std::shared_ptr<TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::shared_ptr<Tag> execute()override;
};

class LoopToken :public FlowKCToken {
public:
	LoopToken();
	std::shared_ptr<Token> conditionToken;
	std::shared_ptr<Tag> execute()override;
};


class WaitToken :public KPToken {
public:
	WaitToken();
	std::shared_ptr<TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::vector<std::shared_ptr<Token>> listToken;
	std::shared_ptr<Token> integerToken;
	std::shared_ptr<Tag> execute()override;
};

class AndToken :public KPToken {
public:
	AndToken();
	DataType getDataType()override;
	std::shared_ptr<Token> handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::vector<std::shared_ptr<Token>> listBoolToken;
	std::shared_ptr<Tag> execute()override;
};

class OrToken :public KPToken {
public:
	OrToken();
	DataType getDataType()override;
	std::shared_ptr<Token> handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::vector<std::shared_ptr<Token>> listBoolToken;
	std::shared_ptr<Tag> execute()override;
};

class NotToken :public KPToken {
public:
	NotToken();
	 DataType getDataType();
	std::shared_ptr<TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::shared_ptr<Token> handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::shared_ptr<Token> boolToken;
	std::shared_ptr<Tag> execute()override;
};

class FalseToken : public LToken {
public:
	FalseToken();
	DataType getDataType()override;
};

class TrueToken : public LToken {
public:
	TrueToken();
	DataType getDataType()override;
};

class SecondToken : public LToken {
public:
	SecondToken();
	DataType getDataType()override;
};
class MilliSecondToken : public LToken {
public:
	MilliSecondToken();
	DataType getDataType()override;
};
class MinuteToken : public LToken {
public:
	MinuteToken();
	DataType getDataType()override;
};

class NumericToken : public LToken {
public:
	NumericToken(const std::string& nb);
	DataType getDataType()override;
	int number;
};

class StringLiteralToken :public LToken {
public:
	StringLiteralToken(const std::string& content);
	std::shared_ptr<Tag> execute()override;
};




class ListToken :public KPToken {
public:
	ListToken();
	std::shared_ptr<Token> addOab(IteratorList<Token>& tokenList);
	std::shared_ptr<Token> addCab(IteratorList<Token>& tokenList);
	std::shared_ptr<Token> addType(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	DataType dType;
	std::shared_ptr<Token> handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::shared_ptr<TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::vector<std::shared_ptr<Token>> listToken;
	std::shared_ptr<Tag> execute()override;
};

class StoreToken :public KPToken {
public:
	StoreToken();
	std::shared_ptr<Token> identifierToken;
	std::shared_ptr<Token> valueToken;
	std::shared_ptr<Token> addIdentifier(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	std::shared_ptr<Token> addValue(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	std::shared_ptr < TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::shared_ptr<Tag> execute()override;
};


class EKPToken :public KPToken {
public:
	std::shared_ptr < TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
};

class IntegerToken :public EKPToken {
public:
	IntegerToken();
	std::shared_ptr<Token> intToken;
	DataType getDataType()override;
	std::shared_ptr<Token> handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::shared_ptr<Tag> execute()override;
};

class FloatToken :public EKPToken {
public:
	FloatToken();
	std::shared_ptr<Token> handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	DataType getDataType()override;
	std::shared_ptr<Token> floatToken;
	std::shared_ptr<Tag> execute()override;
};

class BoolToken :public EKPToken {
public:
	BoolToken();
	std::shared_ptr<Token> handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	std::shared_ptr<Token> boolToken;
	DataType getDataType()override;
	std::shared_ptr<Tag> execute()override;
};
class StringToken :public EKPToken {
public:

	StringToken();
	std::shared_ptr<Token> handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes);
	std::shared_ptr<Token> stringToken;
	std::shared_ptr<Tag> execute()override;
};
class CoordToken :public EKPToken {
public:
	CoordToken();
	DataType getDataType()override;
	std::shared_ptr<Token> xPoint;
	std::shared_ptr<Token> yPoint;
	std::shared_ptr<Token> coordToken;
	std::shared_ptr<Token> handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::shared_ptr<Tag> execute()override;
};

class ZoneToken :public EKPToken {
public:
	ZoneToken();
	DataType getDataType()override;
	std::shared_ptr<Token> topLeft;
	std::shared_ptr<Token> bottomRight;
	std::shared_ptr<Token> zoneToken;
	std::shared_ptr<Token> handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	std::shared_ptr<Tag> execute()override;
};

class DirectionToken :public EKPToken {
public:
	DirectionToken();
	std::shared_ptr<Token> dirToken;
	std::shared_ptr<Token> handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	DataType getDataType()override;
	std::shared_ptr<Tag> execute()override;
};

class CompareToken :public KPToken {
public:
	CompareToken();
	DataType getDataType()override;
	std::vector<std::shared_ptr<Token>> listTokens;
	CompareType cmpType;
	DataType valuesType;
	std::shared_ptr<Tag> execute()override;
};



class PrintToken :public KPToken {
public:
	PrintToken();
	std::shared_ptr<Tag> execute()override;
};

class BreakToken :public KToken {
public:
	BreakToken();
	std::shared_ptr < TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	virtual std::shared_ptr<Tag> execute()override;
};

class ContinueToken :public KToken {
public:
	ContinueToken();
	std::shared_ptr < TokenResult> addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)override;
	virtual std::shared_ptr<Tag> execute()override;
};

class OpenParenthesisToken : public PToken {
public:
	OpenParenthesisToken();
};

class CloseParenthesisToken : public PToken {
public:
	CloseParenthesisToken();
};

class CloseBracketsToken : public PToken {
public:
	CloseBracketsToken();
};

class OpenBracketsToken : public PToken {
public:
	OpenBracketsToken();
};

class CloseAngleBracketsToken : public PToken {
public:
	CloseAngleBracketsToken();
};

class OpenAngleBracketsToken : public PToken {
public:
	OpenAngleBracketsToken();
};

class CommaToken : public PToken {
public:
	CommaToken();
};




std::shared_ptr<Token> getToken(const std::string& text);

std::shared_ptr<Token> getToken(const TokenVALUE& tValue,const std::string&text);

static bool isSpaceToken(std::string& c);



class Lexer {
public:
	Lexer();
	Lexer(const std::string& text);
	std::string nameToken;
	std::string totalContent;
	std::string updatedContent;
	bool empty();
	void extractTokens(const std::string&text);
	std::vector<std::shared_ptr<Token>> listTokens;
	std::string showAllTokens();
	UserVariables* uv;
};




class Interpretor {

public:

	Interpretor();
	Interpretor(std::string folder);
	~Interpretor();
	void readActivityFile(std::string ActivityName);
	std::string copyActivity(std::filesystem::path ActivityName);
	std::shared_ptr<Tag> getActivityTag();
	void createMainTag(const std::string& text);
	std::shared_ptr<MainToken> mainToken;
	std::shared_ptr<MainTag> mainTag;
	std::shared_ptr<Lexer> mainStack;
	UserVariables uv;
	std::shared_ptr<TokenResult> tr;
	IteratorList<Token> tl;
	Lexer lex;
	std::filesystem::path ActivityFolder;
};


#pragma once
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <map>
#include <unordered_set>
#include "Tags.h"
#include "TextManager.h"


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

//Operators Tokens 
//string
static const std::string plusO = "+";
static const std::string minusO = "-";
static const std::string divideO = "/";
static const std::string multiplyO = "*";
static const std::string biggerO = ">";
static const std::string biggerEqualO = ">=";
static const std::string lesserO = "<";
static const std::string lesserEqualO = "<=";

static const std::vector<std::string> allOperatorsTokensStrings = { plusO, minusO, divideO, multiplyO, biggerO, biggerEqualO, lesserO, lesserEqualO };


//Literal Tokens string
static const std::string trueL = "true";
static const std::string falseL = "false";
static const std::string secondL = "SECOND";
static const std::string millisecondL = "MILLISECOND";
static const std::string minuteL = "MINUTE";
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


bool isTokenString(const std::string& text);
bool isKeywordString(const std::string& text);
bool isLiteralString(const std::string& text);
bool isPunctuationString(const std::string& text);
bool isOperatorString(const std::string& text);

static bool isTokenStringContained(const std::string& text);

std::string getStringLiteral(std::string& text);
enum class DataType {
	NONE, COORD, ZONE, STRING, DIRECTION, FLOAT, INT, BOOL, TIMETYPE,DATATYPE
};
enum class TokenVALUE {
	NOT, TOKEN, UNKNOWN, QUOTATION, OPENANGLEBRACKETS, CLOSEANGLEBRACKETS, FLOW, COMMA, SEMICOLON, NUMERIC, IDENTIFIER, CLOSEBRACKETS, OPENBRACKETS, OPENPARENTHESIS, CLOSEPARENTHESIS, STRINGLITERAL, TRUELITERAL, FALSELITERAL, SECOND, WHITESPACE, MINUTE, MILLISECOND, INTEGER, WAIT, FLOAT, BOOL, AND, OR, COMPARE, STRING, COORD, DIRECTION, ZONE, LIST, IF, LOOP, DOLOOP, SWITCH, DEFAULT, ELSE, ELIF, BREAK, CONTINUE, CASE, STORE, MAIN, PRINT
};

class Arguments {
public:
	Arguments();
	Arguments(DataType type,bool infinite);
	Arguments(std::vector<DataType> listTypes);
	std::vector<DataType> getArgsTypes();
	void next();
	bool ended()const;
	int size();
	bool empty();
	DataType at(int i);
	bool approveType(const DataType& type);
	DataType getCurrentDataType();
	bool isRepeatable();
	bool isValid()const;
	bool isCompleted();
	bool isEqual(Arguments args);
protected:
	DataType repeatedType;
	bool repeatable;
	bool stillValid;
	bool completed;
	std::vector<DataType> listArgsTypes;
	int currentIndex;
};

class ArgumentsOverload {
public:
	ArgumentsOverload();
	bool isPresent(Arguments args);
	void addArgs(Arguments args,int index);
	void initTabs();
	void next();
	bool approveType(const DataType& type);
	std::vector<DataType> getPossibleDataTypes();
	std::map<int, std::shared_ptr<Arguments>> getValidArguments();
	std::map<int, std::shared_ptr<Arguments>> getCompletedArgs();
	bool ended();
	int size();
	bool empty();
	bool hasValidArg();
	bool hasCompletedArguments();
	void updateCompletedArguments();
	void updateValidArguments();
	void updateTabs();
	unsigned int getID();
	void setCompleteIndex();
protected:
	std::map<int, std::shared_ptr<Arguments>> validArguments;
	std::map<int, std::shared_ptr<Arguments>> completedArguments;
	std::vector<DataType> currentDataTypes;
	std::map<int, std::shared_ptr<Arguments>> mapArguments;
	unsigned int completeIndex;
};


enum class ErrorType { DATATYPE, MISSING, UNEXPECTED };

class Error {
public:
	Error();
	Error(int el, TokenVALUE tv, ErrorType et = ErrorType::MISSING);
	Error(int el, DataType dType);
	void showError();
protected:

	ErrorType errorType;
	TokenVALUE errorValue;
	DataType dType;
	int errorLine;
};

class TokenResult {
public:
	TokenResult();
	TokenResult(TokenVALUE value, int l);
	void showErrors();
	void addError(int l,TokenVALUE value, ErrorType et=ErrorType::MISSING);
	void addError(int l,DataType type);
	void addError(const TokenResult& tokRes);
	void addVar(const std::string& name, const DataType& type);
	bool success();
	std::map<std::string, DataType> getVarTable();
protected:


	std::map<std::string, DataType> varTable;
	std::vector<Error> listErrors;
};



template <typename T>
class IteratorList {
public:

	IteratorList() {
		current = 0;
	}
	IteratorList(std::vector<std::shared_ptr<T>> nextTokens) :IteratorList() {
		this->listTokens = nextTokens;
	}
	std::vector<std::shared_ptr<T>> getTokens() {
		return listTokens;
	}
	std::shared_ptr<T> getFirst() {
		return listTokens.front();
	}
	size_t size() {
		return listTokens.size();
	}
	bool empty() {
		return listTokens.empty();
	}
	void next() {
		++current;
	}
	bool ended() {
		return current >= listTokens.size();
	}
	std::shared_ptr<T> currentToken() {
		return listTokens.at(current);
	}
protected:



	std::vector<std::shared_ptr<T>> listTokens;
	int current;
};

class Token {
public:
	Token();
	virtual DataType getDataType(TokenResult& tRes);
	virtual void showTokenTree(const int nestedLayer);
	TokenVALUE getValue();
	bool addError(TokenResult& tRes,TokenVALUE value, ErrorType et=ErrorType::MISSING);
	bool addError(TokenResult& tRes,DataType type);
	std::string getTokenText();
	int getLine();
	bool hasValue(TokenVALUE value)const;
	virtual bool addTokens(IteratorList<Token>& tl, TokenResult& tRes);
	virtual std::shared_ptr<Tag> execute();
protected:
	std::string tokenText;
	TokenVALUE tValue;
	int line;

}; 


class FlowToken {
public:
	bool addError(TokenResult& tRes, TokenVALUE value, ErrorType et = ErrorType::MISSING);
	bool addError(TokenResult& tRes, DataType type);
	FlowToken();
	FlowToken(int line);
	void showTokenTree(const int nestedLayer);
	bool addToken(TokenVALUE tVal, IteratorList<Token>& tl, TokenResult& tRes);
	bool addType(DataType tVal, IteratorList<Token>& tl, TokenResult& tRes);
	bool addType(std::vector<DataType> listTypes, IteratorList<Token>& tl, TokenResult& tRes);
protected:
	//Implemented
	void printTabs(const int nestedLayer);
	std::vector<std::shared_ptr<Token>> nestedTokens;
	bool addOb(IteratorList<Token>& tl, TokenResult& tRes);
	bool addCb(IteratorList<Token>& tl, TokenResult& tRes);
	bool addNestedTokens(IteratorList<Token>& tl, TokenResult& tRes);
	bool addBody(IteratorList<Token>& tl, TokenResult& tRes);
	//Not Implemented
	int lineFlow;
};

class TemplateToken {
public:

	TemplateToken();
	TemplateToken(int line);
	bool addToken(TokenVALUE tVal, IteratorList<Token>& tl, TokenResult& tRes);
	bool addType(DataType tVal, IteratorList<Token>& tl, TokenResult& tRes);
	bool addType(std::vector<DataType> listTypes, IteratorList<Token>& tl, TokenResult& tRes);
	bool addOab(IteratorList<Token>& tl,TokenResult& tRes);
	bool addCab(IteratorList<Token>& tl, TokenResult& tRes);
	bool addNumber(IteratorList<Token>& tl, TokenResult& tRes);
	bool addDataType(IteratorList<Token>& tl, TokenResult& tRes);
	bool addInteger(IteratorList<Token>& tl, TokenResult& tRes);
	bool addFloat(IteratorList<Token>& tl, TokenResult& tRes);
	bool addCoord(IteratorList<Token>& tl, TokenResult& tRes);
	bool addZone(IteratorList<Token>& tl, TokenResult& tRes);
	bool addBool(IteratorList<Token>& tl, TokenResult& tRes);
	bool addIdentifier(IteratorList<Token>& tl, TokenResult& tRes);
	bool addString(IteratorList<Token>& tl, TokenResult& tRes);
	bool addComma(IteratorList<Token>& tl, TokenResult& tRes);
	bool addTimeType(IteratorList<Token>& tl, TokenResult& tRes);
	virtual bool addTemplatedTypes(IteratorList<Token>& tl, TokenResult tRes);
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes);
	bool addError(TokenResult& tRes, TokenVALUE value, ErrorType et = ErrorType::MISSING);
	bool addError(TokenResult& tRes, DataType type);
protected:
	ArgumentsOverload templateArguments;
	TokenResult tRes;
	int line;

};



class KToken :public Token {
public:
protected:

	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;

};

//Flow Control Keyword with no arguments
class FlowKToken :public KToken, public FlowToken {
public:
	FlowKToken();
	void showTokenTree(const int nestedLayer)override;
protected:
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;
	//Not Implemented
};

//Keyword token with arguments
class PKToken :public KToken {
public:
	PKToken();
	void showTokenTree(const int nestedLayer)override;
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;
protected:
	ArgumentsOverload argsOverload;
	//Implemented
	bool addToken(TokenVALUE tVal, IteratorList<Token>& tl, TokenResult& tRes);
	bool addType(DataType tVal, IteratorList<Token>& tl, TokenResult& tRes);
	bool addType(std::vector<DataType> listTypes, IteratorList<Token>& tl, TokenResult& tRes);
	bool addNumber(IteratorList<Token>& tl, TokenResult& tRes);
	bool addDataType(IteratorList<Token>& tl, TokenResult& tRes);
	bool addInteger(IteratorList<Token>& tl, TokenResult& tRes);
	bool addFloat(IteratorList<Token>& tl, TokenResult& tRes);
	bool addCoord(IteratorList<Token>& tl, TokenResult& tRes);
	bool addZone(IteratorList<Token>& tl, TokenResult& tRes);
	bool addBool(IteratorList<Token>& tl, TokenResult& tRes);
	bool addIdentifier(IteratorList<Token>& tl, TokenResult& tRes);
	bool addString(IteratorList<Token>& tl, TokenResult& tRes);
	bool addComma(IteratorList<Token>& tl, TokenResult& tRes);
	bool addTimeType(IteratorList<Token>& tl, TokenResult& tRes);
	bool addOp(IteratorList<Token>& tl, TokenResult& tRes);
	bool addCp(IteratorList<Token>& tl, TokenResult& tRes);
	virtual bool handleArguments(IteratorList<Token>& tl, TokenResult& tRes);
	virtual void setOverloads();
	virtual void dispatchArguments();
	std::vector<std::shared_ptr<Token>> argTokens;
	//Not Implemented
	virtual void showArguments(const int nestedLayer);
};
//Flow Control Keyword with arguments
class FlowPKToken :public PKToken, public FlowToken {
public:
	FlowPKToken();
	void showTokenTree(const int nestedLayer)override;
protected:
	//Implemented
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;


	//Not Implemented
};


//Keyword token with arguments
class MPKToken :public PKToken {
public:
	MPKToken();
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;
protected:
	void showArguments(const int nestedLayer)override;

};

class BMPKToken :public MPKToken {
public:
	BMPKToken();
	void setOverloads()final;
	DataType getDataType(TokenResult& tRes)override;
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;
protected:

};

class UPKToken : public PKToken {
public:
	UPKToken();
	void dispatchArguments()override;
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;
protected:
	std::shared_ptr<Token> uniqueToken;
	void showArguments(const int nestedLayer)override;
};


//Keyword token with 1 boolean argument
class CKToken : public UPKToken {
public:
	CKToken();
	void setOverloads()final;	
	virtual void dispatchArguments();
	DataType getDataType(TokenResult& tRes)override;
protected:
};

//Flow Control Keyword with 1 boolean argument
class FlowCKToken :public CKToken, public FlowToken {
public:
	FlowCKToken();
protected:


	bool addCondition(IteratorList<Token>& tl, TokenResult tRes);
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;
	void showArguments(const int nestedLayer)override;
};








class IdentifierToken :public Token {
public:
	IdentifierToken(const std::string& varName);
	DataType getDataType(TokenResult& tRes)override;
protected:

	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;
};

class UnknownToken : public Token {
public:
	UnknownToken();
protected:
};

//each .act file will have a mainToken with differents arguments
class MainToken :public FlowPKToken {
public:
	MainToken();
	void setOverloads()override;
	std::shared_ptr<Tag> execute()override;
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;
protected:
};

class DoLoopToken :public FlowCKToken {
public:
	DoLoopToken();
	std::shared_ptr<Tag> execute()override;
protected:
	
};

class IfToken :public FlowCKToken {
public:
	IfToken();
	std::shared_ptr<Tag> execute()override;
protected:
};

class ElseToken :public FlowKToken {
public:
	ElseToken();
	std::shared_ptr<Tag> execute()override;
protected:
};

class ElifToken :public FlowCKToken {
public:
	ElifToken();
	std::shared_ptr<Tag> execute()override;
protected:
};

class LoopToken :public FlowCKToken {
public:
	LoopToken();
	std::shared_ptr<Tag> execute()override;
protected:
};


class WaitToken :public MPKToken {
public:
	WaitToken();
	void setOverloads()final;
	void dispatchArguments()override;
	std::shared_ptr<Tag> execute()override;
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;
protected:
	std::shared_ptr<Token> numberToken;
	std::shared_ptr<Token> timeTypeToken;
};

class AndToken :public BMPKToken {
public:
	AndToken();
	std::shared_ptr<Tag> execute()override;
protected:
};

class OrToken :public BMPKToken {
public:
	OrToken();
	std::shared_ptr<Tag> execute()override;
protected:
};

class NotToken :public CKToken {
public:
	NotToken();
	std::shared_ptr<Tag> execute()override;
protected:
};





class ListToken :public MPKToken,public TemplateToken {
public:
	ListToken();
	void setOverloads()final;
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;
	std::shared_ptr<Tag> execute()override;
protected:
	DataType dType;
	std::vector<std::shared_ptr<Token>> listToken;
};

class StoreToken :public MPKToken {
public:
	StoreToken();
	void setOverloads()final;
	void dispatchArguments()override;
	std::shared_ptr<Tag> execute()override;
protected:
	template<typename T>
	std::vector<T> test(int i);
	std::shared_ptr<Token> identifierToken;
	std::shared_ptr<Token> valueToken;
	bool addValue(IteratorList<Token>& tl, TokenResult tRes);
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;
};

//empty or 1 parameter token
class IntegerToken :public UPKToken {
public:
	IntegerToken();
	void setOverloads()final;
	DataType getDataType(TokenResult& tRes)override;
	std::shared_ptr<Tag> execute()override;
protected:

};



class FloatToken :public UPKToken {
public:
	FloatToken();
	void setOverloads()final;
	DataType getDataType(TokenResult& tRes)override;
	std::shared_ptr<Tag> execute()override;
protected:

};

class BoolToken :public CKToken {
public:
	BoolToken();
	DataType getDataType(TokenResult& tRes)override;
	std::shared_ptr<Tag> execute()override;
protected:

};
class StringToken :public UPKToken {
public:
	StringToken();
	DataType getDataType(TokenResult& tRes)override;
	void setOverloads()final;
	std::shared_ptr<Tag> execute()override;
protected:
};
class CoordToken :public PKToken {
public:
	void showArguments(const int nestedLayer)override;
	CoordToken();
	void setOverloads()final;
	void dispatchArguments()final;
	DataType getDataType(TokenResult& tRes)override;
protected:
	std::shared_ptr<Token> xPoint;
	std::shared_ptr<Token> yPoint;
	std::shared_ptr<Token> coordToken;

	std::shared_ptr<Tag> execute()override;
};

class ZoneToken :public PKToken {
public:
	ZoneToken();
	void setOverloads()final;
	void dispatchArguments()final;
	void showArguments(const int nestedLayer)override;
	DataType getDataType(TokenResult& tRes)override;
protected:
	std::shared_ptr<Token> topLeft;
	std::shared_ptr<Token> bottomRight;
	std::shared_ptr<Token> zoneToken;

	std::shared_ptr<Tag> execute()override;
};

class DirectionToken :public UPKToken {
public:
	DirectionToken();
	void setOverloads()final;
	DataType getDataType(TokenResult& tRes)override;
protected:
	std::shared_ptr<Tag> execute()override;
};

class CompareToken :public PKToken, public TemplateToken {
public:
	CompareToken();
	void setOverloads()final;
	DataType getDataType(TokenResult& tRes)override;
protected:
	std::vector<std::shared_ptr<Token>> listTokens;
	CompareType cmpType;
	DataType valuesType;
	std::shared_ptr<Tag> execute()override;
};



class PrintToken :public UPKToken {
public:
	PrintToken();
	void setOverloads()final;
protected:
	std::shared_ptr<Tag> execute()override;
};

class BreakToken :public KToken {
public:
	BreakToken();
protected:
	virtual std::shared_ptr<Tag> execute()override;
};

class ContinueToken :public KToken {
public:
	ContinueToken();
protected:
	virtual std::shared_ptr<Tag> execute()override;
};

class PToken :public Token {
public:
protected:
};

class LToken :public Token {
public:
protected:
};


class OpenParenthesisToken :public PToken {
public:
	OpenParenthesisToken();
protected:
};

class CloseParenthesisToken :public PToken {
public:
	CloseParenthesisToken();
protected:
};

class CloseBracketsToken :public PToken {
public:
	CloseBracketsToken();
protected:
};

class OpenBracketsToken :public PToken {
public:
	OpenBracketsToken();
protected:
};

class CloseAngleBracketsToken :public PToken {
public:
	CloseAngleBracketsToken();
protected:
};

class OpenAngleBracketsToken :public PToken {
public:
	OpenAngleBracketsToken();
protected:
};

class CommaToken :public PToken {
public:
	CommaToken();
protected:
};




class FalseToken :public LToken {
public:
	FalseToken();
	DataType getDataType(TokenResult& tRes)override;
protected:
};

class TrueToken :public LToken {
public:
	TrueToken();
	DataType getDataType(TokenResult& tRes)override;
protected:
};

class SecondToken :public LToken {
public:
	SecondToken();
	DataType getDataType(TokenResult& tRes)override;
protected:
};
class MilliSecondToken :public LToken {
public:	MilliSecondToken();
	  DataType getDataType(TokenResult& tRes)override;
protected:

};
class MinuteToken :public LToken {
public:	MinuteToken();
	  DataType getDataType(TokenResult& tRes)override;
protected:

};

class NumericToken :public LToken {
public:
	NumericToken(const std::string& nb);
	DataType getDataType(TokenResult& tRes)override;
protected:

	int number;
};

class StringLiteralToken :public LToken {
public:
	StringLiteralToken(const std::string& content);
	DataType getDataType(TokenResult& tRes)override;
protected:
	std::shared_ptr<Tag> execute()override;
};

template<typename T>
inline std::vector<T> StoreToken::test(int depth)
{
	if (depth > 0) {
		// Create a vector containing the result of a recursive call for the next layer
		return test<std::vector<T>>(depth - 1);
	}
	
}

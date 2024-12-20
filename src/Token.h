#pragma once
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <map>
#include <unordered_set>
#include "Tags.h"
#include "TextManager.h"




std::string getStringLiteral(std::string& text);

enum class DataType {
	NONE, COORD, ZONE, STRING, DIRECTION, FLOAT, INT, BOOL, TIMETYPE,COMPARETYPE,DATATYPE,IDENTIFIER
};

enum class TokenVALUE {
	NOT, TOKEN, UNKNOWN, QUOTATION, OPENANGLEBRACKETS, CLOSEANGLEBRACKETS, FLOW, COMMA, SEMICOLON, NUMERIC, IDENTIFIER,
	CLOSEBRACKETS, OPENBRACKETS, OPENPARENTHESIS, CLOSEPARENTHESIS, STRINGLITERAL, TRUELITERAL, FALSELITERAL, SECOND, WHITESPACE,
	MINUTE, MILLISECOND, INTEGER, WAIT, FLOAT, BOOL, AND, OR, COMPARE, STRING, COORD, DIRECTION, ZONE, LIST, IF, LOOP, DOLOOP,
	SWITCH, DEFAULT, ELSE, ELIF, BREAK, CONTINUE, CASE, STORE, MAIN, PRINT, NORTH, SOUTH, EAST, WEST, NORTHW, NORTHE, SOUTHW, SOUTHE,
	TEMPLATE, STRINGTYPE, INTTYPE, FLOATTYPE, COORDTYPE, ZONETYPE, BOOLTYPE, DIRECTIONTYPE, TIMETYPE,DATATYPE, 
	COMPARETYPE,GREATER,LESSER,GREATEREQUAL,LESSEREQUAL,EQUAL,NOTEQUAL
};


class ValueType {
public:
	ValueType();
	ValueType(DataType t, int l = 0);
	ValueType(const ValueType& vt);
	bool equal(const ValueType& vt);
	DataType type;
	int dim;
};

class Arguments {
public:
	Arguments();

		
	Arguments(ValueType type,bool infinite);
	Arguments(std::vector<ValueType> listTypes);
	std::vector<ValueType> getArgsTypes();
	void next();
	bool ended()const;
	int size();
	bool empty();
	ValueType at(int i);
	bool approveType(const ValueType& type);
	ValueType getCurrentValueType();
	bool isRepeatable();
	bool isValid()const;
	bool isCompleted();
	bool isEqual(Arguments args);
protected:
	ValueType repeatedType;
	bool repeatable;
	bool stillValid;
	bool completed;
	std::vector<ValueType> listArgsTypes;
	int currentIndex;
};

class ArgumentsOverload {
public:
	ArgumentsOverload();
	bool isPresent(Arguments args);
	void addArgs(Arguments args,int index);
	void initTabs();
	void next();
	bool approveType(const ValueType& type);
	std::vector<ValueType> getPossibleValueTypes();
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
	std::vector<ValueType> currentDataTypes;
	std::map<int, std::shared_ptr<Arguments>> mapArguments;
	unsigned int completeIndex;
};


enum class ErrorType { DATATYPE, MISSING, UNEXPECTED };

class Error {
public:
	Error();
	Error(TokenVALUE scopeToken, int l, TokenVALUE errorToken, ErrorType et = ErrorType::MISSING);
	Error(TokenVALUE scopeToken, int l, ValueType dType);
	void showError();
protected:
	ErrorType errorType;
	TokenVALUE errorValue;
	TokenVALUE scopeToken;
	ValueType vType;
	int errorLine;
};

class TokenResult {
public:
	TokenResult();
	TokenResult(TokenVALUE value, int l);
	void showErrors();
	bool addError(TokenVALUE scopeToken,int l,TokenVALUE errorToken, ErrorType et=ErrorType::MISSING);
	bool addError(TokenVALUE scopeToken,int l, ValueType& type);
	bool addError(const TokenResult& tokRes);
	void addVar(const std::string& name, const ValueType& type);
	bool success();
	std::map<std::string, ValueType> getVarTable();
protected:


	std::map<std::string, ValueType> varTable;
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
	virtual ValueType getValueType(TokenResult& tRes);
	virtual void showTokenTree(const int nestedLayer);
	TokenVALUE getValue();
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
	FlowToken();
	FlowToken(int line);
	void showTokenTree(const int nestedLayer);
	bool addToken(TokenVALUE tVal, IteratorList<Token>& tl, TokenResult& tRes);
protected:
	//Implemented
	void printTabs(const int nestedLayer);
	std::vector<std::shared_ptr<Token>> nestedTokens;

	bool addNestedTokens(IteratorList<Token>& tl, TokenResult& tRes);
	bool addBody(IteratorList<Token>& tl, TokenResult& tRes);
	//Not Implemented
	int lineFlow;
};

class TemplateToken {
public:

	TemplateToken();
	TemplateToken(int line);
	virtual void setTemplateOverload()=0;
	virtual void dispatchTemplateArguments()=0;
	bool addToken(TokenVALUE tVal, IteratorList<Token>& tl, TokenResult& tRes);
	bool addType(ValueType tVal, IteratorList<Token>& tl, TokenResult& tRes);
	bool addTypes(std::vector<ValueType> listTypes, IteratorList<Token>& tl, TokenResult& tRes);
	bool addNumber(IteratorList<Token>& tl, TokenResult& tRes);
	bool addTemplatedTypes(IteratorList<Token>& tl, TokenResult& tRes);
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes);
protected:
	ArgumentsOverload templateArguments;
	std::vector<std::shared_ptr<Token>> templTokens;
	TokenResult tRes;
	int templLine;

};



class KToken :public Token {
public:
protected:


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

	bool addType(ValueType tVal, IteratorList<Token>& tl, TokenResult& tRes);
	bool addTypes(std::vector<ValueType> listTypes, IteratorList<Token>& tl, TokenResult& tRes);
	bool addNumber(IteratorList<Token>& tl, TokenResult& tRes);
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
	ValueType getValueType(TokenResult& tRes)override;
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;
protected:

};

class UPKToken : public PKToken {
public:
	UPKToken();
	void dispatchArguments()override;
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
	ValueType getValueType(TokenResult& tRes)override;
protected:
};

//Flow Control Keyword with 1 boolean argument
class FlowCKToken :public CKToken, public FlowToken {
public:
	FlowCKToken();
	void showTokenTree(const int nestedLayer);
protected:
	bool addCondition(IteratorList<Token>& tl, TokenResult tRes);
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;
};








class IdentifierToken :public Token {
public:
	IdentifierToken(const std::string& varName);
	ValueType getValueType(TokenResult& tRes)override;
protected:
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




//list<DATATYPE,INT>()
//list<STRING, 1>(list<STRING,0>("deed","hukj","oijde"),list<STRING,0>("fezf","zef","zgg"))
class ListToken :public MPKToken,public TemplateToken {
public:
	ListToken();
	void setOverloads()final;
	void setTemplateOverload()final;
	void dispatchTemplateArguments()final;
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;
	std::shared_ptr<Tag> execute()override;
protected:
	ValueType vType;
	int tabDim;//1 list of single elem ; 2 list of 1D tab ; 3 list of 2D tabs ; n list of (n-1)D tabs. 
	std::vector<std::shared_ptr<Token>> listToken;
};

//store<DATATYPE,INT>(IDENTIFIER,VALUE)
//store<FLOAT,1>(var1,list<FLOAT,0>(4.2,5.3,6))
class StoreToken :public MPKToken, public TemplateToken {
public:
	StoreToken();
	void setTemplateOverload()final;
	void setOverloads()final;
	void dispatchArguments()override;
	void dispatchTemplateArguments()override;
	std::shared_ptr<Tag> execute()override;
protected:
	ValueType vType;
	template<typename T>
	std::vector<T> test(int i);
	std::shared_ptr<Token> identifierToken;
	std::shared_ptr<Token> valueToken;
	bool addValue(IteratorList<Token>& tl, TokenResult tRes);
	bool addTokens(IteratorList<Token>& tl, TokenResult& tRes)override;
};

//compare<DATATYPE,COMPARETYPE>(VALUE1,VALUE2,VALUE3,...,VALUEN)
//compare<FLOAT,GREATER>(5.3,2.3,1.3,0.6,0.2)
class CompareToken :public PKToken, public TemplateToken {
public:
	CompareToken();
	void setOverloads()final;
	void setTemplateOverload()final;
	void dispatchTemplateArguments()final;
	void dispatchArguments()final;
	ValueType getValueType(TokenResult& tRes)override;
protected:
	std::vector<std::shared_ptr<Token>> listTokens;
	CompareType cmpType;
	ValueType vType;
	std::shared_ptr<Tag> execute()override;
};





//empty or 1 parameter token
class IntegerToken :public UPKToken {
public:
	IntegerToken();
	void setOverloads()final;

	ValueType getValueType(TokenResult& tRes)override;
	std::shared_ptr<Tag> execute()override;
protected:

};



class FloatToken :public UPKToken {
public:
	FloatToken();
	void setOverloads()final;
	ValueType getValueType(TokenResult& tRes)override;
	std::shared_ptr<Tag> execute()override;
protected:

};

class BoolToken :public CKToken {
public:
	BoolToken();
	ValueType getValueType(TokenResult& tRes)override;
	std::shared_ptr<Tag> execute()override;
protected:

};
class StringToken :public UPKToken {
public:
	StringToken();
	ValueType getValueType(TokenResult& tRes)override;
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
	ValueType getValueType(TokenResult& tRes)override;
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
	ValueType getValueType(TokenResult& tRes)override;
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
	ValueType getValueType(TokenResult& tRes)override;
protected:
	std::shared_ptr<Tag> execute()override;
};

class PrintToken :public MPKToken {
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

ValueType getDescribedType(const std::shared_ptr<Token>& t);


class DataTypeToken :public LToken {
public:
	DataTypeToken();
	ValueType getValueType(TokenResult& tRes)final;
protected:
};
//INT
class IntTypeToken :public DataTypeToken {
public:
	IntTypeToken();
protected:
};
//BOOL
class BoolTypeToken :public DataTypeToken {
public:
	BoolTypeToken();
protected:
};
//STRING
class StringTypeToken :public DataTypeToken {
public:
	StringTypeToken();
protected:
};
//FLOAT
class FloatTypeToken :public DataTypeToken {
public:
	FloatTypeToken();
protected:
};
//COORD
class CoordTypeToken :public DataTypeToken {
public:
	CoordTypeToken();
protected:
};
//ZONE
class ZoneTypeToken :public DataTypeToken {
public:
	ZoneTypeToken();
protected:
};
//DIRECTION
class DirectionTypeToken :public DataTypeToken {
public:
	DirectionTypeToken();
protected:
};
//TIMETYPE
class TimeTypeToken :public DataTypeToken {
public:
	TimeTypeToken();
};
class CompareTypeToken :public DataTypeToken {
public:
	CompareTypeToken();
	CompareType getCmpType();
protected:
};

class GreaterToken :public CompareTypeToken {
public:
	GreaterToken();
};

class LesserToken :public CompareTypeToken {
public:
	LesserToken();
};

class GreaterequalToken :public CompareTypeToken {
public:
	GreaterequalToken();
};

class LesserequalToken :public CompareTypeToken {
public:
	LesserequalToken();
};

class EqualToken :public CompareTypeToken {
public:
	EqualToken();
};

class NotequalToken :public CompareTypeToken {
public:
	NotequalToken();
};

class FalseToken :public LToken {
public:
	FalseToken();
	ValueType getValueType(TokenResult& tRes)override;
protected:
};

class TrueToken :public LToken {
public:
	TrueToken();
	ValueType getValueType(TokenResult& tRes)override;
protected:
};

class SecondToken :public LToken {
public:
	SecondToken();
	ValueType getValueType(TokenResult& tRes)override;
protected:
};
class MilliSecondToken :public LToken {
public:	MilliSecondToken();
	  ValueType getValueType(TokenResult& tRes)override;
protected:

};
class MinuteToken :public LToken {
public:	MinuteToken();
	  ValueType getValueType(TokenResult& tRes)override;
protected:

};

class DirectionLToken :public LToken {
public:	
	  ValueType getValueType(TokenResult& tRes)final;
protected:

};

class NorthToken :public DirectionLToken {
public:
	NorthToken();
};

class NorthWToken :public DirectionLToken {
public:
	NorthWToken();
};

class NorthEToken :public DirectionLToken {
public:
	NorthEToken();
};

class SouthToken :public DirectionLToken {
public:
	SouthToken();
};

class SouthWToken :public DirectionLToken {
public:
	SouthWToken();
};

class SouthEToken :public DirectionLToken {
public:
	SouthEToken();
};


class NumericToken :public LToken {
public:
	NumericToken(const std::string& nb);
	ValueType getValueType(TokenResult& tRes)override;
protected:

	int number;
};

class StringLiteralToken :public LToken {
public:
	StringLiteralToken(const std::string& content);
	void showTokenTree(const int nestedLayer)override;
	ValueType getValueType(TokenResult& tRes)override;
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

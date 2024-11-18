#include "Token.h"

bool isTokenString(const std::string& text)
{
	return isKeywordString(text) || isLiteralString(text) || isPunctuationString(text) || isOperatorString(text);
}

bool isKeywordString(const std::string& text)
{
	for (auto& tokenString : allKeywordsTokensString) {
		if (tokenString.compare(text) == 0)return true;
	}
	return false;
}
bool isBooleanToken(const std::shared_ptr<Token>& token) {
	if (token) {
		return token->getDataType() == DataType::BOOL;
	}
	return false;
}
//ADD TABLE OF ID + TYPES and then implement search in it
bool isNumericToken(const std::shared_ptr<Token>& token) {
	if (token) {
		auto d = token->getDataType();
		return d == DataType::INT || d == DataType::FLOAT;
	}
	return false;
}

bool isTimeTypeToken(const std::shared_ptr<Token>& token) {
	if (token) {
		return token->getDataType() == DataType::TIMETYPE;
	}
	return false;
}

bool isStringToken(const std::shared_ptr<Token>& token) {
	if (token) {
		return token->getDataType() == DataType::STRING;
	}
	return false;
}

bool isIntegerToken(const std::shared_ptr<Token>& token) {
	if (token) {
		return token->getDataType() == DataType::INT;
	}
	return false;
}
bool isBoolToken(const std::shared_ptr<Token>& token) {
	if (token) {
		return token->getDataType() == DataType::BOOL;
	}
	return false;
}
bool isCoordToken(const std::shared_ptr<Token>& token) {
	if (token) {
		return token->getDataType() == DataType::COORD;
	}
	return false;
}
bool isDirectionToken(const std::shared_ptr<Token>& token) {
	if (token) {
		return token->getDataType() == DataType::DIRECTION;
	}
	return false;
}
bool isFloatToken(const std::shared_ptr<Token>& token) {
	if (token) {
		return token->getDataType() == DataType::FLOAT;
	}
	return false;
}
bool isZoneToken(const std::shared_ptr<Token>& token) {
	if (token) {
		return token->getDataType() == DataType::ZONE;
	}
	return false;
}

bool isLiteralString(const std::string& text)
{
	for (auto& tokenString : allLiteralsTokensStrings) {
		if (tokenString.compare(text) == 0)return true;
	}
	return false;
}

bool isPunctuationString(const std::string& text)
{
	for (auto& tokenString : allPunctuationsTokensStrings) {
		if (tokenString.compare(text) == 0)return true;
	}
	return false;
}

bool isOperatorString(const std::string& text)
{
	for (auto& tokenString : allOperatorsTokensStrings) {
		if (tokenString.compare(text) == 0)return true;
	}
	return false;
}

bool isTokenStringContained(const std::string& text) {
	for (auto& word : allTokensStrings) {
		if (text.find(word) != std::string::npos)return true;
	}
	return false;
}


std::string getNextNumeric(const std::string str) {
	std::string digits = "0123456789";
	size_t firstDigitPos = str.find_first_of(digits);
	if (firstDigitPos != std::string::npos)
	{
		std::size_t const firstNonDigitPos = str.find_first_not_of(digits, firstDigitPos);
		return str.substr(firstDigitPos, firstNonDigitPos == std::string::npos ? firstNonDigitPos - firstDigitPos : firstNonDigitPos);
	}
}



bool isIdentifier(const std::shared_ptr<Token>& token) {
	if (token)return token->getValue() == TokenVALUE::IDENTIFIER;
	return false;
}



//Doesn't have an escape character to store "
std::string getStringLiteral(std::string& text) {
	std::string temp = getStringBefore(text, quotation);
	if (temp.empty())return text;
	else return temp;
}





IdentifierToken::IdentifierToken(const std::string& varName)
{
	tValue = TokenVALUE::IDENTIFIER;
	tokenText = varName;
}


bool IdentifierToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	tl.next();
	return true;
}

MainToken::MainToken() :FlowPKToken()
{

	tValue = TokenVALUE::MAIN;
	tokenText = mainK;
	canEnd = true;
}

void MainToken::setOverloads()
{
	argsOverload.addArgs(Arguments());
	//Set the functions DataType in a file;
	argsOverload.addArgs(Arguments());
}



//Leave empty
bool PKToken::handleArguments(IteratorList<Token>& tl, TokenResult& tRes)
{
 
	DataType correctType;
	if (!argsOverload.empty()) {
		correctType = argsOverload.getFirst().front();
		while (!tl.ended() && (!argsOverload.ended()/*|| repeat*/)) {
			auto elem = tl.currentToken();
			if (mustEnd) {
				return addCp(tl, tRes);
			}
			else if (mustComma) {
				if (addComma(tl,tRes)) {
					mustComma = false;
					continue;
				}
				else break;
			}
			else {
				//if (!1)correctType = argsOverload.getCurrentDataType();
				if (addCorrectType(tl,tRes, correctType, elem))continue;
				else break;
			}
		}
	}
	return false;
}

void PKToken::setOverloads()
{
	argsOverload = ArgumentsOverload();
}

bool PKToken::addCorrectType(IteratorList<Token>& tl, TokenResult& tRes, DataType correctType, std::shared_ptr<Token> elem) {
	if (elem->getDataType() == correctType) {
		if (elem->addTokens(tl, tRes)) {
			mustComma = true;
			argTokens.push_back(elem);		
			if (!1) {
				argsOverload.next();
				if (argsOverload.ended())mustEnd = true;
			}
		}
	}
	return false;
}










void PKToken::showTokenTree(const int nestedLayer)
{
	Token::showTokenTree(nestedLayer);
	std::cout << openParenthesisP;
	showArguments(nestedLayer);
	std::cout << closeParenthesisP;
}

void PKToken::showArguments(const int nestedLayer)
{

}

PKToken::PKToken() :KToken()
{
	argsOverload = ArgumentsOverload();
	mustComma = mustEnd=canEnd = false;
	setOverloads();
}

bool Token::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	return true;
}

bool KToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	tl.next();
	return true;
}

bool PKToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	KToken::addTokens(tl, tRes);
	if (addOp(tl,tRes)) {
		return handleArguments(tl, tRes);
	}
	return false;
}


bool UPKToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	//same as PKToken, handleArguments gets only 1 token
	return PKToken::addTokens(tl, tRes);
}
bool FlowKToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	if (KToken::addTokens(tl, tRes)) {
		return FlowToken::addBody(tl, tRes);
	}
	return false;
}
bool FlowPKToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	if (PKToken::addTokens(tl, tRes)){
		return FlowToken::addBody(tl, tRes);
	}
	return false;
}

bool MainToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	FlowPKToken::addTokens(tl, tRes);
	//handle main logic
	return false;
}

bool FlowCKToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	if (CKToken::addTokens(tl, tRes)) {
		return FlowToken::addBody(tl, tRes);
	}
	return false;
}

bool WaitToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	return MPKToken::addTokens(tl, tRes);
}

bool StoreToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	return MPKToken::addTokens(tl, tRes);
}




bool ListToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	if (TemplateToken::addTokens(tl, tRes)) {
		return MPKToken::addTokens(tl, tRes);
	}
	return false;
}

bool TemplateToken::addNumber(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType({ DataType::INT,DataType::FLOAT },tl,tRes);
}

bool PKToken::addNumber(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType({ DataType::INT,DataType::FLOAT },tl,tRes);
}


bool PKToken::addToken(TokenVALUE tVal, IteratorList<Token>& tl, TokenResult& tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->getValue()==tVal) {
			return elem->addTokens(tl, tRes);
		}
	}
	return addError(tRes,tVal);
}
bool TemplateToken::addToken(TokenVALUE tVal, IteratorList<Token>& tl, TokenResult& tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->getValue() == tVal) {
			return elem->addTokens(tl, tRes);
		}
	}
	return addError(tRes, tVal);
}

bool PKToken::addType(DataType tVal, IteratorList<Token>& tl, TokenResult& tRes)
{
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->getDataType() == tVal) {
			return elem->addTokens(tl, tRes);
		}
	}
	return addError(tRes, tVal);
}
bool PKToken::addType(std::vector<DataType> listTypes, IteratorList<Token>& tl, TokenResult& tRes)
{
	for (auto t : listTypes) {
		if (addType(t, tl, tRes))return true;
	}
	return false;
}

bool TemplateToken::addType(DataType tVal, IteratorList<Token>& tl, TokenResult& tRes)
{
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->getDataType() == tVal) {
			return elem->addTokens(tl, tRes);
		}
	}
	return addError(tRes, tVal);
}
bool TemplateToken::addType(std::vector<DataType> listTypes, IteratorList<Token>& tl, TokenResult& tRes)
{
	for (auto t : listTypes) {
		if (addType(t, tl, tRes))return true;
	}
	return false;
}

bool PKToken::addDataType(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::DATATYPE, tl, tRes);
}
bool PKToken::addInteger(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::INT, tl, tRes);
}

bool PKToken::addFloat(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::FLOAT, tl, tRes);
}

bool PKToken::addCoord(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::COORD, tl, tRes);
}

bool PKToken::addZone(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::ZONE, tl, tRes);
}

bool PKToken::addString(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::STRING, tl, tRes);
}

bool PKToken::addComma(IteratorList<Token>& tl, TokenResult& tRes) {
	return addToken(TokenVALUE::COMMA, tl, tRes);
}

bool PKToken::addTimeType(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::TIMETYPE, tl, tRes);
}

bool PKToken::addBool(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::BOOL, tl, tRes);
}

bool PKToken::addOp(IteratorList<Token>& tl, TokenResult& tRes)
{
	return addToken(TokenVALUE::OPENPARENTHESIS, tl, tRes);
}

bool PKToken::addCp(IteratorList<Token>& tl, TokenResult& tRes)
{
	return addToken(TokenVALUE::CLOSEPARENTHESIS, tl, tRes);
}

bool TemplateToken::addDataType(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::DATATYPE, tl, tRes);
}
bool TemplateToken::addInteger(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::INT, tl, tRes);
}

bool TemplateToken::addFloat(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::FLOAT, tl, tRes);
}

bool TemplateToken::addCoord(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::COORD, tl, tRes);
}

bool TemplateToken::addZone(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::ZONE, tl, tRes);
}

bool TemplateToken::addString(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::STRING, tl, tRes);
}
bool TemplateToken::addComma(IteratorList<Token>& tl, TokenResult& tRes) {
	return addToken(TokenVALUE::COMMA, tl, tRes);
}

bool TemplateToken::addTimeType(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::TIMETYPE, tl, tRes);
}

bool TemplateToken::addBool(IteratorList<Token>& tl, TokenResult& tRes) {
	return addType(DataType::BOOL, tl, tRes);
}

bool TemplateToken::addIdentifier(IteratorList<Token>& tl, TokenResult& tRes)
{
	return addToken(TokenVALUE::IDENTIFIER,tl,tRes);
}





DataType IdentifierToken::getDataType()
{
	return DataType::IDENTIFIER;
}

bool PKToken::addIdentifier(IteratorList<Token>& tl, TokenResult& tRes) {
	return addToken(TokenVALUE::IDENTIFIER, tl, tRes);
}

bool isValue(std::shared_ptr<Token> token) {
	return token->getDataType() != DataType::NONE;
}

bool isType(std::shared_ptr<Token> token) {
	static const std::unordered_set<TokenVALUE> validTypes = {
		TokenVALUE::INTEGER, TokenVALUE::FLOAT, TokenVALUE::BOOL,
		TokenVALUE::COORD, TokenVALUE::ZONE, TokenVALUE::DIRECTION,
		TokenVALUE::STRING
	};
	return validTypes.count(token->getValue()) > 0;
}

bool StoreToken::addValue(IteratorList<Token>& tl, TokenResult tRes) {
	//if (!tl.ended()) {
	//	auto elem = tl.currentToken();
	//	auto res = elem->addTokens(tl, tRes);
	//	if (res.success()) {
	//		if (isValue(elem)) {
	//			valueToken = elem;
	//			tRes.addVar(identifierToken->getTokenText(), valueToken->getDataType());
	//			return elem;
	//		}
	//	}
	//	else updateRes(res);
	//	return elem;
	//}
	//addError(TokenVALUE::INTEGER);
	return false;
}



bool FlowToken::addCb(IteratorList<Token>& tl, TokenResult& tRes)
{
	return addToken(TokenVALUE::CLOSEBRACKETS, tl, tRes);
}

bool FlowToken::addNestedTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->hasValue(TokenVALUE::CLOSEBRACKETS)) {
			return addCb(tl,tRes);
		}
		else {
			if (elem->addTokens(tl, tRes)) {
				nestedTokens.push_back(elem);
			}
		}
	}
	return addError(tRes, TokenVALUE::CLOSEBRACKETS);
}

bool FlowToken::addBody(IteratorList<Token>& tl, TokenResult& tRes)
{
	//no tl.next() because there's no keyword
	if (addOb(tl,tRes)) {
		return addNestedTokens(tl, tRes);
	}
	return false;
}

bool FlowToken::addOb(IteratorList<Token>& tl, TokenResult& tRes)
{
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->hasValue( TokenVALUE::OPENBRACKETS)) {
			tl.next();
			return false;
		}
	}
	addError(tRes,TokenVALUE::OPENBRACKETS);
	return false;
}



FlowPKToken::FlowPKToken() :PKToken(), FlowToken(line)
{

}

void FlowPKToken::setOverloads()
{
	PKToken::setOverloads();
}


FlowKToken::FlowKToken() :KToken(), FlowToken(line)
{

}

void FlowKToken::showTokenTree(const int nestedLayer)
{
	Token::showTokenTree(nestedLayer);
	std::cout << openBracketsP + '\n';
	for (auto elem : nestedTokens) {
		elem->showTokenTree(nestedLayer+1);
	}
	std::cout << closeBracketsP + '\n';

}



void FlowPKToken::showTokenTree(const int nestedLayer)
{
	PKToken::showTokenTree(nestedLayer);
	FlowToken::showTokenTree(nestedLayer);
}


std::shared_ptr<Token> getStringLiteralToken(std::string& stringLiteral) {
	return std::make_shared<StringLiteralToken>(stringLiteral);
}



FlowCKToken::FlowCKToken() :CKToken(), FlowToken(line)
{
}



bool FlowCKToken::addCondition(IteratorList<Token>& tl, TokenResult tRes)
{

	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (addBool(tl, tRes)) {
			condition = elem;
			return true;
		}
	}
	return false;
}



void FlowCKToken::showArguments(const int nestedLayer)
{
	condition->showTokenTree(nestedLayer);
}






std::shared_ptr<Tag> MainToken::execute()
{
	return std::shared_ptr<Tag>();
}


DoLoopToken::DoLoopToken():FlowCKToken()
{

	tValue = TokenVALUE::DOLOOP;
	tokenText = doloopK;
}

std::shared_ptr<Tag> DoLoopToken::execute()
{
	return std::shared_ptr<Tag>();
}

IfToken::IfToken() :FlowCKToken()
{
	tValue = TokenVALUE::IF;
	tokenText = ifK;
}


std::shared_ptr<Tag> IfToken::execute()
{
	return std::shared_ptr<Tag>();
}

ElseToken::ElseToken() :FlowKToken()
{
	tValue = TokenVALUE::ELSE;
	tokenText = elseK;
}

std::shared_ptr<Tag> ElseToken::execute()
{
	return std::shared_ptr<Tag>();
}

ElifToken::ElifToken() :FlowCKToken()
{
	tValue = TokenVALUE::ELIF;
	tokenText = elifK;
}



std::shared_ptr<Tag> ElifToken::execute()
{
	return std::shared_ptr<Tag>();
}

LoopToken::LoopToken() :FlowCKToken()
{
	tValue = TokenVALUE::LOOP;
	tokenText = loopK;
}

std::shared_ptr<Tag> LoopToken::execute()
{
	return std::shared_ptr<Tag>();
}

BoolToken::BoolToken()
{
	tValue = TokenVALUE::BOOL;
	tokenText = boolK;
}

DataType BoolToken::getDataType()
{
	return DataType::BOOL;
}

std::shared_ptr<Tag> BoolToken::execute()
{
	return std::shared_ptr<Tag>();
}

WaitToken::WaitToken() :MPKToken()
{
	tValue = TokenVALUE::WAIT;
	tokenText = waitK;
}

void WaitToken::setOverloads()
{
	argsOverload.addArgs(Arguments({ DataType::INT,DataType::TIMETYPE }));
	argsOverload.addArgs(Arguments({ DataType::FLOAT,DataType::TIMETYPE }));
}



std::shared_ptr<Tag> WaitToken::execute()
{
	return std::shared_ptr<Tag>();
}

AndToken::AndToken() :BMPKToken()
{

	tValue = TokenVALUE::AND;
	tokenText = andK;
}





CKToken::CKToken() :UPKToken()
{

}

void CKToken::setOverloads()
{
	argsOverload.addArgs(Arguments(DataType::BOOL,false));
}

DataType CKToken::getDataType()
{
	return DataType::BOOL;
}


std::shared_ptr<Tag> AndToken::execute()
{
	return std::shared_ptr<Tag>();
}

void AndToken::showArguments(const int nestedLayer)
{
	for (auto arg : listBoolToken) {
		arg->showTokenTree(nestedLayer);
	}
}

OrToken::OrToken() :BMPKToken()	
{
	tValue = TokenVALUE::OR;
	tokenText = orK;
}





std::shared_ptr<Tag> OrToken::execute()
{
	return std::shared_ptr<Tag>();
}

NotToken::NotToken()
{
	tValue = TokenVALUE::NOT;
	tokenText = notK;
}

std::shared_ptr<Tag> NotToken::execute()
{
	return std::shared_ptr<Tag>();
}

FalseToken::FalseToken()
{
	tValue = TokenVALUE::FALSELITERAL;
	tokenText = falseL;
}

DataType FalseToken::getDataType()
{
	return DataType::BOOL;
}

TrueToken::TrueToken()
{
	tValue = TokenVALUE::TRUELITERAL;
	tokenText = trueL;
}

DataType TrueToken::getDataType()
{
	return DataType::BOOL;
}

OpenParenthesisToken::OpenParenthesisToken()
{
	tValue = TokenVALUE::OPENPARENTHESIS;
	tokenText = openParenthesisP;
}

CloseParenthesisToken::CloseParenthesisToken()
{
	tValue = TokenVALUE::CLOSEPARENTHESIS;
	tokenText = closeParenthesisP;
}

CloseBracketsToken::CloseBracketsToken()
{
	tValue = TokenVALUE::CLOSEBRACKETS;
	tokenText = closeBracketsP;
}

OpenBracketsToken::OpenBracketsToken()
{
	tValue = TokenVALUE::OPENBRACKETS;
	tokenText = openBracketsP;
}

CommaToken::CommaToken()
{
	tValue = TokenVALUE::COMMA;
	tokenText = commaP;
}

ContinueToken::ContinueToken()
{
	tValue = TokenVALUE::CONTINUE;
	tokenText = continueK;
}


std::shared_ptr<Tag> ContinueToken::execute()
{
	return std::make_shared<ContinueTag>();
}

BreakToken::BreakToken()
{
	tValue = TokenVALUE::BREAK;
	tokenText = breakK;
}


std::shared_ptr<Tag> BreakToken::execute()
{
	return std::make_shared<BreakTag>();
}

PrintToken::PrintToken()
{
	tValue = TokenVALUE::PRINT;
	tokenText = printK;
}

void PrintToken::setOverloads()
{
	argsOverload.addArgs(Arguments(DataType::STRING,true));
}

std::shared_ptr<Tag> PrintToken::execute()
{
	return std::shared_ptr<Tag>();
}

DirectionToken::DirectionToken()
{
	tValue = TokenVALUE::DIRECTION;
	tokenText = directionK;
}

void DirectionToken::setOverloads()
{
	argsOverload.addArgs(Arguments());
	argsOverload.addArgs(Arguments(DataType::DIRECTION, false));
}



DataType DirectionToken::getDataType()
{
	return DataType::DIRECTION;
}

std::shared_ptr<Tag> DirectionToken::execute()
{
	return std::shared_ptr<Tag>();
}

ZoneToken::ZoneToken()
{
	tokenText = zoneK;
	tValue = TokenVALUE::ZONE;
}

void ZoneToken::setOverloads()
{
	argsOverload.addArgs(Arguments());
	argsOverload.addArgs(Arguments(DataType::ZONE, false));
}

DataType ZoneToken::getDataType()
{
	return DataType::ZONE;
}



std::shared_ptr<Tag> ZoneToken::execute()
{
	return std::shared_ptr<Tag>();
}

CoordToken::CoordToken()
{
	tokenText = coordK;
	tValue = TokenVALUE::COORD;
}

void CoordToken::setOverloads()
{
	argsOverload.addArgs(Arguments());
	argsOverload.addArgs(Arguments(DataType::COORD, false));
}

DataType CoordToken::getDataType()
{
	return DataType::COORD;
}





std::shared_ptr<Tag> CoordToken::execute()
{
	if (coordToken) {
		return std::make_shared<CoordTag>(coordToken->execute());
	}
	else if (xPoint && yPoint) {
		return std::make_shared<CoordTag>(xPoint->execute(), yPoint->execute());
	}
	else return std::make_shared<CoordTag>();
}

CompareToken::CompareToken()
{
	tokenText = compareK;
	tValue = TokenVALUE::COMPARE;
}

void CompareToken::setOverloads()
{
	switch (valuesType) {
		case DataType::BOOL:
			argsOverload.addArgs(Arguments(DataType::BOOL,true));
			break;
		case DataType::COORD:
			argsOverload.addArgs(Arguments(DataType::COORD, true));
			break;
		case DataType::DATATYPE:
			argsOverload.addArgs(Arguments(DataType::DATATYPE, true));
			break;
		case DataType::DIRECTION:
			argsOverload.addArgs(Arguments(DataType::DIRECTION, true));
			break;
		case DataType::FLOAT:
			argsOverload.addArgs(Arguments(DataType::FLOAT, true));
			break;
		case DataType::INT:
			argsOverload.addArgs(Arguments(DataType::INT, true));
			break;
		case DataType::STRING:
			argsOverload.addArgs(Arguments(DataType::STRING, true));
			break;
	}
}

DataType CompareToken::getDataType()
{
	return DataType::BOOL;
}

std::shared_ptr<Tag> CompareToken::execute()
{
	return std::shared_ptr<Tag>();
}

FloatToken::FloatToken()
{
	tokenText = floatK;
	tValue = TokenVALUE::FLOAT;
}

void FloatToken::setOverloads()
{
	argsOverload.addArgs(Arguments());
	argsOverload.addArgs(Arguments(DataType::FLOAT, false));
}

DataType FloatToken::getDataType()
{
	return DataType::FLOAT;
}

std::shared_ptr<Tag> FloatToken::execute()
{
	return std::make_shared<FloatTag>(floatToken->execute());
}

IntegerToken::IntegerToken()
{
	tokenText = intK;
	tValue = TokenVALUE::INTEGER;
}

void IntegerToken::setOverloads()
{
	argsOverload.addArgs(Arguments());
	argsOverload.addArgs(Arguments(DataType::INT,false));
}

DataType IntegerToken::getDataType()
{
	return DataType::INT;
}



std::shared_ptr<Tag> IntegerToken::execute()
{
	return std::make_shared<IntTag>(intToken->execute());
}

StoreToken::StoreToken()
{
	tokenText = storeK;
	tValue = TokenVALUE::STORE;
}

StoreToken::StoreToken(DataType type, int tabDim):StoreToken()
{

}

void StoreToken::setOverloads()
{

}

std::shared_ptr<Tag> StoreToken::execute()
{
	return std::shared_ptr<Tag>();
}

ListToken::ListToken()
{
	tokenText = listK;
	dType = DataType::NONE;
	tValue = TokenVALUE::LIST;
}

std::shared_ptr<Tag> ListToken::execute()
{
	return std::shared_ptr<Tag>();
}

StringToken::StringToken()
{
	tokenText = stringK;
	tValue = TokenVALUE::STRING;
}

void StringToken::setOverloads()
{
	argsOverload.addArgs(Arguments());
	argsOverload.addArgs(Arguments(DataType::STRING, false));
}

std::shared_ptr<Tag> StringToken::execute()
{
	return std::shared_ptr<Tag>();
}

SecondToken::SecondToken()
{
	tokenText = secondL;
	tValue = TokenVALUE::SECOND;
}

DataType SecondToken::getDataType()
{
	return DataType::TIMETYPE;
}

MilliSecondToken::MilliSecondToken()
{
	tokenText = millisecondL;
	tValue = TokenVALUE::MILLISECOND;
}

DataType MilliSecondToken::getDataType()
{
	return DataType::TIMETYPE;
}

MinuteToken::MinuteToken()
{
	tokenText = minuteL;
	tValue = TokenVALUE::MINUTE;
}

DataType MinuteToken::getDataType()
{
	return DataType::TIMETYPE;
}

NumericToken::NumericToken(const std::string& nb)
{
	tValue = TokenVALUE::NUMERIC;
	tokenText = nb;
	number = std::stoi(nb);
}

DataType NumericToken::getDataType()
{
	return DataType::INT;
}

UnknownToken::UnknownToken()
{
	tValue = TokenVALUE::UNKNOWN;
	tokenText = "Unknown";
}

StringLiteralToken::StringLiteralToken(const std::string& content) :LToken()
{
	tokenText = content;
	tValue = TokenVALUE::STRINGLITERAL;
}



std::shared_ptr<Tag> StringLiteralToken::execute()
{
	return std::shared_ptr<Tag>();
}

CloseAngleBracketsToken::CloseAngleBracketsToken()
{
	tokenText = closeAngleBracketsP;
	tValue = TokenVALUE::CLOSEANGLEBRACKETS;
}

OpenAngleBracketsToken::OpenAngleBracketsToken()
{
	tokenText = openAngleBracketsP;
	tValue = TokenVALUE::OPENANGLEBRACKETS;
}











TokenResult::TokenResult()
{
	varTable = std::map<std::string, DataType>();
	listErrors = std::vector<Error>();
}

TokenResult::TokenResult(TokenVALUE value, int l) :TokenResult()
{
}

void TokenResult::showErrors()
{
	for (auto& error : listErrors) {
		error.showError();
	}
}

void TokenResult::addError(int l,TokenVALUE value, ErrorType et)
{
	listErrors.push_back(Error(l,value, et));
}

void TokenResult::addError(int l,DataType type)
{
	listErrors.push_back(Error(l,type));
}

void TokenResult::addError(const TokenResult& tRes)
{
	listErrors.insert(listErrors.end(), tRes.listErrors.begin(), tRes.listErrors.end());
}

void TokenResult::addVar(const std::string& name, const DataType& type)
{
	varTable.insert(std::pair<std::string, DataType>(name, type));
}

bool TokenResult::success()
{
	return listErrors.empty();
}

std::map<std::string, DataType> TokenResult::getVarTable()
{
	return varTable;
}

Token::Token()
{
	
	tValue = TokenVALUE::TOKEN;
	tokenText = "Token";
}

Error::Error()
{
	dType = DataType::NONE;
	errorType = ErrorType::DATATYPE;
}

Error::Error(int el, TokenVALUE tv, ErrorType et) :Error()
{
	errorType = et;
	errorValue = tv;
	errorLine = el;
}

Error::Error(int el, DataType dType) :Error()
{
	errorLine = el;
	this->dType = dType;
}

void Error::showError()
{
}

bool Token::addError(TokenResult& tRes,TokenVALUE value, ErrorType et)
{
	tRes.addError(line,value, et );
	return false;
}

bool Token::addError(TokenResult& tRes,DataType type)
{
	tRes.addError(line,type);
	return false;
}

bool FlowToken::addError(TokenResult& tRes, TokenVALUE value, ErrorType et)
{
	tRes.addError(lineFlow, value, et);
	return false;
}

bool FlowToken::addError(TokenResult& tRes, DataType type)
{
	tRes.addError(lineFlow, type);
	return false;
}


std::string Token::getTokenText()
{
	return tokenText;
}

int Token::getLine()
{
	return line;
}

bool Token::hasValue(TokenVALUE value) const
{
	return tValue==value;
}

FlowToken::FlowToken()
{

}

FlowToken::FlowToken(int line):FlowToken()
{
	lineFlow = line;
}



void FlowToken::showTokenTree(const int nestedLayer)
{
	std::cout << openBracketsP + "\n";
	printTabs(nestedLayer + 1);
	for (auto elem : nestedTokens) {
		elem->showTokenTree(nestedLayer + 1);
	}
	std::cout << '\n';
	printTabs(nestedLayer);
	std::cout << closeBracketsP + '\n';
}

bool FlowToken::addToken(TokenVALUE tVal, IteratorList<Token>& tl, TokenResult& tRes)
{
	return false;
}

bool FlowToken::addType(DataType tVal, IteratorList<Token>& tl, TokenResult& tRes)
{
	return false;
}

bool FlowToken::addType(std::vector<DataType> listTypes, IteratorList<Token>& tl, TokenResult& tRes)
{
	return false;
}

void FlowToken::printTabs(const int nestedLayer)
{
	for (int i = 0; i < nestedLayer; ++i) {
		std::cout << '\t';
	}
}

void Token::showTokenTree(const int nestedLayer)
{
	std::cout << tokenText;
}

TokenVALUE Token::getValue()
{
	return this->tValue;
}

DataType Token::getDataType()
{
	return DataType::NONE;
}




std::shared_ptr<Tag> Token::execute()
{
	return std::shared_ptr<Tag>();
}

UPKToken::UPKToken() :PKToken()
{
}

void UPKToken::setOverloads()
{
	PKToken::setOverloads();
}




bool UPKToken::checkType(std::shared_ptr<Token>& elem)
{
	return elem->getDataType() == DataType::NONE;
}

void UPKToken::showArguments(const int nestedLayer)
{
	if (!argTokens.empty())argTokens.front()->showTokenTree(nestedLayer);
}

MPKToken::MPKToken() :PKToken()
{

}

void MPKToken::setOverloads()
{
	PKToken::setOverloads();
}

bool MPKToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	return PKToken::addTokens(tl, tRes);
}


void MPKToken::showArguments(const int nestedLayer)
{
	for (auto elem : argTokens) {
		elem->showTokenTree(nestedLayer);
	}
}

Arguments::Arguments()
{
	currentIndex = 0;
	valid = true;
}

Arguments::Arguments(DataType type, bool repeat):Arguments()
{
	if (repeat) {
		repeatable = true;
		repeatedType = type;
	}
	else listArgsTypes.push_back(type);
}

Arguments::Arguments(std::vector<DataType> listTypes) :Arguments()
{
	listArgsTypes = listTypes;
}


std::vector<DataType> Arguments::getArgsTypes()
{
	return listArgsTypes;
}

void Arguments::next()
{
	++currentIndex;
}

DataType Arguments::getFirst()
{
	return this->listArgsTypes.front();
}

bool Arguments::ended()
{
	return currentIndex >= listArgsTypes.size();
}

int Arguments::size()
{
	return listArgsTypes.size();
}

bool Arguments::empty()
{
	return getArgsTypes().empty();
}

DataType Arguments::at(int i)
{
	if (i < size())return listArgsTypes.at(i);
}

DataType Arguments::getCurrentDataType()
{
	return listArgsTypes.at(currentIndex);
}

bool Arguments::isRepeatable()
{
	return repeatable;
}

bool Arguments::isValid()
{
	return valid;
}

bool Arguments::isEqual(Arguments args)
{
	if (repeatable != args.repeatable)return false;
	if (args.size() != size())return false;

	for (int i = 0; i < args.size(); ++i) {
		if (args.at(i) != at(i))return false;
	}
	return true;
}

BMPKToken::BMPKToken():MPKToken()
{
}

void BMPKToken::setOverloads()
{
	argsOverload=ArgumentsOverload(Arguments(DataType::BOOL, true));
}

DataType BMPKToken::getDataType()
{
	return DataType::BOOL;
}

bool BMPKToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	return MPKToken::addTokens(tl,tRes);
}

bool ArgumentsOverload::isValid()
{
	for (auto arg:listArguments) {
		if (arg.isValid())return true;
	}
	return false;
}

ArgumentsOverload::ArgumentsOverload()
{
	currentIndex = 0;
	listArguments.clear();
	currentDataTypes.clear();
}

ArgumentsOverload::ArgumentsOverload(Arguments args):ArgumentsOverload()
{
	addArgs(args);
}

ArgumentsOverload::ArgumentsOverload(std::vector<Arguments> ovArgs):ArgumentsOverload()
{
	listArguments  =   ovArgs;
}

bool ArgumentsOverload::isPresent(Arguments args)
{
	for (auto arg : listArguments) {
		if (arg.isEqual(args))return true;
	}
}

void ArgumentsOverload::addArgs(Arguments args)
{
	if (!isPresent(args)) {
		listArguments.push_back(args);
	}
}

std::vector<DataType> ArgumentsOverload::getPossibleDataTypes()
{
	currentDataTypes.clear();
	for (auto arg : listArguments) {
		if (!arg.ended() && arg.isValid()) {
			currentDataTypes.push_back(arg.getCurrentDataType());
		}
	}
	return currentDataTypes;
}

void ArgumentsOverload::next()
{
	for (auto arg : listArguments)arg.next();
}

std::vector<DataType> ArgumentsOverload::getFirst()
{
	currentIndex = 0;
	return getPossibleDataTypes();
}

bool ArgumentsOverload::ended()
{
	for (auto arg : listArguments) {
		if (!arg.ended())return false;
	}
	return true;
}

int ArgumentsOverload::size()
{
	return listArguments.size();
}

bool ArgumentsOverload::empty()
{
	return listArguments.empty();
}

TemplateToken::TemplateToken()
{

}

TemplateToken::TemplateToken(int line) :TemplateToken()
{
	this->line = line;
}

bool TemplateToken::addOab(IteratorList<Token>& tl,TokenResult& tRes)
{
	return addToken(TokenVALUE::OPENANGLEBRACKETS, tl, tRes);
}

bool TemplateToken::addCab(IteratorList<Token>& tl, TokenResult& tRes)
{
	return addToken(TokenVALUE::CLOSEANGLEBRACKETS, tl, tRes);
}

bool TemplateToken::addTemplatedTypes(IteratorList<Token>& tl, TokenResult tRes)
{
	return false;
}

bool TemplateToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	if (addOab(tl,tRes)) {
		return addCab(tl, tRes);
	}
	return false;
}

bool TemplateToken::addError(TokenResult& tRes, TokenVALUE value, ErrorType et)
{
	tRes.addError(line,value, et);
	return false;
}

bool TemplateToken::addError(TokenResult& tRes, DataType type)
{
	tRes.addError(line,type);
	return false;
}

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


std::shared_ptr<TokenResult> IdentifierToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	return updateRes(tRes);
}

MainToken::MainToken() :FlowPKToken()
{
	args = std::make_shared<Arguments>(DataType::NONE, true);
	tRes = std::make_shared<TokenResult>();
	tValue = TokenVALUE::MAIN;
	tokenText = mainK;
	canEnd = true;
}

std::shared_ptr<Token> PKToken::addOp(IteratorList<Token>& tl)
{
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->getValue() == TokenVALUE::OPENPARENTHESIS) {
			tl.next();
			return elem;
		}
	}
	addError(TokenVALUE::OPENPARENTHESIS);
	return nullptr;
}

std::shared_ptr<Token> PKToken::addCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->getValue() == TokenVALUE::CLOSEPARENTHESIS) {
			tl.next();
			return elem;
		}
	}
	addError(TokenVALUE::CLOSEPARENTHESIS);
	return nullptr;
}

//Leave empty
std::shared_ptr<Token> PKToken::handleArguments(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	DataType correctType;
	if (!args->empty()) {
		if (repeat)correctType = args->getFirst();
		while (!tl.ended() && (!args->ended() || repeat)) {
			auto elem = tl.currentToken();
			if (mustEnd) {
				return addCp(tl, tRes);
			}
			else if (mustComma) {
				if (addComma(tl)) {
					mustComma = false;
					continue;
				}
				else break;
			}
			else {
				if (!repeat)correctType = args->getCurrentDataType();
				if (addCorrectType(tl, correctType, elem))continue;
				else break;
			}
		}
	}
	return nullptr;
}

std::shared_ptr<Token> PKToken::addCorrectType(IteratorList<Token>& tl,DataType correctType, std::shared_ptr<Token> elem) {
	if (elem->getDataType() == correctType) {
		auto elemRes = elem->addTokens(tl, tRes);
		if (elemRes->success()) {
			mustComma = true;
			argTokens.push_back(elem);		
			if (!repeat) {
				args->next();
				if (args->ended())mustEnd = true;
			}
		}
		else updateRes(elemRes);
	}
	return nullptr;
}







void PKToken::addParameter()
{
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
	args = std::make_shared<Arguments>();
	mustComma = mustEnd=canEnd = false;

}

std::shared_ptr<TokenResult> Token::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	return tRes;
}

std::shared_ptr<TokenResult> KToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	return tRes;
}

std::shared_ptr<TokenResult> PKToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	KToken::addTokens(tl, tRes);
	if (addOp(tl)) {
		handleArguments(tl, tRes);
	}
	return updateRes(tRes);
}
std::shared_ptr<TokenResult> UPKToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	//same as PKToken, handleArguments gets only 1 token
	return updateRes(PKToken::addTokens(tl, tRes));
}
std::shared_ptr<TokenResult> FlowKToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	if (KToken::addTokens(tl, tRes)->success()) {
		return updateRes(FlowToken::addBody(tl, tRes));
	}
	else return updateRes(tRes);
}
std::shared_ptr<TokenResult> FlowPKToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	if (PKToken::addTokens(tl, tRes)->success()){
		return FlowToken::addBody(tl, tRes);
	}
	return updateRes(tRes);
}

std::shared_ptr<TokenResult> MainToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	FlowPKToken::addTokens(tl, tRes);
	//handle main logic
	return updateRes(tRes);
}

std::shared_ptr<TokenResult> FlowCKToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	if (addOp(tl)) {
		if (addCondition(tl, tRes)) {
			if (addCp(tl, tRes)) {
				if (addOb(tl))addCb(tl, tRes);
			}
		}
	}
	return updateRes(tRes);
}

std::shared_ptr<TokenResult> WaitToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	if (addOp(tl)) {
		if (addNumber(tl, tRes)) {
			if (addComma(tl)) {
				if (addTimeType(tl, tRes)) {
					addCp(tl, tRes);
				}
			}
		}
	}
	return updateRes(tRes);
}

std::shared_ptr<TokenResult> StoreToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	if (addOp(tl)) {
		if (addIdentifier(tl, tRes)) {
			if (addComma(tl)) {
				if (addValue(tl, tRes)) {
					addCp(tl, tRes);
				}
			}
		}
	}
	return updateRes(tRes);
}



std::shared_ptr<TokenResult> DoLoopToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	return updateRes(tRes);
}

std::shared_ptr<TokenResult> IfToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	return updateRes(tRes);
}

std::shared_ptr<TokenResult> ElifToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	return updateRes(tRes);
}

std::shared_ptr<TokenResult> ListToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	if (addOab(tl)) {
		if (addType(tl, tRes)) {
			if (addCab(tl)) {
				if (addOp(tl)) {
					if (handleArguments(tl, tRes));
				}
			}
		}
	}
	return updateRes(tRes);
}

std::shared_ptr<Token> PKToken::addNumber(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		elem->addTokens(tl, tRes);
		if (tRes->success()) {
			if (isNumericToken(elem)) {
				return elem;
			}
		}
	}
	addError(TokenVALUE::NUMERIC);
	return nullptr;
}

std::shared_ptr<Token> PKToken::addInteger(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		elem->addTokens(tl, tRes);
		if (tRes->success()) {
			if (isIntegerToken(elem)) {
				return elem;
			}
		}
	}
	addError(TokenVALUE::INTEGER);
	return nullptr;
}

std::shared_ptr<Token> PKToken::addFloat(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		elem->addTokens(tl, tRes);
		if (tRes->success()) {
			if (isFloatToken(elem)) {
				return elem;
			}
		}
	}
	addError(TokenVALUE::FLOAT);
	return nullptr;
}

std::shared_ptr<Token> PKToken::addCoord(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		elem->addTokens(tl, tRes);
		if (tRes->success()) {
			if (isCoordToken(elem)) {
				return elem;
			}
		}
	}
	addError(TokenVALUE::COORD);
	return nullptr;
}

std::shared_ptr<Token> PKToken::addString(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		auto res = elem->addTokens(tl, tRes);
		if (res->success()) {
			if (isStringToken(elem)) {
				return elem;
			}
		}
		else updateRes(res);
	}
	addError(TokenVALUE::STRING);
	return nullptr;
}

std::shared_ptr<Token> PKToken::addComma(IteratorList<Token>& tl) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->getValue() == TokenVALUE::COMMA) {
			tl.next();
			return elem;
		}
	}
	addError(TokenVALUE::COMMA);
	return nullptr;
}

std::shared_ptr<Token> PKToken::addTimeType(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (isTimeTypeToken(elem)) {
			tl.next();
			return elem;
		}
	}
	addError(TokenVALUE::SECOND);
	return nullptr;
}





DataType IdentifierToken::getDataType()
{
	DataType res = DataType::NONE;
	if (tRes) {
		auto varTable = tRes->getVarTable();
		auto iter = varTable->find(tokenText);
		if (iter != varTable->end()) {
			res = iter->second;
		}
	}
	return res;
}
std::shared_ptr<Token> StoreToken::addIdentifier(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (isIdentifier(elem)) {
			identifierToken = elem;
			tl.next();
			return elem;
		}
	}
	addError(TokenVALUE::SECOND);
	return nullptr;
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

std::shared_ptr<Token> StoreToken::addValue(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		auto res = elem->addTokens(tl, tRes);
		if (res->success()) {
			if (isValue(elem)) {
				valueToken = elem;
				tRes->addVar(identifierToken->getTokenText(), valueToken->getDataType());
				return elem;
			}
		}
		else updateRes(res);
		return elem;
	}
	addError(TokenVALUE::INTEGER);
	return nullptr;
}



std::shared_ptr<Token> FlowToken::addCb(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{

	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem) {
			if (elem->getValue() == TokenVALUE::CLOSEBRACKETS) {
				return elem;
			}
			else {
				auto elemRes = elem->addTokens(tl, tRes);
				if (elemRes->success()) {
					nestedTokens.push_back(elem);
				}
				else {
					tRes->addError(elemRes);
					break;
				}
			}
		}
	}
	tRes->addError(lineFlow,TokenVALUE::CLOSEBRACKETS);
	return nullptr;
}

std::shared_ptr<TokenResult> FlowToken::addBody(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	//no tl.next() because there's no keyword
	if (addOb(tl))addCb(tl, tRes);
	return updateBodyRes();
}

std::shared_ptr<TokenResult> FlowToken::updateBodyRes()
{
	for (auto token : nestedTokens) {
		nestedRes->addError(token->getResult());
	}
	return nestedRes;
}

std::shared_ptr<Token> FlowToken::addOb(IteratorList<Token>& tl)
{
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->getValue() == TokenVALUE::OPENBRACKETS) {
			tl.next();
			return elem;
		}
	}
	nestedRes->addError(lineFlow,TokenVALUE::OPENBRACKETS);
	return nullptr;
}



FlowPKToken::FlowPKToken() :PKToken(), FlowToken(line)
{

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
	args = std::make_shared<Arguments>(DataType::BOOL, false);
}

std::shared_ptr<Token> FlowCKToken::addCondition(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (isBooleanToken(elem)) {
			condition = elem;
			tl.next();
			return elem;
		}
	}
	addError(TokenVALUE::BOOL);
	return nullptr;
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
	args = std::make_shared<Arguments>(DataType::BOOL,false);
}

DataType CKToken::getDataType()
{
	return DataType::BOOL;
}

void CKToken::addParameter()
{
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


std::shared_ptr<TokenResult> NotToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	return updateRes(tRes);
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

std::shared_ptr<Tag> PrintToken::execute()
{
	return std::shared_ptr<Tag>();
}

DirectionToken::DirectionToken()
{
	tValue = TokenVALUE::DIRECTION;
	tokenText = directionK;
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

std::shared_ptr<Token> ListToken::addOab(IteratorList<Token>& tl) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->getValue() == TokenVALUE::OPENANGLEBRACKETS) {
			tl.next();
			return elem;
		}
	}
	addError(TokenVALUE::OPENANGLEBRACKETS);
	return nullptr;
}

std::shared_ptr<Token> ListToken::addCab(IteratorList<Token>& tl) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->getValue() == TokenVALUE::CLOSEANGLEBRACKETS) {
			tl.next();
			return elem;
		}
	}
	addError(TokenVALUE::CLOSEANGLEBRACKETS);
	return nullptr;
}



std::shared_ptr<Token> ListToken::addType(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (isType(elem)) {
			dType = elem->getDataType();
			tl.next();
			return elem;
		}
	}
	addError(TokenVALUE::INTEGER);
	return nullptr;
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
	varTable = std::make_shared<std::map<std::string, DataType>>();
	listErrors = std::vector<std::shared_ptr<Error>>();
}

TokenResult::TokenResult(TokenVALUE value, int l) :TokenResult()
{
}

void TokenResult::showErrors()
{
	for (auto& error : listErrors) {
		if (error)error->showError();
	}
}

void TokenResult::addError(int l,TokenVALUE value, ErrorType et)
{
	listErrors.push_back(std::make_shared<Error>(l,value, et));
}

void TokenResult::addError(DataType type, int l)
{
	listErrors.push_back(std::make_shared<Error>(l,type));
}

void TokenResult::addError(const std::shared_ptr < TokenResult>& tokRes)
{
	if (tokRes)listErrors.insert(listErrors.end(), tokRes->listErrors.begin(), tokRes->listErrors.end());
}

void TokenResult::addVar(const std::string& name, const DataType& type)
{
	varTable->insert(std::pair<std::string, DataType>(name, type));
}

bool TokenResult::success()
{
	return listErrors.empty();
}

std::shared_ptr<std::map<std::string, DataType>> TokenResult::getVarTable()
{
	return varTable;
}

Token::Token()
{
	tRes = std::make_shared<TokenResult>();
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

std::shared_ptr<TokenResult> Token::updateRes(std::shared_ptr<TokenResult> tResult)
{
	tRes->addError(tResult);
	return tRes;
}

std::shared_ptr<TokenResult> Token::addError(TokenVALUE value, ErrorType et)
{
	tRes->addError(line,value, et );
	return tRes;
}

std::shared_ptr<TokenResult> Token::addError(DataType type)
{
	tRes->addError(type, line);
	return tRes;
}

std::string Token::getTokenText()
{
	return tokenText;
}

int Token::getLine()
{
	return line;
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

std::shared_ptr<TokenResult> Token::getResult()
{
	return tRes;
}



std::shared_ptr<Tag> Token::execute()
{
	return std::shared_ptr<Tag>();
}

UPKToken::UPKToken() :PKToken()
{
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

std::shared_ptr<TokenResult> MPKToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	return PKToken::addTokens(tl, tRes);
}


void MPKToken::showArguments(const int nestedLayer)
{
	for (auto elem : argTokens) {
		elem->showTokenTree(nestedLayer);
	}
}

std::shared_ptr<TokenResult> EUPKToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	return std::shared_ptr<TokenResult>();
}

Arguments::Arguments()
{
	currentIndex = 0;
	valid = true;
}

Arguments::Arguments(DataType type, bool infinite):Arguments()
{
	listArgsTypes.push_back(type);
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

bool Arguments::isInfinite()
{
	return infinite;
}

bool Arguments::isValid()
{
	return valid;
}

bool Arguments::isEqual(Arguments args)
{
	if (infinite != args.infinite)return false;
	if (args.size() != size())return false;

	for (int i = 0; i < args.size(); ++i) {
		if (args.at(i) != at(i))return false;
	}
	return true;
}

BMPKToken::BMPKToken():MPKToken()
{
	args = std::make_shared<Arguments>(DataType::BOOL,true);
	repeat = true;
}

DataType BMPKToken::getDataType()
{
	return DataType::BOOL;
}

std::shared_ptr<TokenResult> BMPKToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	return MPKToken::addTokens(tl,tRes);
}

bool OverloadArguments::isValid()
{
	for (auto arg:listArguments) {
		if (arg.isValid())return true;
	}
	return false;
}

OverloadArguments::OverloadArguments()
{
	currentIndex = 0;
	listArguments.clear();
	possibleDataTypes.clear();
}

OverloadArguments::OverloadArguments(Arguments args):OverloadArguments()
{
	if (!isPresent(args)) {
		listArguments.push_back(args);
	}
}

OverloadArguments::OverloadArguments(std::vector<Arguments> ovArgs):OverloadArguments()
{
	listArguments  =   ovArgs;
}

bool OverloadArguments::isPresent(Arguments args)
{
	for (auto arg : listArguments) {
		if (arg.isEqual(args))return true;
	}
}

void OverloadArguments::addArgs(Arguments args)
{
	listArguments.push_back(args);
}

std::vector<DataType> OverloadArguments::getPossibleDataTypes()
{
	possibleDataTypes.clear();
	for (auto arg : listArguments) {
		if (!arg.ended() && arg.isValid()) {
			possibleDataTypes.push_back(arg.getCurrentDataType());
		}
	}
	return possibleDataTypes;
}

void OverloadArguments::next()
{
	for (auto arg : listArguments)arg.next();
}

std::vector<DataType> OverloadArguments::getFirst()
{
	currentIndex = 0;
	return getPossibleDataTypes();
}

bool OverloadArguments::ended()
{
	for (auto arg : listArguments) {
		if (!arg.ended())return false;
	}
	return true;
}

int OverloadArguments::size()
{
	return listArguments.size();
}

bool OverloadArguments::empty()
{
	return listArguments.empty();
}

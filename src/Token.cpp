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
	if (token)return token->tValue == TokenVALUE::IDENTIFIER;
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

MainToken::MainToken() :FlowKPToken()
{
	tRes = std::make_shared<TokenResult>();
	tValue = TokenVALUE::MAIN;
	tokenText = mainK;
}

std::shared_ptr<Token> KPToken::addOp(IteratorList<Token>& tl)
{
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->tValue == TokenVALUE::OPENPARENTHESIS) {
			tl.next();
			return elem;
		}
	}
	addError(TokenVALUE::OPENPARENTHESIS);
	return nullptr;
}

std::shared_ptr<Token> KPToken::addCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->tValue == TokenVALUE::CLOSEPARENTHESIS) {
			tl.next();
			return elem;
		}
	}
	addError(TokenVALUE::CLOSEPARENTHESIS);
	return nullptr;
}
//Leave empty
std::shared_ptr<Token> KPToken::handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	return std::shared_ptr<Token>();
}

void KPToken::showTokenTree()
{
	Token::showTokenTree();
	std::cout << openParenthesisP;
	showArguments();
	std::cout << closeParenthesisP;
}

void KPToken::showArguments()
{
}

KPToken::KPToken() :KToken()
{
}

std::shared_ptr<TokenResult> KPToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	if (addOp(tl)) {
		handleCp(tl, tRes);
	}
	return updateRes(tRes);
}

std::shared_ptr<Token> KPToken::addNumber(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		elem->addTokens(tl, tRes);
		if (tRes->isSuccess()) {
			if (isNumericToken(elem)) {
				return elem;
			}
		}
	}
	addError(TokenVALUE::NUMERIC);
	return nullptr;
}

std::shared_ptr<Token> KPToken::addInteger(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		elem->addTokens(tl, tRes);
		if (tRes->isSuccess()) {
			if (isIntegerToken(elem)) {
				return elem;
			}
		}
	}
	addError(TokenVALUE::INTEGER);
	return nullptr;
}

std::shared_ptr<Token> KPToken::addFloat(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		elem->addTokens(tl, tRes);
		if (tRes->isSuccess()) {
			if (isFloatToken(elem)) {
				return elem;
			}
		}
	}
	addError(TokenVALUE::FLOAT);
	return nullptr;
}

std::shared_ptr<Token> KPToken::addCoord(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		elem->addTokens(tl, tRes);
		if (tRes->isSuccess()) {
			if (isCoordToken(elem)) {
				return elem;
			}
		}
	}
	addError(TokenVALUE::COORD);
	return nullptr;
}

std::shared_ptr<Token> KPToken::addString(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		auto res = elem->addTokens(tl, tRes);
		if (res->isSuccess()) {
			if (isStringToken(elem)) {
				return elem;
			}
		}
		else addError(res);
	}
	addError(TokenVALUE::STRING);
	return nullptr;
}

std::shared_ptr<Token> KPToken::addComma(IteratorList<Token>& tl) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->tValue == TokenVALUE::COMMA) {
			tl.next();
			return elem;
		}
	}
	addError(TokenVALUE::COMMA);
	return nullptr;
}

std::shared_ptr<Token> KPToken::addTimeType(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
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



DataType IdentifierToken::getDataType()
{
	DataType res = DataType::NONE;
	if (tRes) {
		auto varTable = tRes->varTable;
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
	return validTypes.count(token->tValue) > 0;
}

std::shared_ptr<Token> StoreToken::addValue(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		auto res = elem->addTokens(tl, tRes);
		if (res->isSuccess()) {
			if (isValue(elem)) {
				valueToken = elem;
				tRes->addVar(identifierToken->tokenText, valueToken->getDataType());
				return elem;
			}
		}
		else addError(res);
		return elem;
	}
	addError(TokenVALUE::INTEGER);
	return nullptr;
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



std::shared_ptr<Token> FlowKToken::addOb(IteratorList<Token>& tl)
{
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->tValue == TokenVALUE::OPENBRACKETS) {
			tl.next();
			return elem;
		}
	}
	addError(TokenVALUE::OPENBRACKETS);
	return nullptr;
}

std::shared_ptr<Token> FlowKToken::addCb(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem) {
			if (elem->tValue == TokenVALUE::CLOSEBRACKETS) {
				return elem;
			}
			else {
				auto elemRes = elem->addTokens(tl, tRes);
				if (elemRes->isSuccess()) {
					nestedTokens.push_back(elem);
				}
				else {
					addError(elemRes);
					break;
				}
			}
		}
	}
	addError(TokenVALUE::CLOSEBRACKETS);
	return nullptr;
}

FlowKPToken::FlowKPToken() :KPToken()
{
}

std::shared_ptr<Token> FlowKPToken::addOb(IteratorList<Token>& tl)
{
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->tValue == TokenVALUE::OPENBRACKETS) {
			tl.next();
			return elem;
		}
	}
	addError(TokenVALUE::OPENBRACKETS);
	return nullptr;
}

std::shared_ptr<Token> FlowKPToken::addCb(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem) {
			if (elem->tValue == TokenVALUE::CLOSEBRACKETS) {
				return elem;
			}
			else {
				auto elemRes = elem->addTokens(tl, tRes);
				if (elemRes->isSuccess()) {
					nestedTokens.push_back(elem);
				}
				else {
					addError(elemRes);
					break;
				}
			}
		}
	}
	addError(TokenVALUE::CLOSEBRACKETS);
	return nullptr;
}


std::shared_ptr<TokenResult> FlowKToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	if (addOb(tl))addCb(tl, tRes);
	return updateRes(tRes);
}

void FlowKToken::showTokenTree()
{
	Token::showTokenTree();
	std::cout << openBracketsP + '\n';
	for (auto elem : nestedTokens) {
		elem->showTokenTree();
	}
	std::cout << closeBracketsP + '\n';

}

std::shared_ptr<TokenResult> FlowKPToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	if (addOp(tl)) {
		if (addCp(tl, tRes)) {
			if (addOb(tl))addCb(tl, tRes);
		}
	}
	return updateRes(tRes);
}

void FlowKPToken::showTokenTree()
{
	KPToken::showTokenTree();
	std::cout << '\n' + openBracketsP + "\n\t";
	for (auto elem : nestedTokens) {

		elem->showTokenTree();
	}
	std::cout << '\n' + closeBracketsP + '\n';
}


std::shared_ptr<Token> getStringLiteralToken(std::string& stringLiteral) {
	return std::make_shared<StringLiteralToken>(stringLiteral);
}



FlowKCToken::FlowKCToken() :FlowKPToken()
{
}

std::shared_ptr<Token> FlowKCToken::addCondition(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
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

std::shared_ptr<TokenResult> FlowKCToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
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




std::shared_ptr<TokenResult> MainToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	if (addOp(tl)) {
		if (addCp(tl, tRes)) {
			if (addOb(tl))addCb(tl, tRes);
		}
	}
	return updateRes(tRes);
}




std::shared_ptr<Tag> MainToken::execute()
{
	return std::shared_ptr<Tag>();
}


DoLoopToken::DoLoopToken()
{
	tValue = TokenVALUE::DOLOOP;
	tokenText = doloopK;
}

std::shared_ptr<TokenResult> DoLoopToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	return updateRes(tRes);
}

std::shared_ptr<Tag> DoLoopToken::execute()
{
	return std::shared_ptr<Tag>();
}

IfToken::IfToken()
{
	tValue = TokenVALUE::IF;
	tokenText = ifK;
}

std::shared_ptr<TokenResult> IfToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	return updateRes(tRes);
}

std::shared_ptr<Tag> IfToken::execute()
{
	return std::shared_ptr<Tag>();
}

ElseToken::ElseToken()
{
	tValue = TokenVALUE::ELSE;
	tokenText = elseK;
}

std::shared_ptr<Tag> ElseToken::execute()
{
	return std::shared_ptr<Tag>();
}

ElifToken::ElifToken()
{
	tValue = TokenVALUE::ELIF;
	tokenText = elifK;
}

std::shared_ptr<TokenResult> ElifToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	return updateRes(tRes);
}

std::shared_ptr<Tag> ElifToken::execute()
{
	return std::shared_ptr<Tag>();
}

LoopToken::LoopToken()
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

WaitToken::WaitToken()
{
	tValue = TokenVALUE::WAIT;
	tokenText = waitK;
}



std::shared_ptr<Tag> WaitToken::execute()
{
	return std::shared_ptr<Tag>();
}

AndToken::AndToken()
{
	tValue = TokenVALUE::AND;
	tokenText = andK;
}

DataType AndToken::getDataType()
{
	return DataType::BOOL;
}

std::shared_ptr<Token> AndToken::handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	bool mustComma = false;
	bool mustEnd = false;
	while (!tl.ended()) {
		if (mustComma) {
			if (addComma(tl))continue;
			else break;
		}
		auto elem = tl.currentToken();
		if (isBoolToken(elem) && !mustEnd) {
			elem->addTokens(tl, tRes);
			if (tRes->isSuccess()) {
				listBoolToken.push_back(elem);
				mustEnd = true;
				continue;
			}
			else break;
		}
		addCp(tl, tRes);
		break;
	}
	return nullptr;
}

std::shared_ptr<Token> OrToken::handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	bool mustComma = false;
	bool mustEnd = false;
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (mustComma) {
			if (addComma(tl))continue;
			else break;
		}
		if (isBoolToken(elem) && !mustEnd) {
			elem->addTokens(tl, tRes);
			if (tRes->isSuccess()) {
				listBoolToken.push_back(elem);
				mustEnd = true;
				continue;
			}
			else break;
		}
		addCp(tl, tRes);
		break;
	}
	return nullptr;
}
std::shared_ptr<Token> NotToken::handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	bool addedCond = false;
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (addedCond)return addCp(tl, tRes);
		else if (isBoolToken(elem)) {
			elem->addTokens(tl, tRes);
			if (tRes->isSuccess()) {
				this->boolToken = elem;
				addedCond = true;
				continue;
			}
			else break;
		}
		else {
			addError(TokenVALUE::BOOL);
			break;
		}
	}
	return nullptr;
}

std::shared_ptr<Tag> AndToken::execute()
{
	return std::shared_ptr<Tag>();
}

OrToken::OrToken()
{
	tValue = TokenVALUE::OR;
	tokenText = orK;
}

DataType OrToken::getDataType()
{
	return DataType::BOOL;
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

DataType NotToken::getDataType()
{
	return DataType::BOOL;
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

std::shared_ptr<TokenResult> ContinueToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	return updateRes(tRes);
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

std::shared_ptr<TokenResult> BreakToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	return updateRes(tRes);
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

std::shared_ptr<TokenResult> EKPToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	if (addOp(tl)) {
		handleCp(tl, tRes);
	}
	return updateRes(tRes);
}




std::shared_ptr<Token> CoordToken::handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	bool mustEnd = false;
	bool mustComma = false;
	int nbElements = 0;
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (mustComma) {
			if (addComma(tl))continue;
			else break;
		}
		if (isCoordToken(elem) && !mustEnd) {
			if (nbElements == 0) {
				elem->addTokens(tl, tRes);
				if (tRes->isSuccess()) {
					coordToken = elem;
					mustEnd = true;
					continue;
				}
			}
			else {
				addError(TokenVALUE::COORD, ErrorType::UNEXPECTED);
				break;
			}
		}
		if (isIntegerToken(elem) && !mustEnd) {
			++nbElements;
			elem->addTokens(tl, tRes);
			if (tRes->isSuccess()) {
				switch (nbElements) {
				case 1:
					xPoint = elem;
					mustComma = true;
					continue;
				case 2:
					yPoint = elem;
					mustEnd = true;
					continue;
				default:
					return nullptr;
				}
			}
		}
		addCp(tl, tRes);
		break;
	}
	return nullptr;
}

std::shared_ptr<Token> ZoneToken::handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	bool mustEnd = false;
	bool mustComma = false;
	int nbElements = 0;
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (mustComma) {
			if (addComma(tl))continue;
			else break;
		}
		if (isZoneToken(elem) && !mustEnd) {
			if (nbElements == 0) {
				elem->addTokens(tl, tRes);
				if (tRes->isSuccess()) {
					zoneToken = elem;
					mustEnd = true;
					continue;
				}
			}
			else {
				addError(TokenVALUE::COORD, ErrorType::UNEXPECTED);
			}
			break;
		}
		if (isCoordToken(elem) && !mustEnd) {
			++nbElements;
			elem->addTokens(tl, tRes);
			if (tRes->isSuccess()) {
				switch (nbElements) {
				case 1:
					topLeft = elem;
					mustComma = true;
					continue;
				case 2:
					bottomRight = elem;
					mustEnd = true;
					continue;
				default:
					return nullptr;
				}
			}
		}
		addCp(tl, tRes);
		break;
	}
	return nullptr;
}

std::shared_ptr<Token> IntegerToken::handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	bool mustEnd = false;
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (isIntegerToken(elem) && !mustEnd) {
			elem->addTokens(tl, tRes);
			if (tRes->isSuccess()) {
				intToken = elem;
				mustEnd = true;
				continue;
			}
			else break;
		}
		addCp(tl, tRes);
		break;
	}
	return nullptr;
}

std::shared_ptr<Token> FloatToken::handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	bool mustEnd = false;
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (isFloatToken(elem) && !mustEnd) {
			elem->addTokens(tl, tRes);
			if (tRes->isSuccess()) {
				floatToken = elem;
				mustEnd = true;
				continue;
			}
			else break;
		}
		addCp(tl, tRes);
		break;
	}
	return nullptr;
}

std::shared_ptr<Token> StringToken::handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	bool mustEnd = false;
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (isStringToken(elem) && !mustEnd) {
			elem->addTokens(tl, tRes);
			if (tRes->isSuccess()) {
				stringToken = elem;
				mustEnd = true;
				continue;
			}
			else break;
		}
		addCp(tl, tRes);
		break;
	}
	return nullptr;
}

std::shared_ptr<Token> BoolToken::handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	bool mustEnd = false;
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (isBoolToken(elem) && !mustEnd) {
			elem->addTokens(tl, tRes);
			if (tRes->isSuccess()) {
				boolToken = elem;
				mustEnd = true;
				continue;
			}
			else break;
		}
		addCp(tl, tRes);
		break;
	}
	return nullptr;
}
std::shared_ptr<Token> DirectionToken::handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	bool mustEnd = false;
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (isDirectionToken(elem) && !mustEnd) {
			elem->addTokens(tl, tRes);
			if (tRes->isSuccess()) {
				dirToken = elem;
				mustEnd = true;
				continue;
			}
			else break;
		}
		addCp(tl, tRes);
		break;
	}
	return nullptr;
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
		if (elem->tValue == TokenVALUE::OPENANGLEBRACKETS) {
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
		if (elem->tValue == TokenVALUE::CLOSEANGLEBRACKETS) {
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

std::shared_ptr<Token> ListToken::handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	bool addedComma = false;
	bool firstEntry = true;
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem) {
			if (firstEntry) {
				firstEntry = false;
				if (elem->getDataType() == dType) {
					auto elemRes = elem->addTokens(tl, tRes);
					if (elemRes->isSuccess()) {
						listToken.push_back(elem);
						tl.next();
					}
					else {
						addError(elemRes);
						break;
					}
					continue;
				}
				else if (elem->tValue == TokenVALUE::CLOSEPARENTHESIS) {
					return elem;
				}
				else break;
			}

			if (addedComma) {
				if (elem->getDataType() == dType) {
					addedComma = false;
					auto elemRes = elem->addTokens(tl, tRes);
					if (elemRes->isSuccess()) {
						listToken.push_back(elem);
						tl.next();
					}
					else {
						addError(elemRes);
						break;
					}
					continue;
				}
				else break;
			}
			else {
				if (elem->tValue == TokenVALUE::CLOSEPARENTHESIS) {
					return elem;
				}
				else if (elem->tValue == TokenVALUE::COMMA) {
					addedComma = true;
					tl.next();
					continue;
				}
				else break;
			}
		}
	}
	addError(TokenVALUE::CLOSEPARENTHESIS);
	return nullptr;
}

std::shared_ptr<TokenResult> ListToken::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	if (addOab(tl)) {
		if (addType(tl, tRes)) {
			if (addCab(tl)) {
				if (addOp(tl)) {
					if (handleCp(tl, tRes));
				}
			}
		}
	}
	return updateRes(tRes);
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

void TokenResult::addError(TokenVALUE value, ErrorType et, int l)
{
	listErrors.push_back(std::make_shared<Error>(value, et, l));
}

void TokenResult::addError(DataType type, ErrorType et, int l)
{
	listErrors.push_back(std::make_shared<Error>(type, et, l));
}

void TokenResult::addError(const std::shared_ptr < TokenResult>& nestedToken)
{
	if (nestedToken)	listErrors.insert(listErrors.end(), nestedToken->listErrors.begin(), nestedToken->listErrors.end());
}

void TokenResult::addVar(const std::string& name, const DataType& type)
{
	varTable->insert(std::pair<std::string, DataType>(name, type));
}

bool TokenResult::isSuccess()
{
	return listErrors.empty();
}

Token::Token()
{

	tValue = TokenVALUE::TOKEN;
	tokenText = "Token";
}

Error::Error()
{
}

Error::Error(TokenVALUE ev, ErrorType et, int el) :Error()
{
	errorType = et;
	errorValue = ev;
	errorLine = el;
	dType = DataType::NONE;
}

Error::Error(DataType dType, ErrorType et, int el)
{
	errorType = et;
	errorLine = el;
	this->dType = dType;
}

void Error::showError()
{
}

std::shared_ptr<TokenResult> Token::updateRes(std::shared_ptr<TokenResult> tRes)
{
	this->tRes = tRes;
	return tRes;
}

std::shared_ptr<TokenResult> Token::addError(TokenVALUE value, ErrorType et)
{
	if (tRes)tRes->addError(value, et, line);
	return tRes;
}

std::shared_ptr<TokenResult> Token::addError(const std::shared_ptr<TokenResult>& tr)
{
	if (tRes)tRes->addError(tr);
	return tRes;
}

std::shared_ptr<TokenResult> Token::addError(DataType type, ErrorType et)
{
	if (tRes)tRes->addError(type, et, line);
	return tRes;
}

void Token::showTokenTree()
{
	std::cout << tokenText;
}

DataType Token::getDataType()
{
	return DataType::NONE;
}

std::shared_ptr<TokenResult> Token::addTokens(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	tl.next();
	return updateRes(tRes);
}

std::shared_ptr<Tag> Token::execute()
{
	return std::shared_ptr<Tag>();
}


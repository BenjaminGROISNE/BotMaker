#include "Interpretor.h"

bool isTokenString(const std::string& text)
{
	return isKeywordString(text)|| isLiteralString(text)|| isPunctuationString(text) || isOperatorString(text);
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
		return token->getDataType()==DataType::TIMETYPE;
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

bool isWhitespace(const char& c) {
	return c == ' ';
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

bool isNumber(const std::string& s)
{
	std::string::const_iterator it = s.begin();
	while (it != s.end() && std::isdigit(*it)) ++it;
	return !s.empty() && it == s.end();
}

bool isIdentifier(const std::string& s) {
	bool notEmpty = !s.empty();
	bool notTokenString = !isTokenString(s);
	bool isAlnum = true;
	for (char i : s) {
		if (!std::isalnum(i))isAlnum = false;
	}
	return notEmpty && notTokenString && isAlnum;
}

bool isIdentifier(const std::shared_ptr<Token>& token) {
	if (token)return token->tValue == TokenVALUE::IDENTIFIER;
	return false;
}



std::string getNextPunctuationToken(const std::string& str) {
	auto lowest = std::string::npos;
	std::string punc = "";
	if (!str.empty()) {
		for (auto& l : allPunctuationsTokensStrings) {
			auto k = str.find(l);
			if (k < lowest) {
				lowest = k;
				punc = l;
			}
		}
	}
	if (lowest != std::string::npos)return str.substr(lowest,punc.size());
	return std::string();
}


std::string getNextTokenString(const std::string& text)
{
	std::string nextPunc = getNextPunctuationToken(text);
	std::string contentBefore = getContentBefore(text,nextPunc);
	if (!nextPunc.empty()) {
		if (!contentBefore.empty()) {
			return contentBefore;
		}
		else return nextPunc;
	}
	return std::string();
}

void getPastTokenString(std::string& text,const std::string tokenText)
{
	text=getContentAfter(text, tokenText);
}

TokenVALUE getNextTokenValue(const std::string& text)
{
	return getTokenValue(getNextTokenString(text));
}

std::string getStringLiteral(std::string& text) {
	std::string temp = getContentBefore(text, quotation);
	if (temp.empty())return text;
	else return temp;
}

std::shared_ptr<Token> getStringLiteralToken(std::string& stringLiteral) {
	return std::make_shared<StringLiteralToken>(stringLiteral);
}
void Lexer::extractTokens(const std::string& text)
{
	if (!text.empty()) {
		std::string newText = text;
		std::string nextTokenString = getNextTokenString(newText);
		if (nextTokenString == quotation) {
			getPastTokenString(newText, nextTokenString);
			nextTokenString = getStringLiteral(newText);
			listTokens.push_back(getToken(TokenVALUE::STRINGLITERAL,nextTokenString));
			getPastTokenString(newText, nextTokenString);
		}
		else if (isSpaceToken(nextTokenString)) {
			skipSpace(newText);
		}
		else {
			listTokens.push_back(getToken(nextTokenString));
			getPastTokenString(newText, nextTokenString);
		}
		extractTokens(newText);
	}
}

TokenVALUE getTokenValue(const std::string& text) {
	if (text == mainK) {
		return TokenVALUE::MAIN;
	}
	else if (text == loopK) {
		return TokenVALUE::LOOP;
	}
	else if (text == boolK) {
		return TokenVALUE::BOOL;
	}
	else if (text == storeK) {
		return TokenVALUE::STORE;
	}
	else if (text == stringK) {
		return TokenVALUE::STRING;
	}
	else if (text == coordK) {
		return TokenVALUE::COORD;
	}
	else if (text == listK) {
		return TokenVALUE::LIST;
	}
	else if (text == intK) {
		return TokenVALUE::INTEGER;
	}
	else if (text == floatK) {
		return TokenVALUE::FLOAT;
	}
	else if (text == compareK) {
		return TokenVALUE::COMPARE;
	}
	else if (text == zoneK) {
		return TokenVALUE::ZONE;
	}
	else if (text == ifK) {
		return TokenVALUE::IF;
	}
	else if (text == elseK) {
		return TokenVALUE::ELSE;
	}
	else if (text == elifK) {
		return TokenVALUE::ELIF;
	}
	else if (text == doloopK) {
		return TokenVALUE::DOLOOP;
	}
	else if (text == andK) {
		return TokenVALUE::AND;
	}
	else if (text == notK) {
		return TokenVALUE::NOT;
	}
	else if (text == orK) {
		return TokenVALUE::OR;
	}
	else if (text == directionK) {
		return TokenVALUE::DIRECTION;
	}
	else if (text == switchK) {
		return TokenVALUE::SWITCH;
	}
	else if (text == defaultK) {
		return TokenVALUE::DEFAULT;
	}
	else if (text == breakK) {
		return TokenVALUE::BREAK;
	}
	else if (text == continueK) {
		return TokenVALUE::CONTINUE;
	}
	else if (text == caseK) {
		return TokenVALUE::CASE;
	}
	else if (text == printK) {
		return TokenVALUE::PRINT;
	}
	else if (text == waitK) {
		return TokenVALUE::WAIT;
	}
	else if (text == waitK) {
		return TokenVALUE::WAIT;
	}
	else if (text == commaP) {
		return TokenVALUE::COMMA;
	}
	else if (text == openBracketsP) {
		return TokenVALUE::OPENBRACKETS;
	}
	else if (text == closeBracketsP) {
		return TokenVALUE::CLOSEBRACKETS;
	}	
	else if (text == openAngleBracketsP) {
		return TokenVALUE::OPENANGLEBRACKETS;
	}
	else if (text == closeAngleBracketsP) {
		return TokenVALUE::CLOSEANGLEBRACKETS;
	}
	else if (text == openParenthesisP) {
		return TokenVALUE::OPENPARENTHESIS;
	}
	else if (text == closeParenthesisP) {
		return TokenVALUE::CLOSEPARENTHESIS;
	}	
	else if (text == falseL) {
		return TokenVALUE::FALSELITERAL;
	}
	else if (text == trueL) {
		return TokenVALUE::TRUELITERAL;
	}
	else if (text == millisecondL) {
		return TokenVALUE::MILLISECOND;
	}		
	else if (text == secondL) {
		return TokenVALUE::SECOND;
	}	
	else if (text == minuteL) {
		return TokenVALUE::MINUTE;
	}
	else if (text == quotation) {
		return TokenVALUE::QUOTATION;
	}
	else if (isNumber(text)) {
		return TokenVALUE::NUMERIC;
	}	
	else if (isIdentifier(text)) {
		return TokenVALUE::IDENTIFIER;
	}
	else return TokenVALUE::UNKNOWN;
}


std::string getTokenString(TokenVALUE value)
{
	switch (value) {
	case TokenVALUE::MAIN:
		return mainK;
	case TokenVALUE::LOOP:
		return loopK;
	case TokenVALUE::BOOL:
		return boolK;
	case TokenVALUE::STORE:
		return storeK;
	case TokenVALUE::STRING:
		return stringK;
	case TokenVALUE::COORD:
		return coordK;
	case TokenVALUE::LIST:
		return listK;
	case TokenVALUE::INTEGER:
		return intK;
	case TokenVALUE::FLOAT:
		return floatK;
	case TokenVALUE::COMPARE:
		return compareK;
	case TokenVALUE::ZONE:
		return zoneK;
	case TokenVALUE::IF:
		return ifK;
	case TokenVALUE::ELSE:
		return elseK;
	case TokenVALUE::ELIF:
		return elifK;
	case TokenVALUE::DOLOOP:
		return doloopK;
	case TokenVALUE::AND:
		return andK;
	case TokenVALUE::NOT:
		return notK;
	case TokenVALUE::OR:
		return orK;
	case TokenVALUE::DIRECTION:
		return directionK;
	case TokenVALUE::BREAK:
		return breakK;
	case TokenVALUE::CONTINUE:
		return continueK;
	case TokenVALUE::PRINT:
		return printK;
	case TokenVALUE::WAIT:
		return waitK;
	case TokenVALUE::CLOSEBRACKETS:
		return closeBracketsP;
	case TokenVALUE::OPENBRACKETS:
		return openBracketsP;
	case TokenVALUE::CLOSEANGLEBRACKETS:
		return closeAngleBracketsP;
	case TokenVALUE::OPENANGLEBRACKETS:
		return openAngleBracketsP;
	case TokenVALUE::CLOSEPARENTHESIS:
		return closeParenthesisP;
	case TokenVALUE::OPENPARENTHESIS:
		return openParenthesisP;
	case TokenVALUE::COMMA:
		return commaP;
	case TokenVALUE::FALSELITERAL:
		return falseL;	
	case TokenVALUE::TRUELITERAL:
		return trueL;
	case TokenVALUE::QUOTATION:
		return quotation;
	case TokenVALUE::MILLISECOND:
		return millisecondL;
	case TokenVALUE::SECOND:
		return secondL;
	case TokenVALUE::MINUTE:
		return minuteL;
	case TokenVALUE::NUMERIC:
		return "NUMERIC";
	case TokenVALUE::IDENTIFIER:
		return "ID";

	default:
		return "UNKNOWN";
	}
}

std::shared_ptr<Token> getToken(const std::string& text) {
	return getToken(getTokenValue(text), text);
}
std::shared_ptr<Token> getToken(const TokenVALUE& tValue, const std::string& text) {
	switch (tValue) {
	case TokenVALUE::MAIN:
		return std::make_shared<MainToken>();	
	case TokenVALUE::LOOP:
		return std::make_shared<LoopToken>();	
	case TokenVALUE::BOOL:
		return std::make_shared<BoolToken>();	
	case TokenVALUE::STORE:
		return std::make_shared<StoreToken>();	
	case TokenVALUE::STRING:
		return std::make_shared<StringToken>();		
	case TokenVALUE::COORD:
		return std::make_shared<CoordToken>();	
	case TokenVALUE::LIST:
		return std::make_shared<ListToken>();	
	case TokenVALUE::INTEGER:
		return std::make_shared<IntegerToken>();		
	case TokenVALUE::FLOAT:
		return std::make_shared<FloatToken>();
	case TokenVALUE::COMPARE:
		return std::make_shared<CompareToken>();	
	case TokenVALUE::ZONE:
		return std::make_shared<ZoneToken>();
	case TokenVALUE::IF:
		return std::make_shared<IfToken>();
	case TokenVALUE::ELSE:
		return std::make_shared<ElseToken>();
	case TokenVALUE::ELIF:
		return std::make_shared<ElifToken>();
	case TokenVALUE::DOLOOP:
		return std::make_shared<DoLoopToken>();
	case TokenVALUE::AND:
		return std::make_shared<AndToken>();
	case TokenVALUE::NOT:
		return std::make_shared<NotToken>();	
	case TokenVALUE::OR:
		return std::make_shared<OrToken>();
	case TokenVALUE::DIRECTION:
		return std::make_shared<DirectionToken>();
	case TokenVALUE::BREAK:
		return std::make_shared<BreakToken>();
	case TokenVALUE::CONTINUE:
		return std::make_shared<ContinueToken>();
	case TokenVALUE::PRINT:
		return std::make_shared<PrintToken>();
	case TokenVALUE::WAIT:
		return std::make_shared<WaitToken>();
	case TokenVALUE::CLOSEBRACKETS:
		return std::make_shared<CloseBracketsToken>();	
	case TokenVALUE::OPENBRACKETS:
		return std::make_shared<OpenBracketsToken>();
	case TokenVALUE::CLOSEANGLEBRACKETS:
		return std::make_shared<CloseAngleBracketsToken>();	
	case TokenVALUE::OPENANGLEBRACKETS:
		return std::make_shared<OpenAngleBracketsToken>();
	case TokenVALUE::CLOSEPARENTHESIS:
		return std::make_shared<CloseParenthesisToken>();	
	case TokenVALUE::OPENPARENTHESIS:
		return std::make_shared<OpenParenthesisToken>();
	case TokenVALUE::COMMA:
		return std::make_shared<CommaToken>();	
	case TokenVALUE::FALSELITERAL:
		return std::make_shared<FalseToken>();
	case TokenVALUE::TRUELITERAL:
		return std::make_shared<TrueToken>();
	case TokenVALUE::MILLISECOND:
		return std::make_shared<MilliSecondToken>();
	case TokenVALUE::SECOND:
		return std::make_shared<SecondToken>();
	case TokenVALUE::MINUTE:
		return std::make_shared<MinuteToken>();
	case TokenVALUE::NUMERIC:
		return std::make_shared<NumericToken>(text);
	case TokenVALUE::IDENTIFIER:
		return std::make_shared<IdentifierToken>(text);
	case TokenVALUE::STRINGLITERAL:
		return std::make_shared<StringLiteralToken>(text);
	default: 
		return std::make_shared<UnknownToken>();
	}
}


Lexer::Lexer()
{
}

Lexer::Lexer(const std::string& text)
{
	totalContent = text;
	//extractTokens(totalContent);
}

static bool isSpaceToken(std::string& c) {
	auto j= c.find_first_of(" \t\b\n\r") != std::string::npos;
	return j;
}

bool Lexer::empty()
{
	return listTokens.empty();
}






std::string Lexer::showAllTokens()
{


	std::cout << "Console is now available for debugging output!" << std::endl;
	std::string str;
	for (auto& s : this->listTokens) {
		auto h = s->tokenText;

		if(s)str.append(h+' ');
		if (h == "{" || h == "}")str.append("\n");
	}
	return str;
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

MainToken::MainToken():FlowKPToken()
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

std::shared_ptr<Token> KPToken::handleCp(IteratorList<Token>& tl, std::shared_ptr<TokenResult> tRes)
{
	return nullptr;
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
		elem->addTokens(tl,tRes);
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
		auto res = elem->addTokens(tl,tRes);
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
		if (elem->tValue==TokenVALUE::COMMA) {
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
	DataType res=DataType::NONE;
	if (tRes) {
		auto varTable = tRes->varTable;
		auto iter = varTable->find(tokenText);
		if (iter!= varTable->end()) {
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
		auto res=elem->addTokens(tl,tRes);
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



std::shared_ptr<Token> FlowKToken::addOb( IteratorList<Token>& tl)
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
				auto elemRes = elem->addTokens(tl,tRes);
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
				auto elemRes = elem->addTokens(tl,tRes);
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

FlowKPToken::FlowKPToken() :KPToken()
{
}
	


FlowKToken::FlowKToken():KToken()
{
}




TokenResult::TokenResult()
{
	varTable = std::make_shared<std::map<std::string, DataType>>();
}

TokenResult::TokenResult(TokenVALUE value, int l):TokenResult()
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

void TokenResult::addError(DataType type, ErrorType et,int l)
{
	listErrors.push_back(std::make_shared<Error>(type,et,l));
}

void TokenResult::addError(const std::shared_ptr < TokenResult>& nestedToken)
{
	if(nestedToken)	listErrors.insert(listErrors.end(), nestedToken->listErrors.begin(), nestedToken->listErrors.end());
}

void TokenResult::addVar(const std::string& name, const DataType& type)
{
	varTable->insert(std::pair<std::string, DataType>(name, type));
}

bool TokenResult::isSuccess()
{
	return listErrors.empty();
}



KPToken::KPToken():KToken()
{
}





FlowKCToken::FlowKCToken():FlowKPToken()
{

}

Token::Token()
{

	tValue = TokenVALUE::TOKEN;
	tokenText = "Token";
}

Error::Error()
{
}

Error::Error(TokenVALUE ev, ErrorType et, int el):Error()
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
	if(tRes)tRes->addError(value, et, line);
	return tRes;
}

std::shared_ptr<TokenResult> Token::addError(const std::shared_ptr<TokenResult>& tr)
{
	if (tRes)tRes->addError(tr);
	return tRes;
}

std::shared_ptr<TokenResult> Token::addError(DataType type, ErrorType et)
{
	if (tRes)tRes->addError(type,et,line);
	return tRes;
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

void Interpretor::readActivityFile(const std::string& ActivityName)
{
 	createMainTag(copyActivity(appendToFolder(ActivityFolder,ActivityName)));
}

std::string Interpretor::copyActivity(const std::string& ActivityPath)
{
	return getFileContent(ActivityPath);
}

std::shared_ptr<Tag> Interpretor::getActivityTag()
{
	return mainTag;
}

void Interpretor::createMainTag(const std::string& text)
{
	lex.extractTokens(text); 
	std::string allTokens = lex.showAllTokens();
	tl = IteratorList(lex.listTokens);
	if (!tl.nextTokens.empty()) {
		mainToken = std::dynamic_pointer_cast<MainToken>(tl.nextTokens.front());
		if (tl.nextTokens.size() > 1) {
			if (mainToken)tr=mainToken->addTokens(tl, mainToken->tRes);
		}
	}
	if (tr->isSuccess())mainTag = std::dynamic_pointer_cast<MainTag>(mainToken->execute());

	int a = 1;
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
				this->boolToken=elem;
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
		if (isCoordToken(elem)&& !mustEnd) {
			if (nbElements == 0) {
				elem->addTokens(tl,tRes);
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
		if (isIntegerToken(elem)&& !mustEnd) {
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
					auto elemRes = elem->addTokens(tl,tRes);
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
					auto elemRes = elem->addTokens(tl,tRes);
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

StringLiteralToken::StringLiteralToken(const std::string& content):LToken()
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


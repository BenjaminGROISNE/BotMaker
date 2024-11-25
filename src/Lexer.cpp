#include "Lexer.h"


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

TokenVALUE Lexer::getTokenValue(const std::string& text) {
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
std::string Lexer::getTokenString(TokenVALUE value)
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
std::shared_ptr<Token> Lexer::getToken(const std::string& text) {
	return getToken(getTokenValue(text), text);
}
std::shared_ptr<Token> Lexer::getToken(const TokenVALUE& tValue, const std::string& text) {
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
}



std::string Lexer::showAllTokens(std::vector<std::shared_ptr<Token>> listTokens)
{
	std::cout << "Console is now available for debugging output!" << std::endl;
	std::string str;
	for (auto& s : listTokens) {
		auto h = s->getTokenText();

		if (s)str.append(h + ' ');
		if (h == "{" || h == "}")str.append("\n");
	}
	return str;
}


std::string Lexer::getNextPunctuationToken(const std::string& str) {
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
	if (lowest != std::string::npos)return str.substr(lowest, punc.size());
	return std::string();
}


std::string Lexer::getNextTokenString(const std::string& text)
{
	std::string nextPunc = getNextPunctuationToken(text);
	std::string contentBefore = getStringBefore(text, nextPunc);
	if (!nextPunc.empty()) {
		if (!contentBefore.empty()) {
			return contentBefore;
		}
		else return nextPunc;
	}
	return std::string();
}

void Lexer::skipTokenString(std::string& text, const std::string tokenText)
{
	text = getStringAfter(text, tokenText);
}

void Lexer::skipStringLiteral(std::string& text, const std::string tokenText)
{
	text = getStringAfter(getStringAfter(text, tokenText), quotation);
}

TokenVALUE Lexer::getNextTokenValue(const std::string& text)
{
	return getTokenValue(getNextTokenString(text));
}


void Lexer::extractStringLiteral(std::string& newText, std::string& nextTokenString) {
	skipTokenString(newText, nextTokenString);
	nextTokenString = getStringLiteral(newText);
	listTokens.push_back(getToken(TokenVALUE::STRINGLITERAL, nextTokenString));
	skipTokenString(newText, nextTokenString);
	skipSequence(newText, quotation);
}

std::vector<std::shared_ptr<Token>> Lexer::extractTokens(const std::string& text)
{
	std::string newText = text;
	while (!newText.empty()) {
		std::string nextTokenString = getNextTokenString(newText);
		if (nextTokenString == quotation) {
			extractStringLiteral(newText, nextTokenString);
		}
		else if (beginsBySpace(nextTokenString)) {
			skipSpace(newText);
		}
		else {
			listTokens.push_back(getToken(nextTokenString));
			skipTokenString(newText, nextTokenString);
		}
	}
	return listTokens;
}

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

// Define the map to store the mappings between token strings and TokenVALUE enums
static const std::unordered_map<std::string, TokenVALUE> tokenMap = {
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

TokenVALUE Lexer::getTokenValue(const std::string& text) {
	auto it = tokenMap.find(text);
	if (it != tokenMap.end()) {
		return it->second;
	}
	else if (isNumber(text)) {
		return TokenVALUE::NUMERIC;
	}
	else if (isIdentifier(text)) {
		return TokenVALUE::IDENTIFIER;
	}
	else {
		return TokenVALUE::UNKNOWN;
	}
}
// Define a macro to generate the mappings
#define TOKEN_FACTORY(tokenValue, tokenClass) { tokenValue, []() { return std::make_shared<tokenClass>(); } }
#define TOKEN_FACTORY_STR(tokenValue, tokenClass) { tokenValue, [](const std::string& text) { return std::make_shared<tokenClass>(text); } }

// Define the map to store the mappings between TokenVALUE enums and token creation functions
static const std::unordered_map<TokenVALUE, std::function<std::shared_ptr<Token>(const std::string&)>> tokenFactoryMapStr = {
	TOKEN_FACTORY_STR(TokenVALUE::NUMERIC, NumericToken),
	TOKEN_FACTORY_STR(TokenVALUE::IDENTIFIER, IdentifierToken),
	TOKEN_FACTORY_STR(TokenVALUE::STRINGLITERAL, StringLiteralToken)
};

static const std::unordered_map<TokenVALUE, std::function<std::shared_ptr<Token>()>> tokenFactoryMap = {
	TOKEN_FACTORY(TokenVALUE::MAIN, MainToken),
	TOKEN_FACTORY(TokenVALUE::LOOP, LoopToken),
	TOKEN_FACTORY(TokenVALUE::BOOL, BoolToken),
	TOKEN_FACTORY(TokenVALUE::STORE, StoreToken),
	TOKEN_FACTORY(TokenVALUE::STRING, StringToken),
	TOKEN_FACTORY(TokenVALUE::COORD, CoordToken),
	TOKEN_FACTORY(TokenVALUE::LIST, ListToken),
	TOKEN_FACTORY(TokenVALUE::INTEGER, IntegerToken),
	TOKEN_FACTORY(TokenVALUE::FLOAT, FloatToken),
	TOKEN_FACTORY(TokenVALUE::COMPARE, CompareToken),
	TOKEN_FACTORY(TokenVALUE::ZONE, ZoneToken),
	TOKEN_FACTORY(TokenVALUE::IF, IfToken),
	TOKEN_FACTORY(TokenVALUE::ELSE, ElseToken),
	TOKEN_FACTORY(TokenVALUE::ELIF, ElifToken),
	TOKEN_FACTORY(TokenVALUE::DOLOOP, DoLoopToken),
	TOKEN_FACTORY(TokenVALUE::AND, AndToken),
	TOKEN_FACTORY(TokenVALUE::NOT, NotToken),
	TOKEN_FACTORY(TokenVALUE::OR, OrToken),
	TOKEN_FACTORY(TokenVALUE::DIRECTION, DirectionToken),
	TOKEN_FACTORY(TokenVALUE::BREAK, BreakToken),
	TOKEN_FACTORY(TokenVALUE::CONTINUE, ContinueToken),
	TOKEN_FACTORY(TokenVALUE::PRINT, PrintToken),
	TOKEN_FACTORY(TokenVALUE::WAIT, WaitToken),
	TOKEN_FACTORY(TokenVALUE::CLOSEBRACKETS, CloseBracketsToken),
	TOKEN_FACTORY(TokenVALUE::OPENBRACKETS, OpenBracketsToken),
	TOKEN_FACTORY(TokenVALUE::CLOSEANGLEBRACKETS, CloseAngleBracketsToken),
	TOKEN_FACTORY(TokenVALUE::OPENANGLEBRACKETS, OpenAngleBracketsToken),
	TOKEN_FACTORY(TokenVALUE::CLOSEPARENTHESIS, CloseParenthesisToken),
	TOKEN_FACTORY(TokenVALUE::OPENPARENTHESIS, OpenParenthesisToken),
	TOKEN_FACTORY(TokenVALUE::COMMA, CommaToken),
	TOKEN_FACTORY(TokenVALUE::FALSELITERAL, FalseToken),
	TOKEN_FACTORY(TokenVALUE::TRUELITERAL, TrueToken),
	TOKEN_FACTORY(TokenVALUE::MILLISECOND, MilliSecondToken),
	TOKEN_FACTORY(TokenVALUE::SECOND, SecondToken),
	TOKEN_FACTORY(TokenVALUE::MINUTE, MinuteToken),
	TOKEN_FACTORY(TokenVALUE::NORTH, NorthToken),
	TOKEN_FACTORY(TokenVALUE::NORTHW, NorthWToken),
	TOKEN_FACTORY(TokenVALUE::NORTHE, NorthEToken),
	TOKEN_FACTORY(TokenVALUE::SOUTH, SouthToken),
	TOKEN_FACTORY(TokenVALUE::SOUTHW, SouthWToken),
	TOKEN_FACTORY(TokenVALUE::SOUTHE, SouthEToken),
	TOKEN_FACTORY(TokenVALUE::BOOLTYPE, BoolTypeToken),
	TOKEN_FACTORY(TokenVALUE::INTTYPE, IntTypeToken),
	TOKEN_FACTORY(TokenVALUE::FLOATTYPE, FloatTypeToken),
	TOKEN_FACTORY(TokenVALUE::STRINGTYPE, StringTypeToken),
	TOKEN_FACTORY(TokenVALUE::COORDTYPE, CoordTypeToken),
	TOKEN_FACTORY(TokenVALUE::ZONETYPE, ZoneTypeToken),
	TOKEN_FACTORY(TokenVALUE::TIMETYPE, TimeTypeToken),
	TOKEN_FACTORY(TokenVALUE::DIRECTIONTYPE, DirectionTypeToken)
};

std::shared_ptr<Token> Lexer::getToken(const TokenVALUE& tValue, const std::string& text) {
	auto it1 = tokenFactoryMap.find(tValue);
	if (it1 != tokenFactoryMap.end()) {
		return it1->second();
	}
	else {
		auto it2 = tokenFactoryMapStr.find(tValue);
		if (it2 != tokenFactoryMapStr.end()) {
			return it2->second(text);
		}
		else {
			assert(false);
			return std::make_shared<UnknownToken>();
		}
	}
}


std::shared_ptr<Token> Lexer::getToken(const std::string& text) {
	return getToken(getTokenValue(text), text);
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

#include "Token.h"

bool isTokenString(const std::string& text)
{
	for (const auto& str : allKeywordsTokensString) {
		if (str.compare(text) == 0)return true;
	}
	return false;
}

bool isKeywordString(const std::string& text)
{
	for (auto& tokenString : allKeywordsTokensString) {
		if (tokenString.compare(text) == 0)return true;
	}
	return false;
}

//ADD TABLE OF ID + TYPES and then implement search in it
bool isNumericToken(const std::shared_ptr<Token>& token, TokenResult& tRes) {
	if (token) {
		auto d = token->getValueType(tRes);
		return d.type == DataType::INT || d.type == DataType::FLOAT;
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

ValueType getDescribedType(const std::shared_ptr<Token>& t)
{
	switch (t->getValue()) {
	case TokenVALUE::INTTYPE:
			return DataType::INT;
	case TokenVALUE::FLOATTYPE:
		return DataType::FLOAT;
	case TokenVALUE::STRINGTYPE:
		return DataType::STRING;
	case TokenVALUE::COORDTYPE:
		return DataType::COORD;
	case TokenVALUE::ZONETYPE:
		return DataType::ZONE;
	case TokenVALUE::DIRECTIONTYPE:
		return DataType::DIRECTION;
	case TokenVALUE::TIMETYPE:
		return DataType::TIMETYPE;
	case TokenVALUE::BOOLTYPE:
		return DataType::BOOL;
	default:
		return DataType::NONE;
	}
}





IdentifierToken::IdentifierToken(const std::string& varName):Token()
{
	tValue = TokenVALUE::IDENTIFIER;
	tokenText = varName;
}


MainToken::MainToken() :FlowPKToken()
{

	tValue = TokenVALUE::MAIN;
	tokenText = mainK;
	setOverloads();
}

void MainToken::setOverloads()
{
	argsOverload.addArgs(Arguments(),0);
	//Set the functions DataType in a file;
	argsOverload.addArgs(Arguments(),1);
}

//Leave empty
bool PKToken::handleArguments(IteratorList<Token>& tl, TokenResult& tRes)
{
	bool mustSeparate = false;
	bool mustOpenParenthesis = true;
	argsOverload.initTabs();
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (!elem->addTokens(tl, tRes))return false;
		if (elem->hasValue(TokenVALUE::OPENPARENTHESIS)) {
			if (!mustOpenParenthesis)tRes.addError(getValue(), line, TokenVALUE::OPENPARENTHESIS, ErrorType::UNEXPECTED);
			else {
				mustOpenParenthesis = false;
				continue;
			}
		}
		if (elem->hasValue(TokenVALUE::CLOSEPARENTHESIS)) {
			if (!argsOverload.hasCompletedArguments()) {
				return tRes.addError(getValue(),line,TokenVALUE::CLOSEPARENTHESIS, ErrorType::UNEXPECTED);
			}
			else {
				argsOverload.setCompleteIndex();
				dispatchArguments();
				return true;
			}
		}
		if (elem->hasValue(TokenVALUE::COMMA)) {
			if(!mustSeparate)tRes.addError(getValue(),line,TokenVALUE::COMMA, ErrorType::UNEXPECTED);
			else {
				mustSeparate = false;
				continue;
			}
		}
		else {
			ValueType type = elem->getValueType(tRes);
			if (argsOverload.approveType(type)) {
				argTokens.push_back(elem);
				mustSeparate = true;
				argsOverload.next();
			}
			else return tRes.addError(getValue(),line,DataType::NONE, type);
		}
	}
}

bool TemplateToken::addTemplatedTypes(IteratorList<Token>& tl, TokenResult& tRes)
{
	bool mustSeparate = false;
	bool mustOpenAngle = true;
	templateArguments.initTabs();
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (!elem->addTokens(tl, tRes))return false;
		if (elem->hasValue(TokenVALUE::OPENANGLEBRACKETS)) {
			if (!mustOpenAngle)tRes.addError(getTValue(), templLine, TokenVALUE::OPENANGLEBRACKETS, ErrorType::UNEXPECTED);
			else {
				mustOpenAngle = false;
				continue;
			}
		}
		if (elem->hasValue(TokenVALUE::CLOSEANGLEBRACKETS)) {
			if (!templateArguments.hasCompletedArguments()) {
				return tRes.addError(getTValue(), templLine, TokenVALUE::CLOSEPARENTHESIS, ErrorType::UNEXPECTED);
			}
			else {
				templateArguments.setCompleteIndex();
				dispatchTemplateArguments();
				return true;
			}
		}
		if (elem->hasValue(TokenVALUE::COMMA)) {
			if (!mustSeparate)tRes.addError(getTValue(), templLine, TokenVALUE::COMMA, ErrorType::UNEXPECTED);
			else {
				mustSeparate = false;
				continue;
			}
		}
		else {
			ValueType type = elem->getValueType(tRes);
			if (templateArguments.approveType(type)) {
				templTokens.push_back(elem);
				mustSeparate = true;
				templateArguments.next();
			}
			else return tRes.addError(getTValue(), templLine, type);
		}
	}
}

void PKToken::setOverloads()
{
}

void PKToken::dispatchArguments()
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

void CoordToken::showArguments(const int nestedLayer)
{
	if (xPoint && yPoint) {
		xPoint->showTokenTree(nestedLayer);
		std::cout << ',';
		yPoint->showTokenTree(nestedLayer);
	}
	else if(coordToken){
		coordToken->showTokenTree(nestedLayer);
	}
}

PKToken::PKToken() :KToken()
{
	setOverloads();
}


bool Token::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	tl.next();
	return true;
}

bool PKToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	tl.next();
	return handleArguments(tl, tRes);
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
	KToken::addTokens(tl, tRes);
	if (TemplateToken::addTemplatedTypes(tl, tRes)) {
		return MPKToken::addTokens(tl, tRes);
	}
	return false;
}
bool CompareToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	KToken::addTokens(tl,tRes);
	if (TemplateToken::addTemplatedTypes(tl, tRes)) {
		return MPKToken::addTokens(tl, tRes);
	}
	return false;
}

bool ListToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	KToken::addTokens(tl, tRes);
	if (TemplateToken::addTemplatedTypes(tl, tRes)) {
		return MPKToken::addTokens(tl, tRes);
	}
	return false;
}

bool TemplateToken::addNumber(IteratorList<Token>& tl, TokenResult& tRes) {
	return addTypes({ DataType::INT,DataType::FLOAT },tl,tRes);
}

bool PKToken::addNumber(IteratorList<Token>& tl, TokenResult& tRes) {
	return addTypes({ DataType::INT,DataType::FLOAT },tl,tRes);
}


bool PKToken::addToken(TokenVALUE tVal, IteratorList<Token>& tl, TokenResult& tRes) {
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->getValue()==tVal) {
			return elem->addTokens(tl, tRes);
		}
	}
	return tRes.addError(getValue(), line, tVal);
}

bool PKToken::addType(ValueType tData, IteratorList<Token>& tl, TokenResult& tRes)
{
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->getValueType(tRes).equal(tData)) {
			return elem->addTokens(tl, tRes);
		}
	}
	return tRes.addError(getValue(),line, tData);
}


bool PKToken::addTypes(std::vector<ValueType> listTypes, IteratorList<Token>& tl, TokenResult& tRes)
{
	for (auto& t : listTypes) {
		if (addType(t, tl, tRes))return true;
	}
	return false;
}


bool TemplateToken::addTypes(std::vector<ValueType> listTypes, IteratorList<Token>& tl, TokenResult& tRes)
{
	for (auto& t : listTypes) {
		if (addType(t, tl, tRes))return true;
	}
	return false;
}

ValueType IdentifierToken::getValueType(TokenResult& tRes)
{
	auto map=tRes.getVarTable();
	auto elem = map.find(tokenText);
	if(elem != map.end())return elem->second;
	return ValueType(DataType::IDENTIFIER,0);
}


bool FlowToken::addNestedTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	while (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->hasValue(TokenVALUE::CLOSEBRACKETS)) {
			return addToken(TokenVALUE::CLOSEBRACKETS,tl,tRes);
		}
		else {
			if (elem->addTokens(tl, tRes)) {
				nestedTokens.push_back(elem);
			}
		}
	}
	return tRes.addError(TokenVALUE::FLOW,lineFlow,TokenVALUE::CLOSEBRACKETS);
}

bool FlowToken::addBody(IteratorList<Token>& tl, TokenResult& tRes)
{
	if (addToken(TokenVALUE::OPENBRACKETS,tl,tRes)) {
		return addNestedTokens(tl, tRes);
	}
	return false;
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
}

void FlowCKToken::showTokenTree(const int nestedLayer)
{
	CKToken::showTokenTree(nestedLayer);
	FlowToken::showTokenTree(nestedLayer);
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

BoolToken::BoolToken() :CKToken()
{
	tValue = TokenVALUE::BOOL;
	tokenText = boolK;
	setOverloads();
}

ValueType BoolToken::getValueType(TokenResult& tRes)
{
	return ValueType(DataType::BOOL);
}

std::shared_ptr<Tag> BoolToken::execute()
{
	return std::shared_ptr<Tag>();
}

WaitToken::WaitToken() :MPKToken()
{
	tValue = TokenVALUE::WAIT;
	tokenText = waitK;
	setOverloads();
}

void WaitToken::setOverloads()
{
	argsOverload.addArgs(Arguments({ DataType::INT,DataType::TIMETYPE }),0);
	argsOverload.addArgs(Arguments({ DataType::FLOAT,DataType::TIMETYPE }),1);
}

void WaitToken::dispatchArguments()
{
	switch (argsOverload.getID()) {
	default:
		if (argTokens.size() > 1) {
			numberToken = argTokens.front();
			timeTypeToken = argTokens.at(1);
		}
		break;
	}
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
	setOverloads();
}

void CKToken::setOverloads()
{
	argsOverload.addArgs(Arguments(DataType::BOOL,false),0);
}

void CKToken::dispatchArguments()
{
	switch (argsOverload.getID()) {
	case 0:
		if (argTokens.size() > 0)uniqueToken = argTokens.front();
	}
}

ValueType CKToken::getValueType(TokenResult& tRes)
{
	return DataType::BOOL;
}


std::shared_ptr<Tag> AndToken::execute()
{
	return std::shared_ptr<Tag>();
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

NotToken::NotToken() :CKToken()
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

ValueType FalseToken::getValueType(TokenResult& tRes)
{
	return DataType::BOOL;
}

TrueToken::TrueToken()
{
	tValue = TokenVALUE::TRUELITERAL;
	tokenText = trueL;
}

ValueType TrueToken::getValueType(TokenResult& tRes)
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

CommaToken::CommaToken() :PToken()
{
	tValue = TokenVALUE::COMMA;
	tokenText = commaP;
}

ContinueToken::ContinueToken() :KToken()
{
	tValue = TokenVALUE::CONTINUE;
	tokenText = continueK;
}


std::shared_ptr<Tag> ContinueToken::execute()
{
	return std::make_shared<ContinueTag>();
}

BreakToken::BreakToken() :KToken()
{
	tValue = TokenVALUE::BREAK;
	tokenText = breakK;
}


std::shared_ptr<Tag> BreakToken::execute()
{
	return std::make_shared<BreakTag>();
}

PrintToken::PrintToken() :MPKToken()
{
	tValue = TokenVALUE::PRINT;
	tokenText = printK;
	setOverloads();
}

void PrintToken::setOverloads()
{
	argsOverload.addArgs(Arguments(DataType::STRING,true),0);
}

std::shared_ptr<Tag> PrintToken::execute()
{
	return std::shared_ptr<Tag>();
}

DirectionToken::DirectionToken():UPKToken()
{
	tValue = TokenVALUE::DIRECTION;
	tokenText = directionK;
	setOverloads();
}

void DirectionToken::setOverloads()
{
	argsOverload.addArgs(Arguments(),0);
	argsOverload.addArgs(Arguments(DataType::DIRECTION, false),1);
}



ValueType DirectionToken::getValueType(TokenResult& tRes)
{
	return DataType::DIRECTION;
}

std::shared_ptr<Tag> DirectionToken::execute()
{
	return std::shared_ptr<Tag>();
}

ZoneToken::ZoneToken() :PKToken()
{
	tokenText = zoneK;
	tValue = TokenVALUE::ZONE;
	setOverloads();
}

void ZoneToken::setOverloads()
{
	argsOverload.addArgs(Arguments(),0);
	argsOverload.addArgs(Arguments(DataType::ZONE, false),1);
	argsOverload.addArgs(Arguments({ DataType::COORD,DataType::COORD }), 2);
}

void ZoneToken::dispatchArguments()
{
	switch (argsOverload.getID()) {
	case 0:
		topLeft = nullptr;
		bottomRight = nullptr;
		break;
	case 1:
		zoneToken = argTokens.front();
		break;
	case 2:
		topLeft = argTokens.at(0);
		bottomRight = argTokens.at(1);
		break;
	}
}

void ZoneToken::showArguments(const int nestedLayer)
{
	if (zoneToken) {
		zoneToken->showTokenTree(nestedLayer);
	}
	else if (topLeft && bottomRight) {
		topLeft->showTokenTree(nestedLayer);
		std::cout << ',';
		bottomRight->showTokenTree(nestedLayer);
	}
}

ValueType ZoneToken::getValueType(TokenResult& tRes)
{
	return DataType::ZONE;
}



std::shared_ptr<Tag> ZoneToken::execute()
{
	return std::shared_ptr<Tag>();
}

CoordToken::CoordToken() :PKToken()
{
	tokenText = coordK;
	tValue = TokenVALUE::COORD;
	setOverloads();
}

void CoordToken::setOverloads()
{
	argsOverload.addArgs(Arguments(),0);
	argsOverload.addArgs(Arguments(DataType::COORD, false),1);
	argsOverload.addArgs(Arguments({ DataType::INT, DataType::INT }), 2);
}

void CoordToken::dispatchArguments()
{
	switch (argsOverload.getID()) {
	case 0:
		xPoint = nullptr;
		yPoint = nullptr;
		break;
	case 1:
		coordToken = argTokens.front();
		break;
	case 2:
		xPoint = argTokens.at(0);
		yPoint = argTokens.at(1);
		break;
	}
}

ValueType CoordToken::getValueType(TokenResult& tRes)
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



FloatToken::FloatToken() :UPKToken()
{
	tokenText = floatK;
	tValue = TokenVALUE::FLOAT;
	setOverloads();
}

void FloatToken::setOverloads()
{
	argsOverload.addArgs(Arguments(),0);
	argsOverload.addArgs(Arguments(DataType::FLOAT, false),1);
	argsOverload.addArgs(Arguments(DataType::INT, false),2);
}

ValueType FloatToken::getValueType(TokenResult& tRes)
{
	return DataType::FLOAT;
}

std::shared_ptr<Tag> FloatToken::execute()
{
	return std::make_shared<FloatTag>(uniqueToken->execute());
}

IntegerToken::IntegerToken() :UPKToken()
{
	tokenText = intK;
	tValue = TokenVALUE::INTEGER;
	setOverloads();
}

void IntegerToken::setOverloads()
{
	argsOverload.addArgs(Arguments(),0);
	argsOverload.addArgs(Arguments(DataType::INT,false),1);
	argsOverload.addArgs(Arguments(DataType::FLOAT, false), 2);
}



ValueType IntegerToken::getValueType(TokenResult& tRes)
{
	return DataType::INT;
}



std::shared_ptr<Tag> IntegerToken::execute()
{
	return std::make_shared<IntTag>(uniqueToken->execute());
}

StoreToken::StoreToken() :MPKToken(), TemplateToken(line)
{
	tokenText = storeK;
	tValue = TokenVALUE::STORE;
	setTemplateOverload();
}

void StoreToken::setTemplateOverload()
{
	templateArguments.addArgs(Arguments({ DataType::DATATYPE,DataType::INT }), 0);
}

void StoreToken::setOverloads()
{
	argsOverload.addArgs(Arguments({ DataType::IDENTIFIER,vType}), 0);
}
void StoreToken::dispatchArguments()
{
	if (!argsOverload.empty()) {
		switch (argsOverload.getID()) {
		case 0:
			identifierToken = argTokens.at(0);
			valueToken = argTokens.at(1);
			break;
		}
	}
}

void StoreToken::dispatchTemplateArguments()
{
	if (!templateArguments.empty()) {

		switch (templateArguments.getID()) {
		case 0:	
			vType = ValueType(getDescribedType(templTokens.at(0)).type, stoi(templTokens.at(1)->getTokenText()));//Only expect a number, not a variable
			setOverloads();
			break;
		}
	}
}


std::shared_ptr<Tag> StoreToken::execute()
{
	return std::shared_ptr<Tag>();
}

ListToken::ListToken() :MPKToken(),TemplateToken(line)
{
	tokenText = listK;
	tValue = TokenVALUE::LIST;
	setTemplateOverload();
}

void ListToken::setOverloads()
{
	ValueType containedType (vType);
	containedType.dim--;
	argsOverload.addArgs(Arguments(containedType,true), 0);
}

void ListToken::setTemplateOverload()
{
	templateArguments.addArgs(Arguments({ DataType::DATATYPE,DataType::INT }), 0);
}

void ListToken::dispatchTemplateArguments()
{
	if (!templateArguments.empty()) {
		switch (templateArguments.getID()) {
		case 0:
			vType = ValueType(getDescribedType(templTokens.at(0)).type, stoi(templTokens.at(1)->getTokenText()));//Only expect a number, not a variable
			setOverloads();
			break;
		}
	}
}

std::shared_ptr<Tag> ListToken::execute()
{
	return std::shared_ptr<Tag>();
}

CompareToken::CompareToken() : MPKToken()
{
	tokenText = compareK;
	tValue = TokenVALUE::COMPARE;
	setTemplateOverload();
}

void CompareToken::setOverloads()
{
	switch (vType.type) {
	case DataType::BOOL:
		argsOverload.addArgs(Arguments(DataType::BOOL, true), 0);
		break;
	case DataType::COORD:
		argsOverload.addArgs(Arguments(DataType::COORD, true), 1);
		break;
	case DataType::DATATYPE:
		argsOverload.addArgs(Arguments(DataType::DATATYPE, true), 2);
		break;
	case DataType::DIRECTION:
		argsOverload.addArgs(Arguments(DataType::DIRECTION, true), 3);
		break;
	case DataType::FLOAT:
		argsOverload.addArgs(Arguments(DataType::FLOAT, true), 4);
		break;
	case DataType::INT:
		argsOverload.addArgs(Arguments(DataType::INT, true), 5);
		break;
	case DataType::STRING:
		argsOverload.addArgs(Arguments(DataType::STRING, true), 6);
		break;
	}
}

void CompareToken::setTemplateOverload()
{
	templateArguments.addArgs(Arguments({ DataType::DATATYPE,DataType::COMPARETYPE }), 0);
}

void CompareToken::dispatchTemplateArguments()
{
	if (!templateArguments.empty()) {

		switch (templateArguments.getID()) {
		case 0:
			vType = getDescribedType(templTokens.at(0));
			auto ctt = std::dynamic_pointer_cast<CompareTypeToken>(templTokens.at(1));
			if(ctt)cmpType = ctt->getCmpType();
			setOverloads();
			break;
		}
	}
}

void CompareToken::dispatchArguments()
{
}

ValueType CompareToken::getValueType(TokenResult& tRes)
{
	return DataType::BOOL;
}

std::shared_ptr<Tag> CompareToken::execute()
{
	return std::shared_ptr<Tag>();
}


StringToken::StringToken() :UPKToken()
{
	tokenText = stringK;
	tValue = TokenVALUE::STRING;
	setOverloads();
}

ValueType StringToken::getValueType(TokenResult& tRes)
{
	return DataType::STRING;
}

void StringToken::setOverloads()
{
	argsOverload.addArgs(Arguments(),0);
	argsOverload.addArgs(Arguments(DataType::STRING, false),1);
}

std::shared_ptr<Tag> StringToken::execute()
{
	return std::make_shared<StringTag>();
}

SecondToken::SecondToken()
{
	tokenText = secondL;
	tValue = TokenVALUE::SECOND;
}

ValueType SecondToken::getValueType(TokenResult& tRes)
{
	return DataType::TIMETYPE;
}

MilliSecondToken::MilliSecondToken()
{
	tokenText = millisecondL;
	tValue = TokenVALUE::MILLISECOND;
}

ValueType MilliSecondToken::getValueType(TokenResult& tRes)
{
	return DataType::TIMETYPE;
}

MinuteToken::MinuteToken()
{
	tokenText = minuteL;
	tValue = TokenVALUE::MINUTE;
}

ValueType MinuteToken::getValueType(TokenResult& tRes)
{
	return DataType::TIMETYPE;
}

NumericToken::NumericToken(const std::string& nb)
{
	tValue = TokenVALUE::NUMERIC;
	tokenText = nb;
	number = std::stoi(nb);
}

ValueType NumericToken::getValueType(TokenResult& tRes)
{
	return DataType::INT;
}

UnknownToken::UnknownToken():Token()
{
	tValue = TokenVALUE::UNKNOWN;
	tokenText = "Unknown";
}

StringLiteralToken::StringLiteralToken(const std::string& content) :LToken()
{
	tokenText = content;
	tValue = TokenVALUE::STRINGLITERAL;
}

void StringLiteralToken::showTokenTree(const int nestedLayer)
{
	std::cout << '"';
	Token::showTokenTree(nestedLayer);
	std::cout << '"';
}

ValueType StringLiteralToken::getValueType(TokenResult& tRes)
{
	return DataType::STRING;
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
	varTable = std::map<std::string, ValueType>();
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

bool TokenResult::addError(TokenVALUE scopeToken, int l, TokenVALUE errorToken, ErrorType et)
{
	listErrors.push_back(Error(scopeToken,l, errorToken, et));
	return false;
}

bool TokenResult::addError(TokenVALUE scopeToken,int l, ValueType& expectedType, ValueType& receivedType)
{
	listErrors.push_back(Error(scopeToken,l, expectedType,receivedType));
	return false;
}

bool TokenResult::addError(const TokenResult& tRes)
{
	listErrors.insert(listErrors.end(), tRes.listErrors.begin(), tRes.listErrors.end());
	return false;
}

void TokenResult::addVar(const std::string& name, const ValueType& type)
{
	varTable.insert(std::pair<std::string, ValueType>(name, type));
}

bool TokenResult::success()
{
	return listErrors.empty();
}

std::map<std::string, ValueType> TokenResult::getVarTable()
{
	return varTable;
}

Token::Token()
{
	
	tValue = TokenVALUE::TOKEN;
	tokenText = "Token";
	line = 0;
}

Error::Error()
{
	dataTypeError=false;
	tokenValueError=false;
	this->expectedType = DataType::NONE;
	this->receivedType = DataType::NONE;
	errorType = ErrorType::DATATYPE;
}

Error::Error(TokenVALUE scToken,int el, TokenVALUE tv, ErrorType et) :Error()
{
	tokenValueError = true;
	errorType = et;
	errorValue = tv;
	errorLine = el;
	scopeToken = scToken;
	showError();
}

Error::Error(TokenVALUE scToken,int el, ValueType& expectedType, ValueType& receivedType) :Error()
{
	dataTypeError = true;
	errorLine = el;
	this->expectedType = expectedType;
	this->receivedType = receivedType;
	scopeToken = scToken;
	showError();
}

void Error::showError()
{
	if (dataTypeError) {
		std::cout << "Error in " << TokenValueStrMap.find(scopeToken)->second << " line: " << this->errorLine<< " Expected type: ";
		expectedType.show();
		std::cout << "Received Type: ";
		receivedType.show();
	}
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
	std::cout << openBracketsP;
	for (auto& elem : nestedTokens) {
		std::cout << '\n';
		printTabs(nestedLayer + 1);
		elem->showTokenTree(nestedLayer + 1);
	}
	std::cout << '\n';
	printTabs(nestedLayer);
	std::cout << closeBracketsP + '\n';
}

bool FlowToken::addToken(TokenVALUE tVal, IteratorList<Token>& tl, TokenResult& tRes)
{
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->getValue() == tVal) {
			return elem->addTokens(tl, tRes);
		}
	}
	return tRes.addError(TokenVALUE::FLOW,lineFlow,tVal);
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

ValueType Token::getValueType(TokenResult& tRes)
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

void UPKToken::dispatchArguments()
{
	if (!argTokens.empty()) {

		switch (argsOverload.getID()) {
		case 0:
			uniqueToken = nullptr;// will create Basic tag at compile time
			break;
		case 1:
			uniqueToken = argTokens.front();
			break;
		}
	}
}



void UPKToken::showArguments(const int nestedLayer)
{
	if (uniqueToken)uniqueToken->showTokenTree(nestedLayer);
}

MPKToken::MPKToken() :PKToken()
{

}

bool MPKToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	return PKToken::addTokens(tl, tRes);
}


void MPKToken::showArguments(const int nestedLayer)
{
	int i = 0;
	for (auto& elem : argTokens) {
		elem->showTokenTree(nestedLayer);
		
		if(i<argTokens.size()-1)std::cout << ',';
		++i;
	}
}

Arguments::Arguments()
{
	currentIndex = 0;
	stillValid = true;
	completed = true;
	repeatable = false;
	repeatedType = DataType::NONE;
}
//repeatable type can be empty
Arguments::Arguments(ValueType type, bool repeat):Arguments()
{
	if (repeat) {
		repeatable = true;
		repeatedType = type;
	}
	else listArgsTypes.push_back(type);
	completed = true;
}


Arguments::Arguments(std::vector<ValueType> listTypes) :Arguments()
{
	listArgsTypes = listTypes;
	if (!listArgsTypes.empty())completed = false;
}


std::vector<ValueType> Arguments::getArgsTypes()
{
	return listArgsTypes;
}

void Arguments::next()
{

	if(!repeatable)	++currentIndex;
}

bool Arguments::ended()const
{
	if (repeatable)return false;
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

ValueType Arguments::at(int i)
{
	if (repeatable)return repeatedType;
	else if (i < size())return listArgsTypes.at(i);
}

bool Arguments::approveType(const ValueType& type)
{
	if (!ended()&&stillValid) {
		if (getCurrentValueType().equal(type)) {
			if (repeatable)completed = true;
			else if (currentIndex == listArgsTypes.size()-1) {
				completed = true;
			}
			return true;
		}
	}
	if(stillValid)stillValid = false;
	completed = false;
	return false;
}

ValueType Arguments::getCurrentValueType()
{	
	if (repeatable)return repeatedType;
	else return listArgsTypes.at(currentIndex);
}

bool Arguments::isRepeatable()
{
	return repeatable;
}

bool Arguments::isValid()const
{
	return stillValid;
}

bool Arguments::isCompleted()
{
	return completed;
}

bool Arguments::isEqual(Arguments args)
{
	if (repeatable != args.repeatable)return false;
	if (args.size() != size())return false;

	for (int i = 0; i < args.size(); ++i) {
		if (!args.at(i).equal(at(i)))return false;
	}
	return true;
}

BMPKToken::BMPKToken():MPKToken()
{
	setOverloads();
}

void BMPKToken::setOverloads()
{
	argsOverload.addArgs(Arguments(DataType::BOOL, true),0);
}


ValueType BMPKToken::getValueType(TokenResult& tRes)
{
	return ValueType(DataType::BOOL);
}

bool BMPKToken::addTokens(IteratorList<Token>& tl, TokenResult& tRes)
{
	return MPKToken::addTokens(tl,tRes);
}

bool ArgumentsOverload::hasValidArg()
{
	return getValidArguments().size() > 0;
}

bool ArgumentsOverload::hasCompletedArguments()
{
	return !getCompletedArgs().empty();
}

void ArgumentsOverload::updateCompletedArguments()
{
	std::vector<int> toDelete;
	for (auto& args : completedArguments) {
		if (!args.second->isValid())toDelete.emplace_back(args.first);
	}
	for (int i : toDelete) {
		completedArguments.erase(i);
	}
	for (auto& args : validArguments) {
		if (args.second->isCompleted())completedArguments.emplace(args);
	}
}

void ArgumentsOverload::updateValidArguments()
{
	std::vector<int> toDelete;
	for (auto& args : validArguments) {
		if (!args.second->isValid())toDelete.emplace_back(args.first);
	}
	for (int i : toDelete) {
		validArguments.erase(i);
	}
}

void ArgumentsOverload::updateTabs()
{
	updateValidArguments();
	updateCompletedArguments();
}

ArgumentsOverload::ArgumentsOverload()
{
	mapArguments.clear();
	currentDataTypes.clear();
	completeIndex = 0;
}

bool ArgumentsOverload::isPresent(Arguments args)
{
	for (auto arg : mapArguments) {
		if (arg.second->isEqual(args))return true;
	}
	return false;
}

void ArgumentsOverload::addArgs(Arguments args,int index)
{
	if (!isPresent(args)) {
		mapArguments.emplace(std::pair<int, std::shared_ptr<Arguments>>(index, std::make_shared<Arguments>(args)));
	}
}

void ArgumentsOverload::initTabs()
{
	for (auto& args : mapArguments) {
		if (args.second->isValid())validArguments.emplace(args);
		if (args.second->isCompleted())completedArguments.emplace(args);
	}
}

std::vector<ValueType> ArgumentsOverload::getPossibleValueTypes()
{
	currentDataTypes.clear();
	for (auto& arg : validArguments) {
		currentDataTypes.push_back(arg.second->getCurrentValueType());
	}
	return currentDataTypes;
}

void ArgumentsOverload::next()
{
	for (auto& arg : mapArguments)arg.second->next();
}

bool ArgumentsOverload::approveType(const ValueType& type)
{
	bool res=false;
	for (auto& arg : validArguments) {
		if(arg.second->approveType(type))res=true;
	}
	updateTabs();
	return res;
}

unsigned int ArgumentsOverload::getID()
{
	return completeIndex;
}

void ArgumentsOverload::setCompleteIndex()
{
	completeIndex = completedArguments.begin()->first;
}

std::map<int, std::shared_ptr<Arguments>> ArgumentsOverload::getValidArguments()
{
	return validArguments;
}

std::map<int, std::shared_ptr<Arguments>> ArgumentsOverload::getCompletedArgs()
{
	return completedArguments;
}



bool ArgumentsOverload::ended()
{
	for (auto arg : mapArguments) {
		if (!arg.second->ended())return false;
	}
	return true;
}

int ArgumentsOverload::size()
{
	return mapArguments.size();
}

bool ArgumentsOverload::empty()
{
	return mapArguments.empty();
}

TemplateToken::TemplateToken()
{
	templLine = 0;
}

TemplateToken::TemplateToken(int line) :TemplateToken()
{
	this->templLine = line;
//	this->setTemplateOverload();
}

bool TemplateToken::addToken(TokenVALUE tVal, IteratorList<Token>& tl, TokenResult& tRes)
{
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->getValue() == tVal) {
			return elem->addTokens(tl, tRes);
		}
	}
	return tRes.addError(TokenVALUE::TEMPLATE, templLine, tVal);
}

bool TemplateToken::addType(ValueType tData, IteratorList<Token>& tl, TokenResult& tRes)
{
	if (!tl.ended()) {
		auto elem = tl.currentToken();
		if (elem->getValueType(tRes).equal(tData)) {
			return elem->addTokens(tl, tRes);
		}
		tRes.addError(TokenVALUE::TEMPLATE, templLine, tData, elem->getValueType(tRes))
		return tRes.addError(TokenVALUE::TEMPLATE, templLine, tData, elem->getValueType(tRes));
	}

}


TokenVALUE TemplateToken::getTValue() const
{
	return TokenVALUE::TEMPLATE;
}

ValueType DirectionLToken::getValueType(TokenResult& tRes)
{
	return DataType::DIRECTION;
}

NorthToken::NorthToken()
{
	tValue = TokenVALUE::NORTH;
	tokenText = northL;
}

NorthWToken::NorthWToken()
{
	tValue = TokenVALUE::NORTHW;
	tokenText = northwL;
}

NorthEToken::NorthEToken()
{
	tValue = TokenVALUE::NORTHE;
	tokenText = northeL;
}

SouthToken::SouthToken()
{
	tValue = TokenVALUE::SOUTH;
	tokenText = southL;
}

SouthWToken::SouthWToken()
{
	tValue = TokenVALUE::SOUTHW;
	tokenText = southwL;
}

SouthEToken::SouthEToken()
{
	tValue = TokenVALUE::SOUTHE;
	tokenText = southeL;
}

DataTypeToken::DataTypeToken()
{
	tValue = TokenVALUE::DATATYPE;
	tokenText = datatypeL;
}

ValueType DataTypeToken::getValueType(TokenResult& tRes)
{
	return DataType::DATATYPE;
}

IntTypeToken::IntTypeToken()
{
	tValue = TokenVALUE::INTTYPE;
	tokenText = intL;
}

BoolTypeToken::BoolTypeToken()
{
	tValue = TokenVALUE::BOOLTYPE;
	tokenText = boolL;
}

StringTypeToken::StringTypeToken()
{
	tValue = TokenVALUE::STRINGTYPE;
	tokenText = stringL;
}

FloatTypeToken::FloatTypeToken()
{
	tValue = TokenVALUE::FLOATTYPE;
	tokenText = floatL;
}

CoordTypeToken::CoordTypeToken()
{
	tValue = TokenVALUE::COORDTYPE;
	tokenText = coordL;
}

ZoneTypeToken::ZoneTypeToken()
{
	tValue = TokenVALUE::ZONETYPE;
	tokenText = zoneL;
}

DirectionTypeToken::DirectionTypeToken()
{
	tValue = TokenVALUE::DIRECTIONTYPE;
	tokenText = directionL;
}

TimeTypeToken::TimeTypeToken()
{
	tValue = TokenVALUE::TIMETYPE;
	tokenText = timetypeL;
}

ValueType::ValueType()
{
	type = DataType::NONE;
	dim = 0;
}

ValueType::ValueType(DataType t, int l)
{
	dim = l;
	type = t;
}

ValueType::ValueType(const ValueType& vt)
{
	dim = vt.dim;
	type = vt.type;
}

bool ValueType::equal(const ValueType& vt)
{
	return vt.type==type&&vt.dim==dim;
}

std::string ValueType::show()
{
	auto str=dataStrMap.find(type);
	if (str != dataStrMap.end())
	std::cout << "DataType: " << str->second << "Dimension: "<<dim;
	return str->second;
}

CompareTypeToken::CompareTypeToken()
{
	tValue = TokenVALUE::COMPARETYPE;
	tokenText = comparetypeL;
}

CompareType CompareTypeToken::getCmpType()
{
	switch (tValue) {
	case TokenVALUE::GREATER:
		return CompareType::GREATER;
	case TokenVALUE::LESSER:
		return CompareType::LESSER;
	case TokenVALUE::EQUAL:
		return CompareType::EQUAL;
	case TokenVALUE::NOTEQUAL:
		return CompareType::NOTEQUAL;
	case TokenVALUE::LESSEREQUAL:
		return CompareType::LESSEREQUAL;
	case TokenVALUE::GREATEREQUAL:
		return CompareType::GREATEREQUAL;
	default:
		return CompareType::NONE;
	}
}

ValueType CompareTypeToken::getValueType(TokenResult& tRes)
{
	return ValueType(DataType::COMPARETYPE,0);
}

GreaterToken::GreaterToken()
{
	tValue = TokenVALUE::GREATER;
	tokenText = greaterL;
}

LesserToken::LesserToken()
{
	tValue = TokenVALUE::LESSER;
	tokenText = lesserL;
}

GreaterequalToken::GreaterequalToken()
{
	tValue = TokenVALUE::GREATEREQUAL;
	tokenText = greaterequalL;
}

LesserequalToken::LesserequalToken()
{
	tValue = TokenVALUE::LESSEREQUAL;
	tokenText = lesserequalL;
}

EqualToken::EqualToken()
{
	tValue = TokenVALUE::EQUAL;
	tokenText = equalL;
}

NotequalToken::NotequalToken()
{
	tValue = TokenVALUE::NOTEQUAL;
	tokenText = notequalL;
}

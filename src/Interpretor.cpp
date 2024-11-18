#include "Interpretor.h"

void Interpretor::compileScript(const std::string& text)
{
	lex.extractTokens(text);
	std::string allTokens = lex.showAllTokens();
	tl = IteratorList(lex.listTokens);
	if (!tl.empty()) {
		mainToken = std::dynamic_pointer_cast<MainToken>(tl.getFirst());
		if (tl.size() > 1) {
			if (mainToken)tr = mainToken->addTokens(tl, mainToken->getResult());
		}
	}
	mainToken->showTokenTree(0);
	if (tr->success())mainTag = std::dynamic_pointer_cast<MainTag>(mainToken->execute());

	int a = 1;
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
	compileScript(copyActivity(appendToFolder(ActivityFolder,ActivityName)));
}

std::string Interpretor::copyActivity(const std::string& ActivityPath)
{
	return getFileContent(ActivityPath);
}

std::shared_ptr<Tag> Interpretor::getActivityTag()
{
	return mainTag;
}





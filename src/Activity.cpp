#include "Activity.h"

Activity::Activity()
{
	activate = false;
	automatic = false;
	name.clear();
	key = '\0';
}

Activity::Activity(bool act, std::string n, char k)
{
	activate = act;
	name = n;
	key = k;
}

Activity::Activity(const Activity& copy)
{
	activate = copy.activate;
	name = copy.name;
	key = copy.key;
}

void Activity::showActivity() const
{
	std::cout << name << ":" << key << "  ";
}

ShortCut::ShortCut()
{
	key = '\0';
	string.clear();
	description.clear();
}

ShortCut::ShortCut(char k, std::string n, std::string desc)
{
	key = k;
	string = n;
	description = desc;
}

ShortCut::ShortCut(const ShortCut& copy)
{
	key = copy.key;
	string = copy.string;
	description = copy.description;
}

void ShortCut::showShortCut() const
{
	std::cout<<key<<":"<<description<<"\n";
}

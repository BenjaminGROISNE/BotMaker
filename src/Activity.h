#pragma once
#include "Interactions.h"


class Activity :public Interactions{
public:
	bool activate;
	bool automatic;
	std::string name;
	virtual bool doActivity() { return false; };
	char key;
	Activity();
	Activity(bool act, std::string n, char k);
	Activity(const Activity& copy);
	void showActivity()const;
};

class ShortCut {
public:
	char key;
	std::string string, description;
	ShortCut();
	ShortCut(char k, std::string n, std::string desc);
	ShortCut(const ShortCut& copy);
	void showShortCut()const;
};
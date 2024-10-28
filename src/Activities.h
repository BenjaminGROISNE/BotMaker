#pragma once

#include "Activity.h"

class Activities: public Activity
{
public:

Interactions inte;
std::vector<Activity*>allActivities;
std::vector<ShortCut>allShortCuts;
};

class Alchemy : public Activity 
{
public:
	Alchemy();
	bool doActivity()override;
	bool craftNormalPill(std::vector<std::string>craftsdigits, std::vector<std::string>coremat, std::vector<std::string>outcomecraft, std::vector<std::string>goodoutcome, Zone craft, Zone matquality, int pillbeforeepic, bool& cancraftmat, Coord cmaterial, std::string corequality, bool& cancraftanymat);
	bool craftBetterPill(std::vector<std::string>craftsdigits, std::vector<std::string>coremat, std::vector<std::string>outcomecraft, std::vector<std::string>goodoutcome, Zone craft, Zone matquality, bool& cancraftmat, Coord cmaterial, std::string corequality, bool& cancraftbetterpill, bool& cancraftanymat);
};

class Garden : public Activity
{
public:
	Garden();
	bool doActivity()override;

};

class Worldrift : public Activity
{
public:
	Worldrift();
	bool doActivity()override;

};

class Arena : public Activity
{
public:
	Arena();
	bool doActivity()override;

};

class Assistant : public Activity
{
public:
	Assistant();
	bool doActivity()override;

};

class Divinities : public Activity
{
public:
	Divinities();
	bool doActivity()override;

};

class Heirloom : public Activity
{
public:
	Heirloom();
	bool doActivity()override;

};

class Mail : public Activity
{
public:
	Mail();
	bool doActivity()override;

};

class Otherworld : public Activity
{
public:
	Otherworld();
	bool doActivity()override;

};

class Pack : public Activity
{
public:
	Pack();
	bool doActivity()override;

};

class Perks : public Activity
{
public:
	Perks();
	bool doActivity()override;

};

class Ranking : public Activity
{
public:
	Ranking();
	bool doActivity()override;

};

class Ressources : public Activity
{
public:
	Ressources();
	bool doActivity()override;

};

class goHomeFirstBoot : public Activity
{
public:
	goHomeFirstBoot();
	bool doActivity()override;
};

class goHome : public Activity
{
public:
	goHome();
	bool doActivity()override;

};

class launchGame : public Activity
{
public:
	launchGame();
	bool doActivity()override;

};

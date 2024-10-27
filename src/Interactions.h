#pragma once
#include "Units.h"
#include "paths.h"
#include "fadb.h"
#include "fopencv.h"
#include "BotConfig.h"

class Interactions
{
public:
	Interactions();
	bool fcA(const std::string& Templ, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY);
	bool fA(const std::string& Templ, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	bool fcpopupA();
	Template loopPopUp(typeMat color = Gray);
	void fillPopup();
	bool fcOneTemplateMultipleTemplateA(const std::string& Templ, Direction direction, int order, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY);
	bool fOneTemplateMultipleTemplateA(const std::string& Templ, Direction direction, int order, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	bool fOneTemplateMultipleTemplateA(const std::vector<std::string>& alltempl, const Direction& direction, int order, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	bool fcOneTemplateMultipleTemplateA(const std::vector<std::string>& alltempl, const Direction& direction, int order, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY);
	bool fOneMultipleTemplateA(std::vector<std::vector<std::string>> allTempl, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	bool fMultipleTemplateA(const std::vector<std::string>alltempl, std::vector<std::string>& foundTemplates, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	bool fMultipleCompareTemplateA(const std::vector<std::string>alltempl, const std::vector<std::string>simTempl, std::vector<std::string>& foundTemplates, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	bool fcOneTemplateA(const std::vector<std::string>& allTempl, std::string& foundTempl, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY);
	bool fOneTemplateA(const std::vector<std::string> allTempl, std::string& foundTempl, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	bool fcOneTemplateA(const std::vector<std::string>& toclick, const  std::vector<std::string>& tonotclick, std::string& foundTempl, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY);
	bool fcOneTemplateA(const std::vector<std::string>& toclick, const  std::vector<std::string>& tonotclick, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY);
	bool fCompareOneTemplateA(const std::vector<std::string>& allTempl, const std::vector<std::string>& simTempl, std::string& Tresult, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	bool fcOneTemplateA(const std::vector<std::string>& allTempl, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY);
	bool fOneTemplateA(const std::vector<std::string>& allTempl, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	bool fOneTemplateEraseA(std::vector<std::string>& allTempl, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	bool fcOneTemplateEraseA(std::vector<std::string>& allTempl, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY);
	bool fnbTemplateA(const std::string& Templ, int& nb, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	int fnbTemplateA(const std::string& Templ, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	int fnbTemplateA(const std::vector<std::string>& allTempl, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	int fnbMultipleCompareTemplateA(const std::vector<std::string>& allTempl, const std::vector<std::string>& simTempls, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	int fnbUniqueCompareTemplateA(const std::vector<std::string>& allTempl, const std::vector<std::string>& simTempls, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	int fnbCompareTemplateA(const std::string& Templ, const std::vector<std::string>& simTempls, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);

	bool fcCompareA(const std::string& goodTemplate, const std::string& similartemplate, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY);
	bool fCompareA(const std::string& goodTemplate, const std::string& similartemplate, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	bool fcCompareMultipleTemplateA(const std::string& goodTemplate, const std::vector<std::string>& similartemplates, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY);
	bool fCompareMultipleTemplateA(const std::string& goodTemplate, const std::vector<std::string>& similartemplates, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	bool fMultipleCompareMultipleTemplateA(const std::string& goodTemplate, const std::vector<std::string>& similartemplates, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	bool fCompareOneTemplateMultipleTemplateA(const std::vector<std::string>& alltempl, const std::vector<std::string>& simTempl, Direction direction, int order, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY, bool willInteract = false);
	bool fcCompareOneTemplateMultipleTemplateA(const std::vector<std::string>& alltempl, const std::vector<std::string>& simTempl, Direction direction, int order, Zone Z = Zone(), unsigned int delayMilliseconds = DEFAULTDELAY);
	bool templateFound(bool found, const Template& templ, bool willInteract = false, unsigned int delayMilliseconds = DEFAULTDELAY);
	bool templateFound(bool found, const Template& templ, bool willInteract = false, Action act = Action::Click, Coord coord = Coord(), unsigned int delayMilliseconds = DEFAULTDELAY);
	void resetCount();
	bool fSwipeA(std::string imgtemplate, Coord Cresult, Zone Z = Zone(), bool willInteract = true);
	bool fSwipeA(std::string imgtemplate, Direction endx, Zone Z = Zone(), bool willInteract = true);
	bool fMultipleDigitsA(int nb, std::vector<std::string>Nb, std::vector<Template>& Tresult, Zone Z);


	Number fNumberIntegerA(std::vector<std::string>digits, std::vector<Unit>Units, Zone Z);
	Number fNumberIntegerA(std::vector<std::string>digits, Zone Z);
	Number fNumberDecimalA(std::vector<std::string>digits, std::vector<Unit>Units, std::string dot, Zone Z);
	Number fNumberDecimalA(std::vector<std::string>digits, std::string dot, Zone Z);
	std::vector<Coord>orderCoords(const std::vector<Coord>& coords, Direction dir);
	std::vector<Template>orderTemplates(const std::vector<Template>& allTempl, Direction dir);
	std::vector<Coord> orderPoint(const std::vector<Coord>& coord, const Coord& Cf);
	Template getTemplate(const std::string& images, typeMat type);
	std::vector<Template> getTemplate(const std::vector<std::string>& images, typeMat type);
	int getDimX();
	int getDimY();
	day getUTCDay();
	int getUTCHour();
	void loadTemplate(std::string image);
	void loadAllTemplates();
	void getFilesPath(std::vector<std::string>& tab, std::filesystem::directory_iterator path);
	void showVector(std::vector<std::string> vect);
	int getYear();
	int getMonth();
	int getDay();
	int getHour();
	int getMinute();
	int getSecond();
	void setRebootCount(int nb);
	int getRebootCount();
	void setDimX(int x);
	void setDimY(int y);
	int getMatWidth(const std::string& name);
	int getMatHeight(const std::string& name);
	void wait(int milliSeconds);
	void gSwipeX(double x1, double x2, int millitime);
	void gSwipeY(double y1, double y2, int millitime);
	void gSwipe(double x1, double y1, double x2, double y2, int millitime);
	void click(Coord Coord);
	void click(int x, int y);
	void orderDigits(std::vector<Digit>& digits);
	bool eraseString(std::vector<std::string>& allTempl, std::string templ);
	bool fStr(const std::vector<std::string>& allTempl, std::string templ);
	bool fStr(const std::vector<std::string>& allTempl, const std::vector<std::string> allkeys);
	bool fOneStr(const std::vector<std::string>& allTempl, const std::vector<std::string> allkeys);
	int distancePoint(const Coord& fixpt, const Coord& pt);
	Coord resCoords;
	std::vector<Coord> resMultCoords;
	std::string adbId;
	const static int waitnotfound = 1000;
	const static int waitfound = 2000;
	const static int DEFAULTDELAY = waitfound;
	const static int MAXREBOOT = 20;
	std::vector<Popup>listPopup;
	std::map<std::string, Template> allTemplates;
	Template background;
	int rebootCount;
	int dimX, dimY;
};



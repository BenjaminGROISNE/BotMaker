#pragma once
#include <string>
#include <iostream>
enum EmulatorType
{
	BLUESTACKS,
	LDPLAYER,
	NOX,
	MEMU,
	OTHER
};

class Instance {
public:
	Instance();
	Instance(std::string instanceName);
	virtual std::string getAdbKey();
	const std::string getGlobalId();
	virtual void setGlobalId()=0;
	virtual EmulatorType getType();
	virtual void setLaunch(bool b);
	virtual void showInstance() = 0;
	std::string instanceName;
	std::string instanceFolder;
	std::string adbKey;
	std::string instanceId;
	EmulatorType myType;
	bool launch;
	int dpi;
	int width;
	int height;
	bool hasGame;
};
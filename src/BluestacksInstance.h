#pragma once
#include "Instance.h"
class BluestacksInstance : public Instance
{
public:
	BluestacksInstance();
	void showInstance()override;
	void setGlobalId()override;
	int bootTime;
	std::string engineName;
	std::string instancePath;
	std::string id;
};


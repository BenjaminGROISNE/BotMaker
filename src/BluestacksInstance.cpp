#include "BluestacksInstance.h"

BluestacksInstance::BluestacksInstance() : Instance()
{
	instancePath.clear();
	engineName.clear();
	bootTime = 0;
	myType =BLUESTACKS;
}

void BluestacksInstance::showInstance()
{
	std::cout << "	Instance Name: " << instanceName << std::endl;
	std::cout << "	Instance Path: " << instancePath << std::endl;
	std::cout << "	Instance Id: " << id << std::endl;
	std::cout << "	Instance Engine: " << engineName << std::endl;
	std::cout << "	Instance Boot Time: " << bootTime << std::endl;
	std::cout << "	Instance DPI: " << dpi << std::endl;
	std::cout << "	Instance Width: " << width << std::endl;
	std::cout << "	Instance Height: " << height << std::endl;
	std::cout << "	Instance Has Game: " << hasGame << std::endl;
	std::cout << "	Instance Type: Bluestacks" << std::endl;
}

void BluestacksInstance::setGlobalId()
{
		instanceId = id;
}

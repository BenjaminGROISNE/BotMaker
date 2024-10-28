#include "LDPlayerInstance.h"

LDPlayerInstance::LDPlayerInstance():Instance()
{
	packageInfoFileName = "reportbody.data";
	ldId = -1;
	myType = LDPLAYER;
}

LDPlayerInstance::LDPlayerInstance(std::string instanceName):LDPlayerInstance()
{
	this->instanceName = instanceName;
}

void LDPlayerInstance::showInstance()
{
	std::cout << "	Instance Name: " << instanceName << std::endl;
	std::cout << "	Instance Folder: " << instanceFolder << std::endl;
	std::cout << "	Instance LD Id: " << ldId << std::endl;
	std::cout << "	Instance Id: " << instanceId << std::endl;
	std::cout << "	Instance Engine: " << engineName << std::endl;
	std::cout << "	Instance DPI: " << dpi << std::endl;
	std::cout << "	Instance Width: " << width << std::endl;
	std::cout << "	Instance Height: " << height << std::endl;
	std::cout << "	Instance Has Game: " << hasGame << std::endl;
	std::cout << "	Instance Type : LDPlayer" << std::endl;

}

void LDPlayerInstance::setGlobalId()
{
		instanceId = engineName;
}




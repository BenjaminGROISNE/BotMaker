#include "Instance.h"

Instance::Instance()
{
	instanceName.clear();
	instanceFolder.clear();
	adbKey.clear();
	width=0;
	height =0;
	dpi=0;
	hasGame=false;
	launch = false;
	myType = OTHER;
}

Instance::Instance(std::string instanceName):Instance()
{
	this->instanceName = instanceName;
}

std::string Instance::getAdbKey()
{
	return adbKey;
}

const std::string Instance::getGlobalId()
{
	return instanceId;
}

EmulatorType Instance::getType()
{
	return myType;
}

void Instance::setLaunch(bool b)
{
	launch = b;
}

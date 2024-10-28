#pragma once
#include "Instance.h"

class LDPlayerInstance : public Instance {
	public:
		LDPlayerInstance();
		LDPlayerInstance(std::string instanceName);
		void showInstance()override;
		void setGlobalId()override;
		std::string packageInfoFileName;
		std::string engineName;
		int ldId;
};
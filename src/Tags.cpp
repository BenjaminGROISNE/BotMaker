#include "tags.h"



Tag::Tag()
{
	myType = TAG;
	nbParents = 0;
	tagText.clear();
	doExecute = true;
}

Tag::Tag(const Tag& toCopy) :Tag()
{
	doExecute = toCopy.doExecute;
	nbParents = toCopy.nbParents;
	tagText = toCopy.tagText;

}



std::shared_ptr<Tag> Tag::clone() {
	return std::make_shared<Tag>(*this);
} 

std::shared_ptr<FlowTag> FlowTag::clone() {
	return std::make_shared<FlowTag>(*this);
}

std::shared_ptr<MainTag> MainTag::clone() {
	return std::make_shared<MainTag>(*this);
}

std::shared_ptr<DoLoopTag> DoLoopTag::clone() {
	return std::make_shared<DoLoopTag>(*this);
}

std::shared_ptr<IfTag> IfTag::clone() {
	return std::make_shared<IfTag>(*this);
}

std::shared_ptr<ElseTag> ElseTag::clone() {
	return std::make_shared<ElseTag>(*this);
}

std::shared_ptr<ElifTag> ElifTag::clone() {
	return std::make_shared<ElifTag>(*this);
}

std::shared_ptr<LoopTag> LoopTag::clone() {
	return std::make_shared<LoopTag>(*this);
}

std::shared_ptr<BreakTag> BreakTag::clone() {
	return std::make_shared<BreakTag>(*this);
}

std::shared_ptr<ContinueTag> ContinueTag::clone() {
	return std::make_shared<ContinueTag>(*this);
}
template <typename T>
std::shared_ptr<StoreTag<T>> StoreTag<T>::clone() {
	return std::make_shared<StoreTag>(*this);
}

template<typename T>
std::shared_ptr<LoadTag<T>> LoadTag<T>::clone() {
	return std::make_shared<LoadTag>(*this);
}

std::shared_ptr<TimeTypeTag> TimeTypeTag::clone() {
	return std::make_shared<TimeTypeTag>(*this);
}

std::shared_ptr<CompareTypeTag> CompareTypeTag::clone() {
	return std::make_shared<CompareTypeTag>(*this);
}

template<typename T>
std::shared_ptr<ListTag<T>> ListTag<T>::clone() {
	return std::make_shared<ListTag>(*this);
}

std::shared_ptr<WaitTag> WaitTag::clone() {
	return std::make_shared<WaitTag>(*this);
}

template<typename T>
std::shared_ptr<PrintTag<T>> PrintTag<T>::clone() {
	return std::make_shared<PrintTag>(*this);
}

std::shared_ptr<BoolTag> BoolTag::clone() {
	return std::make_shared<BoolTag>(*this);
}

std::shared_ptr<IntTag> IntTag::clone() {
	return std::make_shared<IntTag>(*this);
}

std::shared_ptr<FloatTag> FloatTag::clone() {
	return std::make_shared<FloatTag>(*this);
}
std::shared_ptr<NotTag> NotTag::clone() {
	return std::make_shared<NotTag>(*this);
}
std::shared_ptr<AndTag> AndTag::clone() {
	return std::make_shared<AndTag>(*this);
}

std::shared_ptr<OrTag> OrTag::clone() {
	return std::make_shared<OrTag>(*this);
}

template <typename T>
std::shared_ptr<CompareTag<T>> CompareTag<T>::clone() {
	return std::make_shared<CompareTag>(*this);
}
std::shared_ptr<CoordTag> CoordTag::clone() {
	return std::make_shared<CoordTag>(*this);
}
std::shared_ptr<ZoneTag> ZoneTag::clone() {
	return std::make_shared<ZoneTag>(*this);
}
std::shared_ptr<DirectionTag> DirectionTag::clone() {
	return std::make_shared<DirectionTag>(*this);
}
std::shared_ptr<StringTag> StringTag::clone() {
	return std::make_shared<StringTag>(*this);
}


FlowTag::FlowTag(const FlowTag& toCopy) :FlowTag()
{
	if(toCopy.condition)condition =toCopy.condition->clone();
	previousTag= toCopy.previousTag;
	parentFlowTag = toCopy.parentFlowTag;
	listPreviousFlowTags = toCopy.listPreviousFlowTags;
}


MainTag::MainTag(const MainTag& toCopy) :FlowTag(toCopy)
{

}

DoLoopTag::DoLoopTag(const DoLoopTag& toCopy) :FlowTag(toCopy)
{

}

IfTag::IfTag(const IfTag& toCopy) :FlowTag(toCopy)
{

}
ElseTag::ElseTag(const ElseTag& toCopy) :FlowTag(toCopy)
{

}
ElifTag::ElifTag(const ElifTag& toCopy) :FlowTag(toCopy)
{

}

LoopTag::LoopTag(const LoopTag & toCopy) :FlowTag(toCopy)
{

}
BreakTag::BreakTag(const BreakTag& toCopy) :FlowTag(toCopy)
{

}
ContinueTag::ContinueTag(const ContinueTag& toCopy) :FlowTag(toCopy)
{

}
template <typename T>
StoreTag<T>::StoreTag(const StoreTag& toCopy) :Tag(toCopy)
{
	varName = toCopy.varName;
	myVariables = toCopy.myVariables;

}
template <typename Type>
LoadTag<Type>::LoadTag(const LoadTag& toCopy) :Tag(toCopy)
{
	varName = toCopy.varName;
	myVariables = toCopy.myVariables;
}
TimeTypeTag::TimeTypeTag(const TimeTypeTag& toCopy) :Tag(toCopy)
{
	myTime = toCopy.myTime;
}

CompareTypeTag::CompareTypeTag(const CompareTypeTag& toCopy) :Tag(toCopy)
{
	myComp = toCopy.myComp;
}


template<typename Type>
ListTag<Type>::ListTag(const ListTag& toCopy) :Tag(toCopy)
{
	myList = toCopy.myList;

}
WaitTag::WaitTag(const WaitTag& toCopy) :Tag(toCopy)
{
	type = toCopy.type;
	time = toCopy.type;
}

template <typename T>
PrintTag<T>::PrintTag(const PrintTag& toCopy) :Tag(toCopy)
{

}
BoolTag::BoolTag(const BoolTag& toCopy) :Tag(toCopy)
{
	myBool = toCopy.myBool;

}
IntTag::IntTag(const IntTag& toCopy) :Tag(toCopy)
{
	myInt = toCopy.myInt;
}
FloatTag::FloatTag(const FloatTag& toCopy) :Tag(toCopy)
{
	myFloat = toCopy.myFloat;
}
NotTag::NotTag(const NotTag& toCopy) :BoolTag(toCopy)
{
}

AndTag::AndTag(const AndTag& toCopy) :BoolTag(toCopy)
{
}
OrTag::OrTag(const OrTag& toCopy) :BoolTag(toCopy)
{
}

template <typename T>
CompareTag<T>::CompareTag(const CompareTag& toCopy) :BoolTag(toCopy)
{
	myBool = toCopy.myBool;
	compType = toCopy.compType;
}

CoordTag::CoordTag(const CoordTag& toCopy) :Tag(toCopy)
{
	myCoords = toCopy.myCoords;

}
ZoneTag::ZoneTag(const ZoneTag& toCopy) :Tag(toCopy)
{
	myZone = toCopy.myZone;

}
DirectionTag::DirectionTag(const DirectionTag& toCopy) :Tag(toCopy)
{
	myDir = toCopy.myDir;

}

StringTag::StringTag(const StringTag& toCopy) :Tag(toCopy)
{
	myString = toCopy.myString;
}



void FlowTag::showTag()
{
		for (int i = 0; i < nbParents; ++i) {
			std::cout << '\t';
		}
		std::cout << "Tag:"<<this->tagText<<"\n";
		for (int i = 0; i < nbParents+1; ++i) {
			std::cout << '\t';
		}
		std::cout<<"nestedTags:\n";
		for (auto& tag : nestedTags) {
			for (int i = 0; i < nbParents + 2; ++i) {
				std::cout << '\t';
			}
			if (tag) {
				std::cout <<"Tag:"<< tag->tagText << "\n";
			}
		}
		std::cout << '\n';
}



void Tag::execute()
{
}



//Flow Tags

FlowTag::FlowTag() :Tag()
{
	myType = FLOWTAG;
	previousTag = nullptr;
	parentFlowTag = nullptr;
	canReboot = false;
	executed = false;
	nestedTags.clear();
	forcedExit = false;
	rebootLoop = false;
	condition = nullptr;
}



DoLoopTag::DoLoopTag() :FlowTag()
{
	canReboot = true;
	tagText = doloopK;
	myType = DOLOOPTAG;
}

DoLoopTag::DoLoopTag(const std::shared_ptr<Tag>& condition,const std::vector<std::shared_ptr<Tag>>&listTags):DoLoopTag() {
	this->condition = condition->clone();
	nestedTags.insert(nestedTags.begin(), listTags.begin(), listTags.end());
	linkTags();
}

void DoLoopTag::execute()
{
	showTag();
	do{ 
		for (auto& currentTag : nestedTags) {
			currentTag->execute();
			if (forcedExit|| rebootLoop) {
				break;
			}
		}
		if (forcedExit) {
			forcedExit = false;
			break;
		}
	} while (checkCondition());
}

ElifTag::ElifTag() :FlowTag()
{
	canReboot = false;
	tagText = elifK;
	myType = ELIFTAG;
}

void ElifTag::execute()
{
	showTag();
	if (previousTag) {
		if (!previousTag->executed&&checkCondition()) {
			for (auto& currentTag : nestedTags) {
				currentTag->execute();
				if (forcedExit) {
					forcedExit = false;
					break;
				}
			}
		}
	}

}

ElifTag::ElifTag(const std::shared_ptr<Tag>& condition,const std::vector<std::shared_ptr<Tag>>& listTags):ElifTag()
{
	this->condition = condition->clone();
	nestedTags.insert(nestedTags.begin(), listTags.begin(), listTags.end());
	linkTags();
}

CaseTag::CaseTag() :FlowTag()
{
	canReboot = false;
	tagText = caseK;
	myType = CASETAG;
}

SwitchTag::SwitchTag() :FlowTag()
{
	canReboot = false;
	tagText = switchK;
	myType = SWITCHTAG;
}


ElseTag::ElseTag() : FlowTag()
{
	canReboot = false;
	tagText = elseK;
	myType = ELSETAG;
}

void ElseTag::execute()
{
	showTag();
	if (previousTag) {
		if (!previousTag->executed) {
			for (auto& currentTag : nestedTags) {
				currentTag->execute();
			}
		}
	}
}

ElseTag::ElseTag(const std::vector<std::shared_ptr<Tag>>& listTags) :ElseTag()
{
	nestedTags.insert(nestedTags.begin(), listTags.begin(), listTags.end());
	linkTags();
}

IfTag::IfTag() :FlowTag()
{
	canReboot = false;
	tagText = ifK;
	myType = IFTAG;
}

void IfTag::execute()
{
	showTag();
	if (checkCondition()) {
		for (auto& currentTag : nestedTags) {
			currentTag->execute();
			if (forcedExit) {
				forcedExit = false;
				break;
			}
		}
	}
}


IfTag::IfTag(const std::shared_ptr<Tag>& condition,const std::vector<std::shared_ptr<Tag>>& listTags):IfTag()
{
	this->condition =condition->clone();
	nestedTags.insert(nestedTags.begin(), listTags.begin(), listTags.end());
	linkTags();
}

//IfTag::IfTag(const IfTag&toCopy) :IfTag(toCopy.condition,toCopy.nestedTags)
//{
//
//}


LoopTag::LoopTag() :FlowTag()
{
	canReboot = true;
	tagText = loopK;
	myType = LOOPTAG;
}

LoopTag::LoopTag(const std::shared_ptr<Tag> conditionTag)
{
}

LoopTag::LoopTag(const std::shared_ptr<Tag>& condition,const std::vector<std::shared_ptr<Tag>>&listTags) :LoopTag() {
	this->condition = condition->clone();
	nestedTags.insert(nestedTags.begin(), listTags.begin(), listTags.end());
	linkTags();
}

void LoopTag::execute()
{

	while (checkCondition()) {
		showTag();
		for (auto& currentTag : nestedTags) {
			currentTag->execute();
			if (forcedExit || rebootLoop) {
				break;
			}
		}
		if (forcedExit) {
			forcedExit = false;
			break;
		}
	}
}



//Action Tags
template <typename T>
StoreTag<T>::StoreTag() :Tag()
{
	tagText = storeK;
	myVariables = nullptr;
	myType = STORETAG;
}


ClickTag::ClickTag() :Tag()
{
	tagText = clickK;
	
}

SwipeTag::SwipeTag() :Tag()
{
	tagText = swipeK;
	
}



WaitTag::WaitTag() :Tag()
{
	time = 0;
	this->type = SECOND;
	tagText = waitK;
	myType = WAITTAG;
}

WaitTag::WaitTag(const std::shared_ptr<Tag>& parameter,const TimeType& tType) :WaitTag()
{
	this->type = tType;
	timeTag=parameter;
}

WaitTag::WaitTag(int time, TimeType tType) :WaitTag()
{
	this->type = tType;
	this->time = time;
}



std::shared_ptr<WaitTag> getWaitTag(std::shared_ptr<Tag> tag)
{
	return std::dynamic_pointer_cast<WaitTag>(tag);
}

void WaitTag::execute()
{
	time = executeInt(timeTag);
	switch (type) {
	case MILLISECOND:
		std::this_thread::sleep_for(std::chrono::milliseconds(time));
		break;
	case SECOND:
		std::this_thread::sleep_for(std::chrono::seconds(time));
		break;
	case MINUTE:
		std::this_thread::sleep_for(std::chrono::minutes(time));
		break;
	default:
		std::this_thread::sleep_for(std::chrono::milliseconds(time));
	}
}


template<typename T>
std::shared_ptr < LoadTag<T>> getLoadTag(std::shared_ptr<Tag> tag) {
	return std::dynamic_pointer_cast<LoadTag<T>>(tag);
}

template<typename T>
LoadTag<T>::LoadTag() :Tag()
{
	myVariables = nullptr;
	value = T();
	myType = LOADTAG;
}

template <>
void StoreTag<int>::execute() {
	if (myVariables)myVariables->addVariable(varName, executeInt(valueTag));
}

template <>
void StoreTag<float>::execute() {
	if (myVariables)myVariables->addVariable(varName, executeFloat(valueTag));
}

template <>
void StoreTag<bool>::execute() {
	if (myVariables)myVariables->addVariable(varName, executeBool(valueTag));
}

template <>
void StoreTag<std::string>::execute() {
	if (myVariables)myVariables->addVariable(varName, executeString(valueTag));
}

template <>
void StoreTag<Coord>::execute() {
	if (myVariables)myVariables->addVariable(varName, executeCoord(valueTag));
}

template <>
void StoreTag<Zone>::execute() {
	if (myVariables)myVariables->addVariable(varName, executeZone(valueTag));
}

template <>
void StoreTag<Direction>::execute() {
	if (myVariables)myVariables->addVariable(varName, executeDirection(valueTag));
}


//vectors
template <typename T>
void StoreTag<std::vector<T>>::execute() {
	if (myVariables) {
		myVariables->addVariable(varName, executeList<T>(listValueTags));
	}
}

template <typename T>
std::vector<T> executeList(std::vector<std::shared_ptr<Tag>>listTags) {
	ListTag<T> listTag(listTags);
	listTag.execute();
	return listTag.myList;
}


template <typename T>
StoreTag<T>::StoreTag(const std::string& name,const std::shared_ptr<Tag>& value,UserVariables* uv):StoreTag<T>()
{
	this->valueTag = value;
	this->varName = name;
	this->myVariables = uv;
}

template<typename T>
LoadTag<T>::LoadTag(const std::string& name,UserVariables* uv) :LoadTag() {
	varName = name;
	myVariables = uv;
}

template<typename T>
void LoadTag<T>::execute() {
	value = myVariables->getValue<T>(varName);
}

BreakTag::BreakTag() :FlowTag()
{
	tagText = breakK;
	myType = BREAKTAG;
}

void BreakTag::execute()
{
	FlowTag* recParent = parentFlowTag;
	while (recParent) {
		recParent->forcedExit = true;
		if (recParent->canReboot)break;
		recParent = recParent->parentFlowTag;
	}
}

ContinueTag::ContinueTag() :FlowTag()
{
	tagText = continueK;
	myType = CONTINUETAG;
}

void ContinueTag::execute()
{
	FlowTag* recParent = parentFlowTag;
	while (recParent) {
		if (recParent->canReboot) {
			recParent->rebootLoop = true;
			break;
		}
		recParent->forcedExit = true;
		recParent = recParent->parentFlowTag;
	}
}


DimYTag::DimYTag() :Tag()
{

	
}

DimXTag::DimXTag() :Tag()
{

	
}
template<typename T>
std::shared_ptr < ListTag<T>> getListTag(std::shared_ptr<Tag> tag) {
	return std::dynamic_pointer_cast<ListTag<T>>(tag);
}

template<typename T>
void ListTag<T>::execute() {
	for (auto& tag : listTags) {
		if (tag) {
			switch (this->myType) {
				case LOADTAG: {
					std::shared_ptr<LoadTag<std::vector<T>>> ldTagList = getLoadTag<std::vector<T>>(tag);
					std::shared_ptr < LoadTag<T>> ldTag = getLoadTag<T>(tag);
					if (ldTagList) {
						ldTagList->execute();
						myList.insert(myList.end(), ldTagList->value.begin(), ldTagList->value.end());
					}
					else if (ldTag) {
						ldTag->execute();
						myList.push_back(ldTag->value);
					}
					break;
				}
				case LISTTAG: {
					std::shared_ptr <ListTag<T>> liTag = getListTag<T>(tag);
					if (liTag) {
						liTag->execute();
						myList.insert(myList.end(), liTag->myList.begin(), liTag->myList.end());
					}
					break;
				}
				default: {
					executeSimpleTag(tag);
					break;
				}
			}
		}
	}
}

template<>
void ListTag<int>::executeSimpleTag(std::shared_ptr<Tag> tag)
{
	if (tag) {
		myList.push_back(executeInt(tag));
	}
}
template<>
void ListTag<float>::executeSimpleTag(std::shared_ptr<Tag> tag)
{
	if (tag) {
		myList.push_back(executeFloat(tag));
	}
}
template<>
void ListTag<bool>::executeSimpleTag(std::shared_ptr<Tag> tag)
{
	if (tag) {
		myList.push_back(executeBool(tag));
	}
}

template<>
void ListTag<std::string>::executeSimpleTag(std::shared_ptr<Tag> tag)
{
	if (tag) {
		myList.push_back(executeString(tag));
	}
}

template<>
void ListTag<Coord>::executeSimpleTag(std::shared_ptr<Tag> tag)
{
	if (tag) {
		myList.push_back(executeCoord(tag));
	}
}

template<>
void ListTag<Zone>::executeSimpleTag(std::shared_ptr<Tag> tag)
{
	if (tag) {
		myList.push_back(executeZone(tag));
	}
}
template<>
void ListTag<Direction>::executeSimpleTag(std::shared_ptr<Tag> tag)
{
	if (tag) {
		myList.push_back(executeDirection(tag));
	}
}

template<typename Type>
ListTag<Type>::ListTag(const std::vector<std::shared_ptr<Tag>>& parameters):ListTag()
{
	listTags.insert(listTags.begin(), parameters.begin(), parameters.end());
}

template<typename Type>
ListTag<Type>::ListTag() :Tag()
{
	tagText = listK;
	myType = LISTTAG;
}

StringTag::StringTag() :Tag()
{
	tagText = stringK;
	myType = STRINGTAG;
}

std::shared_ptr<StringTag> getStringTag(std::shared_ptr<Tag> tag)
{
	return std::dynamic_pointer_cast<StringTag>(tag);
}

std::string executeString(const std::shared_ptr<Tag>& parameter) {
	std::shared_ptr<Tag> myTag = parameter;
	std::shared_ptr < StringTag> sTag;
	if (myTag) {
		switch (myTag->myType) {
		case STRINGTAG:
			sTag = getStringTag(myTag);
			sTag->execute();
			return sTag->myString;
		case LOADTAG:
				std::shared_ptr <LoadTag<std::string>> lTag = getLoadTag<std::string>(myTag);
				lTag->execute();
				return lTag->value;
			break;
		}
	}
	return std::string();
}

void StringTag::execute()
{
	if (doExecute) {
		myString = executeString(stringTag);
	}
}

StringTag::StringTag(std::string v):StringTag()
{
	myString = v;
	doExecute = false;
}

StringTag::StringTag(const std::shared_ptr<Tag>& parameter):StringTag()
{
	stringTag=parameter;
}

DirectionTag::DirectionTag() :Tag()
{
	tagText = directionK;
	myDir = N;
	myType = DIRECTIONTAG;
}

DirectionTag::DirectionTag(const std::shared_ptr<Tag>& parameter) :DirectionTag()
{
	dirTag=parameter;
}

DirectionTag::DirectionTag(Direction dir) :DirectionTag()
{
	myDir = dir;
	doExecute = false;
}

std::shared_ptr < DirectionTag> getDirectionTag(std::shared_ptr<Tag> tag) {
	return std::dynamic_pointer_cast<DirectionTag>(tag);
}

Direction executeDirection(const std::shared_ptr<Tag>& parameter) {
	std::shared_ptr < DirectionTag> myDir;
	if (parameter) {
		switch (parameter->myType) {
		case DIRECTIONTAG:
			myDir = getDirectionTag(parameter);
			myDir->execute();
			return myDir->myDir;
			break;
		}
	}
	return N;
}

void DirectionTag::execute() {
	if (doExecute) {
		myDir = executeDirection(dirTag);
	}
}

ZoneTag::ZoneTag() :Tag()
{
	tagText = zoneK;
}

ZoneTag::ZoneTag(const std::shared_ptr<Tag>& parameter) :ZoneTag()
{
	zoneTag=parameter;
}

std::shared_ptr < ZoneTag> getZoneTag(std::shared_ptr<Tag> tag)
{
	return std::dynamic_pointer_cast<ZoneTag>(tag);
}

std::shared_ptr < FlowTag> getFlowTag(std::shared_ptr<Tag> tag)
{
	return std::dynamic_pointer_cast<FlowTag>(tag);
}

Zone executeZone(const std::shared_ptr<Tag>& nestedTag) {
	std::shared_ptr<Tag> tag1 = nestedTag;
	if (tag1) {
		switch (tag1->myType) {
		case ZONETAG:
			return executeZone(getZoneTag(tag1)->zoneTag);
			break;
		case LOADTAG:
			std::shared_ptr < LoadTag<Zone>> lTag = getLoadTag<Zone>(tag1);
			lTag->execute();
			return lTag->value;
			break;
		}
	}
}

Zone executeZone(std::vector<std::shared_ptr<Tag>> nestedTags) {
	int nbPar = nestedTags.size();
	std::shared_ptr<Tag> tag1;
	std::shared_ptr<Tag> tag2;
	switch (nbPar) {
	case 1:
		executeZone(nestedTags.at(0));
		break;
	case 2:
		tag1 = nestedTags.at(0);
		tag2 = nestedTags.at(1);
		return Zone(executeCoord(tag1), executeCoord(tag2));
	}
	return Zone();
}

void ZoneTag::execute()
{
	myZone = executeZone(zoneTag);
}


CoordTag::CoordTag() :Tag()
{
	tagText = coordK;
	
}

CoordTag::CoordTag(const std::shared_ptr<Tag>& parameter) :CoordTag()
{
	coordsTag=parameter;
}

CoordTag::CoordTag(const std::shared_ptr<Tag>& xPoint, const std::shared_ptr<Tag>& yPoint)
{
}

std::shared_ptr < CoordTag> getCoordTag(std::shared_ptr<Tag> tag)
{
	return std::dynamic_pointer_cast<CoordTag>(tag);
}



Coord executeCoord(const std::shared_ptr<Tag>& tag1) {
	if (tag1) {
		switch (tag1->myType) {
		case COORDSTAG:
			return executeCoord(getCoordTag(tag1)->coordsTag);
			break;
		case LOADTAG: {
			std::shared_ptr<LoadTag<Coord>> lTag = getLoadTag<Coord>(tag1);
			lTag->execute();
			return lTag->value;
			break;
		}
		case LISTTAG: {
			auto listTag = getListTag<int>(tag1);
			listTag->execute();
			if (!listTag->myList.empty()) {
				return Coord(listTag->myList.front(), listTag->myList.back());
			}
			return Coord();
			break;
		}
		default:
			int c = executeInt(tag1);
			return Coord(c, c);
		}
	}
}

void CoordTag::execute()
{
	myCoords = executeCoord(coordsTag);
}



FindSwipeTag::FindSwipeTag() :Tag()
{
	tagText = findswipeK;
}


FindClickTag::FindClickTag() :Tag()
{
	tagText = findclickK;
	
}


FindTag::FindTag() : Tag()
{
	tagText = findK;

}

OrTag::OrTag() :BoolTag()
{
	tagText = orK;
	myType = ORTAG;
}



AndTag::AndTag() :BoolTag()
{
	tagText = andK;
	myType = ANDTAG;
}

AndTag::AndTag(const std::vector<std::shared_ptr<Tag>>& parameter) :AndTag()
{
	listTags.insert(listTags.begin(), parameter.begin(), parameter.end());
}

std::shared_ptr < AndTag> getAndTag(std::shared_ptr<Tag> tag)
{
	return std::dynamic_pointer_cast<AndTag>(tag);
}

void AndTag::execute()
{
	bool turned = false;
	for (auto& myTag : listTags) {
		if (!executeBool(myTag)) {
			myBool = false;
			turned = true;
			break;
		}
	}
	if (!turned)myBool = true;
}

std::shared_ptr < NotTag> getNotTag(std::shared_ptr<Tag> tag)
{
	return std::dynamic_pointer_cast<NotTag>(tag);
}

NotTag::NotTag(const std::shared_ptr<Tag>& parameter) :NotTag()
{
	boolTag=parameter;
}
NotTag::NotTag(bool b) :NotTag()
{
	myBool = !b;
	doExecute = false;
}

std::shared_ptr < OrTag> getOrTag(std::shared_ptr<Tag> tag)
{
	return std::dynamic_pointer_cast<OrTag>(tag);
}

OrTag::OrTag(const std::vector<std::shared_ptr<Tag>>& parameter) :OrTag()
{
	listTags.insert(listTags.begin(), parameter.begin(), parameter.end());
}

void OrTag::execute()
{
	bool myOr = false;
	for (auto& myTag : listTags) {
		if (executeBool(myTag)) {
			myOr = true;
			break;
		}
	}
}


NotTag::NotTag() :BoolTag()
{
	tagText = notK;
	myType = NOTTAG;
}

bool executeBool(const std::shared_ptr<Tag>& parameter) {
	std::shared_ptr < AndTag> aTag;
	std::shared_ptr < NotTag> nTag;
	std::shared_ptr < BoolTag> bTag;
	std::shared_ptr < OrTag> oTag;
	std::shared_ptr < LoadTag<bool>> lTag;
	std::shared_ptr < CompareTag<bool>> cTag;
	if (parameter) {
		std::shared_ptr<Tag> myTag = parameter;
		switch (myTag->myType) {
		case BOOLTAG:
			bTag = getBoolTag(myTag);
			bTag->execute();
			return bTag->myBool;
		case ANDTAG:
			aTag = getAndTag(myTag);
			if (aTag) {
				aTag->execute();
				return aTag->myBool;
			}
			break;
		case NOTTAG:
			nTag = getNotTag(myTag);
			if (nTag) {
				nTag->execute();
				return nTag->myBool;
			}
			break;
		case ORTAG:
			oTag = getOrTag(myTag);
			if (oTag) {
				oTag->execute();
				return oTag->myBool;
			}	
			break;
		case COMPARETAG:
			cTag = getCompareTag<bool>(myTag);
			if (cTag) {
				cTag->execute();
				return cTag->myBool;
			}
			break;
		case LOADTAG:
			lTag = getLoadTag<bool>(myTag);
			if (lTag) {
				lTag->execute();
				return lTag->value;
			}
			break;
		}
	}
	return false;
}

int executeInt(const std::shared_ptr<Tag>& myTag) {
	std::shared_ptr < IntTag> myInt;
	if (myTag) {

		switch (myTag->myType) {
		case INTEGERTAG:
			myInt = getIntTag(myTag);
			myInt->execute();
			return myInt->myInt;
			break;
		case LOADTAG:
			std::shared_ptr<LoadTag<int>> lTag = getLoadTag<int>(myTag);
			lTag->execute();
			return lTag->value;		
			break;
		}
	}
	return 0;
}

float executeFloat(const std::shared_ptr<Tag>& parameter) {
	std::shared_ptr < FloatTag> myFloat;
	std::shared_ptr<Tag> myTag;
	if (parameter) {
		myTag = parameter;
		switch (myTag->myType) {
		case FLOATTAG:
			myFloat = getFloatTag(myTag);
			myFloat->execute();
			return myFloat->myFloat;
		case LOADTAG:
			std::shared_ptr<LoadTag<float>> lTag = getLoadTag<float>(myTag);
			lTag->execute();
			return lTag->value;
			break;
		}
	}
	return 0;
}

void NotTag::execute()
{
	myBool = !executeBool(boolTag);
}

FloatTag::FloatTag():Tag()
{
	tagText = floatK;
	myFloat = 0;
	myType = FLOATTAG;
}

FloatTag::FloatTag(const std::shared_ptr<Tag>& parameter):FloatTag() {
	floatTag=parameter;
}

FloatTag::FloatTag(float nb) :FloatTag() {
	myFloat = nb;
	doExecute = false;
}

std::shared_ptr < FloatTag> getFloatTag(std::shared_ptr<Tag> tag)
{
	return std::dynamic_pointer_cast<FloatTag>(tag);
}

void FloatTag::execute()
{
	myFloat = executeFloat(floatTag);
}

FunctionTag::FunctionTag() :Tag()
{
	tagText = functionK;

}

IntTag::IntTag():Tag()
{
	tagText = intK;
	myInt = 0;
	myType = INTEGERTAG;
}

IntTag::IntTag(const std::shared_ptr<Tag>& parameter):IntTag() {
	intTag=parameter;
}

IntTag::IntTag(int nb) :IntTag() {
	myInt = nb;
	doExecute = false;
}

std::shared_ptr < IntTag> getIntTag(std::shared_ptr<Tag> tag)
{
	return std::dynamic_pointer_cast<IntTag>(tag);
}

void IntTag::execute()
{
	myInt=executeInt(intTag);
}

template <typename T>
CompareTag<T>::CompareTag() :BoolTag()
{
	tagText = compareK;
	compType = EQUAL;
	myBool = false;
	myType = COMPARETAG;
}

template <typename T>
std::shared_ptr<CompareTag<T>> getCompareTag(std::shared_ptr<Tag> tag)
{
	return std::dynamic_pointer_cast<CompareTag<T>>(tag);
}

template <typename T>
bool CompareGreater(std::vector<T> list) {
	if (!list.empty()) {
		T previous =list.at(0);
		for (int i = 1; i < list.size(); ++i) {
			if (previous >list.at(i)) {
				return false;
				break;
			}
		}
	}
	return true;
}

template <typename T>
bool CompareLesser(std::vector<T> list) {
	if (!list.empty()) {
		T previous = list.at(0);
		for (int i = 1; i < list.size(); ++i) {
			if (previous < list.at(i)) {
				return false;
				break;
			}
		}
	}
	return true;
}

template <typename T>
bool CompareGreaterEqual(std::vector<T> list) {
	if (!list.empty()) {
		T previous =list.at(0);
		for (int i = 1; i < list.size(); ++i) {
			if (previous >= list.at(i)) {
				return false;
				break;
			}
		}
	}
	return true;
}

template <typename T>
bool CompareLesserEqual(std::vector<T> list) {
	if (!list.empty()) {
		T previous = list.at(0);
		for (int i = 1; i < list.size(); ++i) {
			if (previous <= list.at(i)) {
				return false;
				break;
			}
		}
	}
	return true;
}

template <typename T>
bool CompareEqual(std::vector<T> list) {
	if (!list.empty()) {
		T previous = list.at(0);
		for (int i = 1; i < list.size(); ++i) {
			if (previous != list.at(i)) {
				return false;
				break;
			}
		}
	}
	return true;
}

template <typename T>
bool CompareNotEqual(std::vector<T> list) {
	if (!list.empty()) {
		T previous = list.at(0);
		for (int i = 1; i < list.size(); ++i) {
			if (previous == list.at(i)) {
				return false;
				break;
			}
		}
	}
	return true;
}

template <typename T>
void CompareTag<T>::execute()
{
	ListTag<T> listtag(listTags);
	listtag.execute();
	auto listValues = listtag.myList;
	switch (compType) {
	case EQUAL:
		myBool = CompareEqual(listValues);
		break;
	case NOTEQUAL:
		myBool = CompareNotEqual(listValues);
		break;
	}
}


template <>
void CompareTag<int>::execute()
{
	ListTag<int> listtag(listTags);
	listtag.execute();
	auto listValues = listtag.myList;
	switch (compType) {
	case EQUAL:
		myBool = CompareEqual(listValues);
		break;
	case NOTEQUAL:
		myBool = CompareNotEqual(listValues);
		break;
	case LESSER:
		myBool = CompareLesser(listValues);
		break;
	case GREATER:
		myBool = CompareGreater(listValues);
		break;
	case LESSEREQUAL:
		myBool = CompareLesserEqual(listValues);
		break;
	case GREATEREQUAL:
		myBool = CompareGreaterEqual(listValues);
		break;
	}
}

template <>
void CompareTag<float>::execute()
{
	ListTag<float> listtag(listTags);
	listtag.execute();
	auto listValues = listtag.myList;
	switch (compType) {
	case EQUAL:
		myBool = CompareEqual(listValues);
		break;
	case NOTEQUAL:
		myBool = CompareNotEqual(listValues);
		break;
	case LESSER:
		myBool = CompareLesser(listValues);
		break;
	case GREATER:
		myBool = CompareGreater(listValues);
		break;
	case LESSEREQUAL:
		myBool = CompareLesserEqual(listValues);
		break;
	case GREATEREQUAL:
		myBool = CompareGreaterEqual(listValues);
		break;
	}
}





template <typename T>
CompareTag<T>::CompareTag(const std::vector<std::shared_ptr<Tag>> &nestedTags,const CompareType& cType):CompareTag()
{
	this->listTags.insert(this->listTags.begin(), nestedTags.begin(), nestedTags.end());
	this->compType = cType;
}







bool FlowTag::checkCondition()
{
	if (!nestedTags.empty()) {
		return executeBool(condition);
	}
	return false;
}

void FlowTag::linkTags()
{
	std::vector<FlowTag*> prevTags;
	for (auto& currentTag : nestedTags) {
		if (isFlowTag(currentTag)) {
			std::shared_ptr < DoLoopTag> fTag = std::dynamic_pointer_cast<DoLoopTag>(currentTag);
			if (fTag) {
				fTag->parentFlowTag = this;
				if (!prevTags.empty())fTag->previousTag = prevTags.at(prevTags.size() - 1);
				else fTag->previousTag = nullptr;
				fTag->listPreviousFlowTags = prevTags;
				prevTags.push_back(fTag.get());
			}
			
		}
		if(currentTag)currentTag->nbParents = nbParents + 1;
	}
	for (auto& tag : nestedTags) {
		if (tag)tag->nbParents = nbParents + 1;
	}
}

BoolTag::BoolTag()
{
	myBool = false;
	myType = BOOLTAG;
	tagText = boolK;
}

BoolTag::BoolTag(const std::shared_ptr<Tag>& parameter) :BoolTag() {
	boolTag=parameter;
}

std::shared_ptr < BoolTag> getBoolTag(std::shared_ptr<Tag> tag)
{
	return std::dynamic_pointer_cast<BoolTag>(tag);
}



void BoolTag::execute()
{
	myBool = executeBool(boolTag);
}

BoolTag::BoolTag(bool v) :BoolTag()
{
	myBool = v;
	doExecute = false;
}

bool sameTagsType(std::vector<std::shared_ptr<Tag>> tags)
{
	TagType type;
	for (int i = 0; i < tags.size();++i) {
		if (i == 0) {
			type = tags.at(0)->myType;
			continue;
		}
		if (type != tags.at(i)->myType)return false;
	}
	return true;
}

MainTag::MainTag():FlowTag()
{
	myType = MAINTAG;
	tagText = mainK;
}

void MainTag::execute()
{
	showTag();
	for (auto& currentTag : nestedTags) {
		currentTag->execute();
		if (forcedExit) {
			forcedExit = false;
			break;
		}
	}
}

MainTag::MainTag(const std::vector<std::shared_ptr<Tag>>& tags):MainTag()
{
	nestedTags.insert(nestedTags.begin(), tags.begin(), tags.end());
	linkTags();
}

template <typename T>
PrintTag<T>::PrintTag():Tag()
{
	myType = PRINTTAG;
	tagText = printK;
}

template <typename T>
PrintTag<T>::PrintTag(const std::shared_ptr<Tag>& tag) :PrintTag<T>()
{
	toPrintTag=tag;
}

std::string getDirection(Direction d) {
	switch (d) {
	case N:
		return "N";
		break;
	case S:
		return "S";
		break;
	case E:
		return "E";
		break;
	case W:
		return "W";
		break;
	case NE:
		return "NE";
		break;
	case NW:
		return "NW";
		break;
	case SE:
		return "SE";
		break;
	case SW:
		return "SW";
		break;
	default:
		return "N";
	}
}

Direction getDirection(std::string d) {
	if (d == "N") {
		return N;
	}
	else if (d == "S") {
		return S;
	}
	else if (d == "E") {
		return E;
	}
	else if (d == "W") {
		return W;
	}
	else if (d == "NE") {
		return NE;
	}
	else if (d == "NW") {
		return NW;
	}
	else if (d == "SE") {
		return SE;
	}
	else if (d == "SW") {
		return SW;
	}
	else return N;
}

template <>
void PrintTag<int>::execute() {
	std::cout << executeInt(toPrintTag);
}
template <>
void PrintTag<float>::execute() {	
	std::cout << executeFloat(toPrintTag);
}
template <>
void PrintTag<std::string>::execute() {	
	std::cout << executeString(toPrintTag);
}
template <>
void PrintTag<bool>::execute() {
	std::cout << executeBool(toPrintTag);
}

template <>
void PrintTag<Coord>::execute() {	
	executeCoord(toPrintTag).showCoords();
}

template <>
void PrintTag<Zone>::execute() {	
	executeZone(toPrintTag).showZone();
}
template <>
void PrintTag<Direction>::execute() {	
	std::cout << getDirection(executeDirection(toPrintTag));
}

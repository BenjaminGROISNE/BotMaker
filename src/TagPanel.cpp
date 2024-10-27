#include "TagPanel.h"


TagPanel::TagPanel(bool topConstructor) :wxPanel()
{

}

TagPanel::TagPanel(wxWindow* parentWindow, bool topConstructor) :wxPanel(parentWindow)
{
	isInserting = false;
	isHovering = false;
	if (topConstructor) {
		init(this);
	}
}

TagPanel::TagPanel(const TagPanel& toCopy, bool topConstructor):TagPanel(toCopy.GetParent(), topConstructor){
	if(toCopy.myTag)myTag = toCopy.myTag->clone();
	isInserting = toCopy.isInserting;
	isHovering = toCopy.isHovering;
	previousHover = toCopy.previousHover;
	insertPoint = toCopy.insertPoint;
	insertRect = toCopy.insertRect;

}






FlowTagPanel::FlowTagPanel(bool topConstructor) :TagPanel(false) {}
FlowTagPanel::FlowTagPanel(wxWindow* parentWindow, bool topConstructor) :TagPanel(parentWindow, false) {
	auto parent = getFlowTagPanel(parentWindow);
	childrenShown = true;
	if (parent)nbFlowParents = parent->nbFlowParents+1;
	if (topConstructor) {
		init(this);
	}
}

FlowTagPanel::FlowTagPanel(const FlowTagPanel& toCopy, bool topConstructor):TagPanel(toCopy, topConstructor) {

	auto parent = getFlowTagPanel(toCopy.GetParent());
	childrenShown = true;
	if (parent)nbFlowParents = parent->nbFlowParents + 1;
	if (topConstructor) {
		init(this);
		cloneChildren(&toCopy);
		updateLayout();
		Layout();
	}
}

BoolTagPanel::BoolTagPanel(const BoolTagPanel& toCopy) :TagPanel(toCopy,false) {
	init(this);
}

EmptyPanel::EmptyPanel(const EmptyPanel& toCopy) :TagPanel(toCopy, false) {
	init(this);
}

IfTagPanel::IfTagPanel(const IfTagPanel& toCopy) :FlowTagPanel(toCopy,false) {
	init(this);
	cloneChildren(&toCopy);
	updateLayout();
	Layout();
}
LoopTagPanel::LoopTagPanel(const LoopTagPanel& toCopy) :FlowTagPanel(toCopy,false) {

	init(this);
	cloneChildren(&toCopy);
	updateLayout();
	Layout();
}



MainTagPanel::MainTagPanel(wxWindow* window):FlowTagPanel(window,false)
{
	init(this);
}

LoopTagPanel::LoopTagPanel() :FlowTagPanel(false)
{
}

LoopTagPanel::LoopTagPanel(wxWindow* parentWindow) :FlowTagPanel(parentWindow,false)
{
	init(this);
}

IfTagPanel::IfTagPanel(wxWindow* parentWindow) :FlowTagPanel(parentWindow, false)
{
	init(this);
}
BoolTagPanel::BoolTagPanel(wxWindow* parentWindow) :TagPanel(parentWindow, false)
{
	init(this);
}

EmptyPanel::EmptyPanel(wxWindow* parentWindow) :TagPanel(parentWindow, false)
{
	init(this);
}

FlowTagPanel::~FlowTagPanel() 
{
	for (TagPanel* panel : getChildrenTags()) {
		delete panel;
	}
}

int TagPanel::getTagHeight()
{
	return GetMinHeight();
}

int FlowTagPanel::getTagHeight()
{
	int height = 0;
	for (TagPanel* panel : getChildrenTags()) {
		height += panel->getTagHeight();
	}
	height += GetSize().GetHeight();
	return height;
}


void TagPanel::setColors()
{
	setPanelColor();
	setTagNameColor();
}

void TagPanel::setTagNameColor()
{
	tagNameColor = wxColour(panelColor.Red(), 255 - panelColor.Green(), 255 - panelColor.Blue());
}

void TagPanel::addMover()
{
	moveUpButton = new wxButton(this, wxID_ANY, "^");
	moveDownButton = new wxButton(this, wxID_ANY, "v");
	cloneButton = new wxButton(this, wxID_ANY, "D");
	cutButton = new wxButton(this, wxID_ANY, "M");
	auto flags = wxSizerFlags().Border(wxLEFT | wxRIGHT, 7).Expand().Border();
	SetLineFont(moveUpButton);
	SetLineFont(moveDownButton);
	SetLineFont(cloneButton);
	SetLineFont(cutButton);
	wxSize upSize = moveUpButton->GetTextExtent(moveUpButton->GetLabelText());
	wxSize downSize = moveDownButton->GetTextExtent(moveDownButton->GetLabelText());
	moveUpButton->SetSizeHints(wxSize(upSize.x*2,upSize.y));
	moveDownButton->SetSizeHints(wxSize(downSize.x * 2, downSize.y));
	wxSize dupSize = cloneButton->GetTextExtent(cloneButton->GetLabelText());
	wxSize cutPasteSize = cutButton->GetTextExtent(cutButton->GetLabelText());
	cloneButton->SetSizeHints(wxSize(upSize.x * 2, upSize.y));
	cutButton->SetSizeHints(wxSize(downSize.x * 2, downSize.y));

	moveUpButton->Bind(wxEVT_LEFT_DOWN, &TagPanel::OnMoveUpClick, this);
	moveDownButton->Bind(wxEVT_LEFT_DOWN, &TagPanel::OnMoveDownClick, this);
	lineSizer->Add(cloneButton, flags);
	lineSizer->Add(cutButton, flags);
	lineSizer->Add(moveUpButton, flags);
	lineSizer->Add(moveDownButton, flags);

}

bool TagPanel::isLastTag()
{
	auto parent = GetParentTag(this);
	if (parent) {
		return parent->isLastTagChild(this);
	}
	else return false;
}

bool TagPanel::isFirstTag()
{
	auto parent = GetParentTag(this);
	if (parent) {
		return parent->isFirstTagChild(this);
	}
	else return false;
}

void TagPanel::OnMoveUpClick(wxMouseEvent& evt)
{
	auto parent = GetParentTag(this);
	if (parent) {
		if (isFirstTag()) {
			auto GrandParent = GetParentTag(parent);
			if (GrandParent) {
				parent->detachPanel(this);
				Reparent(GrandParent);
				GrandParent->insertChild(parent, this, ABOVE);
				if (parent->isEmpty()) {
					parent->showEmptyPanel();
				}
				GrandParent->updateLayout();
				GrandParent->Layout();
				
			}
		}
		else {
			auto previousTag = getPreviousTag();
			if (previousTag) {
				parent->swapChildren(previousTag, this);
			}
			parent->updateLayout();
		}
	}
}

void TagPanel::OnDuplicateClick(wxMouseEvent& evt) {
	auto parent = GetParentTag(this);
	if (parent) {
		parent->insertChild(this, clone(), UNDER);
		parent->updateLayout();
		parent->Layout();
	}
}

void TagPanel::OnMoveDownClick(wxMouseEvent& evt)
{
	auto parent = GetParentTag(this);
	
	if (parent) {
		if (isLastTag()) {
			auto GrandParent = GetParentTag(parent);
			if (GrandParent) {
				parent->detachPanel(this);
				Reparent(GrandParent);
				GrandParent->insertChild(parent, this, UNDER);
				if (parent->isEmpty()) {
					parent->showEmptyPanel();
				}
				GrandParent->updateLayout();
				GrandParent->Layout();

			}
		}
		else {
			auto nextTag = getNextTag();
			if (nextTag) {
				parent->swapChildren(this, nextTag);
			}
			parent->updateLayout();
		}
	}
}




void TagPanel::setPaintEvents()
{
	Bind(wxEVT_PAINT, &TagPanel::DrawBordersEvent, this);
	Bind(wxEVT_PAINT, &TagPanel::OnHoverPaint, this);
	Bind(wxEVT_PAINT, &TagPanel::OnInsert, this);
}

bool TagPanel::isTagPanel(wxWindow* window)
{
	return dynamic_cast<TagPanel*>(window) != nullptr;
}

TagPanel* TagPanel::getPreviousTag()
{
	auto parent = GetParentTag(this);
	TagPanel* prev = nullptr;
	if (parent) {
		prev = parent->getPreviousTagChild(this);
	}
	return prev;
}

TagPanel* TagPanel::getNextTag()
{
	auto parent = GetParentTag(this);
	TagPanel* next=nullptr;
	if (parent) {
		next = parent->getNextTagChild(this);
	}
	return next;
}

std::shared_ptr<Tag> TagPanel::getTag()const
{
	return myTag;
}

void TagPanel::setTag(std::shared_ptr<Tag> tag)
{
	myTag =tag;
}

void TagPanel::initDetailsPanel(wxPanel* parentPanel) {
	detailsSizer = new wxBoxSizer(wxVERTICAL);
	detailsPanel = new wxPanel(parentPanel);
	detailsPanel->SetBackgroundColour(panelColor);
}

TagPanel* TagPanel::clone() {
	return new TagPanel(*this);
}
EmptyPanel* EmptyPanel::clone() {
	return new EmptyPanel(*this);
}
FlowTagPanel* FlowTagPanel::clone() {
	return new FlowTagPanel(*this);
}

BoolTagPanel* BoolTagPanel::clone() {
	return new BoolTagPanel(*this);
}
IfTagPanel* IfTagPanel::clone() {
	return new IfTagPanel(*this);
}

LoopTagPanel* LoopTagPanel::clone() {
	return new LoopTagPanel(*this);
}
MainTagPanel* MainTagPanel::clone() {
	return new MainTagPanel(*this);
}





void TagPanel::initDetailsTagName() {
	wxBoxSizer* nameDetails = new wxBoxSizer(wxHORIZONTAL);
	auto label = new wxStaticText(detailsPanel, wxID_ANY, "Tag:");
	SetLineFont(label, 11);
	label->SetForegroundColour(tagNameColor);
	staticDetailsName = new wxStaticText(detailsPanel, wxID_ANY, tagName);
	SetLineFont(staticDetailsName);
	staticDetailsName->SetForegroundColour(tagNameColor);
	nameDetails->Add(label);
	nameDetails->Add(staticDetailsName,wxSizerFlags().Border(wxLEFT));
	detailsSizer->Add(nameDetails, wxSizerFlags().Border().Expand());
}

void TagPanel::SetLineFont(wxButton* button, int policeSize) {
	button->SetFont(wxFont(policeSize, wxFONTFAMILY_DEFAULT, wxFONTSTYLE_NORMAL, wxFONTWEIGHT_BOLD));
}

void TagPanel::SetLineFont(wxStaticText* text, int policeSize) {
	text->SetFont(wxFont(policeSize, wxFONTFAMILY_DEFAULT, wxFONTSTYLE_NORMAL, wxFONTWEIGHT_BOLD));
}



void TagPanel::linkToPanel(wxBoxSizer* gridSizer) {
	detailsPanel->SetSizer(detailsSizer);
	gridSizer->Add(detailsPanel, wxSizerFlags().Expand().Proportion(1).Border());
}

void TagPanel::createDetailsPanel(TagPanel* tag,wxPanel* parentPanel, wxBoxSizer* gridSizer)
{
	tag->initDetailsPanel(parentPanel);
	tag->initDetailsTagName();
	tag->linkToPanel(gridSizer);
}

wxBoxSizer* TagPanel::getDetailsSizer()
{
	return detailsSizer;
}

wxPanel* TagPanel::getDetailsPanel()
{
	return detailsPanel;
}

wxColour TagPanel::getPanelColour()
{
	return panelColor;
}

wxBoxSizer* TagPanel::getTagSizer()
{
	return tagSizer;
}

TagPanel* TagPanel::getParentTag()
{
	return dynamic_cast<FlowTagPanel*>(GetParent());
}

TagPanel* TagPanel::getTagPanel(wxWindow* window)
{
	return dynamic_cast<TagPanel*>(window);
}

void TagPanel::setInsertPoint(HoverPosition hovPos)
{
	switch (hovPos) {
	case TOPHOVER:
		insertPoint = wxPoint(0, 0);
		break;
	case BOTTOMHOVER:
		insertPoint = wxPoint(0, 2*GetSize().GetHeight()/3);
		break;
	}
	previousHover = hovPos;
}

void FlowTagPanel::setInsertPoint(HoverPosition hovPos)
{
	wxSize tagLineSize = getLineSizer()->GetSize();
	auto bottomSpacer = getBottomSpacer();
	wxSize bottomSize;
	if (!isMinimized()) {
		bottomSize = getBottomSpacer()->GetSize();
	}
	int bottomHeight = bottomSize.y;
	int lineHeight = tagLineSize.y;
	switch (hovPos) {
	case TOPHOVER:
		insertPoint = wxPoint(0, 0);
		break;
	case MIDTOPHOVER:
		insertPoint = wxPoint(0, 2*lineHeight/3);
		break;
	case MIDBOTHOVER:
		insertPoint= wxPoint(0, GetSize().GetHeight() - bottomHeight);
		break;
	case BOTTOMHOVER:
		insertPoint = wxPoint(0, GetSize().GetHeight() - bottomHeight/3);
		break;
	}
	previousHover = hovPos;
}

void TagPanel::updateInsertRect()
{
	insertRect = wxSize(GetSize().x, (GetSize().y / 3)+1);
}
wxPoint TagPanel::getInsertPoint()
{
	return insertPoint;
}

wxButton* TagPanel::getCloneButton()const {
	return cloneButton;
}

wxButton* TagPanel::getCutButton() const
{
	return cutButton;
}

void FlowTagPanel::updateInsertRect()
{
	wxSize tagLineSize = getLineSizer()->GetSize();
	int lineHeight = tagLineSize.y;
	int hoverHeight = lineHeight / 3;
	insertRect = wxSize(GetSize().x, hoverHeight+1);
}


FlowTagPanel* getFlowTagPanel(wxWindow* window)
{
	return dynamic_cast<FlowTagPanel*>(window);
}



MainTagPanel* getMainTagPanel(TagPanel* window)
{
	return dynamic_cast<MainTagPanel*>(window);
}


void TagPanel::init(TagPanel* tag)
{
	tag->Hide();
	tag->initSizers();
	tag->setTagName();
	tag->setColors();
	tag->setPaintEvents();
	tag->addComponents(tag);
	tag->Show();
}

void TagPanel::setTagNameCustom(const std::string& name)
{
	tagName = name;
}




void FlowTagPanel::setTagName()
{
	setTagNameCustom("FlowTag");
}

void TagPanel::setTagName()
{
	setTagNameCustom("Tag");
}

void EmptyPanel::setTagName()
{
	setTagNameCustom("Empty");
}
void IfTagPanel::setTagName()
{
	setTagNameCustom("IF");
}

void BoolTagPanel::setTagName()
{
	setTagNameCustom("Bool");
}


void LoopTagPanel::setTagName()
{
	setTagNameCustom("Loop");
}






void MainTagPanel::setTagName()
{
	setTagNameCustom("Main");
}


void TagPanel::initSizers()
{
	initTagSizer();
	initLineSizer();
	setTagSizer();
}

void FlowTagPanel::initSizers()
{
	initTagSizer();
	initLineSizer();
	initChildrenSizer();
	addBottomSpacer();
	setTagSizer();
}

void FlowTagPanel::detachPanel(TagPanel* panel)
{
	bool k=childrenSizer->Detach(panel);
}

void FlowTagPanel::updateLayout() {//Goes to MainPanel to resize everything, might fail if the structure of a panelTag changes
	FlowTagPanel* parent = GetParentTag(this);
	if (parent) {
		parent->updateLayout();
	}
	else {
		auto programWindow = GetParent();
		if (programWindow)programWindow->SetVirtualSize(programWindow->GetBestVirtualSize());
		Layout();
	}
}

void FlowTagPanel::hideChildren()
{
	for (TagPanel* m : getChildrenTags()) {
		if (m)m->Hide();
	}
	tagSizer->Detach(getIndexOf(tagSizer, bottomSpacer));
}
 
void FlowTagPanel::showChildren()
{
	for (TagPanel* m : getChildrenTags()) {
		if(m)m->Show();
	}
	bottomSpacer=tagSizer->AddSpacer(leftBorder);
}

void FlowTagPanel::hideEmptyPanel()
{
	emptyTagPanel->Hide();
	childrenSizer->Detach(emptyTagPanel);
}
void FlowTagPanel::showEmptyPanel()
{
	if (emptyTagPanel) {
		addChild(emptyTagPanel);
		emptyTagPanel->Show();
	}
}

void FlowTagPanel::initEmptyPanel()
{
	emptyTagPanel = new EmptyPanel(this);
	addChild(emptyTagPanel);
}

void TagPanel::initDeletePanel()
{
	deleteButton = new wxButton(this, wxID_ANY, "X");
	SetLineFont(deleteButton,13);
	deleteButton->SetForegroundColour(wxColour(255, 255, 255));
	deleteButton->SetBackgroundColour(wxColour(255, 0, 0));
	wxSize textSize = deleteButton->GetTextExtent(deleteButton->GetLabel());
	deleteButton->SetSizeHints(wxSize(textSize.GetWidth()*2.5, textSize.GetHeight()));
	lineSizer->Add(deleteButton, wxSizerFlags().Expand().Border());
	deleteButton->Bind(wxEVT_LEFT_DOWN, &TagPanel::OnDeleteButtonClick, this);
}

void TagPanel::OnDeleteButtonClick(wxMouseEvent& evt) {
	auto parent = GetParentTag(this);
	if (parent) {
		parent->getChildrenSizer()->Detach(this);
	}
	Destroy();
	if (parent) {
		if (parent->isEmpty()) {
			parent->showEmptyPanel();
		}
		parent->Layout();
		parent->updateLayout();
	}
}

void FlowTagPanel::RemoveTagPanel(TagPanel* childPanel) {
	if(containsChild(childPanel))RemoveChild(childPanel);
	int index = getIndex(childPanel);
	if(index>=0)childrenSizer->Remove(index);
}

void FlowTagPanel::setChildrenVisibility(bool state)
{
	switch (state) {
	case false:
		hideChildren();
		hider->SetLabelText("+");
		childrenShown = false;
		break;
	case true:
		showChildren();
		hider->SetLabelText("-");
		childrenShown = true;
		break;
	}
}

int FlowTagPanel::getIndex(wxSizerItem* item)
{
	if (item) {
		auto listChildren = childrenSizer->GetChildren();
		for (auto it : listChildren) {
			if (it == item) {
				return listChildren.IndexOf(it);
			}
		}
	}
	return -1;
}

int TagPanel::getIndexOf(wxSizer* sizer,wxSizerItem* item)
{
	if (item) {
		auto& listChildren = sizer->GetChildren();
		for (auto it : listChildren) {
			if (it == item) {
				return listChildren.IndexOf(it);
			}
		}
	}
	return -1;
}

int FlowTagPanel::getIndex(TagPanel* panel)
{
	if (panel) {
		wxSizerItem* item=getSizerItem(panel);
		if (item) {
			return childrenSizer->GetChildren().IndexOf(item);
		}
	}
	return -1;
}


int FlowTagPanel::getNbParents()
{
	return nbFlowParents;
}

bool FlowTagPanel::childrenVisible()
{
	return childrenShown;
}

wxButton* FlowTagPanel::getHider()
{
	return hider;
}




void TagPanel::DrawBordersEvent(wxPaintEvent& event) {
	wxPaintDC dc(this);
	dc.SetPen(wxPen(wxColour(0, 0, 0), 1));
	dc.SetBrush(*wxTRANSPARENT_BRUSH);
	wxSize size = GetSize();
	dc.DrawRectangle(wxPoint(0, 0), size);
	event.Skip();
}
void TagPanel::OnHoverPaint(wxPaintEvent& event) {

	if (isHovering&&!isInserting) {
		wxPaintDC dc(this);
		int red = getPanelColour().Red()*0.7;
		int green = getPanelColour().Green()*0.7;
		int blue = getPanelColour().Blue()*0.7;
		wxColour HoverColor = wxColour(red, green, blue);
		dc.SetPen(wxPen(HoverColor));
		dc.SetBrush(wxBrush(HoverColor));
		dc.DrawRectangle(wxPoint(0, 0), GetSize());
	}
	event.Skip();
}
void TagPanel::OnInsert(wxPaintEvent& evt)
{
	if (isInserting) {
		wxPaintDC dc(this);
		wxColour grayColour(150, 150, 150);
		dc.SetPen(wxPen(grayColour));
		dc.SetBrush(wxBrush(grayColour));
		updateInsertRect();
		dc.DrawRectangle(insertPoint, insertRect);
	}
	evt.Skip();
}


void TagPanel::setStaticTagName()
{
	policeSize = 13;
	staticTagName = new wxStaticText(this, wxID_ANY,tagName);
	wxFont font(policeSize, wxFONTFAMILY_DEFAULT, wxFONTSTYLE_NORMAL, wxFONTWEIGHT_EXTRAHEAVY);
	staticTagName->SetFont(font);
	staticTagName->SetForegroundColour(tagNameColor);
	addTagName();
}

void TagPanel::addTagName()
{
	lineSizer->Add(staticTagName, wxSizerFlags().Expand().Border(wxALL,7));
}

void TagPanel::setPanelColor()
{
	panelColor = wxColour(200,200,200);
	SetBackgroundColour(panelColor);
	this->SetBackgroundColour(panelColor);
}
void EmptyPanel::setPanelColor()
{
	panelColor = wxColour(255, 255, 255);
	SetBackgroundColour(panelColor);
	this->SetBackgroundColour(panelColor);
}

void LoopTagPanel::setPanelColor()
{
	panelColor = wxColour(255, 255, 0);
	SetBackgroundColour(panelColor);
	this->SetBackgroundColour(panelColor);
}

void MainTagPanel::setPanelColor()
{
	panelColor = wxColour(30, 30, 30);
	SetBackgroundColour(panelColor);
	this->SetBackgroundColour(panelColor);
}
void IfTagPanel::setPanelColor()
{
	panelColor = wxColour(150, 255, 100);
	SetBackgroundColour(panelColor);
	this->SetBackgroundColour(panelColor);
}

void BoolTagPanel::setPanelColor()
{
	panelColor = wxColour(50, 50, 230);
	SetBackgroundColour(panelColor);
	this->SetBackgroundColour(panelColor);
}


void FlowTagPanel::setPanelColor()
{
	int r = rand() % 255;
	int g = r + 255 / 2;
	panelColor = wxColour(r, g, rand() % 255);
	SetBackgroundColour(panelColor);
	this->SetBackgroundColour(panelColor);
}

void TagPanel::initTagSizer()
{
	tagSizer = new wxBoxSizer(wxVERTICAL);
}

void TagPanel::initLineSizer()
{
	lineSizer = new wxBoxSizer(wxHORIZONTAL);
	tagSizer->Add(lineSizer, wxSizerFlags().Expand());
}


void FlowTagPanel::initChildrenSizer()
{
	childrenSizer = new wxBoxSizer(wxVERTICAL);
	if (tagSizer)tagSizer->Add(childrenSizer, wxSizerFlags().Expand());
}

void FlowTagPanel::cloneChildren(const FlowTagPanel* toCopy) {
	for (TagPanel* panel : toCopy->getChildrenTags()) {
		if (panel != toCopy->emptyTagPanel) {
			TagPanel* copy = panel->clone();
			copy->Reparent(this);
			addChild(copy);
		}
	}
}

void TagPanel::addComponents(TagPanel* tag)
{
	tag->setTagLine();
}

void FlowTagPanel::addComponents(TagPanel* tag)
{
	tag->setTagLine();
	initEmptyPanel();
}

void TagPanel::setTagLine()
{
	setStaticTagName();
	lineSizer->AddStretchSpacer();
	addMover();
	initDeletePanel();
}
void BoolTagPanel::setTagLine()
{
	setStaticTagName();
	lineSizer->AddStretchSpacer();
	addMover();
	initDeletePanel();
}
void EmptyPanel::setTagLine()
{
	setStaticTagName();
}

void FlowTagPanel::setTagLine()
{
	setStaticTagName();
	lineSizer->AddStretchSpacer();
	addMover();
	initDeletePanel();
	addHider();
}

void IfTagPanel::setTagLine()
{
	setStaticTagName();
	addCondition();
	lineSizer->AddStretchSpacer();
	addMover();
	initDeletePanel();
	addHider();
}


void LoopTagPanel::setTagLine()
{
	setStaticTagName();
	addCondition();
	lineSizer->AddStretchSpacer();
	addMover();
	initDeletePanel();
	addHider();

}
void MainTagPanel::setTagLine()
{
	setStaticTagName();
	lineSizer->AddStretchSpacer();
	addHider();
}


void FlowTagPanel::addHider() {
	hider = new wxButton(this, wxID_ANY, "-");
	SetLineFont(hider,18);
	wxSize textSize = hider->GetTextExtent(hider->GetLabel());
	hider->SetSizeHints(wxSize(textSize.GetWidth()*3, textSize.GetHeight()));
	lineSizer->Add(hider,wxSizerFlags().Expand());
	hider->Bind(wxEVT_LEFT_DOWN, &FlowTagPanel::switchFlowTagVisibility, this);
}


void FlowTagPanel::addCondition()
{
	wxButton* conditionButton = new wxButton(this, wxID_ANY, "CONDITION");
	lineSizer->Add(conditionButton, wxSizerFlags().Expand());
}

bool FlowTagPanel::isEmpty()
{
	auto listChildren = getChildrenTags();
	int size = listChildren.size();
	if (size == 0)return true;
	else if (size == 1) {
		if (listChildren.front() == emptyTagPanel)return true;
		else return false;
	}
	return false;
}

wxSizerItem* FlowTagPanel::getBottomSpacer()
{
	return bottomSpacer;
}

void FlowTagPanel::addBottomSpacer()
{
	bottomSpacer = tagSizer->AddSpacer(leftBorder);
}

void TagPanel::setTagSizer()
{
	SetSizer(tagSizer);
}

wxBoxSizer* FlowTagPanel::getChildrenSizer()
{
	return childrenSizer;
}
wxBoxSizer* TagPanel::getLineSizer()
{
	return lineSizer;
}
EmptyPanel* FlowTagPanel::getEmptyTagPanel()
{
	return emptyTagPanel;
}

wxwxSizerItemListNode* FlowTagPanel::getTagNode(TagPanel* panel)
{
	if (!panel)return nullptr;
	return childrenSizer->GetChildren().Find(getSizerItem(panel));
}

TagPanel* FlowTagPanel::getPreviousTagChild(TagPanel* panel)
{
	if (!panel)return nullptr;
	return getNodeToTag(getTagNode(panel)->GetPrevious());
}

TagPanel* FlowTagPanel::getNodeToTag(wxwxSizerItemListNode* node)
{
	if (!node)return nullptr;
	return dynamic_cast<TagPanel*>(node->GetData()->GetWindow());
}

TagPanel* FlowTagPanel::getNextTagChild(TagPanel* panel)
{
	if (!panel)return nullptr;
	return getNodeToTag(getTagNode(panel)->GetNext());
}

TagPanel* FlowTagPanel::getFirstTag()
{
	auto listChildren = getChildrenTags();
	TagPanel* firstTag=nullptr;
	if (!listChildren.empty()) {
		firstTag = listChildren.front();
	}
	return firstTag;
}

TagPanel* FlowTagPanel::getLastTag()
{
	auto listChildren = getChildrenTags();
	TagPanel* lastTag = nullptr;
	if (!listChildren.empty()) {
		lastTag = listChildren.back();
	}
	return lastTag;
}

bool FlowTagPanel::isLastTagChild(TagPanel* panel)
{
	auto j = getChildrenTags();
	TagPanel* teg = getLastTag();
	return teg==panel;
}

bool FlowTagPanel::isFirstTagChild(TagPanel* panel)
{
	return getFirstTag() == panel;
}

int FlowTagPanel::getNbChildren()
{
	return childrenSizer->GetChildren().GetCount();
}


wxSizerItem* FlowTagPanel::getSizerItem(TagPanel* panel)
{
	auto& listChildren = childrenSizer->GetChildren();
	for (auto item : listChildren) {
		if (panel == item->GetWindow())return item;
	}
	return nullptr;
}

bool FlowTagPanel::isMinimized() const
{
	return !childrenShown;
}

void FlowTagPanel::addChild(TagPanel* childrenTag)
{
	if (childrenTag){
		if (nbFlowParents < MAXFLOW) {
			insertChild(childrenTag, childrenSizer->GetItemCount());
		}
	}
}

void FlowTagPanel::switchFlowTagVisibility(wxMouseEvent& evt) {
	wxButton* but = dynamic_cast<wxButton*>(evt.GetEventObject());
	if (but) {
		FlowTagPanel* parentPanel = dynamic_cast<FlowTagPanel*>(but->GetParent());
		if (parentPanel) {
			switch (parentPanel->childrenVisible()) {
			case false:
				parentPanel->setChildrenVisibility(true);
				break;
			case true:
				parentPanel->setChildrenVisibility(false);
				break;
			}
			parentPanel->updateLayout();
		}
	}

}

void FlowTagPanel::insertChild(TagPanel* target,TagPanel* newPanel,AddPosition pos)
{
	int offset=0;
	switch (pos) {
	case ABOVE:
		offset = 0;
		break;
	case UNDER:
		offset = 1;
		break;
	}
	wxSizerItem* item=getSizerItem(target);
	if (item) {
		int targetIndex = getIndexOf(childrenSizer, item);
		insertChild(newPanel, targetIndex + offset);
	}
}

void FlowTagPanel::insertChild(TagPanel* newPanel, int index)
{
	if (containsChild(newPanel))return;
	wxSizerFlags insertFlags = wxSizerFlags().Expand().Border(wxLEFT, leftBorder);
	childrenSizer->Insert(index, newPanel, insertFlags);
	if (isEmpty())showEmptyPanel();
	else hideEmptyPanel();
}

void FlowTagPanel::swapChildren(TagPanel* panel1, TagPanel* panel2) {
	if (containsChild(panel1) && containsChild(panel2)) {
		int index2=getIndex(panel2);
		detachPanel(panel2);
		childrenSizer->Replace(panel1, panel2);
		insertChild(panel1, index2);
		childrenSizer->Layout();
	}
}

bool FlowTagPanel::containsChild(TagPanel* panel)
{
	return getIndex(panel)!=-1;
}

void FlowTagPanel::addChildren(std::vector<TagPanel*> childrenTags)
{
	for (TagPanel* tag : childrenTags) {
		addChild(tag);
	}
}

void FlowTagPanel::addTagAndSize(std::vector<TagPanel*> childrenTags)
{
	addChildren(childrenTags);
	setTagSizer();
}


std::vector<TagPanel*> FlowTagPanel::getChildrenTags()const
{
	std::vector<TagPanel*> windowList;

	auto& sizerChildren = this->childrenSizer->GetChildren();
	for (auto& item : sizerChildren) {
		auto child = dynamic_cast<TagPanel*>(item->GetWindow());
		if(child)windowList.push_back(child);
	}
	return windowList;
}

FlowTagPanel* GetParentTag(TagPanel* panel)
{
	return dynamic_cast<FlowTagPanel*>(panel->getParentTag());
}

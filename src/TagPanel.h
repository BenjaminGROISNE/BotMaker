#pragma once
#include <wx/wxprec.h>
#include <wx/artprov.h>
#include <wx/splitter.h>
#include <math.h>
#include "Tags.h"

#ifndef WX_PRECOMP
#include <wx/wx.h>
#endif

static int MAXFLOW = 5;
static int leftBorder = 30;
enum AddPosition { ABOVE, UNDER };
enum HoverPosition{NOHOVER,TOPHOVER,BOTTOMHOVER,MIDTOPHOVER,MIDBOTHOVER};
class TagPanel:public wxPanel
{
public:
	TagPanel(bool topConstructor = true);
	TagPanel(wxWindow* parentWindow, bool topConstructor=true);
	TagPanel(const TagPanel& toCopy, bool topConstructor = true);

	virtual TagPanel* clone();
	virtual int getTagHeight();
	virtual void setTagName();
    void setTagNameCustom(const std::string&name);
	virtual void setTagLine();
	virtual void setColors();
	void initLineSizer();
	virtual void setPanelColor();
	virtual void initTagSizer();
	virtual void addComponents(TagPanel*tag);
	virtual void initSizers();
	static int getIndexOf(wxSizer* sizer, wxSizerItem* item);
	void DrawBordersEvent(wxPaintEvent& event);
	void OnHoverPaint(wxPaintEvent& event);
	//static methods
	void setStaticTagName();
	static void init(TagPanel* tag);
	static bool isTagPanel(wxWindow* window);
	TagPanel* getPreviousTag();
	TagPanel* getNextTag();
	wxBoxSizer* getLineSizer();
	virtual void addTagName();
	void setTagSizer();
	void setTagNameColor();
	void addMover();
	bool isLastTag();
	bool isFirstTag();
	void OnMoveUpClick(wxMouseEvent& evt);
	void OnDuplicateClick(wxMouseEvent& evt);
	void OnMoveDownClick(wxMouseEvent& evt);
	virtual void initDeletePanel();
	void OnDeleteButtonClick(wxMouseEvent& evt);
	void setPaintEvents();
	std::shared_ptr<Tag> getTag()const;
	void setTag(std::shared_ptr<Tag> tag);
	void initDetailsPanel(wxPanel* parentPanel);
	void initDetailsTagName();
	void SetLineFont(wxButton* button,int policeSize=15);
	void SetLineFont(wxStaticText* button,int policeSize = 15);
	void OnInsert(wxPaintEvent& evt);
	void linkToPanel(wxBoxSizer* gridSizer);
	virtual void createDetailsPanel(TagPanel* tag, wxPanel* parentPanel, wxBoxSizer* gridSizer);
	wxBoxSizer* getDetailsSizer();
	wxPanel* getDetailsPanel();
	wxColour getPanelColour();
	wxBoxSizer* getTagSizer();
	TagPanel* getParentTag();
	static TagPanel* getTagPanel(wxWindow* window);
	virtual void setInsertPoint(HoverPosition hovPos);
	virtual void updateInsertRect();
	wxPoint getInsertPoint();
	wxButton* getCloneButton()const;
	wxButton* getCutButton()const;
	HoverPosition previousHover;
	bool isInserting;
	bool isHovering;
protected:
	wxButton* cloneButton;
	wxButton* cutButton;
	wxButton* moveUpButton;
	wxButton* moveDownButton;
	wxButton* deleteButton;
	wxPoint insertPoint;
	wxSize insertRect;
	wxColour panelColor;
	wxColour tagNameColor;
	wxString tagName;
	int policeSize;
	wxBoxSizer* lineSizer;
	wxBoxSizer* tagSizer;
	wxStaticText* staticTagName;
	wxStaticText* staticDetailsName;
	wxBoxSizer* detailsSizer;
	wxPanel* detailsPanel;
	std::shared_ptr<Tag> myTag;

};

class EmptyPanel : public TagPanel {
public:
	EmptyPanel(wxWindow* parentWindow);
	EmptyPanel(const EmptyPanel& toCopy);
	EmptyPanel* clone()override;
	void setTagName()override;
	void setTagLine()override;
	void setPanelColor()override;
};

class FlowTagPanel :public TagPanel
{
public:
	FlowTagPanel(bool topConstructor = true);
	FlowTagPanel(wxWindow* parentWindow,bool topConstructor=true);
	FlowTagPanel(const FlowTagPanel& toCopy, bool topConstructor = true);
	FlowTagPanel* clone()override;
	~FlowTagPanel();
	void initChildrenSizer();
	void cloneChildren(const FlowTagPanel* toCopy);
	void addComponents(TagPanel* tag)override;
	void addChild(TagPanel* childrenTag);
	void switchFlowTagVisibility(wxMouseEvent& evt);
	void insertChild(TagPanel* target, TagPanel* newPanel, AddPosition pos);
	void insertChild(TagPanel* newPanel, int index);
	void swapChildren(TagPanel* panel1, TagPanel* panel2);
	bool containsChild(TagPanel* panel);
	void addChildren(std::vector<TagPanel*> childrenTag);
	void addTagAndSize(std::vector<TagPanel*> childrenTag);
	int getTagHeight()override;
	void addHider();
	void setTagLine()override;
	void updateInsertRect()override;
	void setPanelColor()override;
	void setTagName()override;
	void initSizers()override;
	void detachPanel(TagPanel* panel);
	void setInsertPoint(HoverPosition hovPos)override;
	void updateLayout();
	void hideChildren();
	void showChildren();
	void hideEmptyPanel();
	void showEmptyPanel();
	void initEmptyPanel();
	void addCondition();
	bool isEmpty();
	wxSizerItem* getBottomSpacer();
	void addBottomSpacer();
	void RemoveTagPanel(TagPanel* childPanel);
	void setChildrenVisibility(bool state);
	int getIndex(wxSizerItem* item);
	int getIndex(TagPanel* panel);
	int getNbParents();
	bool childrenVisible();
	wxButton* getHider();
	wxBoxSizer* getChildrenSizer();
	EmptyPanel* getEmptyTagPanel();
	wxwxSizerItemListNode* getTagNode(TagPanel* panel);
	TagPanel* getPreviousTagChild(TagPanel* panel);
	TagPanel* getNodeToTag(wxwxSizerItemListNode* node);
	TagPanel* getNextTagChild(TagPanel* panel);
	TagPanel* getFirstTag();
	TagPanel* getLastTag();
	bool isLastTagChild(TagPanel* panel);
	bool isFirstTagChild(TagPanel* panel);
	int getNbChildren();
	wxSizerItem* getSizerItem(TagPanel* panel);
	bool isMinimized()const;
	std::vector<TagPanel*> getChildrenTags()const;
protected:
	wxSizerItem* bottomSpacer;
	EmptyPanel* emptyTagPanel;
	int nbFlowParents;
	wxBoxSizer* childrenSizer;
	wxButton* hider;
private:
	bool childrenShown;
};


class MainTagPanel :public FlowTagPanel
{
public:
	MainTagPanel(wxWindow* window);
	void setTagName();
	void setPanelColor();
	void setTagLine()override;
	MainTagPanel* clone()override;

protected:
};

class BoolTagPanel :public TagPanel
{
public:
	BoolTagPanel(wxWindow* parentWindow);
	BoolTagPanel* clone()override;
	BoolTagPanel(const BoolTagPanel& toCopy);
	void setTagName();
	void setPanelColor();
	void setTagLine()override;
protected:

private:
	BoolTag* myTag;
};

class LoopTagPanel : public FlowTagPanel
{
public:
	LoopTagPanel();
	LoopTagPanel(wxWindow* parentWindow);
	LoopTagPanel* clone()override;
	LoopTagPanel(const LoopTagPanel& toCopy);
	void setPanelColor()override;
	void setTagLine()override;
	void setTagName()override;
protected:

};

class IfTagPanel : public FlowTagPanel
{
public:
	IfTagPanel(wxWindow* parentWindow);
	IfTagPanel* clone()override;
	IfTagPanel(const IfTagPanel& toCopy);
	void setPanelColor()override;
	void setTagLine()override;
	void setTagName()override;
protected:

};
FlowTagPanel* getFlowTagPanel(wxWindow* window);
MainTagPanel* getMainTagPanel(TagPanel* panel);
FlowTagPanel* GetParentTag(TagPanel* panel);
TagPanel* CreatePanelFromTag(wxWindow* parentTag,Tag* tag);






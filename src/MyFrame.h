#pragma once
// For compilers that support precompilation, includes "wx/wx.h".
#include <wx/wxprec.h>
#include <wx/artprov.h>
#include <wx/splitter.h>
#include <math.h>

// for all others, include the necessary headers (this file is usually all you
// need because it includes almost all "standard" wxWidgets headers)
#ifndef WX_PRECOMP
#include <wx/wx.h>
#endif
#include "TagPanel.h"
#include "Interpretor.h"



class MyFrame : public wxFrame
{
public:
    MyFrame(const wxString& title);
    ~MyFrame();
    void setMainPanel();
    void setMenu();
    void setTopPanel();
    void setBottomPanel();
    void setProgramPanel();
    void setTagChoicePanel();
    void setProgramWindow();
    void setTagDetailsPanel();
    void initDetailsTitle();
    std::shared_ptr<MainTag> getMainTag();
    void closeDetailsPanel();
    void setNeighboursPanel(TagPanel* panel,HoverPosition currentHover);
    FlowTagPanel* createPanelTags();
    void resetTagChoicePanel();
    void createLogicTagChoice();
    void createMathsTagChoice();
    void createFindTagChoice();
    void createControlTagChoice();
    void BindChoiceButtons();
    void BindControlButtons();
    void BindEventsRec(TagPanel* panel);
    void BindMainTagPanelEvents();
    void BindFlowTagPanelEvents(TagPanel* newFlowPanel);
    void BindTagPanelEvents(TagPanel* newPanel);
    void BindEmptyPanelEvents(TagPanel* newPanel);
    void resetInsertHover();
    MainTagPanel* mainTagPanel;
    bool HoverSwitch;
private:
    TagPanel* hoverNeighbour;
    TagPanel* previousNeighbour;

    wxFont mainChoiceFont;
    wxFont subChoiceFont;
    std::shared_ptr<MainTag> mainTag;
    Interpretor myInterpretor;

    wxPanel* mainPanel;
    wxBoxSizer* mainSizer;

    wxPanel* topPanel;
    wxGridSizer* topGrid;

    wxPanel* tagDetailsPanel;
    wxBoxSizer* tagDetailsSizer;

    wxPanel* tagChoicePanel;
    wxBoxSizer* tagChoiceSizer;



    wxPanel* programPanel;
    wxBoxSizer* programSizer;

    wxScrolledWindow* programWindow;
    wxBoxSizer* programGrid;


    TagPanel* activeDetailsPanel;
    wxButton* leaveDetailsButton;

    TagPanel* pointedPanel;
    TagPanel* previousPointed;

    wxPoint drawPoint;
    wxSize drawRect;
    TagPanel* drawPanel;

    wxPanel* bottomPanel;
    wxBoxSizer* bottomSizer;

    std::shared_ptr<Tag> tagToAdd;

    //Top buttons
    wxButton* LogicButton;
    wxButton* MathsButton;
    wxButton* FindButton;
    wxButton* VariableButton;
    wxButton* ControlButton;
    wxButton* previousMainChoice;
    //Sub buttonS
    // Sub Logic
    wxButton* previousSubChoice;

    wxButton* boolButton;
    wxButton* orButton;
    wxButton* andButton;
    wxButton* notButton;
    wxButton* compareButton;

    //Sub Maths
    wxButton* addButton;
    wxButton * substractButton;
    wxButton* multiplyButton;
    wxButton* divideButton;

    //Sub Find
    wxButton* findButton;
    wxButton* findclickButton;
    wxButton* findOneMultipleButton;
    wxButton* findSwipeButton;

    //Sub Control
    wxButton* loopButton;
    wxButton* ifButton;
    wxButton* doLoopButton;
    wxButton* elseButton;
    wxButton* elseIfButton;


    void CreateInsertPanel(std::shared_ptr<Tag>tagAdd,TagPanel* flowPanel,AddPosition pos);
    MainTagPanel* CreateMainPanel();
    TagPanel* CreatePanelFromTag(wxWindow* parentTag,const std::shared_ptr<Tag>& tag);
    TagPanel* CreatePanelFromTagRec(wxWindow* parentTag, const std::shared_ptr<Tag>& tag);
    void CreatePanelFromTagRec(wxWindow* parentTag, const std::vector<std::shared_ptr<Tag>>& listTag);
    void openDetailsPanel(TagPanel* tag);
    //Tags
    std::vector<wxButton*>listChoices;
    bool placingTag;
    //Buttons
    wxBitmap getBitmap(const wxString& name, const wxSize& resize = wxSize());
    wxBitmap getBitmap(wxImage& name,const wxSize& resize=wxSize());

    wxStaticBitmap* getStaticBitmap(const wxString& name, wxWindow* window,wxSize resize=wxSize());
    wxStaticBitmap* getStaticBitmap(wxImage& image, wxWindow* window, wxSize resize);
    wxStaticBitmap* getStaticBitmap(wxBitmap& bitmap, wxWindow* window);
    wxRect getVisibleArea(wxScrolledWindow* window);
    wxImage getImage(const wxString& name,const wxSize& resize = wxSize());
    bool Intersects(const wxRect& rect1, const wxRect& rect2);
    void isInClientArea(TagPanel* tag, const wxRect& clientArea);
    wxSize getFrameSize();
    int getFrameWidth();
    int getFrameHeight();
    void ResizeImage(wxImage& image, const wxSize& size);

    //Program
    void LoadPanelFromTag(FlowTagPanel* ptag, std::shared_ptr<Tag> ltag);
    void LoadPanelFromTags(FlowTagPanel* ptag,std::vector<std::shared_ptr<Tag>> ltag);

    //Events
    void resetDetailsPanel();
    void OnProgramWindowScrolled(wxScrollWinEvent& evt);
    void OnCutClick(wxMouseEvent& evt);
    void switchFlowTagVisibility(wxMouseEvent& evt);
    void SwitchChoicePanel(wxMouseEvent& evt);
    void SwitchControlChoicePanel(wxMouseEvent& evt);
    void ChangeTagToAdd(std::shared_ptr<Tag> newTag);
    void PlacedTagToAdd();
    void PlacingTagToAdd();
    void testEvent(wxKeyEvent& evt);
    void OnTagPanelClick(wxMouseEvent& evt);
    void CalcCreateInsertInFlowPanel(wxPoint mousePos, FlowTagPanel* flowPanel);
    void CalcCreateInsertInPanel(wxPoint mousePos, TagPanel* panel);
    void OnMouseClicked(wxMouseEvent& evt);
    void OnTagPanelEnter(wxMouseEvent& evt);
    void OnTagPanelLeave(wxMouseEvent& evt);
    void OnMouseMoved(wxMouseEvent& evt);
    void OnDuplicateClick(wxMouseEvent& evt);
    void OnCloseDetails(wxMouseEvent& event);
};


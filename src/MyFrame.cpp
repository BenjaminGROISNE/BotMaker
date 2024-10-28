#include "MyFrame.h"

MyFrame::MyFrame(const wxString& title)
    : wxFrame(nullptr, wxID_ANY, title)
{
    wxInitAllImageHandlers();
    setMainPanel();
}

MyFrame::~MyFrame()
{
    delete myInterpretor;
}

void MyFrame::setMainPanel()
{
    myInterpretor = new Interpretor;
    srand(time(NULL));
    mainPanel = new wxPanel(this);
    placingTag = false;
    HoverSwitch = false;
    SetDoubleBuffered(true);
    setMenu();
    setTopPanel();
    setBottomPanel();
    mainSizer = new wxBoxSizer(wxVERTICAL);
    mainSizer->Add(topPanel, wxSizerFlags(1).Expand().Border(wxALL, 3)); // Set proportion of topPanel to 3
    mainSizer->Add(bottomPanel, wxSizerFlags(5).Expand().Border(wxALL, 5)); // Set proportion of bottomPanel to 12
    mainPanel->SetSizerAndFit(mainSizer);
}

void MyFrame::setMenu()
{
    wxMenuBar* menuBar = new wxMenuBar();

    wxMenu* mnu = new wxMenu();

    wxMenuItem* item1 = new wxMenuItem(mnu, wxID_EXIT);

    wxMenu* subMenu = new wxMenu();

    subMenu->Append(1, "ioezhf");
    subMenu->Append(2, "rghrge");
    subMenu->Append(3, "ioegegezhf");
    subMenu->Append(4, "ioeeherhzhf");
    item1->SetBitmap(wxArtProvider::GetBitmap("wxART_QUIT"));
    mnu->Append(item1);
    mnu->AppendSeparator();
    mnu->Append(wxID_NEW);
    mnu->AppendSeparator();
    mnu->AppendSubMenu(subMenu, "sub");
    menuBar->Append(mnu, "File");
    SetMenuBar(menuBar);
}
void MyFrame::BindChoiceButtons() {
    LogicButton->Bind(wxEVT_LEFT_DOWN, &MyFrame::SwitchChoicePanel, this);
    MathsButton->Bind(wxEVT_LEFT_DOWN, &MyFrame::SwitchChoicePanel, this);
    FindButton->Bind(wxEVT_LEFT_DOWN, &MyFrame::SwitchChoicePanel, this);
    VariableButton->Bind(wxEVT_LEFT_DOWN, &MyFrame::SwitchChoicePanel, this);
    ControlButton->Bind(wxEVT_LEFT_DOWN, &MyFrame::SwitchChoicePanel, this);
}
void MyFrame::setTopPanel()
{
    mainChoiceFont = wxFont(17, wxFONTFAMILY_ROMAN, wxFONTSTYLE_MAX, wxFONTWEIGHT_BOLD);
    topPanel = new wxPanel(mainPanel);
    topGrid = new wxGridSizer(0,0,wxSize(0,0));
    LogicButton = new wxButton(topPanel,wxID_ANY,"LOGIC");
    MathsButton = new wxButton(topPanel,wxID_ANY,"MATHS");
    FindButton = new wxButton(topPanel,wxID_ANY,"FIND");
    VariableButton = new wxButton(topPanel,wxID_ANY,"VARIABLES");
    ControlButton = new wxButton(topPanel,wxID_ANY,"CONTROL");
    LogicButton->SetFont(mainChoiceFont);
    MathsButton->SetFont(mainChoiceFont);
    FindButton->SetFont(mainChoiceFont);
    VariableButton->SetFont(mainChoiceFont);
    ControlButton->SetFont(mainChoiceFont);
    topGrid->Add(LogicButton,wxSizerFlags().Expand());
    topGrid->Add(MathsButton,wxSizerFlags().Expand());
    topGrid->Add(FindButton,wxSizerFlags().Expand());
    topGrid->Add(VariableButton,wxSizerFlags().Expand());
    topGrid->Add(ControlButton,wxSizerFlags().Expand());
    BindChoiceButtons();
    topPanel->SetMinSize(wxSize(0, 50));

    topPanel->SetSizer(topGrid);
}

void MyFrame::setBottomPanel()
{
    bottomPanel = new wxPanel(mainPanel);
    bottomSizer = new wxBoxSizer(wxHORIZONTAL);
    setProgramPanel();
    setTagDetailsPanel();
    bottomSizer->Add(programPanel, wxSizerFlags().Expand().Proportion(3));
    bottomSizer->Add(tagDetailsPanel, wxSizerFlags().Expand().Proportion(2));
    bottomPanel->SetSizer(bottomSizer);
}



void MyFrame::setProgramPanel()
{
    programSizer = new wxBoxSizer(wxVERTICAL);
    programPanel = new wxPanel(bottomPanel);
    setTagChoicePanel();
    setProgramWindow();
    programSizer->Add(tagChoicePanel, wxSizerFlags().Proportion(1).Expand());
    programSizer->Add(programWindow, wxSizerFlags().Proportion(5).Expand());
    programPanel->SetSizer(programSizer);

}



void MyFrame::setProgramWindow()
{
    programWindow = new wxScrolledWindow(programPanel);
    programGrid = new wxBoxSizer(wxVERTICAL);
    programWindow->SetSizer(programGrid);
    programWindow->SetScrollRate(0, 70);
    programWindow->AlwaysShowScrollbars();
    //programWindow->Bind(wxEVT_SCROLLWIN_LINEDOWN, &MyFrame::OnProgramWindowScrolled, this);
   // programWindow->Bind(wxEVT_SCROLLWIN_LINEUP, &MyFrame::OnProgramWindowScrolled, this);
    createPanelTags();
}

void MyFrame::OnProgramWindowScrolled(wxScrollWinEvent& evt) {
    if (mainTagPanel) {
       // wxRect clientRect=getVisibleArea(programWindow);
        //isInClientArea(mainTagPanel, wxRect(clientRect.x, clientRect.y-200, clientRect.width, clientRect.height+200));
      //  programWindow->Refresh();
      //  programWindow->Update();
       // programWindow->Layout();
       // mainTagPanel->updateLayout();
    }
    evt.Skip();
}

void MyFrame::OnDuplicateClick(wxMouseEvent& evt) {
    auto cloneBut = dynamic_cast<wxButton*>(evt.GetEventObject());
    if (cloneBut) {
        auto buttonParent = dynamic_cast<TagPanel*>(cloneBut->GetParent());
        if (buttonParent) {
            auto buttonGrandParent = dynamic_cast<FlowTagPanel*>(buttonParent->getParentTag());
            if (buttonGrandParent) {
                TagPanel* newPanel = buttonParent->clone();
                buttonGrandParent->insertChild(buttonParent, newPanel, UNDER);
                BindEventsRec(newPanel);
                buttonGrandParent->updateLayout();
            }
        }
    }
    evt.Skip();
}

void MyFrame::OnCutClick(wxMouseEvent& evt) {
    auto cutButton = dynamic_cast<wxButton*>(evt.GetEventObject());
    if (cutButton) {
        auto buttonParent = dynamic_cast<TagPanel*>(cutButton->GetParent());
        if (buttonParent) {
            auto buttonGrandParent = dynamic_cast<FlowTagPanel*>(buttonParent->GetParent());
            if (buttonGrandParent) {
                ChangeTagToAdd(buttonParent->getTag());
                buttonGrandParent->RemoveTagPanel(buttonParent);
                buttonGrandParent->updateLayout();
            }
        }
    }
    evt.Skip();
}

void MyFrame::BindEventsRec(TagPanel* panel) {
    auto flowPanel = dynamic_cast<FlowTagPanel*>(panel);
    if (flowPanel) {
        for (auto& child : flowPanel->getChildrenTags()) {
            BindEventsRec(child);
        }
    }
    BindTagPanelEvents(panel);
}

void MyFrame::isInClientArea(TagPanel* tag,const wxRect& clientArea ) {

    wxRect tagRect = tag->GetRect();
    if (Intersects(tagRect, clientArea)) {
        if(!tag->IsShown())tag->Show();
        FlowTagPanel* flow = dynamic_cast<FlowTagPanel*>(tag);
        if (flow) {
            for (auto child : flow->getChildrenTags()) {
                isInClientArea(child, clientArea);
            }
        }
    }
    else {
        if(tag->IsShown())tag->Hide();
        FlowTagPanel* flow = dynamic_cast<FlowTagPanel*>(tag);
        if (flow) {
            auto h = flow->getChildrenTags();
            for (auto child : h) {
                isInClientArea(child, clientArea);
            }
        }
    }
}

wxRect MyFrame::getVisibleArea(wxScrolledWindow* window) {

    wxPoint viewStart=window->GetViewStart();
    int scrollPixelsX, scrollPixelsY;
    window->GetScrollPixelsPerUnit(&scrollPixelsX, &scrollPixelsY);
    int xPixel = viewStart.x * scrollPixelsX;
    int yPixel = viewStart.y * scrollPixelsY;
    wxSize clientSize = window->GetClientSize();
    return wxRect(xPixel, yPixel, clientSize.GetWidth(), clientSize.GetHeight());
}

bool MyFrame::Intersects(const wxRect& rect1, const wxRect& rect2) {
    return rect1.Intersects(rect2);
}




void MyFrame::setTagDetailsPanel()
{

    tagDetailsPanel = new wxPanel(bottomPanel);
    tagDetailsSizer = new wxBoxSizer(wxVERTICAL);
    tagDetailsPanel->SetSizer(tagDetailsSizer);
    tagDetailsPanel->SetBackgroundColour(wxColour(0, 230, 230));
    closeDetailsPanel();
}

void MyFrame::initDetailsTitle() {
    auto titleGrid = new wxBoxSizer(wxHORIZONTAL);
    auto detailsText = new wxStaticText(tagDetailsPanel, wxID_ANY, "Tag Details");
    detailsText->SetFont(wxFont(13, wxFONTFAMILY_DEFAULT, wxFONTSTYLE_NORMAL,wxFONTWEIGHT_BOLD));
    leaveDetailsButton = new wxButton(tagDetailsPanel, wxID_ANY, "Close");
    leaveDetailsButton->SetFont(wxFont(12, wxFONTFAMILY_DECORATIVE, wxFONTSTYLE_MAX, wxFONTWEIGHT_BOLD));
    leaveDetailsButton->Bind(wxEVT_LEFT_DOWN, &MyFrame::OnCloseDetails, this);
    titleGrid->Add(detailsText,wxSizerFlags());
    titleGrid->AddStretchSpacer();
    titleGrid->Add(leaveDetailsButton, wxSizerFlags());
    tagDetailsSizer->Add(titleGrid, wxSizerFlags().Expand().Border());
}

void MyFrame::resetDetailsPanel() {
    tagDetailsPanel->DestroyChildren();
    tagDetailsSizer->Clear();
}

void MyFrame::openDetailsPanel(TagPanel* tag)
{
    resetDetailsPanel();
    initDetailsTitle();
    if (tag)tag->createDetailsPanel(tag,tagDetailsPanel, tagDetailsSizer);
    tagDetailsPanel->Show();
    bottomPanel->Layout();
} 
void MyFrame::closeDetailsPanel() {
    resetDetailsPanel();
    tagDetailsPanel->Hide();
    bottomPanel->Layout();
}




std::shared_ptr<MainTag> MyFrame::getMainTag()
{
    std::string currentPath = "C:\\Users\\bgroi\\OneDrive - Université De Technologie De Belfort - Montbeliard\\Documents\\Coding\\C++\\Bots\\BotOVMT\\";
    myInterpretor->ActivityFolder = currentPath +"Activities\\";
    myInterpretor->readActivityFile("short.act");
    mainTag = std::dynamic_pointer_cast<MainTag>(myInterpretor->getActivityTag());
    return mainTag;
}

bool IsPanelVisible(wxScrolledWindow* scrolledWindow, wxPanel* panel) {

    wxRect visibleRect = scrolledWindow->GetClientRect();
    scrolledWindow->CalcScrolledPosition(visibleRect.x, visibleRect.y, &visibleRect.x, &visibleRect.y);
    wxPoint panelPosition = panel->GetPosition();
    wxRect panelRect(panelPosition, panel->GetSize());
    return visibleRect.Intersects(panelRect);
}

FlowTagPanel* MyFrame::createPanelTags()
{
    getMainTag();
    mainTagPanel=dynamic_cast<MainTagPanel*>(CreatePanelFromTagRec(programWindow,mainTag));
    programGrid->Add(mainTagPanel, wxSizerFlags().Expand());
    return mainTagPanel;
}

void MyFrame::SwitchChoicePanel(wxMouseEvent& evt)
{
    wxColour basicColour(255, 255, 255);
    wxColour pressedColour(100, 170, 100);
    if (!previousMainChoice) {
        previousMainChoice = dynamic_cast<wxButton*>(evt.GetEventObject());
        if (!previousMainChoice)return;
    }
    else {
        previousMainChoice->SetBackgroundColour(basicColour);
         wxButton* newClickButton =dynamic_cast<wxButton*>(evt.GetEventObject());
         if (newClickButton) {
             if (newClickButton == previousMainChoice) {
                 tagChoicePanel->Hide();
                 previousMainChoice = nullptr;
                 programPanel->Layout();
                 return;
             }
             else previousMainChoice = newClickButton;
         }
    }
    resetTagChoicePanel();
    previousMainChoice->SetBackgroundColour(basicColour);
    if (previousMainChoice == LogicButton) {
        LogicButton->SetBackgroundColour(pressedColour);
        createLogicTagChoice();
    }
    if (previousMainChoice == MathsButton) {
        MathsButton->SetBackgroundColour(pressedColour);
        createMathsTagChoice();
    }
    if (previousMainChoice == FindButton) {
        FindButton->SetBackgroundColour(pressedColour);
        createFindTagChoice();
    }
    if (previousMainChoice == VariableButton) {
        VariableButton->SetBackgroundColour(pressedColour);
        createLogicTagChoice();
    }
    if (previousMainChoice == ControlButton) {
        ControlButton->SetBackgroundColour(pressedColour);
        createControlTagChoice();
    }
    tagChoicePanel->Show();
    programPanel->Layout();
}

void MyFrame::SwitchControlChoicePanel(wxMouseEvent& evt) {
    wxColour basicColour(255, 255, 255);
    wxColour pressedColour(200, 170, 100);
    wxButton* newClickButton = dynamic_cast<wxButton*>(evt.GetEventObject());
    if (newClickButton) {
        if (previousSubChoice == doLoopButton) {
            ChangeTagToAdd(std::make_shared<DoLoopTag>());
        }
        if (previousSubChoice == elseIfButton) {
            ChangeTagToAdd(std::make_shared<ElifTag>());
        }
        if (previousSubChoice == elseButton) {
            ChangeTagToAdd(std::make_shared<ElseTag>());
        }
        if (previousSubChoice == ifButton) {
            ChangeTagToAdd(std::make_shared<IfTag>());
        }
        if (previousSubChoice == loopButton) {
            ChangeTagToAdd(std::make_shared<LoopTag>());
        }
    }
}

void MyFrame::ChangeTagToAdd(std::shared_ptr<Tag> newTag) {
    tagToAdd.swap(newTag);
    PlacingTagToAdd();
}

void MyFrame::PlacedTagToAdd() {
    tagToAdd = nullptr;
    placingTag = false;
}
void MyFrame::PlacingTagToAdd() {
    placingTag = true;
}

void MyFrame::resetTagChoicePanel() {
    tagChoiceSizer->Clear();
    tagChoicePanel->DestroyChildren();
}
void MyFrame::setTagChoicePanel()
{
    subChoiceFont = wxFont(15, wxFONTFAMILY_ROMAN, wxFONTSTYLE_MAX, wxFONTWEIGHT_BOLD);
    tagChoicePanel = new wxPanel(programPanel);
    tagChoicePanel->SetBackgroundColour(wxColour(0, 0, 200));
    tagChoiceSizer = new wxBoxSizer(wxHORIZONTAL);
    tagChoicePanel->SetSizer(tagChoiceSizer);
    tagChoicePanel->Hide();
    tagChoicePanel->Layout();
}
void MyFrame::createLogicTagChoice()
{
    boolButton = new wxButton(tagChoicePanel, wxID_ANY, "BOOL");
    orButton = new wxButton(tagChoicePanel, wxID_ANY, "OR");
    andButton = new wxButton(tagChoicePanel, wxID_ANY, "AND");
    notButton = new wxButton(tagChoicePanel, wxID_ANY, "NOT");
    compareButton = new wxButton(tagChoicePanel, wxID_ANY, "COMPARE");
    wxSizerFlags flags = wxSizerFlags().Expand().Proportion(1);
    boolButton->SetFont(subChoiceFont);
    orButton->SetFont(subChoiceFont);
    andButton->SetFont(subChoiceFont);
    notButton->SetFont(subChoiceFont);
    compareButton->SetFont(subChoiceFont);
    tagChoiceSizer->Add(boolButton, flags);
    tagChoiceSizer->Add(orButton, flags);
    tagChoiceSizer->Add(andButton, flags);
    tagChoiceSizer->Add(notButton, flags);
    tagChoiceSizer->Add(compareButton, flags);
}

void MyFrame::createMathsTagChoice()
{
    addButton = new wxButton(tagChoicePanel, wxID_ANY, "ADD");
    substractButton = new wxButton(tagChoicePanel, wxID_ANY, "MINUS");
    multiplyButton = new wxButton(tagChoicePanel, wxID_ANY, "MULTIPLY");
    divideButton = new wxButton(tagChoicePanel, wxID_ANY, "DIVIDE");
    wxSizerFlags flags = wxSizerFlags().Expand().Proportion(1);
    addButton->SetFont(subChoiceFont);
    substractButton->SetFont(subChoiceFont);
    multiplyButton->SetFont(subChoiceFont);
    divideButton->SetFont(subChoiceFont);
    tagChoiceSizer->Add(addButton, flags);
    tagChoiceSizer->Add(substractButton, flags);
    tagChoiceSizer->Add(multiplyButton, flags);
    tagChoiceSizer->Add(divideButton, flags);
}

void MyFrame::createFindTagChoice()
{
    findButton = new wxButton(tagChoicePanel, wxID_ANY, "FIND");
    findclickButton = new wxButton(tagChoicePanel, wxID_ANY, "FINDCLICK");
    findOneMultipleButton = new wxButton(tagChoicePanel, wxID_ANY, "FINDONE");
    findSwipeButton = new wxButton(tagChoicePanel, wxID_ANY, "FINDSWIPE");
    wxSizerFlags flags = wxSizerFlags().Expand().Proportion(1);
    findButton->SetFont(subChoiceFont);
    findclickButton->SetFont(subChoiceFont);
    findOneMultipleButton->SetFont(subChoiceFont);
    findSwipeButton->SetFont(subChoiceFont);
    tagChoiceSizer->Add(findButton, flags);
    tagChoiceSizer->Add(findclickButton, flags);
    tagChoiceSizer->Add(findOneMultipleButton, flags);
    tagChoiceSizer->Add(findSwipeButton, flags);
}
void MyFrame::createControlTagChoice()
{
    loopButton = new wxButton(tagChoicePanel, wxID_ANY, "LOOP");
    ifButton = new wxButton(tagChoicePanel, wxID_ANY, "IF");
    doLoopButton = new wxButton(tagChoicePanel, wxID_ANY, "DOLOOP");
    elseButton = new wxButton(tagChoicePanel, wxID_ANY, "ELSE");
    elseIfButton = new wxButton(tagChoicePanel, wxID_ANY, "ELSEIF");
    wxSizerFlags flags = wxSizerFlags().Expand().Proportion(1);

    BindControlButtons();

    loopButton->SetFont(subChoiceFont);
    ifButton->SetFont(subChoiceFont);
    doLoopButton->SetFont(subChoiceFont);
    elseButton->SetFont(subChoiceFont);
    elseIfButton->SetFont(subChoiceFont);
    tagChoiceSizer->Add(loopButton, flags);
    tagChoiceSizer->Add(ifButton, flags);
    tagChoiceSizer->Add(doLoopButton, flags);
    tagChoiceSizer->Add(elseButton, flags);
    tagChoiceSizer->Add(elseIfButton, flags);
}

void MyFrame::BindControlButtons() {
    loopButton->Bind(wxEVT_LEFT_DOWN, &MyFrame::SwitchControlChoicePanel, this);
    ifButton->Bind(wxEVT_LEFT_DOWN, &MyFrame::SwitchControlChoicePanel, this);
    doLoopButton->Bind(wxEVT_LEFT_DOWN, &MyFrame::SwitchControlChoicePanel, this);
    elseButton->Bind(wxEVT_LEFT_DOWN, &MyFrame::SwitchControlChoicePanel, this);
    elseIfButton->Bind(wxEVT_LEFT_DOWN, &MyFrame::SwitchControlChoicePanel, this);
}





void MyFrame::OnTagPanelClick(wxMouseEvent& evt) {
    TagPanel* panel = dynamic_cast<TagPanel*>(evt.GetEventObject());
    FlowTagPanel* flowPanel = dynamic_cast<FlowTagPanel*>(evt.GetEventObject());
    wxPoint mousePos = evt.GetPosition();
    if (flowPanel) {
        if (placingTag) {
            CalcCreateInsertInFlowPanel(mousePos,flowPanel);
            return;
        }
    }
    else if (panel) {
        if (placingTag) {
            CalcCreateInsertInPanel(mousePos,panel);
            return;
        }
        openDetailsPanel(panel);
    }
}

void MyFrame::CalcCreateInsertInFlowPanel(wxPoint mouseClick,FlowTagPanel* flowPanel) {
    wxSize tagLineSize = flowPanel->getLineSizer()->GetSize();
    wxPoint tagPanelPos = flowPanel->GetPosition();
    wxSize panelSize = flowPanel->GetSize();
    int lineHeight = tagLineSize.y;
    int panelHeight = panelSize.y;
    int mouseHeight = mouseClick.y;
    int half = lineHeight / 2;

    if (mouseHeight < half) {
        CreateInsertPanel(tagToAdd, flowPanel, ABOVE);
    }
    else if (mouseHeight > half && mouseHeight < lineHeight) {
        std::vector<TagPanel*> listChildren = flowPanel->getChildrenTags();
        if (!listChildren.empty()) {
            CreateInsertPanel(tagToAdd, listChildren.front(), ABOVE);
        }
    }
    else if (mouseHeight > panelHeight - lineHeight && mouseHeight < panelHeight - half) {
        std::vector<TagPanel*> listChildren = flowPanel->getChildrenTags();
        if (!listChildren.empty()) {
            CreateInsertPanel(tagToAdd, listChildren.back(), UNDER);
        }
    }
    else if (mouseHeight > panelHeight - half && mouseHeight < panelHeight) {
        CreateInsertPanel(tagToAdd, flowPanel, UNDER);
    }
    resetInsertHover();
}
void MyFrame::CalcCreateInsertInPanel(wxPoint mouseClick,TagPanel* panel) {
    int mouseHeight = mouseClick.y;
    wxPoint tagPanelPos = panel->GetPosition();
    wxSize panelSize = panel->GetSize();
    int panelHeight = panelSize.y;
    if (mouseHeight < panelHeight / 2) {
        CreateInsertPanel(tagToAdd, panel, ABOVE);
    }
    else if (mouseHeight > panelHeight / 2 && mouseHeight < panelHeight) {
        CreateInsertPanel(tagToAdd, panel, UNDER);
    }
    resetInsertHover();
}

void MyFrame::resetInsertHover() {
    if (hoverNeighbour) {
        hoverNeighbour->isInserting = false;
        hoverNeighbour->Refresh();
    }
    if (pointedPanel) {
        pointedPanel->isInserting = false;
        pointedPanel->Refresh();
    }
}

void MyFrame::CreateInsertPanel(std::shared_ptr<Tag>tagAdd, TagPanel* targetPanel, AddPosition pos) {

   if (targetPanel && tagAdd) {
        FlowTagPanel* parent= getFlowTagPanel(targetPanel->getParentTag());
        if (parent) {
            TagPanel* newPanel;
            switch (pos) {
            case MIDBOTHOVER:
                newPanel=CreatePanelFromTagRec(targetPanel, tagAdd);
                break;
            case MIDTOPHOVER:
                newPanel= CreatePanelFromTagRec(targetPanel, tagAdd);
                break;
            default:
                newPanel= CreatePanelFromTagRec(targetPanel->GetParent(), tagAdd);
                break;
            }
            if (newPanel) {
                parent->insertChild(targetPanel, newPanel, pos);
                mainTagPanel->updateLayout();
                PlacedTagToAdd();
            }
        }
   }

}


MainTagPanel* MyFrame::CreateMainPanel() {

    mainTagPanel=dynamic_cast<MainTagPanel*>(CreatePanelFromTag(programWindow, mainTag));
    return mainTagPanel;
}


TagPanel* MyFrame::CreatePanelFromTag(wxWindow* parentTag,const std::shared_ptr<Tag>& tag) {
    if (parentTag&&tag) {
        FlowTagPanel* parent = dynamic_cast<FlowTagPanel*>(parentTag);
        if (parent && parent->getNbParents() > MAXFLOW)return nullptr;
        TagPanel* newPanel = nullptr;
        FlowTagPanel* newFlowPanel = nullptr;
        switch (tag->myType) {
        case LOOPTAG:
            newFlowPanel = new LoopTagPanel(parentTag);
            break;
        case IFTAG:
            newFlowPanel = new IfTagPanel(parentTag);
            break;
        case MAINTAG:
            newFlowPanel = new MainTagPanel(parentTag);
            break;
        case LOADTAG:
            newPanel = new TagPanel(parentTag);
            break;
        case STORETAG:
            newPanel = new TagPanel(parentTag);
            break;
        case BOOLTAG:
            newPanel = new BoolTagPanel(parentTag);
            break;
        default:
            newPanel = new TagPanel(parentTag);
            break;
        }
        if (newFlowPanel) {
            BindFlowTagPanelEvents(newFlowPanel);
            newPanel = newFlowPanel;
        }
        else if (newPanel) {
            BindTagPanelEvents(newPanel);
        }
        newPanel->setTag(tag);
        return newPanel;
    }
    return nullptr;
}

TagPanel* MyFrame::CreatePanelFromTagRec(wxWindow* parentTag, const std::shared_ptr<Tag>& tag) {
    
    auto flowParent = dynamic_cast<FlowTagPanel*>(parentTag);
    auto newPanel = CreatePanelFromTag(parentTag, tag);
    if (newPanel) {
        auto newFlowPanel = getFlowTagPanel(newPanel);
        if (newFlowPanel)CreatePanelFromTagRec(newFlowPanel, getFlowTag(newFlowPanel->getTag())->nestedTags);
        if (flowParent && newPanel)flowParent->addChild(newPanel);
    }
    return newPanel;
}

void MyFrame::CreatePanelFromTagRec(wxWindow* parentTag, const std::vector<std::shared_ptr<Tag>>& listTag)
{
    for (auto& tag : listTag) {
        CreatePanelFromTagRec(parentTag, tag);
    }
}


void MyFrame::LoadPanelFromTag(FlowTagPanel* parentTag, std::shared_ptr<Tag> tag)
{
    if (parentTag->getNbParents() < MAXFLOW) {
        TagPanel* newPanel = CreatePanelFromTag(parentTag,tag);
        FlowTagPanel* newFlowPanel = dynamic_cast<FlowTagPanel*>(newPanel);
        if(newFlowPanel) LoadPanelFromTags(newFlowPanel, getFlowTag(tag)->nestedTags);
        if (newPanel);
    }
}

void MyFrame::OnTagPanelLeave(wxMouseEvent& evt) {
    TagPanel* panel = dynamic_cast<TagPanel*>(evt.GetEventObject());
    if (panel) {
        HoverSwitch = true;
        panel->isHovering = false;
        if (placingTag) {
            panel->isInserting = false;
            panel->previousHover = NOHOVER;
        }
        else panel->Refresh();

    }
    evt.Skip();
}

void MyFrame::OnTagPanelEnter(wxMouseEvent& evt) {
    TagPanel* panel = dynamic_cast<TagPanel*>(evt.GetEventObject());
    if (panel) {
        HoverSwitch = true;
        panel->isHovering = true;
        if (placingTag) {
            pointedPanel = panel;
            pointedPanel->isInserting = true;
        }
        else panel->Refresh();
    }
}

void MyFrame::OnMouseMoved(wxMouseEvent& evt){
    if (placingTag) {
        wxObject* obj = evt.GetEventObject();
        TagPanel* panel = dynamic_cast<TagPanel*>(obj);
        FlowTagPanel* flowPanel = dynamic_cast<FlowTagPanel*>(obj);
        wxPoint mousePos = evt.GetPosition();
        int mouseHeight = mousePos.y;
        
        if (panel) {
            wxPoint previousPoint = drawPoint;
            HoverPosition h=NOHOVER;
            HoverPosition previousHover= panel->previousHover;
            TagPanel* parent = panel->getParentTag();
            std::vector<TagPanel*> tempHover;
            if (flowPanel) {
                wxSize panelSize = flowPanel->GetSize();
                int panelHeight = panelSize.GetHeight();
                wxSize tagLineSize = flowPanel->getLineSizer()->GetSize();
                wxSize bottomSize;
                if (!flowPanel->isMinimized()) {
                    bottomSize = flowPanel->getBottomSpacer()->GetSize();
                }
                int bottomHeight = bottomSize.GetHeight();
                int lineHeight = tagLineSize.y;
                int half = lineHeight / 2;
                if (mouseHeight < half) {
                    h = TOPHOVER;
                }
                else if (mouseHeight > half && mouseHeight < lineHeight) {
                    h = MIDTOPHOVER;
                }
                else if (mouseHeight > panelHeight - bottomHeight-1 && mouseHeight < panelHeight - (bottomHeight/2)) {
                    h = MIDBOTHOVER;
                }
                else if (mouseHeight > panelHeight - (bottomHeight/2) && mouseHeight < panelHeight) {
                    h = BOTTOMHOVER;
                }
                panel = flowPanel;
            }
            else if (panel) {
                wxSize panelSize = panel->GetSize();
                int panelHeight = panelSize.GetHeight();
                if (mouseHeight < (panelHeight / 2)+1) {
                    h = TOPHOVER;
                }
                else if (mouseHeight > panelHeight / 2 && mouseHeight < panelHeight) {
                    h = BOTTOMHOVER;
                }
            }
            if (previousHover != h)HoverSwitch = true;
            if (HoverSwitch) {
                if (hoverNeighbour) {
                    hoverNeighbour->isInserting = false;
                    hoverNeighbour->Refresh();
                }
                panel->isInserting = true;
                if(hoverNeighbour!=panel)panel->Refresh();

                setNeighboursPanel(panel, h);
                if(hoverNeighbour)hoverNeighbour->Refresh();
                HoverSwitch = false;
            }
        }
    }
}

void MyFrame::setNeighboursPanel(TagPanel*panel,HoverPosition currentHover) {
    panel->setInsertPoint(currentHover);
    auto parent = panel->getParentTag();
    FlowTagPanel* flowTag = dynamic_cast<FlowTagPanel*>(panel);
    switch (currentHover) {
    case TOPHOVER:
        hoverNeighbour = panel->getPreviousTag();
        if (hoverNeighbour) {
            hoverNeighbour->isInserting = true;
            hoverNeighbour->setInsertPoint(BOTTOMHOVER);
        }
        else if (parent) {
            parent->isInserting = true;
            parent->setInsertPoint(MIDTOPHOVER);
            hoverNeighbour = parent;
        }
        break;
    case MIDTOPHOVER:
        if (flowTag) {
            hoverNeighbour = flowTag->getFirstTag();
            if (hoverNeighbour) {
                hoverNeighbour->isInserting = true;
                hoverNeighbour->setInsertPoint(TOPHOVER);
            }
        }
        break;
    case MIDBOTHOVER:
        if (flowTag) {
            hoverNeighbour = flowTag->getLastTag();
            if (hoverNeighbour) {
                hoverNeighbour->isInserting = true;
                hoverNeighbour->setInsertPoint(BOTTOMHOVER);
            }
        }
        break;
    case BOTTOMHOVER:
        hoverNeighbour = panel->getNextTag();
        if (hoverNeighbour) {
            hoverNeighbour->isInserting = true;
            hoverNeighbour->setInsertPoint(TOPHOVER);
        }
        else if (parent) {
            parent->isInserting = true;
            parent->setInsertPoint(MIDBOTHOVER);
            hoverNeighbour = parent;
        }
        break;
    }
}


void MyFrame::OnMouseClicked(wxMouseEvent& evt)
{
    wxPoint mousePosition = evt.GetPosition();
    if (placingTag) {
    }
}

void MyFrame::BindFlowTagPanelEvents(TagPanel* newPanel) {
    FlowTagPanel* flowPanel = dynamic_cast<FlowTagPanel*>(newPanel);
    if (flowPanel) {
        BindEmptyPanelEvents(flowPanel->getEmptyTagPanel());
    }
    BindTagPanelEvents(newPanel);
}

void MyFrame::BindTagPanelEvents(TagPanel* newPanel) {
    if (newPanel) {
        newPanel->Bind(wxEVT_LEFT_DOWN, &MyFrame::OnTagPanelClick, this);
        newPanel->Bind(wxEVT_MOTION, &MyFrame::OnMouseMoved, this);
        newPanel->Bind(wxEVT_ENTER_WINDOW, &MyFrame::OnTagPanelEnter, this);
        newPanel->Bind(wxEVT_LEAVE_WINDOW, &MyFrame::OnTagPanelLeave, this);
        newPanel->Bind(wxEVT_LEAVE_WINDOW, &MyFrame::OnTagPanelLeave, this);
        auto cloneButton = newPanel->getCloneButton();
        if (cloneButton) {
            cloneButton->Bind(wxEVT_LEFT_DOWN, &MyFrame::OnDuplicateClick, this);
        }  
        auto cutButton = newPanel->getCutButton();
        if (cutButton) {
            cutButton->Bind(wxEVT_LEFT_DOWN, &MyFrame::OnCutClick, this);
        }
    }
}

void MyFrame::BindEmptyPanelEvents(TagPanel* emptyPanel) {
    if (emptyPanel) {
        emptyPanel->Bind(wxEVT_LEFT_DOWN, &MyFrame::OnTagPanelClick, this);
        emptyPanel->Bind(wxEVT_MOTION, &MyFrame::OnMouseMoved, this);
        emptyPanel->Bind(wxEVT_ENTER_WINDOW, &MyFrame::OnTagPanelEnter, this);
        emptyPanel->Bind(wxEVT_LEAVE_WINDOW, &MyFrame::OnTagPanelLeave, this);
        emptyPanel->Bind(wxEVT_LEAVE_WINDOW, &MyFrame::OnTagPanelLeave, this);
    }
}

void MyFrame::LoadPanelFromTags(FlowTagPanel* ptag, std::vector<std::shared_ptr<Tag>> ltag)
{
    for (auto& tag : ltag) {
        LoadPanelFromTag(ptag, tag);
    }
}


void MyFrame::OnCloseDetails(wxMouseEvent& evt)
{
    closeDetailsPanel();
}

void MyFrame::testEvent(wxKeyEvent& evt)
{
    evt.Skip();
}

wxBitmap MyFrame::getBitmap(wxImage& image, const wxSize& resize)
{
    if (image.IsOk()) {
        if (resize != wxSize())ResizeImage(image, resize);
        return wxBitmap(image);
    }
}

wxBitmap MyFrame::getBitmap(const wxString& name,const wxSize& resize)
{
    return wxBitmap(getImage(name, resize));
}



wxStaticBitmap* MyFrame::getStaticBitmap(const wxString& name, wxWindow* window, wxSize resize)
{
    return new wxStaticBitmap(window, wxID_ANY, getBitmap(name, resize));
}

wxStaticBitmap* MyFrame::getStaticBitmap(wxImage& image, wxWindow* window, wxSize resize)
{
    return new wxStaticBitmap(window, wxID_ANY, getBitmap(image, resize));
}


wxStaticBitmap* MyFrame::getStaticBitmap(wxBitmap& bitmap,wxWindow* window)
{
    return new wxStaticBitmap(window, wxID_ANY, bitmap);
}

wxImage MyFrame::getImage(const wxString& name,const wxSize& resize)
{
     wxImage image= wxImage(name);
     if (resize != wxSize())ResizeImage(image, resize);
     return image;
}

wxSize MyFrame::getFrameSize()
{
    return this->GetSize();
}

int MyFrame::getFrameWidth()
{
    return getFrameSize().GetWidth();
}

int MyFrame::getFrameHeight()
{
    return getFrameSize().GetHeight();
}


void MyFrame::ResizeImage(wxImage& image,const wxSize& size) {
    wxSize imageSize = image.GetSize();
    int boxWidth = size.GetWidth();
    int boxHeight = size.GetHeight();
    int imageWidth = imageSize.GetWidth();
    int imageHeight = imageSize.GetHeight();
    float widthRatio = float(boxWidth) / float(imageWidth);
    float heightRatio = float(boxHeight) / float(imageHeight);
    float ratio = std::min(widthRatio,heightRatio);
    int newWidth =imageWidth*ratio;
    int newHeight = imageHeight*ratio;
    image.Rescale(newWidth, newHeight);
}




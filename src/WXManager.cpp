#include "WXManager.h"

IMPLEMENT_APP(WXManager);

bool WXManager::OnInit()
{
    MyFrame* frame = new MyFrame("Main app");
   frame->SetClientSize(1000, 800);
   frame->SetPosition(wxPoint(0, 0));
    frame->Show();
    return true;
}




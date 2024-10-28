#include "WXManager.h"



bool WXManager::OnInit()
{
    MyFrame* frame = new MyFrame("Main app");
	AllocConsole();  
	FILE* fp;
	freopen_s(&fp, "CONOUT$", "w", stdout);
	freopen_s(&fp, "CONOUT$", "w", stderr);
    frame->SetClientSize(1000, 800);
    frame->SetPosition(wxPoint(0, 0));
    frame->Show();
	fclose(fp);
    return true;

}


IMPLEMENT_APP(WXManager);

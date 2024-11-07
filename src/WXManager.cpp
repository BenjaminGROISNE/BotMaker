#include "WXManager.h"

void runTests() {
    std::string text1 = "[[[[abc]";
    std::string result1 = getStringInsideBorders(text1, "[", "]");


    std::string text2 = "][";
    std::string result2 = getStringInsideBorders(text2, "[", "]");


    std::string text3 = "[[[[abc]]]]";
    std::string result3 = getStringInsideBorders(text3, "[", "]");


    // Additional cases
    std::string text4 = "abc";
    std::string result4 = getStringInsideBorders(text4, "[", "]");

    std::string text5 = "[abc]";
    std::string result5 = getStringInsideBorders(text5, "[", "]");

    std::string text6 = "[abc][def]";
    std::string result6 = getStringInsideBorders(text6, "[", "]");

    std::string text7 = "[[a[b[c]d]e]f]";
    std::string result7 = getStringInsideBorders(text7, "[", "]");
    std::cout << "ended";
}

bool WXManager::OnInit()
{

    AllocConsole();

    FILE* fp;
    freopen_s(&fp, "CONOUT$", "w", stdout);
    freopen_s(&fp, "CONOUT$", "w", stderr);

    MyFrame* frame = new MyFrame("Main app");

    frame->SetClientSize(1000, 800);
    frame->SetPosition(wxPoint(0, 0));
    frame->Show();
	fclose(fp);
    return true;

}


IMPLEMENT_APP(WXManager);

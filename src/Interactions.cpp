#include "Interactions.h"



void Interactions::fillPopup()
{
    std::vector<std::string> discpopup = { };
    std::vector<std::string> blockpopup = { };
    for (std::string discstr : discpopup) {
        Popup P(discstr, state::disconnect);
        listPopup.push_back(P);
    }
    for (std::string blockstr : blockpopup) {
        Popup P(blockstr, state::block);
        listPopup.push_back(P);
    }
}



Interactions::Interactions()
{
    background = Template(background.id, Gray);
    dimX = 1080;
    dimY = 1920;
    fillPopup();
}

bool Interactions::fcA(const std::string& templ, Zone Z, unsigned int delayMilliseconds) {
    return fA(templ, Z, delayMilliseconds, true);
}

bool Interactions::fA(const std::string& templ, Zone Z, unsigned int delayMilliseconds, bool willInteract) {
    bool found = false;
    resetCount();
    Template Mat = getTemplate(templ,Gray);
    Template Tresult;
    loopPopUp(Mat.type);
    if (cvmtfTemplate(Mat, background, Tresult, Z))found = true;
    return templateFound(found, Tresult, willInteract, delayMilliseconds);
}

bool Interactions::fcpopupA()
{
    Template bmat(backg, Gray);
    for (Popup pop : listPopup) {
        Template Tresult;
        if (cvmtfTemplate(pop, bmat, Tresult)) {
            state cond = pop.action;
            switch (cond) {
            case state::block:
                click(Tresult.C);
                wait(DEFAULTDELAY);
                return true;
                break;

            case state::disconnect:
                throw RebootException();
                break;
            }
        }

    }
    return false;
}

Template Interactions::loopPopUp(typeMat color)
{

    do {
        screenshot(adbId);
        background.mat = CreateMat(backg, Color);
        background.graymat = CreateMat(backg, Gray);
        background.type = color;
    } while (fcpopupA());
    return background;
}


bool Interactions::fcOneTemplateMultipleTemplateA(const std::string& templ, Direction direction, int order, Zone Z, unsigned int delayMilliseconds) {
    return fOneTemplateMultipleTemplateA(templ, direction, order, Z, delayMilliseconds, true);
}

bool Interactions::fOneTemplateMultipleTemplateA(const std::string& templ, Direction direction, int order, Zone Z, unsigned int delayMilliseconds, bool willInteract) {

    bool found = false;
    resetCount();
    std::vector<Template>Tresults;

    Template Tresult = getTemplate(templ,Gray);
    loopPopUp(Tresult.type);
    if (cvmtfMultipleTemplate(Tresult, background, Tresults, Z)) {
        found = true;
        Tresults = orderTemplates(Tresults, direction);
        if (order < 0)order = 0;
        if (order >= Tresults.size())order = Tresults.size() - 1;
        Tresult = Tresults.at(order);
    }
    return templateFound(found, Tresult, willInteract, delayMilliseconds);
}


bool Interactions::fCompareOneTemplateMultipleTemplateA(const std::vector<std::string>& alltempl, const std::vector<std::string>& simTempl, Direction direction, int order, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    bool found = false;
    resetCount();

    std::vector<Template>Tresults;
    std::vector<Template>allMats = getTemplate(alltempl, Color);
    std::vector<Template>simMats = getTemplate(simTempl, Color);
    Template Tresult;
    loopPopUp(Color);
    if (cvmtfMultipleTemplateCompareMultipleTemplate(allMats, background, simMats, Tresults, Z)) {
        found = true;
        Tresults = orderTemplates(Tresults, direction);
        if (order < 0)order = 0;
        if (order >= Tresults.size())order = Tresults.size() - 1;
        Tresult = Tresults.at(order);
    }
    return templateFound(found, Tresult, willInteract, delayMilliseconds);
}

bool Interactions::fcCompareOneTemplateMultipleTemplateA(const std::vector<std::string>& alltempl, const std::vector<std::string>& simTempl, Direction direction, int order, Zone Z, unsigned int delayMilliseconds)
{
    return fCompareOneTemplateMultipleTemplateA(alltempl, simTempl, direction, order, Z, delayMilliseconds, true);
}


bool Interactions::fOneTemplateMultipleTemplateA(const std::vector<std::string>& alltempl, const Direction& direction, int order, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    bool found = false;
    resetCount();

    std::vector<Template>Tresults;
    std::vector<Template>allMats = getTemplate(alltempl,Gray);
    Template Tresult;
    loopPopUp();
    if (cvmtfMultipleTemplate(allMats, background, Tresults, Z)) {
        found = true;
        Tresults = orderTemplates(Tresults, direction);
        if (order < 0)order = 0;
        if (order >= Tresults.size())order = Tresults.size() - 1;
        Tresult = Tresults.at(order);
    }
    return templateFound(found, Tresult, willInteract, delayMilliseconds);
}

bool Interactions::fcOneTemplateMultipleTemplateA(const std::vector<std::string>& alltempl, const Direction& direction, int order, Zone Z, unsigned int delayMilliseconds)
{
    return fOneTemplateMultipleTemplateA(alltempl, direction, order, Z, delayMilliseconds, true);
}

bool Interactions::fnbTemplateA(const std::string& templ, int& nb, Zone Z, unsigned int delayMillisecond, bool willInteract)
{
    bool found = false;
    resetCount();
    std::vector<Template>Tresult;
    nb = 0;
    Template Templ = getTemplate(templ,Gray);
    loopPopUp();
    if (cvmtfMultipleTemplate(Templ, background, Tresult, Z)) {
        found = true;
        nb = Tresult.size();
    }
    return found;
}

int Interactions::fnbTemplateA(const std::string& templ, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    resetCount();
    bool found = false;
    std::vector<Template>Tresult;
    Template Templ = getTemplate(templ,Gray);
    loopPopUp();
    if (cvmtfMultipleTemplate(Templ, background, Tresult, Z)) {
        found = true;
    }
    templateFound(found, Template(), willInteract, delayMilliseconds);
    return Tresult.size();
}

int Interactions::fnbTemplateA(const std::vector<std::string>& allTempl, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    resetCount();
    bool found = false;
    std::vector<Template>Tresult;
    std::vector<Template> Templ = getTemplate(allTempl,Gray);
    loopPopUp();
    if (cvmtfMultipleTemplate(Templ, background, Tresult, Z)) {
        found = true;
    }
    templateFound(found, Template(), willInteract, delayMilliseconds);
    return Tresult.size();
}

int Interactions::fnbMultipleCompareTemplateA(const std::vector<std::string>& allTempl, const std::vector<std::string>& simTempls, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    resetCount();
    bool found = false;
    std::vector<Template>Tresult;
    std::vector<Template> Templ = getTemplate(allTempl, Color);
    std::vector<Template> simTemplates = getTemplate(simTempls, Color);
    loopPopUp(Color);
    if (cvmtfMultipleTemplateCompareMultipleTemplate(Templ, background, simTemplates, Tresult, Z)) {
        found = true;
    }
    templateFound(found, Template(), willInteract, delayMilliseconds);
    return Tresult.size();
}

int Interactions::fnbUniqueCompareTemplateA(const std::vector<std::string>& allTempl, const std::vector<std::string>& simTempls, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    resetCount();
    bool found = false;
    std::vector<Template>Tresult;
    std::vector<Template> Templ = getTemplate(allTempl, Color);
    std::vector<Template> simTemplates = getTemplate(simTempls, Color);
    std::vector<std::string> tid;
    loopPopUp(Color);
    if (cvmtfMultipleTemplateCompareMultipleTemplate(Templ, background, simTemplates, Tresult, Z)) {
        found = true;
        for (Template t : Tresult) {
            tid.push_back(t.id);
        }
        auto last = std::unique(tid.begin(), tid.end());
        tid.erase(last, tid.end());

    }
    templateFound(found, Template(), willInteract, delayMilliseconds);
    return tid.size();
}

int Interactions::fnbCompareTemplateA(const std::string& templ, const std::vector<std::string>& simTempls, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    resetCount();
    bool found = false;
    std::vector<Template>Tresult;
    Template Templ = getTemplate(templ, Color);
    std::vector<Template> simTempl = getTemplate(simTempls, Color);
    loopPopUp(Color);
    if (cvmtfMultipleTemplateCompareMultipleTemplate(Templ, background, simTempl, Tresult, Z)) {
        found = true;
    }
    templateFound(found, Template(), willInteract, delayMilliseconds);
    return Tresult.size();
}

bool Interactions::fcCompareA(const std::string& goodTemplate, const std::string& similartemplate, Zone Z, unsigned int delayMilliseconds)
{
    return fCompareA(goodTemplate, similartemplate, Z, delayMilliseconds, true);
}

bool Interactions::fCompareA(const std::string& goodTemplate, const std::string& similartemplate, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    bool found = false;
    resetCount();
    Template Tresult;
    Template goodmat = getTemplate(goodTemplate, Color);
    Template smat = getTemplate(similartemplate, Color);
    loopPopUp(goodmat.type);
    if (cvmtfCompareTemplate(goodmat, background, smat, Tresult, Z))found = true;
    return templateFound(found, Tresult, willInteract, delayMilliseconds);
}


bool Interactions::fcCompareMultipleTemplateA(const std::string& goodTemplate, const std::vector<std::string>& similartemplates, Zone Z, unsigned int delayMillisesconds)
{
    return fCompareMultipleTemplateA(goodTemplate, similartemplates, Z, delayMillisesconds, true);
}

bool Interactions::fCompareMultipleTemplateA(const std::string& goodTemplate, const std::vector<std::string>& similartemplates, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    bool found = false;
    resetCount();
    Template Tresult;
    Template goodmat = getTemplate(goodTemplate, Color);
    std::vector<Template>simMats = getTemplate(similartemplates, Color);
    loopPopUp(goodmat.type);
    if (cvmtfCompareMultipleTemplate(goodmat, background, simMats, Tresult))found = true;
    return templateFound(found, Tresult, willInteract, delayMilliseconds);
}


bool Interactions::fMultipleCompareMultipleTemplateA(const std::string& goodTemplate, const std::vector<std::string>& similartemplates, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    bool found = false;
    resetCount();
    std::vector<Template>Tresult;
    Template goodmat = getTemplate(goodTemplate, Color);
    std::vector<Template>simMats = getTemplate(similartemplates, Color);
    loopPopUp(goodmat.type);

    if (cvmtfMultipleTemplateCompareMultipleTemplate(goodmat, background, simMats, Tresult, Z))found = true;
    return templateFound(found, goodmat, willInteract, delayMilliseconds);
}


bool Interactions::templateFound(bool found, const Template& templ, bool willInteract, unsigned int delayMilliseconds)
{
    if (found) {
        rebootCount = 0;
        resCoords = templ.C;
        int width = templ.width;
        int height = templ.height;
        int randomXOffset = rand() % ((width / 2) + 1);
        int randomYOffset = rand() % ((height / 2) + 1);
        int randomX = resCoords.x + randomXOffset + 1;
        int randomY = resCoords.y + randomYOffset + 1;

        if (willInteract) {
            click(randomX, randomY);
        }
        if (delayMilliseconds == DEFAULTDELAY) {
            wait(waitfound);
        }
        else wait(delayMilliseconds);
    }
    else {
        rebootCount++;
        if (delayMilliseconds == DEFAULTDELAY) {
            wait(waitnotfound);
        }
        else wait(delayMilliseconds);
    }
    return found;
}

void Interactions::resetCount() {
    if (rebootCount >= MAXREBOOT) {
        std::cout << "Rebooting\n" << std::endl;
        throw RebootException();
    }
}


bool Interactions::templateFound(bool found, const Template& templ, bool willInteract, Action act, Coord coord, unsigned int delayMilliseconds)
{
    if (found) {
        rebootCount = 0;
        resCoords = templ.C;
        int width = templ.width;
        int height = templ.height;
        int randomXOffset = rand() % (width / 2) + 1;
        int randomYOffset = rand() % (height / 2) + 1;
        int randomX = resCoords.x + randomXOffset;
        int randomY = resCoords.y + randomYOffset;
        if (willInteract) {
            switch (act) {
            case Action::Click:
                click(randomX, randomY);
                break;
            case Action::Swipe:
                gSwipe(randomX, randomY, coord.x, coord.y, 500);
                break;
            }
        }
        if (delayMilliseconds == DEFAULTDELAY) {
            wait(waitfound);
        }
        else wait(delayMilliseconds);
    }
    else {
        rebootCount++;
        if (delayMilliseconds == DEFAULTDELAY) {
            wait(waitnotfound);
        }
        else wait(delayMilliseconds);
    }
    return found;
}

bool Interactions::fcOneTemplateA(const std::vector<std::string>& allTempl, std::string& foundTempl, Zone Z, unsigned int delayMilliseconds)
{
    return fOneTemplateA(allTempl, foundTempl, Z, delayMilliseconds, true);
}
bool Interactions::fcOneTemplateA(const std::vector<std::string>& toclick, const std::vector<std::string>& tonotclick, std::string& foundTempl, Zone Z, unsigned int delayMilliseconds)
{

    if (fOneTemplateA(tonotclick, foundTempl, Z, delayMilliseconds, false))return true;
    if (fcOneTemplateA(toclick, foundTempl, Z, delayMilliseconds))return true;
    return false;
}
bool Interactions::fcOneTemplateA(const std::vector<std::string>& toclick, const std::vector<std::string>& tonotclick, Zone Z, unsigned int delayMilliseconds)
{
    bool foundclick = false;
    bool foundnoclick = false;

    foundclick = fcOneTemplateA(toclick, Z, delayMilliseconds);
    foundnoclick = fOneTemplateA(tonotclick, Z, delayMilliseconds, false);
    return foundclick || foundnoclick;
}


bool Interactions::fOneTemplateA(std::vector<std::string> allTempl, std::string& foundTempl, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    bool found = false;
    resetCount();
    std::vector<Template>allMats = getTemplate(allTempl,Gray);
    Template Tresult;
    loopPopUp();
    if (cvmtfOneTemplate(allMats, background, Tresult, Z)) {
        found = true;
        foundTempl = Tresult.id;
    }
    return templateFound(found, Tresult, willInteract, delayMilliseconds);
}

bool Interactions::fMultipleTemplateA(const std::vector<std::string> allTempl, std::vector<std::string>& foundTemplates, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    bool found = false;
    resetCount();
    std::vector<Template>allMats = getTemplate(allTempl,Gray);
    std::vector<Template> Tresults;
    loopPopUp();
    if (cvmtfMultipleTemplate(allMats, background, Tresults, Z)) {
        found = true;
        for (Template t : Tresults) {
            foundTemplates.push_back(t.id);
        }
        auto last = std::unique(foundTemplates.begin(), foundTemplates.end());
        foundTemplates.erase(last, foundTemplates.end());
    }
    return templateFound(found, Template(), willInteract, delayMilliseconds);
}

bool Interactions::fMultipleCompareTemplateA(const std::vector<std::string> alltempl, const std::vector<std::string> simTempl, std::vector<std::string>& foundTemplates, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    bool found = false;
    resetCount();
    std::vector<Template>allMats = getTemplate(alltempl, Color);
    std::vector<Template>allSim = getTemplate(simTempl, Color);
    std::vector<Template> Tresults;
    loopPopUp(Color);
    if (cvmtfMultipleTemplateCompareMultipleTemplate(allMats, background, allSim, Tresults, Z)) {
        found = true;
        for (Template t : Tresults) {
            foundTemplates.push_back(t.id);
        }
        auto last = std::unique(foundTemplates.begin(), foundTemplates.end());
        foundTemplates.erase(last, foundTemplates.end());
    }
    return templateFound(found, Template(), willInteract, delayMilliseconds);
}


bool Interactions::fCompareOneTemplateA(const std::vector<std::string>& allTempl, const std::vector<std::string>& simTempl, std::string& foundTempl, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    bool found = false;
    resetCount();
    std::vector<Template>allMats = getTemplate(allTempl, Color);
    std::vector<Template>simMats = getTemplate(simTempl, Color);
    Template Tresult;
    loopPopUp(Color);
    if (cvmtfCompareOneTemplate(allMats, background, simMats, Tresult, Z)) {
        found = true;
        foundTempl = Tresult.id;
    }
    return templateFound(found, Tresult, willInteract, delayMilliseconds);
}



bool Interactions::fcOneTemplateA(const std::vector<std::string>& allTempl, Zone Z, unsigned int delayMilliseconds)
{
    return fOneTemplateA(allTempl, Z, delayMilliseconds, true);
}


bool Interactions::fOneTemplateA(const std::vector<std::string>& allTempl, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    bool found = false;
    resetCount();
    std::vector<Template>allMats = getTemplate(allTempl,Gray);
    Template Tresult;
    loopPopUp();
    if (cvmtfOneTemplate(allMats, background, Tresult, Z))found = true;
    return templateFound(found, Tresult, willInteract, delayMilliseconds);
}

bool Interactions::fOneTemplateEraseA(std::vector<std::string>& allTempl, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    bool found = false;
    resetCount();
    std::vector<Template>allMats = getTemplate(allTempl,Gray);
    Template Tresult;
    loopPopUp();
    if (cvmtfOneTemplate(allMats, background, Tresult, Z)) {
        eraseString(allTempl, Tresult.id);
        found = true;
    }
    return templateFound(found, Tresult, willInteract, delayMilliseconds);
}

bool Interactions::fcOneTemplateEraseA(std::vector<std::string>& allTempl, Zone Z, unsigned int delayMilliseconds)
{
    return fOneTemplateEraseA(allTempl, Z, delayMilliseconds, true);
}


bool Interactions::fOneMultipleTemplateA(std::vector<std::vector<std::string>> allTempl, Zone Z, unsigned int delayMilliseconds, bool willInteract)
{
    bool found = false;
    resetCount();
    std::vector<std::vector<Template>>allMats;
    std::vector<Template>Tresult;
    for (std::vector<std::string> oneAll : allTempl) {
        allMats.push_back(getTemplate(oneAll,Gray));
    }
    loopPopUp();
    if (cvmtfOneMultipleTemplate(allMats, background, Tresult, Z))found = true;
    return templateFound(found, Template(), willInteract, delayMilliseconds);
}



bool Interactions::fSwipeA(std::string templ, Coord Cresult, Zone Z, bool willInteract)
{
    bool found = false;
    resetCount();
    Template Mat = getTemplate(templ,Gray);
    Template Tresult;
    loopPopUp();
    if (cvmtfTemplate(Mat, background, Tresult, Z))found = true;
    return templateFound(found, Tresult, willInteract, Swipe, Cresult);
}

bool Interactions::fSwipeA(std::string templ, Direction dir, Zone Z, bool willInteract)
{
    bool found = false;
    resetCount();
    Coord Cend;
    Template Mat = getTemplate(templ,Gray);
    Template Tresult;
    loopPopUp();
    if (cvmtfTemplate(Mat, background, Tresult, Z)) {
        found = true;
        switch (dir) {
        case N:
            Cend.x = resCoords.x;
            break;
        case S:
            Cend.x = dimX;
            break;
        case E:
            Cend.x = 0;
            break;
        case W:
			Cend.x = 0;
			break;
		case NE:
			Cend.x = dimX;
			break;
		case NW:
			Cend.x = 0;
			break;
		case SE:
			Cend.x = dimX;
			break;
		case SW:
			Cend.x = 0;
			break;
		default:
			Cend.x = 0;
			break;
		}
	}
	return templateFound(found, Tresult, willInteract, Swipe, Cend);
}

bool Interactions::fMultipleDigitsA(int nb, std::vector<std::string>digits, std::vector<Template>& Tresult, Zone Z) {
    std::string imagetofind;
    if (nb < digits.size()) {
        imagetofind = digits.at(nb);
    }
    Template Tdigit = getTemplate(imagetofind,Color);
    Template Tbackg(backg, Color);
    std::vector<Template>Tdigits = getTemplate(digits,Color);
    return cvmtfMultipleTemplateCompareMultipleTemplate(Tdigit, Tbackg, Tdigits, Tresult, Z);
}

Number Interactions::fNumberIntegerA(std::vector<std::string>digits, std::vector<Unit>Units, Zone Z) {
    screenshot(adbId);
    Number Ntemp;
    Digit Dtemp;
    std::vector<Template> Tresult;
    for (int i = 0; i < digits.size(); ++i) {
        if (fMultipleDigitsA(i, digits, Tresult, Z)) {
            for (Template Templ : Tresult) {
                Dtemp.Cnb = Templ.C;
                Dtemp.dig = i;
                Ntemp.digits.push_back(Dtemp);
            }
        }
    }
    std::vector<std::string>allsameImages;
    for (std::string image : digits) {
        allsameImages.push_back(image);
    }
    for (Unit image : Units) {
        allsameImages.push_back(image.nameunit);
    }
    orderDigits(Ntemp.digits);
    for (int l = 0; l < Ntemp.digits.size(); ++l) {
        Ntemp.nb += Ntemp.digits.at(l).dig * pow(10, Ntemp.digits.size() - l - 1);
    }
    std::cout << Ntemp.nb << std::endl;
    for (Unit u : Units) {
        switch (u.Ord) {

        case Orders::noOrd:
            break;
        case Orders::K:
            if (fCompareMultipleTemplateA(u.nameunit, allsameImages, Z)) {
                Ntemp.K = true;
                Ntemp.nb *= pow(10, 3);
                std::cout << Ntemp.nb << std::endl;
                return Ntemp;
            }
            break;
        case Orders::M:
            if (fCompareMultipleTemplateA(u.nameunit, allsameImages, Z)) {
                Ntemp.M = true;
                Ntemp.nb *= pow(10, 6);
                std::cout << Ntemp.nb << std::endl;
                return Ntemp;
            }
            break;
        case Orders::B:
            if (fCompareMultipleTemplateA(u.nameunit, allsameImages, Z)) {
                Ntemp.B = true;
                Ntemp.nb *= pow(10, 9);
                std::cout << Ntemp.nb << std::endl;
                return Ntemp;
            }
            break;
        case Orders::T:
            if (fCompareMultipleTemplateA(u.nameunit, allsameImages, Z)) {
                Ntemp.T = true;
                Ntemp.nb *= pow(10, 12);
                std::cout << Ntemp.nb << std::endl;
                return Ntemp;
            }
            break;
        }

    }
    std::cout << Ntemp.nb << std::endl;
    return Ntemp;
}

Number Interactions::fNumberIntegerA(std::vector<std::string>digits, Zone Z) {
    screenshot(adbId);
    Number Ntemp;
    Digit Dtemp;
    std::vector<Template> Tresult;
    for (int i = 0; i < digits.size(); ++i) {
        if (fMultipleDigitsA(i, digits, Tresult, Z)) {
            for (Template Templ : Tresult) {
                Dtemp.Cnb = Templ.C;
                Dtemp.dig = i;
                Ntemp.digits.push_back(Dtemp);
            }
        }
    }
    std::vector<std::string>allsameImages;
    for (std::string image : digits) {
        allsameImages.push_back(image);
    }
    orderDigits(Ntemp.digits);
    for (int l = 0; l < Ntemp.digits.size(); ++l) {
        Ntemp.nb += Ntemp.digits.at(l).dig * pow(10, Ntemp.digits.size() - l - 1);
    }
    std::cout << Ntemp.nb << std::endl;
    return Ntemp;
}

Number Interactions::fNumberDecimalA(std::vector<std::string>digits, std::vector<Unit>Units, std::string dot, Zone Z) {
    screenshot(adbId);
    Number Ntemp;
    Digit Dtemp;
    std::vector<Template> Tresult;
    for (int i = 0; i < digits.size(); ++i) {
        if (fMultipleDigitsA(i, digits, Tresult, Z)) {
            for (Template Templ : Tresult) {
                Dtemp.Cnb = Templ.C;
                Dtemp.dig = i;
                Ntemp.digits.push_back(Dtemp);
            }
        }
    }

    std::vector<std::string>allsameImages;
    for (std::string image : digits) {
        allsameImages.push_back(image);
    }
    for (Unit image : Units) {
        allsameImages.push_back(image.nameunit);
    }
    Coord Cdot;
    if (fCompareMultipleTemplateA(dot, allsameImages, Z)) {
        Ntemp.decimal = true;
        Cdot = resCoords;
        std::cout << "DECIMAL NUMBER" << std::endl;
    }
    orderDigits(Ntemp.digits);
    for (int l = 0; l < Ntemp.digits.size(); ++l) {
        Ntemp.nb += Ntemp.digits.at(l).dig * pow(10, Ntemp.digits.size() - l - 1);
    }
    std::cout << Ntemp.nb << "\n";
    if (Ntemp.decimal == true) {
        for (int i = 0; i < Ntemp.digits.size(); i++) {
            if (Cdot.x < Ntemp.digits.at(i).Cnb.x) {
                Ntemp.dotposition = i;
                Ntemp.nb /= pow(10, Ntemp.digits.size() - i);
                std::cout << "Dot position:" << i << std::endl;
                break;
            }
        }
    }
    std::cout << Ntemp.nb << std::endl;
    for (Unit u : Units) {
        switch (u.Ord) {

        case Orders::noOrd:

            break;
        case Orders::K:
            if (fCompareMultipleTemplateA(u.nameunit, allsameImages, Z)) {
                Ntemp.K = true;
                Ntemp.nb *= pow(10, 3);
                std::cout << Ntemp.nb << std::endl;
                return Ntemp;
            }
            break;
        case Orders::M:
            if (fCompareMultipleTemplateA(u.nameunit, allsameImages, Z)) {
                Ntemp.M = true;
                Ntemp.nb *= pow(10, 6);
                std::cout << Ntemp.nb << std::endl;
                return Ntemp;
            }
            break;
        case Orders::B:
            if (fCompareMultipleTemplateA(u.nameunit, allsameImages, Z)) {
                Ntemp.B = true;
                Ntemp.nb *= pow(10, 9);
                std::cout << Ntemp.nb << std::endl;
                return Ntemp;
            }
            break;
        case Orders::T:
            if (fCompareMultipleTemplateA(u.nameunit, allsameImages, Z)) {
                Ntemp.T = true;
                Ntemp.nb *= pow(10, 12);
                std::cout << Ntemp.nb << std::endl;
                return Ntemp;
            }
            break;
        }
    }
    std::cout << Ntemp.nb << std::endl;
    return Ntemp;
}

Number Interactions::fNumberDecimalA(std::vector<std::string>digits, std::string dot, Zone Z) {
    screenshot(adbId);
    Number Ntemp;
    Digit Dtemp;
    std::vector<Template> Tresult;
    for (int i = 0; i < digits.size(); ++i) {
        if (fMultipleDigitsA(i, digits, Tresult, Z)) {
            for (Template Templ : Tresult) {
                Dtemp.Cnb = Templ.C;
                Dtemp.dig = i;
                Ntemp.digits.push_back(Dtemp);
            }
        }
    }

    std::vector<std::string>allsameImages;
    for (std::string image : digits) {
        allsameImages.push_back(image);
    }
    Coord Cdot;
    if (fCompareMultipleTemplateA(dot, allsameImages, Z)) {
        Ntemp.decimal = true;
        Cdot = resCoords;
        std::cout << "DECIMAL NUMBER" << std::endl;
    }
    orderDigits(Ntemp.digits);
    for (int l = 0; l < Ntemp.digits.size(); ++l) {
        Ntemp.nb += Ntemp.digits.at(l).dig * pow(10, Ntemp.digits.size() - l - 1);
    }
    std::cout << Ntemp.nb << std::endl;
    if (Ntemp.decimal == true) {
        for (int i = 0; i < Ntemp.digits.size(); i++) {
            if (Cdot.x < Ntemp.digits.at(i).Cnb.x) {
                Ntemp.dotposition = i;
                Ntemp.nb /= pow(10, Ntemp.digits.size() - i);
                std::cout << "Dot position:" << i << std::endl;
                break;
            }
        }
    }
    std::cout << Ntemp.nb << std::endl;
    return Ntemp;
}



int Interactions::distancePoint(const Coord& fixpt, const Coord& pt)
{
    return (int)sqrt(pow(fixpt.x - pt.x, 2) + pow(fixpt.y - pt.y, 2));
}

std::vector<Coord> Interactions::orderCoords(const std::vector<Coord>& coordes, Direction dir) {
    std::vector<Coord> coords = coordes;

    switch (dir) {
    case N:
        std::sort(coords.begin(), coords.end(), [](const Coord& a, const Coord& b) {
            return a.y < b.y;
            });
        break;

    case S:
        std::sort(coords.begin(), coords.end(), [](const Coord& a, const Coord& b) {
            return a.y > b.y;
            });
        break;

    case W:
        std::sort(coords.begin(), coords.end(), [](const Coord& a, const Coord& b) {
            return a.x < b.x;
            });
        break;

    case E:
        std::sort(coords.begin(), coords.end(), [](const Coord& a, const Coord& b) {
            return a.x > b.x;
            });
        break;

    case NW:
        std::sort(coords.begin(), coords.end(), [](const Coord& a, const Coord& b) {
            return a.x + a.y < b.x + b.y;
            });
        break;

    case NE:
        std::sort(coords.begin(), coords.end(), [](const Coord& a, const Coord& b) {
            return a.x - a.y > b.x - b.y;
            });
        break;

    case SW:
        std::sort(coords.begin(), coords.end(), [](const Coord& a, const Coord& b) {
            return a.y - a.x > b.y - b.x;
            });
        break;

    case SE:
        std::sort(coords.begin(), coords.end(), [](const Coord& a, const Coord& b) {
            return a.x + a.y > b.x + b.y;
            });
        break;
    }


    return coords;
}

std::vector<Template> Interactions::orderTemplates(const std::vector<Template>& allTempl, Direction dir) {
    std::vector<Template> coords = allTempl;

    switch (dir) {
    case N:
        std::sort(coords.begin(), coords.end(), [](const Template& a, const Template& b) {
            return a.C.y < b.C.y;
            });
        break;

    case S:
        std::sort(coords.begin(), coords.end(), [](const Template& a, const Template& b) {
            return a.C.y > b.C.y;
            });
        break;

    case W:
        std::sort(coords.begin(), coords.end(), [](const Template& a, const Template& b) {
            return a.C.x < b.C.x;
            });
        break;

    case E:
        std::sort(coords.begin(), coords.end(), [](const Template& a, const Template& b) {
            return a.C.x > b.C.x;
            });
        break;

    case NW:
        std::sort(coords.begin(), coords.end(), [](const Template& a, const Template& b) {
            return a.C.x + a.C.y < b.C.x + b.C.y;
            });
        break;

    case NE:
        std::sort(coords.begin(), coords.end(), [](const Template& a, const Template& b) {
            return a.C.x - a.C.y > b.C.x - b.C.y;
            });
        break;

    case SW:
        std::sort(coords.begin(), coords.end(), [](const Template& a, const Template& b) {
            return a.C.y - a.C.x > b.C.y - b.C.x;
            });
        break;

    case SE:
        std::sort(coords.begin(), coords.end(), [](const Template& a, const Template& b) {
            return a.C.x + a.C.y > b.C.x + b.C.y;
            });
        break;
    }


    return coords;
}

std::vector<Coord> Interactions::orderPoint(const std::vector<Coord>& coords, const Coord& Cf)
{
    std::vector<Coord> newcoords = coords;
    std::sort(newcoords.begin(), newcoords.end(), [&](const Coord& a, const Coord& b) {
        int distanceA = distancePoint(a, Cf);
        int distanceB = distancePoint(b, Cf);
        return distanceA < distanceB;
        });
    return newcoords;
}

Template Interactions::getTemplate(const std::string& image, typeMat type) {
    if (allTemplates.find(image) != allTemplates.end()) {
        Template templ = allTemplates.at(image);
        templ.setType(type);
        return templ;
    }
    else return Template();
}

std::vector<Template> Interactions::getTemplate(const std::vector<std::string>& images, typeMat type)
{
    std::vector<Template> result;
    for (std::string image : images) {
        if (allTemplates.find(image) != allTemplates.end()) {
            Template templ = allTemplates.at(image);
            templ.setType(type);
            result.push_back(templ);
        }
    }
    return result;
}



int Interactions::getDimX() {
    return dimX;
}

int Interactions::getDimY() {
    return dimY;
}

day Interactions::getUTCDay() {
    auto now = std::chrono::system_clock::now();
    std::time_t current_time = std::chrono::system_clock::to_time_t(now);
    tm utc_time;
    gmtime_s(&utc_time, &current_time);
    int day = utc_time.tm_wday;
    switch (day) {
    case 0:
        return Sunday;
    case 1:
        return Monday;
    case 2:
        return Tuesday;
    case 3:
        return Wednesday;
    case 4:
        return Thursday;
    case 5:
        return Friday;
    case 6:
        return Saturday;
    default:
        return Monday;
    }
}

int Interactions::getUTCHour() {
    auto now = std::chrono::system_clock::now();
    std::time_t current_time = std::chrono::system_clock::to_time_t(now);
    tm utc_time;
    gmtime_s(&utc_time, &current_time);
    return utc_time.tm_hour;
}

void Interactions::loadTemplate(std::string image)
{
    Template newTemplate(image);
    allTemplates.insert(std::pair<std::string, Template>(image, newTemplate));
}

void Interactions::loadAllTemplates() {
    std::string path = ".\\assets\\templates";
    std::filesystem::directory_iterator dir(path);
    std::vector<std::string>allFileNames;
    getFilesPath(allFileNames, dir);
    for (const std::string str : allFileNames) {
        loadTemplate(str);
    }
}

void Interactions::getFilesPath(std::vector<std::string>& tab, std::filesystem::directory_iterator path)
{
    std::filesystem::path currentPath = path->path().parent_path();

    std::vector<std::filesystem::directory_entry> folders;
    folders.clear();
    for (const auto& entry : path) {
        if (entry.is_regular_file()) {
            tab.push_back(entry.path().string());
        }
        else if (entry.is_directory()) {
            folders.push_back(entry);
        }
    }
    for (int i = 0; i < folders.size(); i++) {
        if (!std::filesystem::is_empty(folders.at(i).path())) {
            getFilesPath(tab, std::filesystem::directory_iterator(folders.at(i).path()));
        }
        else {
            std::filesystem::path currentPath = folders.at(i).path();
        }
    }
}

void Interactions::showVector(std::vector<std::string> vect)
{
    for (auto str : vect) {
        std::cout << str << "\n";
    }
}


int Interactions::getYear() {
    std::time_t t = std::time(nullptr);
    std::tm now;
    localtime_s(&now, &t);
    return now.tm_year + 1900;
}

int Interactions::getMonth() {
    std::time_t t = std::time(nullptr);
    std::tm now;
    localtime_s(&now, &t);
    return now.tm_mon + 1;
}

int Interactions::getDay() {
    std::time_t t = std::time(nullptr);
    std::tm now;
    localtime_s(&now, &t);
    int dayOfWeek = now.tm_wday;
    if (dayOfWeek == 0) {
        dayOfWeek = 7;
    }
    return dayOfWeek;
}

int Interactions::getHour() {
    std::time_t t = std::time(nullptr);
    std::tm now;
    localtime_s(&now, &t);
    return now.tm_hour;
}

int Interactions::getMinute() {
    std::time_t t = std::time(nullptr);
    std::tm now;
    localtime_s(&now, &t);
    return now.tm_min;
}

int Interactions::getSecond() {
    std::time_t t = std::time(nullptr);
    std::tm now;
    localtime_s(&now, &t);
    return now.tm_sec;
}

void Interactions::setRebootCount(int nb) {
    rebootCount = nb;
}


int Interactions::getRebootCount() {
    return rebootCount;
}

void Interactions::setDimX(int x) {
    dimX = x;
}
void Interactions::setDimY(int y) {
    dimY = y;
}
int Interactions::getMatWidth(const std::string& name)
{
    return CreateMat(name, Gray).cols;
}
int Interactions::getMatHeight(const std::string& name)
{
    return CreateMat(name, Gray).rows;
}

void Interactions::wait(int milliSeconds)
{
    std::this_thread::sleep_for(std::chrono::milliseconds(milliSeconds));
}
void Interactions::gSwipeX(double x1, double x2, int millitime)
{
    gSwipe(x1, dimY / 2, x2, dimY / 2, millitime);
}

void Interactions::gSwipeY(double y1, double y2, int millitime)
{
    gSwipe(dimX / 2, y1, dimX / 2, y2, millitime);
}

void Interactions::gSwipe(double x1, double y1, double x2, double y2, int millitime)
{
    swipe((int)x1, (int)y1, (int)x2, (int)y2, millitime, adbId);
}

void Interactions::click(Coord Coord)
{
    click(Coord.x, Coord.y);
}
void Interactions::click(int x, int y)
{
    touch(x, y, adbId);
}

void Interactions::orderDigits(std::vector<Digit>& digits) {
    std::sort(digits.begin(), digits.end(), [&](const Digit& a, const Digit& b) {
        return a.Cnb.x < b.Cnb.x;
        });
}

bool Interactions::eraseString(std::vector<std::string>& allTempl, std::string templ)
{
    std::vector<std::string>::iterator it;
    it = std::find(allTempl.begin(), allTempl.end(), templ);
    if (it != allTempl.end()) {
        allTempl.erase(it);
        return true;
    }
    return false;
}

bool Interactions::fStr(const std::vector<std::string>& allTempl, std::string templ)
{
    std::vector<std::string>::iterator it;
    return std::find(allTempl.begin(), allTempl.end(), templ) != allTempl.end();
}

bool Interactions::fStr(const std::vector<std::string>& allTempl, const std::vector<std::string> allkeys)
{
    for (std::string key : allkeys) {
        if (!fStr(allTempl, key))return false;
    }
    return true;
}

bool Interactions::fOneStr(const std::vector<std::string>& allTempl, const std::vector<std::string> allkeys)
{
    for (std::string key : allkeys) {
        if (fStr(allTempl, key))return true;
    }
    return false;
}


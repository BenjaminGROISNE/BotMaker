#include "Activities.h"

Alchemy::Alchemy() :Activity()
{
	name = "ALCHEMY";
	activate = false;
	this->key='a';
}

bool Alchemy::doActivity()
{
    try {
        
        while (!fcA(cabode));
        std::vector<std::string>abodecult = { corporiacave,magickacave };
        while (!fA(peticon))gSwipe(0.3 * dimX, 0.5 * dimY, 0.8 * dimX, 0.5 * dimY, 500);
        while (!fcOneTemplateA(abodecult))gSwipe(0.6 * dimX, 0.5 * dimY, 0.4 * dimX, 0.5 * dimY, 1000);
        while (fcOneTemplateA(abodecult));
        while (!fA(pickelixir));
        bool corporia = false;
        bool magicka = false;
        if (fA(magickacultivator))magicka = true;
        else if (fA(corporiacultivator))corporia = true;
        while (!fOneTemplateA(abodecult)) {
            fcA(back);
        }
        while (!fcA(alchemyicon))gSwipe(0.4 * dimX, 0.5 * dimY, 0.6 * dimX, 0.5 * dimY, 1000);
        while (fcA(alchemyicon));
        std::vector<std::string>alchemychoice = { selectcauldron ,cancelpills,claimpills };
        while (!fOneTemplateA(alchemychoice));
        while (!fA(onselectpillpage)) {
            if (fcA(selectcauldron));
            else if (fcA(claimpills)) {
                while (fcA(changepills));
            }
            else if (fA(cauldron)) {
                while (fcA(changepills));
            }
            else if (fA(cancelpills)) {
                while (fA(cancelpills));
            }
        }
        bool cancraftpill = false;
        std::vector<std::string>corpPowder = { dracophantpowder,caelumpowder,utilitypowder,foursymbolspowder };
        std::vector<std::string>magickaPill = { choosejadeitypill,choosecosmospill,goldendragonpill,etiquettepill };
        if (corporia || magicka) {
            if (corporia) {
                if (fcOneTemplateA(corpPowder)) {
                    if (!fcA(norecipe))cancraftpill = true;
                }
            }
            else if (magicka) {
                if (fcOneTemplateA(magickaPill)) {
                    if (!fcA(norecipe))cancraftpill = true;
                }
            }
        }

        if (cancraftpill) {
            while (fcA(selectpill));


            std::vector<std::string>limitpill = { zerolimit,onelimit,twolimit,threelimit,fourlimit,fivelimit,sixlimit,sevenlimit,eightlimit,ninelimit };
            std::vector<std::string>craftpills = { zerocraft,onecraft,twocraft,threecraft,fourcraft,fivecraft,sixcraft,sevencraft,eightcraft,ninecraft };
            std::vector<std::string>coremat = { graycore,greencore,bluecore,purplecore,yellowcore };
            std::vector<std::string>outcomecraft = { nopillmat,tapblank,claimpills };
            std::vector<std::string>goodoutcome = { tapblank,claimpills };

            Coord Cmaterial(0.37 * dimX, 0.78 * dimY);

            Coord C1mat(0.271 * dimX, 0.24 * dimY);
            Coord C2mat(0.355 * dimX, 0.636 * dimY);
            Zone matquality(C1mat, C2mat);

            Coord C1TextZone((double)0, 0.833 * dimY);
            Coord C2TextZone((double)dimX, 0.877 * dimY);
            Zone text(C1TextZone, C2TextZone);


            Coord C1pillLimit, C2pillLimit;
            Zone zonepillLimit;

            Zone ZoneCraftPill;
            Coord C1craftpill, C2craftpill;

            bool firstcraftepic = false;
            bool epicpill = false;
            bool legpill = false;
            bool initpilldim = true;
            bool cancraftgreen = true;
            bool cancraftgray = true;
            bool cancraftblue = true;
            bool cancraftepic = true;
            bool cancraftleg = true;
            bool cancraftbetterpill = false;
            int pillbeforeepic = 0;

            bool cancraftanymat = true;
            //Zone craftmats
            while (!fA(quantitypill)) {
                fcA(craftpill);
            }
            while (!fA(lowerpill));
            C1craftpill.x = resCoords.x + getMatWidth(lowerpill);
            C1craftpill.y = resCoords.y - getMatHeight(lowerpill);
            while (!fA(augmentpill));
            C2craftpill.x = resCoords.x - getMatWidth(augmentpill);
            C2craftpill.y = resCoords.y - getMatHeight(augmentpill);
            ZoneCraftPill.C1 = C1craftpill;
            ZoneCraftPill.C2 = C2craftpill;
            while (!fA(cauldron))click(0.5 * dimX, 0.999 * dimY);



            while (cancraftgreen || cancraftgray || cancraftblue) {
                if (!cancraftanymat)break;
                //Find HOW MANY CRAFTS TILL THRESHOLD
                if (fA(alwaystext, text)) {
                    epicpill = true;
                    cancraftbetterpill = true;
                }
                else if (fA(gettext, text)) {
                    cancraftbetterpill = true;
                    legpill = true;
                }
                if (initpilldim && !cancraftbetterpill) {
                    while (!fA(crafttext, text));
                    C1pillLimit.x = resCoords.x + (getMatWidth(crafttext) / 2);
                    C1pillLimit.y = resCoords.y - (getMatHeight(crafttext) / 2);
                    while (!fA(moretext, text));
                    C2pillLimit.x = resCoords.x - (getMatWidth(moretext) / 2);
                    C2pillLimit.y = resCoords.y + (getMatHeight(moretext) / 2);
                    initpilldim = false;
                    zonepillLimit.C1 = C1pillLimit;
                    zonepillLimit.C2 = C2pillLimit;
                    zonepillLimit.showZone();
                    initpilldim = false;
                }
                if (!cancraftbetterpill) {
                    Number nbpillLimit = fNumberIntegerA(limitpill, zonepillLimit);
                    std::cout << nbpillLimit.nb << " pills before legendary pill" << std::endl;
                    std::cout << (int)nbpillLimit.nb % 100 << " pills before legendary or epic pill" << std::endl;
                    pillbeforeepic = (int)nbpillLimit.nb % 100;
                }

                //CRAFT BETTER PILL
                if (cancraftbetterpill) {
                    while (cancraftbetterpill) {
                        if (epicpill) {
                            if (cancraftleg) {
                                craftBetterPill(craftpills, coremat, outcomecraft, goodoutcome, ZoneCraftPill, matquality, cancraftleg, Cmaterial, yellowcore, cancraftbetterpill, cancraftanymat);
                            }
                            else if (cancraftepic) {
                                craftBetterPill(craftpills, coremat, outcomecraft, goodoutcome, ZoneCraftPill, matquality, cancraftepic, Cmaterial, purplecore, cancraftbetterpill, cancraftanymat);
                            }
                            else if (cancraftblue) {
                                craftBetterPill(craftpills, coremat, outcomecraft, goodoutcome, ZoneCraftPill, matquality, cancraftblue, Cmaterial, bluecore, cancraftbetterpill, cancraftanymat);
                            }

                            else {
                                cancraftbetterpill = false;
                                epicpill = false;
                            }

                        }
                        else if (legpill) {
                            if (cancraftgray) {
                                craftBetterPill(craftpills, coremat, outcomecraft, goodoutcome, ZoneCraftPill, matquality, cancraftgray, Cmaterial, graycore, cancraftbetterpill, cancraftanymat);
                            }
                            else if (cancraftgreen) {
                                craftBetterPill(craftpills, coremat, outcomecraft, goodoutcome, ZoneCraftPill, matquality, cancraftgreen, Cmaterial, greencore, cancraftbetterpill, cancraftanymat);
                            }
                            else if (cancraftblue) {
                                craftBetterPill(craftpills, coremat, outcomecraft, goodoutcome, ZoneCraftPill, matquality, cancraftblue, Cmaterial, bluecore, cancraftbetterpill, cancraftanymat);
                            }
                            else {
                                cancraftbetterpill = false;
                                legpill = false;
                            }
                        }
                    }
                }

                //CRAFT NORMAL PILL
                else if (cancraftblue) {
                    craftNormalPill(craftpills, coremat, outcomecraft, goodoutcome, ZoneCraftPill, matquality, pillbeforeepic, cancraftblue, Cmaterial, bluecore, cancraftanymat);

                }
                else if (cancraftgreen) {
                    craftNormalPill(craftpills, coremat, outcomecraft, goodoutcome, ZoneCraftPill, matquality, pillbeforeepic, cancraftgreen, Cmaterial, greencore, cancraftanymat);

                }
                else if (cancraftgray) {
                    craftNormalPill(craftpills, coremat, outcomecraft, goodoutcome, ZoneCraftPill, matquality, pillbeforeepic, cancraftgray, Cmaterial, graycore, cancraftanymat);
                }
            }
        }
        
    }
    catch (RebootException e) {
        return false;
    }
}
bool Alchemy::craftNormalPill(std::vector<std::string>craftsdigits, std::vector<std::string>coremat, std::vector<std::string>outcomecraft, std::vector<std::string>goodoutcome, Zone craft, Zone matquality, int pillbeforeepic, bool& cancraftmat, Coord cmaterial, std::string core, bool& cancraftanymat) {
    while (!fA(craftingmaterial)) {
        click(cmaterial.x, cmaterial.y);
        if (fA(nopillmat)) {
            cancraftanymat = false;
            return false;
        }
    }
    int multiplier = 1;
    if (core == bluecore)multiplier = 4;
    else if (core == greencore)multiplier = 1;
    else if (core == graycore)multiplier = 0;

    if (fcCompareMultipleTemplateA(core, coremat, matquality)) {
        while (fcCompareMultipleTemplateA(core, coremat, matquality));
        while (!fA(quantitypill)) {
            fcA(craftpill);
        }
        Number nbpillCraft = fNumberIntegerA(craftsdigits, craft);
        int compteur = 0;
        while (nbpillCraft.nb * multiplier - pillbeforeepic >= multiplier && nbpillCraft.nb > 0) {
            nbpillCraft.nb--;
            compteur++;
            fcA(lowerpill, Zone(), 0);
            std::cout << "Minus one" << std::endl;
        }
        while (fcA(confirmpillquantity));
        while (!fOneTemplateA(outcomecraft))fA(activecauldron);
        while (fcOneTemplateA(goodoutcome));
        if (fA(nopillmat)) {
            while (!fA(cauldron))click(0.5 * dimX, 0.999 * dimY);
        }
        if (compteur == 0) cancraftmat = false;
    }
    else {
        while (!fA(cauldron))click(0.5 * dimX, 0.999 * dimY);
        cancraftmat = false;
    }
}

bool Alchemy::craftBetterPill(std::vector<std::string>craftsdigits, std::vector<std::string>coremat, std::vector<std::string>outcomecraft, std::vector<std::string>goodoutcome, Zone craft, Zone matquality, bool& cancraftmat, Coord cmaterial, std::string core, bool& cancraftbetterpill, bool& cancraftanymat) {
    while (!fA(craftingmaterial)) {
        click(cmaterial.x, cmaterial.y);
        if (fA(nopillmat)) {
            cancraftanymat = false;
            return false;
        }
    }
    if (fcCompareMultipleTemplateA(core, coremat, matquality)) {
        while (fcCompareMultipleTemplateA(core, coremat, matquality));
        while (!fA(quantitypill)) {
            fcA(craftpill);
        }
        Number nbpillCraft = fNumberIntegerA(craftsdigits, craft);
        if (nbpillCraft.nb == 1) {
            fcA(confirmpillquantity);
            while (!fOneTemplateA(outcomecraft))fA(activecauldron);
            if (fcOneTemplateA(goodoutcome)) {
                cancraftmat = false;
            }
            else if (fA(nopillmat)) {
                while (!fA(cauldron))click(0.5 * dimX, 0.999 * dimY);
            }
            cancraftmat = false;
        }
        else {
            fSwipeA(buttonchoosequantity, W);
            fcA(confirmpillquantity);
            while (!fcOneTemplateA(goodoutcome));
            cancraftbetterpill = false;
        }
    }
    else {
        while (!fA(cauldron))click(0.5 * dimX, 0.999 * dimY);
        cancraftmat = false;
    }
}

Garden::Garden():Activity()
{
    name="GARDEN";
}

bool Garden::doActivity()
{
    try {
        

        while (!fcA(cabode));
        while (!fcA(gardenicon)) {
            gSwipe(0.9 * dimX, 0.5 * dimY, 0.3 * dimX, 0.5 * dimY, 500);
        }
        while (fcA(gardenicon));
        while (fcA(claimgarden));
        while (!fA(waterattempts)) {
            fcA(water);
        }
        Coord attemptsC1 = { resCoords.x ,resCoords.y };
        Coord attemptsC2 = { attemptsC1.x + getMatWidth(waterattempts),attemptsC1.y + getMatHeight(waterattempts) };
        Coord rightnbattemptsC1 = { attemptsC2.x,attemptsC1.y };
        Coord rightnbattemptsC2 = { attemptsC2.x + getMatWidth(waterattempts) / 2,attemptsC2.y };
        Coord downattemptsC1 = { attemptsC1.x,attemptsC2.y };
        Coord downattemptsC2 = { attemptsC2.x,attemptsC2.y + getMatHeight(waterattempts) };
        Zone GoodZone;
        bool foundnbattempts = false;
        while (!foundnbattempts) {
            Zone rightnbattemptsZone(rightnbattemptsC1, rightnbattemptsC2);
            if (fA(slash, rightnbattemptsZone)) {
                foundnbattempts = true;
                GoodZone = rightnbattemptsZone;
            }
            Zone downnbattempts(downattemptsC1, downattemptsC2);
            if (fA(slash, downnbattempts)) {
                foundnbattempts = true;
                GoodZone = downnbattempts;
            }
        }
        GoodZone.showZone();
        std::vector<std::string>digits = { zerowater,onewater,twowater,threewater,fourwater,fivewater,sixwater,sevenwater,eightwater,ninewater };
        Number nb = fNumberIntegerA(digits, GoodZone);
        bool foundfate = false;
        if (nb.digits.size() <= 3 && nb.digits.size() >= 2) {
            if (nb.digits.at(0).dig == 1) {
                while (!foundfate) {
                    if (fA(fatewater)) {
                        foundfate = true;
                        Coord C1fate(resCoords.x, resCoords.y);
                        Coord C2fate(C1fate.x + getMatWidth(fatewater), C1fate.y + getMatHeight(fatewater));
                        Coord C1freefate(C2fate.x, C1fate.y);
                        Coord C2freefate(C2fate.x + 2 * getMatWidth(fatewater), C2fate.y);
                        Zone freezone(C1freefate, C2freefate);
                        freezone.showZone();
                        if (!fA(freegarden, freezone)) {
                            std::cout << "Garden already done, SET IT MANUALLY NEXT TIME IF NOT" << std::endl;
                            while (!fcA(cancelwater));
                            while (fcA(cancelwater));
                        }
                        else {
                            std::cout << "Only one free garden selected AUGMENT IT MANUALLY";
                            while (!fcA(confirmwater));
                            while (fcA(confirmwater));
                        }
                    }
                }
            }
            else if (nb.digits.at(0).dig < 6) {
                std::cout << "First garden of the day";
                while (!fcA(confirmwater));
                while (fcA(confirmwater));
            }
        }
        while (fcA(claimgarden));
        
        return true;
    }
    catch (RebootException e) {
        return false;
    }
}

Worldrift::Worldrift():Activity()
{
	name="WORLDRIFT";
}

bool Worldrift::doActivity()
{
    try {
        
        bool noattempts = false;
        bool domanualreincarnation = true;
        bool doautoreincarnation = false;
        while (!fcA(cabode));
        while (!fcA(worldrifticon)) {
            gSwipe(0.3 * dimX, 0.5 * dimY, 0.9 * dimX, 0.5 * dimY, 500);
        }
        while (!fA(startreincarnation)) {
            fcA(worldrifticon);
        }
        if (fA(autoworldrift)) {
            doautoreincarnation = true;
            domanualreincarnation = false;
        }

        if (doautoreincarnation) {
            while (!fcA(autoworldrift));
            Coord C1mid(0, 0);
            Coord C2mid(dimX, 0.65 * dimY);
            Zone mid(C1mid, C2mid);
            while (!fA(autoreincarnationtile, mid)) {
                if (fA(noreincarnationattempts)) {
                    noattempts = true;
                    break;
                }
                fcA(autoworldrift);
            }
            if (!noattempts) {
                while (!fSwipeA(changenbreincarnation, E));
                while (!fcA(tapblank))fcA(autoreincarnationtile, mid);
                while (fcA(tapblank));
            }
        }
        else if (domanualreincarnation) {
        anothertry:
            while (!fcA(randomstart)) {
                if (fA(noreincarnationattempts)) {
                    noattempts = true;
                    break;
                }
                fcA(startreincarnation);
            }
            if (!noattempts) {
                while (!fcA(startreincarnation));
                while (fcA(startreincarnation));
                while (!fcA(skipreincarnation));
                while (fcA(skipreincarnation));
                while (!fcA(confirmreincarnation));
                while (fcA(confirmreincarnation));
                while (!fcA(endreincarnation));
                while (fcA(endreincarnation));
                goto anothertry;
            }
        }
        return true;
    }
    catch (RebootException e) {
        return false;
    }
}

Arena::Arena():Activity()
{
	name="ARENA";
}

bool Arena::doActivity()
{
    try {
        
        while (!fA(sectlibrary)) {
            fcA(csect);
        }
        while (!fcA(arena)) {
            fcA(ctown);
        }
        while (!fcA(challengearena)) {
            fcA(arena);
        }
        while (!fA(challengetilearena)) {
            fcA(challengearena);
        }
        int nb = 0;
        fnbTemplateA(yinyangarena, nb);
        if (nb <= 1) {
            fcA(refresharena);
            fnbTemplateA(yinyangarena, nb);
        }
        if (nb > 1) {
            Coord C1enemytofight, C2enemytofight;
            std::vector<std::string> figures = { zeroarena,onearena,twoarena,threearena,fourarena,fivearena,sixarena,sevenarena,eightarena,ninearena };
            Unit Ubillionarena(billionarena, B);
            std::vector<Unit> Units = { Ubillionarena };
            double mybr, enemybr;
            Coord topbr, bottombr;
            int nbrefresh = 0;

            fOneTemplateMultipleTemplateA(yinyangarena, S, 0);
            topbr = { resCoords.x ,resCoords.y };
            bottombr = { (int)(topbr.x + 0.182 * dimX),(int)(topbr.y + getMatHeight(yinyangarena)) };
            Zone Tofight = { topbr,bottombr };
            Tofight.showZone();
            Number br = fNumberDecimalA(figures, Units, dotarena, Tofight);
            mybr = br.nb;
            while (nbrefresh < 3) {
                fOneTemplateMultipleTemplateA(yinyangarena, S, 1);
                topbr = { resCoords.x,resCoords.y };
                bottombr = { (int)(topbr.x + 0.182 * dimX),(int)(topbr.y + getMatHeight(yinyangarena)) };
                Tofight = { topbr,bottombr };
                Tofight.showZone();
                br = fNumberDecimalA(figures, Units, dotarena, Tofight);
                enemybr = br.nb;
                if (mybr * 1.025 > enemybr) {
                    while (fcOneTemplateMultipleTemplateA(challengetilearena, S, 0) && !fA(heroinvitationarena));
                    if (fA(heroinvitationarena)) {
                        while (!fcA(back))click(0.5 * dimX, 0.99 * dimY);
                        break;
                    }
                    std::vector<std::string>outcome = { victorytower,defeattower };
                    std::vector<std::string>speed = { speedone,speedtwo,speedthree };
                    std::vector<std::string>infspeed = { speedone,speedtwo };
                    while (fOneTemplateA(speed)) {
                        while (!fA(speedthree)) {
                            if (!fcOneTemplateA(infspeed))break;
                        }
                    }
                    while (!fA(challengearena)) {
                        fcOneTemplateA(outcome);
                    }
                }
                nbrefresh++;
                while (!fA(challengetilearena)) {
                    fcA(challengearena);
                }
                fcA(refresharena);
            }
        }
        return true;
    }
    catch (RebootException e) {
        return false;
    }
}

Assistant::Assistant():Activity()
{
    name="ASSISTANT";
}

bool Assistant::doActivity()
{
    try {
        std::vector<Coord>savecoord;
        std::vector<std::string>tasks = { atask,btask,ctask,dtask,etask };
        std::vector<std::string>collections = { yellowcollection,purplecollection,bluecollection,greencollection,graycollection };
        std::vector<std::string>wanted = { yellowwanted,purplewanted,bluewanted,greenwanted,graywanted };
        
        while (!fcA(assistant));
        while (fcA(assistant));
        while (!fcA(assistantmarket));
        while (fcA(assistantmarket));
        while (!fA(back));
        while (fcA(implement));
        while (!fcA(back));
        while (!fcA(assistantfatevillion));
        while (fcA(assistantfatevillion));
        while (!fSwipeA(refreshfatevillion, W));
        while (!fcA(implement));
        while (!fcA(back));
        while (fcA(back));
        while (!fcA(assistantalchemy));
        while (fcA(assistantalchemy));
        while (!fA(back));
        if (!fA(stopassistant)) {
            fcA(claimalchemyassistant);
            while (!fcA(implementalchemyassistant));
        }
        while (!fcA(back));
        while (fcA(back));
        while (!fcA(assistantbounty));
        while (!fA(back))fcA(assistantbounty);
        while (fcCompareMultipleTemplateA(yellowcollection, collections) || fcCompareMultipleTemplateA(yellowwanted, wanted)) {
            Coord other = { resCoords.x + dimX * 0.72,resCoords.y + 0.13 * dimY };
            Zone Z = { resCoords,other };
            if (!fcA(completebounty, Z))break;
        }
        while (fcCompareMultipleTemplateA(purplecollection, collections) || fcCompareMultipleTemplateA(purplewanted, wanted)) {
            Coord other = { resCoords.x + dimX * 0.72,resCoords.y + 0.13 * dimY };
            Zone Z = { resCoords,other };
            if (!fcA(completebounty, Z))break;
        }
        while (fcCompareMultipleTemplateA(bluecollection, collections) || fcCompareMultipleTemplateA(bluewanted, wanted)) {
            Coord other = { resCoords.x + dimX * 0.72,resCoords.y + 0.13 * dimY };
            Zone Z = { resCoords,other };
            if (!fcA(completebounty, Z))break;
        }
        while (!fcA(back));
        while (!fcA(assistantdailytask))fcA(back);
        while (!fA(back))fcA(assistantdailytask);
        fcA(quickimplement);
        while (fA(quickimplement))fcA(secttaskassistant);
        while (fcCompareMultipleTemplateA(atask, tasks)) {
            Coord other = { resCoords.x + dimX * 0.72,resCoords.y + 0.13 * dimY };
            Zone Z = { resCoords,other };
            if (!fcA(starttask, Z))break;
        }
        while (fcCompareMultipleTemplateA(btask, tasks)) {
            Coord other = { resCoords.x + dimX * 0.72,resCoords.y + 0.13 * dimY };
            Zone Z = { resCoords,other };
            if (!fcA(starttask, Z))break;
        }
        while (fcCompareMultipleTemplateA(ctask, tasks)) {
            Coord other = { resCoords.x + dimX * 0.72,resCoords.y + 0.13 * dimY };
            Zone Z = { resCoords,other };
            if (!fcA(starttask, Z))break;
        }
        while (fcA(back));
        while (!fA(ctown))click(0.5 * dimX, 0.95 * dimY);
        

    }
    catch (RebootException e) {
        return false;
    }
}

Divinities::Divinities():Activity()
{
	name="DIVINITIES";
}

bool Divinities::doActivity()
{
    try {
        
        gSwipe(0.4 * dimX, 0.5 * dimY, 0.6 * dimX, 0.5 * dimY, 1000);
        while (!fA(divinities)) {
            gSwipe(0.3 * dimX, 0.5 * dimY, 0.7 * dimX, 0.5 * dimY, 1000);
        }
        while (!fA(mail)) {
            gSwipe(0.3 * dimX, 0.5 * dimY, 0.7 * dimX, 0.5 * dimY, 1000);
        }
        while (!fcA(divinities))
            while (!fcA(immortalbanquet)) {
                fcA(divinities);
            }
        while (fcA(immortalbanquet));
        std::vector<std::string>choice;
        choice = { invitebanquet,partyover };
        while (!fOneTemplateA(choice));
        if (fcA(invitebanquet)) {
            while (fcA(invitebanquet));
            while (!fcA(bagbanquet));
            while (fcA(bagbanquet));
        }
        
    }
    catch (RebootException e) {
        return false;
    }
}

Heirloom::Heirloom():Activity()
{
	name="HEIRLOOM";
}

bool Heirloom::doActivity()
{
    try {
        
        while (!fA(divinities)) {
            gSwipe(0.4 * dimX, 0.5 * dimY, 0.6 * dimX, 0.5 * dimY, 500);
        }
        while (!fcA(heirloom)) {
            gSwipe(0.6 * dimX, 0.5 * dimY, 0.4 * dimX, 0.5 * dimY, 500);
        }
        while (fcA(heirloom));
        while (!fcA(cexotic));
        if (fA(freeexplore)) {
            if (fcCompareA(exploreonce, explore10)) {
                while (fcA(tapblank));
            }
        }
        while (!fcA(cspiritland));
        if (fA(freeexplore)) {
            if (fcCompareA(exploreonce, explore10)) {
                while (fcA(tapblank));
            }
        }
        
    }
    catch (RebootException e) {
        return false;
    }
}

Mail::Mail():Activity()
{
	name="MAIL";
}

bool Mail::doActivity()
{
    try {
        
        while (!fA(divinities)) {
            gSwipe(0.4 * dimX, 0.5 * dimY, 0.6 * dimX, 0.5 * dimY, 1000);
        }
        while (fA(divinities) && !fcA(mail)) {
            gSwipe(0.6 * dimX, 0.5 * dimY, 0.4 * dimX, 0.5 * dimY, 1000);
        }
        while (!fcA(claimallmail))fcA(mail);
        while (fcA(tapblank));
        


    }
    catch (RebootException e) {
        return false;
    }
}

Otherworld::Otherworld():Activity()
{
	name="OTHERWORLD";
}

bool Otherworld::doActivity()
{
    try {
        goHome home;
        if (((getUTCDay() == Thursday && getUTCHour() > 16 && getUTCHour() < 23) || (getUTCDay() == Friday && getUTCHour() < 1))) {
            home.doActivity();
            return false;
        }

        while (!fcA(ctown));
        while (!fcA(otherworld)) {
            gSwipe(0.8 * dimX, 0.5 * dimY, 0.3 * dimX, 0.5 * dimY, 500);
        }
        while (!fcA(xserverduel)) {
            fcA(otherworld);
        }
        while (!fA(refreshxserver)) {
            fcA(xserverduel);
        }
        std::vector<std::string>mybr = { myzeroxserver,myonexserver ,mytwoxserver ,mythreexserver ,myfourxserver ,myfivexserver ,mysixxserver ,mysevenxserver ,myeightxserver ,myninexserver };
        Unit mybillion(mybillionxserver, B);
        std::vector<Unit>myunits = { mybillion };
        std::vector<std::string>enemybr = { zeroxserver,onexserver,twoxserver,threexserver,fourxserver,fivexserver,sixxserver,sevenxserver,eightxserver,ninexserver };
        Unit enemybillion(billionxserver, B);
        Unit enemythousand(thousandxserver, K);
        Unit enemymillion(millionxserver, M);
        std::vector<Unit>enemyunits = { enemybillion,enemymillion,enemythousand };
        int nbrefresh = 0;
        int nbbattle = 0;
        bool nobattleleft = false;
        while (!fA(bottomxserver)) {
            gSwipe(0.5 * dimX, 0.6 * dimY, 0.5 * dimX, 0.3 * dimY, 500);
        }
        int mypos;
        if (fA(battleotherworld)) {
            mypos = 1;
        }
        else mypos = 0;

        fOneTemplateMultipleTemplateA(yinyangxserver, S, mypos);
        Coord C1br = { resCoords.x + getMatWidth(yinyangxserver),resCoords.y };
        Coord C2br = { C1br.x + getMatWidth(yinyangxserver) * 4,C1br.y + getMatHeight(yinyangxserver) };
        Zone mybrZone = { C1br,C2br };
        mybrZone.showZone();
        Number myPower = fNumberDecimalA(mybr, myunits, mydotxserver, mybrZone);
        std::cout << myPower.nb;


        while (true) {
            bool onlysweep = false;
            fOneTemplateMultipleTemplateA(yinyangxserver, S, mypos + 1);
            Coord C1br = { resCoords.x + getMatWidth(yinyangxserver),resCoords.y };
            Coord C2br = { C1br.x + getMatWidth(yinyangxserver) * 4,C1br.y + getMatHeight(yinyangxserver) };
            Zone enemybrZone = { C1br,C2br };
            enemybrZone.showZone();
            Number enemyPower = fNumberDecimalA(enemybr, enemyunits, dotxserver, enemybrZone);
            std::cout << enemyPower.nb;
            bool retry = true;
            if (enemyPower.nb < myPower.nb / 50) {
                std::cout << "Rank Reset will fight weak ennemies" << std::endl;
                while (!fcA(xserverduel)) {
                    fcA(back);
                }
                while (!fA(refreshxserver)) {
                    fcA(xserverduel);
                }

                while (retry) {
                    fcOneTemplateMultipleTemplateA(challengexserver, N, 0);
                    std::vector<std::string> outcome = { victorytower,defeattower };
                    while (!fOneTemplateA(outcome)) {
                        std::vector<std::string>speed = { speedone,speedtwo,speedthree };
                        std::vector<std::string>infspeed = { speedone,speedtwo };
                        while (fOneTemplateA(speed)) {
                            while (!fA(speedthree)) {
                                if (!fcOneTemplateA(infspeed))break;
                            }
                        }
                        if (fA(duelinvitationxserver)) {
                          //  goto end;
                        }
                    }
                    if (fA(duelinvitationxserver)) {
                       // goto end;
                    }
                    if (fA(victorytower)) {
                        while (fcA(victorytower));
                        while (fcA(tapblank));
                    }
                    if (fA(defeattower)) {
                        while (fcA(defeattower));
                        retry = false;
                    }

                }

            }
            if (enemyPower.nb < myPower.nb && !onlysweep && retry) {
                std::cout << "Same power enemies" << std::endl;
                fcOneTemplateMultipleTemplateA(challengexserver, S, 0);
                std::vector<std::string> outcome = { victorytower,defeattower };
                while (!fOneTemplateA(outcome)) {
                    std::vector<std::string>speed = { speedone,speedtwo,speedthree };
                    std::vector<std::string>infspeed = { speedone,speedtwo };
                    while (fOneTemplateA(speed)) {
                        while (!fA(speedthree)) {
                            if (!fcOneTemplateA(infspeed))break;
                        }
                    }
                    if (fA(duelinvitationxserver)) {
                       // goto end;
                    }
                }
                if (fA(duelinvitationxserver)) {
                   // goto end;
                }
                if (fA(victorytower)) {
                    while (fcA(victorytower));
                    while (fcA(tapblank));
                }
                if (fA(defeattower)) {
                    while (fcA(defeattower));
                    onlysweep = true;
                }
            }
            else {
                while (!fA(bottomxserver)) {
                    gSwipe(0.5 * dimX, 0.6 * dimY, 0.5 * dimX, 0.3 * dimY, 500);
                }
                if (!fcA(battlexotherworld)) {
                    fcA(battleotherworld);
                    while (fcA(tapblank));
                  //  goto end;
                }
                else {
                    while (fcA(tapblank));
                }
            }

        }
   // end:
    }

    catch (RebootException e) {
        return false;
    }
}

Pack::Pack(): Activity()
{
	name="PACK";
}

bool Pack::doActivity()
{
    try {
        while (!fcA(pack));
        while (fcA(pack));
        std::vector<std::string>rcmd = { crcmd,ncrcmd };
        while (!fA(dailyspecial)) {
            while (!fcOneTemplateA(rcmd));
        }
        while (!fcOneTemplateMultipleTemplateA(viewrcmd, E, 0));
        while (fcOneTemplateMultipleTemplateA(viewrcmd, E, 0));
        std::vector<std::string>freespecialtile = { freedailyspecial,soldoutdailyspecial };
        while (!fcOneTemplateA(freespecialtile))
            while (!fA(dailyspecial)) {
                click(0.5 * dimX, 0.99 * dimY);
            }
        std::vector<std::string>flashsales = { cflashsales,ncflashsales };
        while (!fA(specialoffers)) {
            click(0.5 * dimX, 0.99 * dimY);
            while (!fcOneTemplateA(flashsales));
        }
        fcA(specialoffers);
        while (fcA(freepack)) {
            while (!fcA(tapblank));
            while (fcA(tapblank));
        }
        fcA(weeklypack);
        while (fcA(freepack)) {
            while (!fcA(tapblank));
            while (fcA(tapblank));
        }
    }
    catch (RebootException e) {
        return false;
    }
}

Perks::Perks(): Activity()
{
	name="PERKS";
}

bool Perks::doActivity()
{
    try {
        while (!fcA(perks));
        while (fcA(perks));
        while (!fA(cdailyperks));
        while (fcA(claimperks));
        while (fcA(rewardtreasureperks));
        while (fcA(tapblank));

        while (!fcA(cweeklyperks));
        while (fcA(claimperks));
        while (fcA(rewardtreasureperks));
        while (fcA(tapblank));
    }
    catch (RebootException e) {
        return false;
    }
}

Ranking::Ranking(): Activity()
{
    name="RANKING";
}

bool Ranking::doActivity()
{
    try {
        while (!fA(ranking)) {
            gSwipe(0.8 * dimX, 0.5 * dimY, 0.3 * dimX, 0.5 * dimY, 1000);
        }
        while (!fcA(brranking)) {
            fcA(ranking);
        }
        wait(1000);
        while (fcCompareA(likeranking, clikeranking));
    }
    catch (RebootException e) {
        return false;
    }
}

Ressources::Ressources():Activity()
{
	name="RESSOURCES";
}

bool Ressources::doActivity()
{
    try {
        while (fcA(cultivationicon))while (fcA(confirmcultivation));;
        while (fcA(ressourcesicon)) {
            while (!fcA(claimedressources)) {
                fcA(claimexploration);
            }
        }
    }
    catch (RebootException e) {
        return false;
    }
}

goHomeFirstBoot::goHomeFirstBoot()
{
    name="GOHOMEFIRSTBOOT";
    automatic = true;
}

bool goHomeFirstBoot::doActivity()
{
    try {
        std::cout << "First Boot of Bot and detected you're on package trying to goHome\n";
        std::cout << "Will restart if impossible\n";
        int MAXSTEPS = 7;
        std::vector<std::string> menuelements = { ctown };
        std::vector<std::string>goback{ back,start,notice,loadingpage };
        while (MAXSTEPS >= 0 && !fOneTemplateA(menuelements)) {
            if (!fcOneTemplateA(goback))MAXSTEPS--;
        }
        return MAXSTEPS >= 0;
    }
    catch (RebootException e) {
        return false;
    }
}

goHome::goHome()
{
	name="GOHOME";
	automatic = true;
}

bool goHome::doActivity()
{
    try {
        std::vector<std::string>home{ back,start,notice,loadingpage };
        while (!fA(ctown)) {
            fcOneTemplateA(home);
        }
    }
    catch (RebootException e) {
        return false;
    }
}

bool launchGame::doActivity()
{
    try {
        std::vector<std::string>launch{ back,start,notice,loadingpage };
        while (!fA(ctown)) {
            fcOneTemplateA(launch);
        }
    }
    catch (RebootException& e) {
        return false;
    }
}

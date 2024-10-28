#include "fsfml.h"


fsfml::fsfml()
{
	initVariables();
}

fsfml::~fsfml()
{
	for (std::map<std::string, sf::Texture*>::iterator it = textures.begin(); it != textures.end(); ++it) {
		delete (it->second);
	}
	delete window;
}

void fsfml::startSFML()
{
	GetRelativeResolution(screenWidth, screenHeight);
	initGraphics();
	initWindow();
	windowLoop();
}

void fsfml::initWindow() {
	window = new sf::RenderWindow(sf::VideoMode(0, 0), "Window", sf::Style::None);
	window->setKeyRepeatEnabled(true);
	window->setPosition(sf::Vector2i(0, (screenHeight / 4)));
	window->setVerticalSyncEnabled(true);
}

void fsfml::windowLoop()
{
	collectText = false;
	initAllPanels();
	seePanel(Menu);
	//window->close();
	while (window->isOpen())
	{
		window->requestFocus();
		//mousePos = getMousePosition(window);
		eventLoop();
		//clearWindow(sf::Color::Cyan);
		//handleActivePanel();
		//displayWindow();
		fillTextTyped();

		resetEventVariables();
	}
}

void fsfml::eventLoop()
{
	sf::Event event;
	while (window->pollEvent(event))
	{
		switch (event.type)
		{
		case sf::Event::Closed:
			closeWindow = true;
			break;
		case sf::Event::MouseButtonReleased:
			switch (event.mouseButton.button) {
			case sf::Mouse::Left:
				clickPos = mousePos;
				showVector(convertf(clickPos));
				hasPressedClick = false;
				hasClicked = true;
				break;
			}
			break;
		case sf::Event::MouseButtonPressed:
			switch (event.mouseButton.button) {
			case sf::Mouse::Left:
				clickPos = mousePos;
				showVector(convertf(clickPos));
				hasPressedClick = true;
				break;
			}
			break;
		case sf::Event::KeyReleased:
			switch (event.key.code) {
			case sf::Keyboard::A:
				break;
			}
			break;
		case sf::Event::KeyPressed:
			break;
		case sf::Event::MouseMoved:
			mouseMoved = true;
			break;
		case sf::Event::MouseEntered:
			mouseInWindow = true;
			break;
		case sf::Event::MouseLeft:
			mouseInWindow = false;
			break;
		case sf::Event::TextEntered:
			if (event.text.unicode < 128) {
				charEntered = static_cast<char>(event.text.unicode);
				charTyped = true;
				std::cout << "SFML PART" << charEntered << "\n";
			}
			break;
		case sf::Event::Resized:
			size = convertf(window->getSize());
			view.setCenter(size / 2.f);
			view.setSize(size);
			window->setSize(convertu(size));
			window->setView(view);
			windowResized = true;
			break;
		default:
			break;
		}
	}

}

void fsfml::clearWindow(sf::Color color)
{
	window->clear(color);
}

void fsfml::displayWindow()
{
	window->display();
}

void fsfml::loadTexture(const std::string& nameImg)
{
	sf::Texture* texture = new sf::Texture();
	texture->loadFromFile(nameImg);
	texture->setSmooth(true);
	addTexture(nameImg, texture);
}

void fsfml::loadSprite(const std::string& nameImg)
{
	sf::Sprite sprite(*getTexture(nameImg));
	addSprite(nameImg, sprite);
}

void fsfml::updateElement(Element& element)
{
	element.setSize();
	element.setArea();
}

void fsfml::loadAllSprites(const std::vector<std::string>& allPaths)
{
	for (const auto path : allPaths) {
		loadSprite(path);
	}
}

Element fsfml::initElement(const std::string& nameImg)
{
	Element element(getSprite(nameImg));
	return element;
}

void fsfml::seeElement(Element& element)
{
	element.setVisible(true);
}
void fsfml::unseeElement(Element& element)
{
	element.setVisible(false);
}

void fsfml::unseePanel(Panel& page)
{
	for (auto element : page.elements) {
		unseeElement(element);
	}
}
void fsfml::seePanel(Panel& page)
{
	for (Element& element : page.elements) {
		seeElement(element);
	}
	setActivePanel(page);
}

void fsfml::drawActivePanel()
{
	drawPanel(activePanel);

}

void fsfml::handleActivePanel()
{
	PanelEvent(activePanel);
	drawActivePanel();
}

void fsfml::PanelEvent(Panel& page)
{
	autoResize(page.elements);
	autoPadding(page.elements);
	autoSeparate(page.elements);
	for (Element& element : page.elements) {
		ElementEvent(element);
	}

}

void fsfml::ElementEvent(Element& element)
{
	updateElement(element);
	getElementState(element);
	changeColorElement(element);
}

void fsfml::initElementsPanel(std::vector<Element>allElements, Panel& page)
{
	for (Element& element : allElements) {
		addElement(element, page);
	}
}

void fsfml::initElementsPanel(std::vector<std::string>allElementstr, Panel& page)
{
	std::vector<Element>allElements;
	for (std::string& element : allElementstr) {
		allElements.push_back(initElement(element));
	}
	initElementsPanel(allElements, page);
}

void fsfml::initPanel(Panel& page, std::string id, std::vector<std::string>& allstr)
{
	page.id = id;
	initElementsPanel(allstr, page);
	seePanel(page);
}

void fsfml::initMenu()
{
	std::vector<std::string> allstr = {};
	std::string id = "Menu";
	initPanel(Menu, id, allstr);
}

void fsfml::initAllPanels()
{
	initMenu();
}


void fsfml::loadAllTextures(const std::vector<std::string>& allPaths)
{
	for (const auto path : allPaths) {
		loadTexture(path);
	}
}

void fsfml::initGraphics()
{
	std::vector<std::string> allpaths;
	loadAllTextures(allpaths);
	loadAllSprites(allpaths);
}

void fsfml::add(std::vector<std::string>& vect, std::string str)
{
	vect.push_back(str);
}

void fsfml::setActivePanel(Panel& page)
{
	activePanel = page;
}


void fsfml::createTexture(std::string nameTexture, int width, int height)
{
	sf::Texture* texture = nullptr;
	texture->create(width, height);
	addTexture(nameTexture, texture);
}

void fsfml::drawSprite(sf::Sprite& sprite) {
	window->draw(sprite);
}

void fsfml::drawElement(Element& element)
{
	if (element.isVisible)drawSprite(element.sprite);
}

void fsfml::drawPanel(Panel& page)
{
	for (Element& element : page.elements) {
		drawElement(element);
	}
}

void fsfml::drawAllSprites(std::vector<sf::Sprite>& allSprites)
{
	for (sf::Sprite& sprite : allSprites) {
		drawSprite(sprite);
	}
}



void fsfml::setSpriteColor(sf::Sprite& sprite, const sf::Color& color) {
	sprite.setColor(color);
}

void fsfml::updateAllElements(Panel& panel)
{
	for (Element& element : panel.elements) {
		updateElement(element);
	}
}



void fsfml::moveSprite(sf::Sprite& sprite, const sf::Vector2f& vect) {
	sprite.move(vect);
}

void fsfml::autoSeparate(std::vector<Element>& elements)
{
	for (int i = 0; i < elements.size() - 1; ++i) {
		separateElementX(elements.at(i), elements.at(i + 1));
		separateElementY(elements.at(i), elements.at(i + 1));
	}

}

void fsfml::separateElementX(Element& unmovable, Element& movable)
{
	sf::Vector2f distance = { (float)distanceBorderElementX(unmovable,movable),0 };
	moveElement(movable, distance);
}


void fsfml::separateElementY(Element& unmovable, Element& movable)
{
	sf::Vector2f distance = { 0, (float)distanceBorderElementY(unmovable,movable) };
	moveElement(movable, distance);
}

void fsfml::addPadding(Element& elem, Padding padding)
{
	elem.setPadding(padding);
}

void fsfml::addPadding(std::vector<Element>& allElem, Padding padding)
{
	for (Element& elem : allElem) {
		addPadding(elem, padding);
	}
}

int fsfml::distanceBorderElementY(Element& elem1, Element& elem2)
{
	int borderbottomelem1 = elem1.bottomRight.y;
	int bordertopelem2 = elem2.topLeft.y;
	int distance = bordertopelem2 - borderbottomelem1;
	if (distance >= 0)return 0;
	else return abs(distance);
}

int fsfml::distanceBorderElementX(Element& elem1, Element& elem2)
{
	int borderrightelem1 = elem1.bottomRight.x;
	int borderleftelem2 = elem2.topLeft.x;
	int distance = borderleftelem2 - borderrightelem1;
	if (distance >= 0)return 0;
	else return abs(distance);
}

void fsfml::moveElement(Element& element, const sf::Vector2f& vect)
{
	moveSprite(element.sprite, vect);
	updateElement(element);
}

void fsfml::rotateSprite(sf::Sprite& sprite, float angle) {
	sprite.rotate(angle);
}

void fsfml::setRelSpriteScale(sf::Sprite& sprite, const sf::Vector2f& vect) {
	sprite.scale(vect);
}

void fsfml::setAbsSpriteScale(sf::Sprite& sprite, const sf::Vector2f& vect) {
	sprite.setScale(vect);
}

void fsfml::setSpritePos(sf::Sprite& sprite, const sf::Vector2f& vect)
{
	sprite.setPosition(vect);
}

void fsfml::setElementPos(Element& element, const sf::Vector2f& vect)
{
	setSpritePos(element.sprite, vect);
	updateElement(element);
}



sf::Vector2u fsfml::getWindowSize()
{
	return window->getSize();
}


int fsfml::getWindowHeight()
{
	return getWindowSize().y;
}



int fsfml::getWindowWidth()
{
	return getWindowSize().x;
}

sf::Vector2i fsfml::getMousePosition()
{
	return sf::Mouse::getPosition();
}
sf::Vector2i fsfml::getMousePosition(sf::RenderWindow* window)
{
	return sf::Mouse::getPosition(*window);
}

sf::Vector2i fsfml::converti(sf::Vector2f v) {
	return sf::Vector2i(v);
}
sf::Vector2i fsfml::converti(sf::Vector2u v) {
	return sf::Vector2i(v);
}
sf::Vector2f fsfml::convertf(sf::Vector2i v) {
	return sf::Vector2f(v);
}
sf::Vector2f fsfml::convertf(sf::Vector2u v) {
	return sf::Vector2f(v);
}
sf::Vector2u fsfml::convertu(sf::Vector2i v) {
	return sf::Vector2u(v);
}
sf::Vector2u fsfml::convertu(sf::Vector2f v) {
	return sf::Vector2u(v);
}

bool fsfml::OnSprite(sf::Sprite& sprite, sf::Vector2f Direction)
{
	sf::Vector2f topCorner(sprite.getPosition());
	sf::Vector2f bottomCorner(topCorner.x + sprite.getGlobalBounds().width, topCorner.y + sprite.getGlobalBounds().height);
	bool belowtopcorner = topCorner.x <= Direction.x && topCorner.y <= Direction.y;
	bool abovebottomcorner = Direction.x <= bottomCorner.x && Direction.y <= bottomCorner.y;
	return (belowtopcorner && abovebottomcorner);
}

bool fsfml::OnElement(Element& element, sf::Vector2f Direction)
{
	return OnSprite(element.sprite, Direction);
}

void fsfml::resetEventVariables()
{
	if (!collectText)textTyped.clear();
	//charEntered = '\0';
	charTyped = false;
	hasClicked = false;
	mouseInWindow = false;
	windowResized = false;
	mouseMoved = false;
	mousePos = sf::Vector2i();
	if (!hasPressedClick)clickPos = sf::Vector2i();
}

void fsfml::initVariables()
{
	activePanel = Panel();
	sprites.clear();
	textures.clear();
	closeWindow, hasClicked, hasPressedClick = false;
	window = nullptr;
	sizeTextEntered = 0;
	textTyped.clear();
	charEntered = '\0';
	hasClicked = mouseInWindow = mouseMoved = charTyped = collectText = windowResized = false;
}

void fsfml::setLine(std::vector<Element>& allElements)
{
	for (int i = 0; i < allElements.size() - 1; ++i) {
		separateElementX(allElements.at(i), allElements.at(i + 1));
	}
}


void fsfml::setColumn(std::vector<Element>& allElements)
{
	for (int i = 0; i < allElements.size() - 1; ++i) {
		separateElementY(allElements.at(i), allElements.at(i + 1));
	}
}

bool fsfml::changeColorElementHover(Element& element, sf::Color color)
{
	if (element.bstate == Hover) {
		setSpriteColor(element.sprite, color);
		return true;
	}
	else {
		setSpriteColor(element.sprite, sf::Color::White);
		return false;
	}
}


bool fsfml::changeColorElementPressed(Element& element, sf::Color color)
{
	if (element.bstate == Pressed) {
		setSpriteColor(element.sprite, color);
		return true;
	}
	else {
		setSpriteColor(element.sprite, sf::Color::White);
		return false;
	}
}

void fsfml::changeColorElementMouse(Element& element)
{
	if (changeColorElementPressed(element, sf::Color(220, 255, 220)));
	else changeColorElementHover(element, sf::Color(255, 225, 225));
}

void fsfml::changeColorElement(Element& element)
{
	changeColorElementMouse(element);
}

void fsfml::getElementState(Element& element)
{
	if (clickedElement(element))element.setState(ButtonState::Clicked);
	else if (pressedElement(element))element.setState(ButtonState::Pressed);
	else if (OnElement(element, convertf(mousePos)))element.setState(ButtonState::Hover);
	else element.setState(ButtonState::Normal);
}

bool fsfml::clickedElement(Element& element)
{
	if (hasClicked) {
		if (OnElement(element, convertf(mousePos))) {
			return true;
		}
		else return false;
	}
	else return hasClicked;
}
bool fsfml::pressedElement(Element& element)
{
	if (hasPressedClick) {
		if (OnElement(element, convertf(mousePos))) {
			return true;
		}
		else return false;
	}
	else return false;
}

void fsfml::showVector(sf::Vector2f v2) {
	std::cout << "X: " << v2.x << "  Y: " << v2.y << std::endl;
}

int fsfml::getMousePosX()
{
	return getMousePosition().x;
}
int fsfml::getMousePosY()
{
	return getMousePosition().y;
}

bool fsfml::isKeyPressed(sf::Keyboard::Key key)
{
	return sf::Keyboard::isKeyPressed(key);
}

void fsfml::updateWindowSize()
{
	windowWidth = getWindowWidth();
	windowHeight = getWindowHeight();
}

void fsfml::fillTextTyped()
{
	if (collectText) {
		if (charTyped) {
			sizeTextEntered = textTyped.size();
			textTyped.push_back(charEntered);
			std::cout << "Text: " << textTyped << std::endl;
		}
	}
}

void fsfml::addTexture(std::string key, sf::Texture* texture) {
	textures.insert(std::pair<std::string, sf::Texture*>(key, texture));
}
void fsfml::addSprite(std::string key, sf::Sprite& sprite) {
	sprites.insert(std::pair<std::string, sf::Sprite>(key, sprite));
}

void fsfml::addElement(Element& element, Panel& page)
{
	page.elements.push_back(element);
}

void fsfml::autoPadding(Element& element)
{
	sf::Vector2f spritesize = element.sprite.getGlobalBounds().getSize() / 3.f;
	element.setPadding(Padding(spritesize.x, spritesize.y));
}
void fsfml::autoPadding(std::vector<Element>& elements)
{
	for (Element& element : elements) {
		autoPadding(element);
	}
}


sf::Texture* fsfml::getTexture(std::string key) {
	if (textures.find(key) != textures.end())return textures.at(key);
	else return nullptr;
}

sf::Sprite fsfml::getSprite(const std::string& key) {

	if (sprites.find(key) != sprites.end())return sprites.at(key);
	else return sf::Sprite();
}

sf::Vector2f fsfml::getScale(Element& element)
{
	return getScale(element.sprite);
}

sf::Vector2f fsfml::getScale(sf::Sprite& sprite)
{
	return sprite.getScale();
}

void fsfml::resizeSprite(sf::Sprite& sprite, sf::Vector2f vect)
{
	sprite.setScale(vect);
}

void fsfml::resizeElement(Element& element, sf::Vector2f vect)
{
	resizeSprite(element.sprite, vect);
}

void fsfml::autoResize(Element& element)
{
	float ratioWidth, ratioHeight;
	do {
		ratioWidth = element.width / windowWidth;
		ratioHeight = element.height / windowHeight;
		if (ratioWidth > 0.3 || ratioHeight > 0.3) {
			sf::Vector2f currentScale = getScale(element);
			resizeElement(element, sf::Vector2f(currentScale.x * 0.9, currentScale.y * 0.9));
		}
	} while (ratioWidth > 0.3 || ratioHeight > 0.3);
}

void fsfml::autoResize(std::vector<Element>& allElements)
{
	for (Element& element : allElements) {
		autoResize(element);
	}
}



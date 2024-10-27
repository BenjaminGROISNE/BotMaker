#ifndef FSFML_H
#define FSFML_H

#include <SFML/Graphics.hpp>
#include <SFML/System.hpp>
#include <SFML/Window.hpp>
#include <SFML/Config.hpp>
#include <SFML/Main.hpp>
#include <SFML/Network.hpp>
#include <iostream>
#include "sysCommands.h"

enum ButtonState { Normal, Hover, Pressed, Clicked };

struct Padding {
	float top;
	float right;
	float bottom;
	float left;

	Padding() {
		top = right = bottom = left = 0;
	}
	Padding(float top, float bottom, float right, float left) {
		this->top = top;
		this->right = right;
		this->bottom = bottom;
		this->left = left;
	}
	Padding(float all) {
		top = right = bottom = left = all;
	}
	Padding(float horizontal, float vertical) {
		top = bottom = vertical;
		right = left = horizontal;
	}
};

struct Element {
	sf::Sprite sprite;
	bool canInteract;
	bool isVisible;
	std::string label;
	sf::Vector2f topLeft, bottomRight;
	float width, height;
	ButtonState bstate;
	Padding padding;
	Element() {
		label.clear();
		sprite = sf::Sprite();
		canInteract = isVisible = false;
		bstate = Normal;
		padding = Padding();
		topLeft = bottomRight = sf::Vector2f();
		setSize();
		setArea();
	}
	Element(sf::Sprite sprite) {
		label.clear();
		this->sprite = sprite;
		canInteract = isVisible = false;
		bstate = Normal;
		padding = Padding();
		topLeft = bottomRight = sf::Vector2f();
		setSize();
		setArea();
	}
	Element(sf::Sprite& sprite, bool canInteract, bool visible) {
		label.clear();
		this->sprite = sprite;
		this->canInteract = canInteract;
		this->isVisible = visible;
		bstate = Normal;
		padding = Padding();
		topLeft = bottomRight = sf::Vector2f();
		setSize();
		setArea();
	}
	void setLabel(const std::string& label) {
		this->label = label;
	}
	void setVisible(bool visible) {
		isVisible = visible;
	}
	void setInteract(bool interact) {
		this->canInteract = interact;
	}
	void setSprite(sf::Sprite sprite) {
		this->sprite = sprite;
	}
	void setSize() {
		sf::Vector2f size = sprite.getGlobalBounds().getSize();
		this->width = size.x;
		this->height = size.y;
	}
	void setState(ButtonState bstate) {
		this->bstate = bstate;
	}
	void setPadding(Padding padding) {
		this->padding = padding;
	}
	void setPadding(float top, float bottom, float right, float left) {
		this->padding = Padding(top, bottom, right, left);
	}
	void setPadding(float vertical, float horizontal) {
		this->padding = Padding(vertical, horizontal);
	}
	void setArea() {
		sf::Vector2f Direction = sprite.getGlobalBounds().getPosition();
		topLeft = sf::Vector2f(Direction.x - padding.left, Direction.y - padding.top);
		bottomRight = sf::Vector2f(Direction.x + width + padding.right, Direction.y + height + padding.bottom);
	}
};

struct Line {
	Padding padding;
	float height, width;
	sf::Vector2f topRight, bottomLeft;
	std::vector<Element> elements;
	Line() {
		padding = Padding();
		elements.clear();
		height = width = 0;
	}
};
struct Panel {
	std::string id;
	std::vector<Element> elements;
	std::vector<Line> lines;
	Panel() {
		id.clear();
		elements.clear();
	}
	Panel(std::vector<Element> elem) {
		elements = elem;
	}
	Panel(std::vector<Element> elem, std::string id) {
		this->id = id;
		elements = elem;
	}
	Panel(std::vector<Element> elem, std::string id, bool shown) {
		this->id = id;
		elements = elem;
	}
};

class fsfml
{
public:
	fsfml();
	~fsfml();
	void startSFML();
	void initWindow();
	void windowLoop();
	void eventLoop();
	void clearWindow(sf::Color color);
	void displayWindow();
	sf::Vector2u getWindowSize();
	int getWindowHeight();
	int getWindowWidth();
	void resetEventVariables();
	void initVariables();

	void initPanel(Panel& page, std::string id, std::vector<std::string>& allstr);
	void initMenu();
	void initAllPanels();

	void createTexture(std::string nameTexture, int width, int height);
	void addTexture(std::string key, sf::Texture* texture);
	void addSprite(std::string key, sf::Sprite& sprite);
	void addElement(Element& element, Panel& page);
	void autoPadding(Element& element);
	void autoPadding(std::vector<Element>& element);
	sf::Sprite getSprite(const std::string& path);
	sf::Vector2f getScale(Element& element);
	sf::Vector2f getScale(sf::Sprite& sprite);
	void resizeSprite(sf::Sprite& sprite, sf::Vector2f vect);
	void resizeElement(Element& element, sf::Vector2f vect);
	void autoResize(Element& element);
	void autoResize(std::vector<Element>& allElements);


	void loadSprite(const std::string& nameImg);
	void updateElement(Element& element);
	void loadAllSprites(const std::vector<std::string>& allPaths);
	Element initElement(const std::string& nameImg);
	void seeElement(Element& element);
	void unseeElement(Element& element);
	void seePanel(Panel& page);
	void unseePanel(Panel& page);
	void drawActivePanel();
	void handleActivePanel();
	void PanelEvent(Panel& page);
	void ElementEvent(Element& element);

	void initElementsPanel(std::vector<Element> allElements, Panel& page);
	void initElementsPanel(std::vector<std::string> allElements, Panel& page);

	sf::Texture* getTexture(std::string key);
	void loadTexture(const std::string& path);
	void loadAllTextures(const std::vector<std::string>& allPaths);
	void initGraphics();
	void add(std::vector<std::string>& vect, std::string str);
	void setActivePanel(Panel& page);


	void moveSprite(sf::Sprite& sprite, const sf::Vector2f& vect);
	void autoSeparate(std::vector<Element>& element);
	void separateElementX(Element& elem1, Element& elem2);
	int distanceBorderElementX(Element& elem1, Element& elem2);
	void separateElementY(Element& elem1, Element& elem2);
	void addPadding(Element& elem, Padding padding);
	void addPadding(std::vector<Element>& allElem, Padding padding);
	int distanceBorderElementY(Element& elem1, Element& elem2);
	void moveElement(Element& element, const sf::Vector2f& vect);
	void rotateSprite(sf::Sprite& sprite, float angle);
	void setRelSpriteScale(sf::Sprite& sprite, const sf::Vector2f& vect);
	void setAbsSpriteScale(sf::Sprite& sprite, const sf::Vector2f& vect);
	void setSpritePos(sf::Sprite& sprite, const sf::Vector2f& vect);
	void setElementPos(Element& element, const sf::Vector2f& vect);
	void drawSprite(sf::Sprite& sprite);
	void drawElement(Element& element);
	void drawPanel(Panel& page);
	void drawAllSprites(std::vector<sf::Sprite>& allSprites);
	void setSpriteColor(sf::Sprite& sprite, const sf::Color& color);
	void updateAllElements(Panel& panel);
	void setPanelLayout(Panel& page);
	void setLine(std::vector<Element>& allElements);
	void setColumn(std::vector<Element>& allElements);
	bool changeColorElementHover(Element& element, sf::Color color);
	bool changeColorElementPressed(Element& element, sf::Color color);
	void changeColorElementMouse(Element& elemen);
	void changeColorElement(Element& element);
	void getElementState(Element& element);
	bool clickedElement(Element& element);
	bool pressedElement(Element& element);
	sf::Vector2i getMousePosition();
	sf::Vector2i getMousePosition(sf::RenderWindow* window);
	sf::Vector2i converti(sf::Vector2f v);
	sf::Vector2i converti(sf::Vector2u v);
	sf::Vector2f convertf(sf::Vector2i v);
	sf::Vector2f convertf(sf::Vector2u v);
	sf::Vector2u convertu(sf::Vector2i v);
	sf::Vector2u convertu(sf::Vector2f v);


	bool OnSprite(sf::Sprite& sprite, sf::Vector2f Direction);
	bool OnElement(Element& element, sf::Vector2f Direction);
	int getMousePosX();
	int getMousePosY();
	void showVector(sf::Vector2f v2);
	static bool isKeyPressed(sf::Keyboard::Key key);
	void updateWindowSize();
	void fillTextTyped();
	sf::RenderWindow* window;
	sf::Vector2f size;
	sf::View view;
	int screenWidth, screenHeight, windowWidth, windowHeight;
	sf::Vector2i clickPos, mousePos;
	std::string textTyped;
	int sizeTextEntered;
	char charEntered;
	bool hasClicked, mouseInWindow, mouseMoved, charTyped, collectText, windowResized, closeWindow, hasPressedClick;
	std::map<std::string, sf::Texture*> textures;
	std::map<std::string, sf::Sprite> sprites;
	Panel activePanel;

	//Panels;
	Panel Menu;



};
#endif


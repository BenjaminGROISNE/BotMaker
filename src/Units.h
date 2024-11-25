#pragma once

#include <iostream>
#include <string>
#include <list>
#include <vector>
#include <algorithm>
#include <opencv2/opencv.hpp>

enum class Direction { N, S, W, E, NW, NE, SW, SE };
enum state { disconnect, block, waitTemplate };
enum Orders { noOrd, K, M, B, T };
enum day { Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday };
enum Action { Click, Swipe };
enum typeMat { Color, Gray, notype };

struct RebootException : public std::exception {};
struct endBotException : public std::exception {};

struct Coord {
	int x, y;
	Coord() {
		x = y = 0;
	}
	Coord(const Coord& Co) {
		*this = Co;
	}
	Coord(int x1, int y1) {
		x = x1;
		y = y1;
	}
	Coord(double x1, double y1) {
		x = (int)x1;
		y = (int)y1;
	}
	Coord(int x1, double y1) {
		x = x1;
		y = (int)y1;
	}
	Coord(double x1, int y1) {
		x = (int)x1;
		y = y1;
	}
	void showCoords() {
		std::cout << "{X: " << x << " Y: " << y << "}" << std::endl;
	}
	bool isNull() {
		return x == 0 && y == 0;
	}
	bool operator==(const Coord& other) const {
		return x == other.x && y == other.y;
	}
	bool operator!=(const Coord& other) const {
		return x!=other.x || y!=other.y;
	}
	Coord& operator=(const Coord& other) {
		x = other.x;
		y = other.y;
		return *this;
	}

	Coord operator+(const Coord& other) const {
		return Coord(x + other.x, y + other.y);
	}

	Coord operator-(const Coord& other) const {
		return Coord(x - other.x, y - other.y);
	}

	Coord operator*(const Coord& other) const {
		return Coord(x * other.x, y * other.y);
	}
};

struct Zone {
	Coord C1, C2;
	int width, height, right, left, top, bottom;
	Zone() {
		C1 = C2 = Coord();
		width = height = right = left = top = bottom = 0;
	}
	Zone(const Coord& c1, const Coord& c2) {
		C1 = c1;
		C2 = c2;
		width = abs(C2.x - C1.x);
		height = abs(C2.y - C1.y);
		right = C2.x;
		left = C1.x;
		top = C1.y;
		bottom = C2.y;
	}
	Zone(const Zone& Z) {
		*this = Z;
	}
	void showZone() {
		std::cout << "C1";
		C1.showCoords();
		std::cout << "C2";
		C2.showCoords();
	}
	bool operator==(const Zone& other) const {
		return C1 == other.C1 && C2 == other.C2;
	}
	bool operator!=(const Zone& other) const {
		return C1 != other.C1 || C2 != other.C2;
	}

	Zone& operator=(const Zone& other) {
		C1 = other.C1;
		C2 = other.C2;
		width = other.width;
		height = other.height;
		right = other.right;
		left = other.left;
		top = other.top;
		bottom = other.bottom;
		return *this;
	}

	Zone operator+(const Zone& other) const {
		return Zone(C1 + other.C1, C2 + other.C2);
	}

	Zone operator-(const Zone& other) const {
		return Zone(C1 - other.C1, C2 - other.C2);
	}

	bool isNull() {
		return C1.isNull() && C2.isNull();
	}
};

struct Template {
	Coord C;
	cv::Mat mat, graymat;
	typeMat type;
	std::string id;
	float score;
	int width, height;
	Template() {
		score = 0;
		C = Coord();
		id.clear();
		type = notype;
		width = height = 0;
		mat = graymat = cv::Mat();
	}
	Template(const std::string namemat) {
		*this = Template();
		id = namemat;
		mat = cv::imread(id, cv::IMREAD_COLOR);
		graymat = cv::imread(id, cv::IMREAD_GRAYSCALE);
		width = mat.cols;
		height = mat.rows;

	}
	Template(const std::string id, typeMat type) {
		*this = Template(id);
		this->type = type;
	}
	Template(const Template& other) {
		C = other.C;
		graymat = other.graymat;
		mat = other.mat;
		id = other.id;
		width = other.width;
		height = other.height;
		type = other.type;
		score = other.score;
	}
	void setType(typeMat type) {
		this->type = type;
	}
};

struct Digit {
	int dig;
	Coord Cnb;
	Digit() {
		dig = 0;
		Cnb.x = Cnb.y = 0;
	}
};

struct Unit {
	std::string nameunit;
	Orders Ord;
	Unit() {
		nameunit.clear();
		Ord = Orders::noOrd;
	}
	Unit(std::string name, Orders ord) {
		nameunit = name;
		Ord = ord;
	}
};

struct Number {
	Unit U;
	long double nb;
	bool integer, decimal;
	bool T, B, M, K;
	std::vector<Digit> digits;
	int dotposition;
	Number() {
		nb = dotposition = 0;
		T = B = M = K = integer = decimal = false;
		digits.clear();
	}
};

struct CustomNumber {
	Number number;
	int Direction;
	CustomNumber() {
		number.digits.clear();
		Direction = 0;
	}
};

struct Popup : Template {
	state action;
	Popup() :Template() {
		action = state::waitTemplate;
	}
	Popup(Template T) : Template(T) {
		action = state::waitTemplate;
	}
	Popup(std::string pop) :Template(pop) {
		action = state::waitTemplate;
	}
	Popup(std::string pop, state c) :Template(pop) {
		action = c;
	}
	Popup(std::string pop, state c, typeMat t) :Template(pop, t) {
		action = c;
	}
};

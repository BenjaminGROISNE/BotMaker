
#ifndef FOPENCV_H
#define FOPENCV_H
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/photo.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/opencv.hpp>
#include <iostream>
#include <vector>
#include <random>
#include <time.h>
#include <stdlib.h>
#include <fstream>
#include "sysCommands.h"
#include "Units.h"

const static double matchTemplatePrecision=0.8;

cv::Mat CreateMat(const std::string& path, typeMat color);
bool intersects(const Template& templ1, const Template& templ2);
bool intersects(const Zone& rect1, const Zone& rect2);
void messageFound(const Template& templ, bool found);
void messageFound(const std::vector<Template>& Tresult, bool found);
bool cvmtfTemplate(const Template& tempimg, const Template& backgroundimage, Template& Tresult, Zone Z = Zone());
bool cvmtfMultipleTemplate(const Template& tempimg, const Template& background, std::vector<Template>& Tresult, Zone Z = Zone());
bool cvmtfMultipleTemplate(const std::vector<Template>& allTempl, const Template& background, std::vector<Template>& Tresult, Zone Z = Zone());

bool cvmtfOneTemplate(const std::vector<Template>& allTempl, const Template& background, Template& Tresult, Zone Z = Zone());
bool cvmtfCompareOneTemplate(const std::vector<Template>& allTempl, const Template& background, const std::vector<Template>& similartemplates, Template& Tresult, Zone Z = Zone());
bool cvmtfOneMultipleTemplate(const std::vector<std::vector<Template>>& allTempl, const Template& background, std::vector<Template>& Tresult, Zone Z = Zone());
bool cvmtfAllTemplate(const std::vector<Template>& allTempl, const Template& background, std::vector<Template>& Tresult, Zone Z = Zone());
bool cvmtfCompareTemplate(const Template& goodTemplate, const Template& background, const Template& similartemplate, Template& Tresult, Zone Z = Zone());
bool cvmtfCompareMultipleTemplate(const Template& goodTemplate, const Template& background, const std::vector<Template>& similartemplates, Template& Tresult, Zone Z = Zone());
bool cvmtfMultipleTemplateCompareMultipleTemplate(const Template& goodTemplate, const Template& background, const std::vector<Template>& similartemplates, std::vector<Template>& Tresult, Zone Z = Zone());
bool cvmtfMultipleTemplateCompareMultipleTemplate(const std::vector<Template>& goodTemplate, const Template& background, const std::vector<Template>& similartemplates, std::vector<Template>& Tresult, Zone Z = Zone());
bool multipleMatchTemplate(const Template& templateImg, const Template& background, std::vector<Template>& Tresult);
bool oneMatchTemplate(const Template& templateImg, const Template& background, Template& Tresult);
Template extractZone(const Template& image, Zone Z);


cv::Vec3b getPixel(cv::Mat img, int x, int y);
void showImg(std::string path);
void showMat(const cv::Mat& img);
cv::Mat DetectContours(cv::Mat img);
void getContours(std::string pathImg);
void showImgtest(std::string path);
void performCannyEdgeDetectionFromLiveCamera();
cv::Mat performCannyEdgeDetectionFromPicture(const std::string& imagePath);
void CannyEdgeVideo(std::string videoPath);
void SaveCannyEdgeVideo(const std::string& videoPath, const std::string& outputPath);

#endif

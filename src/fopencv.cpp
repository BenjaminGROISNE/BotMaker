#include "fopencv.h"



cv::Vec3b getPixel(cv::Mat img, int x, int y) {
    return img.at<cv::Vec3b>(y, x);
}

cv::Mat CreateMat(const std::string& imagePath, typeMat color = Color) {
    cv::Mat image;
    switch (color) {
    case Color:
        image = cv::imread(imagePath, cv::IMREAD_COLOR);
        break;
    case Gray:
        image = cv::imread(imagePath, cv::IMREAD_GRAYSCALE);
        break;
    }
    if (image.empty()) {
        std::cerr << "Error: Failed to read image from " << imagePath << std::endl;
        return cv::Mat();
    }
    return image;
}

void showImg(std::string path) {
    cv::Mat img = CreateMat(path);
    imshow("img", img);
    int width, height;
    GetRelativeResolution(width, height);
    int x = (width - img.cols) / 2;
    int y = (height - img.rows) / 2;
    cv::moveWindow("img", x, y);
    cv::waitKey(0);
}
void showMat(const cv::Mat& img) {
    imshow("img", img);
    int width, height;
    GetRelativeResolution(width, height);
    int x = (width - img.cols) / 4;
    int y = (height - img.rows) / 4;
    cv::moveWindow("img", x, y);
    cv::waitKey(1000);
    cv::destroyWindow("img");
}

cv::Mat DetectContours(cv::Mat img) {
    cv::Mat imgBlur, imgCanny, imgDil, imgErode, imgRange;
    cv::Scalar lower(50, 0, 100);
    cv::Scalar upper(100, 255, 150);
    inRange(img, lower, upper, imgRange);
    GaussianBlur(imgRange, imgBlur, cv::Size(0, 0), 1, 0);
    Canny(imgBlur, imgCanny, 65, 75);
    cv::Mat kernel = cv::getStructuringElement(cv::MORPH_RECT, cv::Size(3, 3));
    dilate(imgCanny, imgDil, kernel);
    return imgDil;
}

void getContours(std::string pathImg) { //à revoir
    cv::Mat img = CreateMat(pathImg);
    cv::Mat imgDil = DetectContours(img);
    std::vector<std::vector<cv::Point>>contours;
    std::vector<cv::Vec4i>hierarchy;
    findContours(imgDil, contours, hierarchy, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_SIMPLE);
    drawContours(img, contours, -1, cv::Scalar(255, 0, 255), 2);
    imshow("img", img);
}

void showImgtest(std::string path) {
    int hmin = 0, smin = 0, vmin = 0;
    int hmax = 255, smax = 255, vmax = 255;
    cv::Mat img = cv::imread(path, cv::IMREAD_REDUCED_COLOR_2);

    cv::Mat imgHSV, mask;
    cvtColor(img, imgHSV, cv::COLOR_BGR2HSV);
    cv::namedWindow("Track", (640, 200));
    cv::createTrackbar("Hue min", "Track", &hmin, 255);
    cv::createTrackbar("Hue max", "Track", &hmax, 255);
    cv::createTrackbar("sat min", "Track", &smin, 255);
    cv::createTrackbar("sat max", "Track", &smax, 255);
    cv::createTrackbar("Val min", "Track", &vmin, 255);
    cv::createTrackbar("Val max", "Track", &vmax, 255);

    while (true) {
        cv::Scalar lower(0, 0, 213);
        cv::Scalar upper(255, 179, 255);
        inRange(imgHSV, lower, upper, mask);
        imshow("Image", img);
        cv::moveWindow("Image", 0, 0);
        imshow("imagemod1", imgHSV);
        cv::moveWindow("imagemod1", 0, 400);
        imshow("imagemod2", mask);
        cv::moveWindow("imagemod2", 700, 0);
        cv::moveWindow("Track", 700, 400);
        cv::waitKey(1);
    }
}

bool intersects(const Template& templ1, const Template& templ2)
{
    Coord topLeft1 = templ1.C;
    Coord topLeft2 = templ2.C;
    Coord botRight1 = templ1.C + Coord(templ1.width, templ1.height);
    Coord botRight2 = templ2.C + Coord(templ2.width, templ2.height);
    Zone rect1(topLeft1, botRight1);
    Zone rect2(topLeft2, botRight2);
    return intersects(rect1, rect2);
}

bool intersects(const Zone& rect1, const Zone& rect2)
{
    return (rect1.left < rect2.right && rect1.right > rect2.left && rect1.top < rect2.bottom && rect1.bottom > rect2.top);
}

void messageFound(const Template& Tresult, bool found) {
    if (found) {
        std::cout << "FOUND MaxVal: " << Tresult.score << " X: " << Tresult.C.x << " Y: " << Tresult.C.y << " " << Tresult.id << "\n";
    }
    else {
        std::cout << "Not found MaxVal: " << Tresult.score << " " << Tresult.id << "\n";
    }
}

void messageFound(const std::vector<Template>& Tresult, bool found) {
    for (Template templ : Tresult) {
        messageFound(templ, found);
    }
}



Template extractZone(const Template& templ, Zone Z) {
    int newRight = Z.right;
    int newLeft = Z.left;
    int newTop = Z.top;
    int newBottom = Z.bottom;
    if (newRight < templ.mat.cols)newRight++;
    if (newLeft > 0)newLeft--;
    if (newTop > 0)newTop--;
    if (newBottom < templ.mat.rows)newBottom++;
    int newWidth = abs(newRight - newLeft);
    int newHeight = abs(newBottom - newTop);
    cv::Rect roiRect(newLeft, newTop, newWidth, newHeight);
    cv::Mat graymatROI = templ.graymat(roiRect).clone();
    cv::Mat matROI = templ.mat(roiRect).clone();
    Template newTemplate(templ);
    newTemplate.width = graymatROI.cols;
    newTemplate.height = graymatROI.rows;
    newTemplate.graymat = graymatROI;
    newTemplate.mat = matROI;
    return newTemplate;
}

bool cvmtfTemplate(const Template& templateImg, const Template& background, Template& Tresult, Zone Z) {
    Template newback(background);
    if (!Z.isNull()) {
        newback = extractZone(background, Z);
    }
    bool found = oneMatchTemplate(templateImg, newback, Tresult);
    Tresult.C.x += Z.C1.x;
    Tresult.C.y += Z.C1.y;
    if (found) {
        messageFound(Tresult, true);
        return true;
    }
    else {
        messageFound(Tresult, false);
        return false;
    }
}


bool oneMatchTemplate(const Template& templateImg, const Template& background, Template& Tresult) {
    double maxVal = 0;
    cv::Point maxLoc;
    cv::Mat resultMat;
    cv::Point topLeft;
    switch (background.type) {
    case Color:
        cv::matchTemplate(background.mat, templateImg.mat, resultMat, cv::TM_CCOEFF_NORMED);
        cv::minMaxLoc(resultMat, NULL, &maxVal, NULL, &maxLoc);
        topLeft = maxLoc;
        break;

    case Gray:
        cv::matchTemplate(background.graymat, templateImg.graymat, resultMat, cv::TM_CCOEFF_NORMED);
        cv::minMaxLoc(resultMat, NULL, &maxVal, NULL, &maxLoc);
        topLeft = maxLoc;
        break;
    }
    Tresult = templateImg;
    Tresult.C = Coord(topLeft.x, topLeft.y);
    Tresult.score = maxVal;
    return Tresult.score >= matchTemplatePrecision;
}

bool multipleMatchTemplate(const Template& templateImg, const Template& background, std::vector<Template>& Tresult) {
    cv::Mat matresult;
    switch (background.type) {
    case Color:
        cv::matchTemplate(background.mat, templateImg.mat, matresult, cv::TM_CCOEFF_NORMED);
        break;

    case Gray:
        cv::matchTemplate(background.graymat, templateImg.graymat, matresult, cv::TM_CCOEFF_NORMED);
        break;
    }
    int x, y;
    float matchVal = 0;
    bool duplicate;
    Template newResult(templateImg);
    for (y = 0; y < matresult.rows; ++y) {
        for (x = 0; x < matresult.cols; ++x) {
            matchVal = matresult.at<float>(y, x);
            if (matchVal >= matchTemplatePrecision) {
                newResult.C = Coord(x, y);
                newResult.score = matchVal;
                duplicate = false;
                for (int i = 0; i < Tresult.size(); ++i) {
                    if (intersects(Tresult.at(i), newResult)) {
                        if (Tresult.at(i).score < newResult.score) {
                            Tresult.erase(Tresult.begin() + i);
                            break;
                        }
                        else duplicate = true;
                    }
                }
                if (!duplicate)Tresult.push_back(newResult);
            }
        }
    }
    return !Tresult.empty();
}

bool cvmtfMultipleTemplate(const Template& templateImg, const Template& background, std::vector<Template>& Tresult, Zone Z) {
    Tresult.clear();
    Template newback = background;
    if (!Z.isNull()) {
        newback = extractZone(background, Z);
    }
    multipleMatchTemplate(templateImg, newback, Tresult);
    if (Tresult.empty()) {
        return false;
    }
    else {
        for (Template res : Tresult) {
            res.C = res.C + Z.C1;
        }
        return true;
    }
}

bool cvmtfMultipleTemplate(const std::vector<Template>& allTempl, const Template& background, std::vector<Template>& Tresult, Zone Z)
{
    std::vector<Template>result;
    for (const Template& templ : allTempl) {
        cvmtfMultipleTemplate(templ, background, result, Z);
        Tresult.insert(Tresult.end(), result.begin(), result.end());
    }
    return !Tresult.empty();
}


bool cvmtfOneTemplate(const std::vector<Template>& allTempl, const Template& background, Template& Tresult, Zone Z)
{
    for (const Template& templ : allTempl) {
        if (cvmtfTemplate(templ, background, Tresult, Z))return true;
    }
    return false;
}

bool cvmtfCompareOneTemplate(const std::vector<Template>& allTempl, const Template& background, const std::vector<Template>& similartemplates, Template& Tresult, Zone Z)
{
    for (const Template& templ : allTempl) {
        if (cvmtfCompareMultipleTemplate(templ, background, similartemplates, Tresult, Z)) {
            return true;
        }
    }
    return false;
}

bool cvmtfOneMultipleTemplate(const std::vector<std::vector<Template>>& allTempl, const Template& background, std::vector<Template>& Tresult, Zone Z)
{
    for (const std::vector<Template>& templ : allTempl) {
        if (cvmtfAllTemplate(templ, background, Tresult, Z)) {
            messageFound(Tresult, true);
            return true;
        }
    }
    return false;
}

bool cvmtfAllTemplate(const std::vector<Template>& allTempl, const Template& background, std::vector<Template>& Tresult, Zone Z)
{
    Template resultSearch;
    Coord temp;
    for (const Template& templ : allTempl) {
        if (cvmtfTemplate(templ, background, resultSearch, Z)) {
            messageFound(resultSearch, true);
            Tresult.push_back(resultSearch);
        }
        else return false;
    }
    return true;
}

bool cvmtfCompareTemplate(const Template& goodTemplate, const Template& background, const Template& simTemplate, Template& Tresult, Zone Z)
{
    std::vector<Template>resSimTemplate;
    if (cvmtfTemplate(goodTemplate, background, Tresult, Z)) {
        if (cvmtfMultipleTemplate(simTemplate, background, resSimTemplate, Z)) {
            for (const Template& simTempl : resSimTemplate) {
                if (intersects(Tresult, simTempl)) {
                    if (simTempl.score > Tresult.score && simTempl.id != Tresult.id) {
                        messageFound(simTempl, true);
                        return false;
                    }
                }
            }
        }
        messageFound(Tresult, true);
        return true;
    }
    messageFound(Tresult, false);
    return false;

}


bool cvmtfCompareMultipleTemplate(const Template& goodTemplate, const Template& background, const std::vector<Template>& simTemplates, Template& Tresult, Zone Z)
{
    if (cvmtfTemplate(goodTemplate, background, Tresult, Z)) {
        std::vector<Template>resSimTemplates;
        if (cvmtfMultipleTemplate(simTemplates, background, resSimTemplates, Z)) {
            for (const Template& simTempl : resSimTemplates) {
                if (intersects(simTempl, Tresult)) {
                    if (simTempl.score > Tresult.score && simTempl.id != Tresult.id) {
                        messageFound(simTempl, true);
                        return false;
                    }
                }
            }
        }
        messageFound(Tresult, true);
        return true;
    }
    messageFound(Tresult, false);
    return false;
}


bool cvmtfMultipleTemplateCompareMultipleTemplate(const Template& goodTemplate, const Template& background, const std::vector<Template>& simTemplates, std::vector<Template>& Tresult, Zone Z)
{
    std::vector<Template>tabGoodTemplates;
    if (cvmtfMultipleTemplate(goodTemplate, background, tabGoodTemplates, Z)) {
        std::vector<Template>resSimTemplates;
        if (cvmtfMultipleTemplate(simTemplates, background, resSimTemplates, Z)) {
            for (const Template& Templ : tabGoodTemplates) {
                bool overlap = false;
                for (const Template& simTempl : resSimTemplates) {
                    if (intersects(simTempl, Templ)) {
                        if (simTempl.score > Templ.score && simTempl.id != Templ.id) {
                            messageFound(simTempl, true);
                            overlap = true;
                            break;
                        }
                    }
                }
                if (!overlap)Tresult.push_back(Templ);
                messageFound(Templ, true);
            }
        }
        else Tresult.insert(Tresult.end(), tabGoodTemplates.begin(), tabGoodTemplates.end());
    }
    if (Tresult.empty()) {
        messageFound(goodTemplate, false);
        return false;
    }
    else {
        messageFound(Tresult, true);
        return true;
    }
}

bool cvmtfMultipleTemplateCompareMultipleTemplate(const std::vector<Template>& goodTemplate, const Template& background, const std::vector<Template>& simTemplates, std::vector<Template>& Tresult, Zone Z)
{
    std::vector<Template>tabGoodTemplates;
    if (cvmtfMultipleTemplate(goodTemplate, background, tabGoodTemplates, Z)) {
        std::vector<Template>resSimTemplates;
        if (cvmtfMultipleTemplate(simTemplates, background, resSimTemplates, Z)) {
            for (const Template& Templ : tabGoodTemplates) {
                bool overlap = false;
                for (const Template& simTempl : resSimTemplates) {
                    if (intersects(simTempl, Templ)) {
                        if (simTempl.score > Templ.score && simTempl.id != Templ.id) {
                            messageFound(simTempl, true);
                            overlap = true;
                            break;
                        }
                    }
                }
                if (!overlap)Tresult.push_back(Templ);
            }
        }
        else Tresult.insert(Tresult.end(), tabGoodTemplates.begin(), tabGoodTemplates.end());
    }
    if (Tresult.empty()) {
        messageFound(goodTemplate, false);
        return false;
    }
    else {
        messageFound(Tresult, true);
        return true;
    }
}












void CannyEdgeVideo(std::string videoPath) {

    cv::VideoCapture cap(videoPath);
    if (!cap.isOpened()) {
        std::cout << "Error: Unable to open the video file: " << videoPath << std::endl;
        return;
    }

    cv::namedWindow("Edge Image", cv::WINDOW_NORMAL);

    int frameCount = 0; // For debugging purposes, let's count the frames processed

    while (true) {
        cv::Mat frame;
        cap >> frame; // Read a new frame from the video

        if (frame.empty()) {
            std::cout << "End of video!" << std::endl;
            break;
        }

        cv::Mat edges;
        cv::cvtColor(frame, edges, cv::COLOR_BGR2GRAY);
        cv::Canny(edges, edges, 100, 200);

        cv::imshow("Edge Image", edges);

        // Break the loop if the 'q' or 'Esc' key is pressed
        char key = cv::waitKey(25);
        if (key == 'q' || key == 27) {
            std::cout << "Video playback stopped by the user!" << std::endl;
            break;
        }

        // For debugging, print frame count at regular intervals
        if (frameCount % 100 == 0) {
            std::cout << "Processed " << frameCount << " frames." << std::endl;
        }
        frameCount++;
    }

    cap.release();
    cv::destroyAllWindows();
}

void SaveCannyEdgeVideo(const std::string& videoPath, const std::string& outputPath) {
    cv::VideoCapture cap(videoPath);
    if (!cap.isOpened()) {
        std::cout << "Error: Unable to open the video file: " << videoPath << std::endl;
        return;
    }

    // Get video properties to create a video writer
    int frameWidth = static_cast<int>(cap.get(cv::CAP_PROP_FRAME_WIDTH));
    int frameHeight = static_cast<int>(cap.get(cv::CAP_PROP_FRAME_HEIGHT));
    double fps = cap.get(cv::CAP_PROP_FPS);
    int codec = cv::VideoWriter::fourcc('X', '2', '6', '4'); // Change this to the desired codec (e.g., 'X', '2', '6', '4' for H.264)

    cv::VideoWriter writer(outputPath, codec, fps, cv::Size(frameWidth, frameHeight), false);

    if (!writer.isOpened()) {
        std::cout << "Error: Unable to create the video writer!" << std::endl;
        return;
    }


    while (true) {
        cv::Mat frame;
        cap >> frame; // Read a new frame from the video

        if (frame.empty()) {
            std::cout << "End of video!" << std::endl;
            break;
        }

        cv::Mat edges;
        cv::cvtColor(frame, edges, cv::COLOR_BGR2GRAY);
        cv::Canny(edges, edges, 100, 200);

        writer.write(edges);
    }

    cap.release();
    writer.release();
    cv::destroyAllWindows();
}

cv::Mat performCannyEdgeDetectionFromPicture(const std::string& imagePath) {
    // Load the input image from the file path
    cv::Mat inputImage = cv::imread(imagePath);

    // Check if the input image is valid
    if (inputImage.empty()) {
        std::cerr << "Error: Unable to load the image: " << imagePath << std::endl;
        return cv::Mat();
    }

    // Apply Canny edge detection
    cv::Mat edges;
    cv::Canny(inputImage, edges, 100, 200); // You can adjust the threshold values (100 and 200) as needed

    return edges;
}




void performCannyEdgeDetectionFromLiveCamera() {
    cv::VideoCapture cap(0); // Open the default camera (change the index if you have multiple cameras)

    if (!cap.isOpened()) {
        std::cerr << "Error: Unable to access the camera!" << std::endl;
        return;
    }

    cv::Mat frame;
    cv::namedWindow("Live Camera", cv::WINDOW_AUTOSIZE);

    while (true) {
        cap >> frame; // Capture a frame from the camera

        cv::Mat edges;
        cv::Canny(frame, edges, 100, 100); // You can adjust the threshold values (100 and 200) as needed

        cv::imshow("Live Camera", edges);

        char c = cv::waitKey(1);
        if (c == 27) // Press 'Esc' key to exit
            break;
    }

    cap.release();
    cv::destroyAllWindows();
}

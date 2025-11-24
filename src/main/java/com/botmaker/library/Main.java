package com.botmaker.library;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;


public class Main {

    static{
        Loader.load(opencv_java.class);
    }

    public static void main(String[] args) {
        try {
            //testLiveCapture();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
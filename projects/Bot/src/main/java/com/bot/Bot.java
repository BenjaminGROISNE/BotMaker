package com.bot;

public class Bot {
    public static void newMethodd() {
	}

	public static void main(String[] args) {
		float decimald = 0.0f;
		System.out.println(decimald);
		overloadedMethod(decimald);
		
    }

	public static void overloadedMethod(double decimal) {
		System.out.println(decimal);
	}

	public static void overloadedMethod(int number, double decimal) {
	}

	public static void newMethod() {
	}
}

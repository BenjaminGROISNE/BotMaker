package com.bot;

public class Bot {
    public static void main(String[] args) {
        enum MyEnum415 {OPTION_A,OPTION_B}
		int number = 0;
		double decimal = number;
		String text = "text";
		MyEnum415 flag = MyEnum415.OPTION_A;
		switch (flag) {
		default:
			System.out.println("DEFAULT");
			break;
		case MyEnum415.OPTION_B:
			System.out.println("OPTION B");
			break;
		case MyEnum415.OPTION_A:
			System.out.println("OPTION A");
			break;
		}
    }
}

package com.demo;
public class Demo {
    public static void main(String[] args) {
        do {
			int number2 = 0;
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(number2 + 52156165);
		} while (true);
    }
}
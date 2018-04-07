/**
@Author vu9767@kettering.edu
*/
package main;

import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws Exception{
		boolean debug = false;
		boolean planned = true;
		if (args.length == 1 && args[0].equals("-d")) {
			debug = true;
			System.out.println("DEBUG MODE: ON");
		}else {
			System.out.println("DEBUG MODE: OFF");
		}
		Scanner sc = new Scanner(System.in);
		System.out.println("Please enter R for a random path; otherwise, the program will run in planned path:");
		String token = sc.nextLine();
		sc.close();
		if (token.length() == 1) {
			char plan = token.charAt(0);
			planned = plan != 'R';
		}
		System.out.println(String.format("Program is running on %s mode", planned ? "PLAN" : "RANDOM"));
		Environment env = new Environment(Config.SQUARE_MAP_WIDTH, debug);
		Car car = new Car(env);
		if (planned) {
			car.goSmart();
		}else {
			car.goDummy();
		}
	}
}

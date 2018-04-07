/**
@Author vu9767@kettering.edu
*/
package main;

public interface Config {
	public static final int TURN_TIME = 4;
	public static final int GO_STRAIGHT_TIME = 3;
	public static final int SIGNAL_LIGHT_WAIT_TIME = 1;
	public static final int BUMP_TIME = 10;
	public static final int SIGNAL_INTERVAL = 2;
	public static final int ADVENTURE_RISK = TURN_TIME + SIGNAL_LIGHT_WAIT_TIME - GO_STRAIGHT_TIME + 1;
	public static final int SQUARE_MAP_WIDTH = 10;
}

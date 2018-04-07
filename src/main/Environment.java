package main;
import java.util.HashMap;
import java.util.Map;

import main.Directions;
import main.Location;
import main.SignalColors;
import main.InforKeys;
public class Environment {
	private final int MAP_WIDTH;
	private Location carLoc;
	private final Location destination;
	private Directions currentDirection;
	private int uptime;
	private boolean isBumped;
	private boolean debug_on;
	private final String header = "The path of the car: \r\nTime\tCarLocation\tCarDirection\tGreenLightDirection\tMoveChosen";
	private boolean header_printed = false;
	private int num_bumps;
//	constructor
	public Environment(int map_width, boolean debug_on) {
		this.carLoc = new Location(0,0);
		this.MAP_WIDTH = map_width;
		this.currentDirection = Directions.EAST;
		this.uptime = 0;
		this.destination = new Location((int) Math.floor(Math.random() * map_width), (int) Math.floor(Math.random() * map_width));
		this.isBumped = false;
		this.debug_on = debug_on;
		this.num_bumps = 0;
	}
	private enum PrintDataKeys{
		TIME, 
		LOCATION, 
		DIRECTION, 
		GREEN_LIGHT, 
		MOVE
	}
	private void myPrint(Map<PrintDataKeys, String> data) {
		System.out.println("" + data.get(PrintDataKeys.TIME) + 
				"\t" + data.get(PrintDataKeys.LOCATION) +
				"\t"+ data.get(PrintDataKeys.DIRECTION) +
				"\t\t" + data.get(PrintDataKeys.GREEN_LIGHT) +
				"\t\t\t" + data.get(PrintDataKeys.MOVE)
				);
	}
	private String greenLightDirection(Directions dir, boolean green) {
		String ew = "E-W";
		String ns = "N-S";
		if (dir == Directions.EAST || dir == Directions.WEST) {
			return green ? ew : ns;
		}
		return green ? ns : ew;
	}
	private void handleDebug(String moveChosen) {
		if (!this.header_printed) {
			System.out.println(this.header);
			this.header_printed = true;
		}
		
		String location = "x: "+ this.getCarLoc().getX() + " y: " + this.getCarLoc().getY();
		String uptime = "" + this.getUptime();
		String greenDirection = this.greenLightDirection(this.getCurrentDirection(), this.getSignalColorByDir(this.getCurrentDirection()) == SignalColors.GREEN);
		Map<PrintDataKeys, String> data = new HashMap<>();
		data.put(PrintDataKeys.LOCATION, location);
		data.put(PrintDataKeys.TIME, uptime);
		data.put(PrintDataKeys.DIRECTION, this.getCurrentDirection().toString());
		data.put(PrintDataKeys.GREEN_LIGHT, greenDirection);
		data.put(PrintDataKeys.MOVE, moveChosen);
		myPrint(data);
	}
	public boolean willBump(Directions dir) {
		switch(dir) {
			case NORTH:{
				return carLoc.getY() == this.MAP_WIDTH - 1;
			}
			case SOUTH:{
				return carLoc.getY() == 0; 
			}
			case WEST:{
				return carLoc.getX() == 0;
			}
			case EAST:{
				return carLoc.getX() == this.MAP_WIDTH - 1;		
				}
			default: return false;
		}
	}
	private SignalColors getSignalColorByDir(Directions dir) {
		uptime = this.getUptime();
		boolean isGreen = (uptime % Config.SIGNAL_INTERVAL == 0 && (dir == Directions.EAST || dir == Directions.WEST)) || (uptime % Config.SIGNAL_INTERVAL != 0 && (dir == Directions.NORTH || dir == Directions.SOUTH));
		return isGreen ? SignalColors.GREEN : SignalColors.RED;
	}
	private int getUptime() {
		return this.uptime;
	}
	private Location getCarLoc() {
		return this.carLoc;
	}
	private Location getDestination() {
		return this.destination;
	}
	private boolean isBumped() {
		return this.isBumped;
	}
	private boolean reached() {
		return this.getCarLoc().equals(this.getDestination());
	}
	private Directions getCurrentDirection() {
		return this.currentDirection;
	}
	public void goStraight() {
		int new_x = this.getCarLoc().getX();
		int new_y = this.getCarLoc().getY();
		Directions dir = this.getCurrentDirection();
		if (!this.willBump(dir)) {
			switch(dir) {
			case NORTH:{
				new_y++;
				break;
			}
			case SOUTH:{
				new_y--;
				break;
			}
			case WEST:{
				new_x--;
				break;
			}
			case EAST:{
				new_x++;
				break;
			}
			}
			this.isBumped = false;
			this.carLoc.setLoc(new_x, new_y);
			this.uptime += Config.GO_STRAIGHT_TIME;
		}
		else{
			this.isBumped = true;
			this.uptime += Config.BUMP_TIME;
			this.num_bumps++;
		}
		
		if (this.debug_on) {
			handleDebug("Go Straight");			
		}

	}
	public void goLeft() {
		Directions dir;
		int new_x = this.getCarLoc().getX();
		int new_y = this.getCarLoc().getY();
		switch(this.getCurrentDirection()) {
		case NORTH:{
			dir = Directions.WEST;
			if (!this.willBump(dir)) {
				new_x--;
			}
			break;
		}
		case SOUTH:{
			dir = Directions.EAST;
			if (!this.willBump(dir)) {
				new_x++;
			}
			break;
		}
		case WEST:{
			dir = Directions.SOUTH;
			if (!this.willBump(dir)) {
				new_y--;
			}
			break;
		}
		case EAST:{
			dir = Directions.NORTH;
			if (!this.willBump(dir)) {
				new_y++;
			}
			break;
		}
		default: {
			dir = Directions.EAST;
		}
		}
		this.currentDirection = dir;
		if (this.willBump(dir)) {
			this.uptime += Config.BUMP_TIME;
			this.isBumped = true;
			this.num_bumps++;
		}else {
			this.isBumped = false;
			this.carLoc.setLoc(new_x, new_y);
			this.uptime += Config.TURN_TIME;
		}
		if (this.debug_on) {
			handleDebug("Turn Left");
		}
	}
	public void goRight() {
		Directions dir;
		int new_x = this.getCarLoc().getX();
		int new_y = this.getCarLoc().getY();
		switch(this.getCurrentDirection()) {
		case NORTH:{
			dir = Directions.EAST;
			if (!this.willBump(dir)) {
				new_x++;
			}
			break;
		}
		case SOUTH:{
			dir = Directions.WEST;
			if(!this.willBump(dir)) {
				new_x--;
			}
			break;
		}
		case WEST:{
			dir = Directions.NORTH;
			if(!this.willBump(dir)) {
				new_y++;
			}
			break;
		}
		case EAST:{
			dir = Directions.SOUTH;
			if (!this.willBump(dir)) {
				new_y--;
			}
			break;
		}
		default: {
			dir = Directions.EAST;
		}
		}
		this.currentDirection = dir;
		if (this.willBump(dir)) {
			this.isBumped = true;
			this.num_bumps++;
			this.uptime += Config.BUMP_TIME;
		}else {
			this.isBumped = false;
			this.carLoc.setLoc(new_x, new_y);
			this.uptime += Config.TURN_TIME;
		}
		if (this.debug_on) {
			handleDebug("Turn Right");
		}
	}
	public void waitAtSignal() {
		this.uptime++;
		if (this.debug_on) {
			handleDebug("Wait Signal");
		}
	}
	public void stop() {
		assert this.carLoc.equals(this.destination);
		System.out.println("Reached destination at square (" + this.getCarLoc().getX() + "," + this.getCarLoc().getY() + ")");
		System.out.println("Total time: " + this.getUptime());
		System.out.println("Number of bumps: " + this.num_bumps);
	}
	public Map<InforKeys, Object> getPercept(){
		Map<InforKeys, Object> state = new HashMap<>();
		state.put(InforKeys.LOCATION, this.getCarLoc());
		state.put(InforKeys.BUMP, new Boolean(this.isBumped()));
		state.put(InforKeys.SIGNAL_COLORS, this.getSignalColorByDir(this.getCurrentDirection()));
		state.put(InforKeys.REACHED, new Boolean(reached()));
		return state;
	}
}

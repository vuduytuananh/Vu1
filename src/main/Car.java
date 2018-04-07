package main;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
public class Car {
	private Location loc;
	private boolean bump;
	private SignalColors signalColor;
	private boolean reached;
	private Environment env;	
	private Map<InforKeys, Object> state;
	private Directions currentDirection;
	private int numPossibleMoves;
	public Car(Environment env) {
		this.env = env;
		this.state = this.env.getPercept();
		this.loc = (Location) this.state.get(InforKeys.LOCATION);
		this.bump = ((Boolean) this.state.get(InforKeys.BUMP)).booleanValue();
		this.reached = ((Boolean) this.state.get(InforKeys.REACHED)).booleanValue();
		this.signalColor = (SignalColors) this.state.get(InforKeys.SIGNAL_COLORS);
		this.currentDirection = Directions.EAST;
		this.numPossibleMoves = PossibleMoves.values().length;
	}
	private enum PossibleMoves {
		STRAIGHT, LEFT, RIGHT
	}
	private int xChange(Directions curDir, PossibleMoves move) {
		switch(curDir) {
		case NORTH:{
			switch(move) {
			case STRAIGHT: return 0;
			case LEFT: return -1;
			case RIGHT: return 1;
			default: return -999;
			}
		}
		case SOUTH:{
			switch(move) {
			case STRAIGHT: return 0;
			case LEFT: return 1;
			case RIGHT: return -1;
			default: return -999;
			}
		}
		case WEST:{
			switch(move) {
			case STRAIGHT: return -1;
			case LEFT: return 0;
			case RIGHT: return 0;
			default: return -999;
			}
		}
		case EAST:{
			switch(move) {
			case STRAIGHT: return 1;
			case LEFT: return 0;
			case RIGHT: return 0;
			default: return -999;
			}
		}
		default:{
			return -999;
		}
		}
	}
	private int yChange(Directions curDir, PossibleMoves move) {
		switch(curDir) {
		case NORTH:{
			switch(move) {
			case STRAIGHT: return 1;
			case LEFT: return 0;
			case RIGHT: return 0;
			default: return -999;
			}
		}
		case SOUTH:{
			switch(move) {
			case STRAIGHT: return -1;
			case LEFT: return 0;
			case RIGHT: return 0;
			default: return -999;
			}
		}
		case WEST:{
			switch(move) {
			case STRAIGHT: return 0;
			case LEFT: return -1;
			case RIGHT: return 1;
			default: return -999;
			}
		}
		case EAST:{
			switch(move) {
			case STRAIGHT: return 0;
			case LEFT: return 1;
			case RIGHT: return -1;
			default: return -999;
			}
		}
		default:{
			return -999;
		}
		}
	}
	private Location getNewLocation(Directions curDir, Location curLoc, PossibleMoves move) {
		return new Location(curLoc.getX() + xChange(curDir, move), curLoc.getY() + yChange(curDir, move));
	}
	private int getTimeCost(SignalColors signal, PossibleMoves move) {
		if (signal == SignalColors.GREEN) {
			return move == PossibleMoves.STRAIGHT ? Config.GO_STRAIGHT_TIME : Config.TURN_TIME + Config.SIGNAL_LIGHT_WAIT_TIME;
		}
		return move == PossibleMoves.STRAIGHT ? Config.GO_STRAIGHT_TIME + Config.SIGNAL_LIGHT_WAIT_TIME : Config.TURN_TIME;
	}
	private int getRiskCost(int max_discovered, Location nextLocation) {
		return (nextLocation.getX() > max_discovered) || (nextLocation.getY() > max_discovered) ? Config.ADVENTURE_RISK : 0;
	}
	private Directions getNewDirection(Directions prev, PossibleMoves move) {
		if (move == PossibleMoves.STRAIGHT) {
			return prev;
		}
		switch(prev){
		case NORTH:{
			switch(move) {
			case LEFT: return Directions.WEST;
			case RIGHT: return Directions.EAST; 
			default: return Directions.NORTH;
			}
		}
		case SOUTH:{
			switch(move) {
			case LEFT: return Directions.EAST;
			case RIGHT: return Directions.WEST;
			default: return Directions.SOUTH;
			}
		}
		case WEST:{
			switch(move) {
			case LEFT: return Directions.SOUTH;
			case RIGHT: return Directions.NORTH;
			default: return Directions.WEST;
			}
		}
		case EAST:{
			switch(move) {
			case LEFT: return Directions.NORTH;
			case RIGHT: return Directions.SOUTH;
			default: return Directions.EAST;
			}
		}
		default: return Directions.WEST;
		}
	}
	public void goDummy() {
		while(!this.reached) {
//			DUMMY MOVES
			switch(PossibleMoves.values()[((int) Math.floor(Math.random() * 3)) % 3]) {
			case STRAIGHT:{
//				System.out.println(nextMove);
				if (this.signalColor == SignalColors.GREEN) {
					env.goStraight();
				}else {
					env.waitAtSignal();
					env.goStraight();
				}
				break;
			}
			case LEFT:{
//				System.out.println(nextMove);
				if (this.signalColor == SignalColors.GREEN) {
					env.waitAtSignal();
					env.goLeft();
				}else {
					env.goLeft();					
				}

				break;
			}
			case RIGHT:{
//				System.out.println(nextMove);
				if (this.signalColor == SignalColors.GREEN) {
					env.waitAtSignal();
					env.goRight();
				}else {
					env.goRight();					
				}
				break;
			}
			default:;
			}
//			update the reached status
			this.state = this.env.getPercept();
			this.loc = (Location) this.state.get(InforKeys.LOCATION);
			this.bump = ((Boolean) this.state.get(InforKeys.BUMP)).booleanValue();
			this.reached = ((Boolean) this.state.get(InforKeys.REACHED)).booleanValue();
			this.signalColor = (SignalColors) this.state.get(InforKeys.SIGNAL_COLORS);
		}
//		Destination reached, stop the car
		this.env.stop();
	}
	public void goSmart() {
		int[] costs = new int[this.numPossibleMoves];
		Set<Location> visited_locs = new HashSet<Location>();
		visited_locs.add(new Location(this.loc.getX(), this.loc.getY()));
		Location[] nextPossibleLocations = new Location[this.numPossibleMoves];
		int max_discovered = Math.max(this.loc.getX(), this.loc.getY());
		while(!this.reached) {
//			SMART MOVES
			int min_cost = Integer.MAX_VALUE;
			int min_nav_idx = Integer.MAX_VALUE;
			PossibleMoves[] moves = PossibleMoves.values();
//			System.out.println("Current x: " + this.loc.getX() + "\t Current y: " + this.loc.getY());
//			System.out.println("List length: " + visited_locs.size());
			for (int i = 0; i < moves.length; i++) {
				nextPossibleLocations[i] = getNewLocation(this.currentDirection, this.loc, moves[i]);
//				System.out.println("Next x: " + nextPossibleLocations[i].getX() + "\t Next y: " + nextPossibleLocations[i].getY());
//				visited_locs.stream().forEach(loc -> System.out.println("Include: x - " + loc.getX() + " - y - " + loc.getY()));
				if (visited_locs.contains(nextPossibleLocations[i]) || nextPossibleLocations[i].getX() < 0 || nextPossibleLocations[i].getY() < 0) {
//					System.out.println("Skip x: " + nextPossibleLocations[i].getX() + "\t Skip y: " + nextPossibleLocations[i].getY());
					continue;
				}
				costs[i] = getTimeCost(this.signalColor, moves[i]) + getRiskCost(max_discovered, nextPossibleLocations[i]);
//				System.out.println("Cost_Analyzing x: " + nextPossibleLocations[i].getX() + "\t Cost_Analyzing y: " + nextPossibleLocations[i].getY());
				if (min_cost > costs[i]) {
					min_cost = costs[i];
					min_nav_idx = i;
				}
			}
//			System.out.println("Decide x: " + nextPossibleLocations[min_nav_idx].getX() + "\t Decide y: " + nextPossibleLocations[min_nav_idx].getY());

			PossibleMoves nextMove = moves[min_nav_idx];
			switch(nextMove) {
			case STRAIGHT:{
//				System.out.println(nextMove);
				if (this.signalColor == SignalColors.GREEN) {
					env.goStraight();
				}else {
					env.waitAtSignal();
					env.goStraight();
				}
				break;
			}
			case LEFT:{
//				System.out.println(nextMove);
				if (this.signalColor == SignalColors.GREEN) {
					env.waitAtSignal();
					env.goLeft();
				}else {
					env.goLeft();					
				}

				break;
			}
			case RIGHT:{
//				System.out.println(nextMove);
				if (this.signalColor == SignalColors.GREEN) {
					env.waitAtSignal();
					env.goRight();
				}else {
					env.goRight();					
				}
				break;
			}
			default:;
			}
//			update the reached status
			this.state = this.env.getPercept();
			this.loc = (Location) this.state.get(InforKeys.LOCATION);
			visited_locs.add(new Location(this.loc.getX(), this.loc.getY()));
			this.bump = ((Boolean) this.state.get(InforKeys.BUMP)).booleanValue();
			this.reached = ((Boolean) this.state.get(InforKeys.REACHED)).booleanValue();
			this.signalColor = (SignalColors) this.state.get(InforKeys.SIGNAL_COLORS);
			this.currentDirection = this.getNewDirection(this.currentDirection, nextMove);
			int max_xy = Math.max(this.loc.getX(), this.loc.getY());
			max_discovered = max_xy > max_discovered ? max_xy : max_discovered;
		}
//		Destination reached, stop the car
		this.env.stop();
	}
}

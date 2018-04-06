package main;

public class Location {
	private int x;
	private int y;
	public Location(int x, int y) {
		this.x = x;
		this.y = y; 
	}

	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public void setLoc(int x, int y) {
		this.x = x;
		this.y = y;
	}
//	ALWAYS OVERRIDE HASHCODE AND EQUALS AT THE SAME TIME!!!!
	@Override
	public boolean equals(Object other) {
		Location other_loc = (Location) other;
		return (this.getX() == other_loc.getX()) && (this.getY() == other_loc.getY());
	}
	
	@Override
	public int hashCode() {
		return this.getX()*this.getX() + this.getY()*this.getY();
	}
	
}

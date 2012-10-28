package org.testevol.engine.diff;

public class Pair<T> {

	private T first;
	private T second;
	boolean match;
	
	public Pair( T first, T second ) {
		this.first = first;
		this.second = second;
	}
	
	public void setMatch( boolean m ) {
		match = m;
	}
	
	public T getFirst() {
		return first;
	}
	
	public T getSecond() {
		return second;
	}
	
	public boolean isMatch() {
		return match;
	}
}

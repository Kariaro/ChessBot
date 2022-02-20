package me.hardcoded.chess.utils;

/**
 * This class implements two methods {@link #read()} and {@link #write(T value)}.
 *
 * When read is called the internal value is consumed and is changed to {@code null}.
 * You can only write when the internal value is {@code null}.
 */
public class ReadReset<T> {
	private T object;
	
	public synchronized T read() {
		T old = object;
		object = null;
		return old;
	}
	
	public synchronized boolean write(T value) {
		if (object != null) {
			return false;
		}
		
		object = value;
		return true;
	}
}

package me.hardcoded.chess.utils;

import java.awt.Point;

public final class MathUtils {
	public static int clamp(int value, int min, int max) {
		return (value < min ? min:(value > max ? max:value));
	}
	
	@Deprecated
	public static Point convert(BoardPanel panel, int i) {
		boolean flip = panel.isFlipped();
		int x = (flip ? (7 - (i & 7)):(i & 7));
		int y = (flip ? (i >>> 3):(7 - (i >>> 3)));
		
		return new Point(x, y);
	}
}

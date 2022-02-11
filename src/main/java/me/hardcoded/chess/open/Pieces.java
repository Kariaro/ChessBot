package me.hardcoded.chess.open;

/**
 * Negative values is black and positive is white
 */
public interface Pieces {
	int NONE = 0;
	int KING = 1;
	int QUEEN = 2;
	int BISHOP = 3;
	int KNIGHT = 4;
	int ROOK = 5;
	int PAWN = 6;
	
	int[] VALUES = {
		-100, // PAWN   -6
		-500, // ROOK   -5
		-300, // KNIGHT -4
		-300, // BISHOP -3
		-900, // QUEEN  -2
		  -0, // KING   -1
		   0, // NONE    0
		   0, // KING    1
		 900, // QUEEN   2
		 300, // BISHOP  3
		 300, // KNIGHT  4
		 500, // ROOK    5
		 100, // PAWN    6
	};
	
	String[] STRING_VALUES = {
		"NONE",
		"KING",
		"QUEEN",
		"BISHOP",
		"KNIGHT",
		"ROOK",
		"PAWN"
	};
	
	static int value(int id) {
		return VALUES[id + 6];
//		if(id < 0) {
//			id = -id;
//			switch(id) {
//				case KING: return 0;
//				case QUEEN: return -900;
//				case BISHOP: return -300;
//				case KNIGHT: return -300;
//				case ROOK: return -500;
//				case PAWN: return -100;
//				default: return 0;
//			}
//		}
//		
//		switch(id) {
//			case KING: return 0;
//			case QUEEN: return 900;
//			case BISHOP: return 300;
//			case KNIGHT: return 300;
//			case ROOK: return 500;
//			case PAWN: return 100;
//			default: return 0;
//		}
	}
	
	static String toString(int id) {
		return STRING_VALUES[Math.abs(id)];
	}
}

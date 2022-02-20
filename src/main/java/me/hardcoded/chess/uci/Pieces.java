package me.hardcoded.chess.uci;

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
	
	int KING_SQ = KING * KING;
	int QUEEN_SQ = QUEEN * QUEEN;
	int BISHOP_SQ = BISHOP * BISHOP;
	int KNIGHT_SQ = KNIGHT * KNIGHT;
	int ROOK_SQ = ROOK * ROOK;
	int PAWN_SQ = PAWN * PAWN;
	
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
	
	char[] PRINTABLE = {
		'p', // PAWN   -6
		'r', // ROOK   -5
		'n', // KNIGHT -4
		'b', // BISHOP -3
		'q', // QUEEN  -2
		'k', // KING   -1
		' ', // NONE    0
		'K', // KING    1
		'Q', // QUEEN   2
		'B', // BISHOP  3
		'N', // KNIGHT  4
		'R', // ROOK    5
		'P', // PAWN    6
	};
	
	int[] PROMOTION = {
		QUEEN,
		BISHOP,
		KNIGHT,
		ROOK,
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
	}
	
	static char printable(int id) {
		return PRINTABLE[id + 6];
	}
	
	static String toString(int id) {
		return STRING_VALUES[Math.abs(id)];
	}
	
	static int fromPrintable(char c) {
		return switch (c) {
			case 'p' -> -PAWN;
			case 'r' -> -ROOK;
			case 'n' -> -KNIGHT;
			case 'b' -> -BISHOP;
			case 'q' -> -QUEEN;
			case 'k' -> -KING;
			case 'P' -> PAWN;
			case 'R' -> ROOK;
			case 'N' -> KNIGHT;
			case 'B' -> BISHOP;
			case 'Q' -> QUEEN;
			case 'K' -> KING;
			default -> NONE;
		};
	}
}

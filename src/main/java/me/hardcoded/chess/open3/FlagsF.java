package me.hardcoded.chess.open3;

/**
 * This interface contains castling right flags
 * 
 * @author HardCoded
 */
public interface FlagsF {
	// White
	int WHITE_CASTLE_KING = 1;
	int WHITE_CASTLE_QUEEN = 2;
	int WHITE_CASTLE_BOTH = WHITE_CASTLE_KING | WHITE_CASTLE_QUEEN;
	
	// Black
	int BLACK_CASTLE_KING = 4;
	int BLACK_CASTLE_QUEEN = 8;
	int BLACK_CASTLE_BOTH = BLACK_CASTLE_KING | BLACK_CASTLE_QUEEN;
}

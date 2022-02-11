package me.hardcoded.chess.advanced;

public interface CastlingFlags {
	// White
	int WHITE_CASTLE_K = 1;
	int WHITE_CASTLE_Q = 2;
	int WHITE_CASTLE_ANY = WHITE_CASTLE_K | WHITE_CASTLE_Q;
	
	// Black
	int BLACK_CASTLE_K = 4;
	int BLACK_CASTLE_Q = 8;
	int BLACK_CASTLE_ANY = BLACK_CASTLE_K | BLACK_CASTLE_Q;
}

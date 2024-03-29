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
	
	// Both
	int ANY_CASTLE_K = WHITE_CASTLE_K | BLACK_CASTLE_K;
	int ANY_CASTLE_Q = WHITE_CASTLE_Q | BLACK_CASTLE_Q;
	int ANY_CASTLE_ANY = WHITE_CASTLE_ANY | BLACK_CASTLE_ANY;
	
	// Position
	int WHITE_KING = 4;
	int BLACK_KING = 60;
	int WHITE_ROOK_K = 7;
	int WHITE_ROOK_Q = 0;
	int BLACK_ROOK_K = 63;
	int BLACK_ROOK_Q = 56;
}

#pragma once

#ifndef __PIECES_H__
#define __PIECES_H__

#include "utils_type.h"

enum Pieces {
	// Typed pieces
	B_PAWN = -6,
	B_ROOK,
	B_KNIGHT,
	B_BISHOP,
	B_QUEEN,
	B_KING,
	NONE,
	W_KING,
	W_QUEEN,
	W_BISHOP,
	W_KNIGHT,
	W_ROOK,
	W_PAWN,

	// Untyped pieces
	KING = 1,
	QUEEN,
	BISHOP,
	KNIGHT,
	ROOK,
	PAWN,

	// Squared values
	KING_SQ = KING * KING,
	QUEEN_SQ = QUEEN * QUEEN,
	BISHOP_SQ = BISHOP * BISHOP,
	KNIGHT_SQ = KNIGHT * KNIGHT,
	ROOK_SQ = ROOK * ROOK,
	PAWN_SQ = PAWN * PAWN,
};

enum SM {
	NORMAL     = 0b00000000,
	EN_PASSANT = 0b01000000,
	PROMOTION  = 0b10000000,
	CASTLING   = 0b11000000,
};

enum Promotion {
	LEFT   = 0b001,
	RIGHT  = 0b010,
	MIDDLE = 0b100,
};

#define MASK_WHITE_K (uint64)(0b0000000000000000000000000000000000000000000000000000000000000110L)
#define MASK_WHITE_Q (uint64)(0b0000000000000000000000000000000000000000000000000000000001110000L)
#define MASK_BLACK_K (uint64)(0b0000011000000000000000000000000000000000000000000000000000000000L)
#define MASK_BLACK_Q (uint64)(0b0111000000000000000000000000000000000000000000000000000000000000L)

enum CastlingFlags {
	// White
	WHITE_CASTLE_K = 1,
	WHITE_CASTLE_Q = 2,
	WHITE_CASTLE_ANY = WHITE_CASTLE_K | WHITE_CASTLE_Q,
	
	// Black
	BLACK_CASTLE_K = 4,
	BLACK_CASTLE_Q = 8,
	BLACK_CASTLE_ANY = BLACK_CASTLE_K | BLACK_CASTLE_Q,
	
	// Both
	ANY_CASTLE_K = WHITE_CASTLE_K | BLACK_CASTLE_K,
	ANY_CASTLE_Q = WHITE_CASTLE_Q | BLACK_CASTLE_Q,
	ANY_CASTLE_ANY = WHITE_CASTLE_ANY | BLACK_CASTLE_ANY,
	
	// Position
	WHITE_KING = 3,
	BLACK_KING = 59,
	WHITE_ROOK_K = 0,
	WHITE_ROOK_Q = 7,
	BLACK_ROOK_K = 56,
	BLACK_ROOK_Q = 63,
};

#endif // !__PIECES_H__


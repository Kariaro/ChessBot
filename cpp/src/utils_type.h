#pragma once
typedef unsigned long long uint64;
typedef signed long long int64;
typedef unsigned int uint;
typedef signed char int8;
typedef unsigned char uint8;

struct Chessboard {
	int8 pieces[64];
	uint64 pieceMask;
	uint64 whiteMask;
	uint64 blackMask;
	int lastCapture;
	int lastPawn;
	int halfMove;
	int flags;
};

struct Move {
	uint from;
	uint to;
	uint special;
	bool valid;
};

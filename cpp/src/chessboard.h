#pragma once

#ifndef __CHESSBOARD_H__
#define __CHESSBOARD_H__

#include "utils_type.h"
#include "pieces.h"

namespace Board {
	inline bool isWhite(Chessboard& board) {
		return (board.halfMove & 1) == 0;
	}

	inline bool hasFlags(Chessboard& board, int flags) {
		return (board.flags & flags) != 0;
	}

	inline void setPiece(Chessboard& board, uint idx, int piece) {
		int old = board.pieces[idx];
		board.pieces[idx] = piece;
		
		uint64 mask = (uint64)(1) << idx;
		if (old < 0 && piece >= 0) board.blackMask &= ~mask;
		if (old > 0 && piece <= 0) board.whiteMask &= ~mask;
		if (piece > 0) board.whiteMask |= mask;
		if (piece < 0) board.blackMask |= mask;

		board.pieceMask = board.blackMask | board.whiteMask;
	}
}

#endif // !__CHESSBOARD_H__


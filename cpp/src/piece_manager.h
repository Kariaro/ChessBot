#pragma once

#ifndef __PIECE_MANAGER_H__
#define __PIECE_MANAGER_H__

#include "utils_type.h"

namespace PieceManager {
	extern uint64 piece_move(Chessboard& board, int piece, uint idx);

	extern uint special_piece_move(Chessboard& board, int piece, uint idx);

	extern bool isAttacked(Chessboard& board, uint idx);

	extern bool isKingAttacked(Chessboard& board, bool isWhite);
}

#endif // !__PIECE_MANAGER_H__


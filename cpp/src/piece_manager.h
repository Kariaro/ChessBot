#pragma once

#ifndef PIECE_MANAGER_H
#define PIECE_MANAGER_H

#include "utils_type.h"

namespace PieceManager {
	extern uint64_t piece_move(Chessboard& board, int piece, uint32_t idx);

	extern uint32_t special_piece_move(Chessboard& board, int piece, uint32_t idx);

	extern bool isAttacked(Chessboard& board, uint32_t idx);

	extern bool isKingAttacked(Chessboard& board, bool isWhite);
}

#endif // !PIECE_MANAGER_H


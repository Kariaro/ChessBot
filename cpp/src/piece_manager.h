#pragma once

#ifndef PIECE_MANAGER_H
#define PIECE_MANAGER_H

#include "utils_type.h"

namespace PieceManager {
	extern uint64 piece_move(Chessboard& board, int piece, uint idx);

	extern uint special_piece_move(Chessboard& board, int piece, uint idx);

	extern bool isAttacked(Chessboard& board, uint idx);

	extern bool isKingAttacked(Chessboard& board, bool isWhite);
}

#endif // !PIECE_MANAGER_H


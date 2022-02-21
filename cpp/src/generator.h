#pragma once

#ifndef __GENERATOR_H__
#define __GENERATOR_H__

#include "utils_type.h"
#include "chessboard.h"
#include <vector>

using std::vector;

namespace Generator {
	extern vector<Move> generateValidMoves(Chessboard& board);

	extern bool isValid(Chessboard& board, uint fromIdx, uint toIdx, uint special);
	extern bool isValid(Chessboard& board, Move& move);

	extern bool playMove(Chessboard& board, uint fromIdx, uint toIdx, uint special);
	extern bool playMove(Chessboard& board, Move& move);
}

#endif // !__GENERATOR_H__



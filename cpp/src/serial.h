#pragma once

#ifndef __SERIAL_H__
#define __SERIAL_H__

#include "utils_type.h"

namespace Serial {
	extern inline int get_piece_value(int piece);
	extern inline char get_piece_character(int piece);

	extern char* get_square_string(int square);

	extern char* getMoveString(int piece, uint from, uint to, uint special);

	extern char* getBoardString(Chessboard* board);
}

#endif // !__SERIAL_H__


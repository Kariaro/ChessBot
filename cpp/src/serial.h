#pragma once

#ifndef SERIAL_H
#define SERIAL_H

#include "utils_type.h"

namespace Serial {
	extern inline int get_piece_value(int piece);
	extern inline char get_piece_character(int piece);
	extern inline int get_piece_from_character(char c);

	extern char* get_square_string(int square);
	
	char* getFancyMoveString(int piece, uint from, uint to, uint special);

	extern char* getMoveString(uint from, uint to, uint special);

	extern char* getBoardString(Chessboard* board);
}

#endif // !SERIAL_H


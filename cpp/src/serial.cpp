#pragma once

#ifndef __SERIAL_CPP__
#define __SERIAL_CPP__

#include "pieces.h"
#include "serial.h"
#include <stdio.h>
#include <stdlib.h>

static inline void appendSquare(char* ptr, uint square) {
	*(ptr    ) = 'h' - (square & 7);
	*(ptr + 1) = '1' + ((square >> 3) & 7);
}

static void appendChars(char* ptr, const char* val) {
	for (int i = 0; ; i++) {
		char c = *(val + i);
		if (c == '\0') {
			break;
		}

		*(ptr + i) = c;
	}
}

const int const VALUES[13] = { -100, -500, -300, -300, -900, 0, 0, 0, 900, 300, 300, 500, 100 };

namespace Serial {
	inline int get_piece_value(int i) {
		if (i > -7 && i < 7) {
			return VALUES[i + 6];
		}

		return 0;
	}

	inline char get_piece_character(int i) {
		if (i > -7 && i < 7) {
			return "prnbqk\0KQBNRP"[i + 6];
		}

		return '\0';

		/*
		switch (piece) {
			case B_ROOK:   return 'r';
			case B_KNIGHT: return 'n';
			case B_BISHOP: return 'b';
			case B_QUEEN:  return 'q';
			case B_KING:   return 'k';
			case B_PAWN:   return 'p';
			case W_ROOK:   return 'R';
			case W_KNIGHT: return 'N';
			case W_BISHOP: return 'B';
			case W_QUEEN:  return 'Q';
			case W_KING:   return 'K';
			case W_PAWN:   return 'P';
			default: return '\0';
		}
		*/
	}

	char* get_square_string(int square) {
		char* result = (char*)calloc(3, sizeof(char));
		if (!result) return 0;

		*(result    ) = 'h' - (square & 7);
		*(result + 1) = '1' + ((square >> 3) & 7);
		*(result + 2) = '\0';
		return result;
	}

	char* getMoveString(int piece, uint from, uint to, uint special) {
		char* buffer = (char*)calloc(32, sizeof(char));
		if (!buffer) {
			return 0;
		}
		char* ptr = buffer;

		uint type = special & 0b11000000;

		switch (type) {
			case NORMAL: {
				char pieceChar = get_piece_character(piece);
				if (pieceChar != '\0') {
					*ptr = pieceChar;
					ptr++;
				}

				appendSquare(ptr, from);
				appendSquare(ptr + 2, to);
				*(ptr + 4) = '\0';
				break;
			}
			case CASTLING: {
				appendChars(ptr, (special & ANY_CASTLE_K) ? "O-O\0" : "O-O-O\0");
				break;
			}
			case EN_PASSANT: {
				appendSquare(ptr, from);
				appendSquare(ptr + 2, to);
				appendChars(ptr + 4, " (en passant)\0");
				break;
			}
			case PROMOTION: {
				appendSquare(ptr, to);
				*(ptr + 2) = '=';
				*(ptr + 3) = get_piece_character((special >> 3) & 0b111);
				*(ptr + 4) = '\0';
				break;
			}
		}

		return buffer;
	}

	char* getBoardString(Chessboard* board) {
		char* chars = (char*)malloc(128);
		if (!chars) {
			return 0;
		}

		chars[127] = '\0';

		char* ptr = chars;
		for (int i = 0; i < 64; i++) {
			char c = get_piece_character(board->pieces[i]);

			if (i > 0) {
				*(ptr++) = ((i & 7) == 0) ? '\n' : ' ';
			}

			*(ptr++) = (c == '\0') ? '.' : c;
		}

		return chars;
	}
}

#endif // !__SERIAL_CPP__


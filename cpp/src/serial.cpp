#ifndef SERIAL_CPP
#define SERIAL_CPP

#include "pieces.h"
#include "serial.h"
#include <stdio.h>
#include <stdlib.h>

// TODO: Remove
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
		return (i > -7 && i < 7) ? (VALUES[i + 6]) : (0);
	}

	inline char get_piece_character(int i) {
		return (i > -7 && i < 7) ? ("prnbqk\0KQBNRP"[i + 6]) : ('\0');
	}

	inline int get_piece_from_character(char c) {
		switch (c) {
			case 'r': return Pieces::B_ROOK;
			case 'n': return Pieces::B_KNIGHT;
			case 'b': return Pieces::B_BISHOP;
			case 'q': return Pieces::B_QUEEN;
			case 'k': return Pieces::B_KING;
			case 'p': return Pieces::B_PAWN;
			case 'R': return Pieces::W_ROOK;
			case 'N': return Pieces::W_KNIGHT;
			case 'B': return Pieces::W_BISHOP;
			case 'Q': return Pieces::W_QUEEN;
			case 'K': return Pieces::W_KING;
			case 'P': return Pieces::W_PAWN;
			default: return Pieces::NONE;
		}
	}

	char* get_square_string(int square) {
		char* result = (char*)calloc(3, sizeof(char));
		if (!result) return 0;

		*(result    ) = 'h' - (square & 7);
		*(result + 1) = '1' + ((square >> 3) & 7);
		*(result + 2) = '\0';
		return result;
	}
	
	// TODO: Remove
	char* getFancyMoveString(int piece, uint from, uint to, uint special) {
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
				
				*(ptr    ) = 'h' - (from & 7);
				*(ptr + 1) = '1' + ((from >> 3) & 7);
				*(ptr + 2) = 'h' - (to & 7);
				*(ptr + 3) = '1' + ((to >> 3) & 7);
				*(ptr + 4) = '\0';
				break;
			}
			case CASTLING: {
				appendChars(ptr, (special & ANY_CASTLE_K) ? "O-O\0" : "O-O-O\0");
				break;
			}
			case EN_PASSANT: {
				*(ptr    ) = 'h' - (from & 7);
				*(ptr + 1) = '1' + ((from >> 3) & 7);
				*(ptr + 2) = 'h' - (to & 7);
				*(ptr + 3) = '1' + ((to >> 3) & 7);
				appendChars(ptr + 4, " (en passant)\0");
				break;
			}
			case PROMOTION: {
				*(ptr + 0) = 'h' - (to & 7);
				*(ptr + 1) = '1' + ((to >> 3) & 7);
				*(ptr + 2) = '=';
				*(ptr + 3) = get_piece_character((special >> 3) & 0b111);
				*(ptr + 4) = '\0';
				break;
			}
		}

		return buffer;
	}

	char* getMoveString(uint from, uint to, uint special) {
		char* buffer = (char*)calloc(8, sizeof(char));
		if (!buffer) {
			return 0;
		}

		*(buffer    ) = 'a' + (from & 7);
		*(buffer + 1) = '1' + ((from >> 3) & 7);
		*(buffer + 2) = 'a' + (to & 7);
		*(buffer + 3) = '1' + ((to >> 3) & 7);

		if ((special & 0b11000000) == SM::PROMOTION) {
			*(buffer + 4) = get_piece_character(-(int)((special >> 3) & 0b111));
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

#endif // !SERIAL_CPP


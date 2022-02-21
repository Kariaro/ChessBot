#pragma once

#ifndef PIECE_MANAGER_CPP
#define PIECE_MANAGER_CPP

#include "utils_type.h"
#include "utils.h"
#include "pieces.h"
#include "precomputed.h"
#include "chessboard.h"
#include "piece_manager.h"

uint64 black_pawn_move(Chessboard& board, uint idx) {
	uint64 pawn = (uint64)(1) << idx;
	uint64 step = pawn >> 8;
	uint64 result = 0;
	
	int ypos = idx >> 3;
	int xpos = idx & 7;
	
	if (ypos > 1) {
		result |= step & ~board.pieceMask;
		if (result != 0 && ypos == 6) { // Pawn jump
			result |= (step >> 8) & ~board.pieceMask;
		}
		
		if (xpos > 0) { // Takes
			result |= board.whiteMask & (step >> 1);
		}
		
		if (xpos < 7) {
			result |= board.whiteMask & (step << 1);
		}
	}
	
	return result;
}

uint64 white_pawn_move(Chessboard& board, uint idx) {
	uint64 pawn = (uint64)(1) << idx;
	uint64 step = pawn << 8;
	uint64 result = 0;
	
	int ypos = idx >> 3;
	int xpos = idx & 7;
	
	if (ypos < 6) {
		result |= step & ~board.pieceMask;
		if (result != 0 && ypos == 1) { // Pawn jump
			result |= (step << 8) & ~board.pieceMask;
		}
		
		if (xpos > 0) { // Takes
			result |= board.blackMask & (step >> 1);
		}
		
		if (xpos < 7) {
			result |= board.blackMask & (step << 1);
		}
	}
	
	return result;
}

uint64 bishop_move(uint64 board_pieceMask, uint idx) {
	uint64 moveMask = PrecomputedTable::BISHOP_MOVES[idx];
	uint64 checkMask = board_pieceMask & moveMask;
	
	const uint64* SHADOW = PrecomputedTable::BISHOP_SHADOW_MOVES[idx];
	while (checkMask != 0) {
		uint64 pick = Utils::lowestOneBit(checkMask);
		checkMask &= ~pick;
		uint64 shadowMask = SHADOW[Utils::numberOfTrailingZeros(pick)];
		moveMask &= shadowMask;
		checkMask &= shadowMask;
	}

	return moveMask;
}

uint64 rook_move(uint64 board_pieceMask, uint idx) {
	uint64 moveMask = PrecomputedTable::ROOK_MOVES[idx];
	uint64 checkMask = board_pieceMask & moveMask;
			
	const uint64* SHADOW = PrecomputedTable::ROOK_SHADOW_MOVES[idx];
	while (checkMask != 0) {
		uint64 pick = Utils::lowestOneBit(checkMask);
		checkMask &= ~pick;
		uint64 shadowMask = SHADOW[Utils::numberOfTrailingZeros(pick)];
		moveMask &= shadowMask;
		checkMask &= shadowMask;
	}

	return moveMask;
}

uint64 queen_move(uint64 board_pieceMask, uint idx) {
	uint64 bishop_moveMask = PrecomputedTable::BISHOP_MOVES[idx];
	uint64 bishop_checkMask = board_pieceMask & bishop_moveMask;

	uint64 rook_moveMask = PrecomputedTable::ROOK_MOVES[idx];
	uint64 rook_checkMask = board_pieceMask & rook_moveMask;
	
	const uint64* BISHOP_SHADOW = PrecomputedTable::BISHOP_SHADOW_MOVES[idx];
	const uint64* ROOK_SHADOW = PrecomputedTable::ROOK_SHADOW_MOVES[idx];

	// A rook and a bishop on the same square will never have the same positions
	// This is only true when both of them are zero.
	while (bishop_checkMask != rook_checkMask) {
		uint64 pick;
		uint64 shadowMask;
		
		if (bishop_checkMask != 0) {
			pick = Utils::lowestOneBit(bishop_checkMask);
			bishop_checkMask &= ~pick;
			shadowMask = BISHOP_SHADOW[Utils::numberOfTrailingZeros(pick)];
			bishop_moveMask &= shadowMask;
			bishop_checkMask &= shadowMask;
		}

		if (rook_checkMask != 0) {
			pick = Utils::lowestOneBit(rook_checkMask);
			rook_checkMask &= ~pick;
			shadowMask = ROOK_SHADOW[Utils::numberOfTrailingZeros(pick)];
			rook_moveMask &= shadowMask;
			rook_checkMask &= shadowMask;
		}

		/*
		pick = Utils::lowestOneBit(bishop_checkMask);
		bishop_checkMask &= ~pick;
		shadowMask = pick == 0 ? ~0 : BISHOP_SHADOW[Utils::numberOfTrailingZeros(pick)];
		bishop_moveMask &= shadowMask;
		bishop_checkMask &= shadowMask;

		pick = Utils::lowestOneBit(rook_checkMask);
		rook_checkMask &= ~pick;
		shadowMask = pick == 0 ? ~0 : ROOK_SHADOW[Utils::numberOfTrailingZeros(pick)];
		rook_moveMask &= shadowMask;
		rook_checkMask &= shadowMask;
		*/
	}

	return bishop_moveMask | rook_moveMask;
	// return bishop_move(board_pieceMask, idx) | rook_move(board_pieceMask, idx);
}

inline uint64 king_move(uint idx) {
	return PrecomputedTable::KING_MOVES[idx];
}

inline uint64 knight_move(uint idx) {
	return PrecomputedTable::KNIGHT_MOVES[idx];
}

inline uint64 white_pawn_attack(uint idx) {
	return PrecomputedTable::PAWN_ATTACK_WHITE[idx];
}

inline uint64 black_pawn_attack(uint idx) {
	return PrecomputedTable::PAWN_ATTACK_BLACK[idx];
}

template <int A, int B>
bool _hasTwoPiece(Chessboard& board, uint64 mask) {
	if constexpr (A < 0) {
		mask &= board.blackMask;
	} else {
		mask &= board.whiteMask;
	}
		
	while (mask != 0) {
		uint64 pick = Utils::lowestOneBit(mask);
		mask &= ~pick;
		uint idx = Utils::numberOfTrailingZeros(pick);
		
		int piece = board.pieces[idx];
		if (piece == A || piece == B) {
			return true;
		}
	}
		
	return false;
}

template <int A>
bool _hasPiece(Chessboard& board, uint64 mask) {
	if constexpr (A < 0) {
		mask &= board.blackMask;
	} else {
		mask &= board.whiteMask;
	}
	
	while (mask != 0) {
		uint64 pick = Utils::lowestOneBit(mask);
		mask &= ~pick;
		uint idx = Utils::numberOfTrailingZeros(pick);
		
		if (board.pieces[idx] == A) {
			return true;
		}
	}
	
	return false;
}

template <int A>
uint _getFirst(Chessboard& board) {
	uint64 mask;
	if constexpr (A < 0) {
		mask = board.blackMask;
	} else {
		mask = board.whiteMask;
	}
	
	while (mask != 0) {
		uint64 pick = Utils::lowestOneBit(mask);
		mask &= ~pick;
		uint idx = Utils::numberOfTrailingZeros(pick);
		
		if (board.pieces[idx] == A) {
			return idx;
		}
	}
	
	return -1;
}

namespace PieceManager {
	uint64 piece_move(Chessboard& board, int piece, uint idx) {
		switch (piece) {
			case Pieces::W_KNIGHT: return knight_move(idx) & ~board.whiteMask;
			case Pieces::W_BISHOP: return bishop_move(board.pieceMask, idx) & ~board.whiteMask;
			case Pieces::W_ROOK:   return rook_move(board.pieceMask, idx) & ~board.whiteMask;
			case Pieces::W_QUEEN:  return queen_move(board.pieceMask, idx) & ~board.whiteMask;
			case Pieces::W_PAWN:   return white_pawn_move(board, idx);
			case Pieces::W_KING:   return king_move(idx) & ~board.whiteMask;

			case Pieces::B_KNIGHT: return knight_move(idx) & ~board.blackMask;
			case Pieces::B_BISHOP: return bishop_move(board.pieceMask, idx) & ~board.blackMask;
			case Pieces::B_ROOK:   return rook_move(board.pieceMask, idx) & ~board.blackMask;
			case Pieces::B_QUEEN:  return queen_move(board.pieceMask, idx) & ~board.blackMask;
			case Pieces::B_PAWN:   return black_pawn_move(board, idx);
			case Pieces::B_KING:   return king_move(idx) & ~board.blackMask;
			default: return 0;
		}
	}

	uint white_pawn_special_move(Chessboard& board, uint idx) {
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		// En passant
		if (ypos == 4 && board.lastPawn != 0) {
			int lyp = board.lastPawn >> 3;
			if (lyp == 5) {
				int lxp = board.lastPawn & 7;
				if (xpos - 1 == lxp) {
					return (idx + 7) | SM::EN_PASSANT;
				}
				
				if (xpos + 1 == lxp) {
					return (idx + 9) | SM::EN_PASSANT;
				}
			}
		}
		
		// Promotion
		if (ypos == 6) {
			int result = 0;
			
			if (xpos > 0) {
				uint64 mask = (uint64)(1) << (idx + 7);
				if ((board.blackMask & mask) != 0) {
					result |= Promotion::LEFT;
				}
			}
			
			if (xpos < 7) {
				uint64 mask = (uint64)(1) << (idx + 9);
				if ((board.blackMask & mask) != 0) {
					result |= Promotion::RIGHT;
				}
			}
			
			{
				uint64 mask = (uint64)(1) << (idx + 8);
				if (((board.pieceMask) & mask) == 0) {
					result |= Promotion::MIDDLE;
				}
			}
			
			return result == 0 ? 0 : (result | SM::PROMOTION);
		}
		
		return 0;
	}

	uint black_pawn_special_move(Chessboard& board, uint idx) {
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		// En passant
		if (ypos == 3 && board.lastPawn != 0) {
			int lyp = board.lastPawn >> 3;
			if (lyp == 2) {
				int lxp = board.lastPawn & 7;
				if (xpos - 1 == lxp) {
					return (idx - 9) | SM::EN_PASSANT;
				}
				
				if (xpos + 1 == lxp) {
					return (idx - 7) | SM::EN_PASSANT;
				}
			}
		}
		
		// Promotion
		if (ypos == 1) {
			int result = 0;
			
			if (xpos > 0) {
				uint64 mask = (uint64)(1) << (idx - 9);
				if ((board.whiteMask & mask) != 0) {
					result |= Promotion::LEFT;
				}
			}
			
			if (xpos < 7) {
				uint64 mask = (uint64)(1) << (idx - 7);
				if ((board.whiteMask & mask) != 0) {
					result |= Promotion::RIGHT;
				}
			}
			
			{
				uint64 mask = (uint64)(1) << (idx - 8);
				if (((board.pieceMask) & mask) == 0) {
					result |= Promotion::MIDDLE;
				}
			}
			
			return result == 0 ? 0 : (result | SM::PROMOTION);
		}
		
		return 0;
	}

	uint white_king_special_move(Chessboard& board, uint idx) {
		uint result = 0;
		if ((board.pieceMask & MASK_WHITE_K) == 0 && Board::hasFlags(board, CastlingFlags::WHITE_CASTLE_K)) {
			result |= SM::CASTLING | CastlingFlags::WHITE_CASTLE_K;
		}
			
		if ((board.pieceMask & MASK_WHITE_Q) == 0 && Board::hasFlags(board, CastlingFlags::WHITE_CASTLE_Q)) {
			result |= SM::CASTLING | CastlingFlags::WHITE_CASTLE_Q;
		}
		
		return result;
	}

	uint black_king_special_move(Chessboard& board, uint idx) {
		uint result = 0;
		if ((board.pieceMask & MASK_BLACK_K) == 0 && Board::hasFlags(board, CastlingFlags::BLACK_CASTLE_K)) {
			result |= SM::CASTLING | CastlingFlags::BLACK_CASTLE_K;
		}
			
		if ((board.pieceMask & MASK_BLACK_Q) == 0 && Board::hasFlags(board, CastlingFlags::BLACK_CASTLE_Q)) {
			result |= SM::CASTLING | CastlingFlags::BLACK_CASTLE_Q;
		}
		
		return result;
	}

	uint special_piece_move(Chessboard& board, int piece, uint idx) {
		switch (piece) {
			case Pieces::W_PAWN: return white_pawn_special_move(board, idx);
			case Pieces::W_KING: return white_king_special_move(board, idx);

			case Pieces::B_PAWN: return black_pawn_special_move(board, idx);
			case Pieces::B_KING: return black_king_special_move(board, idx);
			default: return 0;
		}
	}
	
	bool isAttacked(Chessboard& board, uint idx) {
		bool isWhite = Board::isWhite(board);
		uint64 pieceMask = board.pieceMask;
		
		if (isWhite) {
			uint64 _rook_move = (rook_move(pieceMask, idx) & ~board.whiteMask) & board.blackMask;
			if (_hasTwoPiece<Pieces::B_ROOK, Pieces::B_QUEEN>(board, _rook_move)) {
				return true;
			}
			
			uint64 _bishop_move = (bishop_move(pieceMask, idx) & ~board.whiteMask) & board.blackMask;
			if (_hasTwoPiece<Pieces::B_BISHOP, Pieces::B_QUEEN>(board, _bishop_move)) {
				return true;
			}
			
			uint64 _knight_move = (knight_move(idx) & ~board.whiteMask) & board.blackMask;
			if (_hasPiece<Pieces::B_KNIGHT>(board, _knight_move)) {
				return true;
			}
			
			uint64 _king_move = (king_move(idx) & ~board.whiteMask) & board.blackMask;
			if (_hasPiece<Pieces::B_KING>(board, _king_move)) {
				return true;
			}
			
			uint64 _pawn_move = white_pawn_attack(idx) & board.blackMask;
			return _hasPiece<Pieces::B_PAWN>(board, _pawn_move);
		} else {
			uint64 _rook_move = (rook_move(pieceMask, idx) & ~board.blackMask) & board.whiteMask;
			if (_hasTwoPiece<Pieces::W_ROOK, Pieces::W_QUEEN>(board, _rook_move)) {
				return true;
			}
			
			uint64 _bishop_move = (bishop_move(pieceMask, idx) & ~board.blackMask) & board.whiteMask;
			if (_hasTwoPiece<Pieces::W_BISHOP, Pieces::W_QUEEN>(board, _bishop_move)) {
				return true;
			}
			
			uint64 _knight_move = (knight_move(idx) & ~board.blackMask) & board.whiteMask;
			if (_hasPiece<Pieces::W_KNIGHT>(board, _knight_move)) {
				return true;
			}
			
			uint64 _king_move = (king_move(idx) & ~board.blackMask) & board.whiteMask;
			if (_hasPiece<Pieces::W_KING>(board, _king_move)) {
				return true;
			}
			
			uint64 _pawn_move = black_pawn_attack(idx) & board.whiteMask;
			return _hasPiece<Pieces::W_PAWN>(board, _pawn_move);
		}
	}
	
	bool isKingAttacked(Chessboard& board, bool isWhite) {
		int old = board.halfMove;
		uint idx;
		board.halfMove = isWhite ? 0 : 1;
		
		// Find the king
		if (isWhite) {
			idx = _getFirst<Pieces::W_KING>(board);
		} else {
			idx = _getFirst<Pieces::B_KING>(board);
		}
		
		if (idx != -1 && isAttacked(board, idx)) {
			board.halfMove = old;
			return true;
		}
		
		board.halfMove = old;
		return false;
	}
}

#endif // !PIECE_MANAGER_CPP


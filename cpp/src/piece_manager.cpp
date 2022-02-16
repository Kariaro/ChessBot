#pragma once

#ifndef __PIECE_MANAGER_CPP__
#define __PIECE_MANAGER_CPP__

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
	int ypos = idx >> 3;
	int xpos = idx & 7;

	uint64 result = 0;
	for (int j = 0; j < 4; j++) {
		int dx = (j  & 1) * 2 - 1;
		int dy = (j >> 1) * 2 - 1;
		
		for (int i = 1; i < 8; i++) {
			int xp = xpos + dx * i;
			int yp = ypos + dy * i;
			
			if (xp < 0 || xp > 7 || yp < 0 || yp > 7) {
				break;
			}

			uint64 mask = (uint64)(1) << (xp + (yp << 3));
			result |= mask;
			
			if ((mask & board_pieceMask) != 0) {
				break;
			}
		}
	}
	
	return result;
}

uint64 rook_move(uint64 board_pieceMask, uint idx) {
	int ypos = idx >> 3;
	int xpos = idx & 7;
	
	uint64 result = 0;
	for (int j = 0; j < 4; j++) {
		int dr = 1 - (j & 2);
		int dx = ((j    ) & 1) * dr;
		int dy = ((j + 1) & 1) * dr;
		
		for (int i = 1; i < 8; i++) {
			int xp = xpos + dx * i;
			int yp = ypos + dy * i;
			
			if (xp < 0 || xp > 7 || yp < 0 || yp > 7) {
				break;
			}
			
			uint64 mask = (uint64)(1) << (xp + (yp << 3));
			result |= mask;
			
			if ((mask & board_pieceMask) != 0) {
				break;
			}
		}
	}
	
	return result;
}

uint64 queen_move(uint64 board_pieceMask, uint idx) {
	return bishop_move(board_pieceMask, idx) | rook_move(board_pieceMask, idx);
}

uint64 king_move(uint idx) {
	return PrecomputedTable::KING_MOVES[idx];
}

uint64 knight_move(uint idx) {
	return PrecomputedTable::KNIGHT_MOVES[idx];
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

	uint white_pawn_special_move(Chessboard board, uint idx) {
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

	uint king_special_move(Chessboard& board, uint idx) {
		uint result = 0;
		if (Board::isWhite(board)) {
			if ((board.pieceMask & MASK_WHITE_K) == 0 && Board::hasFlags(board, CastlingFlags::WHITE_CASTLE_K)) {
				result |= SM::CASTLING | CastlingFlags::WHITE_CASTLE_K;
			}
			
			if ((board.pieceMask & MASK_WHITE_Q) == 0 && Board::hasFlags(board, CastlingFlags::WHITE_CASTLE_Q)) {
				result |= SM::CASTLING | CastlingFlags::WHITE_CASTLE_Q;
			}
		} else {
			if ((board.pieceMask & MASK_BLACK_K) == 0 && Board::hasFlags(board, CastlingFlags::BLACK_CASTLE_K)) {
				result |= SM::CASTLING | CastlingFlags::BLACK_CASTLE_K;
			}
			
			if ((board.pieceMask & MASK_BLACK_Q) == 0 && Board::hasFlags(board, CastlingFlags::BLACK_CASTLE_Q)) {
				result |= SM::CASTLING | CastlingFlags::BLACK_CASTLE_Q;
			}
		}
		
		return result;
	}

	uint special_piece_move(Chessboard& board, int piece, uint idx) {
		switch (piece) {
			case Pieces::W_PAWN: return white_pawn_special_move(board, idx);
			case Pieces::W_KING: return king_special_move(board, idx);

			case Pieces::B_PAWN: return black_pawn_special_move(board, idx);
			case Pieces::B_KING: return king_special_move(board, idx);
			default: return 0;
		}
	}

	inline uint64 pawn_attack(bool isWhite, uint idx) {
		return isWhite ? PrecomputedTable::PAWN_ATTACK_BLACK[idx] : PrecomputedTable::PAWN_ATTACK_WHITE[idx];
	}

	inline uint64 white_pawn_attack(uint idx) {
		return PrecomputedTable::PAWN_ATTACK_WHITE[idx];
	}

	inline uint64 black_pawn_attack(uint idx) {
		return PrecomputedTable::PAWN_ATTACK_BLACK[idx];
	}
	
	bool hasTwoPiece(Chessboard& board, uint64 mask, int findA, int findB) {
		// the mask only contains pieces that belongs to the correct team
		mask &= (findA < 0 ? board.blackMask : board.whiteMask);
		
		while (mask != 0) {
			uint64 pick = Utils::lowestOneBit(mask);
			mask &= ~pick;
			uint idx = Utils::numberOfTrailingZeros(pick);
			
			int8 piece = board.pieces[idx];
			if (piece == findA || piece == findB) {
				return true;
			}
		}
		
		return false;
	}
	
	bool hasPiece(Chessboard& board, uint64 mask, int find) {
		// the mask only contains pieces that belongs to the correct team
		mask &= (find < 0 ? board.blackMask : board.whiteMask);
		
		while (mask != 0) {
			uint64 pick = Utils::lowestOneBit(mask);
			mask &= ~pick;
			uint idx = Utils::numberOfTrailingZeros(pick);
			
			if (board.pieces[idx] == find) {
				return true;
			}
		}
		
		return false;
	}
	
	uint getFirst(Chessboard& board, uint64 mask, int find) {
		// the mask only contains pieces that belongs to the correct team
		mask &= (find < 0 ? board.blackMask :board.whiteMask);
		
		while (mask != 0) {
			uint64 pick = Utils::lowestOneBit(mask);
			mask &= ~pick;
			uint idx = Utils::numberOfTrailingZeros(pick);
			
			if (board.pieces[idx] == find) {
				return idx;
			}
		}
		
		return -1;
	}
	
	bool isAttacked(Chessboard& board, uint idx) {
		bool isWhite = Board::isWhite(board);
		uint64 pieceMask = board.pieceMask;
		
		if (isWhite) {
			uint64 _rook_move = (rook_move(pieceMask, idx) & ~board.whiteMask) & board.blackMask;
			if (hasTwoPiece(board, _rook_move, Pieces::B_ROOK, Pieces::B_QUEEN)) {
				return true;
			}
			
			uint64 _bishop_move = (bishop_move(pieceMask, idx) & ~board.whiteMask) & board.blackMask;
			if (hasTwoPiece(board, _bishop_move, Pieces::B_BISHOP, Pieces::B_QUEEN)) {
				return true;
			}
			
			uint64 _knight_move = (knight_move(idx) & ~board.whiteMask) & board.blackMask;
			if (hasPiece(board, _knight_move, Pieces::B_KNIGHT)) {
				return true;
			}
			
			uint64 _king_move = (king_move(idx) & ~board.whiteMask) & board.blackMask;
			if (hasPiece(board, _king_move, Pieces::B_KING)) {
				return true;
			}
			
			uint64 _pawn_move = white_pawn_attack(idx) & board.blackMask;
			return hasPiece(board, _pawn_move, Pieces::B_PAWN);
		} else {
			uint64 _rook_move = (rook_move(pieceMask, idx) & ~board.blackMask) & board.whiteMask;
			if (hasTwoPiece(board, _rook_move, Pieces::W_ROOK, Pieces::W_QUEEN)) {
				return true;
			}
			
			uint64 _bishop_move = (bishop_move(pieceMask, idx) & ~board.blackMask) & board.whiteMask;
			if (hasTwoPiece(board, _bishop_move, Pieces::W_BISHOP, Pieces::W_QUEEN)) {
				return true;
			}
			
			uint64 _knight_move = (knight_move(idx) & ~board.blackMask) & board.whiteMask;
			if (hasPiece(board, _knight_move, Pieces::W_KNIGHT)) {
				return true;
			}
			
			uint64 _king_move = (king_move(idx) & ~board.blackMask) & board.whiteMask;
			if (hasPiece(board, _king_move, Pieces::W_KING)) {
				return true;
			}
			
			uint64 _pawn_move = black_pawn_attack(idx) & board.whiteMask;
			return hasPiece(board, _pawn_move, Pieces::W_PAWN);
		}
	}
	
	bool isKingAttacked(Chessboard& board, bool isWhite) {
		int old = board.halfMove;
		uint idx;
		board.halfMove = isWhite ? 0 : 1;
		
		// Find the king
		if (isWhite) {
			idx = getFirst(board, board.whiteMask, Pieces::W_KING);
		} else {
			idx = getFirst(board, board.blackMask, Pieces::B_KING);
		}
		
		if (idx != -1 && isAttacked(board, idx)) {
			board.halfMove = old;
			return true;
		}
		
		board.halfMove = old;
		return false;
	}
}

#endif // !__PIECE_MANAGER_CPP__


#pragma once

#ifndef __GENERATOR_CPP__
#define __GENERATOR_CPP__

#include "utils_type.h"
#include "utils.h"
#include "pieces.h"
#include "chessboard.h"
#include "piece_manager.h"
#include "generator.h"
#include <vector>

using std::vector;

uint const PROMOTION_PIECES[4] {
	KNIGHT,
	BISHOP,
	QUEEN,
	ROOK,
};

namespace Generator {
	vector<Move> generate_moves(Chessboard& board) {
		vector<Move> vector_moves;
		vector_moves.reserve(96);

		bool isWhite = Board::isWhite(board);
		uint64 mask = isWhite ? board.whiteMask : board.blackMask;
		
		while (mask != 0) {
			uint64 pick = Utils::lowestOneBit(mask);
			mask &= ~pick;
			uint idx = Utils::numberOfTrailingZeros(pick);
			
			int piece = board.pieces[idx];
			uint64 moves = PieceManager::piece_move(board, piece, idx);
			
			while (moves != 0) {
				uint64 move_bit = Utils::lowestOneBit(moves);
				moves &= ~move_bit;
				uint move_idx = Utils::numberOfTrailingZeros(move_bit);
				vector_moves.push_back(Move{ piece, idx, move_idx, 0 });
			}
			
			if (piece * piece == 1 || piece * piece == 36) {
				uint special = PieceManager::special_piece_move(board, piece, idx);
				int type = special & 0b11000000;
				if (type == SM::CASTLING) {
					// Split the castling moves up into multiple moves
					uint specialFlag;
					if ((specialFlag = (special & CastlingFlags::ANY_CASTLE_K)) != 0) {
						vector_moves.push_back(Move{ piece, idx, idx - 2, SM::CASTLING | specialFlag });
					}
					if ((specialFlag = (special & CastlingFlags::ANY_CASTLE_Q)) != 0) {
						vector_moves.push_back(Move{ piece, idx, idx + 2, SM::CASTLING | specialFlag });
					}

				} else if (type == SM::EN_PASSANT) {
					vector_moves.push_back(Move{ piece, idx, special & 0b111111, special });

				} else if (type == SM::PROMOTION) {
					// Split promotion into multiple moves
					uint specialFlag;
					if ((specialFlag = (special & Promotion::LEFT)) != 0) {
						for (uint promotionPiece : PROMOTION_PIECES) {
							vector_moves.push_back(Move{ piece, idx, idx + (isWhite ? 8 : -8) - 1, SM::PROMOTION | specialFlag | promotionPiece << 3 });
						}
					}
					if ((specialFlag = (special & Promotion::MIDDLE)) != 0) {
						for (uint promotionPiece : PROMOTION_PIECES) {
							vector_moves.push_back(Move{ piece, idx, idx + (isWhite ? 8 : -8), SM::PROMOTION | specialFlag | promotionPiece << 3 });
						}
					}
					if ((specialFlag = (special & Promotion::RIGHT)) != 0) {
						for (uint promotionPiece : PROMOTION_PIECES) {
							vector_moves.push_back(Move{ piece, idx, idx + (isWhite ? 8 : -8) + 1, SM::PROMOTION | specialFlag | promotionPiece << 3 });
						}
					}

				}
			}
		}

		return vector_moves;
	}

	vector<Move> generateValidMoves(Chessboard& board) {
		vector<Move> vector_moves;
		vector_moves.reserve(96);

		bool isWhite = Board::isWhite(board);
		uint64 mask = isWhite ? board.whiteMask : board.blackMask;
		
		while (mask != 0) {
			uint64 pick = Utils::lowestOneBit(mask);
			mask &= ~pick;
			uint idx = Utils::numberOfTrailingZeros(pick);
			
			int piece = board.pieces[idx];
			uint64 moves = PieceManager::piece_move(board, piece, idx);
			
			while (moves != 0) {
				uint64 move_bit = Utils::lowestOneBit(moves);
				moves &= ~move_bit;
				uint move_idx = Utils::numberOfTrailingZeros(move_bit);

				if (isValid(board, idx, move_idx, 0)) {
					vector_moves.push_back(Move{ piece, idx, move_idx, 0 });
				}
			}
			
			if (piece * piece == 1 || piece * piece == 36) {
				uint special = PieceManager::special_piece_move(board, piece, idx);
				int type = special & 0b11000000;
				if (type == SM::CASTLING) {
					// Split the castling moves up into multiple moves
					uint specialFlag;
					if ((specialFlag = (special & CastlingFlags::ANY_CASTLE_K)) != 0) {
						Move move = Move{ piece, idx, idx - 2, SM::CASTLING | specialFlag };
						if (isValid(board, move)) {
							vector_moves.push_back(move);
						}
					}
					if ((specialFlag = (special & CastlingFlags::ANY_CASTLE_Q)) != 0) {
						Move move = Move{ piece, idx, idx + 2, SM::CASTLING | specialFlag };
						if (isValid(board, move)) {
							vector_moves.push_back(move);
						}
					}

				} else if (type == SM::EN_PASSANT) {
					if (isValid(board, idx, special & 0b111111, special)) {
						vector_moves.push_back(Move{ piece, idx, special & 0b111111, special });
					}

				} else if (type == SM::PROMOTION) {
					// Split promotion into multiple moves
					uint toIdx = idx + (isWhite ? 8 : -8);
					uint specialFlag;
					if ((specialFlag = (special & Promotion::LEFT)) != 0) {
						for (uint promotionPiece : PROMOTION_PIECES) {
							Move move = Move{ piece, idx, toIdx - 1, SM::PROMOTION | specialFlag | promotionPiece << 3 };
							if (isValid(board, move)) {
								vector_moves.push_back(move);
							}
						}
					}
					if ((specialFlag = (special & Promotion::MIDDLE)) != 0) {
						for (uint promotionPiece : PROMOTION_PIECES) {
							Move move = Move{ piece, idx, toIdx, SM::PROMOTION | specialFlag | promotionPiece << 3 };
							if (isValid(board, move)) {
								vector_moves.push_back(move);
							}
						}
					}
					if ((specialFlag = (special & Promotion::RIGHT)) != 0) {
						for (uint promotionPiece : PROMOTION_PIECES) {
							Move move = Move{ piece, idx, toIdx + 1, SM::PROMOTION | specialFlag | promotionPiece << 3 };
							if (isValid(board, move)) {
								vector_moves.push_back(move);
							}
						}
					}

				}
			}
		}

		return vector_moves;
	}

	bool isValid(Chessboard& board, uint fromIdx, uint toIdx, uint special) {
		bool isWhite = Board::isWhite(board);
		
		if (special == 0) {
			int oldFrom = board.pieces[fromIdx];
			int oldTo = board.pieces[toIdx];
			
			Board::setPiece(board, fromIdx, Pieces::NONE);
			Board::setPiece(board, toIdx, oldFrom);
			// board.setPiece(fromIdx, Pieces.NONE);
			// board.setPiece(toIdx, oldFrom);
			
			bool isValid = !PieceManager::isKingAttacked(board, isWhite);
			
			Board::setPiece(board, fromIdx, oldFrom);
			Board::setPiece(board, toIdx, oldTo);
			// board.setPiece(fromIdx, oldFrom);
			// board.setPiece(toIdx, oldTo);
			
			return isValid;
		} else {
			int type = special & 0b11000000;
			
			switch (type) {
				case SM::CASTLING: {
					if ((special & CastlingFlags::ANY_CASTLE_K) != 0) {
						return !(PieceManager::isAttacked(board, fromIdx)
							|| PieceManager::isAttacked(board, fromIdx - 1)
							|| PieceManager::isAttacked(board, fromIdx - 2));
					}
					
					if ((special & CastlingFlags::ANY_CASTLE_Q) != 0) {
						return !(PieceManager::isAttacked(board, fromIdx)
							|| PieceManager::isAttacked(board, fromIdx + 1)
							|| PieceManager::isAttacked(board, fromIdx + 2));
					}
				}
				
				case SM::EN_PASSANT: {
					int oldFrom = board.pieces[fromIdx];
					int remIdx = toIdx + (isWhite ? -8 : 8);
					int oldRem = board.pieces[remIdx];
					int oldTo = board.pieces[toIdx];
					
					Board::setPiece(board, fromIdx, Pieces::NONE);
					Board::setPiece(board, remIdx, Pieces::NONE);
					Board::setPiece(board, toIdx, oldFrom);
					// board.setPiece(fromIdx, Pieces.NONE);
					// board.setPiece(remIdx, Pieces.NONE);
					// board.setPiece(toIdx, oldFrom);
					
					bool isValid = !PieceManager::isKingAttacked(board, isWhite);
					
					Board::setPiece(board, fromIdx, oldFrom);
					Board::setPiece(board, remIdx, oldRem);
					Board::setPiece(board, toIdx, oldTo);
					// board.setPiece(fromIdx, oldFrom);
					// board.setPiece(remIdx, oldRem);
					// board.setPiece(toIdx, oldTo);
					
					return isValid;
				}
				
				case SM::PROMOTION: {
					int oldFrom = board.pieces[fromIdx];
					int oldTo = board.pieces[toIdx];
					
					Board::setPiece(board, fromIdx, Pieces::NONE);
					Board::setPiece(board, toIdx, oldFrom);
					// board.setPiece(fromIdx, Pieces.NONE);
					// board.setPiece(toIdx, oldFrom);
					
					bool isValid = !PieceManager::isKingAttacked(board, isWhite);
					
					Board::setPiece(board, fromIdx, oldFrom);
					Board::setPiece(board, toIdx, oldTo);
					// board.setPiece(fromIdx, oldFrom);
					// board.setPiece(toIdx, oldTo);
					
					return isValid;
				}
			}
			
			return false;
		}
	}
	
	bool isValid(Chessboard& board, Move& move) {
		return isValid(board, move.from, move.to, move.special);
	}
	
	bool playMove(Chessboard& board, uint fromIdx, uint toIdx, uint special) {
		bool isWhite = Board::isWhite(board);
		int mul = isWhite ? 1 : -1;
		
		// Increase moves since last capture
		int nextLastCapture = board.lastCapture + 1;
		int nextHalfMove = board.halfMove + 1;
		int nextLastPawn = 0;
		
		switch (special & 0b11000000) {
			case SM::NORMAL: {
				int oldFrom = board.pieces[fromIdx];
				int oldTo = board.pieces[toIdx];
				int pieceSq = oldFrom * oldFrom;
				
				switch (pieceSq) {
					case Pieces::ROOK_SQ: {
						if (isWhite) {
							if (fromIdx == CastlingFlags::WHITE_ROOK_K) {
								board.flags &= ~CastlingFlags::WHITE_CASTLE_K;
							}
							
							if (fromIdx == CastlingFlags::WHITE_ROOK_Q) {
								board.flags &= ~CastlingFlags::WHITE_CASTLE_Q;
							}
						} else {
							if (fromIdx == CastlingFlags::BLACK_ROOK_K) {
								board.flags &= ~CastlingFlags::BLACK_CASTLE_K;
							}
							
							if (fromIdx == CastlingFlags::BLACK_ROOK_Q) {
								board.flags &= ~CastlingFlags::BLACK_CASTLE_Q;
							}
						}
						break;
					}
					case Pieces::KING_SQ: {
						if (isWhite) {
							if (fromIdx == CastlingFlags::WHITE_KING) {
								board.flags &= ~CastlingFlags::WHITE_CASTLE_ANY;
							}
						} else {
							if (fromIdx == CastlingFlags::BLACK_KING) {
								board.flags &= ~CastlingFlags::BLACK_CASTLE_ANY;
							}
						}
						break;
					}
					case Pieces::PAWN_SQ: {
						// Only double jumps are saved
						int distance = (fromIdx - toIdx) * (fromIdx - toIdx);
						
						// Because double pawns jump two rows they will always have a distance of 256
						if (distance == 256) {
							nextLastPawn = toIdx + (isWhite ? -8 : 8);
						}
						
						nextLastCapture = 0;
						break;
					}
				}
				
				if (oldTo != Pieces::NONE) {
					// Capture
					nextLastCapture = 0;
				}
				
				if (board.flags != 0) {
					// Recalculate the castling flags
					if (isWhite) {
						if (toIdx == CastlingFlags::BLACK_ROOK_K) {
							board.flags &= ~CastlingFlags::BLACK_CASTLE_K;
						}
						
						if (toIdx == CastlingFlags::BLACK_ROOK_Q) {
							board.flags &= ~CastlingFlags::BLACK_CASTLE_Q;
						}
					} else {
						if (toIdx == CastlingFlags::WHITE_ROOK_K) {
							board.flags &= ~CastlingFlags::WHITE_CASTLE_K;
						}
						
						if (toIdx == CastlingFlags::WHITE_ROOK_Q) {
							board.flags &= ~CastlingFlags::WHITE_CASTLE_Q;
						}
					}
				}
				
				Board::setPiece(board, fromIdx, Pieces::NONE);
				Board::setPiece(board, toIdx, oldFrom);
				// board.setPiece(fromIdx, Pieces.NONE);
				// board.setPiece(toIdx, oldFrom);
				break;
			}
			
			case SM::CASTLING: {
				if ((special & CastlingFlags::ANY_CASTLE_K) != 0) {
					Board::setPiece(board, fromIdx - 3, Pieces::NONE);
					Board::setPiece(board, fromIdx - 2, Pieces::KING * mul);
					Board::setPiece(board, fromIdx - 1, Pieces::ROOK * mul);
					Board::setPiece(board, fromIdx, Pieces::NONE);
					// board.setPiece(fromIdx - 3, Pieces.NONE);
					// board.setPiece(fromIdx - 2, Pieces.KING * mul);
					// board.setPiece(fromIdx - 1, Pieces.ROOK * mul);
					// board.setPiece(fromIdx, Pieces.NONE);
					board.flags &= isWhite ? ~CastlingFlags::WHITE_CASTLE_ANY : ~CastlingFlags::BLACK_CASTLE_ANY;
				}
				
				if ((special & CastlingFlags::ANY_CASTLE_Q) != 0) {
					Board::setPiece(board, fromIdx + 4, Pieces::NONE);
					Board::setPiece(board, fromIdx + 2, Pieces::KING * mul);
					Board::setPiece(board, fromIdx + 1, Pieces::ROOK * mul);
					Board::setPiece(board, fromIdx, Pieces::NONE);
					// board.setPiece(fromIdx + 4, Pieces.NONE);
					// board.setPiece(fromIdx + 2, Pieces.KING * mul);
					// board.setPiece(fromIdx + 1, Pieces.ROOK * mul);
					// board.setPiece(fromIdx, Pieces.NONE);
					board.flags &= isWhite ? ~CastlingFlags::WHITE_CASTLE_ANY : ~CastlingFlags::BLACK_CASTLE_ANY;
				}

				break;
			}
			
			case SM::EN_PASSANT: {
				int oldFrom = board.pieces[fromIdx];
				int remIdx = toIdx + (isWhite ? -8 : 8);
				
				nextLastCapture = 0;
				Board::setPiece(board, fromIdx, Pieces::NONE);
				Board::setPiece(board, remIdx, Pieces::NONE);
				Board::setPiece(board, toIdx, oldFrom);
				// board.setPiece(fromIdx, Pieces.NONE);
				// board.setPiece(remIdx, Pieces.NONE);
				// board.setPiece(toIdx, oldFrom);
				break;
			}
			
			case SM::PROMOTION: {
				int piece = (special & 0b111000) >> 3;
				
				switch (piece) {
					case Pieces::QUEEN:
					case Pieces::BISHOP:
					case Pieces::KNIGHT:
					case Pieces::ROOK: {
						int oldFrom = board.pieces[fromIdx];
						Board::setPiece(board, fromIdx, Pieces::NONE);
						Board::setPiece(board, toIdx, piece * mul);
						// b.setPiece(fromIdx, Pieces.NONE);
						// b.setPiece(toIdx, piece * mul);
						
						if (oldFrom != 0) {
							nextLastCapture = 0;
						}
						break;
					}
					default: {
						return false;
					}
				}

				break;
			}
		}
		
		board.lastCapture = nextLastCapture;
		board.lastPawn = nextLastPawn;
		board.halfMove = nextHalfMove;
		return true;
	}
	
	bool playMove(Chessboard& board, Move& move) {
		return playMove(board, move.from, move.to, move.special);
	}
}

#endif // !__GENERATOR_CPP__



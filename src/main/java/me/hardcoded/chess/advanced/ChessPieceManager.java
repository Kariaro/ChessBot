package me.hardcoded.chess.advanced;

import static me.hardcoded.chess.open.Pieces.*;

import me.hardcoded.chess.open.Pieces;
import me.hardcoded.chess.visual.PlayableChessBoard;

/**
 * This is the piece manager for the chess engine.
 * This manager will give all possible moves for a given piece.
 * 
 * @author HardCoded
 */
public class ChessPieceManager {
	public static final PlayableChessBoard BOARD_PANEL = new PlayableChessBoard();
	
	public static long piece_move(ChessBoardImpl board, int piece, int idx) {
		return piece_move(board, piece, board.isWhite(), idx);
	}
	
	protected static long piece_move(ChessBoardImpl board, int piece, boolean isWhite, int idx) {
		if (isWhite) {
			return switch (piece) {
				case KNIGHT -> knight_move(board, idx) & ~board.whiteMask;
				case BISHOP -> bishop_move(board, idx) & ~board.whiteMask;
				case ROOK -> rook_move(board, idx) & ~board.whiteMask;
				case QUEEN -> (bishop_move(board, idx) | rook_move(board, idx)) & ~board.whiteMask;
				case PAWN -> white_pawn_move(board, idx);
				case KING -> king_move(board, idx) & ~board.whiteMask;
				default -> 0;
			};
		} else {
			return switch (-piece) {
				case KNIGHT -> knight_move(board, idx) & ~board.blackMask;
				case BISHOP -> bishop_move(board, idx) & ~board.blackMask;
				case ROOK -> rook_move(board, idx) & ~board.blackMask;
				case QUEEN -> (bishop_move(board, idx) | rook_move(board, idx)) & ~board.blackMask;
				case PAWN -> black_pawn_move(board, idx);
				case KING -> king_move(board, idx) & ~board.blackMask;
				default -> 0;
			};
		}
	}
	
	public static int special_piece_move(ChessBoardImpl board, int piece, boolean isWhite, int idx) {
		if (isWhite) {
			return switch (piece) {
				case PAWN -> white_pawn_special_move(board, idx);
				case KING -> king_special_move(board, idx);
				default -> 0;
			};
		} else {
			return switch (-piece) {
				case PAWN -> black_pawn_special_move(board, idx);
				case KING -> king_special_move(board, idx);
				default -> 0;
			};
		}
	}
	
	private static long knight_move(ChessBoardImpl board, int idx) {
		return PrecomputedTable.KNIGHT_MOVES[idx];
	}
	
	private static long rook_move(ChessBoardImpl board, int idx) {
		long moveMask = PrecomputedTable.ROOK_MOVES[idx];
		long checkMask = board.pieceMask & moveMask;
		
		long[] SHADOW = PrecomputedTable.ROOK_SHADOW_MOVES[idx];
		while (checkMask != 0) {
			long pick = Long.lowestOneBit(checkMask);
			checkMask &= ~pick;
			long shadowMask = SHADOW[Long.numberOfTrailingZeros(pick)];
			moveMask &= shadowMask;
			checkMask &= shadowMask;
		}
		
		return moveMask;
	}
	
	private static long bishop_move(ChessBoardImpl board, int idx) {
		long moveMask = PrecomputedTable.BISHOP_MOVES[idx];
		long checkMask = board.pieceMask & moveMask;
		
		long[] SHADOW = PrecomputedTable.BISHOP_SHADOW_MOVES[idx];
		while (checkMask != 0) {
			long pick = Long.lowestOneBit(checkMask);
			checkMask &= ~pick;
			long shadowMask = SHADOW[Long.numberOfTrailingZeros(pick)];
			moveMask &= shadowMask;
			checkMask &= shadowMask;
		}
		
		return moveMask;
	}
	
	private static long white_pawn_move(ChessBoardImpl board, int idx) {
		long pawn = 1L << idx;
		long step = pawn << 8;
		long result = 0;
		
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		if (ypos < 6) {
			result |= step & ~board.pieceMask;
			if (result != 0 && ypos == 1) { // Pawn jump
				result |= (step << 8) & ~board.pieceMask;
			}
			
			if (xpos > 0) { // Takes
				result |= board.blackMask & (step >>> 1);
			}
			
			if (xpos < 7) {
				result |= board.blackMask & (step << 1);
			}
		}
		
		return result;
	}
	
	private static long black_pawn_move(ChessBoardImpl board, int idx) {
		long pawn = 1L << idx;
		long step = pawn >>> 8;
		long result = 0;
		
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		if (ypos > 1) {
			result |= step & ~board.pieceMask;
			if (result != 0 && ypos == 6) { // Pawn jump
				result |= (step >>> 8) & ~board.pieceMask;
			}
			
			if (xpos > 0) { // Takes
				result |= board.whiteMask & (step >>> 1);
			}
			
			if (xpos < 7) {
				result |= board.whiteMask & (step << 1);
			}
		}
		
		return result;
	}
	
	public static final int SM_NORMAL        = 0b00_000000;
	public static final int SM_EN_PASSANT    = 0b01_000000;
	public static final int SM_PROMOTION     = 0b10_000000;
	public static final int SM_CASTLING      = 0b11_000000;
	
	public static final int PROMOTION_LEFT   = 0b001;
	public static final int PROMOTION_RIGHT  = 0b010;
	public static final int PROMOTION_MIDDLE = 0b100;
//	public static final int PROMOTION_TYPE   = 0b111000;
	private static int white_pawn_special_move(ChessBoardImpl board, int idx) {
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		// En passant
		if (ypos == 4 && board.lastPawn != 0) {
			// rp....PR
			// .p..P..R
			// ....p.PR
			
			int lyp = board.lastPawn >> 3;
			if (lyp == 5) {
				int lxp = board.lastPawn & 7;
				if (xpos - 1 == lxp) {
					return (idx + 7) | SM_EN_PASSANT;
				}
				
				if (xpos + 1 == lxp) {
					return (idx + 9) | SM_EN_PASSANT;
				}
			}
		}
		
		// Promotion
		if (ypos == 6) {
			int result = 0;
			
			if (xpos > 0) {
				long mask = 1L << (idx + 7);
				if ((board.blackMask & mask) != 0) {
					result |= PROMOTION_LEFT;
				}
			}
			
			if (xpos < 7) {
				long mask = 1L << (idx + 9);
				if ((board.blackMask & mask) != 0) {
					result |= PROMOTION_RIGHT;
				}
			}
			
			{
				long mask = 1L << (idx + 8);
				if (((board.pieceMask) & mask) == 0) {
					result |= PROMOTION_MIDDLE;
				}
			}
			
			return result == 0 ? 0 : (result | SM_PROMOTION);
		}
		
		return 0;
	}
	
	private static int black_pawn_special_move(ChessBoardImpl board, int idx) {
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		// En passant
		if (ypos == 3 && board.lastPawn != 0) {
			// rp....PR
			// ...p..PR
			// .p.P...R
			
			int lyp = board.lastPawn >> 3;
			if (lyp == 2) {
				int lxp = board.lastPawn & 7;
				if (xpos - 1 == lxp) {
					return (idx - 9) | SM_EN_PASSANT;
				}
				
				if (xpos + 1 == lxp) {
					return (idx - 7) | SM_EN_PASSANT;
				}
			}
		}
		
		// Promotion
		if (ypos == 1) {
			int result = 0;
			
			if (xpos > 0) {
				long mask = 1L << (idx - 9);
				if ((board.whiteMask & mask) != 0) {
					result |= PROMOTION_LEFT;
				}
			}
			
			if (xpos < 7) {
				long mask = 1L << (idx - 7);
				if ((board.whiteMask & mask) != 0) {
					result |= PROMOTION_RIGHT;
				}
			}
			
			{
				long mask = 1L << (idx - 8);
				if (((board.pieceMask) & mask) == 0) {
					result |= PROMOTION_MIDDLE;
				}
			}
			
			return result == 0 ? 0 : (result | SM_PROMOTION);
		}
		
		return 0;
	}
	
	private static long king_move(ChessBoardImpl board, int idx) {
		return PrecomputedTable.KING_MOVES[idx];
	}
	
	private static final long WHITE_K = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_01100000L;
	private static final long WHITE_Q = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00001110L;
	private static final long BLACK_K = 0b01100000_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
	private static final long BLACK_Q = 0b00001110_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
	private static int king_special_move(ChessBoardImpl board, int idx) {
		int result = 0;
		if (board.isWhite()) {
			if ((board.pieceMask & WHITE_K) == 0 && board.hasFlags(CastlingFlags.WHITE_CASTLE_K)) {
				result |= SM_CASTLING | CastlingFlags.WHITE_CASTLE_K;
			}
			
			if ((board.pieceMask & WHITE_Q) == 0 && board.hasFlags(CastlingFlags.WHITE_CASTLE_Q)) {
				result |= SM_CASTLING | CastlingFlags.WHITE_CASTLE_Q;
			}
		} else {
			if ((board.pieceMask & BLACK_K) == 0 && board.hasFlags(CastlingFlags.BLACK_CASTLE_K)) {
				result |= SM_CASTLING | CastlingFlags.BLACK_CASTLE_K;
			}
			
			if ((board.pieceMask & BLACK_Q) == 0 && board.hasFlags(CastlingFlags.BLACK_CASTLE_Q)) {
				result |= SM_CASTLING | CastlingFlags.BLACK_CASTLE_Q;
			}
		}
		
		return result;
	}
	
	private static long white_pawn_attack(int idx) {
		return PrecomputedTable.PAWN_ATTACK_WHITE[idx];
	}
	
	private static long black_pawn_attack(int idx) {
		return PrecomputedTable.PAWN_ATTACK_BLACK[idx];
	}
	
	/**
	 * Returns if the piece at the current position is attacked
	 *
	 * TODO: Calculate the pinned pieces from the previous round. Edge cases is when pieces can still move
	 *       in that direction. Tell if the piece is pinned horizontal, vertical, diagonal_1 or diagonal 2.
	 * TODO: This might be slow
	 * TODO: Inline functions
	 */
	public static boolean isAttacked(ChessBoardImpl board, int idx) {
		boolean isWhite = board.isWhite();
		
		if (isWhite) {
			long rook_move = (rook_move(board, idx) & ~board.whiteMask) & board.blackMask;
			if (hasTwoPiece(board, rook_move, -Pieces.ROOK, -Pieces.QUEEN)) {
				return true;
			}
			
			long bishop_move = (bishop_move(board, idx) & ~board.whiteMask) & board.blackMask;
			if (hasTwoPiece(board, bishop_move, -Pieces.BISHOP, -Pieces.QUEEN)) {
				return true;
			}
			
			long knight_move = (knight_move(board, idx) & ~board.whiteMask) & board.blackMask;
			if (hasPiece(board, knight_move, -Pieces.KNIGHT)) {
				return true;
			}
			
			long king_move = (king_move(board, idx) & ~board.whiteMask) & board.blackMask;
			if (hasPiece(board, king_move, -Pieces.KING)) {
				return true;
			}
			
			long pawn_move = white_pawn_attack(idx) & board.blackMask;
			return hasPiece(board, pawn_move, -Pieces.PAWN);
		} else {
			long rook_move = (rook_move(board, idx) & ~board.blackMask) & board.whiteMask;
			if (hasTwoPiece(board, rook_move, Pieces.ROOK, Pieces.QUEEN)) {
				return true;
			}
			
			long bishop_move = (bishop_move(board, idx) & ~board.blackMask) & board.whiteMask;
			if (hasTwoPiece(board, bishop_move, Pieces.BISHOP, Pieces.QUEEN)) {
				return true;
			}
			
			long knight_move = (knight_move(board, idx) & ~board.blackMask) & board.whiteMask;
			if (hasPiece(board, knight_move, Pieces.KNIGHT)) {
				return true;
			}
			
			long king_move = (king_move(board, idx) & ~board.blackMask) & board.whiteMask;
			if (hasPiece(board, king_move, Pieces.KING)) {
				return true;
			}
			
			long pawn_move = black_pawn_attack(idx) & board.whiteMask;
			return hasPiece(board, pawn_move, Pieces.PAWN);
		}
	}
	
	public static boolean isKingAttacked(ChessBoardImpl board, boolean isWhite) {
		int old = board.halfMove;
		int idx;
		board.halfMove = isWhite ? 0 : 1;
		
		// Find the king
		if (isWhite) {
			idx = getFirst(board, board.whiteMask, Pieces.KING);
		} else {
			idx = getFirst(board, board.blackMask, -Pieces.KING);
		}
		
//		if (idx == -1) {
//			// If the king does not exist we will allow this?
//			// ChessGenerator.debug("Invalid", board.pieces);
//			return false;
//		}
		
		if (idx != -1 && isAttacked(board, idx)) {
			board.halfMove = old;
			return true;
		}
		
		board.halfMove = old;
		return false;
	}
	
	private static boolean hasTwoPiece(ChessBoardImpl board, long mask, int findA, int findB) {
		// the mask only contains pieces that belongs to the correct team
		mask &= (findA < 0 ? board.blackMask : board.whiteMask);
		
		while (mask != 0) {
			long pick = Long.lowestOneBit(mask);
			mask &= ~pick;
			int idx = Long.numberOfTrailingZeros(pick);
			
			int piece = board.pieces[idx];
			if (piece == findA || piece == findB) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean hasPiece(ChessBoardImpl board, long mask, int find) {
		// the mask only contains pieces that belongs to the correct team
		mask &= (find < 0 ? board.blackMask : board.whiteMask);
		
		while (mask != 0) {
			long pick = Long.lowestOneBit(mask);
			mask &= ~pick;
			int idx = Long.numberOfTrailingZeros(pick);
			
			if (board.pieces[idx] == find) {
				return true;
			}
		}
		
		return false;
	}
	
	private static int getFirst(ChessBoardImpl board, long mask, int find) {
		// the mask only contains pieces that belongs to the correct team
		mask &= (find < 0 ? board.blackMask :board.whiteMask);
		
		while (mask != 0) {
			long pick = Long.lowestOneBit(mask);
			mask &= ~pick;
			int idx = Long.numberOfTrailingZeros(pick);
			
			if (board.pieces[idx] == find) {
				return idx;
			}
		}
		
		return -1;
	}
}

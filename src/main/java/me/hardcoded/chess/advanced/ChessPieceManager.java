package me.hardcoded.chess.advanced;

import static me.hardcoded.chess.open.Pieces.*;

import me.hardcoded.chess.open.Pieces;
import me.hardcoded.chess.visual.ChessBoardPanel;

/**
 * This is the piece manager for the chess engine.
 * This manager will give all possible moves for a given piece.
 * 
 * @author HardCoded
 */
public class ChessPieceManager {
	public static final ChessBoardPanel BOARD_PANEL = new ChessBoardPanel();
	
	public static long piece_move(ChessBoard board, int piece, int idx) {
		return piece_move(board, piece, board.isWhite(), idx);
	}
	
	protected static long piece_move(ChessBoard board, int piece, boolean isWhite, int idx) {
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
	
	public static long special_piece_move(ChessBoard board, int piece, boolean isWhite, int idx) {
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
	
	private static long knight_move(ChessBoard board, int idx) {
		return PrecomputedTable.KNIGHT_MOVES[idx];
	}
	
	private static long rook_move(ChessBoard board, int idx) {
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		long result = 0;
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
				
				final long mask = 1L << (xp + (yp << 3));
				result |= mask;
				
				if ((mask & board.pieceMask) != 0) {
					break;
				}
			}
		}
		
		return result;
	}
	
	private static long bishop_move(ChessBoard board, int idx) {
		int ypos = idx >> 3;
		int xpos = idx & 7;
		long result = 0;
		
		for (int j = 0; j < 4; j++) {
			int dx = (j   & 1) * 2 - 1;
			int dy = (j >>> 1) * 2 - 1;
			
			for (int i = 1; i < 8; i++) {
				int xp = xpos + dx * i;
				int yp = ypos + dy * i;
				
				if (xp < 0 || xp > 7 || yp < 0 || yp > 7) {
					break;
				}
				
				final long mask = 1L << (xp + (yp << 3));
				result |= mask;
				
				if ((mask & board.pieceMask) != 0) {
					break;
				}
			}
		}
		
		return result;
	}
	
	private static long white_pawn_move(ChessBoard board, int idx) {
		long pawn = 1L << idx;
		long step = pawn << 8;
		long result = 0;
		
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		result |= step & ~board.pieceMask;
		if (result != 0 && ypos == 1) { // Pawn jump
			result |= (step << 8L) & ~board.pieceMask;
		}
		
		if (xpos > 0) { // Takes
			result |= board.blackMask & (step >>> 1);
		}
		
		if (xpos < 7) {
			result |= board.blackMask & (step << 1);
		}
		
		return result;
	}
	
	private static long black_pawn_move(ChessBoard board, int idx) {
		long pawn = 1L << idx;
		long step = pawn >>> 8;
		long result = 0;
		
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		// TODO: Do not give any move suggestions for the eights or first rank because that's where the promotion bit
		//       will be set
		
		result |= step & ~board.pieceMask;
		if (result != 0 && ypos == 6) { // Pawn jump
			result |= (step >>> 8L) & ~board.pieceMask;
		}
		
		if (xpos > 0) { // Takes
			result |= board.whiteMask & (step >>> 1);
		}
		
		if (xpos < 7) {
			result |= board.whiteMask & (step << 1);
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
	private static int white_pawn_special_move(ChessBoard board, int idx) {
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		// En passant
		if (ypos == 4 && board.lastPawn != 0) {
			// rp....PR
			// .p..P..R
			// ....p.PR
			
			int lyp = board.lastPawn >> 3;
			if (lyp == 4) {
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
	
	private static int black_pawn_special_move(ChessBoard board, int idx) {
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		// En passant
		if (ypos == 3 && board.lastPawn != 0) {
			// rp....PR
			// ...p..PR
			// .p.P...R
			
			int lyp = board.lastPawn >> 3;
			if (lyp == 3) {
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
	
	private static long king_move(ChessBoard board, int idx) {
		return PrecomputedTable.KING_MOVES[idx];
	}
	
	private static final long WHITE_K = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00000110L;
	private static final long WHITE_Q = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_01110000L;
	private static final long BLACK_K = 0b00000110_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
	private static final long BLACK_Q = 0b01110000_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
	private static long king_special_move(ChessBoard board, int idx) {
		long result = 0;
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
	
	private static long pawn_attack(ChessBoard board, boolean isWhite, int idx) {
		return isWhite
			? PrecomputedTable.PAWN_ATTACK_BLACK[idx]
			: PrecomputedTable.PAWN_ATTACK_WHITE[idx];
	}
	
	/**
	 * Returns if the piece at the current position is attacked
	 *
	 * TODO: Calculate the pinned pieces from the previous round. Edge cases is when pieces can still move
	 *       in that direction. Tell if the piece is pinned horizontal, vertical, diagonal_1 or diagonal 2.
	 * TODO: This might be slow
	 * TODO: Inline functions
	 */
	public static boolean isAttacked(ChessBoard board, int idx) {
		boolean isWhite = board.isWhite();
		
		if (isWhite) {
		
//			case KNIGHT -> knight_move(board, idx) & ~board.whiteMask;
//			case BISHOP -> bishop_move(board, idx) & ~board.whiteMask;
//			case ROOK -> rook_move(board, idx) & ~board.whiteMask;
			
			long rook_move = (rook_move(board, idx) & ~board.whiteMask) & board.blackMask;
			if (ChessUtils.hasPiece(board, rook_move , -Pieces.ROOK)) {
				return true;
			}
			
			long bishop_move = (bishop_move(board, idx) & ~board.whiteMask) & board.blackMask;
			if (ChessUtils.hasPiece(board, bishop_move, -Pieces.BISHOP)) {
				return true;
			}
			
			long queen_move = rook_move | bishop_move;
			if (ChessUtils.hasPiece(board, queen_move, -Pieces.QUEEN)) {
				return true;
			}
			
			long knight_move = (knight_move(board, idx) & ~board.whiteMask) & board.blackMask;
			if (ChessUtils.hasPiece(board, knight_move, -Pieces.KNIGHT)) {
				return true;
			}
			
			long king_move = (king_move(board, idx) & ~board.whiteMask) & board.blackMask;
			if (ChessUtils.hasPiece(board, king_move, -Pieces.KING)) {
				return true;
			}
			
			long pawn_move = pawn_attack(board, false, idx) & board.blackMask;
			return ChessUtils.hasPiece(board, pawn_move, -Pieces.PAWN);
		} else {
			long rook_move = (rook_move(board, idx) & ~board.blackMask) & board.whiteMask;
			if (ChessUtils.hasPiece(board, rook_move, Pieces.ROOK)) {
				return true;
			}
			
			long bishop_move = (bishop_move(board, idx) & ~board.blackMask) & board.whiteMask;
			if (ChessUtils.hasPiece(board, bishop_move, Pieces.BISHOP)) {
				return true;
			}
			
			long queen_move = rook_move | bishop_move;
			if (ChessUtils.hasPiece(board, queen_move, Pieces.QUEEN)) {
				return true;
			}
			
			long knight_move = (knight_move(board, idx) & ~board.blackMask) & board.whiteMask;
			if (ChessUtils.hasPiece(board, knight_move, Pieces.KNIGHT)) {
				return true;
			}
			
			long king_move = (king_move(board, idx) & ~board.blackMask) & board.whiteMask;
			if (ChessUtils.hasPiece(board, king_move, Pieces.KING)) {
				return true;
			}
			
			long pawn_move = pawn_attack(board, true, idx) & board.whiteMask;
			return ChessUtils.hasPiece(board, pawn_move, Pieces.PAWN);
		}
	}
	
	public static boolean isKingAttacked(ChessBoard board, boolean isWhite) {
		int old = board.halfMove;
		int idx;
		board.halfMove = isWhite ? 0 : 1;
		
		// Find the king
		if (isWhite) {
			idx = ChessUtils.getFirst(board, board.whiteMask, Pieces.KING);
		} else {
			idx = ChessUtils.getFirst(board, board.blackMask, -Pieces.KING);
		}
		
		if (idx == -1) {
			// If the king does not exist we will allow this?
			// ChessGenerator.debug("Invalid", board.pieces);
			return true;
		}
		
		if (isAttacked(board, idx)) {
			board.halfMove = old;
			return false;
		}
		
		board.halfMove = old;
		return true;
	}
}

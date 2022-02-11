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
				case KNIGHT -> knight_move(board, idx) & ~board.white_mask;
				case BISHOP -> bishop_move(board, idx) & ~board.white_mask;
				case ROOK -> rook_move(board, idx) & ~board.white_mask;
				case QUEEN -> (bishop_move(board, idx) | rook_move(board, idx)) & ~board.white_mask;
				case PAWN -> white_pawn_move(board, idx);
				case KING -> king_move(board, idx) & ~board.white_mask;
				default -> 0;
			};
		} else {
			return switch (-piece) {
				case KNIGHT -> knight_move(board, idx) & ~board.black_mask;
				case BISHOP -> bishop_move(board, idx) & ~board.black_mask;
				case ROOK -> rook_move(board, idx) & ~board.black_mask;
				case QUEEN -> (bishop_move(board, idx) | rook_move(board, idx)) & ~board.black_mask;
				case PAWN -> black_pawn_move(board, idx);
				case KING -> king_move(board, idx) & ~board.black_mask;
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
				
				final long mask = 1L << (xp + (yp << 3L));
				result |= mask;
				
				if ((mask & board.piece_mask) != 0) {
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
				
				final long mask = 1L << (xp + (yp << 3L));
				result |= mask;
				
				if ((mask & board.piece_mask) != 0) {
					break;
				}
			}
		}
		
		return result;
	}
	
	private static long white_pawn_move(ChessBoard board, int idx) {
		long pawn = 1L << (long)idx ;
		long step = pawn << 8L;
		long result = 0;
		
		{
			int ypos = idx >> 3;
			int xpos = idx & 7;
			
			result |= step & ~board.piece_mask;
			if (result != 0 && ypos == 1) { // Pawn jump
				result |= (step << 8L) & ~board.piece_mask;
			}
			
			// Takes
			if (xpos > 0) {
				result |= board.black_mask & (step >>> 1);
			}
			
			if (xpos < 7) {
				result |= board.black_mask & (step << 1);
			}
		}
		
		return result;
	}
	
	private static long black_pawn_move(ChessBoard board, int idx) {
		long pawn = 1L << (long)idx;
		long step = pawn >>> 8L;
		long result = 0;
		
		{
			int ypos = idx >> 3;
			int xpos = idx & 7;
			
			result |= step & ~board.piece_mask;
			if (result != 0 && ypos == 6) { // Pawn jump
				result |= (step >>> 8L) & ~board.piece_mask;
			}
			
			// Takes
			if (xpos > 0) {
				result |= board.white_mask & (step >>> 1);
			}
			
			if (xpos < 7) {
				result |= board.white_mask & (step << 1);
			}
		}
		
		return result;
	}
	
	public static final int sm_en_passant	= 0b01_000000;
	public static final int sm_promotion	= 0b10_000000;
	public static final int sm_castling 	= 0b11_000000;
	private static long white_pawn_special_move(ChessBoard board, int idx) {
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		// En passant
		if (ypos == 4 && board.lastpawn != 0) {
			// rp....PR
			// .p..P..R
			// ....p.PR
			
			int lyp = board.lastpawn >> 3;
			
			if (lyp == 4) {
				int lxp = board.lastpawn & 7;
				if (xpos - 1 == lxp) {
					return (idx + 7) | sm_en_passant;
				}
				
				if (xpos + 1 == lxp) {
					return (idx + 9) | sm_en_passant;	
				}
			}
		}
		
		// Promotion
		if (ypos == 6) {
			int result = 0;
			
			if (xpos > 0) {
				long mask = 1L << (idx + 7L);
				if ((board.black_mask & mask) != 0) {
					result |= 1; // left
				}
			}
			
			if (xpos < 7) {
				long mask = 1L << (idx + 9L);
				if ((board.black_mask & mask) != 0) {
					result |= 2; // right
				}
			}
			
			{
				long mask = 1L << (idx + 8L);
				if (((board.piece_mask) & mask) == 0) {
					result |= 4; // straight
				}
			}
			
			return result == 0 ? 0 : (result | sm_promotion);
		}
		
		return 0;
	}
	
	private static long black_pawn_special_move(ChessBoard board, int idx) {
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		// En passant
		if (ypos == 3 && board.lastpawn != 0) {
			// rp....PR
			// ...p..PR
			// .p.P...R
			
			int lyp = board.lastpawn >> 3;
			
			if (lyp == 3) {
				int lxp = board.lastpawn & 7;
				if (xpos - 1 == lxp) {
					return (idx - 9) | sm_en_passant;
				}
				
				if (xpos + 1 == lxp) {
					return (idx - 7) | sm_en_passant;	
				}
			}
		}
		
		// Promotion
		if (ypos == 1) {
			int result = 0;
			
			if (xpos > 0) {
				long mask = 1L << (idx - 9L);
				if ((board.white_mask & mask) != 0) {
					result |= 1; // left
				}
			}
			
			if (xpos < 7) {
				long mask = 1L << (idx - 7L);
				if ((board.white_mask & mask) != 0) {
					result |= 2; // right
				}
			}
			
			{
				long mask = 1L << (idx - 8L);
				if (((board.piece_mask) & mask) == 0) {
					result |= 4; // straight
				}
			}
			
			return result == 0 ? 0 : (result | sm_promotion);
		}
		
		return 0;
	}
	
	private static long king_move(ChessBoard board, int idx) {
		return PrecomputedTable.KING_MOVES[idx];
	}
	
	private static long king_special_move(ChessBoard board, int idx) {
//		boolean attacker = board.isWhite();
		
		long result = 0;
		if (board.isWhite()) {
			if (board.hasFlags(CastlingFlags.WHITE_CASTLE_K)) {
				if (!(is_attacked(board, idx - 2)
				|| is_attacked(board, idx - 1)
				|| is_attacked(board, idx))) {
					result |= sm_castling | CastlingFlags.WHITE_CASTLE_K;
				}
			}
			
			if (board.hasFlags(CastlingFlags.WHITE_CASTLE_Q)) {
				if (!(is_attacked(board, idx + 3)
				|| is_attacked(board, idx + 2)
				|| is_attacked(board, idx + 1)
				|| is_attacked(board, idx))) {
					result |= sm_castling | CastlingFlags.WHITE_CASTLE_Q;
				}
			}
		} else {
			if (board.hasFlags(CastlingFlags.BLACK_CASTLE_K)) {
				if (!(is_attacked(board, idx - 2)
				|| is_attacked(board, idx - 1)
				|| is_attacked(board, idx))) {
					result |= sm_castling | CastlingFlags.BLACK_CASTLE_K;
				}
			}

			if (board.hasFlags(CastlingFlags.BLACK_CASTLE_Q)) {
				if (!(is_attacked(board, idx + 3)
				|| is_attacked(board, idx + 2)
				|| is_attacked(board, idx + 1)
				|| is_attacked(board, idx))) {
					result |= sm_castling | CastlingFlags.BLACK_CASTLE_Q;
				}
			}
		}
		
		return result;
	}
	
	private static long pawn_attack(ChessBoard board, boolean isWhite, int idx) {
		long pawn_move = 0;
		int y = idx >>> 3;
		int x = idx & 7;
		
		if (isWhite) {
			if (y < 6) {
				if (x > 0) {
					pawn_move |= 1L << (x - 1L + ((y + 1L) << 3L));
				}
				
				if (x < 7) {
					pawn_move |= 1L << (x + 1L + ((y + 1L) << 3L));
				}
			}
		} else {
			if (y > 0) {
				if (x > 0) {
					pawn_move |= 1L << (x - 1L + ((y - 1L) << 3L));
				}
				
				if (x < 7) {
					pawn_move |= 1L << (x + 1L + ((y - 1L) << 3L));
				}
			}
		}
		
		return pawn_move;
	}
	
	/**
	 * Returns if the piece at the current position is attacked
	 *
	 * TODO: Calculate the pinned pieces from the previous round. Edge cases is when pieces can still move
	 *       in that direction. Tell if the piece is pinned horizontal, vertical, diagonal_1 or diagonal 2.
	 * TODO: This might be slow
	 */
	private static boolean is_attacked(ChessBoard board, int idx) {
		if (board.isWhite()) {
			long rook_move = piece_move(board, Pieces.ROOK, idx) & board.black_mask;
			if (ChessUtils.hasPiece(board, rook_move, -Pieces.ROOK)) {
				return true;
			}
			
			long bishop_move = piece_move(board, Pieces.BISHOP, idx) & board.black_mask;
			if (ChessUtils.hasPiece(board, bishop_move, -Pieces.BISHOP)) {
				return true;
			}
			
			long knight_move = piece_move(board, Pieces.KNIGHT, idx) & board.black_mask;
			if (ChessUtils.hasPiece(board, knight_move, -Pieces.KNIGHT)) {
				return true;
			}
			
			long pawn_move = pawn_attack(board, true, idx) & board.black_mask;
			return ChessUtils.hasPiece(board, pawn_move, -Pieces.PAWN);
		} else {
			long rook_move = piece_move(board, -Pieces.ROOK, idx) & board.white_mask;
			if (ChessUtils.hasPiece(board, rook_move, Pieces.ROOK)) {
				return true;
			}
			
			long bishop_move = piece_move(board, -Pieces.BISHOP, idx) & board.white_mask;
			if (ChessUtils.hasPiece(board, bishop_move, Pieces.BISHOP)) {
				return true;
			}
			
			long knight_move = piece_move(board, -Pieces.KNIGHT, idx) & board.white_mask;
			if (ChessUtils.hasPiece(board, knight_move, Pieces.KNIGHT)) {
				return true;
			}
			
			long pawn_move = pawn_attack(board, false, idx) & board.white_mask;
			return ChessUtils.hasPiece(board, pawn_move, Pieces.PAWN);
		}
	}
	
	public static boolean isValid(ChessBoard board, boolean isWhite) {
	
	}
}

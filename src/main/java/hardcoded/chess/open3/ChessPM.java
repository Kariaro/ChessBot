package hardcoded.chess.open3;

import static hardcoded.chess.open.Pieces.*;

import hardcoded.chess.open.Pieces;

public class ChessPM {
	public static long piece_move(ChessB board, int piece, int idx) {
		return piece_move(board, piece, board.isWhite(), idx);
	}
	
	protected static long piece_move(ChessB board, int piece, boolean isWhite, int idx) {
		if(isWhite) {
			switch(piece) {
				case KNIGHT: return knight_move(board, idx) & ~board.white_mask;
				case BISHOP: return bishop_move(board, idx) & ~board.white_mask;
				case ROOK: return rook_move(board, idx) & ~board.white_mask;
				case QUEEN: return (bishop_move(board, idx) | rook_move(board, idx)) & ~board.white_mask;
				case PAWN: return white_pawn_move(board, idx);
				case KING: return king_move(board, idx) & ~board.white_mask;
			}
		} else {
			switch(-piece) {
				case KNIGHT: return knight_move(board, idx) & ~board.black_mask;
				case BISHOP: return bishop_move(board, idx) & ~board.black_mask;
				case ROOK: return rook_move(board, idx) & ~board.black_mask;
				case QUEEN: return (bishop_move(board, idx) | rook_move(board, idx)) & ~board.black_mask;
				case PAWN: return black_pawn_move(board, idx);
				case KING: return king_move(board, idx) & ~board.black_mask;
			}
		}
		
		return 0;
	}
	
	public static long special_piece_move(ChessB board, int piece, int idx) {
		if(board.isWhite()) {
			switch(piece) {
				default: return 0;
				case PAWN: return white_pawn_special_move(board, idx);
				case KING: return king_special_move(board, idx);
			}
		} else {
			switch(-piece) {
				default: return 0;
				case PAWN: return black_pawn_special_move(board, idx);
				case KING: return king_special_move(board, idx);
			}
		}
	}
	
	public static long knight_move(ChessB board, int idx) {
		return Table.KNIGHT_MOVES[idx];
	}
	
	public static long rook_move(ChessB board, int idx) {
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		long result = 0;
		for(int j = 0; j < 4; j++) {
			int dr = 1 - (j & 2);
			int dx = ((j    ) & 1) * dr;
			int dy = ((j + 1) & 1) * dr;
			
			for(int i = 1; i < 8; i++) {
				int xp = xpos + dx * i;
				int yp = ypos + dy * i;
				
				if(xp < 0 || xp > 7 || yp < 0 || yp > 7) break;
				result |= 1L << (xp + yp * 8L);
				
				if((result & board.piece_mask) != 0) {
					break;
				}
			}
		}
		
		return result;
	}
	
	public static long bishop_move(ChessB board, int idx) {
		int ypos = idx >> 3;
		int xpos = idx & 7;
		long result = 0;
		
		for(int j = 0; j < 4; j++) {
			int dx = (j   & 1) * 2 - 1;
			int dy = (j >>> 1) * 2 - 1;
			
			for(int i = 1; i < 8; i++) {
				int xp = xpos + dx * i;
				int yp = ypos + dy * i;
				
				if(xp < 0 || xp > 7 || yp < 0 || yp > 7) break;
				result |= 1L << (xp + yp * 8L);
				
				if((result & board.piece_mask) != 0) {
					break;
				}
			}
		}
		
		return result;
	}
	
	public static long white_pawn_move(ChessB board, int idx) {
		long pawn = 1L << (idx + 0L);
		long step = pawn << 8L;
		long result = 0;
		
		{
			int ypos = idx >> 3;
			int xpos = idx & 7;
			
			result |= step & ~board.piece_mask;
			if(result != 0 && ypos == 1) { // Pawn jump
				result |= (step << 8L) & ~board.piece_mask;
			}
			
			// Takes
			if(xpos > 0) result |= board.black_mask & (step >>> 1);
			if(xpos < 7) result |= board.black_mask & (step << 1);
		}
		
		return result;
	}
	
	public static long black_pawn_move(ChessB board, int idx) {
		long pawn = 1L << (idx + 0L);
		long step = pawn >>> 8L;
		long result = 0;
		
		{
			int ypos = idx >> 3;
			int xpos = idx & 7;
			
			result |= step & ~board.piece_mask;
			if(result != 0 && ypos == 6) { // Pawn jump
				result |= (step >>> 8L) & ~board.piece_mask;
			}
			
			// Takes
			if(xpos > 0) result |= board.white_mask & (step >>> 1);
			if(xpos < 7) result |= board.white_mask & (step << 1);
		}
		
		return result;
	}
	
	public static final int sm_en_passant	= 0b01_000000;
	public static final int sm_promotion	= 0b10_000000;
	public static final int sm_castling 	= 0b11_000000;
	public static long white_pawn_special_move(ChessB board, int idx) {
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		// En passant
		if(ypos == 4 && board.lastpawn != 0) {
			// rp....PR
			// .p..P..R
			// ....p.PR
			
			int lyp = board.lastpawn >> 3;
			
			if(lyp == 4) {
				int lxp = board.lastpawn & 7;
				if(xpos - 1 == lxp) return (idx + 7) | sm_en_passant;
				if(xpos + 1 == lxp) return (idx + 9) | sm_en_passant;	
			}
		}
		
		// Promotion
		if(ypos == 6) {
			int result = 0;
			
			if(xpos > 0) {
				long mask = 1L << (idx + 7L);
				if((board.black_mask & mask) != 0) result |= 1; // left
			}
			
			if(xpos < 7) {
				long mask = 1L << (idx + 9L);
				if((board.black_mask & mask) != 0) result |= 2; // right
			}
			
			{
				long mask = 1L << (idx + 8L);
				if(((board.piece_mask) & mask) == 0) result |= 4; // straight
			}
			
			return result == 0 ? 0:(result | sm_promotion);
		}
		
		return 0;
	}
	
	public static long black_pawn_special_move(ChessB board, int idx) {
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		// En passant
		if(ypos == 3 && board.lastpawn != 0) {
			// rp....PR
			// ...p..PR
			// .p.P...R
			
			int lyp = board.lastpawn >> 3;
			
			if(lyp == 3) {
				int lxp = board.lastpawn & 7;
				if(xpos - 1 == lxp) return (idx - 9) | sm_en_passant;
				if(xpos + 1 == lxp) return (idx - 7) | sm_en_passant;	
			}
		}
		
		// Promotion
		if(ypos == 1) {
			int result = 0;
			
			if(xpos > 0) {
				long mask = 1L << (idx - 9L);
				if((board.white_mask & mask) != 0) result |= 1; // left
			}
			
			if(xpos < 7) {
				long mask = 1L << (idx - 7L);
				if((board.white_mask & mask) != 0) result |= 2; // right
			}
			
			{
				long mask = 1L << (idx - 8L);
				if(((board.piece_mask) & mask) == 0) result |= 4; // straight
			}
			
			return result == 0 ? 0:(result | sm_promotion);
		}
		
		return 0;
	}
	
	public static long king_move(ChessB board, int idx) {
		return Table.KING_MOVES[idx];
	}
	
	// FIXME: Missing moves
	@SuppressWarnings("unused")
	private static final long king_WCKM = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_01100000L;
	@SuppressWarnings("unused")
	private static final long king_WCQM = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00001110L;
	@SuppressWarnings("unused")
	private static final long king_BCKM = 0b01100000_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
	@SuppressWarnings("unused")
	private static final long king_BCQM = 0b00001110_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
	public static long king_special_move(ChessB board, int idx) {
		int ypos = idx >> 3;
		int xpos = idx & 7;
		
		// TODO: Check if squares .kk#qqq.
		//                        r..k...r
		//       are attacked.
		
		if(board.isWhite()) {
			if(board.canWC()) { // can white castle
				boolean a = is_attacked(board, idx - 2);
				boolean b = is_attacked(board, idx - 1);
				boolean c = is_attacked(board, idx);
				
				System.out.printf("%s, %s, %s\n", a, b, c);
			}
		} else {
			if(board.canBC()) { // can black castle
				
			}
		}
		
		return 0;
	}
	
	
	
	// FIXME: Probably slow.
	private static boolean is_attacked(ChessB board, int idx) {
		if(board.isWhite()) {
			long rook_move = piece_move(board, Pieces.ROOK, idx) & board.black_mask;
			if(UtilsF.hasPiece(board, rook_move, -Pieces.ROOK)) return true;
			
			long bishop_move = piece_move(board, Pieces.BISHOP, idx) & board.black_mask;
			if(UtilsF.hasPiece(board, bishop_move, -Pieces.BISHOP)) return true;
			
			long knight_move = piece_move(board, Pieces.KNIGHT, idx) & board.black_mask;
			if(UtilsF.hasPiece(board, knight_move, -Pieces.KNIGHT)) return true;
			
			
			System.out.printf("*** PIECE: %s\n", UtilsF.toBitString(knight_move));
		}
		
		return false;
	}
}

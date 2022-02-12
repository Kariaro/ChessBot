package me.hardcoded.chess.advanced;

public final class ChessUtils {
	public static String toBitString(long value) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < 64; i++) {
			if ((i & 7) == 0) {
				sb.append("_");
			}
			
			sb.append(((value >>> (long)i) & 1));
		}
		
		return sb.substring(1);
	}
	
	public static String toBitFancyString(long value) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < 64; i++) {
			if ((i & 7) == 0) {
				sb.append("\n");
			}
			
			sb.append(((value >>> (long)i) & 1)).append(' ');
		}
		
		return sb.toString();
	}
	
	public static String toSquare(int a) {
		return (char)('h' - (a & 7)) + "" + ((a / 8) + 1);
	}
	
	public static int fromSquare(String square) {
		char a = square.charAt(0);
		char b = square.charAt(1);
		return ('h' - a) + ((b - '1') << 3);
	}
	
	public static String toCastlingMove(int fields) {
		StringBuilder sb = new StringBuilder();
		sb.append(((fields & CastlingFlags.WHITE_CASTLE_K) != 0) ? "K" : "")
		  .append(((fields & CastlingFlags.WHITE_CASTLE_Q) != 0) ? "Q" : "")
		  .append(((fields & CastlingFlags.WHITE_CASTLE_ANY) == 0) ? "-" : "")
		  .append(" / ")
		  .append(((fields & CastlingFlags.BLACK_CASTLE_K) != 0) ? "K" : "")
		  .append(((fields & CastlingFlags.BLACK_CASTLE_Q) != 0) ? "Q" : "")
		  .append(((fields & CastlingFlags.BLACK_CASTLE_ANY) == 0) ? "-" : "");
		return sb.toString();
	}
	
	public static String toSpecialString(int mask) {
		return switch (mask) {
//			case ChessPieceManager.SM_NORMAL -> "Normal";
			case ChessPieceManager.SM_CASTLING -> "Castling";
			case ChessPieceManager.SM_EN_PASSANT -> "En Passant";
			case ChessPieceManager.SM_PROMOTION -> "Promotion";
			default -> "unknown";
		};
	}
	
	/**
	 * Check if the board has the specified piece on the mask
	 */
	public static boolean hasPiece(ChessBoard board, long mask, int find) {
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
	
	/**
	 * Returns the first index of the piece to find
	 */
	public static int getFirst(ChessBoard board, long mask, int find) {
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

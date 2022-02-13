package me.hardcoded.chess.decoder;

import me.hardcoded.chess.advanced.CastlingFlags;
import me.hardcoded.chess.advanced.ChessBoard;
import me.hardcoded.chess.advanced.ChessUtils;
import me.hardcoded.chess.open.Pieces;

public interface ChessCodec {
	class FEN {
		public static ChessBoard load(String text) {
			ChessBoard board = new ChessBoard();
			
			return board;
		}
		
		public static String save(ChessBoard board) {
			int[] pieces = board.getPieces();
			
			StringBuilder sb = new StringBuilder();
			for (int y = 7; y >= 0; y--) {
				int empty = 0;
				for (int x = 7; x >= 0; x--) {
					int piece = pieces[x + (y << 3)];
					char print = Pieces.printable(piece);
					
					if (print == ' ') {
						empty ++;
					} else {
						if (empty > 0) {
							sb.append(empty);
							empty = 0;
						}
						
						sb.append(print);
					}
				}
				
				if (empty > 0) {
					sb.append(empty);
				}
				
				if (y > 0) {
					sb.append('/');
				}
			}
			sb.append(board.isWhite() ? " w " : " b ");
			
			int flags = board.getFlags();
			if ((flags & CastlingFlags.ANY_CASTLE_ANY) == 0) {
				sb.append("- ");
			} else {
				sb.append((flags & CastlingFlags.WHITE_CASTLE_K) != 0 ? "K" : "")
				  .append((flags & CastlingFlags.WHITE_CASTLE_Q) != 0 ? "Q" : "")
				  .append((flags & CastlingFlags.BLACK_CASTLE_K) != 0 ? "k" : "")
				  .append((flags & CastlingFlags.BLACK_CASTLE_Q) != 0 ? "q" : "")
				  .append(' ');
			}
			
			int lastPawn = board.getLastPawn();
			sb.append(lastPawn == 0 ? "-" : ChessUtils.toSquare(board.getLastPawn())).append(' ')
			  .append(board.getHalfMove()).append(' ')
			  .append(board.getFullMove());
			
			System.out.println(sb.toString());
			
			return "";
		}
	}
}

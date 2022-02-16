package me.hardcoded.chess.decoder;

import me.hardcoded.chess.advanced.CastlingFlags;
import me.hardcoded.chess.advanced.ChessBoardImpl;
import me.hardcoded.chess.open.Pieces;
import me.hardcoded.chess.utils.ChessUtils;

/**
 * Internal class for handling FEN
 *
 * @author HardCoded
 */
class ChessFEN {
	static ChessBoardImpl load(ChessBoardImpl board, String text) {
		String[] parts = text.split(" ");
		if (parts.length != 6) {
			throw new RuntimeException("Invalid FEN: The input string did not have 6 parts");
		}
		
		int[] pieces = new int[64];
		int halfMove;
		int lastPawn;
		int lastCapture;
		int flags;
		try {
			String[] ranks = parts[0].split("/");
			if (ranks.length != 8) {
				throw new RuntimeException("Invalid FEN: The input string did not have 8 ranks");
			}
			
			for (int i = 0; i < 8; i++) {
				String rank = ranks[i];
				
				int index = 0;
				for (int j = 0, len = rank.length(); j < len; j++) {
					char c = rank.charAt(j);
					int idx = 7 - index + ((7 - i) << 3);
					
					switch (Character.toLowerCase(c)) {
						case 'p', 'r', 'n', 'b', 'q', 'k' -> {
							pieces[idx] = Pieces.fromPrintable(c);
							index ++;
						}
						case '1', '2', '3', '4', '5', '6', '7', '8' -> {
							index += c - '0';
						}
					}
				}
			}
			
			// Each completed turn is two half moves
			halfMove = Integer.parseInt(parts[5]) * 2 + ("b".equals(parts[1]) ? 1 : 0);
			
			// The amount of half moves since the last pawn move or capture
			lastCapture = Integer.parseInt(parts[4]);
			
			// The last pawn jump
			lastPawn = "-".equals(parts[3]) ? 0 : ChessUtils.fromSquare(parts[3]);
			
			// Castling rights
			flags = 0;
			if (parts[2].contains("K")) flags |= CastlingFlags.WHITE_CASTLE_K;
			if (parts[2].contains("Q")) flags |= CastlingFlags.WHITE_CASTLE_Q;
			if (parts[2].contains("k")) flags |= CastlingFlags.BLACK_CASTLE_K;
			if (parts[2].contains("q")) flags |= CastlingFlags.BLACK_CASTLE_Q;
		} catch (NumberFormatException ignore) {
			throw new RuntimeException("Invalid FEN: The input string contained bad numbers");
		}
		
		board.setStates(flags, halfMove, lastPawn, lastCapture, pieces);
		return board;
	}
	
	static String get(ChessBoardImpl board) {
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
			.append(board.getLastCapture()).append(' ')
			.append(board.getFullMove());
		return sb.toString();
	}
}

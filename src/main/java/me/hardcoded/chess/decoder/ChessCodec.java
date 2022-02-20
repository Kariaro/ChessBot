package me.hardcoded.chess.decoder;

import me.hardcoded.chess.advanced.ChessBoardImpl;
import me.hardcoded.chess.api.ChessBoard;
import me.hardcoded.chess.api.ChessMove;

import java.util.List;

/**
 * This interface contains serializing and deserializing classes for chess
 *
 * @author HardCoded
 */
public interface ChessCodec {
	/** FEN utility namespace */
	class FEN {
		public static ChessBoard from(String text) {
			return ChessFEN.load(new ChessBoardImpl(), text);
		}
		
		public static ChessBoard load(ChessBoard board, String text) {
			return ChessFEN.load((ChessBoardImpl)board, text);
		}
		
		public static String get(ChessBoard board) {
			return ChessFEN.get((ChessBoardImpl)board);
		}
	}
	
	/** PGN utility namespace */
	class PGN {
		public static String get(String fen, List<ChessMove> moves) {
			PGNGame game = new PGNGame();
			game.setTag(PGNTag.FEN, fen);
			game.setMoves(moves);
			return ChessPGN.get(game);
		}
		
		public static String get(PGNGame game) {
			return ChessPGN.get(game);
		}
		
		public static PGNGame from(String text) {
			return ChessPGN. from(text);
		}
	}
}

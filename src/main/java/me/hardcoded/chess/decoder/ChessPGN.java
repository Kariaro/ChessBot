package me.hardcoded.chess.decoder;

import me.hardcoded.chess.advanced.CastlingFlags;
import me.hardcoded.chess.advanced.ChessBoardImpl;
import me.hardcoded.chess.advanced.ChessGenerator;
import me.hardcoded.chess.advanced.ChessPieceManager;
import me.hardcoded.chess.api.ChessMove;
import me.hardcoded.chess.open.Pieces;
import me.hardcoded.chess.utils.ChessUtils;

import java.util.List;

/**
 * Internal class for handling PGN
 *
 * @author HardCoded
 */
public class ChessPGN {
	// TODO: Make sure the PGN generator works for all inputs
	//       this is really important for both importing and saving games on the disk
	
	// TODO: This method should follow the
	static String get(PGNGame game) {
		String fen = game.getTag(PGNTag.FEN);
		ChessBoardImpl board = new ChessBoardImpl(fen == null ? "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0" : fen);
		List<ChessMove> moves = game.moves;
		
		StringBuilder sb = new StringBuilder();
		for (PGNTag tag : PGNTag.values()) {
			String value = game.getTag(tag);
			if (value != null) {
				sb.append("[").append(tag.name()).append(" \"").append(value).append("\"]\n");
			}
		}
		sb.append("\n");
		
		for (int i = 0, len = moves.size(); i < len; i++) {
			ChessMove move = moves.get(i);
			
			int prevPiece = board.getPiece(move.to);
			String unique = CodecHelper.getUniquePiecePosition(board, move);
			if (!ChessGenerator.playMove(board, move)) {
				// Invalid match
				return null;
			}
			
			if ((i & 1) == 0) {
				sb.append(1 + (i / 2)).append(". ");
			}
			
			int pieceSq = move.piece * move.piece;
			int type = move.special & 0b11_000000;
			
			switch (type) {
				case ChessPieceManager.SM_CASTLING -> {
					sb.append(((move.special & CastlingFlags.ANY_CASTLE_K) != 0) ? "O-O" : "O-O-O");
				}
				case ChessPieceManager.SM_NORMAL -> {
					if (pieceSq != Pieces.PAWN_SQ) {
						char c = Pieces.printable(Math.abs(move.piece));
						sb.append(c);
					}
					
					if (prevPiece != Pieces.NONE) {
						if (pieceSq == Pieces.PAWN_SQ) {
							sb.append(ChessUtils.toFileChar(move.from & 7));
						} else {
							sb.append(unique);
						}
						sb.append('x');
					} else {
						sb.append(unique);
					}
					
					sb.append(ChessUtils.toSquare(move.to));
				}
				case ChessPieceManager.SM_EN_PASSANT -> {
					char from = ChessUtils.toColumn(move.from);
					sb.append(from).append('x').append(ChessUtils.toSquare(move.to));
				}
				case ChessPieceManager.SM_PROMOTION -> {
					char c = Pieces.printable(Math.abs(move.special & 0b111000) >> 3);
					sb.append(ChessUtils.toSquare(move.to)).append('=').append(c);
				}
			}
			
			if (CodecHelper.isGameCheckmate(board)) {
				sb.append('#');
			} else if (CodecHelper.isKingChecked(board)) {
				sb.append('+');
			}
			
			sb.append(' ');
		}
		
		return sb.toString();
	}
	
	static PGNGame from(String text) {
		// TODO: Implement
		return null;
	}
}

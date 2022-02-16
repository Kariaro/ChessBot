package me.hardcoded.chess.decoder;

import me.hardcoded.chess.advanced.ChessBoardImpl;
import me.hardcoded.chess.advanced.ChessGenerator;
import me.hardcoded.chess.advanced.ChessPieceManager;
import me.hardcoded.chess.api.ChessMove;
import me.hardcoded.chess.utils.ChessUtils;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is package private and is only used for chess Codecs
 *
 * @author HardCoded
 */
class CodecHelper {
	/**
	 * Returns the text added before a {@link ChessCodec.PGN} move.
	 *
	 * For some moves two or more pieces can reach the same square and if those
	 * pieces also have the same type we need to calculate what values are needed
	 * for them to be read correctly.
	 */
	public static String getUniquePiecePosition(ChessBoardImpl board, ChessMove move) {
		// First calculate all moves and filter them for the target square and piece type
		Set<ChessMove> moves = ChessGenerator.generateGuiMoves(board).stream()
			.filter(m -> m.to == move.to && m.piece == move.piece && m.from != move.from)
			.collect(Collectors.toSet());
		
		int rank = move.from >> 3;
		int file = move.from & 7;
		boolean sameRank = false;
		boolean sameFile = false;
		
		for (ChessMove m : moves) {
			int mRank = m.to >> 3;
			int mFile = m.to & 7;
			sameRank |= (rank == mRank);
			sameFile |= (file == mFile);
		}
		
		// If we have a piece on the same file we need to specify the rank
		// If we have a piece on the same rank we need to specify the file
		return (sameRank ? ChessUtils.toFileChar(file) : "") + "" + (sameFile ? ChessUtils.toRankChar(rank) : "");
	}
	
	/**
	 * Returns if the last move played checked the king.
	 */
	public static boolean isKingChecked(ChessBoardImpl board) {
		return ChessPieceManager.isKingAttacked(board, board.isWhite());
	}
	
	/**
	 * Returns if the last move played resulted in a checkmate.
	 */
	public static boolean isGameCheckmate(ChessBoardImpl board) {
		if (!ChessPieceManager.isKingAttacked(board, board.isWhite())) {
			return false;
		}
		
		boolean[] hasMoves = new boolean[1];
		ChessGenerator.generate(board, (fromIdx, toIdx, special) -> {
			if (!ChessGenerator.isValid(board, fromIdx, toIdx, special)) {
				return true;
			}
			
			hasMoves[0] = true;
			// Stop generating moves because we found a valid move
			return false;
		});
		
		return !hasMoves[0];
	}
}

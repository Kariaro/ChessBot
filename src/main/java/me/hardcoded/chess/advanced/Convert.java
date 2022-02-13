package me.hardcoded.chess.advanced;

import me.hardcoded.chess.open.Action;
import me.hardcoded.chess.open.Flags;
import me.hardcoded.chess.open.Pieces;
import me.hardcoded.chess.open2.Chess;

@Deprecated(forRemoval = true)
public class Convert {
	public static ChessBoard toChessBoard(Chess chess) {
		ChessBoard board = new ChessBoard();
		me.hardcoded.chess.open.Move chess_move = chess.last_move;
		if (chess_move.action() == Action.PAWN_JUMP) {
			board.lastPawn = chess_move.to();
		} else {
			board.lastPawn = 0;
		}
		// TODO: Calculate the correct move index
		board.halfMove = chess.isWhiteTurn() ? 0 : 1;
		System.arraycopy(chess.board, 0, board.pieces, 0, 64);
		board.flags = 0;
		
		if (chess.isFlagSet(Flags.CASTLE_WK)) board.flags |= CastlingFlags.WHITE_CASTLE_K;
		if (chess.isFlagSet(Flags.CASTLE_WQ)) board.flags |= CastlingFlags.WHITE_CASTLE_Q;
		if (chess.isFlagSet(Flags.CASTLE_BK)) board.flags |= CastlingFlags.BLACK_CASTLE_K;
		if (chess.isFlagSet(Flags.CASTLE_BQ)) board.flags |= CastlingFlags.BLACK_CASTLE_Q;
		
		// Masks
		{
			board.whiteMask = 0;
			board.blackMask = 0;
			for (int i = 0; i < 64; i++) {
				long idx = 1L << (long)i;
				int piece = board.pieces[i];
				if (piece > 0) {
					board.whiteMask |= idx;
				}
				
				if (piece < 0) {
					board.blackMask |= idx;
				}
			}
			board.pieceMask = board.whiteMask | board.blackMask;
		}
		
		return board;
	}
	
	public static Chess toChess(ChessBoard chessBoard) {
		boolean isWhite = chessBoard.isWhite();
		
		Chess chess = new Chess();
		System.arraycopy(chessBoard.pieces, 0, chess.board, 0, 64);
		chess.flags = 0;
		chess.flags |= isWhite ? Flags.TURN : 0;
		
		if (chessBoard.hasFlags(CastlingFlags.WHITE_CASTLE_K)) chess.flags |= Flags.CASTLE_WK;
		if (chessBoard.hasFlags(CastlingFlags.WHITE_CASTLE_Q)) chess.flags |= Flags.CASTLE_WQ;
		if (chessBoard.hasFlags(CastlingFlags.BLACK_CASTLE_K)) chess.flags |= Flags.CASTLE_BK;
		if (chessBoard.hasFlags(CastlingFlags.BLACK_CASTLE_Q)) chess.flags |= Flags.CASTLE_BQ;
		
		if (chessBoard.lastPawn != 0) {
			int from = chessBoard.lastPawn + (isWhite ? -8 : 8);
			int to = chessBoard.lastPawn;
			chess.last_move = me.hardcoded.chess.open.Move.of(Pieces.PAWN * (isWhite ? -1 : 1), from, to, Action.PAWN_JUMP);
		}
		
		return chess;
	}
}

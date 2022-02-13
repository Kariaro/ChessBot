package me.hardcoded.chess.utils;

import me.hardcoded.chess.advanced.CastlingFlags;
import me.hardcoded.chess.advanced.ChessBoard;
import me.hardcoded.chess.open.Action;
import me.hardcoded.chess.open.Flags;
import me.hardcoded.chess.open.Pieces;
import me.hardcoded.chess.open2.Chess;

@Deprecated(forRemoval = true)
public class Convert {
	public static ChessBoard toChessBoard(Chess chess) {
		ChessBoard board = new ChessBoard();
		me.hardcoded.chess.open.Move chess_move = chess.last_move;
		int[] pieces = new int[64];
		int lastPawn = 0;
		if (chess_move.action() == Action.PAWN_JUMP) {
			lastPawn = chess_move.to();
		} else {
			lastPawn = 0;
		}
		
		int halfMove = chess.isWhiteTurn() ? 0 : 1;
		System.arraycopy(chess.board, 0, pieces, 0, 64);
		
		int flags = 0;
		if (chess.isFlagSet(Flags.CASTLE_WK)) flags |= CastlingFlags.WHITE_CASTLE_K;
		if (chess.isFlagSet(Flags.CASTLE_WQ)) flags |= CastlingFlags.WHITE_CASTLE_Q;
		if (chess.isFlagSet(Flags.CASTLE_BK)) flags |= CastlingFlags.BLACK_CASTLE_K;
		if (chess.isFlagSet(Flags.CASTLE_BQ)) flags |= CastlingFlags.BLACK_CASTLE_Q;
		
		board.setStates(flags, halfMove,  lastPawn, 0, pieces);
		return board;
	}
	
	public static Chess toChess(ChessBoard chessBoard) {
		boolean isWhite = chessBoard.isWhite();
		
		Chess chess = new Chess();
		System.arraycopy(chessBoard.getPieces(), 0, chess.board, 0, 64);
		chess.flags = 0;
		chess.flags |= isWhite ? Flags.TURN : 0;
		
		if (chessBoard.hasFlags(CastlingFlags.WHITE_CASTLE_K)) chess.flags |= Flags.CASTLE_WK;
		if (chessBoard.hasFlags(CastlingFlags.WHITE_CASTLE_Q)) chess.flags |= Flags.CASTLE_WQ;
		if (chessBoard.hasFlags(CastlingFlags.BLACK_CASTLE_K)) chess.flags |= Flags.CASTLE_BK;
		if (chessBoard.hasFlags(CastlingFlags.BLACK_CASTLE_Q)) chess.flags |= Flags.CASTLE_BQ;
		
		if (chessBoard.getLastPawn() != 0) {
			int from = chessBoard.getLastPawn() + (isWhite ? -8 : 8);
			int to = chessBoard.getLastPawn();
			chess.last_move = me.hardcoded.chess.open.Move.of(Pieces.PAWN * (isWhite ? -1 : 1), from, to, Action.PAWN_JUMP);
		}
		
		return chess;
	}
}

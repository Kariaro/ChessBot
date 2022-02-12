package me.hardcoded.chess.open;

import static me.hardcoded.chess.open.Pieces.*;

public class ChessUtils {
	public static Piece toPiece(int pieceId) {
		if(pieceId < 0) pieceId = -pieceId;
		switch(pieceId) {
			case KING: return Piece.KING;
			case QUEEN: return Piece.QUEEN;
			case BISHOP: return Piece.BISHOP;
			case KNIGHT: return Piece.KNIGHT;
			case ROOK: return Piece.ROOK;
			case PAWN: return Piece.PAWN;
			default: return Piece.NONE;
		}
	}

	public static String toSquare(int index) {
		return (char)('a' + (index & 7)) + "" + ((index / 8) + 1);
	}
	
	public static void printBoard(int[] board) {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < 64; i++) {
			if((i % 8) == 0) sb.append('\n');
			
			int pieceId = board[i];
			Piece p = toPiece(pieceId);
			char letter = p.letter();
			if(p == Piece.PAWN) letter = 'p';
			
			if(pieceId > 0) letter = Character.toUpperCase(letter);
			if(pieceId == 0) letter = ' ';
			sb.append(letter).append(" ");
		}
		
		String str = sb.toString().substring(1);
		System.out.println(str);
	}
}

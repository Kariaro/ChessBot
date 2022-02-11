package me.hardcoded.chess.advanced;

import me.hardcoded.chess.open.Pieces;
import me.hardcoded.chess.open2.Chess;

public class Test {
	// TODO: Validate this chess board compared to the old one
	public static void main(String[] args) {
		ChessBoard board = new ChessBoard();
		
		//long start = System.nanoTime();
//		for(int i = 0; i < 100000; i++) {
//		}
		ChessGenerator.generate(board, (fromIdx, toIdx, special) -> {
			// This will generate all legal moves. Check if the move actually is legal
			if (!ChessGenerator.isValid(board, fromIdx, toIdx, special)) {
				return;
			}
			
			int piece = board.getPiece(fromIdx);
			if (special == 0) {
				System.out.printf("%9s: from %s, to %s\n", Pieces.toString(piece), ChessUtils.toSquare(fromIdx), ChessUtils.toSquare(toIdx));
			} else {
				int fields = special & 0b00111111;
				int type   = special & 0b11000000;
				switch (type >> 6) {
					case 1, 2 -> {
						System.out.printf("    ----: (%s) (%s) %s\n\n", ChessUtils.toSpecialString(type), ChessUtils.toSquare(fields), ChessUtils.toBitString(special));
					}
					case 3 -> {
						System.out.printf("    ----: (%s) (%s)\n\n", ChessUtils.toSpecialString(type), ChessUtils.toCastlingMove(fields));
					}
				}
			}
		});
		
		//long ellapsed = System.nanoTime() - start;
		//System.out.printf("Took: %.2f ms", ellapsed / 1000000.0f);
	}
}

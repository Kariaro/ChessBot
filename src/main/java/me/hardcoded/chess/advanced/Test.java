package me.hardcoded.chess.advanced;

import javax.swing.*;

public class Test {
	// TODO: Validate this chess board compared to the old one
	public static void main(String[] args) {
		ChessBoard board = new ChessBoard();
		
		//long start = System.nanoTime();
//		for(int i = 0; i < 100000; i++) {
//		}
//		ChessGenerator.generate(board, (fromIdx, toIdx, special) -> {
//			// This will generate all legal moves. Check if the move actually is legal
//			if (!ChessGenerator.isValid(board, fromIdx, toIdx, special)) {
//				return true;
//			}
//
//			int piece = board.getPiece(fromIdx);
//			if (special == 0) {
//				System.out.printf("%9s: from %s, to %s\n", Pieces.toString(piece), ChessUtils.toSquare(fromIdx), ChessUtils.toSquare(toIdx));
//			} else {
//				int fields = special & 0b00111111;
//				int type   = special & 0b11000000;
//				switch (type) {
//					case ChessPieceManager.SM_EN_PASSANT -> {
//						System.out.printf("    ----: (%s) (%s) %s\n\n", ChessUtils.toSpecialString(type), ChessUtils.toSquare(fields), ChessUtils.toBitString(special));
//					}
//					case ChessPieceManager.SM_PROMOTION -> {
//						System.out.printf("    ----: (%s) (%s) %s\n\n", ChessUtils.toSpecialString(type), ChessUtils.toSquare(toIdx), ChessUtils.toBitString(special));
//					}
//					case ChessPieceManager.SM_CASTLING -> {
//						System.out.printf("    ----: (%s) (%s)\n\n", ChessUtils.toSpecialString(type), ChessUtils.toCastlingMove(fields));
//					}
//				}
//			}
//
//			return true;
//		});
		
		JFrame frame = new JFrame();
		frame.add(ChessPieceManager.BOARD_PANEL);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		while (true) {
//			ChessGenerator.debug("Move " + board.halfMove, board.pieces);
			frame.setTitle("Move " + board.halfMove);
			ChessPieceManager.BOARD_PANEL.setTargets(board.pieces);
//			AnalyserTool.Scan0 scan0 = AnalyserTool.analyse(board);
//			AnalyserTool.Move best = scan0.best.move;
//			ChessGenerator.playMove(board, best.from, best.to, best.special);
			
//			AnalyserTool1.Scan0 scan0 = AnalyserTool1.analyse(board);
//			AnalyserTool1.Move best = scan0.best.move;
//			ChessGenerator.playMove(board, best.from, best.to, best.special);
			
			AnalyserTool2.Scanner scan0 = AnalyserTool2.analyse(board);
			AnalyserTool2.Move best = scan0.best;
			ChessGenerator.playMove(board, best.from, best.to, best.special);
			System.out.println(best.material);
			
			// TODO: Castling does not seem to work
			me.hardcoded.chess.open.ChessUtils.printBoard(board.pieces);
			System.out.println();
			
//			ChessGenerator.generate(board, (fromIdx, toIdx, special) -> {
//				// This will generate all legal moves. Check if the move actually is legal
//				if(!ChessGenerator.isValid(board, fromIdx, toIdx, special)) {
//					return true;
//				}
//
//				ChessGenerator.playMove(board, fromIdx, toIdx, special);
//
//				return false;
//			});
		}
		
		//long ellapsed = System.nanoTime() - start;
		//System.out.printf("Took: %.2f ms", ellapsed / 1000000.0f);
	}
}

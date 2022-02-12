package me.hardcoded.chess.advanced;

import me.hardcoded.chess.open2.Analyser7;
import me.hardcoded.chess.open2.Chess;

import javax.swing.*;

public class Test {
	public static void main(String[] args) throws Exception {
		ChessBoard board = new ChessBoard();
		
		JFrame frame = new JFrame();
		frame.add(ChessPieceManager.BOARD_PANEL);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		int index = 0;
		frame.setTitle("New Move " + index++);
		ChessPieceManager.BOARD_PANEL.setTargets(board.pieces);
		while (true) {
			
			AnalyserTool3.Scanner scan0 = AnalyserTool3.analyse(board);
			AnalyserTool3.Move best = scan0.best;
			ChessGenerator.playMove(board, best.from, best.to, best.special);
			System.out.println(best.material);
			
			frame.setTitle("New Move " + index++);
			ChessPieceManager.BOARD_PANEL.setTargets(board.pieces);
			
//			AnalyserTool2.Scanner scan0 = AnalyserTool2.analyse(board);
//			AnalyserTool2.Move best = scan0.best;
//			ChessGenerator.playMove(board, best.from, best.to, best.special);
//			System.out.println(best.material);
			
			Chess chess = Convert.toChess(board);
			System.out.println(chess.isWhiteTurn());
			chess.doMove(Analyser7.analyse(chess).best.move);
			board = Convert.toChessBoard(chess);
			
			frame.setTitle("Old Move " + index++);
			ChessPieceManager.BOARD_PANEL.setTargets(board.pieces);
			
			// TODO: Castling does not seem to work
//			me.hardcoded.chess.open.ChessUtils.printBoard(board.pieces);
			System.out.println(best);
			
//			Thread.sleep(5000);
		}
	}
}

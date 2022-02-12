package me.hardcoded.chess.advanced;

import me.hardcoded.chess.open.Analyser;
import me.hardcoded.chess.open2.Analyser7;
import me.hardcoded.chess.open2.Chess;
import me.hardcoded.chess.visual.ChessBoardPanel;

import javax.swing.*;
import java.awt.*;

public class Test {
	public static void main(String[] args) throws Exception {
		ChessBoard board = new ChessBoard();
		
		ChessBoardPanel panel = new ChessBoardPanel();
		JFrame frame = new JFrame();
		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		int index = 0;
		frame.setTitle("New Move " + index++);
		frame.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
		panel.setTargets(board.pieces);
		frame.repaint();
		
		Thread.sleep(1000);
		while (true) {
			
			AnalyserTool3.Scanner scan = AnalyserTool3.analyse(board);
			if (scan.best == null) {
				// The other computer won
				break;
			}
			AnalyserTool3.Move best = scan.best;
			ChessGenerator.playMove(board, best.from, best.to, best.special);
			System.out.println(best.material);
			
			frame.setTitle("New Move " + index++);
			panel.setTargets(board.pieces);
			
//			AnalyserTool2.Scanner scan0 = AnalyserTool2.analyse(board);
//			AnalyserTool2.Move best = scan0.best;
//			ChessGenerator.playMove(board, best.from, best.to, best.special);
//			System.out.println(best.material);
			
			Chess chess = Convert.toChess(board);
			Analyser.Move0 bestMove = Analyser7.analyse(chess).best;
			if (bestMove != null) {
				chess.doMove(bestMove.move);
			} else {
				// Game was won by the other player
				break;
			}
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

package me.hardcoded.chess.advanced;

import me.hardcoded.chess.api.ChessAnalysis;
import me.hardcoded.chess.decoder.ChessCodec;
import me.hardcoded.chess.visual.PlayableChessBoard;

import javax.swing.*;
import java.awt.*;

// TODO: http://wbec-ridderkerk.nl/html/UCIProtocol.html
public class Test {
	public static void main(String[] args) throws Exception {
		ChessBoard board = new ChessBoard();
		
		PlayableChessBoard panel = new PlayableChessBoard();
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
		
		final boolean customPlayer = true;

		Thread.sleep(1000);
		
		ChessAnalysis analysis;
		while (true) {
			analysis = AnalyserTool3.analyse(board);
			if (!ChessGenerator.playMove(board, analysis.getBestMove())) {
				// We probably lost the game
				break;
			}
			
			System.out.println("Material: " + analysis.getMaterial());

			frame.setTitle("New Move " + index++);
			panel.setTargets(board.pieces);

			if (customPlayer) {
				AnalyserConverted7.ScanConverted scan2 = AnalyserConverted7.analyse(board);
				if (scan2.best == null) {
					break;
				}
				best = scan2.best.move;
				ChessGenerator.playMove(board, best.from, best.to, best.special);

				frame.setTitle("Old Move " + index++);
				panel.setTargets(board.pieces);
			}

			me.hardcoded.chess.open.ChessUtils.printBoard(board.pieces);
			System.out.println(best);
			ChessCodec.FEN.save(board);
		}
	}
}

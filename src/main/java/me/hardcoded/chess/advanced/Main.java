package me.hardcoded.chess.advanced;

import me.hardcoded.chess.decoder.ChessCodec;
import me.hardcoded.chess.decoder.PGNGame;
import me.hardcoded.chess.decoder.PGNTag;
import me.hardcoded.chess.visual.PlayableChessBoard;

import javax.swing.*;

// TODO: http://wbec-ridderkerk.nl/html/UCIProtocol.html
public class Main {
	public static void main(String[] args) throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ChessBoard board = new ChessBoard();
//		board = ChessCodec.FEN.from("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0");
//		board = ChessCodec.FEN.from("8/8/RB5r/6rk/2P4p/1P6/P4P2/1K3R2 w - - 7 39");
		
		PlayableChessBoard panel = new PlayableChessBoard();
		panel.setDisplayedBoard(board);
		
		JFrame frame = new JFrame(ChessCodec.FEN.get(board));
		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		PGNGame pgnGame = new PGNGame();
		pgnGame.setTag(PGNTag.FEN, ChessCodec.FEN.get(board));
		pgnGame.setTag(PGNTag.White, "AnalyserTool4");
		pgnGame.setTag(PGNTag.Black, "AnalyserTool4");
		
		while (true) {
//			ChessAnalysis analysis = AnalyserTool4.analyseTest(board);
//			if (!ChessGenerator.playMove(board, analysis.getBestMove())) {
//				// The game ended
//				break;
//			}
//			System.out.println("\nMaterial: " + analysis.getMaterial());
//			pgnGame.addMove(analysis.getBestMove());
			pgnGame.addMove(panel.awaitMoveNonNull());
			
			frame.setTitle(ChessCodec.FEN.get(board));
			
			System.out.println(ChessCodec.FEN.get(board));
			System.out.println(ChessCodec.PGN.get(pgnGame));
		}
	}
}

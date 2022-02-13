package me.hardcoded.chess.advanced;

import me.hardcoded.chess.api.ChessAnalysis;
import me.hardcoded.chess.api.ChessMove;
import me.hardcoded.chess.decoder.ChessCodec;
import me.hardcoded.chess.visual.PlayableChessBoard;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

// TODO: http://wbec-ridderkerk.nl/html/UCIProtocol.html
public class Test {
	public static void main(String[] args) throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ChessBoard board = new ChessBoard();
//		board = ChessCodec.FEN.load("6k1/1p1r2p1/n4pp1/8/4P1n1/8/PPP2PPP/R4RK1 w - - 138 94");
//		board = ChessCodec.FEN.from("rnbqkbnr/pppp1ppp/8/4p3/8/4P3/PPPP1PPP/RNBQKBNR w KQkq - 0 2");
//		board = ChessCodec.FEN.from("r1bqk2r/p4ppp/2p4n/1p1pP3/1n6/4PN2/PPP1BPPP/RN1QK2R w KQkq d6 0 10");
//		board = ChessCodec.FEN.from("rnbqkbnr/pppp1ppp/4p3/8/8/4P3/PPPP1PPP/RNBQKBNR w KQkq - 0 1");
//		board = ChessCodec.FEN.from("r1bqk2r/1pp2ppp/p1pb1n2/4p3/8/3PPN2/PPPB1PPP/RN1QK2R w KQkq - 2 6");
//		board = ChessCodec.FEN.from("r3k2r/1ppq1ppp/p1pbbn2/4p3/8/2NPPN1P/PPPB1PP1/R2QK2R w KQkq - 3 8");
		board = ChessCodec.FEN.from("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0");
		board = ChessCodec.FEN.from("r7/8/4K3/8/8/4k3/8/R7 w - - 0 0");
		board = ChessCodec.FEN.from("k7/1p6/8/8/8/8/P7/K7 w - - 0 1");
		board = ChessCodec.FEN.from("8/P2k4/8/8/8/8/1p1K4/8 w - - 0 0");
		String startFen = ChessCodec.FEN.get(board);
		
		System.out.println(ChessCodec.FEN.get(board));
		
		PlayableChessBoard panel = new PlayableChessBoard();
		JFrame frame = new JFrame();
		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		int index = 0;
		frame.setTitle("New Move " + index++);
		panel.setDisplayedBoard(board);
		frame.repaint();
		
		boolean customPlayer = true;
		ChessAnalysis analysis;
		
		boolean startWhiteTest = false;
		
		List<ChessMove> moves = new ArrayList<>();
		
		while (true) {
//			if (!startWhiteTest) {
//				analysis = AnalyserTool4.analyseTest(board);
//				if (!ChessGenerator.playMove(board, analysis.getBestMove())) {
//					// The game ended
//					break;
//				}
//				moves.add(analysis.getBestMove());
//
//				System.out.println("Material: " + analysis.getMaterial());
//				frame.setTitle("New Move " + index++);
//				panel.setTargets(1L << analysis.getBestMove().from | 1L << analysis.getBestMove().to);
//			}
//			startWhiteTest = false;
			
//			if (customPlayer) {
//				analysis = AnalyserConverted7.analyse(board);
//				if (!ChessGenerator.playMove(board, analysis.getBestMove())) {
//					// The game ended
//					break;
//				}
//
//				frame.setTitle("Old Move " + index++);
//			}
			
			moves.add(panel.awaitMoveNonNull());
			System.out.println(ChessPieceManager.isKingAttacked(board, true));
			System.out.println(ChessPieceManager.isKingAttacked(board, false));
			
			System.out.println(ChessCodec.PGN.get(startFen, moves));

			me.hardcoded.chess.open.ChessUtils.printBoard(board.pieces);
			System.out.println(ChessCodec.FEN.get(board));
//			System.out.println(best);
		}
	}
}

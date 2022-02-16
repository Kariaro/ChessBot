package me.hardcoded.chess.advanced;

import me.hardcoded.chess.analysis.AnalyserTool4;
import me.hardcoded.chess.analysis.AnalyserV1_AlphaBetaPruning;
import me.hardcoded.chess.api.ChessAnalysis;
import me.hardcoded.chess.api.ChessBoard;
import me.hardcoded.chess.api.ChessMove;
import me.hardcoded.chess.decoder.ChessCodec;
import me.hardcoded.chess.decoder.PGNGame;
import me.hardcoded.chess.decoder.PGNTag;
import me.hardcoded.chess.open.ChessUtilsOld;
import me.hardcoded.chess.utils.ChessUtils;
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
		
		final ChessBoard board;// = new ChessBoardImpl();
//		board = ChessCodec.FEN.from("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0");
//		board = ChessCodec.FEN.from("8/8/RB5r/6rk/2P4p/1P6/P4P2/1K3R2 w - - 7 39");
//		board = ChessCodec.FEN.from("rnk5/pb4r1/1ppQ1R2/5P2/1P4P1/P2B2P1/2PP1N2/R3K3 w Q - 1 29");
		board = ChessCodec.FEN.from("r6r/pp1k1p1p/4pq2/2ppnn2/1b3Q2/2N1P2N/PPPP1PPP/R1B1K2R w KQ - 0 12");
		
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
		pgnGame.setTag(PGNTag.Black, "AnalyserV1_AlphaBetaPruning");
		
		ChessAnalysis analysis;
		
		ChessGenerator.generate(board,false, (a, b, c) -> {
			if (!ChessGenerator.isValid(board, a, b, c)) {
				return true;
			}
			
			System.out.println(new ChessMove(board.getPiece(a), a, b, c));
			return true;
		});
		
		while (true) {
			analysis = AnalyserV1_AlphaBetaPruning.analyseTest(board);
			if (!ChessGenerator.playMove(board, analysis.getBestMove())) {
				// The game ended
				break;
			}
			pgnGame.addMove(analysis.getBestMove());
			frame.setTitle(ChessCodec.FEN.get(board));
			
			System.out.println(ChessCodec.FEN.get(board));
			System.out.println(ChessCodec.PGN.get(pgnGame));
			
			analysis = AnalyserTool4.analyseTest(board);
			if (!ChessGenerator.playMove(board, analysis.getBestMove())) {
				// The game ended
				break;
			}
			pgnGame.addMove(analysis.getBestMove());
			frame.setTitle(ChessCodec.FEN.get(board));
			
			System.out.println(ChessCodec.FEN.get(board));
			System.out.println(ChessCodec.PGN.get(pgnGame));
			
//			System.out.println("\nMaterial: " + analysis.getMaterial());
//			pgnGame.addMove(panel.awaitMoveNonNull());
//			frame.setTitle(ChessCodec.FEN.get(board));
//
//			System.out.println(ChessCodec.FEN.get(board));
//			System.out.println(ChessCodec.PGN.get(pgnGame));
			ChessUtilsOld.printBoard(((ChessBoardImpl)board).pieces);
		}
		
		System.out.println("-----------------------------------------");
		System.out.println(ChessCodec.FEN.get(board));
		System.out.println(ChessCodec.PGN.get(pgnGame));
	}
}

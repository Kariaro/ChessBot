package me.hardcoded.chess.advanced;

import me.hardcoded.chess.analysis.AnalyserTool4;
import me.hardcoded.chess.analysis.AnalyserV1_AlphaBetaPruning;
import me.hardcoded.chess.analysis.AnalyserV2_AlphaBetaPruning;
import me.hardcoded.chess.api.ChessAnalysis;
import me.hardcoded.chess.api.ChessBoard;
import me.hardcoded.chess.decoder.ChessCodec;
import me.hardcoded.chess.decoder.PGNGame;
import me.hardcoded.chess.decoder.PGNTag;
import me.hardcoded.chess.open.ChessUtilsOld;
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
		
		PGNGame pgnGame = new PGNGame();
		ChessBoard board;
//		board = ChessCodec.FEN.from("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0");
//		board = ChessCodec.FEN.from("8/8/RB5r/6rk/2P4p/1P6/P4P2/1K3R2 w - - 7 39");
//		PGNGame pgnGame = ChessCodec.PGN.from("""
//			[White "AnalyserTool4"]
//			[Black "AnalyserV1_AlphaBetaPruning"]
//			[FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0"]
//
//			1. e3 e5 2. Nc3 Nf6 3. Nf3 d6 4. d3 Nc6 5. Be2 Be6 6. Bd2 d5 7. Qc1 e4 8. Nd4 Nxd4 9. exd4 Qd7 10. dxe4 dxe4 11. Bg5 Qxd4 12. O-O Bd6 13. Rd1 Qe5 14. Bb5+ c6 15. g3 cxb5 16. Bf4 Qh5 17. Bxd6 e3 18. fxe3 Ng4 19. Qd2 O-O-O 20. Nxb5 Qxb5 21. Qd4 Qc6 22. Qxa7 Rxd6 23. h3 Rxd1+ 24. Rxd1 Qxc2 25. Qa8+ Kc7 26. Qa5+ Kc6 27. Qd2 Qxd2 28. Rxd2 Nxe3 29. Kf2 Nd5 30. Kg1 Bxh3 31. Kh1 Be6 32. Kg1 b5 33. a3 Ra8 34. Rd1 h5 35. Rf1 Ne3 36. Re1 Nc4 37. Kh1 Nxb2 38. Kh2 Rxa3 39. Rc1+ Nc4 40. Rc2 b4 41. Kg2 b3 42. Rc1 b2 43. Rb1 Ra1 44. Rxb2 Nxb2 45. Kf2 Kd5 46. Kg2 Ke4 47. g4 Kf4 48. Kf2 Nc4 49. gxh5 Ra2+ 50. Kg1 Kg3 51. Kf1 Bg4 52. Kg1 Ra1#
//		""");
		
		board = pgnGame.getBoard(Integer.MAX_VALUE);
		
		System.out.println(ChessCodec.PGN.get(pgnGame));
		
		
		PlayableChessBoard panel = new PlayableChessBoard();
		panel.setDisplayedBoard(board);
		
		JFrame frame = new JFrame(ChessCodec.FEN.get(board));
		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		pgnGame = new PGNGame();
		pgnGame.setTag(PGNTag.FEN, ChessCodec.FEN.get(board));
		pgnGame.setTag(PGNTag.White, "AnalyserTool4");
		pgnGame.setTag(PGNTag.Black, "AnalyserV1_AlphaBetaPruning");
		
		ChessAnalysis analysis;
		
		boolean delay = true;
		while (true) {
			if (!delay) {
				analysis = AnalyserV2_AlphaBetaPruning.analyseTest(board);
				if(!ChessGenerator.playMove(board, analysis.getBestMove())) {
					// The game ended
					break;
				}
				pgnGame.addMove(analysis.getBestMove());
				frame.setTitle(ChessCodec.FEN.get(board));
				
//				analysis = AnalyserTool4.analyseTest(board);
//				if (!ChessGenerator.playMove(board, analysis.getBestMove())) {
//					// The game ended
//					break;
//				}
//				pgnGame.addMove(analysis.getBestMove());
			}
			delay = false;
//
//			System.out.println(ChessCodec.FEN.get(board));
//			System.out.println(ChessCodec.PGN.get(pgnGame));
//
//			analysis = AnalyserTool4.analyseTest(board);
//			if (!ChessGenerator.playMove(board, analysis.getBestMove())) {
//				// The game ended
//				break;
//			}
//			pgnGame.addMove(analysis.getBestMove());
//			frame.setTitle(ChessCodec.FEN.get(board));
//
//			System.out.println(ChessCodec.FEN.get(board));
//			System.out.println(ChessCodec.PGN.get(pgnGame));
			
//			System.out.println("\nMaterial: " + analysis.getMaterial());
			pgnGame.addMove(panel.awaitMoveNonNull());
			frame.setTitle(ChessCodec.FEN.get(board));
//
//			System.out.println(ChessCodec.FEN.get(board));
			System.out.println(ChessCodec.PGN.get(pgnGame));
			ChessUtilsOld.printBoard(((ChessBoardImpl)board).pieces);
		}
		
		System.out.println("-----------------------------------------");
		System.out.println(ChessCodec.FEN.get(board));
		System.out.println(ChessCodec.PGN.get(pgnGame));
	}
}

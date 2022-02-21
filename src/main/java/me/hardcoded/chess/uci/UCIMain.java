package me.hardcoded.chess.uci;

import me.hardcoded.chess.advanced.ChessBoardImpl;
import me.hardcoded.chess.advanced.ChessGenerator;
import me.hardcoded.chess.advanced.ChessPieceManager;
import me.hardcoded.chess.analysis.AlphaBetaPruningV3;
import me.hardcoded.chess.analysis.AlphaBetaPruningV4;
import me.hardcoded.chess.api.ChessMove;
import me.hardcoded.chess.utils.ChessUtils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class UCIMain {
	public static void init(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);
		
		PrintStream output = System.out;
		System.setOut(new PrintStream(OutputStream.nullOutputStream()));
		
		ChessBoardImpl board = new ChessBoardImpl();
		// https://www.stmintz.com/ccc/index.php?id=141612
		while (true) {
			String command = scanner.nextLine();
			
			switch (command) {
				case "uci" -> {
					StringBuilder sb = new StringBuilder();
					// Identification
					sb.append("id name HardCodedBot 1.0\n");
					sb.append("id author HardCoded\n");
					sb.append("\n");
					
					// Options
					sb.append("option name Skill Level type spin default 20 min 0 max 20\n");
					sb.append("option name Move Overhead type spin default 1 min 1 max 4096\n");
					sb.append("option name Threads type spin default 1 min 1 max 512\n");
					sb.append("option name Hash type spin default 256 min 1 max 4096\n");
					
					// Uciok
					sb.append("uciok\n");
					
					output.print(sb);
				}
				case "isready" -> {
					output.print("readyok\n");
				}
				case "quit" -> {
					System.exit(0);
				}
			}
			
			if (command.startsWith("position startpos")) {
				command = command.substring("position startpos".length()).trim();
				
				if (command.startsWith("moves ")) {
					String[] moves = command.substring("moves ".length()).trim().split(" ");
					board = new ChessBoardImpl();
					playMoves(board, moves);
				}
				
				
				continue;
			}
			
			if (command.startsWith("go")) {
				ChessMove move = AlphaBetaPruningV4.analyseTest(board).getBestMove();
				output.print("info [" + move + "]\n");
				
				if (move != null) {
					String moveString = getMoveString(move);
					output.print("bestmove " + moveString + "\n");
				}
			}
		}
	}
	
	private static String getMoveString(ChessMove move) {
		if ((move.special & 0b11_000000) == ChessPieceManager.SM_CASTLING) {
			return ChessUtils.toSquare(move.from) + ChessUtils.toSquare(move.to);
		} else if ((move.special & 0b11_000000) == ChessPieceManager.SM_PROMOTION) {
			return ChessUtils.toSquare(move.from) + ChessUtils.toSquare(move.to) + Character.toLowerCase(Pieces.printable((move.special >> 3) & 7));
		}
		
		return ChessUtils.toSquare(move.from) + ChessUtils.toSquare(move.to);
	}
	
	private static void playMoves(ChessBoardImpl board, String[] moves) {
		for (final String move : moves) {
			final ChessMove[] found = new ChessMove[1];
			ChessGenerator.generate(board, false, (from, to, special) -> {
				if (!ChessGenerator.isValid(board, from, to, special)) {
					return true;
				}
				
				ChessMove chessMove = new ChessMove(board.getPiece(from), from, to, special);
				if (getMoveString(chessMove).equals(move)) {
					found[0] = chessMove;
					return false;
				}
				
				return true;
			});
			
			if (found[0] != null) {
				ChessGenerator.playMove(board, found[0].from, found[0].to, found[0].special);
			} else {
				throw new RuntimeException("INVALID MOVE [" + move + "]");
			}
		}
	}
}

package me.hardcoded.chess.api;

import me.hardcoded.chess.advanced.ChessBoard;

/**
 * This class is an interface for how chess analysers should be constructed.
 *
 * @author HardCoded
 */
public interface ChessAnalyser {
	
	/**
	 * Analyze the current state of the board.
	 *
	 * @param board the board to analyze
	 */
	ChessAnalysis analyse(ChessBoard board);
}

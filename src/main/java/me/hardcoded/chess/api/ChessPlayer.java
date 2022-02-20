package me.hardcoded.chess.api;

import java.util.function.Function;

public class ChessPlayer {
	private final Function<ChessBoard, ChessAnalysis> func;
	private final String name;
	private final boolean manual;
	
	public ChessPlayer(String name, Function<ChessBoard, ChessAnalysis> func, boolean manual) {
		this.name = name;
		this.func = func;
		this.manual = manual;
	}
	
	public ChessPlayer(String name, Function<ChessBoard, ChessAnalysis> func) {
		this(name, func, false);
	}
	
	public ChessAnalysis apply(ChessBoard board) {
		return func.apply(board);
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isManual() {
		return manual;
	}
}

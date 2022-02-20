package me.hardcoded.chess.decoder;

import me.hardcoded.chess.advanced.ChessBoardImpl;
import me.hardcoded.chess.advanced.ChessGenerator;
import me.hardcoded.chess.api.ChessBoard;
import me.hardcoded.chess.api.ChessMove;

import java.util.*;

public class PGNGame {
	final Map<String, String> tags;
	final List<ChessMove> moves;
	
	public PGNGame() {
		this.tags = new LinkedHashMap<>();
		this.moves = new ArrayList<>();
	}
	
	public PGNGame setTag(PGNTag type, String value) {
		return setTag(type.name(), value);
	}
	
	public String getTag(PGNTag type) {
		return getTag(type.name());
	}
	
	public String getTagOrDefault(PGNTag type, String defaultValue) {
		return getTagOrDefault(type.name(), defaultValue);
	}
	
	public PGNGame setTag(String name, String value) {
		tags.put(name, value);
		return this;
	}
	
	public String getTag(String name) {
		return tags.get(name);
	}
	
	public String getTagOrDefault(String name, String defaultValue) {
		return tags.getOrDefault(name, defaultValue);
	}
	
	public void setMoves(List<ChessMove> moves) {
		this.moves.clear();
		this.moves.addAll(moves);
	}
	
	public ChessBoard getBoard(int moves) {
		String fen = getTag(PGNTag.FEN);
		ChessBoardImpl board;
		if (fen != null) {
			board = new ChessBoardImpl(fen);
		} else {
			board = new ChessBoardImpl();
		}
		
		for (int i = 0; i < Math.min(this.moves.size(), moves); i++) {
			ChessMove move = this.moves.get(i);
			if (!ChessGenerator.playMove(board,move.from, move.to, move.special)) {
				return null;
			}
		}
		
		return board;
	}
	
	public List<ChessMove> getMoves() {
		return moves;
	}
	
	public void addMove(ChessMove move) {
		moves.add(move);
	}
}

package me.hardcoded.chess.decoder;

import me.hardcoded.chess.api.ChessMove;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PGNGame {
	Map<String, String> tags;
	List<ChessMove> moves;
	
	public PGNGame() {
		this.tags = new HashMap<>();
		this.moves = new ArrayList<>();
	}
	
	public PGNGame setTag(PGNTag type, String value) {
		tags.put(type.name(), value);
		return this;
	}
	
	public PGNGame removeTag(PGNTag type) {
		tags.remove(type.name());
		return this;
	}
	
	public PGNGame addTag(PGNTag type, String value) {
		return addCustomTag(type.name(), value);
	}
	
	public String getTag(PGNTag type) {
		return getCustomTag(type.name());
	}
	
	public String getTagOrDefault(PGNTag type, String defaultValue) {
		return getCustomTagOrDefault(type.name(), defaultValue);
	}
	
	public PGNGame addCustomTag(String name, String value) {
		if (value == null) {
			tags.remove(name);
		} else {
			tags.put(name, value);
		}
		return this;
	}
	
	public String getCustomTag(String name) {
		return tags.get(name);
	}
	
	public String getCustomTagOrDefault(String name, String defaultValue) {
		return tags.getOrDefault(name, defaultValue);
	}
	
	public void setMoves(List<ChessMove> moves) {
		this.moves.clear();
		this.moves.addAll(moves);
	}
	
	public List<ChessMove> getMoves() {
		return moves;
	}
	
	public void addMove(ChessMove move) {
		moves.add(move);
	}
}

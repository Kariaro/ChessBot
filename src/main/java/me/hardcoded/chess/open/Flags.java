package me.hardcoded.chess.open;

public interface Flags {
	int TURN			= (1 << 0);
	int CASTLE_WQ		= (1 << 1);
	int CASTLE_WK		= (1 << 2);
	int CASTLE_BQ		= (1 << 3);
	int CASTLE_BK		= (1 << 4);
	int CHECKED			= (1 << 5);
	
	
	int DEFAULT = TURN | CASTLE_WK | CASTLE_WQ | CASTLE_BK | CASTLE_BQ;
}

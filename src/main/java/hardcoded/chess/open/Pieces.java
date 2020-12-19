package hardcoded.chess.open;

/**
 * Negative values is black and positive is white
 */
public interface Pieces {
	int KING = 1;
	int QUEEN = 2;
	int BISHOP = 3;
	int KNIGHT = 4;
	int ROOK = 5;
	int PAWN = 6;
	
	static int value(int id) {
		if(id < 0) {
			id = -id;
			switch(id) {
				case KING: return 0;
				case QUEEN: return -9;
				case BISHOP: return -3;
				case KNIGHT: return -3;
				case ROOK: return -5;
				case PAWN: return -1;
				default: return 0;
			}
		}
		
		switch(id) {
			case KING: return 0;
			case QUEEN: return 9;
			case BISHOP: return 3;
			case KNIGHT: return 3;
			case ROOK: return 5;
			case PAWN: return 1;
			default: return 0;
		}
	}
}

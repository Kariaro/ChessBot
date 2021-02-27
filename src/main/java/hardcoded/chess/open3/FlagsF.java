package hardcoded.chess.open3;

public interface FlagsF {
	/** White castle kingside */	int WCK = 1;
	/** White castle queenside */	int WCQ = 2;
	/** White castling rights */    int WCR = WCK | WCQ;
	/** Black castle kingside */	int BCK = 4;
	/** Black castle queenside */	int BCQ = 8;
	/** Black castling rights */    int BCR = BCK | BCQ;
}

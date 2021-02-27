package hardcoded.chess.open3;

public final class UtilsF {
	public static String toBitString(long value) {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < 64; i++) {
			if((i & 7) == 0) sb.append("_");
			sb.append(((value >>> (long)i) & 1));
		}
		
		return sb.toString().substring(1);
	}
	
	public static String toSquare(int a) {
		return (char)('a' + (a & 7)) + "" + ((a / 8) + 1);
	}
	
	public static String toSpecialString(int mask) {
		switch(mask) {
			case ChessPM.sm_castling: return "Castling";
			case ChessPM.sm_en_passant: return "En Passant";
			case ChessPM.sm_promotion: return "Promotion";
		}
		return "unknown";
	}
}

package hardcoded.chess.open3;

public class Test {
	public static void main(String[] args) {
		ChessB board = new ChessB();
		
		//long start = System.nanoTime();
//		for(int i = 0; i < 100000; i++) {
//		}
		ChessM.generate(board);
		
		//long ellapsed = System.nanoTime() - start;
		//System.out.printf("Took: %.2f ms", ellapsed / 1000000.0f);
	}
}

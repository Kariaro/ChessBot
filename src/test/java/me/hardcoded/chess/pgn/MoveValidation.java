package me.hardcoded.chess.pgn;

import me.hardcoded.chess.advanced.ChessBoardImpl;
import me.hardcoded.chess.advanced.ChessGenerator;
import me.hardcoded.chess.advanced.ChessState;
import me.hardcoded.chess.api.ChessMove;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MoveValidation {
	
	private long getMoveCount(ChessBoardImpl board, int depth) {
		if (depth < 1) {
			return 1;
		}
		
		List<ChessMove> moves = new ArrayList<>(96);
		ChessGenerator.generate(board, false, (from, to, special) -> {
			if (!ChessGenerator.isValid(board, from, to, special)) {
				return true;
			}
			
			moves.add(new ChessMove(board.getPiece(from), from, to, special));
			return true;
		});
		
		ChessState old = ChessState.of(board);
		
		long count = 0;
		for (ChessMove move : moves) {
			if (!ChessGenerator.playMove(board, move.from, move.to, move.special)) {
				continue;
			}
			
			count += getMoveCount(board, depth - 1);
			old.write(board);
		}
		
		old.write(board);
		return count;
	}
	@Test
	public void testMoves() {
		long start, end;
		start = System.nanoTime();
		Assert.assertEquals(872389934L, getMoveCount(new ChessBoardImpl("r2qkb1r/1Q3pp1/pN1p3p/3P1P2/3pP3/4n3/PP4PP/1R3RK1 b - - 0 0"), 6));
		end = System.nanoTime();
		System.out.printf("Check 1 took: %.2f sec\n", (end - start) / 1000000000.0);
		
		start = System.nanoTime();
		Assert.assertEquals(103793326L, getMoveCount(new ChessBoardImpl("r6r/pp1k1p1p/4pq2/2ppnn2/1b3Q2/2N1P2N/PPPP1PPP/R1B1K2R w KQ - 0 12"), 5));
		end = System.nanoTime();
		System.out.printf("Check 2 took: %.2f sec\n", (end - start) / 1000000000.0);
	}
}

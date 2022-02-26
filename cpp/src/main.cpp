#include <stdio.h>
// #include "utils.h"
#include "fen_import.h"
#include "serial.h"
#include "generator.h"
#include "analyser.h"
#include "uci.h"
#include <chrono>

Chessboard board;
long moveCount;

long goDepth(Chessboard& parent, int depth) {
	if (depth < 2) {
		return 1;
	}

	Chessboard board = parent;

	long count = 0;
	for (Move move : Generator::generateValidMoves(board)) {
		if (!Generator::playMove(board, move)) {
			continue;
		}

		count += goDepth(board, depth - 1);
		board = parent;
	}

	return count;
}

long computePreft(Chessboard& parent, int depth) {
	Chessboard board = parent;

	printf("Checking depth = %d\n\n", depth);
	//auto start = std::chrono::high_resolution_clock::now();
	//long moveCount = computePreft(board, 6);
	//auto finish = std::chrono::high_resolution_clock::now();
	//auto timeTook = std::chrono::duration_cast<std::chrono::nanoseconds>(finish-start).count();
	
	auto start = std::chrono::high_resolution_clock::now();

	long totalCount = 0;
	for (Move move : Generator::generateValidMoves(board)) {
		if (!Generator::playMove(board, move)) {
			continue;
		}

		char* chars = Serial::getMoveString(move.from, move.to, move.special);
		long count = goDepth(board, depth);
		printf("%s: %d\n", chars, count);
		free(chars);

		totalCount += count;
		board = parent;
	}

	auto finish = std::chrono::high_resolution_clock::now();
	auto timeTook = std::chrono::duration_cast<std::chrono::nanoseconds>(finish-start).count();

	printf("\nTotal moves: %d\n", totalCount);
	printf("Moves: %d / sec\n", (long)(totalCount / (timeTook / 1000000000.0)));
	printf("Time: %.2f / sec\n", (timeTook / 1000000000.0));
	
	return totalCount;
}

int main(int argc, char** argv) {
	//int result = import_fen(&board, "r6r/pp1k1p1p/4pq2/2ppnn2/1b3Q2/2N1P2N/PPPP1PPP/R1B1K2R w KQ - 0 12");
	int result = import_fen(&board, "r2qkb1r/1Q3pp1/pN1p3p/3P1P2/3pP3/4n3/PP4PP/1R3RK1 b - - 0 0");
	// int result = import_fen(&board, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0");

	{
		char* buffer = export_fen(&board);
		printf("FEN: [%s]\n", buffer);
		free(buffer);
	}
	
	// TODO: Get this working
	UCI::StartUCI();

	/*
	char* chars = Serial::getBoardString(&board);
	printf("result: %d\n", result);
	printf("Board:\n%s\n", chars);
	printf("lastCapture: %d\n", board.lastCapture);
	printf("lastPawn: %d\n", board.lastPawn);
	printf("halfMove: %d\n", board.halfMove);
	printf("whiteMask: %lld\n", board.whiteMask);
	printf("blackMask: %lld\n", board.blackMask);
	printf("pieceMask: %lld\n", board.pieceMask);
	printf("\n");
	free(chars);
	*/

	// computePreft(board, 6);

	/*
	for (Move move : Generator::generateValidMoves(board)) {
		char* chars = Serial::getMoveString(move.piece, move.from, move.to, move.special);

		printf("move: [%s]\n", chars);
		free(chars);
	}
	*/

	// Move best = analyse(board);

	return 0;
}
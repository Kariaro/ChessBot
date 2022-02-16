#include <stdio.h>
// #include "utils.h"
#include "fen_import.h"
#include "serial.h"
#include "generator.h"
#include "analyser.h"

// TODO: Convert this project to 'C'

Chessboard board;

int main(int argc, char** argv) {
	int result = import_fen(&board, "r6r/pp1k1p1p/4pq2/2ppnn2/1b3Q2/2N1P2N/PPPP1PPP/R1B1K2R w KQ - 0 12");
	// int result = import_fen(&board, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 2147483648 2147483648");

	{
		char* buffer = export_fen(&board);
		printf("FEN: [%s]\n", buffer);
		free(buffer);
	}

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

	for (Move move : Generator::generate_moves(board)) {
		char* chars = Serial::getMoveString(move.piece, move.from, move.to, move.special);

		printf("move: [%s]\n", chars);
		free(chars);
	}

	Move best = analyse(board);

	free(chars);
	return 0;
}
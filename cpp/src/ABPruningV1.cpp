#pragma once

#ifndef ANALYSER_CPP
#define ANALYSER_CPP

#include "utils_type.h"
#include "utils.h"
#include "piece_manager.h"
#include "chessboard.h"
#include "generator.h"
#include "pieces.h"
#include "serial.h"
#include <chrono>

#define DEPTH 5
#define NEGATIVE_INFINITY -1000000000.0
#define POSITIVE_INFINITY  1000000000.0
#define max(a, b) (a > b ? a : b)
#define min(a, b) (a < b ? a : b)

Move MOVES[DEPTH + 1][1024];

struct BranchResult {
	double value;
	int numMoves;
	Move moves[DEPTH];
};

struct Scanner {
	bool white;
	bool draw;
	double base;
	Move best;
	double bestMaterial;
};

static double get_scanner_material(Scanner& scanner) {
	if (scanner.draw) {
		return 0;
	}

	if (!scanner.best.valid) {
		return scanner.base;
	}

	return scanner.bestMaterial;
}

void updateMoves(Move& move, BranchResult& result, BranchResult& branch) {
	result.numMoves = branch.numMoves + 1;
	result.moves[0] = move;
	memcpy(result.moves + 1, branch.moves, sizeof(Move) * branch.numMoves);
}

Move* get_all_moves(Chessboard& board, int depth) {
	Move* moves = MOVES[depth];

	vector<Move> vector_moves = Generator::generateValidMoves(board);
	int j = 0;
	for (int i = 0, len = vector_moves.size(); i < len; i++) {
		moves[j++] = vector_moves[i];
	}

	moves[j].valid = false;

	return moves;
}

int get_material(Chessboard& board) {
	uint64 mask = board.pieceMask;
	int material = 0;
	
	material -= 6 * (Board::hasFlags(board, CastlingFlags::BLACK_CASTLE_K) + Board::hasFlags(board, CastlingFlags::BLACK_CASTLE_Q));
	material += 6 * (Board::hasFlags(board, CastlingFlags::WHITE_CASTLE_K) + Board::hasFlags(board, CastlingFlags::WHITE_CASTLE_Q));
	
	while (mask != 0) {
		uint64 pick = Utils::lowestOneBit(mask);
		mask &= ~pick;
		uint idx = Utils::numberOfTrailingZeros(pick);
		int piece = board.pieces[idx];
		int val = Serial::get_piece_value(piece);
		
		if (piece == Pieces::W_PAWN) {
			val += (idx >> 3) * 5;
		}
		
		if (piece == Pieces::B_PAWN) {
			val -= (8 - (idx >> 3)) * 5;
		}

		material += val;
	}
	
	return material;
}
	
int non_developing(Chessboard& board) {
	int result = 0;
	if (board.pieces[1] == Pieces::W_KNIGHT) result -= 10;
	if (board.pieces[2] == Pieces::W_BISHOP) result -= 10;
	if (board.pieces[5] == Pieces::W_BISHOP) result -= 10;
	if (board.pieces[6] == Pieces::W_KNIGHT) result -= 10;
	if (board.pieces[11] == Pieces::W_PAWN) result -= 11;
	if (board.pieces[12] == Pieces::W_PAWN) result -= 11;
	if (board.pieces[4] == Pieces::W_KING) result -= 8;

	if (board.pieces[57] == Pieces::B_KNIGHT) result += 10;
	if (board.pieces[58] == Pieces::B_BISHOP) result += 10;
	if (board.pieces[61] == Pieces::B_BISHOP) result += 10;
	if (board.pieces[62] == Pieces::B_KNIGHT) result += 10;
	if (board.pieces[51] == Pieces::B_PAWN) result += 11;
	if (board.pieces[52] == Pieces::B_PAWN) result += 11;
	if (board.pieces[60] == Pieces::B_KING) result += 8;
		
	return result * 3;
}

int un_developing(int piece, Move& move) {
	int move_to = move.to;
	int result = 0;
	
	switch (piece) {
		case Pieces::W_KNIGHT: {
			if (move_to == 1 || move_to == 6) result -= 10;
			break;
		}
		case Pieces::B_KNIGHT: {
			if (move_to == 57 || move_to == 62) result += 10;
			break;
		}
		case Pieces::W_BISHOP: {
			if (move_to == 2 || move_to == 5) result -= 10;
			break;
		}
		case Pieces::B_BISHOP: {
			if (move_to == 58 || move_to == 61) result += 10;
			break;
		}
		case Pieces::W_QUEEN: {
			result -= 5;
			break;
		}
		case Pieces::B_QUEEN: {
			result += 5;
			break;
		}
		case Pieces::W_KING: {
			result -= 5;
			break;
		}
		case Pieces::B_KING: {
			result += 5;
			break;
		}
	}
		
	return result * 3;
}

double get_advanced_material(Chessboard& board, Move& lastMove) {
	double material = get_material(board);
	material += un_developing(board.pieces[lastMove.from], lastMove);
	material += non_developing(board);
	return material;
}


long nodes;
BranchResult analyseBranches_prune(Chessboard& parent, Move& lastMove, int depth, double alpha, double beta, bool white) {
	nodes++;
	if (depth == 0) {
		return { get_advanced_material(parent, lastMove) };
	}
	
	Chessboard board = parent;
	Move* moves = get_all_moves(board, depth - 1);
	double value;
	
	BranchResult result { 0 };
	
	if (white) {
		value = -10000.0 * (depth + 1.0);

		for (int i = 0; i < 1024; i++) {
			Move move = moves[i];
			if (!move.valid) {
				break;
			}
		
			if (!Generator::playMove(board, move)) {
				continue;
			}
			
			BranchResult scannedResult = analyseBranches_prune(board, move, depth - 1, alpha, beta, false);
			if (value < scannedResult.value) {
				updateMoves(move, result, scannedResult);
			}
			
			value = max(value, scannedResult.value);
			if (value >= beta) {
				break;
			}
			
			board = parent;
			alpha = max(alpha, value);
		}
	} else {
		value = 10000.0 * (depth + 1.0);
		
		for (int i = 0; i < 1024; i++) {
			Move move = moves[i];
			if (!move.valid) {
				break;
			}
		
			if (!Generator::playMove(board, move)) {
				continue;
			}
			
			BranchResult scannedResult = analyseBranches_prune(board, move, depth - 1, alpha, beta, true);
			if (value > scannedResult.value) {
				updateMoves(move, result, scannedResult);
			}
			
			value = min(value, scannedResult.value);
			if (value <= alpha) {
				break;
			}
			
			board = parent;
			beta = min(beta, value);
		}
	}
	result.value = value;
	
	return result;
}

void evaluate(Chessboard& board, Scanner scan) {
	bool isWhite = Board::isWhite(board);
	if (PieceManager::isKingAttacked(board, isWhite)) {
		double delta = isWhite ? -1 : 1;
		scan.base += 10 * delta;
		
		if (!scan.best.valid) {
			// Checkmate
			scan.base = 10000 * delta;
		}
	} else {
		if (!scan.best.valid) {
			// Stalemate
			scan.base = 0;
		}
	}
	
	if (board.lastCapture >= 50) {
		// The game should end
		scan.best.valid = false;
	}
}

Scanner analyseBranchMoves(Chessboard& parent) {
	Scanner scan { };
	scan.base = get_material(parent);
	scan.best.valid = false;
	// scan.best.piece = = new Scanner(board, get_material(board));
	
	Chessboard board = parent;
	// ChessState state = ChessState.of(board);
	Move* moves = get_all_moves(parent, DEPTH);
	
	for (int i = 0; i < 1024; i++) {
		Move move = moves[i];
		if (!move.valid) {
			break;
		}
		
		if (!Generator::playMove(board, move)) {
			continue;
		}

		auto start = std::chrono::high_resolution_clock::now();
		nodes = 0;
		BranchResult branchResult = analyseBranches_prune(board, move, DEPTH, NEGATIVE_INFINITY, POSITIVE_INFINITY, Board::isWhite(board));
		auto finish = std::chrono::high_resolution_clock::now();
		double scannedResult = branchResult.value;
		// move.material = scannedResult;
		
		{
			char* buffer = Serial::getMoveString(move.from, move.to, move.special);
			printf("move: %-5s, (%.2f), [", buffer, scannedResult / 100.0);
			free(buffer);

			for (int i = 0; i < branchResult.numMoves; i++) {
				if (i > 0) {
					printf(", ");
				}

				Move m = branchResult.moves[i];
				buffer = Serial::getMoveString(m.from, m.to, m.special);
				printf("%s", buffer);
				free(buffer);
			}
			auto timeTook = std::chrono::duration_cast<std::chrono::nanoseconds>(finish-start).count();
			printf("]\t%d nodes / sec\n", (long)(nodes / (timeTook / 1000000000.0)));
		}

		if (scan.white) {
			if (scan.bestMaterial < scannedResult) {
				scan.best = move;
				scan.bestMaterial = scannedResult;
			}
		} else {
			if (scan.bestMaterial > scannedResult) {
				scan.best = move;
				scan.bestMaterial = scannedResult;
			}
		}

		board = parent;
	}
	
	evaluate(parent, scan);
	return scan;
}

Move analyse(Chessboard& board) {
	Scanner scanner = analyseBranchMoves(board);
	return Move { scanner.best.from, scanner.best.to, scanner.best.special, true };
}

#endif // !ANALYSER_CPP
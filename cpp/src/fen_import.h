#pragma once

#ifndef __FEN_IMPORT_H__
#define __FEN_IMPORT_H__

#include "utils_type.h"

#define FEN_IMPORT_SUCESSFULL 0
#define FEN_IMPORT_INVALID_FILE 1
#define FEN_IMPORT_INVALID_CHARACTER 2
#define FEN_IMPORT_INVALID_FILE_OOB 3
#define FEN_IMPORT_INVALID_RANK_OOB 4
#define FEN_IMPORT_INVALID_BOARD 5
#define FEN_IMPORT_INVALID_TURN 6
#define FEN_IMPORT_INVALID_FORMAT 7

extern int import_fen(Chessboard* board, const char* chars);

extern char* export_fen(Chessboard* board);

#endif // !__FEN_IMPORT_H__


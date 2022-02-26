#pragma once

#ifndef AB_PRUNING_V2_H
#define AB_PRUNING_V2_H

#include <analyser/chess_analyser.h>

class ABPruningV2 : public ChessAnalyser {
private:
	void thread_loop();

protected:
	virtual void on_option_change(UciOption* option);
public:
	ABPruningV2();

	virtual bool stop_analysis();
	virtual bool start_analysis(ChessAnalysis& analysis);
};

#endif // !AB_PRUNING_V2_H

#pragma once

#ifndef CHESS_ANALYSER_H
#define CHESS_ANALYSER_H

#include <uci/uci_option.h>
#include <thread>

#include "../../src/utils.h"

struct ChessAnalysis {
	Chessboard board{};
	Move bestmove{};
	Move ponder{};
};

class ChessAnalyser {
public:
	volatile ChessAnalysis* m_analysis;
	std::vector<UciOption*> m_options;
	std::thread m_thread;
	bool m_running;

	/// This method is called when an option changes	
	virtual void on_option_change(UciOption* option) = 0;
	
public:
	ChessAnalyser() = default;

	/// Returns a vector of options this analyser has
	const std::vector<UciOption*>& get_options() {
		return m_options;
	}

	/// Update the value of an option inside this analyser
	bool set_option(const std::string& key, std::string& value) {
		UciOption* option = nullptr;
		for (UciOption* item : m_options) {
			if (key == item->get_key() && (option == nullptr || (option->get_key().length() < item->get_key().length()))) {
				option = item;
			}
		}

		if (option == nullptr) {
			return false;
		}

		bool result = option->set_value(value);

		if (result) {
			// Only call the update method when the operation was successful
			on_option_change(option);
		}

		return result;
	}

	// TODO: Make the thread deamon
	// virtual bool stop_thread() = 0;

	/// Stop analyse the position
	virtual bool stop_analysis() = 0;

	/// Start analyse the position
	virtual bool start_analysis(ChessAnalysis& analysis) = 0;
};

#endif // !CHESS_ANALYSER_H
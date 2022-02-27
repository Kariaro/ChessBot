#pragma once

#ifndef AB_PRUNING_V2_CPP
#define AB_PRUNING_V2_CPP

#include "../ab_pruning_v2.h"
#include <chrono>
#include <thread>
#include <iostream>

ABPruningV2::ABPruningV2() {
	m_options.insert(m_options.end(), {
		new UciOption::Spin("Skill Level", 0, 20, 20),
		new UciOption::Spin("Move Overhead", 1, 4096, 1),
		new UciOption::Spin("Threads", 1, 512, 1),
		new UciOption::Spin("Hash", 1, 4096, 256),
		new UciOption::String("String", "Testing this tool"),
		new UciOption::Button("btn"),
		new UciOption::Combo("Combo", { "Alpha", "Beta", "Gamma", "Delta" }, 0),

		/*
		new UciOption::String("NalimovPath", ""),
		new UciOption::Spin("NalimovCache", 0, 10, 10),
		new UciOption::Check("Ponder", false),
		new UciOption::Check("OwnBook", false),
		new UciOption::Spin("MultiPV", 1, 1, 1),
		new UciOption::Check("UCI_ShowCurrLine", false),
		new UciOption::Check("UCI_ShowRefutations", false),
		new UciOption::Check("UCI_LimitStrength", false),
		new UciOption::Spin("UCI_Elo", 100, 5000, 1500),
		new UciOption::Check("UCI_AnalyseMode", false),
		new UciOption::String("UCI_Opponent", ""),
		*/
	});

	// Start the thread
	m_thread = std::thread(&ABPruningV2::thread_loop, this);
}

void ABPruningV2::on_option_change(UciOption* option) {
	// std::cerr << "info string option change: " << option->get_key() << std::endl;
}

void ABPruningV2::thread_loop() {
	m_running = true;
	
	while (m_running) {
		std::this_thread::sleep_for(std::chrono::milliseconds(1000));
		// std::cerr << "Loop" << std::endl;
	}
}

bool ABPruningV2::stop_analysis() {
	return false;
}

bool ABPruningV2::start_analysis(ChessAnalysis& analysis) {
	return false;
}

#endif // !AB_PRUNING_V2_CPP

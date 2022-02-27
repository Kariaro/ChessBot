#pragma once

#ifndef UCI_MANAGER_H
#define UCI_MANAGER_H

#include <analyser/chess_analyser.h>
#include <string>

// TODO: Potential name 'CLIManager'
class UciManager {
private:
	const std::string m_author;
	const std::string m_name;
	ChessAnalyser* m_analyser;
	ChessAnalysis  m_analysis;
	bool m_running;

	bool process_go(std::string command);
	bool process_position(std::string command);
	bool process_setoption(std::string command);
	bool process_debug_command(std::string command);
public:
	UciManager(const std::string author, const std::string name, ChessAnalyser* analyser);

	/// Process a uci command
	/// @return `false` if the command was not processed
	bool process_command(std::string command);

	/// Process std::cin until the process is closed
	void run();

	/// Returns if this manager is active
	bool running();
};

#endif // !UCI_MANAGER_H

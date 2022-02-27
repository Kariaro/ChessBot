#include <uci/uci_manager.h>
#include <codec/fen_codec.h>
// TODO: Without this line the linker does not implement the included files
#include "../codec/fen_codec.cpp"

#include <iostream>

void _Debug_options(ChessAnalyser* analyser) {
	for (UciOption* option : analyser->get_options()) {
		std::cerr << "into string [" << option->get_key() << "] ";

		switch (option->get_type()) {
		case UciOptionType::CHECK: {
			UciOption::Check* opt = (UciOption::Check*)option;
			std::cerr << "[check] = " << (opt->get_value() ? "true" : "false") << std::endl;
			break;
		}
		case UciOptionType::SPIN: {
			UciOption::Spin* opt = (UciOption::Spin*)option;
			std::cerr << "[spin] = " << opt->get_value() << std::endl;
			break;
		}
		case UciOptionType::COMBO: {
			UciOption::Combo* opt = (UciOption::Combo*)option;
			std::cerr << "[combo] = " << opt->get_list()[opt->get_value()] << std::endl;
			break;
		}
		case UciOptionType::BUTTON: {
			std::cerr << "[button]" << std::endl;
			break;
		}
		case UciOptionType::STRING: {
			UciOption::String* opt = (UciOption::String*)option;
			std::cerr << "[string] = " << opt->get_value() << std::endl;
			break;
		}
		default: {
			std::cerr << "Undefined UciOptionType ( " << (int)option->get_type() << " )" << std::endl;
			break;
		}
		}

		option++;
	}
}

UciManager::UciManager(const std::string author, const std::string name, ChessAnalyser* analyser) : m_author(author), m_name(name), m_analyser(analyser) {
	m_running = true;
}

/*
bool UciManager::process_ponderhit(std::string command) {
	return false;
}
*/

bool UciManager::process_go(std::string command) {
	// TODO: Implement this method
	return false;
}

bool UciManager::process_position(std::string command) {
	if (command._Starts_with("position fen ")) {
		command = command.substr(13);
		int matched;
		if (Codec::FEN::import_fen(m_analysis.board, command, matched) != FEN_CODEC_SUCCESSFUL) {
			std::cerr << "Invalid usage of 'position fen'. Invalid fen [" << command << "]" << std::endl;
			return false;
		}

		command = command.substr(matched);
	} else if (command._Starts_with("position startpos")) {
		command = command.substr(17);
		int matched;
		if (Codec::FEN::import_fen(m_analysis.board, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0", matched) != FEN_CODEC_SUCCESSFUL) {
			std::cerr << "Invalid usage of 'position fen'. Invalid fen [" << command << "]" << std::endl;
			return false;
		}

	} else {
		std::cerr << "Invalid usage of 'position'. Expected 'fen' or 'startpos' but got [" << command << "]" << std::endl;
		return false;
	}

	// Check if there is more data to parse
	if (command.empty()) {
		return true;
	} else if (command[0] != ' ') {
		std::cerr << "Invalid usage of 'position'. Expected space after fen [" << command << "]" << std::endl;
		return false;
	}

	// Remove the space
	command = command.substr(1);

	// TODO: Calculate the correct moves for the chess board
	std::cerr << "Remaining: [" << command << "]" << std::endl;

	return false;
}

bool UciManager::process_setoption(std::string command) {
	if (!command._Starts_with("setoption name ")) {
		std::cerr << "Invalid usage of 'setoption' [" << command << "]" << std::endl;
		return false;
	}

	// Remove the alias from the command
	command = command.substr(15);

	// Match the longest command
	UciOption* option = nullptr;
	for (UciOption* item : m_analyser->get_options()) {
		if (command._Starts_with(item->get_key()) && (option == nullptr || (item->get_key().length() > option->get_key().length()))) {
			option = item;
		}
	}

	if (option == nullptr) {
		std::cerr << "Invalid usage of 'setoption'. The option [" << command << "] does not exist" << std::endl;
		return false;
	}

	// Removed the matched name from the command
	command = command.substr(option->get_key().length());

	if (option->get_type() != UciOptionType::BUTTON) {
		if (!command._Starts_with(" value ")) {
			std::cerr << "Invalid usage of 'setoption'. Value tag was missing" << std::endl;
			return false;
		}

		// Remove ' value ' text
		command = command.substr(7);
	}

	return m_analyser->set_option(option->get_key(), command);
}

bool UciManager::process_debug_command(std::string command) {
	if (command == "@debugoptions") {
		_Debug_options(m_analyser);
	} else if (command == "@debugboard") {
		std::cerr << Serial::getBoardString(&m_analysis.board) << std::endl;
	}

	return true;
}

bool UciManager::process_command(std::string command) {
	if (command == "uci") {
		std::cout << "id name " << m_name << std::endl
				  << "id author " << m_author << std::endl;

		for (UciOption* option : m_analyser->get_options()) {
			std::cout << option->to_string() << std::endl;
		}

		std::cout << "uciok" << std::endl;
		return true;
	} else if (command == "ucinewgame") {
		return true;
	} else if (command == "stop") {
		m_analyser->stop_analysis();
		return true;
	} else if (command == "quit") {
		m_running = false;
		return true;
	} else if (command == "isready") {
		std::cout << "readyok" << std::endl;
		return true;
	}

	if (command._Starts_with("setoption")) {
		return process_setoption(command);
	} else if (command._Starts_with("position")) {
		return process_position(command);
	} else if (command._Starts_with("@")) {
		return process_debug_command(command);
	}
	
	/*
	if (alias == "ponderhit") {
		return process_ponderhit(command);
	}
	*/

	return false;
}

void UciManager::run() {
	std::string command;
	while (m_running) {
		std::getline(std::cin, command);
		if (!process_command(command)) {
			// Invalid command
			std::cerr << "Unknown uci command [" << command << "]" << std::endl;
		}
	}
}

bool UciManager::running() {
	return m_running;
}

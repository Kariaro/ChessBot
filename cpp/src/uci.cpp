#pragma once

#ifndef UCI_CPP
#define UCI_CPP

#include "uci.h"
#include <string>
#include <array>
#include <iostream>

enum class UciOptionType : int {
	CHECK,
	SPIN,
	COMBO,
	BUTTON,
	STRING,
};

struct _UciSpin {
	const int min;
	const int max;
	const int def;
	int val = def;
};

struct _UciCheck {
	const bool def;
	bool val = def;
};

struct _UciCombo {
	const char** values;
	int val = 0;
};

struct _UciString {
	const char* def;
	char* val = (char*)def;
};

struct UciOption {
	const char* key;
	const UciOptionType type;
	union {
		_UciSpin spin;
		_UciCheck check;
		_UciCombo combo;
		_UciString string;
	};

#pragma warning( push )
#pragma warning( disable: 26495 )
	UciOption(const char* _key, _UciSpin _val) : key(_key), type(UciOptionType::SPIN), spin(_val) {}
	UciOption(const char* _key, _UciCheck _val) : key(_key), type(UciOptionType::CHECK), check(_val) {}
	UciOption(const char* _key, _UciCombo _val) : key(_key), type(UciOptionType::COMBO), combo(_val) {}
	UciOption(const char* _key, _UciString _val) : key(_key), type(UciOptionType::STRING), string(_val) {}
	UciOption(const char* _key) : key(_key), type(UciOptionType::BUTTON) {}
#pragma warning( pop )
};

using std::string;

constexpr auto ENGINE_AUTHOR = "HardCoded";
constexpr auto ENGINE_NAME   = "HardCodedBot 1.0";

const char* COMBO_OPTIONS[5] = { "Alpha", "Beta", "Gamma", "Delta", nullptr };
const UciOption options[] = {
	{ "Skill Level", _UciSpin { 0, 20, 20 } },
	{ "Move Overhead", _UciSpin { 1, 4096, 1 } },
	{ "Threads", _UciSpin { 1, 512, 1 } },
	{ "Hash", _UciSpin { 1, 4096, 256 } },
	{ "String", _UciString { "Testing this tool" } },
	{ "Combo", _UciCombo { COMBO_OPTIONS } },

/*
	{ "NalimovPath", _UciString { "" } },
	{ "NalimovCache", _UciSpin { 0, 10, 10 } },
	{ "Ponder", _UciCheck { false } },
	{ "OwnBook", _UciCheck { false } },
	{ "MultiPV", _UciSpin { 1, 1, 1 } },
	{ "UCI_ShowCurrLine", _UciCheck { false } },
	{ "UCI_ShowRefutations", _UciCheck { false } },
	{ "UCI_LimitStrength", _UciCheck { false } },
	{ "UCI_Elo", _UciSpin { 100, 5000, 1500 } },
	{ "UCI_AnalyseMode", _UciCheck { false } },
	{ "UCI_Opponent", _UciString { "" } },
*/
	{ nullptr }
};

namespace UCI {
	std::string GetUciCommandAlias(std::string& command) {
		const char* begin = command.c_str();
		const char* position = std::strchr(begin, ' ');

		return position == nullptr
			? command
			: command.substr(0, position - begin);
	}

	void PrintUciOptions() {
		const UciOption* option = &(options[0]);
		while (option->key != nullptr) {
			std::cout << "option name " << option->key << " type ";

			switch (option->type) {
				case UciOptionType::CHECK: {
					std::cout << "check default " << option->check.def << std::endl;
					break;
				}
				case UciOptionType::SPIN: {
					std::cout << "spin default " << option->spin.def
							  << " min " << option->spin.min
							  << " max " << option->spin.max << std::endl;
					break;
				}
				case UciOptionType::COMBO: {
					std::cout << "combo default " << option->combo.values[option->combo.val];

					const char** name = option->combo.values;
					while (*name != nullptr) {
						std::cout << " var " << *name;
						name ++;
					}

					std::cout << std::endl;
					break;
				}
				case UciOptionType::BUTTON: {
					std::cout << "button" << std::endl;
					break;
				}
				case UciOptionType::STRING: {
					std::cout << "string default " << option->string.def << std::endl;
					break;
				}
				default: {
					std::cerr << "Undefined UciOptionType ( " << (int)option->type << " )" << std::endl;
					break;
				}
			}

			option++;
		}
	}

	void SetUciOption(std::string command) {
		if (!command._Starts_with("setoption name ")) {
			std::cerr << "Invalid usage of 'setoption' [" << command << "]" << std::endl;
			return;
		}

		// Remove the alias from the command
		command = command.substr(15);

		// Match the longest command
		UciOption* option = nullptr;
		int matchedLength = 0;
		const UciOption* iter = &(options[0]);
		while (iter->key != nullptr) {
			if (command._Starts_with(iter->key)) {
				int len = std::strlen(iter->key);
				if (len > matchedLength) {
					matchedLength = len;
					option = (UciOption*)iter;
				}
			}

			iter++;
		}

		if (option == nullptr) {
			std::cerr << "Invalid usage of 'setoption'. The option does not exist [" << command << "]" << std::endl;
			return;
		}

		// Removed the matched name from the command
		command = command.substr(matchedLength);

		std::string value = (command._Starts_with(" value ")) ? command.substr(7) : command;

		switch (option->type) {
			case UciOptionType::CHECK: {
				if (value == "true") {
					option->check.val = true;
				} else if (value == "false") {
					option->check.val = false;
				} else {
					std::cerr << "Invalid usage of 'setoption'. UciOptionType::CHECK does not allow the value [" << value << "]" << std::endl;
					return;
				}
				break;
			}
			case UciOptionType::SPIN: {
				char* endPos;
				int num = std::strtol(value.c_str(), &endPos, 10);

				if ((value.c_str() + value.length()) != endPos) {
					std::cerr << "Invalid usage of 'setoption'. UciOptionType::SPIN not all characters was a number [" << value << "]" << std::endl;
					return;
				}

				if (num < option->spin.min || num > option->spin.max) {
					std::cerr << "Invalid usage of 'setoption'. UciOptionType::SPIN number is outside ranges ["
							  << option->spin.min << ", " << option->spin.max << "],  [" << value << "]" << std::endl;
					return;
				}

				option->spin.val = num;
				break;
			}
			case UciOptionType::COMBO: {
				const char** values = &(options->combo.values[0]);
				int idx = 0;
				while (*values != nullptr) {
					if (value == *values) {
						option->combo.val = idx;
						return;
					}
					idx++;
					values++;
				}

				std::cerr << "Invalid usage of 'setoption'. UciOptionType::COMBO element does not exist [" << value << "]" << std::endl;
				return;
			}
			case UciOptionType::BUTTON: {
				// Callback
				break;
			}
			case UciOptionType::STRING: {
				if (option->string.val != option->string.def) {
					delete option->string.val;
				}

				// Allocate a new string and make sure that the old one is deleted
				char* copy = new char[value.length() + 1];
				std::copy(value.begin(), value.end(), copy);
				copy[value.size()] = '\0';
				option->string.val = copy;
				break;
			}
		}
	}

	// http://wbec-ridderkerk.nl/html/UCIProtocol.html
	void StartUCI() {
		std::string line;
		std::string alias;

		// While true keep looping
		while (true) {
			std::getline(std::cin, line);

			alias = GetUciCommandAlias(line);

			if (alias == "uci") {
				// Print information about the name and author of this bot
				std::cout << "id name " << ENGINE_NAME << std::endl
				          << "id author " << ENGINE_AUTHOR << std::endl
						  << std::endl;
				
				// Print information about the currently available options
				PrintUciOptions();
				
				std::cout << "uciok" << std::endl;
				continue;
			}

			if (alias == "setoption") {
				// Update an option
				SetUciOption(line);
				continue;
			}

			if (alias == "isready") {
				std::cout << "readyok";
				continue;
			}

			if (alias == "quit") {
				// Break the loop
				return;
			}

			// Print an error
			std::cerr << "Unknown uci command [" << line << "]" << std::endl;
		}
	}
}

#endif // !UCI_CPP
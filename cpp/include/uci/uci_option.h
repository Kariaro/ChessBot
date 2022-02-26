#pragma once

#ifndef UCI_OPTION_H
#define UCI_OPTION_H

#include <string>
#include <vector>
#include <cinttypes>

enum class UciOptionType : int {
	CHECK,
	SPIN,
	COMBO,
	STRING,
	BUTTON,
};

/// This class contains information about uci options
class UciOption {
private:
	const std::string m_key;
public:
	// Forward declare option classes
	class Check;
	class Spin;
	class Combo;
	class Button;
	class String;

	UciOption(const std::string& key) : m_key(key) {}

	virtual ~UciOption() = default;

	/// Returns the key value of this uci option
	const std::string& get_key() {
		return m_key;
	}

	/// Returns the key value of this uci option
	const std::string& get_key() const {
		return m_key;
	}

	/// Returns the type of this uci option
	virtual UciOptionType get_type() = 0;
	
	/// Returns the type of this uci option
	const UciOptionType get_type() const {
		return const_cast<UciOption*>(this)->get_type();
	}

	/// Change the value of this uci option
	/// @return `false` if the method failed
	virtual bool set_value(std::string& value) = 0;
};

/// UciCheckOption definition
class UciOption::Check : public UciOption {
private:
	const bool m_def;
	bool m_val;
public:
	UciOption::Check(const std::string& key, bool def);

	bool get_default();
	bool get_value();

	virtual UciOptionType get_type();
	virtual bool set_value(std::string& value);
};

/// UciSpinOption definition
class UciOption::Spin : public UciOption {
private:
	const int64_t m_def;
	const int64_t m_min;
	const int64_t m_max;
	int64_t m_val;
public:
	UciOption::Spin(const std::string& key, int64_t min, int64_t max, int64_t def);

	int64_t get_default();
	int64_t get_minimum();
	int64_t get_maximum();
	int64_t get_value();

	virtual UciOptionType get_type();
	virtual bool set_value(std::string& value);
};

/// UciComboOption definition
class UciOption::Combo : public UciOption {
private:
	std::vector<std::string> m_list;
	const int64_t m_def;
	int64_t m_val;
public:
	UciOption::Combo(const std::string& key, std::initializer_list<std::string> list, int64_t def);

	const std::vector<std::string>& get_list();
	int64_t get_default();
	int64_t get_value();

	virtual UciOptionType get_type();
	virtual bool set_value(std::string& value);
};

/// UciStringOption definition
class UciOption::String : public UciOption {
private:
	const std::string m_def;
	std::string m_val;
public:
	UciOption::String(const std::string& key, const std::string& def);

	const std::string& get_default();
	const std::string& get_value();

	virtual UciOptionType get_type();
	virtual bool set_value(std::string& value);
};

/// UciButtonOption definition
class UciOption::Button : public UciOption {
public:
	UciOption::Button(const std::string& key);

	virtual UciOptionType get_type();
	virtual bool set_value(std::string& value);
};

#endif // !UCI_OPTION_H

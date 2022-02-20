package me.hardcoded.lexer.notation;

public class InvalidNotation extends RuntimeException {
	public InvalidNotation(String format, Object... args) {
		super(String.format(format, args));
	}
}

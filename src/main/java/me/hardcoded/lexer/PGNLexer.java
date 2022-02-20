package me.hardcoded.lexer;

public class PGNLexer {
	public static final GenericLexerContext<Type> LEXER;
	static {
		LEXER = new GenericLexerContext<Type>()
			.addRule(Type.INVALID, i -> i.addRegex("."))
			.addRule(Type.WHITESPACE, i -> i.addRegex("[ \t\r\n]+"))
			.addRule(Type.COMMENT, i -> i.addMultiline("{", "\\", "}"))
			.addRule(Type.RESERVED_A, i -> i.addString("<"))
			.addRule(Type.RESERVED_B, i -> i.addString(">"))
			
			.addRule(Type.NAG, i -> i.addString("$"))
			.addRule(Type.DOT, i -> i.addString("."))
			.addRule(Type.TAG_ENTER, i -> i.addString("["))
			.addRule(Type.TAG_LEAVE, i -> i.addString("]"))
			.addRule(Type.VAR_ENTER, i -> i.addString("("))
			.addRule(Type.VAR_LEAVE, i -> i.addString(")"))
			
			.addRule(Type.SYMBOL, i -> i.addRegex("[a-zA-Z0-9]([a-zA-Z0-9_+#=:-]*)(\\?\\?|\\?!|!\\?|\\?|!)?"))
			.addRule(Type.STRING, i -> i.addSingleline("\"", "\\", "\""))
			.addRule(Type.INTEGER, i -> i.addRegex("[0-9]+"))
			.addRule(Type.TERMINATION, i -> i.addRegex("1-0|0-1|1/2-1/2|\\*"))
			.toImmutable();
	}
	
	public enum Type {
		// Whitespace
		INVALID,
		WHITESPACE,
		COMMENT,
		
		// Parts
		RESERVED_A, // '<'
		RESERVED_B, // '>'
		
		TAG_ENTER, // '['
		TAG_LEAVE, // ']'
		
		VAR_ENTER, // '('
		VAR_LEAVE, // ')'
		
		INTEGER, // [0-9]+
		STRING, // "..."
		NAG, // '$'
		DOT, // '\\.'
		SYMBOL,
		TERMINATION,
	}
}

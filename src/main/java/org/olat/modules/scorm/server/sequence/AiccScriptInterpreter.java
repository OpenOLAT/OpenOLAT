/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.scorm.server.sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.modules.scorm.server.servermodels.SequencerModel;

/**
 * A self-contained interpreter for the AICC "aicc_script" prerequisite scripting
 * language used by SCORM 1.2 content packages (the <code>adlcp:prerequisites</code>
 * element). It replaces the former JavaScript (Nashorn) based evaluation.
 * <p>
 * The language is defined by the AICC CMI001 <i>Guidelines for Interoperability</i>
 * and supports the following operators (C-like precedence, from lowest to highest):
 * <ul>
 * <li><code>|</code> &mdash; logical OR</li>
 * <li><code>&amp;</code> &mdash; logical AND</li>
 * <li><code>~</code> &mdash; logical NOT (unary)</li>
 * <li><code>=</code> &mdash; equals, compares a status to a quoted literal</li>
 * <li><code>&lt;&gt;</code> and <code>&lt;</code> &mdash; not equals</li>
 * <li><code>{ a, b, c }</code> &mdash; a set of members</li>
 * <li><code>N*{ ... }</code> &mdash; at least N members of the set must be complete</li>
 * <li><code>( ... )</code> &mdash; grouping / precedence</li>
 * </ul>
 * A bare element identifier evaluates to <code>true</code> when its status is
 * "completed" or "passed".
 * <p>
 * To keep the historical behaviour, when a referenced identifier is not known to
 * the tracking model the whole expression evaluates to <code>true</code> (the item
 * can be launched).
 *
 * Initial date: 17 juil. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class AiccScriptInterpreter {

	/**
	 * Provides the run-time status of the referenced learning content elements.
	 */
	@FunctionalInterface
	public interface StatusProvider {
		/**
		 * @param identifier the identifier of a SCO / item
		 * @return the current <code>cmi.core.lesson_status</code> value of the item,
		 * 		or <code>null</code> if the item does not exist in the tracking model
		 */
		String getStatus(String identifier);
	}

	private final List<Token> tokens;
	private int pos;

	private AiccScriptInterpreter(List<Token> tokens) {
		this.tokens = tokens;
	}

	/**
	 * Evaluate an aicc_script prerequisite expression.
	 *
	 * @param prerequisites the expression, e.g. <code>I1&amp;I2</code>
	 * @param provider resolves the status of referenced identifiers
	 * @return <code>true</code> if the prerequisites are fulfilled, or if the
	 * 		expression references an unknown identifier
	 */
	public static boolean evaluate(String prerequisites, StatusProvider provider) {
		if (prerequisites == null || prerequisites.isBlank()) {
			return true;
		}
		List<Token> tokens = tokenize(prerequisites);
		AiccScriptInterpreter interpreter = new AiccScriptInterpreter(tokens);
		Node ast = interpreter.parseExpression();
		if (interpreter.peek().type != TokenType.EOF) {
			throw new IllegalArgumentException("Unexpected token '" + interpreter.peek().text
					+ "' in prerequisites: " + prerequisites);
		}

		// Historical behaviour: an unknown identifier makes the whole expression true.
		Set<String> identifiers = new java.util.HashSet<>();
		ast.collectIdentifiers(identifiers);
		for (String identifier : identifiers) {
			if (provider.getStatus(identifier) == null) {
				return true;
			}
		}
		return ast.eval(provider);
	}

	private static boolean isComplete(String status) {
		return SequencerModel.ITEM_COMPLETED.equals(status) || SequencerModel.ITEM_PASSED.equals(status);
	}

	// ----------------------------------------------------------------------
	// Tokenizer
	// ----------------------------------------------------------------------

	private enum TokenType {
		IDENT, STRING, AND, OR, NOT, EQ, NEQ, LPAREN, RPAREN, LBRACE, RBRACE, COMMA, STAR, EOF
	}

	private record Token(TokenType type, String text) {
	}

	private static boolean isIdentifierChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '-';
	}

	private static List<Token> tokenize(String src) {
		List<Token> tokens = new ArrayList<>();
		int i = 0;
		int len = src.length();
		while (i < len) {
			char c = src.charAt(i);
			if (Character.isWhitespace(c)) {
				i++;
			} else if (c == '&') {
				tokens.add(new Token(TokenType.AND, "&"));
				i++;
			} else if (c == '|') {
				tokens.add(new Token(TokenType.OR, "|"));
				i++;
			} else if (c == '~') {
				tokens.add(new Token(TokenType.NOT, "~"));
				i++;
			} else if (c == '(') {
				tokens.add(new Token(TokenType.LPAREN, "("));
				i++;
			} else if (c == ')') {
				tokens.add(new Token(TokenType.RPAREN, ")"));
				i++;
			} else if (c == '{') {
				tokens.add(new Token(TokenType.LBRACE, "{"));
				i++;
			} else if (c == '}') {
				tokens.add(new Token(TokenType.RBRACE, "}"));
				i++;
			} else if (c == ',') {
				tokens.add(new Token(TokenType.COMMA, ","));
				i++;
			} else if (c == '*') {
				tokens.add(new Token(TokenType.STAR, "*"));
				i++;
			} else if (c == '=') {
				tokens.add(new Token(TokenType.EQ, "="));
				i++;
			} else if (c == '<') {
				// both "<>" and "<" mean "not equals"
				if (i + 1 < len && src.charAt(i + 1) == '>') {
					tokens.add(new Token(TokenType.NEQ, "<>"));
					i += 2;
				} else {
					tokens.add(new Token(TokenType.NEQ, "<"));
					i++;
				}
			} else if (c == '"') {
				int end = src.indexOf('"', i + 1);
				if (end == -1) {
					throw new IllegalArgumentException("Unterminated string literal in prerequisites: " + src);
				}
				tokens.add(new Token(TokenType.STRING, src.substring(i + 1, end)));
				i = end + 1;
			} else if (isIdentifierChar(c)) {
				int start = i;
				while (i < len && isIdentifierChar(src.charAt(i))) {
					i++;
				}
				tokens.add(new Token(TokenType.IDENT, src.substring(start, i)));
			} else {
				throw new IllegalArgumentException("Illegal character '" + c + "' in prerequisites: " + src);
			}
		}
		tokens.add(new Token(TokenType.EOF, ""));
		return tokens;
	}

	// ----------------------------------------------------------------------
	// Parser (recursive descent)
	// ----------------------------------------------------------------------

	private Token peek() {
		return tokens.get(pos);
	}

	private Token next() {
		return tokens.get(pos++);
	}

	private Token expect(TokenType type) {
		Token token = peek();
		if (token.type != type) {
			throw new IllegalArgumentException("Expected " + type + " but found '" + token.text + "'");
		}
		return next();
	}

	// expr := orExpr
	private Node parseExpression() {
		return parseOr();
	}

	// orExpr := andExpr ( '|' andExpr )*
	private Node parseOr() {
		Node node = parseAnd();
		while (peek().type == TokenType.OR) {
			next();
			node = new OrNode(node, parseAnd());
		}
		return node;
	}

	// andExpr := notExpr ( '&' notExpr )*
	private Node parseAnd() {
		Node node = parseNot();
		while (peek().type == TokenType.AND) {
			next();
			node = new AndNode(node, parseNot());
		}
		return node;
	}

	// notExpr := '~' notExpr | compExpr
	private Node parseNot() {
		if (peek().type == TokenType.NOT) {
			next();
			return new NotNode(parseNot());
		}
		return parseComparison();
	}

	// compExpr := primary [ ('=' | '<>' | '<') STRING ]
	private Node parseComparison() {
		Node left = parsePrimary();
		TokenType op = peek().type;
		if (op == TokenType.EQ || op == TokenType.NEQ) {
			if (!(left instanceof IdentNode ident)) {
				throw new IllegalArgumentException("Left side of a comparison must be an identifier");
			}
			next();
			Token value = expect(TokenType.STRING);
			return new CompareNode(ident.name, op == TokenType.EQ, value.text);
		}
		return left;
	}

	// primary := '(' expr ')' | '{' set '}' | NUMBER '*' '{' set '}' | IDENT
	private Node parsePrimary() {
		Token token = peek();
		switch (token.type) {
			case LPAREN -> {
				next();
				Node node = parseExpression();
				expect(TokenType.RPAREN);
				return node;
			}
			case LBRACE -> {
				return parseSet(-1);
			}
			case IDENT -> {
				// a numeric identifier followed by '*' introduces a "N or more" set
				if (isInteger(token.text) && tokens.get(pos + 1).type == TokenType.STAR) {
					next(); // number
					next(); // '*'
					int min = Integer.parseInt(token.text);
					return parseSet(min);
				}
				next();
				return new IdentNode(token.text);
			}
			default -> throw new IllegalArgumentException("Unexpected token '" + token.text + "' in prerequisites");
		}
	}

	// set := '{' expr ( ',' expr )* '}'
	private Node parseSet(int minCount) {
		expect(TokenType.LBRACE);
		List<Node> members = new ArrayList<>();
		members.add(parseExpression());
		while (peek().type == TokenType.COMMA) {
			next();
			members.add(parseExpression());
		}
		expect(TokenType.RBRACE);
		return new SetNode(members, minCount);
	}

	private static boolean isInteger(String text) {
		if (text.isEmpty()) {
			return false;
		}
		for (int i = 0; i < text.length(); i++) {
			if (!Character.isDigit(text.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	// ----------------------------------------------------------------------
	// Abstract syntax tree
	// ----------------------------------------------------------------------

	private interface Node {
		boolean eval(StatusProvider provider);

		void collectIdentifiers(Set<String> identifiers);
	}

	private record IdentNode(String name) implements Node {
		@Override
		public boolean eval(StatusProvider provider) {
			return isComplete(provider.getStatus(name));
		}

		@Override
		public void collectIdentifiers(Set<String> identifiers) {
			identifiers.add(name);
		}
	}

	private record CompareNode(String name, boolean equals, String value) implements Node {
		@Override
		public boolean eval(StatusProvider provider) {
			boolean same = value.equals(provider.getStatus(name));
			return equals == same;
		}

		@Override
		public void collectIdentifiers(Set<String> identifiers) {
			identifiers.add(name);
		}
	}

	private record NotNode(Node child) implements Node {
		@Override
		public boolean eval(StatusProvider provider) {
			return !child.eval(provider);
		}

		@Override
		public void collectIdentifiers(Set<String> identifiers) {
			child.collectIdentifiers(identifiers);
		}
	}

	private record AndNode(Node left, Node right) implements Node {
		@Override
		public boolean eval(StatusProvider provider) {
			return left.eval(provider) && right.eval(provider);
		}

		@Override
		public void collectIdentifiers(Set<String> identifiers) {
			left.collectIdentifiers(identifiers);
			right.collectIdentifiers(identifiers);
		}
	}

	private record OrNode(Node left, Node right) implements Node {
		@Override
		public boolean eval(StatusProvider provider) {
			return left.eval(provider) || right.eval(provider);
		}

		@Override
		public void collectIdentifiers(Set<String> identifiers) {
			left.collectIdentifiers(identifiers);
			right.collectIdentifiers(identifiers);
		}
	}

	/**
	 * A set of members. When <code>minCount</code> is negative all members must be
	 * complete, otherwise at least <code>minCount</code> members must be complete.
	 */
	private record SetNode(List<Node> members, int minCount) implements Node {
		@Override
		public boolean eval(StatusProvider provider) {
			int complete = 0;
			for (Node member : members) {
				if (member.eval(provider)) {
					complete++;
				}
			}
			if (minCount < 0) {
				return complete == members.size();
			}
			return complete >= minCount;
		}

		@Override
		public void collectIdentifiers(Set<String> identifiers) {
			for (Node member : members) {
				member.collectIdentifiers(identifiers);
			}
		}
	}
}

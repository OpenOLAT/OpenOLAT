/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.commons.services.ai.content;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.BreakIterator;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.Parser;
import org.olat.core.commons.services.ai.essay.AiContentChunk;
import org.olat.core.logging.Tracing;
import org.springframework.stereotype.Service;

/**
 *
 * CommonMark-aware Markdown chunker for the essay generation pipeline.
 * <p>
 * Heading rules:
 * <ul>
 *   <li>H1 is always a chunk boundary.</li>
 *   <li>H2 is a boundary unless the previous chunk is below the merge
 *       threshold (&lt; 300 tokens), in which case H2 merges into the
 *       previous chunk.</li>
 *   <li>H3 and lower stay in the current chunk.</li>
 * </ul>
 * Atomic units (fenced or indented code blocks, ordered / unordered lists,
 * GFM tables, block quotes that embed atomic units) are never split. If a
 * single atomic unit exceeds the chunk budget, the containing chunk is
 * marked {@code oversize=true} so the caller can either re-chunk the source
 * or truncate when assembling the generator prompt.
 * <p>
 * Token estimation uses a character-based heuristic: {@code char/4} for most
 * scripts and {@code char/1.6} when more than 30&nbsp;% of the non-whitespace
 * characters are in the CJK (Han / Hiragana / Katakana / Hangul) Unicode
 * ranges. Overflow inside a single paragraph falls back to a three-stage
 * split — paragraph → sentence ({@link BreakIterator#getSentenceInstance})
 * → hard word boundary — in that order.
 * <p>
 * Overlap: each chunk prepends the last {@code overlapTokens} (default 120)
 * of the previous chunk cut at the nearest sentence boundary, never mid-word.
 * <p>
 * The chunk {@code id} is the first 16 hex characters of
 * {@code SHA-1(normalisedText)} — normalisation collapses all whitespace to
 * single spaces and trims. Short identifiers keep the generator provenance
 * readable without sacrificing uniqueness inside a single document.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class AiContentChunker {
	private static final Logger log = Tracing.createLoggerFor(AiContentChunker.class);

	/** Default chunk budget in tokens. */
	public static final int DEFAULT_BUDGET_TOKENS = 800;
	/** Default overlap prepended from the previous chunk, in tokens. */
	public static final int DEFAULT_OVERLAP_TOKENS = 120;
	/** If the previous chunk is below this token count an H2 boundary merges instead of splitting. */
	private static final int H2_MERGE_THRESHOLD = 300;
	/** Ratio of CJK to non-whitespace chars above which CJK token heuristic kicks in. */
	private static final double CJK_RATIO_THRESHOLD = 0.30d;

	private final int budgetTokens;
	private final int overlapTokens;

	public AiContentChunker() {
		this(DEFAULT_BUDGET_TOKENS, DEFAULT_OVERLAP_TOKENS);
	}

	/**
	 * Test-friendly constructor. Tests inject smaller budgets to force the
	 * overflow paths without having to build large Markdown fixtures.
	 */
	public AiContentChunker(int budgetTokens, int overlapTokens) {
		this.budgetTokens = Math.max(1, budgetTokens);
		this.overlapTokens = Math.max(0, Math.min(overlapTokens, this.budgetTokens - 1));
	}

	/**
	 * Chunk the given Markdown under the chunker's token budget.
	 *
	 * @param markdown the raw Markdown source; {@code null}/blank returns
	 *                 an empty list.
	 * @param locale   used by the sentence-split fallback; {@code null}
	 *                 falls back to {@link Locale#ENGLISH}.
	 */
	public List<AiContentChunk> chunk(String markdown, Locale locale) {
		if (markdown == null || markdown.isBlank()) {
			return List.of();
		}
		Locale effective = locale == null ? Locale.ENGLISH : locale;

		Parser parser = Parser.builder().extensions(List.of(TablesExtension.create())).build();
		Node document = parser.parse(markdown);

		List<Segment> segments = new ArrayList<>();
		Deque<String> headingStack = new ArrayDeque<>();
		flattenTopLevel(document, segments, headingStack, markdown);

		return assemble(segments, markdown, effective);
	}

	// -------------------------------------------------------------- Segments

	/**
	 * Top-level traversal: walks immediate children of the document and
	 * emits one {@link Segment} per block. Headings update the heading
	 * stack; other blocks are captured by source span so we preserve the
	 * original Markdown (fences, list markers, tables) verbatim.
	 */
	private void flattenTopLevel(Node document, List<Segment> segments,
			Deque<String> headingStack, String source) {
		Node node = document.getFirstChild();
		while (node != null) {
			if (node instanceof Heading heading) {
				int level = heading.getLevel();
				pushHeading(headingStack, level, headingText(heading));
				String text = extractSource(node, source);
				segments.add(new Segment(SegmentType.HEADING, level, text,
						estimateTokens(text), headingPath(headingStack), isAtomic(node)));
			} else {
				String text = extractSource(node, source);
				SegmentType type = classify(node);
				segments.add(new Segment(type, 0, text, estimateTokens(text),
						headingPath(headingStack), isAtomic(node)));
			}
			node = node.getNext();
		}
	}

	private static SegmentType classify(Node node) {
		if (node instanceof FencedCodeBlock || node instanceof IndentedCodeBlock) return SegmentType.CODE;
		if (node instanceof BulletList || node instanceof OrderedList) return SegmentType.LIST;
		if (node instanceof BlockQuote) return SegmentType.QUOTE;
		if (node instanceof Paragraph) return SegmentType.PARAGRAPH;
		return SegmentType.OTHER;
	}

	/** Atomic units must never be split across chunks. */
	private static boolean isAtomic(Node node) {
		if (node instanceof FencedCodeBlock || node instanceof IndentedCodeBlock
				|| node instanceof BulletList || node instanceof OrderedList) {
			return true;
		}
		if (node instanceof BlockQuote quote) {
			// A block quote is atomic if any of its children is atomic.
			Node child = quote.getFirstChild();
			while (child != null) {
				if (isAtomic(child)) return true;
				child = child.getNext();
			}
		}
		// GFM tables come from the tables extension as custom blocks — the
		// safe default is to treat them as atomic too.
		String typeName = node.getClass().getSimpleName();
		return "TableBlock".equals(typeName) || "TableHead".equals(typeName) || "TableBody".equals(typeName);
	}

	private static String headingText(Heading heading) {
		StringBuilder sb = new StringBuilder();
		collectText(heading, sb);
		return sb.toString().trim();
	}

	private static void collectText(Node node, StringBuilder sb) {
		Node child = node.getFirstChild();
		while (child != null) {
			if (child instanceof org.commonmark.node.Text t) {
				sb.append(t.getLiteral());
			}
			collectText(child, sb);
			child = child.getNext();
		}
	}

	private static void pushHeading(Deque<String> stack, int level, String text) {
		// Pop deeper-or-equal levels before pushing this heading's entry.
		while (stack.size() >= level) {
			stack.removeLast();
		}
		// Pad with empty slots so stack depth tracks level.
		while (stack.size() < level - 1) {
			stack.addLast("");
		}
		stack.addLast(text);
	}

	private static List<String> headingPath(Deque<String> stack) {
		List<String> path = new ArrayList<>(stack.size());
		for (String entry : stack) {
			if (entry != null && !entry.isEmpty()) {
				path.add(entry);
			}
		}
		return List.copyOf(path);
	}

	/**
	 * Extract the original Markdown for a block. CommonMark provides
	 * source spans via the parser only when explicitly enabled; to keep
	 * the dependency minimal we fall back to a simple textual rendering
	 * of the node.
	 */
	private static String extractSource(Node node, String source) {
		// Prefer source spans if the parser recorded them (not enabled by default here).
		if (node.getSourceSpans() != null && !node.getSourceSpans().isEmpty()) {
			int start = node.getSourceSpans().get(0).getInputIndex();
			int end = start;
			for (var span : node.getSourceSpans()) {
				int spanEnd = span.getInputIndex() + span.getLength();
				if (spanEnd > end) end = spanEnd;
			}
			if (start >= 0 && end <= source.length() && end > start) {
				return source.substring(start, end);
			}
		}
		StringBuilder sb = new StringBuilder();
		if (node instanceof Heading h) {
			sb.append("#".repeat(h.getLevel())).append(' ').append(headingText(h));
		} else if (node instanceof FencedCodeBlock fcb) {
			// FencedCodeBlock stores content in getLiteral(), not in child Text nodes.
			String fence = fcb.getFenceCharacter().repeat(Math.max(3, fcb.getFenceLength()));
			sb.append(fence);
			if (fcb.getInfo() != null && !fcb.getInfo().isEmpty()) {
				sb.append(fcb.getInfo());
			}
			sb.append('\n');
			if (fcb.getLiteral() != null) {
				sb.append(fcb.getLiteral());
			}
			sb.append(fence);
		} else if (node instanceof IndentedCodeBlock icb) {
			// IndentedCodeBlock stores content in getLiteral() too.
			if (icb.getLiteral() != null) {
				for (String line : icb.getLiteral().split("\n", -1)) {
					sb.append("    ").append(line).append('\n');
				}
			}
		} else {
			collectText(node, sb);
		}
		return sb.toString();
	}

	// -------------------------------------------------------------- Assembly

	private List<AiContentChunk> assemble(List<Segment> segments, String source, Locale locale) {
		List<AiContentChunk> result = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		int currentTokens = 0;
		List<String> currentPath = List.of();
		boolean currentOversize = false;
		String carryOverOverlap = "";

		for (Segment seg : segments) {
			boolean forcedBoundary = seg.type() == SegmentType.HEADING && seg.level() == 1;
			boolean softBoundary = seg.type() == SegmentType.HEADING && seg.level() == 2
					&& currentTokens >= H2_MERGE_THRESHOLD;

			// 1. Heading-driven boundaries.
			if ((forcedBoundary || softBoundary) && current.length() > 0) {
				emit(result, carryOverOverlap, current.toString(), currentTokens, currentPath, currentOversize);
				carryOverOverlap = buildOverlap(current.toString(), locale);
				current.setLength(0);
				currentTokens = 0;
				currentOversize = false;
			}

			// 2. Budget check before appending the segment.
			if (!current.isEmpty() && currentTokens + seg.tokens() > budgetTokens) {
				if (seg.atomic()) {
					// Emit the in-progress chunk and put the atomic unit into a new chunk.
					emit(result, carryOverOverlap, current.toString(), currentTokens, currentPath, currentOversize);
					carryOverOverlap = buildOverlap(current.toString(), locale);
					current.setLength(0);
					currentTokens = 0;
					currentOversize = false;
				} else {
					// Non-atomic: flush, then attempt to split the segment across chunks.
					emit(result, carryOverOverlap, current.toString(), currentTokens, currentPath, currentOversize);
					carryOverOverlap = buildOverlap(current.toString(), locale);
					current.setLength(0);
					currentTokens = 0;
					currentOversize = false;
				}
			}

			// 3. Append the segment — split on overflow for non-atomic blocks.
			if (seg.atomic() && seg.tokens() > budgetTokens) {
				// Atomic unit too large on its own → emit as oversize single-chunk.
				if (!current.isEmpty()) {
					emit(result, carryOverOverlap, current.toString(), currentTokens, currentPath, currentOversize);
					carryOverOverlap = buildOverlap(current.toString(), locale);
					current.setLength(0);
					currentTokens = 0;
					currentOversize = false;
				}
				emit(result, carryOverOverlap, seg.text(), seg.tokens(), seg.headingPath(), true);
				carryOverOverlap = buildOverlap(seg.text(), locale);
				continue;
			}

			if (!seg.atomic() && seg.tokens() > budgetTokens) {
				// Non-atomic overflow: paragraph → sentence → hard split.
				List<String> pieces = splitOverflow(seg.text(), locale);
				for (String piece : pieces) {
					int pieceTokens = estimateTokens(piece);
					if (!current.isEmpty() && currentTokens + pieceTokens > budgetTokens) {
						emit(result, carryOverOverlap, current.toString(), currentTokens,
								currentPath, currentOversize);
						carryOverOverlap = buildOverlap(current.toString(), locale);
						current.setLength(0);
						currentTokens = 0;
						currentOversize = false;
					}
					if (!current.isEmpty()) current.append("\n\n");
					current.append(piece);
					currentTokens += pieceTokens;
					currentPath = seg.headingPath();
				}
				continue;
			}

			if (!current.isEmpty()) current.append("\n\n");
			current.append(seg.text());
			currentTokens += seg.tokens();
			currentPath = seg.headingPath();
		}

		if (!current.isEmpty()) {
			emit(result, carryOverOverlap, current.toString(), currentTokens, currentPath, currentOversize);
		}
		return List.copyOf(result);
	}

	private void emit(List<AiContentChunk> result, String overlapPrefix, String body,
			int bodyTokens, List<String> headingPath, boolean oversize) {
		String finalText;
		int finalTokens;
		if (overlapPrefix != null && !overlapPrefix.isEmpty()) {
			finalText = overlapPrefix + "\n\n" + body;
			finalTokens = bodyTokens + estimateTokens(overlapPrefix);
		} else {
			finalText = body;
			finalTokens = bodyTokens;
		}
		String id = computeChunkId(finalText);
		result.add(new AiContentChunk(id, finalText, headingPath, finalTokens, oversize));
	}

	/**
	 * Build the overlap-prefix text: take the last {@code overlapTokens}
	 * tokens of the emitted chunk, snap to the nearest sentence boundary,
	 * never cut mid-word.
	 */
	private String buildOverlap(String emittedText, Locale locale) {
		if (overlapTokens <= 0 || emittedText == null || emittedText.isEmpty()) {
			return "";
		}
		int approxChars = tokensToChars(overlapTokens, emittedText);
		if (approxChars >= emittedText.length()) {
			return emittedText.trim();
		}
		int cutStart = emittedText.length() - approxChars;
		// Snap to sentence boundary — walk forward until we hit one.
		BreakIterator iter = BreakIterator.getSentenceInstance(locale);
		iter.setText(emittedText);
		int boundary = iter.following(cutStart);
		if (boundary != BreakIterator.DONE && boundary < emittedText.length()) {
			cutStart = boundary;
		} else {
			// No sentence boundary ahead → move to next whitespace so we don't split a word.
			while (cutStart < emittedText.length() && !Character.isWhitespace(emittedText.charAt(cutStart))) {
				cutStart++;
			}
		}
		return emittedText.substring(cutStart).trim();
	}

	// -------------------------------------------------------------- Overflow split

	/**
	 * Progressive overflow split for non-atomic paragraph-like text.
	 * Stage 1 tries paragraph boundaries (blank line), stage 2 falls back
	 * to sentence boundaries for the locale, stage 3 hard-splits on word
	 * boundaries.
	 */
	private List<String> splitOverflow(String text, Locale locale) {
		List<String> paragraphs = new ArrayList<>(Arrays.asList(text.split("\\n{2,}")));
		List<String> intermediate = new ArrayList<>();
		for (String paragraph : paragraphs) {
			if (estimateTokens(paragraph) <= budgetTokens) {
				intermediate.add(paragraph);
			} else {
				intermediate.addAll(splitSentences(paragraph, locale));
			}
		}
		List<String> result = new ArrayList<>();
		for (String piece : intermediate) {
			if (estimateTokens(piece) <= budgetTokens) {
				result.add(piece);
			} else {
				result.addAll(hardSplitWords(piece));
			}
		}
		return result;
	}

	private List<String> splitSentences(String text, Locale locale) {
		List<String> out = new ArrayList<>();
		BreakIterator iter = BreakIterator.getSentenceInstance(locale);
		iter.setText(text);
		int start = iter.first();
		for (int end = iter.next(); end != BreakIterator.DONE; start = end, end = iter.next()) {
			String sentence = text.substring(start, end).trim();
			if (!sentence.isEmpty()) out.add(sentence);
		}
		return out;
	}

	private List<String> hardSplitWords(String text) {
		List<String> out = new ArrayList<>();
		int targetChars = tokensToChars(budgetTokens, text);
		int pos = 0;
		while (pos < text.length()) {
			int end = Math.min(text.length(), pos + targetChars);
			if (end < text.length()) {
				while (end > pos && !Character.isWhitespace(text.charAt(end))) {
					end--;
				}
				if (end == pos) {
					// No whitespace available — take the full slice anyway.
					end = Math.min(text.length(), pos + targetChars);
				}
			}
			String slice = text.substring(pos, end).trim();
			if (!slice.isEmpty()) out.add(slice);
			pos = end;
		}
		return out;
	}

	// -------------------------------------------------------------- Tokens

	/**
	 * Token estimate: {@code char/4} for most scripts, {@code char/1.6}
	 * when CJK-heavy. See javadoc for the full rule.
	 */
	public static int estimateTokens(String text) {
		if (text == null || text.isEmpty()) return 0;
		int cjk = 0;
		int totalNonWs = 0;
		for (int i = 0; i < text.length(); ) {
			int cp = text.codePointAt(i);
			if (!Character.isWhitespace(cp)) {
				totalNonWs++;
				if (isCjk(cp)) cjk++;
			}
			i += Character.charCount(cp);
		}
		if (totalNonWs == 0) return 0;
		double divisor = (((double) cjk / totalNonWs) > CJK_RATIO_THRESHOLD) ? 1.6d : 4.0d;
		return Math.max(1, (int) Math.ceil(text.length() / divisor));
	}

	private static int tokensToChars(int tokens, String sample) {
		// Invert the estimator using the CJK ratio of the provided sample.
		int cjk = 0;
		int totalNonWs = 0;
		for (int i = 0; i < sample.length(); ) {
			int cp = sample.codePointAt(i);
			if (!Character.isWhitespace(cp)) {
				totalNonWs++;
				if (isCjk(cp)) cjk++;
			}
			i += Character.charCount(cp);
		}
		double divisor = totalNonWs > 0 && ((double) cjk / totalNonWs) > CJK_RATIO_THRESHOLD ? 1.6d : 4.0d;
		return (int) Math.ceil(tokens * divisor);
	}

	private static boolean isCjk(int cp) {
		// Han, Hiragana, Katakana, Hangul Syllables, CJK Unified Ideographs
		return (cp >= 0x3040 && cp <= 0x30FF)
				|| (cp >= 0x4E00 && cp <= 0x9FFF)
				|| (cp >= 0xAC00 && cp <= 0xD7AF);
	}

	// -------------------------------------------------------------- ID

	/**
	 * Chunk identifier: first 16 hex chars of SHA-1 over whitespace-normalised text.
	 */
	public static String computeChunkId(String text) {
		String normalised = text == null ? "" : text.replaceAll("\\s+", " ").trim();
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] hash = digest.digest(normalised.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(32);
			for (byte b : hash) {
				hex.append(String.format(Locale.ROOT, "%02x", b));
			}
			return hex.substring(0, 16);
		} catch (NoSuchAlgorithmException e) {
			log.warn("SHA-1 unavailable — falling back to hashCode-based chunk id");
			return String.format(Locale.ROOT, "%016x", (long) normalised.hashCode());
		}
	}

	// -------------------------------------------------------------- Types

	private enum SegmentType { HEADING, PARAGRAPH, LIST, CODE, QUOTE, OTHER }

	private record Segment(SegmentType type, int level, String text, int tokens,
			List<String> headingPath, boolean atomic) { }

}

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.olat.core.commons.services.ai.essay.AiContentChunk;

/**
 * Unit tests for {@link AiContentChunker} — chunk boundaries, edge cases,
 * CJK token estimation, chunk id determinism.
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class AiContentChunkerTest {

	// ---------------------------------------------------------------- null / blank

	@Test
	public void chunk_nullMarkdownReturnsEmptyList() {
		AiContentChunker chunker = new AiContentChunker();
		List<AiContentChunk> chunks = chunker.chunk(null, Locale.ENGLISH);
		assertNotNull(chunks);
		assertTrue(chunks.isEmpty());
	}

	@Test
	public void chunk_blankMarkdownReturnsEmptyList() {
		AiContentChunker chunker = new AiContentChunker();
		List<AiContentChunk> chunks = chunker.chunk("   \n   ", Locale.ENGLISH);
		assertTrue(chunks.isEmpty());
	}

	// ---------------------------------------------------------------- single small document → one chunk

	@Test
	public void chunk_smallDocumentProducesOneChunk() {
		AiContentChunker chunker = new AiContentChunker();
		String md = "# My Document\n\nThis is a short paragraph.";
		List<AiContentChunk> chunks = chunker.chunk(md, Locale.ENGLISH);
		assertEquals("Small document must produce exactly one chunk", 1, chunks.size());
	}

	@Test
	public void chunk_singleChunkHasNonBlankId() {
		AiContentChunker chunker = new AiContentChunker();
		List<AiContentChunk> chunks = chunker.chunk("Hello world", Locale.ENGLISH);
		assertEquals(1, chunks.size());
		assertNotNull(chunks.get(0).id());
		assertFalse(chunks.get(0).id().isBlank());
	}

	// ---------------------------------------------------------------- H1 forces chunk boundary

	@Test
	public void chunk_twoH1SectionsProduceTwoChunks() {
		// Use a small budget so the test is not dependent on document size
		AiContentChunker chunker = new AiContentChunker(50, 0);
		String md = "# Section One\n\nParagraph in section one.\n\n# Section Two\n\nParagraph in section two.";
		List<AiContentChunk> chunks = chunker.chunk(md, Locale.ENGLISH);
		assertTrue("Two H1 sections must produce at least 2 chunks", chunks.size() >= 2);
	}

	// ---------------------------------------------------------------- budget overflow → multiple chunks

	@Test
	public void chunk_oversizedDocumentProducesMultipleChunks() {
		// Budget of 20 tokens, no overlap — every ~80-char paragraph overflows
		AiContentChunker chunker = new AiContentChunker(20, 0);
		StringBuilder md = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			md.append("Paragraph ").append(i)
					.append(": This is a paragraph with several words that will exceed the token budget.\n\n");
		}
		List<AiContentChunk> chunks = chunker.chunk(md.toString(), Locale.ENGLISH);
		assertTrue("Oversized document must produce multiple chunks", chunks.size() > 1);
	}

	// ---------------------------------------------------------------- chunk id is deterministic

	@Test
	public void computeChunkId_deterministic() {
		String id1 = AiContentChunker.computeChunkId("hello world");
		String id2 = AiContentChunker.computeChunkId("hello world");
		assertEquals("Chunk id must be deterministic", id1, id2);
	}

	@Test
	public void computeChunkId_length16() {
		String id = AiContentChunker.computeChunkId("test text");
		assertEquals("Chunk id must be 16 hex chars", 16, id.length());
	}

	@Test
	public void computeChunkId_whitespaceNormalized() {
		// Whitespace-normalised text must produce the same id
		String id1 = AiContentChunker.computeChunkId("hello   world");
		String id2 = AiContentChunker.computeChunkId("hello world");
		assertEquals("Whitespace normalisation must produce the same id", id1, id2);
	}

	@Test
	public void computeChunkId_differentInputsDifferentIds() {
		String id1 = AiContentChunker.computeChunkId("text one");
		String id2 = AiContentChunker.computeChunkId("text two");
		assertFalse("Different inputs must produce different chunk ids", id1.equals(id2));
	}

	// ---------------------------------------------------------------- token estimation

	@Test
	public void estimateTokens_emptyReturnsZero() {
		assertEquals(0, AiContentChunker.estimateTokens(""));
	}

	@Test
	public void estimateTokens_nullReturnsZero() {
		assertEquals(0, AiContentChunker.estimateTokens(null));
	}

	@Test
	public void estimateTokens_latinScriptUsesDivFour() {
		// 40 ASCII chars / 4 ≈ 10 tokens
		String text = "A".repeat(40);
		int tokens = AiContentChunker.estimateTokens(text);
		assertTrue("Latin text estimate should be ~10 tokens", tokens >= 8 && tokens <= 12);
	}

	@Test
	public void estimateTokens_cjkHeavyTextUsesDivOneSix() {
		// 160 CJK chars → CJK-heavy → 160/1.6 = 100 tokens
		String cjk = "字".repeat(160);
		int tokens = AiContentChunker.estimateTokens(cjk);
		assertTrue("CJK-heavy text must use /1.6 divisor; got " + tokens, tokens >= 95 && tokens <= 105);
	}

	// ---------------------------------------------------------------- null locale fallback

	@Test
	public void chunk_nullLocaleUsesEnglishFallback() {
		AiContentChunker chunker = new AiContentChunker();
		List<AiContentChunk> chunks = chunker.chunk("# Title\n\nSome text.", null);
		assertFalse("Null locale must not throw", chunks.isEmpty());
	}

	// ---------------------------------------------------------------- oversize flag

	@Test
	public void chunk_atomicUnitExceedingBudgetIsMarkedOversize() {
		// Very tight budget so a code block exceeds it
		AiContentChunker chunker = new AiContentChunker(5, 0);
		String md = "```\n" + "x".repeat(100) + "\n```";
		List<AiContentChunk> chunks = chunker.chunk(md, Locale.ENGLISH);
		assertFalse(chunks.isEmpty());
		assertTrue("Oversize atomic unit must be flagged", chunks.stream().anyMatch(AiContentChunk::oversize));
	}
}

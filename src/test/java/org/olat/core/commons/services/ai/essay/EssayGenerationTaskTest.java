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
package org.olat.core.commons.services.ai.essay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.olat.core.commons.services.ai.essay.EssayGenerationService.GenerationDestination;
import org.olat.core.commons.services.ai.essay.EssayGenerationService.GenerationRequest;
import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;

/**
 * Unit tests for {@link EssayGenerationTask} — the task is XStream-serialised
 * into {@code o_ex_task.e_task} by the generic task executor (same setup as
 * {@code PersistentTaskDAO}), so the full request payload must survive the
 * XML round-trip.
 *
 * Pure Java — no Spring context required.
 *
 * Initial date: 2026-06-10<br>
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 */
public class EssayGenerationTaskTest {

	private static XStream createTaskExecutorXStream() {
		// Mirrors the XStream setup of PersistentTaskDAO
		XStream xstream = XStreamHelper.createXStreamInstance();
		XStreamHelper.allowDefaultPackage(xstream);
		return xstream;
	}

	@Test
	public void xstreamRoundTrip_quizPartRequest() {
		GenerationRequest request = GenerationRequest.forQuizPart(
				"# Topic\nSome content here.", 42L, Locale.GERMAN, null,
				10L, 20L, 3, 5,
				List.of(AiBloomLevel.ANALYSE, AiBloomLevel.EVALUATE), 4,
				List.of("Compare A with B", "Evaluate trade-offs"));
		EssayGenerationTask task = new EssayGenerationTask(request);

		XStream xstream = createTaskExecutorXStream();
		String xml = xstream.toXML(task);
		EssayGenerationTask restored = (EssayGenerationTask) xstream.fromXML(xml);

		GenerationRequest rebuilt = restored.toGenerationRequest(null);
		assertEquals(request.pageMarkdown(), rebuilt.pageMarkdown());
		assertEquals(request.repositoryEntryKey(), rebuilt.repositoryEntryKey());
		assertEquals(request.targetQuestionCount(), rebuilt.targetQuestionCount());
		assertEquals(request.mcQuestionCount(), rebuilt.mcQuestionCount());
		assertEquals(request.targetBloomLevels(), rebuilt.targetBloomLevels());
		assertEquals(request.targetDifficulty(), rebuilt.targetDifficulty());
		assertEquals(request.learningObjectives(), rebuilt.learningObjectives());
		assertEquals(Locale.GERMAN.toLanguageTag(), rebuilt.language().toLanguageTag());
		assertEquals(request.pageKey(), rebuilt.pageKey());
		assertEquals(request.quizPartKey(), rebuilt.quizPartKey());
		assertEquals(GenerationDestination.QUIZ_PART, rebuilt.destination());
		assertNull(restored.getRequesterKey());
	}

	@Test
	public void xstreamRoundTrip_poolRequestWithTaxonomy() {
		GenerationRequest request = GenerationRequest.forPool(
				"Source text for the pool.", null, Locale.ENGLISH, null,
				2, 0, 99L);
		EssayGenerationTask task = new EssayGenerationTask(request);

		XStream xstream = createTaskExecutorXStream();
		EssayGenerationTask restored = (EssayGenerationTask) xstream.fromXML(xstream.toXML(task));

		GenerationRequest rebuilt = restored.toGenerationRequest(null);
		assertEquals(request.pageMarkdown(), rebuilt.pageMarkdown());
		assertNull(rebuilt.repositoryEntryKey());
		assertEquals(GenerationDestination.POOL, rebuilt.destination());
		assertEquals(Long.valueOf(99L), rebuilt.taxonomyLevelKey());
		assertEquals(0, rebuilt.mcQuestionCount());
	}

	@Test
	public void xstreamRoundTrip_minimalDrawerRequest() {
		GenerationRequest request = GenerationRequest.of(
				"Drawer source markdown.", 7L, null, null);
		EssayGenerationTask task = new EssayGenerationTask(request);

		XStream xstream = createTaskExecutorXStream();
		EssayGenerationTask restored = (EssayGenerationTask) xstream.fromXML(xstream.toXML(task));

		GenerationRequest rebuilt = restored.toGenerationRequest(null);
		assertEquals(request.pageMarkdown(), rebuilt.pageMarkdown());
		assertNull(rebuilt.language());
		assertEquals(GenerationDestination.DRAWER, rebuilt.destination());
		assertNull(rebuilt.taxonomyLevelKey());
	}
}

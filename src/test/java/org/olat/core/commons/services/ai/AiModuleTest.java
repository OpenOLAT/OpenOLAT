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
package org.olat.core.commons.services.ai;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import dev.langchain4j.model.chat.ChatModel;

/**
 * Unit tests for {@link AiModule} business logic.
 *
 * Initial date: 28.02.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 */
public class AiModuleTest extends OlatTestCase {

	private static class SimpleAiSpi implements AiSPI {
		private final String id;
		private boolean enabled;

		SimpleAiSpi(String id, boolean enabled) {
			this.id = id;
			this.enabled = enabled;
		}

		@Override public String getId() { return id; }
		@Override public String getName() { return id; }
		@Override public boolean isEnabled() { return enabled; }
		@Override public void setEnabled(boolean enabled) { this.enabled = enabled; }
		@Override public org.olat.core.gui.control.Controller createAdminController(
				org.olat.core.gui.UserRequest ureq, org.olat.core.gui.control.WindowControl wControl) {
			return null;
		}
		@Override public ChatModel buildChatModel(String modelName, int maxTokens) { return null; }
		@Override public List<String> getAvailableModels() { return List.of(); }
	}

	@Autowired
	private AiModule module;

	private void setConfig(String spiId, String model) throws Exception {
		var spiIdField = AiModule.class.getDeclaredField("mcGeneratorSpiId");
		spiIdField.setAccessible(true);
		spiIdField.set(module, spiId);

		var modelField = AiModule.class.getDeclaredField("mcGeneratorModel");
		modelField.setAccessible(true);
		modelField.set(module, model);
	}

	private void setImgDescConfig(String spiId, String model) throws Exception {
		var spiIdField = AiModule.class.getDeclaredField("imgDescSpiId");
		spiIdField.setAccessible(true);
		spiIdField.set(module, spiId);

		var modelField = AiModule.class.getDeclaredField("imgDescModel");
		modelField.setAccessible(true);
		modelField.set(module, model);
	}


	// ─── isMCQuestionGeneratorEnabled ──────────────────────────────────────────

	@Test
	public void isMCQuestionGeneratorEnabled_noSpiIdConfigured_returnsFalse() throws Exception {
		module.setSpringProviders(List.of(new SimpleAiSpi("OpenAI", true)));
		setConfig(null, null);

		assertFalse(module.isMCQuestionGeneratorEnabled());
	}

	@Test
	public void isMCQuestionGeneratorEnabled_spiIdNotMatchingAnyProvider_returnsFalse() throws Exception {
		module.setSpringProviders(List.of(new SimpleAiSpi("OpenAI", true)));
		setConfig("Claude", "gpt-4o");

		assertFalse(module.isMCQuestionGeneratorEnabled());
	}

	@Test
	public void isMCQuestionGeneratorEnabled_matchingSpiButDisabled_returnsFalse() throws Exception {
		module.setSpringProviders(List.of(new SimpleAiSpi("OpenAI", false)));
		setConfig("OpenAI", "gpt-4o");

		assertFalse(module.isMCQuestionGeneratorEnabled());
	}

	@Test
	public void isMCQuestionGeneratorEnabled_noModelConfigured_returnsFalse() throws Exception {
		module.setSpringProviders(List.of(new SimpleAiSpi("OpenAI", true)));
		setConfig("OpenAI", null);

		assertFalse(module.isMCQuestionGeneratorEnabled());
	}

	@Test
	public void isMCQuestionGeneratorEnabled_allConditionsMet_returnsTrue() throws Exception {
		module.setSpringProviders(List.of(new SimpleAiSpi("OpenAI", true)));
		setConfig("OpenAI", "gpt-4o");

		assertTrue(module.isMCQuestionGeneratorEnabled());
	}

	@Test
	public void isMCQuestionGeneratorEnabled_multipleProviders_onlyMatchingCounts() throws Exception {
		module.setSpringProviders(List.of(
				new SimpleAiSpi("OpenAI", true),
				new SimpleAiSpi("Claude", false)
		));
		setConfig("Claude", "claude-3-5-sonnet");

		assertFalse(module.isMCQuestionGeneratorEnabled());
	}


	// ─── isImageDescriptionGeneratorEnabled ───────────────────────────────────

	@Test
	public void isImageDescriptionGeneratorEnabled_noSpiIdConfigured_returnsFalse() throws Exception {
		module.setSpringProviders(List.of(new SimpleAiSpi("OpenAI", true)));
		setImgDescConfig(null, null);

		assertFalse(module.isImageDescriptionGeneratorEnabled());
	}

	@Test
	public void isImageDescriptionGeneratorEnabled_spiIdNotMatchingAnyProvider_returnsFalse() throws Exception {
		module.setSpringProviders(List.of(new SimpleAiSpi("OpenAI", true)));
		setImgDescConfig("Unknown", "gpt-4o");

		assertFalse(module.isImageDescriptionGeneratorEnabled());
	}

	@Test
	public void isImageDescriptionGeneratorEnabled_matchingSpiButDisabled_returnsFalse() throws Exception {
		module.setSpringProviders(List.of(new SimpleAiSpi("OpenAI", false)));
		setImgDescConfig("OpenAI", "gpt-4o");

		assertFalse(module.isImageDescriptionGeneratorEnabled());
	}

	@Test
	public void isImageDescriptionGeneratorEnabled_noModelConfigured_returnsFalse() throws Exception {
		module.setSpringProviders(List.of(new SimpleAiSpi("OpenAI", true)));
		setImgDescConfig("OpenAI", null);

		assertFalse(module.isImageDescriptionGeneratorEnabled());
	}

	@Test
	public void isImageDescriptionGeneratorEnabled_allConditionsMet_returnsTrue() throws Exception {
		module.setSpringProviders(List.of(new SimpleAiSpi("OpenAI", true)));
		setImgDescConfig("OpenAI", "gpt-4o");

		assertTrue(module.isImageDescriptionGeneratorEnabled());
	}


	// ─── resolveProvider ──────────────────────────────────────────────────────

	@Test
	public void resolveProvider_nullId_returnsNull() {
		module.setSpringProviders(List.of(new SimpleAiSpi("OpenAI", true)));

		assertNull(module.resolveProvider(null));
	}

	@Test
	public void resolveProvider_noMatchingId_returnsNull() {
		module.setSpringProviders(List.of(new SimpleAiSpi("OpenAI", true)));

		assertNull(module.resolveProvider("Claude"));
	}

	@Test
	public void resolveProvider_matchingButDisabled_returnsNull() {
		module.setSpringProviders(List.of(new SimpleAiSpi("OpenAI", false)));

		assertNull(module.resolveProvider("OpenAI"));
	}

	@Test
	public void resolveProvider_matchingAndEnabled_returnsProvider() {
		SimpleAiSpi spi = new SimpleAiSpi("OpenAI", true);
		module.setSpringProviders(List.of(spi));

		AiSPI result = module.resolveProvider("OpenAI");
		assertNotNull(result);
		assertEquals("OpenAI", result.getId());
	}


	// ─── getEnabledProviders ──────────────────────────────────────────────────

	@Test
	public void getEnabledProviders_returnsOnlyEnabled() {
		module.setSpringProviders(List.of(
				new SimpleAiSpi("OpenAI", true),
				new SimpleAiSpi("Claude", false),
				new SimpleAiSpi("Other", true)
		));

		List<AiSPI> result = module.getEnabledProviders();
		assertEquals(2, result.size());
		assertTrue(result.stream().anyMatch(s -> s.getId().equals("OpenAI")));
		assertTrue(result.stream().anyMatch(s -> s.getId().equals("Other")));
	}

	@Test
	public void getEnabledProviders_allDisabled_returnsEmptyList() {
		module.setSpringProviders(List.of(
				new SimpleAiSpi("OpenAI", false),
				new SimpleAiSpi("Claude", false)
		));

		assertTrue(module.getEnabledProviders().isEmpty());
	}
}

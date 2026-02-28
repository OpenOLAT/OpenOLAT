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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.services.ai.model.AiMCQuestionsResponse;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.coordinate.CoordinatorManager;

/**
 * Unit tests for {@link AiModule} business logic.
 * Uses Mockito to avoid Spring context and AbstractSpringModule infrastructure.
 * Fields are set directly via the module's own setters/reflection to keep tests
 * focused on the logic under test.
 *
 * Initial date: 28.02.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 */
public class AiModuleTest {

	/** Minimal AiSPI that is NOT an AiMCQuestionGeneratorSPI */
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
	}

	/** Minimal SPI that also implements AiMCQuestionGeneratorSPI */
	private static class MCGeneratorSpi extends SimpleAiSpi implements AiMCQuestionGeneratorSPI {
		private String model;

		MCGeneratorSpi(String id, boolean enabled) {
			super(id, enabled);
		}

		@Override public AiMCQuestionsResponse generateMCQuestionsResponse(String input, int number) { return null; }
		@Override public void setMCGeneratorModel(String model) { this.model = model; }
		@Override public String getMCGeneratorModel() { return model; }
		@Override public List<String> getAvailableMCGeneratorModels() { return List.of(); }
	}

	private AiModule module;

	@Before
	public void setUp() throws Exception {
		// Minimal mock of CoordinatorManager so AbstractSpringModule constructor does not NPE
		CoordinatorManager coordinatorManager = mock(CoordinatorManager.class);
		Coordinator coordinator = mock(Coordinator.class);
		when(coordinatorManager.getCoordinator()).thenReturn(coordinator);

		module = new AiModule(coordinatorManager);
	}

	/** Set mcGeneratorSpiId and mcGeneratorModel via the module's config method,
	 *  bypassing property persistence by directly manipulating fields via reflection. */
	private void setConfig(String spiId, String model) throws Exception {
		var spiIdField = AiModule.class.getDeclaredField("mcGeneratorSpiId");
		spiIdField.setAccessible(true);
		spiIdField.set(module, spiId);

		var modelField = AiModule.class.getDeclaredField("mcGeneratorModel");
		modelField.setAccessible(true);
		modelField.set(module, model);
	}


	// ─── isMCQuestionGeneratorEnabled ──────────────────────────────────────────

	@Test
	public void isMCQuestionGeneratorEnabled_noSpiIdConfigured_returnsFalse() throws Exception {
		module.setAiProviders(List.of(new MCGeneratorSpi("OpenAI", true)));
		setConfig(null, null);

		assertFalse(module.isMCQuestionGeneratorEnabled());
	}

	@Test
	public void isMCQuestionGeneratorEnabled_spiIdNotMatchingAnyProvider_returnsFalse() throws Exception {
		module.setAiProviders(List.of(new MCGeneratorSpi("OpenAI", true)));
		setConfig("Claude", "gpt-4o");

		assertFalse(module.isMCQuestionGeneratorEnabled());
	}

	@Test
	public void isMCQuestionGeneratorEnabled_matchingSpiButDisabled_returnsFalse() throws Exception {
		module.setAiProviders(List.of(new MCGeneratorSpi("OpenAI", false)));
		setConfig("OpenAI", "gpt-4o");

		assertFalse(module.isMCQuestionGeneratorEnabled());
	}

	@Test
	public void isMCQuestionGeneratorEnabled_spiDoesNotImplementMCInterface_returnsFalse() throws Exception {
		// SPI with matching ID and enabled, but NOT an AiMCQuestionGeneratorSPI
		module.setAiProviders(List.of(new SimpleAiSpi("OpenAI", true)));
		setConfig("OpenAI", "gpt-4o");

		assertFalse(module.isMCQuestionGeneratorEnabled());
	}

	@Test
	public void isMCQuestionGeneratorEnabled_allConditionsMet_returnsTrue() throws Exception {
		module.setAiProviders(List.of(new MCGeneratorSpi("OpenAI", true)));
		setConfig("OpenAI", "gpt-4o");

		assertTrue(module.isMCQuestionGeneratorEnabled());
	}

	@Test
	public void isMCQuestionGeneratorEnabled_multipleProviders_onlyMatchingCounts() throws Exception {
		module.setAiProviders(List.of(
				new MCGeneratorSpi("OpenAI", true),
				new MCGeneratorSpi("Claude", false)
		));
		setConfig("Claude", "claude-3-5-sonnet");

		// Claude is disabled → false
		assertFalse(module.isMCQuestionGeneratorEnabled());
	}


	// ─── isAiEnabled ──────────────────────────────────────────────────────────

	@Test
	public void isAiEnabled_delegatesToMCQuestionGeneratorEnabled() throws Exception {
		module.setAiProviders(List.of(new MCGeneratorSpi("OpenAI", true)));
		setConfig("OpenAI", "gpt-4o");

		assertEquals(module.isMCQuestionGeneratorEnabled(), module.isAiEnabled());
	}


	// ─── getMCQuestionGenerator ────────────────────────────────────────────────

	@Test
	public void getMCQuestionGenerator_noSpiIdConfigured_returnsNull() throws Exception {
		module.setAiProviders(List.of(new MCGeneratorSpi("OpenAI", true)));
		setConfig(null, null);

		assertNull(module.getMCQuestionGenerator());
	}

	@Test
	public void getMCQuestionGenerator_spiDisabled_returnsNull() throws Exception {
		module.setAiProviders(List.of(new MCGeneratorSpi("OpenAI", false)));
		setConfig("OpenAI", "gpt-4o");

		assertNull(module.getMCQuestionGenerator());
	}

	@Test
	public void getMCQuestionGenerator_properlyConfigured_returnsGeneratorWithModelSet() throws Exception {
		MCGeneratorSpi spi = new MCGeneratorSpi("OpenAI", true);
		module.setAiProviders(List.of(spi));
		setConfig("OpenAI", "gpt-4o");

		AiMCQuestionGeneratorSPI generator = module.getMCQuestionGenerator();
		assertNotNull(generator);
		assertEquals("gpt-4o", generator.getMCGeneratorModel());
	}

	@Test
	public void getMCQuestionGenerator_setsModelOnSpiBeforeReturning() throws Exception {
		MCGeneratorSpi spi = new MCGeneratorSpi("Claude", true);
		module.setAiProviders(List.of(spi));
		setConfig("Claude", "claude-3-5-sonnet");

		AiMCQuestionGeneratorSPI generator = module.getMCQuestionGenerator();
		// Verify the model was injected
		assertEquals("claude-3-5-sonnet", spi.getMCGeneratorModel());
	}


	// ─── getEnabledSPIsFor ─────────────────────────────────────────────────────

	@Test
	public void getEnabledSPIsFor_returnsOnlyEnabledSPIsImplementingFeature() {
		MCGeneratorSpi enabledMC = new MCGeneratorSpi("OpenAI", true);
		MCGeneratorSpi disabledMC = new MCGeneratorSpi("Claude", false);
		SimpleAiSpi enabledSimple = new SimpleAiSpi("Other", true);
		module.setAiProviders(List.of(enabledMC, disabledMC, enabledSimple));

		List<AiSPI> result = module.getEnabledSPIsFor(AiMCQuestionGeneratorSPI.class);
		assertEquals(1, result.size());
		assertEquals("OpenAI", result.get(0).getId());
	}

	@Test
	public void getEnabledSPIsFor_noMatchingProviders_returnsEmptyList() {
		module.setAiProviders(List.of(new SimpleAiSpi("Other", true)));

		List<AiSPI> result = module.getEnabledSPIsFor(AiMCQuestionGeneratorSPI.class);
		assertTrue(result.isEmpty());
	}

	@Test
	public void getEnabledSPIsFor_allDisabled_returnsEmptyList() {
		module.setAiProviders(List.of(
				new MCGeneratorSpi("OpenAI", false),
				new MCGeneratorSpi("Claude", false)
		));

		List<AiSPI> result = module.getEnabledSPIsFor(AiMCQuestionGeneratorSPI.class);
		assertTrue(result.isEmpty());
	}

	@Test
	public void getEnabledSPIsFor_multipleEnabledProviders_returnsAll() {
		module.setAiProviders(List.of(
				new MCGeneratorSpi("OpenAI", true),
				new MCGeneratorSpi("Claude", true)
		));

		List<AiSPI> result = module.getEnabledSPIsFor(AiMCQuestionGeneratorSPI.class);
		assertEquals(2, result.size());
	}
}

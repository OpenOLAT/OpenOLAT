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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.id.Identity;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.prefs.ram.RamPreferences;

/**
 * Unit test of the resolution of the AI preference against the system default.
 * It covers the six rows of the effect table of the specification, the
 * unavailable feature and the null identity. No database, no Spring context.
 *
 * Initial date: Sep 13, 2026<br>
 * @author AI, ai@frentix.com, https://www.frentix.com
 *
 */
public class AiUserPreferenceServiceTest {

	/**
	 * In-memory Preferences that is not a RamPreferences, so it is not treated as
	 * the preferences of a guest.
	 */
	private static final class MapPreferences implements Preferences {

		private final Map<String, Object> store = new HashMap<>();

		@Override
		public Object get(Class<?> attributedClass, String key) {
			return get(attributedClass.getName(), key);
		}

		@Override
		public Object get(String attributedClass, String key) {
			return store.get(attributedClass + ":" + key);
		}

		@Override
		public Object get(Class<?> attributedClass, String key, Object defaultValue) {
			Object value = get(attributedClass, key);
			return value == null ? defaultValue : value;
		}

		@Override
		public <U> List<U> getList(Class<?> attributedClass, String key, Class<U> type) {
			return List.of();
		}

		@Override
		public void putAndSave(Class<?> attributedClass, String key, Object value) {
			putAndSave(attributedClass.getName(), key, value);
		}

		@Override
		public void putAndSave(String attributedClass, String key, Object value) {
			store.put(attributedClass + ":" + key, value);
		}

		@Override
		public Object findPrefByKey(String key) {
			return null;
		}
	}

	private static final String ESSAY_KEY = "ai.optout.essay-grading";
	private static final String IMAGE_KEY = "ai.optout.image-description-generator";

	@Mock
	private AiModule aiModuleMock;
	@Mock
	private AiEssayGradingService aiEssayGradingServiceMock;
	@Mock
	private AiImageDescriptionService aiImageDescriptionServiceMock;

	@InjectMocks
	private AiUserPreferenceService sut = new AiUserPreferenceService();

	private MapPreferences prefs;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		prefs = new MapPreferences();
	}

	private void featuresAvailable(boolean available) {
		when(aiEssayGradingServiceMock.isEnabled()).thenReturn(available);
		when(aiImageDescriptionServiceMock.isEnabled()).thenReturn(available);
	}

	private void systemDefault(boolean on) {
		when(aiModuleMock.isUserDefaultOn(AiFeature.EssayGrading)).thenReturn(on);
		when(aiModuleMock.isUserDefaultOn(AiFeature.ImageDescriptionGenerator)).thenReturn(on);
	}

	private void store(String value) {
		prefs.putAndSave(AiUserPreferenceService.class, ESSAY_KEY, value);
		prefs.putAndSave(AiUserPreferenceService.class, IMAGE_KEY, value);
	}

	// --- WP1: the flag on the enum ---

	@Test
	public void feature_userControlled_onlyTwoConstants() {
		assertThat(AiFeature.EssayGrading.isUserControlled()).isTrue();
		assertThat(AiFeature.ImageDescriptionGenerator.isUserControlled()).isTrue();
		assertThat(AiFeature.MCQuestionGenerator.isUserControlled()).isFalse();
		assertThat(AiFeature.EssayGeneration.isUserControlled()).isFalse();
		assertThat(AiFeature.TaxonomyMatching.isUserControlled()).isFalse();
	}

	@Test
	public void feature_i18nKeys_derivedFromTheExistingKey() {
		assertThat(AiFeature.EssayGrading.getI18nKey()).isEqualTo("ai.feature.essay-grading");
		assertThat(AiFeature.EssayGrading.getI18nDescriptionKey()).isEqualTo("ai.feature.essay-grading.desc");
		assertThat(AiFeature.EssayGrading.getI18nUserNameKey()).isEqualTo("ai.feature.essay-grading.user");
		assertThat(AiFeature.EssayGrading.getType()).isEqualTo("essay-grading");
	}

	// --- Effect table, rows 1 to 6. Every row assumes an available feature. ---

	@Test
	public void effectTable_row1_preferenceOn_defaultOn_active() {
		featuresAvailable(true);
		systemDefault(true);
		store("on");

		assertThat(sut.isActive(prefs, AiFeature.EssayGrading)).isTrue();
		assertThat(sut.isActive(prefs, AiFeature.ImageDescriptionGenerator)).isTrue();
	}

	@Test
	public void effectTable_row2_preferenceOn_defaultOff_active() {
		featuresAvailable(true);
		systemDefault(false);
		store("on");

		assertThat(sut.isActive(prefs, AiFeature.EssayGrading)).isTrue();
		assertThat(sut.isActive(prefs, AiFeature.ImageDescriptionGenerator)).isTrue();
	}

	@Test
	public void effectTable_row3_preferenceOff_defaultOn_inactive() {
		featuresAvailable(true);
		systemDefault(true);
		store("off");

		assertThat(sut.isActive(prefs, AiFeature.EssayGrading)).isFalse();
		assertThat(sut.isActive(prefs, AiFeature.ImageDescriptionGenerator)).isFalse();
	}

	@Test
	public void effectTable_row4_preferenceOff_defaultOff_inactive() {
		featuresAvailable(true);
		systemDefault(false);
		store("off");

		assertThat(sut.isActive(prefs, AiFeature.EssayGrading)).isFalse();
		assertThat(sut.isActive(prefs, AiFeature.ImageDescriptionGenerator)).isFalse();
	}

	@Test
	public void effectTable_row5_noPreference_defaultOn_active() {
		featuresAvailable(true);
		systemDefault(true);

		assertThat(sut.isActive(prefs, AiFeature.EssayGrading)).isTrue();
		assertThat(sut.isActive(prefs, AiFeature.ImageDescriptionGenerator)).isTrue();

		store("default");
		assertThat(sut.isActive(prefs, AiFeature.EssayGrading)).isTrue();
		assertThat(sut.isActive(prefs, AiFeature.ImageDescriptionGenerator)).isTrue();
	}

	@Test
	public void effectTable_row6_noPreference_defaultOff_inactive() {
		featuresAvailable(true);
		systemDefault(false);

		assertThat(sut.isActive(prefs, AiFeature.EssayGrading)).isFalse();
		assertThat(sut.isActive(prefs, AiFeature.ImageDescriptionGenerator)).isFalse();

		store("default");
		assertThat(sut.isActive(prefs, AiFeature.EssayGrading)).isFalse();
		assertThat(sut.isActive(prefs, AiFeature.ImageDescriptionGenerator)).isFalse();
	}

	// --- The unavailable feature ---

	@Test
	public void isActive_featureNotAvailable_falseForEveryPreference() {
		featuresAvailable(false);
		systemDefault(true);

		for (String value : new String[] { "on", "off", "default" }) {
			store(value);
			assertThat(sut.isActive(prefs, AiFeature.EssayGrading)).isFalse();
			assertThat(sut.isActive(prefs, AiFeature.ImageDescriptionGenerator)).isFalse();
		}
	}

	@Test
	public void isFeatureAvailable_onlyTheTwoControlledFeatures() {
		featuresAvailable(true);

		assertThat(sut.isFeatureAvailable(AiFeature.EssayGrading)).isTrue();
		assertThat(sut.isFeatureAvailable(AiFeature.ImageDescriptionGenerator)).isTrue();
		assertThat(sut.isFeatureAvailable(AiFeature.MCQuestionGenerator)).isFalse();
		assertThat(sut.isFeatureAvailable(AiFeature.EssayGeneration)).isFalse();
		assertThat(sut.isFeatureAvailable(AiFeature.TaxonomyMatching)).isFalse();
		assertThat(sut.isFeatureAvailable(null)).isFalse();
	}

	// --- The null preferences ---

	@Test
	public void isActive_nullPreferences_failsClosed() {
		featuresAvailable(true);
		systemDefault(true);

		assertThat(sut.isActive((Preferences) null, AiFeature.EssayGrading)).isFalse();
		assertThat(sut.isActive((Preferences) null, AiFeature.ImageDescriptionGenerator)).isFalse();
	}

	// --- get and hasPreference ---

	@Test
	public void get_absentRow_default() {
		assertThat(sut.get(prefs, AiFeature.EssayGrading)).isEqualTo(AiUserPreference.DEFAULT);
	}

	@Test
	public void get_unreadableValue_default() {
		prefs.putAndSave(AiUserPreferenceService.class, ESSAY_KEY, "yes please");
		assertThat(sut.get(prefs, AiFeature.EssayGrading)).isEqualTo(AiUserPreference.DEFAULT);

		prefs.putAndSave(AiUserPreferenceService.class, ESSAY_KEY, Integer.valueOf(1));
		assertThat(sut.get(prefs, AiFeature.EssayGrading)).isEqualTo(AiUserPreference.DEFAULT);
	}

	@Test
	public void get_storedValues_mapToTheEnum() {
		store("on");
		assertThat(sut.get(prefs, AiFeature.EssayGrading)).isEqualTo(AiUserPreference.ON);
		store("off");
		assertThat(sut.get(prefs, AiFeature.EssayGrading)).isEqualTo(AiUserPreference.OFF);
		store("default");
		assertThat(sut.get(prefs, AiFeature.EssayGrading)).isEqualTo(AiUserPreference.DEFAULT);
	}

	@Test
	public void hasPreference_trueOnlyForStoredOnOrOff() {
		assertThat(sut.hasPreference(prefs, AiFeature.EssayGrading)).isFalse();

		store("default");
		assertThat(sut.hasPreference(prefs, AiFeature.EssayGrading)).isFalse();

		store("not a value");
		assertThat(sut.hasPreference(prefs, AiFeature.EssayGrading)).isFalse();

		store("on");
		assertThat(sut.hasPreference(prefs, AiFeature.EssayGrading)).isTrue();

		store("off");
		assertThat(sut.hasPreference(prefs, AiFeature.EssayGrading)).isTrue();
	}

	// --- set ---

	@Test
	public void set_writeIsVisibleToTheNextRead() {
		sut.set(prefs, AiFeature.EssayGrading, AiUserPreference.ON);
		assertThat(sut.get(prefs, AiFeature.EssayGrading)).isEqualTo(AiUserPreference.ON);

		sut.set(prefs, AiFeature.EssayGrading, AiUserPreference.OFF);
		assertThat(sut.get(prefs, AiFeature.EssayGrading)).isEqualTo(AiUserPreference.OFF);

		sut.set(prefs, AiFeature.EssayGrading, AiUserPreference.DEFAULT);
		assertThat(sut.get(prefs, AiFeature.EssayGrading)).isEqualTo(AiUserPreference.DEFAULT);
	}

	@Test
	public void set_writesTheLowerCaseString() {
		sut.set(prefs, AiFeature.EssayGrading, AiUserPreference.ON);
		assertThat(prefs.get(AiUserPreferenceService.class, ESSAY_KEY)).isEqualTo("on");

		sut.set(prefs, AiFeature.ImageDescriptionGenerator, AiUserPreference.DEFAULT);
		assertThat(prefs.get(AiUserPreferenceService.class, IMAGE_KEY)).isEqualTo("default");
	}

	@Test
	public void set_perFeature_doesNotTouchTheOtherFeature() {
		sut.set(prefs, AiFeature.EssayGrading, AiUserPreference.OFF);

		assertThat(sut.get(prefs, AiFeature.EssayGrading)).isEqualTo(AiUserPreference.OFF);
		assertThat(sut.get(prefs, AiFeature.ImageDescriptionGenerator)).isEqualTo(AiUserPreference.DEFAULT);
	}

	@Test
	public void set_guest_writesNothing() {
		RamPreferences guestPrefs = new RamPreferences();

		sut.set(guestPrefs, AiFeature.EssayGrading, AiUserPreference.OFF);

		assertThat(guestPrefs.get(AiUserPreferenceService.class, ESSAY_KEY)).isNull();
		assertThat(sut.get(guestPrefs, AiFeature.EssayGrading)).isEqualTo(AiUserPreference.DEFAULT);
	}

	@Test
	public void set_nullArguments_writeNothing() {
		sut.set(null, AiFeature.EssayGrading, AiUserPreference.ON);
		sut.set(prefs, null, AiUserPreference.ON);
		sut.set(prefs, AiFeature.EssayGrading, null);

		assertThat(prefs.get(AiUserPreferenceService.class, ESSAY_KEY)).isNull();
	}

	// --- The Identity overload of get(...) ---

	/**
	 * The overload for callers without a user session. Only the null branch is
	 * testable here: a non-null Identity resolves the preferences through the
	 * static PreferencesFactory.getInstance(), which needs the Spring context.
	 */
	@Test
	public void get_nullIdentity_default() {
		featuresAvailable(true);
		systemDefault(true);

		assertThat(sut.get((Identity) null, AiFeature.EssayGrading)).isEqualTo(AiUserPreference.DEFAULT);
		assertThat(sut.get((Identity) null, AiFeature.ImageDescriptionGenerator)).isEqualTo(AiUserPreference.DEFAULT);
		assertThat(sut.get((Identity) null, null)).isEqualTo(AiUserPreference.DEFAULT);
	}

	/**
	 * The reason codes of the defensive guard in EssayAiCorrectionService. The
	 * guard refuses on an unavailable feature and on a stored OFF, never on
	 * DEFAULT, because the consent of one run is invisible to the service.
	 */
	@Test
	public void get_defaultIsDistinguishableFromOff_forTheServiceGuard() {
		featuresAvailable(true);
		systemDefault(false);

		assertThat(sut.get(prefs, AiFeature.EssayGrading)).isEqualTo(AiUserPreference.DEFAULT);
		assertThat(sut.isActive(prefs, AiFeature.EssayGrading)).isFalse();

		store("off");
		assertThat(sut.get(prefs, AiFeature.EssayGrading)).isEqualTo(AiUserPreference.OFF);
	}

}

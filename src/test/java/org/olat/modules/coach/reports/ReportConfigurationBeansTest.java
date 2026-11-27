/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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
package org.olat.modules.coach.reports;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumReportConfiguration;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The report templates configured in Spring: their identifiers and their
 * texts in German and English.
 *
 * Initial date: 27 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ReportConfigurationBeansTest extends OlatTestCase {

	private static final List<String> TEMPLATE_IDS = List.of("coaching.openbookingorders",
			"coaching.absences", "coaching.certificates", "courseplanner.bookingorders");

	@Autowired
	private List<ReportConfiguration> templates;
	@Autowired
	private ReportTemplatesRegistry reportTemplatesRegistry;

	@Test
	public void templates() {
		Set<String> ids = new HashSet<>();
		for (ReportConfiguration template : templates) {
			Assert.assertTrue("Missing id: " + template, StringHelper.containsNonWhitespace(template.getId()));
			Assert.assertTrue("Duplicate id: " + template.getId(), ids.add(template.getId()));
		}
		Assert.assertEquals(Set.copyOf(TEMPLATE_IDS), ids);

		for (String id : TEMPLATE_IDS) {
			ReportConfiguration template = reportTemplatesRegistry.getTemplate(id);
			Assert.assertNotNull(id, template);
			Assert.assertEquals(id, template.getId());
		}
		Assert.assertTrue(reportTemplatesRegistry.getTemplate("courseplanner.bookingorders") instanceof CurriculumReportConfiguration);
	}

	@Test
	public void parameterDefinitions() {
		for (ReportConfiguration template : templates) {
			List<ReportParameterDefinition> definitions = template.getParameterDefinitions();
			Assert.assertFalse(template.getId(), definitions.isEmpty());
			Assert.assertEquals(template.getId(), ReportParameterName.timeRange, definitions.get(0).name());
			Assert.assertNotNull(template.getId(), definitions.get(0).defaultTimeRange());

			// The defaults of the dialog are valid
			ReportParameters defaults = ReportParameters.defaults(definitions);
			Assert.assertTrue(template.getId(), defaults.validate(definitions, new Date()).isEmpty());
		}
	}

	@Test
	public void translations() {
		for (ReportConfiguration template : templates) {
			String id = template.getId();
			assertTranslated(id, template.getName(Locale.GERMAN), template.getName(Locale.ENGLISH));
			assertTranslated(id, template.getDescription(Locale.GERMAN), template.getDescription(Locale.ENGLISH));
			assertTranslated(id, template.getCategory(Locale.GERMAN), template.getCategory(Locale.ENGLISH));

			boolean changedSince = template.getParameterDefinitions().stream()
					.anyMatch(definition -> definition.name() == ReportParameterName.changedSince);
			for (ReportParametersText text : ReportParametersText.values()) {
				if (text == ReportParametersText.changedSinceHint && !changedSince) {
					continue;
				}
				assertTranslated(id + " " + text, template.getParametersText(text, Locale.GERMAN),
						template.getParametersText(text, Locale.ENGLISH));
			}
		}
	}

	/**
	 * Both texts exist and the German one doesn't fall back to English.
	 */
	private static void assertTranslated(String message, String german, String english) {
		Assert.assertTrue(message, StringHelper.containsNonWhitespace(german));
		Assert.assertTrue(message, StringHelper.containsNonWhitespace(english));
		Assert.assertFalse(message + ": " + german, german.startsWith(Translator.NO_TRANSLATION_ERROR_PREFIX));
		Assert.assertFalse(message + ": " + english, english.startsWith(Translator.NO_TRANSLATION_ERROR_PREFIX));
		Assert.assertNotEquals(message, german, english);
	}
}

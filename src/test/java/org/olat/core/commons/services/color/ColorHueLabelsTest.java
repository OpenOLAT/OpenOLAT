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
package org.olat.core.commons.services.color;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

/**
 * The choosers of the sets {@link ColorSet} label every hue of
 * {@link ColorService#HUES} with the key "color.{hue}" of the colour bundle.
 * German and English must have a label for each hue.
 *
 * Initial date: 24 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 *
 */
public class ColorHueLabelsTest {

	@Test
	public void germanLabels() throws IOException {
		assertLabels("de");
	}

	@Test
	public void englishLabels() throws IOException {
		assertLabels("en");
	}

	private void assertLabels(String language) throws IOException {
		Properties properties = load(language);
		for (String hue : ColorService.HUES) {
			assertThat(properties.getProperty("color." + hue))
					.as("color.%s in %s", hue, language)
					.isNotBlank();
		}
	}

	private Properties load(String language) throws IOException {
		Properties properties = new Properties();
		String path = "_i18n/LocalStrings_" + language + ".properties";
		try (InputStream in = ColorServiceImpl.class.getResourceAsStream(path)) {
			assertThat(in).as(path).isNotNull();
			properties.load(in);
		}
		return properties;
	}
}

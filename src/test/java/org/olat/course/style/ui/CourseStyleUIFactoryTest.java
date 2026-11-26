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
package org.olat.course.style.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.commons.services.color.ColorSet;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.translator.Translator;
import org.olat.course.style.ColorCategory;

/**
 * Checks {@link CourseStyleUIFactory#getSwatchKey(String, java.util.Collection)}:
 * the stored CSS class of a colour category and the colour the chooser of
 * the administration preselects.
 *
 * Initial date: 24 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 *
 */
public class CourseStyleUIFactoryTest {

	/** The keys of the chooser: the set M with two theme colours */
	private static final List<String> KEYS = ColorSet.M.getKeysWithThemeColors(List.of("theme1", "theme2"));

	@Test
	public void swatchClassOfEveryHue() {
		for (String hue : ColorService.HUES) {
			assertThat(CourseStyleUIFactory.getSwatchKey("o_swatch_" + hue, KEYS)).isEqualTo(hue);
		}
	}

	@Test
	public void swatchClassOfThemeColor() {
		assertThat(CourseStyleUIFactory.getSwatchKey("o_swatch_theme2", KEYS)).isEqualTo("theme2");
	}

	@Test
	public void swatchClassTrimmed() {
		assertThat(CourseStyleUIFactory.getSwatchKey(" o_swatch_red ", KEYS)).isEqualTo("red");
	}

	@Test
	public void themeColorNotInChooser() {
		assertThat(CourseStyleUIFactory.getSwatchKey("o_swatch_theme3", KEYS)).isNull();
	}

	@Test
	public void swatchWithVariant() {
		assertThat(CourseStyleUIFactory.getSwatchKey("o_swatch_blue o_swatch_light", KEYS)).isNull();
		assertThat(CourseStyleUIFactory.getSwatchKey("o_swatch_blue-light", KEYS)).isNull();
		assertThat(CourseStyleUIFactory.getSwatchKey("o_swatch_blue-solid", KEYS)).isNull();
	}

	@Test
	public void predefinedCategoryClass() {
		// Classes of the predefined categories (upgrade to 21.2): the colour is preselected
		Map<String,String> expected = Map.of("o_swatch_blue", "blue", "o_swatch_cyan", "cyan", "o_swatch_magenta", "magenta",
				"o_swatch_red", "red", "o_swatch_orange", "orange", "o_swatch_yellow", "yellow", "o_swatch_lime", "lime",
				"o_swatch_green", "green");
		expected.forEach((cssClass, key) -> assertThat(CourseStyleUIFactory.getSwatchKey(cssClass, KEYS)).isEqualTo(key));
	}

	@Test
	public void oldPredefinedCategoryClass() {
		// Classes of the predefined categories before the upgrade: no colour is preselected, the class is kept
		for (String cssClass : List.of("o_colcat_dark_blue", "o_colcat_light_green")) {
			assertThat(CourseStyleUIFactory.getSwatchKey(cssClass, KEYS)).isNull();
		}
	}

	@Test
	public void freeClass() {
		assertThat(CourseStyleUIFactory.getSwatchKey("my_category", KEYS)).isNull();
		assertThat(CourseStyleUIFactory.getSwatchKey("blue", KEYS)).isNull();
		assertThat(CourseStyleUIFactory.getSwatchKey("o_swatch_", KEYS)).isNull();
		assertThat(CourseStyleUIFactory.getSwatchKey("o_swatch_unknown", KEYS)).isNull();
	}

	@Test
	public void noClass() {
		assertThat(CourseStyleUIFactory.getSwatchKey(null, KEYS)).isNull();
		assertThat(CourseStyleUIFactory.getSwatchKey("", KEYS)).isNull();
		assertThat(CourseStyleUIFactory.getSwatchKey("o_swatch_blue", null)).isNull();
		assertThat(CourseStyleUIFactory.getSwatchKey("o_swatch_blue", List.of())).isNull();
	}

	@Test
	public void pickerColorOfValidClass() {
		ColorPickerElement.Color color = CourseStyleUIFactory.createColor(category("my.cat", " o_colcat_bg  o_swatch_blue "), translator());
		assertThat(color.id()).isEqualTo("my.cat");
		assertThat(color.cssClass()).isEqualTo("o_colcat_bg  o_swatch_blue");
	}

	@Test
	public void pickerColorOfHostileClass() {
		// a class an administrator entered as text before the chooser renders as no colour
		for (String cssClass : List.of("\"><script>alert(1)</script>", "blue onmouseover=alert(1)",
				"blue;background:url(x)", "o_cal_blue\" x=\"", "o_cal_blue' x='", "a\nb", "x".repeat(10_000))) {
			ColorPickerElement.Color color = CourseStyleUIFactory.createColor(category("my.cat", cssClass), translator());
			assertThat(color.cssClass()).as(cssClass).isNull();
		}
		assertThat(CourseStyleUIFactory.createColor(category("my.cat", null), translator()).cssClass()).isNull();
		assertThat(CourseStyleUIFactory.createColor(category("my.cat", ""), translator()).cssClass()).isNull();
	}

	private static ColorCategory category(String identifier, String cssClass) {
		ColorCategory category = mock(ColorCategory.class);
		when(category.getIdentifier()).thenReturn(identifier);
		when(category.getCssClass()).thenReturn(cssClass);
		when(category.isEnabled()).thenReturn(true);
		return category;
	}

	private static Translator translator() {
		Translator translator = mock(Translator.class);
		when(translator.translate(any(String.class), any(), any())).thenReturn("Name");
		return translator;
	}
}

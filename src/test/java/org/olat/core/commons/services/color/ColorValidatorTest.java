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

import java.util.List;

import org.junit.Test;

/**
 * Checks the patterns of {@link ColorValidator} against valid keys and
 * classes and against junk, and the value in the log.
 *
 * Initial date: 25 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class ColorValidatorTest {

	/** Values that must never pass as key, class or class list */
	static final List<String> JUNK = List.of(
			"\"><script>alert(1)</script>",
			"blue onmouseover=alert(1)",
			"blue;background:url(x)",
			"o_cal_blue\" x=\"",
			"o_cal_blue' x='",
			"blue\nred",
			"blue\r\nX-INJECTED: 1",
			"bl\u0000ue",
			"bl​ue",
			"blüe",
			"红",
			"blue\\",
			"blue/red",
			"blue:red",
			"blue(1)",
			"blue&amp;",
			"x".repeat(10_000),
			"1blue",
			"-blue");

	@Test
	public void validKeys() {
		for (String key : List.of("blue", "lightblue", "blue-light", "theme2-dark", "theme12", "light_blue",
				"light.blue", "rebeccapurple", "a", "o9")) {
			assertThat(ColorValidator.isValidKey(key)).as(key).isTrue();
		}
		for (String key : ColorService.CALENDAR_COLORS) {
			assertThat(ColorValidator.isValidKey(key)).as(key).isTrue();
		}
		for (String key : ColorService.HUES) {
			assertThat(ColorValidator.isValidKey(key)).as(key).isTrue();
		}
		for (String key : ColorServiceImpl.COLOR_SERVICE_ALIASES.keySet()) {
			assertThat(ColorValidator.isValidKey(key)).as(key).isTrue();
		}
		for (ColorSet colorSet : ColorSet.values()) {
			for (String key : colorSet.getKeys(List.of("red", "theme1"))) {
				assertThat(ColorValidator.isValidKey(key)).as(key).isTrue();
			}
		}
	}

	@Test
	public void invalidKeys() {
		for (String key : List.of("Blue", "blue-", "blue--light", "-blue", "1blue", "blue light", "", " blue",
				"o_color_blue_", "x".repeat(ColorValidator.MAX_KEY_LENGTH + 1))) {
			assertThat(ColorValidator.isValidKey(key)).as(key).isFalse();
		}
		assertThat(ColorValidator.isValidKey(null)).isFalse();
		for (String junk : JUNK) {
			assertThat(ColorValidator.isValidKey(junk)).as(junk).isFalse();
		}
		assertThat(ColorValidator.validKey("blue", "test")).isEqualTo("blue");
		assertThat(ColorValidator.validKey("blue onmouseover=alert(1)", "test")).isNull();
	}

	@Test
	public void validCssClasses() {
		for (String cssClass : List.of("o_color_lightblue", "o_cal_blue", "o_swatch_theme1", "o_colcat_dark_blue",
				"O_CAL_Blue", "_x", "a-b_c")) {
			assertThat(ColorValidator.isValidCssClass(cssClass)).as(cssClass).isTrue();
			assertThat(ColorValidator.isValidCssClasses(cssClass)).as(cssClass).isTrue();
		}
		assertThat(ColorValidator.isValidCssClasses("o_swatch_blue o_swatch_light")).isTrue();
		assertThat(ColorValidator.isValidCssClasses("  o_colcat_bg  o_colcat_red\t")).isTrue();
		assertThat(ColorValidator.validCssClasses("  o_colcat_bg  o_colcat_red ", "test")).isEqualTo("o_colcat_bg  o_colcat_red");
	}

	@Test
	public void invalidCssClasses() {
		for (String cssClass : List.of("light.blue", "o_color_light.blue", "1x", "-x", "", "x".repeat(ColorValidator.MAX_CSS_CLASS_LENGTH + 1))) {
			assertThat(ColorValidator.isValidCssClass(cssClass)).as(cssClass).isFalse();
		}
		assertThat(ColorValidator.isValidCssClass(null)).isFalse();
		assertThat(ColorValidator.isValidCssClass("o_swatch_blue o_swatch_light")).isFalse();
		assertThat(ColorValidator.isValidCssClasses(null)).isFalse();
		assertThat(ColorValidator.isValidCssClasses("   ")).isFalse();
		assertThat(ColorValidator.isValidCssClasses("a b c d e f g h i")).isFalse();
		for (String junk : JUNK) {
			assertThat(ColorValidator.isValidCssClass(junk)).as(junk).isFalse();
			assertThat(ColorValidator.isValidCssClasses(junk)).as(junk).isFalse();
			assertThat(ColorValidator.isValidColorValue(junk)).as(junk).isFalse();
			assertThat(ColorValidator.validCssClasses(junk, "test")).as(junk).isNull();
		}
	}

	@Test
	public void colorValues() {
		assertThat(ColorValidator.isValidColorValue("light.blue")).isTrue();
		assertThat(ColorValidator.isValidColorValue("o_color_lightblue")).isTrue();
		assertThat(ColorValidator.isValidColorValue("O_CAL_Blue")).isTrue();
		assertThat(ColorValidator.isValidColorValue(null)).isFalse();
		assertThat(ColorValidator.isValidColorValue("")).isFalse();
		assertThat(ColorValidator.isValidColorValue("o_swatch_blue o_swatch_light")).isFalse();
	}

	@Test
	public void logValue() {
		assertThat(ColorValidator.toLogValue(null)).isEqualTo("null");
		assertThat(ColorValidator.toLogValue("blue")).isEqualTo("blue");
		// no line breaks, no control characters: one log line
		assertThat(ColorValidator.toLogValue("blue\r\nWARN fake")).isEqualTo("blue\\u000d\\u000aWARN fake");
		assertThat(ColorValidator.toLogValue("a\u0000b\u001Bc d​e")).isEqualTo("a\\u0000b\\u001bc\\u2028d\\u200be");
		assertThat(ColorValidator.toLogValue("\"x\\")).isEqualTo("\\\"x\\\\");
		// letters of other scripts stay readable
		assertThat(ColorValidator.toLogValue("grün 红")).isEqualTo("grün 红");
		// long values are cut
		String longValue = ColorValidator.toLogValue("x".repeat(10_000));
		assertThat(longValue).hasSize(ColorValidator.MAX_LOG_LENGTH + 3).endsWith("...");
		for (String junk : JUNK) {
			assertThat(ColorValidator.toLogValue(junk)).as(junk).doesNotContain("\n", "\r", "\u0000");
		}
	}

	@Test
	public void logInvalidNeverThrows() {
		ColorValidator.resetLogged();
		ColorValidator.logInvalid(null, null);
		ColorValidator.logInvalid("test", "");
		ColorValidator.logInvalid(null, "junk\n");
		for (String junk : JUNK) {
			ColorValidator.logInvalid("test", junk);
			// a second time is not logged again
			ColorValidator.logInvalid("test", junk);
		}
		for (int i = 0; i < 2000; i++) {
			ColorValidator.logInvalid("test", "junk " + i);
		}
		ColorValidator.resetLogged();
	}
}

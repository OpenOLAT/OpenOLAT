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

/**
 * A swatch of the {@link Palette}: a hue (e.g. "blue") and a variant
 * ({@link #SOLID}, {@link #TINT}, {@link #LIGHT} or {@link #DARK}). The colour values of
 * the roles are resolved by the {@link Palette}.
 *
 * Initial date: 29 Oct 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public record Swatch(String hue, String variant) {

	public static final String SOLID = "solid";
	public static final String TINT = "tint";
	public static final String LIGHT = "light";
	public static final String DARK = "dark";

}

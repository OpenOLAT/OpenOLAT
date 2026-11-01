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
 * The source of a stored colour key. Some bare keys exist in more than
 * one family with a different colour (e.g. "orange" in the {@link ColorService}
 * and in the calendar), see {@link ColorService#resolve(String, ColorFamily)}.
 *
 * Initial date: 29 Oct 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public enum ColorFamily {

	/** Keys of the {@link ColorService} (color.list) and the badge colours gold, silver, bronze */
	COLOR_SERVICE,
	/** Keys of the calendar colours, e.g. "blue" */
	CALENDAR,
	/** Identifiers of the predefined course colour categories, e.g. "light.blue" */
	CATEGORY,
	/** Colours of the project avatars and the user initials, e.g. "light_blue" */
	AVATAR

}

/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.form.flexible.elements;

import org.olat.core.gui.components.form.flexible.FormItem;

/**
 * Initial date: 2023-03-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface ColorPickerElement extends FormItem {
	void setColor(String color);

	void setNonSelectedText(String text);

	void setAllowUnselect(boolean allowUnselect);

	Color getColor();

	/**
	 * A colour the user picked in the browser, not a colour set with
	 * {@link #setColor(String)}. A chooser that preselects the nearest colour
	 * for an old stored value keeps the stored value as long as this is false,
	 * see {@link org.olat.core.commons.services.color.ColorUIFactory#getValueToSave(String, ColorPickerElement)}.
	 *
	 * @return true if the user picked a colour (or "no colour") since the
	 * 		element was created or since {@link #resetColorPicked()}, also the
	 * 		colour that was preselected
	 */
	boolean isColorPicked();

	/**
	 * Forgets the colour the user picked, e.g. when the form shows another item.
	 */
	void resetColorPicked();

	void setResetButtonId(String resetButtonId);

	void setDomReplacementWrapperRequired(boolean required);

	/**
	 * @return The presentation of the colours, {@link Presentation#LIST} if
	 * 		not set
	 */
	Presentation getPresentation();

	/**
	 * Selects how the callout of the picker shows the colours. The entries,
	 * the value and the "no colour" option are the same in both presentations.
	 *
	 * @param presentation The presentation, null for {@link Presentation#LIST}
	 */
	void setPresentation(Presentation presentation);

	/**
	 * The presentation of the colours in the callout of the picker. The
	 * callout opens at window level below or above the button of the picker,
	 * so a dialog or a scroll container does not clip it.
	 */
	enum Presentation {
		/** One entry per line: the colour square and the name (default) */
		LIST,
		/**
		 * A grid of colour cells without names: one column per colour, one
		 * row per variant, light (top) to dark (bottom). The picker places an
		 * entry by its key, see {@link org.olat.core.commons.services.color.Swatch#fromKey(String)}:
		 * "blue-soft" goes to the column "blue" and the row "soft". An entry
		 * that is no swatch key gets its own column in the row "solid".
		 * The columns of a group follow after a gap with the group as heading,
		 * e.g. the theme colours. The name of an entry is the accessible name
		 * of its cell.
		 */
		GRID
	}

	/**
	 * An entry of the color picker. Entries with the same group follow each
	 * other: the picker draws a gap and the group as heading before them,
	 * e.g. the theme colours after the hues. An entry without a group renders
	 * as before.
	 *
	 * @param id
	 * @param translatedName
	 * @param cssClass
	 * @param group The translated heading of the group, or null for no group
	 */
	record Color(String id, String translatedName, String cssClass, String group) {

		public Color(String id, String translatedName, String cssClass) {
			this(id, translatedName, cssClass, null);
		}
	}
}

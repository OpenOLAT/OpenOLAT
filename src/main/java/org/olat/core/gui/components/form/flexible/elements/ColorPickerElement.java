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
	void setColor(String orchid);

	/**
	 * @return true if the color picker is configured to send an ONCLICK form event every time the user selects
	 * a color, false if the picker provides the color property in form submission handling. The default setting
	 * is false.
	 */
	boolean isAjaxOnlyMode();

	/**
	 * The color picker has two modes: the default form submission mode and the ajax-only mode. In both cases,
	 * the color picker provides a color property ({@link #getColor() getColor}) to retrieve the selected color.
	 * <br/>
	 * In the form submission mode, the picker provides the color property when handling the form submission.
	 * <br/>
	 * In ajax-only mode, the picker sends an ONCLICK form event to the server every time the user selects a color
	 * using the color picker.
	 *
	 * @param ajaxOnlyMode Set this to true if you want to switch to ajax-only mode. The default setting is
	 *                     form submission mode (ajaxOnlyMode = false).
	 */
	void setAjaxOnlyMode(boolean ajaxOnlyMode);

	Color getColor();

	class Color {
		private final String id;
		private final String text;

		public Color(String id, String text) {
			this.id = id;
			this.text = text;
		}

		public String getId() {
			return id;
		}

		public String getText() {
			return text;
		}
	}
}

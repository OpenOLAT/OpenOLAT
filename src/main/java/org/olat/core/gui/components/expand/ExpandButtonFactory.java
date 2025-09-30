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
package org.olat.core.gui.components.expand;

import org.olat.core.gui.components.form.flexible.FormItem;

/**
 * 
 * Initial date: Sep 3, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ExpandButtonFactory {
	
	public static FormExpandButton createLink(String name) {
		FormExpandButtonImpl button = new FormExpandButtonImpl(name);
		button.setCssClass("btn btn-link");
		button.setIconLeftExpandedCss("o_icon o_icon_close_togglebox");
		button.setIconLeftCollapsedCss("o_icon o_icon_open_togglebox");
		return button;
	}
	
	public static ExpandButton createSelectionDisplay(FormItem element) {
		ExpandButton button = new ExpandButton(element);
		button.setCssClass("btn btn-default o_selection_display o_button_printed");
		button.setIconRightExpandedCss("o_icon o_icon_caret");
		button.setIconRightCollapsedCss("o_icon o_icon_caret");
		button.setDisabledAsText(true);
		return button;
	}

}

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
package org.olat.course.assessment.ui.tool;

import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 3 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserVisibilityCellRenderer extends LabelCellRenderer {
	
	private final boolean showText;
	
	public UserVisibilityCellRenderer(boolean showText) {
		this.showText = showText;
	}
	
	public String render(Boolean userVisibility, Translator translator) {
		StringOutput target = new StringOutput();
		render(target, translator, userVisibility);
		return target.toString();
	}

	@Override
	protected String getCellValue(Object val, Translator translator) {
		Boolean userVisibility = getUserVisibility(val);
		if (showText) {
			return userVisibility != null && userVisibility.booleanValue()
					? translator.translate("user.visibility.visible")
					: translator.translate("user.visibility.hidden");
		}
		return null;
	}

	@Override
	protected String getIconCssClass(Object val) {
		Boolean userVisibility = getUserVisibility(val);
		if (userVisibility == null) {
			return "o_icon_results_hidden o_no_user_visibility";
		} else if (userVisibility.booleanValue()) {
			return "o_icon_results_visible";
		}
		return "o_icon_results_hidden";
	}

	@Override
	protected String getElementCssClass(Object val) {
		Boolean userVisibility = getUserVisibility(val);
		if (userVisibility == null) {
			return "o_results_hidden o_no_user_visibility";
		} else if (userVisibility.booleanValue()) {
			return "o_results_visible";
		}
		return "o_results_hidden";
	}
	
	@Override
	protected String getTitle(Object val, Translator translator) {
		Boolean userVisibility = getUserVisibility(val);
		return userVisibility != null && userVisibility.booleanValue()
				? translator.translate("user.visibility.visible.tooltip")
				: translator.translate("user.visibility.hidden.tooltip");
	}

	@Override
	protected String getExportValue(Object val, Translator translator) {
		Boolean userVisibility = getUserVisibility(val);
		return userVisibility != null && userVisibility.booleanValue()
				? translator.translate("yes")
				: translator.translate("no");
	}
	
	protected Boolean getUserVisibility(Object val) {
		if (val instanceof Boolean) {
			return (Boolean) val;
		}
		return null;
	}
}
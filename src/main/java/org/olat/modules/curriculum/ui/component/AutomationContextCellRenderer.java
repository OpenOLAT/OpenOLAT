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
package org.olat.modules.curriculum.ui.component;

import org.olat.core.gui.components.table.IconCssCellRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.curriculum.AutomationContext;

/**
 * Initial date: 2026-06-30<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class AutomationContextCellRenderer extends IconCssCellRenderer {

	private final Translator translator;

	public AutomationContextCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	protected String getIconCssClass(Object val) {
		if (val instanceof AutomationContext ctx) {
			return "o_icon o_icon-fw " + switch (ctx) {
				case IMPLEMENTATION -> "o_icon_curriculum_implementations";
				case ELEMENT -> "o_icon_curriculum_element";
				case CONTENT -> "o_CourseModule_icon";
			};
		}
		return null;
	}

	@Override
	protected String getCellValue(Object val) {
		if (val instanceof AutomationContext ctx) {
			return translator.translate("automation.context." + ctx.name().toLowerCase());
		}
		return null;
	}

}

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
package org.olat.modules.todo.ui;

import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.todo.ToDoPriority;

/**
 *
 * Initial date: 03 Jul 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ToDoTaskPriorityRenderer extends LabelCellRenderer {

	private final Translator translator;
	private final boolean light;

	public ToDoTaskPriorityRenderer(Translator translator, boolean light) {
		this.translator = translator;
		this.light = light;
	}

	@Override
	protected boolean isLabelLight() {
		return light;
	}

	private ToDoPriority getPriority(Object val) {
		if (val instanceof ToDoTaskRow row) {
			return row.getPriority();
		}
		if (val instanceof ToDoPriority priority) {
			return priority;
		}
		return null;
	}

	@Override
	protected String getCellValue(Object val, Translator t) {
		return ToDoUIFactory.getDisplayName(translator, getPriority(val));
	}

	@Override
	protected String getIconCssClass(Object val) {
		ToDoPriority priority = getPriority(val);
		if (priority == null) {
			return null;
		}
		return "o_icon-fw " + ToDoUIFactory.getIconCss(priority);
	}

	@Override
	protected String getElementCssClass(Object val) {
		return ToDoUIFactory.getPriorityLabelCssClass(getPriority(val));
	}

	@Override
	protected String getTitle(Object val, Translator t) {
		return ToDoUIFactory.getDisplayName(translator, getPriority(val));
	}

}

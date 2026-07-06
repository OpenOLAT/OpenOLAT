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
import org.olat.core.util.StringHelper;
import org.olat.modules.todo.ToDoStatus;

/**
 *
 * Initial date: 03 Jul 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ToDoTaskStatusRenderer extends LabelCellRenderer {

	private final Translator translator;
	private final boolean light;

	public ToDoTaskStatusRenderer(Translator translator, boolean light) {
		this.translator = translator;
		this.light = light;
	}

	@Override
	protected boolean isLabelLight() {
		return light;
	}

	private ToDoStatus getStatus(Object val) {
		if (val instanceof ToDoTaskRow row) {
			return row.getStatus();
		}
		if (val instanceof ToDoStatus status) {
			return status;
		}
		return null;
	}

	@Override
	protected String getCellValue(Object val, Translator t) {
		ToDoStatus status = getStatus(val);
		if (status == null) {
			return null;
		}
		if (val instanceof ToDoTaskRow row && StringHelper.containsNonWhitespace(row.getStatusText())) {
			return row.getStatusText();
		}
		return ToDoUIFactory.getDisplayName(translator, status);
	}

	@Override
	protected String getIconCssClass(Object val) {
		ToDoStatus status = getStatus(val);
		if (status == null) {
			return null;
		}
		return "o_icon-fw " + ToDoUIFactory.getIconCss(status);
	}

	@Override
	protected String getElementCssClass(Object val) {
		return ToDoUIFactory.getStatusLabelCssClass(getStatus(val));
	}

}

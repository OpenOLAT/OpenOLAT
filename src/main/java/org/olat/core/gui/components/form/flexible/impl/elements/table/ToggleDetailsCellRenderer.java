/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 14 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ToggleDetailsCellRenderer implements FlexiCellRenderer {
	
	private final FlexiCellRenderer detailsRenderer;
	private final FlexiCellRenderer staticRenderer;
	private final FlexiTableComponentDelegate tableDelegate;
	
	public ToggleDetailsCellRenderer(FlexiTableComponentDelegate tableDelegate) {
		detailsRenderer = new StaticFlexiCellRenderer("tt-details", new TextFlexiCellRenderer());
		staticRenderer = new TextFlexiCellRenderer();
		this.tableDelegate = tableDelegate;
	}
	
	@Override
	public void render(Renderer renderer, StringOutput target,
			Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		Object rowObject = source.getFormItem().getTableDataModel().getObject(row);
		if(tableDelegate.isDetailsRow(row, rowObject)) {
			detailsRenderer.render(renderer, target, cellValue, row, source, ubu, translator);
		} else {
			staticRenderer.render(renderer, target, cellValue, row, source, ubu, translator);
		}
	}
}

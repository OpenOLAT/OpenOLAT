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
package org.olat.modules.selectus.ui.components;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.application.ParallelApplication;
import org.olat.modules.selectus.ui.model.ApplicationRow;

/**
 * 
 * Initial date: 25 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParallelApplicationsCellRenderer implements FlexiCellRenderer {
	
	private final Locale locale;
	
	public ParallelApplicationsCellRenderer(Locale locale) {
		this.locale = locale;
	}
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue != null) {
			ApplicationRow appRow = (ApplicationRow)source.getFormItem().getTableDataModel().getObject(row);
			List<ParallelApplication> apps = appRow.getParallelApplications();
			for(int i=0; i<apps.size(); i++) {
				if(i > 0) {
					target.append(", ");
				}
				PositionLight position = apps.get(i).getPosition();
				if(StringHelper.containsNonWhitespace(position.getPlaningsNumber())) {
					target.append(position.getPlaningsNumber()).append(": ");
				}
				String title = position.getMLTitle(locale);
				if(StringHelper.containsNonWhitespace(title)) {
					target.append(title);
				}
			}
		}
	}
}

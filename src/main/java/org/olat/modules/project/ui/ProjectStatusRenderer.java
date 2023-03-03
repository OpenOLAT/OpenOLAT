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
package org.olat.modules.project.ui;

import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 1 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjectStatusRenderer extends LabelCellRenderer {
	
	@Override
	protected String getCellValue(Object val, Translator translator) {
		if (val instanceof ProjProjectRow) {
			ProjProjectRow row = (ProjProjectRow)val;
			return row.getTranslatedStatus();
		}
		return null;
	}
	
	@Override
	protected String getIconCssClass(Object val) {
		if (val instanceof ProjProjectRow) {
			ProjProjectRow row = (ProjProjectRow)val;
			return ProjectUIFactory.getStatusIconCss(row.getStatus());
		}
		return null;
	}
	
	@Override
	protected String getElementCssClass(Object val) {
		if (val instanceof ProjProjectRow) {
			ProjProjectRow row = (ProjProjectRow)val;
			return "o_proj_project_status_" + row.getStatus().name();
		}
		return null;
	}

}

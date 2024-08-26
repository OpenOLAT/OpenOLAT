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
package org.olat.modules.project.ui;

import org.olat.admin.user.projects.ProjectsOverviewRow;
import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 1 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ProjectStatusRenderer extends LabelCellRenderer {
	
	@Override
	protected String getCellValue(Object val, Translator translator) {
		if (val instanceof ProjProjectRow row) {
			return row.getTranslatedStatus();
		} else if (val instanceof ProjectsOverviewRow row) {
			return row.getTranslatedStatus();
		}
		return null;
	}
	
	@Override
	protected String getIconCssClass(Object val) {
		if (val instanceof ProjProjectRow row) {
			return ProjectUIFactory.getStatusIconCss(row.getStatus());
		} else if (val instanceof ProjectsOverviewRow row) {
			return ProjectUIFactory.getStatusIconCss(row.getStatus());
		}
		return null;
	}
	
	@Override
	protected String getElementCssClass(Object val) {
		if (val instanceof ProjProjectRow row) {
			return "o_proj_project_status_" + row.getStatus().name();
		} else if (val instanceof ProjectsOverviewRow row) {
			return "o_proj_project_status_" + row.getStatus().name();
		}
		return null;
	}

}

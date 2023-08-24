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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 14 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjProjectTemplatesController extends ProjProjectMyController {

	public ProjProjectTemplatesController(UserRequest ureq, WindowControl wControl,
			BreadcrumbedStackedPanel stackPanel) {
		super(ureq, wControl, stackPanel);
	}
	
	@Override
	protected FlexiFiltersTab getInitialTab() {
		return tabAll;
	}

	@Override
	protected String getTitleI18n() {
		return "segment.templates";
	}

	@Override
	protected boolean isCreateProjectEnabled() {
		return false;
	}

	@Override
	protected boolean isCreateTemplateEnabled() {
		return true;
	}
	
	@Override
	protected boolean isColumnCreateFromTemplateEnabled() {
		return true;
	}

	@Override
	protected boolean isTabActivityEnabled() {
		return false;
	}

	@Override
	protected boolean isTabsTemplateAccessEnabled() {
		return true;
	}
	
	@Override
	protected boolean isFilterTemplateAccessEnabled() {
		return true;
	}
	
	@Override
	protected Boolean getSearchTemplates() {
		return Boolean.TRUE;
	}

}

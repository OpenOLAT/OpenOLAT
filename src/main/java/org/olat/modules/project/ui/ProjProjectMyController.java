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
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.project.ProjProjectSearchParams;

/**
 * 
 * Initial date: 27 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjProjectMyController extends ProjProjectListController {

	public ProjProjectMyController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel) {
		super(ureq, wControl, stackPanel);
		
		initForm(ureq);
		loadModel(ureq);
	}
	
	@Override
	protected boolean isCreateFromTemplateEnabled() {
		return true;
	}

	@Override
	protected boolean isCreateForEnabled() {
		return false;
	}

	@Override
	protected boolean isBulkEnabled() {
		return false;
	}

	@Override
	protected boolean isToolsEnabled() {
		return false;
	}

	@Override
	protected boolean isColumnTypeEnabled() {
		return false;
	}
	
	@Override
	protected boolean isColumnCreateFromTemplateEnabled() {
		return false;
	}

	@Override
	protected boolean isCustomRendererEnabled() {
		return true;
	}

	@Override
	protected boolean isTabNoActivityEnabled() {
		return false;
	}

	@Override
	protected boolean isTabToDeleteEnabled() {
		return false;
	}

	@Override
	protected boolean isFilterOrphanEnabled() {
		return false;
	}

	@Override
	protected boolean isFilterMemberEnabled() {
		return false;
	}
	
	@Override
	protected ProjProjectSearchParams createSearchParams() {
		ProjProjectSearchParams searchParams = new ProjProjectSearchParams();
		searchParams.setIdentity(getIdentity());
		return searchParams;
	}

	@Override
	protected Boolean getSearchTemplates() {
		return Boolean.FALSE;
	}

}

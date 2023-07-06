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

import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.project.ProjProjectSearchParams;

/**
 * 
 * Initial date: 27 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjProjectAdminController extends ProjProjectListController {

	private final List<OrganisationRef> projectManagerOrganisations;

	public ProjProjectAdminController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel) {
		super(ureq, wControl, stackPanel);
		this.projectManagerOrganisations = ureq.getUserSession().getRoles()
				.getOrganisationsWithRoles(OrganisationRoles.projectmanager, OrganisationRoles.administrator);
		 
		initForm(ureq);
		loadModel(ureq);
	}

	@Override
	protected boolean isCreateProjectEnabled() {
		return true;
	}

	@Override
	protected boolean isCreateTemplateEnabled() {
		return false;
	}

	@Override
	protected boolean isCreateFromTemplateEnabled() {
		return false;
	}

	@Override
	protected boolean isCreateForEnabled() {
		return true;
	}

	@Override
	protected boolean isBulkEnabled() {
		return true;
	}

	@Override
	protected boolean isToolsEnabled() {
		return true;
	}

	@Override
	protected boolean isColumnTypeEnabled() {
		return true;
	}

	@Override
	protected boolean isColumnCreateFromTemplateEnabled() {
		return false;
	}

	@Override
	protected boolean isCustomRendererEnabled() {
		return false;
	}

	@Override
	protected boolean isTabNoActivityEnabled() {
		return true;
	}

	@Override
	protected boolean isTabToDeleteEnabled() {
		return true;
	}

	@Override
	protected boolean isFilterOrphanEnabled() {
		return true;
	}

	@Override
	protected boolean isFilterMemberEnabled() {
		return true;
	}
	
	@Override
	protected ProjProjectSearchParams createSearchParams() {
		ProjProjectSearchParams searchParams = new ProjProjectSearchParams();
		searchParams.setProjectOrganisations(projectManagerOrganisations);
		return searchParams;
	}

	@Override
	protected Boolean getSearchTemplates() {
		return null;
	}

}

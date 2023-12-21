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
package org.olat.modules.quality.generator.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.quality.ui.security.MainSecurityCallback;

/**
 * 
 * Initial date: 1 Dec 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PreviewListController extends AbstractPreviewListController {
	
	private final MainSecurityCallback secCallback;

	public PreviewListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			MainSecurityCallback secCallback) {
		super(ureq, wControl, stackPanel);
		this.secCallback = secCallback;
		
		initForm(ureq);
		initFilters();
		initFilterTabs(ureq);
		selectFilterTab(ureq, tabAll);
	}

	@Override
	protected List<OrganisationRef> getDataCollectionOrganisationRefs() {
		return secCallback.getViewDataCollectionOrganisationRefs();
	}

	@Override
	protected List<OrganisationRef> getLearnResourceManagerOrganisationRefs() {
		return secCallback.getLearnResourceManagerOrganisationRefs();
	}

	@Override
	protected List<OrganisationRef> getGeneratorOrganisationRefs() {
		return secCallback.getViewGeneratorOrganisationRefs();
	}

	@Override
	protected boolean isFilterGenerator() {
		return true;
	}

}

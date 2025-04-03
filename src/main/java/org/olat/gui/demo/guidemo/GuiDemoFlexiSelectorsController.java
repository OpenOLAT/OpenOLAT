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
package org.olat.gui.demo.guidemo;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.user.ui.organisation.element.OrgSelectorElement;

/**
 * Initial date: 2025-04-03<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GuiDemoFlexiSelectorsController extends FormBasicController {

	private OrgSelectorElement singleOrgList;

	public GuiDemoFlexiSelectorsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initOrgSelectorSection(formLayout);
	}

	private void initOrgSelectorSection(FormItemContainer formLayout) {
		FormLayoutContainer sectionContainer = uifactory.addDefaultFormLayout("orgSelectorSection", null, 
				formLayout);
		sectionContainer.setFormTitle(translate("selectors.org"));
		
		List<Organisation> orgs = getOrgList();
		singleOrgList = uifactory.addOrgSelectorElement("org.selector", "selectors.org.single", 
				sectionContainer, getWindowControl(), orgs);
		singleOrgList.setMultipleSelection(false);
	}
	
	private List<Organisation> getOrgList() {
		List<Organisation> orgList = new ArrayList<Organisation>();
		
		orgList.add(new OrgBuilder(1L).name("A").location("Processes").path("/1/").build());
		orgList.add(new OrgBuilder(2L).name("A.1").location("HR").path("/1/2/").build());
		orgList.add(new OrgBuilder(3L).name("A.2").location("Procurement").path("/1/3/").build());
		orgList.add(new OrgBuilder(4L).name("A.1.1").location("Salaries").path("/1/2/4/").build());
		orgList.add(new OrgBuilder(5L).name("B").location("Manufacturing").path("/5/").build());
		orgList.add(new OrgBuilder(6L).name("B.1").location("Assembly").path("/5/6/").build());
		orgList.add(new OrgBuilder(7L).name("C").location("Marketing").path("/7/").build());
		orgList.add(new OrgBuilder(8L).name("C.1").location("Online marketing").path("/7/8/").build());
		
		return orgList;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	class OrgBuilder {
		OrganisationImpl org;
		
		public OrgBuilder(Long key) {
			org = new OrganisationImpl();
			org.setKey(key);
		}
		
		public OrgBuilder name(String name) {
			org.setDisplayName(name);
			return this;
		}
		
		public OrgBuilder path(String path) {
			org.setMaterializedPathKeys(path);
			return this;
		}
		
		public OrgBuilder location(String location) {
			org.setLocation(location);
			return this;
		}
		
		public Organisation build() {
			return org;
		}
	}
}

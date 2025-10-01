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

import java.util.List;

import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectDisplayValues;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectListSource;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectOption;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectOption.ObjectOptionValues;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;

/**
 * Initial date: 2025-04-03<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GuiDemoFlexiSelectorsController extends FormBasicController {

	public GuiDemoFlexiSelectorsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initOrgSelectorSection(formLayout);
	}

	private void initOrgSelectorSection(FormItemContainer formLayout) {
		FormLayoutContainer sectionContainer = uifactory.addDefaultFormLayout("selectorSection", null, formLayout);
		sectionContainer.setFormTitle(translate("guidemo_flexi_form_selectors"));
		
		ObjectListSource organisationSource = createOrganisationSource();
		uifactory.addObjectSelectionElement("objects.single", "selection.objects.single", sectionContainer,
				getWindowControl(), false, organisationSource);
		
		uifactory.addObjectSelectionElement("objects.multi", "selection.objects.multi", sectionContainer,
				getWindowControl(), true, organisationSource);
		
		uifactory.addObjectSelectionElement("objects.multi.load.more", "selection.objects.load.more", sectionContainer,
				getWindowControl(), true, createNamesSource(false));
		
		ObjectSelectionElement objectSelectionDisabledEl = uifactory.addObjectSelectionElement("objects.disabled",
				"selection.objects.disabled", sectionContainer, getWindowControl(), true, createNamesSource(true));
		objectSelectionDisabledEl.setEnabled(false);
	}
	
	private ObjectListSource createOrganisationSource() {
		return new ObjectListSource(List.of(
				new ObjectOptionValues("", "Head", "/", null),
				new ObjectOptionValues("1", "Processes", "/1/", null),
				new ObjectOptionValues("2", "HR", "/1/2/", null),
				new ObjectOptionValues("4", "Salaries", "/1/2/4/", null),
				new ObjectOptionValues("3", "Procurement", "/1/3/", null),
				new ObjectOptionValues("5", "Manufacturing", "/5/", null),
				new ObjectOptionValues("6", "Assembly", "/5/6/", null),
				new ObjectOptionValues("6a", "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore", "/Lorem/.../sed/", "/Lorem/ipsum/dolor/sit/amet,/consetetur/sadipscing/elitr,/sed/diam/nonumy/eirmod/tempor/invidunt/ut/labore/et/dolore/magna/aliquyam/erat,/sed/diam/voluptua/"),
				new ObjectOptionValues("7", "Marketing", "/7/", null),
				new ObjectOptionValues("8", "Online marketing", "/7/8", null),
				new ObjectOptionValues("9", "Offline marketing", "/7/9", null),
				new ObjectOptionValues("10", "Word of mouth", "/7/10", null),
				new ObjectOptionValues("11", "Guerilla marketing", "/7/11", null),
				new ObjectOptionValues("12", "Loyalty programmes", "/7/12", null)
			));
	}
	
	private ObjectListSource createNamesSource(boolean initDefaultSelection) {
		ObjectDisplayValues defaultDisplayValue = null;
		if (initDefaultSelection) {
			String title = "";
			title += NameSource.ALL.get(2) + ", ";
			title += NameSource.ALL.get(20) + ", ";
			title += NameSource.ALL.get(200);
			defaultDisplayValue = new ObjectDisplayValues(title, title);
		}
		
		List<ObjectOption> options = NameSource.ALL.stream()
				.map(name -> (ObjectOption)new ObjectOptionValues(name, name, null, null))
				.toList();
		return new ObjectListSource(
				defaultDisplayValue,
				options,
				translate("selection.objects.load.more.options"));
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

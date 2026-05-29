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
import java.util.Date;
import java.util.List;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.model.TagInfoImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectListSource;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectOption.ObjectOptionValues;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.user.IdentitySelectionSource;
import org.olat.user.UserManager;

/**
 * Initial date: 2025-04-03<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GuiDemoFlexiSelectorsController extends FormBasicController {

	public GuiDemoFlexiSelectorsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initObjectSection(formLayout);
		initTagSection(formLayout);
	}

	private void initObjectSection(FormItemContainer formLayout) {
		FormLayoutContainer sectionContainer = uifactory.addDefaultFormLayout("objectSection", null, formLayout);
		sectionContainer.setFormTitle(translate("selection.objects"));
		
		ObjectListSource organisationSource = createOrganisationSource();
		uifactory.addObjectSelectionElement("objects.single", "selection.objects.single", sectionContainer,
				getWindowControl(), false, organisationSource);
		
		uifactory.addObjectSelectionElement("objects.multi", "selection.objects.multi", sectionContainer,
				getWindowControl(), true, organisationSource);
		
		uifactory.addObjectSelectionElement("objects.multi.load.more", "selection.objects.load.more", sectionContainer,
				getWindowControl(), true, createNamesSource(false));

		uifactory.addObjectSelectionElement("objects.browser", "selection.objects.browser", sectionContainer,
				getWindowControl(), true, createBrowserNamesSource());

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
	
	private IdentitySelectionSource createNamesSource(boolean initDefaultSelection) {
		List<Identity> identities = createDemoIdentities();
		List<Identity> selected = initDefaultSelection
				? List.of(identities.get(2), identities.get(20), identities.get(200))
				: List.of(getIdentity());
		return new IdentitySelectionSource(getLocale(), selected, () -> identities);
	}

	private IdentitySelectionSource createBrowserNamesSource() {
		List<Identity> identities = createDemoIdentities();
		List<Identity> selected = List.of(identities.get(0), identities.get(1));
		return new IdentitySelectionSource(getLocale(), selected, () -> identities,
				multi -> (u, w) -> new GuiDemoObjectSelectionBrowserController(u, w, identities));
	}

	private List<Identity> createDemoIdentities() {
		int cssCount = UserManager.USER_INITIALS_CSS.size();
		List<Identity> identities = new ArrayList<>(NameSource.ALL.size() + 1);
		for (int i = 0; i < NameSource.ALL.size(); i++) {
			String[] tokens = NameSource.ALL.get(i).split(" ", 2);
			TransientIdentity identity = new TransientIdentity();
			identity.setKey(-(long) (i + 1));
			identity.setProperty(UserConstants.FIRSTNAME, tokens[0]);
			identity.setProperty(UserConstants.LASTNAME, tokens[1]);
			identity.setInitialsCssClass(UserManager.USER_INITIALS_CSS.get(i % cssCount));
			identities.add(identity);
		}
		return identities;
	}

	private void initTagSection(FormItemContainer formLayout) {
		FormLayoutContainer sectionContainer = uifactory.addDefaultFormLayout("selctionTags", null, formLayout);
		sectionContainer.setFormTitle(translate("selection.tags"));
		
		List<TagInfo> allTags = new ArrayList<>();
		TagInfoImpl tag1 = new TagInfoImpl(1l, new Date(), translate("select.1"), 3l, true);
		allTags.add(tag1);
		allTags.add(new TagInfoImpl(2l, new Date(), translate("select.2"), 2l, false));
		allTags.add(new TagInfoImpl(3l, new Date(), translate("select.3"), 0l, false));
		allTags.add(new TagInfoImpl(4l, new Date(), translate("select.4"), 311l, false));
		allTags.add(new TagInfoImpl(5l, new Date(), translate("select.5"), 0l, false));
		allTags.add(new TagInfoImpl(6l, new Date(), translate("select.6"), 3l, false));
		TagInfoImpl tag7 = new TagInfoImpl(7l, new Date(), translate("select.7"), 30l, true);
		allTags.add(tag7);
		allTags.add(new TagInfoImpl(8l, new Date(), translate("select.8"), 3l, false));
		uifactory.addTagSelection("tags", "tags", sectionContainer, getWindowControl(), allTags);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
}

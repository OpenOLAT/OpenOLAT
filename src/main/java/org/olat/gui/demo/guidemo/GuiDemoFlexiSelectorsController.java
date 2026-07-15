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
import org.olat.core.commons.services.tag.ui.component.TagSelection;
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
		
		ObjectSelectionElement objectSelectionDisabledNoSelectionEl = uifactory.addObjectSelectionElement(
				"selection.objects.disabled.no.selection", "selection.objects.disabled.no.selection", sectionContainer,
				getWindowControl(), true, new ObjectListSource(List.of()));
		objectSelectionDisabledNoSelectionEl.setEnabled(false);

		ObjectSelectionElement objectSelectionDisabledEl = uifactory.addObjectSelectionElement("selection.objects.disabled",
				"selection.objects.disabled", sectionContainer, getWindowControl(), true, createNamesSource(true));
		objectSelectionDisabledEl.setEnabled(false);
		
		ObjectSelectionElement objectSelectionDisabledBrowserEl = uifactory.addObjectSelectionElement(
				"selection.objects.disabled.browser", "selection.objects.disabled.browser", sectionContainer,
				getWindowControl(), true, createBrowserNamesSource());
		objectSelectionDisabledBrowserEl.setEnabled(false);
	}
	
	private ObjectListSource createOrganisationSource() {
		return new ObjectListSource(List.of(
				new ObjectOptionValues("", "Lakeside International School", "International Schools Association / European Region / Switzerland Chapter /"),
				new ObjectOptionValues("1", "Academics", "Lakeside International School / Academic Affairs and Curriculum Office /"),
				new ObjectOptionValues("2", "Sciences", "Lakeside International School / Academic Affairs and Curriculum Office / Faculty of Natural Sciences and Laboratory Studies /"),
				new ObjectOptionValues("4", "Chemistry", "Lakeside International School / Academic Affairs and Curriculum Office / Faculty of Natural Sciences and Laboratory Studies / Department of Chemistry and Materials Science /"),
				new ObjectOptionValues("3", "Languages", "Lakeside International School / Academic Affairs and Curriculum Office / Faculty of Modern Languages and Cultural Studies /"),
				new ObjectOptionValues("5", "Sports", "Sports /"),
				new ObjectOptionValues("6", "Football", "Sports / Physical Education and Extracurricular Athletics Programme / Team Sports and Competitive Leagues /"),
				new ObjectOptionValues("6a", "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore", "Lorem / ipsum / dolor / sit / amet, / consetetur / sadipscing / elitr, / sed / diam / nonumy / eirmod / tempor / invidunt / ut / labore / et / dolore / magna / aliquyam / erat, / sed / diam / voluptua /"),
				new ObjectOptionValues("7", "Administration", "School Administration and Operations Office /"),
				new ObjectOptionValues("8", "Admissions", "School Administration and Operations Office / Student Admissions and Enrolment Services / Applications, Interviews and Onboarding /"),
				new ObjectOptionValues("9", "Library", "School Administration and Operations Office / Library, Archives and Learning Resources Centre /"),
				new ObjectOptionValues("10", "Cafeteria", "Cafeteria /"),
				new ObjectOptionValues("11", "Counselling", "School Administration and Operations Office / Student Wellbeing, Counselling and Mental Health Services /"),
				new ObjectOptionValues("12", "Alumni", "School Administration and Operations Office / Alumni Relations and Lifelong Learning Network /")
			));
	}
	
	private IdentitySelectionSource createNamesSource(boolean initDefaultSelection) {
		List<Identity> identities = createDemoIdentities();
		List<Identity> selected = initDefaultSelection
				? List.of(identities.get(2), identities.get(20), identities.get(200))
				: List.of(getIdentity());
		return new IdentitySelectionSource(getLocale(), selected, () -> identities, getIdentity());
	}

	private IdentitySelectionSource createBrowserNamesSource() {
		List<Identity> identities = createDemoIdentities();
		List<Identity> selected = List.of(identities.get(0), identities.get(1));
		return new IdentitySelectionSource(getLocale(), selected, () -> identities,
				multi -> (u, w) -> new GuiDemoObjectSelectionBrowserController(u, w, identities), getIdentity());
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
		
		TagSelection tagDisabledEl = uifactory.addTagSelection("selection.tags.disabled", "selection.tags.disabled",
				sectionContainer, getWindowControl(), allTags);
		tagDisabledEl.setEnabled(false);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
}

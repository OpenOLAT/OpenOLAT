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
package org.olat.modules.curriculum.ui.member;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.event.EditMemberEvent;
import org.olat.user.UserInfoProfile;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberDetailsController extends FormBasicController {

	private FormLink editMemberShipButton;
	
	private final Identity member;
	private final boolean withEdit;
	private final boolean withAcceptDecline;
	private final UserInfoProfileConfig profileConfig;

	private MemberHistoryDetailsController historyDetailsCtrl;
	private final MemberRolesDetailsController rolesDetailsCtrl;
	
	@Autowired
	private UserInfoService userInfoService;
	
	public MemberDetailsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			Curriculum curriculum, CurriculumElement selectedCurriculumElement, List<CurriculumElement> elements,
			Identity identity, UserInfoProfileConfig profileConfig, CurriculumRoles alwaysVisibleRole,
			boolean withEdit, boolean withAcceptDecline, boolean withHistory) {
		super(ureq, wControl, LAYOUT_CUSTOM, "member_details_view", rootForm);
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, getLocale()));

		this.member = identity;
		this.withEdit = withEdit;
		this.profileConfig = profileConfig;
		this.withAcceptDecline = withAcceptDecline;

		rolesDetailsCtrl = new MemberRolesDetailsController(ureq, getWindowControl(), rootForm,
				curriculum, selectedCurriculumElement, elements, member, alwaysVisibleRole);
		listenTo(rolesDetailsCtrl);
		
		if(withHistory) {
			historyDetailsCtrl = new MemberHistoryDetailsController(ureq, getWindowControl(), rootForm,
					selectedCurriculumElement, member);
			listenTo(historyDetailsCtrl);
		}
		
		initForm(ureq);
	}
	
	public List<MemberRolesDetailsRow> getRolesDetailsRows() {
		return rolesDetailsCtrl.getRolesDetailsRows();
	}
	
	public void setModifications(List<MembershipModification> modifications) {
		rolesDetailsCtrl.setModifications(modifications);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			// Profile
			UserInfoProfile memberConfig = userInfoService.createProfile(member);
			MemberUserDetailsController profile = new MemberUserDetailsController(ureq, getWindowControl(), mainForm,
					member, profileConfig, memberConfig);
			listenTo(profile);
			layoutCont.put("profil", profile.getInitialComponent());
		}
		
		editMemberShipButton = uifactory.addFormLink("edit.member", formLayout, Link.BUTTON);
		editMemberShipButton.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		editMemberShipButton.setVisible(withEdit);
	
		formLayout.add("roles", rolesDetailsCtrl.getInitialFormItem());
		if(historyDetailsCtrl != null) {
			formLayout.add("history", historyDetailsCtrl.getInitialFormItem());
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(editMemberShipButton == source) {
			fireEvent(ureq, new EditMemberEvent(member));
		}
		super.formInnerEvent(ureq, source, event);
	}
}

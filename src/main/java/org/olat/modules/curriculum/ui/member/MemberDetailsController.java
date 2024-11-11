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

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
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
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailPackage;
import org.olat.group.ui.main.EditSingleMembershipController;
import org.olat.group.ui.main.MemberPermissionChangeEvent;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.user.UserAvatarMapper;
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
	private final Curriculum curriculum;
	private final CurriculumElement curriculumElement;
	private final UserInfoProfileConfig profileConfig;
	
	private CloseableModalController cmc;
	private EditSingleMembershipController editSingleMemberCtrl;
	private final MemberRolesDetailsController rolesDetailsCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private CurriculumService curriculumService;
	
	public MemberDetailsController(UserRequest ureq, WindowControl wControl,
			Curriculum curriculum,  CurriculumElement curriculumElement, List<CurriculumElement> elements, MemberRow row,
			UserAvatarMapper avatarMapper, String avatarMapperBaseURL, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "member_details_view", rootForm);
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, getLocale()));
		this.curriculum = curriculum;
		this.curriculumElement = curriculumElement;
		
		member = securityManager.loadIdentityByKey(row.getIdentityKey());
		profileConfig = userInfoService.createProfileConfig();
		profileConfig.setChatEnabled(true);
		profileConfig.setAvatarMapper(avatarMapper);
		profileConfig.setAvatarMapperBaseURL(avatarMapperBaseURL);
		
		rolesDetailsCtrl = new MemberRolesDetailsController(ureq, getWindowControl(), rootForm,
				curriculum,  elements, member);
		listenTo(rolesDetailsCtrl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			// Profile
			UserInfoProfile memberConfig = userInfoService.createProfile(member);
			MemberUserDetailsController profile = new MemberUserDetailsController(ureq, getWindowControl(), member, profileConfig, memberConfig);
			listenTo(profile);
			layoutCont.put("profil", profile.getInitialComponent());
		}
		
		editMemberShipButton = uifactory.addFormLink("edit.member", formLayout, Link.BUTTON);
		editMemberShipButton.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
	
		formLayout.add("roles", rolesDetailsCtrl.getInitialFormItem());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editSingleMemberCtrl == source) {
			if(event instanceof MemberPermissionChangeEvent e) {
				doChangePermission(ureq, e, false);
				rolesDetailsCtrl.loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editSingleMemberCtrl);
		removeAsListenerAndDispose(cmc);
		editSingleMemberCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(editMemberShipButton == source) {
			doEditMembership(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doEditMembership(UserRequest ureq) {
		if(guardModalController(editSingleMemberCtrl)) return;
		
		editSingleMemberCtrl = new EditSingleMembershipController(ureq, getWindowControl(), member, curriculum, curriculumElement, false, false);
		listenTo(editSingleMemberCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editSingleMemberCtrl.getInitialComponent(),
				true, translate("edit.member"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doChangePermission(UserRequest ureq, MemberPermissionChangeEvent e, boolean sendMail) {
		MailPackage mailing = new MailPackage(sendMail);
		Roles roles = ureq.getUserSession().getRoles();	
		curriculumService.updateCurriculumElementMemberships(getIdentity(), roles, e.getCurriculumChanges(), mailing);
		dbInstance.commitAndCloseSession();
	}
}

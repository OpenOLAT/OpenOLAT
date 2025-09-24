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
package org.olat.modules.certificationprogram.ui;

import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.user.PortraitUser;
import org.olat.user.UserInfoController;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramMemberPortraitController extends UserInfoController {

	private final Identity assessedIdentity;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public CertificationProgramMemberPortraitController(UserRequest ureq, WindowControl wControl, Form mainForm,
			Identity assessedIdentity, UserInfoProfileConfig profileConfig, PortraitUser portraitUser) {
		super(ureq, wControl, mainForm, profileConfig, portraitUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.assessedIdentity = assessedIdentity;
		initForm(ureq);
	}
	
	@Override
	protected void initFormItems(FormLayoutContainer itemsCont, Controller listener, UserRequest ureq) {
		super.initFormItems(itemsCont, listener, ureq);
		
		if(organisationModule.isEnabled()) {
			List<Organisation> userOrganisations = organisationService.getOrganisations(assessedIdentity, OrganisationRoles.user);
			List<String> userOrganisationsNamesList = userOrganisations
					.stream().map(org -> StringHelper.escapeHtml(org.getDisplayName()))
					.toList();
			String userOrganisationsNames = String.join(", ", userOrganisationsNamesList);
			uifactory.addStaticTextElement("member.home.base", "member.home.base", userOrganisationsNames, itemsCont);
		}
	}
}

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
package org.olat.modules.selectus.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  17 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ClosedApplicationMessageController extends FormBasicController {
	
	private Position position;
	private final OrganisationUnit organisationSettings;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public ClosedApplicationMessageController(UserRequest ureq, WindowControl wControl, Position position) {
		super(ureq, wControl, "app_closed");
		this.position = position;
		organisationSettings = recruitingService.getOrganisationUnit(position);
		initForm(ureq);
	}
	
	public void setPosition(Position position) {
		this.position = position;
		setI18nArguments(flc);

	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			setI18nArguments(layoutCont);
		}
	}
	
	private void setI18nArguments(FormLayoutContainer layoutCont) {
		String[] i18nArguments = new String[] {
			recruitingModule.getOfficeMail(),
			recruitingModule.getStaffMail(position, organisationSettings)
		};
		layoutCont.contextPut("i18nArguments", i18nArguments);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
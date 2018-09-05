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
package org.olat.modules.portfolio.ui.wizard;

import java.util.Collections;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.Binder;

/**
 * 
 * Initial date: 15.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddMember_3_ChoosePermissionStep extends BasicStep {

	private final Binder binder;
	private final Identity preselectedIdentity;
	
	public AddMember_3_ChoosePermissionStep(UserRequest ureq, Binder binder, Identity preselectedIdentity) {
		super(ureq);
		this.binder = binder;
		this.preselectedIdentity = preselectedIdentity;
		setNextStep(new AddMember_4_MailStep(ureq, binder));
		setI18nTitleAndDescr("add.permission.title", "add.permission.title");
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		boolean back = preselectedIdentity == null;
		return new PrevNextFinishConfig(back, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		if(preselectedIdentity != null) {
			AccessRightsContext rightsContext = (AccessRightsContext)runContext.get("rightsContext");
			if(rightsContext == null) {
				rightsContext = new AccessRightsContext();
			}
			rightsContext.setIdentities(Collections.singletonList(preselectedIdentity));
			runContext.put("rightsContext", rightsContext);
		}
		return new AccessRightsEditStepController(ureq, wControl, binder, form, runContext);
	}
}

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
package org.olat.modules.project.ui.wizard;

import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ui.ProjectUIFactory;


/**
 * Initial date: 1 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
public class AddMemberRolesStep extends BasicStep {

	public AddMemberRolesStep(UserRequest ureq) {
		super(ureq);
		setNextStep(NOSTEP);
		setTranslator(Util.createPackageTranslator(ProjectUIFactory.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("member.wizard.roles", "member.wizard.roles");
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, false, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		ProjectRolesContextImpl rolesContext = new ProjectRolesContextImpl();
		runContext.put("roles", rolesContext);
		return new AddMemberRolesController(ureq, wControl, form, runContext, rolesContext, true);
	}
	
	static class ProjectRolesContextImpl implements ProjectRolesContext {
		
		private Set<ProjectRole> roles;

		@Override
		public Set<ProjectRole> getProjectRoles() {
			return roles;
		}

		@Override
		public void setProjectRoles(Set<ProjectRole> roles) {
			this.roles = roles;
		}
		
	}
}

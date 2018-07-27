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
package org.olat.course.member.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.group.BusinessGroup;
import org.olat.modules.curriculum.Curriculum;
import org.olat.repository.RepositoryEntry;


/**
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImportMember_3_ChoosePermissionStep extends BasicStep {

	private final BusinessGroup group;
	private final RepositoryEntry repoEntry;
	private final Curriculum curriculum;
	private final boolean overrideManaged;
	
	public ImportMember_3_ChoosePermissionStep(UserRequest ureq, RepositoryEntry repoEntry, BusinessGroup group,
			Curriculum curriculum, boolean overrideManaged) {
		super(ureq);
		this.group = group;
		this.repoEntry = repoEntry;
		this.curriculum = curriculum;
		this.overrideManaged = overrideManaged;
		setNextStep(new ImportMember_4_MailStep(ureq, repoEntry));
		setI18nTitleAndDescr("import.permission.title", "import.permission.title");
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new ImportMemberPermissionChoiceController(ureq, wControl, repoEntry, group, curriculum, overrideManaged, form, runContext);
	}
}

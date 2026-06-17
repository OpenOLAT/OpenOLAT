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
package org.olat.modules.selectus.ui.app_wizard;

import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentOption;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Position;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  11 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DocumentsStep extends BasicStep {

	private final Position preselectedPosition;
	private final RecruitingModule recruitingModule;
	
	public DocumentsStep(UserRequest ureq, Position preselectedPosition) {
		super(ureq);
		this.preselectedPosition = preselectedPosition;
		recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		
		setI18nTitleAndDescr("wizard.documents.title", "wizard.documents.description");

		RefereesStep refereeStep = new RefereesStep(ureq, preselectedPosition);
		if(refereeStep.isEnabled()) {
			setNextStep(refereeStep);
		} else {
			setNextStep(refereeStep.nextStep());
		}
	}
	
	protected boolean isEnabled() {
		if(preselectedPosition == null) return true;

		//the loop make sure the type of documents exists
		Set<String> available = preselectedPosition.getAvailableDocuments();
		Set<String> mandatory = preselectedPosition.getMandatoryDocuments();
		for(DocumentOption docOption:recruitingModule.getDocumentOptions()) {
			DocumentEnum doc = docOption.getDoc();
			if(doc == DocumentEnum.combined) {
				continue;
			}
			
			if(!available.contains(doc.name()) && !mandatory.contains(doc.name())) {
				continue;
			}
			return true;
		}
		return false;
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		form.setMultipartEnabled(true);
		return new DocumentsStepController(ureq, wControl, runContext, form);
	}
}

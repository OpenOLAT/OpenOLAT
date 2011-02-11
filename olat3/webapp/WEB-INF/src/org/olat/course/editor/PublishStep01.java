/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.course.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.util.Util;
import org.olat.repository.PropPupForm;
import org.olat.repository.RepositoryEntry;

/**
 * Description:<br>
 * TODO: patrickb Class Description for PublishStep01
 * 
 * <P>
 * Initial Date:  21.01.2008 <br>
 * @author patrickb
 */
class PublishStep01 extends BasicStep {

	private PrevNextFinishConfig prevNextConfig;
	private boolean hasPublishableChanges;

	public PublishStep01(UserRequest ureq, boolean hasPublishableChanges) {
		super(ureq);
		setI18nTitleAndDescr("publish.access.header", null);
		
		this.hasPublishableChanges = hasPublishableChanges;
		if(hasPublishableChanges){
			setNextStep(new PublishStep00a(ureq));
			prevNextConfig = PrevNextFinishConfig.BACK_NEXT_FINISH;
		}else{
			setNextStep(Step.NOSTEP);
			prevNextConfig = PrevNextFinishConfig.BACK_FINISH;
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.BasicStep#getInitialPrevNextFinishConfig()
	 */
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		//can go back and finish immediately
		return prevNextConfig;
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.BasicStep#getStepController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl, org.olat.core.gui.control.generic.wizard.StepsRunContext, org.olat.core.gui.components.form.flexible.impl.Form)
	 */
	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form form) {
		return new PublishStep01AccessForm(ureq, wControl, form, stepsRunContext, hasPublishableChanges);
	}

	class PublishStep01AccessForm extends StepFormBasicController {

		private SingleSelection accessSelbox;
		private String selectedAccess;
		private boolean hasPublishableChanges2;

		PublishStep01AccessForm(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext, boolean hasPublishableChanges2) {
			super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
			this.hasPublishableChanges2 = hasPublishableChanges2;
			selectedAccess = (String) getFromRunContext("selectedCourseAccess");
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			// TODO Auto-generated method stub			
		}

		@Override
		protected void formOK(UserRequest ureq) {
			String newAccess = accessSelbox.getKey(accessSelbox.getSelected());
			if(!selectedAccess.equals(newAccess)){
				//only change if access was changed
				addToRunContext("changedaccess", newAccess);
			}
			if(hasPublishableChanges2){
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			}else{
				fireEvent(ureq, StepsEvent.INFORM_FINISHED);
			}
		
		}

		@Override
		@SuppressWarnings("unused")
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			PackageTranslator pt = (PackageTranslator) Util.createPackageTranslator(PropPupForm.class, getLocale(), getTranslator());
			
			FormItemContainer fic = FormLayoutContainer.createCustomFormLayout("access", pt, this.velocity_root+"/publish_courseaccess.html");
			formLayout.add(fic);
			String[] keys = new String[] {
					"" + RepositoryEntry.ACC_OWNERS,
					"" + RepositoryEntry.ACC_OWNERS_AUTHORS,
					"" + RepositoryEntry.ACC_USERS,
					"" + RepositoryEntry.ACC_USERS_GUESTS
				};
			String[] values = new String[] {
				pt.translate("cif.access.owners"),
				pt.translate("cif.access.owners_authors"),
				pt.translate("cif.access.users"),
				pt.translate("cif.access.users_guests"),
			};
			//use the addDropDownSingleselect method with null as label i18n - key, because there is no label to set. OLAT-3682
			accessSelbox = uifactory.addDropdownSingleselect("accessBox",null, fic, keys, values, null);
			accessSelbox.select(selectedAccess, true);
			
		}
		
	}

	
}

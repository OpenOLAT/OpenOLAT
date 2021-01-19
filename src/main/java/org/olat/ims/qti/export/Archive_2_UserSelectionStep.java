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
package org.olat.ims.qti.export;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.ims.qti.export.QTIArchiver.Type;

/**
 * 
 * Initial date: 19.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Archive_2_UserSelectionStep extends BasicStep {
	
	private final boolean advanced;
	
	public Archive_2_UserSelectionStep(UserRequest ureq, boolean advanced) {
		super(ureq);
		this.advanced = advanced;
		setI18nTitleAndDescr("wizard.user.selection.title", "wizard.user.selection.howto");
		if(advanced) {
			setNextStep(new Archive_3_SettingsStep(ureq));
		} else {
			setNextStep(Step.NOSTEP);
		}
		
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, advanced, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new UserRestrictionsController(ureq, wControl, form, runContext);
	}
	
	public static class UserRestrictionsController extends StepFormBasicController {
		
		private QTIArchiver archiver;
		private MultipleSelectionElement restrictionEl;
		
		public UserRestrictionsController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
			archiver = ((QTIArchiver)getFromRunContext("archiver"));
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			String[] restrictions;
			String[] restrictionValues;
			if(canAnonym()) {
				restrictions = new String[]{ "participant", "others", "anonym" };
				restrictionValues = new String[] {
						translate("archive.participants"), translate("archive.all.users"), translate("archive.anonymous.users")
				};
			} else {
				restrictions = new String[]{ "participant", "others" };
				restrictionValues = new String[] {
						translate("archive.participants"), translate("archive.all.users")
				};
			}
			restrictionEl = uifactory.addCheckboxesVertical("user.restrictions", formLayout, restrictions, restrictionValues, 1);
			restrictionEl.setEnabled(archiver.getType() == Type.qti21);
			for(String restriction:restrictions) {
				restrictionEl.select(restriction, true);
			}
		}
		
		private boolean canAnonym() {
			if(archiver.getType() == Type.qti21) {
				return archiver.getCourseNode().getModuleConfiguration()
						.getBooleanSafe(IQEditController.CONFIG_ALLOW_ANONYM, false);
			}
			
			return false;
		}
		
		@Override
		protected void doDispose() {
			//
		}
		
		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			
			if(!restrictionEl.isAtLeastSelected(1)) {
				restrictionEl.setErrorKey("error.select.type.users", null);
				allOk &= false;
			}
			
			return allOk;
		}

		@Override
		protected void formOK(UserRequest ureq) {
			archiver.setParticipants(restrictionEl.isSelected(0));
			archiver.setAllUsers(restrictionEl.isSelected(1));
			if(restrictionEl.getSize() > 1) {
				archiver.setAnonymUsers(restrictionEl.isSelected(2));
			}
			if(archiver.getType() == Type.qti21) {
				fireEvent(ureq, StepsEvent.INFORM_FINISHED);
			} else {
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			}
		}
	}
}

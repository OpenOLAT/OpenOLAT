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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.OLATResourceable;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.ims.qti.export.QTIArchiver.Type;

/**
 * 
 * Initial date: 20.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Archive_1_SelectNodeStep extends BasicStep {
	
	private final boolean advanced;
	private final OLATResourceable courseOres;
	private final List<AssessmentNodeData> nodeList;
	
	public Archive_1_SelectNodeStep(UserRequest ureq, OLATResourceable courseOres, List<AssessmentNodeData> nodeList, boolean advanced) {
		super(ureq);
		this.nodeList = nodeList;
		this.advanced = advanced;
		this.courseOres = courseOres;
		setI18nTitleAndDescr("wizard.nodechoose.title", "wizard.nodechoose.howto");
		setNextStep(new Archive_2_UserSelectionStep(ureq, advanced));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, false, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		if(!runContext.containsKey("archiver")) {
			runContext.put("archiver", new QTIArchiver(courseOres, ureq.getLocale()));
		}
		return new SelectNodeController(ureq, wControl, form, nodeList, runContext);
	}
	
	public class SelectNodeController extends StepFormBasicController {
		
		private final SelectTestOrSurveyController selectCtrl;
		
		public SelectNodeController(UserRequest ureq, WindowControl wControl, Form rootForm,
				List<AssessmentNodeData> nodeList, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, "resources");
			QTIArchiver archiver = ((QTIArchiver)getFromRunContext("archiver"));
			selectCtrl = new SelectTestOrSurveyController(ureq, wControl, archiver, nodeList, rootForm);
			listenTo(selectCtrl);
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			//
		}
		
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			formLayout.add(selectCtrl.getInitialFormItem());
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			return selectCtrl.validateFormLogic(ureq) & super.validateFormLogic(ureq);
		}

		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			if(source == selectCtrl) {
				if(event instanceof SelectTestOrSurveyEvent) {
					QTIArchiver archiver = ((QTIArchiver)getFromRunContext("archiver"));
					if(archiver.getType() == Type.qti12 && !advanced) {
						fireEvent(ureq, StepsEvent.INFORM_FINISHED);	
					} else {
						fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
					}
				}
			}
			super.event(ureq, source, event);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}
}

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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.ims.qti.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.helpTooltip.HelpTooltip;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableDefaultController;
import org.olat.ims.qti.editor.beecom.objects.Assessment;
import org.olat.ims.qti.editor.beecom.objects.Control;
import org.olat.ims.qti.editor.beecom.objects.Duration;
import org.olat.ims.qti.editor.beecom.objects.OutcomesProcessing;
import org.olat.ims.qti.editor.beecom.objects.SelectionOrdering;

/**
 * Initial Date: Oct 21, 2004 <br>
 * 
 * @author mike
 */
public class AssessmentController extends TabbableDefaultController implements ControllerEventListener {
	
	private VelocityContainer main;
	
	private Assessment assessment;
	private QTIEditorPackage qtiPackage;
	private boolean surveyMode = false;
	private final boolean restrictedEdit;
	private final boolean  blockedEdit;

	/**
	 * @param assessment
	 * @param qtiPackage
	 * @param trnsltr
	 * @param wControl
	 */
	public AssessmentController(Assessment assessment, QTIEditorPackage qtiPackage, UserRequest ureq, WindowControl wControl,
			boolean restrictedEdit, boolean blockedEdit) {
		super(ureq, wControl);

		this.restrictedEdit = restrictedEdit;
		this.blockedEdit = blockedEdit;
		this.assessment = assessment;
		this.qtiPackage = qtiPackage;
				
		main = createVelocityContainer("tab_assess");

		//add Help Links for labels
		HelpTooltip selectionPreHelpText = new HelpTooltip("selectionPreHelpText", translate("form.assessment.selection_pre.hover"));
		main.put("selectionPreHelpText", selectionPreHelpText);

		HelpTooltip orderTypeHelpText = new HelpTooltip("orderTypeHelpText", translate("form.assessment.order_type.hover"));
		main.put("orderTypeHelpText", orderTypeHelpText);

		HelpTooltip globalfeedbackNsolutionHelpText = new HelpTooltip("globalfeedbackNsolutionHelpText", translate("form.metadata.globalfeedbackNsolution.hover"), "Test and Questionnaire Editor in Detail#details_testeditor_feedback", getLocale());
		main.put("globalfeedbackNsolutionHelpText", globalfeedbackNsolutionHelpText);

		main.contextPut("assessment", assessment);
		// fix missing selection ordering, new feature introduced in 9.3.3
		if (assessment.getSelection_ordering() == null) {
			assessment.setSelection_ordering(new SelectionOrdering());
		}
		main.contextPut("order_type", assessment.getSelection_ordering().getOrderType());
		main.contextPut("selection_number", String.valueOf(assessment.getSelection_ordering().getSelectionNumber()));
		main.contextPut("mediaBaseURL", qtiPackage.getMediaBaseURL());
		main.contextPut("control", QTIEditHelper.getControl(assessment));
		main.contextPut("isRestrictedEdit", restrictedEdit ? Boolean.TRUE : Boolean.FALSE);
		main.contextPut("isBlockedEdit", Boolean.valueOf(blockedEdit));
		surveyMode = qtiPackage.getQTIDocument().isSurvey();
		main.contextPut("isSurveyMode", surveyMode ? "true" : "false");

		if (!surveyMode) {
			if (assessment.getDuration() != null) {
				main.contextPut("duration", assessment.getDuration());
			}
		}

		// Adding outcomes processing parameters
		OutcomesProcessing outcomesProcessing = assessment.getOutcomes_processing();
		if (outcomesProcessing == null) {
			main.contextPut(OutcomesProcessing.CUTVALUE, "0.0");
		} else {
			main.contextPut(OutcomesProcessing.CUTVALUE, outcomesProcessing.getField(OutcomesProcessing.CUTVALUE));
		}		
		putInitialPanel(main);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == main) {
			if (event.getCommand().equals("sao")) { // asessment options submitted
				// Handle all data that is useless in survey mode
				String newTitle = ureq.getParameter("title");
				String oldTitle = assessment.getTitle();
				boolean hasTitleChange = newTitle != null && !newTitle.equals(oldTitle);
				String newObjectives = ureq.getParameter("objectives");
				String oldObjectives = assessment.getObjectives();
				boolean hasObjectivesChange = newObjectives != null && !newObjectives.equals(oldObjectives);
				NodeBeforeChangeEvent nce = new NodeBeforeChangeEvent();
				if (hasTitleChange) {
					nce.setNewTitle(newTitle);
				}
				if (hasObjectivesChange) {
					nce.setNewObjectives(newObjectives);
				}
				if (hasTitleChange || hasObjectivesChange) {
					// create a memento first
					fireEvent(ureq, nce);
					// then apply changes
					assessment.setTitle(newTitle);
					assessment.setObjectives(newObjectives);
				}
				//
				if (!surveyMode && !restrictedEdit && !blockedEdit) {
					//ordering
					assessment.getSelection_ordering().setOrderType(ureq.getParameter("order_type"));
					assessment.getSelection_ordering().setSelectionNumber(ureq.getParameter("selection_number"));
					main.contextPut("order_type", assessment.getSelection_ordering().getOrderType());
					main.contextPut("selection_number", String.valueOf(assessment.getSelection_ordering().getSelectionNumber()));
					
					Control tmpControl = QTIEditHelper.getControl(assessment);
					boolean oldInheritControls = assessment.isInheritControls();
					boolean newInheritControls = ureq.getParameter("inheritswitch").equals("Yes");
					assessment.setInheritControls(newInheritControls);

					String feedbackswitchTmp = ureq.getParameter("feedbackswitch");
					String hintswitchTmp = ureq.getParameter("hintswitch");
					String solutionswitchTmp = ureq.getParameter("solutionswitch");
					tmpControl.setSwitches(feedbackswitchTmp, hintswitchTmp, solutionswitchTmp);
					if (tmpControl.getHint() != Control.CTRL_UNDEF || tmpControl.getHint() != Control.CTRL_UNDEF
							|| tmpControl.getSolution() != Control.CTRL_UNDEF) assessment.setInheritControls(true);

					if (oldInheritControls && !newInheritControls) {
						tmpControl.setSwitches(Control.CTRL_UNDEF, Control.CTRL_UNDEF, Control.CTRL_UNDEF);
						assessment.setInheritControls(false);
					}

					OutcomesProcessing outcomesProcessing = assessment.getOutcomes_processing();
					if (outcomesProcessing == null) {
						// Create outcomes processing object if it doesn't already exist.
						// Happens
						// when creating a new assessment
						outcomesProcessing = new OutcomesProcessing();
						assessment.setOutcomes_processing(outcomesProcessing);
					}
					String cutval = ureq.getParameter(OutcomesProcessing.CUTVALUE);
					try {
						Float.parseFloat(cutval);
					} catch (NumberFormatException nfe) {
						cutval = "0.0";						
						this.showWarning("error.cutval");
					}
					outcomesProcessing.setField(OutcomesProcessing.CUTVALUE, cutval);
					main.contextPut(OutcomesProcessing.CUTVALUE, cutval);

					if (ureq.getParameter("duration").equals("Yes")) {
						String durationMin = ureq.getParameter("duration_min");
						String durationSec = ureq.getParameter("duration_sec");
						try {
							Integer.parseInt(durationMin);
							int sec = Integer.parseInt(durationSec);
							if (sec > 60) throw new NumberFormatException();
						} catch (NumberFormatException nfe) {
							durationMin = "0";
							durationSec = "0";							
							this.showWarning("error.duration");
						}
						Duration d = new Duration(durationMin, durationSec);
						assessment.setDuration(d);
						main.contextPut("duration", assessment.getDuration());
					} else {
						assessment.setDuration(null);
						main.contextRemove("duration");
					}
				}
				qtiPackage.serializeQTIDocument();
				
				//refresh for removing dirty marking of button even if nothing changed
				main.setDirty(true);	
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		main = null;		
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.TabbableController#addTabs(org.olat.core.gui.components.TabbedPane)
	 */
	public void addTabs(TabbedPane tabbedPane) {
		tabbedPane.addTab(translate(surveyMode ? "tab.survey" : "tab.assessment"), main);
	}

}
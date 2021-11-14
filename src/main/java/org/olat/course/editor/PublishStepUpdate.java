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
package org.olat.course.editor;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
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
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * 
 * Initial date: 06.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class PublishStepUpdate extends BasicStep {
	
	private final PrevNextFinishConfig prevNextConfig;
	
	public PublishStepUpdate(UserRequest ureq, boolean hasPublishableChanges) {
		super(ureq);

		setI18nTitleAndDescr("publish.step.update.title", null);
		
		if(hasPublishableChanges) {
			setNextStep(new PublishStep00b(ureq));
			prevNextConfig = PrevNextFinishConfig.BACK_NEXT_FINISH;
		} else {
			setNextStep(Step.NOSTEP);
			prevNextConfig = PrevNextFinishConfig.BACK_FINISH;
		}
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return prevNextConfig;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new PublishStepUpdateForm(ureq, wControl, form, runContext);
	}
	
	static class PublishStepUpdateForm extends StepFormBasicController {
		
		private final CourseNodeFactory courseNodeFactory;
		
		public PublishStepUpdateForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "publish_update");
			
			courseNodeFactory = CoreSpringFactory.getImpl(CourseNodeFactory.class);

			initForm(ureq);
		}
		
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			
			@SuppressWarnings("unchecked")
			List<StatusDescription> noteDescriptions = (List<StatusDescription>)getFromRunContext("updateNotes");
			if(noteDescriptions != null && noteDescriptions.size() > 0) {
				PublishProcess publishProcess = (PublishProcess)getFromRunContext("publishProcess");
				StringBuilder notes = new StringBuilder();
				
				Collections.sort(noteDescriptions, new StatusDescriptionComparator());
				StatusDescription currentDesc = null;
				for (Iterator<StatusDescription> it=noteDescriptions.iterator(); it.hasNext(); ) {
					if(currentDesc == null) {
						currentDesc = it.next();
					}
					
					String nodeId = currentDesc.getDescriptionForUnit();
					CourseNode courseNode = publishProcess.getCourseEditorTreeModel().getCourseNode(nodeId);
					CourseNodeConfiguration config = courseNodeFactory.getCourseNodeConfiguration(courseNode.getType());	
					String cssClass = config.getIconCSSClass();
					String nodeName = courseNode.getShortName();
					notes.append("<i class='o_icon o_icon-fw ").append(cssClass).append("'> </i> <b>").append(nodeName).append("</b><ul>");
					for ( ;currentDesc != null && currentDesc.getDescriptionForUnit().equals(nodeId); currentDesc = (it.hasNext() ? it.next() : null)) {
						notes.append("<li>").append(currentDesc.getShortDescription(ureq.getLocale())).append("</li>");
					}
					notes.append("</ul>");
				}

				uifactory.addStaticTextElement("updateNotes", null, notes.toString(), formLayout);
			}
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}
	
	static class StatusDescriptionComparator implements Comparator<StatusDescription> {
		@Override
		public int compare(StatusDescription s1, StatusDescription s2) {
			String i1 = s1.getDescriptionForUnit();
			String i2 = s2.getDescriptionForUnit();
			
			if(i1 == null && i2 == null) return 0;
			if(i1 == null) return -1;
			if(i2 == null) return 1;
			return i1.compareTo(i2);
		}
	}
}

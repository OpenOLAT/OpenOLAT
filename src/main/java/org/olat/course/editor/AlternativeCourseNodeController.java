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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * 
 * Initial date: 14.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AlternativeCourseNodeController extends FormBasicController {
	
	private SingleSelection alternativesEl;
	
	private final CourseNode courseNode;

	public AlternativeCourseNodeController(UserRequest ureq, WindowControl wControl, CourseNode courseNode) {
		super(ureq, wControl);
		
		this.courseNode = courseNode;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("alternative.choose.description");

		CourseNodeConfiguration config
			= CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType());

		List<String> alternativeKeyList = new ArrayList<>(4);
		List<String> alternativeValueList = new ArrayList<>(4);
		for(String alt:config.getAlternativeCourseNodes()) {
			CourseNodeConfiguration altConfig = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(alt);
			if(altConfig.isEnabled()) {
				alternativeKeyList.add(alt);
				alternativeValueList.add(altConfig.getLinkText(getLocale()));
			}
		}

		String[] alternativeKeys = alternativeKeyList.toArray(new String[alternativeKeyList.size()]);
		String[] alternativeValues = alternativeValueList.toArray(new String[alternativeValueList.size()]);
		alternativesEl = uifactory.addRadiosVertical("alternative.bbs", formLayout, alternativeKeys, alternativeValues);
		if(alternativeKeys.length == 1) {
			alternativesEl.select(alternativeKeys[0], true);
		}

		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsLayout);
		uifactory.addFormSubmitButton("ok", buttonsLayout);
		uifactory.addFormCancelButton("cancel", buttonsLayout, ureq, getWindowControl());
	}
	
	public String getSelectedAlternative() {
		return alternativesEl.isOneSelected() ? alternativesEl.getSelectedKey() : null;
	}
	
	public CourseNode getCourseNode() {
		return courseNode;
	}
	

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		alternativesEl.clearError();
		if(!alternativesEl.isOneSelected()) {
			alternativesEl.setErrorKey("form.legende.mandatory", null);
			allOk = false;
		}
		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}

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
package org.olat.course.nodes;

import java.util.Date;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;

/**
 * Initial date: 23.07.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CourseNodeDatesListController extends FormBasicController {

	private CourseNode courseNode;
	
	public CourseNodeDatesListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_DEFAULT);
		
		
		setTranslator(Util.createPackageTranslator(CourseNode.class, getLocale(), getTranslator()));
	}	
	
	public void updateCourseNode(CourseNode courseNode, UserRequest ureq) {
		this.courseNode = courseNode;
		
		this.flc.removeAll();
		initForm(ureq);
	}
	
	@Override
	protected void doDispose() {
		// Nothing to do here
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (!courseNode.hasNodeSpecificDates()) {
			return;
		}
		
		for (Map.Entry<String, Date> entry : courseNode.getNodeSpecificDatesWithLabel().entrySet()) {
			DateChooser dateChooser = uifactory.addDateChooser(entry.getKey(), entry.getValue(), formLayout);
			dateChooser.setEnabled(false);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// TODO Auto-generated method stub
		
	}

}

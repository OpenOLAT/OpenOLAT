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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditCommentController extends FormBasicController {
	
	private TextElement commentEl;
	private final AssessmentRow row;
	private final GTACourseNode gtaNode;
	private final OLATResourceable courseOres;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public EditCommentController(UserRequest ureq, WindowControl wControl,
			OLATResourceable courseOres, GTACourseNode gtaNode, AssessmentRow row) {
		super(ureq, wControl, "comment_callout");
		this.row = row;
		this.gtaNode = gtaNode;
		this.courseOres = courseOres;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		commentEl = uifactory.addTextAreaElement("ucomment", "comment", 2500, 5, 40, true, false, row.getComment(), formLayout);
		commentEl.setElementCssClass("o_sel_course_gta_comment");

		uifactory.addFormSubmitButton("save", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		String comment = commentEl.getValue();
		row.setComment(comment);
		
		ICourse course = CourseFactory.loadCourse(courseOres);
		UserCourseEnvironment userCourseEnv = row.getUserCourseEnvironment(course);
		courseAssessmentService.updatedUserComment(gtaNode, comment, userCourseEnv, getIdentity());
		
		if(StringHelper.containsNonWhitespace(comment)) {
			row.getCommentEditLink().setIconLeftCSS("o_icon o_icon_comments");
		} else {
			row.getCommentEditLink().setIconLeftCSS("o_icon o_icon_comments_none");
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}

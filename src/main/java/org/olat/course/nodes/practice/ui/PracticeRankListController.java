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
package org.olat.course.nodes.practice.ui;

import java.util.List;

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.PracticeCourseNode;
import org.olat.course.nodes.practice.PracticeService;
import org.olat.course.nodes.practice.model.RankedIdentity;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserAvatarMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeRankListController extends FormBasicController {

	private MultipleSelectionElement showEl;
	
	private boolean shared;
	private final MapperKey avatarMapperKey;
	private final RepositoryEntry courseEntry;
	private final PracticeCourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;

	@Autowired
	private MapperService mapperService;
	@Autowired
	private PracticeService practiceService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public PracticeRankListController(UserRequest ureq, WindowControl wControl, Form rootForm,
			RepositoryEntry courseEntry, PracticeCourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl, LAYOUT_CUSTOM, "rank_list", rootForm);
		this.courseNode = courseNode;
		this.courseEntry = courseEntry;
		this.userCourseEnv = userCourseEnv;
		avatarMapperKey = mapperService.register(null, "avatars-members", new UserAvatarMapper(false));
		
		AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, userCourseEnv);
		shared = assessmentEntry != null && assessmentEntry.getShare() != null && assessmentEntry.getShare().booleanValue();
		
		initForm(ureq);
		if(shared) {
			loadModel();
		}
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("avatarBaseURL", avatarMapperKey.getUrl());
		}
		
		SelectionValues showValues = new SelectionValues();
		showValues.add(SelectionValues.entry("share", translate("share.score.value")));
		
		showEl = uifactory.addCheckboxesHorizontal("share.score", null, formLayout, showValues.keys(), showValues.values());
		showEl.addActionListener(FormEvent.ONCHANGE);
		showEl.setVisible(!shared);
	}

	private void loadModel() {	
		List<RankedIdentity> identities = practiceService.getRankList(getIdentity(), courseEntry, courseNode.getIdent(), 5);
		flc.contextPut("rankList", identities);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(showEl == source) {
			doShare(showEl.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	
	private void doShare(boolean share) {
		this.shared = share;
		
		AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, userCourseEnv);
		assessmentEntry.setShare(Boolean.valueOf(share));
		userCourseEnv.getCourseEnvironment().getAssessmentManager().updateAssessmentEntry(assessmentEntry);
		
		showEl.setVisible(!shared);
		if(shared) {
			loadModel();
		} else {
			flc.contextRemove("rankList");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}

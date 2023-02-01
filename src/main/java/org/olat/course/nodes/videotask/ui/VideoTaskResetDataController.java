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
package org.olat.course.nodes.videotask.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroupService;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskResetDataController extends FormBasicController {
	
	private MultipleSelectionElement acknowledgeEl;
	
	private CourseEnvironment courseEnv;
	private VideoTaskCourseNode courseNode;
	
	private final ArchiveOptions options;
	private final List<Identity> identities;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private VideoAssessmentService videoAssessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public VideoTaskResetDataController(UserRequest ureq, WindowControl wControl,
			CourseEnvironment courseEnv, AssessmentToolOptions asOptions, VideoTaskCourseNode courseNode) {
		super(ureq, wControl, "confirm_reset_data");
		this.courseNode = courseNode;
		this.courseEnv = courseEnv;
		
		options = new ArchiveOptions();
		if(asOptions.getGroup() == null && asOptions.getIdentities() == null) {
			identities = ScoreAccountingHelper.loadUsers(courseEnv);
			options.setIdentities(identities);
		} else if (asOptions.getIdentities() != null) {
			identities = asOptions.getIdentities();
			options.setIdentities(identities);
		} else {
			identities = businessGroupService.getMembers(asOptions.getGroup());
			options.setGroup(asOptions.getGroup());
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			String msg = translate("reset.test.data.text", Integer.toString(identities.size()) );
			layoutCont.contextPut("msg", msg);
		}
		
		FormLayoutContainer confirmCont = uifactory.addDefaultFormLayout("confirm", null, formLayout); 
		
		SelectionValues acknowledgeValues = new SelectionValues();
		acknowledgeValues.add(SelectionValues.entry("on", translate("reset.test.data.acknowledge")));
		acknowledgeEl = uifactory.addCheckboxesHorizontal("acknowledge", "confirmation", confirmCont,
				acknowledgeValues.keys(), acknowledgeValues.values());
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, confirmCont);
		uifactory.addFormSubmitButton("reset.data", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		acknowledgeEl.clearError();
		if(!acknowledgeEl.isAtLeastSelected(1)) {
			acknowledgeEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doResetData();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doResetData() {
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		ICourse course = CourseFactory.loadCourse(courseEntry);
		archiveData(course, options);
		
		videoAssessmentService.deleteTaskSessions(identities, courseEntry, courseNode.getIdent());
		for(Identity identity:identities) {
			ScoreEvaluation scoreEval = new ScoreEvaluation(null, null, null, null, null,
					AssessmentEntryStatus.notStarted, null, null, 0.0d, AssessmentRunStatus.notStarted, null);
			IdentityEnvironment ienv = new IdentityEnvironment(identity, Roles.userRoles());
			UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, courseEnv);
			courseAssessmentService.updateScoreEvaluation(courseNode, scoreEval, uce, getIdentity(), false,
					Role.coach);
			courseAssessmentService.updateCurrentCompletion(courseNode, uce, null, null, AssessmentRunStatus.notStarted,
					Role.coach);
			dbInstance.commitAndCloseSession();
		}
	}

	private void archiveData(ICourse course, ArchiveOptions archiveOptions) {
		File exportDirectory = CourseFactory.getOrCreateDataExportDirectory(getIdentity(), course.getCourseTitle());
		String archiveName = courseNode.getType() + "_"
				+ StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis())) + ".zip";

		File exportFile = new File(exportDirectory, archiveName);
		try(FileOutputStream fileStream = new FileOutputStream(exportFile);
			ZipOutputStream exportStream = new ZipOutputStream(fileStream)) {
			
			courseNode.archiveNodeData(getLocale(), course, archiveOptions, exportStream, "", "UTF-8");
		} catch (IOException e) {
			logError("", e);
		}
	}
}

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
package org.olat.course.nodes.iq;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentStatus;
import org.olat.modules.grading.GradingService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This controller will make an archive of the current test entry results
 * and propose some informations.
 * 
 * 
 * Initial date: 24 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmChangeResourceController extends FormBasicController {
	
	private FormLink replaceButton;
	private FormLink downloadButton;
	
	private final ICourse course;
	private final File downloadArchiveFile;
	private final QTICourseNode courseNode;
	private final RepositoryEntry newTestEntry;
	private final RepositoryEntry currentTestEntry;
	private final List<Identity> assessedIdentities;
	private final int numOfAssessedIdentities;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public ConfirmChangeResourceController(UserRequest ureq, WindowControl wControl, ICourse course, QTICourseNode courseNode,
			RepositoryEntry newTestEntry, RepositoryEntry currentTestEntry, List<Identity> assessedIdentities, int numOfAssessedIdentities) {
		super(ureq, wControl, "confirm_change");
		this.course = course;
		this.courseNode = courseNode;
		this.newTestEntry = newTestEntry;
		this.currentTestEntry = currentTestEntry;
		this.assessedIdentities = assessedIdentities;
		this.numOfAssessedIdentities = numOfAssessedIdentities;
		downloadArchiveFile = prepareArchive(ureq);
		initForm(ureq);
	}
	
	public RepositoryEntry getNewTestEntry() {
		return newTestEntry;
	}
	
	public RepositoryEntry getCurrentTestEntry() {
		return currentTestEntry;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			if(numOfAssessedIdentities == 1) {
				layoutCont.contextPut("infos1", translate("confirmation.change.warning.1", Integer.toString(numOfAssessedIdentities)));
			} else {
				layoutCont.contextPut("infos1", translate("confirmation.change.warning.1.plural", Integer.toString(numOfAssessedIdentities)));
			}
			String[] archiveArgs = new String[] { downloadArchiveFile.getParentFile().getName(), downloadArchiveFile.getName() };
			layoutCont.contextPut("infos3", translate("confirmation.change.warning.3", archiveArgs));
		}
		
		downloadButton = uifactory.addFormLink("download", downloadArchiveFile.getName(), null, formLayout, Link.LINK | Link.NONTRANSLATED);
		downloadButton.setIconLeftCSS("o_icon o_icon_downloads");
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		replaceButton = uifactory.addFormLink("reset.replace.file", formLayout, Link.BUTTON);
		replaceButton.setElementCssClass("btn btn-default btn-danger");
	}
	
	private File prepareArchive(UserRequest ureq) {
		File exportDir = CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
		String label = StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeWithMinutes(new Date()) + ".zip";
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		File archiveFile = new File(exportDir, urlEncodedLabel);
		
		try(OutputStream out= new FileOutputStream(archiveFile);
				ZipOutputStream exportStream = new ZipOutputStream(out)) {
			courseNode.archiveNodeData(getLocale(), course, null, exportStream, "", "UTF8");
			return archiveFile;
		} catch(Exception e) {
			logError("", e);
			return null;
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(downloadButton == source) {
			doDownload(ureq);
		} else if(replaceButton == source) {
			doReplace(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doDownload(UserRequest ureq) {
		ureq.getDispatchResult()
			.setResultingMediaResource(new FileMediaResource(downloadArchiveFile, true));
	}
	
	private void doReplace(UserRequest ureq) {
		// reset the data
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		
		for(Identity assessedIdentity:assessedIdentities) {
			IdentityEnvironment ienv = new IdentityEnvironment(assessedIdentity, Roles.userRoles());
			UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, courseEnv);
			
			// Cancel test sessions and grading assignment
			List<AssessmentTestSession> sessions = qtiService.getAssessmentTestSessions(courseEntry, courseNode.getIdent(), assessedIdentity, true);
			if(sessions.isEmpty()) {
				AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, uce);
				for (AssessmentTestSession session:sessions) {
					if (!newTestEntry.equals(session.getTestEntry())
							&& !session.isCancelled() && !session.isExploded()) {
						session.setCancelled(true);
						session = qtiService.updateAssessmentTestSession(session);
						deactivateGradingAssignment(assessmentEntry, session);
					}
				}
			}
			
			ScoreEvaluation scoreEval = new ScoreEvaluation(null, null, null, null, AssessmentEntryStatus.notStarted,
					Boolean.FALSE, null, 0.0d, AssessmentRunStatus.notStarted, null);
			courseAssessmentService.updateScoreEvaluation(courseNode, scoreEval, uce, getIdentity(), false,
					Role.coach);
			courseAssessmentService.updateCurrentCompletion(courseNode, uce, null, null, AssessmentRunStatus.notStarted,
					Role.coach);
			courseAssessmentService.updateAttempts(courseNode, 0, null, uce, getIdentity(),
					Role.coach);
			dbInstance.commitAndCloseSession();
		}
		
		// replacement is done by the parent controller
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void deactivateGradingAssignment(AssessmentEntry assessmentEntry, AssessmentTestSession session) {
		if (gradingService.isGradingEnabled(session.getTestEntry(), null)) {
			GradingAssignment assignment = gradingService.getGradingAssignment(session.getTestEntry(), assessmentEntry);
			if (assignment != null && session.getKey().equals(assessmentEntry.getAssessmentId())) {
				GradingAssignmentStatus assignmentStatus = assignment.getAssignmentStatus();
				if (assignmentStatus == GradingAssignmentStatus.assigned
						|| assignmentStatus == GradingAssignmentStatus.inProcess
						|| assignmentStatus == GradingAssignmentStatus.done) {
					gradingService.deactivateAssignment(assignment);
				}
			}
		}
	}
}
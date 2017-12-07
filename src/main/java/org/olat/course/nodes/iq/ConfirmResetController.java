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
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti21.QTI21LoggingAction;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmResetController extends FormBasicController {
	
	private final String[] onKeys = new String[]{ "on" };

	private MultipleSelectionElement acknowledgeEl;

	private final Identity assessedIdentity;
	private final AssessableCourseNode courseNode;
	private final RepositoryEntry courseEntry, testEntry;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private QTI21Service qtiService;
	
	public ConfirmResetController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			AssessableCourseNode courseNode, RepositoryEntry testEntry, Identity assessedIdentity) {
		super(ureq, wControl, "confirm_reset_data");
		setTranslator(Util.createPackageTranslator(AssessmentTestDisplayController.class, getLocale(), getTranslator()));
		this.assessedIdentity = assessedIdentity;
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		this.testEntry = testEntry;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String[] args = new String[]{ userManager.getUserDisplayName(assessedIdentity) };
			String msg = translate("reset.test.data.text.identity", args);
			layoutCont.contextPut("msg", msg);
		}
		
		FormLayoutContainer confirmCont = FormLayoutContainer.createDefaultFormLayout("confirm", getTranslator());
		formLayout.add("confirm", confirmCont);
		confirmCont.setRootForm(mainForm);
		
		String[] onValues = new String[]{ translate("reset.test.data.acknowledge") };
		acknowledgeEl = uifactory.addCheckboxesHorizontal("acknowledge", "confirmation", confirmCont, onKeys, onValues);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		confirmCont.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("reset.data", buttonsCont);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		acknowledgeEl.clearError();
		if(!acknowledgeEl.isAtLeastSelected(1)) {
			acknowledgeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
				.createAndInitUserCourseEnvironment(assessedIdentity, course.getCourseEnvironment());

		List<Identity> identities = Collections.singletonList(assessedIdentity);
		ArchiveOptions options = new ArchiveOptions();
		options.setIdentities(identities);

		File exportDirectory = CourseFactory.getOrCreateDataExportDirectory(getIdentity(), course.getCourseTitle());
		String archiveName = courseNode.getType()
				+ "_" + StringHelper.transformDisplayNameToFileSystemName(userManager.getUserDisplayName(assessedIdentity))
				+ "_" + StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis())) + ".zip";

		File exportFile = new File(exportDirectory, archiveName);
		try(FileOutputStream fileStream = new FileOutputStream(exportFile);
			ZipOutputStream exportStream = new ZipOutputStream(fileStream)) {
			courseNode.archiveNodeData(getLocale(), course, options, exportStream, "UTF-8");
		} catch (IOException e) {
			logError("", e);
		}

		qtiService.deleteAssessmentTestSession(identities, testEntry, courseEntry, courseNode.getIdent());

		ScoreEvaluation scoreEval = new ScoreEvaluation(null, null);
		courseNode.updateUserScoreEvaluation(scoreEval, assessedUserCourseEnv, getIdentity(), false, Role.coach);
		courseNode.updateCurrentCompletion(assessedUserCourseEnv, getIdentity(), null, AssessmentRunStatus.notStarted, Role.coach);
		

		ThreadLocalUserActivityLogger.log(QTI21LoggingAction.QTI_RESET_IN_COURSE, getClass());
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

}

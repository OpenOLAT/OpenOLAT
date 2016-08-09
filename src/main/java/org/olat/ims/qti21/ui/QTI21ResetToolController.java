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
package org.olat.ims.qti21.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.AssessmentToolOptions;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroupService;
import org.olat.ims.qti21.QTI21Service;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ResetToolController extends BasicController {
	
	private final Roles studentRoles = new Roles(false, false, false, false, false, false, false, false);
	private final Link resetButton;
	
	private DialogBoxController confirmResetDialog;
	
	private final QTICourseNode courseNode;
	private final CourseEnvironment courseEnv;
	private final AssessmentToolOptions asOptions;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private BusinessGroupService businessGroupService;

	public QTI21ResetToolController(UserRequest ureq, WindowControl wControl, 
			CourseEnvironment courseEnv, AssessmentToolOptions asOptions, QTICourseNode courseNode) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.courseEnv = courseEnv;
		this.asOptions = asOptions;
		
		resetButton = LinkFactory.createButton("reset.test.data.title", null, this);
		resetButton.setTranslator(getTranslator());
		putInitialPanel(resetButton);
		getInitialComponent().setSpanAsDomReplaceable(true); // override to wrap panel as span to not break link layout 
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(resetButton == source) {
			doConfirmReset(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmResetDialog == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				doReset(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	private void doConfirmReset(UserRequest ureq) {
		String title = translate("reset.test.data.title");
		String text = translate("reset.test.data.text");
		confirmResetDialog = activateOkCancelDialog(ureq, title, text, confirmResetDialog);
	}
	
	private void doReset(UserRequest ureq) {
		List<Identity> identities;

		ArchiveOptions options = new ArchiveOptions();
		if(asOptions.getGroup() == null) {
			identities = asOptions.getIdentities();
			options.setIdentities(identities);
		} else {
			identities = businessGroupService.getMembers(asOptions.getGroup());
			options.setGroup(asOptions.getGroup());
		}

		RepositoryEntry testEntry = courseNode.getReferencedRepositoryEntry();
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();

		if(courseNode instanceof IQTESTCourseNode) {
			IQTESTCourseNode testCourseNode = (IQTESTCourseNode)courseNode;
			
			ICourse course = CourseFactory.loadCourse(courseEntry);
			archiveData(course, options);
			
			qtiService.deleteAssessmentTestSession(identities, testEntry, courseEntry, courseNode.getIdent());
			for(Identity identity:identities) {
				ScoreEvaluation scoreEval = new ScoreEvaluation(null, null);
				
				IdentityEnvironment ienv = new IdentityEnvironment(identity, studentRoles);
				UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, courseEnv);
				testCourseNode.updateUserScoreEvaluation(scoreEval, uce, getIdentity(), false);
			}
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void archiveData(ICourse course, ArchiveOptions options) {
		File exportDirectory = CourseFactory.getOrCreateDataExportDirectory(getIdentity(), course.getCourseTitle());
		String archiveName = courseNode.getType() + "_"
				+ StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis())) + ".zip";

		File exportFile = new File(exportDirectory, archiveName);
		try(FileOutputStream fileStream = new FileOutputStream(exportFile);
			ZipOutputStream exportStream = new ZipOutputStream(fileStream)) {
			
			courseNode.archiveNodeData(getLocale(), course, options, exportStream, "UTF-8");
		} catch (IOException e) {
			logError("", e);
		}
	}
}

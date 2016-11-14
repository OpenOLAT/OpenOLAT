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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroupService;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.archive.QTI21ArchiveFormat;
import org.olat.modules.assessment.AssessmentToolOptions;
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
	private Link resetButton;
	
	private CloseableModalController cmc;
	private ConfirmResetController confirmResetCtrl;
	
	private ArchiveOptions options;
	private QTICourseNode courseNode;
	private List<Identity> identities;
	private CourseEnvironment courseEnv;
	private RepositoryEntry assessedEntry;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private BusinessGroupService businessGroupService;

	public QTI21ResetToolController(UserRequest ureq, WindowControl wControl, 
			CourseEnvironment courseEnv, AssessmentToolOptions asOptions, QTICourseNode courseNode) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.courseEnv = courseEnv;
		initButton(asOptions);
	}
	
	public QTI21ResetToolController(UserRequest ureq, WindowControl wControl, 
			RepositoryEntry assessedEntry, AssessmentToolOptions asOptions) {
		super(ureq, wControl);
		this.assessedEntry = assessedEntry;
		initButton(asOptions);
	}
	
	private void initButton(AssessmentToolOptions asOptions) {
		options = new ArchiveOptions();
		if(asOptions.getGroup() == null) {
			identities = asOptions.getIdentities();
			options.setIdentities(identities);
		} else {
			identities = businessGroupService.getMembers(asOptions.getGroup());
			options.setGroup(asOptions.getGroup());
		}
		
		resetButton = LinkFactory.createButton("reset.test.data.title", null, this);
		resetButton.setIconLeftCSS("o_icon o_icon_delete_item");
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
		if(confirmResetCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doReset(ureq);
				fireEvent(ureq, Event.DONE_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmResetCtrl);
		removeAsListenerAndDispose(cmc);
		confirmResetCtrl = null;
		cmc = null;
	}

	private void doConfirmReset(UserRequest ureq) {
		if(confirmResetCtrl != null) return;
		
		confirmResetCtrl = new ConfirmResetController(ureq, this.getWindowControl());
		listenTo(confirmResetCtrl);

		String title = translate("reset.test.data.title");
		cmc = new CloseableModalController(getWindowControl(), null, confirmResetCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doReset(UserRequest ureq) {
		if(courseNode instanceof IQTESTCourseNode) {
			IQTESTCourseNode testCourseNode = (IQTESTCourseNode)courseNode;
			RepositoryEntry testEntry = courseNode.getReferencedRepositoryEntry();
			RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
			
			ICourse course = CourseFactory.loadCourse(courseEntry);
			archiveData(course);
			
			qtiService.deleteAssessmentTestSession(identities, testEntry, courseEntry, courseNode.getIdent());
			for(Identity identity:identities) {
				ScoreEvaluation scoreEval = new ScoreEvaluation(null, null);
				IdentityEnvironment ienv = new IdentityEnvironment(identity, studentRoles);
				UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, courseEnv);
				testCourseNode.updateUserScoreEvaluation(scoreEval, uce, getIdentity(), false);
			}
		} else if(assessedEntry != null) {
			archiveData(assessedEntry);
			qtiService.deleteAssessmentTestSession(identities, assessedEntry, null, null);
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void archiveData(ICourse course) {
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
	
	private void archiveData(RepositoryEntry testEntry) {
		//backup
		String archiveName = "qti21test_"
				+ StringHelper.transformDisplayNameToFileSystemName(testEntry.getDisplayname())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis())) + ".zip";
		Path exportPath = Paths.get(FolderConfig.getCanonicalRoot(), FolderConfig.getUserHomes(), getIdentity().getName(),
				"private", "archive", StringHelper.transformDisplayNameToFileSystemName(testEntry.getDisplayname()), archiveName);
		File exportFile = exportPath.toFile();
		exportFile.getParentFile().mkdirs();
		
		try(FileOutputStream fileStream = new FileOutputStream(exportFile);
			ZipOutputStream exportStream = new ZipOutputStream(fileStream)) {
			new QTI21ArchiveFormat(getLocale(), true, true, true).export(testEntry, exportStream);
		} catch (IOException e) {
			logError("", e);
		}
		
	}
	
	private class ConfirmResetController extends FormBasicController {
		
		private final String[] onKeys = new String[]{ "on" };

		private MultipleSelectionElement acknowledgeEl;
		
		public ConfirmResetController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, "confirm_reset_data");
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			if(formLayout instanceof FormLayoutContainer) {
				FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
				String[] args = new String[]{ Integer.toString(identities.size()) };
				String msg = translate("reset.test.data.text", args);
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
			fireEvent(ureq, Event.DONE_EVENT);
		}

		@Override
		protected void formCancelled(UserRequest ureq) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}
}

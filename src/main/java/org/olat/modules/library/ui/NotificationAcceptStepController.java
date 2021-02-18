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
package org.olat.modules.library.ui;

import java.util.Set;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.LibraryModule;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * <h3>Description:</h3>
 * <p>
 * Notification step of the library publish process. An e-mail is send to
 * the submitter of the document.
 * <p>
 * Events fired:
 * <ul>
 * 	<li>ACTIVATE_NEXT</li>
 * 	<li>FAILED_EVENT</li>
 * </ul>
 * 
 * Initial Date:  Sep 24, 2009 <br>
 * @author twuersch, timo.wuersch@frentix.com, www.frentix.com
 */
public class NotificationAcceptStepController extends StepFormBasicController {
	
	public static final String STEPS_RUN_CONTEXT_NOTIFICATION_SUBJECT_KEY = "notificationsubject";
	public static final String STEPS_RUN_CONTEXT_NOTIFICATION_BODY_KEY = "notificationbody";
	private TextElement subjectTextElement;
	private TextElement bodyTextElement;
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private LibraryModule libraryModule;
	@Autowired
	private LibraryManager libraryManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public NotificationAcceptStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form rootForm) {
		super(ureq, wControl, rootForm, stepsRunContext, LAYOUT_DEFAULT, null);

		String relativeSourceFileName = (String) getFromRunContext(MetadataAcceptStepController.STEPS_RUN_CONTEXT_FILENAME_KEY);
		if (!containsRunContextKey(STEPS_RUN_CONTEXT_NOTIFICATION_BODY_KEY)) {
			addToRunContext(STEPS_RUN_CONTEXT_NOTIFICATION_BODY_KEY, translate("acceptstep.notification.msg.body", relativeSourceFileName));
		}
		if (!containsRunContextKey(STEPS_RUN_CONTEXT_NOTIFICATION_SUBJECT_KEY)) {
			addToRunContext(STEPS_RUN_CONTEXT_NOTIFICATION_SUBJECT_KEY, translate("acceptstep.notification.msg.subject", relativeSourceFileName));
		}
		initForm(ureq);
	}

	@Override
	protected void doDispose() {
		//nothing to dispose
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isInputValid = true;
		String subject = subjectTextElement.getValue();
		if(!StringHelper.containsNonWhitespace(subject)) {
			subjectTextElement.setErrorKey("error.mail.subject.empty", null);
			isInputValid = false;
		}
		String message = bodyTextElement.getValue();
		if(!StringHelper.containsNonWhitespace(message)) {
			bodyTextElement.setErrorKey("error.mail.message.empty", null);
			isInputValid = false;
		}
		return isInputValid;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void formOK(UserRequest ureq) {
		try {
			// get the source file name.
			String relativeSourceFileName = (String) getFromRunContext(MetadataAcceptStepController.STEPS_RUN_CONTEXT_FILENAME_KEY); 
			String relativeNewSourceFileName = (String) getFromRunContext(MetadataAcceptStepController.STEPS_RUN_CONTEXT_NEW_FILENAME_KEY); 
			VFSLeaf sourceFile = (VFSLeaf)libraryManager.getUploadFolder().resolve(relativeSourceFileName);
			String targetFileName = relativeNewSourceFileName == null ? relativeSourceFileName : relativeNewSourceFileName;
			
			// get the relative destination file name, the tree model, and the selection.
			LibraryTreeModel treeModel = (LibraryTreeModel)getFromRunContext(DestinationAcceptStepController.STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_MODEL_KEY);
			Set<String> selection = (Set<String>) getFromRunContext(DestinationAcceptStepController.STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_KEY);
			
			VFSContainer rootContainer = VFSManager.olatRootContainer("", null);
			
			// for all selected destination folders, ...
			for (String key : selection) {
				TreeNode selectedNode = treeModel.getNodeById(key);
				// ...calculate the absolute destination file name...
				String relativeDestinationDirectoryName = (String) selectedNode.getUserObject();
				VFSContainer destinationDirectory = (VFSContainer)rootContainer.resolve(relativeDestinationDirectoryName);
				VFSLeaf targetFile = destinationDirectory.createChildLeaf(targetFileName);
				
				// ...and copy the file there.
				if (!VFSManager.copyContent(sourceFile, targetFile, true, getIdentity())) {
					showError("acceptstep.notification.copyerror");
					logError("Error while copying \"" + sourceFile.getName() + "\" to \"" + destinationDirectory.getName() + "\".", null);
				}
				
				VFSMetadata metaInfo = targetFile.getMetaInfo();
				metaInfo.copyValues((VFSMetadata)getFromRunContext(MetadataAcceptStepController.STEPS_RUN_CONTEXT_METADATA_KEY), true);
				metaInfo = vfsRepositoryService.updateMetadata(metaInfo);
				if (metaInfo == null) {
					logError("Error writing metadata for " + relativeDestinationDirectoryName + "/" + relativeSourceFileName, null);
					showError("acceptstep.notification.metafileerror");
				}
			}

			// send notification e-mail
			VFSMetadata metaInfo = sourceFile.getMetaInfo();
			Identity uploaderIdentity = metaInfo.getFileInitializedBy();

			String mailto;
			if(StringHelper.containsNonWhitespace(libraryModule.getEmailContactsToNotifyAfterFreeing())) {
				mailto = libraryModule.getEmailContactsToNotifyAfterFreeing();
			} else {
				mailto = uploaderIdentity.getUser().getProperty(UserConstants.EMAIL, getLocale());
			}
			MailBundle bundle = new MailBundle();
			bundle.setTo(mailto);
			bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
			bundle.setContent(subjectTextElement.getValue(), bodyTextElement.getValue());
			mailManager.sendMessage(bundle);
			
			sourceFile.delete();
			vfsRepositoryService.deleteMetadata(metaInfo);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} catch (Exception e) {
			logError("Exception while reading source folder.", e);
			showError("acceptstep.notification.ioexception");
			fireEvent(ureq, Event.FAILED_EVENT);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,  UserRequest ureq) {
		subjectTextElement = uifactory.addTextElement("acceptstep.notification.ui.subject", "acceptstep.notification.ui.subject", -1, containsRunContextKey(STEPS_RUN_CONTEXT_NOTIFICATION_SUBJECT_KEY) ? (String) getFromRunContext(STEPS_RUN_CONTEXT_NOTIFICATION_SUBJECT_KEY) : "", formLayout);
		bodyTextElement = uifactory.addTextAreaElement("acceptstep.notification.ui.body", "acceptstep.notification.ui.body", -1, 10, 30, false, false, containsRunContextKey(STEPS_RUN_CONTEXT_NOTIFICATION_BODY_KEY) ? (String) getFromRunContext(STEPS_RUN_CONTEXT_NOTIFICATION_BODY_KEY) : "", formLayout);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
		if (event.getCommand().equals(Event.DONE_EVENT.getCommand())) {
			addToRunContext(STEPS_RUN_CONTEXT_NOTIFICATION_SUBJECT_KEY, this.subjectTextElement.getValue());
			addToRunContext(STEPS_RUN_CONTEXT_NOTIFICATION_BODY_KEY, this.bodyTextElement.getValue());
		}
	}
}

/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.ta;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.olat.admin.quota.QuotaConstants;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.render.velocity.VelocityHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.FullAccessWithQuotaCallback;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  02.09.2004
 * @author Mike Stock
 */

public class DropboxController extends BasicController {
	
	private static final String LOGIN = "login";
	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";
	private static final String EMAIL = "email";
	private static final String FILENAME = "filename";
	private static final String DATE = "date";
	private static final String TIME = "time";
	private static final Collection<String> VARIABLE_NAMES = List.of(LOGIN, FIRST_NAME, LAST_NAME, EMAIL, FILENAME, DATE, TIME);
	
	public static Collection<String> variableNames() {
		return VARIABLE_NAMES;
	}

	public static final String DROPBOX_DIR_NAME = "dropboxes";
	// config
	
	protected ModuleConfiguration config;
	protected CourseNode node;
	protected UserCourseEnvironment userCourseEnv;
	private VelocityContainer myContent;
	private FileChooserController fileChooserController;
	private SubscriptionContext subsContext;
	private ContextualSubscriptionController contextualSubscriptionCtr;
	private Link ulButton;
	private CloseableModalController cmc;

	@Autowired
	private QuotaManager quotaManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private NotificationsManager notificationsManager;

	
	// Constructor for ProjectBrokerDropboxController
	protected DropboxController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		this.setBasePackage(DropboxController.class);
	}
	
	/**
	 * Implements a dropbox.
	 * @param ureq
	 * @param wControl
	 * @param config
	 * @param node
	 * @param userCourseEnv
	 * @param previewMode
	 */
	public DropboxController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config, CourseNode node, UserCourseEnvironment userCourseEnv, boolean previewMode) {
		super(ureq, wControl);
		this.setBasePackage(DropboxController.class);
		this.config = config;
		this.node = node;
		this.userCourseEnv = userCourseEnv;
		boolean hasNotification = userCourseEnv.isAdmin() || userCourseEnv.isCoach();
		init(ureq, wControl, previewMode, hasNotification);
	}
	
	protected void init(UserRequest ureq, WindowControl wControl, boolean previewMode, boolean hasNotification) {
		myContent = createVelocityContainer("dropbox");
		
		ulButton = LinkFactory.createButton("dropbox.upload", myContent, this);
		ulButton.setVisible(!userCourseEnv.isCourseReadOnly());
		
		if (!previewMode) {
			VFSContainer fDropbox = getDropBox(ureq.getIdentity());
			List<VFSItem> files = fDropbox.getItems(new VFSSystemItemFilter());
			int numFiles = files.size();
			if (numFiles > 0) {
				myContent.contextPut("numfiles", new String[] {Integer.toString(numFiles)});
				myContent.contextPut("filelist", files);
			}
			
		} else {
			myContent.contextPut("numfiles", "0");
		}
		myContent.contextPut("previewMode", previewMode ? Boolean.TRUE : Boolean.FALSE);

		// notification
		CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
		subsContext = DropboxFileUploadNotificationHandler.getSubscriptionContext(courseEnv, node);
		if ( hasNotification && !previewMode) {
			// offer subscription, but not to guests
			if (subsContext != null) {
				String path = getDropboxPathRelToFolderRoot(courseEnv, node);
				contextualSubscriptionCtr = AbstractTaskNotificationHandler.createContextualSubscriptionController(ureq, wControl, path, subsContext, DropboxController.class);
				myContent.put("subscription", contextualSubscriptionCtr.getInitialComponent());
				myContent.contextPut("hasNotification", Boolean.TRUE);
			}
		} else {
			myContent.contextPut("hasNotification", Boolean.FALSE);
		}

		putInitialPanel(myContent);
	}
	
	/**
	 * Dropbox path relative to folder root.
	 * @param courseEnv
	 * @param cNode
	 * @return Dropbox path relative to folder root.
	 */
	public static String getDropboxPathRelToFolderRoot(CourseEnvironment courseEnv, CourseNode cNode) {
		return courseEnv.getCourseBaseContainer().getRelPath() + File.separator + DROPBOX_DIR_NAME + File.separator + cNode.getIdent();
	}
	
	
	/**
	 * Get the dropbox of an identity.
	 * @param identity
	 * @return Dropbox of an identity
	 */
	protected VFSContainer getDropBox(Identity identity) {
		LocalFolderImpl dropBox = VFSManager.olatRootContainer(getRelativeDropBoxFilePath(identity), null);
		if (!dropBox.getBasefile().exists()) dropBox.getBasefile().mkdirs();
		return dropBox;
	}
	
	protected String getRelativeDropBoxFilePath(Identity identity) {
		return getDropboxPathRelToFolderRoot(userCourseEnv.getCourseEnvironment(), node) + File.separator + identity.getName();
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == ulButton) {
			
			removeAsListenerAndDispose(fileChooserController);
			fileChooserController = new FileChooserController(ureq, getWindowControl(), getUploadLimit());
			listenTo(fileChooserController);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), fileChooserController.getInitialComponent(), true, "Upload");
			listenTo(cmc);
			
			cmc.activate();
		}
	}

	/**
	 * Get upload limit for dropbox of a certain user. The upload can be limited 
	 * by available-folder space, max folder size or configurated upload-limit.
	 * @param ureq
	 * @return max upload limit in KB
	 */
	private int getUploadLimit() {
		String dropboxPath = getRelativeDropBoxFilePath(getIdentity());
		Quota dropboxQuota = quotaManager.getCustomQuota(dropboxPath);
		if (dropboxQuota == null) {
			dropboxQuota = quotaManager.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES);
		}
		VFSContainer rootFolder = VFSManager.olatRootContainer(getRelativeDropBoxFilePath(getIdentity()), null);
		VFSContainer dropboxContainer = new NamedContainerImpl(getIdentity().getName(), rootFolder);
		FullAccessWithQuotaCallback secCallback = new FullAccessWithQuotaCallback(dropboxQuota);
		rootFolder.setLocalSecurityCallback(secCallback);
		return quotaManager.getUploadLimitKB(dropboxQuota.getQuotaKB(),dropboxQuota.getUlLimitKB(),dropboxContainer);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == fileChooserController) {
			cmc.deactivate();
			if (event.equals(Event.DONE_EVENT)) {
				boolean success = false;
				File fIn = fileChooserController.getUploadFile();
				VFSContainer fDropbox = getDropBox(ureq.getIdentity());
				String filename = fileChooserController.getUploadFileName();

				VFSLeaf fOut;
				if (fDropbox.resolve(filename) != null) {
					//FIXME ms: check if dropbox quota is exceeded -> clarify with customers 
					fOut = fDropbox.createChildLeaf(getNewUniqueName(filename));
				} else {
					fOut = fDropbox.createChildLeaf(filename);
				}
				
				success = VFSManager.copyContent(fIn, fOut, getIdentity());
					
				if (success) {
					int numFiles = fDropbox.getItems(new VFSSystemItemFilter()).size();
					myContent.contextPut("numfiles", new String[] {Integer.toString(numFiles)});
					myContent.contextPut("filelist", fDropbox.getItems());
					// assemble confirmation
					String confirmation = getConfirmation(ureq, fOut.getName());
					// send email if necessary
					Boolean sendEmail = (Boolean)config.get(TACourseNode.CONF_DROPBOX_ENABLEMAIL);
					if (sendEmail == null) sendEmail = Boolean.FALSE;
					boolean sendMailError = false;
					if (sendEmail.booleanValue()) {
						//send mail
						MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
						MailBundle bundle = new MailBundle();
						bundle.setContext(context);
						bundle.setToId(ureq.getIdentity());
						bundle.setContent(translate("conf.mail.subject"), confirmation);
						MailerResult result = CoreSpringFactory.getImpl(MailManager.class).sendMessage(bundle);
						if(result.getFailedIdentites().size() > 0) {
							List<Identity> disabledIdentities = new ArrayList<>();
							disabledIdentities = result.getFailedIdentites();
							//show error that message can not be sent
							ArrayList<String> myButtons = new ArrayList<>();
							myButtons.add(translate("back"));
							String title = MailHelper.getTitleForFailedUsersError(ureq.getLocale());
							String message = MailHelper.getMessageForFailedUsersError(ureq.getLocale(), disabledIdentities);
							// add dropbox specific error message
							message += "\n<br />"+translate("conf.mail.error");
							//FIXME:FG:6.2: fix problem in info message, not here
							message += "\n<br />\n<br />"+confirmation.replace("\n", "&#10;").replace("\r", "&#10;").replace("\u2028", "&#10;");
							DialogBoxController noUsersErrorCtr = null;
							noUsersErrorCtr = activateGenericDialog(ureq, title, message, myButtons, noUsersErrorCtr);
							sendMailError = true;
						} else if(result.getReturnCode() > 0) {
							//show error that message can not be sent
							ArrayList<String> myButtons = new ArrayList<>();
							myButtons.add(translate("back"));
							DialogBoxController noUsersErrorCtr = null;
							String message = translate("conf.mail.error");
							//FIXME:FG:6.2: fix problem in info message, not here
							message += "\n<br />\n<br />"+confirmation.replace("\n", "&#10;").replace("\r", "&#10;").replace("\u2028", "&#10;");
							noUsersErrorCtr = activateGenericDialog(ureq, translate("error.header"), message, myButtons, noUsersErrorCtr);
							sendMailError = true;
						} 
					}

					// inform subscription manager about new element
					if (subsContext != null) {
						notificationsManager.markPublisherNews(subsContext, ureq.getIdentity(), true);
					}													
					// configuration is already translated, don't use showInfo(i18nKey)! 
					//FIXME:FG:6.2: fix problem in info message, not here
					if(!sendMailError) {
						getWindowControl().setInfo(confirmation.replace("\n", "&#10;").replace("\r", "&#10;").replace("\u2028", "&#10;"));
					}
				} else {
					showInfo("dropbox.upload.failed");
				}
			}
		}
	}
	
	private String getNewUniqueName(String name) {
    String body = null;
		String ext = null;
		int dot = name.lastIndexOf(".");
		if (dot != -1) {
			body = name.substring(0, dot);
			ext = name.substring(dot);
		} else {
			body = name;
			ext = "";
		}
		String tStamp = new SimpleDateFormat("yyMMdd-HHmmss").format(new Date());
		return body + "." + tStamp + ext;
	}
	
	private String getConfirmation(UserRequest ureq, String filename) {
		String confirmation = config.getStringValue(TACourseNode.CONF_DROPBOX_CONFIRMATION);
		
		Context c = new VelocityContext();
		Identity identity = ureq.getIdentity();
		c.put(LOGIN, identity.getName());
		c.put(FIRST_NAME, identity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()));
		c.put("first", identity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()));
		c.put(LAST_NAME, identity.getUser().getProperty(UserConstants.LASTNAME, getLocale()));
		c.put("last", identity.getUser().getProperty(UserConstants.LASTNAME, getLocale()));
		c.put(EMAIL, UserManager.getInstance().getUserDisplayEmail(identity, getLocale()));
		c.put(FILENAME, filename);
		Date now = new Date();
		Formatter f = Formatter.getInstance(ureq.getLocale());
		c.put(DATE, f.formatDate(now));
		c.put(TIME, f.formatTime(now));
		
		// update attempts counter for this user: one file - one attempts
		courseAssessmentService.incrementAttempts(node, userCourseEnv, Role.user);
				
		// log entry for this file
		UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
		am.appendToUserNodeLog(node, identity, identity, "FILE UPLOADED: " + filename, null);

		return VelocityHelper.getInstance().evaluateVTL(confirmation, c);
	}
	
	@Override
	protected void doDispose() {
		// DialogBoxController gets disposed by BasicController
		if (fileChooserController != null) {
			fileChooserController.dispose();
			fileChooserController = null;
		}
		if (contextualSubscriptionCtr != null) {
			contextualSubscriptionCtr.dispose();
			contextualSubscriptionCtr = null;
		}
        super.doDispose();
	}	
}

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
* <p>
*/ 

package org.olat.core.commons.modules.bc.commands;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementImpl;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * File Upload command class
 * 
 * <P>
 * Initial Date:  09.06.2006 <br>
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class CmdUpload extends BasicController implements FolderCommand {
	public static final Event FOLDERCOMMAND_CANCELED = new Event("fc_canceled");

	private int status = FolderCommandStatus.STATUS_SUCCESS;

	private VelocityContainer mainVC;
	private VFSContainer currentContainer;
	private VFSContainer inheritingContainer;
	private VFSSecurityCallback secCallback;

	private ProgressBar ubar;
	private String uploadFileName;
	private long quotaKB;
	private long uploadLimitKB;
	private boolean overwritten = false;
	private FileUploadController fileUploadCtr;
	private boolean cancelResetsForm;
	private boolean showMetadata = false;
	private boolean showCancel = true; // default is to show cancel button
	
	@Autowired
	private NotificationsManager notificationsManager;
	
	public CmdUpload(UserRequest ureq, WindowControl wControl, boolean showMetadata, boolean showCancel) {
		super(ureq, wControl, Util.createPackageTranslator(FileElementImpl.class, ureq.getLocale()));
		this.showMetadata = showMetadata;
		this.showCancel = showCancel;
	}

	protected CmdUpload(UserRequest ureq, WindowControl wControl, boolean showMetadata) {
		super(ureq, wControl);
		this.showMetadata = showMetadata;
	}

	@Override
	public Controller execute(FolderComponent fc, UserRequest ureq, WindowControl windowControl, Translator trans) {
		return execute(fc, ureq, trans, false);
	}

	public Controller execute(FolderComponent folderComponent, UserRequest ureq, Translator trans, boolean cancelResetsButton) {
		this.cancelResetsForm = cancelResetsButton;
		
		setTranslator(trans);
		currentContainer = folderComponent.getCurrentContainer();
		if (currentContainer.canWrite() != VFSConstants.YES)
			throw new AssertException("Cannot write to selected folder.");
		// mainVC is the main view
		
		mainVC = createVelocityContainer("upload");
		// Add progress bar
		ubar = new ProgressBar("ubar");
		ubar.setWidth(200);
		ubar.setUnitLabel("MB");
		ubar.setLabelAlignment(LabelAlignment.right);
		mainVC.put(ubar.getComponentName(), ubar);

		// Calculate quota and limits
		long actualUsage = 0;
		quotaKB = Quota.UNLIMITED;
		uploadLimitKB = Quota.UNLIMITED;
		
		inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(currentContainer);
		if (inheritingContainer != null) {
			secCallback = inheritingContainer.getLocalSecurityCallback();
			actualUsage = VFSManager.getUsageKB(inheritingContainer);
			ubar.setActual(actualUsage / 1024f);
			if (inheritingContainer.getLocalSecurityCallback().getQuota() != null) {
				quotaKB = secCallback.getQuota().getQuotaKB().longValue();
				uploadLimitKB = (int) secCallback.getQuota().getUlLimitKB().longValue();
			}
		}		
		// set wether we have a quota on this folder
		if (quotaKB == Quota.UNLIMITED) {
			ubar.setIsNoMax(true);
		} else if(quotaKB == 0) {
			ubar.setMax(quotaKB);
		} else {
			ubar.setMax(quotaKB / 1024f);
		}
		// set default ulLimit if none is defined...
		if (uploadLimitKB == Quota.UNLIMITED) {
			uploadLimitKB = CoreSpringFactory.getImpl(QuotaManager.class).getDefaultQuotaDependingOnRole(ureq.getIdentity(), ureq.getUserSession().getRoles()).getUlLimitKB().longValue();
		}
		
		// Add file upload form
		long remainingQuotaKB;
		if (quotaKB == Quota.UNLIMITED) remainingQuotaKB = quotaKB;
		else if (quotaKB - actualUsage < 0) remainingQuotaKB = 0;
		else remainingQuotaKB = quotaKB - actualUsage;
		
		removeAsListenerAndDispose(fileUploadCtr);
		fileUploadCtr = new FileUploadController(getWindowControl(), currentContainer, ureq, uploadLimitKB, remainingQuotaKB, null, false,
				true, showMetadata, true, showCancel, false);
		listenTo(fileUploadCtr);
		mainVC.put("fileUploadCtr", fileUploadCtr.getInitialComponent());
		mainVC.contextPut("showFieldset", Boolean.TRUE);

		//if folder full show error msg
		if (remainingQuotaKB == 0 ) {
			String supportAddr = WebappHelper.getMailConfig("mailQuota");
			String msg = translate("QuotaExceededSupport", new String[] { supportAddr });
			getWindowControl().setError(msg);
			return null;
		}
		
		putInitialPanel(mainVC);
		return this;
	}
	
	public void refreshActualFolderUsage(){
		long actualUsage = 0;
		quotaKB = Quota.UNLIMITED;
		uploadLimitKB = Quota.UNLIMITED;
		
		inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(currentContainer);
		if (inheritingContainer != null) {
			secCallback = inheritingContainer.getLocalSecurityCallback();
			actualUsage = VFSManager.getUsageKB(inheritingContainer);
			quotaKB = secCallback.getQuota().getQuotaKB().longValue();
			uploadLimitKB = (int)secCallback.getQuota().getUlLimitKB().longValue();
			ubar.setActual(actualUsage / 1024);
			fileUploadCtr.setMaxUploadSizeKB(uploadLimitKB);
		}
	}
	
	/**
	 * Call this to remove the fieldset
	 */
	public void hideFieldset() {
		if (mainVC == null) {
			throw new AssertException("Programming error - execute must be called before calling hideFieldset()");
		}
		mainVC.contextPut("showFieldset", Boolean.FALSE);
		if (fileUploadCtr != null) {
			fileUploadCtr.hideTitleAndFieldset();
		}
	}

	@Override
	public int getStatus() {
		return status;
	}
	@Override
	public boolean runsModal() {
		return false;
	}
	
	@Override
	public String getModalTitle() {
		return translate("ul");
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// no events to catch
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == fileUploadCtr) {
			// catch upload event
			if (event instanceof FolderEvent && event.getCommand().equals(FolderEvent.UPLOAD_EVENT)) {
				FolderEvent folderEvent = (FolderEvent) event;
				// Get file from temp folder location
				uploadFileName = folderEvent.getFilename();
				VFSItem vfsNewFile = currentContainer.resolve(uploadFileName);
				overwritten = fileUploadCtr.isExistingFileOverwritten();
				if (vfsNewFile != null) {
					notifyFinished(ureq);
				} else {
					showError("file.element.error.general");
				}
			} else if (event.equals(Event.CANCELLED_EVENT)) {
				if (cancelResetsForm) {
					fileUploadCtr.reset();
				} else {
					status = FolderCommandStatus.STATUS_CANCELED;
					fireEvent(ureq, FOLDERCOMMAND_FINISHED);			
				}
			}
		}
	}
	
	private void notifyFinished(UserRequest ureq) {		
		// After upload, notify the subscribers
		if (secCallback != null) {
			SubscriptionContext subsContext = secCallback.getSubscriptionContext();
			if (subsContext != null) {
				notificationsManager.markPublisherNews(subsContext, ureq.getIdentity(), true);
			}
		}
		// Notify everybody
		fireEvent(ureq, FOLDERCOMMAND_FINISHED);
	}

	/**
	 * Get the filename of the uploaded file or NULL if nothing uploaded
	 * 
	 * @return
	 */
	public String getFileName(){
		return uploadFileName;
	}
	
	public Boolean fileWasOverwritten(){
		return overwritten;
	}
}
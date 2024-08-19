/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.folder.ui;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import org.olat.core.commons.modules.bc.FolderLicenseHandler;
import org.olat.core.commons.services.folder.ui.event.FileBrowserSelectionEvent;
import org.olat.core.commons.services.folder.ui.event.FolderAddEvent;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.model.VFSTransientMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.CopySourceLeaf;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.NamedLeaf;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.VFSSuccess;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Aug 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FileBrowserCopyToController extends BasicController {
	
	private FileBrowserMainController fileBrowserCtrl;
	
	private final List<VFSItem> itemsToCopy;
	
	@Autowired
	private VFSLockManager vfsLockManager;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private FolderLicenseHandler licenseHandler;
	@Autowired
	private NotificationsManager notificationsManager;

	public FileBrowserCopyToController(UserRequest ureq, WindowControl wControl, List<VFSItem> itemsToCopy) {
		super(ureq, wControl);
		this.itemsToCopy = itemsToCopy;
		
		TooledStackedPanel stackedPanel = new TooledStackedPanel("fileHubBreadcrumb", getTranslator(), this);
		stackedPanel.setToolbarEnabled(false);
		putInitialPanel(stackedPanel);
		
		fileBrowserCtrl = new FileBrowserMainController(ureq, getWindowControl(), stackedPanel,
				FileBrowserSelectionMode.targetSingle, null, translate("copy"));
		listenTo(fileBrowserCtrl);
		stackedPanel.pushController(translate("browser.file.hub"), fileBrowserCtrl);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == fileBrowserCtrl) {
			if (event instanceof FileBrowserSelectionEvent selectionEvent) {
				Optional<VFSItem> targetContainer = selectionEvent.getVfsItems().stream()
						.filter(item -> item instanceof VFSContainer)
						.findFirst();
				if (targetContainer.isPresent()) {
					doCopy((VFSContainer)targetContainer.get());
					fireEvent(ureq, Event.DONE_EVENT);
				} else {
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	// Should do the same as FolderController.doCopyMove()
	private void doCopy(VFSContainer targetContainer) {
		if (VFSStatus.YES != targetContainer.canWrite()) {
			showWarning("error.copy.target.read.only");
			return;
		}
		
		for (VFSItem itemToCopy : itemsToCopy) {
			if (itemToCopy instanceof VFSContainer sourceContainer) {
				if (VFSManager.isContainerDescendantOrSelf(targetContainer, sourceContainer)) {
					showWarning("error.copy.overlapping");
					return;
				}
			}
			if (vfsLockManager.isLockedForMe(itemToCopy, getIdentity(), VFSLockApplicationType.vfs, null)) {
				showWarning("error.copy.locked");
				return;
			}
			if (itemToCopy.canCopy() != VFSStatus.YES) {
				showWarning("error.copy.other");
				return;
			}
		}
		
		FolderAddEvent addEvent = new FolderAddEvent();
		VFSSuccess vfsStatus = VFSSuccess.SUCCESS;
		ListIterator<VFSItem> listIterator = itemsToCopy.listIterator();
		while (listIterator.hasNext() && vfsStatus == VFSSuccess.SUCCESS) {
			VFSItem vfsItemToCopy = listIterator.next();
			// Paranoia: Check isItemNotAvailable and canEdit before every single file.
			if (vfsItemToCopy.exists() && VFSStatus.YES == vfsItemToCopy.canCopy()) {
				vfsStatus = isQuotaAvailable(targetContainer, vfsItemToCopy);
				if (vfsStatus == VFSSuccess.SUCCESS) {
					vfsItemToCopy = appendMissingLicense(vfsItemToCopy);
					VFSItem targetItem = targetContainer.resolve(vfsItemToCopy.getName());
					if (targetItem != null) {
						vfsItemToCopy = makeNameUnique(targetContainer, vfsItemToCopy);
					}
					vfsStatus = targetContainer.copyFrom(vfsItemToCopy, getIdentity());
				}
				if (vfsStatus == VFSSuccess.SUCCESS) {
					addEvent.addFilename(vfsItemToCopy.getName());
				}
			} else {
				vfsStatus = VFSSuccess.ERROR_FAILED;
			}
		}
		
		if (vfsStatus == VFSSuccess.ERROR_QUOTA_EXCEEDED) {
			showWarning("error.copy.quota.exceeded");
		} else if (vfsStatus == VFSSuccess.ERROR_QUOTA_ULIMIT_EXCEEDED) {
			showWarning("error.copy.quota.ulimit.exceeded");
		} else if (vfsStatus != VFSSuccess.SUCCESS) {
			showWarning("error.copy");
		} else if (addEvent.getFilenames().size() == 1) {
			showInfo("copy.success.single", addEvent.getFilenames().get(0));
		} else if (addEvent.getFilenames().size() > 1) {
			showInfo("copy.success.multi", String.valueOf(addEvent.getFilenames().size()));
		}
		
		markNews(targetContainer);
	}
	
	private VFSSuccess isQuotaAvailable(VFSContainer targetContainer, VFSItem vfsItemToCopy) {
		if (vfsItemToCopy instanceof VFSLeaf vfsLeaf) {
			long sizeKB = vfsLeaf.getSize() / 1024;
			long quotaULimitKB = VFSManager.getQuotaULimitKB(targetContainer);
			if (quotaULimitKB != Quota.UNLIMITED && quotaULimitKB < sizeKB) {
				return VFSSuccess.ERROR_QUOTA_ULIMIT_EXCEEDED;
			}
			long quotaLeft = VFSManager.getQuotaLeftKB(targetContainer);
			if (quotaLeft != Quota.UNLIMITED && quotaLeft < sizeKB) {
				return VFSSuccess.ERROR_QUOTA_EXCEEDED;
			}
		}
		return VFSSuccess.SUCCESS;
	}
	
	private VFSItem makeNameUnique(VFSContainer targetContainer, VFSItem vfsItem) {
		String nonExistingName = VFSManager.similarButNonExistingName(targetContainer, vfsItem.getName());
		if (vfsItem instanceof VFSContainer) {
			return new NamedContainerImpl(nonExistingName, (VFSContainer)vfsItem);
		}
		return new NamedLeaf(nonExistingName, (VFSLeaf)vfsItem);
	}
	
	private VFSItem appendMissingLicense(VFSItem vfsItem) {
		if (licenseModule.isEnabled(licenseHandler) && isLicenseMissing(vfsItem)) {
			VFSLeaf itemWithLicense = (VFSLeaf)vfsItem;
			VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
			if (vfsMetadata == null) {
				vfsMetadata = new VFSTransientMetadata();
				itemWithLicense = new CopySourceLeaf(itemWithLicense, vfsMetadata);
			}
			License license = licenseService.createDefaultLicense(licenseHandler, getIdentity());
			vfsMetadata.setLicenseType(license.getLicenseType());
			vfsMetadata.setLicenseTypeName(license.getLicenseType().getName());
			vfsMetadata.setLicensor(license.getLicensor());
			vfsMetadata.setLicenseText(LicenseUIFactory.getLicenseText(license));
			return itemWithLicense;
		}
		return vfsItem;
	}
	
	private boolean isLicenseMissing(VFSItem vfsItem) {
		if (vfsItem instanceof VFSLeaf && vfsItem.canMeta() == VFSStatus.YES) {
			VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
			if (vfsMetadata == null || !StringHelper.containsNonWhitespace(vfsMetadata.getLicenseTypeName())) {
				return true;
			}
		}
		return false;
	}
	
	private void markNews(VFSItem targetContainer) {
		VFSContainer container = VFSManager.findInheritingSecurityCallbackContainer(targetContainer);
		VFSSecurityCallback secCallback = container.getLocalSecurityCallback();
		if (secCallback != null) {
			SubscriptionContext subsContext = secCallback.getSubscriptionContext();
			if (subsContext != null) {
				notificationsManager.markPublisherNews(subsContext, getIdentity(), true);
			}
		}
	}

}

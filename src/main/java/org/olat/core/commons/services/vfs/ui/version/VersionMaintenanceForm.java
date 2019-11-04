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
package org.olat.core.commons.services.vfs.ui.version;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.olat.admin.SystemAdminMainController;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSMetadataRef;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.VFSVersionModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.async.ProgressDelegate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * This is a controller to configure the SimpleVersionConfig, the configuration
 * of the versioning system for briefcase.
 * 
 * <P>
 * Initial Date:  21 sept. 2009 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class VersionMaintenanceForm extends FormBasicController implements ProgressDelegate {
	
	private FormLink pruneLink;
	private FormLink cleanUpLink;
	private FormLink showOrphanLink;
	private StaticTextElement orphanSizeEl;
	private StaticTextElement versionsSizeEl;
	
	private CloseableModalController cmc;
	private ProgressController progressCtrl;
	private DialogBoxController confirmPruneHistoryBox;
	private DialogBoxController confirmDeleteOrphansBox;
	private VersionsDeletedFileController orphansController; 
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VFSVersionModule versionsModule;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public VersionMaintenanceForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		// use combined translator from system admin main
		setTranslator(Util.createPackageTranslator(SystemAdminMainController.class, ureq.getLocale(), getTranslator()));
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// First add title and context help
		setFormTitle("version.maintenance.title");
		setFormDescription("version.maintenance.intro");
		
		versionsSizeEl = uifactory.addStaticTextElement("version.size", "version.size", "", formLayout);
		orphanSizeEl = uifactory.addStaticTextElement("version.orphan.size", "version.orphan.size", "", formLayout);
		
		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsLayout);
		
		showOrphanLink = uifactory.addFormLink("version.show.orphans", buttonsLayout, Link.BUTTON);
		cleanUpLink = uifactory.addFormLink("version.clean.up", buttonsLayout, Link.BUTTON);
		pruneLink = uifactory.addFormLink("version.prune.history", buttonsLayout, Link.BUTTON);

		
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		long versionsSize = vfsRepositoryService.getRevisionsTotalSize();
		versionsSizeEl.setValue(Formatter.formatBytes(versionsSize));
		long versionsDeletedFiles = vfsRepositoryService.getRevisionsTotalSizeOfDeletedFiles();
		orphanSizeEl.setValue(Formatter.formatBytes(versionsDeletedFiles));
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == orphansController) {
			cmc.deactivate();
			cleanup();
		} else if(source == confirmDeleteOrphansBox) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doDeleteOrphans(ureq);
			}
		} else if(source == confirmPruneHistoryBox) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doPruneHistory(ureq);
			}
		} else if(source == cmc) {
			cleanup();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanup() {
		removeAsListenerAndDispose(orphansController);
		removeAsListenerAndDispose(cmc);
		orphansController = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == showOrphanLink) {
			doOpenOrphansList(ureq);
		} else if(source == cleanUpLink) {
			String text = translate("confirm.delete.orphans");
			confirmDeleteOrphansBox = activateYesNoDialog(ureq, null, text, confirmDeleteOrphansBox);
		} else if(source == pruneLink) {
			String text = translate("confirm.prune.history");
			confirmPruneHistoryBox = activateYesNoDialog(ureq, null, text, confirmPruneHistoryBox);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenOrphansList(UserRequest ureq) {
		if(guardModalController(orphansController)) return;
		
		orphansController = new VersionsDeletedFileController(ureq, getWindowControl());			
		listenTo(orphansController);
		
		String title = translate("version.show.orphans");
		cmc = new CloseableModalController(getWindowControl(), "close", orphansController.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeleteOrphans(UserRequest ureq) {
		final List<VFSMetadataRef> deleted = vfsRepositoryService.getMetadataOfDeletedFiles();
		
		progressCtrl = new ProgressController(ureq, getWindowControl());
		progressCtrl.setMessage(translate("version.clean.up"));
		progressCtrl.setPercentagesEnabled(false);
		progressCtrl.setUnitLabel("%");
		progressCtrl.setActual(0.0f);
		progressCtrl.setMax(100.0f);
		listenTo(progressCtrl);
		
		taskExecutorManager.execute(() -> {
			waitASecond();
			deletedDeletedFilesRevisions(deleted);
		});

		synchronized(this) {
			if(progressCtrl != null) {
				String title = translate("version.clean.up");
				cmc = new CloseableModalController(getWindowControl(), null, progressCtrl.getInitialComponent(), true, title, false);
				cmc.activate();
				listenTo(cmc);
			}
		}
	}
	
	private void deletedDeletedFilesRevisions(List<VFSMetadataRef> toDeleteList) {
		try {
			int count = 0;
			for(VFSMetadataRef toDelete:toDeleteList) {
				VFSMetadata meta = vfsRepositoryService.getMetadata(toDelete);
				vfsRepositoryService.deleteMetadata(meta);
				dbInstance.commitAndCloseSession();
				setActual((++count / (float)toDeleteList.size()) * 100.0f);
			}	
		} catch (Exception e) {
			dbInstance.closeSession();
			logError("", e);
		}
		finished();
	}
	
	private void doPruneHistory(UserRequest ureq) {
		final int numOfVersions = getNumOfVersions();
		final List<VFSMetadataRef> metadata = vfsRepositoryService.getMetadataWithMoreRevisionsThan(numOfVersions);
		progressCtrl = new ProgressController(ureq, getWindowControl());
		progressCtrl.setMessage(translate("version.prune.history"));
		progressCtrl.setPercentagesEnabled(false);
		progressCtrl.setUnitLabel("%");
		progressCtrl.setMax(100.0f);
		progressCtrl.setActual(0.0f);
		listenTo(progressCtrl);

		taskExecutorManager.execute(() -> {
			waitASecond();
			pruneRevisions(metadata, numOfVersions); 
		});

		synchronized(this) {
			if(progressCtrl != null) {
				String title = translate("version.prune.history");
				cmc = new CloseableModalController(getWindowControl(), null, progressCtrl.getInitialComponent(),
						true, title, false);
				cmc.activate();
				listenTo(cmc);
			}
		}
	}
	
	private void pruneRevisions(final List<VFSMetadataRef> metadata, final int numOfVersions) {
		try {
			final Identity actingIdentity = getIdentity();
			int count = 0;
			for(VFSMetadataRef data:metadata) {
				List<VFSRevision> revs = vfsRepositoryService.getRevisions(data);
				Collections.sort(revs, new AscendingRevisionNrComparator());
				List<VFSRevision> toDelete = revs.subList(0, revs.size() - numOfVersions);
				vfsRepositoryService.deleteRevisions(actingIdentity, toDelete);
				dbInstance.commitAndCloseSession();
				setActual((++count / (float)metadata.size()) * 100.0f);
			}
		} catch (Exception e) {
			dbInstance.closeSession();
			logError("", e);
		}
		finished();
	}
	
	private final void waitASecond() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logError("Can't wait", e);
		}
	}
	
	public int getNumOfVersions() {
		return versionsModule.getMaxNumberOfVersions();
	}
	
	public final void calculateOrphanSize() {
		long size = vfsRepositoryService.getRevisionsTotalSize();
		String sizeStr =Formatter.formatBytes(size);

		if(orphanSizeEl != null && !isDisposed()) {
			orphanSizeEl.setValue(sizeStr);
		}
	}
	
	@Override
	public void setMax(float max) {
		if(progressCtrl != null && !progressCtrl.isDisposed()) {
			progressCtrl.setMax(max);
		}
	}

	@Override
	public void setActual(float value) {
		if(progressCtrl != null && !progressCtrl.isDisposed()) {
			progressCtrl.setActual(value);
		}
	}

	@Override
	public void setInfo(String message) {
		if(progressCtrl != null && !progressCtrl.isDisposed()) {
			progressCtrl.setInfo(message);
		}
	}

	@Override
	public synchronized void finished() {
		if(cmc != null && !cmc.isDisposed()) {
			cmc.deactivate();
		}
		cleanup();
	}
	
	private static class AscendingRevisionNrComparator implements Comparator<VFSRevision> {
		@Override
		public int compare(VFSRevision o1, VFSRevision o2) {
			int n1 = o1.getRevisionNr();
			int n2 = o2.getRevisionNr();
			return Integer.compare(n1, n2);
		}
	}
}

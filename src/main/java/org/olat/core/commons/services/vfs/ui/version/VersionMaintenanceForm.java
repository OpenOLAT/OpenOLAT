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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.olat.admin.SystemAdminMainController;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
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
	
	private FormLink cleanUpLink, pruneLink, showOrphanLink, orphanSize;
	private StaticTextElement orphanSizeEl;
	private CloseableModalController cmc;
	private DialogBoxController confirmPrunehistoryBox;
	private DialogBoxController confirmDeleteOrphansBox;
	private ProgressController progressCtrl;
	
	@Autowired
	private VFSVersionModule versionsModule;
	@Autowired
	private VFSRepositoryService versionsManager;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	
	public VersionMaintenanceForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		// use combined translator from system admin main
		setTranslator(Util.createPackageTranslator(SystemAdminMainController.class, ureq.getLocale(), getTranslator()));
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// First add title and context help
		setFormTitle("version.maintenance.title");
		setFormDescription("version.maintenance.intro");

		orphanSizeEl = uifactory.addStaticTextElement("version.orphan.size", "version.orphan.size", "???", formLayout);
		
		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsLayout);
		
		orphanSize = uifactory.addFormLink("version.orphan.size.calc", buttonsLayout, Link.BUTTON);
		showOrphanLink = uifactory.addFormLink("version.show.orphans", buttonsLayout, Link.BUTTON);
		cleanUpLink = uifactory.addFormLink("version.clean.up", buttonsLayout, Link.BUTTON);
		
		FormLayoutContainer buttons2Layout = FormLayoutContainer.createButtonLayout("buttons2", getTranslator());
		formLayout.add(buttons2Layout);
		
		pruneLink = uifactory.addFormLink("version.prune.history", buttons2Layout, Link.BUTTON);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		/* if(source == orphansController) {
			cmc.deactivate();
			cleanup();
		} else */ if(source == confirmDeleteOrphansBox) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doDeleteOrphans(ureq);
			}
		} else if(source == confirmPrunehistoryBox) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doPruneHistory(ureq);
			}
		} else if(source == cmc) {
			cleanup();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanup() {
		removeAsListenerAndDispose(cmc);
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == showOrphanLink) {
			/*
			List<OrphanVersion> orphans = new ArrayList<>();//TODO metadata versions versionsManager.orphans();
			orphansController = new OrphanVersionsController(ureq, getWindowControl(), orphans);			
			listenTo(orphansController);
			cmc = new CloseableModalController(getWindowControl(), "close", orphansController.getInitialComponent());
			cmc.activate();
			*/
		} else if(source == cleanUpLink) {
			String text = translate("confirm.delete.orphans");
			confirmDeleteOrphansBox = activateYesNoDialog(ureq, null, text, confirmDeleteOrphansBox);
		} else if(source == pruneLink) {
			String text = translate("confirm.prune.history");
			confirmPrunehistoryBox = activateYesNoDialog(ureq, null, text, confirmPrunehistoryBox);
		} else if (source == orphanSize) {
			orphanSizeEl.setValue(translate("version.orphan.size.calculating"));
			taskExecutorManager.execute(new Runnable() {
				public void run() {
					calculateOrphanSize();
				}
			});
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doDeleteOrphans(UserRequest ureq) {
		progressCtrl = new ProgressController(ureq, getWindowControl());
		progressCtrl.setMessage(translate("version.clean.up"));
		progressCtrl.setPercentagesEnabled(false);
		progressCtrl.setUnitLabel("");
		progressCtrl.setActual(0.0f);
		progressCtrl.setMax(100.0f);
		listenTo(progressCtrl);
		
		taskExecutorManager.execute(new Runnable() {
			public void run() {
				waitASecond();
				//TODO metadata versions 
				//versionsManager.deleteOrphans(VersionMaintenanceForm.this);
			}
		});

		synchronized(this) {
			if(progressCtrl != null) {
				cmc = new CloseableModalController(getWindowControl(), null, progressCtrl.getInitialComponent(), true, null, false);
				cmc.activate();
				listenTo(cmc);
			}
		}
	}
	
	private void doPruneHistory(UserRequest ureq) {
		progressCtrl = new ProgressController(ureq, getWindowControl());
		progressCtrl.setMessage(translate("version.prune.history"));
		progressCtrl.setPercentagesEnabled(false);
		progressCtrl.setUnitLabel("");
		//TODO metadata versions 
		//progressCtrl.setMax(versionsManager.countDirectories());
		listenTo(progressCtrl);

		taskExecutorManager.execute(new Runnable() {
			public void run() {
				waitASecond();
				int numOfVersions = getNumOfVersions();
				//TODO metadata versions 
				// versionsManager.pruneHistory(numOfVersions, VersionMaintenanceForm.this);
			}
		});

		synchronized(this) {
			if(progressCtrl != null) {
				cmc = new CloseableModalController(getWindowControl(), null, progressCtrl.getInitialComponent(), true, null, false);
				cmc.activate();
				listenTo(cmc);
			}
		}
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
		long size = 0l;
		//TODO metadata versions 
		/*
		List<OrphanVersion> orphans = new ArrayList<> ();//versionsManager.orphans();
		for(OrphanVersion orphan:orphans) {
			List<VFSRevision> revisions = orphan.getVersions().getRevisions();
			if(revisions != null) {
				for(VFSRevision revision:revisions) {
					size += revision.getSize();
				}
			}
		}
		*/

		String unit = "KB";
		double humanSize = size / 1024.0d;
		if(humanSize > 1024) {
			humanSize /= 1024;
			unit = "MB";
		}

		DecimalFormat sizeFormat = new DecimalFormat("#0.#", new DecimalFormatSymbols(Locale.ENGLISH));
		String readableSize = sizeFormat.format(humanSize) + " " + unit;
		if(orphanSizeEl != null && !isDisposed()) {
			orphanSizeEl.setValue(readableSize);
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
}

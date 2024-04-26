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
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.vfs.VFSMetadataRef;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.VFSVersionModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressController;
import org.olat.core.gui.components.progressbar.ProgressDelegate;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * This is a controller to configure the SimpleVersionConfig, the configuration
 * of the versioning system for briefcase.
 * 
 * <P>
 * Initial Date: 21 sept. 2009 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AdminConfigurationController extends FormBasicController implements ProgressDelegate {

	private FormToggle enabledEl;
	private FormLayoutContainer numVersionCont;
	private StaticTextElement versionsSizeEl;
	private FormLink pruneLink;
	private FormLayoutContainer versCont;
	private SingleSelection numOfVersionsEl;
	private FormLayoutContainer licenseCont;
	private MultipleSelectionElement forceLicensCheckeEl;

	private DialogBoxController confirmPruneHistoryBox;
	private CloseableModalController cmc;
	private ProgressController progressCtrl;

	private String[] keys = new String[] { "2", "3", "4", "5", "10", "25", "50", "-1" };
	private String[] values = new String[] { "2", "3", "4", "5", "10", "25", "50", "-1" };

	@Autowired
	private DB dbInstance;
	@Autowired
	private VFSVersionModule versionsModule;
	@Autowired
	private FolderModule folderModule;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	public AdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(SystemAdminMainController.class, ureq.getLocale(), getTranslator()));

		values[values.length - 1] = getTranslator().translate("version.unlimited");

		initForm(ureq);
		updateVersioningUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer confCont = FormLayoutContainer.createDefaultFormLayout("conf", getTranslator());
		confCont.setRootForm(mainForm);
		formLayout.add(confCont);
		confCont.setFormTitle(translate("version.title"));
		confCont.setFormContextHelp("manual_admin/administration/Files_and_Folders/");

		enabledEl = uifactory.addToggleButton("version.enabled", "version.enabled", translate("on"), translate("off"),
				confCont);
		enabledEl.toggle(versionsModule.getMaxNumberOfVersions() != 0);
		enabledEl.addActionListener(FormEvent.ONCHANGE);

		numVersionCont = FormLayoutContainer.createButtonLayout("gradeCont", getTranslator());
		numVersionCont.setElementCssClass("o_inline_cont");
		numVersionCont.setLabel("version.size", null, true);
		numVersionCont.setRootForm(mainForm);
		confCont.add(numVersionCont);

		versionsSizeEl = uifactory.addStaticTextElement("version.size", null, "", numVersionCont);
		updateVersionsUI();

		pruneLink = uifactory.addFormLink("version.prune.history", numVersionCont, Link.BUTTON);
		pruneLink.setIconLeftCSS(CSSHelper.getIconCssClassFor(CSSHelper.CSS_CLASS_REVISION));

		versCont = FormLayoutContainer.createDefaultFormLayout("vers", getTranslator());
		versCont.setRootForm(mainForm);
		formLayout.add(versCont);
		versCont.setFormDescription(translate("version.intro"));

		numOfVersionsEl = uifactory.addDropdownSingleselect("version.numOfVersions", versCont, keys, values, null);
		numOfVersionsEl.addActionListener(FormEvent.ONCHANGE);
		int maxNumber = versionsModule.getMaxNumberOfVersions();
		if (maxNumber == -1) {
			numOfVersionsEl.select("-1", true); // unlimited
		} else {
			String str = Integer.toString(maxNumber);
			boolean found = false;
			for (String value : values) {
				if (value.equals(str)) {
					found = true;
					break;
				}
			}

			if (found) {
				numOfVersionsEl.select(str, true);
			} else {
				// set a default value if the saved number is not in the list,
				// normally not possible but...
				numOfVersionsEl.select("10", true);
			}
		}

		licenseCont = FormLayoutContainer.createDefaultFormLayout("license", getTranslator());
		licenseCont.setRootForm(mainForm);
		formLayout.add(licenseCont);
		licenseCont.setFormTitle(translate("license.header"));

		forceLicensCheckeEl = uifactory.addCheckboxesHorizontal("license.force", licenseCont, new String[] { "xx" },
				new String[] { translate("license.force.value") });
		forceLicensCheckeEl.addActionListener(FormEvent.ONCHANGE);
		forceLicensCheckeEl.select(forceLicensCheckeEl.getKey(0), folderModule.isForceLicenseCheck());
	}

	private void updateVersionsUI() {
		long versionsSize = vfsRepositoryService.getRevisionsTotalSize();
		versionsSizeEl.setValue(Formatter.formatBytes(versionsSize));
	}

	private void updateVersioningUI() {
		boolean enabled = enabledEl.isOn();
		numVersionCont.setVisible(enabled);
		versCont.setVisible(enabled);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl) {
			saveVersioning();
			updateVersioningUI();
		} else if (source == numOfVersionsEl) {
			saveVersioning();
		} else if (source == forceLicensCheckeEl) {
			saveLicense();
		} else if (source == pruneLink) {
			String text = translate("confirm.prune.history");
			confirmPruneHistoryBox = activateYesNoDialog(ureq, null, text, confirmPruneHistoryBox);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmPruneHistoryBox) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doPruneHistory(ureq);
			}
		} else if (source == cmc) {
			cleanup();
		}
		super.event(ureq, source, event);
	}

	private void cleanup() {
		removeAsListenerAndDispose(cmc);
		cmc = null;
		updateVersionsUI();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void saveVersioning() {
		if (enabledEl.isOn()) {
			try {
				String num = numOfVersionsEl.getSelectedKey();
				int maxNumber = Integer.parseInt(num);
				versionsModule.setMaxNumberOfVersions(maxNumber);
			} catch (NumberFormatException e) {
				showError("version.notANumber");
			}
		} else {
			// Disabled
			versionsModule.setMaxNumberOfVersions(0);
		}
	}

	private void saveLicense() {
		folderModule.setForceLicense(forceLicensCheckeEl.isAtLeastSelected(1));
	}

	private void doPruneHistory(UserRequest ureq) {
		final int numOfVersions = versionsModule.getMaxNumberOfVersions();
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

		synchronized (this) {
			if (progressCtrl != null) {
				String title = translate("version.prune.history");
				cmc = new CloseableModalController(getWindowControl(), null, progressCtrl.getInitialComponent(), true,
						title, false);
				cmc.activate();
				listenTo(cmc);
			}
		}
	}

	private void pruneRevisions(final List<VFSMetadataRef> metadata, final int numOfVersions) {
		try {
			final Identity actingIdentity = getIdentity();
			int count = 0;
			for (VFSMetadataRef data : metadata) {
				List<VFSRevision> revs = vfsRepositoryService.getRevisions(data);
				Collections.sort(revs, new AscendingRevisionNrComparator());
				List<VFSRevision> toDelete = revs.subList(0, revs.size() - numOfVersions);
				vfsRepositoryService.deleteRevisions(actingIdentity, toDelete);
				dbInstance.commitAndCloseSession();
				setActual((++count / (float) metadata.size()) * 100.0f);
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

	@Override
	public void setMax(float max) {
		if (progressCtrl != null && !progressCtrl.isDisposed()) {
			progressCtrl.setMax(max);
		}
	}

	@Override
	public void setActual(float value) {
		if (progressCtrl != null && !progressCtrl.isDisposed()) {
			progressCtrl.setActual(value);
		}
	}

	@Override
	public void setInfo(String message) {
		if (progressCtrl != null && !progressCtrl.isDisposed()) {
			progressCtrl.setInfo(message);
		}
	}

	@Override
	public synchronized void finished() {
		if (cmc != null && !cmc.isDisposed()) {
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

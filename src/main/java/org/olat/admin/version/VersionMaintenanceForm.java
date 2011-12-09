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
package org.olat.admin.version;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import org.olat.admin.SystemAdminMainController;
import org.olat.core.commons.taskExecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.version.OrphanVersion;
import org.olat.core.util.vfs.version.VFSRevision;
import org.olat.core.util.vfs.version.VersionsManager;

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
 //fxdiff FXOLAT-127: file versions maintenance tool
public class VersionMaintenanceForm extends FormBasicController {
	
	private FormLink cleanUp, orphanSize;
	private StaticTextElement orphanSizeEl;
	private CloseableModalController cmc;
	private OrphanVersionsController orphansController;
	
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
		setFormContextHelp(VersionMaintenanceForm.class.getPackage().getName(), "maintenance.html", "help.hover.version");
		
		orphanSizeEl = uifactory.addStaticTextElement("version.orphan.size", "version.orphan.size", "???", formLayout);
		
		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsLayout);
		
		orphanSize = uifactory.addFormLink("version.orphan.size.calc", buttonsLayout, Link.BUTTON);
		cleanUp = uifactory.addFormLink("version.clean.up", buttonsLayout, Link.BUTTON);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == cleanUp) {
			List<OrphanVersion> orphans = VersionsManager.getInstance().orphans();
			orphansController = new OrphanVersionsController(ureq, getWindowControl(), orphans);			
			listenTo(orphansController);
			cmc = new CloseableModalController(getWindowControl(), "close", orphansController.getInitialComponent());
			cmc.insertHeaderCss();
			cmc.activate();
		} else if (source == orphanSize) {
			orphanSizeEl.setValue(translate("version.orphan.size.calculating"));
			TaskExecutorManager.getInstance().runTask(new Runnable() {
				public void run() {
					calculateOrphanSize();
				}
			});
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	public final void calculateOrphanSize() {
		long size = 0l;
		List<OrphanVersion> orphans = VersionsManager.getInstance().orphans();
		for(OrphanVersion orphan:orphans) {
			List<VFSRevision> revisions = orphan.getVersions().getRevisions();
			if(revisions != null) {
				for(VFSRevision revision:revisions) {
					size += revision.getSize();
				}
			}
		}

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
}

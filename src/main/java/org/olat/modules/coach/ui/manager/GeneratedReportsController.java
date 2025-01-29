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
package org.olat.modules.coach.ui.manager;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.folder.ui.FileBrowserCopyToController;
import org.olat.core.commons.services.folder.ui.FolderUIFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.GeneratedReport;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2025-01-27<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GeneratedReportsController extends FormBasicController implements FlexiTableComponentDelegate, Activateable2 {

	private static final String DELETE_CMD = "delete";
	private static final String DOWNLOAD_CMD = "download";
	private static final String COPY_TO_CMD = "copy.to";
	
	private GeneratedReportsDataModel tableModel;
	private FlexiTableElement tableEl;
	private int count = 0;

	private DialogBoxController confirmDeleteCtrl;
	private FileBrowserCopyToController copyToCtrl;
	private CloseableModalController cmc;

	@Autowired
	private CoachingService coachingService;
			
	public GeneratedReportsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "generated_reports", Util.createPackageTranslator(GeneratedReportsController.class, 
				ureq.getLocale(), Util.createPackageTranslator(FolderUIFactory.class, ureq.getLocale())));
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GeneratedReportsDataModel.GeneratedReportsCols.name));
		
		tableModel = new GeneratedReportsDataModel(columnModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "list",
				tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setElementCssClass("o_sel_coach_generated_reports");
		VelocityContainer row = new VelocityContainer(null, "vc_row_", 
				Util.getPackageVelocityRoot(GeneratedReportsController.class) + "/generated_report_row.html",
				getTranslator(), this);
		row.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(row, this);
	}
	
	private void loadModel() {
		List<String> componentNamesToCleanUp = flc.getFormComponents().values().stream()
				.filter(formItem -> formItem.getUserObject() instanceof GeneratedReportsRow)
				.map(FormItem::getName).toList();
		for (String componentName : componentNamesToCleanUp) {
			flc.remove(componentName);
		}
		List<GeneratedReportsRow> rows = coachingService.getGeneratedReports(getIdentity()).stream()
				.map(this::mapToRow).toList();
		tableModel.setObjects(rows);
		tableEl.reset();
	}

	private GeneratedReportsRow mapToRow(GeneratedReport generatedReport) {
		GeneratedReportsRow row = new GeneratedReportsRow();
		row.setGeneratedReport(generatedReport);
		VFSMetadata metadata = generatedReport.getMetadata();
		row.setCreationDate(metadata.getCreationDate());
		row.setExpirationDate(metadata.getExpirationDate());
		row.setName(metadata.getTitle());
		row.setFileSize(metadata.getFileSize());
		forgeRow(row);
		return row;
	}

	private void forgeRow(GeneratedReportsRow row) {
		String c = Integer.toString(++count);
		
		String name = row.getName();
		FormLink downloadLink = uifactory.addFormLink("download-link-".concat(c), DOWNLOAD_CMD, name, 
				null, flc, Link.NONTRANSLATED);
		row.setDownloadLink(downloadLink);
		downloadLink.setUserObject(row);

		FormLink copyToButton = uifactory.addFormLink("copy-to-".concat(c), COPY_TO_CMD, "browser.copy.to", 
				null, flc, Link.BUTTON);
		copyToButton.setIconLeftCSS("o_icon o_icon-fw o_icon_copy");
		row.setCopyToButton(copyToButton);
		copyToButton.setUserObject(row);

		FormLink deleteButton = uifactory.addFormLink("delete-".concat(c), DELETE_CMD, "delete", 
				null, flc, Link.BUTTON);
		deleteButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		row.setDeleteButton(deleteButton);
		deleteButton.setUserObject(row);

		FormLink downloadButton = uifactory.addFormLink("download-".concat(c), DOWNLOAD_CMD, "download", 
				null, flc, Link.BUTTON);
		downloadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		row.setDownloadButton(downloadButton);
		downloadButton.setUserObject(row);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (confirmDeleteCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) && confirmDeleteCtrl.getUserObject() instanceof GeneratedReportsRow row) {
				doDelete(ureq, row);
			}
		} else if (copyToCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(copyToCtrl);
		removeAsListenerAndDispose(cmc);
		copyToCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if (DELETE_CMD.equals(cmd) && link.getUserObject() instanceof GeneratedReportsRow row) {
				doConfirmDelete(ureq, row);
			} else if (DOWNLOAD_CMD.equals(cmd) && link.getUserObject() instanceof GeneratedReportsRow row) {
				doDownload(ureq, row);
			} else if (COPY_TO_CMD.equals(cmd) && link.getUserObject() instanceof GeneratedReportsRow row) {
				doCopyTo(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doCopyTo(UserRequest ureq, GeneratedReportsRow row) {
		if (guardModalController(copyToCtrl)) {
			return;
		}

		VFSLeaf leaf = coachingService.getGeneratedReportLeaf(getIdentity(), row.getGeneratedReport().getMetadata());
		copyToCtrl = new FileBrowserCopyToController(ureq, getWindowControl(), List.of(leaf));
		listenTo(copyToCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), 
				copyToCtrl.getInitialComponent(), true, translate("browser.copy.to"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmDelete(UserRequest ureq, GeneratedReportsRow row) {
		String title = translate("delete.report.confirm.title");
		String text = translate("delete.report.confirm.text");
		confirmDeleteCtrl = activateYesNoDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(row);
	}

	private void doDownload(UserRequest ureq, GeneratedReportsRow row) {
		MediaResource mediaResource;
		VFSLeaf leaf = coachingService.getGeneratedReportLeaf(getIdentity(), row.getGeneratedReport().getMetadata());
		if (leaf != null) {
			mediaResource = new VFSMediaResource(leaf);			
		} else {
			mediaResource = new NotFoundMediaResource();
		}
		ureq.getDispatchResult().setResultingMediaResource(mediaResource);		
	}

	private void doDelete(UserRequest ureq, GeneratedReportsRow row) {
		coachingService.deleteGeneratedReport(getIdentity(), row.getGeneratedReport().getMetadata());
		reload();
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = null;
		if (rowObject instanceof GeneratedReportsRow generatedReportsRow) {
			components = new ArrayList<>();
			if (generatedReportsRow.getDownloadLink() != null) {
				components.add(generatedReportsRow.getDownloadLink().getComponent());
			}
			if (generatedReportsRow.getCopyToButton() != null) {
				components.add(generatedReportsRow.getCopyToButton().getComponent());
			}
			if (generatedReportsRow.getDeleteButton() != null) {
				components.add(generatedReportsRow.getDeleteButton().getComponent());
			}
			if (generatedReportsRow.getDownloadButton() != null) {
				components.add(generatedReportsRow.getDownloadButton().getComponent());
			}
		}
		return components;
	}

	public void reload() {
		loadModel();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
}

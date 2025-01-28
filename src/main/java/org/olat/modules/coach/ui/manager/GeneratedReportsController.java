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

import org.olat.core.commons.services.folder.ui.FolderUIFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.GeneratedReport;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2025-01-27<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GeneratedReportsController extends FormBasicController implements FlexiTableComponentDelegate, Activateable2 {

	private GeneratedReportsDataModel tableModel;
	private FlexiTableElement tableEl;
	private int count = 0;

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
		List<GeneratedReportsRow> rows = coachingService.getGeneratedReports(getIdentity()).stream()
				.map(this::mapToRow).toList();
		tableModel.setObjects(rows);
		tableEl.reset();
	}

	private GeneratedReportsRow mapToRow(GeneratedReport generatedReport) {
		GeneratedReportsRow row = new GeneratedReportsRow();
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
		FormLink downloadLink = uifactory.addFormLink("download-link-".concat(c), "download", name, 
				null, flc, Link.NONTRANSLATED);
		row.setDownloadLink(downloadLink);
		downloadLink.setUserObject(row);

		FormLink copyToButton = uifactory.addFormLink("copy-to-".concat(c), "copy.to", "browser.copy.to", 
				null, flc, Link.BUTTON);
		copyToButton.setIconLeftCSS("o_icon o_icon-fw o_icon_copy");
		row.setCopyToButton(copyToButton);
		copyToButton.setUserObject(row);

		FormLink deleteButton = uifactory.addFormLink("delete-".concat(c), "delete", "delete", 
				null, flc, Link.BUTTON);
		deleteButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		row.setDeleteButton(deleteButton);
		deleteButton.setUserObject(row);

		FormLink downloadButton = uifactory.addFormLink("download-".concat(c), "download", "download", 
				null, flc, Link.BUTTON);
		downloadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		row.setDownloadButton(downloadButton);
		downloadButton.setUserObject(row);
	}

	@Override
	protected void formOK(UserRequest ureq) {

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

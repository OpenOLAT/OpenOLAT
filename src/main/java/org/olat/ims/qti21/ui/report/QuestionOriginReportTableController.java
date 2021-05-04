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
package org.olat.ims.qti21.ui.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.repository.ui.RepositoryEntryACColumnDescriptor;
import org.olat.repository.ui.RepositoryFlexiTableModel;
import org.olat.repository.ui.RepositoryFlexiTableModel.RepoCols;
import org.olat.repository.ui.author.TypeRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionOriginReportTableController extends FormBasicController {
	
	private FormLink generateReportButton;
	
	private FlexiTableElement tableEl;
	private RepositoryFlexiTableModel tableModel;
	
	@Autowired
	private RepositoryManager repositoryManager;
	
	public QuestionOriginReportTableController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "report_list", Util.createPackageTranslator(AssessmentTestDisplayController.class, ureq.getLocale(),
				Util.createPackageTranslator(RepositoryService.class, ureq.getLocale())));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.ac, new RepositoryEntryACColumnDescriptor()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.repoEntry, new TypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.externalId));// visible if managed
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.displayname));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.author));

		tableModel = new RepositoryFlexiTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "curriculum-element-resource-list");
		tableEl.setEmptyTableMessageKey("search.empty");
		
		generateReportButton = uifactory.addFormLink("report.question.to.course", "report.question.to.course", null, formLayout, Link.BUTTON);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	protected void loadModel(UserRequest ureq, String searchString, String author) {
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
		params.addResourceTypes(ImsQTI21Resource.TYPE_NAME);
		params.setIdentity(getIdentity());
		params.setRoles(ureq.getUserSession().getRoles());
		params.setIdRefsAndTitle(searchString);
		params.setAuthor(author);
		
		List<RepositoryEntry> entries = repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, true);
		tableModel.setObjects(entries);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(generateReportButton == source) {
			doReport(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doReport(UserRequest ureq) {
		List<RepositoryEntry> entries = getSelectedEntries();
		if(entries.isEmpty()) {
			showWarning("warning.at.least.one.test");
		} else {
			String filename = "Questions_" + Formatter.formatDatetimeWithMinutes(ureq.getRequestTimestamp());
			filename = StringHelper.transformDisplayNameToFileSystemName(filename);
			MediaResource report = new QuestionOriginMediaResource(filename, entries, getTranslator());
			ureq.getDispatchResult().setResultingMediaResource(report);
		}
	}
	
	private List<RepositoryEntry> getSelectedEntries() {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<RepositoryEntry> selectedEntries = new ArrayList<>(selectedIndexes.size());
		for(Integer selectedIndex:selectedIndexes) {
			RepositoryEntry entry = tableModel.getObject(selectedIndex.intValue());
			selectedEntries.add(entry);
		}
		return selectedEntries;
	}
}

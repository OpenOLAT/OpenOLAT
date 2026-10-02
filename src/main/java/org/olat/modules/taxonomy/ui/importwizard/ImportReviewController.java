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
package org.olat.modules.taxonomy.ui.importwizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.modules.taxonomy.manager.importexport.FileRow.Status;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.manager.importexport.FileRow;
import org.olat.modules.taxonomy.ui.importwizard.ImportReviewTableModel.ReviewCols;

/**
 * Review of the level types or of the levels: status and messages per row.
 * 
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class ImportReviewController extends StepFormBasicController {

	private static final String CMD_CHANGES = "changes";

	private int counter = 0;
	private final boolean levels;
	private final String descriptionKey;
	private final TaxonomyImportContext context;

	private ImportChangesController changesCtrl;
	private CloseableCalloutWindowController calloutCtrl;

	public ImportReviewController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, TaxonomyImportContext context, boolean levels) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(TaxonomyImportWizard.getTranslator(ureq));
		this.context = context;
		this.levels = levels;
		this.descriptionKey = levels ? "import.wizard.levels.desc" : "import.wizard.types.desc";
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription(descriptionKey);
		if(!levels) {
			// warnings of the file itself, e.g. an other format version
			String warnings = context.getData().getMessages().stream()
					.filter(message -> !message.isError())
					.map(message -> translate(message.i18nKey(), message.args()))
					.collect(Collectors.joining("<br>"));
			if(StringHelper.containsNonWhitespace(warnings)) {
				setFormWarning("noTransOnlyParam", new String[] { warnings });
			}
		}

		ImportReviewCellRenderer renderer = new ImportReviewCellRenderer();
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.row));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.status, renderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.messages, renderer));
		if(levels) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.path));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.title));
		if(levels) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.type));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.sortOrder));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.images));
		} else {
			DefaultFlexiColumnModel subTypesCol = new DefaultFlexiColumnModel(ReviewCols.type);
			subTypesCol.setHeaderLabel(translate("importexport.type.sub.types"));
			columnsModel.addFlexiColumnModel(subTypesCol);
		}

		List<FileRow> rows = new ArrayList<>(levels ? context.getLevels() : context.getLevelTypes());
		Map<FileRow,FormLink> changeLinks = new HashMap<>();
		ImportReviewTableModel model = new ImportReviewTableModel(columnsModel, getLocale(), changeLinks);
		model.setObjects(rows);

		FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 25, false, getTranslator(), formLayout);
		// changed rows: the status opens a callout with the changed values
		for(FileRow row:rows) {
			if(row.getStatus() == Status.changed && !row.getChanges().isEmpty()) {
				String text = "<span class='o_nowrap'><i class='o_icon o_icon-fw o_icon_edit'> </i> "
						+ translate("importexport.status.changed") + " (" + row.getChanges().size() + ")</span>";
				FormLink link = uifactory.addFormLink("changes_" + (++counter), CMD_CHANGES, text, tableEl, Link.LINK | Link.NONTRANSLATED);
				link.setTitle(translate("importexport.change.show"));
				link.setUserObject(row);
				changeLinks.put(row, link);
			}
		}
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(true);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey(levels ? "table.taxonomy.level.empty" : "table.taxonomy.level.type.empty")
				.build());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink link && CMD_CHANGES.equals(link.getCmd()) && link.getUserObject() instanceof FileRow row) {
			doOpenChanges(ureq, link, row);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(changesCtrl);
		calloutCtrl = null;
		changesCtrl = null;
	}

	private void doOpenChanges(UserRequest ureq, FormLink link, FileRow row) {
		cleanUp();
		changesCtrl = new ImportChangesController(ureq, getWindowControl(), row);
		listenTo(changesCtrl);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), changesCtrl.getInitialComponent(),
				link.getFormDispatchId(), "", true, "", new CalloutSettings(true, CalloutOrientation.bottom, false, "", true));
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}

	@Override
	protected void formNext(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formFinish(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}

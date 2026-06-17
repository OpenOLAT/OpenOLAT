/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.application;

import static org.olat.modules.selectus.ui.RecruitingHelper.normalizeFilename;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.SelectusReviewService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.model.review.ReviewElementType;
import org.olat.modules.selectus.ui.PositionApplicationsDataModel;
import org.olat.modules.selectus.ui.PositionApplicationsDataModel.Fields;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.model.ApplicationRow;
import org.olat.modules.selectus.ui.resources.GeneratedListExcelResource;
import org.olat.modules.selectus.ui.resources.GeneratedListExcelResource.ListSettings;

/**
 * 
 * Initial date: 22 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GenerateApplicationListController extends FormBasicController {
	
	private MultipleSelectionElement columnsEl;
	
	private final FlexiTableElement tableEl;
	private final PositionApplicationsDataModel applicationsDataModel;
	
	private final Position position;
	private final List<ApplicationRow> applications;
	private final FlexiTableColumnModel columnsModel;

	private final boolean hasReviewTexts;
	private final boolean hasReviewSliders;
	private final boolean hasReviewDiscussions;
	private List<ReviewElementDefinition> reviewElementDefinitions;
	private final RecruitingPositionSecurityCallback secCallback;

	@Autowired
	private SelectusReviewService reviewService;
	
	public GenerateApplicationListController(UserRequest ureq, WindowControl wControl,
			Position position, List<ApplicationRow> applications, FlexiTableElement tableEl,
			PositionApplicationsDataModel applicationsDataModel, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, "generate_list", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.tableEl = tableEl;
		this.position = position;
		this.secCallback = secCallback;
		this.applications = applications;
		this.applicationsDataModel = applicationsDataModel.createCopyWithEmptyList();

		FlexiTableColumnModel uiColumnsModel = applicationsDataModel.getTableColumnModel();
		columnsModel = getExportColumnsModel(uiColumnsModel);
		
		if(secCallback.canExcelReviewStatistics()) {
			PositionReviewDefinition reviewDefinition = position.getReviewDefinition();
			hasReviewDiscussions = reviewDefinition.isReviewCommentEnabled();
			reviewDefinition = reviewService.getReviewDefinition(reviewDefinition);
			reviewElementDefinitions = reviewDefinition.getElements().stream()
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			hasReviewSliders = reviewElementDefinitions.stream()
					.anyMatch(def -> def.getType() == ReviewElementType.slider);
			hasReviewTexts = reviewElementDefinitions.stream()
					.anyMatch(def -> def.getType() == ReviewElementType.text);
		} else {
			hasReviewTexts = false;
			hasReviewSliders = false;
			hasReviewDiscussions = false;
		}
		initForm(ureq);
	}
	
	private FlexiTableColumnModel getExportColumnsModel(FlexiTableColumnModel uiColumnsModel) {
		FlexiTableColumnModel model = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		for(int i=0; i<uiColumnsModel.getColumnCount(); i++) {
			FlexiColumnModel column = uiColumnsModel.getColumnModel(i);
			if("edit".equals(column.getAction()) || "delete".equals(column.getAction())
					|| "new-window".equals(column.getAction()) || "open.new.window".equals(column.getHeaderKey())
					|| column.getColumnIndex() == Fields.myRating.ordinal()
					|| column.getColumnIndex() == Fields.notes.ordinal()
					|| column.getColumnIndex() == Fields.reviewButton.ordinal()) {
				continue;
			}
			model.addFlexiColumnModel(column);
		}
		return model;
	}
	
	public List<ApplicationRow> getApplications() {
		return applications;
	}
	
	public MediaResource getMediaResource() {
		Collection<String> selectedColumnIndex = columnsEl.getSelectedKeys();
		List<Integer> exportedColumnIndex = new ArrayList<>(selectedColumnIndex.size());

		int numOfColumns = columnsModel.getColumnCount();
		for(int i=0; i<numOfColumns; i++) {
			FlexiColumnModel column = columnsModel.getColumnModel(i);
			if(selectedColumnIndex.contains(Integer.toString(column.getColumnIndex()))) {
				exportedColumnIndex.add(Integer.valueOf(column.getColumnIndex()));
			}
		}

		applicationsDataModel.setObjects(applications);
		
		int[] exportedColumnIndexArr = exportedColumnIndex.stream()
				.mapToInt(Integer::intValue)
				.toArray();
		applicationsDataModel.setExportColumnIndex(exportedColumnIndexArr);

		try {
			String derivedFilename = RecruitingHelper.getPositionDerivedFilename(position, getLocale());
			String filename = normalizeFilename(derivedFilename) + "_applications.xlsx";
			ListSettings settings = new ListSettings();
			settings.setWithReviewSliders(selectedColumnIndex.contains("rev-sliders"));
			settings.setWithReviewTexts(selectedColumnIndex.contains("rev-texts"));
			settings.setWithReviewDiscussions(selectedColumnIndex.contains("rev-discussions"));
			return new GeneratedListExcelResource(filename, position, applicationsDataModel,
					getIdentity(), secCallback, settings, getTranslator());
		} catch (Exception e) {
			logError("", e);
			return null;
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<String> selectedColumns = new ArrayList<>();
		SelectionValues columnKeyValues = new SelectionValues();
		
		int numOfColumns = columnsModel.getColumnCount();
		for(int i=0; i<numOfColumns; i++) {
			FlexiColumnModel column = columnsModel.getColumnModel(i);
			String columnIndex = Integer.toString(column.getColumnIndex());
			String label;
			if(StringHelper.containsNonWhitespace(column.getHeaderLabel())) {
				label = column.getHeaderLabel();
			} else {
				label = translate(column.getHeaderKey());
			}
			columnKeyValues.add(SelectionValues.entry(columnIndex, label));
			if(tableEl.isColumnModelVisible(column)) {
				selectedColumns.add(columnIndex);
			}
		}
		
		if(hasReviewSliders) {
			columnKeyValues.add(SelectionValues.entry("rev-sliders", translate("review.sliders")));
		}
		if(hasReviewTexts) {
			columnKeyValues.add(SelectionValues.entry("rev-texts", translate("review.texts")));
		}
		if(hasReviewDiscussions) {
			columnKeyValues.add(SelectionValues.entry("rev-discussions", translate("review.discussions")));
		}
		
		columnsEl = uifactory.addCheckboxesVertical("generate.application.list.columns", formLayout,
				columnKeyValues.keys(), columnKeyValues.values(), 1);
		columnsEl.setLabel(null, null);
		for(String selectedColumn:selectedColumns) {
			columnsEl.select(selectedColumn, true);
		}

		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}

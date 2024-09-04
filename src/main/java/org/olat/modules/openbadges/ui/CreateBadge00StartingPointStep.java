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
package org.olat.modules.openbadges.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.ui.wizard.BadgesRow;
import org.olat.modules.openbadges.ui.wizard.BadgesTableModel;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-08-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadge00StartingPointStep extends BasicStep {

	private final CreateBadgeClassWizardContext createBadgeClassContext;

	public CreateBadge00StartingPointStep(UserRequest ureq, CreateBadgeClassWizardContext createBadgeClassContext) {
		super(ureq);
		this.createBadgeClassContext = createBadgeClassContext;
		setI18nTitleAndDescr("form.starting.point", null);
		if (createBadgeClassContext.isStartFromScratch()) {
			setNextStep(new CreateBadge00ImageStep(ureq, createBadgeClassContext));
		} else {
			setNextStep(new CreateBadge02DetailsStep(ureq, createBadgeClassContext));
		}
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		runContext.put(CreateBadgeClassWizardContext.KEY, createBadgeClassContext);
		form.setMultipartEnabled(true);
		return new CreateBadge00StartingPointForm(ureq, wControl, form, runContext);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.NEXT;
	}

	private class CreateBadge00StartingPointForm extends StepFormBasicController {
		private static final String FILTER_BADGE_TITLE = "badgeTitle";
		private static final String FILTER_COURSE_NAME = "courseName";
		private static final String FILTER_COURSE_REFERENCE = "courseReference";

		private final SelectionValues startingPointKV;
		private CreateBadgeClassWizardContext createContext;
		private SingleSelection startingPointSelection;
		private BadgesTableModel tableModel;
		private FlexiTableElement tableEl;

		@Autowired
		private OpenBadgesManager openBadgesManager;

		public CreateBadge00StartingPointForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "starting_point_step");

			if (runContext.get(CreateBadgeClassWizardContext.KEY) instanceof CreateBadgeClassWizardContext createBadgeClassWizardContext) {
				createContext = createBadgeClassWizardContext;
			}

			startingPointKV = new SelectionValues();
			startingPointKV.add(SelectionValues.entry("form.create.from.existing.badge.title", translate("form.create.from.existing.badge.text")));
			startingPointKV.add(SelectionValues.entry("form.create.from.scratch.title", translate("form.create.from.scratch.text")));

			initForm(ureq);
			updateUI();

			flc.getFormItemComponent().addListener(this);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == startingPointSelection) {
				createBadgeClassContext.setStartFromScratch(startingPointSelection.isKeySelected("form.create.from.scratch.title"));
				if (createBadgeClassContext.isStartFromScratch()) {
					createBadgeClassContext.startFromScratch();
					setNextStep(new CreateBadge00ImageStep(ureq, createBadgeClassContext));
				} else {
					createBadgeClassContext.copyFromExistingBadge(getTranslator());
					setNextStep(new CreateBadge02DetailsStep(ureq, createBadgeClassContext));
				}
				fireEvent(ureq, StepsEvent.STEPS_CHANGED);

				updateUI();
			} else if (source == tableEl) {
				if (event instanceof SelectionEvent selectionEvent) {
					BadgesRow row = tableModel.getObject(selectionEvent.getIndex());
					Long key = row.badgeClassWithSizeAndCount().badgeClass().getKey();
					createBadgeClassContext.setSourceBadgeClassKey(key);
					createBadgeClassContext.copyFromExistingBadge(getTranslator());
				} else if (event instanceof FlexiTableSearchEvent){
					loadModel();
				}
			}
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);

			tableEl.clearError();
			if (!createBadgeClassContext.isStartFromScratch()) {
				if (createBadgeClassContext.getSourceBadgeClassKey() == null) {
					tableEl.setErrorKey("error.select.a.badge");
					allOk &= false;
				}
			}

			return allOk;
		}

		@Override
		protected void formNext(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			startingPointSelection = uifactory.addCardSingleSelectHorizontal("form.starting.point", formLayout,
					startingPointKV.keys(),
					Arrays.stream(startingPointKV.keys()).map(this::translate).toList().toArray(String[]::new),
					startingPointKV.values(), null);
			startingPointSelection.addActionListener(FormEvent.ONCHANGE);
			initTable(formLayout, ureq);
			loadModel();
		}

		private void initTable(FormItemContainer formLayout, UserRequest ureq) {
			String mediaUrl = registerMapper(ureq, new BadgesMediaFileMapper());
			FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			FlexiColumnModel imageModel = new DefaultFlexiColumnModel(BadgesTableModel.BadgesCols.image,
					(renderer, sb, val, row, source, ubu, translator) -> {
						Size targetSize = tableModel.getObject(row).badgeClassWithSizeAndCount().fitIn(55, 55);
						int width = targetSize.getWidth();
						int height = targetSize.getHeight();
						sb.append("<div style='width: ").append(width).append("px; height: ").append(height).append("px;'>");
						sb.append("<div class='o_image'>");
						if (val instanceof String image) {
							sb.append("<img src=\"");
							sb.append(mediaUrl).append("/").append(image).append("\" ");
							sb.append(" width='").append(width).append("px' height='").append(height).append("px' >");
						}
						sb.append("</div>");
						sb.append("</div>");
					});
			imageModel.setColumnCssClass("o_badge_image");
			columnModel.addFlexiColumnModel(imageModel);
			columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgesTableModel.BadgesCols.title));
			columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgesTableModel.BadgesCols.createdOn));
			columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgesTableModel.BadgesCols.assertions));
			columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgesTableModel.BadgesCols.course));
			columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgesTableModel.BadgesCols.courseReference));

			tableModel = new BadgesTableModel(columnModel, getLocale(), getTranslator());

			tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 5,
					true, getTranslator(), formLayout);
			tableEl.setSelection(true, false, true);

			initFilters();
		}

		private void initFilters() {
			List<FlexiTableExtendedFilter> filters = new ArrayList<>();

			FlexiTableTextFilter badgeTitleFilter = new FlexiTableTextFilter(
					translate("filter.badge.title"), FILTER_BADGE_TITLE, true);
			filters.add(badgeTitleFilter);

			FlexiTableTextFilter courseNameFilter = new FlexiTableTextFilter(
					translate("filter.course.name"), FILTER_COURSE_NAME, true);
			filters.add(courseNameFilter);

			FlexiTableTextFilter courseReferenceFilter = new FlexiTableTextFilter(
					translate("filter.course.reference"), FILTER_COURSE_REFERENCE, true);
			filters.add(courseReferenceFilter);

			tableEl.setFilters(true, filters, false, true);
		}

		private void loadModel() {
			FlexiTableTextFilter badgeTitleFilter = (FlexiTableTextFilter) FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_BADGE_TITLE);
			FlexiTableTextFilter courseNameFilter = (FlexiTableTextFilter) FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_COURSE_NAME);
			FlexiTableTextFilter courseReferenceFilter = (FlexiTableTextFilter) FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_COURSE_REFERENCE);

			List<BadgesRow> rows = openBadgesManager.getCourseBadgeClassesWithSizesAndCounts(getIdentity()).stream()
					.filter((row) -> {
						boolean show = true;
						if (StringHelper.containsNonWhitespace(badgeTitleFilter.getValue())) {
							show &= row.badgeClass().getName().toLowerCase()
									.contains(badgeTitleFilter.getValue().toLowerCase());
						}
						if (StringHelper.containsNonWhitespace(courseNameFilter.getValue())) {
							show &= row.badgeClass().getEntry().getDisplayname().toLowerCase()
									.contains(courseNameFilter.getValue().toLowerCase());
						}
						if (StringHelper.containsNonWhitespace(courseReferenceFilter.getValue())) {
							if (row.badgeClass().getEntry().getExternalRef() == null) {
								show &= false;
							} else {
								show &= row.badgeClass().getEntry().getExternalRef().toLowerCase()
										.contains(courseReferenceFilter.getValue().toLowerCase());
							}
						}
						return show;
					})
					.map(result -> new BadgesRow(result, result.badgeClass().getEntry().getDisplayname(),
							result.badgeClass().getEntry().getExternalRef()))
					.toList();
			tableModel.setObjects(rows);
			tableEl.reset(true, true, true);
		}

		private void updateUI() {
			if (createBadgeClassContext.isStartFromScratch()) {
				startingPointSelection.select("form.create.from.scratch.title", true);
				tableEl.setVisible(false);
			} else {
				startingPointSelection.select("form.create.from.existing.badge.title", true);
				tableEl.setVisible(true);
			}
		}

		private class BadgesMediaFileMapper implements Mapper {

			@Override
			public MediaResource handle(String relPath, HttpServletRequest request) {
				VFSLeaf classFileLeaf = openBadgesManager.getBadgeClassVfsLeaf(relPath);
				if (classFileLeaf != null) {
					return new VFSMediaResource(classFileLeaf);
				}
				return new NotFoundMediaResource();
			}
		}
	}
}


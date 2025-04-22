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
package org.olat.modules.curriculum.ui.copy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyElementSetting;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyResources;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.copy.CopyElementOverviewTableModel.CopyElementCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CopyElementOverviewController extends StepFormBasicController implements FlexiTableComponentDelegate {

	private static final String TOGGLE_DETAILS_CMD = "toggle-details";
	
	private FormLink shiftDatesButton;
	private FlexiTableElement tableEl;
	private CopyElementOverviewTableModel tableModel;
	private final VelocityContainer detailsVC;
	
	private int counter = 0;
	private final CopyElementContext context;
	
	private CloseableModalController cmc;
	private ShiftDatesController shiftDatesCtrl;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public CopyElementOverviewController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			CopyElementContext context) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "elements");
		setTranslator(Util.createPackageTranslator(CurriculumComposerController.class, getLocale(), getTranslator()));
		this.context = context;

		detailsVC = createVelocityContainer("element_details");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		shiftDatesButton = uifactory.addFormLink("shift.dates", formLayout, Link.BUTTON);
		shiftDatesButton.setIconLeftCSS("o_icon o_icon-fw o_icon_shift");
		
		TreeNodeFlexiCellRenderer treeNodeRenderer = new TreeNodeFlexiCellRenderer(TOGGLE_DETAILS_CMD);
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CopyElementCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyElementCols.displayName, treeNodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyElementCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyElementCols.beginDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyElementCols.endDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyElementCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyElementCols.numOfResources,
				new CopyInfosCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyElementCols.numOfTemplates,
				new CopyInfosCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyElementCols.numOfLectureBlocks,
				new CopyInfosCellRenderer()));
		
		tableModel = new CopyElementOverviewTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "elements", tableModel, 2000, true, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
	}
	
	private void loadModel() {
		List<CopyElementRow> rows = new ArrayList<>();
		Map<Long,CopyElementRow> keyToRows = new HashMap<>();
		
		CurriculumElement curriculumElement = context.getCurriculumElement();
		CurriculumElementInfosSearchParams searchParams = CurriculumElementInfosSearchParams.searchDescendantsOf(null, curriculumElement);
		List<CurriculumElementInfos> elementsWithInfos = curriculumService.getCurriculumElementsWithInfos(searchParams);
		
		for(CurriculumElementInfos element:elementsWithInfos) {
			CopyElementRow row = forgeRow(element);
			rows.add(row);
			keyToRows.put(row.getKey(), row);
		}
		
		// Build parent line
		for(CopyElementRow row:rows) {
			if(row.getParentKey() != null) {
				row.setParent(keyToRows.get(row.getParentKey()));
			}
		}
		
		if(rows.size() > 1) {
			Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private CopyElementRow forgeRow(CurriculumElementInfos elementWithInfos) {
		CurriculumElement element = elementWithInfos.curriculumElement();
		
		long effectiveLecturesBlocks = 0l;
		long lectureBlocks = elementWithInfos.numOfLectureBlocks();
		long lectureBlocksStandalone = elementWithInfos.numOfLectureBlocks() - elementWithInfos.numOfLectureBlocksWithEntry();
		long lectureBlocksWithEntry = elementWithInfos.numOfLectureBlocksWithEntry();
		if(context.getCoursesEventsCopySetting() == CopyResources.resource
				|| context.getCoursesEventsCopySetting() == CopyResources.relation) {
			effectiveLecturesBlocks += lectureBlocksWithEntry;
		}
		if(context.isStandaloneEventsCopySetting()) {
			effectiveLecturesBlocks += lectureBlocksStandalone;
		}
		CopyInfos numOfLectureBlocks = new CopyInfos(effectiveLecturesBlocks, lectureBlocks);
		
		long effectiveTemplates = 0l;
		long templates = elementWithInfos.numOfTemplates();
		if(context.getCoursesEventsCopySetting() == CopyResources.relation
				|| context.getCoursesEventsCopySetting() == CopyResources.resource) {
			effectiveTemplates = templates;
		}
		long resources = elementWithInfos.numOfResources();
		long effectiveResources = 0l;
		if((context.getCoursesEventsCopySetting() == CopyResources.relation)
				|| (context.getCoursesEventsCopySetting() == CopyResources.resource && effectiveTemplates == 0)) {
			effectiveResources = resources;
		}
		CopyInfos numOfResources = new CopyInfos(effectiveResources, resources);
		CopyInfos numOfTemplates = new CopyInfos(effectiveTemplates, templates);
		
		CopyElementSetting setting = calculateSetting(element);
		CopyElementRow row = new CopyElementRow(element, setting, numOfResources, numOfTemplates, numOfLectureBlocks);
		
		Date beginDate = context.shiftDate(element.getBeginDate());
		DateChooser beginDateEl = uifactory.addDateChooser("begin.date." + (++counter), null, beginDate, flc);
		row.setBeginDateEl(beginDateEl);
		
		Date endDate = context.shiftDate(element.getEndDate());
		DateChooser endDateEl = uifactory.addDateChooser("end.date." + (++counter), null, endDate, flc);
		row.setEndDateEl(endDateEl);
		
		return row;
	}
	
	private CopyElementSetting calculateSetting(CurriculumElement element) {
		String displayName = element.getDisplayName();
		if(element.equals(context.getCurriculumElement())
				&& StringHelper.containsNonWhitespace(context.getDisplayName())) {
			displayName = context.getDisplayName();
		}
		String identifier = context.evaluateIdentifier(element);
		return new CopyElementSetting(element, displayName, identifier, null, null);
	}
	
	private Date getEarliestDate() {
		Date earliestDate = null;
		List<CopyElementRow> rows = tableModel.getObjects();
		for(CopyElementRow row:rows) {
			Date beginDate = row.getCurriculumElement().getBeginDate();
			if(beginDate != null && (earliestDate == null || earliestDate.after(beginDate))) {
				earliestDate = beginDate;
			}
		}
		return earliestDate;
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if(rowObject instanceof CopyElementRow elementRow
				&& elementRow.getDetailsController() != null) {
			components.add(elementRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return components;
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(shiftDatesCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(shiftDatesCtrl);
		removeAsListenerAndDispose(cmc);
		shiftDatesCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(shiftDatesButton == source) {
			doOpenShiftDates(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if(TOGGLE_DETAILS_CMD.equals(cmd)) {
					CopyElementRow row = tableModel.getObject(se.getIndex());
					if(row.getDetailsController() != null) {
						doCloseElementDetails(row);
						tableEl.collapseDetails(se.getIndex());
					} else {
						doOpenElementDetails(ureq, row);
						tableEl.expandDetails(se.getIndex());
					}
				}
			} else if(event instanceof DetailsToggleEvent toggleEvent) {
				CopyElementRow row = tableModel.getObject(toggleEvent.getRowIndex());
				if(toggleEvent.isVisible()) {
					doOpenElementDetails(ureq, row);
				} else {
					doCloseElementDetails(row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		List<CopyElementRow> rows = tableModel.getObjects();
		for(CopyElementRow row:rows) {
			allOk &= validateCopyElementRow(row);
		}
		
		return allOk;
	}
	
	private boolean validateCopyElementRow(CopyElementRow row) {
		boolean allOk = true;
		
		Date begin = row.getBeginDateEl().getDate();
		Date end = row.getEndDateEl().getDate();
		if(begin != null && end != null && end.before(begin)) {
			row.getEndDateEl().setErrorKey("form.error.date");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formNext(UserRequest ureq) {
		List<CopyElementRow> rows = tableModel.getObjects();
		List<CopyElementSetting> elementsToCopy = rows.stream().map(row -> {
			Date begin = row.getBeginDateEl().getDate();
			Date end = row.getEndDateEl().getDate();
			String displayName = row.getSetting().displayName();
			String identifier = row.getSetting().identifier();			
			return new CopyElementSetting(row.getCurriculumElement(), displayName, identifier, begin, end);
		}).toList();
		
		context.setCurriculumElementsToCopy(elementsToCopy);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenShiftDates(UserRequest ureq) {
		Date earliestDate = getEarliestDate();
		shiftDatesCtrl = new ShiftDatesController(ureq, getWindowControl(), context, earliestDate);
		listenTo(shiftDatesCtrl);
		
		String title = translate("shift.dates");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), shiftDatesCtrl.getInitialComponent(),
				true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenElementDetails(UserRequest ureq, CopyElementRow row) {
		if(row == null) return;
		
		if(row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}
		
		CopyElementDetailsController detailsCtrl = new CopyElementDetailsController(ureq, getWindowControl(), mainForm,
				row.getCurriculumElement(), context);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}
	
	private void doCloseElementDetails(CopyElementRow row) {
		if(row.getDetailsController() == null) return;
		removeAsListenerAndDispose(row.getDetailsController());
		flc.remove(row.getDetailsController().getInitialFormItem());
		row.setDetailsController(null);
	}
}

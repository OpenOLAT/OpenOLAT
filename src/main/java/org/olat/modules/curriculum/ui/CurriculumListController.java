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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumSecurityCallbackFactory;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumManagerDataModel.CurriculumCols;
import org.olat.modules.curriculum.ui.component.CurriculumActiveCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is a list of curriculum for the site "My courses" for
 * participants.
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumListController extends FormBasicController implements Activateable2 {
	
	private FlexiTableElement tableEl;
	private CurriculumManagerDataModel tableModel;
	
	private BreadcrumbedStackedPanel stackPanel;
	
	private CurriculumSecurityCallback secCallback;
	private CurriculumElementListController elementListCtrl;
	
	private Identity assessedIdentity;
	
	@Autowired
	private CurriculumService curriculumService;
	
	/**
	 * This opens the list of the curriculum of the logged in user with standard permissions.
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param stackPanel The bread crumb panel
	 */
	public CurriculumListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel) {
		super(ureq, wControl, "curriculum_list");
		this.stackPanel = stackPanel;
		assessedIdentity = getIdentity();
		secCallback = CurriculumSecurityCallbackFactory.createDefaultCallback();
		
		initForm(ureq);
		loadModel();
	}
	
	/**
	 * This opens the list of curriculums of the specified user.
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param assessedIdentity The identity to look at the curriculums
	 */
	public CurriculumListController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity) {
		super(ureq, wControl, "curriculum_list");
		this.assessedIdentity = assessedIdentity;
		secCallback = CurriculumSecurityCallbackFactory.userLookCallback();
		
		initForm(ureq);
		loadModel();
	}

	/**
	 * This opens the list of curriculums of the specified user
	 *
	 * @param ureq
	 * @param wControl
	 * @param assessedIdentity
	 * @param stackedPanel
	 */
	public CurriculumListController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity, BreadcrumbedStackedPanel stackedPanel) {
		this(ureq, wControl, assessedIdentity);

		this.stackPanel = stackedPanel;
	}
	
	public String getName() {
		return "curriculum";
	}
	
	public void setBreadcrumbPanel(BreadcrumbedStackedPanel stackPanel) {
		this.stackPanel = stackPanel;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.active, new CurriculumActiveCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.displayName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.identifier, "select"));
		
		tableModel = new CurriculumManagerDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableSettings("table.curriculum.empty", null, "o_icon_curriculum_element");
		tableEl.setFilters("activity", getFilters(), false);
		if(assessedIdentity.equals(getIdentity())) {
			tableEl.setSelectedFilterKey("active");
		}
		tableEl.setAndLoadPersistedPreferences(ureq, "cur-curriculum-list-v2");
	}
	
	private List<FlexiTableFilter> getFilters() {
		List<FlexiTableFilter> filters = new ArrayList<>(5);
		filters.add(new FlexiTableFilter(translate("filter.active"), "active"));
		filters.add(new FlexiTableFilter(translate("filter.inactive"), "inactive"));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("show.all"), "all", true));
		return filters;
	}
	
	private void loadModel() {
		List<Curriculum> curriculums = curriculumService.getMyCurriculums(assessedIdentity);
		List<CurriculumRef> activeRefs = curriculumService.getMyActiveCurriculumRefs(assessedIdentity);
		List<Long> activeKeys = activeRefs.stream().map(CurriculumRef::getKey).collect(Collectors.toList());
		List<CurriculumRow> rows = curriculums.stream()
				.map(c -> new CurriculumRow(c, activeKeys.contains(c.getKey())))
				.collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(tableModel.getRowCount() == 1) {
				CurriculumRow row = tableModel.getObject(0);
				if(elementListCtrl == null || !elementListCtrl.getCurriculum().getKey().equals(row.getKey())) {
					doSelectCurriculum(ureq, row);
				}
			}
		} else {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Curriculum".equals(type)) {
				Long curriculumKey = entries.get(0).getOLATResourceable().getResourceableId();
				if(elementListCtrl == null || !elementListCtrl.getCurriculum().getKey().equals(curriculumKey)) {
					for(CurriculumRow row:tableModel.getObjects()) {
						if(curriculumKey.equals(row.getKey())) {
							doSelectCurriculum(ureq, row);
						}
					}
				}
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					CurriculumRow row = tableModel.getObject(se.getIndex());
					doSelectCurriculum(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSelectCurriculum(UserRequest ureq, CurriculumRow row) {
		stackPanel.popUpToController(this);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Curriculum", row.getKey());
		WindowControl swControl = addToHistory(ureq, ores, null);
		elementListCtrl = new CurriculumElementListController(ureq, swControl, stackPanel,
				assessedIdentity, row, secCallback);
		listenTo(elementListCtrl);
		stackPanel.pushController(row.getDisplayName(), elementListCtrl);
	}
}

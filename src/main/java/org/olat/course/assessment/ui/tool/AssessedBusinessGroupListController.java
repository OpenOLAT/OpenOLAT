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
package org.olat.course.assessment.ui.tool;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.ui.tool.AssessedBusinessGroupTableModel.ABGCols;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedBusinessGroupListController extends FormBasicController implements Activateable2 {
	
	private FlexiTableElement tableEl;
	private AssessedBusinessGroupTableModel tableModel;
	private final TooledStackedPanel stackPanel;
	
	private AssessmentIdentityListCourseTreeController currentCtrl;
	
	private final RepositoryEntry courseEntry;
	private final AssessmentToolContainer toolContainer;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public AssessedBusinessGroupListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, "groups");
		this.courseEntry = courseEntry;
		this.assessmentCallback = assessmentCallback;
		this.toolContainer = toolContainer;
		this.stackPanel = stackPanel;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ABGCols.key, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ABGCols.name, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ABGCols.description));
		
		tableModel = new AssessedBusinessGroupTableModel(columnsModel); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
	}
	
	protected void loadModel() {
		if(assessmentCallback.canAssessBusinessGoupMembers()) {
			SearchBusinessGroupParams params = new SearchBusinessGroupParams();
			if(assessmentCallback.isAdmin()) {
				//all groups
			} else {
				params.setOwner(true);
				params.setIdentity(getIdentity());
			}
			List<BusinessGroup> businessGroups = businessGroupService.findBusinessGroups(params, courseEntry, 0, -1);
			List<AssessedBusinessGroupRow> businessGroupRows = new ArrayList<>();
			for(BusinessGroup group:businessGroups) {
				businessGroupRows.add(new AssessedBusinessGroupRow(group));
			}
			tableModel.setObjects(businessGroupRows);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("BusinessGroup".equalsIgnoreCase(resName) || "Group".equalsIgnoreCase(resName)) {
			Long groupKey = entries.get(0).getOLATResourceable().getResourceableId();
			BusinessGroup businessGroup = businessGroupService.loadBusinessGroup(groupKey);
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doSelect(ureq, businessGroup).activate(ureq, subEntries, entries.get(0).getTransientState());
		}
		
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				AssessedBusinessGroupRow row = tableModel.getObject(se.getIndex());
				if("select".equals(cmd)) {
					doSelect(ureq, row);
				}
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	private AssessmentIdentityListCourseTreeController doSelect(UserRequest ureq, AssessedBusinessGroupRow row) {
		BusinessGroup businessGroup = businessGroupService.loadBusinessGroup(row.getKey());
		return doSelect(ureq, businessGroup);
	}
	
	private AssessmentIdentityListCourseTreeController doSelect(UserRequest ureq, BusinessGroup businessGroup) {
		removeAsListenerAndDispose(currentCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("BusinessGroup", businessGroup.getKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		addToHistory(ureq, bwControl);
		AssessmentIdentityListCourseTreeController treeCtrl = new AssessmentIdentityListCourseTreeController(ureq, bwControl, stackPanel,
				courseEntry, businessGroup, toolContainer, assessmentCallback);
		listenTo(treeCtrl);
		
		String groupName = StringHelper.escapeHtml(businessGroup.getName());
		stackPanel.pushController(groupName, treeCtrl);
		currentCtrl = treeCtrl;
		treeCtrl.activate(ureq, null, null);
		return currentCtrl;
	}
}
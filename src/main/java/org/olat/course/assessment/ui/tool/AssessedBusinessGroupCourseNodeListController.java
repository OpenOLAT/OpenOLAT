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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.model.AssessedBusinessGroup;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.tool.AssessedBusinessGroupTableModel.ABGCols;
import org.olat.course.assessment.ui.tool.event.CourseNodeEvent;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.coach.ui.ProgressRenderer;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedBusinessGroupCourseNodeListController extends FormBasicController implements Activateable2 {
	
	private FlexiTableElement tableEl;
	private AssessedBusinessGroupTableModel tableModel;
	private final TooledStackedPanel stackPanel;
	
	private AssessmentIdentityListCourseTreeController currentCtrl;
	
	private final CourseNode courseNode;
	private final RepositoryEntry courseEntry;
	private final UserCourseEnvironment coachCourseEnv;
	private final AssessmentToolContainer toolContainer;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public AssessedBusinessGroupCourseNodeListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, CourseNode courseNode, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, "groups", Util.createPackageTranslator(AssessmentModule.class, ureq.getLocale()));
		this.courseNode = courseNode;
		this.courseEntry = courseEntry;
		this.coachCourseEnv = coachCourseEnv;
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
		
		if(courseNode instanceof AssessableCourseNode) {
			AssessableCourseNode aNode = (AssessableCourseNode)courseNode;
			AssessmentConfig assessmentConfig = courseNode.getAssessmentConfig();
			if(assessmentConfig.hasPassed()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ABGCols.countPassed,
						new ProgressRenderer(false, getTranslator())));
			}
			if(assessmentConfig.hasScore()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ABGCols.averageScore,
						new ScoreCellRenderer()));
			}
		}
		
		tableModel = new AssessedBusinessGroupTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(ABGCols.name.name(), true));
		tableEl.setSortSettings(options);
		tableEl.setAndLoadPersistedPreferences(ureq, "assessment-tool-group-list");
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutcont = (FormLayoutContainer)formLayout;
			if(courseNode != null) {
				String courseNodeCssClass = CourseNodeFactory.getInstance()
						.getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType()).getIconCSSClass();
				layoutcont.contextPut("courseNodeCssClass", courseNodeCssClass);
				layoutcont.contextPut("courseNodeTitle", courseNode.getShortTitle());
			}
		}
	}
	
	protected void loadModel() {
		if(assessmentCallback.canAssessBusinessGoupMembers()) {
			RepositoryEntry testEntry = null;
			if(courseNode.needsReferenceToARepositoryEntry()) {
				testEntry = courseNode.getReferencedRepositoryEntry();
			}
			SearchAssessedIdentityParams params
				= new SearchAssessedIdentityParams(courseEntry, courseNode.getIdent(), testEntry, assessmentCallback);
			if(assessmentCallback.getCoachedGroups() != null) {
				List<Long> groupKeys = assessmentCallback.getCoachedGroups()
					.stream().map(BusinessGroup::getKey).collect(Collectors.toList());
				params.setBusinessGroupKeys(groupKeys);
			}
			
			List<AssessedBusinessGroup> rows = assessmentToolManager.getBusinessGroupStatistics(getIdentity(), params);
			Set<Long> keys = rows.stream().map(AssessedBusinessGroup::getKey).collect(Collectors.toSet());
			
			List<BusinessGroup> groups;
			if(assessmentCallback.isAdmin()) {
				CourseEnvironment courseEnv = CourseFactory.loadCourse(courseEntry).getCourseEnvironment();
				groups = courseEnv.getCourseGroupManager().getAllBusinessGroups();
			} else if(assessmentCallback.getCoachedGroups() != null) {
				groups = assessmentCallback.getCoachedGroups();
			} else {
				groups = Collections.emptyList();
			}

			for(BusinessGroup group:groups) {
				if(!keys.contains(group.getKey())) {
					rows.add(new AssessedBusinessGroup(group.getKey(), group.getName(), 0.0d, 0, 0, 0, 0));
				}
			}
			
			tableModel.setObjects(rows);
			tableEl.reset();
			tableEl.reloadData();
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
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == currentCtrl) {
			if(event instanceof CourseNodeEvent) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
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
				AssessedBusinessGroup row = tableModel.getObject(se.getIndex());
				if("select".equals(cmd)) {
					doSelect(ureq, row);
				}
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	private AssessmentIdentityListCourseTreeController doSelect(UserRequest ureq, AssessedBusinessGroup row) {
		BusinessGroup businessGroup = businessGroupService.loadBusinessGroup(row.getKey());
		return doSelect(ureq, businessGroup);
	}
	
	private AssessmentIdentityListCourseTreeController doSelect(UserRequest ureq, BusinessGroup businessGroup) {
		removeAsListenerAndDispose(currentCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("BusinessGroup", businessGroup.getKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		addToHistory(ureq, bwControl);
		AssessmentIdentityListCourseTreeController treeCtrl = new AssessmentIdentityListCourseTreeController(ureq, bwControl, stackPanel,
				courseEntry, businessGroup, coachCourseEnv, toolContainer, assessmentCallback);
		listenTo(treeCtrl);

		stackPanel.pushController(businessGroup.getName(), null, treeCtrl, "groups");
		currentCtrl = treeCtrl;
		
		List<ContextEntry> entries = BusinessControlFactory.getInstance()
				.createCEListFromString(OresHelper
						.createOLATResourceableInstance("Node", Long.valueOf(courseNode.getIdent())));
		treeCtrl.activate(ureq, entries, null);
		return currentCtrl;
	}
}
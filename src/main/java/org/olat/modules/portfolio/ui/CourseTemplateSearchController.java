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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.IdentityEnvironment;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.course.run.userview.AccessibleFilter;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.ui.CourseTemplateSearchDataModel.CTCols;
import org.olat.modules.portfolio.ui.model.CourseTemplateRow;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseTemplateSearchController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private CourseTemplateSearchDataModel model;
	
	private CourseTemplateRow selectedEntry;

	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private NodeAccessService nodeAccessService;
	
	public CourseTemplateSearchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "course_templates");
		initForm(ureq);
		loadModel(ureq);
	}
	
	public CourseTemplateRow getSelectedEntry() {
		return selectedEntry;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CTCols.course, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CTCols.courseNode, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));
		
		model = new CourseTemplateSearchDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setEmptyTableMessageKey("no.binders.template.available");
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel(UserRequest ureq) {
		IdentityEnvironment identityEnv = ureq.getUserSession().getIdentityEnvironment();
		
		List<Binder> currentBinders = portfolioService.searchOwnedBindersFromCourseTemplate(getIdentity());
		Set<CurrentBinder> currentSet = new HashSet<>();
		for(Binder currentBinder:currentBinders) {
			Long courseEntryKey = currentBinder.getEntry().getKey();
			String nodeIdent = currentBinder.getSubIdent();
			currentSet.add(new CurrentBinder(courseEntryKey, nodeIdent));
		}
		
		List<RepositoryEntry> entries = portfolioService.searchCourseWithBinderTemplates(getIdentity());
		List<CourseTemplateRow> rows = new ArrayList<>(entries.size());
		for(RepositoryEntry entry:entries) {
			ICourse course = CourseFactory.loadCourse(entry);
			UserCourseEnvironment uce = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
			uce.getScoreAccounting().evaluateAll();
			
			CourseNode rootNode = uce.getCourseEnvironment().getRunStructure().getRootNode();
			loadCourseModel(rootNode, uce, rows, currentSet);
		}
		
		model.setObjects(rows);
		tableEl.reset();
		tableEl.reloadData();
	}
	
	private void loadCourseModel(CourseNode courseNode, UserCourseEnvironment uce,
			List<CourseTemplateRow> rows, Set<CurrentBinder> currentSet) {
		if(courseNode instanceof PortfolioCourseNode) {
			PortfolioCourseNode pNode = (PortfolioCourseNode)courseNode;
			TreeNode treeNode = nodeAccessService.getCourseTreeModelBuilder(uce)
					.withFilter(AccessibleFilter.create())
					.build()
					.getNodeById(pNode.getIdent());
			if (treeNode != null && treeNode.isAccessible()) {
				RepositoryEntry refEntry = pNode.getReferencedRepositoryEntry();
				if(refEntry != null && "BinderTemplate".equals(refEntry.getOlatResource().getResourceableTypeName())) {
					RepositoryEntry courseEntry = uce.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
					
					CurrentBinder binderKey = new CurrentBinder(courseEntry.getKey(), pNode.getIdent());
					if(!currentSet.contains(binderKey)) {
						rows.add(new CourseTemplateRow(courseEntry, pNode, refEntry));
					}
				}
			}
		}
		
		for(int i=courseNode.getChildCount(); i-->0; ) {
			loadCourseModel((CourseNode)courseNode.getChildAt(i), uce, rows, currentSet);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				CourseTemplateRow row = model.getObject(se.getIndex());
				if("select".equals(cmd)) {
					selectedEntry = row;
					fireEvent(ureq, ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private static class CurrentBinder {
		
		private final Long courseEntryKey;
		private final String nodeIdent;
		
		public CurrentBinder(Long courseEntryKey, String nodeIdent) {
			this.courseEntryKey = courseEntryKey;
			this.nodeIdent = nodeIdent;
		}

		@Override
		public int hashCode() {
			return courseEntryKey.hashCode() + nodeIdent.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof CurrentBinder) {
				CurrentBinder cb = (CurrentBinder)obj;
				return courseEntryKey.equals(cb.courseEntryKey)
						&& nodeIdent.equals(cb.nodeIdent);
			}
			return false;
		}
	}
}

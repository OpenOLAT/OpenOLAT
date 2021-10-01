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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.ToReviewRow;
import org.olat.modules.assessment.ui.event.UserSelectionEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentToReviewSmallController extends FormBasicController {
	
	private final RepositoryEntry courseEntry;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	private FlexiTableElement tableEl;
	private UsersToReviewTableModel usersTableModel;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final Map<String,String> nodeIdentToNodeShortTitles;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public AssessmentToReviewSmallController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, "overview_to_review");
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.courseEntry = courseEntry;
		this.assessmentCallback = assessmentCallback;
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.reducedUsageIdentifyer, isAdministrativeUser);
		
		nodeIdentToNodeShortTitles = new HashMap<>();
		ICourse course = CourseFactory.loadCourse(courseEntry);
		TreeModel tm = AssessmentHelper.assessmentTreeModel(course);
		new TreeVisitor(node -> {
			if(node instanceof TreeNode) {
				Object uobject = ((TreeNode)node).getUserObject();
				if(uobject instanceof CourseNode) {
					CourseNode tNode = (CourseNode)uobject;
					nodeIdentToNodeShortTitles.put(tNode.getIdent(), tNode.getShortTitle());
				}
			}
		}, tm.getRootNode(), false).visitAll();
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(AssessmentToolConstants.reducedUsageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, "select",
					true, "userProp-" + colIndex));
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToReviewCols.toReview, "select",
				new ElementToReviewCellRenderer(nodeIdentToNodeShortTitles, getTranslator())));

		usersTableModel = new UsersToReviewTableModel(columnsModel); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", usersTableModel, 20, false, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setExportEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setEmptyTableSettings("waiting.review.emtpy", null, "o_icon_correction");
	}
	
	private void loadModel() {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, null, null, assessmentCallback);
		params.setAssessmentObligations(AssessmentObligation.NOT_EXCLUDED);
		List<AssessmentEntry> entries = assessmentToolManager.getAssessmentEntries(getIdentity(), params, AssessmentEntryStatus.inReview);
		List<UserToReviewRow> rows = new ArrayList<>();
		Map<Long,UserToReviewRow> identityKeyToRow = new HashMap<>();
		for(AssessmentEntry entry:entries) {
			if(!nodeIdentToNodeShortTitles.containsKey(entry.getSubIdent())) {
				continue;
			}
			
			Identity assessedIdentity = entry.getIdentity();
			if(identityKeyToRow.containsKey(assessedIdentity.getKey())) {
				identityKeyToRow.get(assessedIdentity.getKey())
					.getSubIndents().add(entry.getSubIdent());
			} else {
				UserToReviewRow row = new UserToReviewRow(entry.getIdentity(), userPropertyHandlers, getLocale());
				row.getSubIndents().add(entry.getSubIdent());
				rows.add(row);
				identityKeyToRow.put(assessedIdentity.getKey(), row);
			}
		}
		
		usersTableModel.setObjects(rows);
		tableEl.reset();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select".equals(se.getCommand())) {
					int index = se.getIndex();
					UserToReviewRow row = usersTableModel.getObject(index);
					fireEvent(ureq, new UserSelectionEvent(row.getIdentityKey(), row.getSubIndents()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public static class UserToReviewRow extends UserPropertiesRow implements ToReviewRow {
		
		private List<String> nodeIndents = new ArrayList<>(3);
		
		public UserToReviewRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
			super(identity, userPropertyHandlers, locale);
		}

		@Override
		public List<String> getSubIndents() {
			return nodeIndents;
		}
	}

	public class UsersToReviewTableModel extends DefaultFlexiTableDataModel<UserToReviewRow> implements SortableFlexiTableDataModel<UserToReviewRow> {

		public UsersToReviewTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}
		
		@Override
		public void sort(SortKey orderBy) {
			SortableFlexiTableModelDelegate<UserToReviewRow> sorter
					= new SortableFlexiTableModelDelegate<>(orderBy, this, null);
			List<UserToReviewRow> views = sorter.sort();
			super.setObjects(views);
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			UserToReviewRow identityRow = getObject(row);
			return getValueAt(identityRow, col);
		}

		@Override
		public Object getValueAt(UserToReviewRow row, int col) {
			if(col >= 0 && col < ToReviewCols.values().length) {
				switch(ToReviewCols.values()[col]) {
					case toReview: return row;
				}
			}
			int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
			return row.getIdentityProp(propPos);
		}

		@Override
		public DefaultFlexiTableDataModel<UserToReviewRow> createCopyWithEmptyList() {
			return new UsersToReviewTableModel(getTableColumnModel());
		}
	}

	public enum ToReviewCols implements FlexiSortableColumnDef {
		toReview("table.header.elements.toReview");
		
		private final String i18nKey;
		
		private ToReviewCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
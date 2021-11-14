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
package org.olat.course.assessment.bulk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.model.FindNamedIdentity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.course.assessment.model.BulkAssessmentDatas;
import org.olat.course.assessment.model.BulkAssessmentRow;
import org.olat.course.assessment.model.BulkAssessmentSettings;
import org.olat.course.nodes.CourseNode;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18.11.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ValidationStepForm extends StepFormBasicController {
	
	private static final String[] userPropsToSearch = new String[]{ UserConstants.EMAIL, UserConstants.INSTITUTIONALEMAIL, UserConstants.INSTITUTIONALUSERIDENTIFIER };
	
	private ValidDataModel validModel;
	private ValidDataModel invalidModel;
	private FlexiTableElement validTableEl;
	private FlexiTableElement invalidTableEl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	
	public ValidationStepForm(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "validation");
		initForm(ureq);
		doValidate();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		CourseNode courseNode = (CourseNode)getFromRunContext("courseNode");
		BulkAssessmentSettings settings = new BulkAssessmentSettings(courseNode);
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.identifier", Cols.identifier.ordinal()));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.lastName", Cols.lastName.ordinal()));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.firstName", Cols.firstName.ordinal()));
		if(settings.isHasScore()) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.score", Cols.score.ordinal(), new ScoreCellRenderer(settings)));
		}
		if(settings.isHasPassed() && settings.getCut() == null) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.passed", Cols.passed.ordinal(), new PassedCellRenderer()));
		}
		if(settings.isHasUserComment()) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.comment", Cols.comment.ordinal()));
		}
		if(settings.isHasReturnFiles()) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.numOfReturnFiles", Cols.numOfReturnFiles.ordinal()));
		}
		
		validModel = new ValidDataModel(Collections.<UserData>emptyList());
		validModel.setTableColumnModel(tableColumnModel);
		validTableEl = uifactory.addTableElement(getWindowControl(), "validList", validModel, getTranslator(), formLayout);
		validTableEl.setCustomizeColumns(false);
		
		invalidModel = new ValidDataModel(Collections.<UserData>emptyList());
		invalidModel.setTableColumnModel(tableColumnModel);
		invalidTableEl = uifactory.addTableElement(getWindowControl(), "notFoundList", invalidModel, getTranslator(), formLayout);
		invalidTableEl.setCustomizeColumns(false);
		
		flc.contextPut("settings", settings);
	}
	
	private void doValidate() {
		BulkAssessmentDatas datas = (BulkAssessmentDatas)getFromRunContext("datas");
		if(datas.getRows() != null) {
			doValidateRows(datas);
		}
		flc.contextPut("hasNoItems", Boolean.valueOf(datas.getRows() == null || datas.getRows().isEmpty()));			
	}
	
	private void doValidateRows(BulkAssessmentDatas datas) {
		List<BulkAssessmentRow> rows = datas.getRows();
		
		List<String> assessedIdList = new ArrayList<>(rows.size());
		for(BulkAssessmentRow row : rows) {
			assessedIdList.add(row.getAssessedId());
		}
		
		Map<String,Identity> idToIdentityMap = loadAssessedIdentities(assessedIdList);

		List<UserData> validDatas = new ArrayList<>(idToIdentityMap.size());
		List<UserData> invalidDatas = new ArrayList<>(rows.size() - idToIdentityMap.size());
		for(BulkAssessmentRow row : datas.getRows()) {
			Identity foundIdentity = idToIdentityMap.get(row.getAssessedId());
			if(foundIdentity == null) {
				invalidDatas.add(new UserData(row, null));
			} else {
				row.setIdentityKey(foundIdentity.getKey());
				validDatas.add(new UserData(row, foundIdentity));
			}
		}
		validModel.setObjects(validDatas);
		invalidModel.setObjects(invalidDatas);
		flc.contextPut("hasValidItems", Boolean.valueOf(!validDatas.isEmpty()));
		flc.contextPut("hasInvalidItems", Boolean.valueOf(!invalidDatas.isEmpty()));
		validTableEl.reset();
		invalidTableEl.reset();
	}
	
	private Map<String,Identity> loadAssessedIdentities(List<String> assessedIdList) {
		Map<String,Identity> idToIdentityMap = new HashMap<>();
		
		for(String assessedId : assessedIdList) {
			List<FindNamedIdentity> identities = securityManager.findIdentitiesBy(Collections.singletonList(assessedId));
			if(!identities.isEmpty()) {
				Identity identity = identities.get(0).getIdentity();
				idToIdentityMap.put(assessedId, identity);
				continue;
			}

			for(String prop : userPropsToSearch) {
				List<Identity> found = userManager.findIdentitiesWithProperty(prop, assessedId);
				if(found != null && !found.isEmpty()) {
					// ignore multiple hits, just take the first one
					Identity identity = found.get(0);
					idToIdentityMap.put(assessedId, identity);
					continue;
				}
			}
		}
		
		return idToIdentityMap;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (validModel.getRowCount() == 0) {
			// do not continue wizard without valid data
			return;
		}
		BulkAssessmentDatas datas = (BulkAssessmentDatas)getFromRunContext("datas");
		List<BulkAssessmentRow> rows = new ArrayList<>(validModel.getRowCount() + invalidModel.getRowCount());
		for(int i=validModel.getRowCount(); i-->0; ) {
			rows.add(validModel.getObject(i).getRow());
		}
		for(int i=invalidModel.getRowCount(); i-->0; ) {
			rows.add(invalidModel.getObject(i).getRow());
		}
		datas.setRows(rows);
		addToRunContext("datas", datas);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private static class UserData {
		private final Identity identity;
		private final BulkAssessmentRow row;
		
		public UserData(BulkAssessmentRow row, Identity identity) {
			this.row = row;
			this.identity = identity;
		}
		
		public String getAssessedIdentifier() {
			return row.getAssessedId();
		}
		
		public String getFirstName() {
			return identity == null ? null : identity.getUser().getProperty(UserConstants.FIRSTNAME, null);
		}
		
		public String getLastName() {
			return identity == null ? null : identity.getUser().getProperty(UserConstants.LASTNAME, null);
		}
		
		public Float getScore() {
			return row.getScore();
		}
		
		public Boolean getPassed() {
			return row.getPassed();
		}
		
		public String getComment() {
			return row.getComment();
		}
		
		public int getNumOfReturnFiles() {
			return row.getReturnFiles() == null ? 0 : row.getReturnFiles().size();
		}
		
		public BulkAssessmentRow getRow() {
			if(identity != null) {
				row.setIdentityKey(identity.getKey());
			}
			return row;
		}
	}
	
	private enum Cols {
		identifier,
		lastName,
		firstName,
		score,
		passed,
		status,
		comment,
		numOfReturnFiles
	}
	
	private static class ValidDataModel  extends DefaultTableDataModel<UserData> implements FlexiTableDataModel<UserData> {
		private FlexiTableColumnModel columnModel;
		
		public ValidDataModel(List<UserData> nodes) {
			super(nodes);
		}

		@Override
		public FlexiTableColumnModel getTableColumnModel() {
			return columnModel;
		}

		@Override
		public void setTableColumnModel(FlexiTableColumnModel tableColumnModel) {
			this.columnModel = tableColumnModel;
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public Object getValueAt(int row, int col) {
			UserData data = getObject(row);
			switch(Cols.values()[col]) {
				case identifier: return data.getAssessedIdentifier();
				case firstName: return data.getFirstName();
				case lastName: return data.getLastName();
				case score: return data.getScore();
				case passed: return data.getPassed();
				case status: return data.getPassed();
				case comment: return data.getComment();
				case numOfReturnFiles: return data.getNumOfReturnFiles();
				default: return null;
			}
		}
	}
}

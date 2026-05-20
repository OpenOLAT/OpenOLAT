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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.model.FindNamedIdentity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.course.assessment.model.BulkAssessmentDatas;
import org.olat.course.assessment.model.BulkAssessmentRow;
import org.olat.course.assessment.model.BulkAssessmentSettings;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
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

	private RepositoryEntryRef courseEntry;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;

	public ValidationStepForm(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form rootForm, RepositoryEntryRef courseEntry) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "validation");
		this.courseEntry = courseEntry;
		initForm(ureq);
		doValidate();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		CourseNode courseNode = (CourseNode)getFromRunContext("courseNode");
		BulkAssessmentSettings settings = new BulkAssessmentSettings(courseNode, courseEntry);
		
		initValidTableForm(formLayout, settings);
		initInvalidTableForm(formLayout, settings);
		
		flc.contextPut("settings", settings);
	}

	private void initValidTableForm(FormItemContainer formLayout, BulkAssessmentSettings settings) {
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		initTableColumnModel(tableColumnModel, settings);
		
		validModel = new ValidDataModel(tableColumnModel);
		validTableEl = uifactory.addTableElement(getWindowControl(), "validList", validModel, getTranslator(), formLayout);
		validTableEl.setCustomizeColumns(false);
	}
	
	private void initInvalidTableForm(FormItemContainer formLayout, BulkAssessmentSettings settings) {
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.error, new ErrorCellRenderer()));
		initTableColumnModel(tableColumnModel, settings);

		invalidModel = new ValidDataModel(tableColumnModel);
		invalidTableEl = uifactory.addTableElement(getWindowControl(), "notFoundList", invalidModel, getTranslator(), formLayout);
		invalidTableEl.setCustomizeColumns(false);
	}

	private void initTableColumnModel(FlexiTableColumnModel tableColumnModel, BulkAssessmentSettings settings) {
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.identifier));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lastName));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.firstName));
		if(settings.isHasScore()) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.score, new ScoreCellRenderer(settings)));
		}
		if(settings.isHasPassed() && settings.getCut() == null) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.passed, new PassedCellRenderer(getLocale())));
		}
		if(settings.isHasUserComment()) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.comment));
		}
		if(settings.isHasReturnFiles()) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.numOfReturnFiles));
		}
	}
	
	private void doValidate() {
		BulkAssessmentDatas datas = (BulkAssessmentDatas)getFromRunContext("datas");
		if(datas.getRows() != null) {
			doValidateRows(datas);
		}
		flc.contextPut("hasNoItems", Boolean.valueOf(datas.getRows() == null || datas.getRows().isEmpty()));			
	}
	
	private void doValidateRows(BulkAssessmentDatas datas) {
		List<Identity> participants = repositoryService.getMembers(courseEntry, RepositoryEntryRelationType.all, GroupRoles.participant.name());
		Set<Identity> participantsSet = new HashSet<>(participants);
		List<BulkAssessmentRow> rows = datas.getRows();
		
		List<String> assessedIdList = new ArrayList<>(rows.size());
		for(BulkAssessmentRow row : rows) {
			assessedIdList.add(row.getAssessedId());
		}
		
		Map<String,Identity> idToIdentityMap = loadAssessedIdentities(assessedIdList);

		List<UserData> validDatas = new ArrayList<>(idToIdentityMap.size());
		List<UserData> invalidDatas = new ArrayList<>(rows.size() - idToIdentityMap.size());
		Set<Identity> deduplication = new HashSet<>();
		for(BulkAssessmentRow row : datas.getRows()) {
			Identity foundIdentity = idToIdentityMap.get(row.getAssessedId());
			if(foundIdentity == null) {
				invalidDatas.add(UserData.valueOfWithError(row, foundIdentity, ErrorType.NOT_FOUND));
			} else {
				if(deduplication.contains(foundIdentity)) {
					invalidDatas.add(UserData.valueOfWithError(row, foundIdentity, ErrorType.DUPLICATE));
				} else if(!participantsSet.contains(foundIdentity)) {
					invalidDatas.add(UserData.valueOfWithError(row, foundIdentity, ErrorType.NOT_PARTICIPANT));
				} else {
					row.setIdentityKey(foundIdentity.getKey());
					validDatas.add(UserData.valueOf(row, foundIdentity));
				}
				deduplication.add(foundIdentity);
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
	
	private class ErrorCellRenderer implements FlexiCellRenderer {

		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
				FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			if(cellValue instanceof ErrorType error) {
				switch(error) {
					case NOT_FOUND: renderError(target, "error.user.notfound"); break;
					case DUPLICATE: renderError(target, "error.user.duplicate"); break;
					case NOT_PARTICIPANT: renderError(target, "error.user.notparticipant"); break;
				}
			}
		}
		
		private void renderError(StringOutput target, String i18nKey) {
			target.append("<span><i class='o_icon o_icon_warning'> </i> ")
			      .append(translate(i18nKey))
			      .append("</span>");
		}
	}
	
	private static class UserData {
		private final ErrorType error;
		private final Identity identity;
		private final BulkAssessmentRow row;
		
		private UserData(BulkAssessmentRow row, Identity identity, ErrorType type) {
			this.row = row;
			this.error = type;
			this.identity = identity;
		}
		
		public static final UserData valueOfWithError(BulkAssessmentRow row, Identity identity, ErrorType type) {
			row.setValid(false);
			return new UserData(row, identity, type);
		}
		
		public static final UserData valueOf(BulkAssessmentRow row, Identity identity) {
			row.setValid(true);
			return new UserData(row, identity, null);
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
		
		public ErrorType getError() {
			return error;
		}
		
		public BulkAssessmentRow getRow() {
			if(identity != null) {
				row.setIdentityKey(identity.getKey());
			}
			return row;
		}
	}
	
	private enum ErrorType {
		NOT_FOUND,
		DUPLICATE,
		NOT_PARTICIPANT
	}
	
	private enum Cols implements FlexiSortableColumnDef {
		identifier("table.header.identifier"),
		lastName("table.header.lastName"),
		firstName("table.header.firstName"),
		score("table.header.score"),
		passed("table.header.passed"),
		comment("table.header.comment"),
		numOfReturnFiles("table.header.numOfReturnFiles"),
		error("table.header.error");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
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
	
	private static class ValidDataModel extends DefaultFlexiTableDataModel<UserData> {
		
		private static final Cols[] COLS = Cols.values();
		
		public ValidDataModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			UserData data = getObject(row);
			return switch(COLS[col]) {
				case identifier -> data.getAssessedIdentifier();
				case firstName -> data.getFirstName();
				case lastName -> data.getLastName();
				case score -> data.getScore();
				case passed -> data.getPassed();
				case comment -> data.getComment();
				case numOfReturnFiles -> data.getNumOfReturnFiles();
				case error -> data.getError();
				default -> null;
			};
		}
	}
}

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
package org.olat.course.nodes.practice.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
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
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.tool.AssessmentStatusCellRenderer;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.course.nodes.PracticeCourseNode;
import org.olat.course.nodes.gta.ui.GTACoachedGroupGradingController;
import org.olat.course.nodes.practice.ui.PracticeIdentityTableModel.PracticeIdentityCols;
import org.olat.course.nodes.practice.ui.renders.PracticeChallengeCellRenderer;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeCoachController extends FormBasicController implements Activateable2 {

	protected static final String USER_PROPS_ID = GTACoachedGroupGradingController.class.getCanonicalName();
	
	private FlexiTableElement tableEl;
	private PracticeIdentityTableModel tableModel;
	private TooledStackedPanel stackPanel;

	private RepositoryEntry courseEntry;
	private PracticeCourseNode courseNode;
	private final UserCourseEnvironment coachCourseEnv;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public PracticeCoachController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, PracticeCourseNode courseNode, UserCourseEnvironment coachCourseEnv) {
		super(ureq, wControl, "practice_coach");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.stackPanel = stackPanel;
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		this.coachCourseEnv = coachCourseEnv;
		assessmentCallback = getAssessmentToolSecurityCallback(ureq, coachCourseEnv);
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	private AssessmentToolSecurityCallback getAssessmentToolSecurityCallback(UserRequest ureq, UserCourseEnvironment userCourseEnv) {
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq, courseEntry);
		boolean admin = reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach();
		boolean nonMembers = reSecurity.isEntryAdmin();
		List<BusinessGroup> coachedGroups = null;
		if(reSecurity.isGroupCoach()) {
			coachedGroups = userCourseEnv.getCoachedGroups();
		}
		return new AssessmentToolSecurityCallback(admin, nonMembers, reSecurity.isCourseCoach(), reSecurity.isGroupCoach(), reSecurity.isCurriculumCoach(), coachedGroups);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		
		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, "select", true, "userProp-" + colIndex));
			if(!options.hasDefaultOrderBy()) {
				options.setDefaultOrderBy(new SortKey("userProp-" + colIndex, true));
			}
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PracticeIdentityCols.status,
				new AssessmentStatusCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PracticeIdentityCols.challenges,
				new PracticeChallengeCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("details", translate("details"), "details"));
		
		tableModel = new PracticeIdentityTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_practice_coach_table");
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(true);
		tableEl.setSortSettings(options);
		tableEl.setSelectAllEnable(true);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		// TODO practice
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("details".equals(se.getCommand()) || "select".equals(se.getCommand())) {
					doDetails(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void loadModel() {
		SearchAssessedIdentityParams params = getSearchParameters();
		
		// Get the identities and remove identity without assessment entry.
		List<Identity> practicingIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);

		// Get the assessment entries and put it in a map.
		// Obligation filter is applied in this query.
		Map<Long,AssessmentEntry> entryMap = new HashMap<>();
		assessmentToolManager.getAssessmentEntries(getIdentity(), params, null).stream()
			.filter(entry -> entry.getIdentity() != null)
			.forEach(entry -> entryMap.put(entry.getIdentity().getKey(), entry));
		
		// Apply filters
		
		List<PracticeIdentityRow> rows = new ArrayList<>(practicingIdentities.size());
		for(Identity practicingIdentity:practicingIdentities) {
			AssessmentEntry entry = entryMap.get(practicingIdentity.getKey());
			if(entry != null) {
				rows.add(new PracticeIdentityRow(practicingIdentity, entry.getAssessmentStatus(),
						userPropertyHandlers, getLocale()));
			}
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private SearchAssessedIdentityParams getSearchParameters() {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, courseNode.getIdent(), null, assessmentCallback);
		
		return params;
	}
	
	private void doDetails(UserRequest ureq, PracticeIdentityRow row) {
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		PracticeCoachedIdentityController detailsCtrl = new PracticeCoachedIdentityController(ureq, getWindowControl(),
				coachCourseEnv, courseNode, assessedIdentity);
		listenTo(detailsCtrl);
		
		String fullName = userManager.getUserDisplayName(assessedIdentity);
		stackPanel.pushController(fullName, detailsCtrl);
	}
}

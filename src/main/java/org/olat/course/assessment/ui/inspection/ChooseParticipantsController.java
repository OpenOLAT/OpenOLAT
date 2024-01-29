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
package org.olat.course.assessment.ui.inspection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.bulk.PassedOverridenCellRenderer;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.inspection.ChooseParticipantsListModel.ParticipantsCols;
import org.olat.course.assessment.ui.inspection.elements.YesCellRenderer;
import org.olat.course.assessment.ui.tool.AssessmentForm;
import org.olat.course.assessment.ui.tool.AssessmentStatusCellRenderer;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.course.assessment.ui.tool.UserVisibilityCellRenderer;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.modules.dcompensation.DisadvantageCompensationStatusEnum;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChooseParticipantsController extends StepFormBasicController {
	
	private FlexiTableElement tableEl;
	private ChooseParticipantsListModel tableModel;
	
	private final CreateInspectionContext context;
	private final AssessmentConfig assessmentConfig;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final CreateInspectionStepsListener stepsListener;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private DisadvantageCompensationService disadvantageCompensationService;
	
	public ChooseParticipantsController(UserRequest ureq, WindowControl wControl,
			CreateInspectionContext context, StepsRunContext runContext, Form rootForm,
			CreateInspectionStepsListener stepsListener) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "select_participants");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessmentForm.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessmentInspectionOverviewController.class, getLocale(), getTranslator()));
		
		this.context = context;
		this.stepsListener = stepsListener;
		assessmentConfig = courseAssessmentService.getAssessmentConfig(context.getCourseEntry(), context.getCourseNode());
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			CourseNode courseNode = context.getCourseNode();
			CourseNodeConfiguration nodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration(courseNode.getType());
			layoutCont.contextPut("courseNodeCssClass", nodeConfig.getIconCSSClass());
			layoutCont.contextPut("courseNodeTitle", courseNode.getShortTitle());
		}

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		
		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, null, true, "userProp-" + colIndex));
			if(!options.hasDefaultOrderBy()) {
				options.setDefaultOrderBy(new SortKey("userProp-" + colIndex, true));
			}
			colIndex++;
		}
		
		if(assessmentConfig.isAssessable()) {
			if(Mode.setByNode == assessmentConfig.getScoreMode() || Mode.setByNode == assessmentConfig.getPassedMode()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantsCols.userVisibility, new UserVisibilityCellRenderer(false)));
			}
			
			if(assessmentConfig.isPassedOverridable()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ParticipantsCols.passedOverriden, new PassedOverridenCellRenderer()));
			}
			if(Mode.none != assessmentConfig.getPassedMode()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantsCols.passed, new PassedCellRenderer(getLocale())));
			}
		}
		
		if (assessmentConfig.hasStatus()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantsCols.assessmentStatus, new AssessmentStatusCellRenderer(getLocale())));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantsCols.compensation, new YesCellRenderer()));
		
		tableModel = new ChooseParticipantsListModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "participants", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
	}
	
	private void loadModel() {
		SearchAssessedIdentityParams params = getSearchParameters();
		
		// Get the identities and remove identity without assessment entry.
		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);

		// Get the assessment entries and put it in a map.
		// Obligation filter is applied in this query.
		Map<Long,AssessmentEntry> entryMap = new HashMap<>();
		assessmentToolManager.getAssessmentEntries(getIdentity(), params, null).stream()
				.filter(entry -> entry.getIdentity() != null)
				.forEach(entry -> entryMap.put(entry.getIdentity().getKey(), entry));
		
		List<DisadvantageCompensation> compensations = disadvantageCompensationService
				.getActiveDisadvantageCompensations(context.getCourseEntry(), context.getCourseNode().getIdent());
		Map<Long, DisadvantageCompensation> compensationsMap = compensations.stream().collect(Collectors
				.toMap(compensation -> compensation.getIdentity().getKey(), compensation -> compensation, (u, v) -> u));
	
		List<ChooseParticipantRow> rows = new ArrayList<>(assessedIdentities.size());
		for(Identity assessedIdentity:assessedIdentities) {
			AssessmentEntry entry = entryMap.get(assessedIdentity.getKey());
			DisadvantageCompensation compensation = compensationsMap.get(assessedIdentity.getKey());
			Boolean comp = compensation != null && compensation.getStatusEnum() != DisadvantageCompensationStatusEnum.deleted;
			ChooseParticipantRow row = new ChooseParticipantRow(assessedIdentity, entry, comp, userPropertyHandlers, getLocale());
			rows.add(row);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private SearchAssessedIdentityParams getSearchParameters() {
		RepositoryEntry courseEntry = context.getCourseEntry();
		CourseNode courseNode = context.getCourseNode();
		
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, courseNode.getIdent(),
				null, context.getSecCallback());
		params.setUserPropertyHandlers(userPropertyHandlers);
		return params;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		tableEl.clearError();
		if((context.getParticipants() == null || context.getParticipants().isEmpty())
				&& tableEl.getMultiSelectedIndex().isEmpty()) {
			tableEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		if(!selectedIndexes.isEmpty()) {
			Set<Long> participantsKeys = new HashSet<>();
			List<IdentityRef> participants = new ArrayList<>(selectedIndexes.size());
			for(Iterator<Integer> it=selectedIndexes.iterator(); it.hasNext(); ) {
				ChooseParticipantRow row = tableModel.getObject(it.next().intValue());
				if(row != null) {
					participants.add(new IdentityRefImpl(row.getIdentityKey()));
					participantsKeys.add(row.getIdentityKey());
				}
			}
			
			List<DisadvantageCompensation> compensations = disadvantageCompensationService
					.getActiveDisadvantageCompensations(context.getCourseEntry(), context.getCourseNode().getIdent());
			List<DisadvantageCompensation> participantsCompensations = compensations.stream()
					.filter(compensation -> participantsKeys.contains(compensation.getIdentity().getKey()))
					.toList();
			context.setParticipants(participants, participantsCompensations);
		}
		
		if(context.getCourseNode() != null) {
			stepsListener.onStepsChanged(ureq);
			fireEvent(ureq, StepsEvent.STEPS_CHANGED);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else {
			tableEl.setErrorKey("form.legende.mandatory");
		}
	}
}

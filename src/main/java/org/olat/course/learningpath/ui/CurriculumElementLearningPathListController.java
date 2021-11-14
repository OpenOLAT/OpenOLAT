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
package org.olat.course.learningpath.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
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
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.learningpath.ui.LearningPathIdentityDataModel.LearningPathIdentityCols;
import org.olat.modules.assessment.AssessmentEntryCompletion;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementLearningPathListController extends FormBasicController {
	
	private static final String USAGE_IDENTIFIER = CurriculumElementLearningPathListController.class.getCanonicalName();
	private static final String ORES_TYPE_IDENTITY = "Identity";
	private static final String CMD_SELECT = "select";
	
	private FlexiTableElement tableEl;
	private LearningPathIdentityDataModel dataModel;

	private CurriculumLearningPathRepositoryController repoCtrl;

	private final TooledStackedPanel stackPanel;
	private final CurriculumElement curriculumElement;
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private AssessmentService assessmentService;

	public CurriculumElementLearningPathListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			CurriculumElement curriculumElement) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.stackPanel = stackPanel;
		this.curriculumElement = curriculumElement;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USAGE_IDENTIFIER, isAdministrativeUser);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colIndex = LearningPathIdentityDataModel.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(USAGE_IDENTIFIER, userPropertyHandler);
			columnsModel.addFlexiColumnModel(
					new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex,
							CMD_SELECT, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathIdentityCols.completion,
				new LearningProgressCompletionCellRenderer()));
		
		dataModel = new LearningPathIdentityDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("table.empty.identities", null, "o_icon_user");
		tableEl.setExportEnabled(true);
		
		loadModel();
	}

	private void loadModel() {
		List<Identity> identities = curriculumService.getMembersIdentity(curriculumElement, CurriculumRoles.participant);
		List<Long> identityKeys = identities.stream().map(Identity::getKey).collect(Collectors.toList());
		List<AssessmentEntryCompletion> completions = assessmentService.loadAvgCompletionsByIdentities(curriculumElement, identityKeys);
		
		Map<Long, Double> identityKeyToCompletion = new HashMap<>();
		for (AssessmentEntryCompletion completion : completions) {
			if (completion.getCompletion() != null) {
				identityKeyToCompletion.put(completion.getKey(), completion.getCompletion());
			}
		}
		
		List<LearningPathIdentityRow> rows = new ArrayList<>(identities.size());
		for (Identity identity : identities) {
			Double completion = identityKeyToCompletion.get(identity.getKey());
			LearningPathIdentityRow row = new LearningPathIdentityRow(identity, userPropertyHandlers,
					getLocale(), completion, null, null);
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				LearningPathIdentityRow row = dataModel.getObject(se.getIndex());
				if (CMD_SELECT.equals(cmd)) {
					doSelect(ureq, row);
				}
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelect(UserRequest ureq, LearningPathIdentityRow row) {
		removeAsListenerAndDispose(repoCtrl);
		
		Identity participant = securityManager.loadIdentityByKey(row.getIdentityKey());
		String fullName = userManager.getUserDisplayName(participant);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_TYPE_IDENTITY, row.getIdentityKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		
		repoCtrl = new CurriculumLearningPathRepositoryController(ureq, bwControl, stackPanel, curriculumElement, participant);
		listenTo(repoCtrl);
		stackPanel.pushController(fullName, repoCtrl);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}

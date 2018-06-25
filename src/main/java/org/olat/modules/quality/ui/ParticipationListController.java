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
package org.olat.modules.quality.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityManager;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.ui.ParticipationDataModel.ParticipationCols;
import org.olat.modules.quality.ui.wizard.AddUser_1_ChooseUserStep;
import org.olat.modules.quality.ui.wizard.IdentityContext;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ParticipationListController extends FormBasicController implements TooledController {

	private static final String CMD_REMOVE = "remove";
	
	private Link addUsersLink;
	private Link addCourseUsersLink;
	private FormLink removeUsersLink;
	private ParticipationDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private final TooledStackedPanel stackPanel;
	private StepsMainRunController addUserWizard;
	private CloseableModalController cmc;
	private ParticipationRemoveConfirmationController removeConformationCtrl;
	
	private final QualitySecurityCallback secCallback;
	private QualityDataCollection dataCollection;
	
	@Autowired
	private QualityManager qualityManager;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private RepositoryService repositoryService;

	public ParticipationListController(UserRequest ureq, WindowControl windowControl,
			QualitySecurityCallback secCallback, TooledStackedPanel stackPanel,
			QualityDataCollectionLight dataCollection) {
		super(ureq, windowControl, "participation_list");
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		this.dataCollection = qualityManager.loadDataCollectionByKey(dataCollection);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.firstname));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.lastname));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.email));
		
		ParticipationDataSource dataSource = new ParticipationDataSource(dataCollection);
		dataModel = new ParticipationDataModel(dataSource, columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "participations", dataModel, 25, true, getTranslator(), formLayout);
		if (secCallback.canRevomeParticipation(dataCollection)) {
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
		}
		
		if (secCallback.canRevomeParticipation(dataCollection)) {
			removeUsersLink = uifactory.addFormLink("participation.remove", formLayout, Link.BUTTON);
		}
	}

	@Override
	public void initTools() {
		addCourseUsersLink = LinkFactory.createToolLink("participation.user.add.course", translate("participation.user.add.course"), this);
		addCourseUsersLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qual_part_user_add_course");
		stackPanel.addTool(addCourseUsersLink, Align.right);
		
		addUsersLink = LinkFactory.createToolLink("participation.user.add", translate("participation.user.add"), this);
		addUsersLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qual_part_user_add");
		stackPanel.addTool(addUsersLink, Align.right);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent) event;
				String cmd = se.getCommand();
				ParticipationRow row = dataModel.getObject(se.getIndex());
				if (CMD_REMOVE.equals(cmd)) {
					List<EvaluationFormParticipationRef> participationRefs = Collections.singletonList(row.getParticipationRef());
					doConfirmRemove(ureq, participationRefs);
				}
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink) source;
			if (link == removeUsersLink) {
				List<EvaluationFormParticipationRef> participationRefs = getSelectedParticipationRefs();
				doConfirmRemove(ureq, participationRefs);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (addUsersLink == source) {
			doAddUsers(ureq);
		} else if (addCourseUsersLink == source) {
			doAddCourseUsers();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (addUserWizard == source) {
			if (event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					tableEl.reset(true, false, true);
				}
				cleanUp();
			}
		} else if (source == removeConformationCtrl) {
			List<EvaluationFormParticipationRef> participationRefs = removeConformationCtrl.getParticipationRefs();
			doRemove(participationRefs);
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cmc.deactivate();
			cleanUp();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(removeConformationCtrl);
		removeAsListenerAndDispose(addUserWizard);
		removeAsListenerAndDispose(cmc);
		removeConformationCtrl = null;
		addUserWizard = null;
		cmc = null;
	}
	
	private List<EvaluationFormParticipationRef> getSelectedParticipationRefs() {
		return tableEl.getMultiSelectedIndex().stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.map(row -> row.getParticipationRef())
				.collect(Collectors.toList());
	}

	private void doAddUsers(UserRequest ureq) {
		removeAsListenerAndDispose(addUserWizard);

		Step start = new AddUser_1_ChooseUserStep(ureq);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
				IdentityContext identityContext = (IdentityContext) runContext.get("identityContext");
				List<Identity> identities = identityContext.getIdentities();
				doAddSelectedUsers(identities);
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		addUserWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("participation.user.add.title"), "");
		listenTo(addUserWizard);
		getWindowControl().pushAsModalDialog(addUserWizard.getInitialComponent());
	}

	private void doAddSelectedUsers(List<Identity> identities) {
		List<EvaluationFormParticipation> participations = qualityManager.addParticipations(dataCollection, identities);
		for (EvaluationFormParticipation participation: participations) {
			qualityManager.createContextBuilder(dataCollection, participation).build();
		}
	}

	private void doConfirmRemove(UserRequest ureq, List<EvaluationFormParticipationRef> participationRefs) {
		if (participationRefs.size() == 0) {
			showWarning("participation.none.selected");
		} else {
			removeConformationCtrl = new ParticipationRemoveConfirmationController(ureq, getWindowControl(), participationRefs);
			listenTo(removeConformationCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					removeConformationCtrl.getInitialComponent(), true, translate("participation.remove"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doAddCourseUsers() {
		dataCollection = qualityManager.loadDataCollectionByKey(dataCollection);
		RepositoryEntry entry = dataCollection.getTopicRepositoryEntry();
		if (entry != null) {
			//TODO uh Rollen abbilden
			List<GroupRoles> roles = Arrays.asList(GroupRoles.participant);
			String[] roleNames = roles.stream().map(GroupRoles::name).toArray(String[]::new);
			List<Identity> members = repositoryService.getMembers(entry, roleNames);
			List<EvaluationFormParticipation> participations = qualityManager.addParticipations(dataCollection, members);
			for (EvaluationFormParticipation participation: participations) {
				qualityManager.createContextBuilder(dataCollection, participation, entry, roles).build();
			}
		}
		tableEl.reset(true, false, true);
	}
	
	private void doRemove(List<EvaluationFormParticipationRef> participationRefs) {
		evaluationFormManager.deleteParticipations(participationRefs);
		tableEl.reset(true, false, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}

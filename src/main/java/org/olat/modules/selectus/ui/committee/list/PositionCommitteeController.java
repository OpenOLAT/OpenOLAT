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
package org.olat.modules.selectus.ui.committee.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.admin.user.UserCreateController;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.modules.co.ContactFormController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.AssignmentService;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.model.ApplicationAssignmentLight;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.assignment.AssignmentKey;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.olat.modules.selectus.ui.committee.imp.ImportCommitteeChoosePositionStep;
import org.olat.modules.selectus.ui.committee.imp.ImportCommitteeMembers;
import org.olat.modules.selectus.ui.committee.imp.ImportCommitteeWizardFinishCallback;
import org.olat.modules.selectus.ui.committee.list.PositionCommitteeDataModel.CommitteeCols;
import org.olat.modules.selectus.ui.committee.member.EditCommitteeMemberController;
import org.olat.modules.selectus.ui.committee.wizard.CommitteeWizardFinishCallback;
import org.olat.modules.selectus.ui.committee.wizard.EmailStep;
import org.olat.modules.selectus.ui.components.AssignmentsCellRenderer;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  30 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionCommitteeController extends FormBasicController implements Activateable2, TooledController {

	public static final int USER_PROP_OFFSET = 500; //only used in wizard
	public static final String formIdentifyer = "Committee";
	
	private Link addMember;
	private Link mailToCommittee;
	private Link importCommitteeLink;
	private FlexiTableElement tableEl;
	private PositionCommitteeDataModel membersDataModel;
	
	private StepsMainRunController addMemberWizard;
	private StepsMainRunController importMembersWizard;
	private final TooledStackedPanel stackPanel;
	private ContactFormController contactController;
	private CloseableModalController contactDialogBox;
	private DialogBoxController confirmDeleteBox;
	private EditCommitteeMemberController editMemberController;
	private CloseableModalController editMemberDialogBox;

	private Position position;
	private final Set<PositionRole> ratingRoles;
	private final RecruitingPositionSecurityCallback secCallback;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private static final String COLUMN_PREFS = "committeeListv7";

	@Autowired
	private UserManager userManager;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired
	private AssignmentService assignmentService;
	
	public PositionCommitteeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			Position position, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, "committee", UserManager.getInstance().getPropertyHandlerTranslator(Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale(),
				Util.createPackageTranslator(UserCreateController.class, ureq.getLocale()))));
		
		this.position = position;
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		
		ratingRoles = recruitingModule.getRolesAllowedToRateSet();
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(formIdentifyer, true);

		initForm(ureq);
		loadModel();
		updateCommitteeStatistics();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(hasUserPropertyHandler(UserConstants.TITLE)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CommitteeCols.title, "send_mail"));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CommitteeCols.name, "send_mail"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CommitteeCols.role, "send_mail"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CommitteeCols.institution, "send_mail"));
		
		RecruitingTableOption userPropertiesOption = recruitingModule.getTableCommitteeUserPropertiesOption();
		if(userPropertiesOption != RecruitingTableOption.disabled) {
			boolean visible = userPropertiesOption == RecruitingTableOption.enabled;
			
			int colIndex = USER_PROP_OFFSET;
			for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
				if (userPropertyHandler == null
						|| userPropertyHandler.getName().equals(UserConstants.FIRSTNAME)
						|| userPropertyHandler.getName().equals(UserConstants.LASTNAME)
						|| userPropertyHandler.getName().equals(UserConstants.TITLE)
						|| userPropertyHandler.getName().equals(UserConstants.INSTITUTIONALNAME)) {
					if(userPropertyHandler != null) {
						colIndex++;
					}
					continue;
				}
				
				boolean colVisible = visible && userManager.isMandatoryUserProperty(formIdentifyer, userPropertyHandler);
				FlexiColumnModel col = new DefaultFlexiColumnModel(colVisible, userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colIndex++, true, userPropertyHandler.getName());
				columnsModel.addFlexiColumnModel(col);
			}
		}
		
		if(recruitingModule.isApplicationAssignmentsEnabled() && secCallback.canEditAssignments()) {
			StaticFlexiCellRenderer hasAssignments = new StaticFlexiCellRenderer("assignments", new AssignmentsCellRenderer());
			hasAssignments.setIconRightCSS("o_icon o_icon_external_link");
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CommitteeCols.hasAssignments.i18nHeaderKey(), CommitteeCols.hasAssignments.ordinal(), "assignments",
					new BooleanCellRenderer(
							hasAssignments,
							new StaticFlexiCellRenderer(null, new AssignmentsCellRenderer()))));
		}
		
		if(secCallback.canEditCommitteeMember()) {
			DefaultFlexiColumnModel editCol = new DefaultFlexiColumnModel("edit", -1, "edit",
					new StaticFlexiCellRenderer("", "edit", null, "o_icon o_icon_edit", translate("edit")));
			editCol.setIconHeader("o_icon o_icon_edit");
			editCol.setHeaderLabel(translate("edit"));
			editCol.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(editCol);
		}
		
		if(secCallback.canRemoveCommitteeMember()) {
			DefaultFlexiColumnModel removeCol = new DefaultFlexiColumnModel("delete", -1, "remove",
					new StaticFlexiCellRenderer("", "delete", null, "o_icon o_icon_delete", translate("remove")));
			removeCol.setIconHeader("o_icon o_icon_delete");
			removeCol.setHeaderLabel(translate("remove"));
			removeCol.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(removeCol);	
		}
		
		membersDataModel = new PositionCommitteeDataModel(columnsModel, userPropertyHandlers, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "committeeTable", membersDataModel, 24, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setExportEnabled(false);
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(CommitteeCols.name.name(), true));
		tableEl.setSortSettings(sortOptions);
		tableEl.setAndLoadPersistedPreferences(ureq, COLUMN_PREFS);
		
		List<FlexiTableFilter> filters = new ArrayList<>(5);
		filters.add(new FlexiTableFilter(translate("has.not.rated"), "hasnotrated"));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("rating.show.all"), "show.all", true));
		tableEl.setFilters("", filters, false);
	}
	
	private boolean hasUserPropertyHandler(String name) {
		for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
			if(userPropertyHandler != null && userPropertyHandler.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	private List<SpecialRoleIdentity> wrapRoles(List<Identity> identites, PositionRole role, List<CommitteeMemberRow> memberRows) {
		List<SpecialRoleIdentity> wrappers = new ArrayList<>(identites.size());
		for(Identity identity:identites) {
			SpecialRoleIdentity sec = new SpecialRoleIdentity(identity, role);
			if(secCallback.canEditCommitteeMember()) {
				FormLink editLink = uifactory.addFormLink("edit_" + role + "_" + sec.getKey(), "edit", null, flc, Link.LINK);
				editLink.setIconLeftCSS("o_icon o_icon_edit");
				editLink.setUserObject(sec);
			}
			if(secCallback.canRemoveCommitteeMember()) {
				FormLink remove = uifactory.addFormLink("remove_" + role + "_" + sec.getKey(), "remove", null, flc, Link.LINK);
				remove.setUserObject(sec);
				remove.setIconLeftCSS("o_icon o_icon_delete");
			}
			
			FormLink mail = uifactory.addFormLink("mail_" + role + "_" + sec.getKey(), sec.getEmail(), null, flc, Link.LINK | Link.NONTRANSLATED);	
			mail.setUserObject(sec);
			mail.setIconLeftCSS("o_icon o_icon_mail");
			wrappers.add(sec);
			
			CommitteeMemberRow row = new CommitteeMemberRow(identity, role, ratingRoles.contains(role));
			memberRows.add(row);
		}
		return wrappers;
	}
	
	private void loadModel() {
		List<Identity> membersOnly = new ArrayList<>(recruitingService.getCommitteeMembers(position));
		List<CommitteeMemberRow> members = new ArrayList<>();
		for(Identity memberOnly: membersOnly) {
			members.add(new CommitteeMemberRow(memberOnly, PositionRole.member, ratingRoles.contains(PositionRole.member)));
		}
		
		List<Identity> secretaries = recruitingService.getSecretaries(position);
		List<SpecialRoleIdentity> wrappedSecretaries = wrapRoles(secretaries, PositionRole.secretary, members);
		flc.contextPut("secretaries", wrappedSecretaries);
		
		List<Identity> heads = recruitingService.getHeads(position);
		List<SpecialRoleIdentity> wrappedHeads = wrapRoles(heads, PositionRole.head, members);
		flc.contextPut("heads", wrappedHeads);

		if(recruitingModule.isRoleExOfficioEnabled()) {
			List<Identity> exOfficios = recruitingService.getExOfficios(position);
			List<SpecialRoleIdentity> wrappedExOfficios = wrapRoles(exOfficios, PositionRole.exofficio, members);
			flc.contextPut("exOfficios", wrappedExOfficios);
		}

		membersDataModel.setObjects(members);
		tableEl.reset(true, true, true);
	}
	
	public void updateCommitteeStatistics() {
		if(secCallback.canEditAssignments()) {
			loadModelAssignments();
		} else {
			loadModelRatingsOnly();
		}
	}
	
	private void loadModelRatingsOnly() {
		List<CommitteeMemberRow> rows = membersDataModel.getObjects();
		List<Identity> committee = rows.stream()
				.map(CommitteeMemberRow::getIdentity)
				.collect(Collectors.toList());
		
		List<UserRating> ratings = recruitingService.getRatings(position, committee);
		Map<Long, Long> ratingsMap = ratings.stream()
		           .collect(Collectors.groupingBy(rating -> rating.getCreator().getKey(), Collectors.counting()));
		
		for(CommitteeMemberRow row: rows) {
			Long identityKey = row.getIdentity().getKey();
			Long numOfRatings = ratingsMap.get(identityKey);
			if(row.isCanRate()) {
				row.setNumOfRatings(numOfRatings == null ? 0 : numOfRatings.intValue());
			} else {
				row.setNumOfRatings(0);
			}
			row.setNumOfAssignedRatings(0);
			row.setNumOfAssignments(0);
		}
	}
	
	/**
	 * Work with the table model
	 */
	private void loadModelAssignments() {
		List<CommitteeMemberRow> rows = membersDataModel.getObjects();
		List<Identity> committee = rows.stream()
				.map(CommitteeMemberRow::getIdentity)
				.collect(Collectors.toList());
		
		List<UserRating> ratings = recruitingService.getRatings(position, committee);

		List<ApplicationAssignmentLight> applicationAssignments = assignmentService.getAssignments(position);
		Map<AssignmentKey, AssignmentKey> assignmentKeys = applicationAssignments.stream()
				.map(assignment -> new AssignmentKey(assignment.getAssigneeKey(), assignment.getApplicationKey()))
				.collect(Collectors.toMap(key -> key, key -> key, (u, v) -> v));
		
		List<ApplicationLight> applications = recruitingService.getApplications(position);
		Set<Long> activeApplications = applications.stream()
				.filter(app -> app.getApplicationStatus() == ApplicationStatus.active)
				.map(ApplicationLight::getKey)
				.collect(Collectors.toSet());

		for(CommitteeMemberRow row: rows) {
			Long identityKey = row.getIdentity().getKey();
			
			int numOfRatings = 0;
			int numOfAssignedRatings = 0;
			if(row.isCanRate()) {
				for(UserRating rating:ratings) {
					Long assigneeKey = rating.getCreator().getKey();
					if(identityKey.equals(assigneeKey)) {
						numOfRatings++;
						Long applicationKey = Long.valueOf(rating.getResSubPath());
						if(activeApplications.contains(applicationKey) && assignmentKeys.containsKey(new AssignmentKey(identityKey, applicationKey))) {
							numOfAssignedRatings++;
						}
					}
				}	
			}
			row.setNumOfRatings(numOfRatings);
			row.setNumOfAssignedRatings(numOfAssignedRatings);// first
			
			int numOfAssignments = 0;
			for(Long applicationKey:activeApplications) {
				if(assignmentKeys.containsKey(new AssignmentKey(identityKey, applicationKey))) {
					numOfAssignments++;
				}
			}
			row.setNumOfAssignments(numOfAssignments); // second
		}
	}

	@Override
	public void initTools() {
		if(secCallback.canAddCommitteeMember()) {
			addMember = LinkFactory.createToolLink("add_committee_member", translate("add_committee_member"), this);
			addMember.setElementCssClass("o_sel_add_committee_member");
			addMember.setIconLeftCSS("o_icon o_icon-lg o_icon_add_member");
			stackPanel.addTool(addMember, Align.right);
		}
		
		if(secCallback.canImportCommitteeMembers()) {
			importCommitteeLink = LinkFactory.createToolLink("import_committee_members", translate("import_committee_members"), this);
			importCommitteeLink.setElementCssClass("o_sel_import_committee_member");
			importCommitteeLink.setIconLeftCSS("o_icon o_icon-lg o_icon_membersmanagement");
			stackPanel.addTool(importCommitteeLink, Align.right);
		}
		
		if(secCallback.canSendMailToCommittee()) {
			mailToCommittee = LinkFactory.createToolLink("send_mail", translate("send_mail"), this);
			mailToCommittee.setIconLeftCSS("o_icon o_icon-lg o_icon_mail");
			mailToCommittee.setElementCssClass("o_sel_amil_to_committee");
			stackPanel.addTool(mailToCommittee, Align.right);
		}
	}
	
	public void removeTools() {
		stackPanel.removeTool(addMember);
		stackPanel.removeTool(mailToCommittee);
		stackPanel.removeTool(importCommitteeLink);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent e) {
				int rowId = e.getIndex();
				if(rowId >= 0 && rowId < membersDataModel.getRowCount()) {
					CommitteeMemberRow member = membersDataModel.getObject(rowId);
					if("remove".equals(e.getCommand())) {
						doConfirmRemove(ureq, member.getIdentity(), member.getRole());
					} else if ("send_mail".equals(e.getCommand())) {
						String list = translate("send_mail.committe_member");
						sendEmail(ureq, list, Collections.singletonList(member.getIdentity()));
					} else if ("edit".equals(e.getCommand())) {
						doEditMember(ureq, member.getIdentity(), null);
					} else if("assignments".equals(e.getCommand())) {
						doJumpToAssignments(ureq, member.getIdentity());
					} else if("delete".equals(e.getCommand())) {
						doConfirmRemove(ureq, member.getIdentity(), member.getRole());
					}
				}
			}
		} else if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			if(cmd == null) {
				//ignore
			} else if(cmd.startsWith("edit_")
					&& link.getUserObject() instanceof SpecialRoleIdentity ident) {
				doEditMember(ureq, ident.getIdentity(), ident.getRole());
			} else if(cmd.startsWith("remove_")
					&& link.getUserObject() instanceof SpecialRoleIdentity ident) {
				doConfirmRemove(ureq, ident.getIdentity(), ident.getRole());
			} else if(cmd.startsWith("mail_")
					&& link.getUserObject() instanceof SpecialRoleIdentity ident) {
				doSendMail(ureq, ident.getIdentity(), ident.getRole());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == addMember) {
			doAddMember(ureq);
		} else if(importCommitteeLink == source) {
			doImportCommitteeMembers(ureq);
		} else if (source == mailToCommittee) {
			doSendMailToCommittee(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == confirmDeleteBox) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				CommitteeMemberRow member = (CommitteeMemberRow)confirmDeleteBox.getUserObject();
				doRemoveCommitteeMember(member.getIdentity(), member.getRole());
				loadModel();
				updateCommitteeStatistics();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == addMemberWizard || source == importMembersWizard) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
			} else if (event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				position = recruitingService.getPosition(position.getKey());
				loadModel();
				updateCommitteeStatistics();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}	else if (event == Event.DONE_EVENT) {
				showError("failed");
			}
		} else if (source == contactController) {
			if(event == Event.DONE_EVENT) {
				logAudit("Email send to committee", null);
			}
			contactDialogBox.deactivate();
			removeAsListenerAndDispose(contactController);
		} else if (source == editMemberController) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				position = recruitingService.getPosition(position.getKey());
				loadModel();
				updateCommitteeStatistics();
			}
			editMemberDialogBox.deactivate();
			removeAsListenerAndDispose(editMemberController);
		}
		super.event(ureq, source, event);
	}
	
	private void doAddMember(UserRequest ureq) {
		EmailStep start = new EmailStep(ureq);
		removeAsListenerAndDispose(addMemberWizard);
		CommitteeWizardFinishCallback finish = new CommitteeWizardFinishCallback(position, getTranslator());
		addMemberWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish,
				new CancelCallback(), translate("add_committee_member"), null);
		listenTo(addMemberWizard);
		getWindowControl().pushAsModalDialog(addMemberWizard.getInitialComponent());
	}
	
	private void doImportCommitteeMembers(UserRequest ureq) {
		final ImportCommitteeMembers importCommittee = new ImportCommitteeMembers(position);
		final ImportCommitteeChoosePositionStep start = new ImportCommitteeChoosePositionStep(ureq, importCommittee);
		removeAsListenerAndDispose(importMembersWizard);
		final ImportCommitteeWizardFinishCallback finish = new ImportCommitteeWizardFinishCallback(position, importCommittee);
		importMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish,
				new CancelCallback(), translate("import_committee_members"), null);
		listenTo(importMembersWizard);
		getWindowControl().pushAsModalDialog(importMembersWizard.getInitialComponent());
	}
	
	private void doConfirmRemove(UserRequest ureq, Identity member, PositionRole role) {
		String title;
		if(role == PositionRole.head) {
			title = translate("confirm.remove_committe.title.head");
		} else if(role == PositionRole.exofficio) {
			title = translate("confirm.remove_committe.title.exofficio");
		} else if(role == PositionRole.secretary) {
			title = translate("confirm.remove_committe.title.secretary");
		} else {
			title = translate("confirm.remove_committe.title");
		}
		
		String name = member.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()) + " " +
			member.getUser().getProperty(UserConstants.LASTNAME, getLocale());
		String text = translate("confirm.remove_committe", new String[]{name});
		confirmDeleteBox = activateYesNoDialog(ureq, title, text, confirmDeleteBox);
		confirmDeleteBox.setUserObject(new CommitteeMemberRow(member, role, ratingRoles.contains(role)));
	}
	
	private void doRemoveCommitteeMember(Identity member, PositionRole role) {
		recruitingService.removeFromCommitte(position, member);

		String messageI18n = "audit.log.committe.remove.member";
		String[] messageArgs = new String[] {
				member.getKey().toString(),
				RecruitingHelper.formatFullNameWithTitle(member, getLocale()),
				translate(role.role())
			};
		auditService.auditCommitteeLog(Action.remove, ActionTarget.committee, messageI18n, messageArgs, getTranslator(), position, member, getIdentity());
	}
	
	private void doEditMember(UserRequest ureq, Identity member, PositionRole role) {
		if(role == null) {
			List<Identity> heads = recruitingService.getHeads(position);
			for(Identity head:heads) {
				if(head.equalsByPersistableKey(member)) {
					role = PositionRole.head;
				}
			}
		}

		if(role == null) {
			List<Identity> secretaries = recruitingService.getSecretaries(position);	
			for(Identity secretary:secretaries) {
				if(secretary.equalsByPersistableKey(member)) {
					role = PositionRole.secretary;
				}
			}
		}
		
		if(role == null) {
			List<Identity> exOfficios = recruitingService.getExOfficios(position);	
			for(Identity exOfficio:exOfficios) {
				if(exOfficio.equalsByPersistableKey(member)) {
					role = PositionRole.exofficio;
				}
			}
		}
		
		if(role == null) {
			role = PositionRole.member;
		}
		
		removeAsListenerAndDispose(editMemberController);
		editMemberController = new EditCommitteeMemberController(ureq, getWindowControl(), position, member, role);
		listenTo(editMemberController);
		String title = "";
		switch(role) {
			case member: title = "edit.committee.member"; break;
			case secretary: title = "edit.committee.secretary"; break;
			case exofficio: title = "edit.committee.exofficio"; break;
			case head: title = "edit.committee.head"; break;
		}
		editMemberDialogBox = new CloseableModalController(getWindowControl(), "c", editMemberController.getInitialComponent(), translate(title));
		editMemberDialogBox.activate();
	}
	
	private void doSendMail(UserRequest ureq, Identity recipient, PositionRole role) {
		String list;
		if(role == PositionRole.head) {
			list = translate("role.head");
		} else if(role == PositionRole.exofficio) {
			list = translate("role.exofficio");
		} else if(role == PositionRole.secretary) {
			list = translate("role.secretary");
		} else {
			return;
		}
		sendEmail(ureq, list, Collections.singletonList(recipient));
	}
	
	private void doSendMailToCommittee(UserRequest ureq) {
		String list = translate("send_mail.committe");
		List<CommitteeMemberRow> committee = new ArrayList<>(membersDataModel.getObjects());
		List<Identity> recipients = committee.stream()
				.map(CommitteeMemberRow::getIdentity)
				.collect(Collectors.toList());
		sendEmail(ureq, list, recipients);
	}
	
	private void sendEmail(UserRequest ureq, String list, List<Identity> recipients) {
		removeAsListenerAndDispose(contactController);
		removeAsListenerAndDispose(contactDialogBox);
		
		ContactMessage message = new ContactMessage(getIdentity());
		message.setSubject(translate("send_mail.subject", new String[] { position.getMLTitle(getLocale())}));
		ContactList contactList = new ContactList(list);
		for(Identity recipient:recipients) {
			contactList.add(recipient);
		}
		message.addEmailTo(contactList);

		contactController = new ContactFormController(ureq, getWindowControl(), true, false, false, message);
		listenTo(contactController);
		contactDialogBox = new CloseableModalController(getWindowControl(), "c", contactController.getInitialComponent(), translate("send_mail"));
		listenTo(contactDialogBox);
		contactDialogBox.activate();
	}
	
	private void doJumpToAssignments(UserRequest ureq, Identity identity) {
		// http://localhost:8082/auth/Positions/0/Position/67174400/Applications/0
		String businessPath = "[Positions:0][Position:" + position.getKey() + "][Applications:0][Assignments:" + identity.getKey() + "]"; 
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
	
	protected class CancelCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			return Step.NOSTEP;
		}
	}
	
	public class SpecialRoleIdentity {
		
		private final Identity identity;
		private final PositionRole role;
		
		public SpecialRoleIdentity(Identity identity, PositionRole role) {
			this.identity = identity;
			this.role = role;
		}
		
		public String getKey() {
			return identity.getKey().toString();
		}
		
		public Identity getIdentity() {
			return identity;
		}
		
		public PositionRole getRole() {
			return role;
		}
		
		public String getEmail() {
			return identity.getUser().getProperty(UserConstants.EMAIL, getLocale());
		}
		
		public String getEmailLabel() {
			return getLabel(UserConstants.EMAIL);
		}
		
		public String getName() {
			return RecruitingHelper.formatLastnameFirstName(identity);
		}
		
		public String getInstitution() {
			String institution = identity.getUser().getProperty(UserConstants.INSTITUTIONALNAME, getLocale());
			if(StringHelper.containsNonWhitespace(institution)) {
				return institution;
			}
			return null;
		}
		
		public String getPhone() {

			String phone = identity.getUser().getProperty(UserConstants.TELOFFICE, getLocale());
			if(StringHelper.containsNonWhitespace(phone)) {
				return phone;
			}
			return null;
		}
		
		public String getPhoneLabel() {
			return getLabel(UserConstants.TELOFFICE);
		}
		
		private String getLabel(String name) {
			for(UserPropertyHandler handler:userPropertyHandlers) {
				if(handler.getName().equals(name)) {
					return translate(handler.i18nFormElementLabelKey());
				}	
			}
			return "";
		}
	}
}

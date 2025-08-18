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
package org.olat.modules.openbadges.ui;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.modules.openbadges.ui.BadgeClassTableModel.BadgeClassCols;
import org.olat.modules.openbadges.ui.CreateBadgeClassWizardContext.Mode;
import org.olat.modules.openbadges.ui.wizard.IssueGlobalBadge01Step;
import org.olat.modules.openbadges.ui.wizard.IssueGlobalBadgeFinish;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-05-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeClassesController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {

	private static final String CMD_SELECT = "select";
	private static final String CMD_AWARDED_COUNT = "awardedCount";
	private static final String CMD_EDIT_BADGE = "editBadge";
	private static final String CMD_CREATE_NEW_VERSION = "createNewVersion";
	private static final String CMD_AWARD_MANUALLY = "awardManually";

	private String mediaUrl;
	private final RepositoryEntry entry;
	private final CourseNode courseNode;
	private final RepositoryEntrySecurity reSecurity;
	private final BreadcrumbPanel breadcrumbPanel;
	private final String createKey;
	private BadgeClassTableModel tableModel;
	private FlexiTableElement tableEl;
	private FormLink createLink;
	private CreateBadgeClassWizardContext createBadgeClassContext;
	private ConfirmDeleteBadgeClassController confirmDeleteCtrl;
	private DialogBoxController confirmRevokeAllBadgesCtrl;
	private StepsMainRunController stepsController;
	private BadgeDetailsController badgeDetailsController;
	private CloseableCalloutWindowController calloutCtrl;
	private ToolsController toolsCtrl;
	private VelocityContainer detailsVC;
	private StepsMainRunController issueGlobalBadgeWizard;
	private IssueCourseBadgeController issueCourseBadgeCtrl;
	private CloseableModalController cmc;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	public BadgeClassesController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
								  CourseNode courseNode, RepositoryEntrySecurity reSecurity,
								  BreadcrumbPanel breadcrumbPanel, String contextHelp, String createKey) {
		super(ureq, wControl, "badge_classes");
		flc.contextPut("contextHelp", contextHelp);
		flc.contextPut("title", entry == null ? translate("form.global.badges") : translate("badges"));
		this.entry = entry;
		this.courseNode = courseNode;
		this.reSecurity = reSecurity;
		this.breadcrumbPanel = breadcrumbPanel;
		this.createKey = createKey;
		initForm(ureq);
		loadModel(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		registerMapper(ureq);

		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassCols.image,
				(renderer, sb, val, row, source, ubu, translator) -> {
					OpenBadgesManager.BadgeClassWithSizeAndCount bc = tableModel.getObject(row).badgeClassWithSizeAndCount();
					Size targetSize = bc.fitIn(60, 60);
					String name = bc.badgeClass().getNameWithScan();
					int width = targetSize.getWidth();
					int height = targetSize.getHeight();
					sb.append("<div style='width: ").append(width).append("px; height: ").append(height).append("px; line-height: ").append(height - 12).append("px'>");
					sb.append("<div class='o_image'>");
					if (val instanceof String image) {
						sb.append("<img src=\"");
						sb.append(mediaUrl).append("/").append(image).append("\" ");
						sb.append(" width='").append(width).append("px' height='").append(height).append("px' ");
						sb.append(" alt='").append(translator.translate("badge.image")).append(": ").append(name).append("'");
						sb.append(">");
					}
					sb.append("</div>");
					sb.append("</div>");
				}));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassCols.name, CMD_SELECT));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassCols.creationDate));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassCols.status, new BadgeClassStatusRenderer()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassCols.type));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassCols.version));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, false, BadgeClassCols.verification.i18nHeaderKey(), null, BadgeClassCols.verification.ordinal(), null, BadgeClassCols.verification.sortable(), BadgeClassCols.verification.sortKey(), DefaultFlexiColumnModel.ALIGNMENT_LEFT, new BadgeClassVerificationCellRenderer()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassCols.awardedCount, CMD_AWARDED_COUNT));

		boolean owner = reSecurity == null || reSecurity.isOwner();
		if (owner) {
			columnModel.addFlexiColumnModel(new ActionsColumnModel(BadgeClassCols.tools));
		}

		tableModel = new BadgeClassTableModel(columnModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 10, true,
				getTranslator(), formLayout);

		detailsVC = createVelocityContainer("badge_class_details");
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "badge-classes");

		if (owner) {
			tableEl.setEmptyTableSettings("empty.badges.table.owner", null,
					"o_icon_badge", "form.create.new.badge", "o_icon_add",
					false);
		} else {
			tableEl.setEmptyTableSettings("empty.badges.table", null,
					"o_icon_badge", null, null,
					false);
		}

		createLink = uifactory.addFormLink("create", "form.create.new.badge", null, formLayout, Link.BUTTON);
		createLink.setElementCssClass("o_sel_badge_classes_create");
		createLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");

		createLink.setVisible(owner);
	}

	private void registerMapper(UserRequest ureq) {
		mediaUrl = registerMapper(ureq, new BadgeClassMediaFileMapper());
	}

	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return new ArrayList<>();
	}

	private void loadModel(UserRequest ureq) {
		List<BadgeClassRow> rows = openBadgesManager.getBadgeClassesWithSizesAndCounts(entry).stream()
				.filter(bc -> {
					if (courseNode == null) {
						return true;
					}
					return openBadgesManager.conditionForCourseNodeExists(bc.badgeClass(), courseNode.getIdent());
				})
				.map(bc -> forgeRow(ureq, bc)).toList();
		tableModel.setObjects(rows);
		tableEl.reset();
	}

	private BadgeClassRow forgeRow(UserRequest ureq, OpenBadgesManager.BadgeClassWithSizeAndCount bc) {
		String editBadgeLinkId = "edit_badge_" + bc.badgeClass().getUuid();
		FormLink editBadgeLink = null;
		if (bc.badgeClass().getStatus().equals(BadgeClass.BadgeClassStatus.preparation)) {
			editBadgeLink = uifactory.addFormLink(editBadgeLinkId, CMD_EDIT_BADGE, "form.edit.badge",
					null, flc, Link.BUTTON);
			editBadgeLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		}
		
		String createNewVersionLinkId = "create_new_badge_" + bc.badgeClass().getUuid();
		FormLink createNewVersionLink = null;
		if (bc.badgeClass().getStatus().equals(BadgeClass.BadgeClassStatus.active)) {
			createNewVersionLink = uifactory.addFormLink(createNewVersionLinkId, CMD_CREATE_NEW_VERSION, 
					"create.a.new.version.and.edit", null, flc, Link.BUTTON);
			createNewVersionLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		}

		String awardManuallyLinkId = "award_manually_" + bc.badgeClass().getUuid();
		FormLink awardManuallyLink = uifactory.addFormLink(awardManuallyLinkId, CMD_AWARD_MANUALLY,
				"award.manually", null, flc,  Link.BUTTON);
		awardManuallyLink.setIconLeftCSS("o_icon o_icon-fw o_icon_badge");

		String criteriaXmlString = bc.badgeClass().getCriteria();
		CriteriaViewController criteriaViewController = new CriteriaViewController(ureq, getWindowControl(), entry,
				courseNode, criteriaXmlString);
		String criteriaComponentName = "criteria_" + bc.badgeClass().getUuid();
		listenTo(criteriaViewController);
		detailsVC.put(criteriaComponentName, criteriaViewController.getInitialComponent());

		if (editBadgeLink != null) {
			detailsVC.put(editBadgeLinkId, editBadgeLink.getComponent());
		}
		
		if (createNewVersionLink != null) {
			detailsVC.put(createNewVersionLinkId, createNewVersionLink.getComponent());
		}

		detailsVC.put(awardManuallyLinkId, awardManuallyLink.getComponent());

		FormLink toolLink = ActionsColumnModel.createLink(uifactory, getTranslator());
		BadgeClassRow row = new BadgeClassRow(bc, toolLink, criteriaComponentName, editBadgeLink, awardManuallyLink,
				createNewVersionLink);
		toolLink.setUserObject(row);
		if (editBadgeLink != null) {
			editBadgeLink.setUserObject(row);
		}
		if (createNewVersionLink != null) {
			createNewVersionLink.setUserObject(row);
		}
		awardManuallyLink.setUserObject(row);
		return row;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createLink) {
			doCreate(ureq);
		} else if (source == tableEl) {
			if (event instanceof SelectionEvent selectionEvent) {
				String command = selectionEvent.getCommand();
				BadgeClassRow row = tableModel.getObject(selectionEvent.getIndex());
				if (CMD_SELECT.equals(command)) {
					doSelect(ureq, row, false);
				} else if (CMD_AWARDED_COUNT.equals(command)) {
					doSelect(ureq, row, true);
				}
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doCreate(ureq);
			}
		} else if (source instanceof FormLink link) {
			if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof BadgeClassRow row) {
				doOpenTools(ureq, link, row);
			} else if (CMD_EDIT_BADGE.equals(link.getCmd()) && link.getUserObject() instanceof BadgeClassRow row) {
				doEdit(ureq, row);
			} else if (CMD_CREATE_NEW_VERSION.equals(link.getCmd()) && link.getUserObject() instanceof BadgeClassRow row) {
				doCreateNewVersionAndEdit(ureq, row);
			} else if (CMD_AWARD_MANUALLY.equals(link.getCmd()) && link.getUserObject() instanceof BadgeClassRow row) {
				doAwardManually(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doAwardManually(UserRequest ureq, BadgeClassRow row) {
		BadgeClass reloadedBadgeClass = openBadgesManager.getBadgeClassByKey(row.badgeClassWithSizeAndCount().badgeClass().getKey());

		if (reloadedBadgeClass.getEntry() == null) {
			Step start = new IssueGlobalBadge01Step(ureq);
			IssueGlobalBadgeFinish finish = new IssueGlobalBadgeFinish(reloadedBadgeClass.getRootId(), openBadgesManager, getIdentity());
			String title = translate("award.global.badge", reloadedBadgeClass.getNameWithScan());
			issueGlobalBadgeWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, title, "o_sel_award_global_badge_manually_wizard");
			listenTo(issueGlobalBadgeWizard);
			getWindowControl().pushAsModalDialog(issueGlobalBadgeWizard.getInitialComponent());
		} else {
			issueCourseBadgeCtrl = new IssueCourseBadgeController(ureq, getWindowControl(), reloadedBadgeClass);
			listenTo(issueCourseBadgeCtrl);

			String title = translate("issueBadge");
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					issueCourseBadgeCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}

	private void doOpenTools(UserRequest ureq, FormLink link, BadgeClassRow row) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}

	private class ToolsController extends BasicController {

		private Link awardManuallyLink;
		private Link createNewVersionLink;
		private Link editLink;
		private final Link deleteLink;
		private Link revokeLink;
		private final Link copyLink;
		private final BadgeClassRow row;

		protected ToolsController(UserRequest ureq, WindowControl wControl, BadgeClassRow row) {
			super(ureq, wControl);
			this.row = row;

			VelocityContainer mainVC = createVelocityContainer("badge_class_tools");

			BadgeClass badgeClass = row.badgeClassWithSizeAndCount().badgeClass();
			
			if (badgeClass.getStatus().equals(BadgeClass.BadgeClassStatus.active)) {
				createNewVersionLink = LinkFactory.createLink("create.a.new.version.and.edit", "createNewVersion",
						getTranslator(), mainVC, this, Link.LINK);
				createNewVersionLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
				mainVC.put("createNewVersion",  createNewVersionLink);
			} else if (badgeClass.getStatus().equals(BadgeClass.BadgeClassStatus.preparation)) {
				editLink = LinkFactory.createLink("edit", "edit", getTranslator(), mainVC, this,
						Link.LINK);
				editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
				mainVC.put("edit", editLink);
			}
			
			switch (badgeClass.getStatus()) {
				case preparation:
				case active:
					awardManuallyLink = LinkFactory.createLink("award.manually", "awardManually", getTranslator(), mainVC, this, Link.LINK);
					awardManuallyLink.setIconLeftCSS("o_icon o_icon-fw o_icon_badge");
					mainVC.put("awardManually",  awardManuallyLink);
					break;
				default:
					break;
			}

			deleteLink = LinkFactory.createLink("table.delete.text", "delete", getTranslator(), mainVC,
					this, Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			mainVC.put("delete", deleteLink);

			if (row.badgeClassWithSizeAndCount().count() > 0 &&
					openBadgesManager.unrevokedBadgeAssertionsExist(badgeClass)) {
				revokeLink = LinkFactory.createLink("table.revoke", "remove", getTranslator(), mainVC,
						this, Link.LINK);
				revokeLink.setIconLeftCSS("o_icon o_icon-fw o_icon_history");
				mainVC.put("revoke", revokeLink);
			}

			copyLink = LinkFactory.createLink("copy", "copy", getTranslator(), mainVC, this, Link.LINK);
			copyLink.setIconLeftCSS("o_icon o_icon-fw o_icon_copy");
			mainVC.put("copy", copyLink);

			putInitialPanel(mainVC);
		}


		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if (source == createNewVersionLink) {
				doCreateNewVersion(ureq, row);
			} else if (source == editLink) {
				doEdit(ureq, row);
		    } else if (source == deleteLink) {
				doConfirmDelete(ureq, row);
			} else if (source == revokeLink) {
				doConfirmRevoke(ureq, row);
			} else if (source == copyLink) {
				doCopy(ureq, row);
			} else if (source == awardManuallyLink) {
				doAwardManually(ureq, row);
			}
		}
	}

	private void doCreateNewVersion(UserRequest ureq, BadgeClassRow row) {
		doCreateNewVersionAndEdit(ureq, row);
	}

	private void doCopy(UserRequest ureq, BadgeClassRow row) {
		openBadgesManager.copyBadgeClass(row.badgeClassWithSizeAndCount().badgeClass().getKey(),
				getTranslator(), getIdentity());

		loadModel(ureq);
	}

	private void doCreate(UserRequest ureq) {
		createBadgeClassContext = new CreateBadgeClassWizardContext(entry, courseNode, reSecurity, getTranslator());
		Step start = createBadgeClassContext.showStartingPointStep(getIdentity()) ?
				new CreateBadge00StartingPointStep(ureq, createBadgeClassContext) :
				new CreateBadge01ImageStep(ureq, createBadgeClassContext);

		StepRunnerCallback finish = (innerUreq, innerWControl, innerRunContext) -> {
			BadgeClass badgeClass = createBadgeClass(createBadgeClassContext);
			openBadgesManager.issueBadgeManually(badgeClass, createBadgeClassContext.getEarners(), getIdentity());
			loadModel(innerUreq);
			return StepsMainRunController.DONE_MODIFIED;
		};

		stepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate(createKey), "o_sel_create_badge_wizard");
		listenTo(stepsController);
		getWindowControl().pushAsModalDialog(stepsController.getInitialComponent());
	}

	private BadgeClass createBadgeClass(CreateBadgeClassWizardContext createContext) {
		BadgeClass badgeClass = createContext.getBadgeClass();
		createContext.updateImage(openBadgesManager, badgeClass, getIdentity());

		if (badgeClass instanceof BadgeClassImpl badgeClassImpl) {
			openBadgesManager.createBadgeClass(badgeClassImpl);
		}

		return openBadgesManager.getBadgeClassByUuid(badgeClass.getUuid());
	}

	private void doEdit(UserRequest ureq, BadgeClassRow row) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClassByUuid(row.badgeClassWithSizeAndCount().badgeClass().getUuid());
		if (badgeClass.getStatus() != BadgeClass.BadgeClassStatus.preparation) {
			showError("warning.badge.cannot.be.edited");
			return;
		}
		doEdit(ureq, badgeClass, false);
	}
	
	private void doEdit(UserRequest ureq, BadgeClass badgeClass, boolean newVersion) {
		createBadgeClassContext = new CreateBadgeClassWizardContext(badgeClass, reSecurity, getTranslator(),
				newVersion ? Mode.editNewVersion : Mode.edit);

		Step start;
		if (createBadgeClassContext.isEditWithVersion()) {
			start = new CreateBadge01ImageStep(ureq, createBadgeClassContext);
		} else {
			start = new CreateBadge03CriteriaStep(ureq, createBadgeClassContext);
		}

		StepRunnerCallback finish = (innerUreq, innerWControl, innerRunContext) -> {
			BadgeClass updatedBadgeClass = openBadgesManager.updateBadgeClass(createBadgeClassContext.getBadgeClass());
			updateImage(createBadgeClassContext, updatedBadgeClass);
			openBadgesManager.issueBadgeManually(updatedBadgeClass, createBadgeClassContext.getEarners(), getIdentity());
			registerMapper(innerUreq);
			loadModel(innerUreq);
			return StepsMainRunController.DONE_MODIFIED;
		};

		stepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate(newVersion ? "form.create.new.badge.version" : "form.edit.badge"),
				"o_sel_edit_badge_wizard");
		listenTo(stepsController);
		getWindowControl().pushAsModalDialog(stepsController.getInitialComponent());
	}
	
	private void doCreateNewVersionAndEdit(UserRequest ureq, BadgeClassRow row) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClassByKey(row.badgeClassWithSizeAndCount().badgeClass().getKey());
		openBadgesManager.createNewBadgeClassVersion(badgeClass.getKey(), getIdentity());
		BadgeClass reloadedBadgeClass = openBadgesManager.getCurrentBadgeClass(badgeClass.getRootId());
		if (reloadedBadgeClass == null) {
			return;
		}
		doEdit(ureq, reloadedBadgeClass, true);
	}

	private void updateImage(CreateBadgeClassWizardContext createContext, BadgeClass badgeClass) {
		if (!createContext.isEditWithVersion() || !createContext.imageWasSelected()) {
			return;
		}
		if (createContext.updateImage(openBadgesManager, badgeClass, getIdentity())) {
			openBadgesManager.updateBadgeClass(badgeClass);
		}
	}

	private void doConfirmDelete(UserRequest ureq, BadgeClassRow row) {
		BadgeClass badgeClass = row.badgeClassWithSizeAndCount().badgeClass();
		long totalUseCount = row.badgeClassWithSizeAndCount().totalUseCount();
		doConfirmDelete(ureq, badgeClass, totalUseCount);
	}

	private void doConfirmRevoke(UserRequest ureq, BadgeClassRow row) {
		String name = row.badgeClassWithSizeAndCount().badgeClass().getNameWithScan();
		String title = translate("confirm.revoke.issued.badges.title", name);
		String text = translate("confirm.revoke.issued.badges.text", name);
		confirmRevokeAllBadgesCtrl = activateOkCancelDialog(ureq, title, text, confirmRevokeAllBadgesCtrl);
		confirmRevokeAllBadgesCtrl.setUserObject(row.badgeClassWithSizeAndCount().badgeClass());
	}

	private void doConfirmDelete(UserRequest ureq, BadgeClass badgeClass, long totalUseCount) {
		String name = badgeClass.getNameWithScan();
		String title = translate("confirm.delete.title", name);

		confirmDeleteCtrl = new ConfirmDeleteBadgeClassController(ureq, getWindowControl(), badgeClass, totalUseCount);
		listenTo(confirmDeleteCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), 
				confirmDeleteCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doSelect(UserRequest ureq, BadgeClassRow row, boolean showRecipientsTab) {
		BadgeClass badgeClass = row.badgeClassWithSizeAndCount().badgeClass();
		Long key = badgeClass.getKey();
		String name = badgeClass.getNameWithScan();
		doSelect(ureq, key, name, showRecipientsTab);
	}

	private void doSelect(UserRequest ureq, Long key, String name, boolean showRecipientsTab) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Badge", key);
		WindowControl swControl = addToHistory(ureq, ores, null);
		badgeDetailsController = new BadgeDetailsController(ureq, swControl, key, reSecurity);
		listenTo(badgeDetailsController);
		breadcrumbPanel.pushController(name, badgeDetailsController);
		if (showRecipientsTab) {
			badgeDetailsController.showRecipientsTab(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			cleanUp();
		} else if (source == issueGlobalBadgeWizard) {
			if (event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					loadModel(ureq);
				}
			}
			cleanUp();
		} else if (source == issueCourseBadgeCtrl) {
			cmc.deactivate();
			cleanUp();
			if (event == Event.DONE_EVENT) {
				loadModel(ureq);
			}
		} else if (source == toolsCtrl) {
			if (calloutCtrl != null) {
				calloutCtrl.deactivate();
			}
			cleanUp();
		} else if (source == stepsController) {
			if (event == Event.CANCELLED_EVENT || event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				if (Event.CANCELLED_EVENT.equals(event)) {
					createBadgeClassContext.cancel();
				}
				getWindowControl().pop();
				removeAsListenerAndDispose(stepsController);
			}
		} else if (source == confirmDeleteCtrl) {
			if (event == Event.DONE_EVENT) {
				BadgeClass badgeClass = confirmDeleteCtrl.getBadgeClass();
				switch (confirmDeleteCtrl.getMode()) {
					case unused:
						doDelete(ureq, badgeClass);
						showInfo("confirm.delete.unused.class.info");
						break;
					case revoke:
						doMarkDeletedAndRevokeIssuedBadges(ureq, badgeClass);
						showInfo("confirm.delete.used.class.option1.info");
						break;
					case remove:
						doDelete(ureq, badgeClass);
						showInfo("confirm.delete.used.class.option2.info");
						break;
				}
				loadModel(ureq);
			}
			cleanUp();
		} else if (source == confirmRevokeAllBadgesCtrl) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				BadgeClass badgeClass = (BadgeClass) confirmRevokeAllBadgesCtrl.getUserObject();
				doRevoke(badgeClass);
				showInfo("confirm.revoke.issued.badges.info");
				loadModel(ureq);
			}
		} else if (source == badgeDetailsController) {
			if (event == FormEvent.BACK_EVENT) {
				breadcrumbPanel.popUpToRootController(ureq);
			}
			if (event == Event.CHANGED_EVENT) {
				String name = badgeDetailsController.getName();
				breadcrumbPanel.changeDisplayname(name);
				registerMapper(ureq);
				loadModel(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(issueGlobalBadgeWizard);
		removeAsListenerAndDispose(issueCourseBadgeCtrl);
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(confirmDeleteCtrl);
		calloutCtrl = null;
		toolsCtrl = null;
		issueGlobalBadgeWizard = null;
		issueCourseBadgeCtrl = null;
		cmc = null;
		confirmDeleteCtrl = null;
	}

	private void doMarkDeletedAndRevokeIssuedBadges(UserRequest ureq, BadgeClass badgeClass) {
		openBadgesManager.markDeletedAndRevokeIssuedBadges(badgeClass, true);
		loadModel(ureq);
	}

	private void doDelete(UserRequest ureq, BadgeClass badgeClass) {
		openBadgesManager.deleteBadgeClassAndAssertions(badgeClass, true);
		loadModel(ureq);
	}

	private void doRevoke(BadgeClass badgeClass) {
		openBadgesManager.revokeBadgeAssertions(badgeClass, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) {
			return;
		}
		OLATResourceable olatResourceable = entries.get(0).getOLATResourceable();
		if ("Badge".equalsIgnoreCase(olatResourceable.getResourceableTypeName())) {
			Long key = olatResourceable.getResourceableId();
			BadgeClass badgeClass = openBadgesManager.getBadgeClassByKey(key);
			if (badgeClass == null) {
				return;
			}
			if (entry != null && badgeClass.getEntry().getKey().equals(entry.getKey())) {
				doSelect(ureq, key, badgeClass.getNameWithScan(), false);
			}
			if (entry == null && badgeClass.getEntry() == null) {
				doSelect(ureq, key, badgeClass.getNameWithScan(), false);
			}
		}
	}

	private class BadgeClassMediaFileMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			VFSLeaf classFileLeaf = openBadgesManager.getBadgeClassVfsLeaf(relPath);
			if (classFileLeaf != null) {
				return new VFSMediaResource(classFileLeaf);
			}
			return new NotFoundMediaResource();
		}
	}
}

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
import java.util.Arrays;
import java.util.List;

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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
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
import org.olat.core.gui.control.generic.modal.ButtonClickedEvent;
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
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-05-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeClassesController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {

	private static final String CMD_SELECT = "select";
	private static final String CMD_TOOLS = "tools";
	private static final String CMD_EDIT_BADGE = "editBadge";
	private static final String CMD_AWARD_MANUALLY = "awardManually";

	private final RepositoryEntry entry;
	private final CourseNode courseNode;
	private final RepositoryEntrySecurity reSecurity;
	private final BreadcrumbPanel breadcrumbPanel;
	private final String createKey;
	private final String editKey;
	private BadgeClassTableModel tableModel;
	private FlexiTableElement tableEl;
	private FormLink createLink;
	private CreateBadgeClassWizardContext createBadgeClassContext;
	private DialogBoxController confirmDeleteUnusedClassCtrl;
	private DialogBoxController confirmDeleteUsedClassCtrl;
	private DialogBoxController confirmRevokeAllBadgesCtrl;
	private StepsMainRunController stepsController;
	private BadgeDetailsController badgeDetailsController;
	private CloseableCalloutWindowController calloutCtrl;
	private ToolsController toolsCtrl;
	private VelocityContainer detailsVC;
	private IssueGlobalBadgeController issueGlobalBadgeCtrl;
	private IssueCourseBadgeController issueCourseBadgeCtrl;
	private CloseableModalController cmc;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	public BadgeClassesController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
								  CourseNode courseNode, RepositoryEntrySecurity reSecurity,
								  BreadcrumbPanel breadcrumbPanel, String contextHelp, String createKey, String editKey) {
		super(ureq, wControl, "badge_classes");
		flc.contextPut("contextHelp", contextHelp);
		flc.contextPut("title", entry == null ? translate("form.global.badges") : translate("badges"));
		this.entry = entry;
		this.courseNode = courseNode;
		this.reSecurity = reSecurity;
		this.breadcrumbPanel = breadcrumbPanel;
		this.createKey = createKey;
		this.editKey = editKey;
		initForm(ureq);
		loadModel(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String mediaUrl = registerMapper(ureq, new BadgeClassMediaFileMapper());

		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassTableModel.BadgeClassCols.image,
				(renderer, sb, val, row, source, ubu, translator) -> {
					Size targetSize = tableModel.getObject(row).badgeClassWithSizeAndCount().fitIn(60, 60);
					int width = targetSize.getWidth();
					int height = targetSize.getHeight();
					sb.append("<div style='width: ").append(width).append("px; height: ").append(height).append("px;'>");
					sb.append("<div class='o_image'>");
					if (val instanceof String image) {
						sb.append("<img src=\"");
						sb.append(mediaUrl).append("/").append(image).append("\" ");
						sb.append(" width='").append(width).append("px' height='").append(height).append("px' >");
					}
					sb.append("</div>");
					sb.append("</div>");
				}));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassTableModel.BadgeClassCols.name, CMD_SELECT));
		if (OpenBadgesUIFactory.isSpecifyVersion()) {
			columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassTableModel.BadgeClassCols.version));
		}
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassTableModel.BadgeClassCols.creationDate));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassTableModel.BadgeClassCols.status, new BadgeClassStatusRenderer()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassTableModel.BadgeClassCols.type));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassTableModel.BadgeClassCols.awardedCount, CMD_SELECT));

		boolean owner = reSecurity == null || reSecurity.isOwner();
		if (owner) {
			StickyActionColumnModel toolsColumn = new StickyActionColumnModel(
					BadgeClassTableModel.BadgeClassCols.tools.i18nHeaderKey(),
					BadgeClassTableModel.BadgeClassCols.tools.ordinal()
			);
			columnModel.addFlexiColumnModel(toolsColumn);
		}

		tableModel = new BadgeClassTableModel(columnModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 10, true,
				getTranslator(), formLayout);

		detailsVC = createVelocityContainer("badge_class_details");
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
		if (owner) {
			tableEl.setEmptyTableSettings("empty.badges.table.owner", null,
					"o_icon_badge", "form.create.new.badge", "o_icon_add",
					false);
		} else {
			tableEl.setEmptyTableSettings("empty.badges.table", null,
					"o_icon_badge", null, null,
					false);
		}

		createLink = uifactory.addFormLink("create", createKey, null, formLayout, Link.BUTTON);
		createLink.setElementCssClass("o_sel_badge_classes_create");

		createLink.setVisible(owner);
	}

	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		return components;
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
		String toolId = "tool_" + bc.badgeClass().getUuid();
		FormLink toolLink = (FormLink) flc.getComponent(toolId);
		if (toolLink == null) {
			toolLink = uifactory.addFormLink(toolId, CMD_TOOLS, "", tableEl,
					Link.LINK | Link.NONTRANSLATED);
			toolLink.setTranslator(getTranslator());
			toolLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolLink.setTitle(translate("table.header.actions"));
		}

		String editBadgeLinkId = "edit_badge_" + bc.badgeClass().getUuid();
		FormLink editBadgeLink = null;
		if (bc.badgeClass().getStatus().equals(BadgeClass.BadgeClassStatus.preparation)) {
			editBadgeLink = uifactory.addFormLink(editBadgeLinkId, CMD_EDIT_BADGE, "form.edit.badge",
					null, flc, Link.BUTTON);
		}

		String awardManuallyLinkId = "award_manually_" + bc.badgeClass().getUuid();
		FormLink awardManuallyLink = uifactory.addFormLink(awardManuallyLinkId, CMD_AWARD_MANUALLY,
				"award.manually", null, flc,  Link.BUTTON);

		String criteriaXmlString = bc.badgeClass().getCriteria();
		CriteriaViewController criteriaViewController = new CriteriaViewController(ureq, getWindowControl(), entry,
				courseNode, criteriaXmlString);
		String criteriaComponentName = "criteria_" + bc.badgeClass().getUuid();
		listenTo(criteriaViewController);
		detailsVC.put(criteriaComponentName, criteriaViewController.getInitialComponent());

		if (editBadgeLink != null) {
			detailsVC.put(editBadgeLinkId, editBadgeLink.getComponent());
		}

		detailsVC.put(awardManuallyLinkId, awardManuallyLink.getComponent());

		BadgeClassRow row = new BadgeClassRow(bc, toolLink, criteriaComponentName, editBadgeLink, awardManuallyLink);
		toolLink.setUserObject(row);
		if (editBadgeLink != null) {
			editBadgeLink.setUserObject(row);
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
					doSelect(ureq, row);
				}
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doCreate(ureq);
			}
		} else if (source instanceof FormLink link) {
			if (CMD_TOOLS.equals(link.getCmd()) && link.getUserObject() instanceof BadgeClassRow row) {
				doOpenTools(ureq, link, row);
			} else if (CMD_EDIT_BADGE.equals(link.getCmd()) && link.getUserObject() instanceof BadgeClassRow row) {
				doEdit(ureq, row);
			} else if (CMD_AWARD_MANUALLY.equals(link.getCmd()) && link.getUserObject() instanceof BadgeClassRow row) {
				doAwardManually(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doAwardManually(UserRequest ureq, BadgeClassRow row) {
		BadgeClass reloadedBadgeClass = openBadgesManager.getBadgeClass(row.badgeClassWithSizeAndCount().badgeClass().getKey());

		if (reloadedBadgeClass.getEntry() == null) {
			issueGlobalBadgeCtrl = new IssueGlobalBadgeController(ureq, getWindowControl(), reloadedBadgeClass);
			listenTo(issueGlobalBadgeCtrl);

			String title = translate("issueGlobalBadge");
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					issueGlobalBadgeCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
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

		private Link editLink;
		private final Link deleteLink;
		private Link revokeLink;
		private final Link copyLink;
		private final BadgeClassRow row;

		protected ToolsController(UserRequest ureq, WindowControl wControl, BadgeClassRow row) {
			super(ureq, wControl);
			this.row = row;

			VelocityContainer mainVC = createVelocityContainer("badge_class_tools");

			if (row.badgeClassWithSizeAndCount().badgeClass().getStatus().equals(BadgeClass.BadgeClassStatus.preparation)) {
				editLink = LinkFactory.createLink("edit", "edit", getTranslator(), mainVC, this,
						Link.LINK);
				editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
				mainVC.put("edit", editLink);
			}

			deleteLink = LinkFactory.createLink("table.delete.text", "delete", getTranslator(), mainVC,
					this, Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			mainVC.put("delete", deleteLink);

			if (row.badgeClassWithSizeAndCount().count() > 0 &&
					openBadgesManager.unrevokedBadgeAssertionsExist(row.badgeClassWithSizeAndCount().badgeClass())) {
				revokeLink = LinkFactory.createLink("table.revoke", "remove", getTranslator(), mainVC,
						this, Link.LINK);
				revokeLink.setIconLeftCSS("o_icon o_icon-fw o_icon_revoke");
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
		    if (source == editLink) {
				doEdit(ureq, row);
		    } else if (source == deleteLink) {
				doConfirmDelete(ureq, row);
			} else if (source == revokeLink) {
				doConfirmRevoke(ureq, row);
			} else if (source == copyLink) {
				doCopy(ureq, row);
			}
		}
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
			openBadgesManager.issueBadge(badgeClass, createBadgeClassContext.getEarners(), getIdentity());
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
		if (createContext.selectedTemplateIsSvg()) {
			String image = openBadgesManager.createBadgeClassImageFromSvgTemplate(badgeClass.getUuid(),
					createContext.getSelectedTemplateKey(), createContext.getBackgroundColorId(),
					createContext.getTitle(), getIdentity());
			badgeClass.setImage(image);
		} else if (createContext.selectedTemplateIsPng()) {
			String image = openBadgesManager.createBadgeClassImageFromPngTemplate(badgeClass.getUuid(),
					createContext.getSelectedTemplateKey());
			badgeClass.setImage(image);
		} else if (createContext.ownFileIsSvg() || createContext.ownFileIsPng()) {
			String image = openBadgesManager.createBadgeClassImage(badgeClass.getUuid(),
					createContext.getTemporaryBadgeImageFile(), createContext.getTargetBadgeImageFileName(),
					getIdentity());
			badgeClass.setImage(image);
		}

		if (badgeClass instanceof BadgeClassImpl badgeClassImpl) {
			openBadgesManager.createBadgeClass(badgeClassImpl);
		}

		return openBadgesManager.getBadgeClass(badgeClass.getUuid());
	}

	private void doEdit(UserRequest ureq, BadgeClassRow row) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClass(row.badgeClassWithSizeAndCount().badgeClass().getUuid());
		if (badgeClass.getStatus() != BadgeClass.BadgeClassStatus.preparation) {
			showError("warning.badge.cannot.be.edited");
			return;
		}
		createBadgeClassContext = new CreateBadgeClassWizardContext(badgeClass, reSecurity);
		Step start = new CreateBadge03CriteriaStep(ureq, createBadgeClassContext);

		StepRunnerCallback finish = (innerUreq, innerWControl, innerRunContext) -> {
			BadgeClass updatedBadgeClass = openBadgesManager.updateBadgeClass(createBadgeClassContext.getBadgeClass());
			openBadgesManager.issueBadge(updatedBadgeClass, createBadgeClassContext.getEarners(), getIdentity());
			loadModel(innerUreq);
			return StepsMainRunController.DONE_MODIFIED;
		};

		stepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate(editKey), "o_sel_edit_badge_wizard");
		listenTo(stepsController);
		getWindowControl().pushAsModalDialog(stepsController.getInitialComponent());
	}

	private void doConfirmDelete(UserRequest ureq, BadgeClassRow row) {
		BadgeClass badgeClass = row.badgeClassWithSizeAndCount().badgeClass();
		if (row.badgeClassWithSizeAndCount().count() == 0) {
			doConfirmDeleteUnusedClass(ureq, badgeClass);
		} else {
			doConfirmDeleteUsedClass(ureq, badgeClass);
		}
	}

	private void doConfirmRevoke(UserRequest ureq, BadgeClassRow row) {
		String name = row.badgeClassWithSizeAndCount().badgeClass().getNameWithScan();
		String title = translate("confirm.revoke.issued.badges.title", name);
		String text = translate("confirm.revoke.issued.badges.text", name);
		confirmRevokeAllBadgesCtrl = activateOkCancelDialog(ureq, title, text, confirmRevokeAllBadgesCtrl);
		confirmRevokeAllBadgesCtrl.setUserObject(row.badgeClassWithSizeAndCount().badgeClass());
	}

	private void doConfirmDeleteUnusedClass(UserRequest ureq, BadgeClass badgeClass) {
		String name = badgeClass.getNameWithScan();
		String title = translate("confirm.delete.unused.class.title", name);
		String text = translate("confirm.delete.unused.class.text", name);
		confirmDeleteUnusedClassCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteUnusedClassCtrl);
		confirmDeleteUnusedClassCtrl.setUserObject(badgeClass);
	}

	private void doConfirmDeleteUsedClass(UserRequest ureq, BadgeClass badgeClass) {
		String name = badgeClass.getNameWithScan();
		StringBuilder sb = new StringBuilder();
		sb.append(translate("confirm.delete.used.class.text", name));
		sb.append("<br/><br/>");
		sb.append("<b>").append(translate("confirm.delete.used.class.option1.title")).append("</b><br/>");
		sb.append(translate("confirm.delete.used.class.option1.text")).append("<br/><br/>");
		sb.append("<b>").append(translate("confirm.delete.used.class.option2.title")).append("</b><br/>");
		sb.append(translate("confirm.delete.used.class.option2.text"));
		String title = translate("confirm.delete.used.class.title", name);
		List<String> buttonLabels = Arrays.asList(
				translate("confirm.delete.used.class.option1.title"),
				translate("confirm.delete.used.class.option2.title"),
				translate("cancel")
		);
		confirmDeleteUsedClassCtrl = activateGenericDialog(ureq, title, sb.toString(), buttonLabels, confirmDeleteUsedClassCtrl);
		confirmDeleteUsedClassCtrl.setPrimary(0);
		confirmDeleteUsedClassCtrl.setDanger(1);
		confirmDeleteUsedClassCtrl.setUserObject(badgeClass);
	}

	private void doSelect(UserRequest ureq, BadgeClassRow row) {
		BadgeClass badgeClass = row.badgeClassWithSizeAndCount().badgeClass();
		Long key = badgeClass.getKey();
		String name = badgeClass.getNameWithScan();
		doSelect(ureq, key, name);
	}

	private void doSelect(UserRequest ureq, Long key, String name) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Badge", key);
		WindowControl swControl = addToHistory(ureq, ores, null);
		badgeDetailsController = new BadgeDetailsController(ureq, swControl, key, reSecurity);
		listenTo(badgeDetailsController);
		breadcrumbPanel.pushController(name, badgeDetailsController);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			cleanUp();
		} else if (source == issueGlobalBadgeCtrl) {
			cmc.deactivate();
			cleanUp();
			if (event == Event.DONE_EVENT) {
				loadModel(ureq);
			}
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
				getWindowControl().pop();
				removeAsListenerAndDispose(stepsController);
			}
		} else if (source == confirmDeleteUnusedClassCtrl) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				BadgeClass badgeClass = (BadgeClass) confirmDeleteUnusedClassCtrl.getUserObject();
				doDelete(ureq, badgeClass);
				showInfo("confirm.delete.unused.class.info");
				loadModel(ureq);
			}
		} else if (source == confirmDeleteUsedClassCtrl && event instanceof ButtonClickedEvent buttonClickedEvent) {
			BadgeClass badgeClass = (BadgeClass) confirmDeleteUsedClassCtrl.getUserObject();
			if (buttonClickedEvent.getPosition() == 0) {
				doMarkDeletedAndRevokeIssuedBadges(ureq, badgeClass);
				showInfo("confirm.delete.used.class.option1.info");
			} else if (buttonClickedEvent.getPosition() == 1) {
				doDelete(ureq, badgeClass);
				showInfo("confirm.delete.used.class.option2.info");
			}
			loadModel(ureq);
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
				loadModel(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(issueGlobalBadgeCtrl);
		removeAsListenerAndDispose(issueCourseBadgeCtrl);
		removeAsListenerAndDispose(cmc);
		calloutCtrl = null;
		toolsCtrl = null;
		issueGlobalBadgeCtrl = null;
		issueCourseBadgeCtrl = null;
		cmc = null;
	}

	private void doMarkDeletedAndRevokeIssuedBadges(UserRequest ureq, BadgeClass badgeClass) {
		badgeClass.setStatus(BadgeClass.BadgeClassStatus.deleted);
		openBadgesManager.updateBadgeClass(badgeClass);
		openBadgesManager.revokeBadgeAssertions(badgeClass);
		loadModel(ureq);
	}

	private void doDelete(UserRequest ureq, BadgeClass badgeClass) {
		openBadgesManager.deleteBadgeClassAndAssertions(badgeClass);
		loadModel(ureq);
	}

	private void doRevoke(BadgeClass badgeClass) {
		openBadgesManager.revokeBadgeAssertions(badgeClass);
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
			BadgeClass badgeClass = openBadgesManager.getBadgeClass(key);
			if (badgeClass == null) {
				return;
			}
			if (entry != null && badgeClass.getEntry().getKey().equals(entry.getKey())) {
				doSelect(ureq, key, badgeClass.getNameWithScan());
			}
			if (entry == null && badgeClass.getEntry() == null) {
				doSelect(ureq, key, badgeClass.getNameWithScan());
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

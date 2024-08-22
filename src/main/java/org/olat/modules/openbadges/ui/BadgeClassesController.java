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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
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
public class BadgeClassesController extends FormBasicController implements Activateable2 {

	private static final String CMD_SELECT = "select";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_TOOLS = "tools";

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
		loadModel();
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
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassTableModel.BadgeClassCols.status));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeClassTableModel.BadgeClassCols.awardedCount, CMD_SELECT));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), CMD_EDIT));

		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(
				BadgeClassTableModel.BadgeClassCols.tools.i18nHeaderKey(),
				BadgeClassTableModel.BadgeClassCols.tools.ordinal()
		);
		columnModel.addFlexiColumnModel(toolsColumn);

		tableModel = new BadgeClassTableModel(columnModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(),
				formLayout);

		createLink = uifactory.addFormLink("create", createKey, null, formLayout, Link.BUTTON);
		createLink.setElementCssClass("o_sel_badge_classes_create");
	}

	private void loadModel() {
		List<BadgeClassRow> rows = openBadgesManager.getBadgeClassesWithSizesAndCounts(entry).stream()
				.filter(bc -> {
					if (courseNode == null) {
						return true;
					}
					return openBadgesManager.conditionForCourseNodeExists(bc.badgeClass(), courseNode.getIdent());
				})
				.map(this::forgeRow).toList();
		tableModel.setObjects(rows);
		tableEl.reset();
	}

	private BadgeClassRow forgeRow(OpenBadgesManager.BadgeClassWithSizeAndCount bc) {
		String toolId = "tool_" + bc.badgeClass().getUuid();
		FormLink toolLink = (FormLink) flc.getComponent(toolId);
		if (toolLink == null) {
			toolLink = uifactory.addFormLink(toolId, CMD_TOOLS, "", tableEl, Link.LINK | Link.NONTRANSLATED);
			toolLink.setTranslator(getTranslator());
			toolLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolLink.setTitle(translate("table.header.actions"));
		}
		BadgeClassRow row = new BadgeClassRow(bc, toolLink);
		toolLink.setUserObject(row);
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
				if (CMD_EDIT.equals(command)) {
					doEdit(ureq, row);
				} else if (CMD_SELECT.equals(command)) {
					doSelect(ureq, row);
				}
			}
		} else if (source instanceof FormLink link) {
			if (CMD_TOOLS.equals(link.getCmd()) && link.getUserObject() instanceof BadgeClassRow row) {
				doOpenTools(ureq, link, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
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

		private final Link deleteLink;
		private Link revokeLink;
		private Link copyLink;
		private final BadgeClassRow row;

		protected ToolsController(UserRequest ureq, WindowControl wControl, BadgeClassRow row) {
			super(ureq, wControl);
			this.row = row;

			VelocityContainer mainVC = createVelocityContainer("badge_class_tools");

			deleteLink = LinkFactory.createLink("table.delete.text", "delete", getTranslator(), mainVC,
					this, Link.LINK);
			mainVC.put("delete", deleteLink);

			if (row.badgeClassWithSizeAndCount().count() > 0 &&
					openBadgesManager.unrevokedBadgeAssertionsExist(row.badgeClassWithSizeAndCount().badgeClass())) {
				revokeLink = LinkFactory.createLink("table.revoke", "remove", getTranslator(), mainVC,
						this, Link.LINK);
				mainVC.put("revoke", revokeLink);
			}

			copyLink = LinkFactory.createLink("copy", "copy", getTranslator(), mainVC, this, Link.LINK);
			mainVC.put("copy", copyLink);

			putInitialPanel(mainVC);
		}


		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if (source == deleteLink) {
				doConfirmDelete(ureq, row);
			} else if (source == revokeLink) {
				doConfirmRevoke(ureq, row);
			} else if (source == copyLink) {
				doCopy(row);
			}
		}
	}

	private void doCopy(BadgeClassRow row) {
		openBadgesManager.copyBadgeClass(row.badgeClassWithSizeAndCount().badgeClass().getKey(),
				getTranslator(), getIdentity());

		loadModel();
	}

	private void doCreate(UserRequest ureq) {
		createBadgeClassContext = new CreateBadgeClassWizardContext(entry, reSecurity);
		Step start = createBadgeClassContext.showStartingPointStep(getIdentity()) ?
				new CreateBadge00StartingPointStep(ureq, createBadgeClassContext) :
				new CreateBadge00ImageStep(ureq, createBadgeClassContext);

		StepRunnerCallback finish = (innerUreq, innerWControl, innerRunContext) -> {
			BadgeClass badgeClass = createBadgeClass(createBadgeClassContext);
			openBadgesManager.issueBadge(badgeClass, createBadgeClassContext.getEarners(), getIdentity());
			loadModel();
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
		Step start = new CreateBadge02DetailsStep(ureq, createBadgeClassContext);

		StepRunnerCallback finish = (innerUreq, innerWControl, innerRunContext) -> {
			BadgeClass updatedBadgeClass = openBadgesManager.updateBadgeClass(createBadgeClassContext.getBadgeClass());
			openBadgesManager.issueBadge(updatedBadgeClass, createBadgeClassContext.getEarners(), getIdentity());
			loadModel();
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
		if (source == toolsCtrl) {
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
				doDelete(badgeClass);
				showInfo("confirm.delete.unused.class.info");
				loadModel();
			}
		} else if (source == confirmDeleteUsedClassCtrl && event instanceof ButtonClickedEvent buttonClickedEvent) {
			BadgeClass badgeClass = (BadgeClass) confirmDeleteUsedClassCtrl.getUserObject();
			if (buttonClickedEvent.getPosition() == 0) {
				doMarkDeletedAndRevokeIssuedBadges(badgeClass);
				showInfo("confirm.delete.used.class.option1.info");
			} else if (buttonClickedEvent.getPosition() == 1) {
				doDelete(badgeClass);
				showInfo("confirm.delete.used.class.option2.info");
			}
			loadModel();
		} else if (source == confirmRevokeAllBadgesCtrl) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				BadgeClass badgeClass = (BadgeClass) confirmRevokeAllBadgesCtrl.getUserObject();
				doRevoke(badgeClass);
				showInfo("confirm.revoke.issued.badges.info");
			}
		} else if (source == badgeDetailsController) {
			if (event == FormEvent.BACK_EVENT) {
				breadcrumbPanel.popUpToRootController(ureq);
			}
			if (event == Event.CHANGED_EVENT) {
				String name = badgeDetailsController.getName();
				breadcrumbPanel.changeDisplayname(name);
				loadModel();
			}
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		calloutCtrl = null;
		toolsCtrl = null;
	}

	private void doMarkDeletedAndRevokeIssuedBadges(BadgeClass badgeClass) {
		badgeClass.setStatus(BadgeClass.BadgeClassStatus.deleted);
		openBadgesManager.updateBadgeClass(badgeClass);
		openBadgesManager.revokeBadgeAssertions(badgeClass);
		loadModel();
	}

	private void doDelete(BadgeClass badgeClass) {
		openBadgesManager.deleteBadgeClassAndAssertions(badgeClass);
		loadModel();
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

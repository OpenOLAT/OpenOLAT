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

import java.util.List;

import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
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
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.repository.RepositoryEntry;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-05-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeClassesController extends FormBasicController implements Activateable2 {

	private final static String CMD_SELECT = "select";
	private static final String CMD_DELETE = "delete";
	private static final String CMD_EDIT = "edit";

	private final RepositoryEntry entry;
	private final BreadcrumbedStackedPanel stackPanel;
	private final String addKey;
	private final String editKey;
	private BadgeClassesTableModel tableModel;
	private FlexiTableElement tableEl;
	private FormLink addLink;
	private CreateBadgeClassWizardContext createBadgeClassContext;
	private DialogBoxController confirmDeleteClassCtrl;
	private StepsMainRunController addStepsController;
	private BadgeDetailsController badgeDetailsController;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	public BadgeClassesController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
								  BreadcrumbedStackedPanel stackPanel, String contextHelp, String addKey,
								  String editKey) {
		super(ureq, wControl, "badge_classes");
		flc.contextPut("contextHelp", contextHelp);
		this.entry = entry;
		this.stackPanel = stackPanel;
		this.addKey = addKey;
		this.editKey = editKey;
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String mediaUrl = registerMapper(ureq, new BadgeClassMediaFileMapper());

		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.image,
				(renderer, sb, val, row, source, ubu, translator) -> {
					Size targetSize = tableModel.getObject(row).fitIn(60, 60);
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
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name, CMD_SELECT));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.status));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.awardedCount, CMD_SELECT));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), CMD_EDIT));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), CMD_DELETE));

		tableModel = new BadgeClassesTableModel(columnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(),
				formLayout);

		addLink = uifactory.addFormLink("add", addKey, null, formLayout, Link.BUTTON);
	}

	private void updateUI() {
		List<OpenBadgesManager.BadgeClassWithSizeAndCount> rows = openBadgesManager.getBadgeClassesWithSizesAndCounts(entry);
		tableModel.setObjects(rows);
		tableEl.reset();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addLink) {
			doAdd(ureq);
		} else if (source == tableEl) {
			if (event instanceof SelectionEvent selectionEvent) {
				String command = selectionEvent.getCommand();
				OpenBadgesManager.BadgeClassWithSizeAndCount row = tableModel.getObject(selectionEvent.getIndex());
				if (CMD_EDIT.equals(command)) {
					doEdit(ureq, row);
				} else if (CMD_DELETE.equals(command)) {
					doConfirmDelete(ureq, row);
				} else if (CMD_SELECT.equals(command)) {
					doSelect(ureq, row.badgeClass().getKey(), row.badgeClass().getName());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doAdd(UserRequest ureq) {
		createBadgeClassContext = new CreateBadgeClassWizardContext(entry);
		Step start = new CreateBadge00ImageStep(ureq, createBadgeClassContext);

		StepRunnerCallback finish = (innerUreq, innerWControl, innerRunContext) -> {
			BadgeClass badgeClass = createBadgeClass(createBadgeClassContext);
			if (entry != null) {
				openBadgesManager.issueBadge(badgeClass, createBadgeClassContext.getEarners(), getIdentity());
			}
			updateUI();
			return StepsMainRunController.DONE_MODIFIED;
		};

		addStepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate(addKey), "o_sel_add_badge_wizard");
		listenTo(addStepsController);
		getWindowControl().pushAsModalDialog(addStepsController.getInitialComponent());
	}

	private BadgeClass createBadgeClass(CreateBadgeClassWizardContext createBadgeClassContext) {
		BadgeClass badgeClass = createBadgeClassContext.getBadgeClass();
		if (createBadgeClassContext.getTemporaryBadgeImageFile() != null) {
			String image = openBadgesManager.createBadgeClassImage(createBadgeClassContext.getTemporaryBadgeImageFile(),
					createBadgeClassContext.getTargetBadgeImageFileName(), getIdentity());
			badgeClass.setImage(image);
		} else {
			String image = openBadgesManager.createBadgeClassImageFromSvgTemplate(
					createBadgeClassContext.getSelectedTemplateKey(), createBadgeClassContext.getBackgroundColorId(),
					createBadgeClassContext.getTitle(), getIdentity());
			badgeClass.setImage(image);
		}
		if (badgeClass instanceof BadgeClassImpl badgeClassImpl) {
			openBadgesManager.createBadgeClass(badgeClassImpl);
		}
		return openBadgesManager.getBadgeClass(badgeClass.getUuid());
	}

	private void doEdit(UserRequest ureq, OpenBadgesManager.BadgeClassWithSizeAndCount row) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClass(row.badgeClass().getUuid());
		if (badgeClass.getStatus() != BadgeClass.BadgeClassStatus.preparation) {
			showError("warning.badge.cannot.be.edited");
			return;
		}
		createBadgeClassContext = new CreateBadgeClassWizardContext(badgeClass);
		Step start = new CreateBadge02DetailsStep(ureq, createBadgeClassContext);

		StepRunnerCallback finish = (innerUreq, innerWControl, innerRunContext) -> {
			BadgeClass updatedBadgeClass = openBadgesManager.updateBadgeClass(createBadgeClassContext.getBadgeClass());
			if (entry != null) {
				openBadgesManager.issueBadge(updatedBadgeClass, createBadgeClassContext.getEarners(), getIdentity());
			}
			updateUI();
			return StepsMainRunController.DONE_MODIFIED;
		};

		addStepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate(editKey), "o_sel_add_badge_wizard");
		listenTo(addStepsController);
		getWindowControl().pushAsModalDialog(addStepsController.getInitialComponent());
	}

	private void doConfirmDelete(UserRequest ureq, OpenBadgesManager.BadgeClassWithSizeAndCount row) {
		BadgeClass badgeClass = row.badgeClass();
		if (row.count() > 0) {
			showError("warning.badge.in.use");
			return;
		}
		String title = translate("confirm.delete.class.title", badgeClass.getName());
		String text = translate("confirm.delete.class", badgeClass.getName());
		confirmDeleteClassCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteClassCtrl);
		confirmDeleteClassCtrl.setUserObject(row);
	}

	private void doSelect(UserRequest ureq, Long key, String name) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Badge", key);
		WindowControl swControl = addToHistory(ureq, ores, null);
		badgeDetailsController = new BadgeDetailsController(ureq, swControl, key);
		listenTo(badgeDetailsController);
		stackPanel.pushController(name, badgeDetailsController);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == addStepsController) {
			if (event == Event.CANCELLED_EVENT || event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(addStepsController);
			}
		} else if (source == confirmDeleteClassCtrl) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				OpenBadgesManager.BadgeClassWithSizeAndCount row = (OpenBadgesManager.BadgeClassWithSizeAndCount) confirmDeleteClassCtrl.getUserObject();
				if (row.count() == 0) {
					doDelete(row.badgeClass());
				}
				updateUI();
			}
		}
	}

	private void doDelete(BadgeClass badgeClass) {
		openBadgesManager.deleteBadgeClass(badgeClass);
		updateUI();
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
				doSelect(ureq, key, badgeClass.getName());
			}
			if (entry == null && badgeClass.getEntry() == null) {
				doSelect(ureq, key, badgeClass.getName());
			}
		}
	}

	enum Cols implements FlexiSortableColumnDef {
		image("form.image"),
		name("form.name"),
		status("form.status"),
		awardedCount("form.awarded.to");

		Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		private final String i18nKey;

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

	private class BadgeClassesTableModel extends DefaultFlexiTableDataModel<OpenBadgesManager.BadgeClassWithSizeAndCount> {
		public BadgeClassesTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			BadgeClass badgeClass = getObject(row).badgeClass();
			Long awardedCount = getObject(row).count();
			return switch (Cols.values()[col]) {
				case image -> badgeClass.getImage();
				case name -> badgeClass.getName();
				case status -> translate("class.status." + badgeClass.getStatus().name());
				case awardedCount -> awardedCount;
			};
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

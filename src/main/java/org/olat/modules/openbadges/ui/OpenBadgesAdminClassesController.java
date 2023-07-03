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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-05-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OpenBadgesAdminClassesController extends FormBasicController {

	private ClassTableModel tableModel;
	private FlexiTableElement tableEl;
	private FormLink addLink;
	private CloseableModalController cmc;
	private EditBadgeClassController editClassCtrl;
	private DialogBoxController confirmDeleteClassCtrl;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	protected OpenBadgesAdminClassesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "classes");

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String mediaUrl = registerMapper(ureq, new BadgeClassMediaFileMapper());
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.image.getI18n(), Cols.image.ordinal(),
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
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name.getI18n(), Cols.name.ordinal()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.status.getI18n(), Cols.status.ordinal()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.awardedCount.getI18n(), Cols.awardedCount.ordinal()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));

		tableModel = new ClassTableModel(columnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "classes", tableModel, getTranslator(),
				formLayout);

		addLink = uifactory.addFormLink("add", "class.add", "class.add", formLayout, Link.BUTTON);
		updateUI();
	}

	private void updateUI() {
		List<OpenBadgesManager.BadgeClassWithSizeAndCount> classesWithSizes = openBadgesManager.getBadgeClassesWithSizesAndCounts(null);
		tableModel.setObjects(classesWithSizes);
		tableEl.reset();
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(editClassCtrl);
		cmc = null;
		editClassCtrl = null;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editClassCtrl) {
			cmc.deactivate();
			cleanUp();
			updateUI();
		} else if (source == confirmDeleteClassCtrl) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				BadgeClass badgeClass = (BadgeClass) confirmDeleteClassCtrl.getUserObject();
				if (tableModel.getObjects().stream().filter(b -> b.badgeClass().getKey() == badgeClass.getKey() && b.count() > 0).findFirst().isEmpty()) {
					doDelete(badgeClass);
				}
				updateUI();
			}
		} else if (source == cmc) {
			cleanUp();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addLink) {
			doUpload(ureq);
		} else if (source == tableEl) {
			SelectionEvent selectionEvent = (SelectionEvent)event;
			String command = selectionEvent.getCommand();
			BadgeClass badgeClass = tableModel.getObject(selectionEvent.getIndex()).badgeClass();
			if ("edit".equals(command)) {
				doEdit(ureq, badgeClass);
			} else if ("delete".equals(command)) {
				if (tableModel.getObjects().stream().filter(b -> b.badgeClass().getKey() == badgeClass.getKey() && b.count() > 0).findFirst().isEmpty()) {
					doConfirmDelete(ureq, badgeClass);
				} else {
					showInfo("info.badgeInUse");
				}
			}
		}
	}

	private void doUpload(UserRequest ureq) {
		editClassCtrl = new EditBadgeClassController(ureq, getWindowControl(), null);
		listenTo(editClassCtrl);

		String title = translate("class.add");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editClassCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEdit(UserRequest ureq, BadgeClass badgeClass) {
		editClassCtrl = new EditBadgeClassController(ureq, getWindowControl(), badgeClass);
		listenTo(editClassCtrl);

		String title = translate("class.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editClassCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmDelete(UserRequest ureq, BadgeClass badgeClass) {
		String title = translate("confirm.delete.class.title", badgeClass.getName());
		String text = translate("confirm.delete.class", badgeClass.getName());
		confirmDeleteClassCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteClassCtrl);
		confirmDeleteClassCtrl.setUserObject(badgeClass);
	}

	private void doDelete(BadgeClass badgeClass) {
		openBadgesManager.deleteBadgeClass(badgeClass);
		updateUI();
	}

	enum Cols {
		image("form.image"),
		name("form.name"),
		status("form.status"),
		awardedCount("form.awarded.to");

		Cols(String i18n) {
			this.i18n = i18n;
		}

		private final String i18n;

		public String getI18n() {
			return i18n;
		}
	}

	private class ClassTableModel extends DefaultFlexiTableDataModel<OpenBadgesManager.BadgeClassWithSizeAndCount> {
		public ClassTableModel(FlexiTableColumnModel columnModel) {
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

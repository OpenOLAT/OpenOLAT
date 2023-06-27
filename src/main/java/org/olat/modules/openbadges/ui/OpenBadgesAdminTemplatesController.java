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
import java.util.stream.Collectors;

import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.TagUIFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
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
import org.olat.modules.openbadges.BadgeTemplate;
import org.olat.modules.openbadges.OpenBadgesManager;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-05-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OpenBadgesAdminTemplatesController extends FormBasicController {

	private TemplateDataModel tableModel;
	private FlexiTableElement tableEl;
	private FormLink uploadLink;
	private CloseableModalController cmc;
	private EditBadgeTemplateController editTemplateCtrl;
	private DialogBoxController confirmDeleteTemplateCtrl;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	protected OpenBadgesAdminTemplatesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "templates");

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String mediaUrl = registerMapper(ureq, new BadgeImageMapper());
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
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name, new TextFlexiCellRenderer(EscapeMode.none)));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.tags, new TextFlexiCellRenderer(EscapeMode.none)));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));

		tableModel = new TemplateDataModel(columnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "templates", tableModel, getTranslator(),
				formLayout);

		uploadLink = uifactory.addFormLink("upload", "template.upload", "template.upload", formLayout, Link.BUTTON);
		updateUI();
	}

	private void updateUI() {
		List<OpenBadgesManager.TemplateWithSize> templatesWithSizes = openBadgesManager.getTemplatesWithSizes();
		tableModel.setObjects(templatesWithSizes);
		tableEl.reset();
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(editTemplateCtrl);
		cmc = null;
		editTemplateCtrl = null;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editTemplateCtrl) {
			cmc.deactivate();
			cleanUp();
			updateUI();
		} else if (source == confirmDeleteTemplateCtrl) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				BadgeTemplate template = (BadgeTemplate) confirmDeleteTemplateCtrl.getUserObject();
				doDelete(template);
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
		if (source == uploadLink) {
			doUpload(ureq);
		} else if (source == tableEl) {
			SelectionEvent selectionEvent = (SelectionEvent)event;
			String command = selectionEvent.getCommand();
			BadgeTemplate template = tableModel.getObject(selectionEvent.getIndex()).template();
			if ("edit".equals(command)) {
				doEdit(ureq, template);
			} else if ("delete".equals(command)) {
				doConfirmDelete(ureq, template);
			}
		}
	}

	private void doUpload(UserRequest ureq) {
		editTemplateCtrl = new EditBadgeTemplateController(ureq, getWindowControl(), null);
		listenTo(editTemplateCtrl);

		String title = translate("template.upload");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editTemplateCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEdit(UserRequest ureq, BadgeTemplate template) {
		editTemplateCtrl = new EditBadgeTemplateController(ureq, getWindowControl(), template);
		listenTo(editTemplateCtrl);

		String title = translate("template.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editTemplateCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmDelete(UserRequest ureq, BadgeTemplate template) {
		String title = translate("confirm.delete.template.title", template.getName());
		String text = translate("confirm.delete.template", template.getName());
		confirmDeleteTemplateCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteTemplateCtrl);
		confirmDeleteTemplateCtrl.setUserObject(template);
	}

	private void doDelete(BadgeTemplate template) {
		openBadgesManager.deleteTemplate(template);
		updateUI();
	}

	enum Cols implements FlexiSortableColumnDef {
		image("form.image"),
		name("form.name"),
		tags("form.tags");

		Cols(String i18n) {
			this.i18n = i18n;
		}

		private final String i18n;

		@Override
		public String i18nHeaderKey() {
			return i18n;
		}

		@Override
		public boolean sortable() {
			return this != tags;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

	private class TemplateDataModel extends DefaultFlexiTableDataModel<OpenBadgesManager.TemplateWithSize> {
		public TemplateDataModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			BadgeTemplate template = getObject(row).template();
			List<TagInfo> tagInfos = openBadgesManager.getCategories(template, null);
			List<Tag> tags = tagInfos.stream().filter(TagInfo::isSelected).map(ti -> (Tag)ti).collect(Collectors.toList());
			return switch (Cols.values()[col]) {
				case image -> template.getImage();
				case name -> OpenBadgesUIFactory.translateTemplateName(getTranslator(), template.getIdentifier());
				case tags -> TagUIFactory.getFormattedTags(getLocale(), tags);
			};
		}
	}

	private class BadgeImageMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			VFSLeaf templateLeaf = openBadgesManager.getTemplateVfsLeaf(relPath);
			if (templateLeaf != null) {
				return new VFSMediaResource(templateLeaf);
			}
			return new NotFoundMediaResource();
		}
	}
}

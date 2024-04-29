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
package org.olat.modules.ceditor.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.GalleryElement;
import org.olat.modules.ceditor.model.GallerySettings;
import org.olat.modules.ceditor.model.jpa.GalleryPart;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

/**
 * Initial date: 2024-04-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GalleryEditorController extends FormBasicController implements PageElementEditorController {

	private static final String UP_ACTION = "up";
	private static final String DOWN_ACTION = "down";
	private static final String CMD_TOOLS = "tools";


	private GalleryPart galleryPart;
	private final PageElementStore<GalleryElement> store;
	private GalleryModel tableModel;
	private FlexiTableElement tableEl;
	private FormLink addImageButton;

	public GalleryEditorController(UserRequest ureq, WindowControl wControl, GalleryPart galleryPart,
								   PageElementStore<GalleryElement> store) {
		super(ureq, wControl, "gallery_editor");
		this.galleryPart = galleryPart;
		this.store = store;

		initForm(ureq);
		loadModel();

		setBlockLayoutClass(galleryPart.getSettings());
	}

	private void setBlockLayoutClass(GallerySettings gallerySettings) {
		flc.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(gallerySettings, false));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addImageButton = uifactory.addFormLink("addImage", "addremove.add.text", "", formLayout, Link.BUTTON);
		addImageButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");

		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GalleryModel.GalleryColumn.up.getI18nKey(),
				GalleryModel.GalleryColumn.up.ordinal(), UP_ACTION, new BooleanCellRenderer(
						new StaticFlexiCellRenderer(translate("gallery.up"), UP_ACTION), null
		)));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GalleryModel.GalleryColumn.down.getI18nKey(),
				GalleryModel.GalleryColumn.down.ordinal(), DOWN_ACTION, new BooleanCellRenderer(
						new StaticFlexiCellRenderer(translate("gallery.down"), DOWN_ACTION), null
		)));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GalleryModel.GalleryColumn.title.getI18nKey(),
				GalleryModel.GalleryColumn.title.ordinal()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GalleryModel.GalleryColumn.description.getI18nKey(),
				GalleryModel.GalleryColumn.description.ordinal()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GalleryModel.GalleryColumn.version.getI18nKey(),
				GalleryModel.GalleryColumn.version.ordinal()));
		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(GalleryModel.GalleryColumn.tools.getI18nKey(),
				GalleryModel.GalleryColumn.tools.ordinal());
		toolsColumn.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsColumn.setColumnCssClass("o_icon-fws o_col_sticky_right o_col_action");
		columnModel.addFlexiColumnModel(toolsColumn);

		tableModel = new GalleryModel(columnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "gallery.images", tableModel, getTranslator(), formLayout);
		updateUI();
	}

	private void loadModel() {
		List<GalleryRow> galleryRows = new ArrayList<>();

		tableModel.setObjects(galleryRows);
		tableEl.reset();
		addTools();
	}

	private void addTools() {
		for (GalleryRow galleryRow : tableModel.getObjects()) {
			String toolId = "tool_" + galleryRow.getId();
			FormLink toolLink = (FormLink) tableEl.getFormComponent(toolId);
			if (toolLink == null) {
				toolLink = uifactory.addFormLink(toolId, CMD_TOOLS, "", tableEl,
						Link.LINK | Link.NONTRANSLATED);
				toolLink.setTranslator(getTranslator());
				toolLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
				toolLink.setTitle(translate("gallery.tools"));
			}
			toolLink.setUserObject(galleryRow);
			galleryRow.setToolLink(toolLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof GalleryInspectorController && event instanceof ChangePartEvent changePartEvent &&
				changePartEvent.getElement() instanceof GalleryPart updatedGalleryPart) {
			if (updatedGalleryPart.equals(galleryPart)) {
				galleryPart = updatedGalleryPart;
				updateUI();
				setBlockLayoutClass(galleryPart.getSettings());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void updateUI() {
		flc.contextPut("title", galleryPart.getSettings().getTitle());
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}
}

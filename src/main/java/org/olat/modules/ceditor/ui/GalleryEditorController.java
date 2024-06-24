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

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.manager.PageDAO;
import org.olat.modules.ceditor.model.GalleryElement;
import org.olat.modules.ceditor.model.GallerySettings;
import org.olat.modules.ceditor.model.jpa.GalleryPart;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaToPagePart;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.manager.MediaToPagePartDAO;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-04-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GalleryEditorController extends FormBasicController implements PageElementEditorController, FlexiTableComponentDelegate {

	private static final String UP_ACTION = "up";
	private static final String DOWN_ACTION = "down";
	private static final String CMD_TOOLS = "tools";


	private GalleryPart galleryPart;
	private final PageElementStore<GalleryElement> store;
	private GalleryModel tableModel;
	private FlexiTableElement tableEl;
	private FormLink addImageButton;
	private ChooseImageController chooseImageController;
	private CloseableModalController cmc;
	private ToolsController toolsController;
	private CloseableCalloutWindowController ccwc;
	private DialogBoxController confirmRemoveDialog;
	private ChooseVersionController chooseVersionController;
	private GalleryRunController.GalleryImages galleryImages;

	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaToPagePartDAO mediaToPagePartDAO;
	@Autowired
	private PageDAO pageDAO;
	@Autowired
	private MediaService mediaService;

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
		DefaultFlexiColumnModel titleColumn = new DefaultFlexiColumnModel(GalleryModel.GalleryColumn.title.getI18nKey(),
				GalleryModel.GalleryColumn.title.ordinal());
		titleColumn.setColumnCssClass("o_gallery_image_title");
		columnModel.addFlexiColumnModel(titleColumn);
		columnModel.getColumnModel(columnModel.getColumnCount() - 1).setColumnCssClass("o_gallery_image_title");
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
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer row = createVelocityContainer("gallery_image_row");
		row.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(row, this);
		tableEl.setCssDelegate(new GalleryCssDelegate());

		galleryImages = new GalleryRunController.GalleryImages(new ArrayList<>());
		String mapperUrl = registerCacheableMapper(ureq, "gallery-" + galleryPart.getId(),
				new GalleryRunController.GalleryMapper(galleryPart, galleryImages, mediaService));
		row.contextPut("mapperUrl", mapperUrl);

		updateUI();
	}

	private void loadModel() {
		List<GalleryRow> galleryRows = mediaToPagePartDAO.loadRelations(galleryPart).stream()
				.map(r -> new GalleryRow(getTranslator(), r, r.getMedia(), getMediaVersion(r))).toList();
		List<GalleryRunController.GalleryImageItem> galleryImageItems = galleryRows.stream()
				.map(GalleryRow::getMediaVersion)
				.map(mv -> new GalleryRunController.GalleryImageItem(Long.toString(mv.getKey()),
						mv.getMedia().getType(), mv, mv.getMedia().getTitle(),
						mv.getMedia().getDescription())).toList();
		galleryImages.items().clear();
		galleryImages.items().addAll(galleryImageItems);
		tableModel.setObjects(galleryRows);
		tableEl.reset();
		addTools();
	}

	public static MediaVersion getMediaVersion(MediaToPagePart relation) {
		if (relation.getMediaVersion() != null) {
			return relation.getMediaVersion();
		}
		if (relation.getMedia().getVersions() != null && !relation.getMedia().getVersions().isEmpty()) {
			return relation.getMedia().getVersions().get(0);
		}
		return null;
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
		} else if (cmc == source) {
			cleanUp();
		} else if (chooseImageController == source) {
			if (event == Event.DONE_EVENT) {
				doSaveMedia(ureq, chooseImageController.getMediaReference());
			}
			cmc.deactivate();
			cleanUp();
		} else if (toolsController == source) {
			if (ccwc != null) {
				ccwc.deactivate();
			}
			cleanUp();
		} else if (confirmRemoveDialog == source) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				if (confirmRemoveDialog.getUserObject() instanceof GalleryRow galleryRow) {
					MediaToPagePart relation = mediaToPagePartDAO.loadRelation(galleryRow.getRelation().getKey());
					mediaToPagePartDAO.deleteRelation(relation);
					loadModel();
				}
			}
		} else if (chooseVersionController == source) {
			if (event == Event.DONE_EVENT) {
				doSaveMediaVersion(ureq, chooseVersionController.getGalleryRow(), chooseVersionController.getMediaVersionId());
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(chooseImageController);
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(toolsController);
		removeAsListenerAndDispose(chooseVersionController);
		cmc = null;
		chooseImageController = null;
		ccwc = null;
		toolsController = null;
		chooseVersionController = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (addImageButton == source) {
			doAddImage(ureq);
		} else if (event instanceof SelectionEvent selectionEvent) {
			MediaToPagePart relation = tableModel.getObject(selectionEvent.getIndex()).getRelation();
			if (UP_ACTION.equals(selectionEvent.getCommand())) {
				doMove(ureq, relation, true);
			} else if (DOWN_ACTION.equals(selectionEvent.getCommand())) {
				doMove(ureq, relation, false);
			}
		} else if (source instanceof FormLink link) {
			if (CMD_TOOLS.equals(link.getCmd()) && link.getUserObject() instanceof GalleryRow galleryRow) {
				doOpenTools(ureq, link, galleryRow);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doOpenTools(UserRequest ureq, FormLink link, GalleryRow galleryRow) {
		toolsController = new ToolsController(ureq, getWindowControl(), galleryRow);
		listenTo(toolsController);

		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsController.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(ccwc);
		ccwc.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doAddImage(UserRequest ureq) {
		chooseImageController = new ChooseImageController(ureq, getWindowControl());
		listenTo(chooseImageController);
		String title = translate("choose.image");
		cmc = new CloseableModalController(getWindowControl(), null,
				chooseImageController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doMove(UserRequest ureq, MediaToPagePart relation, boolean up) {
		galleryPart = (GalleryPart) pageDAO.loadPart(galleryPart);
		mediaToPagePartDAO.move(galleryPart, relation, up);
		dbInstance.commit();
		loadModel();

		fireEvent(ureq, new ChangePartEvent(galleryPart));
	}

	private void doSaveMedia(UserRequest ureq, Media media) {
		GalleryPart reloadedGalleryPart = (GalleryPart) pageDAO.loadPart(galleryPart);
		MediaVersion mediaVersion = media.getVersions().get(0);
		mediaToPagePartDAO.persistRelation(reloadedGalleryPart, media, mediaVersion, getIdentity());
		dbInstance.commit();
		loadModel();

		fireEvent(ureq, new ChangePartEvent(galleryPart));
	}

	private void doSaveMediaVersion(UserRequest ureq, GalleryRow galleryRow, String mediaVersionId) {
		MediaToPagePart relation = mediaToPagePartDAO.loadRelation(galleryRow.getRelation().getKey());
		MediaVersion mediaVersion = relation.getMedia().getVersions().stream()
				.filter(mv -> mv.getVersionUuid().equals(mediaVersionId)).findFirst().orElse(null);
		mediaToPagePartDAO.updateMediaVersion(relation, mediaVersion, getIdentity());
		dbInstance.commit();
		loadModel();

		fireEvent(ureq, new ChangePartEvent(galleryPart));
	}

	private void doChooseVersion(UserRequest ureq, GalleryRow galleryRow) {
		chooseVersionController = new ChooseVersionController(ureq, getWindowControl(), galleryRow);
		listenTo(chooseVersionController);
		String title = translate("gallery.choose.version");
		cmc = new CloseableModalController(getWindowControl(), null,
				chooseVersionController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmRemove(UserRequest ureq, GalleryRow galleryRow) {
		String title = translate("gallery.tools.remove.title");
		String text = translate("gallery.tools.remove.text");
		confirmRemoveDialog = activateYesNoDialog(ureq, title, text, confirmRemoveDialog);
		confirmRemoveDialog.setUserObject(galleryRow);
	}

	private void updateUI() {
		flc.contextPut("title", galleryPart.getSettings().getTitle());
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		GalleryRow galleryRow = tableModel.getObject(row);
		List<Component> components = new ArrayList<>();
		if (galleryRow.getToolLink() != null) {
			components.add(galleryRow.getToolLink().getComponent());
		}
		return components;
	}

	private class ToolsController extends BasicController {

		private final Link chooseVersionLink;
		private final Link removeLink;
		private final GalleryRow galleryRow;

		protected ToolsController(UserRequest ureq, WindowControl wControl, GalleryRow galleryRow) {
			super(ureq, wControl);
			this.galleryRow = galleryRow;

			VelocityContainer mainVC = createVelocityContainer("gallery_tools");

			chooseVersionLink = LinkFactory.createLink("gallery.choose.version", "choose.version", getTranslator(),
					mainVC, this, Link.LINK);
			mainVC.put("chooseVersion", chooseVersionLink);

			removeLink = LinkFactory.createLink("remove", "remove", getTranslator(), mainVC, this,
					Link.LINK);
			mainVC.put("remove", removeLink);

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if (source == chooseVersionLink) {
				doChooseVersion(ureq, galleryRow);
			} else if (source == removeLink) {
				doConfirmRemove(ureq, galleryRow);
			}
		}
	}

	private class ChooseVersionController extends FormBasicController {
		private final GalleryRow galleryRow;
		private SingleSelection versionsDropdown;


		protected ChooseVersionController(UserRequest ureq, WindowControl wControl, GalleryRow galleryRow) {
			super(ureq, wControl);
			this.galleryRow = galleryRow;

			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			SelectionValues versionsKV = new SelectionValues();
			MediaToPagePart relation = mediaToPagePartDAO.loadRelation(galleryRow.getRelation().getKey());
			for (MediaVersion mediaVersion : relation.getMedia().getVersions()) {
				versionsKV.add(SelectionValues.entry(mediaVersion.getVersionUuid(),
						PageEditorUIFactory.getVersionName(getTranslator(), mediaVersion)));
			}

			versionsDropdown = uifactory.addDropdownSingleselect("versionsDropdown", "gallery.image.version",
					formLayout, versionsKV.keys(), versionsKV.values());
			versionsDropdown.addActionListener(FormEvent.ONCHANGE);
			MediaVersion mediaVersion = GalleryEditorController.getMediaVersion(galleryRow.getRelation());
			if (mediaVersion != null) {
				versionsDropdown.select(mediaVersion.getVersionUuid(), true);
			}
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (versionsDropdown == source) {
				fireEvent(ureq, FormEvent.DONE_EVENT);
			}
			super.formInnerEvent(ureq, source, event);
		}

		public String getMediaVersionId() {
			if (versionsDropdown.isOneSelected()) {
				return versionsDropdown.getSelectedKey();
			}
			return null;
		}

		public GalleryRow getGalleryRow() {
			return galleryRow;
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}

	private class GalleryCssDelegate extends DefaultFlexiTableCssDelegate {
		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return "o_gallery_images_table clearfix";
		}

		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_gallery_image_card_cell";
		}
	}
}

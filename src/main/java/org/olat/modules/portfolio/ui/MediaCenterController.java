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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.doceditor.DocTemplates;
import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.MediaLight;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.CreateFileHandler;
import org.olat.modules.portfolio.model.CategoryLight;
import org.olat.modules.portfolio.ui.MediaDataModel.MediaCols;
import org.olat.modules.portfolio.ui.component.CategoriesCellRenderer;
import org.olat.modules.portfolio.ui.event.MediaEvent;
import org.olat.modules.portfolio.ui.event.MediaSelectionEvent;
import org.olat.modules.portfolio.ui.media.CollectCitationMediaController;
import org.olat.modules.portfolio.ui.media.CollectTextMediaController;
import org.olat.modules.portfolio.ui.media.CreateFileMediaController;
import org.olat.modules.portfolio.ui.model.MediaRow;
import org.olat.modules.portfolio.ui.renderer.MediaTypeCellRenderer;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.OrderBy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaCenterController extends FormBasicController
	implements Activateable2, FlexiTableComponentDelegate, TooledController {
	
	private static final Size THUMBNAIL_SIZE = new Size(180, 180, false);
	
	private MediaDataModel model;
	private FormLink newMediaCallout;
	private FlexiTableElement tableEl;
	private String mapperThumbnailUrl;
	private Link addFileLink, createFileLink, addMediaLink, addTextLink, addCitationLink;
	
	private int counter = 0;
	private final boolean select;
	private final DocTemplates editableFileTypes;
	private List<FormLink> tagLinks;
	private final TooledStackedPanel stackPanel;

	private CloseableModalController cmc;
	private MediaDetailsController detailsCtrl;
	private MediaUploadController mediaUploadCtrl;
	private CreateFileMediaController createFileCtrl;
	private CollectTextMediaController textUploadCtrl;
	private CollectCitationMediaController citationUploadCtrl;

	private NewMediasController newMediasCtrl;
	private CloseableCalloutWindowController newMediasCalloutCtrl;
	
	@Autowired
	private PortfolioService portfolioService;
	 
	public MediaCenterController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "medias");
		this.stackPanel = null;
		this.select = true;
		this.editableFileTypes = CreateFileHandler.getEditableTemplates(getIdentity(), ureq.getUserSession().getRoles(), getLocale());
		 
		initForm(ureq);
		loadModel();
	}
	
	public MediaCenterController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, "medias");
		this.stackPanel = stackPanel;
		this.select = false;
		this.editableFileTypes = CreateFileHandler.getEditableTemplates(getIdentity(), ureq.getUserSession().getRoles(), getLocale());
		 
		initForm(ureq);
		loadModel();
	}

	@Override
	public void initTools() {
		if (editableFileTypes.isEmpty()) {
			addFileLink = LinkFactory.createToolLink("add.file", translate("add.file"), this);
			addFileLink.setIconLeftCSS("o_icon o_icon-lg o_icon_files");
			stackPanel.addTool(addFileLink, Align.left);
		} else {
			Dropdown addDropdown = new Dropdown("add.file", "add.file", false, getTranslator());
			addDropdown.setIconCSS("o_icon o_icon-lg o_icon_files");
			
			createFileLink = LinkFactory.createToolLink("create.file", translate("create.file"), this);
			createFileLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
			addDropdown.addComponent(createFileLink);
			
			addFileLink = LinkFactory.createToolLink("upload.file", translate("upload.file"), this);
			addFileLink.setIconLeftCSS("o_icon o_icon-fw o_icon_upload");
			addDropdown.addComponent(addFileLink);
			
			stackPanel.addTool(addDropdown, Align.left);
		}

		addMediaLink = LinkFactory.createToolLink("add.media", translate("add.media"), this);
		addMediaLink.setIconLeftCSS("o_icon o_icon-lg o_icon_media");
		stackPanel.addTool(addMediaLink, Align.left);
		
		addTextLink = LinkFactory.createToolLink("add.text", translate("add.text"), this);
		addTextLink.setIconLeftCSS("o_icon o_icon-lg o_filetype_txt");
		stackPanel.addTool(addTextLink, Align.left);
		
		addCitationLink = LinkFactory.createToolLink("add.citation", translate("add.citation"), this);
		addCitationLink.setIconLeftCSS("o_icon o_icon-lg o_icon_citation");
		stackPanel.addTool(addCitationLink, Align.left);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(select) {
			newMediaCallout = uifactory.addFormLink("new.medias", formLayout, Link.BUTTON);
			newMediaCallout.setIconRightCSS("o_icon o_icon_caret o_icon-fw");
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MediaCols.key, "select"));

		Map<String, MediaHandler> handlersMap = portfolioService.getMediaHandlers()
				.stream().collect(Collectors.toMap (h -> h.getType(), h -> h));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaCols.type,
				new MediaTypeCellRenderer(handlersMap)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaCols.title, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaCols.collectionDate, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaCols.categories, new CategoriesCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));
	
		model = new MediaDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableMessageKey("table.sEmptyTable");
		tableEl.setPageSize(24);
		VelocityContainer row = createVelocityContainer("media_row");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		tableEl.setCssDelegate(new MediaCssDelegate());
		tableEl.setAndLoadPersistedPreferences(ureq, "media-list");
		initSorters(tableEl);
		initFilters(tableEl);
		
		mapperThumbnailUrl = registerCacheableMapper(ureq, "media-thumbnail", new ThumbnailMapper(model));
		row.contextPut("mapperThumbnailUrl", mapperThumbnailUrl);
	}
	
	private void initSorters(FlexiTableElement tableElement) {
		List<FlexiTableSort> sorters = new ArrayList<>(14);
		sorters.add(new FlexiTableSort(translate(MediaCols.key.i18nHeaderKey()), MediaCols.key.name()));
		sorters.add(new FlexiTableSort(translate(MediaCols.type.i18nHeaderKey()), MediaCols.type.name()));
		sorters.add(new FlexiTableSort(translate(MediaCols.title.i18nHeaderKey()), MediaCols.title.name()));
		sorters.add(new FlexiTableSort(translate(MediaCols.collectionDate.i18nHeaderKey()), MediaCols.collectionDate.name()));
		sorters.add(FlexiTableSort.SPACER);

		FlexiTableSortOptions options = new FlexiTableSortOptions(sorters);
		options.setDefaultOrderBy(new SortKey(OrderBy.title.name(), true));
		tableElement.setSortSettings(options);
	}
	
	private void initFilters(FlexiTableElement tableElement) {
		List<FlexiTableFilter> filters = new ArrayList<>(16);
		filters.add(new FlexiTableFilter(translate("filter.show.all"), "showall"));
		filters.add(FlexiTableFilter.SPACER);
		List<MediaHandler> handlers = portfolioService.getMediaHandlers();
		for(MediaHandler handler:handlers) {
			filters.add(new FlexiTableFilter(translate("artefact." + handler.getType()), handler.getType()));
		}
		tableElement.setFilters(null, filters, false);
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		MediaRow mediaRow = model.getObject(row);
		List<Component> components = new ArrayList<>(2);
		if(mediaRow.getOpenFormItem() != null) {
			components.add(mediaRow.getOpenFormItem().getComponent());
		}
		return components;
	}
	
	private void loadModel() {
		String searchString = tableEl.getQuickSearchString();
		List<String> tagNames = getSelectedTagNames();
		Map<Long,MediaRow> currentMap = model.getObjects()
				.stream().collect(Collectors.toMap(MediaRow::getKey, r -> r));

		List<MediaLight> medias = portfolioService.searchOwnedMedias(getIdentity(), searchString, tagNames);
		List<MediaRow> rows = new ArrayList<>(medias.size());
		for(MediaLight media:medias) {
			if(currentMap.containsKey(media.getKey())) {
				rows.add(currentMap.get(media.getKey()));
			} else {
				MediaHandler handler = portfolioService.getMediaHandler(media.getType());
				VFSLeaf thumbnail = handler.getThumbnail(media, THUMBNAIL_SIZE);
				String mediaTitle = StringHelper.escapeHtml(media.getTitle());
				FormLink openLink =  uifactory.addFormLink("select_" + (++counter), "select", mediaTitle, null, flc, Link.NONTRANSLATED);
				MediaRow row = new MediaRow(media, thumbnail, openLink, handler.getIconCssClass(media));
				openLink.setUserObject(row);
				rows.add(row);
			}
		}
		model.setObjects(rows);
		model.filter(null, tableEl.getFilters());
		
		Map<Long,MediaRow> rowMap = model.getObjects()
				.stream().collect(Collectors.toMap(r -> r.getKey(), r -> r));
		
		Set<String> duplicateCategories = new HashSet<>();
		List<CategoryLight> categories = portfolioService.getMediaCategories(getIdentity());
		List<FormLink> newTagLinks = new ArrayList<>(categories.size());
		for(CategoryLight category:categories) {
			String name = category.getCategory();
			MediaRow mRow = rowMap.get(category.getMediaKey());
			if(mRow != null) {
				mRow.addCategory(name);
			}
			
			if(duplicateCategories.contains(name)) {
				continue;
			}
			duplicateCategories.add(name);
			
			FormLink tagLink =  uifactory.addFormLink("tag_" + (++counter), "tag", name, null, null, Link.NONTRANSLATED);
			CategoryState state = new CategoryState(category, tagNames.contains(name));
			tagLink.setUserObject(state);
			if(state.isSelected()) {
				tagLink.setCustomEnabledLinkCSS("tag label label-info o_disabled");
			} else {
				tagLink.setCustomEnabledLinkCSS("tag label label-info");
			}
			flc.add(tagLink);
			newTagLinks.add(tagLink);
		}
		flc.contextPut("tagLinks", newTagLinks);
		tagLinks = newTagLinks;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Media".equalsIgnoreCase(resName)) {
			Long resId = entries.get(0).getOLATResourceable().getResourceableId();
			for(MediaRow row:model.getObjects()) {
				if(row.getKey().equals(resId)) {
					doOpenMedia(ureq, resId);
				}
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				MediaRow row = model.getObject(se.getIndex());
				if("select".equals(cmd)) {
					if(select) {
						doSelect(ureq, row.getKey());
					} else {
						Activateable2 activateable = doOpenMedia(ureq, row.getKey());
						if(activateable != null) {
							activateable.activate(ureq, null, null);
						}
					}
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				loadModel();
			}
		} else if(newMediaCallout == source) {
			doOpenNewMediaCallout(ureq, newMediaCallout);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("select".equals(cmd)) {
				MediaRow row = (MediaRow)link.getUserObject();
				if(select) {
					doSelect(ureq, row.getKey());
				} else {
					Activateable2 activateable = doOpenMedia(ureq, row.getKey());
					if(activateable != null) {
						activateable.activate(ureq, null, null);
					}
				}
			} else if("tag".equals(cmd)) {
				doToggleCategory(link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (createFileCtrl == source || mediaUploadCtrl == source || textUploadCtrl == source
				|| citationUploadCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
				tableEl.reloadData();
			}
			cmc.deactivate();
			cleanUp();
			
			if(select || event == Event.DONE_EVENT) {
				if(createFileCtrl == source) {
					doSelect(ureq, createFileCtrl.getMediaReference().getKey());
				} else if(mediaUploadCtrl == source) {
					doSelect(ureq, mediaUploadCtrl.getMediaReference().getKey());
				} else if(textUploadCtrl == source) {
					doSelect(ureq, textUploadCtrl.getMediaReference().getKey());
				} else if(citationUploadCtrl == source) {
					doSelect(ureq, citationUploadCtrl.getMediaReference().getKey());
				}
			}
		} else if(newMediasCtrl == source) {
			newMediasCalloutCtrl.deactivate();
			if("add.file".equals(event.getCommand())) {
				doAddMedia(ureq, "add.file");
			} else if("add.media".equals(event.getCommand())) {
				doAddMedia(ureq, "add.media");
			} else if("add.text".equals(event.getCommand())) {
				doAddTextMedia(ureq);
			} else if("add.citation".equals(event.getCommand())) {
				doAddCitationMedia(ureq);
			}
		} else if(detailsCtrl == source) {
			if(event instanceof MediaEvent) {
				MediaEvent me = (MediaEvent)event;
				if(MediaEvent.DELETED.equals(me.getCommand())) {
					stackPanel.popUpToController(this);
					loadModel();
					tableEl.reset(false, true, true);
				}
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(citationUploadCtrl);
		removeAsListenerAndDispose(mediaUploadCtrl);
		removeAsListenerAndDispose(createFileCtrl);
		removeAsListenerAndDispose(textUploadCtrl);
		removeAsListenerAndDispose(cmc);
		citationUploadCtrl = null;
		mediaUploadCtrl = null;
		createFileCtrl = null;
		textUploadCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(createFileLink == source) {
			doCreateFile(ureq);
		} else if(addFileLink == source) {
			doAddMedia(ureq, "add.file");
		} else if(addMediaLink == source) {
			doAddMedia(ureq, "add.media");
		} else if(addTextLink == source) {
			doAddTextMedia(ureq);
		} else if(addCitationLink == source) {
			doAddCitationMedia(ureq);

		} else if(source == mainForm.getInitialComponent()) {
			if("ONCLICK".equals(event.getCommand())) {
				String rowKeyStr = ureq.getParameter("img_select");
				if(StringHelper.isLong(rowKeyStr)) {
					try {
						Long rowKey = Long.valueOf(rowKeyStr);
						List<MediaRow> rows = model.getObjects();
						for(MediaRow row:rows) {
							if(row != null && row.getKey().equals(rowKey)) {
								if(select) {
									doSelect(ureq, rowKey);
								} else {
									doOpenMedia(ureq, rowKey);
								}
							}
						}
					} catch (NumberFormatException e) {
						logWarn("Not a valid long: " + rowKeyStr, e);
					}
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doCreateFile(UserRequest ureq) {
		if(guardModalController(createFileCtrl)) return;
		
		createFileCtrl = new CreateFileMediaController(ureq, getWindowControl(), editableFileTypes);
		listenTo(createFileCtrl);
		
		String title = translate("create.file.title");
		cmc = new CloseableModalController(getWindowControl(), null, createFileCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddMedia(UserRequest ureq, String titleKey) {
		if(guardModalController(mediaUploadCtrl)) return;
		
		mediaUploadCtrl = new MediaUploadController(ureq, getWindowControl());
		listenTo(mediaUploadCtrl);
		
		String title = translate(titleKey);
		cmc = new CloseableModalController(getWindowControl(), null, mediaUploadCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddTextMedia(UserRequest ureq) {
		if(guardModalController(textUploadCtrl)) return;
		
		textUploadCtrl = new CollectTextMediaController(ureq, getWindowControl());
		listenTo(textUploadCtrl);
		
		String title = translate("add.text");
		cmc = new CloseableModalController(getWindowControl(), null, textUploadCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddCitationMedia(UserRequest ureq) {
		if(guardModalController(citationUploadCtrl)) return;
		
		citationUploadCtrl = new CollectCitationMediaController(ureq, getWindowControl());
		listenTo(citationUploadCtrl);
		
		String title = translate("add.citation");
		cmc = new CloseableModalController(getWindowControl(), null, citationUploadCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doSelect(UserRequest ureq, Long mediaKey) {
		Media media = portfolioService.getMediaByKey(mediaKey);
		fireEvent(ureq, new MediaSelectionEvent(media));
	}
	
	private void doToggleCategory(FormLink tagLink) {
		CategoryState state = (CategoryState)tagLink.getUserObject();
		state.setSelected(!state.isSelected());
		loadModel();
	}
	
	private List<String> getSelectedTagNames() {
		List<String> tagNames = new ArrayList<>();
		if(tagLinks != null && tagLinks.size() > 0) {
			for(FormLink tagLink:tagLinks) {
				CategoryState state = (CategoryState)tagLink.getUserObject();
				if(state.isSelected()) {
					tagNames.add(state.getName());
				}
			}
		}
		return tagNames;
	}
	
	private void doOpenNewMediaCallout(UserRequest ureq, FormLink link) {
		removeAsListenerAndDispose(newMediasCtrl);
		removeAsListenerAndDispose(newMediasCalloutCtrl);

		newMediasCtrl = new NewMediasController(ureq, getWindowControl());
		listenTo(newMediasCtrl);

		newMediasCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				newMediasCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "", new CalloutSettings(false));
		listenTo(newMediasCalloutCtrl);
		newMediasCalloutCtrl.activate();
	}
	
	private Activateable2 doOpenMedia(UserRequest ureq, Long mediaKey) {
		stackPanel.popUpToController(this);
		
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("Media", mediaKey);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		Media media = portfolioService.getMediaByKey(mediaKey);
		detailsCtrl = new MediaDetailsController(ureq, swControl, stackPanel, media);
		listenTo(detailsCtrl);
		
		stackPanel.pushController(media.getTitle(), detailsCtrl);
		return detailsCtrl;
	}
	
	private static class MediaCssDelegate extends DefaultFlexiTableCssDelegate {

		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return "o_portfolio_medias clearfix";
		}

		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_portfolio_media";
		}
	}
	
	private static class ThumbnailMapper implements Mapper {
		
		private final MediaDataModel binderModel;
		
		public ThumbnailMapper(MediaDataModel model) {
			this.binderModel = model;
		}

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			MediaResource mr = null;
			
			String row = relPath;
			if(row.startsWith("/")) {
				row = row.substring(1, row.length());
			}
			int index = row.indexOf("/");
			if(index > 0) {
				row = row.substring(0, index);
				Long key = Long.valueOf(row); 
				List<MediaRow> rows = binderModel.getObjects();
				for(MediaRow prow:rows) {
					if(key.equals(prow.getKey())) {
						VFSLeaf thumbnail = prow.getThumbnail();
						mr = new VFSMediaResource(thumbnail);
					}
				}
			}
			
			return mr;
		}
	}

	private static class CategoryState {
		
		private boolean selected;
		private final CategoryLight category;
		
		public CategoryState(CategoryLight category, boolean selected) {
			this.category = category;
			this.selected = selected;
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		public String getName() {
			return category.getCategory();
		}
	}
	
	private static class NewMediasController extends BasicController {

		private final Link addFileLink, addMediaLink, addTextLink, addCitationLink;
		
		public NewMediasController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
			
			VelocityContainer mainVc = createVelocityContainer("new_medias");
			
			addFileLink = LinkFactory.createLink("add.file", "add.file", getTranslator(), mainVc, this, Link.LINK);
			addFileLink.setIconLeftCSS("o_icon o_icon_files o_icon-fw");

			addMediaLink = LinkFactory.createLink("add.media", "add.media", getTranslator(), mainVc, this, Link.LINK);
			addMediaLink.setIconLeftCSS("o_icon o_icon_media o_icon-fw");
			
			addTextLink = LinkFactory.createLink("add.text", "add.text", getTranslator(), mainVc, this, Link.LINK);
			addTextLink.setIconLeftCSS("o_icon o_filetype_txt o_icon-fw");
			
			addCitationLink = LinkFactory.createLink("add.citation", "add.citation", getTranslator(), mainVc, this, Link.LINK);
			addCitationLink.setIconLeftCSS("o_icon o_icon_citation o_icon-fw");

			putInitialPanel(mainVc);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(source instanceof Link) {
				Link link = (Link)source;
				fireEvent(ureq, new Event(link.getCommand()));
			}
		}
	}
}

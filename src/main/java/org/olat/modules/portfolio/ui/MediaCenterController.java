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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
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
import org.olat.modules.portfolio.ui.MediaDataModel.MediaCols;
import org.olat.modules.portfolio.ui.event.MediaSelectionEvent;
import org.olat.modules.portfolio.ui.media.CollectCitationMediaController;
import org.olat.modules.portfolio.ui.media.CollectTextMediaController;
import org.olat.modules.portfolio.ui.model.MediaRow;
import org.olat.modules.portfolio.ui.renderer.MediaTypeCellRenderer;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.ui.EPArtefactPoolRunController;
import org.olat.portfolio.ui.artefacts.view.EPArtefactChoosenEvent;
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
	private FlexiTableElement tableEl;
	private String mapperThumbnailUrl;
	private Link addMediaLink, addTextLink, addCitationLink, importArtefactV1Link;
	
	private int counter = 0;
	private final boolean select;
	private final TooledStackedPanel stackPanel;

	private CloseableModalController cmc;
	private MediaDetailsController detailsCtrl;
	private MediaUploadController mediaUploadCtrl;
	private CollectTextMediaController textUploadCtrl;
	private EPArtefactPoolRunController importArtefactv1Ctrl;
	private CollectCitationMediaController citationUploadCtrl;
	
	@Autowired
	private PortfolioService portfolioService;
	 
	public MediaCenterController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "medias");
		this.stackPanel = null;
		this.select = true;
		 
		initForm(ureq);
		loadModel(null);
	}
	
	public MediaCenterController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, "medias");
		this.stackPanel = stackPanel;
		this.select = false;
		 
		initForm(ureq);
		loadModel(null);
	}
	
	@Override
	public void initTools() {
		addMediaLink = LinkFactory.createToolLink("add.file", translate("add.file"), this);
		addMediaLink.setIconLeftCSS("o_icon o_icon-lg o_icon_files");
		stackPanel.addTool(addMediaLink, Align.left);
		
		addTextLink = LinkFactory.createToolLink("add.text", translate("add.text"), this);
		addTextLink.setIconLeftCSS("o_icon o_icon-lg o_filetype_txt");
		stackPanel.addTool(addTextLink, Align.left);
		
		addCitationLink = LinkFactory.createToolLink("add.citation", translate("add.citation"), this);
		addCitationLink.setIconLeftCSS("o_icon o_icon-lg o_icon_citation");
		stackPanel.addTool(addCitationLink, Align.left);
		
		importArtefactV1Link = LinkFactory.createToolLink("import.artefactV1", translate("import.artefactV1"), this);
		importArtefactV1Link.setIconLeftCSS("o_icon o_icon-lg o_icon_import");
		stackPanel.addTool(importArtefactV1Link, Align.left);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MediaCols.key, "select"));

		Map<String, MediaHandler> handlersMap = portfolioService.getMediaHandlers()
				.stream().collect(Collectors.toMap (h -> h.getType(), h -> h));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaCols.type,
				new MediaTypeCellRenderer(handlersMap)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaCols.title, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaCols.collectionDate, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaCols.open));
	
		model = new MediaDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmtpyTableMessageKey("table.sEmptyTable");
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
		tableElement.setFilters(null, filters);
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}
	
	private void loadModel(String searchString) {
		Map<Long,MediaRow> currentMap = model.getObjects()
				.stream().collect(Collectors.toMap(r -> r.getKey(), r -> r));

		List<MediaLight> medias = portfolioService.searchOwnedMedias(getIdentity(), searchString);
		List<MediaRow> rows = new ArrayList<>(medias.size());
		for(MediaLight media:medias) {
			if(currentMap.containsKey(media.getKey())) {
				rows.add(currentMap.get(media.getKey()));
			} else {
				MediaHandler handler = portfolioService.getMediaHandler(media.getType());
				VFSLeaf thumbnail = handler.getThumbnail(media, THUMBNAIL_SIZE);
				FormLink openLink =  uifactory.addFormLink("select_" + (++counter), "select", media.getTitle(), null, null, Link.NONTRANSLATED);
				MediaRow row = new MediaRow(media, thumbnail, openLink, handler.getIconCssClass(media));
				openLink.setUserObject(row);
				rows.add(row);
			}
		}
		model.setObjects(rows);
		model.filter(tableEl.getSelectedFilterKey());
	}

	@Override
	protected void doDispose() {
		//
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
				FlexiTableSearchEvent se = (FlexiTableSearchEvent)event;
				loadModel(se.getSearch());
			}
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
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(mediaUploadCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(null);
				tableEl.reloadData();
			}
			cmc.deactivate();
			cleanUp();
		} else if(textUploadCtrl == source || citationUploadCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(null);
				tableEl.reloadData();
			}
			cmc.deactivate();
			cleanUp();
		} else if(importArtefactv1Ctrl == source) {
			if(event instanceof EPArtefactChoosenEvent) {
				EPArtefactChoosenEvent cEvent = (EPArtefactChoosenEvent)event;
				doImportArtefactV1(cEvent.getArtefact());
				loadModel(null);
				tableEl.reloadData();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {

			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(importArtefactv1Ctrl);
		removeAsListenerAndDispose(citationUploadCtrl);
		removeAsListenerAndDispose(mediaUploadCtrl);
		removeAsListenerAndDispose(textUploadCtrl);
		removeAsListenerAndDispose(cmc);
		importArtefactv1Ctrl = null;
		citationUploadCtrl = null;
		mediaUploadCtrl = null;
		textUploadCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(addMediaLink == source) {
			doAddMedia(ureq);
		} else if(addTextLink == source) {
			doAddTextMedia(ureq);
		} else if(addCitationLink == source) {
			doAddCitationMedia(ureq);
		} else if(importArtefactV1Link == source) {
			doChooseArtefactV1(ureq);
		} else if(source == mainForm.getInitialComponent()) {
			if("ONCLICK".equals(event.getCommand())) {
				String rowKeyStr = ureq.getParameter("img_select");
				if(StringHelper.isLong(rowKeyStr)) {
					try {
						Long rowKey = new Long(rowKeyStr);
						List<MediaRow> rows = model.getObjects();
						for(MediaRow row:rows) {
							if(row != null && row.getKey().equals(rowKey)) {
								doOpenMedia(ureq, rowKey);
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
	
	private void doAddMedia(UserRequest ureq) {
		if(mediaUploadCtrl != null) return;
		
		mediaUploadCtrl = new MediaUploadController(ureq, getWindowControl());
		listenTo(mediaUploadCtrl);
		
		String title = translate("add.media");
		cmc = new CloseableModalController(getWindowControl(), null, mediaUploadCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddTextMedia(UserRequest ureq) {
		if(textUploadCtrl != null) return;
		
		textUploadCtrl = new CollectTextMediaController(ureq, getWindowControl());
		listenTo(textUploadCtrl);
		
		String title = translate("add.text");
		cmc = new CloseableModalController(getWindowControl(), null, textUploadCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddCitationMedia(UserRequest ureq) {
		if(citationUploadCtrl != null) return;
		
		citationUploadCtrl = new CollectCitationMediaController(ureq, getWindowControl());
		listenTo(citationUploadCtrl);
		
		String title = translate("add.citation");
		cmc = new CloseableModalController(getWindowControl(), null, citationUploadCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChooseArtefactV1(UserRequest ureq) {
		if(importArtefactv1Ctrl != null) return;
		
		importArtefactv1Ctrl = new EPArtefactPoolRunController(ureq, this.getWindowControl(), true, false);
		listenTo(importArtefactv1Ctrl);
		
		String title = translate("import.artefactV1");
		cmc = new CloseableModalController(getWindowControl(), null, importArtefactv1Ctrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doImportArtefactV1(AbstractArtefact oldArtefact) {
		MediaHandler handler = portfolioService.getMediaHandler(oldArtefact.getResourceableTypeName());
		if(handler != null) {
			handler.createMedia(oldArtefact);
		}
	}

	private void doSelect(UserRequest ureq, Long mediaKey) {
		Media media = portfolioService.getMediaByKey(mediaKey);
		fireEvent(ureq, new MediaSelectionEvent(media));
	}
	
	private Activateable2 doOpenMedia(UserRequest ureq, Long mediaKey) {
		stackPanel.popUpToController(this);
		
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("Media", mediaKey);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		Media media = portfolioService.getMediaByKey(mediaKey);
		detailsCtrl = new MediaDetailsController(ureq, swControl, media);
		listenTo(detailsCtrl);
		
		String displayName = StringHelper.escapeHtml(media.getTitle());
		stackPanel.pushController(displayName, detailsCtrl);
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
				Long key = new Long(row); 
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
}

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

import javax.servlet.http.HttpServletRequest;

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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.MediaRow;
import org.olat.modules.portfolio.ui.BindersDataModel.PortfolioCols;
import org.olat.modules.portfolio.ui.event.MediaSelectionEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaCenterController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {
	
	private static final Size THUMBNAIL_SIZE = new Size(180, 180, false);
	
	private MediaDataModel model;
	private FlexiTableElement tableEl;
	private String mapperThumbnailUrl;
	
	private int counter = 0;
	private final boolean select;
	private final TooledStackedPanel stackPanel;
	
	private MediaDetailsController detailsCtrl;
	
	@Autowired
	private PortfolioService portfolioService;
	 
	public MediaCenterController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "medias");
		this.stackPanel = null;
		this.select = true;
		 
		initForm(ureq);
		loadModel();
	}
	
	public MediaCenterController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, "medias");
		this.stackPanel = stackPanel;
		this.select = false;
		 
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PortfolioCols.key, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PortfolioCols.title, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PortfolioCols.open));
	
		model = new MediaDataModel(columnsModel);
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
		
		mapperThumbnailUrl = this.registerCacheableMapper(ureq, "media-thumbnail", new ThumbnailMapper(model));
		row.contextPut("mapperThumbnailUrl", mapperThumbnailUrl);
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}
	
	private void loadModel() {
		List<Media> medias = portfolioService.searchOwnedMedias(getIdentity());
		List<MediaRow> rows = new ArrayList<>(medias.size());
		
		for(Media media:medias) {
			MediaHandler handler = portfolioService.getMediaHandler(media.getType());
			VFSLeaf thumbnail = handler.getThumbnail(media, THUMBNAIL_SIZE);
			FormLink openLink =  uifactory.addFormLink("select_" + (++counter), "select", media.getTitle(), null, null, Link.NONTRANSLATED);
			MediaRow row = new MediaRow(media, thumbnail, openLink);
			openLink.setUserObject(row);
			rows.add(row);
		}
		model.setObjects(rows);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
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
	public void event(UserRequest ureq, Component source, Event event) {
		 if(source == mainForm.getInitialComponent()) {
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
	

	private void doSelect(UserRequest ureq, Long mediaKey) {
		Media media = portfolioService.getMediaByKey(mediaKey);
		fireEvent(ureq, new MediaSelectionEvent(media));
	}
	
	private Activateable2 doOpenMedia(UserRequest ureq, Long mediaKey) {
		Media media = portfolioService.getMediaByKey(mediaKey);
		detailsCtrl = new MediaDetailsController(ureq, getWindowControl(), media);
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

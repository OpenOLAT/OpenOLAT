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
package org.olat.modules.video.ui;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.modules.video.ui.VideoEntryDataModel.Cols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.OrderBy;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This site implements a YouTube stile video library for self-study. 
 * 
 * Initial date: 08.05.2016<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public class VideoListingController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {

	private final TooledStackedPanel toolbarPanel;

	private final String imgUrl;
	private FlexiTableElement tableEl;
	private VideoEntryDataSource dataSource;
	private SearchMyRepositoryEntryViewParams searchParams;

	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;

	public VideoListingController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel) {		
		super(ureq, wControl, "video_listing");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		
		this.toolbarPanel = toolbarPanel;
		
		searchParams = new SearchMyRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles(), VideoFileResource.TYPE_NAME);
		searchParams.setEntryStatus(new RepositoryEntryStatusEnum[] { RepositoryEntryStatusEnum.published });
		dataSource = new VideoEntryDataSource(searchParams);
		imgUrl = registerMapper(ureq, new VideoMapper());

		initForm(ureq);
		tableEl.reloadData();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18nKey(), Cols.key.ordinal(), true, OrderBy.key.name()));

		VideoEntryDataModel model = new VideoEntryDataModel(dataSource, columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(false);
		tableEl.setElementCssClass("o_video_listing");
		boolean isAuthor = ureq.getUserSession().getRoles().isAuthor();
		tableEl.setEmptyTableSettings("video.site.empty", isAuthor ? "video.site.empty.hint" : null, "o_icon_video");
		tableEl.setPageSize(24);
		VelocityContainer row = createVelocityContainer("video_cell");
		row.contextPut("imgUrl", imgUrl);
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		tableEl.setCssDelegate(new VideoCssDelegate());

		initSorters(tableEl);
		
		tableEl.setAndLoadPersistedPreferences(ureq, "video-list");
	}
	
	private void initSorters(FlexiTableElement tableElement) {
		List<FlexiTableSort> sorters = new ArrayList<>(8);
		sorters.add(new FlexiTableSort(translate("orderby.automatic"), OrderBy.automatic.name()));
		sorters.add(new FlexiTableSort(translate("orderby.title"), OrderBy.title.name()));
		sorters.add(new FlexiTableSort(translate("orderby.author"), OrderBy.author.name()));
		sorters.add(new FlexiTableSort(translate("orderby.creationDate"), OrderBy.creationDate.name()));
		sorters.add(new FlexiTableSort(translate("orderby.launchCounter"), OrderBy.launchCounter.name()));
		if(repositoryModule.isRatingEnabled()) {
			sorters.add(new FlexiTableSort(translate("orderby.rating"), OrderBy.rating.name()));
		}
		FlexiTableSortOptions options = new FlexiTableSortOptions(sorters);
		options.setDefaultOrderBy(new SortKey(OrderBy.creationDate.name(), false));
		tableElement.setSortSettings(options);
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	/**
	 * Launch a single video and add to breadcrumb
	 * @param ureq
	 * @param id the video resource ID
	 */
	private void doShowVideo(UserRequest ureq, Long id) {
		RepositoryEntry videoEntry = repositoryManager.lookupRepositoryEntry(id);
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq, videoEntry);
		if (reSecurity.canLaunch()) {// no booking implemented for video
			boolean readOnly = videoEntry.getEntryStatus().decommissioned();
			VideoDisplayOptions options = VideoDisplayOptions.valueOf(true, true, true, true, true, false, videoEntry.getDescription(), false, readOnly);
			VideoDisplayController videoDisplayCtr = new VideoDisplayController(ureq, getWindowControl(), videoEntry, null, null, options);
			listenTo(videoDisplayCtr);
			toolbarPanel.pushController(videoEntry.getDisplayname(), videoDisplayCtr);
			// Update launch counter
			repositoryService.incrementLaunchCounter(videoEntry);
			// Update business path and URL
			ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(videoEntry);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ce.getOLATResourceable()));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());
			addToHistory(ureq, bwControl);			
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == mainForm.getInitialComponent()) {
			if("ONCLICK".equals(event.getCommand())) {
				String rowKeyStr = ureq.getParameter("select_row");
				if(StringHelper.isLong(rowKeyStr)) {
					try {
						doShowVideo(ureq, Long.valueOf(rowKeyStr));
					} catch (NumberFormatException e) {
						logWarn("Not a valid long: " + rowKeyStr, e);
					}
				}
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		// cleanup existing video
		if (toolbarPanel.getBreadCrumbs().size() > 1) {
			toolbarPanel.popUpToRootController(ureq);
		}
		if(entries == null || entries.isEmpty()) {
			// nothing to do, default video listing
		} else {
			Long id = entries.get(0).getOLATResourceable().getResourceableId();
			doShowVideo(ureq, id);			
		}
	}
	
	@Override
	protected void doDispose() {
		// controllers auto-disposed by basic controller
	}
	
	public class VideoMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if (StringHelper.containsNonWhitespace(relPath)) {
				int start = relPath.lastIndexOf("/");
				if (start != -1) {
					relPath = relPath.substring(start+1);
					Long id = Long.valueOf(relPath);
					RepositoryEntry entry = repositoryService.loadByKey(id);
					VFSLeaf imageFile = repositoryManager.getImage(entry);
					return new VFSMediaResource(imageFile);
				}
			}
			return new NotFoundMediaResource();
		}
	}
	
	private static class VideoCssDelegate extends DefaultFlexiTableCssDelegate {
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {

			return "o_video_entry";
		}
	}
}

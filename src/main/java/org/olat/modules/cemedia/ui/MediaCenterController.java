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
package org.olat.modules.cemedia.ui;

import static org.olat.core.util.StringHelper.EMPTY;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.doceditor.DocTemplates;
import org.olat.core.commons.services.doceditor.drawio.DrawioModule;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.FlexiTableTagFilter;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFilterTabPosition;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
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
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.ceditor.ui.component.CategoriesCellRenderer;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaModule;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaTag;
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;
import org.olat.modules.cemedia.MediaToTaxonomyLevel;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.handler.FileHandler;
import org.olat.modules.cemedia.handler.VideoHandler;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.cemedia.model.SearchMediaParameters;
import org.olat.modules.cemedia.model.SearchMediaParameters.Scope;
import org.olat.modules.cemedia.model.SearchMediaParameters.UsedIn;
import org.olat.modules.cemedia.ui.MediaDataModel.MediaCols;
import org.olat.modules.cemedia.ui.event.MediaEvent;
import org.olat.modules.cemedia.ui.event.MediaSelectionEvent;
import org.olat.modules.cemedia.ui.event.UploadMediaEvent;
import org.olat.modules.cemedia.ui.medias.AVVideoMediaController;
import org.olat.modules.cemedia.ui.medias.CollectCitationMediaController;
import org.olat.modules.cemedia.ui.medias.CollectTextMediaController;
import org.olat.modules.cemedia.ui.medias.CreateDrawioMediaController;
import org.olat.modules.cemedia.ui.medias.CreateFileMediaController;
import org.olat.modules.cemedia.ui.medias.UploadMedia;
import org.olat.modules.portfolio.ui.model.MediaRow;
import org.olat.modules.portfolio.ui.renderer.MediaTypeCellRenderer;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.repository.ui.author.TaxonomyLevelRenderer;
import org.olat.repository.ui.author.TaxonomyPathsRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaCenterController extends FormBasicController
	implements Activateable2, FlexiTableComponentDelegate {
	
	private static final Size THUMBNAIL_SIZE = new Size(193, 130, false);
	
	public static final String ALL_TAB_ID = "All";
	public static final String MY_TAB_ID = "My";
	public static final String SHARED_TAB_BY_ME_ID = "SharedByMe";
	public static final String SHARED_TAB_WITH_ME_ID = "SharedWithMe";
	public static final String SHARED_TAB_WITH_ENTRY = "SharedWithEntry";
	public static final String NOT_SHARED_TAB_ID = "NotShared";
	public static final String SEARCH_TAB_ID = "Search";

	public static final String FILTER_TAGS = "tags";
	public static final String FILTER_TYPES = "types";
	public static final String FILTER_USED = "useds";
	public static final String FILTER_USED_IN = "usedIn";
	public static final String FILTER_TAXONOMY = "taxonomy";
	public static final String FILTER_SHARED_WITH = "sharedWith";
	
	private MediaDataModel model;
	private FormLink bulkDeleteButton;
	private FormLink newMediaCallout;
	private FlexiTableElement tableEl;
	private FormLink addFileLink;
	private FormLink createFileLink;
	private FormLink addMediaLink;
	private FormLink addTextLink;
	private FormLink addCitationLink;
	private FormLink recordVideoLink;
	private FormLink createDrawioLink;
	private FileElement uploadEl;
	
	private FlexiFiltersTab myTab;
	private FlexiFiltersTab allTab;
	private FlexiFiltersTab sharedWithMeTab;
	private FlexiFiltersTab sharedByMeTab;
	private FlexiFiltersTab sharedWithEntryTab;
	private FlexiFiltersTab notSharedTab;
	private FlexiFiltersTab searchTab;
	
	private int counter = 0;
	private final Roles roles;
	private final boolean withHelp;
	private final boolean withSelect;
	private final boolean withAddMedias;
	private final boolean withUploadCard;
	private final boolean withMediaSelection;
	private final String preselectedType;
	private final DocTemplates editableFileTypes;
	private final TooledStackedPanel stackPanel;
	private final RepositoryEntry repositoryEntry;

	private CloseableModalController cmc;
	private MediaDetailsController detailsCtrl;
	private MediaUploadController mediaUploadCtrl;
	private AVVideoMediaController recordVideoCtrl;
	private CreateFileMediaController createFileCtrl;
	private CollectTextMediaController textUploadCtrl;
	private CreateDrawioMediaController createDrawioCtrl;
	private CollectCitationMediaController citationUploadCtrl;
	private ConfirmDeleteMediaController confirmDeleteMediaCtrl;

	private NewMediasController newMediasCtrl;
	private CloseableCalloutWindowController newMediasCalloutCtrl;
	
	private final Translator taxonomyTranslator;
	
	@Autowired
	private MediaModule mediaModule;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private DrawioModule drawioModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	 
	public MediaCenterController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry repositoryEntry, boolean withAddMedias, boolean withMediaSelection) {
		this(ureq, wControl, null, true, withAddMedias, true, false, withMediaSelection, null,
				(repositoryEntry == null ? SHARED_TAB_WITH_ME_ID :SHARED_TAB_WITH_ENTRY),
				repositoryEntry);
	}
	
	public MediaCenterController(UserRequest ureq, WindowControl wControl, MediaHandler handler,
			boolean withUploadCard, RepositoryEntry repositoryEntry) {
		this(ureq, wControl, null, true, false, false, withUploadCard, true, handler.getType(),
				(repositoryEntry == null ? SHARED_TAB_WITH_ME_ID :SHARED_TAB_WITH_ENTRY),
				repositoryEntry);
	}
	
	public MediaCenterController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		this(ureq, wControl, stackPanel, false, true, true, false, true, null, MY_TAB_ID, null);
	}
	
	private MediaCenterController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			boolean withSelect, boolean withAddMedias, boolean withHelp, boolean withUploadCard, boolean withMediaSelection,
			String preselectedType, String defaultFilterTab, RepositoryEntry repositoryEntry) {
		super(ureq, wControl, "medias", Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale()));
		this.stackPanel = stackPanel;
		this.withHelp = withHelp;
		this.withSelect = withSelect;
		this.withAddMedias = withAddMedias;
		this.withUploadCard = withUploadCard;
		this.withMediaSelection = withMediaSelection;
		this.preselectedType = preselectedType;
		this.repositoryEntry = repositoryEntry;
		roles = ureq.getUserSession().getRoles();
		taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale());
		editableFileTypes = FileHandler.getEditableTemplates(getIdentity(), roles, getLocale());
		 
		initForm(ureq);
		setSelectedTab(ureq, defaultFilterTab);
		loadModel(true);
	}
	
	@Override
	public void setFormTranslatedTitle(String title) {
		super.setFormTranslatedTitle(title);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("withHelp", Boolean.valueOf(withHelp));
			layoutCont.contextPut("withMediaSelection", Boolean.valueOf(withMediaSelection));
		}
		
		if(withSelect) {
			newMediaCallout = uifactory.addFormLink("new.medias", formLayout, Link.BUTTON);
			newMediaCallout.setIconRightCSS("o_icon o_icon_caret o_icon-fw");
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MediaCols.key, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaCols.type, new MediaTypeCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaCols.title, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaCols.collectionDate, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaCols.tags, new CategoriesCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaCols.taxonomyLevels, new TaxonomyLevelRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MediaCols.taxonomyLevelsPaths, new TaxonomyPathsRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));
	
		model = new MediaDataModel(columnsModel, getTranslator(), getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 25, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setMultiSelect(withMediaSelection);
		tableEl.setEmptyTableMessageKey("table.sEmptyTable");
		VelocityContainer row = createVelocityContainer("media_row");
		row.contextPut("mediaSelection",  Boolean.valueOf(withMediaSelection));
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		tableEl.setCssDelegate(new MediaCssDelegate());
		tableEl.setElementCssClass("o");
		initSorters();
		initFilters();
		initFiltersPresets();
		tableEl.setSelectedFilterTab(ureq, allTab);
		tableEl.setAndLoadPersistedPreferences(ureq, "media-list-v3");

		String mapperThumbnailUrl = registerCacheableMapper(ureq, "media-thumbnail", new ThumbnailMapper(model, mediaService));
		row.contextPut("mapperThumbnailUrl", mapperThumbnailUrl);
		
		bulkDeleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		tableEl.addBatchButton(bulkDeleteButton);
		
		if(withUploadCard) {
			uploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "add.card.upload", null, formLayout);
			uploadEl.addActionListener(FormEvent.ONCHANGE);
			if(preselectedType != null) {
				uploadEl.setDndInformations(translate("dnd.infos." + preselectedType));
			}
			uploadEl.setFormLayout("minimal");
			tableEl.setZeroRowItem(uploadEl);
		}

		initTools(formLayout);
	}
	
	private void initTools(FormItemContainer formLayout) {
		addMediaLink = uifactory.addFormLink("add.media", formLayout, Link.BUTTON);
		addMediaLink.setIconLeftCSS("o_icon o_icon-fw o_icon_media");
		addMediaLink.setVisible(withAddMedias);
		
		DropdownItem addDropdown = uifactory.addDropdownMenu("add.more", "add.more", formLayout, getTranslator());
		addDropdown.setOrientation(DropdownOrientation.right);
		addDropdown.setElementCssClass("o_sel_add_more");
		addDropdown.setEmbbeded(true);
		addDropdown.setButton(true);
		addDropdown.setVisible(withAddMedias);
		
		if (!editableFileTypes.isEmpty()) {
			createFileLink = uifactory.addFormLink("create.file.title", formLayout, Link.LINK);
			createFileLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
			addDropdown.addElement(createFileLink);
		}
		
		addTextLink = uifactory.addFormLink("add.text", formLayout, Link.LINK);
		addTextLink.setIconLeftCSS("o_icon o_icon-fw o_filetype_txt");
		addDropdown.addElement(addTextLink);
		
		addCitationLink = uifactory.addFormLink("add.citation", formLayout, Link.LINK);
		addCitationLink.setIconLeftCSS("o_icon o_icon-fw o_icon_citation");
		addDropdown.addElement(addCitationLink);
		
		recordVideoLink = uifactory.addFormLink("create.version.video", formLayout, Link.LINK);
		recordVideoLink.setIconLeftCSS("o_icon o_icon-fw o_icon_video_record");
		addDropdown.addElement(recordVideoLink);
	
		if (drawioModule.isEnabled()) {
			createDrawioLink = uifactory.addFormLink("create.drawio", formLayout, Link.LINK);
			createDrawioLink.setIconLeftCSS("o_icon o_icon-fw o_filetype_drawio");
			addDropdown.addElement(createDrawioLink);
		}
	}
	
	private void initSorters() {
		List<FlexiTableSort> sorters = new ArrayList<>(14);
		sorters.add(new FlexiTableSort(translate(MediaCols.key.i18nHeaderKey()), MediaCols.key.name()));
		sorters.add(new FlexiTableSort(translate(MediaCols.title.i18nHeaderKey()), MediaCols.title.name()));
		sorters.add(new FlexiTableSort(translate(MediaCols.collectionDate.i18nHeaderKey()), MediaCols.collectionDate.name()));
		sorters.add(new FlexiTableSort(translate(MediaCols.tags.i18nHeaderKey()), MediaCols.tags.name()));
		sorters.add(new FlexiTableSort(translate(MediaCols.taxonomyLevels.i18nHeaderKey()), MediaCols.taxonomyLevels.name()));

		FlexiTableSortOptions options = new FlexiTableSortOptions(sorters);
		options.setDefaultOrderBy(new SortKey(MediaCols.title.name(), true));
		tableEl.setSortSettings(options);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		if(preselectedType == null) {
			SelectionValues typesKV = new SelectionValues();
			List<MediaHandler> handlers = mediaService.getMediaHandlers();
			for(MediaHandler handler:handlers) {
				typesKV.add(SelectionValues.entry(handler.getType(), translate("artefact." + handler.getType())));
			}
			FlexiTableMultiSelectionFilter membersFilter = new FlexiTableMultiSelectionFilter(translate("filter.types"),
					FILTER_TYPES, typesKV, true);
			filters.add(membersFilter);
		}
		
		List<TagInfo> tagInfos = mediaService.getTagInfos(null, getIdentity(), false);
		if (!tagInfos.isEmpty()) {
			filters.add(new FlexiTableTagFilter(translate("filter.tags"), FILTER_TAGS, tagInfos, true));
		}
		
		List<TaxonomyRef> taxonomies = mediaModule.getTaxonomyRefs();
		if(taxonomies != null && !taxonomies.isEmpty()) {
			List<TaxonomyLevel> allTaxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomies);
			SelectionValues taxonomyValues = RepositoyUIFactory.createTaxonomyLevelKV(getTranslator(), allTaxonomyLevels);
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.taxonomy.paths"),
					FILTER_TAXONOMY, taxonomyValues, true));
		}

		SelectionValues usedInKV = new SelectionValues();
		usedInKV.add(SelectionValues.entry(UsedIn.PAGE.name(), translate("filter.used.in.page")));
		usedInKV.add(SelectionValues.entry(UsedIn.PORTFOLIO.name(), translate("filter.used.in.portfolio")));
		FlexiTableMultiSelectionFilter usedInFilter = new FlexiTableMultiSelectionFilter(translate("filter.used.in"),
				FILTER_USED_IN, usedInKV, true);
		filters.add(usedInFilter);
		
		SelectionValues usageKV = new SelectionValues();
		usageKV.add(SelectionValues.entry("true", translate("filter.used.yes")));
		usageKV.add(SelectionValues.entry("false", translate("filter.used.no")));
		FlexiTableSingleSelectionFilter usageFilter = new FlexiTableSingleSelectionFilter(translate("filter.used"),
				FILTER_USED, usageKV, true);
		filters.add(usageFilter);
		
		SelectionValues sharedWithKV = new SelectionValues();
		sharedWithKV.add(SelectionValues.entry(MediaToGroupRelationType.USER.name(), translate("filter.shared.with.user")));
		sharedWithKV.add(SelectionValues.entry(MediaToGroupRelationType.BUSINESS_GROUP.name(), translate("filter.shared.with.group")));
		sharedWithKV.add(SelectionValues.entry(MediaToGroupRelationType.REPOSITORY_ENTRY.name(), translate("filter.shared.with.entry")));
		sharedWithKV.add(SelectionValues.entry(MediaToGroupRelationType.ORGANISATION.name(), translate("filter.shared.with.organisation")));
		FlexiTableMultiSelectionFilter sharedWithFilter = new FlexiTableMultiSelectionFilter(translate("filter.shared.with"),
				FILTER_SHARED_WITH, sharedWithKV, true);
		filters.add(sharedWithFilter);
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
			
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.reloadData, List.of());
		allTab.setElementCssClass("o_sel_media_all");
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		myTab = FlexiFiltersTabFactory.tabWithImplicitFilters(MY_TAB_ID, translate("filter.my"),
				TabSelectionBehavior.reloadData, List.of());
		myTab.setElementCssClass("o_sel_media_my");
		myTab.setFiltersExpanded(true);
		tabs.add(myTab);
		
		if(repositoryEntry != null) {
			sharedWithEntryTab = FlexiFiltersTabFactory.tabWithImplicitFilters(SHARED_TAB_WITH_ENTRY, translate("filter.shared.with.entry"),
					TabSelectionBehavior.reloadData, List.of());
			sharedWithEntryTab.setElementCssClass("o_sel_media_shared_entry");
			sharedWithEntryTab.setFiltersExpanded(true);
			tabs.add(sharedWithEntryTab);
		}
		
		sharedWithMeTab = FlexiFiltersTabFactory.tabWithImplicitFilters(SHARED_TAB_WITH_ME_ID, translate("filter.shared.with.me"),
				TabSelectionBehavior.reloadData, List.of());
		sharedWithMeTab.setElementCssClass("o_sel_media_shared_with_me");
		sharedWithMeTab.setFiltersExpanded(true);
		tabs.add(sharedWithMeTab);
		
		sharedByMeTab = FlexiFiltersTabFactory.tabWithImplicitFilters(SHARED_TAB_BY_ME_ID, translate("filter.shared.by.me"),
				TabSelectionBehavior.reloadData, List.of());
		sharedByMeTab.setElementCssClass("o_sel_media_shared_by_me");
		sharedByMeTab.setFiltersExpanded(true);
		tabs.add(sharedByMeTab);
		
		if(repositoryEntry == null) {
			notSharedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(NOT_SHARED_TAB_ID, translate("filter.not.shared"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_USED, "false")));
			notSharedTab.setElementCssClass("o_sel_media_not_shared");
			notSharedTab.setFiltersExpanded(true);
			tabs.add(notSharedTab);
		}
		
		searchTab = FlexiFiltersTabFactory.tabWithImplicitFilters(SEARCH_TAB_ID, translate("filter.search"),
				TabSelectionBehavior.clear, List.of());
		searchTab.setElementCssClass("o_sel_media_search");
		searchTab.setPosition(FlexiFilterTabPosition.right);
		searchTab.setLargeSearch(true);
		searchTab.setFiltersExpanded(true);
		tabs.add(searchTab);
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	public void setSelectedTab(UserRequest ureq, String tabId) {
		FlexiFiltersTab tab;
		switch(tabId) {
			case ALL_TAB_ID: tab = allTab; break;
			case MY_TAB_ID: tab = myTab; break;
			case SHARED_TAB_BY_ME_ID: tab = sharedByMeTab; break;
			case SHARED_TAB_WITH_ME_ID: tab = sharedWithMeTab; break;
			case SHARED_TAB_WITH_ENTRY: tab = sharedWithEntryTab; break;
			case NOT_SHARED_TAB_ID: tab = notSharedTab; break;
			case SEARCH_TAB_ID: tab = searchTab; break;
			default: tab = allTab; break;
		}
		tableEl.setSelectedFilterTab(ureq, tab);
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
	
	private void doSelectTab() {
		FlexiFiltersTab tab = tableEl.getSelectedFilterTab();
		if(tab.getSelectionBehavior() == TabSelectionBehavior.clear) {
			model.setObjects(new ArrayList<>());
			tableEl.reset(true, true, true);
		} else {
			loadModel(true);
		}
	}
	
	private void loadModel(boolean resetPage) {
		SearchMediaParameters params = getSearchParameters();
		List<MediaWithVersion> medias = mediaService.searchMedias(params);
		
		Map<Long,MediaRow> currentMap = model.getObjects()
				.stream().collect(Collectors.toMap(MediaRow::getKey, r -> r));
		List<MediaRow> rows = new ArrayList<>(medias.size());
		for(MediaWithVersion mediaWithVersion:medias) {
			Media media = mediaWithVersion.media();
			if(currentMap.containsKey(mediaWithVersion.getKey())) {
				MediaRow row = currentMap.get(mediaWithVersion.getKey());
				row.getOpenFormItem().getComponent().setCustomDisplayText(StringHelper.escapeHtml(media.getTitle()));
				rows.add(row);
			} else {
				MediaHandler handler = mediaService.getMediaHandler(media.getType());
				if(handler != null) {
					MediaVersion currentVersion = mediaWithVersion.version();
					boolean hasThumbnail = vfsRepositoryService.isThumbnailAvailable(mediaWithVersion.metadata());
					String mediaTitle = StringHelper.escapeHtml(media.getTitle());
					String iconCssClass = currentVersion == null ? "" : handler.getIconCssClass(currentVersion);
					FormLink openLink =  uifactory.addFormLink("select_" + (++counter), "select", mediaTitle, null, flc, Link.NONTRANSLATED);
					openLink.setIconLeftCSS("o_icon ".concat(iconCssClass));
					openLink.setEnabled(withMediaSelection);
					MediaRow row = new MediaRow(media, currentVersion, hasThumbnail, openLink, iconCssClass);
					row.setVersioned(mediaWithVersion.numOfVersions() > 1l);
					openLink.setUserObject(row);
					rows.add(row);
				}
			}
		}
		model.setObjects(rows);
		
		Map<Long,MediaRow> rowMap = model.getObjects()
				.stream().collect(Collectors.toMap(MediaRow::getKey, r -> r, (u, v) -> u));
		
		List<MediaTag> tags = mediaService.getTags(getIdentity(), List.copyOf(rowMap.keySet()));
		for(MediaTag tag:tags) {
			String name = tag.getTag().getDisplayName();
			MediaRow mRow = rowMap.get(tag.getMedia().getKey());
			if(mRow != null) {
				mRow.addTag(name);
			}
		}
		
		List<MediaToTaxonomyLevel> relationToLevels = mediaService.getTaxonomyLevels(getIdentity());
		for(MediaToTaxonomyLevel relationToLevel:relationToLevels) {
			TaxonomyLevel level = relationToLevel.getTaxonomyLevel();
			MediaRow mRow = rowMap.get(relationToLevel.getMedia().getKey());
			if(mRow != null) {
				String levelName = TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, level, EMPTY);
				mRow.addTaxonomyLevel(level, levelName);
			}
		}

		tableEl.reset(resetPage, true, true);
	}
	
	private void reloadBreadcrump() {
		if(stackPanel == null || detailsCtrl == null || detailsCtrl.getMedia() == null) {
			return;
		}
		Media media = detailsCtrl.getMedia();
		stackPanel.changeDisplayname(media.getTitle());
	}
	
	private SearchMediaParameters getSearchParameters() {
		SearchMediaParameters params = new SearchMediaParameters();
		params.setSearchString(tableEl.getQuickSearchString());
		params.setIdentity(getIdentity());
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		
		if(preselectedType != null) {
			params.setTypes(List.of(preselectedType));
		} else {
			FlexiTableFilter typeFilters = FlexiTableFilter.getFilter(filters, FILTER_TYPES);
			if (typeFilters != null) {
				List<String> filterValues = ((FlexiTableExtendedFilter)typeFilters).getValues();
				if (filterValues != null && !filterValues.isEmpty()) {
					params.setTypes(filterValues);
				}
			}
		}
		
		FlexiTableFilter tagsFilters = FlexiTableFilter.getFilter(filters, FILTER_TAGS);
		if (tagsFilters != null) {
			List<String> filterValues = ((FlexiTableTagFilter)tagsFilters).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				List<Long> selectedTagKeys = filterValues.stream()
						.map(Long::valueOf).toList();
				params.setTags(selectedTagKeys);
			}
		}
		
		FlexiTableFilter usedFilters = FlexiTableFilter.getFilter(filters, FILTER_USED);
		if (usedFilters != null) {
			String filterValue = usedFilters.getValue();
			if ("true".equals(filterValue)) {
				params.setUsedIn(List.of(UsedIn.PAGE, UsedIn.PORTFOLIO));
			} else if ("false".equals(filterValue)) {
				params.setUsedIn(List.of(UsedIn.NOT_USED));
			}
		}
		
		FlexiTableFilter usedInFilters = FlexiTableFilter.getFilter(filters, FILTER_USED_IN);
		if (usedInFilters != null) {
			List<String> filterValues = ((FlexiTableMultiSelectionFilter)usedInFilters).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				List<UsedIn> selectedUsedIn = filterValues.stream()
						.map(UsedIn::valueOf).toList();
				params.setUsedIn(selectedUsedIn);
			}
		}
		
		FlexiTableFilter sharedWithFilters = FlexiTableFilter.getFilter(filters, FILTER_SHARED_WITH);
		if (sharedWithFilters != null) {
			List<String> filterValues = ((FlexiTableMultiSelectionFilter)sharedWithFilters).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				List<MediaToGroupRelationType> selectedSharedWith = filterValues.stream()
						.map(MediaToGroupRelationType::valueOf).toList();
				params.setSharedWith(selectedSharedWith);
			}
		}

		FlexiTableFilter taxonomyFilters = FlexiTableFilter.getFilter(filters, FILTER_TAXONOMY);
		if (taxonomyFilters != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)taxonomyFilters).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				List<TaxonomyLevelRef> taxonomyLevels = filterValues.stream()
						.filter(StringHelper::isLong)
						.map( val -> new TaxonomyLevelRefImpl(Long.valueOf(val)))
						.collect(Collectors.toList());
				params.setTaxonomyLevelsRefs(taxonomyLevels);
			}
		}
		
		FlexiFiltersTab selectedTab = tableEl.getSelectedFilterTab();
		if(selectedTab == myTab) {
			params.setScope(Scope.MY);
		} else if(selectedTab == sharedWithMeTab) {
			params.setScope(Scope.SHARED_WITH_ME);
		} else if(selectedTab == sharedByMeTab) {
			params.setScope(Scope.SHARED_BY_ME);
		} else if(selectedTab == sharedWithEntryTab) {
			params.setScope(Scope.SHARED_WITH_ENTRY);
			params.setRepositoryEntry(repositoryEntry);
		} else if(selectedTab == notSharedTab) {
			params.setUsedIn(List.of(UsedIn.NOT_USED));
		} else {
			params.setScope(Scope.ALL);
		}
		return params;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Media".equalsIgnoreCase(resName)) {
			FlexiFiltersTab tab = tableEl.getSelectedFilterTab();
			if(tab == null) {
				tableEl.setSelectedFilterTab(ureq, allTab);
			}
			Long resId = entries.get(0).getOLATResourceable().getResourceableId();
			if(!activateMedia(ureq, resId) && tableEl.getSelectedFilterTab() != allTab) {
				tableEl.setSelectedFilterTab(ureq, allTab);
				activateMedia(ureq, resId);
			}
			
			if(stackPanel != null) {
				addToHistory(ureq, this);
			}
		}
	}
	
	private boolean activateMedia(UserRequest ureq, Long resId) {
		if(detailsCtrl != null) {
			stackPanel.popController(detailsCtrl);
			removeAsListenerAndDispose(detailsCtrl);
		}
		
		for(MediaRow row:model.getObjects()) {
			if(row.getKey().equals(resId)) {
				doOpenMedia(ureq, resId);
				return true;
			}
		}
		return false;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
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
		} else if(recordVideoLink == source) {
			doRecordVideo(ureq);
		} else if(createDrawioLink== source) {
			doAddDrawio(ureq);
		} else if(bulkDeleteButton == source) {
			doConfirmDelete(ureq);
		} else if(uploadEl == source) {
			if("ONCHANGE".equals(event.getCommand())) {
				doUpload(ureq);
			}
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				MediaRow row = model.getObject(se.getIndex());
				if("select".equals(cmd)) {
					if(withSelect) {
						doSelect(ureq, row.getKey());
					} else {
						Activateable2 activateable = doOpenMedia(ureq, row.getKey());
						if(activateable != null) {
							activateable.activate(ureq, null, null);
						}
					}
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				loadModel(true);
			} else if(event instanceof FlexiTableFilterTabEvent) {
				doSelectTab();
			}
		} else if(newMediaCallout == source) {
			doOpenNewMediaCallout(ureq, newMediaCallout);
		} else if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			if("select".equals(cmd)) {
				MediaRow row = (MediaRow)link.getUserObject();
				if(withSelect) {
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
		if (createFileCtrl == source || mediaUploadCtrl == source || textUploadCtrl == source
				|| citationUploadCtrl == source || recordVideoCtrl == source || createDrawioCtrl == source
				|| confirmDeleteMediaCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(false);
			}
			cmc.deactivate();
			if(withSelect || event == Event.DONE_EVENT) {
				if(createFileCtrl == source) {
					doOpenOrSelectNew(ureq, createFileCtrl.getMediaReference());
				} else if(mediaUploadCtrl == source) {
					doOpenOrSelectNew(ureq, mediaUploadCtrl.getMediaReference());
				} else if(textUploadCtrl == source) {
					doOpenOrSelectNew(ureq, textUploadCtrl.getMediaReference());
				} else if(citationUploadCtrl == source) {
					doOpenOrSelectNew(ureq, citationUploadCtrl.getMediaReference());
				} else if(recordVideoCtrl == source) {
					doOpenOrSelectNew(ureq, recordVideoCtrl.getMediaReference());
				} else if(createDrawioCtrl == source) {
					doOpenOrSelectNew(ureq, createDrawioCtrl.getMediaReference());
				}
			}
			cleanUp();
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
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel(false);
				reloadBreadcrump();
			} else if(event instanceof MediaEvent me) {
				if(MediaEvent.DELETED.equals(me.getCommand())) {
					stackPanel.popController(detailsCtrl);
					loadModel(false);
				}
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteMediaCtrl);
		removeAsListenerAndDispose(citationUploadCtrl);
		removeAsListenerAndDispose(createDrawioCtrl);
		removeAsListenerAndDispose(mediaUploadCtrl);
		removeAsListenerAndDispose(recordVideoCtrl);
		removeAsListenerAndDispose(createFileCtrl);
		removeAsListenerAndDispose(textUploadCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteMediaCtrl = null;
		citationUploadCtrl = null;
		createDrawioCtrl = null;
		mediaUploadCtrl = null;
		recordVideoCtrl = null;
		createFileCtrl = null;
		textUploadCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == mainForm.getInitialComponent() && "ONCLICK".equals(event.getCommand())) {
			String rowKeyStr = ureq.getParameter("img_select");
			if(StringHelper.isLong(rowKeyStr)) {
				try {
					Long rowKey = Long.valueOf(rowKeyStr);
					List<MediaRow> rows = model.getObjects();
					for(MediaRow row:rows) {
						if(row != null && row.getKey().equals(rowKey)) {
							if(withSelect) {
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
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doUpload(UserRequest ureq) {
		String filename = uploadEl.getUploadFileName();
		File file = uploadEl.getUploadFile();
		String mimeType = WebappHelper.getMimeType(filename);
		UploadMedia uploadMedia = new UploadMedia(file, filename, mimeType);
		UploadMediaEvent umw = new UploadMediaEvent(uploadMedia);
		this.fireEvent(ureq, umw);
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

	private void doRecordVideo(UserRequest ureq) {
		if(guardModalController(recordVideoCtrl)) return;
		
		String businessPath = getWindowControl().getBusinessControl().getAsString();
		recordVideoCtrl = new AVVideoMediaController(ureq, getWindowControl(), businessPath,
				VideoHandler.MAX_RECORDING_TIME_IN_MS, VideoHandler.VIDEO_QUALITY);
		listenTo(recordVideoCtrl);
		
		String title = translate("record.video");
		cmc = new CloseableModalController(getWindowControl(), null, recordVideoCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddDrawio(UserRequest ureq) {
		if(guardModalController(createDrawioCtrl)) return;
		
		createDrawioCtrl = new CreateDrawioMediaController(ureq, getWindowControl());
		listenTo(createDrawioCtrl);
		
		String title = translate("create.drawio");
		cmc = new CloseableModalController(getWindowControl(), null, createDrawioCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenOrSelectNew(UserRequest ureq, Media media) {
		if(media == null) return;
		
		media = mediaService.getMediaByKey(media.getKey());
		MediaVersion currentVersion = media.getVersions().get(0);
		MediaRow mediaRow = model.getObjectByMediaKey(media.getKey());
		if(mediaRow != null) {
			MediaHandler handler = mediaService.getMediaHandler(media.getType());
			VFSLeaf thumbnail = handler.getThumbnail(currentVersion, THUMBNAIL_SIZE);
			mediaRow.setThumbnailAvailable(thumbnail != null);
		}
		if(withSelect) {
			fireEvent(ureq, new MediaSelectionEvent(media));
		} else {
			doOpenMedia(ureq, media, currentVersion);
		}
	}

	private void doSelect(UserRequest ureq, Long mediaKey) {
		Media media = mediaService.getMediaByKey(mediaKey);
		fireEvent(ureq, new MediaSelectionEvent(media));
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
		Media media = mediaService.getMediaByKey(mediaKey);
		MediaVersion currentVersion = media.getVersions().get(0);
		return doOpenMedia(ureq, media, currentVersion);
	}
		
	private Activateable2 doOpenMedia(UserRequest ureq, Media media, MediaVersion version) {
		stackPanel.popUpToController(this);
		
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("Media", media.getKey());
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		detailsCtrl = new MediaDetailsController(ureq, swControl, media, version);
		listenTo(detailsCtrl);
		
		stackPanel.pushController(media.getTitle(), detailsCtrl);
		return detailsCtrl;
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		List<MediaRow> rows = selectedIndex.stream()
				.map(index -> model.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.toList();
		
		List<Long> rowsKeys = rows.stream()
				.map(MediaRow::getKey)
				.toList();
		
		final List<Long> rowsKeysToDelete = mediaService.filterOwnedDeletableMedias(getIdentity(), rowsKeys);
		if(rowsKeysToDelete.isEmpty()) {
			showWarning("warning.atleast.one.deletable");
		} else {
			List<MediaRow> rowsToDelete = rows.stream()
					.filter(row -> rowsKeysToDelete.contains(row.getKey()))
					.toList();

			confirmDeleteMediaCtrl = new ConfirmDeleteMediaController(ureq, getWindowControl(), rowsToDelete);
			listenTo(confirmDeleteMediaCtrl);
			
			String title = translate("delete");
			cmc = new CloseableModalController(getWindowControl(), null, confirmDeleteMediaCtrl.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private class MediaCssDelegate extends DefaultFlexiTableCssDelegate {

		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return withSelect ? "o_medias_table o_medias_select clearfix" : "o_medias_table clearfix";
		}

		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_media_card_cell";
		}
	}
	
	private static class ThumbnailMapper implements Mapper {
		
		private final MediaDataModel mediaModel;
		private final MediaService mediaService;
		
		public ThumbnailMapper(MediaDataModel model, MediaService mediaService) {
			this.mediaModel = model;
			this.mediaService = mediaService;
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
				MediaRow mediaRow = mediaModel.getObjectByMediaKey(Long.valueOf(row)); 
				if(mediaRow != null) {
					MediaHandler handler = mediaService.getMediaHandler(mediaRow.getType());
					VFSLeaf thumbnail = handler.getThumbnail(mediaRow.getVersion(), THUMBNAIL_SIZE);
					if(thumbnail != null) {
						mr = new VFSMediaResource(thumbnail);
					}
				}
			}
			
			return mr == null ? new NotFoundMediaResource() : mr;
		}
	}
	
	private static class NewMediasController extends BasicController {

		private final Link addFileLink;
		private final Link addMediaLink;
		private final Link addTextLink;
		private final Link addCitationLink;
		
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
			if(source instanceof Link link) {
				fireEvent(ureq, new Event(link.getCommand()));
			}
		}
	}
}

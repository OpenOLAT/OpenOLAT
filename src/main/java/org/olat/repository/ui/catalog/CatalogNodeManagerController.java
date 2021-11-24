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
package org.olat.repository.ui.catalog;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.NewControllerFactory;
import org.olat.admin.securitygroup.gui.GroupController;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.securitygroup.gui.IdentitiesRemoveEvent;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.login.LoginModule;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.CatalogEntry;
import org.olat.repository.CatalogEntry.Style;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.RepositorySearchController;
import org.olat.repository.manager.CatalogManager;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.OrderBy;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.repository.ui.CatalogEntryImageMapper;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.RepositoryTableModel;
import org.olat.repository.ui.author.ACRenderer;
import org.olat.repository.ui.author.AccessRenderer;
import org.olat.repository.ui.author.TypeRenderer;
import org.olat.repository.ui.catalog.CatalogEntryRowModel.Cols;
import org.olat.repository.ui.catalog.NodeEntryRowModel.NodeCols;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogNodeManagerController extends FormBasicController implements Activateable2 {

	public static final String LOCK_TOKEN = "catalogeditlock";
	public static final OLATResourceable lockRes = OresHelper.createOLATResourceableType("CatalogNodeManagerController");
	
	private final TooledStackedPanel toolbarPanel;
	private final AtomicInteger counter = new AtomicInteger();
	
	private GroupController groupCtrl;
	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private RepositorySearchController entrySearchCtrl;
	private CatalogEntryPositionDialogController positionCtrl;
	private CloseableCalloutWindowController positionCalloutCtrl;

	private CatalogNodeManagerController childNodeCtrl;
	private CatalogNodeManagerController positionMoveCtrl;
	private CatalogEntryMoveController categoryMoveCtrl;
	private CatalogEntryMoveController entryResourceMoveCtrl;
	private CatalogEntryEditController addEntryCtrl;
	private CatalogEntryEditController editEntryCtrl;
	private DialogBoxController dialogDeleteLink;
	private DialogBoxController dialogDeleteSubtree;
	
	private FlexiTableElement entriesEl;
	private FlexiTableElement closedEntriesEl;
	private FlexiTableElement nodeEntriesEl;
	private CatalogEntryRowModel entriesModel;
	private CatalogEntryRowModel closedEntriesModel;
	private NodeEntryRowModel nodeEntriesModel;
	
	private Link editLink;
	private Link moveLink;
	private Link deleteLink;
	private Link nominateLink;
	private Link contactLink;
	private Link addCategoryLink;
	private Link addResourceLink;
	private Link orderManuallyLink;

	private boolean showCategoryUpDownColumn;
	private boolean showEntryUpDownColumn;

	private List<DefaultFlexiColumnModel> leafColumns;
	
	private static final String CMD_UP = "leaf_up";
	private static final String CMD_DOWN = "leaf_down";
	
	private LockResult catModificationLock;
	private final MapperKey mapperThumbnailKey;
	private final WindowControl rootwControl;

	private final boolean isGuest;
	private final boolean isAuthor;
	private final boolean isAdministrator;
	private final boolean isLocalTreeAdmin;
	private boolean isOrdering;
	
	private CatalogEntry catalogEntry;

	@Autowired
	private ACService acService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private CatalogManager catalogManager;
	@Autowired
	private RepositoryManager repositoryManager;
	
	public CatalogNodeManagerController(UserRequest ureq, WindowControl wControl, WindowControl rootwControl,
			CatalogEntry catalogEntry, TooledStackedPanel stackPanel, boolean localTreeAdmin) {
		super(ureq, wControl, "node");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(), getTranslator()));
		
		this.toolbarPanel = stackPanel;
		this.catalogEntry = catalogEntry;
		this.rootwControl = rootwControl;
		mapperThumbnailKey = mapperService.register(null, "catalogentryImage", new CatalogEntryImageMapper());
		
		Roles roles = ureq.getUserSession().getRoles();
		isAuthor = roles.isAuthor();
		isGuest = roles.isGuestOnly();
		isAdministrator = roles.isAdministrator() || roles.isLearnResourceManager();
		isOrdering = false;
		flc.contextPut("isOrdering", isOrdering);
		
		this.toolbarPanel.addListener(this);
		
		if(isAdministrator) {
			isLocalTreeAdmin = false;
		} else {
			isLocalTreeAdmin = localTreeAdmin || catalogManager.isOwner(catalogEntry, getIdentity());
		}

		initForm(ureq);
		
		loadEntryInfos();
		loadNodesChildren();
		loadResources(ureq);
		//catch the events from the velocity template
		flc.getFormItemComponent().addListener(this);
	}
	
	public CatalogNodeManagerController(UserRequest ureq, WindowControl wControl, WindowControl rootwControl,
			CatalogEntry catalogEntry, TooledStackedPanel stackPanel, boolean localTreeAdmin, boolean isOrdering) {
		super(ureq, wControl, "node");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(), getTranslator()));
		
		this.toolbarPanel = stackPanel;
		this.catalogEntry = catalogEntry;
		this.rootwControl = rootwControl;
		mapperThumbnailKey = mapperService.register(null, "catalogentryImage", new CatalogEntryImageMapper());
		
		Roles roles = ureq.getUserSession().getRoles();
		isAuthor = roles.isAuthor();
		isGuest = roles.isGuestOnly();
		isAdministrator = roles.isAdministrator() || roles.isLearnResourceManager();
		this.isOrdering = isOrdering;
		flc.contextPut("isOrdering", isOrdering);
		
		this.toolbarPanel.addListener(this);
		
		if(isAdministrator) {
			isLocalTreeAdmin = false;
		} else {
			isLocalTreeAdmin = localTreeAdmin || catalogManager.isOwner(catalogEntry, getIdentity());
		}

		if(catalogEntry.getEntryAddPosition() == null) {
			showEntryUpDownColumn = repositoryModule.getCatalogAddEntryPosition() != 0;
		} else {
			showEntryUpDownColumn = catalogEntry.getEntryAddPosition() != 0;
		}

		if(catalogEntry.getCategoryAddPosition() == null) {
			showCategoryUpDownColumn = repositoryModule.getCatalogAddCategoryPosition() != 0;
		} else {
			showCategoryUpDownColumn = catalogEntry.getCategoryAddPosition() != 0;
		}

		initForm(ureq);

		loadEntryInfos();
		loadNodesChildren();
		loadResources(ureq);
		//catch the events from the velocity template
		flc.getFormItemComponent().addListener(this);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//one mapper for all users
		flc.contextPut("mapperThumbnailUrl", mapperThumbnailKey.getUrl());
		
		int level  = 0;
		CatalogEntry parent = catalogEntry.getParent();
		while (parent != null) {
			level++;
			parent = parent.getParent();			
		}
		flc.contextPut("catalogLevel", level);
		
		String url = Settings.getServerContextPathURI() + "/url/CatalogEntry/" + catalogEntry.getKey();
		if(loginModule.isGuestLoginLinksEnabled()) {
			flc.contextPut("guestExtLink", url + "?guest=true&amp;lang=" + getLocale().getLanguage());
		}
		if (!isGuest) {
			flc.contextPut("extLink", url);
		}
		
		leafColumns = new ArrayList<>();
		
		FlexiTableColumnModel entriesColumnsModel = getCatalogFlexiTableColumnModel("opened-", !isOrdering, showEntryUpDownColumn);
		entriesModel = new CatalogEntryRowModel(entriesColumnsModel);
		entriesEl = uifactory.addTableElement(getWindowControl(), "entries", entriesModel, 20, false, getTranslator(), formLayout);
		
		FlexiTableColumnModel closedEntriesColumnsModel = getCatalogFlexiTableColumnModel("closed-", !isOrdering, showEntryUpDownColumn);
		closedEntriesModel = new CatalogEntryRowModel(closedEntriesColumnsModel);
		closedEntriesEl = uifactory.addTableElement(getWindowControl(), "closedEntries", closedEntriesModel, 20, false, getTranslator(), formLayout);
		
		FlexiTableColumnModel nodeEntriesColumnsModel = getNodeFlexiTableColumnModel(showCategoryUpDownColumn);
		nodeEntriesModel = new NodeEntryRowModel(nodeEntriesColumnsModel);
		nodeEntriesEl = uifactory.addTableElement(getWindowControl(), "nodeEntries", nodeEntriesModel, 20, false, getTranslator(), formLayout);
	}
	
	private FlexiTableColumnModel getCatalogFlexiTableColumnModel(String cmdPrefix, boolean sortEnabled, boolean showUpDownColumn) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel columnModel;
		
		if (!sortEnabled && showUpDownColumn) {
			DefaultFlexiColumnModel leafUpColumnModel = new DefaultFlexiColumnModel(true, Cols.up.i18nKey(), Cols.up.ordinal(), CMD_UP, false, null);
			leafUpColumnModel.setCellRenderer(new BooleanCellRenderer(
					new StaticFlexiCellRenderer("", CMD_UP, "o_icon o_icon-lg o_icon_move_up"),
					null));
			leafUpColumnModel.setIconHeader("o_icon o_icon_fw o_icon-lg o_icon_move_up");
			leafUpColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_ICON);
			leafUpColumnModel.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(leafUpColumnModel);
			
			DefaultFlexiColumnModel leafDownColumnModel = new DefaultFlexiColumnModel(true, Cols.down.i18nKey(), Cols.down.ordinal(), CMD_DOWN, false, null);
			leafDownColumnModel.setCellRenderer(new BooleanCellRenderer(
					new StaticFlexiCellRenderer("", CMD_DOWN, "o_icon o_icon-lg o_icon_move_down"),
					null));
			leafDownColumnModel.setIconHeader("o_icon o_icon_fw o_icon-lg o_icon_move_down");
			leafDownColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_ICON);	
			leafDownColumnModel.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(leafDownColumnModel);

			DefaultFlexiColumnModel leafPositionColumnModel = new DefaultFlexiColumnModel(true, Cols.position.i18nKey(), Cols.position.ordinal(), false, null);
			columnsModel.addFlexiColumnModel(leafPositionColumnModel);
		}
		
		columnModel = new DefaultFlexiColumnModel(false, Cols.key.i18nKey(), Cols.key.ordinal(), sortEnabled, OrderBy.key.name());
		leafColumns.add(columnModel);
		columnsModel.addFlexiColumnModel(columnModel);
		
		columnModel = new DefaultFlexiColumnModel(true, Cols.type.i18nKey(), Cols.type.ordinal(), sortEnabled, OrderBy.type.name(),
				FlexiColumnModel.ALIGNMENT_LEFT, new TypeRenderer());
		leafColumns.add(columnModel);
		columnsModel.addFlexiColumnModel(columnModel);
		
		FlexiCellRenderer renderer = new StaticFlexiCellRenderer(cmdPrefix + "select", new TextFlexiCellRenderer());
		columnModel = new DefaultFlexiColumnModel(Cols.displayName.i18nKey(), Cols.displayName.ordinal(), cmdPrefix + "select",
				sortEnabled, OrderBy.displayname.name(), renderer);
		leafColumns.add(columnModel);
		columnsModel.addFlexiColumnModel(columnModel);
		
		columnModel = new DefaultFlexiColumnModel(false, Cols.authors.i18nKey(), Cols.authors.ordinal(),
				sortEnabled, OrderBy.authors.name());
		leafColumns.add(columnModel);
		columnsModel.addFlexiColumnModel(columnModel);
		
		if(repositoryModule.isManagedRepositoryEntries()) {
			columnModel = new DefaultFlexiColumnModel(false, Cols.externalId.i18nKey(), Cols.externalId.ordinal(),
					sortEnabled, OrderBy.externalId.name());
			leafColumns.add(columnModel);
			columnsModel.addFlexiColumnModel(columnModel);
		}
		
		columnModel = new DefaultFlexiColumnModel(false, Cols.externalRef.i18nKey(), Cols.externalRef.ordinal(),
				sortEnabled, OrderBy.externalRef.name());
		leafColumns.add(columnModel);
		columnsModel.addFlexiColumnModel(columnModel);
		
		columnModel = new DefaultFlexiColumnModel(false, Cols.lifecycleLabel.i18nKey(), Cols.lifecycleLabel.ordinal(),
				sortEnabled, OrderBy.lifecycleLabel.name());
		leafColumns.add(columnModel);
		columnsModel.addFlexiColumnModel(columnModel);
		
		columnModel = new DefaultFlexiColumnModel(false, Cols.lifecycleSoftkey.i18nKey(), Cols.lifecycleSoftkey.ordinal(),
				sortEnabled, OrderBy.lifecycleSoftkey.name());
		leafColumns.add(columnModel);
		columnsModel.addFlexiColumnModel(columnModel);
		
		columnModel = new DefaultFlexiColumnModel(true, Cols.lifecycleStart.i18nKey(), Cols.lifecycleStart.ordinal(),
				sortEnabled, OrderBy.lifecycleStart.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale()));
		leafColumns.add(columnModel);
		columnsModel.addFlexiColumnModel(columnModel);
		
		columnModel = new DefaultFlexiColumnModel(true, Cols.lifecycleEnd.i18nKey(), Cols.lifecycleEnd.ordinal(),
				sortEnabled, OrderBy.lifecycleEnd.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale()));
		leafColumns.add(columnModel);
		columnsModel.addFlexiColumnModel(columnModel);
		
		columnModel = new DefaultFlexiColumnModel(true, Cols.access.i18nKey(), Cols.access.ordinal(),
				sortEnabled, OrderBy.access.name(), FlexiColumnModel.ALIGNMENT_LEFT, new AccessRenderer(getLocale()));
		leafColumns.add(columnModel);
		columnsModel.addFlexiColumnModel(columnModel);
		
		columnModel = new DefaultFlexiColumnModel(true, Cols.ac.i18nKey(), Cols.ac.ordinal(),
				sortEnabled, OrderBy.ac.name(), FlexiColumnModel.ALIGNMENT_LEFT, new ACRenderer());
		leafColumns.add(columnModel);
		columnsModel.addFlexiColumnModel(columnModel);
		
		columnModel = new DefaultFlexiColumnModel(false, Cols.creationDate.i18nKey(), Cols.creationDate.ordinal(),
				sortEnabled, OrderBy.creationDate.name());
		leafColumns.add(columnModel);
		columnsModel.addFlexiColumnModel(columnModel);
		
		if (!isOrdering) {
			if(isAdministrator || isLocalTreeAdmin) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.delete.i18nKey(), translate(Cols.delete.i18nKey()), cmdPrefix + "delete"));
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.move.i18nKey(), translate(Cols.move.i18nKey()), cmdPrefix + "move"));
			}
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.detailsSupported.i18nKey(), Cols.detailsSupported.ordinal(), cmdPrefix + "details",
					new StaticFlexiCellRenderer("", cmdPrefix + "details", "o_icon o_icon-lg o_icon_details", translate("details"))));
		}
		
		return columnsModel;
	}
	
	private FlexiTableColumnModel getNodeFlexiTableColumnModel(boolean showUpDownColumn) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		if (showUpDownColumn) {
			DefaultFlexiColumnModel nodeUpColumnModel = new DefaultFlexiColumnModel(true, NodeCols.up.i18nKey(), NodeCols.up.ordinal(), CMD_UP, false, null);
			nodeUpColumnModel.setCellRenderer(new BooleanCellRenderer(
					new StaticFlexiCellRenderer("", CMD_UP, "o_icon o_icon_fw o_icon-lg o_icon_move_up"),
					null));
			nodeUpColumnModel.setIconHeader("o_icon o_icon_fw o_icon-lg o_icon_move_up");
			nodeUpColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_ICON);
			nodeUpColumnModel.setAlwaysVisible(true);


			DefaultFlexiColumnModel nodeDownColumnModel = new DefaultFlexiColumnModel(true, NodeCols.down.i18nKey(), NodeCols.down.ordinal(), CMD_DOWN, false, null);
			nodeDownColumnModel.setCellRenderer(new BooleanCellRenderer(
					new StaticFlexiCellRenderer("", CMD_DOWN, "o_icon o_icon_fw o_icon-lg o_icon_move_down"),
					null));
			nodeDownColumnModel.setIconHeader("o_icon o_icon_fw o_icon-lg o_icon_move_down");
			nodeDownColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_ICON);
			nodeDownColumnModel.setAlwaysVisible(true);

			DefaultFlexiColumnModel nodePositionColumnModel = new DefaultFlexiColumnModel(true, NodeCols.position.i18nKey(), NodeCols.position.ordinal(), false, null);

			columnsModel.addFlexiColumnModel(nodeUpColumnModel);
			columnsModel.addFlexiColumnModel(nodeDownColumnModel);
			columnsModel.addFlexiColumnModel(nodePositionColumnModel);
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, NodeCols.key.i18nKey(), NodeCols.key.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, NodeCols.displayName.i18nKey(), NodeCols.displayName.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, NodeCols.creationDate.i18nKey(), NodeCols.creationDate.ordinal(), false, null));
		return columnsModel;
	} 

	private void loadEntryInfos() {
		catalogEntry = catalogManager.loadCatalogEntry(catalogEntry);
		flc.contextPut("catalogEntryTitle", catalogEntry.getName());
		flc.contextPut("catalogEntryShortTitle", catalogEntry.getShortTitle());
		if(StringHelper.containsNonWhitespace(catalogEntry.getDescription())) {
			flc.contextPut("catalogEntryDesc", catalogEntry.getDescription());
		}
		VFSLeaf image = catalogManager.getImage(catalogEntry);
		if(image != null) {
			flc.contextPut("catThumbnail", image.getName());
		}
		if(catalogEntry.getStyle() != null) {
			flc.contextPut("listStyle", catalogEntry.getStyle().name());
		} else {
			flc.contextPut("listStyle", Style.tiles.name());
		}
	}

	private void loadResources(UserRequest ureq) {
		catalogEntry = catalogManager.loadCatalogEntry(catalogEntry);
		List<CatalogEntry> detachedChildren = catalogManager.getChildrenOf(catalogEntry);
		
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(getIdentity(), ureq.getUserSession().getRoles());
		params.setParentEntry(catalogEntry);
		List<RepositoryEntry> repoEntries = repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, false);
		
		List<Long> resourceKeys = new ArrayList<>();
		for (RepositoryEntry entry : repoEntries) {
			resourceKeys.add(entry.getOlatResource().getKey());
		}

		List<OLATResourceAccess> resourcesWithOffer = acService.getAccessMethodForResources(resourceKeys, null, true, new Date());
		
		List<CatalogEntryRow> items = new ArrayList<>();
		List<CatalogEntryRow> closedItems = new ArrayList<>();
		for(RepositoryEntry entry:repoEntries) {
			CatalogEntryRow row = new CatalogEntryRow(entry);
			for (CatalogEntry catEntry : detachedChildren) {
				if (catEntry != null && catEntry.getType() == CatalogEntry.TYPE_LEAF && catEntry.getRepositoryEntry().equals(entry)) {
					row = new CatalogEntryRow(entry, catEntry);
					break;
				} 
			}
			
			List<PriceMethod> types = new ArrayList<>(3);
			
			if(entry.isBookable()) {
				// collect access control method icons
				OLATResource resource = entry.getOlatResource();
				for(OLATResourceAccess resourceAccess:resourcesWithOffer) {
					if(resource.getKey().equals(resourceAccess.getResource().getKey())) {
						for(PriceMethodBundle bundle:resourceAccess.getMethods()) {
							String type = (bundle.getMethod().getMethodCssClass() + "_icon").intern();
							String price = bundle.getPrice() == null || bundle.getPrice().isEmpty() ? "" : PriceFormat.fullFormat(bundle.getPrice());
							AccessMethodHandler amh = acModule.getAccessMethodHandler(bundle.getMethod().getType());
							String displayName = amh.getMethodName(getLocale());
							types.add(new PriceMethod(price, type, displayName));
						}
					}
				}
			} else if (!entry.isAllUsers() && !entry.isGuests()) {
				// members only always show lock icon
				types.add(new PriceMethod("", "o_ac_membersonly_icon", translate("cif.access.membersonly.short")));
			} 
			
			if(!types.isEmpty()) {
				row.setAccessTypes(types);
			}
			
			if(entry.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
				FormLink positionLink = uifactory.addFormLink("position_" + counter.incrementAndGet(), "positionClosedEntries", String.valueOf(row.getPosition() + 1), null, null, Link.NONTRANSLATED);
				positionLink.setUserObject(row);
				row.setPositionLink(positionLink);
				
				closedItems.add(row);
			} else {
				FormLink positionLink = uifactory.addFormLink("position_" + counter.incrementAndGet(), "positionEntries", String.valueOf(row.getPosition() + 1), null, null, Link.NONTRANSLATED);
				positionLink.setUserObject(row);
				row.setPositionLink(positionLink);
				
				items.add(row);
			}
		}

		if (catalogManager.isEntrySortingManually(catalogEntry)) {
			Comparator<CatalogEntryRow> comparator = Comparator.comparing(CatalogEntryRow::getPosition);

			items.sort(comparator);
			closedItems.sort(comparator);
		} else {
			Collator collator = Collator.getInstance(getLocale());
			collator.setStrength(Collator.IDENTICAL);

			items.sort(Comparator.comparing(CatalogEntryRow::getDisplayname, collator));
			closedItems.sort(Comparator.comparing(CatalogEntryRow::getDisplayname, collator));
		}
		
		entriesModel.setObjects(items);
		entriesEl.reset(true, true, true);
		entriesEl.setVisible(entriesModel.getRowCount() > 0);
		
		closedEntriesModel.setObjects(closedItems);
		closedEntriesEl.reset(true, true, true);
		closedEntriesEl.setVisible(closedEntriesModel.getRowCount() > 0);
		
		flc.setDirty(true);
	}
	
	protected void loadNodesChildren() {
		catalogEntry = catalogManager.loadCatalogEntry(catalogEntry);
		List<CatalogEntry> catalogChildren = catalogManager.getChildrenOf(catalogEntry);
		List<String> subCategories = new ArrayList<>();
		List<NodeEntryRow> nodeEntries = new ArrayList<>();
		int count = 0;
		boolean tiles = catalogEntry.getStyle() == Style.tiles;

		if (catalogManager.isCategorySortingManually(catalogEntry)) {
			Comparator<CatalogEntry> comparator = Comparator.comparingInt(CatalogEntry::getPosition);
			catalogChildren.sort(comparator);
		} else {
			Collator collator = Collator.getInstance(getLocale());
			collator.setStrength(Collator.IDENTICAL);

			if (catalogEntry.getStyle().equals(Style.tiles)) {
				catalogChildren.sort(Comparator.comparing(entry -> entry.getShortTitle() != null ? entry.getShortTitle() : entry.getName(), collator));
			} else {
				catalogChildren.sort(Comparator.comparing(CatalogEntry::getName, collator));
			}
		}
		
		for (CatalogEntry entry : catalogChildren) {
			if(entry != null && entry.getType() == CatalogEntry.TYPE_NODE) {
				NodeEntryRow row = new NodeEntryRow(entry);
				
				FormLink positionLink = uifactory.addFormLink("position_" + counter.incrementAndGet(), "positionNodes", String.valueOf(row.getPosition() + 1), null, null, Link.NONTRANSLATED);
				positionLink.setUserObject(row);
				row.setPositionLink(positionLink);
				
				nodeEntries.add(row);

				String cmpId = "cat_" + (++count);

				VFSLeaf img = catalogManager.getImage(entry);
				if(img != null) {
					String imgId = "image_" + count;
					flc.contextPut(imgId, img.getName());
				}
				flc.contextPut("k" + cmpId, entry.getKey());

				String title = StringHelper.escapeHtml(tiles ? entry.getShortTitle() : entry.getName());
				Link link = LinkFactory.createCustomLink(cmpId, "select_node", cmpId, Link.LINK + Link.NONTRANSLATED, flc.getFormItemComponent(), this);
				link.setIconLeftCSS("o_icon o_icon_catalog_sub");
				link.setCustomDisplayText(title);
				link.setUserObject(entry.getKey());
				subCategories.add(Integer.toString(count));
				String titleId = "title_" + count;
				flc.contextPut(titleId, title);
			}
		}
		flc.contextPut("subCategories", subCategories);

		if (catalogManager.isCategorySortingManually(catalogEntry)) {
			Comparator<NodeEntryRow> comparator = Comparator.comparingInt(NodeEntryRow::getPosition);
			nodeEntries.sort(comparator);
		} else {
			Collator collator = Collator.getInstance(getLocale());
			collator.setStrength(Collator.IDENTICAL);

			nodeEntries.sort(Comparator.comparing(NodeEntryRow::getDisplayname, collator));
		}

		nodeEntriesModel.setObjects(nodeEntries);
		nodeEntriesEl.reset(true, true, true);
		nodeEntriesEl.setVisible(nodeEntriesModel.getRowCount() > 0);
		
		flc.setDirty(true);
	}
	
	protected void initToolbar() {
		boolean canAddLinks = isAdministrator || isAuthor; // author is allowed to add!
		boolean canAdministrateCategory = isAdministrator || isLocalTreeAdmin;
		boolean canAddSubCategories = isAdministrator || isLocalTreeAdmin;
	
		if (canAdministrateCategory || canAddLinks) {
			if (canAdministrateCategory) {
				if (orderManuallyLink == null) {
					orderManuallyLink = LinkFactory.createToolLink("order", translate("tools.order.catalog"), this, "o_icon_order");
					orderManuallyLink.setElementCssClass("o_sel_catalog_order_category");
					toolbarPanel.addTool(orderManuallyLink, Align.right);
				} else {
					orderManuallyLink.setVisible(true);
				}
			}
			if (canAdministrateCategory) {
				if (editLink == null) {
					editLink = LinkFactory.createToolLink("edit", translate("tools.edit.catalog.category"), this, "o_icon_edit");
					editLink.setElementCssClass("o_sel_catalog_edit_category");
					toolbarPanel.addTool(editLink, Align.left);
				} else {
					editLink.setVisible(true);
				}
			}
			if (canAdministrateCategory) {
				if (nominateLink == null) {
					nominateLink = LinkFactory.createToolLink("nominate", translate("tools.edit.catalog.category.ownergroup"), this, "o_icon_user");
					nominateLink.setElementCssClass("o_sel_catalog_category_owner");
					toolbarPanel.addTool(nominateLink, Align.right); 
				} else {
					nominateLink.setVisible(true);
				}
			}
			if (canAddLinks) {
				if (contactLink == null) {
					contactLink = LinkFactory.createToolLink("contact", translate("tools.new.catalog.categoryrequest"), this, "o_icon_mail");
					contactLink.setElementCssClass("o_sel_catalog_contact_owner");
					toolbarPanel.addTool(contactLink, Align.right);
				} else {
					nominateLink.setVisible(true);
				}
			}
			if (canAdministrateCategory && catalogEntry.getParent() != null) {
				// delete root? very dangerous, disabled!
				if (deleteLink == null) {
					deleteLink = LinkFactory.createToolLink("delete", translate("tools.delete.catalog.entry"), this, "o_icon_delete");
					deleteLink.setElementCssClass("o_sel_catalog_delete_category");
					toolbarPanel.addTool(deleteLink, Align.left);
				} else {
					deleteLink.setVisible(true);
				}
			}
			if (canAdministrateCategory && catalogEntry.getParent() != null) {
				if (moveLink == null) {
					moveLink = LinkFactory.createToolLink("move", translate("tools.move.catalog.entry"), this, "o_icon_move");
					moveLink.setElementCssClass("o_sel_catalog_move_category");
					toolbarPanel.addTool(moveLink, Align.left);
				} else {
					moveLink.setVisible(true);
				}
			}
		}

		if(isAdministrator || isLocalTreeAdmin || isAuthor) {
			if (canAddSubCategories) {
				if (addCategoryLink == null) {
					addCategoryLink = LinkFactory.createToolLink("addResource", translate("tools.add.catalog.category"), this, "o_icon_catalog_sub");
					addCategoryLink.setElementCssClass("o_sel_catalog_add_category");
					toolbarPanel.addTool(addCategoryLink, Align.left);
				} else {
					addCategoryLink.setVisible(true);
				}
				
			}
			if (canAddLinks) {
				if (addResourceLink == null) {
					addResourceLink = LinkFactory.createToolLink("addResource", translate("tools.add.catalog.link"), this, "o_icon_add");
					addResourceLink.setElementCssClass("o_sel_catalog_add_link_to_resource");
					toolbarPanel.addTool(addResourceLink, Align.left);
				} else {
					addResourceLink.setVisible(true);
				}
			}
		}	
	}
	
	@Override
	protected void doDispose() {
		this.toolbarPanel.removeListener(this);	
		releaseLock();
        super.doDispose();
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			return;
		}
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if("CatalogEntry".equalsIgnoreCase(type)) {
			Long entryKey = entry.getOLATResourceable().getResourceableId();
			if(entryKey != null && entryKey.longValue() > 0) {
				activateRoot(ureq, entryKey); 
			}
		} else if("Node".equalsIgnoreCase(type)) {
			//the "Node" is only for internal usage
			StateEntry stateEntry = entry.getTransientState();
			if(stateEntry instanceof CatalogStateEntry) {
				CatalogEntry catEntry = ((CatalogStateEntry)stateEntry).getEntry();
				CatalogNodeManagerController nextCtrl = selectCatalogEntry(ureq, catEntry);
				if(nextCtrl != null && entries.size() > 1) {
					nextCtrl.activate(ureq, entries.subList(1, entries.size()), null);
				}
			}
		}
	}
	
	/**
	 * Build an internal business path made of "Node" with the category
	 * as state entry to prevent loading several times the same entries.
	 * 
	 * @param ureq
	 * @param entryKey
	 */
	private void activateRoot(UserRequest ureq, Long entryKey) {
		List<ContextEntry> parentLine = new ArrayList<>();
		for(CatalogEntry node = catalogManager.getCatalogEntryByKey(entryKey); node.getParent() != null; node=node.getParent()) {
			OLATResourceable nodeRes = OresHelper.createOLATResourceableInstance("Node", node.getKey());
			ContextEntry ctxEntry = BusinessControlFactory.getInstance().createContextEntry(nodeRes);
			ctxEntry.setTransientState(new CatalogStateEntry(node));
			parentLine.add(ctxEntry);
		}
		Collections.reverse(parentLine);
		activate(ureq, parentLine, null);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(entriesEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				CatalogEntryRow row = entriesModel.getObject(se.getIndex());
				if(cmd != null && cmd.startsWith("opened-")) {
					if("opened-details".equals(cmd) || "opened-select".equals(cmd)) {
						launchDetails(ureq, row);	
					} else if("opened-move".equals(cmd)) {
						doMoveCategory(ureq, row);
					} else if("opened-delete".equals(cmd)) {
						doConfirmDelete(ureq, row);
					}
				} else if (cmd.equals(CMD_DOWN)) {
					doMoveCatalogEntry(row.getCatEntryKey(), cmd, ureq);
				} else if (cmd.equals(CMD_UP)) {
					doMoveCatalogEntry(row.getCatEntryKey(), cmd, ureq);
				} 
			}
		} else if(closedEntriesEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				CatalogEntryRow row = closedEntriesModel.getObject(se.getIndex());
				if(cmd != null && cmd.startsWith("closed-")) {
					if("closed-details".equals(cmd) || "closed-select".equals(cmd)) {
						launchDetails(ureq, row);	
					} else if("closed-move".equals(cmd)) {
						doMoveCategory(ureq, row);
					} else if("closed-delete".equals(cmd)) {
						doConfirmDelete(ureq, row);
					} 
				} else if (cmd.equals(CMD_DOWN)) {
					doMoveCatalogEntry(row.getCatEntryKey(), cmd, ureq);
				} else if (cmd.equals(CMD_UP)) {
					doMoveCatalogEntry(row.getCatEntryKey(), cmd, ureq);
				} 
			}
		} else if (nodeEntriesEl == source) {		
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				NodeEntryRow row = nodeEntriesModel.getObject(se.getIndex());
				if (CMD_DOWN.equals(cmd) || CMD_UP.equals(cmd)) {
					doMoveCatalogEntry(row.getKey(), cmd, ureq);
				} 
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink) source;

			if("positionNodes".equals(link.getCmd())) {
				int size = nodeEntriesModel.getObjects().size();
				int smallest = nodeEntriesModel.getObject(0).getPosition();
				int biggest = nodeEntriesModel.getObject(size - 1).getPosition();
				
				doOpenPositionDialog(ureq, link, smallest, biggest);
			} else if("positionEntries".equals(link.getCmd())) {
				int size = entriesModel.getObjects().size();
				int smallest = entriesModel.getObject(0).getPosition();
				int biggest = entriesModel.getObject(size - 1).getPosition();
				
				doOpenPositionDialog(ureq, link, smallest, biggest);
			} else if("positionClosedEntries".equals(link.getCmd())) {
				int size = closedEntriesModel.getObjects().size();
				int smallest = closedEntriesModel.getObject(0).getPosition();
				int biggest = closedEntriesModel.getObject(size - 1).getPosition();
				
				doOpenPositionDialog(ureq, link, smallest, biggest);
			}
		}

		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(editLink == source){
			doEditCategory(ureq);
		} else if(nominateLink == source) {
			doEditOwners(ureq);
		} else if(contactLink == source) {
			doContact(ureq);
		} else if(deleteLink == source) {
			doConfirmDelete(ureq);
		} else if(moveLink == source) {
			doMoveCategory(ureq);
		} else if (orderManuallyLink == source) {
			doActivateOrdering(ureq);
		} else if(addCategoryLink == source) {
			doAddCategory(ureq);
		} else if(addResourceLink == source) {
			doAddResource(ureq);
		} else if(source instanceof Link) {
			Link link = (Link)source;
			if("select_node".equals(link.getCommand())) {
				Long categoryNodeKey = (Long)link.getUserObject();
				CatalogEntry entry = catalogManager.getCatalogNodeByKey(categoryNodeKey);
				selectCatalogEntry(ureq, entry);
			}
		} else if("img_select".equals(event.getCommand())) {
			String node = ureq.getParameter("node");
			if(StringHelper.isLong(node)) {
				try {
					Long categoryNodeKey = Long.valueOf(node);
					CatalogEntry entry = catalogManager.getCatalogNodeByKey(categoryNodeKey);
					selectCatalogEntry(ureq, entry);
				} catch (NumberFormatException e) {
					logWarn("Not a valid long: " + node, e);
				}
			}
		} else if (toolbarPanel == source) {
			if (event instanceof PopEvent) {
				loadNodesChildren();
				loadResources(ureq);
				loadEntryInfos();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addEntryCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadEntryInfos();
				loadResources(ureq);
				loadNodesChildren();
			}
			cmc.deactivate();
			cleanUp();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(editEntryCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				catalogEntry = editEntryCtrl.getEditedCatalogEntry();
				loadEntryInfos();
				loadResources(ureq);
				loadNodesChildren();
			}
			cmc.deactivate();
			cleanUp();
			toolbarPanel.changeDisplayname(catalogEntry.getShortTitle());
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(categoryMoveCtrl == source) {
			cmc.deactivate();
			CatalogEntry moveMe = null;
			if(event.equals(Event.DONE_EVENT)){
				showInfo("tools.move.catalog.entry.success", catalogEntry.getName());
				moveMe = categoryMoveCtrl.getMoveMe();
			} else if(event.equals(Event.FAILED_EVENT)){
				showError("tools.move.catalog.entry.failed");
			}
			loadEntryInfos();
			loadResources(ureq);
			loadNodesChildren();
			cleanUp();
			
			// in any case, remove the lock
			if (catModificationLock != null && catModificationLock.isSuccess()) {
				CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(catModificationLock);
				catModificationLock = null;
			}
			//only after jump to the moved entry
			if(moveMe != null) {
				launchCatalogEntry(ureq, moveMe);
			}
		} else if(dialogDeleteSubtree == source) {
			//from remove subtree dialog -> yes or no
			if (DialogBoxUIFactory.isYesEvent(event)) {
				catalogManager.deleteCatalogEntry(catalogEntry);
				fireEvent(ureq, Event.BACK_EVENT);
			}
			// in any case, remove the lock
			if (catModificationLock != null && catModificationLock.isSuccess()) {
				CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(catModificationLock);
				catModificationLock = null;
			}
		} else if(childNodeCtrl == source) {
			if(event == Event.BACK_EVENT) {
				toolbarPanel.popController(childNodeCtrl);
				toolbarPanel.popUpToController(this);
				removeAsListenerAndDispose(childNodeCtrl);
				childNodeCtrl = null;

				loadEntryInfos();
				loadResources(ureq);
				loadNodesChildren();
			}
		} else if(entrySearchCtrl == source) {
			if (event.getCommand().equals(RepositoryTableModel.TABLE_ACTION_SELECT_LINK)) {
				// successfully selected a repository entry which will be a link within
				// the current Category
				RepositoryEntry selectedEntry = entrySearchCtrl.getSelectedEntry();
				doAddResource(ureq, selectedEntry);
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if(event.getCommand().equals(RepositoryTableModel.TABLE_ACTION_SELECT_ENTRIES)) {
				List<RepositoryEntry> selectedEntries = entrySearchCtrl.getSelectedEntries();
				selectedEntries.forEach(entry -> doAddResource(ureq, entry));
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(groupCtrl == source) {
			if(event instanceof IdentitiesAddEvent || event instanceof IdentitiesRemoveEvent) {
				doAddRemoveOwners(event);
			}
		} else if(contactCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(dialogDeleteLink == source) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				CatalogEntryRow row = (CatalogEntryRow)dialogDeleteLink.getUserObject();
				catalogManager.deleteCatalogEntry(row, catalogEntry);
				loadResources(ureq);
				fireEvent(ureq, Event.BACK_EVENT);
			}
		} else if(entryResourceMoveCtrl == source) {
			CatalogEntry moveMe = entryResourceMoveCtrl.getMoveMe();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				showInfo("tools.move.catalog.entry.success", moveMe.getName());
				loadResources(ureq);
			} else if (event == Event.FAILED_EVENT) {
				showError("tools.move.catalog.entry.success", moveMe.getName());
			}
			cmc.deactivate();
			cleanUp();
		} else if (positionCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadNodesChildren();
				loadResources(ureq);
			}
			positionCalloutCtrl.deactivate();
			cleanUp();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(cmc == source) {
			loadNodesChildren();
			loadResources(ureq);
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		releaseLock();
		
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(groupCtrl);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(addEntryCtrl);
		removeAsListenerAndDispose(editEntryCtrl);
		removeAsListenerAndDispose(entrySearchCtrl);
		removeAsListenerAndDispose(positionCtrl);
		removeAsListenerAndDispose(positionCalloutCtrl);
		removeAsListenerAndDispose(positionMoveCtrl);
		
		cmc = null;
		groupCtrl = null;
		contactCtrl = null;
		addEntryCtrl = null;
		editEntryCtrl = null;
		entrySearchCtrl = null;
		positionCtrl = null;
		positionCalloutCtrl = null;
		positionMoveCtrl = null;
	}
	
	private void releaseLock() {
		//remove the lock, always
		if (catModificationLock != null && catModificationLock.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(catModificationLock);
			catModificationLock = null;
		}
	}

	private CatalogNodeManagerController selectCatalogEntry(UserRequest ureq, CatalogEntry entry) {
		if(entry != null && entry.getType() == CatalogEntry.TYPE_NODE) {
			removeAsListenerAndDispose(childNodeCtrl);
			
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("CatalogEntry", entry.getKey());
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, rootwControl);
			
			childNodeCtrl = new CatalogNodeManagerController(ureq, bwControl, rootwControl, entry, toolbarPanel, isLocalTreeAdmin);
			listenTo(childNodeCtrl);
			toolbarPanel.pushController(entry.getShortTitle(), childNodeCtrl);
			childNodeCtrl.initToolbar();
			
			addToHistory(ureq, childNodeCtrl);
		}
		return childNodeCtrl;
	}
	
	private void doActivateOrdering(UserRequest ureq) {
		catModificationLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, getIdentity(), LOCK_TOKEN, getWindow());
		if (catModificationLock.isSuccess() && !isOrdering) {	
			boolean activateOrdering = false;
			activateOrdering |= nodeEntriesModel.getObjects().size() > 1;
			activateOrdering |= entriesModel.getObjects().size() > 1;
			activateOrdering |= closedEntriesModel.getObjects().size() > 1;
			
			if (activateOrdering) {
				positionMoveCtrl = new CatalogNodeManagerController(ureq, getWindowControl(), rootwControl, catalogEntry, toolbarPanel, isLocalTreeAdmin, true);
				listenTo(positionMoveCtrl);
				
				cmc = new CloseableModalController(getWindowControl(), "close", positionMoveCtrl.getInitialComponent(), true, translate("tools.order.catalog"));
				listenTo(cmc);
				cmc.activate();	
			} else {
				showWarning("catalog.position.deactivated");
			}
		} else {
			showLockError();
		}
	}
	
	private void showLockError() {
		String ownerName = userManager.getUserDisplayName(catModificationLock.getOwner());
		if(catModificationLock.isDifferentWindows()) {
			showError("catalog.locked.by.same.user", ownerName);
		} else {
			showError("catalog.locked.by", ownerName);
		}
	}
	
	private void doMoveCatalogEntry(Long key, String command, UserRequest ureq) {
		if(catalogEntry == null
				|| catalogManager.reorderCatalogEntry(catalogEntry.getKey(), key, CMD_UP.equals(command)) != 0) {
			getWindowControl().setWarning("Catalog has been modified, please try again!");
		}
		
		loadNodesChildren();
		loadResources(ureq);
	}
	
	private void doOpenPositionDialog(UserRequest ureq, FormLink link, int smallest, int biggest) {
		removeAsListenerAndDispose(positionCalloutCtrl);
		removeAsListenerAndDispose(positionCtrl);
		
		catModificationLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, getIdentity(), LOCK_TOKEN, getWindow());
		if (catModificationLock.isSuccess()) {
			Object rowObject = link.getUserObject();
			
			
			if (rowObject instanceof NodeEntryRow) {
				NodeEntryRow row = (NodeEntryRow) rowObject;
				positionCtrl = new CatalogEntryPositionDialogController(ureq, getWindowControl(), row.getKey(), smallest, biggest);
			} else if (rowObject instanceof CatalogEntryRow) {
				CatalogEntryRow row = (CatalogEntryRow) rowObject;
				positionCtrl = new CatalogEntryPositionDialogController(ureq, getWindowControl(), row.getCatEntryKey(), smallest, biggest);
			} else {
				return;
			}
			
			listenTo(positionCtrl);
			
			CalloutSettings settings = new CalloutSettings(true);
			positionCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),positionCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "", settings);
			listenTo(positionCalloutCtrl);
			positionCalloutCtrl.activate();
			
		} else {
			showLockError();
		}
	}
	
	private void doAddResource(UserRequest ureq) {
		removeAsListenerAndDispose(entrySearchCtrl);
		removeAsListenerAndDispose(cmc);

		catModificationLock = CoordinatorManager.getInstance().getCoordinator()
				.getLocker().acquireLock(lockRes, getIdentity(), LOCK_TOKEN, getWindow());
		if (catModificationLock.isSuccess()) {
			entrySearchCtrl = new RepositorySearchController(translate("choose"), ureq, getWindowControl(),
					true, repositoryModule.isCatalogMultiSelectEnabled(), new String[0], false, null, null);
			listenTo(entrySearchCtrl);
			// OLAT-Admin has search form
			if (isAdministrator) {
				entrySearchCtrl.displaySearchForm();
			}
			// an Author gets the list of his repository
			else {
				// admin is responsible for not inserting wrong visibility entries!!
				entrySearchCtrl.doSearchByOwnerLimitAccess(ureq.getIdentity());
			}
			// open form in dialog
			cmc = new CloseableModalController(getWindowControl(), "close", entrySearchCtrl.getInitialComponent(), true, translate("tools.add.catalog.link"));
			listenTo(cmc);
			cmc.activate();	
		} else {
			showLockError();
		}
	}
	
	private void doAddResource(UserRequest ureq, RepositoryEntry selectedEntry) {
		CatalogEntry newLinkNotPersistedYet = catalogManager.createCatalogEntry();
		newLinkNotPersistedYet.setName(selectedEntry.getDisplayname());
		newLinkNotPersistedYet.setDescription(selectedEntry.getDescription());
		newLinkNotPersistedYet.setRepositoryEntry(selectedEntry);
		newLinkNotPersistedYet.setType(CatalogEntry.TYPE_LEAF);
		catalogManager.addCatalogEntry(catalogEntry, newLinkNotPersistedYet);
		loadResources(ureq);
	}
	
	private void doAddCategory(UserRequest ureq) {
		removeAsListenerAndDispose(addEntryCtrl);
		removeAsListenerAndDispose(cmc);
		
		catModificationLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, getIdentity(), LOCK_TOKEN, getWindow());
		if (catModificationLock.isSuccess()) {
			CatalogEntry ce = catalogManager.createCatalogEntry();
			addEntryCtrl = new CatalogEntryEditController(ureq, getWindowControl(), ce, catalogEntry);
			addEntryCtrl.setElementCssClass("o_sel_catalog_add_category_popup");
			listenTo(addEntryCtrl);

			cmc = new CloseableModalController(getWindowControl(), "close", addEntryCtrl.getInitialComponent(), true, translate("tools.add.catalog.category"));
			listenTo(cmc);
			cmc.activate();	
		} else {
			showLockError();
		}
	}
	
	private void doEditCategory(UserRequest ureq) {
		removeAsListenerAndDispose(editEntryCtrl);
		removeAsListenerAndDispose(cmc);
		
		catModificationLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, getIdentity(), LOCK_TOKEN, getWindow());
		if ( catModificationLock.isSuccess()) {
			editEntryCtrl = new CatalogEntryEditController(ureq, getWindowControl(), catalogEntry);
			editEntryCtrl.setElementCssClass("o_sel_catalog_edit_category_popup");
			listenTo(editEntryCtrl);
			
			// open form in dialog
			cmc = new CloseableModalController(getWindowControl(), "close", editEntryCtrl.getInitialComponent(), true, translate("tools.edit.catalog.category"));
			listenTo(cmc);
			
			cmc.activate();	
		} else {
			showLockError();
		}
	}
	
	private void doMoveCategory(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(categoryMoveCtrl);
		
		catModificationLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, getIdentity(), LOCK_TOKEN, getWindow());
		if (catModificationLock.isSuccess()) {
			categoryMoveCtrl= new CatalogEntryMoveController(getWindowControl(), ureq, catalogEntry, getTranslator());					
			listenTo(categoryMoveCtrl);
			cmc = new CloseableModalController(getWindowControl(), "close", categoryMoveCtrl.getInitialComponent(),
					true, translate("tools.move.catalog.entry"));
			listenTo(cmc);
			cmc.activate();
		} else {
			showLockError();
		}
	}
	
	private void doMoveCategory(UserRequest ureq, CatalogEntryRow row) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(entryResourceMoveCtrl);
		CatalogEntry moveMe = catalogManager.getCatalogEntryBy(row, catalogEntry);
		if(moveMe != null) {
			entryResourceMoveCtrl= new CatalogEntryMoveController(getWindowControl(), ureq, moveMe, getTranslator());
			listenTo(entryResourceMoveCtrl);
			cmc = new CloseableModalController(getWindowControl(), "close", entryResourceMoveCtrl.getInitialComponent(),
					true, translate("tools.move.catalog.entry"));
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		catModificationLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, getIdentity(), LOCK_TOKEN, getWindow());
		if ( catModificationLock.isSuccess()) {
			String[] trnslP = { catalogEntry.getName() };
			dialogDeleteSubtree = activateYesNoDialog(ureq, null, translate("dialog.modal.subtree.delete.text", trnslP), dialogDeleteSubtree);
		} else {
			showLockError();
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, CatalogEntryRow row) {
		String[] trnslP = { row.getDisplayname() };
		dialogDeleteLink = activateYesNoDialog(ureq, null, translate("dialog.modal.leaf.delete.text", trnslP), dialogDeleteLink);
		dialogDeleteLink.setUserObject(row);
	}
	
	private void doEditOwners(UserRequest ureq) {
		removeAsListenerAndDispose(groupCtrl);
		removeAsListenerAndDispose(cmc);
		
		// add ownership management
		SecurityGroup secGroup = catalogEntry.getOwnerGroup();
		groupCtrl = new GroupController(ureq, getWindowControl(), true, false, false, false, false, false, secGroup);
		listenTo(groupCtrl);
		
		// open form in dialog
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), "close", groupCtrl.getInitialComponent(), true, translate("tools.edit.catalog.category.ownergroup"));
		listenTo(cmc);
		cmc.activate();	
	}
	
	private void doAddRemoveOwners(Event event) {
		if(event instanceof IdentitiesAddEvent ) {
			IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent) event;
			List<Identity> list = identitiesAddedEvent.getAddIdentities();
	        for (Identity identity : list) {
				if(catalogManager.addOwner(catalogEntry, identity)) {
					identitiesAddedEvent.getAddedIdentities().add(identity);
				}
	        }
		} else if (event instanceof IdentitiesRemoveEvent) {
			IdentitiesRemoveEvent identitiesRemoveEvent = (IdentitiesRemoveEvent) event;
			List<Identity> list = identitiesRemoveEvent.getRemovedIdentities();
			for (Identity identity : list) {
				catalogManager.removeOwner(catalogEntry, identity);
			}		
		}
	}
	
	private void doContact(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(contactCtrl);

		ContactList caretaker = new ContactList(translate("contact.to.groupname.caretaker"));
		List<Identity> owners = new ArrayList<>();
		
		CatalogEntry parent = catalogEntry;
		while(parent != null && owners.isEmpty()) {
			if (parent.getOwnerGroup() != null) {
				owners = catalogManager.getOwners(parent);
			}
			parent = parent.getParent();			
		}
		
		for (int i=owners.size(); i-->0; ) {
			caretaker.add(owners.get(i));
		}
		
		//create e-mail Message
		ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
		cmsg.addEmailTo(caretaker);
		contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
		listenTo(contactCtrl);
		
		// open form in dialog
		cmc = new CloseableModalController(getWindowControl(), "close", contactCtrl.getInitialComponent(), true, translate("contact.caretaker"));
		listenTo(cmc);
		cmc.activate();	
	}
	
	private void launchDetails(UserRequest ureq, RepositoryEntryRef ref) {
		String businessPath = "[RepositoryEntry:" + ref.getKey() + "][Infos:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void launchCatalogEntry(UserRequest ureq, CatalogEntry ref) {
		String businessPath = "[CatalogAdmin:0][CatalogEntry:" + ref.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
}

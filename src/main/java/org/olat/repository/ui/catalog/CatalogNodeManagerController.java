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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
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
	
	private GroupController groupCtrl;
	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private RepositorySearchController entrySearchCtrl;

	private CatalogNodeManagerController childNodeCtrl;
	private CatalogEntryMoveController categoryMoveCtrl;
	private CatalogEntryMoveController entryResourceMoveCtrl;
	private CatalogEntryEditController addEntryCtrl, editEntryCtrl;
	private DialogBoxController dialogDeleteLink, dialogDeleteSubtree;
	
	private FlexiTableElement entriesEl, closedEntriesEl;
	private CatalogEntryRowModel entriesModel, closedEntriesModel;
	
	private Link editLink, moveLink, deleteLink;
	private Link nominateLink, contactLink;
	private Link addCategoryLink, addResourceLink;

	private LockResult catModificationLock;
	private final MapperKey mapperThumbnailKey;
	private final WindowControl rootwControl;

	private final boolean isGuest;
	private final boolean isAuthor;
	private final boolean isAdministrator;
	private final boolean isLocalTreeAdmin;
	
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
		
		FlexiTableColumnModel entriesColumnsModel = getCatalogFlexiTableColumnModel("opened-");
		entriesModel = new CatalogEntryRowModel(entriesColumnsModel);
		entriesEl = uifactory.addTableElement(getWindowControl(), "entries", entriesModel, getTranslator(), formLayout);
		
		FlexiTableColumnModel closedEntriesColumnsModel = getCatalogFlexiTableColumnModel("closed-");
		closedEntriesModel = new CatalogEntryRowModel(closedEntriesColumnsModel);
		closedEntriesEl = uifactory.addTableElement(getWindowControl(), "closedEntries", closedEntriesModel, getTranslator(), formLayout);
	}
	
	private FlexiTableColumnModel getCatalogFlexiTableColumnModel(String cmdPrefix) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18nKey(), Cols.key.ordinal(), true, OrderBy.key.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.type.i18nKey(), Cols.type.ordinal(), true, OrderBy.type.name(),
				FlexiColumnModel.ALIGNMENT_LEFT, new TypeRenderer()));
		FlexiCellRenderer renderer = new StaticFlexiCellRenderer(cmdPrefix + "select", new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.displayName.i18nKey(), Cols.displayName.ordinal(), cmdPrefix + "select",
				true, OrderBy.displayname.name(), renderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.authors.i18nKey(), Cols.authors.ordinal(),
				true, OrderBy.authors.name()));
		if(repositoryModule.isManagedRepositoryEntries()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.externalId.i18nKey(), Cols.externalId.ordinal(),
					true, OrderBy.externalId.name()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.externalRef.i18nKey(), Cols.externalRef.ordinal(),
				true, OrderBy.externalRef.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lifecycleLabel.i18nKey(), Cols.lifecycleLabel.ordinal(),
				true, OrderBy.lifecycleLabel.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lifecycleSoftkey.i18nKey(), Cols.lifecycleSoftkey.ordinal(),
				true, OrderBy.lifecycleSoftkey.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.lifecycleStart.i18nKey(), Cols.lifecycleStart.ordinal(),
				true, OrderBy.lifecycleStart.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.lifecycleEnd.i18nKey(), Cols.lifecycleEnd.ordinal(),
				true, OrderBy.lifecycleEnd.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.access.i18nKey(), Cols.access.ordinal(),
				true, OrderBy.access.name(), FlexiColumnModel.ALIGNMENT_LEFT, new AccessRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.ac.i18nKey(), Cols.ac.ordinal(),
				true, OrderBy.ac.name(), FlexiColumnModel.ALIGNMENT_LEFT, new ACRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.creationDate.i18nKey(), Cols.creationDate.ordinal(),
				true, OrderBy.creationDate.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.delete.i18nKey(), translate(Cols.delete.i18nKey()), cmdPrefix + "delete"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.move.i18nKey(), translate(Cols.move.i18nKey()), cmdPrefix + "move"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.detailsSupported.i18nKey(), Cols.detailsSupported.ordinal(), cmdPrefix + "details",
				new StaticFlexiCellRenderer("", cmdPrefix + "details", "o_icon o_icon-lg o_icon_details", translate("details"))));
		return columnsModel;
	}

	private void loadEntryInfos() {
		flc.contextPut("catalogEntryName", catalogEntry.getName());
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
			List<PriceMethod> types = new ArrayList<PriceMethod>();
			if (entry.isMembersOnly()) {
				// members only always show lock icon
				types.add(new PriceMethod("", "o_ac_membersonly_icon", translate("cif.access.membersonly.short")));
			} else {
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
			}
			
			if(!types.isEmpty()) {
				row.setAccessTypes(types);
			}
			
			if(entry.getRepositoryEntryStatus().isClosed()) {
				closedItems.add(row);
			} else {
				items.add(row);
			}
		}
		
		entriesModel.setObjects(items);
		entriesEl.reset();
		entriesEl.setVisible(entriesModel.getRowCount() > 0);
		
		closedEntriesModel.setObjects(closedItems);
		closedEntriesEl.reset();
		closedEntriesEl.setVisible(closedEntriesModel.getRowCount() > 0);
	}
	
	protected void loadNodesChildren() {
		List<CatalogEntry> childCe = catalogManager.getNodesChildrenOf(catalogEntry);
		Collections.sort(childCe, new CatalogEntryComparator(getLocale()));
		List<String> subCategories = new ArrayList<>();
		int count = 0;
		for (CatalogEntry entry : childCe) {
			if(entry.getType() == CatalogEntry.TYPE_NODE) {
				String cmpId = "cat_" + (++count);
				
				VFSLeaf img = catalogManager.getImage(entry);
				if(img != null) {
					String imgId = "image_" + count;
					flc.contextPut(imgId, img.getName());
				}
				flc.contextPut("k" + cmpId, entry.getKey());
				
				String title = StringHelper.escapeHtml(entry.getName());
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
	}
	
	protected void initToolbar() {
		boolean canAddLinks = isAdministrator || isAuthor; // author is allowed to add!
		boolean canAdministrateCategory = isAdministrator || isLocalTreeAdmin;
		boolean canAddSubCategories = isAdministrator || isLocalTreeAdmin;
	
		if (canAdministrateCategory || canAddLinks) {
			if (canAdministrateCategory) {
				editLink = LinkFactory.createToolLink("edit", translate("tools.edit.catalog.category"), this, "o_icon_edit");
				editLink.setElementCssClass("o_sel_catalog_edit_category");
				toolbarPanel.addTool(editLink, Align.left);
			}
			if (canAdministrateCategory) {
				nominateLink = LinkFactory.createToolLink("nominate", translate("tools.edit.catalog.category.ownergroup"), this, "o_icon_user");
				nominateLink.setElementCssClass("o_sel_catalog_category_owner");
				toolbarPanel.addTool(nominateLink, Align.right); 
			}
			if (canAddLinks) {
				contactLink = LinkFactory.createToolLink("contact", translate("tools.new.catalog.categoryrequest"), this, "o_icon_mail");
				contactLink.setElementCssClass("o_sel_catalog_contact_owner");
				toolbarPanel.addTool(contactLink, Align.right);
			}
			if (canAdministrateCategory && catalogEntry.getParent() != null) {
				// delete root? very dangerous, disabled!
				deleteLink = LinkFactory.createToolLink("delete", translate("tools.delete.catalog.entry"), this, "o_icon_delete");
				deleteLink.setElementCssClass("o_sel_catalog_delete_category");
				toolbarPanel.addTool(deleteLink, Align.left);
			}
			if (canAdministrateCategory && catalogEntry.getParent() != null) {
				moveLink = LinkFactory.createToolLink("move", translate("tools.move.catalog.entry"), this, "o_icon_move");
				moveLink.setElementCssClass("o_sel_catalog_move_category");
				toolbarPanel.addTool(moveLink, Align.left);
			}
		}

		if(isAdministrator || isLocalTreeAdmin || isAuthor) {
			if (canAddSubCategories) {
				addCategoryLink = LinkFactory.createToolLink("addResource", translate("tools.add.catalog.category"), this, "o_icon_catalog_sub");
				addCategoryLink.setElementCssClass("o_sel_catalog_add_category");
				toolbarPanel.addTool(addCategoryLink, Align.left);
			}
			if (canAddLinks) {
				addResourceLink = LinkFactory.createToolLink("addResource", translate("tools.add.catalog.link"), this, "o_icon_add");
				addResourceLink.setElementCssClass("o_sel_catalog_add_link_to_resource");
				toolbarPanel.addTool(addResourceLink, Align.left);
			}
		}	
	}
	
	@Override
	protected void doDispose() {
		//
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
				if(cmd != null && cmd.startsWith("opened-")) {
					CatalogEntryRow row = entriesModel.getObject(se.getIndex());
					if("opened-details".equals(cmd) || "opened-select".equals(cmd)) {
						launchDetails(ureq, row);	
					} else if("opened-move".equals(cmd)) {
						doMoveCategory(ureq, row);
					} else if("opened-delete".equals(cmd)) {
						doConfirmDelete(ureq, row);
					}
				}
			}
		} else if(closedEntriesEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if(cmd != null && cmd.startsWith("closed-")) {
					CatalogEntryRow row = closedEntriesModel.getObject(se.getIndex());
					if("closed-details".equals(cmd) || "closed-select".equals(cmd)) {
						launchDetails(ureq, row);	
					} else if("closed-move".equals(cmd)) {
						doMoveCategory(ureq, row);
					} else if("closed-delete".equals(cmd)) {
						doConfirmDelete(ureq, row);
					}
				}
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
					Long categoryNodeKey = new Long(node);
					CatalogEntry entry = catalogManager.getCatalogNodeByKey(categoryNodeKey);
					selectCatalogEntry(ureq, entry);
				} catch (NumberFormatException e) {
					logWarn("Not a valid long: " + node, e);
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addEntryCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadNodesChildren();
			}
			cmc.deactivate();
			cleanUp();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(editEntryCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				catalogEntry = editEntryCtrl.getEditedCatalogEntry();
				loadEntryInfos();
			}
			cmc.deactivate();
			cleanUp();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(categoryMoveCtrl == source) {
			cmc.deactivate();
			CatalogEntry moveMe = null;
			if(event.equals(Event.DONE_EVENT)){
				showInfo("tools.move.catalog.entry.success", catalogEntry.getName());
				moveMe = categoryMoveCtrl.getMoveMe();
			} else if(event.equals(Event.FAILED_EVENT)){
				showError("tools.move.catalog.entry.failed");
				loadNodesChildren();
			}
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
				toolbarPanel.popUpToController(this);
				removeAsListenerAndDispose(childNodeCtrl);
				childNodeCtrl = null;
				
				loadNodesChildren();
			}
		} else if(entrySearchCtrl == source) {
			if (event.getCommand().equals(RepositoryTableModel.TABLE_ACTION_SELECT_LINK)) {
				// successfully selected a repository entry which will be a link within
				// the current Category
				RepositoryEntry selectedEntry = entrySearchCtrl.getSelectedEntry();
				doAddResource(ureq, selectedEntry);
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
			}
		} else if(entryResourceMoveCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				CatalogEntry moveMe = entryResourceMoveCtrl.getMoveMe();
				showInfo("tools.move.catalog.entry.success", moveMe.getName());
				loadResources(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		//remove the lock, always
		if (catModificationLock != null && catModificationLock.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(catModificationLock);
			catModificationLock = null;
		}
		
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(groupCtrl);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(addEntryCtrl);
		removeAsListenerAndDispose(editEntryCtrl);
		removeAsListenerAndDispose(entrySearchCtrl);
		cmc = null;
		groupCtrl = null;
		contactCtrl = null;
		addEntryCtrl = null;
		editEntryCtrl = null;
		entrySearchCtrl = null;
	}

	private CatalogNodeManagerController selectCatalogEntry(UserRequest ureq, CatalogEntry entry) {
		if(entry != null && entry.getType() == CatalogEntry.TYPE_NODE) {
			removeAsListenerAndDispose(childNodeCtrl);
			
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("CatalogEntry", entry.getKey());
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, rootwControl);
			
			childNodeCtrl = new CatalogNodeManagerController(ureq, bwControl, rootwControl, entry, toolbarPanel, isLocalTreeAdmin);
			listenTo(childNodeCtrl);
			toolbarPanel.pushController(entry.getName(), childNodeCtrl);
			childNodeCtrl.initToolbar();
			
			addToHistory(ureq, childNodeCtrl);
		}
		return childNodeCtrl;
	}
	
	private void doAddResource(UserRequest ureq) {
		removeAsListenerAndDispose(entrySearchCtrl);
		removeAsListenerAndDispose(cmc);
		
		entrySearchCtrl = new RepositorySearchController(translate("choose"), ureq, getWindowControl(), true, false, new String[0], null);
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
		
		catModificationLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, getIdentity(), LOCK_TOKEN);
		if (catModificationLock.isSuccess()) {
			CatalogEntry ce = catalogManager.createCatalogEntry();
			addEntryCtrl = new CatalogEntryEditController(ureq, getWindowControl(), ce, catalogEntry);
			addEntryCtrl.setElementCssClass("o_sel_catalog_add_category_popup");
			listenTo(addEntryCtrl);

			cmc = new CloseableModalController(getWindowControl(), "close", addEntryCtrl.getInitialComponent(), true, translate("tools.add.catalog.category"));
			listenTo(cmc);
			cmc.activate();	
		} else {
			String ownerName = userManager.getUserDisplayName(catModificationLock.getOwner());
			showError("catalog.locked.by", ownerName);
		}
	}
	
	private void doEditCategory(UserRequest ureq) {
		removeAsListenerAndDispose(editEntryCtrl);
		removeAsListenerAndDispose(cmc);
		
		catModificationLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, getIdentity(), LOCK_TOKEN);
		if ( catModificationLock.isSuccess()) {
			editEntryCtrl = new CatalogEntryEditController(ureq, getWindowControl(), catalogEntry);
			editEntryCtrl.setElementCssClass("o_sel_catalog_edit_category_popup");
			listenTo(editEntryCtrl);
			
			// open form in dialog
			cmc = new CloseableModalController(getWindowControl(), "close", editEntryCtrl.getInitialComponent(), true, translate("tools.edit.catalog.category"));
			listenTo(cmc);
			
			cmc.activate();	
		} else {
			String ownerName = userManager.getUserDisplayName(catModificationLock.getOwner());
			showError("catalog.locked.by", ownerName);
		}
	}
	
	private void doMoveCategory(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(categoryMoveCtrl);
		
		catModificationLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, getIdentity(), LOCK_TOKEN);
		if (catModificationLock.isSuccess()) {
			categoryMoveCtrl= new CatalogEntryMoveController(getWindowControl(), ureq, catalogEntry, getTranslator());					
			listenTo(categoryMoveCtrl);
			cmc = new CloseableModalController(getWindowControl(), "close", categoryMoveCtrl.getInitialComponent(),
					true, translate("tools.move.catalog.entry"));
			listenTo(cmc);
			cmc.activate();
		} else {
			String ownerName = userManager.getUserDisplayName(catModificationLock.getOwner());
			showError("catalog.locked.by", ownerName);
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
		catModificationLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, getIdentity(), LOCK_TOKEN);
		if ( catModificationLock.isSuccess()) {
			String[] trnslP = { catalogEntry.getName() };
			dialogDeleteSubtree = activateYesNoDialog(ureq, null, translate("dialog.modal.subtree.delete.text", trnslP), dialogDeleteSubtree);
		} else {
			String ownerName = userManager.getUserDisplayName(catModificationLock.getOwner());
			showError("catalog.locked.by", ownerName);
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
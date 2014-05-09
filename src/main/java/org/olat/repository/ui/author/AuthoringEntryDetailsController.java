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
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsController;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseModule;
import org.olat.course.run.RunMainController;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.RepositoyUIFactory;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.RepositoryMembersController;
import org.olat.repository.controllers.WizardCloseResourceController;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.PriceMethod;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.OfferAccess;
import org.olat.resource.accesscontrol.model.Price;
import org.olat.resource.accesscontrol.ui.OrdersAdminController;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEntryDetailsController extends FormBasicController implements Activateable2 {
	
	private FormLink markLink, startLink;
	
	private Link editLink, launchLink,
		copyLink, deleteLink, closeLink,
		downloadLink, downloadCompatLink, bookmarkLink, catalogLink,
		editSettingsLink, membersLink, orderLink;
	
	private CloseableModalController cmc;
	private WizardCloseResourceController wc;
	private OrdersAdminController ordersCtlr;
	private UserCommentsController commentsCtrl;
	private DialogBoxController deleteDialogCtrl;
	private CatalogSettingsController catalogCtlr;
	private CopyRepositoryEntryController copyCtrl;
	private AuthoringEditEntrySettingsController editCtrl;
	private RepositoryMembersController membersEditController;
	
	private final TooledStackedPanel stackPanel;
	
	private boolean corrupted;
	private final RepositoryEntry entry;
	private final AuthoringEntryRow row;

	@Autowired
	private ACService acService;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private CatalogManager catalogManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
	private String baseUrl;
	private final boolean isOwner;
	private final boolean isAuthor;
	private final boolean isOlatAdmin;
	private final boolean isGuestOnly;
	private LockResult lockResult;
	
	public AuthoringEntryDetailsController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, AuthoringEntryRow row) {
		super(ureq, wControl, "details");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));

		this.stackPanel = stackPanel;
		this.row = row;
		entry = repositoryService.loadByKey(row.getKey());
		
		Identity identity = getIdentity();
		Roles roles = ureq.getUserSession().getRoles();
		isOlatAdmin = roles.isOLATAdmin();
		boolean isInstitutionalResourceManager = !roles.isGuestOnly()
					&& RepositoryManager.getInstance().isInstitutionalRessourceManagerFor(identity, roles, entry);
		isOwner = isOlatAdmin || repositoryService.hasRole(ureq.getIdentity(), entry, GroupRoles.owner.name())
					| isInstitutionalResourceManager;
		isAuthor = isOlatAdmin || roles.isAuthor() | isInstitutionalResourceManager;
		isGuestOnly = roles.isGuestOnly();

		initForm(ureq);
		
		if(stackPanel != null) {
			String displayName = row.getDisplayname();
			stackPanel.pushController(displayName, this);
			initToolbar(ureq);
		}
	}
	
	
	private void initToolbar(UserRequest ureq) {
		// init handler details
		RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(entry);
	
		launchLink = LinkFactory.createToolLink("launch", translate("details.launch"), this, "o_sel_repo_launch");
		launchLink.setEnabled(checkIsRepositoryEntryLaunchable(ureq) && !corrupted);

		if (!isGuestOnly) {
			boolean canDownload = entry.getCanDownload() && handler.supportsDownload(entry);
			// disable download for courses if not author or owner
			if (entry.getOlatResource().getResourceableTypeName().equals(CourseModule.getCourseTypeName()) && !(isOwner || isAuthor)) {
				canDownload = false;
			}
			// always enable download for owners
			if (isOwner && handler.supportsDownload(entry)) {
				canDownload = true;
			}

			downloadLink = LinkFactory.createToolLink("download", translate("details.download"), this, "o_sel_repo_download");
			downloadLink.setEnabled(canDownload && !corrupted);
			downloadCompatLink = LinkFactory.createToolLink("downloadcompat", translate("details.download.compatible"), this, "o_sel_repo_download_backward");
			downloadCompatLink.setEnabled(canDownload && !corrupted
					&& "CourseModule".equals(entry.getOlatResource().getResourceableTypeName()));
			
			boolean marked = markManager.isMarked(entry, getIdentity(), null);
			String css = marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE;
			bookmarkLink = LinkFactory.createToolLink("downloadcompat", translate("details.bookmark"), this);
			bookmarkLink.setEnabled(!corrupted);
			bookmarkLink.setIconLeftCSS(css);
		}

		if (isAuthor || isOwner) {
			if (isOwner) {
				editLink = LinkFactory.createToolLink("edit", translate("details.openeditor"), this, "o_sel_repo_edit_descritpion");
				boolean editManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.editcontent);
				editLink.setEnabled(handler.supportsEdit(entry) && !corrupted && !editManaged);

				editSettingsLink = LinkFactory.createToolLink("editdesc", translate("details.chprop"), this, "o_sel_repor_edit_properties");
				editSettingsLink.setEnabled(!corrupted);

				if(repositoryModule.isCatalogEnabled()) {
					catalogLink = LinkFactory.createToolLink("cat", translate("details.categoriesheader"), this, "o_sel_repo_add_to_catalog");
					catalogLink.setEnabled(!corrupted && (entry.getAccess() >= RepositoryEntry.ACC_USERS || entry.isMembersOnly()));
				}

				boolean closeManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.close);
				if ((OresHelper.isOfType(entry.getOlatResource(), CourseModule.class))
						&& !closeManaged
						&& (!RepositoryManager.getInstance().createRepositoryEntryStatus(entry.getStatusCode()).isClosed())) {
					closeLink = LinkFactory.createToolLink("close", translate("details.close.ressoure"), this, "o_sel_repo_close_resource");
					closeLink.setEnabled(!corrupted);
				}
			}
		
			if(isAuthor) {	
				copyLink = LinkFactory.createToolLink("close", translate("details.copy"), this, "o_sel_repo_copy");
				boolean copyManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.copy);
				copyLink.setEnabled((isOwner || entry.getCanCopy()) && !corrupted && !copyManaged);
			}
			
			if (isOwner) {
				deleteLink = LinkFactory.createToolLink("delete", translate("details.delete"), this, "o_sel_repo_delete");
				boolean deleteManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.delete);
				deleteLink.setEnabled(!corrupted && !deleteManaged);
				
				membersLink = LinkFactory.createToolLink("members", translate("details.members"), this, "o_sel_repo_members");
				membersLink.setEnabled(!corrupted);
				
				orderLink = LinkFactory.createToolLink("order", translate("details.orders"), this, "o_sel_repo_booking");
				boolean booking = acService.isResourceAccessControled(entry.getOlatResource(), null);
				orderLink.setEnabled(!corrupted && booking);
			}
		}
		
		stackPanel.addTool(launchLink);
		
		if(downloadCompatLink.isEnabled()) {
			Dropdown downloadDropdown = new Dropdown("downloads", "details.download", false, getTranslator());
			downloadDropdown.addComponent(downloadLink);
			downloadDropdown.addComponent(downloadCompatLink);
			stackPanel.addTool(downloadDropdown);
		} else if(downloadLink.isEnabled()) {
			stackPanel.addTool(downloadLink);
		}
		
		
		stackPanel.addTool(bookmarkLink);
		
		stackPanel.addTool(editLink);
		stackPanel.addTool(editSettingsLink);
		stackPanel.addTool(catalogLink);
		stackPanel.addTool(copyLink);
		stackPanel.addTool(closeLink);
			
		stackPanel.addTool(deleteLink);
			
		stackPanel.addTool(membersLink);
		stackPanel.addTool(orderLink);
		
	}
	
	private boolean checkIsRepositoryEntryLaunchable(UserRequest ureq) {
		RepositoryHandler type = repositoryHandlerFactory.getRepositoryHandler(entry);
		if (repositoryManager.isAllowedToLaunch(ureq, entry) ||
				(type.supportsLaunch(entry) && ureq.getUserSession().getRoles().isOLATAdmin())) {
			return true;
		}
		return false;
	}
	
	private void setText(String text, String key, FormLayoutContainer layoutCont) {
		if(!StringHelper.containsNonWhitespace(text)) return;
		text = StringHelper.xssScan(text);
		if(baseUrl != null) {
			text = FilterFactory.getBaseURLToMediaRelativeURLFilter(baseUrl).filter(text);
		}
		text = Formatter.formatLatexFormulas(text);
		layoutCont.contextPut(key, text);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int cmpcount = 0;
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("v", entry);
			String cssClass = RepositoyUIFactory.getIconCssClass(entry);
			layoutCont.contextPut("cssClass", cssClass);
			
			RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(entry);
			VFSContainer mediaContainer = handler.getMediaContainer(entry);
			if(mediaContainer != null) {
				baseUrl = registerMapper(ureq, new VFSContainerMapper(mediaContainer.getParentContainer()));
			}
			
			setText(entry.getDescription(), "description", layoutCont);
			setText(entry.getRequirements(), "requirements", layoutCont);
			setText(entry.getObjectives(), "objectives", layoutCont);
			setText(entry.getCredits(), "credits", layoutCont);

			//thumbnail and movie
			VFSLeaf movie = repositoryService.getIntroductionMovie(entry);
			VFSLeaf image = repositoryService.getIntroductionImage(entry);
			if(image != null || movie != null) {
				ImageComponent ic = new ImageComponent(ureq.getUserSession(), "thumbnail");
				if(movie != null) {
					ic.setMedia(movie);
					ic.setMaxWithAndHeightToFitWithin(500, 300);
				} else {
					ic.setMedia(image);
					ic.setMaxWithAndHeightToFitWithin(500, 300);
				}
				layoutCont.put("thumbnail", ic);
			}
			
			//categories
			List<CatalogEntry> categories = catalogManager.getCatalogEntriesReferencing(entry);
			List<String> categoriesLink = new ArrayList<>(categories.size());
			for(CatalogEntry category:categories) {
				String id = "cat_" + ++cmpcount;
				String title = category.getParent().getName();
				FormLink catLink = uifactory.addFormLink(id, "category", title, null, layoutCont, Link.LINK | Link.NONTRANSLATED);
				catLink.setUserObject(category.getKey());
				categoriesLink.add(id);
			}
			layoutCont.contextPut("categories", categoriesLink);
			
			boolean marked;
			if(row == null) {
				marked = markManager.isMarked(entry, getIdentity(), null);
			} else {
				marked = row.isMarked();
			}
			markLink = uifactory.addFormLink("mark", "mark", "&nbsp;&nbsp;&nbsp;&nbsp;", null, layoutCont, Link.NONTRANSLATED);
			markLink.setCustomEnabledLinkCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			

			
			//load memberships
			boolean isMember = repositoryService.isMember(getIdentity(), entry);
			
			//access control
			List<PriceMethod> types = new ArrayList<PriceMethod>();
			if (entry.isMembersOnly()) {
				// members only always show lock icon
				types.add(new PriceMethod("", "b_access_membersonly_icon"));
				if(isMember) {
					startLink = uifactory.addFormLink("start", "start", "start", null, layoutCont, Link.LINK);
				}
			} else {
				AccessResult acResult = acService.isAccessible(entry, getIdentity(), false);
				if(acResult.isAccessible()) {
					startLink = uifactory.addFormLink("start", "start", "start", null, layoutCont, Link.LINK);
				} else if (acResult.getAvailableMethods().size() > 0) {
					for(OfferAccess access:acResult.getAvailableMethods()) {
						AccessMethod method = access.getMethod();
						String type = (method.getMethodCssClass() + "_icon").intern();
						Price p = access.getOffer().getPrice();
						String price = p == null || p.isEmpty() ? "" : PriceFormat.fullFormat(p);
						types.add(new PriceMethod(price, type));
					}
					startLink = uifactory.addFormLink("start", "start", "book", null, layoutCont, Link.LINK);
				} else {
					startLink = uifactory.addFormLink("start", "start", "start", null, layoutCont, Link.LINK);
					startLink.setEnabled(false);
				}
			}
			
			if(!types.isEmpty()) {
				layoutCont.contextPut("ac", types);
			}
			
			if(isMember) {
				//show the list of groups
				SearchBusinessGroupParams params = new SearchBusinessGroupParams(getIdentity(), true, true);
				List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, entry, 0, -1);
				List<String> groupLinkNames = new ArrayList<>(groups.size());
				for(BusinessGroup group:groups) {
					String groupLinkName = "grp_" + ++cmpcount;
					FormLink link = uifactory.addFormLink(groupLinkName, "group", group.getName(), null, layoutCont, Link.LINK | Link.NONTRANSLATED);
					link.setUserObject(group.getKey());
					groupLinkNames.add(groupLinkName);
				}
				layoutCont.contextPut("groups", groupLinkNames);
			}
		}
	}
	
	private void updateView(UserRequest ureq) {
		
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
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(commentsCtrl == source) {
			
		} else if(cmc == source) {
			cleanUp();
		} else if (source == wc) {
			if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			} else if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				updateView(ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == editCtrl) {
			if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				// RepositoryEntry changed
				updateView(ureq);
			}
		} else if (source == deleteDialogCtrl){
			if (DialogBoxUIFactory.isYesEvent(event)){
				deleteRepositoryEntry(ureq, getWindowControl());
			}	
		} else if(copyCtrl == source) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				RepositoryEntry copy = copyCtrl.getCopiedEntry();
				fireEvent(ureq, new OpenEvent(copy));
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (downloadLink == source) { 
			doDownload(ureq, false);
		} else if (downloadCompatLink == source) {
			doDownload(ureq, true);
		} else if (launchLink == source) {
			launch(ureq);
		} else if (editLink == source) {
			doEdit(ureq);
		} else if (editSettingsLink == source) {
			doEditSettings(ureq);
		} else if (catalogLink == source) {
			if(repositoryModule.isCatalogEnabled()) {
				doAddCatalog(ureq);
			}
		} else if (bookmarkLink == source) {
			String css = doMark() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE;
			bookmarkLink.setElementCssClass(css);
		} else if (membersLink == source) {
			doOpenMembers(ureq);
		} else if (orderLink == source) {
			doOrders(ureq);
		} else if (closeLink == source) {
			doCloseResource(ureq);
		} else if (deleteLink == source) {
			doDelete(ureq);
		} else if (copyLink == source) {
			doCopy(ureq);
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(membersEditController);
		removeAsListenerAndDispose(deleteDialogCtrl);
		removeAsListenerAndDispose(commentsCtrl);
		removeAsListenerAndDispose(catalogCtlr);
		removeAsListenerAndDispose(ordersCtlr);
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(wc);
		deleteDialogCtrl = null;
		commentsCtrl = null;
		catalogCtlr = null;
		ordersCtlr = null;
		editCtrl = null;
		cmc = null;
		wc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("category".equals(cmd)) {
				Long categoryKey = (Long)link.getUserObject();
				doOpenCategory(ureq, categoryKey);
			} else if("mark".equals(cmd)) {
				boolean marked = doMark();
				markLink.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			} else if("comments".equals(cmd)) {
				doOpenComments(ureq);
			} else if("start".equals(cmd)) {
				launch(ureq);
			} else if("group".equals(cmd)) {
				Long groupKey = (Long)link.getUserObject();
				doOpenGroup(ureq, groupKey);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doCloseResource(UserRequest ureq) {
		RepositoryHandler repoHandler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(entry);

		removeAsListenerAndDispose(wc);
		wc = repoHandler.createCloseResourceController(ureq, getWindowControl(), entry);
		listenTo(wc);
		
		wc.startWorkflow();
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), wc.getInitialComponent());
		listenTo(cmc);
		
		cmc.activate();
	}
	
	private void doCopy(UserRequest ureq) {
		copyCtrl = new CopyRepositoryEntryController(ureq, getWindowControl(), entry);
		listenTo(copyCtrl);
		
		String title = translate("details.copy");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), copyCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(UserRequest ureq) {
		if (!isOwner) throw new OLATSecurityException("Trying to delete, but not allowed: user = " + ureq.getIdentity());
		//show how many users are currently using this resource
		String dialogTitle = translate("del.header", entry.getDisplayname());
		OLATResourceable courseRunOres = OresHelper.createOLATResourceableInstance(RunMainController.ORES_TYPE_COURSE_RUN, entry.getOlatResource().getResourceableId());
		int cnt = CoordinatorManager.getInstance().getCoordinator().getEventBus().getListeningIdentityCntFor(courseRunOres);
		
		String dialogText = translate(corrupted ? "del.confirm.corrupted" : "del.confirm", String.valueOf(cnt));
		deleteDialogCtrl = activateYesNoDialog(ureq, dialogTitle, dialogText, deleteDialogCtrl);
	}
	
	private void doDownload(UserRequest ureq, boolean backwardsCompatible) {
		RepositoryHandler typeToDownload = RepositoryHandlerFactory.getInstance().getRepositoryHandler(entry);
		if (typeToDownload == null) {
			StringBuilder sb = new StringBuilder(translate("error.download"));
			sb.append(": No download handler for repository entry: ")
			  .append(entry.getKey());
			showError(sb.toString());
			return;
		}
		OLATResource ores = entry.getOlatResource();
		if (ores == null) {
			showError("error.download");
			return;
		}
		
		boolean isAlreadyLocked = typeToDownload.isLocked(ores);
		try {			
		  lockResult = typeToDownload.acquireLock(ores, ureq.getIdentity());
		  if(lockResult == null || (lockResult !=null && lockResult.isSuccess() && !isAlreadyLocked)) {
		    MediaResource mr = typeToDownload.getAsMediaResource(ores, backwardsCompatible);
		    if(mr!=null) {
		      repositoryService.incrementDownloadCounter(entry);
		      ureq.getDispatchResult().setResultingMediaResource(mr);
		    } else {
			    showError("error.export");
			    fireEvent(ureq, Event.FAILED_EVENT);			
		    }
		  } else if(lockResult !=null && lockResult.isSuccess() && isAlreadyLocked) {
		  	String fullName = userManager.getUserDisplayName(lockResult.getOwner());
		  	showInfo("warning.course.alreadylocked.bySameUser", fullName);
		  	lockResult = null; //invalid lock, it was already locked
		  } else {
		  	String fullName = userManager.getUserDisplayName(lockResult.getOwner());
		  	showInfo("warning.course.alreadylocked", fullName);
		  }
		} finally {	
			if((lockResult!=null && lockResult.isSuccess() && !isAlreadyLocked)) {
			  typeToDownload.releaseLock(lockResult);		
			  lockResult = null;
			}
		}
	}
	
	private void doEdit(UserRequest ureq) {
		String businessPath = "[RepositoryEntry:" + entry.getKey() + "][Editor:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	/**
	 * Open the editor for all repository entry metadata, access control...
	 * @param ureq
	 */
	private void doEditSettings(UserRequest ureq) {
		removeAsListenerAndDispose(editCtrl);

		editCtrl = new AuthoringEditEntrySettingsController(ureq, getWindowControl(), stackPanel, row);
		listenTo(editCtrl);
	}
	
	private void doOpenCategory(UserRequest ureq, Long categoryKey) {
		String businessPath = "[CatalogEntry:" + categoryKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenGroup(UserRequest ureq, Long groupKey) {
		String businessPath = "[BusinessGroup:" + groupKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private boolean doMark() {
		OLATResourceable item = OresHelper.clone(entry);
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			return false;
		} else {
			String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
			markManager.setMark(item, getIdentity(), null, businessPath);
			return true;
		}
	}
	
	private void doOpenComments(UserRequest ureq) {
		removeAsListenerAndDispose(commentsCtrl);
		CommentAndRatingSecurityCallback secCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, isGuestOnly);
		commentsCtrl = new UserCommentsController(ureq, getWindowControl(), row.getRepositoryEntryResourceable(), null, secCallback);
		stackPanel.pushController(translate("comments"), commentsCtrl);
	}
	
	private void doOpenMembers(UserRequest ureq) {
		if (!isOwner) throw new OLATSecurityException("Trying to access groupmanagement, but not allowed: user = " + getIdentity());
		removeAsListenerAndDispose(membersEditController);
		membersEditController = new RepositoryMembersController(ureq, getWindowControl(), entry);
		listenTo(membersEditController);
		stackPanel.pushController(translate("details.members"), membersEditController);
	}
	
	private void doOrders(UserRequest ureq) {
		if (!isOwner) throw new OLATSecurityException("Trying to access groupmanagement, but not allowed: user = " + getIdentity());
		removeAsListenerAndDispose(ordersCtlr);
		ordersCtlr = new OrdersAdminController(ureq, getWindowControl(), entry.getOlatResource());
		listenTo(ordersCtlr);
		stackPanel.pushController(translate("details.orders"), ordersCtlr);
	}
	
	private void launch(UserRequest ureq) {
		try {
			String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} catch (CorruptedCourseException e)  {
			logError("", e);
		}
	}
	
	/**
	 * Internal helper to initiate the add to catalog workflow
	 * @param ureq
	 */
	private void doAddCatalog(UserRequest ureq) {
		catalogCtlr = new CatalogSettingsController(ureq, getWindowControl(), stackPanel, entry);
		listenTo(catalogCtlr);
	}
	
	private void deleteRepositoryEntry(UserRequest ureq, WindowControl wControl) {
		if (RepositoryManager.getInstance().deleteRepositoryEntryWithAllData( ureq, wControl, entry) ) {
			fireEvent(ureq, new EntryChangedEvent(entry, EntryChangedEvent.DELETED));
			showInfo("info.entry.deleted");
		} else {
			showInfo("info.could.not.delete.entry");
		}
	}
}

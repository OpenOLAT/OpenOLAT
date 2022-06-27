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
package org.olat.core.commons.modules.glossary;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.resource.OLATResource;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Displays a List of all glossary-entries. 
 * If the user is author or administrator, he will get Links to add, edit or delete Items.
 * The list is sortable by an alphabetical register.
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GlossaryMainController extends BasicController implements Activateable2 {

	private VelocityContainer glistVC;
	private Link addButton;
	private LockResult lockEntry = null;
	private final GlossarySecurityCallback glossarySecCallback;
	private final boolean eventProfil;
	private DialogBoxController deleteDialogCtr;
	private Controller glossEditCtrl;
	private List<GlossaryItem> glossaryItemList;
	private GlossaryItem currentDeleteItem;
	private String filterIndex = "";
	private VFSContainer glossaryFolder;

	
	private UserInfoMainController uimc;
	private CloseableModalController cmcUserInfo;
	private CloseableModalController cmc;
	
	private OLATResourceable resourceable;
	
	private static final String CMD_EDIT = "cmd.edit.";
	private static final String CMD_DELETE = "cmd.delete.";
	private static final String CMD_AUTHOR = "cmd.author.";
	private static final String CMD_MODIFIER = "cmd.modifier.";
	private static final String REGISTER_LINK = "register.link.";
	private final Formatter formatter;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private GlossaryItemManager glossaryItemManager;

	public GlossaryMainController(WindowControl control, UserRequest ureq, VFSContainer glossaryFolder, OLATResource res,
			GlossarySecurityCallback glossarySecCallback, boolean eventProfil) {
		super(ureq, control);
		this.glossarySecCallback = glossarySecCallback;
		this.glossaryFolder = glossaryFolder;
		this.eventProfil = eventProfil;
		this.resourceable = res;
		addLoggingResourceable(CoreLoggingResourceable.wrap(res, OlatResourceableType.genRepoEntry));
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN, getClass());
		glistVC = createVelocityContainer("glossarylist");

		formatter = Formatter.getInstance(getLocale());

		glossaryItemList = glossaryItemManager.getGlossaryItemListByVFSItem(glossaryFolder);
		Properties glossProps = glossaryItemManager.getGlossaryConfig(glossaryFolder);
		Boolean registerEnabled = Boolean.valueOf(glossProps.getProperty(GlossaryItemManager.REGISTER_ONOFF));
		glistVC.contextPut("registerEnabled", registerEnabled);
		if (!registerEnabled) {
			filterIndex = "all";
		}
		glistVC.contextPut("userAllowToEditEnabled", Boolean.valueOf(glossarySecCallback.isUserAllowToEditEnabled()));
		
		addButton = LinkFactory.createButtonSmall("cmd.add", glistVC, this);
		addButton.setIconLeftCSS("o_icon o_icon_add o_icon-fw");
		initEditView(ureq, glossarySecCallback.canAdd());

		updateRegisterAndGlossaryItems();
		
		Link showAllLink = LinkFactory.createCustomLink(REGISTER_LINK + "all", REGISTER_LINK + "all", "glossary.list.showall", Link.LINK,
				glistVC, this);
		glistVC.contextPut("showAllLink", showAllLink);

		putInitialPanel(glistVC);
	}

	@Override
	protected void doDispose() {
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CLOSE, getClass());

		// controllers get disposed itself
		// release edit lock
		if (lockEntry != null){
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
        super.doDispose();
	}
	

	/**
	 * create a List with all Indexes in this Glossary
	 * 
	 * @param gIList
	 * @return List containing the Links.
	 */
	protected List<Link> getIndexLinkList(List<GlossaryItem> gIList) {
		Set<String> addedKeys = new HashSet<>();
		//get existing indexes
		GlossaryItem[] gIArray = gIList.toArray(new GlossaryItem[gIList.size()]);
		for (GlossaryItem gi : gIArray) {
			String indexChar = gi.getIndex();
			if (!addedKeys.contains(indexChar)) {
				addedKeys.add(indexChar);
			}
		}
		//build register, first found should be used later on
		char alpha;
		boolean firstIndexFound = false;
		List<Link> indexLinkList = new ArrayList<>(gIList.size());
		for (alpha='A'; alpha <= 'Z'; alpha++){
			String indexChar = String.valueOf(alpha);
			Link indexLink = LinkFactory.createCustomLink(REGISTER_LINK + indexChar, REGISTER_LINK + indexChar, indexChar, Link.NONTRANSLATED,	glistVC, this);
			if (!addedKeys.contains(indexChar)){
				indexLink.setEnabled(false);
			} else if (!filterIndex.equals("all") && !firstIndexFound && !addedKeys.contains(filterIndex)){
				filterIndex = indexChar;
				firstIndexFound = true;
			}
			indexLinkList.add(indexLink);
		}
		
		return indexLinkList;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == addButton) {
			doItem(ureq, null);
		} else if (source instanceof Link) {
			Link button = (Link) source;
			String cmd = button.getCommand();
			if (button.getUserObject() instanceof GlossaryItem){
				GlossaryItem currentGlossaryItem = (GlossaryItem) button.getUserObject();
				if (cmd.startsWith(CMD_EDIT)) {
					doItem(ureq, currentGlossaryItem);
				} else if (button.getCommand().startsWith(CMD_DELETE)) {
					currentDeleteItem = currentGlossaryItem;
					if (deleteDialogCtr != null) {
						deleteDialogCtr.dispose();
					}
					deleteDialogCtr = activateYesNoDialog(ureq, null, translate("glossary.delete.dialog", StringHelper.escapeHtml(currentGlossaryItem.getGlossTerm())),
							deleteDialogCtr);
				} 
			} else if (button.getCommand().startsWith(REGISTER_LINK)) {
				filterIndex = cmd.substring(cmd.lastIndexOf(".") + 1);

				updateRegisterAndGlossaryItems();				
			}
		} else if (source == glistVC) {
			String cmd = event.getCommand();
			if(cmd.startsWith(CMD_AUTHOR)) {
				String url = event.getCommand().substring(CMD_AUTHOR.length());
				openProfil(ureq, url, true);
			} else if (cmd.startsWith(CMD_MODIFIER)) {
				String url = event.getCommand().substring(CMD_MODIFIER.length());
				openProfil(ureq, url, false);
			}
		}
	}
	
	private void doItem(UserRequest ureq, GlossaryItem glossaryItem) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(glossEditCtrl);
		
		boolean add = glossaryItem == null;
		glossEditCtrl = new GlossaryItemEditorController(ureq, getWindowControl(), glossaryFolder, glossaryItemList, glossaryItem, add);
		listenTo(glossEditCtrl);
		
		String title = add ? translate("glossary.add.title") : translate("glossary.edit.title");
		cmc = new CloseableModalController(getWindowControl(), "close", glossEditCtrl.getInitialComponent(), true, title, true);
		cmc.setContextHelp(getTranslator(), "manual_user/course_operation/Using_Additional_Course_Features/#glossary");
		cmc.activate();
		listenTo(cmc);
	}
	
	private void openProfil(UserRequest ureq, String pos, boolean author) {
		Long identityKey = null;
		int id = Integer.parseInt(pos);
		@SuppressWarnings("unchecked")
		List<GlossaryItemWrapper> wrappers = (List<GlossaryItemWrapper>)glistVC.getContext().get("editAndDelButtonList");
		for(GlossaryItemWrapper wrapper:wrappers) {
			if(id == wrapper.getId()) {
				Revision revision = author ? wrapper.getAuthorRevision() : wrapper.getModifierRevision();
				identityKey = revision.getAuthor().extractKey();
				break;
			}
		}
		
		if(identityKey != null) {
			if(eventProfil) {
				removeAsListenerAndDispose(cmc);
				removeAsListenerAndDispose(uimc);

				Identity selectedIdentity = securityManager.loadIdentityByKey(identityKey);
				if(selectedIdentity != null) {
					uimc = new UserInfoMainController(ureq, getWindowControl(), selectedIdentity, false, false);
					listenTo(uimc);
					
					cmcUserInfo = new CloseableModalController(getWindowControl(), "c", uimc.getInitialComponent());
					listenTo(cmcUserInfo);
					cmcUserInfo.activate();
				}
			} else {
				String businessPath = "[Identity:" + identityKey + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == cmc){
			// modal dialog closed -> persist changes on glossaryitem
			glossaryItemManager.saveGlossaryItemList(glossaryFolder, glossaryItemList);
			glossaryItemList = glossaryItemManager.getGlossaryItemListByVFSItem(glossaryFolder);
			updateRegisterAndGlossaryItems();
			cleanUp();
		} else if(source == cmcUserInfo) {
			removeAsListenerAndDispose(cmcUserInfo);
			removeAsListenerAndDispose(uimc);
			cmcUserInfo = null;
			uimc = null;
		} else if (source == deleteDialogCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				glossaryItemList.remove(currentDeleteItem);
				glossaryItemManager.saveGlossaryItemList(glossaryFolder, glossaryItemList);
				// back to glossary view
				updateRegisterAndGlossaryItems();
			}
		} else if(source == glossEditCtrl) {
			if(event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				cleanUp();
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(glossEditCtrl);
		removeAsListenerAndDispose(cmc);
		glossEditCtrl = null;
		cmc = null;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	private void updateRegisterAndGlossaryItems(){
		glistVC.contextPut("registerLinkList", getIndexLinkList(glossaryItemList));
		glistVC.contextPut("editAndDelButtonList", updateView(glossaryItemList, filterIndex));
	}
	
	
	/**
	 * 
	 * @param List with GlossaryItems
	 * @return a list (same size as GlossaryItems) which contains again lists with
	 *         one editButton and one deleteButton
	 */
	private List<GlossaryItemWrapper> updateView(List<GlossaryItem> gIList, String choosenFilterIndex) {
		int linkNum = 1;
		Set<String> keys = new HashSet<>();
		StringBuilder bufDublicates = new StringBuilder();
		List<GlossaryItemWrapper> items = new ArrayList<>();
		Collections.sort(gIList);
		
		glistVC.contextPut("filterIndex", choosenFilterIndex);		
		if (!filterIndex.equals("all")) {
			// highlight filtered index		
			Link indexLink = (Link) glistVC.getComponent(REGISTER_LINK + choosenFilterIndex);
			if (indexLink!=null){
				indexLink.setCustomEnabledLinkCSS("o_glossary_register_active");
			}
		}
		
		for (GlossaryItem gi : gIList) {
			boolean canEdit = glossarySecCallback.canEdit(gi);
			if(canEdit) {
				Link tmpEditButton = LinkFactory.createCustomLink(CMD_EDIT + linkNum, CMD_EDIT + linkNum, "cmd.edit", Link.BUTTON_XSMALL, glistVC,
					this);
				tmpEditButton.setIconLeftCSS("o_icon o_icon_edit o_icon-fw");
				tmpEditButton.setUserObject(gi);
				Link tmpDelButton = LinkFactory.createCustomLink(CMD_DELETE + linkNum, CMD_DELETE + linkNum, "cmd.delete", Link.BUTTON_XSMALL,
					glistVC, this);
				tmpDelButton.setUserObject(gi);
				tmpDelButton.setIconLeftCSS("o_icon o_icon_delete_item o_icon-fw");
			}
			
			GlossaryItemWrapper wrapper = new GlossaryItemWrapper(gi, linkNum);
			if (keys.contains(gi.getGlossTerm()) && (bufDublicates.indexOf(gi.getGlossTerm()) == -1)) {
				bufDublicates.append(gi.getGlossTerm());
				bufDublicates.append(" ");
			} else {
				keys.add(gi.getGlossTerm());
			}
			items.add(wrapper);
			linkNum++;
		}
		
		if ((bufDublicates.length() > 0) && glossarySecCallback.canAdd()) {
			showWarning("warning.contains.dublicates", bufDublicates.toString());
		}
		return items;
	}

	/**
	 * show edit buttons only if there is not yet a lock on this glossary
	 * 
	 * @param ureq
	 * @param allowGlossaryEditing
	 */
	private void initEditView(UserRequest ureq, boolean allowGlossaryEditing) {
		Boolean editModeEnabled = Boolean.valueOf(allowGlossaryEditing);
		glistVC.contextPut("editModeEnabled", editModeEnabled);
		if (allowGlossaryEditing) {
			// try to get lock for this glossary
			lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker()
					.acquireLock(resourceable, getIdentity(), "GlossaryEdit", getWindow());
			if (!lockEntry.isSuccess()) {
				String fullName = userManager.getUserDisplayName(lockEntry.getOwner());
				if(lockEntry.isDifferentWindows()) {
					showWarning("glossary.locked.same.user", StringHelper.escapeHtml(fullName));
				} else {
					showWarning("glossary.locked", StringHelper.escapeHtml(fullName));
				}
				glistVC.contextPut("editModeEnabled", Boolean.FALSE);
			}
		}
	}
	
	public class GlossaryItemWrapper {
		
		private final int id;
		private final GlossaryItem delegate;

		public GlossaryItemWrapper(GlossaryItem delegate, int id) {
			this.delegate = delegate;
			this.id = id;
		}
		
		public int getId() {
			return id;
		}

		public String getIndex() {
			return delegate.getIndex();
		}
		
		public boolean hasAuthor() {
			Revision authorRev = getAuthorRevision();
			return authorRev != null && StringHelper.containsNonWhitespace(authorRev.getAuthor().getLink());
		}
		
		public String getAuthorName() {
			Revision authorRev = getAuthorRevision();
			return authorRev == null ? null : getRevisionAuthorFullName(authorRev); 
		}
		
		public String getAuthorCmd() {
			return CMD_AUTHOR + id; 
		}
		
		public String getAuthorLink() {
			Revision authorRev = getAuthorRevision();
			return getLink(authorRev);
		}
		
		public boolean hasModifier() {
			Revision modifierRev = getModifierRevision();
			return modifierRev != null && StringHelper.containsNonWhitespace(modifierRev.getAuthor().getLink());
		}
		
		public String getModifierName() {
			Revision modifierRev = getModifierRevision();
			return modifierRev == null ? null : getRevisionAuthorFullName(modifierRev); 
		}
		
		public String getModifierCmd() {
			return CMD_MODIFIER + id; 
		}
		
		public String getModifierLink() {
			Revision modifierRev = getModifierRevision();
			return getLink(modifierRev);
		}
		
		public String getCreationDate() {
			Revision authorRev = getAuthorRevision();
			return getMessageDate(authorRev);
		}
		
		public String getLastModificationDate() {
			Revision modifierRev = getModifierRevision();
			return getMessageDate(modifierRev);
		}
		
		private String getMessageDate(Revision rev) {
			if(rev == null) return "";
			Date date = rev.getJavaDate();
			if(date == null) return "";
			String dateStr = formatter.formatDate(date);
			return translate("glossary.item.at", new String[]{ dateStr });
		}

		public List<Revision> getRevHistory() {
			return delegate.getRevHistory();
		}

		public List<String> getGlossFlexions() {
			return delegate.getGlossFlexions();
		}

		public List<String> getGlossSynonyms() {
			return delegate.getGlossSynonyms();
		}

		public String getGlossDef() {
			return delegate.getGlossDef();
		}

		public List<URI> getGlossLinks() {
			return delegate.getGlossLinks();
		}

		public List<GlossaryItem> getGlossSeeAlso() {
			return delegate.getGlossSeeAlso();
		}

		public String getGlossTerm() {
			return delegate.getGlossTerm();
		}
		
		private String getLink(Revision rev) {
			if(rev == null || rev.getAuthor() == null) return null;
			String url = rev.getAuthor().getLink();
			if(StringHelper.containsNonWhitespace(url) && url.startsWith("[") && url.endsWith("]")) {
				int indexUsername = url.indexOf("[Username:");
				if(indexUsername > 0) {
					url = url.substring(0, indexUsername);
				}
				BusinessControl bc = BusinessControlFactory.getInstance().createFromString(url);
				return BusinessControlFactory.getInstance().getAsURIString(bc, true);
			}
			return null;
		}
		
		public Revision getAuthorRevision() {
			List<Revision> revisions = delegate.getRevHistory();
			if(revisions == null || revisions.isEmpty()) return null;
			Revision revision = revisions.get(0);
			if(revision.getAuthor() != null && "added".equals(revision.getRevisionflag())) {
				return revision;
			}
			return null;
		}
		
		public Revision getModifierRevision() {
			List<Revision> revisions = delegate.getRevHistory();
			if(revisions == null || revisions.isEmpty()) return null;
			
			Revision lastRevision = revisions.get(revisions.size() - 1);
			if(lastRevision.getAuthor() != null && "changed".equals(lastRevision.getRevisionflag())) {
				return lastRevision;
			}
			return null;
		}
		
		private String getRevisionAuthorFullName(Revision revision) {
			if(revision == null || revision.getAuthor() == null) return null;
			
			StringBuilder sb = new StringBuilder();
			if(StringHelper.containsNonWhitespace(revision.getAuthor().getFirstname())) {
				sb.append(revision.getAuthor().getFirstname());
			}
			if(StringHelper.containsNonWhitespace(revision.getAuthor().getSurname())) {
				if(sb.length() > 0) sb.append(' ');
				sb.append(revision.getAuthor().getSurname());
			}
			return sb.toString();
		}
	}
}

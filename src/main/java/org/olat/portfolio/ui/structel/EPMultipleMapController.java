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
package org.olat.portfolio.ui.structel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.portfolio.EPLoggingAction;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.EPSecurityCallbackFactory;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.EPTargetResource;
import org.olat.portfolio.model.structel.ElementType;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.ui.EPMapRunViewOption;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Present a list of maps. allows: 
 * - Open a map  
 * - delete a map
 * - copy a map with or without artefacts and open it
 * 
 * <P>
 * Initial Date: 04.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPMultipleMapController extends BasicController implements Activateable2 {
	private static final String RESTRICT_LINK = "restrictLink";
	private static final String VIEW_LINK_PREFIX = "viewLink";
	private static final String DELETE_LINK_PREFIX = "deleteLink";
	private static final String COPY_LINK_PREFIX = "copyLink";
	private static final String SHARE_LINK_PREFIX = "shareLink";
	private static final String PAGING_LINK_PREFIX = "pageLink";
	
	private static final int ITEMS_PER_PAGE = 9;
	
	private final VelocityContainer vC;
	private DialogBoxController delMapCtrl;
	private DialogBoxController copyMapCtrl;
	private EPMapViewController mapViewCtrl;
	private EPShareListController shareListController;
	private CloseableModalController shareBox;
	private final StackedPanel myPanel;
	
	private final EPMapRunViewOption option;
	private final Identity mapOwner;
	private List<PortfolioStructureMap> userMaps;
	private boolean restrictShareView = true;
	private long start;
	@Autowired
	private EPFrontendManager ePFMgr;
	@Autowired
	private PortfolioModule portfolioModule;
	
//	components for paging
	private Link forwardLink;
	private int currentPageNum = 1;
	private int currentPagingFrom = 0;
	private int currentPagingTo = ITEMS_PER_PAGE;
	private boolean pagingAvailable = false;
	
	public EPMultipleMapController(UserRequest ureq, WindowControl control, EPMapRunViewOption option, Identity mapOwner) {
		super(ureq, control);

		this.option = option;
		this.mapOwner = mapOwner;
		vC = createVelocityContainer("multiMaps");		
		initOrUpdateMaps(ureq);

		myPanel = putInitialPanel(vC);
	}
	
	/**
	 * returns a List of PortfolioStructures to display, depending on options (all OLAT-wide shared maps, only shared to me, paging)
	 * 
	 * @return
	 */
	private List<PortfolioStructure> getUsersStructsToDisplay(){
		pagingAvailable = false;
		// get maps for this user
		List<PortfolioStructure> allUsersStruct;
		switch (option) {
			case OTHER_MAPS:// same as OTHERS_MAPS
			case OTHERS_MAPS:
				vC.remove(vC.getComponent(RESTRICT_LINK));
				if (restrictShareView) {
					if (portfolioModule.isOfferPublicMapList()) {
						LinkFactory.createCustomLink(RESTRICT_LINK, "change", "restrict.show.all", Link.LINK, vC, this);
					}
					allUsersStruct = ePFMgr.getStructureElementsFromOthersWithoutPublic(getIdentity(), mapOwner, ElementType.STRUCTURED_MAP, ElementType.DEFAULT_MAP);
				} else {
					if (portfolioModule.isOfferPublicMapList()) {
						LinkFactory.createCustomLink(RESTRICT_LINK, "change", "restrict.show.limited", Link.LINK, vC, this);
					}
					// this query can be quite time consuming, if fetching all structures -> do paging
					currentPagingFrom = (currentPageNum-1)*ITEMS_PER_PAGE;
					currentPagingTo = currentPagingFrom+ITEMS_PER_PAGE;
					allUsersStruct = ePFMgr.getStructureElementsFromOthers(getIdentity(), mapOwner, currentPagingFrom, currentPagingTo ,ElementType.STRUCTURED_MAP, ElementType.DEFAULT_MAP);
					pagingAvailable = true;
				}
				break;
			case MY_EXERCISES_MAPS:
				allUsersStruct = ePFMgr.getStructureElementsForUser(getIdentity(), ElementType.STRUCTURED_MAP);
				break;
			default:// MY_DEFAULTS_MAPS
				allUsersStruct = ePFMgr.getStructureElementsForUser(getIdentity(), ElementType.DEFAULT_MAP);
		}
		if (isLogDebugEnabled()) {
			logDebug("got all structures to see at: " + System.currentTimeMillis());
		}
		return allUsersStruct;
	}
	
	
	/**
	 * 
	 */
	private void initOrUpdateMaps(UserRequest ureq) {		
		if (isLogDebugEnabled()) {
			start = System.currentTimeMillis();
			logDebug("start loading map overview at : " + start);
		}
		
		List<PortfolioStructure> allUsersStruct = getUsersStructsToDisplay();
		userMaps = new ArrayList<>();
		if (allUsersStruct.isEmpty()) {
			vC.contextPut("noMaps", true);
			return;
		} else vC.contextRemove("noMaps");
		
		//remove forward link (maybe it's not needed (last page) )
		if(forwardLink != null)
			vC.remove(forwardLink);
		
		// now add paging-components if necessary and wanted
		int elementCount   = ePFMgr.countStructureElementsFromOthers(getIdentity(), mapOwner, ElementType.DEFAULT_MAP);
		if(pagingAvailable && elementCount > ITEMS_PER_PAGE){
			vC.contextPut("showPaging", true);
			
			int additionalPage = ((elementCount % ITEMS_PER_PAGE) > 0)?1:0;
			int pageCount = (elementCount/ITEMS_PER_PAGE) + additionalPage;
			List<Component> pagingLinks = new ArrayList<>();
			for(int i = 1; i < pageCount+1; i++){
				Link pageLink = LinkFactory.createCustomLink(PAGING_LINK_PREFIX+i, "switchPage", String.valueOf(i), Link.NONTRANSLATED, vC, this);
				pageLink.setUserObject(Integer.valueOf(i));
				pagingLinks.add(pageLink);
				if(i == currentPageNum){
					pageLink.setEnabled(false);
				}
			}
			
			vC.contextPut("pageLinks",pagingLinks);
			
			if(currentPageNum < pageCount){
				forwardLink = LinkFactory.createCustomLink("forwardLink", "pagingFWD", "table.forward", Link.LINK, vC, this);
				forwardLink.setIconRightCSS("o_icon o_icon_next_page");
			}
		}

		//now display the maps
		
		List<String> artAmount = new ArrayList<>(userMaps.size());
		List<Integer> childAmount = new ArrayList<>(userMaps.size());
		List<String> mapStyles = new ArrayList<>(userMaps.size());
		List<Date> deadLines = new ArrayList<>(userMaps.size());
		List<String> restriStats = new ArrayList<>(userMaps.size());
		List<String> owners = new ArrayList<>(userMaps.size());
		List<String> amounts = new ArrayList<>(userMaps.size());
		
 		int i = 1;
		for (PortfolioStructure portfolioStructure : allUsersStruct) {
			if (portfolioStructure.getRoot() == null) { //only show maps
				PortfolioStructureMap map = (PortfolioStructureMap)portfolioStructure;
				EPSecurityCallback secCallback = EPSecurityCallbackFactory.getSecurityCallback(ureq, map, ePFMgr);

				userMaps.add(map);
				Link vLink = LinkFactory.createCustomLink(VIEW_LINK_PREFIX + i, "viewMap" + map.getResourceableId(), "view.map",
						Link.LINK, vC, this);
				vLink.setUserObject(map);
				vLink.setElementCssClass("o_sel_ep_open_map");
				vLink.setIconRightCSS("o_icon o_icon-fw o_icon_start");
				
				//can always try to delete your own map, but exercise only if the course was deleted
				vC.remove(vC.getComponent(DELETE_LINK_PREFIX + i)); // remove as update could require hiding it
				// can always try to delete your own map, but exercise only if the course was deleted
				final boolean myMaps = (option.equals(EPMapRunViewOption.MY_DEFAULTS_MAPS) || option.equals(EPMapRunViewOption.MY_EXERCISES_MAPS));
				boolean addDeleteLink = myMaps;
				
				if((map instanceof EPStructuredMap) && (((EPStructuredMap) map).getReturnDate() != null)){
						addDeleteLink = false; //it's a portfolio-task that was already handed in, so do not display delete-link
				}
				
				if (addDeleteLink) {
					final Link dLink = LinkFactory.createCustomLink(DELETE_LINK_PREFIX + i, "delMap" + map.getResourceableId(), "delete.map", Link.LINK, vC, this);
					dLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
					dLink.setUserObject(map);
				}
				
				Link cLink = LinkFactory.createCustomLink(COPY_LINK_PREFIX + i, "copyMap" + map.getResourceableId(), "copy.map",
						Link.LINK, vC, this);
				cLink.setIconLeftCSS("o_icon o_icon_copy");
				cLink.setUserObject(map);
				// its not allowed to copy maps from a portfolio-task
				if (map instanceof EPStructuredMap) {
					cLink.setVisible(false); 
				}
				
				vC.remove(vC.getComponent(SHARE_LINK_PREFIX + i)); // remove as update could require hiding it
				if(myMaps && secCallback.canShareMap()) {
					Link shareLink = LinkFactory.createCustomLink(SHARE_LINK_PREFIX + i, "shareMap" + map.getResourceableId(), "map.share",
							Link.LINK, vC, this);
					shareLink.setIconLeftCSS("o_icon o_icon-fw o_icon_share");
					shareLink.setUserObject(map);
					boolean shared = ePFMgr.isMapShared(map);
					if(shared || (map instanceof EPStructuredMap && ((EPStructuredMap)map).getTargetResource() != null)) {
						shareLink.setCustomDisplayText(translate("map.share.shared"));
					}
				}
				if (isLogDebugEnabled()) {
					logDebug("  in loop : got share state at: " + System.currentTimeMillis());
				}
				
				// get deadline + link to course
				if (map instanceof EPStructuredMap){
					EPStructuredMap structMap = (EPStructuredMap)map;
					Date deadLine = structMap.getDeadLine();
					deadLines.add(deadLine);

					EPTargetResource resource = structMap.getTargetResource();
					RepositoryEntry repoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(resource.getOLATResourceable(), false);
					if(repoEntry != null) {
						vC.contextPut("courseName" + i, StringHelper.escapeHtml(repoEntry.getDisplayname()));
						String url = Settings.getServerContextPathURI();
						url += "/url/RepositoryEntry/" + repoEntry.getKey() + "/CourseNode/" + resource.getSubPath();
						vC.contextPut("courseLink" + i, url);
					}
					if (isLogDebugEnabled()) {
						logDebug("  in loop : looked up course at : " + System.currentTimeMillis());
					}
					// get some stats about the restrictions if available
					String[] stats = ePFMgr.getRestrictionStatisticsOfMap(structMap);
					int toCollect = 0;
					if (stats != null){
						try {
							toCollect = Integer.parseInt(stats[1]) - Integer.parseInt(stats[0]);		
						} catch (Exception e) {
							// do nothing
							toCollect = 0;
						}
					}
					if (toCollect != 0) {
						restriStats.add(String.valueOf(toCollect));
					} else {
						restriStats.add(null);
					}
					if (isLogDebugEnabled()) {
						logDebug("  in loop : calculated restriction statistics at : " + System.currentTimeMillis());
					}											
				} else {
					deadLines.add(null);
					restriStats.add(null);
				}
				
				// show owner on shared maps
				if (!secCallback.isOwner()){
					owners.add(ePFMgr.getAllOwnersAsString(map));
				} else owners.add(null);
				
				String artCount = String.valueOf(ePFMgr.countArtefactsInMap(map));
				artAmount.add(artCount);
				Integer childs = ePFMgr.countStructureChildren(map);
				childAmount.add(childs);
				amounts.add(translate("map.contains", new String[]{childs.toString(), artCount}));
				
				mapStyles.add(ePFMgr.getValidStyleName(map));
				if (isLogDebugEnabled()) {
					logDebug("  in loop : got map details (artefact-amount, child-struct-amount, style) at : " + System.currentTimeMillis());
				}
				i++;
			}
		}
		vC.contextPut("owners", owners);
		vC.contextPut("deadLines", deadLines);
		vC.contextPut("restriStats", restriStats);
		vC.contextPut("mapStyles", mapStyles);
		vC.contextPut("childAmount", childAmount);
		vC.contextPut("artefactAmount", artAmount);
		vC.contextPut("amounts", amounts);
		vC.contextPut("userMaps", userMaps);
		if (isLogDebugEnabled()) {
			long now = System.currentTimeMillis();
			logDebug("finished processing all maps at : " + now);
			logDebug("Total processing time for " + (i-1) + " maps was : " + (now-start));
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		Long key = entries.get(0).getOLATResourceable().getResourceableId();
		activateMap(ureq, key);
		if(mapViewCtrl != null && entries.size() > 1) {
			//map successfully activated
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			mapViewCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link srcLink = (Link) source;
			if (srcLink.getUserObject() instanceof PortfolioStructureMap) {
			PortfolioStructureMap selMap = (PortfolioStructureMap) srcLink.getUserObject();
			if (srcLink.getComponentName().startsWith(VIEW_LINK_PREFIX)) {
				activateMap(ureq, selMap);
				fireEvent(ureq, new EPMapEvent(EPStructureEvent.SELECT, selMap));
			} else if (srcLink.getComponentName().startsWith(DELETE_LINK_PREFIX)) {
				deleteMap(ureq, selMap);
			} else if (srcLink.getComponentName().startsWith(COPY_LINK_PREFIX)) {
				List<String> buttonLabels = new ArrayList<>();
				String introKey = "copy.map.intro";
				if (ePFMgr.isMapOwner(getIdentity(), selMap)){
					buttonLabels.add(translate("copy.with.artefacts"));
					introKey = "copy.map.intro2";
				}
				buttonLabels.add(translate("copy.without.artefacts"));
				buttonLabels.add(translate("copy.cancel"));
				String text = translate(introKey, StringHelper.escapeHtml(selMap.getTitle()));
				copyMapCtrl = activateGenericDialog(ureq, translate("copy.map.title"), text, buttonLabels , copyMapCtrl);
				copyMapCtrl.setUserObject(selMap);
			} else if (srcLink.getComponentName().startsWith(SHARE_LINK_PREFIX)) {
				popUpShareBox(ureq, selMap);
			} else if (srcLink.getComponentName().equals(RESTRICT_LINK)){
				restrictShareView = !restrictShareView;
				initOrUpdateMaps(ureq);
			}
		} else{
			if( srcLink.equals(forwardLink)){
				currentPageNum++;
				initOrUpdateMaps(ureq);
			} else if (srcLink.getComponentName().startsWith(PAGING_LINK_PREFIX)){
				Integer page = (Integer) srcLink.getUserObject();
				currentPageNum = page.intValue();
				initOrUpdateMaps(ureq);
			} else if (srcLink.getComponentName().equals(RESTRICT_LINK)) {
				restrictShareView = !restrictShareView;
				initOrUpdateMaps(ureq);
			}
		}
		}
	}
	
	private void deleteMap(UserRequest ureq, PortfolioStructureMap map) {
		String intro = translate("delete.map.intro", StringHelper.escapeHtml(map.getTitle()));
		delMapCtrl = activateYesNoDialog(ureq, translate("delete.map.title"), intro, delMapCtrl);
		delMapCtrl.setUserObject(map);
	}
	
	private void popUpShareBox(UserRequest ureq, PortfolioStructureMap map) {
		removeAsListenerAndDispose(shareListController);
		removeAsListenerAndDispose(shareBox);
		shareListController = new EPShareListController(ureq, getWindowControl(), map);
		listenTo(shareListController);

		String title = translate("map.share");
		shareBox = new CloseableModalController(getWindowControl(), "close", shareListController.getInitialComponent(), true, title);
		listenTo(shareBox);
		shareBox.activate();
	}
	
	private void activateMap(UserRequest ureq, Long mapKey) {
		if(mapKey == null) return;
		
		boolean foundTheMap = false;
		// we have a key, find the corresponding map with the current option (restrcited view or not)
		for(PortfolioStructureMap map: userMaps) {
			if(map.getKey().equals(mapKey) || (map.getResourceableId().equals(mapKey))) {
				activateMap(ureq, map);
				fireEvent(ureq, new EPMapEvent(EPStructureEvent.SELECT, map));
				foundTheMap = true;
				break;
			}
		}
		
		if(!foundTheMap) {
			// map not found, switch the option and retry to found the map
			restrictShareView = !restrictShareView;
			initOrUpdateMaps(ureq);
			for(PortfolioStructureMap map: userMaps) {
				if(map.getKey().equals(mapKey) || (map.getResourceableId().equals(mapKey))) {
					activateMap(ureq, map);
					fireEvent(ureq, new EPMapEvent(EPStructureEvent.SELECT, map));
					break;
				}
			}
		}
	}
	
	public void activateMap(UserRequest ureq, PortfolioStructureMap struct){
		if(userMaps != null && !userMaps.contains(struct)) {
			initOrUpdateMaps(ureq);
		}
		
		if(mapViewCtrl != null) {
			removeAsListenerAndDispose(mapViewCtrl);
		}

		EPSecurityCallback secCallback = EPSecurityCallbackFactory.getSecurityCallback(ureq, struct, ePFMgr);
		//release the previous if not correctly released by CLOSE events
		
		WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(struct.getClass(), struct.getKey()), null);
		mapViewCtrl = new EPMapViewController(ureq, bwControl, struct, true, false, secCallback);
		listenTo(mapViewCtrl);
		myPanel.pushContent(mapViewCtrl.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == delMapCtrl) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				PortfolioStructure mapToDel = (PortfolioStructure) ((DialogBoxController) source).getUserObject();
				String title = mapToDel.getTitle();
				ePFMgr.deletePortfolioStructure(mapToDel);
				showInfo("delete.map.success", title);
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(mapToDel));
				ThreadLocalUserActivityLogger.log(EPLoggingAction.EPORTFOLIO_MAP_REMOVED, getClass());
				initOrUpdateMaps(ureq);
			}
		} else if (source == copyMapCtrl) {
			if (event.equals(Event.CANCELLED_EVENT)) {
				fireEvent(ureq, Event.CANCELLED_EVENT); 
				return;
			} 
			int pos = DialogBoxUIFactory.getButtonPos(event);
			boolean withArtefacts = false;
			PortfolioStructure mapToCopy = (PortfolioStructure) ((DialogBoxController) source).getUserObject();
			if (!ePFMgr.isMapOwner(getIdentity(), mapToCopy)) pos++; // shift clicked pos, when "with artefacts" was hidden before
			if (pos == 2){
				// clicked cancel button
				fireEvent(ureq, Event.CANCELLED_EVENT);
				return;
			} else if (pos == 0) withArtefacts = true;
			PortfolioStructureMap targetMap = ePFMgr.createAndPersistPortfolioDefaultMap(getIdentity(), translate("map.copy.of", mapToCopy.getTitle()), mapToCopy.getDescription());
			ePFMgr.copyStructureRecursively(mapToCopy, targetMap, withArtefacts);			
			// open the map
			String title = targetMap.getTitle();
			showInfo("copy.map.success", title);
			initOrUpdateMaps(ureq);
			String businessPath = "[" +  targetMap.getClass().getSimpleName() + ":" + targetMap.getResourceableId() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} else if (source == mapViewCtrl) {
			if(EPStructureEvent.CLOSE.equals(event.getCommand())) {
				myPanel.popContent();
				fireEvent(ureq, event);
				removeAsListenerAndDispose(mapViewCtrl);
				mapViewCtrl = null;
				// refresh on close (back-link) to prevent stale object errors, when map got changed meanwhile
				initOrUpdateMaps(ureq);
				addToHistory(ureq);
			} else if (EPStructureEvent.SUBMIT.equals(event.getCommand()) || event.equals(Event.CHANGED_EVENT)){
				// refresh on submission of a map or on any other changes which needs an ui-update
				initOrUpdateMaps(ureq);
			} 
		} else if (source == shareListController) {
			shareBox.deactivate();
			removeAsListenerAndDispose(shareListController);
			initOrUpdateMaps(ureq);
		}
		if (event instanceof EPStructureChangeEvent){
			// event from child
			String evCmd = event.getCommand();
			if (evCmd.equals(EPStructureChangeEvent.ADDED) || evCmd.equals(EPStructureChangeEvent.CHANGED)){
				initOrUpdateMaps(ureq);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//
	}
}
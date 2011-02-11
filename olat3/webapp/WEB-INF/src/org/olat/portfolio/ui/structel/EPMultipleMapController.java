/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.portfolio.ui.structel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalWindowWrapperController;
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.home.site.HomeSite;
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
public class EPMultipleMapController extends BasicController implements Activateable {
	private static final String RESTRICT_LINK = "restrictLink";
	private static final String VIEW_LINK_PREFIX = "viewLink";
	private static final String DELETE_LINK_PREFIX = "deleteLink";
	private static final String COPY_LINK_PREFIX = "copyLink";
	private static final String SHARE_LINK_PREFIX = "shareLink";
	private VelocityContainer vC;
	private EPFrontendManager ePFMgr;
	private DialogBoxController delMapCtrl;
	private DialogBoxController copyMapCtrl;
	private EPMapViewController mapViewCtrl;
	private EPShareListController shareListController;
	private CloseableModalWindowWrapperController shareBox;
	private Panel myPanel;
	
	private final EPMapRunViewOption option;
	private final Identity mapOwner;
	private List<PortfolioStructureMap> userMaps;
	private boolean restrictShareView = true;
	private long start;
	private PortfolioModule portfolioModule;
	
	public EPMultipleMapController(UserRequest ureq, WindowControl control, EPMapRunViewOption option, Identity mapOwner) {
		super(ureq, control);

		this.option = option;
		this.mapOwner = mapOwner;
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		portfolioModule = (PortfolioModule) CoreSpringFactory.getBean("portfolioModule");
		vC = createVelocityContainer("multiMaps");		
		initOrUpdateMaps(ureq);

		myPanel = putInitialPanel(vC);
	}

	/**
	 * 
	 */
	private void initOrUpdateMaps(UserRequest ureq) {		
		if (isLogDebugEnabled()) {
			start = System.currentTimeMillis();
			logDebug("start loading map overview at : ", String.valueOf(start));
		}
		// get maps for this user
		List<PortfolioStructure> allUsersStruct;
		switch(option) {
			case OTHER_MAPS://same as OTHERS_MAPS
			case OTHERS_MAPS:
				vC.remove(vC.getComponent(RESTRICT_LINK));
				if (restrictShareView) {
					if (portfolioModule.isOfferPublicMapList()) LinkFactory.createCustomLink(RESTRICT_LINK, "change", "restrict.show.all", Link.LINK, vC, this);
					allUsersStruct = ePFMgr.getStructureElementsFromOthersWithoutPublic(getIdentity(), mapOwner, ElementType.STRUCTURED_MAP, ElementType.DEFAULT_MAP);
				} else {
					if (portfolioModule.isOfferPublicMapList()) LinkFactory.createCustomLink(RESTRICT_LINK, "change", "restrict.show.limited", Link.LINK, vC, this);
					allUsersStruct = ePFMgr.getStructureElementsFromOthers(getIdentity(), mapOwner, ElementType.STRUCTURED_MAP, ElementType.DEFAULT_MAP);
				}
				break;
			case MY_EXERCISES_MAPS:
				allUsersStruct = ePFMgr.getStructureElementsForUser(getIdentity(), ElementType.STRUCTURED_MAP);
				break;
			default://MY_DEFAULTS_MAPS
				allUsersStruct = ePFMgr.getStructureElementsForUser(getIdentity(), ElementType.DEFAULT_MAP);
		}
		if (isLogDebugEnabled()) {
			logDebug("got all structures to see at: ", String.valueOf(System.currentTimeMillis()));
		}
		
		userMaps = new ArrayList<PortfolioStructureMap>();
		if (allUsersStruct.isEmpty()) {
			vC.contextPut("noMaps", true);
			return;
		} else vC.contextRemove("noMaps");
		
		List<String> artAmount = new ArrayList<String>(userMaps.size());
		List<Integer> childAmount = new ArrayList<Integer>(userMaps.size());
		List<String> mapStyles = new ArrayList<String>(userMaps.size());
		List<Date> deadLines = new ArrayList<Date>(userMaps.size());
		List<String> owners = new ArrayList<String>(userMaps.size());
		List<String> amounts = new ArrayList<String>(userMaps.size());
		
 		int i = 1;
		for (PortfolioStructure portfolioStructure : allUsersStruct) {
			if (portfolioStructure.getRoot() == null) { //only show maps
				PortfolioStructureMap map = (PortfolioStructureMap)portfolioStructure;
				EPSecurityCallback secCallback = EPSecurityCallbackFactory.getSecurityCallback(ureq, map, ePFMgr);

				userMaps.add(map);
				Link vLink = LinkFactory.createCustomLink(VIEW_LINK_PREFIX + i, "viewMap" + map.getResourceableId(), "view.map",
						Link.LINK, vC, this);
				vLink.setUserObject(map);
				vLink.setCustomEnabledLinkCSS("b_with_small_icon_right b_open_icon");
				
				boolean myMaps = (option.equals(EPMapRunViewOption.MY_DEFAULTS_MAPS) || option.equals(EPMapRunViewOption.MY_EXERCISES_MAPS));
				//can always try to delete your own map, but exercise only if the course was deleted
				vC.remove(vC.getComponent(DELETE_LINK_PREFIX + i)); // remove as update could require hiding it
				if(myMaps) {
					Link dLink = LinkFactory.createCustomLink(DELETE_LINK_PREFIX + i, "delMap" + map.getResourceableId(), "delete.map",
							Link.LINK, vC, this);
					dLink.setCustomEnabledLinkCSS("b_with_small_icon_left b_delete_icon");
					dLink.setUserObject(map);
				}
				
				Link cLink = LinkFactory.createCustomLink(COPY_LINK_PREFIX + i, "copyMap" + map.getResourceableId(), "copy.map",
						Link.LINK, vC, this);
				cLink.setCustomEnabledLinkCSS("b_with_small_icon_left b_copy_icon");
				cLink.setUserObject(map);
				// its not allowed to copy maps from a portfolio-task
				if (map instanceof EPStructuredMap) {
					cLink.setVisible(false); 
				}
				
				vC.remove(vC.getComponent(SHARE_LINK_PREFIX + i)); // remove as update could require hiding it
				if(myMaps && secCallback.canShareMap()) {
					Link shareLink = LinkFactory.createCustomLink(SHARE_LINK_PREFIX + i, "shareMap" + map.getResourceableId(), "map.share",
							Link.LINK, vC, this);
					shareLink.setCustomEnabledLinkCSS("b_with_small_icon_left b_share_icon");
					shareLink.setUserObject(map);
					boolean shared = ePFMgr.isMapShared(map);
					if(shared) {
						shareLink.setCustomDisplayText(translate("map.share.shared"));
					}
				}
				if (isLogDebugEnabled()) {
					logDebug("  in loop : got share state at: ", String.valueOf(System.currentTimeMillis()));
				}
				
				// get deadline + link to course
				if (map instanceof EPStructuredMap){
					EPStructuredMap structMap = (EPStructuredMap)map;
					Date deadLine = structMap.getDeadLine();
					deadLines.add(deadLine);

					EPTargetResource resource = structMap.getTargetResource();
					RepositoryEntry repoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(resource.getOLATResourceable(), false);
					if(repoEntry != null) {
						vC.contextPut("courseName" + i, repoEntry.getDisplayname());
						String url = Settings.getServerContextPathURI();
						url += "/url/RepositoryEntry/" + repoEntry.getKey() + "/CourseNode/" + resource.getSubPath();
						vC.contextPut("courseLink" + i, url);
					}
					if (isLogDebugEnabled()) {
						logDebug("  in loop : looked up course at : ", String.valueOf(System.currentTimeMillis()));
					}
				}	else {
					deadLines.add(null);	
				}
				
				// show owner on shared maps
				if (!secCallback.isOwner()){
					List<Identity> ownerIdents = BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(map.getOwnerGroup());
					List<String> identNames = new ArrayList<String>();
					for (Identity identity : ownerIdents) {
						String fullName = identity.getUser().getProperty(UserConstants.FIRSTNAME, null)+" "+identity.getUser().getProperty(UserConstants.LASTNAME, null);
						identNames.add(fullName);
					}
					owners.add(StringHelper.formatAsCSVString(identNames));
				} else owners.add(null);
				
				String artCount = String.valueOf(ePFMgr.countArtefactsInMap(map));
				artAmount.add(artCount);
				Integer childs = ePFMgr.countStructureChildren(map);
				childAmount.add(childs);
				amounts.add(translate("map.contains", new String[]{childs.toString(), artCount}));
				
				mapStyles.add(ePFMgr.getValidStyleName(map));
				if (isLogDebugEnabled()) {
					logDebug("  in loop : got map details (artefact-amount, child-struct-amount, style) at : ", String.valueOf(System.currentTimeMillis()));
				}
				i++;
			}
		}
		vC.contextPut("owners", owners);
		vC.contextPut("deadLines", deadLines);
		vC.contextPut("mapStyles", mapStyles);
		vC.contextPut("childAmount", childAmount);
		vC.contextPut("artefactAmount", artAmount);
		vC.contextPut("amounts", amounts);
		vC.contextPut("userMaps", userMaps);
		if (isLogDebugEnabled()) {
			long now = System.currentTimeMillis();
			logDebug("finished processing all maps at : ", String.valueOf(now));
			logDebug("Total processing time for " + (i-1) + " maps was : ", String.valueOf(now-start));
		}
	}

	@Override
	public void activate(UserRequest ureq, String viewIdentifier) {
		int index = viewIdentifier.indexOf("[map:");
		Long key = null;
		boolean idIsKey = true;
		try {
			key = Long.parseLong(viewIdentifier);
		} catch (Exception e) {
			idIsKey = false;
		} 
		
		if(index >= 0 && !idIsKey) {
			int lastIndex = viewIdentifier.indexOf("]", index);
			if(lastIndex < viewIdentifier.length()) {
				String keyStr = viewIdentifier.substring(index + 5, lastIndex);
				key = Long.parseLong(keyStr);
			}
		}
		for(PortfolioStructureMap map: userMaps) {
			if(map.getKey().equals(key) || (idIsKey && map.getResourceableId().equals(key))) {
				activateMap(ureq, map);
				fireEvent(ureq, new EPMapEvent(EPStructureEvent.SELECT, map));
				break;
			}
		}
		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@SuppressWarnings("unused")
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link srcLink = (Link) source;
			PortfolioStructureMap selMap = (PortfolioStructureMap) srcLink.getUserObject();
			if (srcLink.getComponentName().startsWith(VIEW_LINK_PREFIX)) {
				activateMap(ureq, selMap);
				fireEvent(ureq, new EPMapEvent(EPStructureEvent.SELECT, selMap));
			} else if (srcLink.getComponentName().startsWith(DELETE_LINK_PREFIX)) {
				deleteMap(ureq, selMap);
			} else if (srcLink.getComponentName().startsWith(COPY_LINK_PREFIX)) {
				List<String> buttonLabels = new ArrayList<String>();
				String introKey = "copy.map.intro";
				if (ePFMgr.isMapOwner(getIdentity(), selMap)){
					buttonLabels.add(translate("copy.with.artefacts"));
					introKey = "copy.map.intro2";
				}
				buttonLabels.add(translate("copy.without.artefacts"));
				buttonLabels.add(translate("copy.cancel"));
				copyMapCtrl = activateGenericDialog(ureq, translate("copy.map.title"), translate(introKey, selMap.getTitle()), buttonLabels , copyMapCtrl);
				copyMapCtrl.setUserObject(selMap);
			} else if (srcLink.getComponentName().startsWith(SHARE_LINK_PREFIX)) {
				popUpShareBox(ureq, selMap);
			} else if (srcLink.getComponentName().equals(RESTRICT_LINK)){
				restrictShareView = !restrictShareView;
				initOrUpdateMaps(ureq);
			}
		} 
	}
	
	private void deleteMap(UserRequest ureq, PortfolioStructureMap map) {
		delMapCtrl = activateYesNoDialog(ureq, translate("delete.map.title"), translate("delete.map.intro", map.getTitle()), delMapCtrl);
		delMapCtrl.setUserObject(map);
	}
	
	private void popUpShareBox(UserRequest ureq, PortfolioStructureMap map) {
		removeAsListenerAndDispose(shareListController);
		removeAsListenerAndDispose(shareBox);
		shareListController = new EPShareListController(ureq, getWindowControl(), map);
		listenTo(shareListController);

		String title = translate("map.share");
		shareBox = new CloseableModalWindowWrapperController(ureq, getWindowControl(), title, shareListController.getInitialComponent(),
				"shareBox" + map.getKey());
		shareBox.setInitialWindowSize(800, 600);
		listenTo(shareBox);
		shareBox.activate();
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
		mapViewCtrl = new EPMapViewController(ureq, getWindowControl(), struct, true, secCallback);
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
			String activationCmd = targetMap.getClass().getSimpleName() + ":" + targetMap.getResourceableId();
			DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
			dts.activateStatic(ureq, HomeSite.class.getName(), activationCmd);
		} else if (source == mapViewCtrl) {
			if(EPStructureEvent.CLOSE.equals(event.getCommand())) {
				myPanel.popContent();
				fireEvent(ureq, event);
				removeAsListenerAndDispose(mapViewCtrl);
				mapViewCtrl = null;
				// refresh on close (back-link) to prevent stale object errors, when map got changed meanwhile
				initOrUpdateMaps(ureq);
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
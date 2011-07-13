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

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.portfolio.EPLoggingAction;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.EPSecurityCallbackFactory;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.EPStructuredMapTemplate;
import org.olat.portfolio.model.structel.EPTargetResource;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.model.structel.StructureStatusEnum;
import org.olat.portfolio.ui.structel.edit.EPStructureTreeAndDetailsEditController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * shows a map itself with containing pages, structures, etc. 
 * 
 * <P>
 * Initial Date: 04.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPMapViewController extends BasicController {
	
	private PortfolioStructureMap map;
	private final EPFrontendManager ePFMgr;
	private EPMultiplePageController pageCtrl;
	private Link editButton;
	private Link backLink;
	private Link submitAssessLink;
	private EPStructureTreeAndDetailsEditController editCtrl;
	private DialogBoxController confirmationSubmissionCtr;
	private final boolean back;
	private EPSecurityCallback secCallback;
	private LockResult lockEntry;
	
	private final VelocityContainer mainVc;

	public EPMapViewController(UserRequest ureq, WindowControl control, PortfolioStructureMap initialMap, boolean back,
			EPSecurityCallback secCallback) {
		super(ureq, control);
		this.map = initialMap;
		this.back = back;
		this.secCallback = secCallback;
		
		mainVc = createVelocityContainer("mapview");

		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		
		// if this is a structured map (assigned from a template) do a sync first
		if (map instanceof EPStructuredMap && (map.getStatus() == null || !map.getStatus().equals(StructureStatusEnum.CLOSED) )){
			boolean syncOk = ePFMgr.synchronizeStructuredMapToUserCopy(map);
			if (syncOk) showInfo("synced.map.success");
			map = (PortfolioStructureMap) ePFMgr.loadPortfolioStructureByKey(map.getKey());
		}
		
		if(EPSecurityCallbackFactory.isLockNeeded(secCallback)) {
			lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(initialMap, ureq.getIdentity(), "mmp");
			if(!lockEntry.isSuccess()) {
				this.secCallback = EPSecurityCallbackFactory.updateAfterFailedLock(secCallback);
				showWarning("map.already.edited");
			}
		}
		
		if(initialMap instanceof EPStructuredMapTemplate) {
			boolean inUse = ePFMgr.isTemplateInUse(initialMap, null, null, null);
			if(inUse) {
				showWarning("template.alreadyInUse");
			}
		}
		
		initForm(ureq);
		putInitialPanel(mainVc);
	}


	protected void initForm(UserRequest ureq) {
		mainVc.contextPut("map", map);
		mainVc.contextPut("style", ePFMgr.getValidStyleName(map));
		
		Boolean editMode = editButton == null ? Boolean.FALSE : (Boolean)editButton.getUserObject();
		mainVc.remove(mainVc.getComponent("map.editButton"));
		if(secCallback.canEditStructure()) {
			editButton = LinkFactory.createButton("map.editButton", mainVc, this);
			if(Boolean.FALSE.equals(editMode)) {
				editButton.setCustomDisplayText(translate("map.editButton.on"));
			} else {
				editButton.setCustomDisplayText(translate("map.editButton.off"));
			}
			editButton.setUserObject(editMode);
		} 
		if(back) {
			backLink = LinkFactory.createLinkBack(mainVc, this);
		}
		mainVc.remove(mainVc.getComponent("map.submit.assess"));
		if(secCallback.canSubmitAssess() && !StructureStatusEnum.CLOSED.equals(map.getStatus())) {
			submitAssessLink = LinkFactory.createButtonSmall("map.submit.assess", mainVc, this);
		}
		
		if(map instanceof EPStructuredMap) {
			EPTargetResource resource = ((EPStructuredMap)map).getTargetResource();
			RepositoryEntry repoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(resource.getOLATResourceable(), false);
			if(repoEntry != null) {
				mainVc.contextPut("courseName", repoEntry.getDisplayname());
				String url = Settings.getServerContextPathURI();
				url += "/url/RepositoryEntry/" + repoEntry.getKey() + "/CourseNode/" + resource.getSubPath();
				mainVc.contextPut("courseLink", url);
			}
		}
	
		mainVc.remove(mainVc.getComponent("addButton"));
		if(secCallback.canAddPage() && !StructureStatusEnum.CLOSED.equals(map.getStatus())) {
			EPAddElementsController addButton = new EPAddElementsController(ureq, getWindowControl(), map);
			if(secCallback.canAddPage()) {
				addButton.setShowLink(EPAddElementsController.ADD_PAGE);
			}
			mainVc.put("addButton", addButton.getInitialComponent());
			listenTo(addButton);
		} 
		mainVc.contextPut("closed", Boolean.valueOf((StructureStatusEnum.CLOSED.equals(map.getStatus()))));
		
		List<PortfolioStructure> pageList = ePFMgr.loadStructureChildren(map);
		if (pageList!=null && pageList.size() != 0) {
			// prepare to paint pages also
			removeAsListenerAndDispose(pageCtrl);
			pageCtrl = new EPMultiplePageController(ureq, getWindowControl(), pageList, secCallback);
			mainVc.put("pagesCtrl", pageCtrl.getInitialComponent());
			listenTo(pageCtrl);
		} else if (mainVc.getComponent("pagesCtrl")!=null){
			mainVc.remove(mainVc.getComponent("pagesCtrl"));
		}	
	}


	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.components.form.flexible.FormItem, org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@SuppressWarnings("unused")
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == editButton){
			removeAsListenerAndDispose(editCtrl);
			if (Boolean.FALSE.equals(editButton.getUserObject())){
				PortfolioStructure selectedPage = null;
				if(pageCtrl != null) {
					selectedPage = pageCtrl.getSelectedPage();
				}
				initOrUpdateEditMode(ureq, selectedPage);
			} else {
				mainVc.remove(editCtrl.getInitialComponent());
				PortfolioStructure currentEditedStructure = editCtrl.getSelectedStructure();
				initForm(ureq);
				editButton.setUserObject(Boolean.FALSE);
				editButton.setCustomDisplayText(translate("map.editButton.on"));
				if(currentEditedStructure != null && pageCtrl != null) {
					EPPage page = getSelectedPage(currentEditedStructure);
					if(page != null) {
						pageCtrl.selectPage(ureq, page);
					}
				}
			}
		} else if(source == backLink) {
			fireEvent(ureq, new EPMapEvent(EPStructureEvent.CLOSE, map));
		} else if(source == submitAssessLink) {
			submitAssess(ureq);
		} 
	}
	
	private EPPage getSelectedPage(PortfolioStructure structure) {
		PortfolioStructure current = structure;
		
		do {
			if(current instanceof EPPage) {
				return (EPPage)current;
			}
			current = current.getRoot();
		} while (current != null);

		return null;
	}
	
	protected void submitAssess(UserRequest ureq) {
		if(ePFMgr.checkCollectRestrictionOfMap(map)) {
			String title = translate("map.submit.assess.title");
			String text = translate("map.submit.assess.description");
			confirmationSubmissionCtr = activateYesNoDialog(ureq, title, text, confirmationSubmissionCtr);
		} else {
			String title = translate("map.submit.assess.restriction.error.title");
			String[] stats = ePFMgr.getRestrictionStatisticsOfMap(map);
			String text = translate("map.submit.assess.restriction.error.description") + "<br/>" +  translate("map.submit.assess.restriction.error.hint", stats);
			confirmationSubmissionCtr = activateYesNoDialog(ureq, title, text, confirmationSubmissionCtr);
			confirmationSubmissionCtr.setCssClass("b_warning_icon");
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (event instanceof EPStructureChangeEvent && event.getCommand().equals(EPStructureChangeEvent.ADDED)){
			EPStructureChangeEvent changeEvent = (EPStructureChangeEvent)event;
			PortfolioStructure structure = changeEvent.getPortfolioStructure();
//			don't do reloadMapAndRefreshUI(ureq) here; no db-commit yet! no refresh -> stale object!
			if(structure instanceof EPPage) {
				//jump to the edit mode for new pages
				initOrUpdateEditMode(ureq, structure);
			}
		} else if(event instanceof EPStructureEvent && event.getCommand().equals(EPStructureEvent.CHANGE)) {
		// reload map
			reloadMapAndRefreshUI(ureq);
		} else if (source == editCtrl && event.equals(Event.CHANGED_EVENT)) {
			// refresh view on changes in TOC or style
			reloadMapAndRefreshUI(ureq);
			PortfolioStructure selectedPage = editCtrl.getSelectedStructure();
			initOrUpdateEditMode(ureq, selectedPage);
		} else if (source == confirmationSubmissionCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				ePFMgr.submitMap(map);
				secCallback = EPSecurityCallbackFactory.getSecurityCallback(ureq, map, ePFMgr);
				fireEvent(ureq, new EPMapEvent(EPStructureEvent.SUBMIT, map));
				mainVc.remove(mainVc.getComponent("editor")); // switch back to non-edit mode
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(map));
				ThreadLocalUserActivityLogger.log(EPLoggingAction.EPORTFOLIO_TASK_FINISHED, getClass());
				reloadMapAndRefreshUI(ureq);
			}
		} 
		
		fireEvent(ureq, event); // fire to multiple maps controller, so it can refresh itself!
	}
	
	private void initOrUpdateEditMode(UserRequest ureq, PortfolioStructure startStruct) {
		removeAsListenerAndDispose(editCtrl);
		editCtrl = new EPStructureTreeAndDetailsEditController(ureq, getWindowControl(), startStruct, map, secCallback);
		mainVc.put("editor", editCtrl.getInitialComponent());
		listenTo(editCtrl);
		editButton.setUserObject(Boolean.TRUE);
		editButton.setCustomDisplayText(translate("map.editButton.off"));		
	}
	
	private void reloadMapAndRefreshUI(UserRequest ureq){
		this.map = (PortfolioStructureMap) ePFMgr.loadPortfolioStructureByKey(map.getKey());
		initForm(ureq);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		if(lockEntry != null) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
	}
}
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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
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
import org.olat.user.DisplayPortraitController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * shows a map itself with containing pages, structures, etc. 
 * 
 * <P>
 * Initial Date: 04.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPMapViewController extends BasicController implements Activateable2 {

	private Link editButton, backLink, submitAssessLink;
	private final VelocityContainer mainVc;
	
	private EPMultiplePageController pageCtrl;
	private EPStructureTreeAndDetailsEditController editCtrl;
	private DialogBoxController confirmationSubmissionCtr;
	private final boolean back;
	
	private EditMode editMode = EditMode.view;
	private PortfolioStructureMap map;
	private EPSecurityCallback secCallback;
	private LockResult lockEntry;
	
	@Autowired
	private EPFrontendManager ePFMgr;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CoordinatorManager coordinatorManager;

	public EPMapViewController(UserRequest ureq, WindowControl control, PortfolioStructureMap initialMap, boolean back,
			boolean preview, EPSecurityCallback secCallback) {
		super(ureq, control);
		this.map = initialMap;
		this.back = back;
		this.secCallback = secCallback;
		
		mainVc = createVelocityContainer("mapview");
		
		// if this is a structured map (assigned from a template) do a sync first
		if (map instanceof EPStructuredMap && (map.getStatus() == null || !map.getStatus().equals(StructureStatusEnum.CLOSED) )){
			map = (PortfolioStructureMap) ePFMgr.loadPortfolioStructureByKey(map.getKey());
			boolean syncOk = ePFMgr.synchronizeStructuredMapToUserCopy(map);
			if (syncOk) {
				showInfo("synced.map.success");
			} else if(map == null) {
				showWarning("synced.map.deleted");
				putInitialPanel(createVelocityContainer("map_deleted"));
				return;
			} else {
				showError("synced.map.error");
				
			}
		}
		
		if(EPSecurityCallbackFactory.isLockNeeded(secCallback)) {
			lockEntry = coordinatorManager.getCoordinator().getLocker().acquireLock(initialMap, ureq.getIdentity(), "mmp");
			if(!lockEntry.isSuccess()) {
				this.secCallback = EPSecurityCallbackFactory.updateAfterFailedLock(secCallback);
				showWarning("map.already.edited");
			}
		}
		
		//don't show the message for preview
		if(initialMap instanceof EPStructuredMapTemplate && !preview) {
			boolean inUse = ePFMgr.isTemplateInUse(initialMap, null, null, null);
			if(inUse) {
				showWarning("template.alreadyInUse");
			}
		}
		
		initForm(ureq);
		putInitialPanel(mainVc);
	}
	
	public boolean canEditStructure() {
		return secCallback.canEditStructure();
	}
	
	protected void initForm(UserRequest ureq) {
		Identity ownerIdentity = ePFMgr.getFirstOwnerIdentity(map);
		if(ownerIdentity != null) {
			DisplayPortraitController portraitCtr = new DisplayPortraitController(ureq, getWindowControl(), ownerIdentity, false,true,true,false);
			mainVc.put("ownerportrait", portraitCtr.getInitialComponent());
		}
		
		mainVc.contextPut("map", map);
		mainVc.contextPut("style", ePFMgr.getValidStyleName(map));
		
		mainVc.remove(mainVc.getComponent("map.editButton"));
		if(secCallback.canEditStructure()) {
			editButton = LinkFactory.createButton("map.editButton", mainVc, this);
			editButton.setElementCssClass("o_sel_ep_edit_map");
			editButton.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			if(editMode == EditMode.view) {
				editButton.setCustomDisplayText(translate("map.editButton.on"));
			} else {
				editButton.setCustomDisplayText(translate("map.editButton.off"));
			}
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
			RepositoryEntry repoEntry = repositoryManager.lookupRepositoryEntry(resource.getOLATResourceable(), false);
			if(repoEntry != null) {
				mainVc.contextPut("courseName", StringHelper.escapeHtml(repoEntry.getDisplayname()));
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
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == editButton){
			toogleEditMode(ureq);
		} else if(source == backLink) {
			fireEvent(ureq, new EPMapEvent(EPStructureEvent.CLOSE, map));
		} else if(source == submitAssessLink) {
			if (preCheckMapSubmit()){
				doConfirmSubmitAssess(ureq);
			} else {
				showWarning("map.cannot.submit.nomore.coursenode");
			}
		} 
	}
	
	private void toogleEditMode(UserRequest ureq) {
		removeAsListenerAndDispose(editCtrl);
		if (editMode == EditMode.view){
			edit(ureq);
		} else {
			view(ureq);
		}
	}
	
	public void view(UserRequest ureq) {
		PortfolioStructure currentEditedStructure = null;
		if(editCtrl != null) {
			removeAsListenerAndDispose(editCtrl);
			mainVc.remove(editCtrl.getInitialComponent());
			currentEditedStructure = editCtrl.getSelectedStructure();
		}
		initForm(ureq);
		editMode = EditMode.view;
		if(editButton != null) {
			editButton.setCustomDisplayText(translate("map.editButton.on"));
		}
		if(currentEditedStructure != null && pageCtrl != null) {
			EPPage page = getSelectedPage(currentEditedStructure);
			if(page != null) {
				pageCtrl.selectPage(ureq, page);
				addToHistory(ureq, page, null);
			}
		}
	}
	
	public void edit(UserRequest ureq) {
		if(canEditStructure()) {
			removeAsListenerAndDispose(editCtrl);
			PortfolioStructure selectedPage = null;
			if(pageCtrl != null) {
				selectedPage = pageCtrl.getSelectedPage();
			}
			initOrUpdateEditMode(ureq, selectedPage);
			editMode = EditMode.editor;
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		pageCtrl.activate(ureq, entries, state);
	}
	
	public PortfolioStructureMap getMap() {
		return map;
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
	
	private boolean preCheckMapSubmit(){
		EPStructuredMap submittedMap = (EPStructuredMap) map;
		try {
			EPTargetResource resource = submittedMap.getTargetResource();
			OLATResourceable courseOres = resource.getOLATResourceable();
			ICourse course = CourseFactory.loadCourse(courseOres);
			CourseNode courseNode = course.getRunStructure().getNode(resource.getSubPath());
			if (courseNode==null) return false;
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private void doConfirmSubmitAssess(UserRequest ureq) {
		if(ePFMgr.checkCollectRestrictionOfMap(map)) {
			String title = translate("map.submit.assess.title");
			String text = translate("map.submit.assess.description");
			confirmationSubmissionCtr = activateYesNoDialog(ureq, title, text, confirmationSubmissionCtr);
		} else {
			String title = translate("map.submit.assess.restriction.error.title");
			String[] stats = ePFMgr.getRestrictionStatisticsOfMap(map);
			String text = translate("map.submit.assess.restriction.error.description") + "<br/>" +  translate("map.submit.assess.restriction.error.hint", stats);
			confirmationSubmissionCtr = activateYesNoDialog(ureq, title, text, confirmationSubmissionCtr);
			confirmationSubmissionCtr.setCssClass("o_icon_warn");
		}
	}
	
	private void doSubmitAssess(UserRequest ureq) {
		ePFMgr.submitMap(map);
		secCallback = EPSecurityCallbackFactory.getSecurityCallback(ureq, map, ePFMgr);
		fireEvent(ureq, new EPMapEvent(EPStructureEvent.SUBMIT, map));
		mainVc.remove(mainVc.getComponent("editor")); // switch back to non-edit mode
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(map));
		ThreadLocalUserActivityLogger.log(EPLoggingAction.EPORTFOLIO_TASK_FINISHED, getClass());
		reloadMapAndRefreshUI(ureq);
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
				doSubmitAssess(ureq);
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
			coordinatorManager.getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
	}
	
	private enum EditMode {
		view,
		editor
	}
}
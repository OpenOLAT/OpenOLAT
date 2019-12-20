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

import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.EPUIFactory;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.restriction.CollectRestriction;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.model.structel.StructureStatusEnum;
import org.olat.portfolio.ui.artefacts.view.EPMultiArtefactsController;
import org.olat.portfolio.ui.structel.edit.EPCollectRestrictionResultController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * View the content of a page ( structure / artefacts)
 * 
 * <P>
 * Initial Date:  23.08.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPPageViewController extends BasicController {

	private EPPage page;
	private PortfolioStructure map;
	
	private final VelocityContainer vC;
	private final EPSecurityCallback secCallback;
	
	private EPAddElementsController addButtonCtrl;
	private EPCollectRestrictionResultController resultCtrl;
	private UserCommentsAndRatingsController commentsAndRatingCtr;
	
	@Autowired
	private EPFrontendManager ePFMgr;

	public EPPageViewController(UserRequest ureq, WindowControl wControl, PortfolioStructure map, EPPage page, boolean withComments,
			EPSecurityCallback secCallback) {
		super(ureq, wControl);
		vC = createVelocityContainer("pageView");
		this.map = map;
		this.page = page;
		this.secCallback = secCallback;


		init(ureq);
		
		if(withComments && commentsAndRatingCtr != null) {
			commentsAndRatingCtr.expandComments(ureq);
		}

		putInitialPanel(vC);
	}
	
	public EPPage getPage() {
		return page;
	}
	
	private void init(UserRequest ureq) {
		vC.contextPut("page", page);
		PortfolioStructureMap parentOfPage = (PortfolioStructureMap)ePFMgr.loadStructureParent(page);
		boolean parentMapClosed = StructureStatusEnum.CLOSED.equals(parentOfPage.getStatus());
		
		vC.remove(vC.getComponent("checkResults"));
		if(secCallback.isRestrictionsEnabled()) {
			removeAsListenerAndDispose(resultCtrl);
			List<CollectRestriction> restrictions = page.getCollectRestrictions();
			if(!restrictions.isEmpty()) {
				boolean check = ePFMgr.checkCollectRestriction(page);
				resultCtrl = new EPCollectRestrictionResultController(ureq, getWindowControl());
				resultCtrl.setMessage(restrictions, check);
				vC.put("checkResults", resultCtrl.getInitialComponent());
				listenTo(resultCtrl);
			}
		}

		vC.remove(vC.getComponent("artefacts"));
		List<AbstractArtefact> artefacts = ePFMgr.getArtefacts(page);
		if (artefacts.size() != 0) {
			EPMultiArtefactsController artefactCtrl = 
				EPUIFactory.getConfigDependentArtefactsControllerForStructure(ureq, getWindowControl(), artefacts, page, secCallback);
			vC.put("artefacts", artefactCtrl.getInitialComponent());
			listenTo(artefactCtrl);
		}
		
		vC.remove(vC.getComponent("structElements"));
		List<PortfolioStructure> structElements = ePFMgr.loadStructureChildren(page);
		if (structElements.size() != 0) {
			EPStructureElementsController structElCtrl = new EPStructureElementsController(ureq, getWindowControl(),
					structElements, secCallback, parentMapClosed);
			vC.put("structElements", structElCtrl.getInitialComponent());
			listenTo(structElCtrl);
		}
		
		vC.remove(vC.getComponent("addButton"));
		if(!parentMapClosed && (secCallback.canAddArtefact() || secCallback.canAddStructure())) {
			addButtonCtrl = new EPAddElementsController(ureq, getWindowControl(), page);
			listenTo(addButtonCtrl);
			if(secCallback.canAddArtefact()) {
				addButtonCtrl.setShowLink(EPAddElementsController.ADD_ARTEFACT);
			}
			if(secCallback.canAddStructure()) {
				addButtonCtrl.setShowLink(EPAddElementsController.ADD_STRUCTUREELEMENT);
			}
			vC.put("addButton", addButtonCtrl.getInitialComponent());
		}
		
		vC.remove(vC.getComponent("commentCtrl"));
		if(secCallback.canCommentAndRate()) {
			removeAsListenerAndDispose(commentsAndRatingCtr);	

			boolean anonym = ureq.getUserSession().getRoles().isGuestOnly();
			CommentAndRatingSecurityCallback ratingSecCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, anonym);
			commentsAndRatingCtr = new UserCommentsAndRatingsController(ureq, getWindowControl(), map.getOlatResource(), page.getKey().toString(), ratingSecCallback, null, true, true, true);
			listenTo(commentsAndRatingCtr);
			vC.put("commentCtrl", commentsAndRatingCtr.getInitialComponent());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (event instanceof EPStructureChangeEvent){
			this.page = (EPPage) ePFMgr.reloadPortfolioStructure(page);
			init(ureq);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}

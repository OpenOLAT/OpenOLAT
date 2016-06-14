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
package org.olat.portfolio.ui.artefacts.collect;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.portfolio.EPArtefactHandler;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Entry point to the collection wizzard. 
 * 
 * <P>
 * Initial Date: 11.06.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class ArtefactWizzardStepsController extends BasicController {

	private Controller collectStepsCtrl;
	
	private Link addLink;
	AbstractArtefact artefact;
	private OLATResourceable ores;
	private String businessPath;
	private VFSContainer tmpFolder = null;
	
	@Autowired
	private EPFrontendManager ePFMgr;
	@Autowired
	private PortfolioModule portfolioModule;

	public ArtefactWizzardStepsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		EPArtefactHandler<?> handler = portfolioModule.getArtefactHandler("Forum");
		AbstractArtefact newArtefact = handler.createArtefact();
		this.artefact = newArtefact;

		initCollectionStepWizzard(ureq);
		Panel emptyItself = new Panel("emptyItself");
		putInitialPanel(emptyItself);
	}

	/**
	 * to be used to manipulate with the wizzard on an already existing artefact.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param artefact
	 */
	public ArtefactWizzardStepsController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, VFSContainer tmpFolder) {
		super(ureq, wControl);
		
		this.artefact = artefact;
		this.tmpFolder  = tmpFolder;

		initCollectionStepWizzard(ureq);
		Panel emptyItself = new Panel("emptyItself");
		putInitialPanel(emptyItself);
	}

	/**
	 * !! you should not use this constructor directly !!
	 * intention would be to use the EPUIFactory instead. like this the collect-links are hidden, if ePortfolio is disabled!
	 * the use of the EPUIFactory is not yet possible in all places in OLAT (sometimes a businesspath is missing).  
	 * @param ureq
	 * @param wControl
	 * @param ores
	 * @param subPath
	 * @param businessPath
	 */
	public ArtefactWizzardStepsController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, String businessPath) {
		super(ureq, wControl);
		this.ores = ores;
		this.businessPath = businessPath;
		initCollectLinkVelocity();
	}
	
	/**
	 * !! you should not use this constructor directly !!
	 * intention would be to use the EPUIFactory instead. like this the collect-links are hidden, if ePortfolio is disabled!
	 * the use of the EPUIFactory is not yet possible in all places in OLAT (sometimes a businesspath is missing).
	 * @param ureq
	 * @param wControl
	 * @param artefact
	 */
	public ArtefactWizzardStepsController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact) {
		super(ureq, wControl);
		this.artefact = artefact;
		this.businessPath = artefact.getBusinessPath();
		initCollectLinkVelocity();
	}
	
	public ArtefactWizzardStepsController(UserRequest ureq, WindowControl wControl, int numOfArtefacts,
			OLATResourceable ores, String businessPath) {
		super(ureq, wControl);
		this.ores = ores;
		this.businessPath = businessPath;
		initCollectLinkVelocity(numOfArtefacts);
	}
	
	private void initCollectLinkVelocity() {
		List<AbstractArtefact> existingArtefacts = ePFMgr.loadArtefactsByBusinessPath(businessPath, getIdentity());
		int numOfArtefacts = existingArtefacts == null ? 0 : existingArtefacts.size();
		initCollectLinkVelocity(numOfArtefacts);
	}

	private void initCollectLinkVelocity(int numOfArtefacts) {
		addLink = LinkFactory.createCustomLink("add.to.eportfolio", "add.to.eportfolio", "", Link.LINK_CUSTOM_CSS + Link.NONTRANSLATED,
				null, this);
		addLink.setCustomEnabledLinkCSS("o_eportfolio_add");
		addLink.setIconLeftCSS("o_icon o_icon-lg o_icon_eportfolio_add");
		addLink.setTooltip(translate("add.to.eportfolio"));
		addLink.setTranslator(getTranslator());
		if (numOfArtefacts > 0) {
			addLink.setIconLeftCSS("o_icon o_icon-lg o_icon_eportfolio_add");
			addLink.setCustomEnabledLinkCSS("o_eportfolio_add_again");
			addLink.setTooltip(translate("add.to.eportfolio.again", String.valueOf(numOfArtefacts)));	
		}
		putInitialPanel(addLink);
		getInitialComponent().setSpanAsDomReplaceable(true); // special case since controller is actually just a link 
	}
	
	/**
	 * @param ores
	 * @param businessPath
	 */
	private void prepareNewArtefact() {
		EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(ores.getResourceableTypeName());
		AbstractArtefact artefact1 = artHandler.createArtefact();
		artefact1.setAuthor(getIdentity());
		artefact1.setCollectionDate(new Date());
		artefact1.setBusinessPath(businessPath);
		artHandler.prefillArtefactAccordingToSource(artefact1, ores);
		this.artefact = artefact1;
	}



	private void initCollectionStepWizzard(UserRequest ureq) {
		if (artefact == null && ores != null) prepareNewArtefact();
		Step start = new EPCollectStep00(ureq, artefact);
		StepRunnerCallback finish = new EPArtefactWizzardStepCallback(tmpFolder);
		collectStepsCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("collect.wizzard.title"), "o_sel_artefact_add_wizard");
		listenTo(collectStepsCtrl);
		getWindowControl().pushAsModalDialog(collectStepsCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == addLink) {
			// someone triggered the 'add to my portfolio' workflow by its link
			artefact = null; // always collect a new artefact
			initCollectionStepWizzard(ureq);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == collectStepsCtrl) {
			if (event == Event.CHANGED_EVENT) {
				ePFMgr.updateArtefact(artefact);
				showInfo("collect.success", StringHelper.escapeHtml(artefact.getTitle()));
			} else {
				// set back artefact-values
				// artefact = ePFMgr.loadArtefact(artefact.getKey());
			}
			// cancel / done event means no data change but close wizzard and fwd
			// event
			getWindowControl().pop();
			fireEvent(ureq, event);
		}
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}

}

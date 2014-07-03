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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.webFeed.portfolio.EPCreateLiveBlogArtefactStep00;
import org.olat.modules.webFeed.portfolio.LiveBlogArtefact;
import org.olat.portfolio.EPArtefactHandler;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.artefacts.EPTextArtefact;
import org.olat.portfolio.model.artefacts.FileArtefact;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * overlay controller to hold some links for different kind of adding artefacts.
 * - triggers further workflows to add artefact
 * 
 * fires an Done-Event when an artefact was added
 * <P>
 * Initial Date: 26.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPAddArtefactController extends BasicController {

	private Link uploadBtn, liveBlogBtn;
	private VelocityContainer addPage;
	private Link textBtn, addBtn;
	private StepsMainRunController collectStepsCtrl;
	
	private VFSContainer vfsTemp;
	private VelocityContainer addLinkVC;
	private CloseableCalloutWindowController calloutCtr;
	
	@Autowired
	private EPFrontendManager ePFMgr;
	@Autowired
	private PortfolioModule portfolioModule;
	
	private PortfolioStructure preSelectedStruct;

	public EPAddArtefactController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		addLinkVC = createVelocityContainer("addLink");
		addBtn = LinkFactory.createButton("add.artefact", addLinkVC, this);
		addBtn.setElementCssClass("o_sel_add_artfeact");
		putInitialPanel(addLinkVC);
	}

	private void initAddPageVC(){
		addPage = createVelocityContainer("addpanel");
		EPArtefactHandler<?> textHandler = portfolioModule.getArtefactHandler(EPTextArtefact.TEXT_ARTEFACT_TYPE);
		if (textHandler != null && textHandler.isEnabled()) {
			textBtn = LinkFactory.createLink("add.text.artefact", addPage, this);
			textBtn.setElementCssClass("o_sel_add_text_artfeact");
		}
		EPArtefactHandler<?> fileHandler = portfolioModule.getArtefactHandler(FileArtefact.FILE_ARTEFACT_TYPE);
		if (fileHandler != null && fileHandler.isEnabled()) {
			uploadBtn = LinkFactory.createLink("add.artefact.upload", addPage, this);
			uploadBtn.setElementCssClass("o_sel_add_upload_artfeact");
		}
		EPArtefactHandler<?> liveblogHandler = portfolioModule.getArtefactHandler(LiveBlogArtefact.TYPE);
		if (liveblogHandler != null && liveblogHandler.isEnabled()) {
			liveBlogBtn = LinkFactory.createLink("add.artefact.liveblog", addPage, this);
			liveBlogBtn.setCustomDisplayText(translate("add.artefact.blog"));
			liveBlogBtn.setElementCssClass("o_sel_add_liveblog_artfeact");
		}
	}
	
	private void initAddLinkPopup(UserRequest ureq) {
		if (addPage == null) initAddPageVC();
		String title = translate("add.artefact");
		
		removeAsListenerAndDispose(calloutCtr);
		calloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(), addPage, addBtn, title, true, null);
		listenTo(calloutCtr);
		calloutCtr.activate();
	}
	
	public PortfolioStructure getPreSelectedStruct() {
		return preSelectedStruct;
	}

	public void setPreSelectedStruct(PortfolioStructure preSelectedStruct) {
		this.preSelectedStruct = preSelectedStruct;
	}

	private void closeAddLinkPopup(){
		if (calloutCtr != null) {
			calloutCtr.deactivate();
			removeAsListenerAndDispose(calloutCtr);
			calloutCtr = null;
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == addBtn) {
			if (calloutCtr==null){
				initAddLinkPopup(ureq);
				addBtn.setDirty(false);
			} else {
				closeAddLinkPopup();
			}
		} else {
			// close on all clicked links in the popup
			closeAddLinkPopup();
			if (source == textBtn) {
				prepareNewTextArtefactWizzard(ureq);
			} else if (source == uploadBtn) {
				prepareFileArtefactWizzard(ureq);
			} else if (source == liveBlogBtn) {
				prepareNewLiveBlogArtefactWizzard(ureq);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == collectStepsCtrl && event == Event.CANCELLED_EVENT) {
			disposeTempDir();
			getWindowControl().pop();
			removeAsListenerAndDispose(collectStepsCtrl);
		}
		if (source == collectStepsCtrl && event == Event.CHANGED_EVENT) {
			getWindowControl().pop();
			removeAsListenerAndDispose(collectStepsCtrl);
			
			// manually dispose temp vfsContainer here :: FXOLAT-386 
			// this EPAddArtefactController gets disposed "too late" 
			//(vfsTemp can change inbetween, so only the last one get's deleted)
			disposeTempDir();
			showInfo("collect.success.text.artefact");
			fireEvent(ureq, Event.DONE_EVENT);
		} 
		if (source == calloutCtr && event == CloseableCalloutWindowController.CLOSE_WINDOW_EVENT) {
			removeAsListenerAndDispose(calloutCtr);
			calloutCtr = null;
		}
	}

	/**
	 * prepare a new text artefact and open with wizzard initialized with a
	 * special first step for text-artefacts
	 * 
	 * @param ureq
	 */
	private void prepareNewTextArtefactWizzard(UserRequest ureq) {
		EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(EPTextArtefact.TEXT_ARTEFACT_TYPE);
		AbstractArtefact artefact1 = artHandler.createArtefact();
		artefact1.setAuthor(getIdentity());
		artefact1.setSource(translate("text.artefact.source.info"));
		artefact1.setCollectionDate(new Date());
		artefact1.setSignature(-20);

		vfsTemp = ePFMgr.getArtefactsTempContainer(getIdentity());
		Step start = new EPCreateTextArtefactStep00(ureq, artefact1, preSelectedStruct, vfsTemp);
		StepRunnerCallback finish = new EPArtefactWizzardStepCallback(vfsTemp);
		collectStepsCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("create.text.artefact.wizzard.title"), "o_sel_artefact_add_wizard o_sel_artefact_add_text_wizard");
		listenTo(collectStepsCtrl);
		getWindowControl().pushAsModalDialog(collectStepsCtrl.getInitialComponent());
	}

	/**
	 * prepare a file artefact and open with wizzard initialized with a special
	 * first step for file-artefacts
	 * 
	 * @param ureq
	 */
	private void prepareFileArtefactWizzard(UserRequest ureq) {
		EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(FileArtefact.FILE_ARTEFACT_TYPE);
		AbstractArtefact artefact1 = artHandler.createArtefact();
		artefact1.setAuthor(getIdentity());
		artefact1.setSource(translate("file.artefact.source.info"));
		artefact1.setCollectionDate(new Date());
		artefact1.setSignature(-30);

		vfsTemp = ePFMgr.getArtefactsTempContainer(getIdentity());
		Step start = new EPCreateFileArtefactStep00(ureq, artefact1, preSelectedStruct, vfsTemp);
		StepRunnerCallback finish = new EPArtefactWizzardStepCallback(vfsTemp);
		collectStepsCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("create.file.artefact.wizzard.title"), "o_sel_artefact_add_wizard o_sel_artefact_add_file_wizard");
		listenTo(collectStepsCtrl);
		getWindowControl().pushAsModalDialog(collectStepsCtrl.getInitialComponent());
	}
	
	private void prepareNewLiveBlogArtefactWizzard(UserRequest ureq) {
		EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(LiveBlogArtefact.TYPE);
		AbstractArtefact artefact1 = artHandler.createArtefact();
		artefact1.setAuthor(getIdentity());
		artefact1.setCollectionDate(new Date());
		artefact1.setSignature(60); // preset as signed by 60%

		Step start = new EPCreateLiveBlogArtefactStep00(ureq, preSelectedStruct, artefact1);
		StepRunnerCallback finish = new EPArtefactWizzardStepCallback(); // no vfsTemp!, blog doesn't need a directory
		collectStepsCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("create.blog.artefact.wizzard.title"), "o_sel_artefact_add_wizard o_sel_artefact_add_blog_wizard");
		listenTo(collectStepsCtrl);
		getWindowControl().pushAsModalDialog(collectStepsCtrl.getInitialComponent());
	}

	/**
	 * FXOLAT-386
	 * disposed the temp vfsContainer from a file Artefact upload
	 */
	private void disposeTempDir(){
		if(vfsTemp != null ) {
			vfsTemp.delete();
			vfsTemp = null;
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		disposeTempDir();
	}

}

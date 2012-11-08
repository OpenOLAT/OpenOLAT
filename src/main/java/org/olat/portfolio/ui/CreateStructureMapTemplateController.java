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
package org.olat.portfolio.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPStructureManager;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.ui.structel.EPCreateMapController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;
import org.olat.resource.OLATResource;

/**
 * 
 * Description:<br>
 * This is the controller which create a new template in the repository via the repository
 * 
 * <P>
 * Initial Date:  12 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CreateStructureMapTemplateController extends BasicController implements IAddController {
	private OLATResource templateOres;
	private final EPStructureManager eSTMgr;
	private boolean isNewTemplateAndGetsAPage = false; 

	/**
	 * Constructor
	 * 
	 * @param addCallback
	 * @param ureq
	 * @param wControl
	 */
	public CreateStructureMapTemplateController(RepositoryAddCallback addCallback, UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		eSTMgr = (EPStructureManager) CoreSpringFactory.getBean("epStructureManager");
		if (addCallback != null) {
			// create a new template
			isNewTemplateAndGetsAPage = true;
			templateOres = eSTMgr.createPortfolioMapTemplateResource();
			addCallback.setDisplayName(translate(templateOres.getResourceableTypeName()));
			addCallback.setResourceable(templateOres);
			addCallback.setResourceName(translate("EPStructuredMapTemplate"));
			addCallback.finished(ureq);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// Nothing to dispose
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Nothing to catch
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#getTransactionComponent()
	 */
	public Component getTransactionComponent() {
		return getInitialComponent();
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#repositoryEntryCreated(org.olat.repository.RepositoryEntry)
	 */
	public void repositoryEntryCreated(RepositoryEntry re) {
		PortfolioStructureMap mapTemp = eSTMgr.createAndPersistPortfolioMapTemplateFromEntry(getIdentity(), re);
		// add a page, as each map should have at least one per default!
		Translator pt = Util.createPackageTranslator(EPCreateMapController.class, getLocale(), getTranslator());
		String title = pt.translate("new.page.title");
		String description = pt.translate("new.page.desc");
		if (isNewTemplateAndGetsAPage){ // no additional page when this is a copy.
			EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
			ePFMgr.createAndPersistPortfolioPage(mapTemp, title, description);
		}
	}
	
	@Override
	public void repositoryEntryCopied(RepositoryEntry sourceEntry, RepositoryEntry newEntry) {
		//
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#transactionAborted()
	 */
	public void transactionAborted() {
		//nothing persisted
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#transactionFinishBeforeCreate()
	 */
	public boolean transactionFinishBeforeCreate() {
		return true;
	}
}
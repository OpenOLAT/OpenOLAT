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
package org.olat.repository.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.EPSecurityCallbackFactory;
import org.olat.portfolio.EPTemplateMapResource;
import org.olat.portfolio.EPUIFactory;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPXStreamHandler;
import org.olat.portfolio.model.structel.EPAbstractMap;
import org.olat.portfolio.model.structel.EPStructuredMapTemplate;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.ui.CreateStructureMapTemplateController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;
import org.olat.repository.controllers.WizardCloseResourceController;
import org.olat.resource.accesscontrol.ui.RepositoryMainAccessControllerWrapper;
import org.olat.resource.references.ReferenceManager;

import de.bps.onyx.plugin.StreamMediaResource;

/**
 * 
 * Description:<br>
 * Handler wihich allow the portfolio map in repository to be opened and launched.
 * 
 * <P>
 * Initial Date:  12 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
// Loads of parameters are unused
public class PortfolioHandler implements RepositoryHandler {
	private static final OLog log = Tracing.createLoggerFor(PortfolioHandler.class);
	
	public static final String PROCESS_CREATENEW = "create_new";
	public static final String PROCESS_UPLOAD = "upload";
	

	private static final boolean DOWNLOADABLE = false;
	private static final boolean EDITABLE = true;
	private static final boolean LAUNCHABLE = true;
	private static final boolean WIZARD_SUPPORT = false;
	private static final List<String> supportedTypes;

	static { // initialize supported types
		supportedTypes = new ArrayList<String>(1);
		supportedTypes.add(EPTemplateMapResource.TYPE_NAME);
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsDownload()
	 */
	public boolean supportsDownload(RepositoryEntry repoEntry) {
		return DOWNLOADABLE;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsEdit()
	 */
	public boolean supportsEdit(RepositoryEntry repoEntry) {
		return EDITABLE;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsLaunch()
	 */
	public boolean supportsLaunch(RepositoryEntry repoEntry) {
		return LAUNCHABLE;
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsWizard(org.olat.repository.RepositoryEntry)
	 */
	public boolean supportsWizard(RepositoryEntry repoEntry) {
		return WIZARD_SUPPORT;
	}


	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#archive(java.lang.String,
	 *      org.olat.repository.RepositoryEntry)
	 */
	public String archive(Identity archiveOnBehalfOf, String archivFilePath, RepositoryEntry repoEntry) {
		// Apperantly, this method is used for backing up any user related content
		// (comments etc.) on deletion. Up to now, this doesn't exist in blogs.
		return null;
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#readyToDelete(org.olat.core.id.OLATResourceable,
	 *      org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public boolean readyToDelete(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		PortfolioStructure map = ePFMgr.loadPortfolioStructure(res);
		if(map != null) {
			//owner group has its constraints shared beetwen the repository entry and the template
			((EPAbstractMap)map).setOwnerGroup(null);
		}
		if(map instanceof EPStructuredMapTemplate) {
			EPStructuredMapTemplate exercise = (EPStructuredMapTemplate)map;
			if (ePFMgr.isTemplateInUse(exercise, null, null, null)) return false;
		}
		ReferenceManager refM = ReferenceManager.getInstance();
		String referencesSummary = refM.getReferencesToSummary(res, ureq.getLocale());
		if (referencesSummary != null) {
			Translator translator = Util.createPackageTranslator(RepositoryManager.class, ureq.getLocale());
			wControl.setError(translator.translate("details.delete.error.references", new String[] { referencesSummary }));
			return false;
		}		
		return true;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#cleanupOnDelete(org.olat.core.id.OLATResourceable,
	 *      org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public boolean cleanupOnDelete(OLATResourceable res) {
		EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		ePFMgr.deletePortfolioMapTemplate(res);
		return true;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#createCopy(org.olat.core.id.OLATResourceable,
	 *      org.olat.core.gui.UserRequest)
	 */
	public OLATResourceable createCopy(OLATResourceable res, UserRequest ureq) {
		EPFrontendManager ePFMgr = (EPFrontendManager)CoreSpringFactory.getBean("epFrontendManager");
		PortfolioStructure structure = ePFMgr.loadPortfolioStructure(res);
		PortfolioStructure newStructure = EPXStreamHandler.copy(structure);
		PortfolioStructureMap map = ePFMgr.importPortfolioMapTemplate(newStructure, ureq.getIdentity());
		return map.getOlatResource();
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getAddController(org.olat.repository.controllers.RepositoryAddCallback,
	 *      java.lang.Object, org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public IAddController createAddController(RepositoryAddCallback callback, Object userObject, UserRequest ureq, WindowControl wControl) {
		if(PROCESS_CREATENEW.equals(userObject)) {
			return new CreateStructureMapTemplateController(callback, ureq, wControl);
		}
		return new CreateStructureMapTemplateController(null, ureq, wControl);
	}

	/**
	 * Transform the map in a XML file and zip it (Repository export want a zip)
	 * @see org.olat.repository.handlers.RepositoryHandler#getAsMediaResource(org.olat.core.id.OLATResourceable)
	 */
	@Override
	public MediaResource getAsMediaResource(OLATResourceable res, boolean backwardsCompatible) {
		MediaResource mr = null;

		EPFrontendManager ePFMgr = (EPFrontendManager)CoreSpringFactory.getBean("epFrontendManager");
		PortfolioStructure structure = ePFMgr.loadPortfolioStructure(res);
		try {
			InputStream inOut = EPXStreamHandler.toStream(structure);
			mr = new StreamMediaResource(inOut, null, 0l, 0l);
		} catch (IOException e) {
			log.error("Cannot export this map: " + structure, e);
		}
		return mr;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getDetailsComponent(org.olat.core.id.OLATResourceable,
	 *      org.olat.core.gui.UserRequest)
	 */
	public Controller createDetailsForm(UserRequest ureq, WindowControl wControl, OLATResourceable res) {
		return null;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getEditorController(org.olat.core.id.OLATResourceable,
	 *      org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl control) {
		return createLaunchController(re, ureq, control);
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getLaunchController(org.olat.core.id.OLATResourceable,
	 *      java.lang.String, org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, UserRequest ureq,
			WindowControl wControl) {
		EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		PortfolioStructureMap map = (PortfolioStructureMap)ePFMgr.loadPortfolioStructure(re.getOlatResource());
		EPSecurityCallback secCallback = EPSecurityCallbackFactory.getSecurityCallback(ureq, map, ePFMgr);
		Controller epCtr = EPUIFactory.createPortfolioStructureMapController(ureq, wControl, map, secCallback);
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, epCtr.getInitialComponent(), null);
		if(epCtr instanceof Activateable2) {
			layoutCtr.addActivateableDelegate((Activateable2)epCtr);
		}
		layoutCtr.addDisposableChildController(epCtr);
		//fxdiff VCRP-1: access control of learn resources
		RepositoryMainAccessControllerWrapper wrapper = new RepositoryMainAccessControllerWrapper(ureq, wControl, re, layoutCtr);
		return wrapper;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getSupportedTypes()
	 */
	public List<String> getSupportedTypes() {
		return supportedTypes;
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#acquireLock(org.olat.core.id.OLATResourceable,
	 *      org.olat.core.id.Identity)
	 */
	public LockResult acquireLock(OLATResourceable ores, Identity identity) {
		return CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(ores, identity, "subkey");
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#releaseLock(org.olat.core.util.coordinate.LockResult)
	 */
	public void releaseLock(LockResult lockResult) {
		if(lockResult!=null) {
		  CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockResult);
		}
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#isLocked(org.olat.core.id.OLATResourceable)
	 */
	public boolean isLocked(OLATResourceable ores) {
		return CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(ores, "subkey");
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getCreateWizardController(org.olat.core.id.OLATResourceable, org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public Controller createWizardController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		throw new AssertException("Trying to get wizard where no creation wizard is provided for this type.");
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getCloseResourceController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.repository.RepositoryEntry)
	 */
	public WizardCloseResourceController createCloseResourceController(UserRequest ureq, WindowControl control, RepositoryEntry repositoryEntry) {
		// No specific close wizard is implemented.
		throw new AssertException("not implemented");
	}
}

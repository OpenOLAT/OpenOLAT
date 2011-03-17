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
package org.olat.repository.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.jazzlib.ZipEntry;
import net.sf.jazzlib.ZipInputStream;
import net.sf.jazzlib.ZipOutputStream;

import org.hibernate.collection.PersistentList;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.EPSecurityCallbackFactory;
import org.olat.portfolio.EPTemplateMapResource;
import org.olat.portfolio.EPUIFactory;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.restriction.CollectRestriction;
import org.olat.portfolio.model.structel.EPAbstractMap;
import org.olat.portfolio.model.structel.EPDefaultMap;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.EPStructureElement;
import org.olat.portfolio.model.structel.EPStructureToArtefactLink;
import org.olat.portfolio.model.structel.EPStructureToStructureLink;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.EPStructuredMapTemplate;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.ui.CreateStructureMapTemplateController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;
import org.olat.repository.controllers.WizardCloseResourceController;
import org.olat.resource.references.ReferenceManager;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;

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
@SuppressWarnings("unused")
public class PortfolioHandler implements RepositoryHandler {
	private static final OLog log = Tracing.createLoggerFor(PortfolioHandler.class);
	
	public static final String PROCESS_CREATENEW = "create_new";
	public static final String PROCESS_UPLOAD = "upload";
	
	public static XStream myStream = XStreamHelper.createXStreamInstance();

	private static final boolean DOWNLOADABLE = false;
	private static final boolean EDITABLE = true;
	private static final boolean LAUNCHABLE = true;
	private static final boolean WIZARD_SUPPORT = false;
	private static final List<String> supportedTypes;

	static { // initialize supported types
		supportedTypes = new ArrayList<String>(1);
		supportedTypes.add(EPTemplateMapResource.TYPE_NAME);
		
		myStream.alias("defaultMap", EPDefaultMap.class);
		myStream.alias("structureMap", EPStructuredMap.class);
		myStream.alias("templateMap", EPStructuredMapTemplate.class);
		myStream.alias("structure", EPStructureElement.class);
		myStream.alias("page", EPPage.class);
		myStream.alias("structureToArtefact", EPStructureToArtefactLink.class);
		myStream.alias("structureToStructure", EPStructureToStructureLink.class);
		myStream.alias("collectionRestriction", CollectRestriction.class);
		myStream.omitField(EPStructuredMapTemplate.class, "ownerGroup");
		myStream.addDefaultImplementation(PersistentList.class, List.class);
		myStream.addDefaultImplementation(ArrayList.class, List.class);
		myStream.registerConverter(new CollectionConverter(myStream.getMapper()) {
		    public boolean canConvert(Class type) {
		        return PersistentList.class == type;
		    }
		});
		
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
		String stringuified = myStream.toXML(structure);
		PortfolioStructure newStructure = (PortfolioStructure)myStream.fromXML(stringuified);
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
	public MediaResource getAsMediaResource(OLATResourceable res) {
		MediaResource mr = null;

		EPFrontendManager ePFMgr = (EPFrontendManager)CoreSpringFactory.getBean("epFrontendManager");
		PortfolioStructure structure = ePFMgr.loadPortfolioStructure(res);
		String xmlStructure = myStream.toXML(structure);
		try {
			//prepare a zip
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ZipOutputStream zipOut = new ZipOutputStream(out);
			zipOut.putNextEntry(new ZipEntry("map.xml"));
			InputStream in = new ByteArrayInputStream(xmlStructure.getBytes("UTF8"));
			FileUtils.copy(in, zipOut);
			zipOut.closeEntry();
			zipOut.close();
			
			//prepare media resource
			byte[] outArray = out.toByteArray();
			FileUtils.closeSafely(out);
			FileUtils.closeSafely(in);
			InputStream inOut = new ByteArrayInputStream(outArray);
			mr = new StreamMediaResource(inOut, null, 0l, 0l);
		} catch (IOException e) {
			log.error("Cannot export this map: " + structure, e);
		}
		
		return mr;
	}
	
	public static final PortfolioStructure getAsObject(File fMapXml) {
		try {
			//extract from zip
			InputStream in = new FileInputStream(fMapXml);
			ZipInputStream zipIn = new ZipInputStream(in);
			ZipEntry entry = zipIn.getNextEntry();
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			FileUtils.copy(zipIn, out);
			zipIn.closeEntry();
			zipIn.close();
			
			//prepare decoding with xstream
			byte[] outArray = out.toByteArray();
			String xml = new String(outArray);
			return (PortfolioStructure)myStream.fromXML(xml);
		} catch (IOException e) {
			log.error("Cannot export this map: " + fMapXml, e);
		}
		return null;
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
	public Controller createEditorController(OLATResourceable res, UserRequest ureq, WindowControl control) {
		return createLaunchController(res, null, ureq, control);
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getLaunchController(org.olat.core.id.OLATResourceable,
	 *      java.lang.String, org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public MainLayoutController createLaunchController(OLATResourceable res, String initialViewIdentifier, UserRequest ureq,
			WindowControl wControl) {
		RepositoryEntry repoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(res, false);
		EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		PortfolioStructureMap map = (PortfolioStructureMap)ePFMgr.loadPortfolioStructure(repoEntry.getOlatResource());
		EPSecurityCallback secCallback = EPSecurityCallbackFactory.getSecurityCallback(ureq, map, ePFMgr);
		Controller epCtr = EPUIFactory.createPortfolioStructureMapController(ureq, wControl, map, secCallback);
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, epCtr.getInitialComponent(), null);
		layoutCtr.addDisposableChildController(epCtr);
		return layoutCtr;
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

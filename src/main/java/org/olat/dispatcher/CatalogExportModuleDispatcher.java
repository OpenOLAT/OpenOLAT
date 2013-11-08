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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.dispatcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.olat.ControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryIconRenderer;
import org.olat.repository.controllers.RepositoryDetailsController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CatalogExportModuleDispatcher implements Dispatcher {

	public  static final String XML_FILE = "catalog.xml";
	private static final String SYSTEM_DIR = "system";

	private static final String XML_CAT = "catalog";										// catalog root tag
	private static final String XML_NODE = "node";											// node element (catalog hierarchy structure)
	private static final String XML_LEAF = "leaf";											// leaf element (catalog entries (courses, files, ...))
	private static final String XML_CHILDREN = "children";							// child elements of nodes
	private static final String XML_DESCR = "description";							// catalog(!) description of nodes and leaves
	private static final String XML_TYPE = "type";											// catalog entry type (course, PDF, ...) -> translated to installations default language!
	private static final String XML_TYPE_CSS = "iconCSS";								// CSS class for catalog entry type icon
	private static final String XML_LINKS = "links";										// links to entries container
	private static final String XML_LINK = "link";											// link to the entry
	private static final String XML_LINKTYPE = "type";									// type of the link to the entry (see following)
	private static final String XML_LINKTYPE_GUEST = "guest";						// -> link for guest access (only there if accessible by guests)
	private static final String XML_LINKTYPE_LOGIN = "login";						// -> link with login required
	private static final String XML_LINKTYPE_DETAIL = "detail";					// -> link to entry detail page
	private static final String XML_LINKTYPE_ENROLL = "cbb_enrollment";	// -> link to enrollment course buildings blocks in entry (there may be many of this!!!)
	private static final String XML_ACC = "access";											// entry access settings attribute name
	private static final String XML_OWN = "owners";											// entry owners container element name
	private static final String XML_USR = "user";												// entry owners user subelements
	private static final String XML_CUSTOM = "custom";									// custom info (empty for now)
	
	// NLS:
	
	private static final String NLS_CIF_TYPE_NA = "cif.type.na";
	private static final String NLS_TABLE_HEADER_ACCESS_GUEST = "access.guest";
	private static final String NLS_TABLE_HEADER_ACCESS_USER = "access.user";
	private static final String NLS_TABLE_HEADER_ACCESS_AUTHOR = "access.author";
	private static final String NLS_TABLE_HEADER_ACCESS_OWNER = "access.owner";
	
	private static DocumentBuilderFactory domFactory = null;
	private static DocumentBuilder domBuilder = null;
	private static BaseSecurity securityManager = null;
	
	private static Translator repoTypeTranslator =null;
	private static Translator catalogExportTranslator = null;
	
	private static boolean inAccess = false;
	private TimerTask tt;
	private long updateInterval;
	private static boolean instance=false;
	
	/**
	 * 
	 * @return
	 */
	private CatalogExportModuleDispatcher(Long updateInterval) {
		this.updateInterval = updateInterval * 60 * 1000;
		if (this.updateInterval < 60000) {
			//interval is smaller than one minute -> inform and go to default
			this.updateInterval = 5 * 60 * 1000;
			Tracing.logInfo("Update interval is to small, increasing to default of 5min!", this.getClass());
		}
	}

	
	synchronized private boolean reInitialize(){//o_clusterOK by:fj
		boolean retVal=true;
		if(instance){
			return retVal;
		}
		//TODO there is a new way of creating package translator
		repoTypeTranslator = new PackageTranslator(Util.getPackageName(RepositoryDetailsController.class), I18nModule.getDefaultLocale());
		catalogExportTranslator = new PackageTranslator(Util.getPackageName(CatalogExportModuleDispatcher.class), I18nModule.getDefaultLocale());
		securityManager = BaseSecurityManager.getInstance();
		try {
			domFactory = DocumentBuilderFactory.newInstance();												// init
			domBuilder = domFactory.newDocumentBuilder();
		} catch (Exception e) {
			retVal=false;
		}
		tt = new TimerTask() {
			public void run() {
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);								// don't disturb other things going on
				createXML();
			}
		};
		Timer timer = new Timer();
		timer.schedule(tt, (new GregorianCalendar()).getTime(), updateInterval);
		instance=true;
		return retVal;
	}
	
	protected void createXML() {
		Tracing.logDebug("Creating catalog export XML file...", CatalogExportModuleDispatcher.class);
		Document doc = domBuilder.newDocument();																		// create new XML document
		Element cat = doc.createElement(XML_CAT);																		// catalog element
		doc.appendChild(cat);
		cat.setAttribute("date", String.valueOf(System.currentTimeMillis()));				// set date in catalog element
		Element root = doc.createElement(XML_NODE);																	// root node
		root.setAttribute("name", "root");
		cat.appendChild(root);
		
		CatalogManager cm = CatalogManager.getInstance();														// instanciate catalog manager
		List ces = cm.getRootCatalogEntries();
		for (Iterator it = ces.iterator(); it.hasNext();) {													// for every root entry (currently only one)
			CatalogEntry ce = (CatalogEntry) it.next();
			getCatalogSubStructure(doc, root, cm, ce);																// scan this entry
		}
		
		TransformerFactory tranFac = TransformerFactory.newInstance();							// init transfromer to write XML to file 
		Transformer t;
		try {
			t = tranFac.newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");														// insert newlines
			t.setOutputProperty(OutputKeys.METHOD, "xml");
			t.setOutputProperty(OutputKeys.VERSION, "1.0");
			t.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			t.setOutputProperty(OutputKeys.STANDALONE, "yes");
			Source src  = new DOMSource(doc);
			File sysDir = new File(WebappHelper.getUserDataRoot(), SYSTEM_DIR);				// destination is .../olatdata/system/catalog.xml
			File f = new File(sysDir, XML_FILE);
			File o = new File(sysDir, XML_FILE + ".old");
			OutputStream os = new BufferedOutputStream(new FileOutputStream(o));
			try {
				InputStream  is = new BufferedInputStream(new FileInputStream(f));
				FileUtils.copy(is, os);																										// copy old version for access in the meantime
			} catch (Exception e) {
				// initial call of this method or fs error: catalog.xml not found, so don't copy it, that's ok
			}
			os = new BufferedOutputStream(new FileOutputStream(f));
			Result dest = new StreamResult(os);
			inAccess = true;
			t.transform(src, dest);																										// and that's it
			inAccess = false;
			Tracing.logDebug("                                ...done", CatalogExportModuleDispatcher.class);
		} catch (Exception e) {
			Tracing.logError("Error writing catalog export file.", e, CatalogExportModuleDispatcher.class);
		}
	}
	
	private void getCatalogSubStructure(Document doc, Element parent, CatalogManager cm, CatalogEntry ce) {
		Element cur = null;																													// tmp. element
		List l = cm.getChildrenOf(ce);																							// get catalog children
		// all nodes
		for (Iterator it = l.iterator(); it.hasNext();) {														// scan for node entries
			CatalogEntry c = (CatalogEntry) it.next();
			if (c.getType() == CatalogEntry.TYPE_NODE) {															// it's a node
				
				Element node = doc.createElement(XML_NODE);															// node element
				node.setAttribute("name", c.getName());
				parent.appendChild(node);
				
				cur = doc.createElement(XML_DESCR);																			// description element
				cur.appendChild(doc.createTextNode(c.getDescription()));
				node.appendChild(cur);

				if (cm.getChildrenOf(c).size() > 0) {																		// children element containing all subentries
					cur = doc.createElement(XML_CHILDREN);
					node.appendChild(cur);
					getCatalogSubStructure(doc, cur, cm, c);															// recursive scan
				}
				
				cur = doc.createElement(XML_CUSTOM);
				/*
				 * Insert custom info here!
				 */
				node.appendChild(cur);
				
			}
		}
		// all leafes
		for (Iterator it = l.iterator(); it.hasNext();) {														// scan for leaf entries
			CatalogEntry c = (CatalogEntry) it.next();
			if (c.getType() == CatalogEntry.TYPE_LEAF) {
				RepositoryEntry re = c.getRepositoryEntry();														// get repo entry
				if (re.getAccess() > RepositoryEntry.ACC_OWNERS_AUTHORS) {							// just show entries visible for registered users
					Element leaf = doc.createElement(XML_LEAF);														// leaf element
					leaf.setAttribute("name", c.getName());
					parent.appendChild(leaf);
					
					cur = doc.createElement(XML_DESCR);																		// description element
					cur.appendChild(doc.createTextNode(c.getDescription()));
					leaf.appendChild(cur);
					
					cur = doc.createElement(XML_TYPE);
					String typeName = re.getOlatResource().getResourceableTypeName();			// add the resource type
					StringOutput typeDisplayText = new StringOutput(100);
					if (typeName != null) { // add typename code
						RepositoryEntryIconRenderer reir = new RepositoryEntryIconRenderer();
						cur.setAttribute(XML_TYPE_CSS, reir.getIconCssClass(re));
						String tName = ControllerFactory.translateResourceableTypeName(typeName, repoTypeTranslator.getLocale());
						typeDisplayText.append(tName);
					} else {
						typeDisplayText.append(repoTypeTranslator.translate(NLS_CIF_TYPE_NA));
					}
					cur.appendChild(doc.createTextNode(typeDisplayText.toString()));
					leaf.appendChild(cur);

					
					Element links = doc.createElement(XML_LINKS);													// links container
					String tmp = "";
					

					ContextEntry contextEntry = BusinessControlFactory.getInstance().createContextEntry(re);
					String url = BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(contextEntry), false);
					switch (re.getAccess()) { // Attention! This uses the switch-case-fall-through mechanism!
						case RepositoryEntry.ACC_USERS_GUESTS:		tmp = catalogExportTranslator.translate(NLS_TABLE_HEADER_ACCESS_GUEST) + tmp;
																											appendLinkElement(doc, links, XML_LINKTYPE_GUEST, url + "&guest=true&amp;lang=" + I18nModule.getDefaultLocale().toString().toLowerCase());
						case RepositoryEntry.ACC_USERS:						tmp = catalogExportTranslator.translate(NLS_TABLE_HEADER_ACCESS_USER) + tmp;
						case RepositoryEntry.ACC_OWNERS_AUTHORS:	tmp = catalogExportTranslator.translate(NLS_TABLE_HEADER_ACCESS_AUTHOR) + tmp;
						case RepositoryEntry.ACC_OWNERS:					tmp = catalogExportTranslator.translate(NLS_TABLE_HEADER_ACCESS_OWNER) + tmp;
																											appendLinkElement(doc, links, XML_LINKTYPE_LOGIN, url);
																											break;
						default:																	tmp = catalogExportTranslator.translate(NLS_TABLE_HEADER_ACCESS_USER);
																											break;
					}

					// when implemented in OLAT, add link to detail page and enrollment entries here
					//appendLinkElement(doc, links, XML_LINKTYPE_DETAIL, RepoJumpInHandlerFactory.buildRepositoryDispatchURI2DeatilPage(re));
					//appendALotOfLinkElements4EnrollmentCBBsNeverthelessTheyAreVisibleAndOrAccessibleOrNot(doc, links, XML_LINKTYPE_ENROLL, re);
					
					leaf.setAttribute(XML_ACC, tmp);																			// access rights as attribute
					leaf.appendChild(links);																							// append links container
					
					Element owners = doc.createElement(XML_OWN);													// owners node
					leaf.appendChild(owners);
					SecurityGroup sg = re.getOwnerGroup();
					List m = securityManager.getIdentitiesOfSecurityGroup(sg);
					for (Iterator iter = m.iterator(); iter.hasNext();) {
						Identity i = (Identity) iter.next();
						cur = doc.createElement(XML_USR);																		// get all users
						cur.appendChild(doc.createTextNode(i.getName()));
						owners.appendChild(cur);
					}
					
					cur = doc.createElement(XML_CUSTOM);
					/*
					 * Insert custom info here!
					 */
					leaf.appendChild(cur);
					
				}
			}
		}
	}
	
	private void appendLinkElement(Document doc, Element parent, String type, String URL) {
		Element link = doc.createElement(XML_LINK);
		link.appendChild(doc.createTextNode(URL));
		link.setAttribute(XML_LINKTYPE, type);
		parent.appendChild(link);
	}
	
	/**
	 * @return The catalog XML file
	 */
	public static File getFile() throws FileNotFoundException {
		File f;
		if (inAccess)	f = new File(new File(WebappHelper.getUserDataRoot(), SYSTEM_DIR), XML_FILE + ".old");
		else					f = new File(new File(WebappHelper.getUserDataRoot(), SYSTEM_DIR), XML_FILE);
		if (!f.exists() || !f.canRead()) throw new FileNotFoundException("Catalog export file not found!");
		return f;
	}

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		if(!this.reInitialize())
			Tracing.logError("Some Failsaves in reInitialization needed !", CatalogExportModuleDispatcher.class);
			try {
				Tracing.logInfo("Catalog XML file requested by " + request.getRemoteAddr(), CatalogExportModuleDispatcher.class);
				ServletUtil.serveResource(request, response, new FileMediaResource(CatalogExportModuleDispatcher.getFile(), true));
			} catch (Exception e) {
				Tracing.logError("Error requesting catalog export file: ", e, CatalogExportModuleDispatcher.class);
				try {
					ServletUtil.serveResource(request, response, new NotFoundMediaResource(request.getRequestURI()));
				} catch (Exception e1) {
					// what now???
					Tracing.logError("What now ???", CatalogExportModuleDispatcher.class);
				}
			}
	}
}
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.olat.portal.links;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortlet;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.quality.analysis.MultiGroupBy;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
*
* @author skoeber
*/
public class LinksPortlet extends AbstractPortlet {
	
	private static final Logger log = Tracing.createLoggerFor(LinksPortlet.class);
	
	private String cssWrapperClass = "o_portlet_links";
	
	protected static final String LANG_ALL = "*";
	protected static final String LANG_DE = "de";
	protected static final String LANG_EN = "en";
	protected static final String ACCESS_GUEST = "-";
	protected static final String ACCESS_REG = "+";
	protected static final String ACCESS_ALL = "*";
	
	//configuration file
	private static final String CONFIG_FILE = "/WEB-INF/olat_portals_links.xml";
	//configuration file xml elements
	private static final String ELEM_INSTITUTION = "University";
	private static final String ATTR_INSTITUTION_NAME = "name";
	private static final String ELEM_LINK = "Link";
	private static final String ELEM_LINK_TITLE = "Title";
	private static final String ELEM_LINK_URL = "URL";
	private static final String ELEM_LINK_DESC = "Description";
	private static final String ELEM_LINK_IDENT = "Identifier";
	private static final String ELEM_LINK_TARGET = "Target";
	private static final String ELEM_LINK_LANG = "Language";
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				MultiGroupBy.class, PortletLink.class, PortletInstitution.class };
		xstream.addPermission(new ExplicitTypePermission(types));

		xstream.alias("LinksPortlet", Map.class);
		xstream.alias(ELEM_LINK, PortletLink.class);
		xstream.alias(ELEM_INSTITUTION, PortletInstitution.class);
		xstream.aliasAttribute(PortletInstitution.class, ATTR_INSTITUTION_NAME, ATTR_INSTITUTION_NAME);
	}
	
	private static Map<String, PortletInstitution> content;

	private static File fxConfXStreamFile;
	private Controller runCtr;
	
	@Override
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map<String,String> configuration) {
		if(content == null) init();
		LinksPortlet p = new LinksPortlet();
		p.setName(this.getName());
		p.setConfiguration(configuration);
		p.setTranslator(Util.createPackageTranslator(LinksPortlet.class, ureq.getLocale()));
		// override css class if configured
		String cssClass = configuration.get("cssWrapperClass");
		if (cssClass != null) p.setCssWrapperClass(cssClass);
		
		return p;
	}
	
	private static void init() {
		log.debug("START: Loading remote portlets content.");
		
		File configurationFile = new File(WebappHelper.getContextRealPath(CONFIG_FILE));
		// fxdiff: have file outside of war/olatapp
		File fxConfFolder = new File(WebappHelper.getUserDataRoot() + "/customizing/linksPortlet");
		if (!fxConfFolder.exists()) {
			fxConfFolder.mkdir();
		}
		File fxConfFile = new File(fxConfFolder + "/olat_portals_links.xml");
		fxConfXStreamFile = new File(fxConfFolder + "/olat_portals_xstream.xml");
		if (!fxConfFile.exists() && !fxConfXStreamFile.exists()) {
			try {
				if(fxConfFile.createNewFile()) {
					FileUtils.copyFileToFile(configurationFile, fxConfFile, false);
					log.info("portal links portlet: copied initial config from " + CONFIG_FILE);
				}
			} catch (IOException e) {
				new AssertException("could not copy an initial portal links config to olatdata", e);
			}
		}
		
		// this map contains the whole data
		HashMap<String, PortletInstitution> portletMap = new HashMap<>();

		if (!fxConfXStreamFile.exists()){
			try {
				SAXReader reader = SAXReader.createDefault();
				Document doc = reader.read(fxConfFile);
				Element rootElement = doc.getRootElement();
				List<Element> lstInst = rootElement.elements(ELEM_INSTITUTION);
				for( Element instElem : lstInst ) {
					String inst = instElem.attributeValue(ATTR_INSTITUTION_NAME);
					List<Element> lstTmpLinks = instElem.elements(ELEM_LINK);
					List<PortletLink> lstLinks = new ArrayList<>(lstTmpLinks.size());
					for( Element linkElem: lstTmpLinks ) {
						String title = linkElem.elementText(ELEM_LINK_TITLE);
						String url = linkElem.elementText(ELEM_LINK_URL);
						String target = linkElem.elementText(ELEM_LINK_TARGET);
						String lang = linkElem.elementText(ELEM_LINK_LANG);
						String desc = linkElem.elementText(ELEM_LINK_DESC);
						String identifier = linkElem.elementText(ELEM_LINK_IDENT);
						lstLinks.add(new PortletLink(title, url, target, lang, desc, identifier));
					}
					portletMap.put(inst, new PortletInstitution(inst, lstLinks));
				}
			} catch (Exception e) {
				log.error("Error reading configuration file", e);
			} finally {
				content = portletMap;
			}
			// lazy migrate to new format
			saveLinkList(content);
			FileUtils.copyFileToFile(fxConfFile, new File(fxConfFile + ".bak"), true);
		} else {
			content = readConfiguration(fxConfXStreamFile);
		}		
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, PortletInstitution> readConfiguration(File file) {
		return (Map<String, PortletInstitution>) XStreamHelper.readObject(xstream, file);
	}
	
	public static boolean saveLinkList(Map<String, PortletInstitution> portletMap){
		XStreamHelper.writeObject(xstream, fxConfXStreamFile, portletMap);
		return true;		
	}
	
	public static PortletLink getLinkByIdentifier(String identifier){
		for (Iterator<String> iterator = content.keySet().iterator(); iterator.hasNext();) {
			String inst = iterator.next();
			PortletInstitution portletsForInst = content.get(inst);
			List<PortletLink> instLinks = portletsForInst.getLinks();
			for (PortletLink portletLink : instLinks) {
				if (portletLink.getIdentifier().equals(identifier)) return portletLink;
			}
		}
		return null;
	}
	
	public static void removeLink(PortletLink link){
		if (link == null) return;
		for (Iterator<String> iterator = content.keySet().iterator(); iterator.hasNext();) {
			String inst = iterator.next();
			PortletInstitution portletsForInst = content.get(inst);
			List<PortletLink> instLinks = portletsForInst.getLinks();
			for (PortletLink portletLink : instLinks) {
				if (portletLink.getIdentifier().equals(link.getIdentifier())) {
					instLinks.remove(link);
					break;
				}
			}
		}
		saveLinkList(content);
	}
	
	public static void updateLink(PortletLink link){
		if (link == null) return;
		for (Iterator<String> iterator = content.keySet().iterator(); iterator.hasNext();) {
			String inst = iterator.next();
			PortletInstitution portletsForInst = content.get(inst);
			List<PortletLink> instLinks = portletsForInst.getLinks();
			boolean existingLink = false;
			for (PortletLink portletLink : instLinks) {
				if (portletLink.getIdentifier().equals(link.getIdentifier())) {
					portletLink = link;
					existingLink = true;
					break;
				} 
			}
			if (!existingLink && portletsForInst == link.getInstitution()) {				
				portletsForInst.addLink(link);
				break;
			}
		}
		saveLinkList(content);
	}
	
		
	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getTitle()
	 */
	public String getTitle() {
		return getTranslator().translate("portlet.title");
	}	

	public static void reInit(UserRequest ureq){		
		content = null;
		init();
	}
	
	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getDescription()
	 */
	public String getDescription() {
		return getTranslator().translate("portlet.description");
	}	

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getInitialRunComponent(org.olat.gui.control.WindowControl, org.olat.gui.UserRequest)
	 */
	public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
		if(this.runCtr != null) runCtr.dispose();
		this.runCtr = new LinksPortletRunController(ureq, wControl);
		return runCtr.getInitialComponent();
	}

	/**
	 * @see org.olat.gui.control.Disposable#dispose(boolean)
	 */
	public void dispose() {
		disposeRunComponent();
	}
	
	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getCssClass()
	 */
	public String getCssClass() {
		return cssWrapperClass;
	}

	/**
	 * Helper used to overwrite the default css class with the configured class
	 * @param cssWrapperClass
	 */
	void setCssWrapperClass(String cssWrapperClass) {
		this.cssWrapperClass = cssWrapperClass;
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#disposeRunComponent(boolean)
	 */
	public void disposeRunComponent() {
		if (runCtr != null) {
			runCtr.dispose();
			runCtr = null;
		}
	}

	/**
	 * @return Returns the content map.
	 */
	public static Map<String, PortletInstitution> getContent() {
		return content;
	}
	
}


/**
 * @author skoeber
 *
 */
class PortletInstitution {
	
	private String name; 
	private List<PortletLink> links;
	
	public PortletInstitution(String name) {
		this.name = name;
		this.links = new ArrayList<>();
	}
	
	public PortletInstitution(String name, List<PortletLink> links) {
		this.name = name;
		this.links = links;
	}
	
	public void addLink(PortletLink link) {
		links.add(link);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<PortletLink> getLinks() {
		return links;
	}

	public void setLinks(List<PortletLink> links) {
		this.links = links;
	}
}

/**
 * @author skoeber
 *
 */
class PortletLink {
	
	private String title, url, target, language, description;
	private String identifier;
	private transient PortletInstitution institution;
	
	public PortletLink(String title, String url, String target, String language, String description, String identifier) {
		setTitle(title);
		setUrl(url);
		setTarget(target);
		setLanguage(language);
		setDescription(description);
		setIdentifier(identifier);
	}

	public PortletInstitution getInstitution() {
		return institution;
	}

	public void setInstitution(PortletInstitution institution) {
		this.institution = institution;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setIdentifier(String identifier){
		if (identifier == null) {
			this.identifier = UUID.randomUUID().toString().replace("-", "");
		} else {
			this.identifier = identifier;
		}
	}
	
	public String getIdentifier(){
		if (!StringHelper.containsNonWhitespace(identifier)){
			setIdentifier(null);
		}
		return identifier;
	}
}

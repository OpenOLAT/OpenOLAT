/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 * 
 * @author skoeber
 */
package de.bps.olat.portal.links;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortlet;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;

public class LinksPortlet extends AbstractPortlet {
	
	private String cssWrapperClass = "o_pt_w_if";
	
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
	private static final String ELEM_LINK_TARGET = "Target";
	private static final String ELEM_LINK_LANG = "Language";
	
	private static HashMap<String, PortletInstitution> content;
	private Controller runCtr;
	
	/**
	 * @see org.olat.gui.control.generic.portal.AbstractPortlet#createInstance(org.olat.gui.control.WindowControl, org.olat.gui.UserRequest, java.util.Map)
	 */
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map configuration) {
		if(content == null) init();
		LinksPortlet p = new LinksPortlet();
		p.setName(this.getName());
		p.setConfiguration(configuration);
		p.setTranslator(Util.createPackageTranslator(LinksPortlet.class, ureq.getLocale()));
		// override css class if configured
		String cssClass = (String)configuration.get("cssWrapperClass");
		if (cssClass != null) p.setCssWrapperClass(cssClass);
		
		return p;
	}
	
	private void init() {
		OLog logger = Tracing.createLoggerFor(LinksPortlet.class);
		if(logger.isDebug()) logger.debug("START: Loading remote portlets content.");
		
		File configurationFile = new File(WebappHelper.getContextRoot() + CONFIG_FILE);
		
		// this map contains the whole data
		HashMap<String, PortletInstitution> portletMap = new HashMap<String, PortletInstitution>();

		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(configurationFile);
			Element rootElement = doc.getRootElement();
			List<Element> lstInst = rootElement.elements(ELEM_INSTITUTION);
			for( Element instElem : lstInst ) {
				String inst = instElem.attributeValue(ATTR_INSTITUTION_NAME);
				List<Element> lstTmpLinks = instElem.elements(ELEM_LINK);
				List<PortletLink> lstLinks = new ArrayList<PortletLink>(lstTmpLinks.size());
				for( Element linkElem: lstTmpLinks ) {
					String title = linkElem.elementText(ELEM_LINK_TITLE);
					String url = linkElem.elementText(ELEM_LINK_URL);
					String target = linkElem.elementText(ELEM_LINK_TARGET);
					String lang = linkElem.elementText(ELEM_LINK_LANG);
					String desc = linkElem.elementText(ELEM_LINK_DESC);
					lstLinks.add(new PortletLink(title, url, target, lang, desc));
				}
				portletMap.put(inst, new PortletInstitution(inst, lstLinks));
			}
		} catch (Exception e) {
			logger.error("Error reading configuration file", e);
		} finally {
			content = portletMap;
		}
	}
	
	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getTitle()
	 */
	public String getTitle() {
		return getTranslator().translate("portlet.title");
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
		this.links = new ArrayList<PortletLink>();
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
	
	public PortletLink(String title, String url, String target, String language, String description) {
		this.title = title;
		this.url = url;
		this.target = target;
		this.language = language;
		this.description = description;
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
}

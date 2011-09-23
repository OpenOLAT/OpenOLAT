/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstraße 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 *
 * Initial Date:  08.07.2005 <br>
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 **/
package de.bps.olat.portal.institution;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.FastHashMap;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortlet;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.StartupException;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;

import com.anthonyeden.lib.config.Configuration;
import com.anthonyeden.lib.config.ConfigurationException;
import com.anthonyeden.lib.config.XMLConfiguration;

public class InstitutionPortlet extends AbstractPortlet {
	private String cssWrapperClass = "o_pt_w_if";

	private static final String CONFIG_FILE = "/WEB-INF/olat_portals_institution.xml";
	private static FastHashMap institutions = null;

	private static final String POLYMORPHLINK = "polymorphlink";
	private static final String POLYMORPHLINK_TARGETID = "default_targetid";
	private static final String POLYMORPHLINK_TYPE = "type";
	private static final String POLYMORPHLINK_TEXT = "text";
	private static final String POLYMORPHLINK_ELEMENT = "element";
	private static final String POLYMORPHLINK_ELEMENT_ATTRIBUT = "attribute";
	private static final String POLYMORPHLINK_ELEMENT_VALUE = "value";
	private static final String POLYMORPHLINK_ELEMENT_ID = "targetid";
	private static final String POLYMORPHLINK_ELEMENT_CONDITION = "condition";
	private static final String INSTITUTION_NAME = "name";
	private static final String INSTITUTION_LOGO = "logo";
	private static final String INSTITUTION_URL = "url";
	private static final String SUPERVISOR = "supervisor";
	private static final String SUPERVISOR_PERSON = "person";
	private static final String SUPERVISOR_URL = "url";
	private static final String SUPERVISOR_EMAIL = "email";
	private static final String SUPERVISOR_PHONE = "phone";
	private static final String SUPERVISOR_BLOG = "blog";
	private static final String VALUE = "value";

	public static final String TYPE_COURSE = "course";
	public static final String TYPE_CATALOG = "catalog";
	
	public static String HTTP_REQUEST_ATTRIBUT="catalog_node_id";
	private Controller runCtr;

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getTitle()
	 */
	public String getTitle() {
		String title = (String) getConfiguration().get("title_" + getTranslator().getLocale().toString());
		if (title == null) {
			title = getTranslator().translate("institution.title");
		}
		return title;
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getDescription()
	 */
	public String getDescription() {
		String desc = (String) getConfiguration().get("description_" + getTranslator().getLocale().toString());
		if (desc == null) {
			desc = getTranslator().translate("institution.description");
		}
		return desc;
	}

	/**
	 * @see org.olat.gui.control.generic.portal.AbstractPortlet#createInstance(org.olat.gui.control.WindowControl, org.olat.gui.UserRequest, java.util.Map)
	 */
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map configuration) {
		if (institutions == null) init();
		InstitutionPortlet p = new InstitutionPortlet();
		p.setName(this.getName());
		p.setConfiguration(configuration);
		p.setTranslator(new PackageTranslator(Util.getPackageName(InstitutionPortlet.class), ureq.getLocale()));
		// override css class if configured
		String cssClass = (String) configuration.get("cssWrapperClass");
		if (cssClass != null) p.setCssWrapperClass(cssClass);
		return p;
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getInitialRunComponent(org.olat.gui.control.WindowControl, org.olat.gui.UserRequest)
	 */
	public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
		if(this.runCtr != null) runCtr.dispose();
		this.runCtr =  new InstitutionPortletRunController(ureq, wControl);
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
	 * initializes the institution portlet config
	 */
	public void init() {

		File configurationFile = new File(WebappHelper.getContextRoot() + CONFIG_FILE);

		XMLConfiguration instConfigSection = null;
		try {
			instConfigSection = new XMLConfiguration(configurationFile);
		} catch (ConfigurationException ce) {
			throw new StartupException("Error loading institution portlet configuration file!", ce);
		}
		if (instConfigSection == null) { throw new StartupException("Error loading institution portlet configuration file!"); }

		institutions = new FastHashMap();
		for (Iterator iter = instConfigSection.getChildren("institution").iterator(); iter.hasNext();) {
			Configuration instConfigEntry = (Configuration) iter.next(); // the institutions config entry
			String shortName = instConfigEntry.getAttribute("shortname"); // short name of inst

			if (shortName == null) { throw new StartupException("Institution portlet startup: No shortname given for one entry!"); }
			try {
				List<InstitutionPortletSupervisorEntry> supervisors = new ArrayList<InstitutionPortletSupervisorEntry>(1); // there may be more than one supervisor
				for (Iterator it = instConfigEntry.getChildren(SUPERVISOR).iterator(); it.hasNext();) {
					Configuration supervisorElement = (Configuration) it.next(); // one supervisor element
					InstitutionPortletSupervisorEntry ipse = new InstitutionPortletSupervisorEntry(getSupervisorElementChild(supervisorElement.getChild(SUPERVISOR_PERSON)),
								getSupervisorElementChild(supervisorElement.getChild(SUPERVISOR_PHONE)),
								getSupervisorElementChild(supervisorElement.getChild(SUPERVISOR_EMAIL)),
								getSupervisorElementChild(supervisorElement.getChild(SUPERVISOR_URL)),
								getSupervisorElementChild(supervisorElement.getChild(SUPERVISOR_BLOG)));
					supervisors.add(ipse); // save it
				}

				//get polymorph links
				List<Configuration> polymorphConfs = instConfigEntry.getChildren(POLYMORPHLINK);
				List<PolymorphLink> polyList = new ArrayList<PolymorphLink>();
				if (polymorphConfs != null && polymorphConfs.size() > 0) {
					for(Configuration polymorphConf: polymorphConfs) {
						List<PolymorphLinkElement> elemList = new ArrayList<PolymorphLinkElement>();
						for (Iterator<Configuration> it = polymorphConf.getChildren(POLYMORPHLINK_ELEMENT).iterator(); it.hasNext();) {
							Configuration tmp = it.next();
							elemList.add(new PolymorphLinkElement(tmp.getAttribute(POLYMORPHLINK_ELEMENT_ATTRIBUT), tmp
									.getAttribute(POLYMORPHLINK_ELEMENT_VALUE), tmp.getAttribute(POLYMORPHLINK_ELEMENT_ID), tmp
									.getAttribute(POLYMORPHLINK_ELEMENT_CONDITION)));
							
						}
						PolymorphLink polyLink = new PolymorphLink(polymorphConf.getAttribute(POLYMORPHLINK_TARGETID), polymorphConf.getAttribute(POLYMORPHLINK_TYPE),
								polymorphConf.getAttribute(POLYMORPHLINK_TEXT), elemList);
						polyList.add(polyLink);
					}
				}
				
				InstitutionPortletEntry ipe = new InstitutionPortletEntry(instConfigEntry.getChild(INSTITUTION_NAME).getAttribute(VALUE),
						instConfigEntry.getChild(INSTITUTION_URL).getAttribute(VALUE), instConfigEntry.getChild(INSTITUTION_LOGO).getAttribute(VALUE),
						supervisors, polyList);
				institutions.put(shortName.toLowerCase(), ipe); // save inst entry
			} catch (Exception e) {
				e.printStackTrace();
				throw new StartupException(e.getMessage(), e);
			}
		}

		// from now on optimize for non-synchronized read access
		institutions.setFast(true);
	}

	private String getSupervisorElementChild(Configuration child) {
		String value = new String();
		if(child!=null){
			value=child.getAttribute(VALUE);
		}
		return value;
	}

	/**
	 * 
	 * @param institution
	 * @return The entry, or null if not found
	 */
	public static InstitutionPortletEntry getInstitutionPortletEntry(String institution) {
		return (InstitutionPortletEntry) institutions.get(institution);
	}
}

/**
 * 
 * Description:<br>
 * This is one entry of the institution portlet.
 * 
 * <P>
 * Initial Date:  21.07.2006 <br>
 * @author Lars Eberle (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
class InstitutionPortletEntry {
	private String institutionName;
	private String institutionUrl;
	private String institutionLogo;
	private List<InstitutionPortletSupervisorEntry> supervisors;
	private List<PolymorphLink> polymorphLinks;

	/**
	 * @param institutionName Name of the inst.
	 * @param institutionUrl URL of the inst.
	 * @param institutionLogo Logo file name of the inst.
	 * @param supervisors The supervisors. List of type InstitutionportletSupervisorEntry.
	 */
	InstitutionPortletEntry(String institutionName, String institutionUrl, String institutionLogo,
			List<InstitutionPortletSupervisorEntry> supervisors, List<PolymorphLink> polymorphLinks) {
		this.institutionName = institutionName;
		this.institutionUrl = institutionUrl;
		this.institutionLogo = institutionLogo;
		this.supervisors = supervisors;
		this.polymorphLinks = polymorphLinks;
	}

	/**
	 * @return Returns the institutionLogo.
	 */
	public String getInstitutionLogo() {
		return institutionLogo;
	}

	/**
	 * @return Returns the institutionName.
	 */
	public String getInstitutionName() {
		return institutionName;
	}

	/**
	 * @return Returns the institutionUrl.
	 */
	public String getInstitutionUrl() {
		return institutionUrl;
	}

	/**
	 * @return Returns the supervisors.
	 */
	public List<InstitutionPortletSupervisorEntry> getSupervisors() {
		return supervisors;
	}

	protected List<PolymorphLink> getPolymorphLinks() {
		return polymorphLinks;
	}

	protected void setPolymorphLinks(List<PolymorphLink> polymorphLink) {
		this.polymorphLinks = polymorphLink;
	}
}

/**
 * 
 * Description:<br>
 * One supervisor.
 * 
 * <P>
 * Initial Date:  21.07.2006 <br>
 * @author Lars Eberle (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
class InstitutionPortletSupervisorEntry {
	private String supervisorPhone;
	private String supervisorMail;
	private String supervisorPerson;
	private String supervisorURL;
	private String supervisorBlog;

	public String getSupervisorBlog() {
		return supervisorBlog;
	}

	public void setSupervisorBlog(String supervisorBlog) {
		this.supervisorBlog = supervisorBlog;
	}
	
	/**
	 * @param supervisorName The supervisors name. 
	 * @param supervisorSurname The supervisors surname.
	 * @param supervisorPhone The supervisors phone number.
	 * @param supervisorMail The supervisors mail.
	 * @param supervisorBlog The supervisor Blog 	 
	 */
	InstitutionPortletSupervisorEntry(String supervisorPerson, String supervisorPhone, String supervisorMail, String supervisorURL,String supervisorBlog) {
		this.supervisorPerson = supervisorPerson;
		this.supervisorPhone = supervisorPhone;
		this.supervisorMail = supervisorMail;
		this.supervisorURL = supervisorURL;
		this.supervisorBlog = supervisorBlog;
	}

	InstitutionPortletSupervisorEntry(String supervisorURL) {
		this.supervisorURL = supervisorURL;

	}

	/**
	 * @return Returns the supervisorMail.
	 */
	public String getSupervisorMail() {
		return supervisorMail;
	}

	/**
	 * @return Returns the supervisorPhone.
	 */
	public String getSupervisorPhone() {
		return supervisorPhone;
	}

	public String getSupervisorPerson() {
		return supervisorPerson;
	}

	public void setSupervisorPerson(String supervisorPerson) {
		this.supervisorPerson = supervisorPerson;
	}

	public String getSupervisorURL() {
		return supervisorURL;
	}

	public void setSupervisorURL(String supervisorURL) {
		this.supervisorURL = supervisorURL;
	}

}

class PolymorphLink {
	private String defaultId;
	private List<PolymorphLinkElement> linkElements;
	private String linkType;
	private String linkText;

	protected String getDefaultLink() {
		return this.defaultId;
	}

	protected String getLinkType() {
		return this.linkType;
	}

	PolymorphLink(String defautlId, String linkType, String linkText, List<PolymorphLinkElement> linkElements) {
		this.defaultId = defautlId;
		this.linkType = linkType;
		this.linkText = linkText;
		this.linkElements = linkElements;
	}

	/**
	 * used to check over the given rule set and find a matching rule for the user
	 * @param ureq ... we need to get the user from somewhere 
	 * @return Id from the first matching rule, otherwise <b>null</b>  
	 */
	protected String getResultIDForUser(UserRequest ureq) {

		// first value --> orgUnit | second value --> studySubject must be equivalent with enumeration in PolymorphLinkElement

		String orgunit = ureq.getIdentity().getUser().getProperty(UserConstants.ORGUNIT, ureq.getLocale());
		String studysubject = ureq.getIdentity().getUser().getProperty(UserConstants.STUDYSUBJECT, ureq.getLocale());

		String[] userValues = {
						orgunit != null ? orgunit : "",
						studysubject != null ? studysubject : "" };

		for (PolymorphLinkElement elem : linkElements) {
			switch (elem.getCondition()) {
				case 0:
					if (userValues[elem.getAttrib()].startsWith(elem.getValue())) return elem.getId(); break;
				case 1:
					if (userValues[elem.getAttrib()].equals(elem.getValue())) return elem.getId(); break;
				case 2:
					if (userValues[elem.getAttrib()].contains(elem.getValue())) return elem.getId(); break;
			}
		}
		return null;
	}
	
	protected boolean hasConditions() {
		return (linkElements != null && linkElements.size() > 0);
	}

	protected String getLinkText() {
		return linkText;
	}
}

class PolymorphLinkElement {
	private int attrib;
	private String value;
	private int condition = 0;
	private String id;

	protected static final String EQUALS = "equals";
	protected static final String STARTS_WITH = "starts_with";
	protected static final String CONTAINS = "contains";

	protected PolymorphLinkElement(String attrib, String value, String id, String condition) {
		this.value = value;
		this.id = id;

		if (attrib.equals("orgunit")) {
			this.attrib = 0;
		} else if (attrib.equals("studysubject")) {
			this.attrib = 1;
		}

		if (condition.equals(STARTS_WITH)) {
			this.condition = 0;
		} else if (condition.equals(EQUALS)) {
			this.condition = 1;
		} else if (condition.equals(CONTAINS)) {
			this.condition = 2;
		}
	}

	protected int getAttrib() {
		return attrib;
	}

	protected String getValue() {
		return value;
	}

	protected int getCondition() {
		return condition;
	}

	protected String getId() {
		return id;
	}
}

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
import java.util.Collections;
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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;


public class InstitutionPortlet extends AbstractPortlet {
	private String cssWrapperClass = "o_pt_w_if";

	private static final String CONFIG_FILE = "/WEB-INF/olat_portals_institution.xml";
	private static FastHashMap institutions = null;

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

		institutions = new FastHashMap();
		
		File configurationFile = new File(WebappHelper.getContextRoot() + CONFIG_FILE);
		XStream xstream = getInstitutionConfigXStream();
		InstitutionConfiguration configuration = (InstitutionConfiguration)xstream.fromXML(configurationFile);
		
		for(InstitutionPortletEntry institution: configuration.getInstitution()) {
			String shortName = institution.shortname;
			if (shortName == null) { 
				throw new StartupException("Institution portlet startup: No shortname given for one entry!");
			}
			institutions.put(shortName.toLowerCase(), institution);
		}

		// from now on optimize for non-synchronized read access
		institutions.setFast(true);
	}

	/**
	 * 
	 * @param institution
	 * @return The entry, or null if not found
	 */
	public static InstitutionPortletEntry getInstitutionPortletEntry(String institution) {
		return (InstitutionPortletEntry) institutions.get(institution);
	}
		
	public static XStream getInstitutionConfigXStream() {
		XStream xstream = new XStream(new XppDriver(new NoNameCoder()));
		xstream.alias("configuration", InstitutionConfiguration.class);
		xstream.addImplicitCollection(InstitutionConfiguration.class, "institution", "institution", InstitutionPortletEntry.class);
		xstream.alias("institution", InstitutionPortletEntry.class);
		xstream.addImplicitCollection(InstitutionPortletEntry.class, "polymorphlink", "polymorphlink", PolymorphLink.class);
		xstream.aliasAttribute(InstitutionPortletEntry.class, "shortname", "shortname");
		xstream.alias("logo", Value.class);
		xstream.alias("name", Value.class);
		xstream.alias("url", Value.class);
		
		xstream.alias("supervisor", InstitutionPortletSupervisorEntry.class);
		xstream.addImplicitCollection(InstitutionPortletEntry.class, "supervisor", "supervisor", InstitutionPortletSupervisorEntry.class);
		xstream.alias("person", Value.class);
		xstream.alias("phone", Value.class);
		xstream.alias("email", Value.class);
		xstream.alias("blog", Value.class);
		//polymorph link
		xstream.alias("polymorphlink", PolymorphLink.class);
		xstream.aliasAttribute(PolymorphLink.class, "defaultId", "default_targetid");
		xstream.aliasAttribute(PolymorphLink.class, "linkType", "type");
		xstream.aliasAttribute(PolymorphLink.class, "linkText", "text");
		//polymorph link element
		xstream.alias("element", PolymorphLinkElement.class);
		xstream.addImplicitCollection(PolymorphLink.class, "element", "element", PolymorphLinkElement.class);
		xstream.aliasAttribute(PolymorphLinkElement.class, "attribute", "attribute");
		xstream.aliasAttribute(PolymorphLinkElement.class, "value", "value");
		xstream.aliasAttribute(PolymorphLinkElement.class, "cond", "condition");
		xstream.aliasAttribute(PolymorphLinkElement.class, "id", "targetid");

		xstream.aliasAttribute(Value.class, "value", "value");
		return xstream;
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

	public List<InstitutionPortletSupervisorEntry> supervisor;
	public List<PolymorphLink> polymorphlink;
	public Value logo;
	public Value name;
	public Value url;
	public String shortname;

	/**
	 * @param institutionName Name of the inst.
	 * @param institutionUrl URL of the inst.
	 * @param institutionLogo Logo file name of the inst.
	 * @param supervisors The supervisors. List of type InstitutionportletSupervisorEntry.
	 */
	public InstitutionPortletEntry() {
		//
	}

	/**
	 * @return Returns the institutionLogo.
	 */
	public String getInstitutionLogo() {
		return logo == null ? null : logo.value;
	}

	/**
	 * @return Returns the institutionName.
	 */
	public String getInstitutionName() {
		return name == null ? null : name.value;
	}

	/**
	 * @return Returns the institutionUrl.
	 */
	public String getInstitutionUrl() {
		return url == null ? null : url.value;
	}

	/**
	 * @return Returns the supervisors.
	 */
	public List<InstitutionPortletSupervisorEntry> getSupervisors() {
		if(supervisor == null) {
			return Collections.emptyList();
		}
		return supervisor;
	}

	public List<PolymorphLink> getPolymorphLinks() {
		if(polymorphlink == null) {
			return Collections.emptyList();
		}
		return polymorphlink;
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
	public Value phone;
	public Value email;
	public Value person;
	public Value url;
	public Value blog;

	/**
	 * @param supervisorName The supervisors name. 
	 * @param supervisorSurname The supervisors surname.
	 * @param supervisorPhone The supervisors phone number.
	 * @param supervisorMail The supervisors mail.
	 * @param supervisorBlog The supervisor Blog 	 
	 */
	public InstitutionPortletSupervisorEntry() {
		//
	}
	
	public String getSupervisorBlog() {
		return blog == null ? null : blog.value;
	}

	/**
	 * @return Returns the supervisorMail.
	 */
	public String getSupervisorMail() {
		return email == null ? null : email.value;
	}

	/**
	 * @return Returns the supervisorPhone.
	 */
	public String getSupervisorPhone() {
		return phone == null ? null : phone.value;
	}

	public String getSupervisorPerson() {
		return person == null ? null : person.value;
	}

	public String getSupervisorURL() {
		return url == null ? null : url.value;
	}
}

class PolymorphLink {
	public String defaultId;
	public String linkType;
	public String linkText;
	public List<PolymorphLinkElement> element;

	protected String getDefaultLink() {
		return this.defaultId;
	}

	protected String getLinkType() {
		return this.linkType;
	}

	public PolymorphLink() {
		//
	}

	/**
	 * used to check over the given rule set and find a matching rule for the user
	 * @param ureq ... we need to get the user from somewhere 
	 * @return Id from the first matching rule, otherwise <b>null</b>  
	 */
	protected String getResultIDForUser(UserRequest ureq) {
		if(element == null) return null;

		// first value --> orgUnit | second value --> studySubject must be equivalent with enumeration in PolymorphLinkElement

		String orgunit = ureq.getIdentity().getUser().getProperty(UserConstants.ORGUNIT, ureq.getLocale());
		String studysubject = ureq.getIdentity().getUser().getProperty(UserConstants.STUDYSUBJECT, ureq.getLocale());

		String[] userValues = {
						orgunit != null ? orgunit : "",
						studysubject != null ? studysubject : "" };
		
		for (PolymorphLinkElement elem : element) {
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
		return (element != null && element.size() > 0);
	}

	protected String getLinkText() {
		return linkText;
	}
}

class PolymorphLinkElement {
	protected static final String EQUALS = "equals";
	protected static final String STARTS_WITH = "starts_with";
	protected static final String CONTAINS = "contains";

	public String id;
	public String cond;
	public String value;
	public String attribute;

	public PolymorphLinkElement() {
		//
	}

	protected int getAttrib() {
		if ("orgunit".equals(attribute)) {
			return 0;
		} else if ("studysubject".equals(attribute)) {
			return 1;
		}
		return -1;
	}

	protected String getValue() {
		return value;
	}

	protected int getCondition() {
		if (STARTS_WITH.equals(cond)) {
			return 0;
		} else if (EQUALS.equals(cond)) {
			return 1;
		} else if (CONTAINS.equals(cond)) {
			return 2;
		}
		return -1;
	}

	protected String getId() {
		return id;
	}
}

class Value {
	public String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value == null ? "null" : value;
	}
}

class InstitutionConfiguration {
	public List<InstitutionPortletEntry> institution;
	
	public List<InstitutionPortletEntry> getInstitution() {
		if(institution == null) {
			return Collections.emptyList();
		}
		return institution;
	}
}

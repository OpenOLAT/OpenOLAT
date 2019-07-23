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
package de.bps.olat.portal.institution;

import java.io.File;
import java.util.ArrayList;
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
import org.olat.core.id.UserConstants;
import org.olat.core.logging.StartupException;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.security.ExplicitTypePermission;


public class InstitutionPortlet extends AbstractPortlet {
	private String cssWrapperClass = "o_portlet_institutions";

	private static final String CONFIG_FILE = "/WEB-INF/olat_portals_institution.xml";
	private static FastHashMap institutions = null;

	public static final String TYPE_COURSE = "course";
	public static final String TYPE_CATALOG = "catalog";
	
	public static final String HTTP_REQUEST_ATTRIBUT="catalog_node_id";
	private Controller runCtr;

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getTitle()
	 */
	public String getTitle() {
		String title = getConfiguration().get("title_" + getTranslator().getLocale().toString());
		if (title == null) {
			title = getTranslator().translate("institution.title");
		}
		return title;
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getDescription()
	 */
	public String getDescription() {
		String desc = getConfiguration().get("description_" + getTranslator().getLocale().toString());
		if (desc == null) {
			desc = getTranslator().translate("institution.description");
		}
		return desc;
	}

	/**
	 * @see org.olat.gui.control.generic.portal.AbstractPortlet#createInstance(org.olat.gui.control.WindowControl, org.olat.gui.UserRequest, java.util.Map)
	 */
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map<String,String> configuration) {
		if (institutions == null) init();
		InstitutionPortlet p = new InstitutionPortlet();
		p.setName(this.getName());
		p.setConfiguration(configuration);
		p.setTranslator(Util.createPackageTranslator(InstitutionPortlet.class, ureq.getLocale()));
		// override css class if configured
		String cssClass = configuration.get("cssWrapperClass");
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

	@Override
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
		
		File configurationFile = new File(WebappHelper.getContextRealPath(CONFIG_FILE));
		XStream xstream = getInstitutionConfigXStream();
		InstitutionConfiguration configuration = (InstitutionConfiguration)xstream.fromXML(configurationFile);
		
		for(InstitutionPortletEntry institution: configuration.getInstitution()) {
			String shortName = institution.getShortname();
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
		
		XStream.setupDefaultSecurity(xstream);
		Class<?>[] types = new Class[] {
				InstitutionConfiguration.class, Value.class, PolymorphLinkElement.class, PolymorphLink.class,
				InstitutionPortletEntry.class, InstitutionPortletSupervisorEntry.class, InstitutionPortlet.class,
				ArrayList.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		
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

	private List<InstitutionPortletSupervisorEntry> supervisor;
	private List<PolymorphLink> polymorphlink;
	private Value logo;
	private Value name;
	private Value url;
	private String shortname;

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
		return logo == null ? null : logo.getValue();
	}

	/**
	 * @return Returns the institutionName.
	 */
	public String getInstitutionName() {
		return name == null ? null : name.getValue();
	}

	/**
	 * @return Returns the institutionUrl.
	 */
	public String getInstitutionUrl() {
		return url == null ? null : url.getValue();
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

	public List<InstitutionPortletSupervisorEntry> getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(List<InstitutionPortletSupervisorEntry> supervisor) {
		this.supervisor = supervisor;
	}

	public List<PolymorphLink> getPolymorphlink() {
		return polymorphlink;
	}

	public void setPolymorphlink(List<PolymorphLink> polymorphlink) {
		this.polymorphlink = polymorphlink;
	}

	public Value getLogo() {
		return logo;
	}

	public void setLogo(Value logo) {
		this.logo = logo;
	}

	public Value getName() {
		return name;
	}

	public void setName(Value name) {
		this.name = name;
	}

	public Value getUrl() {
		return url;
	}

	public void setUrl(Value url) {
		this.url = url;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
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
	private Value phone;
	private Value email;
	private Value person;
	private Value url;
	private Value blog;

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
		return blog == null ? null : blog.getValue();
	}

	/**
	 * @return Returns the supervisorMail.
	 */
	public String getSupervisorMail() {
		return email == null ? null : email.getValue();
	}

	/**
	 * @return Returns the supervisorPhone.
	 */
	public String getSupervisorPhone() {
		return phone == null ? null : phone.getValue();
	}

	public String getSupervisorPerson() {
		return person == null ? null : person.getValue();
	}

	public String getSupervisorURL() {
		return url == null ? null : url.getValue();
	}

	public Value getPhone() {
		return phone;
	}

	public void setPhone(Value phone) {
		this.phone = phone;
	}

	public Value getEmail() {
		return email;
	}

	public void setEmail(Value email) {
		this.email = email;
	}

	public Value getPerson() {
		return person;
	}

	public void setPerson(Value person) {
		this.person = person;
	}

	public Value getUrl() {
		return url;
	}

	public void setUrl(Value url) {
		this.url = url;
	}

	public Value getBlog() {
		return blog;
	}

	public void setBlog(Value blog) {
		this.blog = blog;
	}
}

class PolymorphLink {
	private String defaultId;
	private String linkType;
	private String linkText;
	private List<PolymorphLinkElement> element;

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
		return (element != null && !element.isEmpty());
	}

	protected String getLinkText() {
		return linkText;
	}

	public String getDefaultId() {
		return defaultId;
	}

	public void setDefaultId(String defaultId) {
		this.defaultId = defaultId;
	}

	public List<PolymorphLinkElement> getElement() {
		return element;
	}

	public void setElement(List<PolymorphLinkElement> element) {
		this.element = element;
	}

	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}

	public void setLinkText(String linkText) {
		this.linkText = linkText;
	}
}

class PolymorphLinkElement {
	protected static final String EQUALS = "equals";
	protected static final String STARTS_WITH = "starts_with";
	protected static final String CONTAINS = "contains";

	private String id;
	private String cond;
	private String value;
	private String attribute;

	public PolymorphLinkElement() {
		//
	}

	public int getAttrib() {
		if ("orgunit".equals(attribute)) {
			return 0;
		} else if ("studysubject".equals(attribute)) {
			return 1;
		}
		return -1;
	}

	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	public int getCondition() {
		if (STARTS_WITH.equals(cond)) {
			return 0;
		} else if (EQUALS.equals(cond)) {
			return 1;
		} else if (CONTAINS.equals(cond)) {
			return 2;
		}
		return -1;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCond() {
		return cond;
	}

	public void setCond(String cond) {
		this.cond = cond;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
}

class Value {
	private String value;

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
	private List<InstitutionPortletEntry> institution;
	
	public List<InstitutionPortletEntry> getInstitution() {
		if(institution == null) {
			return Collections.emptyList();
		}
		return institution;
	}

	public void setInstitution(List<InstitutionPortletEntry> institution) {
		this.institution = institution;
	}
}

package de.bps.olat.portal.institution;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  2 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InstitutionPortletXStream {
	
	public static XStream getXStream() {
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

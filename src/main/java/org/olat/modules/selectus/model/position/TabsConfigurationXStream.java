/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.position;

import java.util.EnumMap;
import java.util.Map;

import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 17 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TabsConfigurationXStream {
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	
	static {
		Class<?>[] types = new Class[] {
				TabsConfiguration.class, TabsConfiguration.Tab.class, TabConfiguration.class,
				Map.class, EnumMap.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		
		xstream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	public static String toXml(TabsConfiguration obj) {
		return xstream.toXML(obj);
	}
	
	public static TabsConfiguration fromXml(String xml) {
		return (TabsConfiguration)xstream.fromXML(xml);
	}
}

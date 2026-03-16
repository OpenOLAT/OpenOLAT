/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.attributes;

import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 17 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionAttributeDefinitionXStream {
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	
	static {
		Class<?>[] types = new Class[] {
			SeparatorConfiguration.class, SelectConfiguration.class, SelectConfiguration.Option.class,
			SelectConfiguration.Order.class, SelectConfiguration.Display.class,
			StaticTextConfiguration.class, StaticTextConfiguration.TextDisplay.class,
			TextConfiguration.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));

		xstream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	public static String toXml(Object obj) {
		return xstream.toXML(obj);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T fromXml(String xml, @SuppressWarnings("unused") Class<T> cl) {
		return (T)xstream.fromXML(xml);
	}
}

/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.letter;

import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;


/**
 * 
 * Initial date: 12 avr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LetterConfigurationXStream {
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	
	static {
		Class<?>[] types = new Class[] {
			LetterConfiguration.class, LetterLanguageConfiguration.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		
		xstream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	public static String toXml(LetterConfiguration obj) {
		return xstream.toXML(obj);
	}
	
	public static LetterConfiguration fromXml(String xml) {
		return (LetterConfiguration)xstream.fromXML(xml);
	}
}

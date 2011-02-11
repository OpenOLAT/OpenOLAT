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
* <p>
*/ 

package org.olat.ims.qti.editor.beecom.parser;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;

/**
 * @author rkulow
 */
public class ParserManager implements IParser {

	private static String PROPERTIES_FILENAME = "org/olat/ims/qti/editor/beecom/parser/qtiparser.properties";
	private HashMap parserMap = null;
	private static String PARSER_DEFAULT = "defaultparser";

	/**
	 * 
	 */
	public ParserManager() {
		this.init();
	}

	private void init() {
		this.parserMap = new HashMap();

		try {
			Properties prop = new Properties();
			prop.load(this.getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILENAME));
			Enumeration enumeration = prop.keys();
			while (enumeration.hasMoreElements()) {
				String key = (String) enumeration.nextElement();
				String value = prop.getProperty(key);
				this.parserMap.put(key, value);
			}
		} catch (Exception e) {
			//
		}

	}

	/**
	 * @param doc
	 * @return
	 */
	public Object parse(Document doc) {
		Element rootElement = doc.getRootElement();
		return this.parse(rootElement);
	}

	/**
	 * @see org.olat.ims.qti.editor.beecom.parser.IParser#parse(org.dom4j.Element)
	 */
	public Object parse(Element element) {
		try {
			if (element == null) return null;
			String name = element.getName();
			String parserClassName = null;
			Object tmpName = this.parserMap.get(name);
			if (tmpName == null) {
				parserClassName = (String) this.parserMap.get(PARSER_DEFAULT);
			} else {
				parserClassName = (String) tmpName;
			}
			if(Tracing.isDebugEnabled(ParserManager.class)){
				Tracing.logDebug("ELEMENTNAME:" + name + "PARSERNAME" + parserClassName,ParserManager.class);
			}
			Class parserClass = this.getClass().getClassLoader().loadClass(parserClassName);
			IParser parser = (IParser) parserClass.newInstance();
			return parser.parse(element);
		} catch (ClassNotFoundException e) {
			throw new OLATRuntimeException(this.getClass(), "Class not found in QTI editor", e);
		} catch (InstantiationException e) {
			throw new OLATRuntimeException(this.getClass(), "Instatiation problem in QTI editor", e);
		} catch (IllegalAccessException e) {
			throw new OLATRuntimeException(this.getClass(), "Illegal Access in QTI editor", e);
		}
	}

}
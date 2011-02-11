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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.olat.core.CoreBeanTypes;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * Description:<br>
 * Initial Date:  02.08.2005 <br>
 * @author Felix
 * @author guido
 */
public class ExtManager {
	private static OLog log = Tracing.createLoggerFor(ExtManager.class);
	
	private static ExtManager instance;
	private long timeOfExtensionStartup;
	private List<Extension> extensions;
	private Object lockObject = new Object();

	/**
	 * @return the instance
	 */
	public static ExtManager getInstance() {
		if (instance == null) {
			return instance = (ExtManager) CoreSpringFactory.getBean("extManager");
		}
		return instance;
	}
	
	/**
	 * [used by spring]
	 */
	public ExtManager() {
		// for spring framework and..
		timeOfExtensionStartup = System.currentTimeMillis();
		instance = this;
	}

	/**
	 * @return the number of extensions
	 */
	public int getExtensionCnt() {
		return (getExtensions() == null? 0 : extensions.size());
	}

	/**
	 * @param i
	 * @return the extension at pos i
	 */
	public Extension getExtension(int i) {
		return getExtensions().get(i);
	}

	
	/**
	 * [used by spring]
	 * @return list
	 */
	public List<Extension> getExtensions() {
		if (extensions == null) {
			synchronized(lockObject) {
				if (extensions == null) {
					initExtentions();
				}
			}
		}
		return extensions;
	}

	/**
	 * @return the time when the extmanager was initialized
	 */
	public long getTimeOfExtensionStartup() {
		return timeOfExtensionStartup;
	}

	/**
	 * @param extensionPoint
	 * @param anExt
	 * @param addInfo additional info to log
	 */
	public void inform(Class extensionPoint, Extension anExt, String addInfo) {
		//Tracing.logAudit(this.getClass(), info: "+addInfo);		// TODO Auto-generated method stub		
	}
	
	private void initExtentions() {
		extensions = new ArrayList<Extension>();
		Map<Integer,Extension> sortedMap = new TreeMap<Integer,Extension>(); 
		Map<String, Object> extensionMap = CoreSpringFactory.getBeansOfType(CoreBeanTypes.extension);
		Collection<Object> extensionValues = extensionMap.values();
		// first build ordered list
		for (Object object : extensionValues) {
			Extension extension = (Extension) object;
			log.debug("initExtentions extention=" + extension);
			int key = extension.getOrder();
			while (sortedMap.containsKey(key) ) {
				// a key with this value already exist => add 1000 because offset must be outside of other values.
				key += 1000;
			}
			if ( key != extension.getOrder() ) {
				log.warn("Extension-Configuration Problem: Dublicate order-value ("+extension.getOrder()+") for extension=" + extension.getClass() + ", append extension at the end");
			}
			sortedMap.put(key, extension);
			log.debug("extension is enabled => add to list of extentions = " + extension);
		}
		for (Object key : sortedMap.keySet()) {
			extensions.add(sortedMap.get(key));
		}
	}

}

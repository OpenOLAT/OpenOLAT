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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;


/**
 * Description:<br>
 * Initial Date:  02.08.2005 <br>
 * @author Felix
 * @author guido
 */
public class ExtManager {

	private static final Logger log = Tracing.createLoggerFor(ExtManager.class);
	
	private static ExtManager instance;
	private long timeOfExtensionStartup;
	private List<Extension> extensions;
	private Object lockObject = new Object();
	
  private Map<Long,Extension> idExtensionlookup;
	
  private Map<ExtensionPointKeyPair,GenericActionExtension> navKeyGAExtensionlookup;
  
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
	 * returns the corresponding extension for a given unique extension id.
	 * if no Extension is found for the specified id, null is returned instead.
	 * 
	 * @param id
	 * @return the corresponding extension or null, if no extension is found for given id
	 */
	public Extension getExtensionByID(long id){
		if(idExtensionlookup.containsKey(id))
			return idExtensionlookup.get(id);
		else return null;
	}
	
	/**
	 * returns the GenericActionExtension that corresponds to the given NavKey. if
	 * no suiting GAE is found, null is returned. 
	 * 
	 * @param navKey
	 * @return the GenericActionExtension or null
	 */
	public GenericActionExtension getActionExtensionByNavigationKey(String extensionPoint, String navKey) {
		ExtensionPointKeyPair key = new ExtensionPointKeyPair(extensionPoint, navKey);
		if (navKeyGAExtensionlookup.containsKey(key)) {
			return navKeyGAExtensionlookup.get(key);
		}
		return null;
	}
	
	/**
	 * [used by spring]
	 * @return list
	 */
	public List<Extension> getExtensions() {
		if (extensions == null) {
			synchronized(lockObject) {
				if (extensions == null) {
					extensions = initExtentions();
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
	
	private ArrayList<Extension> initExtentions() {
		log.info("****** start loading extensions *********");
		Map<Integer, Extension> orderKeys = new HashMap<>();
		idExtensionlookup = new HashMap<>();
		navKeyGAExtensionlookup = new HashMap<>();
		
		ArrayList<Extension> extensionsList = new ArrayList<>();
		Map<String, Extension> extensionMap = CoreSpringFactory.getBeansOfType(Extension.class);
		Collection<Extension> extensionValues = extensionMap.values();

		int count_disabled = 0;
		int count_duplid = 0;
		AtomicInteger count_duplnavkey = new AtomicInteger(0);
		
		boolean debug = log.isDebugEnabled();
		
		// first build ordered list
		for (Extension extension : extensionValues) {
			if (!extension.isEnabled()) {
				count_disabled++;
				log.debug("* Disabled Extension got loaded :: " + extension + ".  Check that you don't use it or that extension returns null for getExtensionFor() when disabled, resp. overwrite isEnabled().");
			}
			int orderKey = extension.getOrder();
			
			if(orderKey == 0){
				//not configured via spring (order not set)
				log.debug("Extension-Configuration Warning: Order-value was not set for extension=" + extension + ", set order-value to config positionioning of extension...");
				if(extension instanceof AbstractExtension){
					((AbstractExtension)extension).setOrder(100000);
				}
			}
			if (orderKeys.containsKey(orderKey)) {
				Extension occupant = orderKeys.get(orderKey);
				if(debug) log.debug("Extension-Configuration Problem: Dublicate order-value ("+extension.getOrder()+") for extension=" + extension + ", orderKey already occupied by "+occupant);
			} else {
				orderKeys.put(orderKey, extension);
			}
		
			Long uid = CodeHelper.getUniqueIDFromString(extension.getUniqueExtensionID());
			if(idExtensionlookup.containsKey(uid)){
					count_duplid++;
					log.warn("Devel-Info :: duplicate unique id generated for extensions :: "+uid+" [ ["+idExtensionlookup.get(uid)+"]  and ["+extension+"] ]");
			}else{
				extensionsList.add(extension);
				idExtensionlookup.put(uid, extension);
				if (extension instanceof GenericActionExtension) {
					GenericActionExtension gAE = (GenericActionExtension) extension;
					if (StringHelper.containsNonWhitespace(gAE.getNavigationKey()) && gAE.getExtensionPoints() != null) {
						List<String>extensionPoints = gAE.getExtensionPoints();
						for(String extensionPoint:extensionPoints) {
							ExtensionPointKeyPair key = new ExtensionPointKeyPair(extensionPoint, gAE.getNavigationKey());
							append(key, gAE, count_duplnavkey);
							List<String> alternativeNavigationKeys = gAE.getAlternativeNavigationKeys();
							if(alternativeNavigationKeys != null && alternativeNavigationKeys.size() > 0) {
								for(String alternativeNavigationKey:alternativeNavigationKeys) {
									ExtensionPointKeyPair altKey = new ExtensionPointKeyPair(extensionPoint, alternativeNavigationKey);
									append(altKey, gAE, count_duplnavkey);
								}
							}
						}
					}
				}
			}
			if(debug) log.debug("Created unique-id "+uid+" for extension:: "+extension);
		}
		log.info("Devel-Info :: initExtensions done. :: "+count_disabled+" disabled Extensions, "+count_duplid+" extensions with duplicate ids, "+count_duplnavkey+ " extensions with duplicate navigationKeys");
		Collections.sort(extensionsList);
		return extensionsList;
	}
	
	private void append(ExtensionPointKeyPair key, GenericActionExtension gAE, AtomicInteger countDuplicate) {
		if (navKeyGAExtensionlookup.containsKey(key)) {
			log.info("Devel-Info :: duplicate navigation-key for extension :: {}", key.navigationKey);
			countDuplicate.incrementAndGet();
		} else {
			navKeyGAExtensionlookup.put(key, gAE);
		}
	}
	
	private static class ExtensionPointKeyPair {
		private String extensionPoint;
		private String navigationKey;
		
		public ExtensionPointKeyPair(String extensionPoint, String navigationKey) {
			this.extensionPoint = extensionPoint;
			this.navigationKey = navigationKey;
		}
		@Override
		public int hashCode() {
			return (extensionPoint  == null ? 9967811 : extensionPoint.hashCode()) + 
					(navigationKey == null ? 8544 : navigationKey.hashCode());
		}
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof ExtensionPointKeyPair) {
				ExtensionPointKeyPair pair = (ExtensionPointKeyPair)obj;
				return extensionPoint != null && extensionPoint.equals(pair.extensionPoint)
						&& navigationKey != null && navigationKey.equals(pair.navigationKey);
			}
			
			return false;
		}
	}

}

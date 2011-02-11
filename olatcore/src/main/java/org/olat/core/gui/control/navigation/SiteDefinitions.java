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

package org.olat.core.gui.control.navigation;

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
 * TODO: Felix Jost Class Description for Sites
 * 
 * <P>
 * Initial Date:  12.07.2005 <br>
 *
 * @author Felix Jost
 */
public class SiteDefinitions {
	private static OLog log = Tracing.createLoggerFor(SiteDefinitions.class);

	private List<SiteDefinition> siteDefList;
	private Object lockObject = new Object();

	/**
	 * 
	 */
	public SiteDefinitions() {
		// Does NOT call initSiteDefinitionList() here because we are not sure if all SiteDef-beans are loaded !
		// and we won't to define spring depends-on
	}
	
	private void initSiteDefinitionList() {
		siteDefList = new ArrayList<SiteDefinition>();
		Map sortedMap = new TreeMap(); 
		Map<String, Object> siteDefMap = CoreSpringFactory.getBeansOfType(CoreBeanTypes.siteDefinition);
		Collection<Object> siteDefValues = siteDefMap.values();
		for (Object object : siteDefValues) {
			SiteDefinition siteDefinition = (SiteDefinition) object;
			if (siteDefinition.isEnabled()) {
				int key = siteDefinition.getOrder();
				while (sortedMap.containsKey(key) ) {
					// a key with this value already exist => add 1000 because offset must be outside of other values.
					key += 1000;
				}
				if ( key != siteDefinition.getOrder() ) {
					log.warn("SiteDefinition-Configuration Problem: Dublicate order-value for siteDefinition=" + siteDefinition + ", append siteDefinition at the end");
				}
				sortedMap.put(key, siteDefinition);
			} else {
				log.debug("Disabled siteDefinition=" + siteDefinition);
			}
		}
		
		for (Object key : sortedMap.keySet()) {
			siteDefList.add((SiteDefinition)sortedMap.get(key));	
		}
	}

	public List<SiteDefinition> getSiteDefList() {
		if (siteDefList == null) { // first try non-synchronized for better performance
			synchronized(lockObject) {
				if (siteDefList == null) { // check again in synchronized-block, only one may create list
					initSiteDefinitionList();
				}
			}
			
		}
		return new ArrayList<SiteDefinition>(siteDefList);
	}
}


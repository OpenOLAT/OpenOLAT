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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.login;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.stereotype.Service;

/**
 * Used to manage a list of all Controllers to be presented afterLogin.
 * 
 * 
 * Initial Date: 02.10.2009 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
@Service("afterLoginInterceptionManager")
public class AfterLoginInterceptionManager {
	private static final OLog log = Tracing.createLoggerFor(AfterLoginInterceptionManager.class);
	
	private List<Map<String, Object>> afterLoginControllerList;

	protected static List<Map<String,Object>> sortControllerListByOrder(List<Map<String,Object>> list2order){
	    int n = list2order.size();
	    for (int pass=1; pass < n; pass++) {  // count how many times
	        // This next loop becomes shorter and shorter
	        for (int i=0; i < n-pass; i++) {
	        	Map<String,Object> currentCtrConfig_1 = list2order.get(i);
	        	Map<String,Object> currentCtrConfig_2 = list2order.get(i+1);
	        	
	        	int order_1 = 1;
	        	int order_2 = 1;
	        	if (currentCtrConfig_1.containsKey(AfterLoginInterceptionController.ORDER_KEY)) {
	    				order_1 = Integer.parseInt(currentCtrConfig_1.get(AfterLoginInterceptionController.ORDER_KEY).toString());
	        	}
	        	if (currentCtrConfig_2.containsKey(AfterLoginInterceptionController.ORDER_KEY)) {
	    				order_2 = Integer.parseInt(currentCtrConfig_2.get(AfterLoginInterceptionController.ORDER_KEY).toString());
	        	}
	            if (order_1 > order_2) {
	            	Collections.swap(list2order,i,i+1);
	            }
	        }
	    }
		return list2order;
	}
	
	/**
	 * @return Returns the afterLoginControllerList.
	 */
	protected List<Map<String, Object>> getAfterLoginControllerList() {
		return afterLoginControllerList;
	}

	/**
	 * appends one or more controllers to the list.
	 * Config should look like:
	 * 
	 * <list> 
	 * 	<map> 
	 * 		<entry key="controller"> 
	 * 			<bean class="org.olat.core.gui.control.creator.AutoCreator" singleton="false">
	 * 				<property name="className" value="org.olat.user.ProfileAndHomePageEditController"/> 
	 * 			</bean> 
	 * 		</entry>
	 * 		<entry key="forceUser"><value>true</value></entry> 
	 * 		<entry key="redoTimeout"><value>10</value></entry> 
	 * 		<entry key="i18nIntro"><value>org.olat.user:runonce.profile.intro</value></entry>
	 * 	</map> 
	 * </list>
	 * 
	 * @param aLConf
	 */
	public void addAfterLoginControllerConfig(AfterLoginConfig aLConf) {
		if (afterLoginControllerList == null) {
			afterLoginControllerList = new ArrayList<Map<String, Object>>();
		}
		log.info("added one or more afterLoginControllers to the list.");
		List<Map<String, Object>> ctrlList = aLConf.getAfterLoginControllerList();
		for (Iterator<Map<String, Object>> iterator = ctrlList.iterator(); iterator.hasNext();) {
			Map<String, Object> map = iterator.next();
			if (map.containsKey("controller-instance")) log.info("  controller-instance: " + map.get("controller-instance"));
			if (map.containsKey("controller")) log.info("  controller-key to instantiate: " + map.get("controller"));
			if (map.containsKey("forceUser")) log.info("  force User: " + map.get("forceUser"));
			if (map.containsKey("redoTimeout")) log.info("  redo-Timeout: " + map.get("redoTimeout"));
		}
		afterLoginControllerList.addAll(aLConf.getAfterLoginControllerList());
	}

	public boolean containsAnyController() {
		if (afterLoginControllerList != null && afterLoginControllerList.size() != 0) {
			return true;
		} else {
			return false;
		}
	}
}

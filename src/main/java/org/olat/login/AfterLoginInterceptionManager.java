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
import java.util.List;
import java.util.Map;

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

	private List<Map<String, Object>> afterLoginControllerList;
	
	/**
	 * @return Returns the afterLoginControllerList.
	 */
	protected List<Map<String, Object>> getAfterLoginControllerList() {
		return afterLoginControllerList;
	}
	
	public List<LoginInterceptorConfiguration> getInterceptorsConfiguration() {
		List<LoginInterceptorConfiguration> interceptors = new ArrayList<>();
		for(Map<String, Object> afterLoginController:afterLoginControllerList) {
			interceptors.add(new LoginInterceptorConfiguration(afterLoginController));
		}
		return interceptors;
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
			afterLoginControllerList = new ArrayList<>();
		}
		afterLoginControllerList.addAll(aLConf.getAfterLoginControllerList());
	}

	public boolean containsAnyController() {
		return afterLoginControllerList != null && !afterLoginControllerList.isEmpty();
	}
}

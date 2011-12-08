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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.search.service.indexer;

import org.springframework.beans.factory.FactoryBean;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 sept. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
//fxdiff FXOLAT-221: start indexer at different times for each instance
public class IndexCronGenerator implements FactoryBean<String> {
	
	private int tomcatId;
	private String enabled;
	
	/**
	 * [used by Spring]
	 * @param tomcatId
	 */
	public void setTomcatId(int tomcatId) {
		this.tomcatId = tomcatId;
	}
	
	/**
	 * [used by Spring]
	 * @param enabled
	 */
	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}
	
	public boolean isCronEnabled() {
		return "enabled".equals(enabled);
	}
	
	public String getCron() {
		int shiftHours = tomcatId % 4;
		String hours;
		switch(shiftHours) {
			case 0: hours = "0,4,8,12,16,20"; break;
			case 1: hours = "1,5,9,13,17,21"; break;
			case 2: hours = "2,6,10,14,18,22"; break;
			default: hours = "3,7,11,15,19,23";
		}
		
		int shiftMinuts = tomcatId % 6;
		return "0 " + shiftMinuts + "0 " + hours + " * * ? *";
	}

	@Override
	public String getObject() throws Exception {
		return getCron();
	}


	@Override
	public Class<?> getObjectType() {
		return String.class;
	}


	@Override
	public boolean isSingleton() {
		return true;
	}
	
	

}

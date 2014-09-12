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
package org.olat.search.service.indexer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * Initial date: 04.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class JmsIndexWork implements Serializable {

	private static final long serialVersionUID = 8790611181901676640L;
	
	public static final String INDEX = "index";
	public static final String DELETE = "delete";
	
	private String indexType;
	private List<Long> keyList;
	private String action;
	
	public JmsIndexWork() {
		//
	}
	
	public JmsIndexWork(String action, String indexType, Long key) {
		this.action = action;
		this.indexType = indexType;
		this.keyList = Collections.singletonList(key);
	}
	
	public JmsIndexWork(String action, String indexType, List<Long> keyList) {
		this.action = action;
		this.indexType = indexType;
		this.keyList = new ArrayList<>(keyList);
	}
	
	public String getIndexType() {
		return indexType;
	}
	
	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}
	
	public List<Long> getKeyList() {
		return keyList;
	}
	
	public void setKeyList(List<Long> keyList) {
		this.keyList = keyList;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}

/**
 * <a href=“http://www.openolat.org“>
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
 * 2011 by frentix GmbH, http://www.frentix.com
 * <p>
**/
package org.olat.group;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchBusinessGroupParams {
	
	private List<String> types;
	private List<String> tools;
	
	public List<String> getTypes() {
		return types;
	}
	
	public void setTypes(List<String> types) {
		this.types = types;
	}
	
	public void addTypes(String... types) {
		if(this.types == null) {
			this.types = new ArrayList<String>();
		}
		for(String type:types) {
			this.types.add(type);
		}
	}

	public List<String> getTools() {
		return tools;
	}

	public void setTools(List<String> tools) {
		this.tools = tools;
	}
	
	public void addTools(String... tools) {
		if(this.tools == null) {
			this.tools = new ArrayList<String>();
		}
		for(String tool:tools) {
			this.tools.add(tool);
		}
	}
}

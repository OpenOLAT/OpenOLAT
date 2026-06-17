/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.model.mail;

/**
 * 
 * Initial date: 24 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionMailTemplateRef {
	
	private Long key;
	private final String id;
	private final String name;
	
	public PositionMailTemplateRef(Long key, String id, String name) {
		this.key = key;
		this.id = id;
		this.name = name;
	}

	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) { 
		this.key = key;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public boolean match(String templateName) {
		return (key != null && key.toString().equals(templateName))
				|| (id != null && id.equalsIgnoreCase(templateName))
				|| (name != null && name.equalsIgnoreCase(templateName));
	}
}

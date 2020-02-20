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
package org.olat.repository.ui.catalog;

import java.util.Date;

import org.olat.repository.CatalogEntry;

/**
 * 
 * Initial date: 04.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NodeEntryRow {

	private Long key;
	private String name;
	private Date creationDate;
	private int position;
	
	public NodeEntryRow(CatalogEntry view) {
		key = view.getKey();
		name = view.getName();
		position = view.getPosition() == null ? 0 :view.getPosition().intValue();
		creationDate = view.getCreationDate();
	}
	
	public String getCssClass() {
		return "o_CourseModule_icon";
	}
	
	public Long getKey() {
		return key;
	}
	
	public String getDisplayname() {
		return name;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public int getPosition() {
		return position;
	}
}
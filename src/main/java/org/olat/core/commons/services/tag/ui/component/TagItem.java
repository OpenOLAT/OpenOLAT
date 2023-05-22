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
package org.olat.core.commons.services.tag.ui.component;

import org.olat.core.gui.components.form.flexible.elements.FormLink;

/**
 * 
 * Initial date: 8 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TagItem {

	private Long key;
	private String displayValue;
	private long count;
	private boolean selected;
	private FormLink link;
	
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getDisplayValue() {
		return displayValue;
	}
	
	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
	}
	
	public long getCount() {
		return count;
	}
	
	public void setCount(long count) {
		this.count = count;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public FormLink getLink() {
		return link;
	}
	
	public void setLink(FormLink link) {
		this.link = link;
	}
	
	public String getLinkName() {
		return link != null? link.getComponent().getComponentName(): null;
	}
	
}

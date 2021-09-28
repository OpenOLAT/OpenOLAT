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
package org.olat.core.gui.components.form.flexible.elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 
 * 
 * Initial date: 10 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiTableFilterValue implements Serializable {

	private static final long serialVersionUID = 8328393974165121258L;
	
	private String filter;
	private Serializable value;
	
	public FlexiTableFilterValue(String filter, Serializable value) {
		this.filter = filter;
		this.value = value;
	}
	
	public static final FlexiTableFilterValue valueOf(Enum<?> filter, Serializable value) {
		return new FlexiTableFilterValue(filter.name(), value);
	}
	
	public static final FlexiTableFilterValue valueOf(String filter, Serializable value) {
		return new FlexiTableFilterValue(filter, value);
	}
	
	public static final FlexiTableFilterValue valueOf(Enum<?> filter, List<String> values) {
		return valueOf(filter.name(), values);
	}
	
	public static final FlexiTableFilterValue valueOf(String filter, List<String> values) {
		return new FlexiTableFilterValue(filter, new ArrayList<>(values));
	}
	
	public String getFilter() {
		return filter;
	}
	
	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Serializable value) {
		this.value = value;
	}
}

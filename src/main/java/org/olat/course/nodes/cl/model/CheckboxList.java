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
package org.olat.course.nodes.cl.model;

import java.beans.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * This object ist serialized in the course xml.
 * 
 * Initial date: 06.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckboxList implements Serializable {

	private static final long serialVersionUID = -5703947579583559550L;
	
	private List<Checkbox> list;
	
	@Transient
	public int getNumOfCheckbox() {
		return list == null ? 0 : list.size();
	}

	public List<Checkbox> getList() {
		return list;
	}

	public void setList(List<Checkbox> list) {
		this.list = list;
	}
	
	public void add(Checkbox checkbox) {
		if(list == null) {
			list = new ArrayList<>();
			list.add(checkbox);
		} else if(!list.contains(checkbox)) {
			list.add(checkbox);
		}
	}
	
	public void remove(Checkbox checkbox) {
		if(list != null) {
			list.remove(checkbox);
		}
	}
}

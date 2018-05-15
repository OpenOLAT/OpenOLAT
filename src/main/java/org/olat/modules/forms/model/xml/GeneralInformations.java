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
package org.olat.modules.forms.model.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.olat.modules.forms.model.xml.GeneralInformation.Type;

/**
 * 
 * Initial date: 14.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GeneralInformations extends AbstractElement {

	private static final long serialVersionUID = 8312618903131914158L;
	
	public static final String TYPE = "formgeneralinformations";
	
	private final Set<GeneralInformation> informations = new HashSet<>();

	@Override
	public String getType() {
		return TYPE;
	}
	
	public Collection<GeneralInformation> asCollection() {
		return new HashSet<>(informations);
	}
	
	public List<GeneralInformation> asOrderedList() {
		ArrayList<GeneralInformation> list = new ArrayList<>(informations);
		list.sort((i1, i2) -> Integer.compare(i1.getType().getOrder(), i2.getType().getOrder()));
		return list;
	}
	
	public void setEnable(GeneralInformation.Type type, boolean enable) {
		if (enable && !isEnabled(type)) {
			add(type);
		} else if (!enable && isEnabled(type)) {
			remove(type);
		}
	}

	private void add(Type type) {
		GeneralInformation information = new GeneralInformation();
		information.setId(UUID.randomUUID().toString());
		information.setType(type);
		informations.add(information);
	}

	private void remove(Type type) {
		informations.removeIf(information -> information.getType().equals(type));
	}

	private boolean isEnabled(GeneralInformation.Type type) {
		for (GeneralInformation information: informations) {
			if (type.equals(information.getType())) {
				return true;
			}
		}
		return false;
	}

}

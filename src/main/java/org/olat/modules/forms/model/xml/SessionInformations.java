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
import java.util.List;

/**
 * 
 * Initial date: 14.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SessionInformations extends AbstractElement {

	private static final long serialVersionUID = 8312618903131914158L;
	
	public static final String TYPE = "formsessioninformations";
	
	public enum Obligation {
		optional,
		mandatory,
		autofill
	}

	public enum InformationType {
		USER_FIRSTNAME(1),
		USER_LASTNAME(2),
		USER_EMAIL(3),
		AGE(4),
		USER_GENDER(5),
		USER_ORGUNIT(6),
		USER_STUDYSUBJECT(7);
		
		private int order;
		
		private InformationType(int order) {
			this.order = order;
		}
		
		public int getOrder() {
			return order;
		}
	}
	
	private Obligation obligation = Obligation.optional;
	private List<InformationType> informationTypes = new ArrayList<>();

	@Override
	public String getType() {
		return TYPE;
	}
	
	public Obligation getObligation() {
		return obligation;
	}

	public void setObligation(Obligation obligation) {
		this.obligation = obligation;
	}

	public List<InformationType> getInformationTypes() {
		return informationTypes;
	}

	public void setInformationTypes(List<InformationType> informationTypes) {
		this.informationTypes = informationTypes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SessionInformations other = (SessionInformations) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
	
}

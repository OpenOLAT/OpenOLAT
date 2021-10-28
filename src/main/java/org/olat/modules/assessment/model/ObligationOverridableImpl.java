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
package org.olat.modules.assessment.model;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.modules.assessment.ObligationOverridable;

/**
 * 
 * Initial date: 8 Oct 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ObligationOverridableImpl implements ObligationOverridable {
	
	private AssessmentObligation current;
	private AssessmentObligation inherited;
	private AssessmentObligation evaluated;
	// Configs
	private AssessmentObligation configCurrent;
	private AssessmentObligation configOriginal;
	private Identity modBy;
	private Date modDate;
	
	public ObligationOverridableImpl() {
		//
	}
	
	public ObligationOverridableImpl(AssessmentObligation current) {
		this.current = current;
	}

	public ObligationOverridableImpl(AssessmentObligation current, AssessmentObligation inherited, AssessmentObligation evaluated,
			AssessmentObligation configCurrent, AssessmentObligation configOriginal, Identity modBy, Date modDate) {
		this.current = current;
		this.inherited = inherited;
		this.evaluated = evaluated;
		this.configCurrent = configCurrent;
		this.configOriginal = configOriginal;
		this.modBy = modBy;
		this.modDate = modDate;
	}
	
	@Override
	public AssessmentObligation getCurrent() {
		return current;
	}

	@Override
	public void setCurrent(AssessmentObligation current) {
		this.current = current;
	}

	@Override
	public AssessmentObligation getInherited() {
		return inherited;
	}

	@Override
	public void setInherited(AssessmentObligation inherited) {
		this.inherited = inherited;
	}
	
	@Override
	public AssessmentObligation getEvaluated() {
		return evaluated;
	}

	@Override
	public void setEvaluated(AssessmentObligation evaluated) {
		this.evaluated = evaluated;
	}

	@Override
	public AssessmentObligation getConfigCurrent() {
		return configCurrent;
	}

	@Override
	public void setConfigCurrent(AssessmentObligation configCurrent) {
		if (isOverridden()) {
			this.configOriginal = configCurrent;
		} else {
			this.configCurrent = configCurrent;
		}
	}
	
	@Override
	public AssessmentObligation getConfigOriginal() {
		return configOriginal;
	}

	@Override
	public Identity getModBy() {
		return modBy;
	}

	@Override
	public Date getModDate() {
		return modDate;
	}

	@Override
	public void overrideConfig(AssessmentObligation custom, Identity modBy, Date modDate) {
		if (!isOverridden()) {
			this.configOriginal = this.configCurrent;
		}
		this.configCurrent = custom;
		this.modBy = modBy;
		this.modDate = modDate;
	}

	@Override
	public boolean isOverridden() {
		return modDate != null;
	}
	
	@Override
	public void reset() {
		if (isOverridden()) {
			configCurrent = configOriginal;
		}
		configOriginal = null;
		modBy = null;
		modDate = null;
	}
	
	@Override
	public ObligationOverridable clone() {
		ObligationOverridableImpl clone = new ObligationOverridableImpl();
		clone.current = this.current;
		clone.inherited = this.inherited;
		clone.evaluated = this.evaluated;
		clone.configCurrent = this.configCurrent;
		clone.configOriginal = this.configOriginal;
		clone.modBy = this.modBy;
		clone.modDate = this.modDate;
		return clone;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ObligationOverridableImpl [current=");
		builder.append(current);
		builder.append(", inherited=");
		builder.append(inherited);
		builder.append(", evaluated=");
		builder.append(evaluated);
		builder.append(", configCurrent=");
		builder.append(configCurrent);
		builder.append(", configOriginal=");
		builder.append(configOriginal);
		builder.append(", modBy=");
		builder.append(modBy);
		builder.append(", modDate=");
		builder.append(modDate);
		builder.append("]");
		return builder.toString();
	}
	
}

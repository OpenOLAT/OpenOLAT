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
public class ObligationOverridableImpl extends OverridableImpl<AssessmentObligation> implements ObligationOverridable {
	
	private AssessmentObligation currentConfig;
	
	public ObligationOverridableImpl() {
		//
	}
	
	public ObligationOverridableImpl(AssessmentObligation current) {
		super(current);
	}
	
	public ObligationOverridableImpl(AssessmentObligation current, AssessmentObligation currentConfig,
			AssessmentObligation original, Identity modBy, Date modDate) {
		super(current, original, modBy, modDate);
		this.currentConfig = currentConfig;
	}
	
	@Override
	public AssessmentObligation getCurrentConfig() {
		return currentConfig;
	}

	@Override
	public boolean isEvaluatedConfig() {
		return AssessmentObligation.evaluated == currentConfig;
	}

	@Override
	public void setEvaluated(AssessmentObligation evaluatedObligation) {
		if (isEvaluatedConfig()) {
			super.setCurrentIntern(evaluatedObligation);
		}
	}

	@Override
	protected void setCurrentIntern(AssessmentObligation current) {
		if (AssessmentObligation.evaluated == current) {
			super.setCurrentIntern(null);
			currentConfig = current;
		} else {
			super.setCurrentIntern(current);
			currentConfig = null;
		}
	}

	@Override
	protected AssessmentObligation getCurrentIntern() {
		if (AssessmentObligation.evaluated == currentConfig) {
			return currentConfig;
		}
		return super.getCurrentIntern();
	}

	@Override
	public ObligationOverridable clone() {
		ObligationOverridableImpl clone = new ObligationOverridableImpl();
		super.cloneValues(clone);
		clone.currentConfig = currentConfig;
		return clone;
	}

}

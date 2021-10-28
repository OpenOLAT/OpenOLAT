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
package org.olat.modules.assessment;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.model.ObligationOverridableImpl;

/**
 * 
 * Initial date: 8 Oct 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ObligationOverridable {
	
	public static ObligationOverridable empty() {
		return new ObligationOverridableImpl();
	}
	
	public static ObligationOverridable of(AssessmentObligation current) {
		return new ObligationOverridableImpl(current);
	}
	
	/** 
	 * The valid current obligation.
	 *
	 * @return
	 */
	public AssessmentObligation getCurrent();
	
	public void setCurrent(AssessmentObligation current);
	
	public AssessmentObligation getInherited();
	
	public void setInherited(AssessmentObligation inherited);
	
	public AssessmentObligation getEvaluated();
	
	public void setEvaluated(AssessmentObligation evaluated);
	
	public AssessmentObligation getConfigCurrent();

	public void setConfigCurrent(AssessmentObligation configCurrent);
	
	public AssessmentObligation getConfigOriginal();
	
	public Identity getModBy();

	public Date getModDate();

	/**
	 * Overrides the current config by a custom config. The original value is set to the
	 * prior current value if not already set.
	 *
	 * @param configCustom
	 * @param by
	 * @param at
	 */
	public void overrideConfig(AssessmentObligation configCustom, Identity by, Date at);
	
	public boolean isOverridden();
	
	/**
	 * Reset the current value to the original value and deletes the override informations.
	 *
	 */
	public void reset();

	public ObligationOverridable clone();

}

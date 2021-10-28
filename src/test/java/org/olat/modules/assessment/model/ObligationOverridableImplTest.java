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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 09. Sept 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ObligationOverridableImplTest {
	
	@Test
	public void shouldSetValues() {
		ObligationOverridableImpl sut = new ObligationOverridableImpl();
		
		sut.setCurrent(AssessmentObligation.evaluated);
		sut.setInherited(AssessmentObligation.excluded);
		sut.setEvaluated(AssessmentObligation.mandatory);
		sut.setConfigCurrent(AssessmentObligation.optional);
		
		assertThat(sut.getCurrent()).isEqualTo(AssessmentObligation.evaluated);
		assertThat(sut.getInherited()).isEqualTo(AssessmentObligation.excluded);
		assertThat(sut.getEvaluated()).isEqualTo(AssessmentObligation.mandatory);
		assertThat(sut.getConfigCurrent()).isEqualTo(AssessmentObligation.optional);
		assertThat(sut.getConfigOriginal()).isNull();
		assertThat(sut.getModBy()).isNull();
		assertThat(sut.getModDate()).isNull();
	}
	
	@Test
	public void shouldOverrideAndReset() {
		Identity identity = mock(Identity.class);
		Date modDate = new GregorianCalendar(2020, 2, 19).getTime();
		ObligationOverridableImpl sut = new ObligationOverridableImpl();
		sut.setCurrent(AssessmentObligation.optional);
		sut.setConfigCurrent(AssessmentObligation.excluded);
		
		sut.overrideConfig(AssessmentObligation.mandatory, identity, modDate);
		
		assertThat(sut.getCurrent()).isEqualTo(AssessmentObligation.optional);
		assertThat(sut.getConfigCurrent()).isEqualTo(AssessmentObligation.mandatory);
		assertThat(sut.getConfigOriginal()).isEqualTo(AssessmentObligation.excluded);
		assertThat(sut.getModBy()).isEqualTo(identity);
		assertThat(sut.getModDate()).isEqualTo(modDate);
		
		sut.reset();
		
		assertThat(sut.getCurrent()).isEqualTo(AssessmentObligation.optional);
		assertThat(sut.getConfigCurrent()).isEqualTo(AssessmentObligation.excluded);
		assertThat(sut.getConfigOriginal()).isNull();
		assertThat(sut.getModBy()).isNull();
		assertThat(sut.getModDate()).isNull();
	}
	
	@Test
	public void shouldBeOverriden() {
		AssessmentObligation custom = AssessmentObligation.mandatory;
		Identity identity = mock(Identity.class);
		Date modDate = new GregorianCalendar(2020, 2, 19).getTime();
		ObligationOverridableImpl sut = new ObligationOverridableImpl();
		assertThat(sut.isOverridden()).isFalse();
		
		sut.overrideConfig(custom, identity, modDate);
		assertThat(sut.isOverridden()).isTrue();
		
		sut.reset();
		assertThat(sut.isOverridden()).isFalse();
	}

}

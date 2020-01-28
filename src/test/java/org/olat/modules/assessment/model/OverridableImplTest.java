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
 * Initial date: 28 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OverridableImplTest {
	
	@Test
	public void shouldSetCurrent() {
		String current = "current";
		OverridableImpl<String> sut = new OverridableImpl<>();
		
		sut.setCurrent(current);
		
		assertThat(sut.getCurrent()).isEqualTo(current);
		assertThat(sut.getOriginal()).isNull();
		assertThat(sut.getModBy()).isNull();
		assertThat(sut.getModDate()).isNull();
	}
	
	@Test
	public void shouldOverride() {
		String original = "original";
		String custom = "custom";
		Identity identity = mock(Identity.class);
		Date modDate = new GregorianCalendar(2020, 2, 19).getTime();
		OverridableImpl<String> sut = new OverridableImpl<>();
		sut.setCurrent(original);
		
		sut.override(custom, identity, modDate);
		
		assertThat(sut.getCurrent()).isEqualTo(custom);
		assertThat(sut.getOriginal()).isEqualTo(original);
		assertThat(sut.getModBy()).isEqualTo(identity);
		assertThat(sut.getModDate()).isEqualTo(modDate);
	}

	@Test
	public void shouldSetCurrentOfOverridden() {
		String original = "original";
		String original2 = "original2";
		String custom = "custom";
		Identity identity = mock(Identity.class);
		Date modDate = new GregorianCalendar(2020, 2, 19).getTime();
		OverridableImpl<String> sut = new OverridableImpl<>();
		sut.setCurrent(original);
		sut.override(custom, identity, modDate);
		
		sut.setCurrent(original2);
		
		assertThat(sut.getCurrent()).isEqualTo(custom);
		assertThat(sut.getOriginal()).isEqualTo(original2);
		assertThat(sut.getModBy()).isEqualTo(identity);
		assertThat(sut.getModDate()).isEqualTo(modDate);
	}
	
	@Test
	public void shouldReset() {
		String original = "original";
		String custom = "custom";
		Identity identity = mock(Identity.class);
		Date modDate = new GregorianCalendar(2020, 2, 19).getTime();
		OverridableImpl<String> sut = new OverridableImpl<>();
		sut.setCurrent(original);
		sut.override(custom, identity, modDate);
		
		sut.reset();
		
		assertThat(sut.getCurrent()).isEqualTo(original);
		assertThat(sut.getOriginal()).isNull();
		assertThat(sut.getModBy()).isNull();
		assertThat(sut.getModDate()).isNull();
	}


}

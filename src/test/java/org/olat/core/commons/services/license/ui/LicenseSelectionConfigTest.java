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
package org.olat.core.commons.services.license.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseHandler;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.manager.TestableLicenseHandler;
import org.olat.core.commons.services.license.model.ResourceLicenseImpl;
import org.olat.core.commons.services.license.model.LicenseTypeImpl;

/**
 * 
 * Initial date: 22.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LicenseSelectionConfigTest {
	
	private static final LicenseHandler LICENSE_HANDLER = new TestableLicenseHandler();
	
	private static final String INACTIVE_LICENSE_KEY = "100";
	private static final String KEY1 = "1";
	private static final String KEY2 = "2";
	private static final String KEY3 = "3";
	
	@Mock
	private LicenseService licenseServiceMock;
	
	private License license = new ResourceLicenseImpl();
	
	private LicenseSelectionConfig sut;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(licenseServiceMock.loadActiveLicenseTypes(any())).thenReturn(getActiveLicenseTypes());
	}

	@Test
	public void shouldBeMandatoryIfNoLicenseTypeIsInactive() {
		when(licenseServiceMock.isNoLicense(any())).thenReturn(Boolean.FALSE);
		
		license.setLicenseType(getActiveLicenseTypes().get(0));
		sut = new LicenseSelectionConfig(licenseServiceMock, LICENSE_HANDLER, license.getLicenseType());
		
		boolean licenseMandatory = sut.isLicenseMandatory();
		
		assertThat(licenseMandatory).isTrue();
	}

	@Test
	public void shouldBeNotMandatoryIfNoLicenseTypeIsActive() {
		when(licenseServiceMock.isNoLicense(any())).thenReturn(Boolean.TRUE);
		
		license.setLicenseType(getActiveLicenseTypes().get(0));
		sut = new LicenseSelectionConfig(licenseServiceMock, LICENSE_HANDLER, license.getLicenseType());
		
		boolean licenseMandatory = sut.isLicenseMandatory();
		
		assertThat(licenseMandatory).isFalse();
	}
	
	@Test
	public void shouldReturnActiveLicenseTypes() {
		license.setLicenseType(getActiveLicenseTypes().get(0));
		sut = new LicenseSelectionConfig(licenseServiceMock, LICENSE_HANDLER, license.getLicenseType());
		
		String[] licenseTypeKeys = sut.getLicenseTypeKeys();
		
		assertThat(licenseTypeKeys).containsExactly(KEY1, KEY2, KEY3);
	}

	@Test
	public void shouldAddInactiveLicenseTypeIfItIsTheActualType() {
		when(licenseServiceMock.isNoLicense(any())).thenReturn(Boolean.FALSE);
	
		license.setLicenseType(getInactiveLicenseType());
		sut = new LicenseSelectionConfig(licenseServiceMock, LICENSE_HANDLER, license.getLicenseType());

		String[] licenseTypeKeys = sut.getLicenseTypeKeys();
		
		assertThat(licenseTypeKeys).containsExactly(INACTIVE_LICENSE_KEY, KEY1, KEY2, KEY3);
	}
	
	@Test
	public void shouldAddNoLicenseType() {
		when(licenseServiceMock.isNoLicense(any())).thenReturn(Boolean.TRUE);
		
		license.setLicenseType(getInactiveLicenseType());
		sut = new LicenseSelectionConfig(licenseServiceMock, LICENSE_HANDLER, license.getLicenseType());

		String[] licenseTypeKeys = sut.getLicenseTypeKeys();
		
		assertThat(licenseTypeKeys).containsExactly(INACTIVE_LICENSE_KEY, KEY1, KEY2, KEY3);
	}

	@Test
	public void shouldReturnTheKeyOfTheActualLicenseType() {
		when(licenseServiceMock.isNoLicense(getInactiveLicenseType())).thenReturn(Boolean.FALSE);
		
		license.setLicenseType(getInactiveLicenseType());
		sut = new LicenseSelectionConfig(licenseServiceMock, LICENSE_HANDLER, license.getLicenseType());

		String key = sut.getSelectionLicenseTypeKey();
		
		assertThat(key).isEqualTo(INACTIVE_LICENSE_KEY);
	}
	
	@Test
	public void shouldReturnTheNoLicenseTypeKeyIfNotMandatory() {
		when(licenseServiceMock.isNoLicense(getActiveLicenseTypes().get(0))).thenReturn(Boolean.TRUE);
		
		license.setLicenseType(getActiveLicenseTypes().get(0));
		sut = new LicenseSelectionConfig(licenseServiceMock, LICENSE_HANDLER, license.getLicenseType());

		String key = sut.getSelectionLicenseTypeKey();
		
		assertThat(key).isEqualTo(KEY1);
	}
	
	@Test
	public void shouldNotReturnTheNoLicenseTypeKeyIfMandatory() {
		when(licenseServiceMock.isNoLicense(getInactiveLicenseType())).thenReturn(Boolean.TRUE);
		
		license.setLicenseType(getInactiveLicenseType());
		sut = new LicenseSelectionConfig(licenseServiceMock, LICENSE_HANDLER, license.getLicenseType());

		String key = sut.getSelectionLicenseTypeKey();
		
		assertThat(key).isNull();
	}

	private List<LicenseType> getActiveLicenseTypes() {
		List<LicenseType> activeLicenseTypes = new ArrayList<>(3);
		activeLicenseTypes.add(new TestableLicenseType(Long.parseLong(KEY1)));
		activeLicenseTypes.add(new TestableLicenseType(Long.parseLong(KEY2)));
		activeLicenseTypes.add(new TestableLicenseType(Long.parseLong(KEY3)));
		return activeLicenseTypes;
	}
	
	private LicenseType getInactiveLicenseType() {
		return new TestableLicenseType(Long.parseLong(INACTIVE_LICENSE_KEY));
	}
	
	@SuppressWarnings("serial")
	private static class TestableLicenseType extends LicenseTypeImpl {
		
		private final Long key;

		public TestableLicenseType(Long key) {
			this.key = key;
		}

		@Override
		public Long getKey() {
			return key;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			TestableLicenseType other = (TestableLicenseType) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			return true;
		}
		
	}

}

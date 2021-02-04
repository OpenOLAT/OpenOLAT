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
package org.olat.core.commons.services.doceditor.onlyoffice.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;

/**
 * 
 * Initial date: 26 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OnlyOfficeServiceImplTest {
	
	@Mock
	private OnlyOfficeModule onlyOfficeModuleMock;
	@Mock
	private DocEditorService documentEditorServiceMock;
	
	@InjectMocks
	private OnlyOfficeServiceImpl sut ;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void shouldAllowEditIfNoLicensesSet() {
		when(onlyOfficeModuleMock.getLicenseEdit()).thenReturn(null);
		
		boolean editLicenseAvailable = sut.isEditLicenseAvailable();
		
		assertThat(editLicenseAvailable).isTrue();
	}
	
	@Test
	public void shouldNotAllowEditIfZeroLicenses() {
		when(onlyOfficeModuleMock.getLicenseEdit()).thenReturn(0);
		
		boolean editLicenseAvailable = sut.isEditLicenseAvailable();
		
		assertThat(editLicenseAvailable).isFalse();
	}
	
	@Test
	public void shouldAllowEditIfNotAllLicensesInUse() {
		when(onlyOfficeModuleMock.getLicenseEdit()).thenReturn(10);
		when(documentEditorServiceMock.getAccessCount(any(), any())).thenReturn(Long.valueOf(4));
		
		boolean editLicenseAvailable = sut.isEditLicenseAvailable();
		
		assertThat(editLicenseAvailable).isTrue();
	}
	
	@Test
	public void shouldAllowEditIfAlmostAllLicensesInUse() {
		// Because the access is created before the license check is done,
		// the access is demanding for the last license.
		when(onlyOfficeModuleMock.getLicenseEdit()).thenReturn(10);
		when(documentEditorServiceMock.getAccessCount(any(), any())).thenReturn(Long.valueOf(10));
		
		boolean editLicenseAvailable = sut.isEditLicenseAvailable();
		
		assertThat(editLicenseAvailable).isTrue();
	}
	
	@Test
	public void shouldNotAllowEditIfAllLicensesInUse() {
		when(onlyOfficeModuleMock.getLicenseEdit()).thenReturn(10);
		when(documentEditorServiceMock.getAccessCount(any(), any())).thenReturn(Long.valueOf(11));
		
		boolean editLicenseAvailable = sut.isEditLicenseAvailable();
		
		assertThat(editLicenseAvailable).isFalse();
	}

}

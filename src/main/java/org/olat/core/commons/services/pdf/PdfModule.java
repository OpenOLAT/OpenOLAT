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
package org.olat.core.commons.services.pdf;

import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 6 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PdfModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final String PDF_ENABLED = "pdf.service.enabled";
	private static final String PDF_SPI = "pdf.service.spi";
	
	@Value("${pdf.service.enabled:false}")
	private boolean enabled;
	
	@Autowired
	private List<PdfSPI> pdfSpies;
	
	private PdfSPI pdfSpi;
	
	public PdfModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabledObj = getStringPropertyValue(PDF_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String spiObj = getStringPropertyValue(PDF_SPI, true);
		if(StringHelper.containsNonWhitespace(spiObj)) {
			for(PdfSPI spi:pdfSpies) {
				if(spiObj.equals(spi.getId())) {
					pdfSpi = spi;
				}
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(PDF_ENABLED, Boolean.toString(enabled), true);
	}
	
	public List<PdfSPI> getPdfServiceProviders() {
		return pdfSpies;
	}
	
	public PdfSPI getPdfServiceProvider() {
		return pdfSpi;
	}
	
	public void setPdfServiceProvider(PdfSPI pdfSpi) {
		this.pdfSpi = pdfSpi;
		if (pdfSpi == null) {
			removeProperty(PDF_SPI, true);
		} else {
			setStringProperty(PDF_SPI, pdfSpi.getId(), true);			
		}
	}
}

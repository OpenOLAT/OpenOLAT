/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.user;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 31 oct. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificatesPage {
	
	private WebDriver browser;
	
	public CertificatesPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CertificatesPage assertOnCertificatesOverviewPage() {
		By certificatesBy = By.className("o_certificates_overview_list");
		OOGraphene.waitElement(certificatesBy, browser);
		return this;
	}
	
	public CertificatesPage assertOnCertificate(String courseTitle) {
		By courseCertificateBy = By.xpath("//div[contains(@class,'o_certificates_list')]//div[@class='o_certificate_meta']//span[contains(text(),'" + courseTitle + "')]");
		OOGraphene.waitElement(courseCertificateBy, browser);
		return this;
	}
	
	public CertificatesPage assertOnValidCertificate(String courseTitle) {
		By courseCertificateBy = By.xpath("//div[contains(@class,'o_certificates_list')]//div[contains(@class,'o_certificate_card')][div/div[contains(@class,'o_certification_status_valid')]]//div[@class='o_certificate_meta']//span[contains(text(),'" + courseTitle + "')]");
		OOGraphene.waitElement(courseCertificateBy, browser);
		return this;
	}
	
	public CertificatesPage assertOnCertificateThumbnail(String courseTitle) {
		By courseCertificateBy = By.xpath("//div[contains(@class,'o_certificates_list')]//div[contains(@class,'o_certificate_card')][div/div[@class='o_certificate_img']/img[contains(@src,'.pdf.png')]]//div[@class='o_certificate_meta']//span[contains(text(),'" + courseTitle + "')]");
		OOGraphene.waitElement(courseCertificateBy, browser);
		return this;
	}

}

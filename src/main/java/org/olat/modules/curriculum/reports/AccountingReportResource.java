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
package org.olat.modules.curriculum.reports;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumAccountingSearchParams;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AccountingReportResource extends OpenXMLWorkbookResource {
	
	private final Locale locale;
	private final Identity manager;
	private final OLATResource resource;
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private AccountingReportConfiguration accountingReportConfiguration;
	
	public AccountingReportResource(String filename, OLATResource resource, Locale locale) {
		super(filename);
		this.locale = locale;
		this.resource = resource;
		this.manager = null;
	}
	
	public AccountingReportResource(Identity manager, Locale locale) {
		super("AccountBooking");
		this.locale = locale;
		this.resource = null;
		this.manager = manager;
	}

	@Override
	protected void generate(OutputStream out) {
		Set<CurriculumRef> curriculums = new HashSet<>();
		Set<CurriculumElementRef> implementations = new HashSet<>();
		if(resource != null) {
			CurriculumElement element = curriculumService.getCurriculumElement(resource);
			if(element != null) {
				CurriculumAccountingSearchParams searchParams = new CurriculumAccountingSearchParams();
				searchParams.setCurriculum(element.getCurriculum());
				searchParams.setCurriculumElement(element);
				accountingReportConfiguration.generateReport(searchParams, curriculums, implementations, locale, out);
			}
		} else {
			CurriculumAccountingSearchParams searchParams = new CurriculumAccountingSearchParams();
			searchParams.setIdentity(manager);
			accountingReportConfiguration.generateReport(searchParams, curriculums, implementations, locale, out);
		}
	}
}

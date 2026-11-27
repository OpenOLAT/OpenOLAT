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
package org.olat.modules.coach.ui.manager;

import java.io.Serial;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.reports.AbstractReportTask;
import org.olat.modules.coach.reports.ReportConfiguration;
import org.olat.modules.coach.reports.ReportParameters;

/**
 * Initial date: 2025-06-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CoachingReportTask extends AbstractReportTask {

	@Serial
	private static final long serialVersionUID = 6361896212294337944L;

	public CoachingReportTask(String title, Identity identity, Locale locale, String templateId,
			ReportParameters params) {
		super(title, identity, locale, templateId, params);
	}

	@Override
	protected VFSContainer getOutputFolder(Identity doer) {
		return CoreSpringFactory.getImpl(CoachingService.class).getGeneratedReportsFolder(doer);
	}

	@Override
	protected void generateReport(ReportConfiguration template, Identity doer, ReportParameters parameters,
			Locale locale, VFSLeaf output, ExportMetadata metadata) {
		template.generateReport(doer, parameters, locale, (LocalFileImpl)output);
	}
}

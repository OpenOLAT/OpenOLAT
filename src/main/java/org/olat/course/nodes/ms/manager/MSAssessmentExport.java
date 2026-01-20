/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes.ms.manager;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfOutputOptions;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.commons.services.taskexecutor.TaskProgressCallback;
import org.olat.core.commons.services.taskexecutor.manager.PersistentTaskProgressCallback;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeExport;
import org.olat.course.assessment.ui.tool.AssessmentEvaluationFormExecutionController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ms.MSService;
import org.olat.course.nodes.ms.MSStatisticController;
import org.olat.course.nodes.ms.MSStatisticsExport;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.forms.EvaluationFormProvider;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Dec 15, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class MSAssessmentExport extends AssessmentCourseNodeExport {
	
	private static final Logger log = Tracing.createLoggerFor(MSAssessmentExport.class);
	
	private final EvaluationFormProvider evaluationFormProvider;
	
	@Autowired
	private MSService msService;
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private PdfService pdfService;

	public MSAssessmentExport(Identity doer, CourseEnvironment courseEnv, CourseNode courseNode,
			List<Identity> identities, boolean withNonParticipants, boolean withPdfs, Locale locale, WindowControl windowControl,
			EvaluationFormProvider evaluationFormProvider) {
		super(doer, courseEnv, courseNode, identities, withNonParticipants, withPdfs, locale, windowControl);
		this.evaluationFormProvider = evaluationFormProvider;
		
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public void export(ZipOutputStream zout, PersistentTaskProgressCallback progress) {
		super.export(zout, progress);
		exportFormPdfs(zout, progress);
		exportExcelResults(zout);
	}
	
	public void exportExcelResults(ZipOutputStream zout) {
		try {
			MSStatisticController statisticCtrl = new MSStatisticController(ureq, windowControl, courseEnv, identities, null, courseNode, evaluationFormProvider);
			MSStatisticsExport statisticsExport = new MSStatisticsExport();
			statisticsExport.export(zout, "", statisticCtrl.getTableComponnet(), statisticCtrl.getColumns(), statisticCtrl.getTranslator());
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	private void exportFormPdfs(ZipOutputStream zout, TaskProgressCallback progressCallback) {
		if (!withPdfs || !pdfModule.isEnabled()) {
			return;
		}
		
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		List<EvaluationFormSession> sessions = msService.getSessions(courseEntry, courseNode.getIdent(), evaluationFormProvider);
		Map<Long, EvaluationFormSession> identityKeyToSession = sessions
				.stream()
				.filter(session -> StringHelper.isLong(session.getSurvey().getIdentifier().getSubident2()))
				.collect(Collectors.toMap(session -> Long.valueOf(session.getSurvey().getIdentifier().getSubident2()), Function.identity()));
		if (identityKeyToSession.isEmpty()) {
			return;
		}
		
		boolean savedPointPassed = !StringHelper.containsNonWhitespace(startPoint);
		if(savedPointPassed) {
			progressCallback.setProgress(0.0f, "start-details");
		}
		
		Collections.sort(identities, new IdentityComparator());
		int numOfIdentities = identities.size();
		int modProgress = withPdfs ? 1 : 1000;
		for(int i=0; i<numOfIdentities; i++) {
			Identity assessedIdentity = identities.get(i);
			if(isCancelled() || assessedIdentity == null || assessedIdentity.getStatus() == null || assessedIdentity.getStatus().equals(Identity.STATUS_DELETED)) {
				continue;
			}
			EvaluationFormSession session = identityKeyToSession.get(assessedIdentity.getKey());
			if (session == null || session.getEvaluationFormSessionStatus() != EvaluationFormSessionStatus.done) {
				continue;
			}
			
			double progress;
			if(i == 0) {
				progress = 0.0d;
			} else {
				progress = i / (double)numOfIdentities;
			}
			savedPointPassed = savedPointPassed || assessedIdentity.getKey().toString().equals(startPoint);
			if(savedPointPassed && i % modProgress == 0) {
				progressCallback.setProgress(progress, assessedIdentity.getKey().toString());
			}
			
			exportFormPdf(zout, assessedIdentity);
		}
	}
	
	private void exportFormPdf(ZipOutputStream zout, Identity assessedIdentity) {
		try {
			String filesPath = "files/";
			User user = assessedIdentity.getUser();
			String name = user.getLastName()
					+ "_" + user.getFirstName()
					+ "_" + (StringHelper.containsNonWhitespace(user.getNickName()) ? user.getNickName() : assessedIdentity.getName());
			
			filesPath += StringHelper.transformDisplayNameToFileSystemName(name);
			
			createFormPdf(zout, filesPath, assessedIdentity);
		} catch (Exception e) {
			log.error("", e);
		}
		
	}
	
	private void createFormPdf(ZipOutputStream zout, String path, Identity assessedIdentity) {
		try {
			ControllerCreator printControllerCreator = (lureq, lwControl) -> {
				UserCourseEnvironment coachedCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(
						assessedIdentity, courseEnv);
				return new AssessmentEvaluationFormExecutionController(lureq, lwControl, coachedCourseEnv, false, false,
						courseNode, evaluationFormProvider);
			};
			
			zout.putNextEntry(new ZipEntry(path + "/form.pdf"));
			pdfService.convert(doer, printControllerCreator, windowControl, PdfOutputOptions.defaultOptions(), zout);
			zout.closeEntry();
		} catch(Exception e) {
			log.error("", e);
		}
	}

	private static class IdentityComparator implements Comparator<Identity> {
		
		@SuppressWarnings("null")
		@Override
		public int compare(Identity o1, Identity o2) {
			if(o1 == null && o2 == null) {
				return 0;
			} else if(o1 != null && o2 == null) {
				return -1;
			} else if(o1 == null && o2 != null) {
				return 1;
			}
			return o1.getKey().compareTo(o2.getKey());
		}
	}

}

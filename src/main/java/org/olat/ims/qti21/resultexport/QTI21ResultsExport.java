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
package org.olat.ims.qti21.resultexport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.pdf.PdfOutputOptions;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.commons.services.taskexecutor.TaskProgressCallback;
import org.olat.core.commons.services.taskexecutor.manager.DefaultTaskProgressCallback;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.DefaultGlobalSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.render.velocity.VelocityHelper;
import org.olat.core.gui.render.velocity.VelocityRenderDecorator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.gui.util.WindowControlMocker;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.archive.QTI21ArchiveFormat;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.ims.qti21.ui.AssessmentResultController;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ResultsExport {
	
	private static final Logger log = Tracing.createLoggerFor(QTI21ResultsExport.class);
	private static final AtomicLong counter = new AtomicLong(0l);
	
	private static final String DATA = "userdata/";
	private static final String SEP = File.separator;
	
	private final SimpleDateFormat assessmentDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final SimpleDateFormat displayDateFormat;
	private VelocityHelper velocityHelper;
	
	private List<Identity> identities;
	private QTICourseNode courseNode;
	private String exportFolderName;
	private Translator translator;
	private RepositoryEntry entry;
	private final boolean withPdfs;
	private final CourseEnvironment courseEnv;
	private final boolean withNonParticipants;
	private UserRequest ureq;
	
	private final Set<RepositoryEntry> testEntries = new HashSet<>();
	
	private final Identity identity;
	private final WindowControl windowControl;
	
	private String startPoint;
	private volatile boolean cancelled = false;
	
	@Autowired
	private PdfService pdfService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private MapperService mapperService;
	
	public QTI21ResultsExport(CourseEnvironment courseEnv, List<Identity> identities,
			boolean withNonParticipants, boolean withPdfs, QTICourseNode courseNode, String archivePath, Locale locale,
			Identity identity, WindowControl windowControl) {
		CoreSpringFactory.autowireObject(this);
		this.courseNode = courseNode;
		this.identities = identities;
		this.courseEnv = courseEnv;
		this.withPdfs = withPdfs;
		this.identity = identity;
		this.windowControl = windowControl;
		this.withNonParticipants = withNonParticipants;
		
		displayDateFormat = new SimpleDateFormat("HH:mm:ss");
		displayDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		ureq = new SyntheticUserRequest(new TransientIdentity(), locale, new UserSession());
		ureq.getUserSession().setRoles(Roles.userRoles());

		velocityHelper = VelocityHelper.getInstance();
		entry = courseEnv.getCourseGroupManager().getCourseEntry();
		translator = Util.createPackageTranslator(QTI21ResultsExport.class, locale);
		exportFolderName = ZipUtil.concat(archivePath, translator.translate("export.folder.name"));
	}
	
	public boolean isCancelled() {
		if(cancelled) return true;
		
		boolean interrupted = Thread.interrupted();
		if(interrupted) {
			cancelled = true;
		}
		return interrupted;
	}
	
	public void setStartPoint(String point) {
		this.startPoint = point;
	}
	
	public void exportTestResults(ZipOutputStream zout) throws IOException {
		exportTestResults(zout, new DefaultTaskProgressCallback());
	}

	/**
	 * Adds the result export to existing zip stream.
	 *
	 * @throws IOException
	 */
	public void exportTestResults(ZipOutputStream zout, TaskProgressCallback progressCallback) throws IOException {
		List<AssessedMember> assessedMembers = createAssessedMembersDetail(zout, progressCallback);
		
		if(!isCancelled()) {
			//convert velocity template to zip entry
			String membersHTML = createMemberListingHTML(assessedMembers);	
			convertToZipEntry(zout, exportFolderName + "/index.html", membersHTML);
			
			exportStaticMaterials(zout);
	
			//materials
			for(RepositoryEntry testEntry:testEntries) {
				copyTestMaterials(testEntry, zout);
			}
		}
	}
	
	public void exportExcelResults(ZipOutputStream zout) {
		for(RepositoryEntry testEntry:testEntries) {
			exportExcelResults(testEntry, zout);
		}
	}
	
	private void exportStaticMaterials(ZipOutputStream zout) {
		//Copy resource files or file trees to export file tree 
		File theme = new File(WebappHelper.getContextRealPath("/static/themes/light/theme.css"));
		ZipUtil.addFileToZip(exportFolderName + "/css/offline/qti/theme.css", theme, zout);
		File themeMap = new File(WebappHelper.getContextRealPath("/static/themes/light/theme.css.map"));
		ZipUtil.addFileToZip(exportFolderName + "/css/offline/qti/theme.css.map", themeMap, zout);
		File fontawesome = new File(WebappHelper.getContextRealPath("/static/font-awesome"));
		ZipUtil.addDirectoryToZip(fontawesome.toPath(), exportFolderName + "/css/font-awesome", zout);
		File qtiJs = new File(WebappHelper.getContextRealPath("/static/js/jquery/"));
		ZipUtil.addDirectoryToZip(qtiJs.toPath(), exportFolderName + "/js/jquery", zout);
	}
	
	private void exportExcelResults(RepositoryEntry testEntry, ZipOutputStream zout) {
		ArchiveOptions options = new ArchiveOptions();
		if(!withNonParticipants) {
			options.setIdentities(identities);
		}
		QTI21StatisticSearchParams searchParams = new QTI21StatisticSearchParams(options, testEntry, entry, courseNode.getIdent());
		if(withNonParticipants) {
			searchParams.setViewNonMembers(true);
		} else {
			searchParams.setLimitToIdentities(identities);
		}
		
		QTI21ArchiveFormat qaf = new QTI21ArchiveFormat(translator.getLocale(), searchParams);
		String label = StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName() + "_" + testEntry.getDisplayname())
				+ "_" + Formatter.formatDatetimeWithMinutes(new Date())
				+ ".xlsx";
		qaf.exportCourseElement(exportFolderName + "/" + label, zout);
	}
	
	private List<AssessedMember> createAssessedMembersDetail(ZipOutputStream zout, TaskProgressCallback progressCallback) throws IOException {
		AssessmentManager assessmentManager = courseEnv.getAssessmentManager();
		List<AssessmentEntry> assessmentEntries = assessmentManager.getAssessmentEntries(courseNode);
		Map<Identity,AssessmentEntry> assessmentEntryMap = assessmentEntries.stream()
				.collect(Collectors.toMap(AssessmentEntry::getIdentity, aEntry -> aEntry, (u, v) -> u)) ;

		int numOfIdentities = identities.size();
		Collections.sort(identities, new IdentityComparator());
		List<AssessedMember> assessedMembers = new ArrayList<>(numOfIdentities);
		
		boolean savedPointPassed = !StringHelper.containsNonWhitespace(startPoint);
		if(savedPointPassed) {
			progressCallback.setProgress(0.0f, "start-details");
		}

		for(int i=0; i<numOfIdentities; i++) {
			Identity assessedIdentity = identities.get(i);
			if(isCancelled() || assessedIdentity == null || assessedIdentity.getStatus() == null || assessedIdentity.getStatus().equals(Identity.STATUS_DELETED)) {
				continue;
			}
			
			double progress;
			if(i == 0) {
				progress = 0.0d;
			} else {
				progress = i / (double)numOfIdentities;
			}
			savedPointPassed = savedPointPassed || assessedIdentity.getKey().toString().equals(startPoint);
			if(savedPointPassed) {
				progressCallback.setProgress(progress, assessedIdentity.getKey().toString());
			}
			AssessmentEntry assessmentEntry = assessmentEntryMap.get(assessedIdentity);
			AssessedMember member = createAssessedMemberDetail(zout, assessedIdentity, assessmentEntry, savedPointPassed);
			assessedMembers.add(member);	
		}
		return assessedMembers;
	}
	
	private AssessedMember createAssessedMemberDetail(ZipOutputStream zout, Identity assessedIdentity,
			AssessmentEntry assessmentEntry, boolean saveFiles) throws IOException {
		AssessmentManager assessmentManager = courseEnv.getAssessmentManager();
		
		String nickname = assessedIdentity.getUser().getNickName();
		String firstName = assessedIdentity.getUser().getFirstName();
		String lastNameOrAnonymous = assessedIdentity.getUser().getLastName();
		if(!StringHelper.containsNonWhitespace(lastNameOrAnonymous)) {
			lastNameOrAnonymous = "anonym";
		}
		String nameOrAnonymous = lastNameOrAnonymous;
		if(StringHelper.containsNonWhitespace(firstName)) {
			nameOrAnonymous += "_" + firstName;
		}
		if(StringHelper.containsNonWhitespace(nickname)) {
			nameOrAnonymous += "_" + nickname;
		} else {
			nameOrAnonymous += "_" + assessedIdentity.getKey();
		}
		
		String names = StringHelper.transformDisplayNameToFileSystemName(nameOrAnonymous);
		String idDir = exportFolderName + "/" + DATA + names;
		idDir = idDir.endsWith(SEP) ? idDir : idDir + SEP;
		if(saveFiles) {
			createZipDirectory(zout, idDir);
		}
		
		//content of single assessed member		
		List<ResultDetail> assessments = createResultDetail(assessedIdentity, zout, idDir);
		List<File> assessmentDocuments = assessmentManager.getIndividualAssessmentDocuments(courseNode, assessedIdentity);
		
		Boolean passed = null;
		BigDecimal score = null;
		String obligationString = null;
		if(assessmentEntry != null) {
			passed = assessmentEntry.getPassed();
			score = assessmentEntry.getScore();
			if (assessmentEntry.getObligation() != null && assessmentEntry.getObligation().getCurrent() != null) {
				switch (assessmentEntry.getObligation().getCurrent()) {
				case mandatory:
					obligationString = translator.translate("obligation.mandatory"); break;
				case optional:
					obligationString = translator.translate("obligation.optional"); break;
				case excluded:
					obligationString = translator.translate("obligation.excluded"); break;
				default:
					break;
				}
			}
		}
		
		String linkToUser = idDir.replace(exportFolderName + "/", "") + "index.html";
		String memberEmail = userManager.getUserDisplayEmail(assessedIdentity, ureq.getLocale());
		AssessedMember member = new AssessedMember(nickname, lastNameOrAnonymous, firstName,
				memberEmail, assessments.size(), passed, score, obligationString, linkToUser);
		
		if(saveFiles) {
			try {
				String singleUserInfoHTML = createResultListingHTML(assessments, assessmentDocuments, member);
				convertToZipEntry(zout, idDir + "index.html", singleUserInfoHTML);
				
				//assessment documents
				for(File document:assessmentDocuments) {
					String assessmentDocDir = idDir + "Assessment_documents/" + document.getName();
					ZipUtil.addFileToZip(assessmentDocDir, document, zout);
				}
			} catch (IOException e) {
				log.error("", e);
			}
		}
		
		return member;
	}
	
	private List<ResultDetail> createResultDetail(Identity assessedIdentity, ZipOutputStream zout, String idDir) throws IOException {
		List<ResultDetail> assessments = new ArrayList<>();
		List<AssessmentTestSession> sessions = qtiService.getAssessmentTestSessions(entry, courseNode.getIdent(), assessedIdentity, true);
		for (AssessmentTestSession session : sessions) {
			Long assessmentID = session.getKey();
			String idPath = idDir + translator.translate("table.user.attempt") + (sessions.indexOf(session)+1) + SEP;
			createZipDirectory(zout, idPath);	
			// content of result table
			String resultsPath = idPath.replace(idDir, "") + assessmentID;
			ResultDetail resultDetail = new ResultDetail(assessmentID.toString(), 
					assessmentDateFormat.format(session.getCreationDate()),
					displayDateFormat.format(new Date(session.getDuration())),
					session.getScore(), session.getManualScore(), createPassedIcons(session.getPassed()),
					resultsPath + ".html", resultsPath + ".pdf");
			
			assessments.add(resultDetail);
			//WindowControlMocker needed because this is not a controller
			WindowControl mockwControl = new WindowControlMocker();
			
			FileResourceManager frm = FileResourceManager.getInstance();
			RepositoryEntry testEntry = session.getTestEntry();
			testEntries.add(testEntry);
			File fUnzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		
			String mapperUri = "../../../test" + testEntry.getKey() + "/";//add test repo key
			String submissionMapperUri = ".";
			String exportUri = "../" + translator.translate("table.user.attempt") + (sessions.indexOf(session)+1);			
			Controller assessmentResultController = new AssessmentResultController(ureq, mockwControl, assessedIdentity, false, session,
					fUnzippedDirRoot, mapperUri, submissionMapperUri,
					QTI21AssessmentResultsOptions.allOptions(), false, true, false, exportUri);

			Component component = assessmentResultController.getInitialComponent();
			String componentHTML = createResultHTML(component); 
			convertToZipEntry(zout, idPath + assessmentID +".html", componentHTML);	
			
			File resultXML = qtiService.getAssessmentResultFile(session);
			convertToZipEntry(zout, idPath + assessmentID +".xml", resultXML);

			if(withPdfs) {
				createResultPDF(zout, idPath + assessmentID +".pdf", assessedIdentity, session, fUnzippedDirRoot);
			}
			
			File signatureXML = qtiService.getAssessmentResultSignature(session);
			if (signatureXML != null) {
				convertToZipEntry(zout, idPath + "assessmentResultSignature.xml", signatureXML);	
			}			
			File submissionDir = qtiService.getSubmissionDirectory(session);
			String baseDir = idPath + "submissions";
			ZipUtil.addDirectoryToZip(submissionDir.toPath(), baseDir, zout);

			File assessmentDocsDir = qtiService.getAssessmentDocumentsDirectory(session);
			if(assessmentDocsDir.exists()) {
				String assessmentDocsBaseDir = idPath + "assessmentdocs";
				ZipUtil.addDirectoryToZip(assessmentDocsDir.toPath(), assessmentDocsBaseDir, zout);
			}
		}
		return assessments;
	}
	
	private void createResultPDF(ZipOutputStream zout, String path, Identity assessedIdentity, AssessmentTestSession candidateSession,
			File fUnzippedDirRoot) {
		try {
			ControllerCreator creator = (uureq, wwControl) -> {
				File submissionDir = qtiService.getSubmissionDirectory(candidateSession);
				URI assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);
				Mapper mapper = new ResourcesMapper(assessmentObjectUri, fUnzippedDirRoot, submissionDir);
	
				MapperKey mapperBaseKey = mapperService.register(uureq.getUserSession(), "QTI21DetailsResources::" + counter.incrementAndGet(), mapper, 3000);
				String mapperUriForPdf = mapperBaseKey.getUrl();
				uureq = new SyntheticUserRequest(ureq.getIdentity(), translator.getLocale(), ureq.getUserSession());
				return new AssessmentResultController(uureq, wwControl, assessedIdentity, false,
						candidateSession, fUnzippedDirRoot, mapperUriForPdf, null,
						QTI21AssessmentResultsOptions.allOptions(), false, true, false);
			};
			
			zout.putNextEntry(new ZipEntry(path));
			pdfService.convert(identity, creator, windowControl, PdfOutputOptions.defaultOptions(), zout);
			zout.closeEntry();
		} catch(Exception e) {
			log.error("", e);
		}
	}
	
	private void copyTestMaterials(RepositoryEntry testEntry, ZipOutputStream zout) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		String baseDir = exportFolderName + "/test" + testEntry.getKey();
		ZipUtil.addDirectoryToZip(fUnzippedDirRoot.toPath(), baseDir, zout);
	}
	
	protected static String createPassedIcons(Boolean passed) {
		if(passed == null) return "";
		
		return passed.booleanValue() ? "<i class='o_icon o_passed o_icon_passed text-success'></i>"
				: "<i class='o_icon o_failed o_icon_failed text-danger'></i>";
	}
	
	private String createResultHTML(Component results) {
		String pagePath = Util.getPackageVelocityRoot(this.getClass()) + "/qti21results.html";
		URLBuilder ubu = new URLBuilder("auth", "1", "0", "-");
		//generate VelocityContainer and put Component
		VelocityContainer mainVC = new VelocityContainer("html", pagePath, translator, null);
		mainVC.contextPut("rootTitle", translator.translate("table.grading"));
		mainVC.put("results", results);
		
		//render VelocityContainer to StringOutPut
		Renderer renderer = Renderer.getInstance(mainVC, translator, ubu, new RenderResult(), new DefaultGlobalSettings(), "-");
		try(StringOutput sb = new StringOutput(32000);
			VelocityRenderDecorator vrdec = new VelocityRenderDecorator(renderer, mainVC, sb)) {
			mainVC.contextPut("r", vrdec);
			renderer.render(sb, mainVC, null);
			return sb.toString();
		} catch(Exception e) {
			log.error("", e);
			return "";
		}
	}
	
	private String createResultListingHTML(List<ResultDetail> assessments, List<File> assessmentDocs, AssessedMember assessedMember) {
		// now put values to velocityContext
		VelocityContext ctx = new VelocityContext();
		ctx.put("t", translator);
		ctx.put("title", translator.translate("table.overview"));
		ctx.put("return", translator.translate("button.return"));
		ctx.put("assessments", assessments);
		ctx.put("assessedMember", assessedMember);
		ctx.put("assessmentDocs", assessmentDocs);
		ctx.put("hasResults", Boolean.valueOf(!assessments.isEmpty()));
		ctx.put("hasDocuments", Boolean.valueOf(!assessmentDocs.isEmpty()));
		ctx.put("hasPdfResults", Boolean.valueOf(withPdfs));

		String template = FileUtils.load(QTI21ResultsExport.class
				.getResourceAsStream("_content/qtiListing.html"), "utf-8");

		return velocityHelper.evaluateVTL(template, ctx);
	}
	
	private String createMemberListingHTML(List<AssessedMember> assessedMembers) {
		Collections.sort(assessedMembers, new AssessedMemberComparator());
		// now put values to velocityContext
		VelocityContext ctx = new VelocityContext();
		ctx.put("t", translator);
		ctx.put("rootTitle", translator.translate("table.overview"));
		ctx.put("assessedMembers", assessedMembers);
		ctx.put("hasObligation", Boolean.valueOf(NodeAccessType.of(courseEnv).getType().equals(LearningPathNodeAccessProvider.TYPE)));

		String template = FileUtils.load(QTI21ResultsExport.class
				.getResourceAsStream("_content/qtiUserlisting.html"), "utf-8");

		return velocityHelper.evaluateVTL(template, ctx);
	}
	
	private void convertToZipEntry(ZipOutputStream zout, String link, File file) throws IOException {
		zout.putNextEntry(new ZipEntry(link));
		try (InputStream in = new FileInputStream(file)) {
			FileUtils.copy(in, zout);
		} catch (Exception e) {
			log.error("Error during copy of resource export", e);
		} finally {
			zout.closeEntry();
		}
	}
	
	private void convertToZipEntry(ZipOutputStream zout, String link, String content) throws IOException {
		zout.putNextEntry(new ZipEntry(link));
		try (InputStream in = new ByteArrayInputStream(content.getBytes())) {
			FileUtils.copy(in, zout);
		} catch (Exception e) {
			log.error("Error during copy of resource export", e);
		} finally {
			zout.closeEntry();
		}
	}
	
	private void createZipDirectory (ZipOutputStream zout, String dir) throws IOException{
		dir = dir.endsWith(SEP) ? dir : dir + SEP;
		zout.putNextEntry(new ZipEntry(dir));
		zout.closeEntry();		
	}
	
	private static class IdentityComparator implements Comparator<Identity> {

		@Override
		public int compare(Identity o1, Identity o2) {
			int c;
			if(o1 == null && o2 == null) {
				c = 0;
			} else if(o1 != null && o2 == null) {
				c = -1;
			} else if(o1 == null && o2 != null) {
				c = 1;
			} else {
				c = o1.getKey().compareTo(o2.getKey());
			}
			return c;
		}
	}
}

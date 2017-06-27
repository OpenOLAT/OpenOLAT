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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.olat.core.gui.GlobalSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.render.velocity.VelocityHelper;
import org.olat.core.gui.render.velocity.VelocityRenderDecorator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.WindowControlMocker;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti.resultexport.AssessedMember;
import org.olat.ims.qti.resultexport.ResultDetail;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.ui.AssessmentResultController;
import org.olat.repository.RepositoryEntry;

public class QTI21ResultsExportMediaResource implements MediaResource {

	private static final OLog log = Tracing.createLoggerFor(QTI21ResultsExportMediaResource.class);
	
	private static final String DATA = "userdata/";
	private static final String SEP = File.separator;
	private static final SimpleDateFormat assessmentDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat displayDateFormat = new SimpleDateFormat("HH:mm:ss");
	static {
		displayDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	private VelocityHelper velocityHelper;
	
	private List<Identity> identities;
	private QTICourseNode courseNode;
	private QTI21Service qtiService;
	private String title, exportFolderName;
	private Translator translator;
	
	private RepositoryEntry entry;
	private UserRequest ureq;
	
	private final Set<RepositoryEntry> testEntries = new HashSet<>();

	public QTI21ResultsExportMediaResource(CourseEnvironment courseEnv, List<Identity> identities, 
			QTICourseNode courseNode, QTI21Service qtiService, UserRequest ureq) {
		this.title = "qti21export";	
		this.courseNode = courseNode;
		this.identities = identities;
		this.velocityHelper = VelocityHelper.getInstance();
		this.qtiService = qtiService;
		this.ureq = ureq;		
		this.entry = courseEnv.getCourseGroupManager().getCourseEntry();
		translator = Util.createPackageTranslator(QTI21ResultsExportMediaResource.class, ureq.getLocale());
	}
	
	public QTI21ResultsExportMediaResource(CourseEnvironment courseEnv, List<Identity> identities, QTICourseNode courseNode, 
			QTI21Service qtiService, UserRequest ureq, Locale locale) {
		this.title = "qti21export";	
		this.courseNode = courseNode;
		this.identities = identities;
		this.velocityHelper = VelocityHelper.getInstance();
		this.qtiService = qtiService;
		this.ureq = ureq;		
		this.entry = courseEnv.getCourseGroupManager().getCourseEntry();
		this.translator = Util.createPackageTranslator(QTI21ResultsExportMediaResource.class, locale);
		this.exportFolderName = translator.translate("export.folder.name");
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return "application/zip";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		//init package translator
		exportFolderName = translator.translate("export.folder.name");
		
		String label = StringHelper.transformDisplayNameToFileSystemName(title);
		if (label != null && !label.toLowerCase().endsWith(".zip")) {
			label += ".zip";
		}

		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + urlEncodedLabel);
		hres.setHeader("Content-Description", urlEncodedLabel);

		try { 			
			ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream());
			zout.setLevel(9);
			exportTestResults(zout);
			zout.close();
		} catch (Exception e) {
			log.error("Unknown error while assessment result resource export", e);
		}
	}
	
	/**
	 * Adds the result export to existing zip stream.
	 *
	 * @throws IOException
	 */
	public void exportTestResults(ZipOutputStream zout) throws IOException {
		
		List<AssessedMember> assessedMembers = createAssessedMembersDetail(zout);
		
		//convert velocity template to zip entry
		String membersHTML = createMemberListingHTML(assessedMembers);	
		convertToZipEntry(zout, exportFolderName + "/index.html", membersHTML);
		
		//Copy resource files or file trees to export file tree 
		File sasstheme = new File(WebappHelper.getContextRealPath("/static/offline/qti"));
		ZipUtil.addDirectoryToZip(sasstheme.toPath(), exportFolderName + "/css/offline/qti/", zout);
		File fontawesome = new File(WebappHelper.getContextRealPath("/static/font-awesome"));
		ZipUtil.addDirectoryToZip(fontawesome.toPath(), exportFolderName + "/css/font-awesome/", zout);
		File qtiJs = new File(WebappHelper.getContextRealPath("/static/js/jquery/"));
		ZipUtil.addDirectoryToZip(qtiJs.toPath(), exportFolderName + "/js/jquery/", zout);

		//materials
		for(RepositoryEntry testEntry:testEntries) {
			copyTestMaterials(testEntry, zout);
		}
	}
	
	private List<ResultDetail> createResultDetail (Identity identity, ZipOutputStream zout, String idDir) throws IOException {
		List<ResultDetail> assessments = new ArrayList<ResultDetail>();				
		List<AssessmentTestSession> sessions = qtiService.getAssessmentTestSessions(entry, courseNode.getIdent(), identity);
		for (AssessmentTestSession session : sessions) {
			Long assessmentID = session.getKey();
			String idPath = idDir + translator.translate("table.user.attempt") + (sessions.indexOf(session)+1) + SEP;
			createZipDirectory(zout, idPath);	
			// content of result table
			ResultDetail resultDetail = new ResultDetail(assessmentID.toString(), 
					assessmentDateFormat.format(session.getCreationDate()),
					displayDateFormat.format(new Date(session.getDuration())), session.getScore().floatValue(), 
					createPassedIcons(session.getPassed() == null ? true : session.getPassed()),
					idPath.replace(idDir, "") + assessmentID + ".html");
			
			assessments.add(resultDetail);
			//WindowControlMocker needed because this is not a controller
			WindowControl mockwControl = new WindowControlMocker();
			
			FileResourceManager frm = FileResourceManager.getInstance();
			RepositoryEntry testEntry = session.getTestEntry();
			testEntries.add(testEntry);
			File fUnzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		
			String mapperUri = "../../../test" + testEntry.getKey() + "/";//add test repo key
			String submissionMapperUri = ".";
			Controller assessmentResultController = new AssessmentResultController(
					ureq, mockwControl, identity, false, session,
					fUnzippedDirRoot, mapperUri, submissionMapperUri, QTI21AssessmentResultsOptions.allOptions(), false, true);

			Component component = assessmentResultController.getInitialComponent();
			String componentHTML = createResultHTML(component); 
			convertToZipEntry(zout, idPath + assessmentID +".html", componentHTML);	
			
			File resultXML = qtiService.getAssessmentResultFile(session);
			convertToZipEntry(zout, idPath + assessmentID +".xml", resultXML);	
			
			File submissionDir = qtiService.getSubmissionDirectory(session);
			String baseDir = idPath + "submissions/";
			ZipUtil.addDirectoryToZip(submissionDir.toPath(), baseDir, zout);
		}
		return assessments;
	}
	
	private List<AssessedMember> createAssessedMembersDetail (ZipOutputStream zout) throws IOException {
		List<AssessedMember> assessedMembers = new ArrayList<>();		
		for (Identity identity : identities) {
			
			String idDir = exportFolderName + "/" + DATA + identity.getName();
			idDir = idDir.endsWith(SEP) ? idDir : idDir + SEP;
			createZipDirectory(zout, idDir);				
			
			//content of single assessed member
			String userName = identity.getName();
			String firstName = identity.getUser().getProperty(UserConstants.FIRSTNAME, null);
			String lastName = identity.getUser().getProperty(UserConstants.LASTNAME, null);
			
			String memberEmail = identity.getUser().getProperty(UserConstants.EMAIL, null);
			AssessedMember assessedMember = new AssessedMember (userName, lastName, firstName, memberEmail, null);
						
			List<ResultDetail> assessments = createResultDetail(identity, zout, idDir);				
			
			String singleUserInfoHTML = createResultListingHTML(assessments, assessedMember);
			convertToZipEntry(zout, exportFolderName + "/" + DATA + identity.getName() + "/index.html", singleUserInfoHTML);
			
			String linkToUser = idDir.replace(exportFolderName + "/", "") + "index.html";				
			//content of assessed members table
			AssessedMember member = new AssessedMember();
			member.setUsername(createLink(identity.getName(), linkToUser, false));
			member.setLastname(createLink(identity.getUser().getProperty(UserConstants.LASTNAME, null),linkToUser,false));
			member.setFirstname(createLink(identity.getUser().getProperty(UserConstants.FIRSTNAME, null),linkToUser,false));
			member.setTries(String.valueOf(assessments.size()));
			assessedMembers.add(member);	
			
		}
		return assessedMembers;
	}
	
	private void copyTestMaterials(RepositoryEntry testEntry, ZipOutputStream zout) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		String baseDir = exportFolderName + "/test" + testEntry.getKey();
		ZipUtil.addDirectoryToZip(fUnzippedDirRoot.toPath(), baseDir, zout);
		
	}
	
	private String createLink(String name, String href, boolean userview) {
		String targetLink = userview ? "_blank" : "_self";
		return "<a href='" + href + "' target='" + targetLink + "' class='userLink'>" + name + "</a>";		
	}
	
	private String createPassedIcons(boolean passed) {
		String icon = passed ? "<i class='o_icon o_passed o_icon_passed text-success'></i>"
				: "<i class='o_icon o_failed o_icon_failed text-danger'></i>";
		return icon;
	}
	
	private String createResultHTML (Component results){
		StringOutput sb = new StringOutput(32000);
		String pagePath = Util.getPackageVelocityRoot(this.getClass()) + "/qti21results.html";
		URLBuilder ubu = new URLBuilder("auth", "1", "0");
		//generate VelocityContainer and put Component
		VelocityContainer mainVC = new VelocityContainer("html", pagePath, translator, null);
		mainVC.contextPut("rootTitle", translator.translate("table.grading"));
		mainVC.put("results", results);
		
		//render VelocityContainer to StringOutPut
		Renderer renderer = Renderer.getInstance(mainVC, translator, ubu, new RenderResult(), new EmptyGlobalSettings());
		VelocityRenderDecorator vrdec = new VelocityRenderDecorator(renderer, mainVC, sb);
		mainVC.contextPut("r", vrdec);
		renderer.render(sb, mainVC, null);
		return sb.toString();
	}
	
	private String createResultListingHTML (List<ResultDetail> assessments,AssessedMember assessedMember){
		// now put values to velocityContext
		VelocityContext ctx = new VelocityContext();
		ctx.put("t", translator);
		ctx.put("title", translator.translate("table.overview"));
		ctx.put("return", translator.translate("button.return"));
		ctx.put("assessments", assessments);
		ctx.put("assessedMember", assessedMember);
		if (assessments.size() > 0) ctx.put("hasResults", Boolean.TRUE);
		
		String template = FileUtils.load(QTI21ResultsExportMediaResource.class
				.getResourceAsStream("_content/qtiListing.html"), "utf-8");

		return velocityHelper.evaluateVTL(template, ctx);
	}
	
	private String createMemberListingHTML(List<AssessedMember> assessedMembers) {
		Collections.sort(assessedMembers, new Comparator<AssessedMember>() {
			@Override
			public int compare(AssessedMember o1, AssessedMember o2) {
				return o1.getUsername().compareTo(o2.getUsername());
			}
		});
		// now put values to velocityContext
		VelocityContext ctx = new VelocityContext();
		ctx.put("t", translator);
		ctx.put("rootTitle", translator.translate("table.overview"));
		ctx.put("assessedMembers", assessedMembers);

		String template = FileUtils.load(QTI21ResultsExportMediaResource.class
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

	@Override
	public void release() {
		//
	}

	private static class EmptyAJAXFlags extends AJAXFlags {
		
		public EmptyAJAXFlags() {
			super(null);
		}
		
		@Override
		public boolean isIframePostEnabled() {
			return false;
		}
	}
	
	private static class EmptyGlobalSettings implements GlobalSettings {
		@Override
		public int getFontSize() {
			return 100;
		}
		
		@Override
		public AJAXFlags getAjaxFlags() {
			return new EmptyAJAXFlags();
		}
		
		@Override
		public boolean isIdDivsForced() {
			return false;
		}
	};
}

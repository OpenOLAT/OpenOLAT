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
package org.olat.ims.qti.resultexport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.dom4j.Document;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.velocity.VelocityHelper;
import org.olat.core.gui.translator.Translator;
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
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.QTIResultSet;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.FilePersister;
import org.olat.ims.qti.render.LocalizedXSLTransformer;
import org.olat.user.UserManager;

public class QTI12ResultsExportMediaResource implements MediaResource {

	private static final OLog log = Tracing.createLoggerFor(QTI12ResultsExportMediaResource.class);
	
	private static final String DATA = "userdata/";
	private static final String SEP = File.separator;
	private static final SimpleDateFormat assessmentDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat displayDateFormat = new SimpleDateFormat("HH:mm:ss");
	static {
		displayDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	private VelocityHelper velocityHelper;
	
	private List<Identity> identities;
	private final QTIResultManager qtiResultManager;
	private QTICourseNode courseNode;
	private CourseEnvironment courseEnv;
	private Locale locale;
	private String title, exportFolderName;
	private Translator translator;

	public QTI12ResultsExportMediaResource(CourseEnvironment courseEnv, List<Identity> identities,
			QTICourseNode courseNode, String archivePath, Locale locale) {
		this.courseNode = courseNode;
		this.courseEnv = courseEnv;
		this.locale = locale;
		title = "qti12export";	
		this.identities = identities;
		velocityHelper = VelocityHelper.getInstance();
		
		translator = Util.createPackageTranslator(QTI12ResultsExportMediaResource.class, locale);
		exportFolderName = ZipUtil.concat(archivePath, translator.translate("export.folder.name"));
		
		qtiResultManager = QTIResultManager.getInstance();
	}

	@Override
	public long getCacheControlDuration() {
		return 0;
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
	
		String label = StringHelper.transformDisplayNameToFileSystemName(title);
		if (label != null && !label.toLowerCase().endsWith(".zip")) {
			label += ".zip";
		}

		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + urlEncodedLabel);
		hres.setHeader("Content-Description", urlEncodedLabel);
	
		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {	
			zout.setLevel(9);
								
			List<AssessedMember> assessedMembers = createAssessedMembersDetail(zout);
			
			//convert velocity template to zip entry
			String usersHTML = createMemberListingHTML(assessedMembers);	
			convertToZipEntry(zout, exportFolderName + "/index.html", usersHTML);
			
			//Copy resource files or file trees to export file tree
			File theme = new File(WebappHelper.getContextRealPath("/static/themes/light/theme.css"));
			ZipUtil.addFileToZip(exportFolderName + "/css/offline/qti/theme.css", theme, zout);
			File themeMap = new File(WebappHelper.getContextRealPath("/static/themes/light/theme.css.map"));
			ZipUtil.addFileToZip(exportFolderName + "/css/offline/qti/theme.css.map", themeMap, zout);
			
			File fontawesome = new File(WebappHelper.getContextRealPath("/static/font-awesome"));
			fsToZip(zout, fontawesome.toPath(), exportFolderName + "/css/font-awesome/");
			File qtiJs = new File(WebappHelper.getContextRealPath("/static/js/jquery/"));
			ZipUtil.addDirectoryToZip(qtiJs.toPath(), exportFolderName + "/js/jquery", zout);
		} catch (Exception e) {
			log.error("Unknown error while assessment result resource export", e);
		}
	}
	
	private List<ResultDetail> createResultDetail (Identity identity, ZipOutputStream zout, String idDir) throws IOException {
		Long resourceId = courseEnv.getCourseResourceableId();
		String resourceDetail = courseNode.getIdent();
		Long resid = courseNode.getReferencedRepositoryEntry().getKey();
		List<QTIResultSet> resultSets = qtiResultManager.getResultSets(resourceId, resourceDetail, resid, identity);		
							
		List<ResultDetail> assessments = new ArrayList<ResultDetail>();
		
		for (QTIResultSet qtiResultSet : resultSets) {
			
			Long assessmentID = qtiResultSet.getAssessmentID();
			String idPath = idDir + translator.translate("table.user.attempt") + (resultSets.indexOf(qtiResultSet)+1) + SEP;
			createZipDirectory(zout, idPath);
			
			String linkToHTML = createHTMLfromQTIResultSet(idPath, idDir, zout, identity, qtiResultSet);
			
			// content of result table
			ResultDetail resultDetail = new ResultDetail(createLink(String.valueOf(assessmentID), linkToHTML, true),
					assessmentDateFormat.format(qtiResultSet.getCreationDate()), 
					displayDateFormat.format(new Date(qtiResultSet.getDuration())),
					qtiResultSet.getScore(), createPassedIcons(qtiResultSet.getIsPassed()), linkToHTML);
			
			assessments.add(resultDetail);			
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
			String memberEmail = UserManager.getInstance().getUserDisplayEmail(identity, locale);
			AssessedMember assessedMember = new AssessedMember (userName, lastName, firstName, memberEmail, null);
			
			List<ResultDetail> assessments = createResultDetail(identity, zout, idDir);
			
			String oneUserHTML = createResultListingHTML(assessments, assessedMember);
			convertToZipEntry(zout, exportFolderName + "/" + DATA + identity.getName() + "/index.html", oneUserHTML);
			
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
	
	/**
	 * Adds the result export to existing zip stream.
	 *
	 * @throws Exception
	 */
	public void exportTestResults(ZipOutputStream zout) throws IOException {		
						
		List<AssessedMember> assessedMembers = createAssessedMembersDetail(zout);
		
		//convert velocity template to zip entry
		String usersHTML = createMemberListingHTML(assessedMembers);	
		convertToZipEntry(zout, exportFolderName + "/index.html", usersHTML);
		
		//Copy resource files or file trees to export file tree 
		File theme = new File(WebappHelper.getContextRealPath("/static/themes/light/theme.css"));
		ZipUtil.addFileToZip(exportFolderName + "/css/offline/qti/theme.css", theme, zout);
		File themeMap = new File(WebappHelper.getContextRealPath("/static/themes/light/theme.css.map"));
		ZipUtil.addFileToZip(exportFolderName + "/css/offline/qti/theme.css.map", themeMap, zout);
		
		File fontawesome = new File(WebappHelper.getContextRealPath("/static/font-awesome"));
		fsToZip(zout, fontawesome.toPath(), exportFolderName + "/css/font-awesome/");
	}
	
	private String createLink(String name, String href, boolean userview){
		String targetLink = userview ? "_blank" : "_self";
		return "<a href='" + href + "' target='" + targetLink + "' class='userLink'>" + name + "</a>";		
	}
	
	
	private String createPassedIcons(boolean passed) {
		return passed ? "<i class='o_icon o_passed o_icon_passed text-success'></i>"
				: "<i class='o_icon o_failed o_icon_failed text-danger'></i>";
	}
	
	private String createResultHTML (String results){
		VelocityContext ctx = new VelocityContext();
		ctx.put("results", results);
		ctx.put("rootTitle", translator.translate("table.grading"));

		String template = FileUtils.load(QTI12ResultsExportMediaResource.class
				.getResourceAsStream("_content/qti12results.html"), "utf-8");

		return velocityHelper.evaluateVTL(template, ctx);
	}
	
	private String createResultListingHTML (List<ResultDetail> assessments, AssessedMember assessedMember){
		// now put values to velocityContext
		VelocityContext ctx = new VelocityContext();
		ctx.put("t", translator);
		ctx.put("title", translator.translate("table.overview"));
		ctx.put("return", translator.translate("button.return"));
		ctx.put("assessments", assessments);
		ctx.put("assessedMember", assessedMember);
		if (assessments.size() > 0) ctx.put("hasResults", Boolean.TRUE);
		
		String template = FileUtils.load(QTI12ResultsExportMediaResource.class
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

		String template = FileUtils.load(QTI12ResultsExportMediaResource.class
				.getResourceAsStream("_content/qtiUserlisting.html"), "utf-8");

		return velocityHelper.evaluateVTL(template, ctx);
	}
	

	private File retrieveXML (Identity subj, long aiid){
		String RES_REPORTING = "resreporting";
		String type = AssessmentInstance.QMD_ENTRY_TYPE_ASSESS;
		File fUserdataRoot = new File(WebappHelper.getUserDataRoot());
		String path = RES_REPORTING + SEP + subj.getName() + SEP + type + SEP + aiid + ".xml";
		File fDoc = new File(fUserdataRoot, path);
		return fDoc;
	}
	
	private String createHTMLfromQTIResultSet(String idPath, String idDir, ZipOutputStream zout,
			Identity assessedIdentity, QTIResultSet resultSet) throws IOException {

		try {
			Document doc = FilePersister.retreiveResultsReporting(assessedIdentity,
					AssessmentInstance.QMD_ENTRY_TYPE_ASSESS, resultSet.getAssessmentID());
			if (doc == null) {
				return "null";
			}
			
			File resourceXML = retrieveXML(assessedIdentity, resultSet.getAssessmentID());			
			String resultsHTML = LocalizedXSLTransformer.getInstance(locale).renderResults(doc);		
			resultsHTML = createResultHTML(resultsHTML);
			
			String html = idPath + resultSet.getAssessmentID() + ".html";
			String xml = html.replace(".html", ".xml");
			convertToZipEntry(zout, html, resultsHTML);		
			convertToZipEntry(zout, xml, resourceXML);
			
			return idPath.replace(idDir, "") + resultSet.getAssessmentID() + ".html";
		} catch (Exception e) {
			log.error("", e);
			return "null";
		}
	}
	
	private void fsToZip(ZipOutputStream zout, final Path sourceFolder, final String targetPath) throws IOException {
		Files.walkFileTree(sourceFolder, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				zout.putNextEntry(new ZipEntry(targetPath + sourceFolder.relativize(file).toString()));
				Files.copy(file, zout);
				zout.closeEntry();
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				zout.putNextEntry(new ZipEntry(targetPath + sourceFolder.relativize(dir).toString() + "/"));
				zout.closeEntry();
				return FileVisitResult.CONTINUE;
			}
		});
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

	}

}

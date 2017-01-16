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
package org.olat.course.nodes.pf.manager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.pf.ui.PFRunController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.user.UserManager;
/**
*
* Initial date: 15.12.2016<br>
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class FileSystemExport implements MediaResource {
	
	private static final OLog log = Tracing.createLoggerFor(FileSystemExport.class);
	
	private List<Identity> identities;
	private PFCourseNode pfNode;
	private CourseEnvironment courseEnv;
	private Translator translator;

	public FileSystemExport(List<Identity> identities, PFCourseNode pfNode, CourseEnvironment courseEnv, Locale locale) {
		super();
		this.identities = identities;
		this.pfNode = pfNode;
		this.courseEnv = courseEnv;
		this.translator = Util.createPackageTranslator(PFRunController.class, locale);

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
		try (ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			
			String pfolder = translator.translate("participant.folder") + "/";
			
			Path relPath = Paths.get(courseEnv.getCourseBaseContainer().getBasefile().getAbsolutePath(),
					PFManager.FILENAME_PARTICIPANTFOLDER, pfNode.getIdent()); 
						
			fsToZip(zout, relPath, pfolder);
			
			zout.close();
			
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}
	
	private void fsToZip(ZipOutputStream zout, final Path sourceFolder, final String targetPath) throws IOException {
		UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
		Set<Long> idKeys = new HashSet<>();
		for (Identity identity : identities) {
			idKeys.add(identity.getKey());
		}
		Files.walkFileTree(sourceFolder, new SimpleFileVisitor<Path>() {
			private String containsID (String relPath) {
				for (Long key : idKeys) {
					if (relPath.contains(key.toString())) {
						String exportFolderName = userManager.getUserDisplayName(key).replace(", ", "_") + "_" + key;
						return relPath.replace(key.toString(), exportFolderName);
					}
				}
				return null;
			}
			
			private boolean boxesEnabled(String relPath) {
				return pfNode.hasParticipantBoxConfigured() && relPath.contains(PFManager.FILENAME_DROPBOX) 
						|| pfNode.hasCoachBoxConfigured() && relPath.contains(PFManager.FILENAME_RETURNBOX);
			}			
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String relPath = sourceFolder.relativize(file).toString();
				if ((relPath = containsID(relPath)) != null && boxesEnabled(relPath)) {
					zout.putNextEntry(new ZipEntry(targetPath + relPath));
					Files.copy(file, zout);
					zout.closeEntry();
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				String relPath = sourceFolder.relativize(dir).toString() + "/";
				if ((relPath = containsID(relPath)) != null && boxesEnabled(relPath)) {
					zout.putNextEntry(new ZipEntry(targetPath + relPath));
					zout.closeEntry();
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
}
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.pf.ui.PFParticipantController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.repository.RepositoryEntry;
/**
*
* Initial date: 15.12.2016<br>
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class FileSystemExport implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(FileSystemExport.class);
	
	private List<Identity> identities;
	private PFCourseNode pfNode;
	private CourseEnvironment courseEnv;
	private Translator translator;

	public FileSystemExport(List<Identity> identities, PFCourseNode pfNode, CourseEnvironment courseEnv, Locale locale) {
		super();
		this.identities = identities;
		this.pfNode = pfNode;
		this.courseEnv = courseEnv;
		this.translator = Util.createPackageTranslator(PFParticipantController.class, locale);

	}
	
	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
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
		RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
		String label = StringHelper.transformDisplayNameToFileSystemName(pfNode.getShortName() + "_" + entry.getDisplayname())
				+ "_" + Formatter.formatDatetimeWithMinutes(new Date())
				+ ".zip";
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + urlEncodedLabel);
		hres.setHeader("Content-Description", urlEncodedLabel);
		
		try (ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			Path relPath = Paths.get(courseEnv.getCourseBaseContainer().getBasefile().getAbsolutePath(),
					PFManager.FILENAME_PARTICIPANTFOLDER, pfNode.getIdent()); 
			fsToZip(zout, "", relPath, pfNode, identities, translator);			
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}
	
	/**
	 * Exports a given filesystem as Zip-Outputstream
	 *
	 * @param zout the Zip-Outputstream
	 * @param sourceFolder the source folder
	 * @param pfNode the PFCourseNode
	 * @param identities
	 * @param translator
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static boolean fsToZip(final ZipOutputStream zout, String zipPath, final Path sourceFolder, PFCourseNode pfNode,
			List<Identity> identities, final Translator translator) {
		
		if(StringHelper.containsNonWhitespace(zipPath)) {
			if(!zipPath.endsWith("/")) {
				zipPath += "/";
			}
		} else {
			zipPath = "";
		}
		
		final String targetPath = zipPath;
		
		Set<String> idKeys = new HashSet<>();
		Map<String, Identity> idMap  = new HashMap<>();
		if (identities != null) {
			for (Identity identity : identities) {
				String identityKey = identity.getKey().toString();
				idKeys.add(identityKey);
				idMap.put(identityKey, identity);
			}
		} else {
			File[] listOfDirectories = sourceFolder.toFile().listFiles(SystemFileFilter.DIRECTORY_ONLY);
			if(listOfDirectories != null) {
				List<Long> idKeysList = new ArrayList<>();
				for (File file : listOfDirectories) {
					String filename = file.getName();
					if(StringHelper.isLong(filename)) {
						idKeys.add(filename);
						idKeysList.add(Long.valueOf(filename));
					}
				}
				final BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
				List<Identity> loadedIdentities = securityManager.loadIdentityByKeys(idKeysList);
				for (Identity identity : loadedIdentities) {
					String identityKey = identity.getKey().toString();
					idMap.put(identityKey, identity);
				}
			}
		}
		
		try {
			Files.walkFileTree(sourceFolder, new SimpleFileVisitor<Path>() {
				//contains identity check  and changes identity key to user display name
				private String containsID (String relPath) {
					for (String key : idKeys) {
						//additional check if folder is a identity-key (coming from fs)
						if (relPath.contains(key) && StringHelper.isLong(key)) {
							String exportFolderName;
							if(idMap.containsKey(key)) {
								Identity id = idMap.get(key);
								exportFolderName = (id.getUser().getLastName() + "_" + id.getUser().getFirstName());
							} else {
								exportFolderName = "";
							}
							exportFolderName = exportFolderName.replace(", ", "_") + "_" + key;
							return relPath.replace(key, exportFolderName);
						}
					}
					return null;
				}
				//checks module config and translates folder name
				private String boxesEnabled(String relPath) {
					if (pfNode.hasParticipantBoxConfigured() && relPath.contains(PFManager.FILENAME_DROPBOX)) {
						return relPath.replace(PFManager.FILENAME_DROPBOX, translator.translate("drop.box"));
					} else if (pfNode.hasCoachBoxConfigured() && relPath.contains(PFManager.FILENAME_RETURNBOX)) {
						return relPath.replace(PFManager.FILENAME_RETURNBOX, translator.translate("return.box"));
					} else {
						return null;
					}
				}			
				
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					String relPath = sourceFolder.relativize(file).toString();
					if ((relPath = containsID(relPath)) != null
							&& (relPath = boxesEnabled(relPath)) != null
							&& !file.toFile().isHidden()) {
						zout.putNextEntry(new ZipEntry(targetPath + relPath));
						copyFile(file, zout);
						zout.closeEntry();
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					String relPath = sourceFolder.relativize(dir).toString() + "/";
					if ((relPath = containsID(relPath)) != null && (relPath = boxesEnabled(relPath)) != null) {
						zout.putNextEntry(new ZipEntry(targetPath + relPath));
						zout.closeEntry();
					}
					return FileVisitResult.CONTINUE;
				}
			});
			return true;
		} catch (IOException e) {
			log.error("Unable to export zip",e);
			return false;
		}
	}
	
	private static void copyFile(Path file, ZipOutputStream zout) {
		try(OutputStream out= new ShieldOutputStream(zout)) {
			Files.copy(file, zout);
		} catch(Exception e) {
			log.error("Cannot zip {}", file, e);
		}
	}
	
}
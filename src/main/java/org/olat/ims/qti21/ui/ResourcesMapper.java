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
package org.olat.ims.qti21.ui;

import java.io.File;
import java.net.URI;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.ForbiddenMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 11.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResourcesMapper implements Mapper {
	
	private static final Logger log = Tracing.createLoggerFor(ResourcesMapper.class);
	private static final String SUBMISSION_SUBPATH = "submissions/";
	private static final String ASSESSMENTDOCS_SUBPATH = "assessmentdocs/";
	
	private final File rootDirectory;
	private final URI assessmentObjectUri;
	private final File submissionDirectory;
	private final Map<Long,File> submissionDirectoryMaps;
	
	public ResourcesMapper(URI assessmentObjectUri, File rootDirectory) {
		this.assessmentObjectUri = assessmentObjectUri;
		this.rootDirectory = rootDirectory;
		submissionDirectory = null;
		submissionDirectoryMaps = null;
	}
	
	public ResourcesMapper(URI assessmentObjectUri, File rootDirectory, File submissionDirectory) {
		this.assessmentObjectUri = assessmentObjectUri;
		this.submissionDirectory = submissionDirectory;
		this.rootDirectory = rootDirectory;
		submissionDirectoryMaps = null;
	}
	
	public ResourcesMapper(URI assessmentObjectUri, File rootDirectory, Map<Long,File> submissionDirectoryMaps) {
		this.assessmentObjectUri = assessmentObjectUri;
		this.rootDirectory = rootDirectory;
		this.submissionDirectoryMaps = submissionDirectoryMaps;
		submissionDirectory = null;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		String filename = null;
		MediaResource resource = null;
		try {
			File root = new File(assessmentObjectUri.getPath());
			String href = request.getParameter("href");
			String data = request.getParameter("data");
			
			boolean asData = false;
			if(StringHelper.containsNonWhitespace(href)) {
				filename = href;	
			} else if(StringHelper.containsNonWhitespace(data)) {
				filename = data;
				asData = true;
			} else if(StringHelper.containsNonWhitespace(relPath)) {
				filename = relPath;
				if(filename.startsWith("/")) {
					filename = filename.substring(1, filename.length());
				}
			}
			
			File file = new File(root.getParentFile(), filename);
			if(file.exists()) {
				if(file.getName().endsWith(".xml")) {
					resource = new ForbiddenMediaResource();
				} else if(FileUtils.isInSubDirectory(rootDirectory, file)) {
					if(asData) {
						resource = new QTI21FileMediaResource(file);
					} else {
						resource = new FileMediaResource(file, true);
					}
				} else {
					resource = new ForbiddenMediaResource();
				}
			} else if(filename != null && filename.endsWith("/raw/_noversion_/images/transparent.gif")) {
				String realPath = request.getServletContext().getRealPath("/static/images/transparent.gif");
				resource = new FileMediaResource(new File(realPath), true);
			} else {
				if(filename != null && filename.contains(SUBMISSION_SUBPATH)) {
					resource = submission(filename);
				} else if(filename != null && filename.contains(ASSESSMENTDOCS_SUBPATH)) {
					resource = assessmentDocuments(filename);
				}
			}
		} catch (Exception e) {
			log.error("", e);
			resource = new NotFoundMediaResource();
		}
		return resource;
	}
	
	private MediaResource submission(String filename) {
		int submissionIndex = filename.indexOf(SUBMISSION_SUBPATH) + SUBMISSION_SUBPATH.length();
		String submission = filename.substring(submissionIndex);
		int candidateSessionIndex = submission.indexOf('/');
		
		File storage = null;
		String submissionName = null;
		if(candidateSessionIndex > 0) {
			submissionName = submission.substring(candidateSessionIndex + 1);
			if(submissionDirectory != null) {
				storage = submissionDirectory;
			} else if(submissionDirectoryMaps != null) {
				String sessionKey = submission.substring(0, candidateSessionIndex);
				storage = getStorageInMap(sessionKey);
			}
		}
		return toResource(storage, submissionName);
	}
	
	private MediaResource assessmentDocuments(String filename) {
		int assessmentIndex = filename.indexOf(ASSESSMENTDOCS_SUBPATH) + ASSESSMENTDOCS_SUBPATH.length();
		String assessment = filename.substring(assessmentIndex);
		int testSessionIndex = assessment.indexOf('/');
		if(testSessionIndex < 0) {
			return new NotFoundMediaResource();
		}
		int itemSessionIndex = assessment.indexOf('/', testSessionIndex + 1);
		if(itemSessionIndex < 0 || itemSessionIndex <= testSessionIndex) {
			return new NotFoundMediaResource();
		}
		
		File storage = null;
		String submissionName = null;
		if(testSessionIndex > 0) {
			File submissionDir = null;
			if(submissionDirectory != null) {
				submissionDir = submissionDirectory;
			} else if(submissionDirectoryMaps != null) {
				String testSession = assessment.substring(0, testSessionIndex);
				submissionDir = getStorageInMap(testSession);
			}
			
			if(submissionDir != null) {
				submissionName = assessment.substring(itemSessionIndex + 1);
				String itemSession = assessment.substring(testSessionIndex + 1, itemSessionIndex);
				storage = new File(submissionDir.getParentFile(), "assessmentdocs");
				storage = new File(storage, itemSession);
			}
		}

		return toResource(storage, submissionName);
	}
	
	private MediaResource toResource(File storage, String filename) {
		if(storage != null && StringHelper.containsNonWhitespace(filename)) {
			File submissionFile = new File(storage, filename);
			if(submissionFile.exists()) {
				return new FileMediaResource(submissionFile, true);
			}
		}
		return new NotFoundMediaResource();
	}
	
	private File getStorageInMap(String sessionKey) {
		File storage = null;
		if(StringHelper.isLong(sessionKey)) {
			try {
				storage = submissionDirectoryMaps.get(Long.valueOf(sessionKey));
			} catch (Exception e) {
				log.error("", e);
				
			}
		}
		return storage;
	}
}
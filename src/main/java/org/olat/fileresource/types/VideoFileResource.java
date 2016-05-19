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
package org.olat.fileresource.types;

import java.io.File;
import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoModule;

/**
 * Initial Date:  Mar 27, 2015
 *
 * @author Dirk Furrer
 */
public class VideoFileResource extends FileResource {


	private static VideoModule videomodule = CoreSpringFactory.getImpl(VideoModule.class);
	private static VideoManager videoManager = CoreSpringFactory.getImpl(VideoManager.class);
	private static MovieService movieService = CoreSpringFactory.getImpl(MovieService.class);

	/**
	 * Movie file resource identifier.
	 */
	public static final String TYPE_NAME = "FileResource.VIDEO";

	public VideoFileResource() {
		super(TYPE_NAME);
	}

	/**
	 * @param file
	 *            file to validate
	 * @param fileName
	 *            name of the file. Necessary since the file.filename contains
	 *            gliberish during upload
	 * @param eval the resource validation. Is set to valid if file is accepted
	 */
	public static void validate(File file, String fileName, ResourceEvaluation eval) {
		if (!videomodule.isEnabled()) {
			return;
		}
		// accept raw mp4 files
		// accept also mov files as iOS saves mp4 movis as mov, but check if the file can be parsed as mp4 file
		boolean isMP4 = movieService.isMP4(new LocalFileImpl(file), fileName);
		if (isMP4) {			
			eval.setValid(true);
			eval.setDisplayname(fileName + " - " + Formatter.formatShortDateFilesystem(new Date()));
		} else if (fileName.endsWith(".zip")) {
			// check if zip contains an exported video resource
			videoManager.validateVideoExportArchive(file, eval);
		}
	}
}

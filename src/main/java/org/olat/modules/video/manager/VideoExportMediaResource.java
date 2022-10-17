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
package org.olat.modules.video.manager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;

/**
 * Implementation to create a video resource export creating a dynamic ZIP. The
 * ZIP contains the original master video, the selected poster image and the
 * configuration. It does not contain the optimized transcoded video
 * alternatives.
 * 
 * Initial date: 21.04.2016<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public class VideoExportMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(VideoExportMediaResource.class);
	private final VFSContainer baseContainer;
	private final String title;
	
	/**
	 * Package scope constructor. Use VideoManager.getVideoExportMediaResource()
	 * 
	 * @param baseContainer
	 *            The container where the video resource is located on disk
	 */
	VideoExportMediaResource(VFSContainer baseContainer, String title) {
		this.baseContainer = baseContainer;
		this.title = title;
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
		String label = StringHelper.transformDisplayNameToFileSystemName(title);
		if(label != null && !label.toLowerCase().endsWith(".zip")) {
			label += ".zip";
		}
		
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);			
		hres.setHeader("Content-Description", urlEncodedLabel);
		
		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			
			final Path unzipPath = ((LocalFolderImpl) baseContainer).getBasefile().toPath();			
			Files.walkFileTree(unzipPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Path relativeFile = unzipPath.relativize(file);
					String names = relativeFile.toString();
					zout.putNextEntry(new ZipEntry(names));							
					zip(file, zout);
					zout.closeEntry();
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (Exception e) {
			log.error("Unknown error while video resource export", e);
		}
	}
	
	private final void zip(Path file, ZipOutputStream zout) {
		try(InputStream in=Files.newInputStream(file)) {
			FileUtils.copy(in, zout);
		} catch (Exception e) {
			log.error("Error during copy of video resource export", e);
		}
	}

	@Override
	public void release() {
		//
	}
}

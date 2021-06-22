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
package org.olat.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.ProviderNotFoundException;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.ServiceConfigurationError;

import org.apache.commons.io.IOUtils;
import org.olat.core.logging.OLATRuntimeException;

/**
 * 
 * Initial date: 08.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PathUtils {
	
	/**
	 * 
	 * @param source
	 * @param targetDir
	 * @param path Relative path where the file is saved from targetDir
	 * @return
	 * @throws IOException
	 */
	public static boolean copyFileToDir(Path source, File targetDir, String path) throws IOException {
		File targetFile = new File(targetDir, path);
		if(!targetFile.getParentFile().exists()) {
			targetFile.getParentFile().mkdirs();
		}
		Files.copy(source, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return true;
	}
	
	/**
	 * Use the closeSubsequentFS method to close the file system. The method doesn't
	 * follow sym. links and its depth is limited.
	 * 
	 * @param file The file to visit
	 * @param filename The filename
	 * @param visitor The visitor
	 * @return
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public static Path visit(File file, String filename, FileVisitor<Path> visitor) 
	throws IOException, IllegalArgumentException {
		if(!StringHelper.containsNonWhitespace(filename)) {
			filename = file.getName();
		}
		
		Path fPath = null;
		if(file.isDirectory()) {
			fPath = file.toPath();
		} else if(filename != null && filename.toLowerCase().endsWith(".zip")) {
			try {
				fPath = FileSystems.newFileSystem(file.toPath(), null).getPath("/");
			} catch (ProviderNotFoundException | ServiceConfigurationError e) {
				throw new IOException("Unreadable file with .zip extension: " + file, e);
			}
		} else {
			fPath = file.toPath();
		}
		if(fPath != null) {
		    Files.walkFileTree(fPath, EnumSet.noneOf(FileVisitOption.class), 32, visitor);
		}
		return fPath;
	}
	
	public static void closeSubsequentFS(Path path) {
		if(path != null && FileSystems.getDefault() != path.getFileSystem()) {
			IOUtils.closeQuietly(path.getFileSystem(), null);
		}
	}
	
	public static class YesMatcher implements PathMatcher {
		@Override
		public boolean matches(Path path) {
			return true;
		}
	}
	
	public static class CopyVisitor extends SimpleFileVisitor<Path> {

		private final Path source;
		private final Path destDir;
		private final PathMatcher filter;
		
		public CopyVisitor(Path source, Path destDir, PathMatcher filter) {
			this.source = source;
			this.destDir = destDir;
			this.filter = filter;
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
	    throws IOException {
			Path relativeFile = source.relativize(file);
	        final Path destFile = Paths.get(destDir.toString(), relativeFile.toString());
	        Path normalizedPath = destFile.normalize();
			if(!normalizedPath.startsWith(destDir)) {
				throw new OLATRuntimeException("Invalid ZIP");
			}
	        if(filter.matches(file)) {
	        	Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
	        }
	        return FileVisitResult.CONTINUE;
		}
	 
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
		throws IOException {
			Path relativeDir = source.relativize(dir);
	        final Path dirToCreate = Paths.get(destDir.toString(), relativeDir.toString());
	        if(Files.notExists(dirToCreate)){
	        	Files.createDirectory(dirToCreate);
	        }
	        return FileVisitResult.CONTINUE;
		}
	}

}

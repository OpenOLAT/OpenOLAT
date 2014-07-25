/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.fileresource.types;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;

/**
 * Initial Date:  Apr 8, 2004
 *
 * @author Mike Stock
 */
public class FileResource implements OLATResourceable {
	
	private static final OLog log = Tracing.createLoggerFor(FileResource.class);

	/**
	 * Generic file resource type identifier.
	 */
	public static final String GENERIC_TYPE_NAME = "FileResource.FILE";
	private final String typeName;
	private Long typeId;
	
	public FileResource() {
		typeName = GENERIC_TYPE_NAME;
		typeId = new Long(CodeHelper.getForeverUniqueID());
	}
	
	public FileResource(String typeName) {
		this.typeName = typeName;
		typeId = new Long(CodeHelper.getForeverUniqueID());
	}

	/**
	 * Only used internally when switching subtypes.
	 * @param newId
	 */
	public void overrideResourceableId(Long newId) {
		typeId = newId;
	}
	
	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableTypeName()
	 */
	public String getResourceableTypeName() {
		return typeName;
	}

	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableId()
	 */
	public Long getResourceableId() {
		return typeId;
	}
	

	
	/**
	 * This method open a new FileSystem for zip
	 * @param file
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static Path getResource(File file, String filename)
	throws IOException {
		if(!StringHelper.containsNonWhitespace(filename)) {
			filename = file.getName();
		}
		
		Path fPath = null;
		if(file.isDirectory()) {
			fPath = file.toPath();
		} else if(filename != null && filename.toLowerCase().endsWith(".zip")) {
			//perhaps find root folder and return it
			fPath = FileSystems.newFileSystem(file.toPath(), null).getPath("/");
			RootSearcher rootSearcher = searchRootDirectory(fPath);
			if(rootSearcher.foundRoot()) {
				Path rootPath = rootSearcher.getRoot();
				fPath = fPath.resolve(rootPath);
			}
		} else {
			fPath = file.toPath();
		}
		return fPath;
	}
	
	protected static  RootSearcher searchRootDirectory(Path fPath)
	throws IOException {
		RootSearcher rootSearcher = new RootSearcher();
		Files.walkFileTree(fPath, rootSearcher);
		return rootSearcher;
	}
	
	public static boolean copyResource(File file, String filename, File targetDirectory) {
		return copyResource(file, filename, targetDirectory, new YesMatcher());
	}
	
	public static boolean copyResource(File file, String filename, File targetDirectory, PathMatcher filter) {
		try {
			Path path = getResource(file, filename);
			if(path == null) {
				return false;
			}
			
			Path destDir = targetDirectory.toPath();
			Files.walkFileTree(path, new CopyVisitor(path, destDir, filter));
			return true;
		} catch (IOException e) {
			log.error("", e);
			return false;
		}
	}

	public static class RootSearcher extends SimpleFileVisitor<Path> {
		
		private Path root;
		private boolean rootFound = false;
		
		public Path getRoot() {
			return root;
		}
		
		public boolean foundRoot() {
			return root != null && rootFound;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		throws IOException {
			Path tokenZero = file.getName(0);
			if("__MACOSX".equals(tokenZero.toString()) || Files.isHidden(file)) {
				//ignore
			} else if(root == null) {
				if(Files.isRegularFile(file) && file.getNameCount() > 1) {
					root = tokenZero;
					rootFound = true;
				}
			} else if(!root.equals(tokenZero)) {
				rootFound = false;
		        return FileVisitResult.TERMINATE;
			}
	        return FileVisitResult.CONTINUE;
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
package org.olat.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 
 * Initial date: 08.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PathUtils {
	
	public static Path visit(File file, String filename, FileVisitor<Path> visitor) 
	throws IOException {
		if(!StringHelper.containsNonWhitespace(filename)) {
			filename = file.getName();
		}
		
		Path fPath = null;
		if(file.isDirectory()) {
			fPath = file.toPath();
		} else if(filename != null && filename.toLowerCase().endsWith(".zip")) {
			fPath = FileSystems.newFileSystem(file.toPath(), null).getPath("/");
		} else {
			fPath = file.toPath();
		}
		if(fPath != null) {
		    Files.walkFileTree(fPath, visitor);
		}
		return fPath;
	}

}

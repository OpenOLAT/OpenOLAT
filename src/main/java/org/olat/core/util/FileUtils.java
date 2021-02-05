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
* <p>
*/ 

package org.olat.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSRepositoryModule;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;


/**
 * @author Mike Stock Comment:
 */
public class FileUtils {

	private static final Logger log = Tracing.createLoggerFor(FileUtils.class);
	
	// the following is for cleaning up file I/O stuff ... so it works fine on NFS
	public static final int BSIZE = 8*1024;

	// matches files and folders of type:
	// bla, bla1, bla12, bla.html, bla1.html, bla12.html
	private static final Pattern fileNamePattern = Pattern.compile("(.+?)\\p{Digit}*(\\.\\w{2,7})?");
	
	//windows: invalid characters for filenames: \ / : * ? " < > | 
	//linux: invalid characters for file/folder names: /, but you have to escape certain chars, like ";$%&*"
	//zip: may cause errors: =
	//OLAT reserved char: ":"	
	private static final char[] FILE_NAME_FORBIDDEN_CHARS = { '/', '\n', '\r', '\t', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':', ',', '=' };

	private static final char[] FILE_NAME_ACCEPTED_CHARS = { '\u0228', '\u0196', '\u0252', '\u0220', '\u0246', '\u0214', ' '};
	// known metadata files
	private static final List<String> META_FILENAMES = Arrays.asList(".DS_Store",".CVS",".nfs",".sass-cache",".hg", ".git");
	
	static {
		Arrays.sort(FILE_NAME_FORBIDDEN_CHARS);
		Arrays.sort(FILE_NAME_ACCEPTED_CHARS);
	}

	/**
	 * @param sourceFile
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean copyFileToDir(String sourceFile, String targetDir, FileFilter filter, String wt) {
		return copyFileToDir(new File(sourceFile), new File(targetDir), false, filter, wt);
	}
	/**
	 * @param sourceFile
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean copyFileToDir(String sourceFile, String targetDir) {
		return copyFileToDir(new File(sourceFile), new File(targetDir), false, null);
	}

	/**
	 * @param sourceFile
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean moveFileToDir(String sourceFile, String targetDir, FileFilter filter, String wt) {
		return copyFileToDir(new File(sourceFile), new File(targetDir), true, filter, wt);
	}
	/**
	 * @param sourceFile
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean moveFileToDir(String sourceFile, String targetDir) {
		return copyFileToDir(new File(sourceFile), new File(targetDir), true, null);
	}

	/**
	 * @param sourceFile
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean copyFileToDir(File sourceFile, File targetDir, FileFilter filter, String wt) {
		return copyFileToDir(sourceFile, targetDir, false, filter, wt);
	}
	/**
	 * @param sourceFile
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean copyFileToDir(File sourceFile, File targetDir, String wt) {
		return copyFileToDir(sourceFile, targetDir, false, null, wt);
	}
	
	/**
	 * 
	 * @param source The source
	 * @param targetDirectory The directory to copy the source in
	 * @param wt A message
	 * @return True if ok
	 */
	public static boolean copyItemToDir(VFSItem source, File targetDirectory, String wt) {
		targetDirectory.mkdirs();

		File target = new File(targetDirectory, source.getName());
		if(source instanceof VFSLeaf) {
			try(InputStream inStream = ((VFSLeaf)source).getInputStream();
					OutputStream outStream = new FileOutputStream(target)) {
				cpio(inStream, outStream, wt);
			} catch(IOException ex) {
				log.error("", ex);
				return false;
			}
		} else if(source instanceof VFSContainer) {
			target.mkdir();
			List<VFSItem> items = ((VFSContainer)source).getItems(new VFSSystemItemFilter());
			for(VFSItem item:items) {
				copyItemToDir(item, target, wt);
			}
		}
		return true;
	}

	/**
	 * @param sourceFile
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean moveFileToDir(File sourceFile, File targetDir, FileFilter filter, String wt) {
		return copyFileToDir(sourceFile, targetDir, true, filter, wt);
	}
	/**
	 * @param sourceFile
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean moveFileToDir(File sourceFile, File targetDir) {
		return copyFileToDir(sourceFile, targetDir, true, null);
	}

	/**
	 * @param sourceDir
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean copyDirToDir(String sourceDir, String targetDir, FileFilter filter, String wt) {
		return copyDirToDir(new File(sourceDir), new File(targetDir), false, filter, wt);
	}
	/**
	 * @param sourceDir
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean copyDirToDir(String sourceDir, String targetDir) {
		return copyDirToDir(new File(sourceDir), new File(targetDir), false, null);
	}

	/**
	 * @param sourceDir
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean moveDirToDir(String sourceDir, String targetDir, FileFilter filter, String wt) {
		return moveDirToDir(new File(sourceDir), new File(targetDir), filter, wt);
	}
	/**
	 * @param sourceDir
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean moveDirToDir(String sourceDir, String targetDir, String wt) {
		return moveDirToDir(new File(sourceDir), new File(targetDir), wt);
	}

	/**
	 * @param sourceDir
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean copyDirToDir(File sourceDir, File targetDir, FileFilter filter, String wt) {
		return copyDirToDir(sourceDir, targetDir, false, filter, wt);
	}
	/**
	 * @param sourceDir
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean copyDirToDir(File sourceDir, File targetDir, String wt) {
		return copyDirToDir(sourceDir, targetDir, false, null, wt);
	}

	/**
	 * @param sourceDir
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean moveDirToDir(File sourceDir, File targetDir, FileFilter filter, String wt) {
		return copyDirInternal(sourceDir, targetDir, true, false, filter, wt);
	}
	/**
	 * @param sourceDir
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean moveDirToDir(File sourceDir, File targetDir, String wt) {
		return copyDirInternal(sourceDir, targetDir, true, false, null, wt);
	}

	/**
	 * Get the size in bytes of a directory
	 * 
	 * @param path
	 * @return true upon success
	 */
	public static long getDirSize(File path) {
		File[] f = path.listFiles();
		if (f == null) {
			return 0;
		}
		
		Iterator<File> pathIterator = (Arrays.asList(f)).iterator();
		long size = 0l;
		while (pathIterator.hasNext()) {
			File currentFile = pathIterator.next();
			if (currentFile.isFile()) {
				size += currentFile.length();
			} else {
				size += getDirSize(currentFile);
			}
		}
		return size;
	}

	/**
	 * Copy the contents of a directory from one spot on hard disk to another. Will create any
	 * target dirs if necessary.
	 * 
	 * @param sourceDir directory which contents to copy on local hard disk.
	 * @param targetDir new directory to be created on local hard disk.
	 * @param filter file filter or NULL if no filter applied
	 * @param move
	 * @return true if the copy was successful.
	 */
	public static boolean copyDirContentsToDir(File sourceDir, File targetDir, boolean move, FileFilter filter, String wt) {
		return copyDirInternal(sourceDir, targetDir, move, false, filter, wt);
	}
	/**
	 * Copy the contents of a directory from one spot on hard disk to another. Will create any
	 * target dirs if necessary.
	 * 
	 * @param sourceDir directory which contents to copy on local hard disk.
	 * @param targetDir new directory to be created on local hard disk.
	 * @param move
	 * @return true if the copy was successful.
	 */
	public static boolean copyDirContentsToDir(File sourceDir, File targetDir, boolean move, String wt) {
		return copyDirInternal(sourceDir, targetDir, move, false, null, wt);
	}
	
	/**
	 * Copy a directory from one spot on hard disk to another. Will create any
	 * target dirs if necessary. The directory itself will be created on the target location.
	 * 
	 * @param sourceDir directory to copy on local hard disk.
	 * @param targetDir new directory to be created on local hard disk.
	 * @param filter file filter or NULL if no filter applied
	 * @param move
	 * @return true if the copy was successful.
	 */
	public static boolean copyDirToDir(File sourceDir, File targetDir, boolean move, FileFilter filter, String wt) {
		return copyDirInternal(sourceDir, targetDir, move, true, filter, wt);
	}
	/**
	 * Copy a directory from one spot on hard disk to another. Will create any
	 * target dirs if necessary. The directory itself will be created on the target location.
	 * 
	 * @param sourceDir directory to copy on local hard disk.
	 * @param targetDir new directory to be created on local hard disk.
	 * @param move
	 * @return true if the copy was successful.
	 */
	public static boolean copyDirToDir(File sourceDir, File targetDir, boolean move, String wt) {
		return copyDirInternal(sourceDir, targetDir, move, true, null, wt);
	}

	/**
	 * Copy a directory from one spot on hard disk to another. Will create any
	 * target dirs if necessary.
	 * 
	 * @param sourceDir directory to copy on local hard disk.
	 * @param targetDir new directory to be created on local hard disk.
	 * @param move
	 * @param createDir If true, a directory with the name of the source directory will be created
	 * @param filter file filter or NULL if no filter applied
	 * @return true if the copy was successful.
	 */
	private static boolean copyDirInternal(File sourceDir, File targetDir, boolean move, boolean createDir, FileFilter filter, String wt) {
		if (sourceDir.isFile()) return copyFileToDir(sourceDir, targetDir, move, filter, wt);
		if (!sourceDir.isDirectory()) return false;

		// copy only if filter allows. filtered items are considered a success
		// and not a failure of the operation
		if (filter != null
				&& ! filter.accept(sourceDir)) return true;
		
		targetDir.mkdirs(); // this will also copy/move empty directories
		if (!targetDir.isDirectory()) return false;

		if (createDir) targetDir = new File(targetDir, sourceDir.getName());
		if (move) {
			// in case of move just rename the directory to new location. The operation might fail 
			// on a NFS or when copying accross different filesystems. In such cases, continue and copy
			// the files instead
			if (sourceDir.renameTo(targetDir)) return true;
		} // else copy structure
		
		targetDir.mkdirs();
		boolean success = true;
		String[] fileList = sourceDir.list();
		if (fileList == null) return false; // I/O error or not a directory
		for (int i = 0; i < fileList.length; i++) {
			File f = new File(sourceDir, fileList[i]);
			if (f.isDirectory()) {
				success &= copyDirToDir(f, targetDir, move, filter, wt+File.separator+f.getName());
			} else {
				success &= copyFileToDir(f, targetDir, move, filter, wt+" file="+f.getName());
			}
		}
		
		// in case of a move accross different filesystems, clean up now
		if (move) {
			deleteFile(sourceDir);
		}
		return success;
	}

	/**
	 * Copy a file from one spot on hard disk to another. Will create any target
	 * dirs if necessary.
	 * 
	 * @param sourceFile file to copy on local hard disk.
	 * @param targetDir new file to be created on local hard disk.
	 * @param move
	 * @param filter file filter or NULL if no filter applied
	 * @return true if the copy was successful.
	 */
	public static boolean copyFileToDir(File sourceFile, File targetDir, boolean move, FileFilter filter, String wt) {
		try {
			// copy only if filter allows. filtered items are considered a success
			// and not a failure of the operation
			if (filter != null
					&& ! filter.accept(sourceFile)) return true;

			// catch if source is directory by accident
			if (sourceFile.isDirectory()) { return copyDirToDir(sourceFile, targetDir, move, filter, wt); }

			// create target directories
			targetDir.mkdirs(); // don't check for success... would return false on
			// existing dirs
			if (!targetDir.isDirectory()) return false;
			File targetFile = new File(targetDir, sourceFile.getName());

			// catch move/copy of "same" file -> buggy under Windows.
			if (sourceFile.getCanonicalPath().equals(targetFile.getCanonicalPath())) return true;
			if (move) { 
				// try to rename it first - operation might only be successful on a local filesystem!
				if (sourceFile.renameTo(targetFile)) return true;
				// it failed, so continue with copy code!
			}

			bcopy (sourceFile, targetFile, "copyFileToDir:"+wt);
			
			if (move) {
				// to finish the move accross different filesystems we need to delete the source file
				deleteFile(sourceFile);
			}
		} catch (IOException e) {
			log.error("Could not copy file::" + sourceFile.getAbsolutePath() + " to dir::" + targetDir.getAbsolutePath(), e);
			return false;
		}
		return true;
	} // end copy
	
	public static boolean copyToFile(InputStream in, File targetFile, String wt) throws IOException {
		if (targetFile.isDirectory()) return false;
		
		// create target directories
		targetFile.getParentFile().mkdirs(); // don't check for success... would return false on

		try(BufferedInputStream  bis = new BufferedInputStream(in);
				OutputStream dst = new FileOutputStream(targetFile);
				BufferedOutputStream bos = getBos (dst)) {
			cpio (bis, bos, wt);
			bos.flush();
			return true;
		} catch (IOException e) {
			throw e;
		}
	}
	
	/**
	 * Copy method to copy a file to another file
	 * @param sourceFile
	 * @param targetFile
	 * @param move true: move file; false: copy file
	 * @return true: success; false: failure
	 */
	public static boolean copyFileToFile(File sourceFile, File targetFile, boolean move) {
		try {
			if (sourceFile.isDirectory() || targetFile.isDirectory()) { return false; }

			// create target directories
			targetFile.getParentFile().mkdirs(); // don't check for success... would return false on
			
			// catch move/copy of "same" file -> buggy under Windows.
			if (sourceFile.getCanonicalPath().equals(targetFile.getCanonicalPath())) return true;
			if (move) { 
				// try to rename it first - operation might only be successful on a local filesystem!
				if (sourceFile.renameTo(targetFile)) return true;
				// it failed, so continue with copy code!
			}

			bcopy (sourceFile, targetFile, "copyFileToFile");
			
			if (move) {
				// to finish the move accross different filesystems we need to delete the source file
				deleteFile(sourceFile);
			}
		} catch (IOException e) {
			log.error("Could not copy file::{} to file::{}", sourceFile.getAbsolutePath(), targetFile.getAbsolutePath(), e);
			return false;
		}
		return true;
	} // end copy

	/**
	 * Copy a file from one spot on hard disk to another. Will create any target
	 * dirs if necessary.
	 * 
	 * @param sourceFile file to copy on local hard disk.
	 * @param targetDir new file to be created on local hard disk.
	 * @param move
	 * @return true if the copy was successful.
	 */
	public static boolean copyFileToDir(File sourceFile, File targetDir, boolean move, String wt) {
			return copyFileToDir(sourceFile, targetDir, move, null, wt);
	}

	/**
	 * Copy an InputStream to an OutputStream, until EOF. Use only when you don't
	 * know the length.
	 * 
	 * @param source InputStream, left open.
	 * @param target OutputStream, left open.
	 * @return true if the copy was successful.
	 */
	public static boolean copy(InputStream source, OutputStream target) {
		try {
			cpio(source, target, "");
			return true;
		} catch (IOException e) {
			log.error("Could not copy stream", e);
			return false;
		}
	}
	
	public static void deleteDirsAndFiles(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
			throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	/**
	 * Delete a file (not a filled directory) and the metadata.
	 * 
	 * @param file The file
	 * @return true if successfully deleted
	 */
	public static boolean deleteFile(File file) {
		boolean deleted = false;
		try {
			if(VFSRepositoryModule.canMeta(file) == VFSConstants.YES) {
				CoreSpringFactory.getImpl(VFSRepositoryService.class).deleteMetadata(file);
			}
			deleted = Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			log.error("Cannot delete file: {}", file, e);
			deleted = false;
		}
		return deleted;
	}

	/**
	 * Get rid of ALL files and subdirectories in given directory, and all subdirs
	 * under it,
	 * 
	 * @param dir would normally be an existing directory, can be a file aswell
	 * @param recursive true if you want subdirs deleted as well
	 * @param deleteDir true if dir needs to be deleted as well
	 * @return true upon success
	 */
	public static boolean deleteDirsAndFiles(File dir, boolean recursive, boolean deleteDir) {

		boolean success = true;

		if (dir == null) return false;

		// We must empty child subdirs contents before can get rid of immediate
		// child subdirs
		if (recursive) {
			String[] allDirs = dir.list();
			if (allDirs != null) {
				for (int i = 0; i < allDirs.length; i++) {
					success &= deleteDirsAndFiles(new File(dir, allDirs[i]), true, false);
				}
			}
		}

		// delete all files in this dir
		String[] allFiles = dir.list();
		if (allFiles != null) {
			for (int i = 0; i < allFiles.length; i++) {
				File deleteFile = new File(dir, allFiles[i]);
				success &= deleteFile(deleteFile);
			}
		}

		// delete passed dir
		if (deleteDir) {
			success &= deleteFile(dir);
		}
		return success;
	} // end deleteDirContents

	/**
	 * @param newF
	 */
	public static void createEmptyFile(File newF) {
		try {
			FileOutputStream fos = new FileOutputStream(newF);
			fos.close();
		} catch (IOException e) {
			throw new AssertException("empty file could not be created for path " + newF.getAbsolutePath(), e);
		}

	}
	
	
	/**
	 * @param baseDir
	 * @param fileVisitor
	 */
	public static void visitRecursively(File baseDir, FileVisitor fileVisitor) {
		visit(baseDir, fileVisitor);
	}
	
	private static void visit(File file, FileVisitor fileVisitor) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				visit(f, fileVisitor);
			}
		}
		else { // regular file
			fileVisitor.visit(file);
		}
	}
	
	
	/**
	 * @param target
	 * @param data
	 * @param encoding
	 */
	public static void save(File target, String data, String encoding) {
		try(OutputStream out=new FileOutputStream(target)) {
			save(out, data, StringHelper.check4xMacRoman(encoding));
		} catch (IOException e) {
			throw new OLATRuntimeException(FileUtils.class, "could not save file", e);
		}
	}
	
	public static InputStream getInputStream(String data, String encoding) throws IOException {
		try {
			byte[] ba = data.getBytes(StringHelper.check4xMacRoman(encoding));
			return new ByteArrayInputStream(ba);
		} catch (IOException e) {
			throw new IOException("could not save to output stream", e);
		}
	}

	/**
	 * @param target
	 * @param data
	 * @param encoding
	 */
	public static void save(OutputStream target, String data, String encoding) {
		try(InputStream bis=getInputStream(data, encoding)) {
			cpio(bis, target, "saveDataToFile");
		} catch (Exception e) {
			throw new OLATRuntimeException(FileUtils.class, "could not save to output stream", e);
		}
	}
	
	/**
	 * Save a given input stream to a file
	 * @param source the input stream
	 * @param target the file
	 */
	public static void save(InputStream source, File target) {
		try(BufferedInputStream bis = new BufferedInputStream(source);
				BufferedOutputStream bos = getBos(target)) {
			cpio (bis, bos, "fileSave");
			bos.flush();
		} catch (IOException e) {
			throw new OLATRuntimeException(FileUtils.class, "could not save stream to file::" + target.getAbsolutePath(), e);
		}		
	}

	/**
	 * @param source
	 * @param encoding
	 * @return the file in form of a string
	 */
	public static String load(File source, String encoding) {
		try(InputStream in=new FileInputStream(source);
				BufferedInputStream bis = new BufferedInputStream(in, FileUtils.BSIZE)) {
			return load(bis, encoding);
		} catch (IOException e) {
			throw new RuntimeException("could not copy file to ram: " + source.getAbsolutePath());
		}
	}

	/**
	 * @param source
	 * @param encoding
	 * @return the inpustream in form of a string
	 */
	public static String load(InputStream source, String encoding) {
		String htmltext = null;
		try(ByteArrayOutputStream bas = new ByteArrayOutputStream()) {
			boolean success = copy(source, bas);
			if (!success) throw new RuntimeException("could not copy inputstream to ram");
			htmltext = bas.toString(StringHelper.check4xMacRoman(encoding));
		} catch (IOException e) {
			throw new OLATRuntimeException(FileUtils.class, "could not load from inputstream", e);
		} finally {
			closeSafely(source);
		}
		return htmltext;
	}

	/**
	 * checks whether the given File is a Directory and if it contains any files or sub-directories
	 * 
	 * 
	 * @return returns true if given File-object is a directory and contains any files or subdirectories 
	 */
	public static boolean isDirectoryAndNotEmpty(File directory){
		String[] content = directory.list();
		if(content == null) return false;
		return (content.length > 0); 
	}
	
	/**
	 * 
	 * @param dir
	 * @param file
	 * @return
	 */
	public static boolean isInSubDirectory(File dir, File file) {
	    if (file == null) {
	        return false;
	    }
	    if (file.equals(dir)) {
	        return true;
	    }
	    return isInSubDirectory(dir, file.getParentFile());
	}
	
	/**
	 * @param cl The closeable to close, may also be null
	 */
	public static void closeSafely(Closeable cl) {
		if (cl == null) return;
		try {
			cl.close();
		} catch (IOException e) {
			// nothing to do
		}
	}

	/**
	 * Extract file suffix. E.g. 'html' from index.html
	 * @param filePath
	 * @return return empty String "" without suffix. 
	 */
	public static String getFileSuffix(String filePath) {
		if(StringHelper.containsNonWhitespace(filePath)) { 
			int lastDot = filePath.lastIndexOf('.');
			if (lastDot > 0) {
				if (lastDot < filePath.length())
					return filePath.substring(lastDot + 1).toLowerCase();
			}
		}
		return "";
	}
	
	/**
	 * Inserts the ending before the suffix.
	 * E.g.: test.html => test_copy.html
	 *
	 * @param filename
	 * @param ending
	 * @return
	 */
	public static String insertBeforeSuffix(String filename, String ending) {
		if (!StringHelper.containsNonWhitespace(filename)) return ending;
		if (!StringHelper.containsNonWhitespace(ending)) return filename;
		
		int lastDot = filename.lastIndexOf('.');
		return lastDot > 0
				? filename.substring(0, lastDot) + ending + filename.substring(lastDot)
				: filename + ending;
	}
	
	/**
	 * Simple check for filename validity. 
	 * It compares each character if it is accepted, forbidden or in a certain (Latin-1) range. <p>
	 * Characters < 33 --> control characters and space
	 * Characters > 255 --> above ASCII
	 * http://www.danshort.com/ASCIImap/
	 * 
	 * @param filename
	 * @return true if filename valid
	 */
	public static boolean validateFilename(String filename) {
		if(!StringHelper.containsNonWhitespace(filename)) {
			return false;
		}

		for(int i=0; i<filename.length(); i++) {
			char character = filename.charAt(i);
			if(Arrays.binarySearch(FILE_NAME_ACCEPTED_CHARS, character)>=0) {
				continue;
			} else if(character<33 || character>255 || Arrays.binarySearch(FILE_NAME_FORBIDDEN_CHARS, character)>=0) {
				return false;
			}
		}
		//check if there are any unwanted path denominators in the name
		if (filename.indexOf("..") > -1) {
			return false;
		}
		return true;
	}
	
	public static String normalizeFilename(String name) {
		String nameFirstPass = name.replace(" ", "_")
				.replace("\u00C4", "Ae")
				.replace("\u00D6", "Oe")
				.replace("\u00DC", "Ue")
				.replace("\u00E4", "ae")
				.replace("\u00F6", "oe")
				.replace("\u00FC", "ue")
				.replace("\u00DF", "ss")
				.replace("\u00F8", "o")
				.replace("\u2205", "o")
				.replace("\u00E6", "ae");
		String nameNormalized = Normalizer.normalize(nameFirstPass, Normalizer.Form.NFKD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+","");
		nameNormalized = nameNormalized.replaceAll("\\W+", "");
		nameNormalized = nameNormalized.length() > 0? nameNormalized: "_";
		return nameNormalized;
	}
	
	/**
	 * Cleans the filename from invalid character to make a filename compatible for
	 * the usual operating systems and browsers.. Suffixes are preserved. This
	 * method is not as strict as {@link #normalizeFilename(String)}.
	 * 
	 * @param filename
	 * @return the cleaned filename
	 */
	public static String cleanFilename(String filename) {
		boolean hasExtension = false;
		String name = filename;
		String extension = getFileSuffix(filename);
		if (extension != null && extension.length() > 0) {
			hasExtension = true;
			name = filename.substring(0, filename.length() - extension.length() - 1);
		}
		StringBuilder normalizedFilename = new StringBuilder();
		normalizedFilename.append(cleanFilenamePart(name));
		if (hasExtension) {
			normalizedFilename.append(".");
			normalizedFilename.append(cleanFilenamePart(extension));
		}
		return normalizedFilename.toString();
	}
	
	private static String cleanFilenamePart(String filename) {
		String cleaned = Normalizer.normalize(filename, Normalizer.Form.NFKD);
		cleaned = cleaned.replaceAll("\\p{InCombiningDiacriticalMarks}+","");
		for (char character: FILE_NAME_FORBIDDEN_CHARS) {
			cleaned = cleaned.replace(character, '_');
		}
		return cleaned;
	}
	
	/**
	 * Creates a new directory in the specified directory, using the given prefix and suffix strings to generate its name.
	 * It uses File.createTempFile() and should provide a unique name.
	 * @param prefix
	 * @param suffix
	 * @param directory
	 * @return
	 */
	public static File createTempDir(String prefix, String suffix, File directory) {
		File tmpDir = null;
		try {
			File tmpFile = File.createTempFile(prefix, suffix, directory);
			if(tmpFile.exists()) {
				deleteFile(tmpFile);
			}
			boolean tmpDirCreated = tmpFile.mkdir();
			if(tmpDirCreated) {
			  tmpDir = tmpFile;
			}			
		} catch (Exception e) {
			//bummer!
		}
		return tmpDir;
	}
	
	public static void bcopy (File src, File dst, String wt) throws IOException {
		try(InputStream in=new FileInputStream(src);
				BufferedInputStream bis = new BufferedInputStream(in, FileUtils.BSIZE);
				OutputStream out=new FileOutputStream(dst)) {
			cpio(bis, out, wt);
		} catch(IOException e) {
			log.error("", e);
		}
	}	
	public static void bcopy(InputStream src, File dst, String wt) throws IOException {
		try(OutputStream out=new FileOutputStream(dst)) {
			cpio(src, out, "copyStreamToFile:".concat(wt));
		} catch(IOException e) {
			log.error("", e);
		}
	}
	public static BufferedOutputStream getBos (FileOutputStream of) {
		return new BufferedOutputStream (of, BSIZE);
	}
	public static BufferedOutputStream getBos (OutputStream os) {
		return new BufferedOutputStream (os, BSIZE);
	}
	public static BufferedOutputStream getBos (File of) throws FileNotFoundException {
		return getBos (new FileOutputStream(of));
	}
	public static BufferedOutputStream getBos (String fname) throws FileNotFoundException {
		return getBos (new File (fname));
	}
	
	/**
	 * copy in, copy out (leaves both streams open) 
	 * <p> 
	 * @see FileUtils.getBos() which creates a matching BufferedOutputStream
	 * </p>
	 * 
	 * @param in BuferedInputStream
	 * @param out BufferedOutputStream
	 * @param wt What this I/O is about
	 */
	public static long cpio (InputStream in, OutputStream out, String wt) throws IOException {
		
		byte[] buffer = new byte[BSIZE];

		int c;
		long tot = 0;
		long s = 0;
		boolean debug = log.isDebugEnabled();
		if(debug) {
			s = System.nanoTime();
		}
		
		while ((c = in.read(buffer, 0, buffer.length)) != -1) {
    		out.write(buffer, 0, c);
    		tot += c;
		}
		
		if(debug) {
			long tim = System.nanoTime() - s;
			double dtim = tim == 0 ? 0.5 : tim; // avg of those less than 1 nanoseconds is taken as 0.5 nanoseconds
			double bps = tot*1000*1000/dtim;
			log.debug(String.format("cpio %,13d bytes %6.2f ms avg %6.1f Mbps %s", tot, dtim/1000/1000, bps/1024, wt));
		}
		return tot;
	}
	
	/**
	 * from a newer version of apache commons.io Determines whether the specified
	 * file is a Symbolic Link rather than an actual file.
	 * <p>
	 * Will not return true if there is a Symbolic Link anywhere in the path, only
	 * if the specific file is.
	 * 
	 * @param file the file to check
	 * @return true if the file is a Symbolic Link
	 * @throws IOException if an IO error occurs while checking the file
	 * @since Commons IO 2.0
	 */
	public static boolean isSymlink(File file) throws IOException {
		if (file == null) { throw new NullPointerException("File must not be null"); }
		if ("\\".equals(File.separatorChar) ) { return false; } // Windows doesn't know symlinks!
		File fileInCanonicalDir = null;
		if (file.getParent() == null) {
			fileInCanonicalDir = file;
		} else {
			File canonicalDir = file.getParentFile().getCanonicalFile();
			fileInCanonicalDir = new File(canonicalDir, file.getName());
		}

		if (fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile())) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Check if the given filename is a metadata filename generated by macOS or
	 * windows when browsing a directory or generated by one of the known
	 * repository systems.
	 * 
	 * @param filename
	 * @return
	 */
	public static boolean isMetaFilename(String filename) {
		boolean isMeta = false;
		if (filename != null) {
			// 1) check for various known filenames 
			isMeta = META_FILENAMES.parallelStream().anyMatch(filename::contains);
			if (!isMeta) {
				// 2) macOS meta files generated with WebDAV starts with ._
				isMeta = filename.startsWith("._");
			}
			
		}
		return isMeta;
	}
	

	
	
	public static String rename(File f) {
		String filename = f.getName();
		String newName = filename;
		File newFile = f;
		for(int count=0; newFile.exists() && count < 999 ; ) {
			count++;
			newName = appendNumberAtTheEndOfFilename(filename, count);
			newFile = new File(f.getParentFile(), newName);
		}
		if(!newFile.exists()) {
			return newName;
		}
		return null;
	}
	
	/**
	 * Sticks together a new filename. If there's a match with a common filename
	 * with extension, add the counter to the end of the filename before the
	 * extension. Else just add the counter to the end of the name. E.g.:
	 * hello.xml => hello1.xml where 1 is the counter
	 * hello1.xml => hello2.xml
	 * blaber 	 => blaber1
	 * blaber1 	 => blaber2
	 * 
	 * @param name
	 * @param number
	 * @return The new name with the counter added
	 */
	public static String appendNumberAtTheEndOfFilename(String name, int number) {
		// Try to match the file to the pattern "[name].[extension]"
		Matcher m = fileNamePattern.matcher(name);
		StringBuilder newName = new StringBuilder();
		if (m.matches()) {
			newName.append(m.group(1)).append(number);
			if (m.group(2) != null) {
				// is null in case it was not a file or does not contain a file ending.
				newName.append(m.group(2));
			}
		} else {
			newName.append(name).append(number);
		}
		return newName.toString();
	}
}
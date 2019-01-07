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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.meta.MetaInfo;
import org.olat.core.util.vfs.version.Versionable;

/**
 * Initial Date:  04.12.2002
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class ZipUtil {

	private static final String DIR_NAME__MACOSX = "__MACOSX/";
	
	private static final OLog log = Tracing.createLoggerFor(ZipUtil.class);

	/**
	 * Unzip a file to a directory
	 * @param zipFile		The zip file to unzip
	 * @param targetDir	The directory to unzip the file to
	 * @return	True if successfull, false otherwise
	 */
	public static boolean unzip(File zipFile, File targetDir) {
		try {
			xxunzip(new FileInputStream(zipFile), targetDir.getAbsolutePath());
			return true;
		} catch (IOException e) {
			log.error("I/O failure while unzipping "+zipFile.getAbsolutePath()+" to "+targetDir.getAbsolutePath());
			return false;
		}
	}
	
	/**
	 * Unzip a VFSLeaf (zip zip archive file) to a directory
	 * @param zipLeaf	zip archive file to unzip
	 * @param targetDir	The directory to unzip the file to
	 * @return	True if successfull, false otherwise
	 */
	public static boolean unzip(VFSLeaf zipLeaf, VFSContainer targetDir) {
		
		if (targetDir instanceof LocalFolderImpl) {
			String outdir = ((LocalFolderImpl) targetDir).getBasefile().getAbsolutePath();
			try {
				xxunzip(zipLeaf.getInputStream(), outdir);
				return true;
			} catch (IOException e) {
				log.error("I/O failure while unzipping "+zipLeaf.getName()+" to "+outdir);
				return false;
			}
		}
		
		return unzip(zipLeaf, targetDir, null, false);
	}
	
	/**
	 * Unzip a file in the target dir with the restricted version
	 * @param zipFile
	 * @param targetDir
	 * @return
	 */
	public static boolean unzipStrict(File zipFile, VFSContainer targetDir) {
		if (targetDir instanceof LocalFolderImpl) {
			String outdir = ((LocalFolderImpl) targetDir).getBasefile().getAbsolutePath();
			
			try(InputStream in = new FileInputStream(zipFile)) {
				xxunzip (in, outdir);
				return true;
			} catch (IOException e) {
				log.error("I/O failure while unzipping "+zipFile.getName()+" to "+outdir);
				return false;
			}
		}
		return false;
	}
	
	
	/**
	 * Unzip a file to a directory using the versioning system of VFS
	 * @param zipLeaf	The file to unzip
	 * @param targetDir	The directory to unzip the file to
	 * @param the identity of who unzip the file
	 * @param versioning enabled or not
	 * @return	True if successfull, false otherwise
	 */
	public static boolean unzip(VFSLeaf zipLeaf, VFSContainer targetDir, Identity identity, boolean versioning) {
		InputStream in = zipLeaf.getInputStream();
		boolean unzipped = unzip(in, targetDir, identity, versioning);
		FileUtils.closeSafely(in);
		return unzipped;
	}	

	/**
	 * Unzip an inputstream to a directory using the versioning system of VFS
	 * @param zipLeaf	The file to unzip
	 * @param targetDir	The directory to unzip the file to
	 * @param the identity of who unzip the file
	 * @param versioning enabled or not
	 * @return	True if successfull, false otherwise
	 */
	private static boolean unzip(InputStream in, VFSContainer targetDir, Identity identity, boolean versioning) {
		try(ZipInputStream oZip = new ZipInputStream(in)) {
			// unzip files
			ZipEntry oEntr = oZip.getNextEntry();
			while (oEntr != null) {
				if (oEntr.getName() != null && !oEntr.getName().startsWith(DIR_NAME__MACOSX)) {
					if (oEntr.isDirectory()) {
						// skip MacOSX specific metadata directory
						// create directories
						getAllSubdirs(targetDir, oEntr.getName(), identity, true);
					} else {
						// create file
						VFSContainer createIn = targetDir;
						String name = oEntr.getName();
						// check if entry has directories which did not show up as
						// directories above
						int dirSepIndex = name.lastIndexOf('/');
						if (dirSepIndex == -1) {
							// try it windows style, backslash is also valid format
							dirSepIndex = name.lastIndexOf('\\');
						}
						if (dirSepIndex > 0) {
							// create subdirs
							createIn = getAllSubdirs(targetDir, name.substring(0, dirSepIndex), identity, true);
							if (createIn == null) {
								if (log.isDebug()) log.debug("Error creating directory structure for zip entry: "
										+ oEntr.getName());
								return false;
							}
							name = name.substring(dirSepIndex + 1);
						}
						
						if(versioning) {
							VFSLeaf newEntry = (VFSLeaf)createIn.resolve(name);
							if(newEntry == null) {
								newEntry = createIn.createChildLeaf(name);
								OutputStream out = newEntry.getOutputStream(false);
								if (!FileUtils.copy(oZip, out)) return false;
								FileUtils.closeSafely(out);
							} else if (newEntry instanceof Versionable) {
								Versionable versionable = (Versionable)newEntry;
								if(versionable.getVersions().isVersioned()) {
									versionable.getVersions().addVersion(identity, "", oZip);
								}
							}
							if(newEntry != null && identity != null && newEntry.canMeta() == VFSConstants.YES) {
								MetaInfo info = newEntry.getMetaInfo();
								if(info != null) {
									info.setAuthor(identity);
									info.write();
								}
							}
							
						} else {
							VFSLeaf newEntry = createIn.createChildLeaf(name);
							if (newEntry != null) {
								OutputStream out = newEntry.getOutputStream(false);
								if (!FileUtils.copy(oZip, out)) return false;
								FileUtils.closeSafely(out);
					
								if(identity != null && newEntry.canMeta() == VFSConstants.YES) {
									MetaInfo info = newEntry.getMetaInfo();
									if(info != null) {
										info.setAuthor(identity);
										info.write();
									}
								}
							}
						}
					}
				}
				oZip.closeEntry();
				oEntr = oZip.getNextEntry();
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	} // unzip
	
	/**
	 * Unzip a file to a directory using the versioning system of VFS and a ZIP
	 * library which handle encoding errors. It may results in special characters
	 * wrongly translated on the file system.
	 * @param zipLeaf	The file to unzip
	 * @param targetDir	The directory to unzip the file to
	 * @param the identity of who unzip the file
	 * @param versioning enabled or not
	 * @return	True if successfull, false otherwise
	 */
	public static boolean unzipNonStrict(VFSLeaf zipLeaf, VFSContainer targetDir, Identity identity, boolean versioning) {
		InputStream in = zipLeaf.getInputStream();
		boolean unzipped = unzipNonStrict(in, targetDir, identity, versioning);
		FileUtils.closeSafely(in);
		return unzipped;
	}	
	
	/**
	 * Unzip with jazzlib
	 * @param in
	 * @param targetDir
	 * @param identity
	 * @param versioning
	 * @return
	 */
	private static boolean unzipNonStrict(InputStream in, VFSContainer targetDir, Identity identity, boolean versioning) {
		try(net.sf.jazzlib.ZipInputStream oZip = new net.sf.jazzlib.ZipInputStream(in)) {
			// unzip files
			net.sf.jazzlib.ZipEntry oEntr = oZip.getNextEntry();
			while (oEntr != null) {
				if (oEntr.getName() != null && !oEntr.getName().startsWith(DIR_NAME__MACOSX)) {
					if (oEntr.isDirectory()) {
						// skip MacOSX specific metadata directory
						// create directories
						getAllSubdirs(targetDir, oEntr.getName(), identity, true);
					} else {
						// create file
						VFSContainer createIn = targetDir;
						String name = oEntr.getName();
						// check if entry has directories which did not show up as
						// directories above
						int dirSepIndex = name.lastIndexOf('/');
						if (dirSepIndex == -1) {
							// try it windows style, backslash is also valid format
							dirSepIndex = name.lastIndexOf('\\');
						}
						if (dirSepIndex > 0) {
							// create subdirs
							createIn = getAllSubdirs(targetDir, name.substring(0, dirSepIndex), identity, true);
							if (createIn == null) {
								if (log.isDebug()) log.debug("Error creating directory structure for zip entry: "
										+ oEntr.getName());
								return false;
							}
							name = name.substring(dirSepIndex + 1);
						}
						
						if(versioning) {
							VFSLeaf newEntry = (VFSLeaf)createIn.resolve(name);
							if(newEntry == null) {
								newEntry = createIn.createChildLeaf(name);
								OutputStream out = newEntry.getOutputStream(false);
								if (!FileUtils.copy(oZip, out)) return false;
								FileUtils.closeSafely(out);
							} else if (newEntry instanceof Versionable) {
								Versionable versionable = (Versionable)newEntry;
								if(versionable.getVersions().isVersioned()) {
									versionable.getVersions().addVersion(identity, "", oZip);
								}
							}
							if(identity != null && newEntry.canMeta() == VFSConstants.YES) {
								MetaInfo info = newEntry.getMetaInfo();
								if(info != null) {
									info.setAuthor(identity);
									info.write();
								}
							}
							
						} else {
							VFSLeaf newEntry = createIn.createChildLeaf(name);
							if (newEntry != null) {
								OutputStream out = newEntry.getOutputStream(false);
								if (!FileUtils.copy(oZip, out)) return false;
								FileUtils.closeSafely(out);
							
								if(identity != null && newEntry.canMeta() == VFSConstants.YES) {
									MetaInfo info = newEntry.getMetaInfo();
									if(info != null) {
										info.setAuthor(identity);
										info.write();
									}
								}
							}
						}
					}
				}
				oZip.closeEntry();
				oEntr = oZip.getNextEntry();
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	} // unzip
	
	/**
	 * Check if a file in the zip is already in the path
	 * @param zipLeaf
	 * @param targetDir
	 * @param identity
	 * @param isAdmin
	 * @return the list of files which already exist
	 */
	public static List<String> checkLockedFileBeforeUnzip(VFSLeaf zipLeaf, VFSContainer targetDir, Identity identity, Roles isAdmin) {
		List<String> lockedFiles = new ArrayList<>();
		VFSLockManager vfsLockManager = CoreSpringFactory.getImpl(VFSLockManager.class);
		
		try(InputStream in = zipLeaf.getInputStream();
				ZipInputStream oZip = new ZipInputStream(in)) {
			// unzip files
			ZipEntry oEntr = oZip.getNextEntry();
			while (oEntr != null) {
				if (oEntr.getName() != null && !oEntr.getName().startsWith(DIR_NAME__MACOSX)) {
					if (oEntr.isDirectory()) {
						// skip MacOSX specific metadata directory
						// directories aren't locked
						oZip.closeEntry();
						oEntr = oZip.getNextEntry();
						continue;
					} else {
						// search file
						VFSContainer createIn = targetDir;
						String name = oEntr.getName();
						// check if entry has directories which did not show up as
						// directories above
						int dirSepIndex = name.lastIndexOf('/');
						if (dirSepIndex == -1) {
							// try it windows style, backslash is also valid format
							dirSepIndex = name.lastIndexOf('\\');
						}
						if (dirSepIndex > 0) {
							// get subdirs
							createIn = getAllSubdirs(targetDir, name.substring(0, dirSepIndex), identity, false);
							if (createIn == null) {
								//sub directories don't exist, and aren't locked
								oZip.closeEntry();
								oEntr = oZip.getNextEntry();
								continue;
							}
							name = name.substring(dirSepIndex + 1);
						}
						
						VFSLeaf newEntry = (VFSLeaf)createIn.resolve(name);
						if(vfsLockManager.isLockedForMe(newEntry, identity, isAdmin)) {
							lockedFiles.add(name);
						}
					}
				}
				oZip.closeEntry();
				oEntr = oZip.getNextEntry();
			}
		} catch (IOException e) {
			return null;
		}

		return lockedFiles;
	}
	
	/**
	 * 
	 * @param zipLeaf
	 * @param targetDir
	 * @param identity
	 * @param roles
	 * @return
	 */
	public static List<String> checkLockedFileBeforeUnzipNonStrict(VFSLeaf zipLeaf, VFSContainer targetDir, Identity identity, Roles roles) {
		List<String> lockedFiles = new ArrayList<>();
		VFSLockManager vfsLockManager = CoreSpringFactory.getImpl(VFSLockManager.class);
		
		try(InputStream in = zipLeaf.getInputStream();
				net.sf.jazzlib.ZipInputStream oZip = new net.sf.jazzlib.ZipInputStream(in);) {
			// unzip files
			net.sf.jazzlib.ZipEntry oEntr = oZip.getNextEntry();
			while (oEntr != null) {
				if (oEntr.getName() != null && !oEntr.getName().startsWith(DIR_NAME__MACOSX)) {
					if (oEntr.isDirectory()) {
						// skip MacOSX specific metadata directory
						// directories aren't locked
						oZip.closeEntry();
						oEntr = oZip.getNextEntry();
						continue;
					} else {
						// search file
						VFSContainer createIn = targetDir;
						String name = oEntr.getName();
						// check if entry has directories which did not show up as
						// directories above
						int dirSepIndex = name.lastIndexOf('/');
						if (dirSepIndex == -1) {
							// try it windows style, backslash is also valid format
							dirSepIndex = name.lastIndexOf('\\');
						}
						if (dirSepIndex > 0) {
							// get subdirs
							createIn = getAllSubdirs(targetDir, name.substring(0, dirSepIndex), identity, false);
							if (createIn == null) {
								//sub directories don't exist, and aren't locked
								oZip.closeEntry();
								oEntr = oZip.getNextEntry();
								continue;
							}
							name = name.substring(dirSepIndex + 1);
						}
						
						VFSLeaf newEntry = (VFSLeaf)createIn.resolve(name);
						if(vfsLockManager.isLockedForMe(newEntry, identity, roles)) {
							lockedFiles.add(name);
						}
					}
				}
				oZip.closeEntry();
				oEntr = oZip.getNextEntry();
			}
		} catch (IOException e) {
			return null;
		}

		return lockedFiles;
	}

	/**
	 * Get the whole subpath.
	 * @param create the missing directories
	 * @param base
	 * @param subDirPath
	 * @return Returns the last container of this subpath.
	 */
	public static VFSContainer getAllSubdirs(VFSContainer base, String subDirPath, Identity identity, boolean create) {
		StringTokenizer st;
		if (subDirPath.indexOf('/') != -1) { 
			st = new StringTokenizer(subDirPath, "/", false);
		} else {
			// try it windows style, backslash is also valid format
			st = new StringTokenizer(subDirPath, "\\", false);
		}
		VFSContainer currentPath = base;
		while (st.hasMoreTokens()) {
			String nextSubpath = st.nextToken();
			VFSItem vfsSubpath = currentPath.resolve(nextSubpath);
			if (vfsSubpath == null && !create) {
				return null;
			}
			if (vfsSubpath == null || (vfsSubpath instanceof VFSLeaf)) {
				vfsSubpath = currentPath.createChildContainer(nextSubpath);
				if (vfsSubpath == null) return null;
				if (identity != null && vfsSubpath.canMeta() == VFSConstants.YES) {
					MetaInfo info = vfsSubpath.getMetaInfo();
					if(info != null) {
						info.setAuthor(identity);
						info.write();
					}
				}
			}
			currentPath = (VFSContainer)vfsSubpath;
		}
		return currentPath;
	}
	
	/**
	 * Add the set of files residing in root to the ZIP file named target.
	 * Files in subfolders will be compressed too.
	 * if target already exists, this will abort and return false.
	 * 
	 * @param files		Filenames to add to ZIP, relative to root
	 * @param root		Base path.
	 * @param target	Target ZIP file.
	 * @param compress to compress ot just store
	 * @return true if successfull, false otherwise.
	 */
	public static boolean zip(Set<String> files, File root, File target, boolean compress) {
		//	Create a buffer for reading the files
		if (target.exists()) return false;
		List<VFSItem> vfsFiles = new ArrayList<>();
		LocalFolderImpl vfsRoot = new LocalFolderImpl(root);
		for (Iterator<String> iter = files.iterator(); iter.hasNext();) {
			String fileName = iter.next();
			VFSItem item = vfsRoot.resolve(fileName);
			if (item == null) return false;
			vfsFiles.add(item);
		}
		return zip(vfsFiles, new LocalFileImpl(target), compress);
	} // zip
	
	/**
	 * Add the set of files residing in root to the ZIP file named target.
	 * Files in subfolders will be compressed too.
	 * 
	 * @param files		Filenames to add to ZIP, relative to root
	 * @param root		Base path.
	 * @param target	Target ZIP file.
	 * @param compress to compress ot just store
	 * @return true if successfull, false otherwise.
	 */
	public static boolean zip(Set<String> files, File root, File target) {
		return zip(files, root, target, true);
	} // zip
	
	public static boolean zip(List<VFSItem> vfsFiles, VFSLeaf target, boolean compress) {
		boolean success = true;
		
		String zname = target.getName();
		if (target instanceof LocalImpl) {
			zname = ((LocalImpl)target).getBasefile().getAbsolutePath();
		}

		
		OutputStream out = target.getOutputStream(false);
		

		if (out == null) {
			throw new OLATRuntimeException(ZipUtil.class, "Error getting output stream for file: " + zname, null);
		}
		
		long s = System.currentTimeMillis();
		
		ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(out, FileUtils.BSIZE));
		
		if (vfsFiles.isEmpty()) {
			try {
				zipOut.close();
			} catch (IOException e) {
				//
			}
			return true;
		}

		zipOut.setLevel(compress?9:0);
		for (Iterator<VFSItem> iter = vfsFiles.iterator(); success && iter.hasNext();) {
			success = addToZip(iter.next(), "", zipOut);
		}
		
		try {
			zipOut.flush();
			zipOut.close();
			log.info("zipped ("+(compress?"compress":"store")+") "+zname+" t="+Long.toString(System.currentTimeMillis()-s));
		} catch (IOException e) {
			throw new OLATRuntimeException(ZipUtil.class, "I/O error closing file: " + zname, null);
		}
		
		return success;
	}
	
	public static boolean addToZip(VFSItem vfsItem, String currentPath, ZipOutputStream out) {

		boolean success = true;
		InputStream in = null;

		byte[] buffer = new byte[FileUtils.BSIZE];

		try {
			// The separator / is the separator defined by the ZIP standard
			String itemName = currentPath.length() == 0 ?
					vfsItem.getName() : currentPath + "/" + vfsItem.getName();
					
			if (vfsItem instanceof VFSContainer) {
				
				out.putNextEntry(new ZipEntry(itemName + "/"));
				out.closeEntry();
				
				List<VFSItem> items = ((VFSContainer)vfsItem).getItems();
				for (Iterator<VFSItem> iter = items.iterator(); iter.hasNext();) {
					if (!addToZip(iter.next(), itemName, out)) {
						success = false;
						break;
					}
				}
				
			} else {
				
				out.putNextEntry(new ZipEntry(itemName));
				in = ((VFSLeaf)vfsItem).getInputStream();
				
				int c;
				while ((c = in.read(buffer, 0, buffer.length)) != -1) {
					out.write(buffer, 0, c);
				}
				
				out.closeEntry();
			}
		} catch (IOException ioe) {
			String name = vfsItem.getName();
			if (vfsItem instanceof LocalImpl) {
				name = ((LocalImpl)vfsItem).getBasefile().getAbsolutePath();
			}
			log.error("I/O error while adding "+name+" to zip:"+ioe);
			return false;
		} finally {
			FileUtils.closeSafely(in);
		}
		return success;
	}


	/**
	 * Zip all files under a certain root directory. (choose to compress or not param compress)
	 * 
	 * @param rootFile
	 * @param targetZipFile
	 * @param compress to compress or just store (if already compressed)
	 * @return true = success, false = exception/error
	 */
	public static boolean zipAll(File rootFile, File targetZipFile, boolean compress) {
		Set<String> fileSet = new HashSet<>();
		String[] files = rootFile.list();
		for (int i = 0; i < files.length; i++) {
			fileSet.add(files[i]);
		}		
		return zip(fileSet, rootFile, targetZipFile, compress);
	}
	
	/**
	 * Add the content of a directory to a zip stream.
	 * 
	 * @param path
	 * @param dirName
	 * @param zout
	 */
	public static void addDirectoryToZip(final Path path, final String baseDirName, final ZipOutputStream zout) {
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if(!attrs.isDirectory()) {
						Path relativeFile = path.relativize(file);
						String names = baseDirName + "/" + relativeFile.toString();
						zout.putNextEntry(new ZipEntry(names));
						
						try(InputStream in=Files.newInputStream(file)) {
							FileUtils.copy(in, zout);
						} catch (Exception e) {
							log.error("", e);
						}
						
						zout.closeEntry();
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	/**
	 * Add a file to a zip stream.
	 * @param path
	 * @param file
	 * @param exportStream
	 */
	public static void addFileToZip(String path, File file, ZipOutputStream exportStream) {
		try(InputStream source = new FileInputStream(file)) {
			exportStream.putNextEntry(new ZipEntry(path));
			FileUtils.copy(source, exportStream);
			exportStream.closeEntry();
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	public static void addFileToZip(String path, Path file, ZipOutputStream exportStream) {
		try(InputStream source = Files.newInputStream(file)) {
			exportStream.putNextEntry(new ZipEntry(path));
			FileUtils.copy(source, exportStream);
			exportStream.closeEntry();
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	/**
	 * Add a directory to a zip stream. The files path are relative to the
	 * specified directory. The name of the directory is not part of
	 * the path of its files.
	 * 
	 * @param path The directory to zip
	 * @param exportStream The stream
	 */
	public static void addPathToZip(final Path path, final ZipOutputStream exportStream) {
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if(!attrs.isDirectory()) {
						Path relativeFile = path.relativize(file);
						String names = relativeFile.toString();
						exportStream.putNextEntry(new ZipEntry(names));
						
						try(InputStream in=Files.newInputStream(file)) {
							FileUtils.copy(in, exportStream);
						} catch (Exception e) {
							log.error("", e);
						}
						
						exportStream.closeEntry();
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	/**
	 * Zip all files under a certain root directory. (with compression)
	 * 
	 * @param rootFile
	 * @param targetZipFile
	 * @return true = success, false = exception/error
	 */
	public static boolean zipAll(File rootFile, File targetZipFile) {
		return zipAll(rootFile, targetZipFile, true);
	}
	
	/**
	 * Unzip files from VFSLeaf into VFSContainer and do NOTHING ELSE!!!
	 * See OLAT-6213
	 * 
	 * @param src, VFSLeaf input data
	 * @param target, outout VFSContainer
	 */
	public static boolean xxunzip (VFSLeaf src, VFSContainer dst) {
		if (dst instanceof LocalImpl) {
			try {
				xxunzip (src.getInputStream(), ((LocalImpl)dst).getBasefile().getAbsolutePath());
				return true;				
			} catch (IOException e) {
				String s = ((LocalImpl)src).getBasefile().getAbsolutePath();
				String d = ((LocalImpl)dst).getBasefile().getAbsolutePath();
				log.error("I/O error unzipping "+s+" to "+d);
				return false;
			}
		} 
		return false;
	}
	/**
	 * Unzip files from stream into target dir and do NOTHING ELSE!!!
	 * See OLAT-6213
	 * 
	 * @param is, stream from zip archive
	 * @param outdir, path to output directory, relative to cwd or absolute
	 */
	private static void xxunzip (InputStream is, String outdir) throws IOException {

		byte[] buffer = new byte[FileUtils.BSIZE];

		try(ZipInputStream zis = new ZipInputStream (new BufferedInputStream(is))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {

				File of = new File(outdir, entry.getName());

				if (entry.isDirectory()) {
					of.mkdirs();
					continue;
				} else {
					File xx = new File (of.getParent());
					if (!xx.exists()) {
						Stack<String> todo = new Stack<>();
						do {
							todo.push (xx.getAbsolutePath());
							xx = new File (xx.getParent());
						} while (!xx.exists());
						while(todo.size()>0) {
							xx = new File (todo.pop());
							if (!xx.exists()) {
								xx.mkdirs();
							}
						}
					}
				}

				BufferedOutputStream bos = new BufferedOutputStream (new FileOutputStream(of), buffer.length);
				FileUtils.cpio(new BufferedInputStream(zis), bos, "unzip:"+entry.getName());

				bos.flush();
				bos.close();
			}
		} catch (IllegalArgumentException e) {
			//problem with chars in entry name likely
		}
	}
}

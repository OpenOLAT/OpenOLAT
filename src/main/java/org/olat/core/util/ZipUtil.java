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
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryModule;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.manager.MetaInfoReader;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.io.ShieldInputStream;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSAllItemsFilter;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;

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
	
	private static final Logger log = Tracing.createLoggerFor(ZipUtil.class);
	
	public static String concat(String dirName, String name) {
		if(StringHelper.containsNonWhitespace(dirName)) {
			StringBuilder sb = new StringBuilder(dirName.length() + name.length() + 2);
			sb.append(dirName);
			if(!dirName.endsWith("/")) {
				sb.append("/");
			}
			sb.append(name);
			return sb.toString();
		}
		return name;
	}
	
	public static boolean isReadable(File zipFile) {
		try(InputStream in = new FileInputStream(zipFile);
				ZipInputStream oZip = new ZipInputStream(in)) {
			// unzip files
			ZipEntry oEntr = oZip.getNextEntry();
			while (oEntr != null) {
				oEntr.getName();
				oZip.closeEntry();
				oEntr = oZip.getNextEntry();
			} 
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	public static boolean isReadable(File file, String encoding) {
		boolean ok = false;
		try(ZipFile zFile = new ZipFile(file, Charset.forName(encoding))) {
			zFile.stream().forEach(ZipEntry::toString);
			ok = true;
		} catch (IOException | IllegalArgumentException e) {
			//this is what we check
		}
		return ok;
	}

	/**
	 * Unzip a file to a directory
	 * @param zipFile		The zip file to unzip
	 * @param targetDir	The directory to unzip the file to
	 * @return	True if successfull, false otherwise
	 */
	public static boolean unzip(File zipFile, File targetDir) {
		try(InputStream in=new FileInputStream(zipFile)) {
			xxunzip(in, zipFile.length(), targetDir.getAbsolutePath());
			return true;
		} catch (IOException e) {
			handleIOException("I/O failure while unzipping " + zipFile.getAbsolutePath() + " to " + targetDir.getAbsolutePath(), e);
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
			try(InputStream in=zipLeaf.getInputStream()) {
				xxunzip(in, zipLeaf.getSize(), outdir);
				return true;
			} catch (IOException e) {
				handleIOException("I/O failure while unzipping " + zipLeaf.getName() + " to " + outdir, e);
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
				xxunzip (in, zipFile.length(), outdir);
				return true;
			} catch (IOException e) {
				handleIOException("I/O failure while unzipping " + zipFile.getName() + " to " + outdir, e);
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
	 * @return	True if successful, false otherwise
	 */
	public static boolean unzip(VFSLeaf zipLeaf, VFSContainer targetDir, Identity identity, boolean versioning) {
		boolean unzipped = false;
		try(InputStream in = zipLeaf.getInputStream()) {
			unzipped = unzip(in, zipLeaf.getSize(), targetDir, identity, versioning);
		} catch(Exception e) {
			handleIOException("", e);
		}
		return unzipped;
	}	

	/**
	 * Unzip an inputstream to a directory using the versioning system of VFS
	 * @param zipLeaf	The file to unzip
	 * @param targetDir	The directory to unzip the file to
	 * @param the identity of who unzip the file
	 * @param versioning enabled or not
	 * @return	True if successful, false otherwise
	 */
	private static boolean unzip(InputStream in, long inFileSize, VFSContainer targetDir, Identity identity, boolean versioning) {
		
		VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		
		ZipStatistics stats = new ZipStatistics(inFileSize);

		try(ZipInputStream oZip = new ZipInputStream(in)) {
			// unzip files
			ZipEntry oEntr = oZip.getNextEntry();
			while (oEntr != null) {
				String name = oEntr.getName();
				if(!targetDir.isInPath(name)) {
					throw new IOException("Invalip ZIP");
				}

				if (name != null && !name.startsWith(DIR_NAME__MACOSX)) {
					if (oEntr.isDirectory()) {
						// skip MacOSX specific metadata directory
						// create directories
						getAllSubdirs(targetDir, name, identity, true);
					} else {
						// create file
						VFSContainer createIn = targetDir;
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
								log.debug("Error creating directory structure for zip entry: {}", oEntr.getName());
								return false;
							}
							name = name.substring(dirSepIndex + 1);
						}
						
						if(versioning) {
							VFSLeaf newEntry = (VFSLeaf)createIn.resolve(name);
							if(newEntry == null) {
								newEntry = createIn.createChildLeaf(name);
								if (!copy(oZip, newEntry)) {
									return false;
								}
								stats.uncompressedEntry(newEntry.getSize());
								vfsRepositoryService.itemSaved(newEntry, identity);
							} else if (newEntry.canVersion() == VFSConstants.YES) {
								vfsRepositoryService.addVersion(newEntry, identity, false, "", oZip);
							}
						} else {
							VFSLeaf newEntry = createIn.createChildLeaf(name);
							if (newEntry != null) {
								if (!copy(oZip, newEntry)) {
									return false;
								}
								stats.uncompressedEntry(newEntry.getSize());
								vfsRepositoryService.itemSaved(newEntry, identity);
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
	
	private static boolean copy(ZipInputStream oZip, VFSLeaf newEntry) {
		try(OutputStream out = newEntry.getOutputStream(false)) {
			return FileUtils.copy(oZip, out);
		} catch(Exception e) {
			handleIOException("", e);
			return false;
		}
	}
	
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
		boolean unzipped = false;
		try(InputStream in = zipLeaf.getInputStream()) {
			unzipped = unzipNonStrict(in, zipLeaf.getSize(), targetDir, identity, versioning);
		} catch(IOException e) {
			handleIOException("", e);
		}
		return unzipped;
	}
	
	public static boolean unzipNonStrict(File zipFile, VFSContainer targetDir, Identity identity, boolean versioning) {
		boolean unzipped = false;
		try(InputStream in = new FileInputStream(zipFile);
				InputStream bin = new BufferedInputStream(in, FileUtils.BSIZE)) {
			unzipped = unzipNonStrict(bin, zipFile.length(), targetDir, identity, versioning);
		} catch(IOException e) {
			handleIOException("", e);
		}
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
	private static boolean unzipNonStrict(InputStream in, long zipFileSize, VFSContainer targetDir, Identity identity, boolean versioning) {
		ZipStatistics stats = new ZipStatistics(zipFileSize);
		
		try(net.sf.jazzlib.ZipInputStream oZip = new net.sf.jazzlib.ZipInputStream(in)) {
			VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
			
			// unzip files
			net.sf.jazzlib.ZipEntry oEntr = oZip.getNextEntry();
			
			VFSLeaf lastLeaf = null;
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
								log.debug("Error creating directory structure for zip entry: {}", oEntr.getName());
								return false;
							}
							name = name.substring(dirSepIndex + 1);
						}
						
						if(name != null && name.startsWith("._oo_meta_")) {
							if(lastLeaf != null && name.endsWith(lastLeaf.getName())) {
								unzipMetadata(oZip, lastLeaf);
							}
						} else if(versioning) {
							VFSLeaf newEntry = (VFSLeaf)createIn.resolve(name);
							if(newEntry == null) {
								newEntry = createIn.createChildLeaf(name);
								if (!copyShielded(oZip, newEntry, identity)) {
									return false;
								}
							} else if (newEntry.canVersion() == VFSConstants.YES) {
								vfsRepositoryService.addVersion(newEntry, identity, false, "", oZip);
							}
							stats.uncompressedEntry(newEntry.getSize());
							vfsRepositoryService.itemSaved(newEntry, identity);
							lastLeaf = newEntry;
						} else {
							VFSLeaf newEntry = createIn.createChildLeaf(name);
							if (newEntry != null) {
								if (!copyShielded(oZip, newEntry, identity)) {
									return false;
								}
								stats.uncompressedEntry(newEntry.getSize());
								vfsRepositoryService.itemSaved(newEntry, identity);
								lastLeaf = newEntry;
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
	
	private static boolean copyShielded(net.sf.jazzlib.ZipInputStream oZip, VFSLeaf newEntry, Identity savedBy) {
		try(InputStream in = new ShieldInputStream(oZip)) {
			return VFSManager.copyContent(in, newEntry, savedBy);
		} catch(Exception e) {
			handleIOException("", e);
			return false;
		}
	}
	
	private static boolean copyShielded(VFSLeaf leaf, ZipOutputStream out) {
		try(OutputStream sout = new ShieldOutputStream(out)) {
			return VFSManager.copyContent(leaf, sout);
		} catch(Exception e) {
			handleIOException("", e);
			return false;
		}
	}
	
	private static void unzipMetadata(InputStream oZip, VFSLeaf newEntry) {
		if(newEntry.canMeta() != VFSConstants.YES) {
			return;
		}
		
		VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		VFSMetadata info = vfsRepositoryService.getMetadataFor(newEntry);
		if(info == null) {
			return;
		}

		try(InputStream in = new ShieldInputStream(oZip)) {
			vfsRepositoryService.copyBinaries(info, in);
		} catch(Exception e) {
			handleIOException("", e);
		}
		vfsRepositoryService.updateMetadata(info);
	}

	/**
	 * 
	 * @param zipLeaf
	 * @param targetDir
	 * @param identity
	 * @return
	 */
	public static List<String> checkLockedFileBeforeUnzipNonStrict(VFSLeaf zipLeaf, VFSContainer targetDir, Identity identity) {
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
						if(vfsLockManager.isLockedForMe(newEntry, identity, VFSLockApplicationType.vfs, null)) {
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
		
		VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		
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
					VFSMetadata info = vfsSubpath.getMetaInfo();
					if(info instanceof VFSMetadataImpl) {
						((VFSMetadataImpl)info).setFileInitializedBy(identity);
						vfsRepositoryService.updateMetadata(info);
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
	 * @param filter    Filter to accept files in the ZIP
	 * @param withMetadata Add metadata as shadow file
	 * @return true if successful, false otherwise.
	 */
	public static boolean zip(Set<String> files, File root, File target, VFSItemFilter filter, boolean withMetadata) {
		//	Create a buffer for reading the files
		if (target.exists()) return false;
		List<VFSItem> vfsFiles = new ArrayList<>();
		LocalFolderImpl vfsRoot = new LocalFolderImpl(root);
		for (String fileName: files) {
			VFSItem item = vfsRoot.resolve(fileName);
			if (item == null) {
				return false;
			}
			vfsFiles.add(item);
		}
		return zip(vfsFiles, new LocalFileImpl(target), filter, withMetadata);
	}
	
	/**
	 * Add the set of files residing in root to the ZIP file named target.
	 * Files in subfolders will be compressed too.
	 * 
	 * @param files		Filenames to add to ZIP, relative to root
	 * @param root		Base path.
	 * @param target	Target ZIP file.
	 * @param withMetadata Add metadata as shadow file
	 * @return true if successful, false otherwise.
	 */
	public static boolean zip(Set<String> files, File root, File target, boolean withMetadata) {
		return zip(files, root, target, VFSAllItemsFilter.ACCEPT_ALL, withMetadata);
	}
	
	/**
	 * Zip the content of the VFS container. The name
	 * of the container is NOT the root of the ZIP file.
	 * 
	 * @param container The container to zip
	 * @param outputFile The output ZIP file
	 * @return true if successful
	 */
	public static boolean zip(VFSContainer container, File outputFile, VFSItemFilter filter, boolean withMetadata) {
		try(OutputStream out = new FileOutputStream(outputFile)) {
			zip(container, out, filter, withMetadata);
			return true;
		} catch(IOException e) {
			handleIOException("", e);
			return false;
		}
	}

	/**
	 * Zip the content of the VFS container. The name
	 * of the container is NOT the root of the ZIP file.
	 * 
	 * @param container the container to zip
	 * @param out The output stream
	 * @return true if successful
	 */
	public static boolean zip(VFSContainer container, OutputStream out, VFSItemFilter filter, boolean withMetadata) {
		try(ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(out, FileUtils.BSIZE))) {
			List<VFSItem> items=container.getItems(new VFSSystemItemFilter());
			for(VFSItem item:items) {
				addToZip(item, "", zipOut, filter, withMetadata);
			}
			return true;
		} catch(IOException e) {
			handleIOException("", e);
			return false;
		}
	}
	
	public static boolean zip(List<VFSItem> vfsFiles, VFSLeaf target, VFSItemFilter filter, boolean withMetadata) {
		boolean success = true;
		
		String zname = target.getName();
		if (target instanceof LocalImpl) {
			zname = ((LocalImpl)target).getBasefile().getAbsolutePath();
		}

		try(OutputStream out = target.getOutputStream(false);
				ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(out, FileUtils.BSIZE))) {
			zipOut.setLevel(9);
			for (VFSItem item:vfsFiles) {
				success = addToZip(item, "", zipOut, filter, withMetadata);
			}
			zipOut.flush();
		} catch (IOException e) {
			throw new OLATRuntimeException(ZipUtil.class, "I/O error closing file: " + zname, null);
		}
		
		return success;
	}
	
	public static boolean addToZip(VFSItem vfsItem, String currentPath, ZipOutputStream out, VFSItemFilter filter, boolean withMetadata) {
		boolean success = true;
		try {
			if(filter == null) {
				filter = VFSAllItemsFilter.ACCEPT_ALL;
			}
			
			// The separator / is the separator defined by the ZIP standard
			String itemName = currentPath.length() == 0 ?
					vfsItem.getName() : currentPath + "/" + vfsItem.getName();
			if(filter.accept(vfsItem)) {
				if (vfsItem instanceof VFSContainer ) {
					out.putNextEntry(new ZipEntry(itemName + "/"));
					out.closeEntry();
					
					List<VFSItem> items = ((VFSContainer)vfsItem).getItems();
					for (VFSItem item:items) {
						if (!addToZip(item, itemName, out, filter, withMetadata)) {
							success = false;
							break;
						}
					}
				} else {
					VFSLeaf leaf = (VFSLeaf)vfsItem;
					ZipEntry entry = new ZipEntry(itemName);
					out.putNextEntry(entry);
					copyShielded(leaf, out);
					out.closeEntry();
					
					if(withMetadata && leaf.canMeta() == VFSConstants.YES) {
						byte[] metadata = MetaInfoReader.toBinaries(leaf.getMetaInfo());
						if(metadata != null && metadata.length > 0) {
							ZipEntry metaEntry = new ZipEntry(currentPath + "/._oo_meta_".concat(vfsItem.getName()));
							out.putNextEntry(metaEntry);
							out.write(metadata);
							out.closeEntry();
						}
					}
				}
			}
		} catch (IOException ioe) {
			String name = vfsItem.getName();
			if (vfsItem instanceof LocalImpl) {
				name = ((LocalImpl)vfsItem).getBasefile().getAbsolutePath();
			}
			handleIOException("I/O error while adding " + name + " to zip:", ioe);
			return false;
		}
		return success;
	}


	/**
	 * Zip all files under a certain root directory. (choose to compress or not param compress)
	 * 
	 * @param rootFile
	 * @param targetZipFile
	 * @param withMetadata A metadata as shadow file
	 * @return true = success, false = exception/error
	 */
	public static boolean zipAll(File rootFile, File targetZipFile, boolean withMetadata) {
		Set<String> fileSet = new HashSet<>();
		String[] files = rootFile.list();
		for (int i = 0; i < files.length; i++) {
			fileSet.add(files[i]);
		}		
		return zip(fileSet, rootFile, targetZipFile, VFSAllItemsFilter.ACCEPT_ALL, withMetadata);
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
							handleIOException("", e);
						}
						
						zout.closeEntry();
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			handleIOException("", e);
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
			handleIOException("", e);
		}
	}
	
	public static void addFileToZip(String path, Path file, ZipOutputStream exportStream) {
		try(InputStream source = Files.newInputStream(file)) {
			exportStream.putNextEntry(new ZipEntry(path));
			FileUtils.copy(source, exportStream);
			exportStream.closeEntry();
		} catch(IOException e) {
			handleIOException("", e);
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
							handleIOException("", e);
						}
						
						exportStream.closeEntry();
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			handleIOException("", e);
		}
	}
	
	/**
	 * Add a directory to a zip stream. The files path are relative to the
	 * specified directory. The name of the directory is not part of
	 * the path of its files.
	 * 
	 * @param path The path
	 * @param directory The directory to zip
	 * @param exportStream The stream
	 */
	public static void addPathToZip(final String path, final Path directory, final ZipOutputStream exportStream) {
		try {
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if(!attrs.isDirectory()) {
						Path relativeFile = directory.relativize(file);
						String name = relativeFile.toString();
						if(StringHelper.containsNonWhitespace(path)) {
							name = path + "/" + name;
						}
						exportStream.putNextEntry(new ZipEntry(name));
						
						try(InputStream in=Files.newInputStream(file)) {
							FileUtils.cpio(in, exportStream, "");
						} catch (Exception e) {
							handleIOException("", e);
						}
						
						exportStream.closeEntry();
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			handleIOException("", e);
		}
	}
	
	/**
	 * Unzip files from stream into target dir and do NOTHING ELSE!!!
	 * See OLAT-6213
	 * 
	 * @param is, stream from zip archive
	 * @param outdir, path to output directory, relative to cwd or absolute
	 */
	private static void xxunzip(InputStream is, long fileSize, String outdir) throws IOException {
		final Path outPath = Paths.get(outdir);
		
		ZipStatistics stats = new ZipStatistics(fileSize);
		
		try(ZipInputStream zis = new ZipInputStream (new BufferedInputStream(is))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				Path filePath = Paths.get(outdir, entry.getName());
				Path normalizedPath = filePath.normalize();
				if(!normalizedPath.startsWith(outPath)) {
					throw new IOException("Invalid ZIP");
				}
				
				File of = new File(outdir, entry.getName());
				if (entry.isDirectory()) {
					of.mkdirs();
				} else {
					File parent = of.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					long uncompressedSize = xxunzipcpio(zis, of);
					stats.uncompressedEntry(uncompressedSize);
				}
			}
		} catch (IllegalArgumentException e) {
			//problem with chars in entry name likely
		}
	}
	
	public static class ZipStatistics {
		
		private static final long GRACE_ENTRY_SIZE = 1024*1024L;
		
		private final long fileCompressedSize;
		private long uncompressedSize = 0l;
		private long numOfEntries = 0l;
		private final double minInflateRatio;
		private final int maxEntries;
		
		public ZipStatistics(long fileCompressedSize) {
			this.fileCompressedSize = fileCompressedSize;
			
			VFSRepositoryModule vfsRepositoryModule = CoreSpringFactory.getImpl(VFSRepositoryModule.class);
			minInflateRatio = vfsRepositoryModule.getZipMinInflateRatio();
			maxEntries = vfsRepositoryModule.getZipMaxEntries();
		}
		
		public void uncompressedEntry(long uncompressedData) throws IOException {
			numOfEntries++;
			if(numOfEntries > maxEntries) {
				throw new IOException("Suspected of ZIP-bomb. Max num. of entries: " + maxEntries);
			}

			uncompressedSize += uncompressedData;
			if(uncompressedSize < GRACE_ENTRY_SIZE) {
				return;
			}
			
			double ratio = fileCompressedSize / (double)uncompressedSize;
			if (ratio >= minInflateRatio) {
				return;
			}
			throw new IOException("Suspected of ZIP-bomb");
		}
	}
	
	private static long xxunzipcpio(ZipInputStream zis, File of) {
		try(BufferedOutputStream bos = new BufferedOutputStream (new FileOutputStream(of), FileUtils.BSIZE)) {
			long size = FileUtils.cpio(new BufferedInputStream(zis), bos, "unzip:" + of.getName());
			bos.flush();
			return size;
		} catch(IOException e) {
			handleIOException("", e);
			return -1l;
		}
	}
	
	private static final void handleIOException(String msg, Exception e) {
		try {
			String className = e.getClass().getSimpleName();
			if("ClientAbortException".equals(className)) {
				log.debug("client browser probably abort during operaation", e);
			} else {
				log.error(msg, e);
			}
		} catch (Exception e1) {
			log.error("", e1);
		}
	}
}

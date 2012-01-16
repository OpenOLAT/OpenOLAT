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
package org.olat.core.util.vfs.version;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.configuration.Initializable;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.OlatRelPathImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSItemSuffixFilter;
import org.olat.core.util.vfs.filters.VFSLeafFilter;
import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Description:<br>
 * This implementation of the VersionsManager saved the revisions of a file in a
 * file with the same name as the original + ".xml". This xml file is saved in
 * a parallel folder .version under the root defined in FolderConfig. Every revision'file
 * have a name made of a generated unique id + the name of the original file.
 * 
 * <P>
 * Initial Date:  21 sept. 2009 <br>
 *
 * @author srosse
 */
public class VersionsFileManager extends VersionsManager implements Initializable {
	private static final OLog log = Tracing.createLoggerFor(VersionsFileManager.class);

	private static final Versions NOT_VERSIONED = new NotVersioned();
	private static XStream mystream;
	

	private File rootFolder;
	private File rootVersionFolder;
	private VFSContainer rootVersionsContainer;

	/**
	 * [spring]
	 */
	private VersionsFileManager() {
		INSTANCE = this;
	}

	@Override
	public Versions createVersionsFor(VFSLeaf leaf) {
		return createVersionsFor(leaf, false);
	}
	
	@Override
	public Versions createVersionsFor(VFSLeaf leaf, boolean force) {
		if (!(leaf instanceof Versionable)) {
			return NOT_VERSIONED;
		} else if (isVersionFile(leaf)) {
			return NOT_VERSIONED;
		}

		Versions versions = readVersions(leaf, false);
		return versions;
	}

	@Override
	public List<Versions> getDeletedFiles(VFSContainer container) {
		List<Versions> deletedRevisions = new ArrayList<Versions>();

		VFSContainer versionContainer = getCanonicalVersionFolder(container, false);
		if (versionContainer != null) {
			Set<String> currentNames = new HashSet<String>();
			for (VFSItem item : container.getItems(new VFSLeafFilter())) {
				currentNames.add(item.getName() + ".xml");
			}

			List<VFSItem> versionItems = versionContainer.getItems(new VFSItemSuffixFilter(new String[] { "xml" }));
			for (VFSItem versionItem : versionItems) {
				if (versionItem instanceof VFSLeaf && !currentNames.contains(versionItem.getName())) {
					Versions versions = readVersions(null, (VFSLeaf) versionItem);
					List<VFSRevision> revisions = versions.getRevisions();
					if (!revisions.isEmpty()) {
						deletedRevisions.add(versions);
					}
				}
			}
		}
		return deletedRevisions;
	}

	private Versions readVersions(VFSLeaf leaf, boolean create) {
		VFSLeaf fVersions = getCanonicalVersionXmlFile(leaf, create);
		if (!create && fVersions == null) {
			VersionsFileImpl versions = new VersionsFileImpl();
			versions.setCurrentVersion((Versionable) leaf);
			versions.setVersioned(isVersioned(leaf));
			versions.setRevisionNr(getNextRevisionNr(versions));
			return versions;
		}
		return readVersions(leaf, fVersions);
	}

	private Versions readVersions(VFSLeaf leaf, VFSLeaf fVersions) {
		if (fVersions == null) { return new NotVersioned(); }

		VFSContainer fVersionContainer = fVersions.getParentContainer();
		VersionsFileImpl versions = (VersionsFileImpl) XStreamHelper.readObject(mystream, fVersions);
		versions.setVersionFile(fVersions);
		versions.setCurrentVersion((Versionable) leaf);
		if (versions.getRevisionNr() == null || versions.getRevisionNr().length() == 0) {
			versions.setRevisionNr(getNextRevisionNr(versions));
		}

		for (VFSRevision revision : versions.getRevisions()) {
			RevisionFileImpl revisionImpl = (RevisionFileImpl) revision;
			revisionImpl.setContainer(fVersionContainer);
		}
		return versions;
	}

	@Override
	public boolean addVersion(Versionable currentVersion, Identity identity, String comment, InputStream newFile) {
		VFSLeaf currentFile = (VFSLeaf) currentVersion;
		if (addToRevisions(currentVersion, identity, comment)) {
			// copy the content of the new file to the old
			boolean closeInputStream = !(newFile instanceof net.sf.jazzlib.ZipInputStream || newFile instanceof java.util.zip.ZipInputStream);
			if (VFSManager.copyContent(newFile, currentFile, closeInputStream)) { return true; }
		} else {
			log.error("Cannot create a version of this file: " + currentVersion);
		}
		return false;
	}

	@Override
	public boolean move(VFSLeaf currentFile, VFSLeaf targetFile, Identity author) {
		VFSLeaf fCurrentVersions = getCanonicalVersionXmlFile(currentFile, true);
		Versions currentVersions = readVersions(currentFile, fCurrentVersions);
		
		boolean brandNewVersionFile = false;
		VFSLeaf fTargetVersions = getCanonicalVersionXmlFile(targetFile, false);
		if(fTargetVersions == null) {
			brandNewVersionFile = true;
			fTargetVersions = getCanonicalVersionXmlFile(targetFile, true);
		}
 
		Versions targetVersions = readVersions(targetFile, fTargetVersions);
		if(!(currentVersions instanceof VersionsFileImpl) || !(targetVersions instanceof  VersionsFileImpl)) {
			return false;
		}
		
		VersionsFileImpl targetVersionsImpl = (VersionsFileImpl)targetVersions;
		if(author != null) {
			targetVersionsImpl.setAuthor(author.getName());
		}
		if(brandNewVersionFile) {
			targetVersionsImpl.setCreator(currentVersions.getCreator());
			targetVersionsImpl.setComment(currentVersions.getComment());
		}

		boolean allOk = true;
		for(VFSRevision revision:currentVersions.getRevisions()) {
			allOk &= copyRevision(revision, fTargetVersions, targetVersionsImpl);
		}

		targetVersionsImpl.setRevisionNr(getNextRevisionNr(targetVersionsImpl));
		XStreamHelper.writeObject(mystream, fTargetVersions, targetVersionsImpl);

		return allOk;
	}
	
	private boolean copyRevision(VFSRevision revision, VFSLeaf fNewVersions, VersionsFileImpl targetVersions) {
		if(!(revision instanceof RevisionFileImpl)) {
			logWarn("Copy only copy persisted revisions", null);
		}
		
		RevisionFileImpl revisionImpl = (RevisionFileImpl)revision;
		String revUuid = revisionImpl.getUuid();
		for(VFSRevision rev:targetVersions.getRevisions()) {
			if(rev instanceof RevisionFileImpl) {
				RevisionFileImpl fRev = (RevisionFileImpl)rev;
				if(StringHelper.containsNonWhitespace(fRev.getUuid()) && fRev.getUuid().equals(revUuid)) {
					return true;
				}
			}
		}

		String uuid = UUID.randomUUID().toString().replace("-", "") + "_" + revision.getName();
		
		RevisionFileImpl newRevision = new RevisionFileImpl();
		newRevision.setName(revision.getName());
		newRevision.setFilename(uuid);
		newRevision.setRevisionNr(getNextRevisionNr(targetVersions));
		newRevision.setComment(revision.getComment());
		newRevision.setAuthor(revision.getAuthor());
		newRevision.setLastModified(revision.getLastModified());
		newRevision.setUuid(revUuid);

		//copy -> the files revision
		InputStream revisionIn = revision.getInputStream();

		VFSLeaf target = fNewVersions.getParentContainer().createChildLeaf(uuid);
		if (VFSManager.copyContent(revisionIn, target)) {
			targetVersions.setComment(revision.getComment());
			targetVersions.getRevisions().add(newRevision);
			targetVersions.setRevisionNr(getNextRevisionNr(targetVersions));
			targetVersions.setAuthor(revision.getAuthor());
			return true;
		}
		return false;
	}

	@Override
	public boolean move(Versionable currentVersion, VFSContainer container) {
		VFSLeaf currentFile = (VFSLeaf) currentVersion;
		VFSLeaf fVersions = getCanonicalVersionXmlFile(currentFile, true);
		Versions versions = readVersions(currentFile, fVersions);

		VFSContainer versionContainer = getCanonicalVersionFolder(container, true);

		boolean allOk = VFSConstants.YES.equals(versionContainer.copyFrom(fVersions));
		for (VFSRevision revision : versions.getRevisions()) {
			RevisionFileImpl revisionImpl = (RevisionFileImpl) revision;
			VFSLeaf revisionFile = revisionImpl.getFile();
			if (revisionFile != null) {
				allOk &= VFSConstants.YES.equals(versionContainer.copyFrom(revisionFile));
			}
		}

		allOk &= VFSConstants.YES.equals(fVersions.delete());
		for (VFSRevision revision : versions.getRevisions()) {
			VFSLeaf revisionFile = ((RevisionFileImpl) revision).getFile();
			if (revisionFile != null) {
				allOk &= VFSConstants.YES.equals(revisionFile.delete());
			}
		}
		return allOk;
	}

	@Override
	public boolean restore(Versionable currentVersion, VFSRevision version, String comment) {
		VFSLeaf currentFile = (VFSLeaf) currentVersion;
		if(!VFSManager.exists(currentFile)) {
			return false;
		}
		
		// add current version to versions file
		if (addToRevisions(currentVersion, null, comment)) {
			// copy the content of the new file to the old
			if (VFSManager.copyContent(version.getInputStream(), currentFile)) { return true; }
		} else {
			log.error("Cannot create a version of this file: " + currentVersion);
		}

		return false;
	}

	@Override
	public boolean restore(VFSContainer container, VFSRevision revision) {
		String filename = revision.getName();
		VFSItem restoredItem = container.resolve(filename);
		if (restoredItem == null) {
			restoredItem = container.createChildLeaf(filename);
		}
		if (restoredItem instanceof VFSLeaf) {
			VFSLeaf restoredLeaf = (VFSLeaf) restoredItem;
			InputStream inStream = revision.getInputStream();
			if (VFSManager.copyContent(inStream, restoredLeaf)) {
				VFSLeaf versionFile = getCanonicalVersionXmlFile(restoredLeaf, true);
				Versions versions = readVersions(restoredLeaf, versionFile);
				if (versions instanceof VersionsFileImpl) {
					versions.getRevisions().remove(revision);
					((VersionsFileImpl) versions).setRevisionNr(getNextRevisionNr(versions));
				}
				XStreamHelper.writeObject(mystream, versionFile, versions);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean deleteRevisions(Versionable currentVersion, List<VFSRevision> versionsToDelete) {
		VFSLeaf currentFile = (VFSLeaf) currentVersion;
		Versions versions = readVersions(currentFile, true);
		List<VFSRevision> allVersions = versions.getRevisions();

		for (VFSRevision versionToDelete : versionsToDelete) {
			RevisionFileImpl versionImpl = (RevisionFileImpl) versionToDelete;
			for (Iterator<VFSRevision> allVersionIt = allVersions.iterator(); allVersionIt.hasNext();) {
				RevisionFileImpl allVersionImpl = (RevisionFileImpl) allVersionIt.next();
				if (allVersionImpl.getFilename() != null && allVersionImpl.getFilename().equals(versionImpl.getFilename())) {
					allVersionIt.remove();
					break;
				}
			}

			VFSLeaf fileToDelete = versionImpl.getFile();
			if (fileToDelete != null) {
				fileToDelete.delete();
			}
		}

		VFSLeaf versionFile = getCanonicalVersionXmlFile(currentFile, true);
		XStreamHelper.writeObject(mystream, versionFile, versions);
		if (currentVersion.getVersions() instanceof VersionsFileImpl) {
			((VersionsFileImpl) currentVersion.getVersions()).update(versions);
		}
		return true;
	}

	@Override
	public boolean deleteVersions(List<Versions> versions) {
		for(Versions versionToDelete:versions) {
			if(versionToDelete instanceof VersionsFileImpl) {
				VersionsFileImpl versionsImpl = (VersionsFileImpl)versionToDelete;
				VFSLeaf versionFile = versionsImpl.getVersionFile();
				if(versionFile != null) {
					//robust against manual file system manipulation
					versionFile.delete();
				}
				for (VFSRevision revisionToDelete : versionsImpl.getRevisions()) {
					RevisionFileImpl versionImpl = (RevisionFileImpl)revisionToDelete;
					VFSLeaf fileToDelete = versionImpl.getFile();
					if (fileToDelete != null) {
						fileToDelete.delete();
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean delete(VFSItem item, boolean force) {
		if (item instanceof VFSContainer) {
			if (force) {
				VFSContainer container = (VFSContainer)item;
				VFSContainer versionContainer = getCanonicalVersionFolder(container, false);
				if (versionContainer == null) { return true; }
				return VFSConstants.YES.equals(versionContainer.delete());
			}
			return true;
		} else if (item instanceof VFSLeaf && item instanceof Versionable) {
			VFSLeaf leaf = (VFSLeaf)item;
			if (force || isTemporaryFile(leaf)) {
				cleanUp(leaf);
			} else {
				addToRevisions((Versionable)leaf, null, null);
			}
		}
		return false;
	}
	
	/**
	 * Some temporary/lock files of specific editors need to be force deleted
	 * with all versions. Word can reuse older names.
	 * @param leaf
	 * @return
	 */
	private boolean isTemporaryFile(VFSLeaf leaf) {
		String name = leaf.getName();
		//temporary files
		if(name.endsWith(".tmp")) {
			//Word 2010: ~WRD0002.tmp
			if(name.startsWith("~WRD") || name.startsWith("~WRL")) {
				return true;
			}
			//PowerPoint 2010: ppt5101.tmp 
			if(name.startsWith("ppt")) {
				return true;
			}
		}
		//lock files of Word 2010, Excel 2010, PowerPoint 2010:
		if(name.startsWith("~$") && (name.endsWith(".docx") || name.endsWith(".xlsx") || name.endsWith(".pptx"))) {
			return true;
		}
		
		//OpenOffice locks: .~lock.Versions_21.odt#
		if(name.startsWith(".~lock.") && (name.endsWith(".odt#") /* Writer */ || name.endsWith(".ods#") /* Calc */
				|| name.endsWith(".odp#") /* Impress */ || name.endsWith("odf#") /* Math */
				|| name.endsWith(".odg#") /* Draw */)) {
			return true;
		}
		//OpenOffice database lock
		if(name.endsWith(".odb.lck")) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Clean up all revisions files, xml file
	 * @param leaf
	 */
	private void cleanUp(VFSLeaf leaf) {
		String relPath = getRelPath(leaf);
		if (relPath == null) return; // cannot handle

		File fVersion = new File(getRootVersionsFile(), relPath + ".xml");
		File fParentVersion = fVersion.getParentFile();
		if (!fParentVersion.exists()) return; //already deleted

		VFSLeaf versionLeaf = null;
		if (fVersion.exists()) {
			LocalFolderImpl localVersionContainer = new LocalFolderImpl(fParentVersion);
			versionLeaf = (VFSLeaf) localVersionContainer.resolve(fVersion.getName());
		}
		
		if (versionLeaf == null) return; //already deleted
		Versions versions = readVersions(leaf, versionLeaf);
		for (VFSRevision versionToDelete : versions.getRevisions()) {
			RevisionFileImpl versionImpl = (RevisionFileImpl) versionToDelete;
			VFSLeaf fileToDelete = versionImpl.getFile();
			if (fileToDelete != null) {
				fileToDelete.delete();
			}
		}
		versionLeaf.delete();
	}

	@Override
	public boolean rename(VFSItem item, String newname) {
		if (item instanceof VFSLeaf) {
			VFSLeaf currentFile = (VFSLeaf) item;
			VFSLeaf versionFile = getCanonicalVersionXmlFile(currentFile, true);
			// infinite loop if rename is own versions file
			return VFSConstants.YES.equals(versionFile.rename(newname + ".xml"));
		} else if (item instanceof VFSContainer) {
			VFSContainer container = (VFSContainer) item;
			VFSContainer versionContainer = getCanonicalVersionFolder(container, false);
			if (versionContainer == null) { return true; }
			return VFSConstants.YES.equals(versionContainer.rename(newname));
		}
		return false;
	}

	/**
	 * @see org.olat.core.util.vfs.version.VersionsManager#addToRevisions(org.olat.core.util.vfs.version.Versionable, org.olat.core.id.Identity, java.lang.String)
	 */
	@Override
	public boolean addToRevisions(Versionable currentVersion, Identity identity, String comment) {
		VFSLeaf currentFile = (VFSLeaf) currentVersion;
		
		VFSLeaf versionFile = getCanonicalVersionXmlFile(currentFile, true);
		if(versionFile == null) {
			return false;//cannot do something with the current file
		}
		
		VFSContainer versionContainer = versionFile.getParentContainer();

		String name = currentFile.getName();

		// read from the
		Versions v = readVersions(currentFile, versionFile);
		if (!(v instanceof VersionsFileImpl)) {
			log.error("Wrong implementation of Versions: " + v);
			return false;
		}
		VersionsFileImpl versions = (VersionsFileImpl) v;

		String uuid = UUID.randomUUID().toString() + "_" + name;
		String versionNr = getNextRevisionNr(versions);
		String currentAuthor = versions.getAuthor();
		long lastModifiedDate = 0;
		if (currentFile instanceof MetaTagged) {
			MetaInfo metaInfo = ((MetaTagged) currentFile).getMetaInfo();
			if(metaInfo != null) {
				metaInfo.clearThumbnails();
				if(currentAuthor == null) { 
					currentAuthor = metaInfo.getAuthor();
				}
				lastModifiedDate = metaInfo.getLastModified();
			}
		}
		
		if(lastModifiedDate <= 0) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			lastModifiedDate = cal.getTimeInMillis();
		}

		RevisionFileImpl newRevision = new RevisionFileImpl();
		newRevision.setUuid(UUID.randomUUID().toString());
		newRevision.setName(name);
		newRevision.setFilename(uuid);
		newRevision.setRevisionNr(versionNr);
		newRevision.setComment(versions.getComment());
		newRevision.setAuthor(currentAuthor);
		newRevision.setLastModified(lastModifiedDate);

		if (versions.getRevisions().isEmpty() && currentVersion instanceof MetaTagged) {
			MetaTagged metaTagged = (MetaTagged) currentVersion;
			versions.setCreator(metaTagged.getMetaInfo().getAuthor());
		}

		VFSLeaf target = versionContainer.createChildLeaf(uuid);
		if (VFSManager.copyContent(currentFile, target)) {
			if (identity != null) {
				versions.setAuthor(identity.getName());
			}
			versions.setComment(comment);
			versions.getRevisions().add(newRevision);
			versions.setRevisionNr(getNextRevisionNr(versions));
			XStreamHelper.writeObject(mystream, versionFile, versions);
			if (currentVersion.getVersions() instanceof VersionsFileImpl) {
				((VersionsFileImpl) currentVersion.getVersions()).update(versions);
			}
			return true;
		} else {
			log.error("Cannot create a version of this file: " + currentVersion);
		}
		return false;
	}

	public String getNextRevisionNr(Versions versions) {
		int maxNumber = 0;
		for (VFSRevision version : versions.getRevisions()) {
			String versionNr = version.getRevisionNr();
			if (versionNr != null && versionNr.length() > 0) {
				try {
					int number = Integer.parseInt(versionNr);
					maxNumber = Math.max(maxNumber, number);
				} catch (Exception ex) {
					// if not a number, don't interest us
				}
			}
		}
		return Integer.toString(maxNumber + 1);
	}

	/**
	 * Get the canonical path to the file's meta file.
	 * 
	 * @param bcPath
	 * @return String
	 */
	private VFSLeaf getCanonicalVersionXmlFile(VFSItem item, boolean create) {
		File f = getOriginFile(item);
		if (!f.exists()) { return null; }

		String relPath = getRelPath(item);
		if (relPath == null) {
			// cannot handle
			return null;
		}

		File fVersion = new File(getRootVersionsFile(), relPath + ".xml");
		File fParentVersion = fVersion.getParentFile();
		if (!fParentVersion.exists() && create) {
			fParentVersion.mkdirs();
		}

		if (fVersion.exists()) {
			LocalFolderImpl localVersionContainer = new LocalFolderImpl(fParentVersion);
			return (VFSLeaf) localVersionContainer.resolve(fVersion.getName());
		} else if (create) {
			LocalFolderImpl localVersionContainer = new LocalFolderImpl(fParentVersion);
			VersionsFileImpl versions = new VersionsFileImpl();
			versions.setVersioned(isVersioned(item));
			versions.setRevisionNr(getNextRevisionNr(versions));
			VFSLeaf fVersions = localVersionContainer.createChildLeaf(fVersion.getName());
			XStreamHelper.writeObject(mystream, fVersions, versions);
			return fVersions;
		}
		return null;
	}

	private VFSContainer getCanonicalVersionFolder(VFSContainer container, boolean create) {
		String relPath = getRelPath(container);
		File fVersion = new File(getRootVersionsFile(), relPath);
		if (fVersion.exists()) { return new LocalFolderImpl(fVersion); }
		if (create) {
			fVersion.mkdirs();
			return new LocalFolderImpl(fVersion);
		}
		return null;
	}

	private String getRelPath(VFSItem item) {
		String relPath = null;
		if (item instanceof NamedContainerImpl) {
			item = ((NamedContainerImpl)item).getDelegate();
		}
		if (item instanceof MergeSource) {
			item = ((MergeSource)item).getRootWriteContainer();
		}
		if (item instanceof OlatRelPathImpl) {
			relPath = ((OlatRelPathImpl) item).getRelPath();
		} else if (item instanceof LocalImpl) {
			LocalImpl impl = (LocalImpl) item;
			String absolutPath = impl.getBasefile().getAbsolutePath();
			if (absolutPath.startsWith(getCanonicalRoot())) {
				relPath = absolutPath.substring(getCanonicalRoot().length());
			}
		}
		return relPath;
	}

	private boolean isVersionFile(VFSItem item) {
		File f = getOriginFile(item);
		if (f == null) return false;

		try {
			String path = f.getCanonicalPath();
			String vPath = getRootVersionsFile().getCanonicalPath();
			return path.startsWith(vPath);
		} catch (IOException e) {
			log.error("Cannot check if this file is a version file: " + item, e);
			return false;
		}
	}

	private boolean isVersioned(VFSItem item) {
		if (item == null) return false;
		VFSContainer parent = item.getParentContainer();
		return FolderConfig.versionsEnabled(parent);
	}

	private File getOriginFile(VFSItem item) {
		if (item instanceof LocalImpl) {
			LocalImpl localImpl = (LocalImpl) item;
			return localImpl.getBasefile();
		}
		if (item instanceof OlatRelPathImpl) {
			OlatRelPathImpl relPath = (OlatRelPathImpl) item;
			return new File(getCanonicalRoot(), relPath.getRelPath());
		}
		return null;
	}

	public String getCanonicalRoot() {
		if(rootFolder == null) {
			rootFolder = new File(FolderConfig.getCanonicalRoot());
		}
		return rootFolder.getAbsolutePath();
	}
	
	public File getRootVersionsFile() {
		if (rootVersionsContainer == null) {
			rootVersionFolder = new File(FolderConfig.getCanonicalVersionRoot());
			if(!rootVersionFolder.exists()) {
				rootVersionFolder.mkdirs();
			}
			rootVersionsContainer = new LocalFolderImpl(rootVersionFolder);
		}
		return rootVersionFolder;
	}

	public VFSContainer getRootVersionsContainer() {
		if (rootVersionsContainer == null) {
			rootVersionFolder = new File(FolderConfig.getCanonicalVersionRoot());
			if(!rootVersionFolder.exists()) {
				rootVersionFolder.mkdirs();
			}
			rootVersionsContainer = new LocalFolderImpl(rootVersionFolder);
		}
		return rootVersionsContainer;
	}
	
	@Override
	//fxdiff FXOLAT-127: file versions maintenance tool
	public boolean delete(OrphanVersion orphan) {
		VFSLeaf versionLeaf = orphan.getVersionsLeaf();

		if (versionLeaf == null) return true; //already deleted
		Versions versions = orphan.getVersions();
		for (VFSRevision versionToDelete : versions.getRevisions()) {
			RevisionFileImpl versionImpl = (RevisionFileImpl) versionToDelete;
			versionImpl.setContainer(orphan.getVersionsLeaf().getParentContainer());
			VFSLeaf fileToDelete = versionImpl.getFile();
			if (fileToDelete != null) {
				fileToDelete.delete();
			}
		}
		versionLeaf.delete();
		return true;
	}

	@Override
	//fxdiff FXOLAT-127: file versions maintenance tool
	public List<OrphanVersion> orphans() {
		List<OrphanVersion> orphans = new ArrayList<OrphanVersion>();
		VFSContainer versionsContainer = getRootVersionsContainer();
		crawlForOrphans(versionsContainer, orphans);
		return orphans;
	}
	//fxdiff FXOLAT-127: file versions maintenance tool
	private void crawlForOrphans(VFSContainer container, List<OrphanVersion> orphans) {
		List<VFSItem> children = container.getItems();
		for(VFSItem child:children) {
			if(child instanceof VFSContainer) {
				crawlForOrphans((VFSContainer)child, orphans);
			}
			if(child instanceof VFSLeaf) {
				VFSLeaf versionsLeaf = (VFSLeaf)child;
				if(child.getName().endsWith(".xml")) {
					Versions versions = isOrphan(versionsLeaf);
					if(versions == null) {
						continue;
					} else {
						List<VFSRevision> revisions = versions.getRevisions();
						if(revisions != null) {
							for(VFSRevision revision:revisions) {
								if(revision instanceof RevisionFileImpl) {
									((RevisionFileImpl)revision).setContainer(container);
								}
							}
						}
					}
					File originalFile = reversedOriginFile(child);
					if(!originalFile.exists()) {
						VFSLeaf orphan = new LocalFileImpl(originalFile);
						orphans.add(new OrphanVersion(orphan, versionsLeaf, versions));
					}
				}
			}
		}
	}
	//fxdiff FXOLAT-127: file versions maintenance tool
	private Versions isOrphan(VFSLeaf potentialOrphan) {
		try {
			VersionsFileImpl versions = (VersionsFileImpl) XStreamHelper.readObject(mystream, potentialOrphan);
			return versions;
		} catch (Exception e) {
			return null;
		}
	}
	//fxdiff FXOLAT-127: file versions maintenance tool
	private File reversedOriginFile(VFSItem versionXml) {
		String path = File.separatorChar + versionXml.getName().substring(0, versionXml.getName().length() - 4);
		for(VFSContainer parent=versionXml.getParentContainer(); parent != null && !parent.isSame(getRootVersionsContainer()); parent = parent.getParentContainer()) {
			path = File.separatorChar + parent.getName() + path;
		}
		
		return new File(getCanonicalRoot(), path);
	}

	/**
	 * 
	 * @see org.olat.core.configuration.Initializable#init()
	 */
	public void init() {
			mystream = XStreamHelper.createXStreamInstance();
			mystream.alias("versions", VersionsFileImpl.class);
			mystream.alias("revision", RevisionFileImpl.class);
			mystream.omitField(VersionsFileImpl.class, "currentVersion");
			mystream.omitField(VersionsFileImpl.class, "versionFile");
			mystream.omitField(RevisionFileImpl.class, "current");
			mystream.omitField(RevisionFileImpl.class, "container");
			mystream.omitField(RevisionFileImpl.class, "file");
	}
}

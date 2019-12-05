package org.olat.admin.sysinfo.model;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.id.Identity;
import org.olat.core.util.WebappHelper;

public class FileStatsTableContentRow {
	
	private final Long key;
	private final String name;
	private final Long size;
	private final String path;
	private final Identity author;
	private final boolean revision;
	private final String fileType;
	private final String fileCategory;
	private final LicenseType license;
	private final Date createdAt;
	private final Date lastModifiedAt;
	private final boolean trashed;
	private final String uuid;
	private final int downloadCount;
	private final String title;
	private final String comment;
	private final String publisher;
	private final String source;
	private final String creator;
	private final Date pubDate;
	private final String language;
	private final boolean locked;
	private final Date lockedAt;
	private final Identity lockedBy;
	private final long revisionNr;
	private final String revisionComment;
		
	public FileStatsTableContentRow(VFSMetadata metadata) {
		key = metadata.getKey();
		name = metadata.getFilename();
		size = metadata.getFileSize();
		path = metadata.getRelativePath();
		author = metadata.getAuthor();
		fileType = WebappHelper.getMimeType(metadata.getFilename()).split("/")[1];
		fileCategory = WebappHelper.getMimeType(metadata.getFilename()).split("/")[0];
		revision = false;
		createdAt = metadata.getCreationDate();
		lastModifiedAt = metadata.getLastModified();
		license = metadata.getLicenseType();
		trashed = metadata.isDeleted();
		uuid = metadata.getUuid();
		downloadCount = metadata.getDownloadCount();
		title = metadata.getTitle();
		comment = metadata.getComment();
		publisher = metadata.getPublisher();
		source = metadata.getSource();
		creator = metadata.getCreator();
		pubDate = calculateDateFromPublicationDateArray(metadata.getPublicationDate());
		language = metadata.getLanguage();
		locked = metadata.isLocked();
		lockedAt = metadata.getLockedDate();
		lockedBy = metadata.getLockedBy();
		revisionNr = metadata.getRevisionNr();
		revisionComment = metadata.getRevisionComment();
	}
	
	public FileStatsTableContentRow(VFSRevision rev) {
		key = rev.getMetadata().getKey();
		name = rev.getFilename();
		size = rev.getSize();
		path = rev.getMetadata().getRelativePath();
		author = rev.getAuthor();
		revision = true;
		fileType = WebappHelper.getMimeType(rev.getFilename()).split("/")[1];
		fileCategory = WebappHelper.getMimeType(rev.getFilename()).split("/")[0];
		createdAt = rev.getCreationDate();
		lastModifiedAt = rev.getFileLastModified();
		license = rev.getMetadata().getLicenseType();
		trashed = rev.getMetadata().isDeleted();
		uuid = rev.getMetadata().getUuid();
		downloadCount = rev.getMetadata().getDownloadCount();
		title = rev.getMetadata().getTitle();
		comment = rev.getMetadata().getComment();
		publisher = rev.getMetadata().getPublisher();
		source = rev.getMetadata().getSource();
		creator = rev.getMetadata().getCreator();
		pubDate = calculateDateFromPublicationDateArray(rev.getMetadata().getPublicationDate());
		language = rev.getMetadata().getLanguage();
		locked = rev.getMetadata().isLocked();
		lockedAt = rev.getMetadata().getLockedDate();
		lockedBy = rev.getMetadata().getLockedBy();
		revisionNr = rev.getRevisionNr();
		revisionComment = rev.getRevisionComment();
	}
	
	private Date calculateDateFromPublicationDateArray(String[] pubDateArray) {
		if(pubDateArray == null || pubDateArray.length == 0) return null;
		try {
			Calendar cal = Calendar.getInstance();
			cal.clear();
			if(pubDateArray.length > 0 && pubDateArray[0] != null) {
				cal.set(Calendar.YEAR, Integer.parseInt(pubDateArray[0]));
			}
			if(pubDateArray.length > 1 && pubDateArray[1] != null) {
				cal.set(Calendar.MONTH, Integer.parseInt(pubDateArray[1]));
			}
			if(pubDateArray.length > 2 && pubDateArray[2] != null) {
				cal.set(Calendar.DATE, Integer.parseInt(pubDateArray[2]));
			}
			return cal.getTime();
		} catch (NumberFormatException e) {
			// can happen
			return null;
		}
	}
	
	public Long getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public Long getSize() {
		return size;
	}

	public String getPath() {
		return path;
	}

	public Identity getAuthor() {
		return author;
	}

	public Boolean isRevision() {
		return revision;
	}

	public String getFileType() {
		return fileType;
	}

	public LicenseType getLicense() {
		return license;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public Date getLastModifiedAt() {
		return lastModifiedAt;
	}

	public String getFileCategory() {
		return fileCategory;
	}
	
	public String getUuid() {
		return uuid;
	}

	public int getDownloadCount() {
		return downloadCount;                 
	}                                         
                                              
	public String getTitle() {                
		return title;                         
	}                                         
                                              
	public String getComment() {              
		return comment;                       
	}                                         
                                              
	public String getPublisher() {            
		return publisher;                     
	}                                         
                                              
	public String getSource() {               
		return source;                        
	}                                         

	public String getCreator() {
		return creator;
	}

	public Date getPubDate() {
		return pubDate;
	}
	public Date getLockedAt() {
		return lockedAt;
	}

	public Identity getLockedBy() {
		return lockedBy;
	}

	public String getLanguage() {
		return language;
	}

	public boolean isLocked() {
		return locked;
	}

	public long getRevisionNr() {
		return revisionNr;
	}

	public String getRevisionComment() {
		return revisionComment;
	}

	public Boolean isTrashed() {
		return trashed;
	}
	
}

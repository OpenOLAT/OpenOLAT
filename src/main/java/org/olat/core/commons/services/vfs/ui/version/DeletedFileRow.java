package org.olat.core.commons.services.vfs.ui.version;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSMetadataRef;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 21 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DeletedFileRow implements VFSMetadataRef {
	
	private final VFSMetadata metadata;
	private VFSRevision lastRevision;
	private final List<VFSRevision> revisions = new ArrayList<>();
	
	private DownloadLink downloadLink;
	
	public DeletedFileRow(VFSMetadata metadata) {
		this.metadata = metadata;
	}
	
	@Override
	public Long getKey() {
		return metadata.getKey();
	}
	
	public String getFilename() {
		return metadata.getFilename();
	}
	
	public Identity getDeletedBy() {
		return lastRevision == null ? null : lastRevision.getAuthor();
	}
	
	public Date getDate() {
		return lastRevision == null ? null : lastRevision.getCreationDate();
	}
	
	public VFSMetadata getMetadata() {
		return metadata;
	}
	
	public DownloadLink getDownloadLink() {
		return downloadLink;
	}
	
	public void setDownloadLink(DownloadLink link) {
		downloadLink = link;
	}
	
	public VFSRevision getLastRevision() {
		return lastRevision;
	}
	
	public List<VFSRevision> getRevisions() {
		return revisions;
	}
	
	public void addRevision(VFSRevision revision) {
		if(revision == null) return;
		
		if(lastRevision == null || lastRevision.getRevisionNr() < revision.getRevisionNr()) {
			lastRevision = revision;
		}
		revisions.add(revision);
	}
}

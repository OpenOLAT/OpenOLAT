package org.olat.user;

import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 23 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface UserDataExportService {
	
	public List<String> getExporterIds();

	public void requestExportData(Identity identity, Collection<String> eporterIds);
	
	public void exportData(Long requestKey);
	
	public UserDataExport getCurrentData(IdentityRef identity);
	
	public MediaResource getDownload(IdentityRef identity);

}

package org.olat.core.commons.services.vfs.manager;

import java.util.List;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 1 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VFSMaintenance {
	
	@Autowired
	public List<VFSMetadata> getVersionsOfDeleteFiles() {
		return null;
	}

}

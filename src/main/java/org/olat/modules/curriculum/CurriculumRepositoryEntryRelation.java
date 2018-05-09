package org.olat.modules.curriculum;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CurriculumRepositoryEntryRelation extends CreateInfo, ModifiedInfo {
	
	public Long getKey();
	
	public CurriculumElement getCurriculumElement();
	
	public RepositoryEntry getEntry();
	
	public boolean isMaster();

}

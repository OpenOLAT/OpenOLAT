package org.olat.course.assessment.manager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface UserCourseInformationsManager {
	
	public UserCourseInformations getUserCourseInformations(Long courseResourceId, Identity identity);
	
	public UserCourseInformations updateUserCourseInformations(Long courseResId, Identity identity);
	
	public Date getInitialLaunchDate(Long courseResourceId, Identity identity);
	
	public Map<Long,Date> getInitialLaunchDates(Long courseResourceId, List<Identity> identities);
	
	public int deleteUserCourseInformations(RepositoryEntry entry);

}

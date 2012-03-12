package org.olat.course.assessment;

import java.util.Date;

import org.olat.core.id.Identity;

/**
 * Some statistical datas about a user visiting a course
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface UserCourseInformations {
	
	public Long getKey();
	
	public Date getInitialLaunch();
	
	public Date getRecentLaunch();
	
	public int getVisit();
	
	public long getTimeSpend();
	
	public Identity getIdentity();

}

package org.olat.course.assessment;

import org.olat.course.core.CourseElement;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CoachingAssignmentStatistics {
	
	public RepositoryEntry getRepositoryEntry();
	
	public CourseElement getCourseElement();
	
	public long getNumOfAssignments();

}

package org.olat.course.assessment.model;

import org.olat.course.assessment.CoachingAssignmentStatistics;
import org.olat.course.core.CourseElement;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachingAssignmentStatisticsImpl implements CoachingAssignmentStatistics {
	
	private RepositoryEntry repositoryEntry;
	private CourseElement courseElement;
	
	private long numOfAssignments;
	
	public CoachingAssignmentStatisticsImpl(RepositoryEntry repositoryEntry, CourseElement courseElement, long numOfAssignments) {
		this.repositoryEntry = repositoryEntry;
		this.courseElement = courseElement;
		this.numOfAssignments = numOfAssignments;
	}

	@Override
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	@Override
	public CourseElement getCourseElement() {
		return courseElement;
	}
	
	public long getNumOfAssignments() {
		return numOfAssignments;
	}
	
	

}

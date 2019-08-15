package org.olat.modules.coach.model;

/**
 * 
 * Initial date: 14 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachingSecurity {
	
	private final boolean masterCoach;
	private final boolean coach;
	private final boolean teacher;
	
	public CoachingSecurity(boolean masterCoach, boolean coach, boolean teacher) {
		this.masterCoach = masterCoach;
		this.coach = coach;
		this.teacher = teacher;
	}

	public boolean isMasterCoachForLectures() {
		return masterCoach;
	}

	public boolean isCoach() {
		return coach;
	}

	public boolean isTeacher() {
		return teacher;
	}
}

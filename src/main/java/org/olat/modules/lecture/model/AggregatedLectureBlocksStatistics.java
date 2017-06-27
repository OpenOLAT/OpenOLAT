package org.olat.modules.lecture.model;

/**
 * 
 * Initial date: 27 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AggregatedLectureBlocksStatistics {

	private long personalPlannedLectures;
	private long attendedLectures;
	private long authorizedAbsentLectures;
	private long absentLectures;
	private double rate;
	
	public AggregatedLectureBlocksStatistics(long personalPlannedLectures, long attendedLectures,
			long authorizedAbsentLectures, long absentLectures, double rate) {
		this.personalPlannedLectures = personalPlannedLectures;
		this.attendedLectures = attendedLectures;
		this.authorizedAbsentLectures = authorizedAbsentLectures;
		this.absentLectures = absentLectures;
		this.rate = rate;
	}
	
	public long getPersonalPlannedLectures() {
		return personalPlannedLectures;
	}
	
	public void setPersonalPlannedLectures(long personalPlannedLectures) {
		this.personalPlannedLectures = personalPlannedLectures;
	}
	
	public long getAttendedLectures() {
		return attendedLectures;
	}
	
	public void setAttendedLectures(long attendedLectures) {
		this.attendedLectures = attendedLectures;
	}
	
	public long getAuthorizedAbsentLectures() {
		return authorizedAbsentLectures;
	}
	
	public void setAuthorizedAbsentLectures(long authorizedAbsentLectures) {
		this.authorizedAbsentLectures = authorizedAbsentLectures;
	}
	
	public long getAbsentLectures() {
		return absentLectures;
	}
	
	public void setAbsentLectures(long absentLectures) {
		this.absentLectures = absentLectures;
	}
	
	public double getRate() {
		return rate;
	}
	
	public void setRate(double rate) {
		this.rate = rate;
	}
}
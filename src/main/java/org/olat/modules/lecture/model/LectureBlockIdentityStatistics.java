package org.olat.modules.lecture.model;

/**
 * 
 * 
 * Initial date: 16 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockIdentityStatistics extends LectureBlockStatistics {
	
	private final String[] identityProps;
	
	public LectureBlockIdentityStatistics(Long identityKey, String[] identityProps,
			Long repoKey, String displayName, boolean calculateRate, double requiredRate) {
		super(identityKey, repoKey, displayName, calculateRate, requiredRate);
		this.identityProps = identityProps;
	}
	
	public String[] getIdentityProps() {
		return identityProps;
	}
	
	public String getIdentityProp(int pos) {
		return identityProps[pos];
	}

}

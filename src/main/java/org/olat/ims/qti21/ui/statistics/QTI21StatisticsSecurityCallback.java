package org.olat.ims.qti21.ui.statistics;

/**
 * 
 * Initial date: 19.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21StatisticsSecurityCallback {
	
	private boolean anonymousUsers;
	private boolean nonParticipantUsers;
	
	public QTI21StatisticsSecurityCallback(boolean anonymousUsers, boolean nonParticipantUsers) {
		this.anonymousUsers = anonymousUsers;
		this.nonParticipantUsers = nonParticipantUsers;
	}
	
	public boolean canViewAnonymousUsers() {
		return anonymousUsers;
	}
	
	public boolean canViewNonParticipantUsers() {
		return nonParticipantUsers;
	}

}

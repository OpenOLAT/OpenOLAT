package org.olat.core.util.vfs.lock;

/**
 * 
 * Initial date: 26 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LockResult {
	
	public static final LockResult LOCK_FAILED = new LockResult(false, null);
	
	private final boolean acquired;
	private final LockInfo lockInfo;
	
	public LockResult(boolean acquired, LockInfo lockInfo) {
		this.acquired = acquired;
		this.lockInfo = lockInfo;
	}

	public boolean isAcquired() {
		return acquired;
	}

	public LockInfo getLockInfo() {
		return lockInfo;
	}
}

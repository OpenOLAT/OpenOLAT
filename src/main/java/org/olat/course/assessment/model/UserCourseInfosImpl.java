package org.olat.course.assessment.model;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.resource.OLATResource;

public class UserCourseInfosImpl extends PersistentObject implements UserCourseInformations, ModifiedInfo {

	private static final long serialVersionUID = -6933599547069673655L;
	
	private Date lastModified;
	private Date initialLaunch;
	private Date recentLaunch;
	private int visit;
	private long timeSpend;
	
	private Identity identity;
	private OLATResource resource;

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public Date getInitialLaunch() {
		return initialLaunch;
	}

	public void setInitialLaunch(Date initialLaunch) {
		this.initialLaunch = initialLaunch;
	}

	@Override
	public Date getRecentLaunch() {
		return recentLaunch;
	}

	public void setRecentLaunch(Date recentLaunch) {
		this.recentLaunch = recentLaunch;
	}

	@Override
	public int getVisit() {
		return visit;
	}

	public void setVisit(int visit) {
		this.visit = visit;
	}

	@Override
	public long getTimeSpend() {
		return timeSpend;
	}

	public void setTimeSpend(long timeSpend) {
		this.timeSpend = timeSpend;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	public OLATResource getResource() {
		return resource;
	}

	public void setResource(OLATResource resource) {
		this.resource = resource;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 9271 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof UserCourseInfosImpl) {
			UserCourseInfosImpl prop = (UserCourseInfosImpl)obj;
			return getKey() != null && getKey().equals(prop.getKey());	
		}
		return false;
	}
}

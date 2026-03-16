/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.Date;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 19.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EmptyUserRating implements UserRating {

	private static final long serialVersionUID = 8713548728714250541L;
	
	private final IdentityRef member;
	
	public EmptyUserRating(IdentityRef member) {
		this.member = member;
	}

	@Override
	public Long getKey() {
		return null;
	}
	
	@Override
	public Date getCreationDate() {
		return null;
	}

	@Override
	public Date getLastModified() {
		return null;
	}

	@Override
	public void setLastModified(Date date) {
		//
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return false;
	}

	@Override
	public String getResName() {
		return null;
	}

	@Override
	public Long getResId() {
		return null;
	}

	@Override
	public String getResSubPath() {
		return null;
	}

	@Override
	public IdentityRef getCreator() {
		return member;
	}

	@Override
	public Integer getRating() {
		return null;
	}

	@Override
	public void setRating(Integer ratingValue) {
		//
	}
}
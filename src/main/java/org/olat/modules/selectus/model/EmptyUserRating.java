/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
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
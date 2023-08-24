/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.assessment.model;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.modules.assessment.Overridable;

/**
 * 
 * Initial date: 27 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OverridableImpl<T> implements Overridable<T> {
	
	private T current;
	private T original;
	private Identity modBy;
	private Date modDate;
	private Date date;
	
	public OverridableImpl() {
		//
	}
	
	public OverridableImpl(T current) {
		this.current = current;
	}

	public OverridableImpl(T current, T original, Date date, Identity modBy, Date modDate) {
		this.current = current;
		this.original = original;
		this.modBy = modBy;
		this.modDate = modDate;
		this.date = date;
	}

	@Override
	public T getCurrent() {
		return current;
	}

	@Override
	public void setCurrent(T current) {
		if (isOverridden()) {
			this.original = current;
		} else {
			this.current = current;
		}
	}

	@Override
	public void override(T custom, Identity modBy, Date modDate) {
		if (!isOverridden()) {
			this.original = this.current;
		}
		this.current = custom;
		this.modBy = modBy;
		this.modDate = modDate;
	}

	@Override
	public boolean isOverridden() {
		return modDate != null;
	}
	
	@Override
	public void reset() {
		if (isOverridden()) {
			current = original;
		}
		original = null;
		modBy = null;
		modDate = null;
	}

	@Override
	public T getOriginal() {
		return original;
	}

	@Override
	public Identity getModBy() {
		return modBy;
	}

	@Override
	public Date getModDate() {
		return modDate;
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public Overridable<T> clone() {
		OverridableImpl<T> clone = new OverridableImpl<>();
		clone.current = this.current;
		clone.original = this.original;
		clone.modBy = this.modBy;
		clone.modDate = this.modDate;
		clone.date = this.date;
		return clone;
	}

}

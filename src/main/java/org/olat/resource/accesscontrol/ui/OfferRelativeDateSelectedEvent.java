/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.resource.accesscontrol.ui;

import org.olat.core.gui.control.Event;
import org.olat.resource.accesscontrol.OfferDateRef;
import org.olat.resource.accesscontrol.OfferDateUnit;

/**
 * Initial date: 2026-06-15<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class OfferRelativeDateSelectedEvent extends Event {

	private static final long serialVersionUID = 1L;

	public static final String COMMAND = "offer-rel-date-selected";

	private final Integer value;
	private final OfferDateUnit unit;
	private final OfferDateRef ref;
	private final boolean from;

	public OfferRelativeDateSelectedEvent(Integer value, OfferDateUnit unit, OfferDateRef ref, boolean from) {
		super(COMMAND);
		this.value = value;
		this.unit = unit;
		this.ref = ref;
		this.from = from;
	}

	public Integer getValue() {
		return value;
	}

	public OfferDateUnit getUnit() {
		return unit;
	}

	public OfferDateRef getRef() {
		return ref;
	}

	public boolean isFrom() {
		return from;
	}

}

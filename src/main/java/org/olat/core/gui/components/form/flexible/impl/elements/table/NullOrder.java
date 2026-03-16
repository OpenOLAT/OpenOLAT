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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

/**
 * Defines how null values are positioned when sorting a column.
 *
 * Initial date: 6 Mar 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public enum NullOrder {

	/** Nulls sort before non-nulls in ascending order, after non-nulls in descending order. */
	NULLS_FIRST,

	/** Nulls sort after non-nulls in ascending order, before non-nulls in descending order. */
	NULLS_LAST,

	/** Nulls always sort before non-nulls, regardless of sort direction. */
	NULLS_ALWAYS_FIRST,

	/** Nulls always sort after non-nulls, regardless of sort direction. */
	NULLS_ALWAYS_LAST
}

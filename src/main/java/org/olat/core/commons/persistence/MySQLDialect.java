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
package org.olat.core.commons.persistence;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.query.sqm.CastType;

/**
 * The dialect override the official one to fix the issue:<br>
 * https://hibernate.atlassian.net/browse/HHH-15647<br>
 * 
 * It replaces the cast(? as bigint) with a MySQL conform cast(? as signed)
 * 
 * 
 * Initial date: 7 nov. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MySQLDialect extends org.hibernate.dialect.MySQLDialect {
	
	public MySQLDialect() {
		super();
	}

	public MySQLDialect(DatabaseVersion version) {
		super(version);

	}

	public MySQLDialect(DialectResolutionInfo info) {
		super(info);
	}

	@Override
	public String castPattern(CastType from, CastType to) {
		if ( to == CastType.LONG ) {
			switch ( from ) {
				case STRING:
				case INTEGER:
				case LONG:
				case FLOAT:
				case DOUBLE:
					return "cast(?1 as signed)";
				default:
					break;
			}
		}
		return super.castPattern( from, to );
	}
}

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
package org.olat.ims.lti13.model.json;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * 
 * Initial date: 5 mars 2021<br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TimestampDeserializer extends StdDeserializer<Date> {

	private static final long serialVersionUID = 7000348246299571620L;

	private static final Logger log = Tracing.createLoggerFor(TimestampDeserializer.class);
	
	private SimpleDateFormat dfz = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	
	private SimpleDateFormat[] dfs = new SimpleDateFormat[] {
		dfz,
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"),
	};

	public TimestampDeserializer() {
		super(Date.class);
	}

	@Override
	public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		if (p.hasToken(JsonToken.VALUE_STRING)) {
			String str = p.getText().trim();
			if (str.isEmpty()) {
				final CoercionAction act = _checkFromStringCoercion(ctxt, str);
				switch (act) { // note: Fail handled above
					case AsEmpty:
						return new java.util.Date(0L);
					case AsNull:
					case TryConvert:
					default:
				}
				return null;
			}
			
			for(SimpleDateFormat df:dfs) {
				try {
					return df.parse(str);
				} catch (ParseException e) {
					log.trace("", e);
				}
			}	
		}
		return null;
	}

}

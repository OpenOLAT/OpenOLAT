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
package org.olat.core.commons.services.video;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.jcodec.common.logging.LogLevel;
import org.jcodec.common.logging.LogSink;
import org.jcodec.common.logging.Message;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 30 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Log4j2Sink implements LogSink {
	
	private static final Logger log = Tracing.createLoggerFor(Log4j2Sink.class);
	
	
	public LogLevel getLogLevel() {
		if(log.isTraceEnabled() || log.isDebugEnabled()) {
			return LogLevel.DEBUG;
		} else if(log.isInfoEnabled()) {
			return LogLevel.INFO;
		}
		return LogLevel.ERROR;
	}

	@Override
	public void postMessage(Message msg) {
		LogLevel jLevel = msg.getLevel();
		Level level = Level.DEBUG;
		if(jLevel == LogLevel.INFO) {
			level = Level.INFO;
		} else if(jLevel == LogLevel.WARN) {
			level = Level.WARN;
		} else if(jLevel == LogLevel.ERROR) {
			level = Level.ERROR;
		}
		log.log(level, "jCodec class:{} method:{} line:{} file:{} message:{}", msg.getClassName(), msg.getMethodName(), String.valueOf(msg.getLineNumber()), msg.getFileName(), msg.getMessage());
	}
}

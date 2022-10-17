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
package org.olat.core.dispatcher;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * Used to override the standard error page of Tomcat. The goal is to
 * suppress the stack trace at any cost.
 * 
 * Initial date: 25 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ErrorsDispatcher implements Dispatcher {
	
	private static final Logger log = Tracing.createLoggerFor(ErrorsDispatcher.class);

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try(PrintWriter writer = response.getWriter()) {
			response.setContentType("text/html;charset=utf-8");
			writer.append("<!DOCTYPE html><html>")
			      .append("<head><title>Unexpected error</title></head>")
			      .append("<body><h3>An unexpected error occured... Sorry!</h3><p>Error code: ")
			      .append(Integer.toString(response.getStatus()))
			      .append("</p></body></html>");
		} catch(Exception e) {
			log.error("", e);
		}
	}
}

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
package org.olat.core.servlets;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * Description:<br>
 * Use this servlet filter to prepare the servlet request and cleanup database
 * related stuff after the request finished.
 * 
 * <P>
 * Initial Date: 29.06.2009 <br>
 * 
 * @author gnaegi
 */
public class ServletWrapperFilter implements Filter {
	private static final OLog log = Tracing.createLoggerFor(ServletWrapperFilter.class);

	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
	// no configuration
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	// nothing to destroy
	}

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain target) throws IOException,
			ServletException {
		if (!(servletRequest instanceof HttpServletRequest)) {
			// don't know what to do, just execute target and quitt
			target.doFilter(servletRequest, servletResponse);
			return;
		}

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		Tracing.setUreq(request);

		boolean success = false;
		try {
			// execute other filters or request
			target.doFilter(request, response);
			DBFactory.getInstance(false).commitAndCloseSession();
			success = true;
		} catch (Throwable e) {
			log.error("Exception in ServletWrapperFilter", e);
			DispatcherAction.sendBadRequest(request.getPathInfo(), response);
		} finally {
			// execute the cleanup code for this request
			Tracing.setUreq(null);
			
			if (!success) {
				DBFactory.getInstance().rollbackAndCloseSession();
			}
		}

	}

}

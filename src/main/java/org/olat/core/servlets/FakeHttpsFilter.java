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
 * Universität Innsbruck
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

/**
 * A Filter that uses a special RequestWrapper to fake an HTTPS connection
 *
 */

public class FakeHttpsFilter implements Filter {
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
	  throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		FakeHttpsRequestWrapper fakereq = new FakeHttpsRequestWrapper(request);
		chain.doFilter(fakereq, res);
	}
	
	public void init(FilterConfig config) throws ServletException {
		// nothing to do
	}
  
	public void destroy() {
		// no resources to release
	}		
}

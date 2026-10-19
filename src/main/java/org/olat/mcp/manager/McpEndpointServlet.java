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
package org.olat.mcp.manager;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;

/**
 * The servlet behind /mcp. It only delegates to the stateless transport of
 * the SDK, which is a servlet itself but not managed by the container. The
 * authentication, the rate limit and the audit happen in {@link McpAuthFilter}
 * before and after this servlet.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpEndpointServlet extends HttpServlet {

	private static final long serialVersionUID = 2026100204L;

	private transient McpServerFactory serverFactory;

	private McpServerFactory getServerFactory() {
		if(serverFactory == null) {
			serverFactory = CoreSpringFactory.getImpl(McpServerFactory.class);
		}
		return serverFactory;
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		getServerFactory().service(request, response);
	}
}

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
package org.olat.core.util.httpclient;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;

/**
 * The content of a response which doesn't read the rest of the body when closed.
 * <p>
 * Apache HttpClient 5 reads the remaining body when a response or its content is
 * closed, to reuse the connection. If the reader stops early (size limit, parser
 * which stops at the end of the document, error...), a remote server can keep the
 * thread busy with an endless body. This stream cancels the request first, which
 * discards the connection. If the body was fully read, the connection is already
 * released and canceling does nothing.
 * <p>
 * Use it for every response from a user-provided URL which is not read to the end
 * by OpenOlat itself:
 * <pre>
 * HttpGet get = new HttpGet(url);
 * try(ClassicHttpResponse response = client.executeOpen(null, get, null);
 * 		InputStream in = CancellingInputStream.content(response, get)) {
 * 	...
 * }
 * </pre>
 *
 * Initial date: 17 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CancellingInputStream extends FilterInputStream {

	private final Cancellable request;

	public CancellingInputStream(InputStream in, Cancellable request) {
		super(in);
		this.request = request;
	}

	/**
	 * @param response The response
	 * @param request The request, typically a {@code HttpUriRequestBase}
	 * @return The content of the response or an empty stream if the response has no body
	 * @throws IOException
	 */
	public static InputStream content(ClassicHttpResponse response, Cancellable request) throws IOException {
		HttpEntity entity = response.getEntity();
		InputStream content = entity == null ? null : entity.getContent();
		return new CancellingInputStream(content == null ? InputStream.nullInputStream() : content, request);
	}

	@Override
	public void close() throws IOException {
		try {
			request.cancel();
		} finally {
			super.close();
		}
	}
}

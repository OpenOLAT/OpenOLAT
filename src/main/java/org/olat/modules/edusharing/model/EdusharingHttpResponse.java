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
package org.olat.modules.edusharing.model;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.olat.modules.edusharing.EdusharingException;
import org.olat.modules.edusharing.EdusharingResponse;

/**
 * 
 * Initial date: 3 Jan 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdusharingHttpResponse implements EdusharingResponse {
	
	private final int status;
	private final CloseableHttpClient httpClient;
	private final CloseableHttpResponse httpResponse;
	private final HttpEntity httpEntity;

	public EdusharingHttpResponse(CloseableHttpClient httpClient, CloseableHttpResponse httpResponse) {
		this.httpClient = httpClient;
		this.httpResponse = httpResponse;
		this.httpEntity = httpResponse.getEntity();
		this.status = httpResponse.getStatusLine().getStatusCode();
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public boolean hasContent() {
		return status == 200;
	}

	@Override
	public long getContentLength() {
		return httpEntity.getContentLength();
	}

	@Override
	public InputStream getContent() throws EdusharingException {
		try {
			return httpEntity.getContent();
		} catch (Exception e) {
			throw new EdusharingException(e);
		}
	}

	@Override
	public String getMimeType() {
		ContentType contentType = ContentType.getOrDefault(httpEntity);
		return contentType.getMimeType();
	}

	@Override
	public void close() throws Exception {
		httpResponse.close();
		httpClient.close();
	}

}

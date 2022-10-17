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
package org.olat.ims.lti.ui;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.imsglobal.basiclti.BasicLTIUtil;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.ims.lti.LTIManager;

/**
 *
 * Initial date: 13.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PostDataMapper implements Mapper {

	private final Map<String,String> unsignedProps;
	private final String launchUrl;
	private final String baseUrl;
	private final String oauthKey;
	private final String oauthSecret;
	private final boolean debug;

	/**
	 *
	 * @param unsignedProps
	 * @param url the URL used as the launch URL and the base URL
	 * @param oauthKey
	 * @param oauthSecret
	 * @param debug
	 */
	public PostDataMapper(Map<String,String> unsignedProps, String url, String oauthKey, String oauthSecret, boolean debug) {
		this(unsignedProps, url, url, oauthKey, oauthSecret, debug);
	}

	/**
	 *
	 * @param unsignedProps
	 * @param launchUrl the LTI launch URL
	 * @param baseUrl the base URL for the oauth sign
	 * @param oauthKey
	 * @param oauthSecret
	 * @param debug
	 */
	public PostDataMapper(Map<String,String> unsignedProps, String launchUrl, String baseUrl, String oauthKey, String oauthSecret, boolean debug) {
		this.unsignedProps = unsignedProps;
		this.launchUrl = launchUrl;
		this.baseUrl = baseUrl;
		this.oauthKey = oauthKey;
		this.oauthSecret = oauthSecret;
		this.debug = debug;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		Map<String,String> props = CoreSpringFactory.getImpl(LTIManager.class).sign(unsignedProps, baseUrl, oauthKey, oauthSecret);
		String postData = BasicLTIUtil.postLaunchHTML(props, launchUrl, debug);
		StringMediaResource mediares = new StringMediaResource();
		mediares.setData(postData);
		mediares.setContentType("text/html;charset=utf-8");
		mediares.setEncoding("UTF-8");
		return mediares;
	}
}
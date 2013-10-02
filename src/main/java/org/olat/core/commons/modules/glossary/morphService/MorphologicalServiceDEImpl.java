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
package org.olat.core.commons.modules.glossary.morphService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.httpclient.HttpClientFactory;
import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;

/**
 * Description:<br>
 * Connects to a morphological service and lets retrieve flexions for a word.
 * This is a variant to be used with german glossaries and is only compatible
 * with the defined service.
 * 
 * <P>
 * Initial Date: 09.12.2008 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class MorphologicalServiceDEImpl implements MorphologicalService {

	private static final String MORPHOLOGICAL_SERVICE_ADRESS = "http://www.cl.uzh.ch/kitt/cgi-bin/olat/ms_de.cgi";
	private static final String SERVICE_NAME = "Morphological Service DE - University Zurich";
	private static final String SERVICE_IDENTIFIER = "ms-de-uzh-cli";
	private static final String PART_OF_SPEECH_PARAM = "pos";
	private static final String GLOSS_TERM_PARAM = "word";

	private static OLog log = Tracing.createLoggerFor(MorphologicalServiceDEImpl.class);

	private String replyStatus = "";

	/**
	 * 
	 */ 
	public MorphologicalServiceDEImpl() {
		// 
	}

	/**
	 * 
	 * @see org.olat.core.commons.modules.glossary.morphService.FlexionServiceClient#getFlexions(java.lang.String)
	 */
	@Override
	public List<String> getFlexions(String word) {
		return getFlexions(assumePartOfSpeech(word), word);
	}

	/**
	 * 
	 * @see org.olat.core.commons.modules.glossary.morphService.FlexionServiceClient#getFlexions(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public List<String> getFlexions(String partOfSpeech, String word) {
		InputStream xmlReplyStream = retreiveXMLReply(partOfSpeech, word);
		XStream xstream = XStreamHelper.createXStreamInstance();
		xstream.alias("xml", FlexionReply.class);
		xstream.alias("wordform", String.class);
		ArrayList<String> stemWithWordforms;
		try {
			Object msReply = XStreamHelper.readObject(xstream, xmlReplyStream);
			FlexionReply flexionReply = (FlexionReply) msReply;
			// set reply status to remind it
			setReplyStatus(flexionReply.getStatus());
			stemWithWordforms = flexionReply.getStem();
		} catch (Exception e) {
			log.error("XML-Reply was not valid XML", e);
			stemWithWordforms = null;
			setReplyStatus(MorphologicalService.STATUS_ERROR);
		}
		return stemWithWordforms;
	}

	/**
	 * Remind: this assumptions are only valuable for german language!
	 * 
	 * @see org.olat.core.commons.modules.glossary.morphService.FlexionServiceClient#assumePartOfSpeech(java.lang.String)
	 */
	@Override
	public String assumePartOfSpeech(String glossTerm) {
		if (glossTerm.contains(",")) {
			// assume the form "House, beautiful"
			return "an";
		}
		// look if glossTerm consists of more than 1 word -> adjective + noun
		String firstChar = glossTerm.substring(0, 1);
		if (glossTerm.split(" ").length > 1 && firstChar.equals(firstChar.toLowerCase())) return "an";
		//single or multiple word with big initial -> noun(s)
		if (firstChar.equals(firstChar.toUpperCase())) return "n";
		return "v";
	}

	private InputStream retreiveXMLReply(String partOfSpeech, String word) {
		try {
			URIBuilder uriBuilder = new URIBuilder(MORPHOLOGICAL_SERVICE_ADRESS);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>(2);
			nvps.add(new BasicNameValuePair(PART_OF_SPEECH_PARAM, partOfSpeech));
			nvps.add(new BasicNameValuePair(GLOSS_TERM_PARAM, word));
			
			CloseableHttpClient client = HttpClientFactory.getHttpClientInstance(true);
			HttpGet method = new HttpGet(uriBuilder.build());
			
			HttpResponse response = client.execute(method);
			int status = response.getStatusLine().getStatusCode();
			if (status == HttpStatus.SC_NOT_MODIFIED || status == HttpStatus.SC_OK) {
				if (log.isDebug()) {
					log.debug("got a valid reply!");
				}
			} else if (status == HttpStatus.SC_NOT_FOUND) {
				log.error("Morphological Service unavailable (404)::" + response.getStatusLine().toString());
			} else {
				log.error("Unexpected HTTP Status::" + response.getStatusLine().toString());
			}
			
			Header responseHeader = response.getFirstHeader("Content-Type");
			if (responseHeader == null) {
				// error
				log.error("URL not found!");
			}
			return response.getEntity().getContent();
		} catch (Exception e) {
			log.error("Unexpected exception trying to get flexions!", e);
			return null;
		}
	}

	/**
	 * 
	 * @see org.olat.core.commons.modules.glossary.morphService.FlexionServiceClient#getReplyStatus()
	 */
	public String getReplyStatus() {
		return replyStatus;
	}

	/**
	 * sets the status found in the reply
	 * 
	 * @param replyStatus
	 */
	private void setReplyStatus(String replyStatus) {
		this.replyStatus = replyStatus;
	}
	
	/**
	 * 
	 * @see org.olat.core.commons.modules.glossary.morphService.FlexionServiceManager#getFlexionServiceDescriptor()
	 */
	public String getMorphServiceDescriptor() {
		return SERVICE_NAME;
	}

	/**
	 * 
	 * @see org.olat.core.commons.modules.glossary.morphService.FlexionServiceManager#getFlexionServiceIdentifier()
	 */
	public String getMorphServiceIdentifier() {
		return SERVICE_IDENTIFIER;
	}

	// private String inputStreamToString(InputStream in) throws IOException {
	// BufferedReader bufferedReader = new BufferedReader(new
	// InputStreamReader(in));
	// StringBuilder stringBuilder = new StringBuilder();
	// String line = null;
	//
	// while ((line = bufferedReader.readLine()) != null) {
	// stringBuilder.append(line + "\n");
	// }
	//
	// bufferedReader.close();
	// return stringBuilder.toString();
	// }

}

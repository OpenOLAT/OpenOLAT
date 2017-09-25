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
package org.olat.login.oauth.spi;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthSPI;
import org.olat.login.oauth.model.OAuthUser;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * Initial date: 04.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LinkedInProvider implements OAuthSPI {
	
	private static final OLog log = Tracing.createLoggerFor(LinkedInProvider.class);
	
	@Autowired
	private OAuthLoginModule oauthModule;
	
	@Override
	public boolean isEnabled() {
		return oauthModule.isLinkedInEnabled();
	}
	
	@Override
	public boolean isRootEnabled() {
		return false;
	}
	
	@Override
	public boolean isImplicitWorkflow() {
		return false;
	}

	@Override
	public String getName() {
		return "linkedin";
	}

	@Override
	public String getProviderName() {
		return "LINKEDIN";
	}

	@Override
	public String getIconCSS() {
		return "o_icon o_icon_provider_linkedin";
	}

	@Override
	public Api getScribeProvider() {
		return new LinkedInApi();
	}

	@Override
	public String getAppKey() {
		return oauthModule.getLinkedInApiKey();
	}

	@Override
	public String getAppSecret() {
		return oauthModule.getLinkedInApiSecret();
	}

	@Override
	public String[] getScopes() {
		return new String[]{ "r_basicprofile", "r_emailaddress" };
	}

	@Override
	public  OAuthUser getUser(OAuthService service, Token accessToken) {
		OAuthRequest oauthRequest = new OAuthRequest(Verb.GET, "http://api.linkedin.com/v1/people/~:(id,first-name,last-name,email-address)");
		service.signRequest(accessToken, oauthRequest);
		Response oauthResponse = oauthRequest.send();
		String body = oauthResponse.getBody();
		return parseInfos(body);
	}
	
	public OAuthUser parseInfos(String body) {
		OAuthUser infos = new OAuthUser();
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new StringReader(body)));
			
			NodeList nodes = doc.getElementsByTagName("person");
		    for (int i = 0; i < nodes.getLength(); i++) {
		      Element element = (Element)nodes.item(i);
		      for(Node node=element.getFirstChild(); node!=null; node=node.getNextSibling()) {
		    	  String localName = node.getNodeName();
		    	  if("first-name".equals(localName)) {
		    		  infos.setFirstName(getCharacterDataFromElement(node));
		    	  } else if("last-name".equals(localName)) {
		    		  infos.setLastName(getCharacterDataFromElement(node));
		    	  } else if("email-address".equals(localName)) {
		    		  infos.setEmail(getCharacterDataFromElement(node));
		    	  } else if("id".equals(localName)) {
		    		  infos.setId(getCharacterDataFromElement(node));
		    	  }
		      }
		    }
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.error("", e);
		}
		return infos;
	}
	
	public String getCharacterDataFromElement(Node parentNode) {
		StringBuilder sb = new StringBuilder();
		for (Node node = parentNode.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() == Node.TEXT_NODE) {
				String text = node.getNodeValue();
				if (StringHelper.containsNonWhitespace(text)) {
					sb.append(text);
				}
			}
		}
		return sb.toString();
	}

	@Override
	public String getIssuerIdentifier() {
		return "https://linkedin.com";
	}
}

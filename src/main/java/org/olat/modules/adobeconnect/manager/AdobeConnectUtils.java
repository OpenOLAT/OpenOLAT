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
package org.olat.modules.adobeconnect.manager;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLUtils;
import org.olat.modules.adobeconnect.model.AdobeConnectError;
import org.olat.modules.adobeconnect.model.AdobeConnectErrorCodes;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.model.BreezeSession;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * Initial date: 23 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectUtils {
	
	private static final Logger log = Tracing.createLoggerFor(AdobeConnectUtils.class);
	
	protected static boolean isStatusOk(HttpEntity entity) {
		try {
			Document doc = getDocumentFromEntity(entity);
			return AdobeConnectUtils.isStatusOk(doc);
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	protected static BreezeSession getBreezeSessionIfOk(HttpResponse response, BreezeSession infoSession) {
		BreezeSession session = null;
		try {
			HttpEntity entity = response.getEntity();
			Document doc = getDocumentFromEntity(entity);
			if(isStatusOk(doc)) {
				Header header = response.getFirstHeader("Set-Cookie");
				if(header != null) {
					session = BreezeSession.valueOf(header);
				} else {
					String cookie = getFirstElementValue(doc.getDocumentElement(), "cookie");
					if(StringHelper.containsNonWhitespace(cookie)) {
						session = BreezeSession.valueOf(cookie);
					} else {
						session = infoSession;
					}
				}
			} else {
				print(doc);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return session;
	}
	
	protected static BreezeSession getBreezeSession(HttpResponse response) {
		BreezeSession session = null;
		try {
			Header header = response.getFirstHeader("Set-Cookie");
			if(header != null) {
				session = BreezeSession.valueOf(header);
				EntityUtils.consume(response.getEntity());
			} else {
				Document doc = getDocumentFromEntity(response.getEntity());
				String cookie = getFirstElementValue(doc.getDocumentElement(), "cookie");
				session = BreezeSession.valueOf(cookie);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return session;
	}
	
	protected static BreezeSession getBreezeSessionFromXml(HttpResponse response) {
		BreezeSession session = null;
		try {
			Document doc = getDocumentFromEntity(response.getEntity());
			String cookie = getFirstElementValue(doc.getDocumentElement(), "cookie");
			if(StringHelper.containsNonWhitespace(cookie)) {
				session = BreezeSession.valueOf(cookie);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return session;
	}
	
    protected static Document getDocumentFromEntity(HttpEntity entity) throws Exception {
    	try(InputStream in=entity.getContent()) {
	        DocumentBuilder dBuilder = OpenXMLUtils.getDocumentBuilder(true, false, false);
	        return dBuilder.parse(in);
    	} catch(Exception e) {
    		throw e;
    	}
    }
    
    protected static String getFirstElementValue(Element parent, String tagName) {
        Element element = getFirstElement(parent, tagName);
        return (element == null) ? "" : getCharacterDataFromElement(element);
    }
    
    protected static Element getFirstElement(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return (nodes != null && nodes.getLength() > 0) ? (Element) (nodes.item(0)) : null;
    }
    
    protected static String getCharacterDataFromElement(Element e) {
    	StringBuilder sb = new StringBuilder();
    	for(Node child = e.getFirstChild(); child != null; child = child.getNextSibling()) {
    		if (child instanceof CharacterData) {
            	CharacterData cd = (CharacterData)child;
                sb.append(cd.getData());
            }
    	}
        return sb.toString();
    }
	
	protected static final boolean isStatusOk(Document doc) {
		NodeList permissionList = doc.getElementsByTagName("status");
		if(permissionList != null && permissionList.getLength() == 1) {
			Element status = (Element)permissionList.item(0);
			return "ok".equalsIgnoreCase(status.getAttribute("code"));
		}
		return true;
	}
	
	protected static final boolean isStatusNoData(Document doc) {
		NodeList permissionList = doc.getElementsByTagName("status");
		if(permissionList != null && permissionList.getLength() == 1) {
			Element status = (Element)permissionList.item(0);
			return "no-data".equalsIgnoreCase(status.getAttribute("code"));
		}
		return true;
	}
	
	protected static final void error(Document doc, AdobeConnectErrors errors) {
		NodeList permissionList = doc.getElementsByTagName("status");
		if(permissionList != null && permissionList.getLength() == 1) {
			Element status = (Element)permissionList.item(0);
			String code = status.getAttribute("code");
			String subcode = status.getAttribute("subcode");
			
			AdobeConnectError error = new AdobeConnectError();
			error.setCode(AdobeConnectErrorCodes.unkown);
			
			if("invalid".equals(code)) {
				String field = null;
				NodeList invalidList = doc.getElementsByTagName("invalid");
				if(invalidList != null && invalidList.getLength() == 1) {
					Element invalid = (Element)invalidList.item(0);
					subcode = invalid.getAttribute("subcode");
					field = invalid.getAttribute("field");
				}

				if("duplicate".equals(subcode)) {
					error.setCode(AdobeConnectErrorCodes.duplicateField);
				} else if("format".equals(subcode)) {
					error.setCode(AdobeConnectErrorCodes.formatError);
				} else if("illegal-operation".equals(subcode)) {
					error.setCode(AdobeConnectErrorCodes.illegalOperation);
				} else if("missing".equals(subcode)) {
					error.setCode(AdobeConnectErrorCodes.missingParameter);
				} else if("no-such-item".equals(subcode)) {
					error.setCode(AdobeConnectErrorCodes.noSuchItem);
				} else if("range".equals(subcode)) {
					error.setCode(AdobeConnectErrorCodes.rangeError);
				}
				
				if(field != null) {
					error.setArguments(new String[] { field });
				}
				
			} else if("no-access".equals(code)) {
				error.setCode(AdobeConnectErrorCodes.noAccessDenied);
			}
			errors.append(error);
		}
	}
	
	protected static String orDefault(String val, String defaultValue) {
		return StringHelper.containsNonWhitespace(val) ? val : defaultValue;
	}
	
	protected static void print(Document document) {
		if(log.isDebugEnabled()) {
		    try(StringWriter writer = new StringWriter()) {
		    	TransformerFactory factory = TransformerFactory.newInstance();
				factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
				Transformer transformer = factory.newTransformer();
				Source source = new DOMSource(document);
				transformer.transform(source, new StreamResult(writer));
				writer.flush();
				log.info(writer.toString());
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
}

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
package org.olat.modules.edusharing.manager;

import java.io.ByteArrayOutputStream;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.modules.edusharing.EdusharingSecurityService;
import org.olat.modules.edusharing.EdusharingSignature;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdusharingSecurityHandler implements SOAPHandler<SOAPMessageContext> {

	private static final Logger log = Tracing.createLoggerFor(EdusharingSecurityHandler.class);
	
	@Autowired
	private EdusharingSecurityService edusharingSecurityService;

	public EdusharingSecurityHandler() {
		CoreSpringFactory.autowireObject(this);
	}
	
	@Override
	public Set<QName> getHeaders() {
		return new TreeSet<>();
	}

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		Boolean outboundProperty = (Boolean)context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outboundProperty.booleanValue()) {
			try {
				EdusharingSignature signature = edusharingSecurityService.createSignature();
				
				SOAPMessage message = context.getMessage();
				if (log.isDebugEnabled()) log.debug("Edusharing saop message (unsigned): " + toXml(message));
				
				SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
				if (envelope.getHeader() == null) {
					envelope.addHeader();
				}
				SOAPHeader header = envelope.getHeader();
				
				header.addNamespaceDeclaration("es", "http://webservices.edu_sharing.org");
				header.addChildElement("appId", "es").setValue(signature.getAppId());
				header.addChildElement("timestamp", "es").setValue(signature.getTimeStamp());
				header.addChildElement("signed", "es").setValue(signature.getSigned());
				header.addChildElement("signature", "es").setValue(signature.getSignature());
				
				if (log.isDebugEnabled()) log.debug("Edusharing saop message (signed): " + toXml(message));
			} catch (Exception e) {
				log.error("", e);
			}
		} else {
			// inbound
		}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return false;
	}

	@Override
	public void close(MessageContext context) {
		//
	}
	
	private String toXml(SOAPMessage message) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			message.writeTo(out);
			return out.toString("UTF-8");
		} catch (Exception e) {
			//
		}
		return null;
	}
}

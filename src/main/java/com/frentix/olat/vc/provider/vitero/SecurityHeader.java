package com.frentix.olat.vc.provider.vitero;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.commons.codec.binary.Base64;

public class SecurityHeader {
	
	public static void addAdminSecurityHeader(ViteroBookingProvider provider, Stub stub) {
		ServiceClient client = stub._getServiceClient();
		OMElement securityEl = generateSecurityHeader(provider.getAdminLogin(), provider.getAdminPassword());
		client.addHeader(securityEl);
	}
	
	public static OMElement generateSecurityHeader(String username, String password) {
		OMFactory omFactory = OMAbstractFactory.getOMFactory();
		
		OMNamespace soapenvNamespace = omFactory.createOMNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
		OMNamespace wsuNamespace = omFactory.createOMNamespace("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "wsu");

		//Security
		OMElement securityElement = omFactory.createOMElement(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security", "wsse"), null);
		securityElement.addAttribute("mustUnderstand", "1", soapenvNamespace);
		
		//UsernameToken
		OMElement tokenEl = omFactory.createOMElement(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "UsernameToken", "wsse"), null);
		tokenEl.addAttribute("Id", "XWSSGID-1317643090236539015674", wsuNamespace);
		securityElement.addChild(tokenEl);
		
		//Username
		OMElement usernameEl = omFactory.createOMElement(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Username", "wsse"), null);
		usernameEl.setText(username);
		tokenEl.addChild(usernameEl);
		//Password
		OMElement passwordEl = omFactory.createOMElement(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Password", "wsse"), null);
		passwordEl.addAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText", null);
		passwordEl.setText(password);
		tokenEl.addChild(passwordEl);
		//Nonce
		OMElement nonceEl = omFactory.createOMElement(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Nonce", "wsse"), null);
		nonceEl.addAttribute("EncodingType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary", null);

		String uuid = UUID.randomUUID().toString();
		String uuid64 = Base64.encodeBase64String(uuid.getBytes());
		nonceEl.setText(uuid64);
		tokenEl.addChild(nonceEl);
		//Created
		OMElement createdEl = omFactory.createOMElement("Created", wsuNamespace);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.S'Z'");
		String created = format.format(new Date());
		
		createdEl.setText(created);
		tokenEl.addChild(createdEl);
		return securityElement;
	}

}

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
package org.olat.core.util.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.Data;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * 
 * Initial date: 16 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class XMLDigitalSignatureUtil {
	
	private static final Logger log = Tracing.createLoggerFor(XMLDigitalSignatureUtil.class);
	
	/**
	 * Validate a XML file with a XML Digital Signature saved in an external file.
	 * 
	 * 
	 * @param xmlFile
	 * @param xmlSignatureFile
	 * @param publicKey
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws MarshalException
	 * @throws XMLSignatureException
	 */
	public static boolean validate(String uri, File xmlFile, File xmlSignatureFile, PublicKey publicKey)
	throws ParserConfigurationException, SAXException, IOException, MarshalException, XMLSignatureException { 
		Document doc = getDocument(xmlSignatureFile);
		Element signatureEl = getSignatureElement(doc);
		if(signatureEl == null) {
			return false;
		}
        
		DOMValidateContext validContext = new DOMValidateContext(publicKey, signatureEl);
		validContext.setBaseURI(uri);
		validContext.setURIDereferencer(new FileURIDereferencer(uri, xmlFile));
		validContext.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.FALSE);//TODO jee

		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
		XMLSignature signature = fac.unmarshalXMLSignature(validContext);
		boolean validFlag = signature.validate(validContext);
		if(!validFlag) {
            // log and throw if not valid
            boolean sv = signature.getSignatureValue().validate(validContext);
            StringBuilder msg = new StringBuilder();
            msg.append("signature validation status: ").append(sv);
            
            int numOfReferences = signature.getSignedInfo().getReferences().size();
            for (int j=0; j<numOfReferences; j++) {
            	Reference ref = signature.getSignedInfo().getReferences().get(j);
                boolean refValid = ref.validate(validContext);
                msg.append(" ref[").append(j).append("] validity status: ").append(refValid);
            }
            log.warn(msg);
		}
		return validFlag;
	}
	
	/**
	 * 
	 * @param uri
	 * @param xmlFile
	 * @param xmlSignatureFile
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws MarshalException
	 * @throws XMLSignatureException
	 */
	public static boolean validate(String uri, File xmlFile, File xmlSignatureFile)
	throws ParserConfigurationException, SAXException, IOException, MarshalException, XMLSignatureException {  

		Document doc = getDocument(xmlSignatureFile);
        NodeList nl = doc.getElementsByTagName("Signature");
        if (nl.getLength() == 0) {
            return false;
        }
        
		DOMValidateContext validContext = new DOMValidateContext(new X509KeySelector(), nl.item(0));
		validContext.setBaseURI(uri);
		validContext.setURIDereferencer(new FileURIDereferencer(uri, xmlFile));
		validContext.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.FALSE); //TODO jee

		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
		XMLSignature signature = fac.unmarshalXMLSignature(validContext);
		boolean validFlag = signature.validate(validContext);
		if(!validFlag) {
            // log and throw if not valid
            boolean sv = signature.getSignatureValue().validate(validContext);
            String msg = "signature validation status: " + sv;
            
            int numOfReferences = signature.getSignedInfo().getReferences().size();
            for (int j=0; j<numOfReferences; j++) {
            	Reference ref = signature.getSignedInfo().getReferences().get(j);
                boolean refValid = ref.validate(validContext);
                msg += " ref["+j+"] validity status: " + refValid;
            }
            log.warn(msg);
		}
		return validFlag;
	}
	
	public static boolean validate(File signedXmlFile, PublicKey publicKey)
	throws ParserConfigurationException, SAXException, IOException, MarshalException, XMLSignatureException {  

		Document doc = getDocument(signedXmlFile);
		
        NodeList nl = doc.getElementsByTagName("Signature");
        if (nl.getLength() == 0) {
            return false;
        }
        
		DOMValidateContext validContext = new DOMValidateContext(publicKey, nl.item(0));
		validContext.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.FALSE);//TODO jee

		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
		XMLSignature signature = fac.unmarshalXMLSignature(validContext);
		boolean validFlag = signature.validate(validContext);
		if(!validFlag) {
            // log and throw if not valid
            boolean sv = signature.getSignatureValue().validate(validContext);
            String msg = "signature validation status: " + sv;
            
            int numOfReferences = signature.getSignedInfo().getReferences().size();
            for (int j=0; j<numOfReferences; j++) {
            	Reference ref =signature.getSignedInfo().getReferences().get(j);
                boolean refValid = ref.validate(validContext);
                msg += " ref["+j+"] validity status: " + refValid;
            }
            log.warn(msg);
		}
		return validFlag;
	}
	
	private static Element getSignatureElement(Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
        	nl = doc.getElementsByTagName("Signature");
        	if (nl.getLength() == 1) {
        		Element signatureEl = (Element)nl.item(0);
        		try {
					Document signatureDoc = createDocument();
					signatureDoc.appendChild(signatureDoc.importNode(signatureEl, true));
					signatureDoc.renameNode(signatureDoc.getDocumentElement(),XMLSignature.XMLNS,
							signatureDoc.getDocumentElement().getTagName());
	        		return signatureDoc.getDocumentElement();
				} catch (Exception e) {
					log.error("", e);
				}
        		return signatureEl;
        	} else {
        		log.warn("Signature element not found");
        		return null;
        	}
        }
        return (Element)nl.item(0);
	}
	
	/**
	 * Produce a signed a XML file. The signature is added in the XML file.
	 * 
	 * @param xmlFile The original XML file
	 * @param xmlSignedFile The signed XML file
	 * @param x509Cert
	 * @param privateKey
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws NoSuchAlgorithmException
	 * @throws GeneralSecurityException
	 * @throws MarshalException
	 * @throws XMLSignatureException
	 * @throws TransformerException
	 */
	public static void signEmbedded(File xmlFile, File xmlSignedFile, X509Certificate x509Cert, PrivateKey privateKey)
	throws IOException, SAXException, ParserConfigurationException, GeneralSecurityException, MarshalException, XMLSignatureException, TransformerException {

		Document doc = getDocument(xmlFile);

        // Create the signature factory for creating the signature.
        XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM");

        List<Transform> transforms = new ArrayList<>();
        
        Transform envelopped = sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
        transforms.add(envelopped);

        // Create the canonicalization transform to be applied after the XSLT.
        CanonicalizationMethod c14n = sigFactory.newCanonicalizationMethod(
                CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null);
        transforms.add(c14n);

        // Create the Reference to the XML to be signed specifying the hash algorithm to be used
        // and the list of transforms to apply. Also specify the XML to be signed as the current
        // document (specified by the first parameter being an empty string).
        Reference reference = sigFactory.newReference(
        		"",
                sigFactory.newDigestMethod(DigestMethod.SHA256, null),
                transforms,
                null,
                null);

        // Create the Signed Info node of the signature by specifying the canonicalization method
        // to use (INCLUSIVE), the signing method (RSA_SHA1), and the Reference node to be signed.
        SignedInfo si = sigFactory.newSignedInfo(c14n,
                        sigFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                        Collections.singletonList(reference));

        // Create the KeyInfo node containing the public key information to include in the signature.
        KeyInfoFactory kif = sigFactory.getKeyInfoFactory();
        X509Data xd = kif.newX509Data(Collections.singletonList(x509Cert));
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

        // Get the node to attach the signature.
        Node signatureInfoNode = doc.getDocumentElement();
     
        // Create a signing context using the private key.
        DOMSignContext dsc = new DOMSignContext(privateKey, signatureInfoNode);

        // Create the signature from the signing context and key info
        XMLSignature signature = sigFactory.newXMLSignature(si, ki);
        signature.sign(dsc);

        write(doc, xmlSignedFile);
	}

	/**
	 * Create a separate XML file with the XML Digital Signature.
	 * 
	 * of the specified XML file.
	 * @param xmlFile The XML File to sign
	 * @param outputSignatureFile Where the Digital Signature is saved
	 * @param signatureDoc A DOM which hold the signature (optional but if you give one, the root element must exists)
	 * @throws ParserConfigurationException 
	 * @throws GeneralSecurityException 
	 * @throws NoSuchAlgorithmException 
	 * @throws XMLSignatureException 
	 * @throws MarshalException 
	 * @throws TransformerException 
	 */
	public static void signDetached(String uri, File xmlFile, File outputSignatureFile, Document signatureDoc,
			String keyName, X509Certificate x509Cert, PrivateKey privateKey)
	throws IOException, SAXException, ParserConfigurationException, GeneralSecurityException, MarshalException, XMLSignatureException, TransformerException {

		Document doc = getDocument(xmlFile);

        // Create the signature factory for creating the signature.
        XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM");

        List<Transform> transforms = new ArrayList<>();

        // Create the canonicalization transform to be applied after the XSLT.
        CanonicalizationMethod c14n = sigFactory.newCanonicalizationMethod(
                CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null);
        transforms.add(c14n);

        // Create the Reference to the XML to be signed specifying the hash algorithm to be used
        // and the list of transforms to apply. Also specify the XML to be signed as the current
        // document (specified by the first parameter being an empty string).
        Reference reference = sigFactory.newReference(
        		uri,
                sigFactory.newDigestMethod(DigestMethod.SHA256, null),
                transforms,
                null,
                null);

        // Create the Signed Info node of the signature by specifying the canonicalization method
        // to use (INCLUSIVE), the signing method (RSA_SHA1), and the Reference node to be signed.
        SignedInfo si = sigFactory.newSignedInfo(c14n,
                        sigFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                        Collections.singletonList(reference));

        // Create the KeyInfo node containing the public key information to include in the signature.
        KeyInfoFactory kif = sigFactory.getKeyInfoFactory();
        X509Data xd = kif.newX509Data(Collections.singletonList(x509Cert));
        
        List<XMLStructure> keyInfoList = new ArrayList<>();
        if(StringHelper.containsNonWhitespace(keyName)) {
            keyInfoList.add(kif.newKeyName(keyName));
        }
        keyInfoList.add(xd);
        KeyInfo ki = kif.newKeyInfo(keyInfoList);

        // Get the node to attach the signature.
        Node signatureInfoNode = doc.getDocumentElement();
     
        // Create a signing context using the private key.
        DOMSignContext dsc = new DOMSignContext(privateKey, signatureInfoNode);
        dsc.setBaseURI(uri);
		dsc.setURIDereferencer(new FileURIDereferencer(uri, xmlFile));
		
        // Create the signature from the signing context and key info
        XMLSignature signature = sigFactory.newXMLSignature(si, ki);
        signature.sign(dsc);

        NodeList nl = doc.getElementsByTagName("Signature");
        if (nl.getLength() == 1) {
        	Element signatureEl = (Element)nl.item(0);
        	if(signatureDoc != null && signatureDoc.getDocumentElement() != null) {
        		Element rootEl = signatureDoc.getDocumentElement();
        		rootEl.appendChild(signatureDoc.importNode(signatureEl, true));
        		write(rootEl, outputSignatureFile);
        	} else {
        		signatureDoc = createDocument();
        		signatureDoc.appendChild(signatureDoc.importNode(signatureEl, true));
        		write(signatureDoc, outputSignatureFile);
        	}
        }
	}
	
	private static void write(Node node, File outputFile)
	throws IOException, TransformerException, TransformerFactoryConfigurationError {
        try (Writer ssw = new FileWriter(outputFile)) {
			TransformerFactory tf = XMLFactories.newTransformerFactory();
			Transformer trans = tf.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.transform(new DOMSource(node), new StreamResult(ssw));
		} catch (TransformerException | TransformerFactoryConfigurationError | IllegalArgumentException | IOException e) {
			throw e;
		}
	}
	
	public static Document getDocument(File xmlFile)
	throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = XMLFactories.newDocumentBuilderFactory();
		dbFactory.setNamespaceAware(true);
		return dbFactory.newDocumentBuilder().parse(xmlFile);
	}
	
	public static Document createDocument()
	throws ParserConfigurationException {
		DocumentBuilderFactory dbFactory = XMLFactories.newDocumentBuilderFactory();
		dbFactory.setNamespaceAware(true);
		return dbFactory.newDocumentBuilder().newDocument();
	}
	
	public static String getReferenceURI(Document doc) {
		NodeList nl = doc.getElementsByTagName("Reference");
        for(int i=nl.getLength(); i-->0; ) {
        	Element referenceEl = (Element)nl.item(i);
        	Node uriNode = referenceEl.getAttributes().getNamedItem("URI");
        	if(uriNode != null) {
				return uriNode.getNodeValue();
        	}
        }
        return null;
	}
	
	public static String getKeyName(Document doc) {
		NodeList nl = doc.getElementsByTagName("KeyName");
        if(nl.getLength() == 1) {
        	return getElementText(doc, "KeyName");
        }
		return null;
	}
	
	public  static String getElementText(Document doc, String elementName) {
		StringBuilder sb = new StringBuilder();
		
		if(doc != null) {
			NodeList nl = doc.getElementsByTagName(elementName);
			if(nl.getLength() == 1) {
				Node element = nl.item(0);
				for(Node child=element.getFirstChild(); child != null; child = child.getNextSibling()) {
					if(child instanceof Text) {
						Text text = (Text)child;
						sb.append(text.getTextContent());	
					}
				}
			}
		}
		
		return sb.toString();
	}
	
	private static class FileURIDereferencer implements URIDereferencer {
		
		private final String uri;
		private final File xmlFile;
		
		public FileURIDereferencer(String uri, File xmlFile) {
			this.uri = uri;
			this.xmlFile = xmlFile;
		}
		
		@Override
		public Data dereference(URIReference uriReference, XMLCryptoContext context) throws URIReferenceException {
			try {
				if(uri.equals(uriReference.getURI())) {
					byte[] bytes = Files.readAllBytes(xmlFile.toPath());
					ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
					return new OctetStreamData(inputStream);
				}
				return null;
			} catch (Exception e) {
				throw new URIReferenceException(e);
			}
		}
	}
	
    private static class X509KeySelector extends KeySelector {
        
    	@Override
    	public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method, XMLCryptoContext context)
        throws KeySelectorException {
  
    			Iterator<?> ki = keyInfo.getContent().iterator();
            while (ki.hasNext()) {
                XMLStructure info = (XMLStructure) ki.next();
                if (!(info instanceof X509Data)) {
                    continue;
                }
                X509Data x509Data = (X509Data) info;
				Iterator<?> xi = x509Data.getContent().iterator();
                while (xi.hasNext()) {
                    Object o = xi.next();
                    if (!(o instanceof X509Certificate)) {
                        continue;
                    }
                    final PublicKey key = ((X509Certificate)o).getPublicKey();

                    if (algEquals(method.getAlgorithm(), key.getAlgorithm())) {
                        return new KeySelectorResult() {
                            public Key getKey() { return key; }
                        };
                    }
                }
            }
            throw new KeySelectorException("No key found!");
        }

        static boolean algEquals(String algURI, String algName) {
            if ((algName.equalsIgnoreCase("DSA") &&
                    algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) ||
                    (algName.equalsIgnoreCase("RSA") &&
                            algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1))) {
                return true;
            } else {
                return false;
            }
        }
    }
}

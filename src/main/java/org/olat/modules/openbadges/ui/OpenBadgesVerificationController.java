package org.olat.modules.openbadges.ui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Base64;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.crypto.CryptoUtil;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.modules.openbadges.BadgeVerification;
import org.olat.modules.openbadges.OpenBadgesBakeContext;
import org.olat.modules.openbadges.manager.OpenBadgesManagerImpl;
import org.olat.modules.openbadges.v2.Assertion;
import org.olat.modules.openbadges.v2.Badge;
import org.olat.modules.openbadges.v2.CryptographicKey;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Controller for OpenBadges verification functionality
 */
public class OpenBadgesVerificationController extends FormBasicController {

    private FormSubmit verifyButton;
    private FileElement fileUpload;
    private StaticTextElement reportEl;
    
    
    @Autowired
    private HttpClientService httpClientService;
    
    public OpenBadgesVerificationController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        
        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        fileUpload = uifactory.addFileElement(getWindowControl(), getIdentity(), "badge.file.upload", formLayout);
        fileUpload.addActionListener(FormEvent.ONCHANGE);
        fileUpload.setMandatory(true);

        reportEl = uifactory.addStaticTextElement("verification.report", "verification.report", formLayout);
        reportEl.setVisible(false);

        FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
        formLayout.add(buttonLayout);
        
        verifyButton = uifactory.addFormSubmitButton("verify", buttonLayout);
        
    }

    @Override
    protected void formOK(UserRequest ureq) {
        if (fileUpload.isUploadSuccess()) {
            generateReport(ureq);
            String fileName = fileUpload.getUploadFileName();
            String mimeType = fileUpload.getUploadMimeType();
            long size = fileUpload.getUploadSize();
            reportEl.setVisible(true);
        } else {
            showError("badge.file.upload.failed");
            reportEl.setVisible(false);
        }
    }

    private void generateReport(UserRequest ureq) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("File name: ").append(fileUpload.getUploadFileName()).append("<br>");
        sb.append("Mime type: ").append(fileUpload.getUploadMimeType()).append("<br>");
        sb.append("Size: ").append(fileUpload.getUploadSize()).append("<br>");
        String mimeType = fileUpload.getUploadMimeType();
        
        if (mimeType.equalsIgnoreCase("image/png")) {
            sb.append("Image type: PNG").append("<br>");
            generatePngReport(sb);
        } else if (mimeType.equalsIgnoreCase("image/svg+xml")) {
            sb.append("Image type: SVG").append("<br>");
            generateSvgReport(sb);
        } else {
            sb.append("Image type: UNKNOWN").append("<br>");
            reportEl.setValue(sb.toString());
            return;
        }
        
        reportEl.setValue(sb.toString());
    }

    private void generatePngReport(StringBuilder sb) {
        InputStream is = fileUpload.getUploadInputStream();
        NamedNodeMap attributes = OpenBadgesManagerImpl.findOpenBadgesTextChunk(is);
        sb.append("PNG type: ");
        if (attributes == null) {
            sb.append("Not a badge.").append("<br>");
            return;
        } else {
            BadgeVerification guessedVerification = OpenBadgesBakeContext.getVerification(attributes);
            sb.append("Badge with verification type <strong>").append(guessedVerification.name()).append("</strong><br>");
            OpenBadgesBakeContext bakeContext = new OpenBadgesBakeContext(attributes, guessedVerification);
            switch (guessedVerification) {
                case hosted:
                    generateHostedPngReport(bakeContext, sb);
                    break;
                case signed:
                    generateSignedPngReport(bakeContext, sb);
                    break;
            }
        }
    }
    private void generateHostedSvgReport(String text, StringBuilder sb) {
        JSONObject jsonObject = new JSONObject(text);
        Assertion assertion = new Assertion(jsonObject);
        generateReport(assertion, sb);
    }

    private void generateHostedPngReport(OpenBadgesBakeContext bakeContext, StringBuilder sb) {
        Assertion assertion = bakeContext.getTextAsAssertion();
        generateReport(assertion, sb);
    }

    private void generateReport(Assertion assertion, StringBuilder sb) {
        sb.append("Assertion:").append("<br>");
        sb.append("&bull; id: ").append(assertion.getId()).append("<br>");
        sb.append("&bull; issuedOn: ").append(assertion.getIssuedOn()).append("<br>");
        sb.append("&bull; verification: ").append(assertion.getVerification().getType()).append("<br>");
        Badge badge = assertion.getBadge();
        generateReport(badge, sb);
    }

    private void generateReport(Badge badge, StringBuilder sb) {
        sb.append("Badge:").append("<br>");
        sb.append("&bull; id: ").append(badge.getId()).append("<br>");
        sb.append("&bull; name: ").append(badge.getName()).append("<br>");
    }

    private void generateSignedPngReport(OpenBadgesBakeContext bakeContext, StringBuilder sb) {
        String text = bakeContext.getText();
        if (text == null) {
            return;
        }
        generateSignedReport(text, sb);
    }

    private void generateSignedReport(String text, StringBuilder sb) {
        String[] parts = text.split("\\.");
        if (parts.length != 3) {
            return;
        }

        JSONObject header = new JSONObject(new String(Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8));
        JSONObject payload = new JSONObject(new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8));

        Assertion assertion = new Assertion(payload);
        String publicKeyUrl = assertion.getVerification().getCreator();

        sb.append("Signature:").append("<br>");
        sb.append("&bull; header.alg: ").append(header.getString("alg")).append("<br>");
        sb.append("&bull; publicKeyUrl: ").append(publicKeyUrl).append("<br>");
        CryptographicKey cryptographicKey = loadPublicKey(publicKeyUrl, sb);
        if (cryptographicKey != null) {
            PublicKey publicKey = CryptoUtil.string2PublicKey(cryptographicKey.getPublicKeyPem());
            if (publicKey instanceof RSAPublicKey rsaPublicKey) {
                RSASSAVerifier verifier = new RSASSAVerifier(rsaPublicKey);
                try {
                    SignedJWT signedJWT = SignedJWT.parse(text);
                    boolean success = signedJWT.verify(verifier);
                    sb.append("&bull; verifyResult: ").append(success ? "success" : "failure").append("<br>");
                } catch (ParseException | JOSEException e) {
                    sb.append("&bull; verifyResult: ").append(e.getMessage()).append("<br>");
                }
            }
        }
        generateReport(assertion, sb);
    }
    
    private CryptographicKey loadPublicKey(String publicKeyUrl, StringBuilder sb) {
        HttpGet httpGet = new HttpGet(publicKeyUrl);
        try (CloseableHttpClient httpClient = httpClientService.createHttpClient();
             CloseableHttpResponse response = httpClient.execute(httpGet)) {
            int status = response.getStatusLine().getStatusCode();
            if (status < 200 || status > 299) {
                sb.append("&bull; publicKeyString: invalid HTTP status ").append(status).append("<br>");
                return null;
            }
            HttpEntity entity = response.getEntity();
            String jsonString = EntityUtils.toString(entity);
            JSONObject jsonObject = new JSONObject(jsonString);
            return new CryptographicKey(jsonObject);
        } catch (IOException e) {
            return null;
        }
    }
    
    private void generateSvgReport(StringBuilder sb) {
		try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String xPathString = "//svg";
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(fileUpload.getUploadInputStream()));
            NodeList nodeList = (NodeList) xPath.compile(xPathString).evaluate(document, XPathConstants.NODESET);
            if (nodeList.getLength() != 1) {
                throw new IllegalStateException("Node list does not have length 1");
            }
            Node firstNonTextNode = findFirstNonTextNode(nodeList.item(0));
            if (firstNonTextNode == null || !"openbadges:assertion".equals(firstNonTextNode.getNodeName())) {
                throw new IllegalStateException("Node list does not have non-empty node");
            }
            Element openBadgeAssertionElement = (Element) firstNonTextNode;
            String verify = openBadgeAssertionElement.getAttribute("verify");
            if (firstNonTextNode.getChildNodes().getLength() == 0) {
                generateSignedReport(verify, sb);
            } else {
                if (findFirstNonTextNode(firstNonTextNode) instanceof CDATASection cdataSection) {
                    generateHostedSvgReport(cdataSection.getData(), sb);
                }
            }
        } catch (Exception e) {
			sb.append("SVG file error: ").append(e.getMessage()).append("<br>");
		}
	}

    private Node findFirstNonTextNode(Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.TEXT_NODE) {
                return childNode;
            }
        }
        return null;
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean allOk = super.validateFormLogic(ureq);
        
        if (!fileUpload.isUploadSuccess()) {
            fileUpload.setErrorKey("badge.file.upload.mandatory", null);
            allOk = false;
        }
        
        return allOk;
    }
}
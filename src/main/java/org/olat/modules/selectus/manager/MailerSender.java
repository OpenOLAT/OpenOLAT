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
package org.olat.modules.selectus.manager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.jcajce.provider.keystore.pkcs12.PKCS12KeyStoreSpi;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.Store;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.pdf.PdfOutputOptions;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.render.velocity.VelocityFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.filter.impl.HtmlFilter;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.manager.MailManagerImpl;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.mail.MailAttachment;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

/**
 * Helper which send e-mails. The from is always "mailFrom", adminemail property. There is no footer.
 * 
 * Initial date: 23.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MailerSender {
	
	private static final Logger log = Tracing.createLoggerFor(MailerSender.class);
	private static final VelocityEngine velocityEngine;
	
	static {
		velocityEngine = VelocityFactory.createNoIntrospectEngine();
	}
	
	private final BouncyCastleProvider bcProvider;
	private final PKCS12KeyStoreSpi.BCPKCS12KeyStore keyStore;
	
	public MailerSender(BouncyCastleProvider bcProvider, PKCS12KeyStoreSpi.BCPKCS12KeyStore keyStore) {
		this.bcProvider = bcProvider;
		this.keyStore = keyStore;
	}
	
	public boolean checkTemplate(String template) {
		VelocityContext context = new VelocityContext();
		return checkTemplate(context, template);
	}
	
	public boolean checkTemplate(Context context, String template) {
		StringWriter subjectWriter = new StringWriter();
		MailerResult mailerResult = new MailerResult();
		evaluate(context, template, subjectWriter, mailerResult);
		return mailerResult.getReturnCode() == MailerResult.OK;
	}
	
	public String renderTemplate(Context context, String template) {
		try(StringWriter subjectWriter = new StringWriter()) {
			MailerResult mailerResult = new MailerResult();
			evaluate(context, template, subjectWriter, mailerResult);
			return subjectWriter.toString();
		} catch(IOException e) {
			log.error("", e);
			return template;
		}
	}
	
	/**
	 * Send an email. The from address is defined as mailFrom (the property adminemail)
	 * @param to
	 * @param app
	 * @param position
	 * @param template
	 * @param mailerResult
	 */
	public SubjectAndBody send(String to, String bcc, ApplicationShort app, List<? extends ApplicationShort> appList, Position position,
			Reference reference, Identity member, List<ApplicationFeedback> feedbacks, ApplicationsFeedbackConfiguration feedbackConfig,
			ApplicationMailTemplate template, MailerResult mailerResult) {
		SubjectAndBody subjectBody = null;
		try {
			subjectBody = createWithContext(app, appList, reference, member, feedbacks, feedbackConfig, position, template, mailerResult);
			// use staff mail from position (depends on org unit) as reply-to mail address
			OrganisationUnit organisationSettings = CoreSpringFactory.getImpl(RecruitingService.class).getOrganisationUnit(position);
			String staffMail = CoreSpringFactory.getImpl(RecruitingModule.class).getStaffMail(position, organisationSettings);
			send(staffMail, to, bcc, subjectBody.getSubject(), subjectBody.getBody(), subjectBody.getLetter(), mailerResult);
		} catch(Exception e) {
			log.error("", e);
			appendError(mailerResult);
		}
		return subjectBody;
	}
	
	private void appendError(MailerResult mailerResult) {
		if (mailerResult.getReturnCode() == MailerResult.OK) {
			mailerResult.setReturnCode(MailerResult.SEND_GENERAL_ERROR);
		}
	}
	
	public static Address[] parseAddress(String addresses) throws AddressException {
		List<Address> addressList = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(addresses)) {
			String[] addresseArr = addresses.split("[;,]");
			for(String address:addresseArr) {
				if(StringHelper.containsNonWhitespace(address)) {
					addressList.add(new InternetAddress(address.trim()));
				}
			}
		}
		return addressList.toArray(new Address[addressList.size()]);
	}
	
	protected void send(String from, String to, String bcc, String subject, String body, MailAttachment attachment, MailerResult mailerResult) {
		try {
			Address addressTo = new InternetAddress(to, null);
			Address[] addressBccs = null;
			
			Address addressFrom = null;
			Address[] addressReplyTo = null;
			if(isFromDomain(from)) {
				addressFrom = new InternetAddress(from, null);
			} else {
				String instanceFrom = WebappHelper.getMailConfig("mailFrom");
				String instanceFromName = WebappHelper.getMailConfig("mailFromName");
				if(!StringHelper.containsNonWhitespace(instanceFromName)) {
					instanceFromName = null;
				}
				addressFrom = createAddressWithName(instanceFrom, instanceFromName);
				if (StringHelper.containsNonWhitespace(from)) {
					addressReplyTo = new Address[] {new InternetAddress(from, null)};
				}
			}

			if(StringHelper.containsNonWhitespace(bcc)) {
				addressBccs = parseAddress(bcc);
			}
			
			boolean sign = "enable".equals(WebappHelper.getMailConfig("mailFromCertificate"));
			MimeMessage message;
			if(sign) {
				MailerResult signedResult = new  MailerResult();
				message = createSignedMessage(addressFrom, addressTo, addressBccs, addressReplyTo, subject, body, signedResult);
				if(message == null) {
					message = createMessage(addressFrom, addressTo, addressBccs, addressReplyTo, subject, body, attachment, mailerResult);
				} else if(mailerResult != null) {
					mailerResult.append(signedResult);
				}
			} else {
				message = createMessage(addressFrom, addressTo, addressBccs, addressReplyTo, subject, body, attachment, mailerResult);
			}
			
			if(message != null && (mailerResult == null || mailerResult.getReturnCode() == MailerResult.OK)) {
				// send mail
				CoreSpringFactory.getImpl(MailManager.class).sendMessage(message, mailerResult);
			}
		} catch (UnsupportedEncodingException | AddressException e) {
			log.error("", e);
			appendError(mailerResult);
		}
	}
	
	private boolean isFromDomain(String from) {
		String mailFrom = WebappHelper.getMailConfig("mailFromDomain");
		return (from != null && mailFrom != null && from.endsWith(mailFrom));
	}
	
	private Address createAddressWithName(String address, String name) throws UnsupportedEncodingException, AddressException {
		InternetAddress add = new InternetAddress(address, name);
		try {
			add.validate();
		} catch (AddressException e) {
			throw e;
		}
		return add;
	}

	public SubjectAndBody createWithContext(ApplicationShort app, List<? extends ApplicationShort> appList, Reference reference,
			Identity member, List<ApplicationFeedback> feedbacks, ApplicationsFeedbackConfiguration feedbackConfiguration,
			Position position, ApplicationMailTemplate template, MailerResult mailerResult) {
		// prepare cc addresses - will stay the same for each mail
		try {
			VelocityContext context = template.getContext();
			template.putVariablesInMailContext(context, app, appList, reference, member, feedbacks, feedbackConfiguration, position);
			
			StringWriter subjectWriter = new StringWriter();
			evaluate(context, template.getSubjectTemplate(), subjectWriter, mailerResult);
			// merge body template with context variables
			StringWriter bodyWriter = new StringWriter();
			evaluate(context, template.getBodyTemplate(), bodyWriter, mailerResult);
			// merge letter template
			StringWriter attachmentWriter = new StringWriter();
			if(template.getLetterTemplate() != null && StringHelper.containsNonWhitespace(template.getLetterTemplate().getContentToPdf())) {
				evaluate(context, template.getLetterTemplate().getContentToPdf(), attachmentWriter, mailerResult);
			}

			// check for errors - exit
			if (mailerResult != null && mailerResult.getReturnCode() != MailerResult.OK) {
				return null;
			}

			String subject = subjectWriter.toString();
			String body = bodyWriter.toString();
			String evaluatedAttachment = attachmentWriter.toString();
			MailAttachment attachment = null;
			if(StringHelper.containsNonWhitespace(evaluatedAttachment)) {
				attachment = new MailAttachment();
				attachment.setContentToPdf(evaluatedAttachment);
				if(app != null) {
					attachment.setFilename(letterFilename(template.getLetterTemplate().getFilename(), app));
				} else {
					attachment.setFilename(template.getLetterTemplate().getFilename());
				}
				attachment.setMimeType(template.getLetterTemplate().getMimeType());
			}
			return new SubjectAndBody(subject, body, attachment);
		} catch (Exception e) {
			log.error("", e);
			appendError(mailerResult);
			return null;
		}
	}
	
	public static String letterFilename(String filename, ApplicationShort app) {
		StringBuilder sb = new StringBuilder(64);
		if(app != null && app.getPerson() != null) {
			if(StringHelper.containsNonWhitespace(app.getPerson().getLastName())) {
				sb.append(app.getPerson().getLastName());
			}
			
			if(StringHelper.containsNonWhitespace(app.getPerson().getFirstName())) {
				if(sb.length() > 0) {
					sb.append("_");
				}
				sb.append(app.getPerson().getFirstName());
			}
		}
		
		if(filename.startsWith(sb.toString())) {
			sb = new StringBuilder(64);
		}

		if(StringHelper.containsNonWhitespace(filename)) {
			if(sb.length() > 0) {
				sb.append("_");
			}
			sb.append(filename);
			
			if(!filename.endsWith(".pdf")) {
				sb.append(".pdf");
			}
		} else {
			sb.append("Letter.pdf");
		}
		return sb.toString();
	} 
	
	private MimeMessage createMessage(Address from, Address recipient, Address[] bccs, Address[] replyTo, String subject, String body,
			MailAttachment attachment, MailerResult result) {
		MimeMessage msg = null;
		try {
			msg = CoreSpringFactory.getImpl(MailManagerImpl.class).createMessage(subject, from);
			msg.setFrom(from);
			msg.setReplyTo(replyTo);				
			msg.setRecipient(Message.RecipientType.TO, recipient);
			if(bccs != null && bccs.length > 0) {
				msg.setRecipients(Message.RecipientType.BCC, bccs);
			}
			msg.setSubject(subject, "utf-8");

			if(StringHelper.isHtml(body) || attachment != null) {
				if(!convertAttachmentToPdf(attachment)) {
					result.setReturnCode(MailerResult.ATTACHMENT_INVALID);
				}
				Multipart alternativePart = createMultipartAlternative(body, attachment);
				msg.setContent(alternativePart);
			} else {
				msg.setText(body, "utf-8");
			}

			msg.saveChanges();
		} catch(Exception e) {
			result.setReturnCode(MailerResult.SEND_GENERAL_ERROR);
			log.error("Could not create MimeMessage", e);
		}
		return msg;
	}
	
	/**
	 * @param attachment The attachment definition
	 * @return true if all is ok, false if an error occured
	 */
	private boolean convertAttachmentToPdf(MailAttachment attachment) {
		if(attachment == null || !StringHelper.containsNonWhitespace(attachment.getContentToPdf())) return true; // Nothing to do
		
		try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			CoreSpringFactory.getImpl(PdfService.class).convert(attachment.getContentToPdf(), PdfOutputOptions.defaultOptions(), out);
			byte[] content = out.toByteArray();
			attachment.setContent(content);
			attachment.setMimeType("application/pdf");
			return content != null && content.length > 512;
		} catch(Exception e) {
			log.error("Cannot convert letter to PDF", e);
			return false;
		}
	}
	
	private MimeMessage createSignedMessage(Address from, Address recipient, Address[] bccs, Address[] replyTo, String subject, String body, MailerResult result) {
		MimeMessage msg = null;
		String password = WebappHelper.getMailConfig("mailFromCertificatePassword");
		String certificatePath = WebappHelper.getMailConfig("mailFromCertificatePath");
		
		try(InputStream certificateIn = new FileInputStream(new File(certificatePath));) {
			
			keyStore.engineLoad(certificateIn, password.toCharArray());

			Enumeration<String> e = keyStore.engineAliases();
			String keyAlias = null;
			while (e.hasMoreElements()) {
				String alias = e.nextElement();
				if (keyStore.engineIsKeyEntry(alias)) {
					keyAlias = alias;
				}
			}

			Certificate[] chain = keyStore.engineGetCertificateChain(keyAlias);
			List<X509Certificate> certList = new ArrayList<>();
			X509Certificate origCert = (X509Certificate) chain[0];
			String signDN = origCert.getIssuerDN().getName();

			KeyPair origKP = getPrivateKey(keyStore, keyAlias, password.toCharArray());
			certList.add(origCert);
			appendIntermediateCertificates(certList);
			
			Store<?> certs = new JcaCertStore(certList);
			// create some smime capabilities in case someone wants to respond
			ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
			SMIMECapabilityVector caps = new SMIMECapabilityVector();
			caps.addCapability(SMIMECapability.dES_EDE3_CBC);
			caps.addCapability(SMIMECapability.rC2_CBC, 128);
			caps.addCapability(SMIMECapability.dES_CBC);

			signedAttrs.add(new SMIMECapabilitiesAttribute(caps));

			//
			// add an encryption key preference for encrypted responses -
			// normally this would be different from the signing certificate...
			//
			IssuerAndSerialNumber issAndSer = new IssuerAndSerialNumber(
					new X500Name(signDN), origCert.getSerialNumber());
			signedAttrs
					.add(new SMIMEEncryptionKeyPreferenceAttribute(issAndSer));
			SMIMESignedGenerator gen = new SMIMESignedGenerator();

			//
			// add a signer to the generator - this specifies we are using SHA1
			// and
			// adding the smime attributes above to the signed attributes that
			// will be generated as part of the signature. The encryption
			// algorithm
			// used is taken from the key - in this RSA with PKCS1Padding
			//
			gen.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder()
					.setProvider(bcProvider)
					.setSignedAttributeGenerator(new AttributeTable(signedAttrs))
					.build("SHA1withRSA", origKP.getPrivate(), origCert));

			//
			// add our pool of certs and cerls (if any) to go with the signature
			//
			gen.addCertificates(certs);

			MimeMultipart mm;
			if(StringHelper.isHtml(body)) {
				Multipart alternativePart = createMultipartAlternative(body, null);
				MimeBodyPart wrap = new MimeBodyPart();
				wrap.setContent(alternativePart);
				mm = gen.generate(wrap);
			} else {
				MimeBodyPart bodyPart = new MimeBodyPart();
				bodyPart.setText(body, "utf-8");
				mm = gen.generate(bodyPart);
			}

			msg = CoreSpringFactory.getImpl(MailManagerImpl.class).createMessage(subject, from);
			msg.setFrom(from);
			msg.setReplyTo(replyTo);				
			msg.setRecipient(Message.RecipientType.TO, recipient);
			if(bccs != null && bccs.length > 0) {
				msg.setRecipients(Message.RecipientType.BCC, bccs);
			}
			msg.setSubject(subject, "utf-8");
			msg.setContent(mm, mm.getContentType());
			msg.saveChanges();
		} catch (Exception e) {
			result.setReturnCode(MailerResult.SEND_GENERAL_ERROR);
			log.error("Could not create MimeMessage", e);
			msg = null;
		}
		return msg;
	}
	
	private Multipart createMultipartAlternative(String text, MailAttachment attachment)
	throws MessagingException {
		MimeMultipart multipart;
		if(attachment != null) {
			multipart = new MimeMultipart("mixed");
			
			if(StringHelper.isHtml(text)) {
				Multipart alternativePart = appendNewChild(multipart, "alternative");
				appendHtmlMultipartAlternative(text, alternativePart);
			} else {
				appendTextMultipartAlternative(text, multipart);
			}

			BodyPart filePart = new MimeBodyPart();
			DataSource source = new ByteArrayDataSource(attachment.getContent(), attachment.getMimeType());
			filePart.setDataHandler(new DataHandler(source));
			filePart.setFileName(attachment.getFilename());
			multipart.addBodyPart(filePart);
			
		} else {
			multipart = new MimeMultipart("alternative");
			if(StringHelper.isHtml(text)) {
				appendHtmlMultipartAlternative(text, multipart);
			} else {
				appendTextMultipartAlternative(text, multipart);
			}
		}

		return multipart;
	}
	
	private Multipart appendNewChild(Multipart parent, String alternative) throws MessagingException {
		MimeMultipart child =  new MimeMultipart(alternative);
		final MimeBodyPart mbp = new MimeBodyPart();
		parent.addBodyPart(mbp);
		mbp.setContent(child);
		return child;
	}
	
	private void appendTextMultipartAlternative(String text, Multipart multipart)
	throws MessagingException {
		MimeBodyPart textPart = new MimeBodyPart();
		textPart.setText(text, "utf-8");
		multipart.addBodyPart(textPart);
	}
	
	private void appendHtmlMultipartAlternative(String text, Multipart multipart)
	throws MessagingException {
		String pureText = new HtmlFilter().filter(text, true);
		MimeBodyPart textPart = new MimeBodyPart();
		textPart.setText(pureText, "utf-8");

		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setText(text, "utf-8", "html");
		
		multipart.addBodyPart(textPart);
		multipart.addBodyPart(htmlPart);
	}
	
	private void appendIntermediateCertificates(List<X509Certificate> certList) {
		String certificateIntermediate1Path = WebappHelper.getMailConfig("mailFromIntermediateCertificate1Path");
		appendIntermediateCertificates(certificateIntermediate1Path, certList);
		String certificateIntermediate2Path = WebappHelper.getMailConfig("mailFromIntermediateCertificate2Path");
		appendIntermediateCertificates(certificateIntermediate2Path, certList);
		String certificateIntermediate3Path = WebappHelper.getMailConfig("mailFromIntermediateCertificate3Path");
		appendIntermediateCertificates(certificateIntermediate3Path, certList);
	}
	
	private void appendIntermediateCertificates(String path, List<X509Certificate> certList) {
		if(StringHelper.containsNonWhitespace(path)) {
			File certificateFile = new File(path);
			if(certificateFile.exists()) {
				X509Certificate certificate = loadCertificate(certificateFile);
				certList.add(certificate);
			}
		}
	}
	
	public X509Certificate loadCertificate(File file) {
		try(Reader reader = new FileReader(file);
			PEMParser parser = new PEMParser(reader)) {
			
			X509CertificateHolder certificateHolder = (X509CertificateHolder)parser.readObject();
			return new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(certificateHolder);
		} catch (Exception e) {
			log.error("Could not load certificate:{}", file, e);
			return null;
		}
	}
	
	private static KeyPair getPrivateKey(PKCS12KeyStoreSpi.BCPKCS12KeyStore keystore, String alias, char[] password) {
		try {
			Key key = keystore.engineGetKey(alias, password);
			if (key instanceof PrivateKey) {
				java.security.cert.Certificate cert = keystore
						.engineGetCertificate(alias);
				PublicKey publicKey = cert.getPublicKey();
				return new KeyPair(publicKey, (PrivateKey) key);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}
		
	private void evaluate(Context context, String template, StringWriter writer, MailerResult mailerResult) {
		try {
			boolean result = velocityEngine.evaluate(context, writer, "mailTemplate", template);
			if (result) {
				mailerResult.setReturnCode(MailerResult.OK);
			} else {
				log.warn("can't send email from user template with no reason");
				mailerResult.setReturnCode(MailerResult.TEMPLATE_GENERAL_ERROR);
			}
		} catch (Exception e) {
			log.warn("can't send email from user template", e);
			mailerResult.setReturnCode(MailerResult.TEMPLATE_GENERAL_ERROR);
		}
	}
}

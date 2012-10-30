package org.olat.core.util.mail;

/**
 * A mail package is the sum of the template, the context and it's result.
 * All or part of thesse can be null.
 * 
 * 
 * @author srosse
 *
 */
public class MailPackage {
	
	private final boolean sendEmail;
	private final MailTemplate template;
	private final MailContext context;
	private final MailerResult result;
	
	public MailPackage() {
		this.sendEmail = true;
		this.template = null;
		this.context = null;
		this.result = new MailerResult();
	}
	
	public MailPackage(boolean sendMail) {
		this.sendEmail = sendMail;
		this.template = null;
		this.context = null;
		this.result = new MailerResult();
	}
	
	public MailPackage(MailTemplate template, MailContext context) {
		this.sendEmail = true;
		this.template = template;
		this.context = context;
		this.result = new MailerResult();
	}
	
	public MailPackage(MailTemplate template, String businessPath, boolean sendMail) {
		this.sendEmail = true;
		this.template = template;
		this.context = new MailContextImpl(null, null, businessPath);
		this.result = new MailerResult();
	}
	
	public MailPackage(MailerResult result, String businessPath, boolean sendMail) {
		this.sendEmail = true;
		this.template = null;
		this.context = new MailContextImpl(null, null, businessPath);
		this.result = result;
	}
	
	public MailPackage(MailTemplate template, MailerResult result, String businessPath, boolean sendMail) {
		this.sendEmail = true;
		this.template = template;
		this.context = new MailContextImpl(null, null, businessPath);
		this.result = result;
	}


	/**
	 * Default is true, you want to send mails. But in rare case, this flag
	 * give you the possibility to skip the mails.
	 * @return
	 */
	public boolean isSendEmail() {
		return sendEmail;
	}

	public MailTemplate getTemplate() {
		return template;
	}
	
	public MailContext getContext() {
		return context;
	}
	
	public MailerResult getResult() {
		return result;
	}

	
	public void appendResult(MailerResult result) {
		
	}
}

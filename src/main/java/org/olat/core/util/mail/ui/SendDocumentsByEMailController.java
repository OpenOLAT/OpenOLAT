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
package org.olat.core.util.mail.ui;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FileSelection;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.commands.CmdSendMail;
import org.olat.core.commons.modules.bc.commands.FolderCommand;
import org.olat.core.commons.modules.bc.commands.FolderCommandHelper;
import org.olat.core.commons.modules.bc.commands.FolderCommandStatus;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.MetaInfoFormController;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.textboxlist.ResultMapProvider;
import org.olat.core.gui.components.textboxlist.TextBoxListComponent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.core.gui.control.generic.folder.FolderHelper;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * <p>
 * Initial Date: 7 feb. 2011 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class SendDocumentsByEMailController extends FormBasicController implements CmdSendMail {

	private TextElement bodyElement;
	private TextElement subjectElement;
	private TextBoxListElement userListBox;
	private FormLayoutContainer attachmentsLayout;
	private final DecimalFormat formatMb = new DecimalFormat("0.00");

	private int status = FolderCommandStatus.STATUS_SUCCESS;
	private List<VFSLeaf> files;
	private FileSelection selection;
	private List<File> attachments;

	private final BaseSecurity securityManager;
	private final boolean allowAttachments;
	private static final int MAX_RESULTS_USERS = 12;

	public SendDocumentsByEMailController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, null, Util.createPackageTranslator(MetaInfoFormController.class, ureq.getLocale(),
				Util.createPackageTranslator(MailModule.class, ureq.getLocale())));
		setBasePackage(MailModule.class);

		securityManager = BaseSecurityManager.getInstance();
		allowAttachments = !FolderConfig.getSendDocumentLinkOnly();

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("send.mail.title");
		setFormDescription("send.mail.description");
		setFormStyle("b_send_documents");

		int emailCols = 25;
		boolean allowExtern = FolderConfig.getSendDocumentToExtern();

		userListBox = uifactory.addTextBoxListElement("send.mail.to.auto", "send.mail.to", "send.mail.to", null, formLayout, getTranslator());
		userListBox.setMapperProvider(new UserListProvider());
		userListBox.setAllowNewValues(allowExtern);
		userListBox.setAllowDuplicates(false);
		userListBox.setMaxResults(MAX_RESULTS_USERS + 2);

		subjectElement = uifactory.addTextElement("tsubject", "send.mail.subject", 255, "", formLayout);

		bodyElement = uifactory.addTextAreaElement("tbody", "send.mail.body", -1, 20, emailCols, false, "", formLayout);

		if (allowAttachments) {
			String page = Util.getPackageVelocityRoot(MailModule.class) + "/sendattachments.html";
			attachmentsLayout = FormLayoutContainer.createCustomFormLayout("attachments", getTranslator(), page);
			attachmentsLayout.setRootForm(mainForm);
			attachmentsLayout.setLabel("send.mail.attachments", null);
			formLayout.add(attachmentsLayout);
		}

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("ok", buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}

	public int getStatus() {
		return status;
	}

	public boolean runsModal() {
		return false;
	}

	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {
		VFSContainer currentContainer = folderComponent.getCurrentContainer();
		VFSContainer rootContainer = folderComponent.getRootContainer();

		if (!VFSManager.exists(currentContainer)) {
			status = FolderCommandStatus.STATUS_FAILED;
			showError(translator.translate("FileDoesNotExist"));
			return null;
		}
		status = FolderCommandHelper.sanityCheck(wControl, folderComponent);
		if (status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}
		selection = new FileSelection(ureq, folderComponent.getCurrentContainerPath());
		status = FolderCommandHelper.sanityCheck3(wControl, folderComponent, selection);
		if (status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}

		boolean selectionWithContainer = false;
		List<String> filenames = selection.getFiles();
		List<VFSLeaf> leafs = new ArrayList<VFSLeaf>();
		for (String file : filenames) {
			VFSItem item = currentContainer.resolve(file);
			if (item instanceof VFSContainer) {
				selectionWithContainer = true;
			} else if (item instanceof VFSLeaf) {
				leafs.add((VFSLeaf) item);
			}
		}
		if (selectionWithContainer) {
			if (leafs.isEmpty()) {
				wControl.setError(getTranslator().translate("send.mail.noFileSelected"));
				return null;
			} else {
				setFormWarning(getTranslator().translate("send.mail.selectionContainsFolder"));
			}
		}
		setFiles(rootContainer, leafs);
		return this;
	}

	protected void setFiles(VFSContainer rootContainer, List<VFSLeaf> leafs) {
		this.files = leafs;

		StringBuilder subjectSb = new StringBuilder();
		if (StringHelper.containsNonWhitespace(subjectElement.getValue())) {
			subjectSb.append(subjectElement.getValue()).append('\n').append('\n');
		}
		StringBuilder bodySb = new StringBuilder();
		if (StringHelper.containsNonWhitespace(bodyElement.getValue())) {
			bodySb.append(bodyElement.getValue()).append('\n').append('\n');
		}

		attachments = new ArrayList<File>();
		long fileSize = 0l;
		for (VFSLeaf file : files) {
			MetaInfo infos = null;
			if (file instanceof MetaTagged) {
				infos = ((MetaTagged) file).getMetaInfo();
			}
			// subject
			appendToSubject(file, infos, subjectSb);

			// body
			appendMetadatas(file, infos, bodySb);
			appendBusinessPath(rootContainer, file, bodySb);
			bodySb.append('\n').append('\n');
			fileSize += file.getSize();
			if (allowAttachments && file instanceof LocalFileImpl) {
				File f = ((LocalFileImpl) file).getBasefile();
				attachments.add(f);
			}
		}

		int mailQuota = CoreSpringFactory.getImpl(MailModule.class).getMaxSizeForAttachement();
		long fileSizeInMB = fileSize / (1024l * 1024l);
		if (allowAttachments) {
			if (fileSizeInMB > mailQuota) {
				attachments.clear();
				setFormWarning("send.mail.fileToBigForAttachments", new String[] { String.valueOf(mailQuota), String.valueOf(fileSizeInMB) });
			} else {
				List<FileInfo> infos = new ArrayList<FileInfo>(files.size());
				for (VFSLeaf file : files) {
					final String name = file.getName();
					final double size = file.getSize() / (1024.0 * 1024.0);
					final String sizeStr = formatMb.format(size);
					final String cssClass = CSSHelper.createFiletypeIconCssClassFor(file.getName());
					infos.add(new FileInfo(name, sizeStr, cssClass));
				}
				attachmentsLayout.contextPut("attachments", infos);
			}
		}

		subjectElement.setValue(subjectSb.toString());
		bodyElement.setValue(bodySb.toString());
	}

	protected void appendToSubject(VFSLeaf file, MetaInfo infos, StringBuilder sb) {
		if (sb.length() > 0)
			sb.append(", ");
		if (infos != null && StringHelper.containsNonWhitespace(infos.getTitle())) {
			sb.append(infos.getTitle());
		} else {
			sb.append(file.getName());
		}
	}

	protected void appendMetadatas(VFSLeaf file, MetaInfo infos, StringBuilder sb) {
		if (infos == null) {
			appendMetadata("mf.filename", file.getName(), sb);
		} else {
			appendMetadata("mf.filename", infos.getName(), sb);
			String title = infos.getTitle();
			if (StringHelper.containsNonWhitespace(title)) {
				appendMetadata("mf.title", title, sb);
			}
			String comment = infos.getComment();
			if (StringHelper.containsNonWhitespace(comment)) {
				appendMetadata("mf.comment", comment, sb);
			}
			String creator = infos.getCreator();
			if (StringHelper.containsNonWhitespace(creator)) {
				appendMetadata("mf.creator", creator, sb);
			}
			String publisher = infos.getPublisher();
			if (StringHelper.containsNonWhitespace(publisher)) {
				appendMetadata("mf.publisher", publisher, sb);
			}
			String source = infos.getSource();
			if (StringHelper.containsNonWhitespace(source)) {
				appendMetadata("mf.source", source, sb);
			}
			String city = infos.getCity();
			if (StringHelper.containsNonWhitespace(city)) {
				appendMetadata("mf.city", city, sb);
			}
			appendPublicationDate(infos, sb);
			String pages = infos.getPages();
			if (StringHelper.containsNonWhitespace(pages)) {
				appendMetadata("mf.pages", pages, sb);
			}
			String language = infos.getLanguage();
			if (StringHelper.containsNonWhitespace(language)) {
				appendMetadata("mf.language", language, sb);
			}
			String url = infos.getUrl();
			if (StringHelper.containsNonWhitespace(url)) {
				appendMetadata("mf.url", url, sb);
			}
			String author = infos.getHTMLFormattedAuthor();
			if (StringHelper.containsNonWhitespace(author)) {
				appendMetadata("mf.author", author, sb);
			}
			String size = StringHelper.formatMemory(file.getSize());
			appendMetadata("mf.size", size, sb);
			long lastModifiedDate = infos.getLastModified();
			if (lastModifiedDate > 0) {
				appendMetadata("mf.lastModified", StringHelper.formatLocaleDate(lastModifiedDate, getLocale()), sb);
			}
			String type = FolderHelper.extractFileType(file.getName(), getLocale());
			if (StringHelper.containsNonWhitespace(type)) {
				appendMetadata("mf.type", type, sb);
			}
			int downloads = infos.getDownloadCount();
			if (infos.getDownloadCount() >= 0) {
				appendMetadata("mf.downloads", String.valueOf(downloads), sb);
			}
		}
	}

	protected void appendMetadata(String i18nKey, String value, StringBuilder sb) {
		sb.append(translate(i18nKey)).append(": ").append(value).append('\n');
	}

	protected void appendPublicationDate(MetaInfo infos, StringBuilder sb) {
		String[] publicationDate = infos.getPublicationDate();
		if (publicationDate == null || publicationDate.length != 2)
			return;
		String month = publicationDate[1];
		String year = publicationDate[0];
		if (StringHelper.containsNonWhitespace(month) || StringHelper.containsNonWhitespace(year)) {
			sb.append(translate("mf.publishDate")).append(":");
			if (StringHelper.containsNonWhitespace(month)) {
				sb.append(" ").append(translate("mf.month").replaceAll("&nbsp;", "")).append(" ").append(month);
			}
			if (StringHelper.containsNonWhitespace(year)) {
				sb.append(" ").append(translate("mf.year").replaceAll("&nbsp;", "")).append(" ").append(year);
			}
			sb.append('\n');
		}
	}

	protected void appendBusinessPath(VFSContainer rootContainer, VFSLeaf file, StringBuilder sb) {
		BusinessControlFactory bCF = BusinessControlFactory.getInstance();
		String businnessPath = getWindowControl().getBusinessControl().getAsString();

		String relPath = getRelativePath(rootContainer, file);
		businnessPath += "[path=" + relPath + "]";

		List<ContextEntry> ces = bCF.createCEListFromString(businnessPath);
		String uri = bCF.getAsURIString(ces, true);
		this.appendMetadata("mf.url", uri, sb);
	}

	protected String getRelativePath(VFSContainer rootContainer, VFSLeaf file) {
		String sb = "/" + file.getName();
		VFSContainer parent = file.getParentContainer();
		while (parent != null && !rootContainer.isSame(parent)) {
			sb = "/" + parent.getName() + sb;
			parent = parent.getParentContainer();
		}
		return sb;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		String subject = subjectElement.getValue();
		subjectElement.clearError();
		if (!StringHelper.containsNonWhitespace(subject)) {
			subjectElement.setErrorKey("form.legende.mandatory", null);
		}

		String body = bodyElement.getValue();
		bodyElement.clearError();
		if (!StringHelper.containsNonWhitespace(body)) {
			bodyElement.setErrorKey("form.legende.mandatory", null);
		}

		
		List<String> invalidTos = getInvalidToAddressesFromTextBoxList();
		if (invalidTos.size() > 0) {
			String[] invalidTosArray = new String[invalidTos.size()];
			userListBox.setErrorKey("mailhelper.error.addressinvalid", invalidTos.toArray(invalidTosArray));
			allOk = false;
		}

		return allOk & super.validateFormLogic(ureq);
	}

	/**
	 * returns a list of invalid Values within the textboxlist.
	 * values are either email-addresses (manually added, thus external) or
	 * usernames (from autocompletion, thus olat users)
	 * 
	 * @return
	 */
	private List<String> getInvalidToAddressesFromTextBoxList() {
		List<String> invalidTos = new ArrayList<String>();

		// the toValues are either usernames (from autocompletion, thus OLAT
		// users) or email-addresses (external)
		List<String> toValues = this.userListBox.getValueList();
		if (FolderConfig.getSendDocumentToExtern()) {
			for (String value : toValues) {
				if (!MailHelper.isValidEmailAddress(value) && !securityManager.isIdentityVisible(value)) {
					invalidTos.add(value);
				}
			}
		} else {
			for (String toValue : toValues) {
				if(!securityManager.isIdentityVisible(toValue)){
					invalidTos.add(toValue);
				}
			}
		}
		return invalidTos;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<Identity> tos = getTos();
		String subject = subjectElement.getValue();
		String body = bodyElement.getValue();
		sendEmail(tos, subject, body, ureq);
		fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
	}

	/**
	 * returns a List of Identites from the names in the userListBox.<br />
	 * 
	 * @return
	 */
	protected List<Identity> getTos() {
		List<String> values = userListBox.getValueList();
		List<Identity> identities = new ArrayList<Identity>();
		for (String value : values) {
			Identity id = securityManager.findIdentityByName(value);
			if (id != null) {
				identities.add(id);
			} else if (FolderConfig.getSendDocumentToExtern()) {
				identities.add(new EMailIdentity(value));
			}
		}
		return identities;
	}

	protected void sendEmail(List<Identity> tos, String subject, String body, UserRequest ureq) {
		File[] attachmentArray = null;
		if (attachments != null && !attachments.isEmpty() && allowAttachments) {
			attachmentArray = new File[attachments.size()];
			attachmentArray = attachments.toArray(attachmentArray);
		}

		MailTemplate mailTemplate = new MailTemplate(subject, body, attachmentArray) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity recipient) {
				// nothing to do;
			}
		};

		MailerResult mailerResult = MailerWithTemplate.getInstance().sendMailAsSeparateMails(null, tos, null, mailTemplate, ureq.getIdentity());
		MailHelper.printErrorsAndWarnings(mailerResult, getWindowControl(), ureq.getLocale());
	}

	public class UserListProvider implements ListProvider, ResultMapProvider {

		protected String formatIdentity(Identity ident) {
			User u = ident.getUser();
			String login = ident.getName();
			String first = u.getProperty(UserConstants.FIRSTNAME, null);
			String last = u.getProperty(UserConstants.LASTNAME, null);
			String mail = u.getProperty(UserConstants.EMAIL, null);
			return login + ": " + last + " " + first + " " + mail;
		}

		@Override
		public void getAutoCompleteContent(String searchValue, Map<String, String> resMap) {
			Map<String, String> userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, searchValue);
			userProperties.put(UserConstants.LASTNAME, searchValue);
			userProperties.put(UserConstants.EMAIL, searchValue);
			if (StringHelper.containsNonWhitespace(searchValue)) {
				List<Identity> res = securityManager.getVisibleIdentitiesByPowerSearch(searchValue, userProperties, false, null, null, null, null,
						null);

				int maxEntries = 14;
				boolean hasMore = false;
				for (Identity ident : res) {
					maxEntries--;
					String login = ident.getName();
					resMap.put(formatIdentity(ident), login);
					if (maxEntries <= 0) {
						hasMore = true;
						break;
					}
				}
				if (hasMore) {
					resMap.put(TextBoxListComponent.MORE_RESULTS_INDICATOR, TextBoxListComponent.MORE_RESULTS_INDICATOR);
				}
			}
		}

		public void getResult(String searchValue, ListReceiver receiver) {
			Map<String, String> userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, searchValue);
			userProperties.put(UserConstants.LASTNAME, searchValue);
			userProperties.put(UserConstants.EMAIL, searchValue);

			String login = (searchValue.equals("") ? null : searchValue);
			List<Identity> res = securityManager.getVisibleIdentitiesByPowerSearch(login, userProperties, false, null, null, null, null, null);

			int maxEntries = MAX_RESULTS_USERS;
			boolean hasMore = false;
			for (Iterator<Identity> it_res = res.iterator(); (hasMore = it_res.hasNext()) && maxEntries > 0;) {
				maxEntries--;
				Identity ident = it_res.next();
				User u = ident.getUser();
				String mail = u.getProperty(UserConstants.EMAIL, null);
				receiver.addEntry(mail, mail);
			}
			if (hasMore) {
				receiver.addEntry(".....", ".....");
			}
		}
	}

	public class FileInfo {
		private final String filename;
		private final String sizeInMB;
		private final String cssClass;

		public FileInfo(String filename, String sizeInMB, String cssClass) {
			this.filename = filename;
			this.sizeInMB = sizeInMB;
			this.cssClass = cssClass;
		}

		public String getFilename() {
			return filename;
		}

		public String getSizeInMB() {
			return sizeInMB;
		}

		public String getCssClass() {
			return cssClass;
		}
	}

	private class EMailIdentity implements Identity {

		private static final long serialVersionUID = -2899896628137672419L;
		private final String email;
		private final User user;

		public EMailIdentity(String email) {
			this.email = email;
			user = new EMailUser(email);
		}

		@Override
		public Long getKey() {
			return null;
		}

		@Override
		public boolean equalsByPersistableKey(Persistable persistable) {
			return this == persistable;
		}

		@Override
		public Date getCreationDate() {
			return null;
		}

		@Override
		public String getName() {
			return email;
		}

		@Override
		public User getUser() {
			return user;
		}

		@Override
		public Date getLastLogin() {
			return null;
		}

		@Override
		public void setLastLogin(Date loginDate) {/**/
		}

		@Override
		public Integer getStatus() {
			return null;
		}

		@Override
		public void setStatus(Integer newStatus) {/**/
		}

		@Override
		public void setName(String name) {/**/
		}
	}

	private class EMailUser implements User, ModifiedInfo {

		private static final long serialVersionUID = 7260225880639460228L;
		private final EMailPreferences prefs = new EMailPreferences();
		private Map<String, String> data = new HashMap<String, String>();

		public EMailUser(String email) {
			data.put(UserConstants.FIRSTNAME, "");
			data.put(UserConstants.LASTNAME, "");
			data.put(UserConstants.EMAIL, email);
		}

		public Long getKey() {
			return null;
		}

		public boolean equalsByPersistableKey(Persistable persistable) {
			return this == persistable;
		}

		public Date getLastModified() {
			return null;
		}

		@Override
		public void setLastModified(Date date) {
			//
		}

		public Date getCreationDate() {
			return null;
		}

		public void setProperty(String propertyName, String propertyValue) {
			//
		}

		public void setPreferences(Preferences prefs) {
			//
		}

		public String getProperty(String propertyName, Locale locale) {
			return data.get(propertyName);
		}

		public void setIdentityEnvironmentAttributes(Map<String, String> identEnvAttribs) {/**/
		}

		public String getPropertyOrIdentityEnvAttribute(String propertyName, Locale locale) {
			return data.get(propertyName);
		}

		public Preferences getPreferences() {
			return prefs;
		}
	}

	private class EMailPreferences implements Preferences {
		private static final long serialVersionUID = 7039109437910126584L;

		@Override
		public String getLanguage() {
			return getLocale().getLanguage();
		}

		@Override
		public void setLanguage(String l) {
			//
		}

		@Override
		public String getFontsize() {
			return null;
		}

		@Override
		public void setFontsize(String l) {
			//
		}

		@Override
		public String getNotificationInterval() {
			return null;
		}

		@Override
		public void setNotificationInterval(String notificationInterval) {/* */
		}

		@Override
		public String getReceiveRealMail() {
			return "true";
		}

		@Override
		public void setReceiveRealMail(String receiveRealMail) {
			//
		}

		@Override
		public boolean getInformSessionTimeout() {
			return false;
		}

		@Override
		public void setInformSessionTimeout(boolean b) {/* */
		}

		@Override
		public boolean getPresenceMessagesPublic() {
			return false;
		}

		@Override
		public void setPresenceMessagesPublic(boolean b) {/* */
		}
	}
}
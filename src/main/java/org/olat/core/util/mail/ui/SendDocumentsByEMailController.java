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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FileSelection;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.commands.CmdSendMail;
import org.olat.core.commons.modules.bc.commands.FolderCommand;
import org.olat.core.commons.modules.bc.commands.FolderCommandHelper;
import org.olat.core.commons.modules.bc.commands.FolderCommandStatus;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.folder.FolderHelper;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

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
	private FormLink addEmailLink;
	private TextElement subjectElement;
	private FormLayoutContainer userListBox;
	private FormLayoutContainer attachmentsLayout;
	private EMailCalloutCtrl emailCalloutCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	
	private final DecimalFormat formatMb = new DecimalFormat("0.00");

	private int status = FolderCommandStatus.STATUS_SUCCESS;
	private List<File> attachments;
	private final boolean allowAttachments;
	private List<IdentityWrapper> toValues = new ArrayList<>();

	@Autowired
	private UserManager userManager;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BaseSecurity securityManager;

	public SendDocumentsByEMailController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, null, Util.createPackageTranslator(MailModule.class, ureq.getLocale(),
				Util.createPackageTranslator(MetaInfoController.class, ureq.getLocale())));
		setBasePackage(MailModule.class);

		allowAttachments = !FolderConfig.getSendDocumentLinkOnly();

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("send.mail.description");
		setFormStyle("o_send_documents");

		int emailCols = 25;

		String toPage = velocity_root + "/tos.html";
		userListBox = FormLayoutContainer.createCustomFormLayout("send.mail.to.auto", getTranslator(), toPage);
		userListBox.setLabel("send.mail.to", null);
		userListBox.setRootForm(mainForm);
		userListBox.contextPut("tos", toValues);
		formLayout.add(userListBox);

		addEmailLink = uifactory.addFormLink("add.email", userListBox);
		addEmailLink.setIconLeftCSS("o_icon o_icon_add");

		subjectElement = uifactory.addTextElement("tsubject", "send.mail.subject", 255, "", formLayout);

		bodyElement = uifactory.addTextAreaElement("tbody", "send.mail.body", -1, 20, emailCols, false, false, "", formLayout);

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
	public int getStatus() {
		return status;
	}

	@Override
	public boolean runsModal() {
		return false;
	}

	@Override
	public String getModalTitle() {
		return translate("send.mail.title");
	}

	@Override
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
		FileSelection selection = new FileSelection(ureq, folderComponent.getCurrentContainer(), folderComponent.getCurrentContainerPath());
		status = FolderCommandHelper.sanityCheck3(wControl, folderComponent, selection);
		if (status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}

		boolean selectionWithContainer = false;
		List<String> filenames = selection.getFiles();
		List<VFSLeaf> leafs = new ArrayList<>();
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

	protected void setFiles(VFSContainer rootContainer, List<VFSLeaf> files) {
		StringBuilder subjectSb = new StringBuilder();
		if (StringHelper.containsNonWhitespace(subjectElement.getValue())) {
			subjectSb.append(subjectElement.getValue()).append('\n').append('\n');
		}
		StringBuilder bodySb = new StringBuilder();
		if (StringHelper.containsNonWhitespace(bodyElement.getValue())) {
			bodySb.append(bodyElement.getValue()).append('\n').append('\n');
		}

		attachments = new ArrayList<>();
		long fileSize = 0l;
		for (VFSLeaf file : files) {
			VFSMetadata infos = null;
			if (file.canMeta() == VFSConstants.YES) {
				infos = file.getMetaInfo();
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
				List<FileInfo> infos = new ArrayList<>(files.size());
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

	protected void appendToSubject(VFSLeaf file, VFSMetadata infos, StringBuilder sb) {
		if (sb.length() > 0)
			sb.append(", ");
		if (infos != null && StringHelper.containsNonWhitespace(infos.getTitle())) {
			sb.append(infos.getTitle());
		} else {
			sb.append(file.getName());
		}
	}

	protected void appendMetadatas(VFSLeaf file, VFSMetadata infos, StringBuilder sb) {
		if (infos == null) {
			appendMetadata("mf.filename", file.getName(), sb);
		} else {
			appendMetadata("mf.filename", infos.getFilename(), sb);
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
			String author = userManager.getUserDisplayName(infos.getFileInitializedBy());
			if (StringHelper.containsNonWhitespace(author)) {
				appendMetadata("mf.author", author, sb);
			}
			String size = Formatter.formatBytes(file.getSize());
			appendMetadata("mf.size", size, sb);
			Date lastModifiedDate = infos.getFileLastModified();
			if (lastModifiedDate != null) {
				appendMetadata("mf.lastModified", Formatter.getInstance(getLocale()).formatDate(lastModifiedDate), sb);
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

	protected void appendPublicationDate(VFSMetadata infos, StringBuilder sb) {
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
		boolean allOk = super.validateFormLogic(ureq);

		String subject = subjectElement.getValue();
		subjectElement.clearError();
		if (!StringHelper.containsNonWhitespace(subject)) {
			subjectElement.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(subject != null && subject.length() > subjectElement.getMaxLength()) {
			subjectElement.setErrorKey("text.element.error.notlongerthan",
					new String[]{ Integer.toString(subjectElement.getMaxLength()) });
			allOk &= false;
		}

		String body = bodyElement.getValue();
		bodyElement.clearError();
		if (!StringHelper.containsNonWhitespace(body)) {
			bodyElement.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		List<Identity> invalidTos = getInvalidToAddressesFromTextBoxList();
		userListBox.clearError();
		if (!invalidTos.isEmpty()) {
			String[] invalidTosArray = new String[invalidTos.size()];
			userListBox.setErrorKey("mailhelper.error.addressinvalid", invalidTos.toArray(invalidTosArray));
			allOk &= false;
		} else if(toValues == null || toValues.isEmpty()) {
			userListBox.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}

	/**
	 * returns a list of invalid Values within the textboxlist.
	 * values are either email-addresses (manually added, thus external) or
	 * usernames (from autocompletion, thus olat users)
	 * 
	 * @return
	 */
	private List<Identity> getInvalidToAddressesFromTextBoxList() {
		List<Identity> invalidTos = new ArrayList<>();

		// the toValues are either usernames (from autocompletion, thus OLAT
		// users) or email-addresses (external)
		if (FolderConfig.getSendDocumentToExtern()) {
			for (IdentityWrapper toValue : toValues) {
				Identity id = toValue.getIdentity();
				if (!MailHelper.isValidEmailAddress(id.getUser().getProperty(UserConstants.EMAIL, null))
						&& !securityManager.isIdentityVisible(id)) {
					invalidTos.add(id);
				}
			}
		} else {
			for (IdentityWrapper toValue : toValues) {
				Identity id = toValue.getIdentity();
				if(!securityManager.isIdentityVisible(id)){
					invalidTos.add(id);
				}
			}
		}
		return invalidTos;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == addEmailLink) {
			doAddEmail(ureq);
		} else if(source instanceof FormLink && source.getUserObject() instanceof IdentityWrapper) {
			if(source.getName().startsWith("rm-")) {
				for(Iterator<IdentityWrapper> wrapperIt=toValues.iterator(); wrapperIt.hasNext(); ) {
					IdentityWrapper wrapper = wrapperIt.next();
					if(source.getUserObject().equals(wrapper)) {
						wrapperIt.remove();
					}
				}
			}
			userListBox.setDirty(true);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == emailCalloutCtrl) {
			if (event instanceof SingleIdentityChosenEvent) {
				addIdentity((SingleIdentityChosenEvent)event);
			}
			calloutCtrl.deactivate();
		}
	}
	
	private void addIdentity(SingleIdentityChosenEvent foundEvent) {
		Identity chosenIdentity = foundEvent.getChosenIdentity();
		if (chosenIdentity != null) {
			addIdentity(chosenIdentity);
		}
		userListBox.setDirty(true);
	}
	
	private void addIdentity(Identity identity) {
		FormLink rmLink = uifactory.addFormLink("rm-" + CodeHelper.getForeverUniqueID(), " ", null, userListBox, Link.NONTRANSLATED + Link.LINK);
		IdentityWrapper wrapper = new IdentityWrapper(identity, rmLink);
		rmLink.setIconLeftCSS("o_icon o_icon_remove");
		rmLink.setUserObject(wrapper);
		toValues.add(wrapper);
		userListBox.setDirty(true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<Identity> tos = new ArrayList<>(toValues.size());
		for(IdentityWrapper wrapper:toValues) {
			tos.add(wrapper.getIdentity());
		}
		String subject = subjectElement.getValue();
		String body = bodyElement.getValue();
		sendEmail(tos, subject, body, ureq);
		fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
	}
	
	protected void doAddEmail(UserRequest ureq) {
		String title = translate("add.email");
		removeAsListenerAndDispose(emailCalloutCtrl);
		boolean allowExtern = FolderConfig.getSendDocumentToExtern();
		emailCalloutCtrl = new EMailCalloutCtrl(ureq, getWindowControl(), allowExtern);
		listenTo(emailCalloutCtrl);
		
		removeAsListenerAndDispose(calloutCtrl);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), emailCalloutCtrl.getInitialComponent(), addEmailLink, title, true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();	
	}

	protected void sendEmail(List<Identity> tos, String subject, String body, UserRequest ureq) {
		File[] attachmentArray = null;
		if (attachments != null && !attachments.isEmpty() && allowAttachments) {
			attachmentArray = attachments.toArray(new File[attachments.size()]);
		}

		MailerResult result = new MailerResult();
		String metaId = UUID.randomUUID().toString().replace("-", "");
		for(Identity to:tos) {
			MailBundle bundle = new MailBundle();
			bundle.setToId(to);
			bundle.setMetaId(metaId);
			bundle.setFromId(ureq.getIdentity());
			bundle.setContent(subject, body, attachmentArray);
			result.append(mailManager.sendMessage(bundle));
		}
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean detailedErrorOutput = roles.isAdministrator() || roles.isSystemAdmin();
		MailHelper.printErrorsAndWarnings(result, getWindowControl(), detailedErrorOutput, ureq.getLocale());
	}

	public static class FileInfo {
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
	
	public final class IdentityWrapper {
		private Identity identity;
		private FormLink removeLink;
		
		public IdentityWrapper(Identity identity, FormLink removeLink) {
			this.identity = identity;
			this.removeLink = removeLink;
		}
		
		public String getName() {
			if(identity instanceof EMailIdentity) {
				return identity.getUser().getProperty(UserConstants.EMAIL, null);
			}
			return userManager.getUserDisplayName(identity);
		}
		
		public Identity getIdentity() {
			return identity;
		}
		
		public String getRemoveLinkName() {
			return removeLink.getComponent().getComponentName();
		}
	}
}
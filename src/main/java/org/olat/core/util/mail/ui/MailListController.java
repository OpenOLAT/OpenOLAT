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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.commons.services.mark.Mark;
import org.olat.core.gui.ShortName;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.model.DBMail;
import org.olat.core.util.mail.model.DBMailLight;
import org.olat.core.util.mail.model.DBMailRecipient;
import org.olat.core.util.mail.ui.MailDataModel.Columns;
import org.olat.core.util.mail.ui.MailDataModel.ContextPair;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Description:<br>
 * Represent a list of mails.
 * 
 * <P>
 * Initial Date:  24 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailListController extends BasicController implements Activateable2 {
	
	private static final String CMD_READ_TOGGLE = "creadt";
	private static final String CMD_READ = "cread";
	private static final String CMD_DELETE = "cdelselected";
	private static final String CMD_MARK_TOGGLE = "cmark";
	private static final String CMD_PROFILE = "cprofile";
	private static final String CMD_SEND_REAL_MAIL = "cfwd";
	private static final String CMD_MARK_READ = "creadselected";
	private static final String CMD_MARK_UNREAD = "cunreadselected";
	private static final String CMD_MARK_MARKED = "cmarkselected";
	private static final String CMD_MARK_UNMARKED = "cunmarkselected";
	private static final String MAIN_CMP = "mainCmp";

	private Link backLink;
	private final VelocityContainer mainVC;
	private MailController mailCtr;
	private MailListController metaMailCtr;
	private final TableController tableCtr;
	private final VelocityContainer tableVC;
	private DialogBoxController deleteConfirmationBox;
	
	
	private final boolean outbox;
	private final String metaId;
	@Autowired
	private MailManager mailManager;
	private final MailContextResolver contextResolver;
	
	public MailListController(UserRequest ureq, WindowControl wControl, boolean outbox, MailContextResolver resolver) {
		this(ureq, wControl, null, outbox, resolver);
	}
	
	private MailListController(UserRequest ureq, WindowControl wControl, String metaId, boolean outbox, MailContextResolver resolver) {
		super(ureq, wControl);
		setBasePackage(MailModule.class);
		this.outbox = outbox;
		this.metaId = metaId;
		this.contextResolver = resolver;

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setDownloadOffered(true);
		tableConfig.setPreferencesOffered(true, "MailBox");		
		tableConfig.setTableEmptyMessage(translate("mail.empty.box"), null, "o_icon_mail");
		tableConfig.setMultiSelect(true);

		mainVC = createVelocityContainer("mails");
		tableVC = createVelocityContainer("mailsTable");
		
		String context = translate("mail.context");
		tableCtr = new TableController(tableConfig, ureq, wControl, Collections.<ShortName>emptyList(), null, context , null, false, getTranslator());

		//only for outbox
		if(outbox) {
			//context / recipients / subject / sendDate
			tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Columns.context.i18nKey(), Columns.context.ordinal(), null,
					getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new MailContextCellRenderer(this, tableVC, getTranslator())) {
					@Override
					public int compareTo(int rowa, int rowb) {
						Object a = table.getTableDataModel().getValueAt(rowa,dataColumn);
						Object b = table.getTableDataModel().getValueAt(rowb,dataColumn);
						if(a instanceof ContextPair && b instanceof ContextPair) {
							ContextPair p1 = (ContextPair)a;
							ContextPair p2 = (ContextPair)b;
							return super.compareString(p1.getName(), p2.getName());
						}
						return super.compareTo(rowa, rowb);
					}
			});
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Columns.recipients.i18nKey(), Columns.recipients.ordinal(), null, getLocale()));
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Columns.subject.i18nKey(), Columns.subject.ordinal(), CMD_READ, getLocale()));
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Columns.sendDate.i18nKey(), Columns.sendDate.ordinal(), null, getLocale()));
		} else {
			//read / marked / context / from / subject / receivedDate
			CustomCellRenderer readRenderer = new BooleanCSSCellRenderer(getTranslator(), "o_icon o_icon-lg o_icon_read", "o_icon o_icon-lg o_icon_to_read", "mail.read", "mail.unread");
			tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Columns.read.i18nKey(), Columns.read.ordinal(), CMD_READ_TOGGLE, 
				getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, readRenderer));
			CustomCellRenderer markRenderer = new BooleanCSSCellRenderer(getTranslator(), Mark.MARK_CSS_LARGE, Mark.MARK_ADD_CSS_LARGE, "mail.marked", "mail.unmarked");
			tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Columns.marked.i18nKey(), Columns.marked.ordinal(), CMD_MARK_TOGGLE, 
					getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, markRenderer));
			tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Columns.context.i18nKey(), Columns.context.ordinal(), null,
					getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new MailContextCellRenderer(this, tableVC, getTranslator())){
						@Override
						public int compareTo(int rowa, int rowb) {
							Object a = table.getTableDataModel().getValueAt(rowa,dataColumn);
							Object b = table.getTableDataModel().getValueAt(rowb,dataColumn);
							if(a instanceof ContextPair && b instanceof ContextPair) {
								ContextPair p1 = (ContextPair)a;
								ContextPair p2 = (ContextPair)b;
								return super.compareString(p1.getName(), p2.getName());
							}
							return super.compareTo(rowa, rowb);
						}
			});
			tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Columns.from.i18nKey(), Columns.from.ordinal(), null,
					getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new MailFromCellRenderer(this, tableVC, getTranslator())));
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Columns.subject.i18nKey(), Columns.subject.ordinal(), CMD_READ, getLocale()));
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Columns.receivedDate.i18nKey(), Columns.receivedDate.ordinal(), null, getLocale()));
		}

		tableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_READ, "mail.action.open", translate("mail.action.open")));

		// only for inbox
		if (!outbox) {
			tableCtr.addMultiSelectAction("mail.action.read", CMD_MARK_READ);
			tableCtr.addMultiSelectAction("mail.action.unread", CMD_MARK_UNREAD);
			tableCtr.addMultiSelectAction("mail.action.mark", CMD_MARK_MARKED);
			tableCtr.addMultiSelectAction("mail.action.unmark", CMD_MARK_UNMARKED);			
		}
		tableCtr.addMultiSelectAction("mail.action.send.real", CMD_SEND_REAL_MAIL);
		tableCtr.addMultiSelectAction("delete", CMD_DELETE);
		
		reloadModel();
		
		int dateSort = outbox ? 4 : 6;
		tableCtr.setSortColumn(dateSort, false);

		listenTo(tableCtr);
		
		tableVC.put("tableCmp", tableCtr.getInitialComponent());
		if(outbox) {
			if(StringHelper.containsNonWhitespace(metaId)) {
				tableVC.contextPut("title", translate("mail.outbox.title"));
				tableVC.contextPut("description", translate("mail.outbox.meta"));
				
			} else {
				tableVC.contextPut("title", translate("mail.outbox.title"));
			}
		} else {
			tableVC.contextPut("title", translate("mail.inbox.title"));
		}
		
		mainVC.put(MAIN_CMP, tableVC);
		if(StringHelper.containsNonWhitespace(metaId)) {
			backLink = LinkFactory.createLinkBack(mainVC, this);
			mainVC.put("back", backLink);
		}

		putInitialPanel(mainVC);
	}
	
	private void replaceInModel(DBMailLight mail) {
		MailDataModel dataModel = (MailDataModel)tableCtr.getTableDataModel();
		dataModel.replace(mail);
		tableCtr.modelChanged();
	}
	
	private void reloadModel() {
		List<DBMailLight> mails;
		if(outbox) {
			if(StringHelper.containsNonWhitespace(metaId)) {
				mails = mailManager.getEmailsByMetaId(metaId);
			} else {
				mails = mailManager.getOutbox(getIdentity(), 0, -1, true);
			}
			
			//strip meta emails
			Set<String> metaIds = new HashSet<>();
			for(Iterator<DBMailLight> it=mails.iterator(); it.hasNext(); ) {
				DBMailLight mail = it.next();
				if(StringHelper.containsNonWhitespace(mail.getMetaId())) {
					if(metaIds.contains(mail.getMetaId())) {
						it.remove();
					} else {
						metaIds.add(mail.getMetaId());
					}
				}
			}
		} else {
			mails = mailManager.getInbox(getIdentity(), null, Boolean.TRUE, null, 0, -1);
		}
		
		//extract contexts
		Map<String, String> bpToContexts = new HashMap<>();
		for(DBMailLight mail:mails) {
			String businessPath = mail.getContext().getBusinessPath();
			if(StringHelper.containsNonWhitespace(businessPath) && !bpToContexts.containsKey(businessPath)) {
				String contextName = contextResolver.getName(businessPath, getLocale());
				if(StringHelper.containsNonWhitespace(contextName)) {
					bpToContexts.put(businessPath, contextName);
				}
			}
		}
		
		if(!bpToContexts.isEmpty()) {
			List<ShortName> filters = new ArrayList<>();
			Map<String, MailContextShortName> uniqueNames = new HashMap<>();
			ShortName allContextFilter = new MailContextShortName("-");
			filters.add(allContextFilter);
			for(Map.Entry<String, String> entry:bpToContexts.entrySet()) {
				String businessPath = entry.getKey();
				String contextName = entry.getValue();
				if(!uniqueNames.containsKey(contextName)) {
					MailContextShortName cxt = new MailContextShortName(contextName, new HashSet<String>());
					filters.add(cxt);
					uniqueNames.put(contextName, cxt);
				}
				uniqueNames.get(contextName).getBusinessPaths().add(businessPath);
			}
			tableCtr.setFilters(filters, allContextFilter);
		}
		
		Formatter formatter = Formatter.getInstance(getLocale());
		MailDataModel dataModel = new MailDataModel(mails, bpToContexts, getIdentity(), getTranslator(), formatter, outbox);
		tableCtr.setTableDataModel(dataModel);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == backLink) {
			if(mailCtr != null) {
				backFromMail();
			} else {
				fireEvent(ureq, event);
			}
		} else if (source instanceof Link && source.getComponentName().startsWith("bp_")) {
			String businessPath = (String)((Link)source).getUserObject();
			if(StringHelper.containsNonWhitespace(businessPath)) {
				contextResolver.open(ureq, getWindowControl(), businessPath);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				DBMailLight mail = (DBMailLight)tableCtr.getTableDataModel().getObject(rowid);
				if(CMD_READ.equals(actionid)) {
					if(outbox && StringHelper.containsNonWhitespace(mail.getMetaId()) && !mail.getMetaId().equals(metaId)) {
						selectMetaMail(ureq, mail.getMetaId());
					} else {
						selectMail(ureq, mail.getKey());
					}
				} else if (CMD_PROFILE.equals(actionid)) {
					DBMailRecipient from = mail.getFrom();
					if(from != null&& from.getRecipient() != null) {
						contextResolver.open(ureq, getWindowControl(), "[Identity:" + from.getRecipient().getKey() + "]");
					}
				} else if (CMD_MARK_TOGGLE.equals(actionid)) {
					mail = mailManager.toggleMarked(mail, getIdentity());
					replaceInModel(mail);
				} else if (CMD_READ_TOGGLE.equals(actionid)) {
					mail = mailManager.toggleRead(mail, getIdentity());
					replaceInModel(mail);
				}
			} else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				// Multiselect events
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				BitSet selectedMails = tmse.getSelection();
				if(selectedMails.isEmpty()){
					tableVC.setDirty(true);
					showWarning("mail.action.empty");
					return;					
				}
				String actionid = tmse.getAction();
				if (CMD_DELETE.equals(actionid)) {
					String title = translate("mail.confirm.delete.title");
					int selected = selectedMails.cardinality();
					String text;
					if (selected == 1) {
						text = translate("mail.confirm.delete.single.text");
					} else {
						text = translate("mail.confirm.delete.multi.text", selected + "");						
					}
					deleteConfirmationBox = activateYesNoDialog(ureq, title, text, deleteConfirmationBox);
					deleteConfirmationBox.setUserObject(selectedMails);
				} else if (CMD_SEND_REAL_MAIL.equals(actionid)) {
					for (int i=selectedMails.nextSetBit(0); i >= 0; i=selectedMails.nextSetBit(i+1)) {
						DBMailLight mail = (DBMailLight) tableCtr.getTableDataModel().getObject(i);						
						MailerResult result = forwardToMyRealMail(mail);
						if(result.getReturnCode() != MailerResult.OK) {
							Roles roles = ureq.getUserSession().getRoles();
							boolean detailedErrorOutput = roles.isAdministrator() || roles.isSystemAdmin();
							MailHelper.printErrorsAndWarnings(result, getWindowControl(), detailedErrorOutput, getLocale());
						} else {
							showInfo("mail.action.send.real.success", mail.getSubject());
						}
					}				
					reloadModel();
				} else if (CMD_MARK_MARKED.equals(actionid) || CMD_MARK_UNMARKED.equals(actionid)) {
					for (int i=selectedMails.nextSetBit(0); i >= 0; i=selectedMails.nextSetBit(i+1)) {
						DBMailLight mail = (DBMailLight)tableCtr.getTableDataModel().getObject(i);
						mailManager.setMarked(mail, CMD_MARK_MARKED.equals(actionid), getIdentity());
					}				
					reloadModel();
				} else if (CMD_MARK_READ.equals(actionid) || CMD_MARK_UNREAD.equals(actionid)) {
					for (int i=selectedMails.nextSetBit(0); i >= 0; i=selectedMails.nextSetBit(i+1)) {
						DBMailLight mail = (DBMailLight) tableCtr.getTableDataModel().getObject(i);
						mailManager.setRead(mail, CMD_MARK_READ.equals(actionid), getIdentity());
					}				
					reloadModel();
				}
				
			} else if (TableController.EVENT_FILTER_SELECTED == event) {
				MailDataModel dataModel = (MailDataModel)tableCtr.getTableDataModel();
				MailContextShortName filter = (MailContextShortName)tableCtr.getActiveFilter();
				dataModel.filter(filter);
				tableCtr.setTableDataModel(dataModel);
			} else if (TableController.EVENT_NOFILTER_SELECTED == event) {
				MailDataModel dataModel = (MailDataModel)tableCtr.getTableDataModel();
				dataModel.filter(null);
				tableCtr.setTableDataModel(dataModel);
			}			
			
		} else if (source == mailCtr) {
			backFromMail();
			
		} else if (source == metaMailCtr) {
			removeAsListenerAndDispose(metaMailCtr);
			metaMailCtr = null;
			mainVC.put(MAIN_CMP, tableVC);
			
		} else if (source == deleteConfirmationBox) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				BitSet deleteMails = (BitSet)deleteConfirmationBox.getUserObject();
				for (int i=deleteMails.nextSetBit(0); i >= 0; i=deleteMails.nextSetBit(i+1)) {
					DBMailLight mail = (DBMailLight)tableCtr.getTableDataModel().getObject(i);
					//reload the message
					mail = mailManager.getMessageByKey(mail.getKey());
					boolean deleteMetaMail = outbox && !StringHelper.containsNonWhitespace(metaId);
					mailManager.delete(mail, getIdentity(), deleteMetaMail);
					// Do not remove from model to prevent concurrent modification
					// exception, instead just reload model afterwards
				}				
				reloadModel();
			}
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			backFromMail();
			return;
		}
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if(DBMail.class.getSimpleName().equals(type)) {
			selectMail(ureq, entry.getOLATResourceable().getResourceableId());
		}
	}

	private void backFromMail() {
		removeAsListenerAndDispose(mailCtr);
		mailCtr = null;
		mainVC.put(MAIN_CMP, tableVC);
	}
	
	private MailerResult forwardToMyRealMail(DBMailLight mail) {
		DBMail fullMail = mailManager.getMessageByKey(mail.getKey());
		return mailManager.forwardToRealInbox(getIdentity(), fullMail, null);
	}
	
	private void selectMetaMail(UserRequest ureq, String metaID) {
		metaMailCtr = new MailListController(ureq, getWindowControl(), metaID, outbox, contextResolver);
		listenTo(metaMailCtr);
		mainVC.put(MAIN_CMP, metaMailCtr.getInitialComponent());
	}
	
	private void selectMail(UserRequest ureq, Long mailKey) {
		DBMail mail = mailManager.getMessageByKey(mailKey);
		if(mail != null) {
			selectMail(ureq, mail);
		}
	}
	
	private void selectMail(UserRequest ureq, DBMail mail) {
		removeAsListenerAndDispose(mailCtr);
		boolean back = !StringHelper.containsNonWhitespace(mail.getMetaId()) || !outbox;
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(DBMail.class, mail.getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		mailCtr = new MailController(ureq, bwControl, mail, back, outbox);
		listenTo(mailCtr);
		mainVC.put(MAIN_CMP, mailCtr.getInitialComponent());
		
		if(mailManager.setRead(mail, Boolean.TRUE, getIdentity())) {
			reloadModel();
		}
	}
}

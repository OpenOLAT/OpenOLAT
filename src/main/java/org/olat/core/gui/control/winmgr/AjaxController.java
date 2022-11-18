/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui.control.winmgr;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.admin.sysinfo.manager.SessionStatsManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.CannotReplaceDOMFragmentException;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.pushpoll.WindowCommand;
import org.olat.core.gui.media.DefaultMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br> - to be used by the windowmanager only! - this controller
 * manages the state of all browser windows on the client side (javascript) and
 * communicates changes to the server side (java)
 * <P>
 * Initial Date: 17.03.2006 <br>
 * 
 * @author Felix Jost
 */
public class AjaxController extends DefaultController {
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(AjaxController.class);
	private static final Logger log = Tracing.createLoggerFor(AjaxController.class);
	private final VelocityContainer myContent;
	private final VelocityContainer pollPeriodContent;
	private final Panel mainP;
	private final Panel pollperiodPanel;
	// protected only for performance improvement
	protected List<WindowCommand> windowcommands = new ArrayList<>(3);
	private final Mapper m, sbm;
	private final MapperKey mKey, sbmKey;
	
	private static final int DEFAULT_POLLPERIOD = 5000;//reasonable default value
	private int pollperiod = DEFAULT_POLLPERIOD;//reasonable default value
	private int pollCount = 0;
	private long creationTime = System.currentTimeMillis();
	private boolean ajaxEnabled;
	
	private WindowBackOffice wboImpl;
	
	@Autowired
	private SessionStatsManager statsManager;

	AjaxController(UserRequest ureq, final WindowBackOfficeImpl wboImpl, boolean ajaxEnabled) {
		super(null);
		this.ajaxEnabled = ajaxEnabled;
		this.wboImpl = wboImpl;
		
		pollPeriodContent = new VelocityContainer("jsserverpartpoll", VELOCITY_ROOT + "/pollperiod.html", null, this);
		pollPeriodContent.contextPut("pollperiod", Integer.valueOf(pollperiod));
		
		myContent = new VelocityContainer("jsserverpart", VELOCITY_ROOT + "/serverpart.html", null, this);
		myContent.contextPut("pollperiod", Integer.valueOf(pollperiod));
		
		// create a mapper to not block main traffic when polling (or vica versa)
		final Window window = wboImpl.getWindow();
		m = new Mapper() {
			@Override
			public MediaResource handle(String relPath, HttpServletRequest request) {
				pollCount++;
				statsManager.incrementAuthenticatedPollerClick();

				String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
				UserRequest uureq = new UserRequestImpl(uriPrefix, request, null);
				boolean reload = false;
				Windows ws = Windows.getWindows(uureq);
				if(ws != null && wboImpl.getChiefController() != null) {
					ChiefController cc = wboImpl.getChiefController();
					reload = cc.wishAsyncReload(uureq, false);
					cc.getWindow().setMarkToBeRemoved(false);
				}
				
				MediaResource resource;
				
				try {
					// check for dirty components now.
					wboImpl.fireCycleEvent(Window.BEFORE_INLINE_RENDERING);
					Command updateDirtyCom = window.handleDirties();
					wboImpl.fireCycleEvent(Window.AFTER_INLINE_RENDERING);
					
					if (updateDirtyCom != null) {
						synchronized (windowcommands) { //o_clusterOK by:fj
							windowcommands.add(new WindowCommand(wboImpl, updateDirtyCom));
							if(reload) {
								String timestampID = uureq.getTimestampID();
								String reRenderUri = window.buildURIFor(window, timestampID, null);
								Command rmrcom = CommandFactory.createParentRedirectTo(reRenderUri);
								windowcommands.add(new WindowCommand(wboImpl, rmrcom));
							}
						}
						resource = extractMediaResource(false);
					} else {
						resource = new NothingChangedMediaResource();
					}
				} catch (CannotReplaceDOMFragmentException e) {
					log.error("", e);
					String timestampID = uureq.getTimestampID();
					String reRenderUri = window.buildURIFor(window, timestampID, null);
					Command rmrcom = CommandFactory.createParentRedirectTo(reRenderUri);
					windowcommands.add(new WindowCommand(wboImpl, rmrcom));
					resource = extractMediaResource(false);
				}
				return resource;
			}
		};

		mKey = CoreSpringFactory.getImpl(MapperService.class).register(ureq.getUserSession(), m);
		myContent.contextPut("mapuri", mKey.getUrl());
		
		final String csrfToken = ureq.getUserSession().getCsrfToken();
		myContent.contextPut("csrfToken", csrfToken);
		
		mainP = new Panel("ajaxMainPanel");
		mainP.setContent(myContent);
		
		pollperiodPanel = new Panel("pollperiodP");
		pollperiodPanel.setContent(pollPeriodContent);
		myContent.put("pollperiodPanel", pollperiodPanel);

		setInitialComponent(mainP);
		
		// either turn ajax on or off
		setAjaxEnabled(ajaxEnabled);
		
		// The following is for the "standby page"
		final Locale flocale = ureq.getLocale();
		
		sbm = new Mapper() {
			
			Translator t = Util.createPackageTranslator(ChiefController.class, flocale);
			@Override
			public MediaResource handle(String relPath, HttpServletRequest request) {
				StringMediaResource smr = new StringMediaResource();
				smr.setContentType("text/html;charset=utf-8");
				smr.setEncoding("utf-8");
				try(StringOutput slink = new StringOutput(50);
						StringOutput blink = new StringOutput(50)) {

					StaticMediaDispatcher.renderStaticURI(slink, null);
					//slink now holds static url base like /olat/raw/700/
					
					URLBuilder ubu = new URLBuilder(WebappHelper.getServletContextPath() + DispatcherModule.PATH_AUTHENTICATED, "1", "1", csrfToken);
					ubu.buildURI(blink, null, null);
					//blink holds the link back to olat like /olat/auth/1%3A1%3A0%3A0%3A0/
		
					String p = FileUtils.load(getClass().getResourceAsStream("_content/standby.html"), WebappHelper.getDefaultCharset());
					p = p.replace("_staticLink_",	slink.toString());
					p = p.replace("_pageTitle_",	t.translate("standby.page.title"));
					p = p.replace("_pageHeading_",	t.translate("standby.page.heading"));
					p = p.replace("_Message_",		t.translate("standby.page.message"));
					p = p.replace("_Button_",		t.translate("standby.page.button"));
					p = p.replace("_linkTitle_",	t.translate("standby.page.button.title"));
					p = p.replace("_olatUrl_",		blink.toString());
					
					smr.setData(p);
					
				} catch (Exception e) {
					smr.setData(e.toString());
				}
				return smr;
			}
		};
		sbmKey = CoreSpringFactory.getImpl(MapperService.class).register(ureq.getUserSession(), sbm);
		myContent.contextPut("sburi", sbmKey.getUrl());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public void pushResource(UserRequest ureq, Writer sb, boolean wrapHTML) throws IOException {
		if (wrapHTML) {
			// most ajax responses are a lot smaller than 16k
			sb.append("<html><head><script>\nfunction invoke(){var r=");
			pushJSONAndClear(ureq, sb);
			sb.append("; ") 
				.append("if (parent!=self&&parent.window.o_info){")
				.append("parent.window.o_ainvoke(r);")
					// normal case: ajax result can be delivered into the hidden iframe.
					// else: no parent frame or parent frame is not olat -> reasons:
					// a) mouse-right-click to open in new tab/window
					// b) fast double-click when target causes a 302 and browser's window's document has been updated, but old link was still clickable
					// c) ...
					// -> in all cases, do not show the json command, but reload the window which contained the link clicked (= window id of url)
				.append(" try{ parent.window.o_removeIframe(document.defaultView.frameElement.id); } catch(e) {} ")
				.append("} else {") 
					// inform user that ajax-request cannot be opened in a new window
				.append("this.document.location=\"")
				.append(StaticMediaDispatcher.createStaticURIFor("msg/json/en/info.html"))
				.append("\";")
				.append("}}\n</script></head><body onLoad=\"invoke()\"></body></html>");
		} else {
			pushJSONAndClear(ureq, sb);
		}
	}
	
	public void pushJSONAndClear(UserRequest ureq, Writer writer) throws IOException {
		synchronized (windowcommands) { //o_clusterOK by:fj
			// handle all windowcommands now, create json
			writer.append("{\"cmds\":[");
			int sum = windowcommands.size();
			if (sum > 0) {
				// treat commands waiting for the poll
				for (int i = 0; i < sum; i++) {
					if(i != 0) writer.append(",");
					WindowCommand wc = windowcommands.get(i);
					pushJSON(wc, writer);
				}
			}
			writer.append("],\"cmdcnt\":").append(Integer.toString(sum));
			appendBusinessPathInfos(ureq, writer);
			writer.append("}");
			windowcommands.clear();
		}
	}
	
	private void appendBusinessPathInfos(UserRequest ureq, Writer writer) throws IOException {
		ChiefController ctrl = wboImpl.getChiefController();
		String documentTitle = ctrl == null ? "" : ctrl.getWindow().getTitle().getValue();
		writer.append(",\"documentTitle\":").append(JSONObject.quote(documentTitle));

		StringBuilder bc = new StringBuilder(128);
		HistoryPoint p = ureq.getUserSession().getLastHistoryPoint();
		if(p != null && StringHelper.containsNonWhitespace(p.getBusinessPath())) {
			List<ContextEntry> ces = p.getEntries();
			String uriPrefix = wboImpl.getWindow().getUriPrefix();
			bc.append(uriPrefix)
			  .append(BusinessControlFactory.getInstance().getAsRestPart(ces, true));
			writer.append(",\"businessPath\":").append(JSONObject.quote(bc.toString()));
		    writer.append(",\"historyPointId\":").append(JSONObject.quote(p.getUuid()));
		}
	}
	
	private void pushJSON(WindowCommand wc, Writer writer) throws IOException {
		Command c = wc.getCommand();
		String winId = wc.getWindowBackOffice().getWindow().getDispatchID();
		try {
			writer.append("{\"w\":\"").append(winId)
			      .append("\",\"cmd\":").append(Integer.toString(c.getCommand()))
			      .append(",\"cda\":");
			c.getSubJSON().write(writer);
			writer.append("}");
		} catch (JSONException e) {
			throw new AssertException("json exception:", e);
		}
	}

	public MediaResource extractMediaResource(boolean wrapHTML) {
		JSONObject json = getAndClearJSON(true);
		String res;
		if (wrapHTML) {
			// most ajax responses are a lot smaller than 16k
			StringBuilder sb = new StringBuilder(16384);
			sb.append("<html><head><script>\n/* <![CDATA[ */\nfunction invoke(){var r=")
			  .append(json.toString()).append("; ") 
			  .append("if (parent!=self&&parent.window.o_info) {")
			  .append("  parent.window.o_ainvoke(r);")
			  .append(" try{ parent.window.o_removeIframe(document.defaultView.frameElement.id); } catch(e) {} ")
					// normal case: ajax result can be delivered into the hidden iframe.
					// else: no parent frame or parent frame is not olat -> reasons:
					// a) mouse-right-click to open in new tab/window
					// b) fast double-click when target causes a 302 and browser's window's document has been updated, but old link was still clickable
					// c) ...
					// -> in all cases, do not show the json command, but reload the window which contained the link clicked (= window id of url)
			  .append("} else {") 
					// inform user that ajax-request cannot be opened in a new window
			  .append("  this.document.location=\"").append(StaticMediaDispatcher.createStaticURIFor("msg/json/en/info.html")).append("\";")
			  .append("}}")
			  .append("\n/* ]]> */\n</script></head><body onLoad=\"invoke()\"></body></html>");
			res = sb.toString();
		} else {
			res = json.toString();
		}

		StringMediaResource smr = new StringMediaResource();
		smr.setContentType("text/html;charset=utf-8");
		smr.setEncoding("utf-8");
		smr.setData(res);
		return smr;
	}

	private JSONObject createJSON(WindowCommand wc) {
		Command c = wc.getCommand();
		WindowBackOffice wbo = wc.getWindowBackOffice();
		String winId = wbo.getWindow().getDispatchID();
		JSONObject jo = new JSONObject();
		try {
			jo.put("cmd", c.getCommand());
			jo.put("w", winId);
			jo.put("cda", c.getSubJSON());
			return jo;
		} catch (JSONException e) {
			throw new AssertException("json exception:", e);
		}
	}

	/**
	 * @param moreCmds a List of WindowCommand objects
	 * @return
	 */
	private JSONObject getAndClearJSON(boolean clear) {
		JSONObject root = new JSONObject();

		try {
			if (Settings.isDebuging()) {
				long time = System.currentTimeMillis();
				root.put("time", time);
			}
			synchronized (windowcommands) { //o_clusterOK by:fj
				// handle all windowcommands now, create json
				int sum = windowcommands.size();
				root.put("cmdcnt", sum); // number of commands: 0..n
				if (sum > 0) {
					JSONArray ja = new JSONArray();
					root.put("cmds", ja);
					// treat commands waiting for the poll
					for (int i = 0; i < sum; i++) {
						WindowCommand wc = windowcommands.get(i);
						JSONObject jo = createJSON(wc);
						ja.put(jo);
					}
					if(clear) {
						windowcommands.clear();
					}
				}
				
			}
			return root;
		} catch (JSONException e) {
			throw new AssertException("wrong data put into json object", e);
		}
	}

	@Override
	protected void doDispose() {
		List<MapperKey> mappers = new ArrayList<>();
		mappers.add(mKey);
		mappers.add(sbmKey);
		CoreSpringFactory.getImpl(MapperService.class).cleanUp(mappers);
		if (ajaxEnabled && pollCount == 0) {
			//the controller should be older than 40s otherwise poll may not started yet
			if ((System.currentTimeMillis() - creationTime) > 40000) log.warn("Client did not send a single polling request though ajax is enabled!");
		}
        super.doDispose();
	}

	/**
	 * @param wco
	 */
	public void sendCommandTo(WindowCommand wco) {
		synchronized (windowcommands) { //o_clusterOK by:fj
			windowcommands.add(wco);
		}
	}

	/**
	 * @param enabled
	 */
	public void setAjaxEnabled(boolean enabled) {
		if (enabled) {
			mainP.setContent(myContent);
		} else {
			mainP.setContent(null);
		}
	}
	
	/**
	 * 
	 * @param pollperiod time in ms between two polls
	 */
	public void setPollPeriod(int pollperiod) {
		if (pollperiod == -1) {
			pollperiod = DEFAULT_POLLPERIOD;
		}
		if (pollperiod != this.pollperiod) {
			this.pollperiod = pollperiod;
			pollPeriodContent.contextPut("pollperiod", Integer.valueOf(pollperiod));
		} // else no need to change anything
	}
	
	private final class NothingChangedMediaResource extends DefaultMediaResource {
		
		@Override
		public long getCacheControlDuration() {
			return ServletUtil.CACHE_NO_CACHE;
		}

		@Override
		public void prepare(HttpServletResponse hres) {
			hres.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		}
	}
}

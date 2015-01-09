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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.admin.sysinfo.manager.SessionStatsManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.pushpoll.WindowCommand;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;

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
	private OLog log = Tracing.createLoggerFor(AjaxController.class);
	private VelocityContainer myContent;
	private VelocityContainer pollPeriodContent;
	private Panel mainP;
	private Panel pollperiodPanel;
	// protected only for performance improvement
	protected List<WindowCommand> windowcommands = new ArrayList<WindowCommand>(3);
	private Mapper m, sbm;
	private boolean showJSON = false;
	protected final WindowBackOfficeImpl wboImpl;
	
	private static final int DEFAULT_POLLPERIOD = 5000;//reasonable default value
	private int pollperiod = DEFAULT_POLLPERIOD;//reasonable default value
	private int pollCount = 0;
	private long creationTime = System.currentTimeMillis();
	private boolean ajaxEnabled;
	
	private final SessionStatsManager statsManager;

	AjaxController(UserRequest ureq, final WindowBackOfficeImpl wboImpl, boolean ajaxEnabled, String iframeName) {
		super(null);
		this.wboImpl = wboImpl;
		this.ajaxEnabled = ajaxEnabled;
		
		statsManager = CoreSpringFactory.getImpl(SessionStatsManager.class);
		
		pollPeriodContent = new VelocityContainer("jsserverpartpoll", VELOCITY_ROOT + "/pollperiod.html", null, this);
		pollPeriodContent.contextPut("pollperiod", new Integer(pollperiod));
		
		myContent = new VelocityContainer("jsserverpart", VELOCITY_ROOT + "/serverpart.html", null, this);
		myContent.contextPut("pollperiod", new Integer(pollperiod));
		
		//more debug information: OLAT-3529
		if (ajaxEnabled) myContent.contextPut("isAdmin", Boolean.valueOf(ureq.getUserSession().getRoles().isOLATAdmin()));
		
		// create a mapper to not block main traffic when polling (or vica versa)
		final Window window = wboImpl.getWindow();
		m = new Mapper() {
			public MediaResource handle(String relPath, HttpServletRequest request) {
				pollCount++;
				statsManager.incrementAuthenticatedPollerClick();

				String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
				UserRequest ureq = new UserRequestImpl(uriPrefix, request, null);
				boolean reload = false;
				Windows ws = Windows.getWindows(ureq);
				if(ws != null && ws.getChiefController() != null) {
					ChiefController cc = ws.getChiefController();
					reload = cc.wishAsyncReload(ureq, false);
				}
				
				// check for dirty components now.
				wboImpl.fireCycleEvent(Window.BEFORE_INLINE_RENDERING);
				Command updateDirtyCom = window.handleDirties();
				wboImpl.fireCycleEvent(Window.AFTER_INLINE_RENDERING);
				
				if (updateDirtyCom != null) {
					synchronized (windowcommands) { //o_clusterOK by:fj
						windowcommands.add(new WindowCommand(wboImpl, updateDirtyCom));
						if(reload) {
							String timestampID = ureq.getTimestampID();
							String reRenderUri = window.buildURIFor(window, timestampID, null);
							Command rmrcom = CommandFactory.createParentRedirectTo(reRenderUri);
							windowcommands.add(new WindowCommand(wboImpl, rmrcom));
						}
					}
				}
				return extractMediaResource(false);
			}
		};

		String uri = CoreSpringFactory.getImpl(MapperService.class).register(ureq.getUserSession(), m);
		myContent.contextPut("mapuri", uri);
		myContent.contextPut("iframeName", iframeName);
		myContent.contextPut("showJSON", Boolean.valueOf(showJSON));
		
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
		
			public MediaResource handle(String relPath, HttpServletRequest request) {
				StringMediaResource smr = new StringMediaResource();
				smr.setContentType("text/html;charset=utf-8");
				smr.setEncoding("utf-8");
				try {
					
					StringOutput slink = new StringOutput(50);
					StaticMediaDispatcher.renderStaticURI(slink, null);
					//slink now holds static url base like /olat/raw/700/
					
					URLBuilder ubu = new URLBuilder(WebappHelper.getServletContextPath() + DispatcherModule.PATH_AUTHENTICATED, "1", "1", null);
					StringOutput blink = new StringOutput(50);
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
				};
				return smr;
			}
		};
		String sburi = CoreSpringFactory.getImpl(MapperService.class).register(ureq.getUserSession(), sbm);
		myContent.contextPut("sburi", sburi);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public void setHighLightingEnabled(boolean enabled) {
		myContent.contextPut("highlight", Boolean.valueOf(enabled));
	}
	
	public void pushResource(Writer sb, boolean wrapHTML) throws IOException {
		if (wrapHTML) {
			// most ajax responses are a lot smaller than 16k
			sb.append("<html><head><script type=\"text/javascript\">\n/* <![CDATA[ */\nfunction invoke(){var r=");
			pushJSONAndClear(sb);
			sb.append("; ") 
				.append("if (parent!=self&&parent.window.o_info){")
				.append("parent.window.o_ainvoke(r);")
					// normal case: ajax result can be delivered into the hidden iframe.
					// else: no parent frame or parent frame is not olat -> reasons:
					// a) mouse-right-click to open in new tab/window
					// b) fast double-click when target causes a 302 and browser's window's document has been updated, but old link was still clickable
					// c) ...
					// -> in all cases, do not show the json command, but reload the window which contained the link clicked (= window id of url)
					
				.append("} else {") 
					// inform user that ajax-request cannot be opened in a new window,
					// todo felix: maybe send back request to bookmark-launch current url? -> new window?
					// we could then come near to what the user probably wanted when he/she opened a link in a new window
				.append("this.document.location=\"")
				.append(StaticMediaDispatcher.createStaticURIFor("msg/json/en/info.html"))
				.append("\";")
					//.append("window.open(self.location+\"?o_win_jsontop=1\"); this.close();")
					//.append("this.document.location=self.location+\"?o_win_jsontop=1\";")
				.append("}}\n/* ]]> */\n</script></head><body onLoad=\"invoke()\"></body></html>");
		} else {
			pushJSONAndClear(sb);
		}
	}
	
	public void pushJSONAndClear(Writer writer) throws IOException {
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
			writer.append("],\"cmdcnt\":").append(Integer.toString(sum)).append("}");
			windowcommands.clear();
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
		String jsonText = json.toString();
		//System.out.println("jsontext:"+jsonText);
		if (wrapHTML) {
			// most ajax responses are a lot smaller than 16k
			StringBuilder sb = new StringBuilder(16384);
			sb.append("<html><head><script type=\"text/javascript\">\n/* <![CDATA[ */\nfunction invoke(){var r=")
				.append(jsonText).append("; ") 
				.append("if (parent!=self&&parent.window.o_info){")
				.append("parent.window.o_ainvoke(r);")
					// normal case: ajax result can be delivered into the hidden iframe.
					// else: no parent frame or parent frame is not olat -> reasons:
					// a) mouse-right-click to open in new tab/window
					// b) fast double-click when target causes a 302 and browser's window's document has been updated, but old link was still clickable
					// c) ...
					// -> in all cases, do not show the json command, but reload the window which contained the link clicked (= window id of url)
					
				.append("} else {") 
					// inform user that ajax-request cannot be opened in a new window,
					// todo felix: maybe send back request to bookmark-launch current url? -> new window?
					// we could then come near to what the user probably wanted when he/she opened a link in a new window
				.append("this.document.location=\"")
				.append(StaticMediaDispatcher.createStaticURIFor("msg/json/en/info.html"))
				.append("\";")
					//.append("window.open(self.location+\"?o_win_jsontop=1\"); this.close();")
					//.append("this.document.location=self.location+\"?o_win_jsontop=1\";")
				.append("}}")
				.append("\n/* ]]> */\n</script></head><body onLoad=\"invoke()\">");
					if (showJSON) {
						try {
							sb.append("<pre style=\"font-size:12px;\">len:").append(jsonText.length()).append(":\n").append(StringEscapeUtils.escapeHtml(json.toString(2))).append("</pre>");
						} catch (JSONException e) {
							sb.append("error while prettyprinting json for debug mode: ").append(e.getMessage());
						}
					}
					sb.append("</body></html>");
					res = sb.toString();
		} else {
			res = jsonText;
		}

		StringMediaResource smr = new StringMediaResource();
		smr.setContentType("text/html;charset=utf-8");
		// TODO: check if it worked also with text/javascript
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

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		List<Mapper> mappers = new ArrayList<Mapper>();
		mappers.add(m);
		mappers.add(sbm);
		CoreSpringFactory.getImpl(MapperService.class).cleanUp(mappers);
		if (ajaxEnabled && pollCount == 0) {
			//the controller should be older than 40s otherwise poll may not started yet
			if ((System.currentTimeMillis() - creationTime) > 40000) log.warn("Client did not send a single polling request though ajax is enabled!");
		}
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
	 * @param showJSON The showJSON to set.
	 */
	public void setShowJSON(boolean showJSON) {
		this.showJSON = showJSON;
		myContent.contextPut("showJSON", Boolean.valueOf(showJSON));
	}
	
	/**
	 * 
	 * @param pollperiod time in ms between two polls
	 */
	public void setPollPeriod(int pollperiod) {
		if (pollperiod != this.pollperiod) {
			if (pollperiod == -1) pollperiod = DEFAULT_POLLPERIOD;
			this.pollperiod = pollperiod;
			pollPeriodContent.contextPut("pollperiod", new Integer(pollperiod));
		} // else no need to change anything
	}
}

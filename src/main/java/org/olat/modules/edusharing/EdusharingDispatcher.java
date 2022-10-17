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
package org.olat.modules.edusharing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.time.LocalDateTime;
import java.util.Properties;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.edu_sharing.webservices.authbyapp.AuthenticationException;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.dispatcher.LocaleNegotiator;
import org.olat.modules.edusharing.model.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 30 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EdusharingDispatcher implements Dispatcher {
	
	private static final Logger log = Tracing.createLoggerFor(EdusharingDispatcher.class);
	
	private static final String EDUSHARING_PATH = "edusharing";
	private static final String METADATA_PATH = "metadata";
	private static final String SEARCH_PATH = "search";
	private static final String SEARCH_CALLBACK_PATH = "searchcallback";
	
	@Autowired
	private EdusharingModule edusharingModule;
	@Autowired
	private EdusharingService edusharingService;
	@Autowired
	private EdusharingConversionService conversionService;
	
	public static final String getMetadataUrl() {
		return getBaseUrl().append(METADATA_PATH).toString();
	}

	public static final String getSearchUrl() {
		return getBaseUrl().append(SEARCH_PATH).toString();
	}
	
	public static final String getSearchCallbackUrl() {
		return getBaseUrl().append(SEARCH_CALLBACK_PATH).toString();
	}

	private static StringBuilder getBaseUrl() {
		return new StringBuilder()
				.append(Settings.getServerContextPathURI())
				.append("/")
				.append(EDUSHARING_PATH)
				.append("/");
	}
	
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!edusharingModule.isEnabled()) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.setContentType("text/plain");
			return;
		}
		
		UserRequest ureq = null;
		try{
			//upon creation URL is checked for
			ureq = new UserRequestImpl("edusharing", request, response);
		} catch(NumberFormatException nfe) {
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}

		try {
			String command = getCommand(request);
			if (METADATA_PATH.equals(command)) {
				buildMetadata(ureq, response);
			} else if (ureq.getUserSession().isAuthenticated()) {
				switch (command) {
				case "search":
					redirectToSearch(ureq, response);
					break;
				case SEARCH_CALLBACK_PATH:
					buildSearchCallback(ureq, response);
					break;
				case "preview":
					buildPreview(ureq, response);
					break;
				case "render":
					buildRender(ureq, response);
					break;
				case "goto":
					buildGoTo(ureq, response);
					break;
				default:
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				}
			} else {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}
		} catch (Exception e) {
			log.error("", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private String getCommand(HttpServletRequest request) {
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		final String origUri = request.getRequestURI();
		String encodedRestPart = origUri.substring(uriPrefix.length());
		String restPart = encodedRestPart;
		try {
			restPart = URLDecoder.decode(encodedRestPart, "UTF8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding", e);
		}
		
		String[] split = restPart.split("/");
		if (split.length >= 1) {
			return split[0];
		}
		return null;
	}

	/**
	 * Metadata to register OpenOLAT as application in edu-sharing.
	 *
	 * @param ureq
	 * @param response
	 * @throws IOException 
	 */
	private void buildMetadata(UserRequest ureq, HttpServletResponse response) throws Exception {
		Properties properties = edusharingService.getConfigForRegistration();
		properties.storeToXML(
				response.getOutputStream(),
				"Generated by OpenOlat on " + LocalDateTime.now(),
				"UTF-8");
	}
	
	private void redirectToSearch(UserRequest ureq, HttpServletResponse response) throws AuthenticationException {
		String language = LocaleNegotiator.getPreferedLocale(ureq).getLanguage();
		Ticket ticket = getTicket(ureq.getUserSession());
		
		String reurl = ureq.getParameter("reurl");
		if (!StringHelper.containsNonWhitespace(reurl)) {
			reurl = getSearchCallbackUrl();
		}
		
		String url = new StringBuilder()
				.append(edusharingModule.getBaseUrl())
				.append("components/search")
				.append("?locale=").append(language)
				.append("&ticket=").append(ticket.getTooken())
				.append("&reurl=").append(reurl)
				.toString();
		
		log.debug("edu-sharing start search {}", url);
		DispatcherModule.redirectTo(response, url);
	}

	private void buildSearchCallback(UserRequest ureq, HttpServletResponse response) throws IOException {
		log.debug("edu-sharing search callback url: " + ureq.getHttpReq().getRequestURL() + "?" + ureq.getHttpReq().getQueryString());
		
		SearchResult result = conversionService.toSearchResult(ureq);
		String json = conversionService.toJson(result);
		log.debug("edu-sharing search callback: {}", json);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>");
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<meta charset=\"UTF-8\">");
		sb.append("<script>");
		sb.append("  (function() {");
		sb.append(" \"use strict\";");
		sb.append(" window.parent.postMessage({ mceAction: \"mceEdusharingContent\", params: ").append(json).append("})}());");
		sb.append("</script>");
		sb.append("</head>");
		sb.append("<body />");
		sb.append("</html>");

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/html");
		response.getWriter().write(sb.toString());
		response.getWriter().flush();
		response.getWriter().close();
	}

	private void buildPreview(UserRequest ureq, HttpServletResponse response) throws Exception {
		log.debug("edu-sharing preview url: " + ureq.getHttpReq().getRequestURL() + "?" + ureq.getHttpReq().getQueryString());
		
		String objectUrl = ureq.getParameter("objectUrl");
		Ticket ticket = getTicket(ureq.getUserSession());
		
		try (EdusharingResponse edusharingResponse = edusharingService.getPreview(ticket, objectUrl)) {
			response.setStatus(edusharingResponse.getStatus());
			if (edusharingResponse.hasContent()) {
				response.setContentType(edusharingResponse.getMimeType());
				response.setContentLengthLong(edusharingResponse.getContentLength());
				stream(edusharingResponse.getContent(), response.getOutputStream());
			}
		}
	}

	private void buildRender(UserRequest ureq, HttpServletResponse response) throws Exception {
		log.debug("edu-sharing render url: " + ureq.getHttpReq().getRequestURL() + "?" + ureq.getHttpReq().getQueryString());
		
		String identifier = ureq.getParameter("identifier");
		String version = ureq.getParameter("version");
		String width = ureq.getParameter("width");
		String height = ureq.getParameter("height");
		Identity viewer = ureq.getUserSession().getIdentity();
		String language = LocaleNegotiator.getPreferedLocale(ureq).getLanguage();
		
		try (EdusharingResponse edusharingResponse = edusharingService.getRendered(viewer, identifier, version, width, height, language)) {
			response.setStatus(edusharingResponse.getStatus());
			if (edusharingResponse.hasContent()) {
				response.setContentType(edusharingResponse.getMimeType());
				response.setContentLengthLong(edusharingResponse.getContentLength());
				stream(edusharingResponse.getContent(), response.getOutputStream());
			}
		}
	}

	private void buildGoTo(UserRequest ureq, HttpServletResponse response) throws Exception {
		log.debug("edu-sharing go to url: " + ureq.getHttpReq().getRequestURL() + "?" + ureq.getHttpReq().getQueryString());
		
		String identifier = ureq.getParameter("identifier");
		Ticket ticket = getTicket(ureq.getUserSession());
		Identity viewer = ureq.getUserSession().getIdentity();
		String language = LocaleNegotiator.getPreferedLocale(ureq).getLanguage();
		String url = edusharingService.getRenderAsWindowUrl(ticket, viewer, identifier, language);
		
		if (!StringHelper.containsNonWhitespace(url)) {
			url = edusharingModule.getBaseUrl();
		}
		log.debug("edu-sharing go to " + url);
		DispatcherModule.redirectTo(response, url);
	}
	
	private Ticket getTicket(UserSession usess) throws AuthenticationException {
		Ticket ticket = null;
		Object ticketObject = usess.getEntry("edusharing-ticket");
		if (ticketObject instanceof Ticket) {
			ticket = (Ticket) ticketObject;
			ticket = edusharingService.validateTicket(ticket)
					.orElse(edusharingService.createTicket(usess.getIdentity()));
		} else {
			ticket = edusharingService.createTicket(usess.getIdentity());
		}
		usess.putEntry("edusharing-ticket", ticket);
		return ticket;
	}
	
	private static long stream(InputStream input, OutputStream output) throws IOException {
		try (
			ReadableByteChannel inputChannel = Channels.newChannel(input);
			WritableByteChannel outputChannel = Channels.newChannel(output);
		) {
			ByteBuffer buffer = ByteBuffer.allocateDirect(10240);
			long size = 0;

			while (inputChannel.read(buffer) != -1) {
				buffer.flip();
				size += outputChannel.write(buffer);
				buffer.clear();
			}
			return size;
		}
	}
	
}

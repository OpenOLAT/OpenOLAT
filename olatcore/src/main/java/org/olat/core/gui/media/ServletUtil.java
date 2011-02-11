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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.gui.media;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.gui.Windows;
import org.olat.core.gui.util.bandwidth.SlowBandWidthSimulator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;

/**
 * @author Felix Jost
 */
public class ServletUtil {
	private static final OLog log = Tracing.createLoggerFor(ServletUtil.class);

	/**
	 * @param httpReq
	 * @param httpResp
	 * @param mr
	 */
	public static void serveResource(HttpServletRequest httpReq, HttpServletResponse httpResp, MediaResource mr) {
		boolean debug = log.isDebug();

		try {
			Long lastModified = mr.getLastModified();
			if (lastModified != null) {
				// give browser a chance to cache images
				long ifModifiedSince = httpReq.getDateHeader("If-Modified-Since");
				// TODO: if no such header, what is the return value
				long lastMod = lastModified.longValue();
				if (ifModifiedSince >= (lastMod / 1000L) * 1000L) {
					httpResp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return;
				}
				httpResp.setDateHeader("Last-Modified", lastModified.longValue());
			}

			if (isFlashPseudoStreaming(httpReq)) {
				httpResp.setContentType("video/x-flv");
				pseudoStreamFlashResource(httpReq, httpResp, mr);
			} else {
				String mime = mr.getContentType();
				if (mime != null) {
					httpResp.setContentType(mime);
				}
				serveFullResource(httpReq, httpResp, mr);
			}
			
			// else there is no stream, but probably just headers
			// like e.g. in case of a 302 http-redirect
		} catch (Exception e) {
			if (debug) {
				log.warn("client browser abort when serving media resource", e);
			}
		} finally {
			try {
				mr.release();
			} catch (Exception e) {
				//we did our best here to clean up
			}
		}
	}
	
	private static boolean isFlashPseudoStreaming(HttpServletRequest httpReq) {
		String start = httpReq.getParameter("undefined");
		if(StringHelper.containsNonWhitespace(start)) {
			return true;
		}
		start = httpReq.getParameter("start");
		if(StringHelper.containsNonWhitespace(start)) {
			return true;
		}
		return false;
	}
	
	private static void serveFullResource(HttpServletRequest httpReq, HttpServletResponse httpResp,  MediaResource mr) {
		boolean debug = log.isDebug();
		
		InputStream in = null;
		OutputStream out = null;
		BufferedInputStream bis = null;

		try {
			Long size = mr.getSize();
			// if the size is known, set it to make browser's life easier
			if (size != null) {
				httpResp.setContentLength(size.intValue());
			}
			// maybe some more preparations
			mr.prepare(httpResp);
			
			in = mr.getInputStream();

			// serve the Resource
			if (in != null) {
				long rstart = 0;
				if (debug) {
					rstart = System.currentTimeMillis();
				}
				
				if (Settings.isDebuging()) {
					SlowBandWidthSimulator sbs = Windows.getWindows(UserSession.getUserSession(httpReq)).getSlowBandWidthSimulator();
					out = sbs.wrapOutputStream(httpResp.getOutputStream());
				} else {
					out = httpResp.getOutputStream();
				}
				
				// buffer input stream
				bis = new BufferedInputStream(in);
				FileUtils.copy(bis, out);
				if (debug) {
					long rstop = System.currentTimeMillis();
					log.debug("time to serve (mr="+mr.getClass().getName()+") "+ (size == null ? "n/a" : "" + size) + " bytes: " + (rstop - rstart));
				}
			}
		} catch (IOException e) {
			FileUtils.closeSafely(in);
			FileUtils.closeSafely(bis);
			FileUtils.closeSafely(out);
			log.error("client browser probably abort when serving media resource", e);
		}
	}
	

	private static void pseudoStreamFlashResource(HttpServletRequest httpReq, HttpServletResponse httpResp,  MediaResource mr) {
		Long range = getRange(httpReq);
		long seekPos = range == null ? 0l : range.longValue();
		long fileSize = mr.getSize() - ((seekPos > 0) ? seekPos  + 1 : 0);

		InputStream s = null;
		OutputStream out = null;
		
		try {
			s = new BufferedInputStream(mr.getInputStream());
			out = httpResp.getOutputStream();

			if(seekPos == 0) {
				httpResp.addHeader("Content-Length", Long.toString(fileSize));
			} else {
				httpResp.addHeader("Content-Length", Long.toString(fileSize + 13));
				byte[] flvHeader = new byte[] {70, 76, 86, 1, 1, 0, 0, 0, 9, 0, 0, 0, 9};
				out.write(flvHeader);
			}

			s.skip(seekPos);
			
			int readSize = 0;

			final int bufferSize = 1024 * 10;
			long left = fileSize;
			while (left > 0) {
				int howMuch = bufferSize;
				if (howMuch > left) {
					howMuch = (int) left;
				}

				byte[] buf = new byte[howMuch];
				int numRead = s.read(buf);
				readSize += numRead;

				out.write(buf, 0, numRead);
				httpResp.flushBuffer();

				if (numRead == -1) {
					break;
				}

				left -= numRead;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("Eof")) {
				//ignore
			} else {
				throw new RuntimeException(e);
			}
		} finally {
			FileUtils.closeSafely(s);
		}
	}
	
	
	private static Long getRange(HttpServletRequest httpReq) {
		if (httpReq.getParameter("start") != null) {
			return Long.parseLong(httpReq.getParameter("start"));
		} else if (httpReq.getParameter("undefined") != null) {
			return Long.parseLong(httpReq.getParameter("undefined"));
		}
		return null;
	}

	/**
	 * @param response
	 * @param result
	 */
	public static void serveStringResource(HttpServletRequest httpReq, HttpServletResponse response, String result) {
		// we ignore the accept-charset from the request and always write in utf-8
		// -> see comment below
		response.setContentType("text/html;charset=utf-8");
		// never allow to cache pages since they contain a timestamp valid only once
		// HTTP 1.1
		response.setHeader("Cache-Control", "private, no-cache, no-store, must-revalidate, proxy-revalidate, s-maxage=0, max-age=0");
		// HTTP 1.0
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);

		// log the response headers prior to sending the output
		boolean isDebug = log.isDebug();
		
		if (isDebug) {
			log.debug("\nResponse headers (some)\ncontent type:" + response.getContentType() + "\ncharacterencoding:"
					+ response.getCharacterEncoding() + "\nlocale:" + response.getLocale());
		}

		try {
			long rstart = 0;
			if (isDebug) {
				rstart = System.currentTimeMillis();
			}
			// make a ByteArrayOutputStream to be able to determine the length.
			// buffer size: assume average length of a char in bytes is max 2
			ByteArrayOutputStream baos = new ByteArrayOutputStream(result.length() * 2);

			// we ignore the accept-charset from the request and always write in
			// utf-8:
			// we have lots of different languages (content) in one application to
			// support, and more importantly,
			// a blend of olat translations and content by authors which can be in
			// different languages.
			OutputStreamWriter osw = new OutputStreamWriter(baos, "utf-8");
			osw.write(result);
			osw.close();
			// the data is now utf-8 encoded in the bytearray -> push it into the outputstream
			int encLen = baos.size();
			response.setContentLength(encLen);
			
			OutputStream os;
			if (Settings.isDebuging()) {
				SlowBandWidthSimulator sbs = Windows.getWindows(UserSession.getUserSession(httpReq)).getSlowBandWidthSimulator();
				os = sbs.wrapOutputStream(response.getOutputStream());	
			} else {
				os = response.getOutputStream();
			}
			byte[] bout = baos.toByteArray();
			os.write(bout);
			os.close();
			
			if (isDebug) {
				long rstop = System.currentTimeMillis();
				log.debug("time to serve inline-resource " + result.length() + " chars / " + encLen + " bytes: " 
					+ (rstop - rstart));
			}
		} catch (IOException e) {
			if (isDebug) {
				log.warn("client browser abort when serving inline", e);
			}
		}
	}
}
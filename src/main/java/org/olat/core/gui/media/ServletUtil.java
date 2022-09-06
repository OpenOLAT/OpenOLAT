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

package org.olat.core.gui.media;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.admin.sysinfo.manager.SessionStatsManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;

/**
 * @author Felix Jost
 */
public class ServletUtil {
	private static final Logger log = Tracing.createLoggerFor(ServletUtil.class);
	
	public static final long CACHE_NO_CACHE = 0l;
	public static final long CACHE_ONE_HOUR = 60l * 60l;
	public static final long CACHE_ONE_DAY = 24l * 60l * 60l;
	
	
	public static final void printOutRequestParameters(HttpServletRequest request) {
		for(Enumeration<String> names=request.getParameterNames(); names.hasMoreElements(); ) {
			String name = names.nextElement();
			log.info("Parameter {} :: {}", name, request.getParameter(name));
		}
	}
	
	public static final void printOutRequestHeaders(HttpServletRequest request) {
		for(Enumeration<String> headers=request.getHeaderNames(); headers.hasMoreElements(); ) {
			String header = headers.nextElement();
			log.info("Header {} :: {}", header, request.getHeader(header));
		}
	}
	
	public static final void printOutRequestCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie=cookies[i];
				String cookieName = cookie.getName();
				String cookieValue = cookie.getValue();
				if (!StringHelper.containsNonWhitespace(cookieName) ) continue;

				log.info("Cookie {} :: {}", cookieName, cookieValue);
			}
		}
	}
	
	public static String getCookie(HttpServletRequest request, String lookup) {
		if ( request == null || lookup == null ) return null;

		// https://stackoverflow.com/questions/11047548/getting-cookie-in-servlet
		Cookie[] cookies = request.getCookies();
		if ( cookies == null ) return null;
		for (int i = 0; i < cookies.length; i++) {
			Cookie cookie=cookies[i];
			String cookieName = cookie.getName();
			String cookieValue = cookie.getValue();
			if (!StringHelper.containsNonWhitespace(cookieName) ) continue;
			if ( cookieName.equalsIgnoreCase(lookup) ) {
				return cookieValue;
			}
		}
		return null;
	}
	
	public static final boolean acceptJson(HttpServletRequest request) {
		boolean acceptJson = false;
		for(Enumeration<String> headers=request.getHeaders("Accept"); headers.hasMoreElements(); ) {
			String accept = headers.nextElement();
			if(accept.contains("application/json")) {
				acceptJson = true;
			}
		}
		return acceptJson;
	}
	
	public static final String getUserAgent(HttpServletRequest request) {
		return request == null ? null : request.getHeader("User-Agent");
	}
	
	/**
	 * @param httpReq
	 * @param httpResp
	 * @param mr
	 */
	public static void serveResource(HttpServletRequest httpReq, HttpServletResponse httpResp, MediaResource mr) {
		boolean debug = log.isDebugEnabled();
		try {
			Long lastModified = mr.getLastModified();
			if (lastModified != null) {
				// give browser a chance to cache images
				long ifModifiedSince = httpReq.getDateHeader("If-Modified-Since");
				long lastMod = lastModified.longValue();
				if (ifModifiedSince >= (lastMod / 1000L) * 1000L) {
					httpResp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return;
				}
				httpResp.setDateHeader("Last-Modified", lastModified.longValue());
			}

			if (isFlashPseudoStreaming(httpReq, mr)) {
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
	
	private static boolean isFlashPseudoStreaming(HttpServletRequest httpReq, MediaResource mr) {
		//exclude some mappers which cannot be flash
		if(mr instanceof JSONMediaResource) {
			return false;
		}
		
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
		boolean debug = log.isDebugEnabled();
		
		InputStream in = null;
		OutputStream out = null;
		BufferedInputStream bis = null;

		try {
			// cache-control first
			setCacheHeaders(httpResp, mr.getCacheControlDuration());
			
			Long size = mr.getSize();
			Long lastModified = mr.getLastModified();
			// accept range to deliver videos for iPad (implementation based on Tomcat)
			List<Range> ranges = parseRange(httpReq, httpResp, (lastModified == null ? -1 : lastModified.longValue()), (size == null ? 0 : size.longValue()));
			if(ranges != null && mr.acceptRanges()) {
				httpResp.setHeader("Accept-Ranges", "bytes");
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
				
				out = httpResp.getOutputStream();

				if (ranges != null && ranges.size() == 1) {
					
					Range range = ranges.get(0);
					httpResp.addHeader("Content-Range", "bytes " + range.start + "-" + range.end + "/" + range.length);
					long length = range.end - range.start + 1;
					if (length < Integer.MAX_VALUE) {
						httpResp.setContentLengthLong(length);
					} else {
						// Set the content-length as String to be able to use a long
						httpResp.setHeader("content-length", "" + length);
					}
					httpResp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

					int bufferSize = httpResp.getBufferSize();
					copy(out, in, range, bufferSize);
				} else {
					if (size != null) {
						httpResp.setContentLengthLong(size.longValue());
					}
					int bufferSize = httpResp.getBufferSize();
					// buffer input stream
					bis = new BufferedInputStream(in, bufferSize);
					IOUtils.copyLarge(bis, out, new byte[bufferSize]);
				}
				
				if (debug) {
					long rstop = System.currentTimeMillis();
					log.debug("time to serve (mr={}) {} bytes: {}", mr.getClass().getName(), (size == null ? "n/a" : "" + size), (rstop - rstart));
				}
			}
		} catch (IOException e) {
			FileUtils.closeSafely(out);
			handleIOException("client browser probably abort when serving media resource", e);
		} finally {
			IOUtils.closeQuietly(bis);
			IOUtils.closeQuietly(in);
		}
	}
	
	public static final void handleIOException(String msg, Exception e) {
		try {
			String className = e.getClass().getSimpleName();
			if("ClientAbortException".equals(className)) {
				log.debug("client browser probably abort during operaation", e);
			} else {
				log.error(msg, e);
			}
		} catch (Exception e1) {
			log.error("", e1);
		}
	}
	
	protected static void copy(OutputStream ostream, InputStream resourceInputStream, Range range, int bufferSize) throws IOException {
		IOException exception = null;
		
		SessionStatsManager stats = CoreSpringFactory.getImpl(SessionStatsManager.class);

		try(InputStream istream = (resourceInputStream instanceof BufferedInputStream)
				? resourceInputStream : new BufferedInputStream(resourceInputStream, bufferSize)) {
			stats.incrementConcurrentStreamCounter();
			exception = copyRange(istream, ostream, range.start, range.end, bufferSize);
		} catch(IOException e) {
			handleIOException("Deliver range of data", e);
		} catch(Exception e) {
			log.error("", e);
		} finally {
			stats.decrementConcurrentStreamCounter();
		}

		// Rethrow any exception that has occurred
		if (exception != null) throw exception;
	}
	
	protected static IOException copyRange(InputStream istream, OutputStream ostream, long start, long end, int bufferSize) {
		try {
			istream.skip(start);
		} catch (IOException e) {
			return e;
		}

		IOException exception = null;
		long bytesToRead = end - start + 1;

		byte[] buffer = new byte[bufferSize];
		int len = buffer.length;
		while ((bytesToRead > 0) && (len >= buffer.length)) {
			try {
				len = istream.read(buffer);
				if (bytesToRead >= len) {
					ostream.write(buffer, 0, len);
					bytesToRead -= len;
				} else {
					ostream.write(buffer, 0, (int) bytesToRead);
					bytesToRead = 0;
				}
			} catch (IOException e) {
				exception = e;
				len = -1;
			}
			if (len < buffer.length) break;
		}

		return exception;
	}

	protected static List<Range> parseRange(HttpServletRequest request, HttpServletResponse response, long lastModified, long fileLength)
			throws IOException {
		
		String headerValue = request.getHeader("If-Range");

    if (headerValue != null) {
        long headerValueTime = (-1L);
        try {
          headerValueTime = request.getDateHeader("If-Range");
        } catch (IllegalArgumentException e) {
          //
        }

        if (headerValueTime != (-1L)) {
            // If the timestamp of the entity the client got is older than
            // the last modification date of the entity, the entire entity
            // is returned.
            if (lastModified > (headerValueTime + 1000))
                return Collections.emptyList();
        }
    }

		if (fileLength == 0) return null;

		// Retrieving the range header (if any is specified
		String rangeHeader = request.getHeader("Range");

		if (rangeHeader == null) return null;
		// bytes is the only range unit supported (and I don't see the point
		// of adding new ones).
		if (!rangeHeader.startsWith("bytes")) {
			response.addHeader("Content-Range", "bytes */" + fileLength);
			response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
			return null;
		}

		rangeHeader = rangeHeader.substring(6);

		// Vector which will contain all the ranges which are successfully
		// parsed.
		List<Range> result = new ArrayList<>();
		StringTokenizer commaTokenizer = new StringTokenizer(rangeHeader, ",");

		// Parsing the range list
		while (commaTokenizer.hasMoreTokens()) {
			String rangeDefinition = commaTokenizer.nextToken().trim();

			Range currentRange = new Range();
			currentRange.length = fileLength;

			int dashPos = rangeDefinition.indexOf('-');

			if (dashPos == -1) {
				response.addHeader("Content-Range", "bytes */" + fileLength);
				response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
				return null;
			}

			if (dashPos == 0) {

				try {
					long offset = Long.parseLong(rangeDefinition);
					currentRange.start = fileLength + offset;
					currentRange.end = fileLength - 1;
				} catch (NumberFormatException e) {
					response.addHeader("Content-Range", "bytes */" + fileLength);
					response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
					return null;
				}

			} else {

				try {
					currentRange.start = Long.parseLong(rangeDefinition.substring(0, dashPos));
					if (dashPos < rangeDefinition.length() - 1) currentRange.end = Long.parseLong(rangeDefinition.substring(dashPos + 1,
							rangeDefinition.length()));
					else currentRange.end = fileLength - 1;
				} catch (NumberFormatException e) {
					response.addHeader("Content-Range", "bytes */" + fileLength);
					response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
					return null;
				}

			}

			if (!currentRange.validate()) {
				response.addHeader("Content-Range", "bytes */" + fileLength);
				response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
				return null;
			}

			result.add(currentRange);
		}

		return result;
	}
  
	private static void pseudoStreamFlashResource(HttpServletRequest httpReq, HttpServletResponse httpResp,  MediaResource mr) {
		Long range = getRange(httpReq);
		long seekPos = range == null ? 0l : range.longValue();
		long fileSize = mr.getSize() - ((seekPos > 0) ? seekPos  + 1 : 0);

		InputStream s = null;
		OutputStream out = null;
		
		try {
			setCacheHeaders(httpResp, mr.getCacheControlDuration());
			
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

			final int bufferSize = 1024 * 10;
			long left = fileSize;
			while (left > 0) {
				int howMuch = bufferSize;
				if (howMuch > left) {
					howMuch = (int) left;
				}

				byte[] buf = new byte[howMuch];
				int numRead = s.read(buf);

				out.write(buf, 0, numRead);
				httpResp.flushBuffer();

				if (numRead == -1) {
					break;
				}

				left -= numRead;
			}
		}
		catch (Exception e) {
			log.error("", e);
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
	public static void serveStringResource(HttpServletResponse response, String result) {
		setStringResourceHeaders(response);

		// log the response headers prior to sending the output
		boolean isDebug = log.isDebugEnabled();
		
		if (isDebug) {
			log.debug("\nResponse headers (some)\ncontent type:" + response.getContentType() + "\ncharacterencoding:"
					+ response.getCharacterEncoding() + "\nlocale:" + response.getLocale());
		}

		try {
			setNoCacheHeaders(response);
			
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
			
			OutputStream os = response.getOutputStream();
			byte[] bout = baos.toByteArray();
			os.write(bout);
			os.close();
			
			if (isDebug) {
				long rstop = System.currentTimeMillis();
				log.debug("time to serve inline-resource {} chars / {} bytes: {}", result.length(), encLen, (rstop - rstart));
			}
		} catch (IOException e) {
			if (isDebug) {
				log.warn("client browser abort when serving inline", e);
			}
		}
	}

	public static boolean serveStringResource(HttpServletResponse response, StringOutput result) {
		setStringResourceHeaders(response);

		// log the response headers prior to sending the output
		boolean isDebug = log.isDebugEnabled();
		if (isDebug) {
			log.debug("\nResponse headers (some)\ncontent type:" + response.getContentType() + "\ncharacterencoding:"
					+ response.getCharacterEncoding() + "\nlocale:" + response.getLocale());
		}

		try(PrintWriter os = response.getWriter();
				Reader reader = result.getReader()) {
			// make a ByteArrayOutputStream to be able to determine the length.
			// buffer size: assume average length of a char in bytes is max 2
			IOUtils.copy(reader, os);
		} catch (IllegalStateException e) {
			debugIllegalGetOutputStream(response, result);
			log.error("Illegal getWriter", e);
			return false;
		} catch (IOException e) {
			if (isDebug) {
				log.warn("client browser abort when serving inline", e);
			}
		}
		return true;
	}
	
	private static void debugIllegalGetOutputStream(HttpServletResponse response, StringOutput result) {
		try {
			for(String header:response.getHeaderNames()) {
				log.error("Illegal getWriter: " + header + " :: " + response.getHeader(header));
			}
		} catch (Exception e) {
			log.error("Illegal getWriter: ", e);
		}
		log.error(result.toString());
	}
	
	public static void setStringResourceHeaders(HttpServletResponse response) {
		// we ignore the accept-charset from the request and always write in utf-8
		// -> see comment below
		response.setContentType("text/html;charset=utf-8");
		// never allow to cache pages since they contain a timestamp valid only once
		setNoCacheHeaders(response);
	}
	
	public static void setJSONResourceHeaders(HttpServletResponse response) {
		// we ignore the accept-charset from the request and always write in utf-8
		// -> see comment below
		response.setContentType("application/json;charset=utf-8");
		// never allow to cache pages since they contain a timestamp valid only once
		setNoCacheHeaders(response);
	}
	
	public static void setCacheHeaders(HttpServletResponse response, long duration) {
		if(duration == 0) {
			setNoCacheHeaders(response);
		} else {
			long now = System.currentTimeMillis();
			response.addHeader("Cache-Control", "max-age=" + duration);
			response.setDateHeader("Expires", now + duration);
		}
	}
	
	public static void setNoCacheHeaders(HttpServletResponse response) {
		// HTTP 1.1
		response.setHeader("Cache-Control", "private, no-cache, no-store, must-revalidate, proxy-revalidate, s-maxage=0, max-age=0");
		// HTTP 1.0
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
	}
	
	/**
	 * Return a context-relative path, beginning with a "/", that represents the
	 * canonical version of the specified path
	 * <p>
	 * ".." and "." elements are resolved out. If the specified path attempts to
	 * go outside the boundaries of the current context (i.e. too many ".." path
	 * elements are present), return <code>null</code> instead.
	 * <p>
	 * 
	 * @author Mike Stock
	 * 
	 * @param path Path to be normalized
	 * @return the normalized path
	 */
	public static String normalizePath(String path) {
		if (path == null) return null;

		// Create a place for the normalized path
		String normalized = path;

		try { // we need to decode potential UTF-8 characters in the URL
			normalized = new String(normalized.getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertException("utf-8 encoding must be supported on all java platforms...");
		}

		if (normalized.equals("/.")) return "/";

		// Normalize the slashes and add leading slash if necessary
		if (normalized.indexOf('\\') >= 0) normalized = normalized.replace('\\', '/');
		if (!normalized.startsWith("/")) normalized = "/" + normalized;

		// Resolve occurrences of "//" in the normalized path
		while (true) {
			int index = normalized.indexOf("//");
			if (index < 0) break;
			normalized = normalized.substring(0, index) + normalized.substring(index + 1);
		}

		// Resolve occurrences of "/./" in the normalized path
		while (true) {
			int index = normalized.indexOf("/./");
			if (index < 0) break;
			normalized = normalized.substring(0, index) + normalized.substring(index + 2);
		}

		// Resolve occurrences of "/../" in the normalized path
		while (true) {
			int index = normalized.indexOf("/../");
			if (index < 0) break;
			if (index == 0) return (null); // Trying to go outside our context
			int index2 = normalized.lastIndexOf('/', index - 1);
			normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
		}

		// Return the normalized path that we have completed
		return (normalized);
	}
	
    /**
     * Extracts a quoted value from a header that has a given key. For instance if the header is
     * <p>
     * content-disposition=form-data; name="my field"
     * and the key is name then "my field" will be returned without the quotes.
     *
     * @param header The header
     * @param key    The key that identifies the token to extract
     * @return The token, or null if it was not found
     */
    public static String extractQuotedValueFromHeader(final String header, final String key) {

        int keypos = 0;
        int pos = -1;
        boolean inQuotes = false;
        for (int i = 0; i < header.length() - 1; ++i) { //-1 because we need room for the = at the end
            char c = header.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    inQuotes = false;
                }
            } else {
                if (key.charAt(keypos) == c) {
                    keypos++;
                } else if (c == '"') {
                    keypos = 0;
                    inQuotes = true;
                } else {
                    keypos = 0;
                }
                if (keypos == key.length()) {
                    if (header.charAt(i + 1) == '=') {
                        pos = i + 2;
                        break;
                    } else {
                        keypos = 0;
                    }
                }
            }

        }
        if (pos == -1) {
            return null;
        }

        int end;
        int start = pos;
        if (header.charAt(start) == '"') {
            start++;
            for (end = start; end < header.length(); ++end) {
                char c = header.charAt(end);
                if (c == '"') {
                    break;
                }
            }
            return header.substring(start, end);

        } else {
            //no quotes
            for (end = start; end < header.length(); ++end) {
                char c = header.charAt(end);
                if (c == ' ' || c == '\t' || c == ';') {
                    break;
                }
            }
            return header.substring(start, end);
        }
    }
	
	//fxdiff FXOLAT-118: accept range to deliver videos for iPad
  protected static class Range {
    public long start;
    public long end;
    public long length;

    /**
     * Validate range.
     */
    public boolean validate() {
        if (end >= length)
            end = length - 1;
        return ( (start >= 0) && (end >= 0) && (start <= end)
                 && (length > 0) );
    }

    public void recycle() {
        start = 0;
        end = 0;
        length = 0;
    }
    
    @Override
    public String toString() {
    	return start + "-" + end + "/" + length;
    }
  }
}
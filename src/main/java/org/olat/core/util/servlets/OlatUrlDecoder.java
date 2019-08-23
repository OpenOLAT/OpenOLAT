package org.olat.core.util.servlets;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Initial date: 2016-06-30<br />
 * @author sev26 (UZH)
 */
public class OlatUrlDecoder {

	private static final Logger log = Tracing.createLoggerFor(OlatUrlDecoder.class);

	/**
	 * "request.getRequestURI()" does not work here because it ignores certain
	 * valid path string characters like ';'.
	 */
	public static String getFullUri(HttpServletRequest request) {
		StringBuffer tmp = request.getRequestURL();
		int fromIndex = 0;
		for (int i = 0; i < 3; i++) {
			fromIndex = tmp.indexOf("/", fromIndex + 1);
		}

		String result = tmp.substring(fromIndex) +
				(request.getQueryString() != null ? "?" + request.getQueryString() : "");
		try {
			result = result.replaceAll("\\+", "%2B");
			result = java.net.URLDecoder.decode(result, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("", e);
		}

		return result;
	}

	/*
	 * "request.getPathInfo()" does not work here because it ignores certain
	 * valid path string characters like ';'.
	 */
	public static String getFullPathInfo(HttpServletRequest request) {
		Path path = Paths.get(OlatUrlDecoder.getFullUri(request));
		assert path.isAbsolute();
		return path.normalize().toString();
	}
}

package org.olat.core;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class OlatServletResource {

	private static ServletContextResourcePatternResolver servletContextResourcePatternResolver =
			new ServletContextResourcePatternResolver(CoreSpringFactory.servletContext);

	private static Resource[] getAllDirectoryPathsOfPath(Object path) throws IOException {
		return servletContextResourcePatternResolver.getResources(
				path.toString() + "/*/");
	}

	private static Resource[] getAllDirectoryPathsOfPaths(Object[] paths) throws IOException {
		ArrayList<Resource> tmp = new ArrayList<>();
		for (Object path : paths) {
			tmp.addAll((Collection<Resource>) Arrays.asList(
					getAllDirectoryPathsOfPath(path)));
		}
		return tmp.toArray(new Resource[tmp.size()]);
	}

	public static Resource[] getAll(String sourcePath) throws IOException {
		return servletContextResourcePatternResolver.getResources(sourcePath + "/**");
	}

	public static List<String> getAllDirectoriesOfPaths(Object[] paths) throws IOException {
		ArrayList<String> result = new ArrayList<>();

		Resource[] resources = getAllDirectoryPathsOfPaths(paths);
		for (Resource resource : resources) {
			String path = resource.getURL().getPath();
			for (int i = path.length() - 3; i > 0; i--) {
				if (path.charAt(i) == '/') {
					result.add(path.substring(i + 1, path.length() - 1));
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Keep this method for now in case the others have performance issues.
	 */
	@Deprecated
	public static List<String> getAllDirectoriesOfPathsOld(Object[] paths) throws IOException {
		try {
			ArrayList<String> result = new ArrayList<>();

			for (Object path : paths) {
				URL url = CoreSpringFactory.servletContext.getResource(path.toString());
				assert url != null : "Themes path does not exist: " + path;
				String[] resources = url.getPath().split("!");
				assert resources.length > 0 : "Not a path to a resource within a JAR: " + url.getPath();
				InputStream inputStream = new FileInputStream(URLDecoder
						.decode(url.getPath().substring(5,
								resources[0].length()), "UTF-8"));
				JarInputStream jarInputStream = new JarInputStream(inputStream);
				outerLoop : for (int i = 1; i < resources.length - 1; i++) {
					for (ZipEntry zipEntry; (zipEntry = jarInputStream
							.getNextEntry()) != null;) {
						if (zipEntry.getName().regionMatches(0,
								resources[i], 1, resources[i].length() - 1)) {
							jarInputStream = new JarInputStream(jarInputStream);
							break outerLoop;
						}
					}
					assert false : "JAR '" + resources[i] + "' not found inside of the JAR '" + resources[i - 1] + "'.";
				}
				String directoryPath = resources[resources.length - 1].substring(1);
				/*
				 * Jetty and Tomcat return a directory path string that
				 * differs by the ending.
				 */
				if (directoryPath.endsWith("/") == false) {
					directoryPath += "/";
				}
				for (ZipEntry zipEntry; (zipEntry = jarInputStream
						.getNextEntry()) != null;) {
					String name = zipEntry.getName();
					if (name.startsWith(directoryPath)) {
						if (name.indexOf('/', directoryPath.length()) == name.length() - 1) {
							result.add(name.substring(directoryPath.length(),
									name.length() - 1));
						}
					}
				}
			}

			return result;
		} catch (MalformedURLException | UnsupportedEncodingException e) {
			assert false : e;
			throw e;
		}
	}

	public static String pathRelativeToSourcePath(URL url, String sourcePath) throws IOException {
		String path = url.getPath();
		int index = path.indexOf(sourcePath);
		return path.substring(index + sourcePath.length());
	}

	public static void appendDirectory(ZipOutputStream zout, String sourcePath,
									   String zipPath) throws IOException {
		Resource[] resources = servletContextResourcePatternResolver.getResources(
				sourcePath + "/**");
		for (Resource resource : resources) {
			URL url = resource.getURL();
			String zipFilePath = zipPath + pathRelativeToSourcePath(url, sourcePath);
			zout.putNextEntry(new ZipEntry(zipFilePath));
			if (url.getPath().endsWith("/") == false) {
				IOUtils.copy(resource.getInputStream(), zout);
			}
			zout.closeEntry();
		}
	}
}

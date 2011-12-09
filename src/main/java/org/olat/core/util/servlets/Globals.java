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
*/
package org.olat.core.util.servlets;


/**
 * Global constants that are applicable to multiple packages within Catalina.
 *
 * @author Craig R. McClanahan
 */

public final class Globals {


    /**
     * The request attribute under which we store the array of X509Certificate
     * objects representing the certificate chain presented by our client,
     * if any.
     */
    public static final String CERTIFICATES_ATTR =
        "javax.servlet.request.X509Certificate";

    /**
     * SSL Certificate Request Attributite.
     */
    public static final String SSL_CERTIFICATE_ATTR = "org.apache.coyote.request.X509Certificate";

    /**
     * The request attribute under which we store the name of the cipher suite
     * being used on an SSL connection (as an object of type
     * java.lang.String).
     */
    public static final String CIPHER_SUITE_ATTR =
        "javax.servlet.request.cipher_suite";


    /**
     * The servlet context attribute under which we store the class loader
     * used for loading servlets (as an object of type java.lang.ClassLoader).
     */
    public static final String CLASS_LOADER_ATTR =
        "org.apache.catalina.classloader";


    /**
     * The JNDI directory context which is associated with the context. This
     * context can be used to manipulate static files.
     */
    public static final String RESOURCES_ATTR =
        "org.apache.catalina.resources";


    /**
     * The servlet context attribute under which we store the class path
     * for our application class loader (as an object of type String),
     * delimited with the appropriate path delimiter for this platform.
     */
    public static final String CLASS_PATH_ATTR =
        "org.apache.catalina.jsp_classpath";


    /**
     * The request attribute under which the original context path is stored
     * on an included dispatcher request.
     */
    public static final String CONTEXT_PATH_ATTR =
        "javax.servlet.include.context_path";


    /**
     * The request attribute under which we forward a Java exception
     * (as an object of type Throwable) to an error page.
     */
    public static final String EXCEPTION_ATTR =
        "javax.servlet.error.exception";


    /**
     * The request attribute under which we forward the request URI
     * (as an object of type String) of the page on which an error occurred.
     */
    public static final String EXCEPTION_PAGE_ATTR =
        "javax.servlet.error.request_uri";


    /**
     * The request attribute under which we forward a Java exception type
     * (as an object of type Class) to an error page.
     */
    public static final String EXCEPTION_TYPE_ATTR =
        "javax.servlet.error.exception_type";


    /**
     * The request attribute under which we forward an HTTP status message
     * (as an object of type STring) to an error page.
     */
    public static final String ERROR_MESSAGE_ATTR =
        "javax.servlet.error.message";


    /**
     * The request attribute under which the Invoker servlet will store
     * the invoking servlet path, if it was used to execute a servlet
     * indirectly instead of through a servlet mapping.
     */
    public static final String INVOKED_ATTR =
        "org.apache.catalina.INVOKED";


    /**
     * The request attribute under which we expose the value of the
     * <code>&lt;jsp-file&gt;</code> value associated with this servlet,
     * if any.
     */
    public static final String JSP_FILE_ATTR =
        "org.apache.catalina.jsp_file";


    /**
     * The request attribute under which we store the key size being used for
     * this SSL connection (as an object of type java.lang.Integer).
     */
    public static final String KEY_SIZE_ATTR =
        "javax.servlet.request.key_size";


    /**
     * The servlet context attribute under which the managed bean Registry
     * will be stored for privileged contexts (if enabled).
     */
    public static final String MBEAN_REGISTRY_ATTR =
        "org.apache.catalina.Registry";


    /**
     * The servlet context attribute under which the MBeanServer will be stored
     * for privileged contexts (if enabled).
     */
    public static final String MBEAN_SERVER_ATTR =
        "org.apache.catalina.MBeanServer";


    /**
     * The request attribute under which we store the servlet name on a
     * named dispatcher request.
     */
    public static final String NAMED_DISPATCHER_ATTR =
        "org.apache.catalina.NAMED";


    /**
     * The request attribute under which the original path info is stored
     * on an included dispatcher request.
     */
    public static final String PATH_INFO_ATTR =
        "javax.servlet.include.path_info";


    /**
     * The request attribute under which the original query string is stored
     * on an included dispatcher request.
     */
    public static final String QUERY_STRING_ATTR =
        "javax.servlet.include.query_string";


    /**
     * The request attribute under which the original request URI is stored
     * on an included dispatcher request.
     */
    public static final String REQUEST_URI_ATTR =
        "javax.servlet.include.request_uri";


    /**
     * The request attribute under which we forward a servlet name to
     * an error page.
     */
    public static final String SERVLET_NAME_ATTR =
        "javax.servlet.error.servlet_name";


    /**
     * The request attribute under which the original servlet path is stored
     * on an included dispatcher request.
     */
    public static final String SERVLET_PATH_ATTR =
        "javax.servlet.include.servlet_path";


    /**
     * The name of the cookie used to pass the session identifier back
     * and forth with the client.
     */
    public static final String SESSION_COOKIE_NAME = "JSESSIONID";


    /**
     * The name of the path parameter used to pass the session identifier
     * back and forth with the client.
     */
    public static final String SESSION_PARAMETER_NAME = "jsessionid";


    /**
     * The request attribute under which we forward an HTTP status code
     * (as an object of type Integer) to an error page.
     */
    public static final String STATUS_CODE_ATTR =
        "javax.servlet.error.status_code";


    /**
     * The servlet context attribute under which we record the set of
     * welcome files (as an object of type String[]) for this application.
     */
    public static final String WELCOME_FILES_ATTR =
        "org.apache.catalina.WELCOME_FILES";


    /**
     * The servlet context attribute under which we store a temporary
     * working directory (as an object of type File) for use by servlets
     * within this web application.
     */
    public static final String WORK_DIR_ATTR =
        "javax.servlet.context.tempdir";


}

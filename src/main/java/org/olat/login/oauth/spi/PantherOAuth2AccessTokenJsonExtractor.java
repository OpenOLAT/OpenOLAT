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
package org.olat.login.oauth.spi;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth2.OAuth2Error;
import com.github.scribejava.core.utils.Preconditions;

/**
 * 
 * Initial date: 17 juin 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PantherOAuth2AccessTokenJsonExtractor implements TokenExtractor<OAuth2AccessToken>  {
	
    private static final Pattern ACCESS_TOKEN_REGEX_PATTERN = Pattern.compile("\"access_token\"\\s*:\\s*\"(\\S*?)\"");
    private static final Pattern TOKEN_TYPE_REGEX_PATTERN = Pattern.compile("\"token_type\"\\s*:\\s*\"(\\S*?)\"");
    private static final Pattern EXPIRES_IN_REGEX_PATTERN = Pattern.compile("\"expires_in\"\\s*:\\s*\"?(\\d*?)\"?\\D");
    private static final Pattern REFRESH_TOKEN_REGEX_PATTERN = Pattern.compile("\"refresh_token\"\\s*:\\s*\"(\\S*?)\"");
    private static final Pattern SCOPE_REGEX_PATTERN = Pattern.compile("\"scope\"\\s*:\\s*\"([^\"]*?)\"");
    private static final Pattern ERROR_REGEX_PATTERN = Pattern.compile("\"error\"\\s*:\\s*\"(\\S*?)\"");
    private static final Pattern ERROR_DESCRIPTION_REGEX_PATTERN
            = Pattern.compile("\"error_description\"\\s*:\\s*\"([^\"]*?)\"");
    private static final Pattern ERROR_URI_REGEX_PATTERN = Pattern.compile("\"error_uri\"\\s*:\\s*\"(\\S*?)\"");

    protected PantherOAuth2AccessTokenJsonExtractor() {
    }

    private static class InstanceHolder {

        private static final PantherOAuth2AccessTokenJsonExtractor INSTANCE = new PantherOAuth2AccessTokenJsonExtractor();
    }

    public static PantherOAuth2AccessTokenJsonExtractor instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public OAuth2AccessToken extract(Response response) throws IOException {
        final String body = response.getBody();
        Preconditions.checkEmptyString(body, "Response body is incorrect. Can't extract a token from an empty string");

        if (response.getCode() != 200 && response.getCode() != 302) {
            generateError(response);
        }
        return createToken(body);
    }

    /**
     * Related documentation: https://tools.ietf.org/html/rfc6749#section-5.2
     *
     * @param response response
     */
    public void generateError(Response response) throws OAuthException, IOException {
        final String body = response.getBody();
        final String errorInString = extractParameter(body, ERROR_REGEX_PATTERN, true);
        final String errorDescription = extractParameter(body, ERROR_DESCRIPTION_REGEX_PATTERN, false);
        final String errorUriInString = extractParameter(body, ERROR_URI_REGEX_PATTERN, false);
        URI errorUri;
        try {
            errorUri = errorUriInString == null ? null : URI.create(errorUriInString);
        } catch (IllegalArgumentException iae) {
            errorUri = null;
        }

        OAuth2Error errorCode;
        try {
            errorCode = OAuth2Error.valueOf(errorInString);
        } catch (IllegalArgumentException iaE) {
            //non oauth standard error code
            errorCode = null;
        }

        throw new OAuth2AccessTokenErrorResponse(errorCode, errorDescription, errorUri, response);
    }

    private OAuth2AccessToken createToken(String response) {
        final String accessToken = extractParameter(response, ACCESS_TOKEN_REGEX_PATTERN, true);
        final String tokenType = extractParameter(response, TOKEN_TYPE_REGEX_PATTERN, false);
        final String expiresInString = extractParameter(response, EXPIRES_IN_REGEX_PATTERN, false);
        Integer expiresIn;
        try {
            expiresIn = expiresInString == null ? null : Integer.valueOf(expiresInString);
        } catch (NumberFormatException nfe) {
            expiresIn = null;
        }
        final String refreshToken = extractParameter(response, REFRESH_TOKEN_REGEX_PATTERN, false);
        final String scope = extractParameter(response, SCOPE_REGEX_PATTERN, false);

        return createToken(accessToken, tokenType, expiresIn, refreshToken, scope, response);
    }

    protected OAuth2AccessToken createToken(String accessToken, String tokenType, Integer expiresIn,
            String refreshToken, String scope, String response) {
        return new OAuth2AccessToken(accessToken, tokenType, expiresIn, refreshToken, scope, response);
    }

    protected static String extractParameter(String response, Pattern regexPattern, boolean required)
            throws OAuthException {
        final Matcher matcher = regexPattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }

        if (required) {
            throw new OAuthException("Response body is incorrect. Can't extract a '" + regexPattern.pattern()
                    + "' from this: '" + response + "'", null);
        }

        return null;
    }

}

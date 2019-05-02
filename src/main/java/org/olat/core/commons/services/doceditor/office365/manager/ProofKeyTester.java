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
package org.olat.core.commons.services.doceditor.office365.manager;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.xml.bind.DatatypeConverter;

/**
 * Source: https://github.com/Microsoft/Office-Online-Test-Tools-and-Documentation/blob/master/samples/java/ProofKeyTester.java
 * 
 * Initial date: 2 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProofKeyTester
{
    public static void main( String args[] ) throws Exception
    {
        testProofKeys();
    }


    /**
     * Tests different proof key scenarios
     *
     * @throws Exception
     */
    public static void testProofKeys() throws Exception
    {

        String strWopiDiscoveryModulus = "0HOWUPFFgmSYHbLZZzdWO/HUOr8YNfx5NAl7GUytooHZ7B9QxQKTJpj0NIJ4XEskQW8e4dLzRrPbNOOJ+KpWHttXz8HoQXkkZV/gYNxaNHJ8/pRXGMZzfVM5vchhx/2C7ULPTrpBsSpmfWQ6ShaVoQzfThFUd0MsBvIN7HVtqzPx9jbSV04wAqyNjcro7F3iu9w7AEsMejHbFlWoN+J05dP5ixryF7+2U5RVmjMt7/dYUdCoiXvCMt2CaVr0XEG6udHU4iDKVKZjmUBc7cTWRzhqEL7lZ1yQfylp38Nd2xxVJ0sSU7OkC1bBDlePcYGaF3JjJgsmp/H5BNnlW9gSxQ==";
        String strWopiDiscoveryExponent = "AQAB";
        String strWopiDiscoveryOldModulus = "u/ppb/da4jeKQ+XzKr69VJTqR7wgQp2jzDIaEPQVzfwod+pc1zvO7cwjNgfzF/KQGkltoOi9KdtMzR0qmX8C5wZI6wGpS8S4pTFAZPhXg5w4EpyR8fAagrnlOgaVLs0oX5UuBqKndCQyM7Vj5nFd+r53giS0ch7zDW0uB1G+ZWqTZ1TwbtV6dmlpVuJYeIPonOJgo2iuh455KuS2gvxZKOKR27Uq7W949oM8sqRjvfaVf4xDmyor++98XX0zadnf4pMWfPr3XE+bCXtB9jIPAxxMrALf5ncNRhnx0Wyf8zfM7Rfq+omp/HxCgusF5MC2/Ffnn7me/628zzioAMy5pQ==";
        String strWopiDiscoveryOldExponent = "AQAB";


        String strWopiHeaderProofKey = "IflL8OWCOCmws5qnDD5kYMraMGI3o+T+hojoDREbjZSkxbbx7XIS1Av85lohPKjyksocpeVwqEYm9nVWfnq05uhDNGp2MsNyhPO9unZ6w25Rjs1hDFM0dmvYx8wlQBNZ/CFPaz3inCMaaP4PtU85YepaDccAjNc1gikdy3kSMeG1XZuaDixHvMKzF/60DMfLMBIu5xP4Nt8i8Gi2oZs4REuxi6yxOv2vQJQ5+8Wu2Olm8qZvT4FEIQT9oZAXebn/CxyvyQv+RVpoU2gb4BreXAdfKthWF67GpJyhr+ibEVDoIIolUvviycyEtjsaEBpOf6Ne/OLRNu98un7WNDzMTQ==";
        String strWopiRequest = "https://contoso.com/wopi/files/vHxYyRGM8VfmSGwGYDBMIQPzuE+sSC6kw+zWZw2Nyg";
        String strAccessToken = "yZhdN1qgywcOQWhyEMVpB6NE3pvBksvcLXsrFKXNtBeDTPW%2fu62g2t%2fOCWSlb3jUGaz1zc%2fzOzbNgAredLdhQI1Q7sPPqUv2owO78olmN74DV%2fv52OZIkBG%2b8jqjwmUobcjXVIC1BG9g%2fynMN0itZklL2x27Z2imCF6xELcQUuGdkoXBj%2bI%2bTlKM";
        String strWopiHeaderTimeStamp = "635655897610773532";
        strWopiRequest = strWopiRequest + "?access_token=" + strAccessToken;
        byte[] expectedProofArray = getExpectedProofBytes( strWopiRequest, strAccessToken, strWopiHeaderTimeStamp );

        // should verify correctly using X-WOPI-Proof and Current Key from Discovery
        System.out.println( "VERIFIED = " + verifyProofKey( strWopiDiscoveryModulus, strWopiDiscoveryExponent, strWopiHeaderProofKey, expectedProofArray ) );


        String strWopiHeaderProofKeyOld = "PjKR1BTNNnfOrUzfo27cLIhlrbSiOVZaANadDyHxKij/77ZYId+liyXoawvvQQPgnBH1dW6jqpr6fh5ZxZ9IOtaV+cTSUGnGdRSn7FyKs1ClpApKsZBO/iRBLXw3HDWOLc0jnA2bnxY8yqbEPmH5IBC9taYzxnf7aGjc6AWFHfs6AEQ8lMio6UoASNzjy3VVNzUX+CK+e5Z45coT0X60mjaJmidGfPdWIfyUw8sSuUwxQa1uNXAd8IceRUL7j5s9/kk7EwsihCw1Y3L+XJGG5zMsGhM9bTK5mvxj30UdmZORouNHdywOfdHaB1iOeKOk+yvWFMW3JsYShWbUhZUOEQ==";
        strWopiRequest = "https://contoso.com/wopi/files/RVQ29k8tf3h8cJ/Endy+aAMPy0iGhLatGNrhvKofPY9p2w";
        strAccessToken = "zo7pjAbo%2fyof%2bvtUJn5gXpYcSl7TSSx0TbQGJbWJSll9PTjRRsAbG%2fSNL7FM2n5Ei3jQ8UJsW4RidT5R4tl1lrCi1%2bhGjxfWAC9pRRl6J3M1wZk9uFkWEeGzbtGByTkaGJkBqgKV%2ffxg%2bvATAhVr6E3LHCBAN91Wi8UG";
        strWopiHeaderTimeStamp = "635655898374047766";
        strWopiRequest = strWopiRequest + "?access_token=" + strAccessToken;

        expectedProofArray = getExpectedProofBytes( strWopiRequest, strAccessToken, strWopiHeaderTimeStamp );

        //  should verify correctly using X-WOPI-ProofOld and Current Key from Discovery
        System.out.println( "VERIFIED = " + verifyProofKey( strWopiDiscoveryModulus, strWopiDiscoveryExponent, strWopiHeaderProofKeyOld, expectedProofArray ) );


        strWopiHeaderProofKey = "qF15pAAnOATqpUTLHIS/Z5K7OYFVjWcgKGbHPa0eHRayXsb6JKTelGQhvs74gEFgg1mIgcCORwAtMzLmEFmOHgrdvkGvRzT3jtVVtwkxEhQt8aQL20N0Nwn4wNah0HeBHskdvmA1G/qcaFp8uTgHpRYFoBaSHEP3AZVNFg5y2jyYR34nNj359gktc2ZyLel3J3j7XtyjpRPHvvYVQfh7RsArLQ0VGp8sL4/BDHdSsUyJ8FXe67TSrz6TMZPwhEUR8dYHYek9qbQjC+wxPpo3G/yusucm1gHo0BjW/l36cI8FRmNs1Fqaeppxqu31FhR8dEl7w5dwefa9wOUKcChF6A==";
        strWopiRequest = "https://contoso.com/wopi/files/DJNj59eQlM6BvwzAHkykiB1vNOWRuxT487+guv3v7HexfA";
        strAccessToken = "pbocsujrb9BafFujWh%2fuh7Y6S5nBnonddEzDzV0zEFrBwhiu5lzjXRezXDC9N4acvJeGVB5CWAcxPz6cJ6FzJmwA4ZgGP6FaV%2b6CDkJYID3FJhHFrbw8f2kRfaceRjV1PzXEvFXulnz2K%2fwwv0rF2B4A1wGQrnmwxGIv9cL5PBC4";
        strWopiHeaderTimeStamp = "635655898062751632";
        strWopiRequest = strWopiRequest + "?access_token=" + strAccessToken;

        expectedProofArray = getExpectedProofBytes( strWopiRequest, strAccessToken, strWopiHeaderTimeStamp );

        //  should verify correctly using X-WOPI-Proof and Old Key from Discovery
        System.out.println( "VERIFIED = " + verifyProofKey( strWopiDiscoveryOldModulus, strWopiDiscoveryOldExponent, strWopiHeaderProofKey, expectedProofArray ) );


        // the case below should fail
        strWopiHeaderProofKey = "qF15pAAnOATqpUTLHIS/Z5K7OYFVjWcgKGbHPa0eHRayXsb6JKTelGQhvs74gEFgg1mIgcCORwAtMzLmEFmOHgrdvkGvRzT3jtVVtwkxEhQt8aQL20N0Nwn4wNah0HeBHskdvmA1G/qcaFp8uTgHpRYFoBaSHEP3AZVNFg5y2jyYR34nNj359gktc2ZyLel3J3j7XtyjpRPHvvYVQfh7RsArLQ0VGp8sL4/BDHdSsUyJ8FXe67TSrz6TMZPwhEUR8dYHYek9qbQjC+wxPpo3G/yusucm1gHo0BjW/l36cI8FRmNs1Fqaeppxqu31FhR8dEl7w5dwefa9wOUKcChF6A==";
        strWopiRequest = "https://contoso.com/wopi/files/DJNj59eQlM6BvwzAHkykiB1vNOWRuxT487+guv3v7HexfA";
        strAccessToken = "pbocsujrb9BafFujWh%2fuh7Y6S5nBnonddEzDzV0zEFrBwhiu5lzjXRezXDC9N4acvJeGVB5CWAcxPz6cJ6FzJmwA4ZgGP6FaV%2b6CDkJYID3FJhHFrbw8f2kRfaceRjV1PzXEvFXulnz2K%2fwwv0rF2B4A1wGQrnmwxGIv9cL5PBC4";
        strWopiHeaderTimeStamp = "635655898062751632";
        strWopiRequest = strWopiRequest + "?access_token=" + strAccessToken;

        expectedProofArray = getExpectedProofBytes( strWopiRequest, strAccessToken, strWopiHeaderTimeStamp );

        //  should verify correctly using X-WOPI-Proof and Old Key from Discovery
        System.out.println( "VERIFIED = " + verifyProofKey( strWopiDiscoveryModulus, strWopiDiscoveryExponent, strWopiHeaderProofKey, expectedProofArray ) );
    }


    /**
     * @param strWopiProofKey    - Proof key from REST header
     * @param expectedProofArray - Byte Array from Specfication -- Contains querystring, time and accesskey combined by defined algorithm in spec
     *                           4 bytes that represent the length, in bytes, of the access_token on the request.
     *                           The access_token.
     *                           4 bytes that represent the length, in bytes, of the full URL of the WOPI request, including any query string parameters.
     *                           The WOPI request URL in all uppercase. All query string parameters on the request URL should be included.
     *                           4 bytes that represent the length, in bytes, of the X-WOPI-TimeStamp value.
     *                           The X-WOPI-TimeStamp value.
     * @return
     * @throws Exception
     */
    public static boolean verifyProofKey( String strModulus, String strExponent,
        String strWopiProofKey, byte[] expectedProofArray ) throws Exception
    {
        PublicKey publicKey = getPublicKey( strModulus, strExponent );

        Signature verifier = Signature.getInstance( "SHA256withRSA" );
        verifier.initVerify( publicKey );
        verifier.update( expectedProofArray ); // Or whatever interface specifies.

        final byte[] signedProof = DatatypeConverter.parseBase64Binary( strWopiProofKey );

        return verifier.verify( signedProof );
    }


    /**
     * Gets a public RSA Key using WOPI Discovery Modulus and Exponent for PKI Signed Validation
     *
     * @param modulus
     * @param exponent
     * @return
     * @throws Exception
     */
    private static RSAPublicKey getPublicKey( String modulus, String exponent ) throws Exception
    {
        BigInteger mod = new BigInteger( 1, DatatypeConverter.parseBase64Binary( modulus ) );
        BigInteger exp = new BigInteger( 1, DatatypeConverter.parseBase64Binary( exponent ) );
        KeyFactory factory = KeyFactory.getInstance( "RSA" );
        KeySpec ks = new RSAPublicKeySpec( mod, exp );

        return (RSAPublicKey) factory.generatePublic( ks );
    }


    /**
     * Generates expected proof
     *
     * @param url
     * @param accessToken
     * @param timestampStr
     * @return
     */
    static byte[] getExpectedProofBytes( String url, final String accessToken, final String timestampStr )
    {
        final byte[] accessTokenBytes = accessToken.getBytes( StandardCharsets.UTF_8 );


        final byte[] hostUrlBytes = url.toUpperCase().getBytes( StandardCharsets.UTF_8 );


        final Long timestamp = Long.valueOf( timestampStr );

        final ByteBuffer byteBuffer = ByteBuffer.allocate( 4 + accessTokenBytes.length + 4 + hostUrlBytes.length + 4 + 8 );
        byteBuffer.putInt( accessTokenBytes.length );
        byteBuffer.put( accessTokenBytes );
        byteBuffer.putInt( hostUrlBytes.length );
        byteBuffer.put( hostUrlBytes );
        byteBuffer.putInt( 8 );
        byteBuffer.putLong( timestamp );

        return byteBuffer.array();
    }


}

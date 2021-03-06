/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomcat.util.net.jsse.openssl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.catalina.util.IOTools;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;

public class TesterOpenSSL {

    public static final int VERSION;

    public static final Set<Cipher> OPENSSL_UNIMPLEMENTED_CIPHERS;

    static {
        // Note: The following lists are intended to be aligned with the most
        //       recent release of each OpenSSL release branch. Running the unit
        //       tests with earlier releases is likely to result in failures.

        String versionString = null;
        try {
            versionString = executeOpenSSLCommand("version");
        } catch (IOException e) {
            versionString = "";
        }
        if (versionString.startsWith("OpenSSL 1.1.1")) {
            // Note: Gump currently tests 9.0.x with OpenSSL master
            //       (a.k.a 1.1.1-dev)
            VERSION = 10101;
        } else if (versionString.startsWith("OpenSSL 1.1.0")) {
            // Support ends 2018-04-30
            VERSION = 10100;
        } else if (versionString.startsWith("OpenSSL 1.0.2")) {
            // Support ends 2019-12-31 (LTS)
            // Note: Gump current tests 8.0.x with OpenSSL 1.0.2
            VERSION = 10002;
        } else if (versionString.startsWith("OpenSSL 1.0.1")) {
            // Support ends 2016-12-31
            VERSION = 10001;
            // Note: Release branches 1.0.0 and earlier are no longer supported by
            //       the OpenSSL team so these tests don't support them either.
        } else {
            VERSION = -1;
        }

        HashSet<Cipher> unimplemented = new HashSet<>();

        // These have been removed from all supported versions.
        unimplemented.add(Cipher.TLS_DHE_DSS_WITH_RC4_128_SHA);
        unimplemented.add(Cipher.TLS_DHE_DSS_EXPORT1024_WITH_DES_CBC_SHA);
        unimplemented.add(Cipher.TLS_RSA_EXPORT1024_WITH_DES_CBC_SHA);
        unimplemented.add(Cipher.TLS_RSA_EXPORT1024_WITH_RC2_CBC_56_MD5);
        unimplemented.add(Cipher.TLS_DHE_DSS_EXPORT1024_WITH_RC4_56_SHA);
        unimplemented.add(Cipher.TLS_RSA_EXPORT1024_WITH_RC4_56_SHA);
        unimplemented.add(Cipher.TLS_RSA_EXPORT1024_WITH_RC4_56_MD5);
        unimplemented.add(Cipher.TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA);
        unimplemented.add(Cipher.TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA);
        unimplemented.add(Cipher.TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA256);
        unimplemented.add(Cipher.TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA256);
        unimplemented.add(Cipher.TLS_DH_RSA_WITH_CAMELLIA_128_CBC_SHA256);
        unimplemented.add(Cipher.TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA256);
        unimplemented.add(Cipher.TLS_DH_anon_WITH_DES_CBC_SHA);
        unimplemented.add(Cipher.TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA);
        unimplemented.add(Cipher.TLS_DH_anon_EXPORT_WITH_RC4_40_MD5);
        unimplemented.add(Cipher.TLS_DHE_RSA_WITH_DES_CBC_SHA);
        unimplemented.add(Cipher.TLS_DHE_DSS_WITH_DES_CBC_SHA);
        unimplemented.add(Cipher.TLS_DH_RSA_WITH_DES_CBC_SHA);
        unimplemented.add(Cipher.TLS_DH_DSS_WITH_DES_CBC_SHA);
        unimplemented.add(Cipher.TLS_RSA_WITH_DES_CBC_SHA);
        unimplemented.add(Cipher.SSL2_DES_64_CBC_WITH_MD5);
        unimplemented.add(Cipher.TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA);
        unimplemented.add(Cipher.TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA);
        unimplemented.add(Cipher.TLS_RSA_EXPORT_WITH_DES40_CBC_SHA);
        unimplemented.add(Cipher.TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5);
        unimplemented.add(Cipher.TLS_RSA_EXPORT_WITH_RC4_40_MD5);
        unimplemented.add(Cipher.SSL_CK_RC2_128_CBC_EXPORT40_WITH_MD5);
        unimplemented.add(Cipher.SSL_CK_RC2_128_CBC_WITH_MD5);
        unimplemented.add(Cipher.SSL_CK_RC4_128_WITH_MD5);
        unimplemented.add(Cipher.SSL2_RC4_128_EXPORT40_WITH_MD5);
        unimplemented.add(Cipher.SSL2_IDEA_128_CBC_WITH_MD5);
        unimplemented.add(Cipher.SSL2_DES_192_EDE3_CBC_WITH_MD5);

        if (VERSION < 10002) {
            // These were implemented in 1.0.2 so won't be available in any
            // earlier version
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_AES_128_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_AES_256_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_AES_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_AES_256_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_AES_128_GCM_SHA256);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_AES_256_GCM_SHA384);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_DES_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_SEED_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_AES_128_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_AES_256_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_AES_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_AES_256_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_AES_128_GCM_SHA256);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_AES_256_GCM_SHA384);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_CAMELLIA_128_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_DES_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_SEED_CBC_SHA);
        } else {
            // These were removed in 1.0.2 so won't be available from that
            // version onwards.
            // None at present.
        }

        if (VERSION < 10100) {
            // These were implemented in 1.1.0 so won't be available in any
            // earlier version
            unimplemented.add(Cipher.TLS_PSK_WITH_NULL_SHA);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_NULL_SHA);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_NULL_SHA);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_AES_128_GCM_SHA256);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_AES_256_GCM_SHA384);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_AES_128_GCM_SHA256);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_AES_256_GCM_SHA384);
            unimplemented.add(Cipher.TLS_PSK_WITH_AES_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_PSK_WITH_AES_256_CBC_SHA384);
            unimplemented.add(Cipher.TLS_PSK_WITH_NULL_SHA256);
            unimplemented.add(Cipher.TLS_PSK_WITH_NULL_SHA384);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_AES_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_AES_256_CBC_SHA384);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_NULL_SHA256);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_NULL_SHA384);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_AES_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_AES_256_CBC_SHA384);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_NULL_SHA256);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_NULL_SHA384);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_RC4_128_SHA);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_AES_128_CBC_SHA);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_AES_256_CBC_SHA);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_RC4_128_SHA);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_AES_128_CBC_SHA);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_AES_256_CBC_SHA);
            unimplemented.add(Cipher.TLS_ECDHE_PSK_WITH_RC4_128_SHA);
            unimplemented.add(Cipher.TLS_ECDHE_PSK_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA);
            unimplemented.add(Cipher.TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA);
            unimplemented.add(Cipher.TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA384);
            unimplemented.add(Cipher.TLS_ECDHE_PSK_WITH_NULL_SHA);
            unimplemented.add(Cipher.TLS_ECDHE_PSK_WITH_NULL_SHA256);
            unimplemented.add(Cipher.TLS_ECDHE_PSK_WITH_NULL_SHA384);
            unimplemented.add(Cipher.TLS_PSK_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_PSK_WITH_CAMELLIA_256_CBC_SHA384);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_CAMELLIA_256_CBC_SHA384);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_CAMELLIA_256_CBC_SHA384);
            unimplemented.add(
                    Cipher.TLS_ECDHE_PSK_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(
                    Cipher.TLS_ECDHE_PSK_WITH_CAMELLIA_256_CBC_SHA384);
            unimplemented.add(Cipher.TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(
                    Cipher.TLS_ECDH_ECDSA_WITH_CAMELLIA_256_CBC_SHA384);
            unimplemented.add(Cipher.TLS_ECDH_RSA_WITH_CAMELLIA_256_CBC_SHA384);
            unimplemented.add(
                    Cipher.TLS_ECDHE_ECDSA_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(
                    Cipher.TLS_ECDHE_RSA_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(
                    Cipher.TLS_ECDHE_ECDSA_WITH_CAMELLIA_256_CBC_SHA384);
            unimplemented.add(
                    Cipher.TLS_ECDH_ECDSA_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(
                    Cipher.TLS_ECDHE_RSA_WITH_CAMELLIA_256_CBC_SHA384);
            unimplemented.add(Cipher.TLS_ECDH_RSA_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_RSA_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DH_anon_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_RSA_WITH_CAMELLIA_256_CBC_SHA256);
            unimplemented.add(Cipher.TLS_PSK_WITH_AES_128_GCM_SHA256);
            unimplemented.add(Cipher.TLS_PSK_WITH_AES_256_GCM_SHA384);
            unimplemented.add(Cipher.TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA256);
            unimplemented.add(Cipher.TLS_RSA_WITH_AES_128_CCM);
            unimplemented.add(Cipher.TLS_RSA_WITH_AES_256_CCM);
            unimplemented.add(Cipher.TLS_DHE_RSA_WITH_AES_128_CCM);
            unimplemented.add(Cipher.TLS_DHE_RSA_WITH_AES_256_CCM);
            unimplemented.add(Cipher.TLS_RSA_WITH_AES_128_CCM_8);
            unimplemented.add(Cipher.TLS_RSA_WITH_AES_256_CCM_8);
            unimplemented.add(Cipher.TLS_DHE_RSA_WITH_AES_128_CCM_8);
            unimplemented.add(Cipher.TLS_DHE_RSA_WITH_AES_256_CCM_8);
            unimplemented.add(Cipher.TLS_PSK_WITH_AES_128_CCM);
            unimplemented.add(Cipher.TLS_PSK_WITH_AES_256_CCM);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_AES_128_CCM);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_AES_256_CCM);
            unimplemented.add(Cipher.TLS_PSK_WITH_AES_128_CCM_8);
            unimplemented.add(Cipher.TLS_PSK_WITH_AES_256_CCM_8);
            unimplemented.add(Cipher.TLS_PSK_DHE_WITH_AES_128_CCM_8);
            unimplemented.add(Cipher.TLS_PSK_DHE_WITH_AES_256_CCM_8);
            unimplemented.add(Cipher.TLS_ECDHE_ECDSA_WITH_AES_128_CCM);
            unimplemented.add(Cipher.TLS_ECDHE_ECDSA_WITH_AES_256_CCM);
            unimplemented.add(Cipher.TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8);
            unimplemented.add(Cipher.TLS_ECDHE_ECDSA_WITH_AES_256_CCM_8);
            unimplemented.add(
                    Cipher.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256);
            unimplemented.add(
                    Cipher.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256);
            unimplemented.add(Cipher.TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256);
            unimplemented.add(Cipher.TLS_PSK_WITH_CHACHA20_POLY1305_SHA256);
            unimplemented.add(
                    Cipher.TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_CHACHA20_POLY1305_SHA256);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_CHACHA20_POLY1305_SHA256);
        } else {
            // These were removed in 1.1.0 so won't be available from that
            // version onwards.
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_AES_128_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_AES_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_AES_128_GCM_SHA256);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_AES_256_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_AES_256_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_AES_256_GCM_SHA384);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_DSS_WITH_SEED_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_AES_128_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_AES_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_AES_128_GCM_SHA256);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_AES_256_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_AES_256_CBC_SHA256);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_AES_256_GCM_SHA384);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_CAMELLIA_128_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_RSA_WITH_SEED_CBC_SHA);
            unimplemented.add(Cipher.TLS_ECDH_ECDSA_WITH_NULL_SHA);
            unimplemented.add(Cipher.TLS_ECDH_ECDSA_WITH_RC4_128_SHA);
            unimplemented.add(Cipher.TLS_ECDH_ECDSA_WITH_RC4_128_SHA);
            unimplemented.add(Cipher.TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA);
            unimplemented.add(Cipher.TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA);
            unimplemented.add(Cipher.TLS_ECDH_RSA_WITH_NULL_SHA);
            unimplemented.add(Cipher.TLS_ECDH_RSA_WITH_RC4_128_SHA);
            unimplemented.add(Cipher.TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_ECDH_RSA_WITH_AES_128_CBC_SHA);
            unimplemented.add(Cipher.TLS_ECDH_RSA_WITH_AES_256_CBC_SHA);
            unimplemented.add(Cipher.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384);
            unimplemented.add(Cipher.TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384);
            unimplemented.add(Cipher.TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256);
            unimplemented.add(Cipher.TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384);
            unimplemented.add(Cipher.TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256);
            unimplemented.add(Cipher.TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384);
            unimplemented.add(
                    Cipher.TLS_ECDH_ECDSA_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(
                    Cipher.TLS_ECDH_ECDSA_WITH_CAMELLIA_256_CBC_SHA384);
            unimplemented.add(Cipher.TLS_ECDH_RSA_WITH_CAMELLIA_128_CBC_SHA256);
            unimplemented.add(Cipher.TLS_ECDH_RSA_WITH_CAMELLIA_256_CBC_SHA384);
            unimplemented.add(Cipher.TLS_RSA_WITH_RC4_128_MD5);
            unimplemented.add(Cipher.TLS_DH_anon_WITH_RC4_128_MD5);
            unimplemented.add(Cipher.TLS_ECDHE_PSK_WITH_RC4_128_SHA);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_RC4_128_SHA);
            unimplemented.add(Cipher.TLS_ECDHE_RSA_WITH_RC4_128_SHA);
            unimplemented.add(Cipher.TLS_RSA_WITH_RC4_128_SHA);
            unimplemented.add(Cipher.TLS_PSK_WITH_RC4_128_SHA);
            unimplemented.add(Cipher.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_RC4_128_SHA);
            unimplemented.add(Cipher.TLS_ECDH_anon_WITH_RC4_128_SHA);
            // 3DES requires a compile time switch to enable. Treat as removed.
            unimplemented.add(Cipher.TLS_PSK_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_DHE_PSK_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_RSA_PSK_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_DH_anon_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_ECDHE_PSK_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_RSA_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA);
            unimplemented.add(Cipher.TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA);
        }
        OPENSSL_UNIMPLEMENTED_CIPHERS = Collections.unmodifiableSet(
                unimplemented);
    }

    private TesterOpenSSL() {
        // Utility class. Hide default constructor.
    }

    public static Set<String> getOpenSSLCiphersAsSet(String specification)
            throws Exception {
        String[] ciphers = getOpenSSLCiphersAsExpression(specification).trim()
                .split(":");
        Set<String> result = new HashSet<>(ciphers.length);
        for (String cipher : ciphers) {
            result.add(cipher);
        }
        return result;

    }

    public static String getOpenSSLCiphersAsExpression(String specification)
            throws Exception {
        String stdout;
        if (specification == null) {
            stdout = executeOpenSSLCommand("ciphers", "-v");
        } else {
            stdout = executeOpenSSLCommand("ciphers", "-v", specification);
        }

        if (stdout.length() == 0) {
            return stdout;
        }

        StringBuilder output = new StringBuilder();
        boolean first = true;

        // OpenSSL should have returned one cipher per line
        String ciphers[] = stdout.split("\n");
        for (String cipher : ciphers) {
            // Handle rename for 1.1.0 onwards
            cipher = cipher.replaceAll("EDH", "DHE");
            if (first) {
                first = false;
            } else {
                output.append(':');
            }
            // Name is first part
            int i = cipher.indexOf(' ');
            output.append(cipher.substring(0, i));

            // Advance i past the space
            while (Character.isWhitespace(cipher.charAt(i))) {
                i++;
            }

            // Protocol is the second
            int j = cipher.indexOf(' ', i);
            output.append('+');
            output.append(cipher.substring(i, j));
        }
        return output.toString();
    }

    /*
     * Use this method to filter parser results when comparing them to OpenSSL
     * results to take account of unimplemented cipher suites.
     */
    public static void removeUnimplementedCiphersJsse(List<String> list) {
        for (Cipher cipher : OPENSSL_UNIMPLEMENTED_CIPHERS) {
            for (String jsseName : cipher.getJsseNames()) {
                list.remove(jsseName);
            }
        }
    }

    private static String executeOpenSSLCommand(String... args)
            throws IOException {
        String openSSLPath = System.getProperty("tomcat.test.openssl.path");
        if (openSSLPath == null || openSSLPath.length() == 0) {
            openSSLPath = "openssl";
        }
        List<String> cmd = new ArrayList<>();
        cmd.add(openSSLPath);
        for (String arg : args) {
            cmd.add(arg);
        }

        ProcessBuilder pb = new ProcessBuilder(cmd.toArray(new String[cmd
                .size()]));
        Process p = pb.start();

        InputStreamToText stdout = new InputStreamToText(p.getInputStream());
        InputStreamToText stderr = new InputStreamToText(p.getErrorStream());

        Thread t1 = new Thread(stdout);
        t1.setName("OpenSSL stdout reader");
        t1.start();

        Thread t2 = new Thread(stderr);
        t2.setName("OpenSSL stderr reader");
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }

        String errorText = stderr.getText();
        if (errorText.length() > 0) {
            System.err.println(errorText);
        }

        return stdout.getText().trim();
    }

    private static class InputStreamToText implements Runnable {

        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private final InputStream is;

        InputStreamToText(InputStream is) {
            this.is = is;
        }

        @Override
        public void run() {
            try {
                IOTools.flow(is, baos);
            } catch (IOException e) {
                // Ignore
            }
        }

        public String getText() {
            return baos.toString();
        }
    }
}

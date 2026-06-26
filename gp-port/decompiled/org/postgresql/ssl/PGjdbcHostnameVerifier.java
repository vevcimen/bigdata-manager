/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.ssl;

import java.net.IDN;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.GT;

public class PGjdbcHostnameVerifier
implements HostnameVerifier {
    private static final Logger LOGGER = Logger.getLogger(PGjdbcHostnameVerifier.class.getName());
    public static final PGjdbcHostnameVerifier INSTANCE = new PGjdbcHostnameVerifier();
    private static final int TYPE_DNS_NAME = 2;
    private static final int TYPE_IP_ADDRESS = 7;
    public static final Comparator<String> HOSTNAME_PATTERN_COMPARATOR = new Comparator<String>(){

        private int countChars(String value, char ch) {
            int count = 0;
            int pos = -1;
            while ((pos = value.indexOf(ch, pos + 1)) != -1) {
                ++count;
            }
            return count;
        }

        @Override
        public int compare(String o1, String o2) {
            int l2;
            int s2;
            int d2;
            int d1 = this.countChars(o1, '.');
            if (d1 != (d2 = this.countChars(o2, '.'))) {
                return d1 > d2 ? 1 : -1;
            }
            int s1 = this.countChars(o1, '*');
            if (s1 != (s2 = this.countChars(o2, '*'))) {
                return s1 < s2 ? 1 : -1;
            }
            int l1 = o1.length();
            if (l1 != (l2 = o2.length())) {
                return l1 > l2 ? 1 : -1;
            }
            return 0;
        }
    };

    @Override
    public boolean verify(String hostname, SSLSession session) {
        String commonName;
        boolean result;
        LdapName dn;
        Collection<List<?>> subjectAltNames;
        String canonicalHostname;
        X509Certificate[] peerCerts;
        try {
            peerCerts = (X509Certificate[])session.getPeerCertificates();
        }
        catch (SSLPeerUnverifiedException e) {
            LOGGER.log(Level.SEVERE, GT.tr("Unable to parse X509Certificate for hostname {0}", hostname), e);
            return false;
        }
        if (peerCerts == null || peerCerts.length == 0) {
            LOGGER.log(Level.SEVERE, GT.tr("No certificates found for hostname {0}", hostname));
            return false;
        }
        if (hostname.startsWith("[") && hostname.endsWith("]")) {
            canonicalHostname = hostname.substring(1, hostname.length() - 1);
        } else {
            try {
                canonicalHostname = IDN.toASCII(hostname);
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "Canonical host name for {0} is {1}", new Object[]{hostname, canonicalHostname});
                }
            }
            catch (IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, GT.tr("Hostname {0} is invalid", hostname), e);
                return false;
            }
        }
        X509Certificate serverCert = peerCerts[0];
        try {
            subjectAltNames = serverCert.getSubjectAlternativeNames();
            if (subjectAltNames == null) {
                subjectAltNames = Collections.emptyList();
            }
        }
        catch (CertificateParsingException e) {
            LOGGER.log(Level.SEVERE, GT.tr("Unable to parse certificates for hostname {0}", hostname), e);
            return false;
        }
        boolean anyDnsSan = false;
        for (List<?> sanItem : subjectAltNames) {
            Object sanType;
            if (sanItem.size() != 2 || (sanType = (Integer)sanItem.get(0)) == null || (Integer)sanType != 7 && (Integer)sanType != 2) continue;
            String san = (String)sanItem.get(1);
            if ((Integer)sanType == 7 && san != null && san.startsWith("*")) continue;
            anyDnsSan |= (Integer)sanType == 2;
            if (!this.verifyHostName(canonicalHostname, san)) continue;
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, GT.tr("Server name validation pass for {0}, subjectAltName {1}", hostname, san));
            }
            return true;
        }
        if (anyDnsSan) {
            LOGGER.log(Level.SEVERE, GT.tr("Server name validation failed: certificate for host {0} dNSName entries subjectAltName, but none of them match. Assuming server name validation failed", hostname));
            return false;
        }
        try {
            dn = new LdapName(serverCert.getSubjectX500Principal().getName("RFC2253"));
        }
        catch (InvalidNameException e) {
            LOGGER.log(Level.SEVERE, GT.tr("Server name validation failed: unable to extract common name from X509Certificate for hostname {0}", hostname), e);
            return false;
        }
        ArrayList<String> commonNames = new ArrayList<String>(1);
        for (Rdn rdn : dn.getRdns()) {
            if (!"CN".equals(rdn.getType())) continue;
            commonNames.add((String)rdn.getValue());
        }
        if (commonNames.isEmpty()) {
            LOGGER.log(Level.SEVERE, GT.tr("Server name validation failed: certificate for hostname {0} has no DNS subjectAltNames, and it CommonName is missing as well", hostname));
            return false;
        }
        if (commonNames.size() > 1) {
            Collections.sort(commonNames, HOSTNAME_PATTERN_COMPARATOR);
        }
        if (!(result = this.verifyHostName(canonicalHostname, commonName = (String)commonNames.get(commonNames.size() - 1)))) {
            LOGGER.log(Level.SEVERE, GT.tr("Server name validation failed: hostname {0} does not match common name {1}", hostname, commonName));
        }
        return result;
    }

    public boolean verifyHostName(@Nullable String hostname, @Nullable String pattern) {
        if (hostname == null || pattern == null) {
            return false;
        }
        int lastStar = pattern.lastIndexOf(42);
        if (lastStar == -1) {
            return hostname.equalsIgnoreCase(pattern);
        }
        if (lastStar > 0) {
            return false;
        }
        if (pattern.indexOf(46) == -1) {
            return false;
        }
        if (hostname.length() < pattern.length() - 1) {
            return false;
        }
        boolean ignoreCase = true;
        int toffset = hostname.length() - pattern.length() + 1;
        if (hostname.lastIndexOf(46, toffset - 1) >= 0) {
            return false;
        }
        return hostname.regionMatches(true, toffset, pattern, 1, pattern.length() - 1);
    }
}


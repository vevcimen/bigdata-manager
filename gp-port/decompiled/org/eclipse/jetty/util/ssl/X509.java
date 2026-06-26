/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.ssl;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class X509 {
    private static final Logger LOG = Log.getLogger(X509.class);
    private static final int KEY_USAGE__KEY_CERT_SIGN = 5;
    private static final int SUBJECT_ALTERNATIVE_NAMES__DNS_NAME = 2;
    private final X509Certificate _x509;
    private final String _alias;
    private final Set<String> _hosts = new LinkedHashSet<String>();
    private final Set<String> _wilds = new LinkedHashSet<String>();

    public static boolean isCertSign(X509Certificate x509) {
        boolean[] keyUsage = x509.getKeyUsage();
        if (keyUsage == null || keyUsage.length <= 5) {
            return false;
        }
        return keyUsage[5];
    }

    public X509(String alias, X509Certificate x509) throws CertificateParsingException, InvalidNameException {
        this._alias = alias;
        this._x509 = x509;
        Collection<List<?>> altNames = x509.getSubjectAlternativeNames();
        if (altNames != null) {
            for (List<?> list : altNames) {
                if (((Number)list.get(0)).intValue() != 2) continue;
                String cn = list.get(1).toString();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Certificate SAN alias={} CN={} in {}", alias, cn, this);
                }
                if (cn == null) continue;
                this.addName(cn);
            }
        }
        LdapName name = new LdapName(x509.getSubjectX500Principal().getName("RFC2253"));
        for (Rdn rdn : name.getRdns()) {
            if (!rdn.getType().equalsIgnoreCase("CN")) continue;
            String cn = rdn.getValue().toString();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Certificate CN alias={} CN={} in {}", alias, cn, this);
            }
            if (cn == null || !cn.contains(".") || cn.contains(" ")) continue;
            this.addName(cn);
        }
    }

    protected void addName(String cn) {
        if ((cn = StringUtil.asciiToLowerCase(cn)).startsWith("*.")) {
            this._wilds.add(cn.substring(2));
        } else {
            this._hosts.add(cn);
        }
    }

    public String getAlias() {
        return this._alias;
    }

    public X509Certificate getCertificate() {
        return this._x509;
    }

    public Set<String> getHosts() {
        return Collections.unmodifiableSet(this._hosts);
    }

    public Set<String> getWilds() {
        return Collections.unmodifiableSet(this._wilds);
    }

    public boolean matches(String host) {
        if (this._hosts.contains(host = StringUtil.asciiToLowerCase(host)) || this._wilds.contains(host)) {
            return true;
        }
        int dot = host.indexOf(46);
        if (dot >= 0) {
            String domain = host.substring(dot + 1);
            return this._wilds.contains(domain);
        }
        return false;
    }

    public String toString() {
        return String.format("%s@%x(%s,h=%s,w=%s)", this.getClass().getSimpleName(), this.hashCode(), this._alias, this._hosts, this._wilds);
    }
}


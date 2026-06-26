/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.translation;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class messages_nl
extends ResourceBundle {
    private static final String[] table;

    @Override
    public Object handleGetObject(String msgid) throws MissingResourceException {
        int hash_val = msgid.hashCode() & Integer.MAX_VALUE;
        int idx = hash_val % 18 << 1;
        String found = table[idx];
        if (found != null && msgid.equals(found)) {
            return table[idx + 1];
        }
        return null;
    }

    public Enumeration getKeys() {
        return new Enumeration(){
            private int idx = 0;
            {
                while (this.idx < 36 && table[this.idx] == null) {
                    this.idx += 2;
                }
            }

            @Override
            public boolean hasMoreElements() {
                return this.idx < 36;
            }

            public Object nextElement() {
                String key = table[this.idx];
                do {
                    this.idx += 2;
                } while (this.idx < 36 && table[this.idx] == null);
                return key;
            }
        };
    }

    public ResourceBundle getParent() {
        return this.parent;
    }

    static {
        String[] t = new String[36];
        t[0] = "";
        t[1] = "Project-Id-Version: PostgreSQL JDBC Driver 8.0\nReport-Msgid-Bugs-To: \nPO-Revision-Date: 2004-10-11 23:55-0700\nLast-Translator: Arnout Kuiper <ajkuiper@wxs.nl>\nLanguage-Team: Dutch <ajkuiper@wxs.nl>\nLanguage: nl\nMIME-Version: 1.0\nContent-Type: text/plain; charset=UTF-8\nContent-Transfer-Encoding: 8bit\n";
        t[2] = "Something unusual has occurred to cause the driver to fail. Please report this exception.";
        t[3] = "Iets ongewoons is opgetreden, wat deze driver doet falen. Rapporteer deze fout AUB: {0}";
        t[8] = "Unknown Types value.";
        t[9] = "Onbekende Types waarde.";
        t[12] = "Fastpath call {0} - No result was returned and we expected an integer.";
        t[13] = "Fastpath aanroep {0} - Geen resultaat werd teruggegeven, terwijl we een integer verwacht hadden.";
        t[20] = "The fastpath function {0} is unknown.";
        t[21] = "De fastpath functie {0} is onbekend.";
        t[22] = "No results were returned by the query.";
        t[23] = "Geen resultaten werden teruggegeven door de query.";
        t[26] = "An unexpected result was returned by a query.";
        t[27] = "Een onverwacht resultaat werd teruggegeven door een query";
        table = t;
    }
}


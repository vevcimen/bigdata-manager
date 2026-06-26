/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.translation;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class messages_es
extends ResourceBundle {
    private static final String[] table;

    @Override
    public Object handleGetObject(String msgid) throws MissingResourceException {
        String found;
        int hash_val = msgid.hashCode() & Integer.MAX_VALUE;
        int idx = hash_val % 37 << 1;
        String found2 = table[idx];
        if (found2 == null) {
            return null;
        }
        if (msgid.equals(found2)) {
            return table[idx + 1];
        }
        int incr = hash_val % 35 + 1 << 1;
        do {
            if ((idx += incr) >= 74) {
                idx -= 74;
            }
            if ((found = table[idx]) != null) continue;
            return null;
        } while (!msgid.equals(found));
        return table[idx + 1];
    }

    public Enumeration getKeys() {
        return new Enumeration(){
            private int idx = 0;
            {
                while (this.idx < 74 && table[this.idx] == null) {
                    this.idx += 2;
                }
            }

            @Override
            public boolean hasMoreElements() {
                return this.idx < 74;
            }

            public Object nextElement() {
                String key = table[this.idx];
                do {
                    this.idx += 2;
                } while (this.idx < 74 && table[this.idx] == null);
                return key;
            }
        };
    }

    public ResourceBundle getParent() {
        return this.parent;
    }

    static {
        String[] t = new String[74];
        t[0] = "";
        t[1] = "Project-Id-Version: JDBC PostgreSQL Driver\nReport-Msgid-Bugs-To: \nPO-Revision-Date: 2004-10-22 16:51-0300\nLast-Translator: Diego Gil <diego@adminsa.com>\nLanguage-Team: \nLanguage: \nMIME-Version: 1.0\nContent-Type: text/plain; charset=UTF-8\nContent-Transfer-Encoding: 8bit\nX-Poedit-Language: Spanish\n";
        t[4] = "The column index is out of range: {0}, number of columns: {1}.";
        t[5] = "El \u00edndice de la columna est\u00e1 fuera de rango: {0}, n\u00famero de columnas: {1}.";
        t[12] = "Unknown Response Type {0}.";
        t[13] = "Tipo de respuesta desconocida {0}.";
        t[16] = "Protocol error.  Session setup failed.";
        t[17] = "Error de protocolo. Fall\u00f3 el inicio de la sesi\u00f3n.";
        t[20] = "The server requested password-based authentication, but no password was provided.";
        t[21] = "El servidor requiere autenticaci\u00f3n basada en contrase\u00f1a, pero no se ha provisto ninguna contrase\u00f1a.";
        t[26] = "A result was returned when none was expected.";
        t[27] = "Se retorn\u00f3 un resultado cuando no se esperaba ninguno.";
        t[28] = "Server SQLState: {0}";
        t[29] = "SQLState del servidor: {0}.";
        t[30] = "The array index is out of range: {0}, number of elements: {1}.";
        t[31] = "El \u00edndice del arreglo esta fuera de rango: {0}, n\u00famero de elementos: {1}.";
        t[32] = "Premature end of input stream, expected {0} bytes, but only read {1}.";
        t[33] = "Final prematuro del flujo de entrada, se esperaban {0} bytes, pero solo se leyeron {1}.";
        t[36] = "The connection attempt failed.";
        t[37] = "El intento de conexi\u00f3n fall\u00f3.";
        t[38] = "Failed to create object for: {0}.";
        t[39] = "Fallo al crear objeto: {0}.";
        t[42] = "An error occurred while setting up the SSL connection.";
        t[43] = "Ha ocorrido un error mientras se establec\u00eda la conexi\u00f3n SSL.";
        t[48] = "No value specified for parameter {0}.";
        t[49] = "No se ha especificado un valor para el par\u00e1metro {0}.";
        t[50] = "The server does not support SSL.";
        t[51] = "Este servidor no soporta SSL.";
        t[52] = "An unexpected result was returned by a query.";
        t[53] = "Una consulta retorn\u00f3 un resultado inesperado.";
        t[60] = "Something unusual has occurred to cause the driver to fail. Please report this exception.";
        t[61] = "Algo inusual ha ocurrido que provoc\u00f3 un fallo en el controlador. Por favor reporte esta excepci\u00f3n.";
        t[64] = "No results were returned by the query.";
        t[65] = "La consulta no retorn\u00f3 ning\u00fan resultado.";
        table = t;
    }
}


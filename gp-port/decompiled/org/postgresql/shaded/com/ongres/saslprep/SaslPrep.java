/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.shaded.com.ongres.saslprep;

import java.nio.CharBuffer;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import org.postgresql.shaded.com.ongres.stringprep.StringPrep;

public class SaslPrep {
    private static final int MAX_UTF = 65535;

    public static String saslPrep(String value, boolean storedString) {
        ArrayList<Integer> valueBuilder = new ArrayList<Integer>();
        ArrayList<Integer> codePoints = new ArrayList<Integer>();
        for (int i = 0; i < value.length(); ++i) {
            int codePoint = value.codePointAt(i);
            codePoints.add(codePoint);
            if (codePoint > 65535) {
                ++i;
            }
            if (StringPrep.prohibitionNonAsciiSpace(codePoint)) continue;
            valueBuilder.add(codePoint);
        }
        StringBuilder stringBuilder = new StringBuilder();
        Iterator codePoint = codePoints.iterator();
        while (codePoint.hasNext()) {
            int codePoint2 = (Integer)codePoint.next();
            if (StringPrep.mapToNothing(codePoint2)) continue;
            char[] characters = Character.toChars(codePoint2);
            stringBuilder.append(characters);
        }
        String normalized = Normalizer.normalize(CharBuffer.wrap(stringBuilder.toString().toCharArray()), Normalizer.Form.NFKC);
        valueBuilder = new ArrayList();
        for (int i = 0; i < normalized.length(); ++i) {
            int codePoint3 = normalized.codePointAt(i);
            codePoints.add(codePoint3);
            if (codePoint3 > 65535) {
                ++i;
            }
            if (StringPrep.prohibitionNonAsciiSpace(codePoint3)) continue;
            valueBuilder.add(codePoint3);
        }
        Iterator iterator = valueBuilder.iterator();
        while (iterator.hasNext()) {
            int character = (Integer)iterator.next();
            if (StringPrep.prohibitionNonAsciiSpace(character) || StringPrep.prohibitionAsciiControl(character) || StringPrep.prohibitionNonAsciiControl(character) || StringPrep.prohibitionPrivateUse(character) || StringPrep.prohibitionNonCharacterCodePoints(character) || StringPrep.prohibitionSurrogateCodes(character) || StringPrep.prohibitionInappropriatePlainText(character) || StringPrep.prohibitionInappropriateCanonicalRepresentation(character) || StringPrep.prohibitionChangeDisplayProperties(character) || StringPrep.prohibitionTaggingCharacters(character)) {
                throw new IllegalArgumentException("Prohibited character " + String.valueOf(Character.toChars(character)));
            }
            if (!storedString || !StringPrep.unassignedCodePoints(character)) continue;
            throw new IllegalArgumentException("Prohibited character " + String.valueOf(Character.toChars(character)));
        }
        StringPrep.bidirectional(valueBuilder);
        return normalized;
    }
}


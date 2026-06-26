/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;

public interface Trie<V> {
    public boolean put(String var1, V var2);

    public boolean put(V var1);

    public V remove(String var1);

    public V get(String var1);

    public V get(String var1, int var2, int var3);

    public V get(ByteBuffer var1);

    public V get(ByteBuffer var1, int var2, int var3);

    public V getBest(String var1);

    public V getBest(String var1, int var2, int var3);

    public V getBest(byte[] var1, int var2, int var3);

    public V getBest(ByteBuffer var1, int var2, int var3);

    public Set<String> keySet();

    public boolean isFull();

    public boolean isCaseInsensitive();

    public void clear();

    public static <T> Trie<T> empty(final boolean caseInsensitive) {
        return new Trie<T>(){

            @Override
            public boolean put(String s2, Object o) {
                return false;
            }

            @Override
            public boolean put(Object o) {
                return false;
            }

            @Override
            public T remove(String s2) {
                return null;
            }

            @Override
            public T get(String s2) {
                return null;
            }

            @Override
            public T get(String s2, int offset, int len) {
                return null;
            }

            @Override
            public T get(ByteBuffer b) {
                return null;
            }

            @Override
            public T get(ByteBuffer b, int offset, int len) {
                return null;
            }

            @Override
            public T getBest(String s2) {
                return null;
            }

            @Override
            public T getBest(String s2, int offset, int len) {
                return null;
            }

            @Override
            public T getBest(byte[] b, int offset, int len) {
                return null;
            }

            @Override
            public T getBest(ByteBuffer b, int offset, int len) {
                return null;
            }

            @Override
            public Set<String> keySet() {
                return Collections.emptySet();
            }

            @Override
            public boolean isFull() {
                return true;
            }

            @Override
            public boolean isCaseInsensitive() {
                return caseInsensitive;
            }

            @Override
            public void clear() {
            }
        };
    }
}


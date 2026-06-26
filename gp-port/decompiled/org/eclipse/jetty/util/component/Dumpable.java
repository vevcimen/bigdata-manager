/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.component;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.Container;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;

@ManagedObject(value="Dumpable Object")
public interface Dumpable {
    public static final String KEY = "key: +- bean, += managed, +~ unmanaged, +? auto, +: iterable, +] array, +@ map, +> undefined";

    @ManagedOperation(value="Dump the nested Object state as a String", impact="INFO")
    default public String dump() {
        return Dumpable.dump(this);
    }

    public void dump(Appendable var1, String var2) throws IOException;

    public static String dump(Dumpable dumpable) {
        StringBuilder b = new StringBuilder();
        try {
            dumpable.dump(b, "");
        }
        catch (IOException e) {
            b.append(e.toString());
        }
        b.append(KEY);
        return b.toString();
    }

    default public String dumpSelf() {
        return this.toString();
    }

    public static void dumpObject(Appendable out, Object o) throws IOException {
        try {
            String s2;
            if (o == null) {
                s2 = "null";
            } else if (o instanceof Dumpable) {
                s2 = ((Dumpable)o).dumpSelf();
                s2 = StringUtil.replace(s2, "\r\n", "|");
                s2 = StringUtil.replace(s2, '\n', '|');
            } else if (o instanceof Collection) {
                s2 = String.format("%s@%x(size=%d)", o.getClass().getName(), o.hashCode(), ((Collection)o).size());
            } else if (o.getClass().isArray()) {
                s2 = String.format("%s@%x[size=%d]", o.getClass().getComponentType(), o.hashCode(), Array.getLength(o));
            } else if (o instanceof Map) {
                s2 = String.format("%s@%x{size=%d}", o.getClass().getName(), o.hashCode(), ((Map)o).size());
            } else {
                s2 = String.valueOf(o);
                s2 = StringUtil.replace(s2, "\r\n", "|");
                s2 = StringUtil.replace(s2, '\n', '|');
            }
            if (o instanceof LifeCycle) {
                out.append(s2).append(" - ").append(AbstractLifeCycle.getState((LifeCycle)o)).append("\n");
            } else {
                out.append(s2).append("\n");
            }
        }
        catch (Throwable ex) {
            out.append("=> ").append(ex.toString()).append("\n");
        }
    }

    public static void dumpObjects(Appendable out, String indent, Object object, Object ... extraChildren) throws IOException {
        int extras;
        Dumpable.dumpObject(out, object);
        int n = extras = extraChildren == null ? 0 : extraChildren.length;
        if (object instanceof Stream) {
            object = ((Stream)object).toArray();
        }
        if (object instanceof Array) {
            object = Arrays.asList(object);
        }
        if (object instanceof Container) {
            Dumpable.dumpContainer(out, indent, (Container)object, extras == 0);
        }
        if (object instanceof Iterable) {
            Dumpable.dumpIterable(out, indent, (Iterable)object, extras == 0);
        } else if (object instanceof Map) {
            Dumpable.dumpMapEntries(out, indent, (Map)object, extras == 0);
        }
        if (extras == 0) {
            return;
        }
        int i = 0;
        for (Object item : extraChildren) {
            String nextIndent = indent + (++i < extras ? "|  " : "   ");
            out.append(indent).append("+> ");
            if (item instanceof Dumpable) {
                ((Dumpable)item).dump(out, nextIndent);
                continue;
            }
            Dumpable.dumpObjects(out, nextIndent, item, new Object[0]);
        }
    }

    public static void dumpContainer(Appendable out, String indent, Container object, boolean last) throws IOException {
        Container container = object;
        ContainerLifeCycle containerLifeCycle = container instanceof ContainerLifeCycle ? (ContainerLifeCycle)container : null;
        Iterator<Object> i = container.getBeans().iterator();
        while (i.hasNext()) {
            Object bean = i.next();
            if (container instanceof DumpableContainer && !((DumpableContainer)((Object)container)).isDumpable(bean)) continue;
            String nextIndent = indent + (i.hasNext() || !last ? "|  " : "   ");
            if (bean instanceof LifeCycle) {
                if (container.isManaged(bean)) {
                    out.append(indent).append("+= ");
                    if (bean instanceof Dumpable) {
                        ((Dumpable)bean).dump(out, nextIndent);
                        continue;
                    }
                    Dumpable.dumpObjects(out, nextIndent, bean, new Object[0]);
                    continue;
                }
                if (containerLifeCycle != null && containerLifeCycle.isAuto(bean)) {
                    out.append(indent).append("+? ");
                    if (bean instanceof Dumpable) {
                        ((Dumpable)bean).dump(out, nextIndent);
                        continue;
                    }
                    Dumpable.dumpObjects(out, nextIndent, bean, new Object[0]);
                    continue;
                }
                out.append(indent).append("+~ ");
                Dumpable.dumpObject(out, bean);
                continue;
            }
            if (containerLifeCycle != null && containerLifeCycle.isUnmanaged(bean)) {
                out.append(indent).append("+~ ");
                Dumpable.dumpObject(out, bean);
                continue;
            }
            out.append(indent).append("+- ");
            if (bean instanceof Dumpable) {
                ((Dumpable)bean).dump(out, nextIndent);
                continue;
            }
            Dumpable.dumpObjects(out, nextIndent, bean, new Object[0]);
        }
    }

    public static void dumpIterable(Appendable out, String indent, Iterable<?> iterable, boolean last) throws IOException {
        Iterator<?> i = iterable.iterator();
        while (i.hasNext()) {
            Object item = i.next();
            String nextIndent = indent + (i.hasNext() || !last ? "|  " : "   ");
            out.append(indent).append("+: ");
            if (item instanceof Dumpable) {
                ((Dumpable)item).dump(out, nextIndent);
                continue;
            }
            Dumpable.dumpObjects(out, nextIndent, item, new Object[0]);
        }
    }

    public static void dumpMapEntries(Appendable out, String indent, Map<?, ?> map, boolean last) throws IOException {
        Iterator<Map.Entry<?, ?>> i = map.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<?, ?> entry = i.next();
            String nextIndent = indent + (i.hasNext() || !last ? "|  " : "   ");
            out.append(indent).append("+@ ").append(String.valueOf(entry.getKey())).append(" = ");
            Object item = entry.getValue();
            if (item instanceof Dumpable) {
                ((Dumpable)item).dump(out, nextIndent);
                continue;
            }
            Dumpable.dumpObjects(out, nextIndent, item, new Object[0]);
        }
    }

    public static Dumpable named(String name, Object object) {
        return (out, indent) -> {
            out.append(name).append(": ");
            Dumpable.dumpObjects(out, indent, object, new Object[0]);
        };
    }

    public static interface DumpableContainer
    extends Dumpable {
        default public boolean isDumpable(Object o) {
            return true;
        }
    }
}


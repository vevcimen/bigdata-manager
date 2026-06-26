/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.util.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.Container;
import org.eclipse.jetty.util.component.Destroyable;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

@ManagedObject(value="Implementation of Container and LifeCycle")
public class ContainerLifeCycle
extends AbstractLifeCycle
implements Container,
Destroyable,
Dumpable.DumpableContainer {
    private static final Logger LOG = Log.getLogger(ContainerLifeCycle.class);
    private final List<Bean> _beans = new CopyOnWriteArrayList<Bean>();
    private final List<Container.Listener> _listeners = new CopyOnWriteArrayList<Container.Listener>();
    private boolean _doStarted;
    private boolean _destroyed;

    @Override
    protected void doStart() throws Exception {
        if (this._destroyed) {
            throw new IllegalStateException("Destroyed container cannot be restarted");
        }
        this._doStarted = true;
        try {
            for (Bean b : this._beans) {
                if (!(b._bean instanceof LifeCycle)) continue;
                LifeCycle l = (LifeCycle)b._bean;
                switch (b._managed) {
                    case MANAGED: {
                        if (!l.isStopped() && !l.isFailed()) break;
                        this.start(l);
                        break;
                    }
                    case AUTO: {
                        if (l.isStopped()) {
                            this.manage(b);
                            this.start(l);
                            break;
                        }
                        this.unmanage(b);
                        break;
                    }
                }
            }
            super.doStart();
        }
        catch (Throwable t) {
            ArrayList<Bean> reverse = new ArrayList<Bean>(this._beans);
            Collections.reverse(reverse);
            for (Bean b : reverse) {
                LifeCycle l;
                if (!(b._bean instanceof LifeCycle) || b._managed != Managed.MANAGED || !(l = (LifeCycle)b._bean).isRunning()) continue;
                try {
                    this.stop(l);
                }
                catch (Throwable cause2) {
                    if (cause2 == t) continue;
                    t.addSuppressed(cause2);
                }
            }
            throw t;
        }
    }

    protected void start(LifeCycle l) throws Exception {
        l.start();
    }

    protected void stop(LifeCycle l) throws Exception {
        l.stop();
    }

    @Override
    protected void doStop() throws Exception {
        this._doStarted = false;
        super.doStop();
        ArrayList<Bean> reverse = new ArrayList<Bean>(this._beans);
        Collections.reverse(reverse);
        MultiException mex = new MultiException();
        for (Bean b : reverse) {
            if (b._managed != Managed.MANAGED || !(b._bean instanceof LifeCycle)) continue;
            LifeCycle l = (LifeCycle)b._bean;
            try {
                this.stop(l);
            }
            catch (Throwable cause) {
                mex.add(cause);
            }
        }
        mex.ifExceptionThrow();
    }

    @Override
    public void destroy() {
        this._destroyed = true;
        ArrayList<Bean> reverse = new ArrayList<Bean>(this._beans);
        Collections.reverse(reverse);
        for (Bean b : reverse) {
            if (!(b._bean instanceof Destroyable) || b._managed != Managed.MANAGED && b._managed != Managed.POJO) continue;
            Destroyable d = (Destroyable)b._bean;
            try {
                d.destroy();
            }
            catch (Throwable cause) {
                LOG.warn(cause);
            }
        }
        this._beans.clear();
    }

    public boolean contains(Object bean) {
        for (Bean b : this._beans) {
            if (b._bean != bean) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isManaged(Object bean) {
        for (Bean b : this._beans) {
            if (b._bean != bean) continue;
            return b.isManaged();
        }
        return false;
    }

    public boolean isAuto(Object bean) {
        for (Bean b : this._beans) {
            if (b._bean != bean) continue;
            return b._managed == Managed.AUTO;
        }
        return false;
    }

    public boolean isUnmanaged(Object bean) {
        for (Bean b : this._beans) {
            if (b._bean != bean) continue;
            return b._managed == Managed.UNMANAGED;
        }
        return false;
    }

    @Override
    public boolean addBean(Object o) {
        if (o instanceof LifeCycle) {
            LifeCycle l = (LifeCycle)o;
            return this.addBean(o, l.isRunning() ? Managed.UNMANAGED : Managed.AUTO);
        }
        return this.addBean(o, Managed.POJO);
    }

    @Override
    public boolean addBean(Object o, boolean managed) {
        if (o instanceof LifeCycle) {
            return this.addBean(o, managed ? Managed.MANAGED : Managed.UNMANAGED);
        }
        return this.addBean(o, managed ? Managed.POJO : Managed.UNMANAGED);
    }

    private boolean addBean(Object o, Managed managed) {
        if (o == null || this.contains(o)) {
            return false;
        }
        Bean newBean = new Bean(o);
        if (o instanceof Container.Listener) {
            this.addEventListener((Container.Listener)o);
        }
        this._beans.add(newBean);
        for (Container.Listener l : this._listeners) {
            l.beanAdded(this, o);
        }
        try {
            switch (managed) {
                case UNMANAGED: {
                    this.unmanage(newBean);
                    break;
                }
                case MANAGED: {
                    this.manage(newBean);
                    if (!this.isStarting() || !this._doStarted) break;
                    LifeCycle l = (LifeCycle)o;
                    if (!l.isRunning()) {
                        this.start(l);
                    }
                    break;
                }
                case AUTO: {
                    LifeCycle l;
                    if (o instanceof LifeCycle) {
                        l = (LifeCycle)o;
                        if (this.isStarting()) {
                            if (l.isRunning()) {
                                this.unmanage(newBean);
                                break;
                            }
                            if (this._doStarted) {
                                this.manage(newBean);
                                this.start(l);
                                break;
                            }
                            newBean._managed = Managed.AUTO;
                            break;
                        }
                        if (this.isStarted()) {
                            this.unmanage(newBean);
                            break;
                        }
                        newBean._managed = Managed.AUTO;
                        break;
                    }
                    newBean._managed = Managed.POJO;
                    break;
                }
                case POJO: {
                    newBean._managed = Managed.POJO;
                }
            }
        }
        catch (Error | RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} added {}", this, newBean);
        }
        return true;
    }

    public void addManaged(LifeCycle lifecycle) {
        this.addBean((Object)lifecycle, true);
        try {
            if (this.isRunning() && !lifecycle.isRunning()) {
                this.start(lifecycle);
            }
        }
        catch (Error | RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addEventListener(Container.Listener listener) {
        if (this._listeners.contains(listener)) {
            return;
        }
        this._listeners.add(listener);
        for (Bean b : this._beans) {
            listener.beanAdded(this, b._bean);
            if (!(listener instanceof Container.InheritedListener) || !b.isManaged() || !(b._bean instanceof Container)) continue;
            if (b._bean instanceof ContainerLifeCycle) {
                ((ContainerLifeCycle)b._bean).addBean((Object)listener, false);
                continue;
            }
            ((Container)b._bean).addBean(listener);
        }
    }

    @Override
    public void manage(Object bean) {
        for (Bean b : this._beans) {
            if (b._bean != bean) continue;
            this.manage(b);
            return;
        }
        throw new IllegalArgumentException("Unknown bean " + bean);
    }

    private void manage(Bean bean) {
        if (bean._managed != Managed.MANAGED) {
            bean._managed = Managed.MANAGED;
            if (bean._bean instanceof Container) {
                for (Container.Listener l : this._listeners) {
                    if (!(l instanceof Container.InheritedListener)) continue;
                    if (bean._bean instanceof ContainerLifeCycle) {
                        ((ContainerLifeCycle)bean._bean).addBean((Object)l, false);
                        continue;
                    }
                    ((Container)bean._bean).addBean(l);
                }
            }
            if (bean._bean instanceof AbstractLifeCycle) {
                ((AbstractLifeCycle)bean._bean).setStopTimeout(this.getStopTimeout());
            }
        }
    }

    @Override
    public void unmanage(Object bean) {
        for (Bean b : this._beans) {
            if (b._bean != bean) continue;
            this.unmanage(b);
            return;
        }
        throw new IllegalArgumentException("Unknown bean " + bean);
    }

    private void unmanage(Bean bean) {
        if (bean._managed != Managed.UNMANAGED) {
            if (bean._managed == Managed.MANAGED && bean._bean instanceof Container) {
                for (Container.Listener l : this._listeners) {
                    if (!(l instanceof Container.InheritedListener)) continue;
                    ((Container)bean._bean).removeBean(l);
                }
            }
            bean._managed = Managed.UNMANAGED;
        }
    }

    @Override
    public Collection<Object> getBeans() {
        return this.getBeans(Object.class);
    }

    public void setBeans(Collection<Object> beans) {
        for (Object bean : beans) {
            this.addBean(bean);
        }
    }

    @Override
    public <T> Collection<T> getBeans(Class<T> clazz) {
        ArrayList<T> beans = null;
        for (Bean b : this._beans) {
            if (!clazz.isInstance(b._bean)) continue;
            if (beans == null) {
                beans = new ArrayList<T>();
            }
            beans.add(clazz.cast(b._bean));
        }
        return beans == null ? Collections.emptyList() : beans;
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        for (Bean b : this._beans) {
            if (!clazz.isInstance(b._bean)) continue;
            return clazz.cast(b._bean);
        }
        return null;
    }

    public void removeBeans() {
        ArrayList<Bean> beans = new ArrayList<Bean>(this._beans);
        for (Bean b : beans) {
            this.remove(b);
        }
    }

    private Bean getBean(Object o) {
        for (Bean b : this._beans) {
            if (b._bean != o) continue;
            return b;
        }
        return null;
    }

    @Override
    public boolean removeBean(Object o) {
        Bean b = this.getBean(o);
        return b != null && this.remove(b);
    }

    private boolean remove(Bean bean) {
        if (this._beans.remove(bean)) {
            boolean wasManaged = bean.isManaged();
            this.unmanage(bean);
            for (Container.Listener l : this._listeners) {
                l.beanRemoved(this, bean._bean);
            }
            if (bean._bean instanceof Container.Listener) {
                this.removeEventListener((Container.Listener)bean._bean);
            }
            if (wasManaged && bean._bean instanceof LifeCycle) {
                try {
                    this.stop((LifeCycle)bean._bean);
                }
                catch (Error | RuntimeException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void removeEventListener(Container.Listener listener) {
        if (this._listeners.remove(listener)) {
            for (Bean b : this._beans) {
                listener.beanRemoved(this, b._bean);
                if (!(listener instanceof Container.InheritedListener) || !b.isManaged() || !(b._bean instanceof Container)) continue;
                ((Container)b._bean).removeBean(listener);
            }
        }
    }

    @Override
    public void setStopTimeout(long stopTimeout) {
        super.setStopTimeout(stopTimeout);
        for (Bean bean : this._beans) {
            if (!bean.isManaged() || !(bean._bean instanceof AbstractLifeCycle)) continue;
            ((AbstractLifeCycle)bean._bean).setStopTimeout(stopTimeout);
        }
    }

    @ManagedOperation(value="Dump the object to stderr")
    public void dumpStdErr() {
        try {
            this.dump(System.err, "");
            System.err.println("key: +- bean, += managed, +~ unmanaged, +? auto, +: iterable, +] array, +@ map, +> undefined");
        }
        catch (IOException e) {
            LOG.warn(e);
        }
    }

    @Override
    @ManagedOperation(value="Dump the object to a string")
    public String dump() {
        return Dumpable.dump(this);
    }

    @Deprecated
    public static String dump(Dumpable dumpable) {
        return Dumpable.dump(dumpable);
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException {
        this.dumpObjects(out, indent, new Object[0]);
    }

    public void dump(Appendable out) throws IOException {
        this.dump(out, "");
    }

    @Deprecated
    protected void dumpThis(Appendable out) throws IOException {
        out.append(String.valueOf(this)).append(" - ").append(this.getState()).append("\n");
    }

    @Deprecated
    public static void dumpObject(Appendable out, Object obj) throws IOException {
        Dumpable.dumpObject(out, obj);
    }

    protected void dumpObjects(Appendable out, String indent, Object ... items) throws IOException {
        Dumpable.dumpObjects(out, indent, this, items);
    }

    @Deprecated
    protected void dumpBeans(Appendable out, String indent, Collection<?> ... items) throws IOException {
        ContainerLifeCycle.dump(out, indent, items);
    }

    @Deprecated
    public static void dump(Appendable out, String indent, Collection<?> ... collections) throws IOException {
        if (collections.length == 0) {
            return;
        }
        int size = 0;
        for (Collection<?> c : collections) {
            size += c.size();
        }
        if (size == 0) {
            return;
        }
        int i = 0;
        for (Collection<?> c : collections) {
            for (Object o : c) {
                out.append(indent).append(" +- ");
                Dumpable.dumpObjects(out, indent + (++i < size ? " |  " : "    "), o, new Object[0]);
            }
        }
    }

    public void updateBean(Object oldBean, Object newBean) {
        if (newBean != oldBean) {
            if (oldBean != null) {
                this.removeBean(oldBean);
            }
            if (newBean != null) {
                this.addBean(newBean);
            }
        }
    }

    public void updateBean(Object oldBean, Object newBean, boolean managed) {
        if (newBean != oldBean) {
            if (oldBean != null) {
                this.removeBean(oldBean);
            }
            if (newBean != null) {
                this.addBean(newBean, managed);
            }
        }
    }

    public void updateBeans(Object[] oldBeans, Object[] newBeans) {
        if (oldBeans != null) {
            block0: for (Object o : oldBeans) {
                if (newBeans != null) {
                    for (Object n : newBeans) {
                        if (o == n) continue block0;
                    }
                }
                this.removeBean(o);
            }
        }
        if (newBeans != null) {
            block2: for (Object n : newBeans) {
                if (oldBeans != null) {
                    for (Object o : oldBeans) {
                        if (o == n) continue block2;
                    }
                }
                this.addBean(n);
            }
        }
    }

    @Override
    public <T> Collection<T> getContainedBeans(Class<T> clazz) {
        HashSet beans = new HashSet();
        this.getContainedBeans(clazz, beans);
        return beans;
    }

    protected <T> void getContainedBeans(Class<T> clazz, Collection<T> beans) {
        beans.addAll(this.getBeans(clazz));
        for (Container c : this.getBeans(Container.class)) {
            Bean bean = this.getBean(c);
            if (bean == null || !bean.isManageable()) continue;
            if (c instanceof ContainerLifeCycle) {
                ((ContainerLifeCycle)c).getContainedBeans(clazz, beans);
                continue;
            }
            beans.addAll(c.getContainedBeans(clazz));
        }
    }

    private static class Bean {
        private final Object _bean;
        private volatile Managed _managed = Managed.POJO;

        private Bean(Object b) {
            if (b == null) {
                throw new NullPointerException();
            }
            this._bean = b;
        }

        public boolean isManaged() {
            return this._managed == Managed.MANAGED;
        }

        public boolean isManageable() {
            switch (this._managed) {
                case MANAGED: {
                    return true;
                }
                case AUTO: {
                    return this._bean instanceof LifeCycle && ((LifeCycle)this._bean).isStopped();
                }
            }
            return false;
        }

        public String toString() {
            return String.format("{%s,%s}", new Object[]{this._bean, this._managed});
        }
    }

    static enum Managed {
        POJO,
        MANAGED,
        UNMANAGED,
        AUTO;

    }
}


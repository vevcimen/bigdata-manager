/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  scala.Enumeration
 *  scala.Enumeration$Value
 */
package io.pivotal.greenplum.spark.externaltable;

import scala.Enumeration;

public final class GpfdistServiceState$
extends Enumeration {
    public static GpfdistServiceState$ MODULE$;
    private final Enumeration.Value Stopped;
    private final Enumeration.Value Failed;
    private final Enumeration.Value Starting;
    private final Enumeration.Value Started;
    private final Enumeration.Value Stopping;
    private final Enumeration.Value Running;

    static {
        new GpfdistServiceState$();
    }

    public Enumeration.Value Stopped() {
        return this.Stopped;
    }

    public Enumeration.Value Failed() {
        return this.Failed;
    }

    public Enumeration.Value Starting() {
        return this.Starting;
    }

    public Enumeration.Value Started() {
        return this.Started;
    }

    public Enumeration.Value Stopping() {
        return this.Stopping;
    }

    public Enumeration.Value Running() {
        return this.Running;
    }

    private GpfdistServiceState$() {
        MODULE$ = this;
        this.Stopped = this.Value();
        this.Failed = this.Value();
        this.Starting = this.Value();
        this.Started = this.Value();
        this.Stopping = this.Value();
        this.Running = this.Value();
    }
}


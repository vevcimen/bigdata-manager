/*
 * Decompiled with CFR 0.152.
 */
package org.postgresql.hostchooser;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.hostchooser.HostStatus;

public enum HostRequirement {
    any{

        @Override
        public boolean allowConnectingTo(@Nullable HostStatus status) {
            return status != HostStatus.ConnectFail;
        }
    }
    ,
    master{

        @Override
        public boolean allowConnectingTo(@Nullable HostStatus status) {
            return primary.allowConnectingTo(status);
        }
    }
    ,
    primary{

        @Override
        public boolean allowConnectingTo(@Nullable HostStatus status) {
            return status == HostStatus.Primary || status == HostStatus.ConnectOK;
        }
    }
    ,
    secondary{

        @Override
        public boolean allowConnectingTo(@Nullable HostStatus status) {
            return status == HostStatus.Secondary || status == HostStatus.ConnectOK;
        }
    }
    ,
    preferSecondary{

        @Override
        public boolean allowConnectingTo(@Nullable HostStatus status) {
            return status != HostStatus.ConnectFail;
        }
    }
    ,
    preferPrimary{

        @Override
        public boolean allowConnectingTo(@Nullable HostStatus status) {
            return status != HostStatus.ConnectFail;
        }
    };


    public abstract boolean allowConnectingTo(@Nullable HostStatus var1);

    public static HostRequirement getTargetServerType(String targetServerType) {
        String allowSlave = targetServerType.replace("lave", "econdary").replace("master", "primary");
        return HostRequirement.valueOf(allowSlave);
    }
}


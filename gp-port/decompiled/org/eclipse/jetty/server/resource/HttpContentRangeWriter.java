/*
 * Decompiled with CFR 0.152.
 */
package org.eclipse.jetty.server.resource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;
import org.eclipse.jetty.http.HttpContent;
import org.eclipse.jetty.server.resource.ByteBufferRangeWriter;
import org.eclipse.jetty.server.resource.InputStreamRangeWriter;
import org.eclipse.jetty.server.resource.RangeWriter;
import org.eclipse.jetty.server.resource.SeekableByteChannelRangeWriter;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class HttpContentRangeWriter {
    private static final Logger LOG = Log.getLogger(HttpContentRangeWriter.class);

    public static RangeWriter newRangeWriter(HttpContent content) {
        block7: {
            Objects.requireNonNull(content, "HttpContent");
            ByteBuffer buffer = content.getDirectBuffer();
            if (buffer == null) {
                buffer = content.getIndirectBuffer();
            }
            if (buffer != null) {
                return new ByteBufferRangeWriter(buffer);
            }
            try {
                ReadableByteChannel channel = content.getReadableByteChannel();
                if (channel != null) {
                    if (channel instanceof SeekableByteChannel) {
                        SeekableByteChannel seekableByteChannel = (SeekableByteChannel)channel;
                        return new SeekableByteChannelRangeWriter(seekableByteChannel, () -> (SeekableByteChannel)content.getReadableByteChannel());
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Skipping non-SeekableByteChannel option " + channel + " from content " + content, new Object[0]);
                    }
                    channel.close();
                }
            }
            catch (IOException e) {
                if (!LOG.isDebugEnabled()) break block7;
                LOG.debug("Skipping ReadableByteChannel option", e);
            }
        }
        return new InputStreamRangeWriter(() -> content.getInputStream());
    }
}


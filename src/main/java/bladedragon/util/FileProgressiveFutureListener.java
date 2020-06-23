package bladedragon.util;

import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;

@Slf4j
public class FileProgressiveFutureListener implements ChannelProgressiveFutureListener {

    private RandomAccessFile raf;

    public FileProgressiveFutureListener(RandomAccessFile raf) {
        this.raf = raf;
    }

    @Override
    public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
        log.debug("Transfer progress: {} / {}", progress, total);
    }

    @Override
    public void operationComplete(ChannelProgressiveFuture future) {
        try {
            raf.close();
        } catch (IOException e) {
            log.error("Transfer occur exception");
            e.printStackTrace();
        }
        log.debug("Transfer complete.");
    }

    public static FileProgressiveFutureListener build(RandomAccessFile raf) {
        return new FileProgressiveFutureListener(raf);
    }
}

package com.ajjpj.asysmon.server.dummy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author arno
 */
public class AsyncServerSocketChannelPlay {
    public static final int PORT = 12321;
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final SocketAddress addr = new InetSocketAddress(PORT);
        final AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(addr);

        System.out.println(Thread.currentThread());
        while(true) {
            final Future<AsynchronousSocketChannel> accepted = server.accept();
            final AsynchronousSocketChannel ch = accepted.get();

            final ByteBuffer readBuffer = ByteBuffer.allocate(128);
            ch.read(readBuffer, null, new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer result, Object attachment) {
                    System.out.println(Thread.currentThread() + ": " + new String(readBuffer.array(), readBuffer.arrayOffset(), readBuffer.position() - readBuffer.arrayOffset()));

                    ch.write(ByteBuffer.wrap("HTTP/1.1\n\nHallo, Arno".getBytes()), null, new CompletionHandler<Integer, Object>() {
                        @Override
                        public void completed(Integer result, Object attachment) {
                            try {
                                System.out.println("closing");
                                ch.close();
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }

                        @Override
                        public void failed(Throwable exc, Object attachment) {
                            System.out.println("failed 2");
                        }
                    });
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    System.out.println("failed: " + Thread.currentThread());
                }
            });
        }
    }
}

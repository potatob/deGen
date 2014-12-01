package com.potatob.converter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * Created by potatob on 11/20/14.
 */
public class AsyncHttp {

    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() << 1, new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    return t;
                }
            });

    private static final CompletionService<byte[]> COMPLETION_SERVICE = new ExecutorCompletionService<byte[]>(EXECUTOR);

    // Performs a simple HTTP get
    public static Future<byte[]> get(String url) {
        return EXECUTOR.submit(new Request(Main.CONTENT_URL + url));
    }

    public static Future<byte[]> getWithFullUrl(String url) {
        return EXECUTOR.submit(new Request(url));
    }

    // Performs a simple HTTP get
    public static Future<byte[]> completionServiceGet(String url) {
        return COMPLETION_SERVICE.submit(new Request(Main.CONTENT_URL + url));
    }

    public static Future<byte[]> completionServiceGetWithFullUrl(String url) {
        return COMPLETION_SERVICE.submit(new Request(url));
    }

    public static CompletionService<byte[]> getCompletionService() {
        return COMPLETION_SERVICE;
    }

    public static class Request implements Callable<byte[]> {

        private final String url;

        public Request(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public byte[] call() throws Exception {
            byte[] result = null;
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[2048];
                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                if (out.size() != 0) {
                    result = out.toByteArray();
                }
            } catch (IOException e) {
                System.err.println("Couldn't connect to " + url);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return result;
        }
    }
}

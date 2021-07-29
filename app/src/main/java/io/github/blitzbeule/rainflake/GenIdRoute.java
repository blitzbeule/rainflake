package io.github.blitzbeule.rainflake;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class GenIdRoute implements HttpHandler {

    IdWorker idWorker;

    public GenIdRoute(IdWorker idWorker) {
        this.idWorker = idWorker;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if(!(ex.getRequestMethod().equalsIgnoreCase("get"))) {
            ex.getRequestBody().close();
            ex.sendResponseHeaders(400, 0);
            byte[] r = "{\"error\":\"wrong method\"}".getBytes();
            ex.getResponseBody().write(r);
        } else {
            String query = ex.getRequestURI().getQuery();
            ex.getRequestBody().close();
            long id = idWorker.nextId();
            byte[] r = null;
            if (query == null) {
                r = ("{\"id\":\"" + id + "\"}").getBytes(StandardCharsets.UTF_8);
            }
            if ("f=b64".equalsIgnoreCase(query)) {
                r = ("{\"id\":\"" + Base64.getUrlEncoder().withoutPadding().encodeToString(longToBytes(id)) + "\"}").getBytes(StandardCharsets.UTF_8);
            }
            ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            ex.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            ex.sendResponseHeaders(200, 0);
            ex.getResponseBody().write(r);
        }
        ex.getResponseBody().close();
    }

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[Long.BYTES];
        for (int i = Long.BYTES - 1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= Byte.SIZE;
        }
        return result;
    }
}


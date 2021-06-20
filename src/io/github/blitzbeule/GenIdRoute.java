package io.github.blitzbeule;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
            ex.getResponseBody().close();
        } else {
            ex.getRequestBody().close();
            byte[] r = ("{\"id\":\"" + idWorker.nextId() + "\"}").getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            ex.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            ex.sendResponseHeaders(200, 0);
            ex.getResponseBody().write(r);
            ex.getResponseBody().close();
        }
    }
}

package io.github.blitzbeule.rainflake;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class App {

    int port;
    IdWorker idWorker;
    HttpServer httpServer;

    public App(int port, IdWorker idWorker) {
        this.port = port;
        this.idWorker = idWorker;

        GenIdRoute genIdRoute = new GenIdRoute(idWorker);

        httpServer = null;
        try {
            httpServer = HttpServer.create(
                    new InetSocketAddress(port),
                    511
            );
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        httpServer.createContext("/id", genIdRoute);
    }

    public void start() {
        System.out.println("Server starting ...");
        httpServer.start();
        System.out.println("Server started!");
    }

    public synchronized  void stop() {
        System.out.println("Starting to stop the server ...");
        httpServer.stop(1);
        System.out.println("Stopped Server!");
    }

    public static void main(String[] args) {
        int port;
        IdWorker idWorker;
        if(args.length == 0) {
            port = 8080;
            idWorker = new IdWorker(1577836800000L, 1);
        } else if(args.length == 1) {
            if (!(args[0].equalsIgnoreCase("env"))) {
                throw new IllegalArgumentException("Wrong count of parameters");
            }
            port = Integer.parseInt(System.getenv("RF_PORT"));
            idWorker = new IdWorker(
                    Long.parseLong(System.getenv("RF_EPOCH")),
                    Integer.parseInt(System.getenv("RF_NODEID")),
                    Integer.parseInt(System.getenv("RF_UNUSED_BITS")),
                    Integer.parseInt(System.getenv("RF_EPOCH_BITS")),
                    Integer.parseInt(System.getenv("RF_NODE_ID_BITS")),
                    Integer.parseInt(System.getenv("RF_SEQUENCE_BITS"))
            );
        } else if(args.length == 2){
            port = Integer.parseInt(args[0]);
            idWorker = new IdWorker(1577836800000L, Integer.parseInt(args[1]));
        } else if(args.length == 3) {
            port = Integer.parseInt(args[0]);
            idWorker = new IdWorker(Long.parseLong(args[2]), Integer.parseInt(args[1]));
        } else if(args.length == 7) {
            port = Integer.parseInt(args[0]);
            idWorker = new IdWorker(
                    Long.parseLong(args[2]),
                    Integer.parseInt(args[1]),
                    Integer.parseInt(args[3]),
                    Integer.parseInt(args[4]),
                    Integer.parseInt(args[5]),
                    Integer.parseInt(args[6])
            );
        } else {
            throw new IllegalArgumentException("Wrong count of parameters");
        }

        App app = new App(port, idWorker);
        app.start();

        var shutdownListener = new Thread(() -> {
            System.out.println("Preparing shutdown...");
            app.stop();
            System.out.println("Exit now!");
        });

        Runtime.getRuntime().addShutdownHook(shutdownListener);
    }
}

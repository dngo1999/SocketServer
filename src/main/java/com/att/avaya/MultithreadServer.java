package com.att.avaya;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Properties;
import java.io.*;

public class MultithreadServer {

    private static final Logger LOG = LoggerFactory.getLogger(MultithreadServer.class);

    private ServerSocket serverSocket;

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            while (true)
            {
                LOG.info("Avaya listener started on port: {}", port);
                new ClientHandler(serverSocket.accept()).start();
            }

        } catch (IOException e) {
            LOG.error("Exception: ", e);
            e.printStackTrace();
        } finally {
            stop();
        }

    }

    public void stop() {
        try {

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                // retrieve db connection
                config();

                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (".".equals(inputLine)) {
                        out.println("bye");
                        LOG.info("---terminating socket---BYE!");
                        break;
                    }
                    out.println(inputLine);
                    LOG.info ("...received...{}", inputLine);
                }

                in.close();
                out.close();
                clientSocket.close();

            } catch (IOException e) {
                LOG.debug(e.getMessage());
            }
        }
    }

    private static void config(){

        try{
            FileInputStream fis = new FileInputStream("dbContext.properties");
            Properties dbProperties = new Properties();
            dbProperties.load(fis);
            String host = dbProperties.getProperty("db.host");
            LOG.info ("Connection string {}", host);
            String login = dbProperties.getProperty("db.login");
            LOG.info ("login {}", login);
            String password = dbProperties.getProperty("db.password");
            LOG.info ("password {}", password);
        } catch (Exception ex) {
            LOG.error("Error retrieving DB info...", ex);
        }
    }

    public static void main(String[] args) {
        MultithreadServer server = new MultithreadServer();
        server.start(6996);
    }

}
package org.example;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerStart {
    private static final int Port = 5555;
    private static final int Start_Pv = 50;
    private static final int Start_Money = 25;

    private static final String[] COLORS = {
            "\u001B[31m",
            "\u001B[32m",
            "\u001B[33m",
            "\u001B[34m",
            "\u001B[35m",
            "\u001B[36m",
    };

    private static ConcurrentHashMap<String, Player> jugadores = new ConcurrentHashMap<>();
    private static List<ServerThread> threads = new ArrayList<>();
    private static List<String> colors = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        String file = "censored.txt";
        Path path = Paths.get(file);
        List<String> censored = Files.readAllLines(path);


        synchronized (colors){
            for (String color: COLORS){
                colors.add(color);
            }
        }
            try (ServerSocket serverSocket = new ServerSocket(Port)) {
                System.out.println("[SERVIDOR]: Iniciando en el puerto " + Port);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    ServerThread thread = new ServerThread(clientSocket, jugadores, threads,Start_Pv,Start_Money,censored);
                    threads.add(thread);
                    thread.start();
                }
            } catch (IOException e) {
                System.out.println("[SERVIDOR]: Socket cerrado");
            }
        }

        public static synchronized void sendAll(String message,String username) {
            for (ServerThread thread : threads) {
                if(message.contains("[SERVIDOR]")||message.contains("=== Estado de los jugadores ===")) {
                    thread.sendMessage(message);
                }else{
                    if(thread.username.equalsIgnoreCase(username)){
                        thread.sendMessage(message);
                    }else{
                        thread.sendMessage("\t\t\t\t\t\t\t\t\t\t\t"+message);
                    }
                }
            }
            if (message.contains("[SERVIDOR]")){
                System.out.println(message);
            }
        }

        public static synchronized void deleteThread(ServerThread thread) {
            threads.remove(thread);
        }

        public synchronized static String getColor(){
        if(colors.isEmpty()) return "\u001B[37m";
        return colors.remove(0);
        }
        public synchronized static void returnColor(String color){
            colors.add(0,color);
        }
    }


package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerThread extends Thread{


    private Socket clienteSocket;
    private ConcurrentHashMap<String, Player> players;
    private List<ServerThread> threads;
    private String username;
    private String asignedColor;
    private PrintWriter writer;
    private int pv;
    private int money;

    public ServerThread(Socket clienteSocket, ConcurrentHashMap<String, Player> jugadores,List<ServerThread> thread,int pv,int money) {
        this.clienteSocket = clienteSocket;
        this.players = jugadores;
        this.threads = thread;
        this.pv=pv;
        this.money = money;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()))) {
            writer = new PrintWriter(clienteSocket.getOutputStream(), true);
            writer.println("[SERVIDOR]: Ingresa tu nombre de usuario:");
            username = reader.readLine().trim();


            asignedColor = ServerStart.getColor();
            players.put(username, new Player(username,pv,money,asignedColor));
            ServerStart.sendAll("[SERVIDOR]: " + asignedColor + username + " se ha unido al chat.\u001B[0m");

            String mensaje;
            while ((mensaje = reader.readLine()) != null) {
                if (mensaje.startsWith("/")) {
                    commands(mensaje);
                } else {
                    ServerStart.sendAll(asignedColor + "[" + username + "]: " + mensaje + "\u001B[0m");
                }
            }
        } catch (IOException e) {
            System.out.println("[SERVIDOR]: se ha cerrado el socket");
        } finally {
            players.remove(username);
            ServerStart.deleteThread(this);
            ServerStart.sendAll("[SERVIDOR]: " + asignedColor + username + " ha abandonado el chat.\u001B[0m");
            ServerStart.returnColor(asignedColor);
            try {
                clienteSocket.close();
            } catch (IOException e) {
                System.out.println("[SERVIDOR]: se ha cerrado el socket");
            }
        }
    }
    private void commands(String message){
        String parts[] = message.split(" ");
        switch (parts[0]){
            case "/atacar":
                if(parts.length<2){
                    sendMessage("[SERVIDOR]: EL formato del comando es (/atacar <nombre_jugador>)");
                }else {
                    attack(parts[1]);
                }
                break;
            case "/resumen":
                resume();
                break;
            case "/salir":
                closeConection();
                break;
            case "/dar":
                if(parts.length<3){
                    sendMessage("[SERVIDOR]: EL formato del comando es (/dar <cantidad> <nombre_jugador>)");
                }else {
                    give(parts[1], parts[2]);
                }
                break;
            case "/mio":
                information();
                break;
            default:
                sendMessage("[SERVIDOR]: Ese comando no existe.");
                break;
        }
    }
    private void attack(String player){
        if(!players.containsKey(player)||player==null){
            sendMessage("[SERVIDOR]: Usuario no encontrado.");
        }else {
            Player attacker = players.get(username);
            Player defensor = players.get(player);
            if (attacker.getMoney() < 5 || defensor.getPv() <= 0) {
                ServerStart.sendAll("[SERVIDOR]: " + username + " atacó a " + player + " pero no surtió efecto.");
            } else {
                attacker.setMoney(attacker.getMoney() - 5);
                defensor.setPv(defensor.getPv() - 10);
                ServerStart.sendAll("[SERVIDOR]: " + username + " atacó a " + player + " (-10 PV).");
            }
        }
    }
    private void resume(){
        ServerStart.sendAll("=== Estado de los jugadores ===");
        String message = "";
        for(Player player: players.values()){
            message=message+player.getName()+" -- PV: "+player.getPv()+", Dinero: "+player.getMoney()+"\n";
        }
        ServerStart.sendAll(message);
    }
    private void give(String amount, String player){
        if(player==null){
            sendMessage("[SERVIDOR]: Usuario no encontrado.");
        }else{
            if(!checktype(amount)){
             sendMessage("[SERVIDOR]: El tipo de dato no es correcto debe de ser un número.");
            }else{
                int amount_ = Integer.parseInt(amount);
                Player donator = players.get(username);
                Player receptor = players.get(player);
                if(donator.getMoney()>amount_){
                    donator.setMoney(donator.getMoney()-amount_);
                    receptor.setMoney(receptor.getMoney()+amount_);
                    for(ServerThread thread: threads) {
                        if(thread.username.equalsIgnoreCase(player)) {
                            thread.sendMessage("[SERVIDOR]: " + username + " te ha donado " + amount_ + " monedas.");
                            break;
                        }
                    }
                }else {
                    sendMessage("[SERVIDOR]: Saldo insuficiente para la donación.");
                }

            }
        }
    }
    private void information(){
        this.toString();
    }
    private void closeConection() {
        try {
            clienteSocket.close();
        } catch (IOException e) {
            System.err.println("SOCKET CERRADO");
        }
    }
    private boolean checktype(String amount){
        try{
            Integer.parseInt(amount);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }
    public void sendMessage(String message) {
        writer.println(message);
    }
}

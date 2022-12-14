package network;

import game.Game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Network {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    PrintWriter out;
    BufferedReader in;
    private Game game;

    private final int defaultPort = 42069;
    private int apiVersion = 1;

    //Starting network as host
    public Network() {
        game = Game.getInstance();
        try {
            serverSocket = new ServerSocket(defaultPort);
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out.println(messageBuilder.build("HELLO",apiVersion,game.getSemester(),game.getPlayerfield()));
            String[] helloAck = messageBuilder.split(in.readLine());
            if (helloAck[0].equals("HELLO_ACK") && Integer.parseInt(helloAck[1])<= apiVersion) {
                game.setSemester(Integer.min(game.getSemester(), Integer.parseInt(helloAck[2])));
                game.setOpponentname(helloAck[3]);
                Thread networklistener = new networkListener();
                networklistener.start();
            } else {
                if (!helloAck[0].equals("HELLO_ACK")) out.println(messageBuilder.build("ERR","-1", "Expected HELLO_ACK, received " + helloAck[0]));
                if (Integer.parseInt(helloAck[1]) < apiVersion) out.println(messageBuilder.build("ERR","-3", "Expected version " + apiVersion + ", received version " + helloAck[1]));
            }
        } catch (IOException e) {
            System.out.println("Could not establish connection: " + e);
        }
    }
    //Starting network as client
    public Network(String ip) {
        game = Game.getInstance();
        String[] ipport = ip.split(":");
        int port = defaultPort;
        ip = ipport[0];
        if (ipport.length == 2){
            if(ipport[1].matches("[0-9]")) {
                port = Integer.parseInt(ipport[1]);
            }
        }
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String[] hello = messageBuilder.split(in.readLine());
            if (hello[0].equals("HELLO")) {
                apiVersion = Integer.min(apiVersion, Integer.parseInt(hello[1]));
                game.setSemester(Integer.min(game.getSemester(), Integer.parseInt(hello[2])));
                game.setOpponentname(hello[3]);
                out.println(messageBuilder.build("HELLO_ACK",apiVersion,game.getSemester(),game.getPlayername()));
                Thread networklistener = new networkListener();
                networklistener.start();
            } else {
                if (!hello[0].equals("HELLO")) out.println(messageBuilder.build("ERR","-1", "Expected HELLO, received " + hello[0]));
            }
        } catch (IOException e) {
            System.out.println("Could not establish connection: " + e);
        }
    }
    public void send(String input) {
        out.println(input);
    }
}

class networkListener extends Thread {
    @Override
    public void run() {
        Game game = Game.getInstance();
        String incoming;
        while(true) {
            try {
                incoming = game.network.in.readLine();
                if (!incoming.isEmpty()) System.out.println(incoming);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}

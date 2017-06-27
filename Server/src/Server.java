/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import controller.ProtocolController;
import protocol.ProtocolError;
import utils.ComUtils;
import view.ServerClientInfo;
import view.ServerThread;

/**
 *
 * @author psanchva9.alumnes
 */
public final class Server {

    // valors relatius a l'execució del main per terminal
    private static final String MAIN_HELP_PARAM = "-h";
    private static final String MAIN_PORT_PARAM = "-p";
    private static final String MAIN_INTERACTIVE_PARAM = "-i";
    private static final String MAIN_USAGE_MESSAGE = "Wrong syntax. Usage: java Server " + MAIN_PORT_PARAM + " <port> ["+ MAIN_INTERACTIVE_PARAM +" 1|2]";

    // port del servidor
    private final int port;
    
    // indica si la IA està activa
    private final boolean isArtificialIntelligenceActive;

    
    //nombre màxim de connexions simultànies
    private final int MAX_CONNECTIONS = 20;
    
    // taula hash dels clients (id/dades que persisteixen si marxa)
    private final ConcurrentHashMap<Integer, ServerClientInfo> clientMap;
    
    
    /**
     * Server Constructor
     * @param port
     * @param isArtificialIntelligenceActive
     */
    public Server(int port, boolean isArtificialIntelligenceActive) {
        this.port = port;
        this.isArtificialIntelligenceActive = isArtificialIntelligenceActive;
        clientMap = new ConcurrentHashMap(MAX_CONNECTIONS);
    }
    
    /**
     * Inicialitza el servidor
     */
    private void _start() {
        
        try {
            
            //crea el socket de servidor que acceptarà als clients
            ServerSocket server = new ServerSocket(port, MAX_CONNECTIONS);
            
            //missatge de servidor actiu
            System.out.println("[!] Server ready and waiting for clients...");
            
            //espera síncronament connexions de clients 
            while (true) {
                
                //accepta les connexions dels clients
                Socket client = server.accept();
                
                //cada client és gestionat en un thread de servidor
                ServerThread sThread = new ServerThread(client, clientMap, isArtificialIntelligenceActive);
                Thread thread = new Thread(sThread);
                thread.start();
            }
           
        //Si alguna cosa falla en aquest punt no podem fer res més que avisar a l'aplicació
        //servidor escribint un missatge a la seva pantalla i enviar un missatge de fallada
        //de servidor a cada client.
        } catch (IOException error) {
            
            //missatge de fallada a les aplicacions client
            for (ServerClientInfo info : clientMap.values()) {
                
                Socket socket = info.getSocket();
                
                try {
                    ComUtils utils = new ComUtils(socket);
                    String message = "The connection has stopped unexpectedly.";
                    utils.writeProtocolError(new ProtocolError(message));
                
                } catch (IOException e) {
                    //Si intenta escriure al buffer s'un client prèviament desconnectat
                    //simplement no es fa res.
                }
            } 
            
        //finalment el servidor informa de la fallada a la seva pantalla
        } finally {
            
            //missatge de fallada a l'aplicació servidor
            System.err.println("Problem with socket or when accepting a client.");
        }
    }

    /**
     * Mètode principal
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // si no s'han donat arguments o s'ha indicat el d'ajuda, es mostra
        // quins paràmetres s'esperen
        if (args.length == 0 || (args.length == 1 && args[0].equals(MAIN_HELP_PARAM))){
            System.out.println(MAIN_USAGE_MESSAGE);
        }
        
        // es computa el port a usar, comprovant que sigui el del protocol
        else if (args.length >= 2 && args[0].equals(MAIN_PORT_PARAM)){
            
            if (ComUtils.isNumeric(args[1])){
                
                int port = Integer.parseInt(args[1]);
                
                if (port != ProtocolController.PROTOCOL_PORT){
                    System.out.println("Protocol expects " + ProtocolController.PROTOCOL_PORT + "as port.");
                }
                
                else{
                
                    boolean isArtificialIntelligenceActive = false;

                    boolean success = false;

                    if (args.length == 2) success = true;

                    else if(args.length == 4 && args[2].equals(MAIN_INTERACTIVE_PARAM)){

                        switch(args[3]){

                            case "1":
                                success = true;
                                break;

                            case "2":
                                isArtificialIntelligenceActive = true;
                                success = true;
                                break;

                            default:
                                break;
                        }
                    }
                  
                    if (success){

                        Server server = new Server(port, isArtificialIntelligenceActive);
                        server._start();
                    }

                    else System.out.println(MAIN_USAGE_MESSAGE);
                }
            }
            
            else{
                System.out.println("The port must be a positive number!");
            }
        }
    }
    
}

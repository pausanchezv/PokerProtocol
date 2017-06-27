/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import protocol.ProtocolController;
import utils.ComUtils;
import view.ClientThread;
import view.ClientThread.ClientInteractivityMode;

/**
 *
 * @author psanchva9.alumnes
 */
public final class Client {
    
    
    // valors relatius a l'execució del main per terminal
    private static final String MAIN_HELP_PARAM = "-h";
    private static final String MAIN_SERVER_PARAM = "-s";
    private static final String MAIN_PORT_PARAM = "-p";
    private static final String MAIN_INTERACTIVE_PARAM = "-i";
    private static final String MAIN_USAGE_MESSAGE = "Wrong syntax. Usage: java Client " + MAIN_SERVER_PARAM + " <server_machine> " + MAIN_PORT_PARAM + " <port> ["+ MAIN_INTERACTIVE_PARAM +" 0|1|2]";

    
    //host de comunicació
    private final String host;
    
    //port de comunicació
    private final int port;
    
    // mode d'interacció
    private final ClientInteractivityMode mode;
    
    
    //scanner per llegir dades de teclat
    private final Scanner sc;
    
    /**
     * Constructor
     * @param host
     * @param port 
     * @param mode 
     */
    public Client(String host, int port, ClientInteractivityMode mode) {
        this.host = host;
        this.port = port;
        this.mode = mode;
        sc = new Scanner(System.in);
    }
    
    /**
     * Inicialitza el client
     */
    private void _start() {
        
        Socket client = null;
        
        try {
            
            //crea el socket del client
            client = new Socket(host, port);
            ComUtils utils = new ComUtils(client);
            
            //crea el fil del client per rebre dades asíncronamet
            ClientThread ct = new ClientThread(utils, mode);
            Thread thread = new Thread(ct);
            thread.start();
            
            //espera que es notifiqui el final del joc amb el servidor
            while (!ct.getProtocolController().hasEnded()) {}
            
        //error al crear el Socket o a l'escriure al seu buffer
        //s'informa de l'error a la consola del procés client
        } catch (IOException e) {
            System.err.println("Connection error! Ensure that server is running and ready before starting client!");
        }
        
        // sempre s'intenta tancar el socket
        finally{
            if (client != null){ 
                try {
                    client.close();
                } catch (IOException ex) {
                }
            }
        }
        
    }
    
    /**
     * Programa principal
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // si no s'han donat arguments o s'ha indicat el d'ajuda, es mostra
        // quins paràmetres s'esperen
        if (args.length == 0 || (args.length == 1 && args[0].equals(MAIN_HELP_PARAM))){
            System.out.println(MAIN_USAGE_MESSAGE);
        }
        
        // s'obté el nom de la màquina servidora
        else if (args.length >= 4 && args[0].equals(MAIN_SERVER_PARAM)){
            
            String host = args[1];
                
            if(args[2].equals(MAIN_PORT_PARAM) && ComUtils.isNumeric(args[3])){
                
                int port = Integer.parseInt(args[3]);
                
                if (port != ProtocolController.PROTOCOL_PORT){
                    System.out.println("Protocol expects " + ProtocolController.PROTOCOL_PORT + "as port.");
                }
                
                else{
                    
                    ClientInteractivityMode mode = ClientInteractivityMode.MANUAL;
                    
                    boolean success = false;

                    if (args.length == 4) success = true;
                    
                    else if (args.length == 6 && args[4].equals(MAIN_INTERACTIVE_PARAM)){
                        
                        switch(args[5]){

                        case "0":
                            mode = ClientInteractivityMode.MANUAL;
                            success = true;
                            break;

                        case "1":
                            mode = ClientInteractivityMode.AUTOMATIC_RANDOM;
                            success = true;
                            break;
                            
                        case "2":
                            mode = ClientInteractivityMode.AUTOMATIC_AI;
                            success = true;
                            break;

                        default:
                            break;
                        }
                    }

                    if (success){

                        Client client = new Client(host, port, mode);
                        client._start();
                    }
                    
                    else System.out.println(MAIN_USAGE_MESSAGE);
                }
            }

            else System.out.println(MAIN_USAGE_MESSAGE);
        }
            
        else{
            System.out.println(MAIN_USAGE_MESSAGE);
        }
    }
}

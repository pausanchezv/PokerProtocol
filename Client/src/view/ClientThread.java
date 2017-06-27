/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import controller.ClientProtocolController;
import java.io.IOException;
import protocol.ProtocolCommand;
import static protocol.ProtocolController.QUIT_COMMAND;
import protocol.ProtocolError;
import protocol.ProtocolObject;
import utils.ComUtils;

/**
 *
 * @author psanchva9.alumnes
 */
public class ClientThread implements Runnable{

    
    // tipus de modes d'interacció
    public enum ClientInteractivityMode {MANUAL, AUTOMATIC_RANDOM, AUTOMATIC_AI};

    
    //utils per rebre dades
    private final ComUtils utils;
    
    // controlador del protocol
    private final ClientProtocolController ctrl;
    
    // unitat de reacció a les comandes rebudes
    private final ClientCommandManager commandManager;
    
    /**
     * Retorna el controlador
     * @return 
     */
    public ClientProtocolController getProtocolController(){
        return ctrl;
    }
    
    
    /**
     * ClientThread Constructor
     * 
     * @param utils 
     * @param mode 
     */
    public ClientThread(ComUtils utils, ClientInteractivityMode mode) {
        
        this.utils = utils;
        this.ctrl = new ClientProtocolController(mode == ClientInteractivityMode.AUTOMATIC_AI);
        this.commandManager = new ClientCommandManager(utils, ctrl, mode);
    }

    @Override
    public void run() {
        
        System.out.println("[!] Client is connected with server and ready for input...\n[!] Type " + QUIT_COMMAND + " to quit at any time.\n");
        
        boolean wantsToQuit = false;
        
        try {
            // s'envia la comanda d'inici
            commandManager.sendStartCommand();
        } catch (ClientCommandManager.ClientQuitException ex) {
            wantsToQuit = true;
        }
        
        //processa les dades rebudes i les mostra per pantalla
        while (!wantsToQuit) {

            try {
                
                // obtenim un objecte de protocol
                ProtocolObject protocolObj = ctrl.readCommand(utils);
                
                if (protocolObj != null){
                    
                    // si s'ha rebut una comanda, es mostra per pantalla
                    if (protocolObj instanceof ProtocolCommand){
                        
                        ProtocolCommand command = (ProtocolCommand)protocolObj;
                        
                        System.out.println("S: " + command);
                        
                        ctrl.processCommand(command.toString());
                        
                        // es reacciona a la comanda, de forma manual o automàtica
                        wantsToQuit = commandManager.reactToCommand(command);
                    }
                    
                    // si hi ha hagut un error en llegir la comanda
                    else if (protocolObj instanceof ProtocolError){
                    }
                    
                    else{
                        //   System.out.println("[!] No command nor error could be obtained when trying to read a command.");
                    }
                }
            } catch (IOException ex) {
                System.out.println("[!] Connection has ended. Quitting..."); 
                wantsToQuit = true;
            }        
        }
        
        stopThread();
    }
    
    
    /**
     * Realitza tasques de finalització
     */
    private void stopThread() {
        ctrl.setEnded(true);
        System.exit(0);
    }
}

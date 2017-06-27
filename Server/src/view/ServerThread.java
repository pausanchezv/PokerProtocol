/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import controller.ServerProtocolController;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import protocol.ProtocolCommand;
import controller.ProtocolController;
import protocol.ProtocolError;
import protocol.ProtocolObject;
import utils.ComUtils;



/**
 *
 * @author psanchva9.alumnes
 */
public class ServerThread implements Runnable {
    
    // taula hash dels clients (id/dades que persisteixen si marxa)
    private final ConcurrentHashMap<Integer, ServerClientInfo> clientMap;
    
    //socket del client a gestionar
    private final Socket client;
    
    //utils per transferir dades
    private final ComUtils utils;
    
    // instància del controlador
    private final ServerProtocolController ctrl;
    
    // condició de sortida del bucle
    boolean shouldExit = false;
    
    // indica si està activa la IA
    private final boolean isArtificialIntelligenceActive;

    // estructura per escriure el log
    File logFile;
    
    /**
     * ServerThread Constructor
     * 
     * @param client
     * @param clientMap
     * @param isArtificialIntelligenceActive
     * @throws java.io.IOException 
     */
    public ServerThread(Socket client, ConcurrentHashMap clientMap, boolean isArtificialIntelligenceActive) throws IOException {
        this.clientMap = clientMap;
        this.client = client;
        this.isArtificialIntelligenceActive = isArtificialIntelligenceActive;
        this.utils = new ComUtils(client);
        this.ctrl = new ServerProtocolController();
    }

    @Override
    public void run() {
        
        // es crea l'arxiu de log per registrar tot el que passi
        createLogFile();
        
        int id = 0;

        // s'obté l'id del jugador, que ha de ser un enter positiu
        while (id <= 0 && !shouldExit){ 
            try {
                //espera la comanda 'start' per iniciar la partida
                id = manageStart();
                
                if (ctrl.isForcedToQuit()){
                    shouldExit = true;
                    notifyForcedToQuit();
                }

            } catch (SocketException ex) {}
        }
        
        if (!shouldExit) {

            // inicialització del controlador del protocol, que reenvia algunes comandes al client
            for (String outCommandString : ctrl.start(clientMap.get(id).getStakes(), isArtificialIntelligenceActive)) {
                printAndLog("S responding (" + id + "): " + outCommandString);
                ProtocolCommand outCommand = ProtocolCommand.obtainProtocolCommandFromString(outCommandString);
                
                boolean success = ctrl.writeCommand(outCommand, utils);
                            
                if (!success){
                    shouldExit = true;
                    String illegalConditionsMessage = "Avorting game (illegal conditions, such as non-positive stakes)";
                    utils.writeProtocolError(new ProtocolError(illegalConditionsMessage));
                    printAndLog(illegalConditionsMessage, false);
                }
            }
        }
        
        if (ctrl.isForcedToQuit()){
            shouldExit = true;
            notifyForcedToQuit();
        }

        ProtocolObject protocolObj;
        ProtocolCommand command;
        ProtocolCommand outCommand;
        String commandType;

        //espera i processa les comandes enviades pel client
        while(!shouldExit) {

            protocolObj = null;

            try{

                //llegeix la comanda enviada pel client
                protocolObj = ctrl.readCommand(utils);
                
            } catch (IOException ex){
            
                String exceptionMessage = ex.getMessage();
                if (exceptionMessage.equals(ProtocolController.BROKEN_PIPE_MESSAGE)){
                    shouldExit = true;
                    break;
                }
            
            }

            if (protocolObj != null){

                if (protocolObj instanceof ProtocolCommand){

                    command = (ProtocolCommand)protocolObj;

                    printAndLog("C (" + id + "): " + command);

                    // el controlador processa la comanda rebuda i envia les respostes
                    ArrayList<String> outCommands = ctrl.processCommand(command.toString());

                    if (outCommands != null && !outCommands.isEmpty()){

                        for (String outCommandString : outCommands) {

                            outCommand = ProtocolCommand.obtainProtocolCommandFromString(outCommandString);

                            printAndLog("S responding (" + id + "): " + outCommand);

                            boolean success = ctrl.writeCommand(outCommand, utils);
                            
                            if (!success){
                                shouldExit = true;
                                String illegalConditionsMessage = "Avorting game (illegal conditions, such as non-positive stakes)";
                                utils.writeProtocolError(new ProtocolError(illegalConditionsMessage));
                                printAndLog(illegalConditionsMessage, false);
                            }
                            
                        }
                    }
                    
                    
                    if (ctrl.isForcedToQuit()){
                        shouldExit = true;
                        notifyForcedToQuit();
                    }
                    else{
                        // comprova si s'ha rebut la comanda de sortida
                        commandType = (String)command.getCommandParts().get(0);
                        shouldExit = commandType.equals(ProtocolController.QUIT_COMMAND);
                    }
                }
                
                
                // si hi ha hagut un error de protocol, l'enviem
                else if (protocolObj instanceof ProtocolError){

                    printRedAndLog("[!] C (" + id + ") has introduced an unkown command!");

                    String errorCommand = ((ProtocolError)protocolObj).obtainComand();
                    printAndLog("S responding to a client with unknown id: "  + errorCommand);
                    utils.writeProtocolError((ProtocolError)protocolObj);
                }
            }
        }
            
        
        if (id > 0) {
            // es processa la sortida del jugador
            ctrl.processCommand(ProtocolController.QUIT_COMMAND);
        }
             
        try {

            // informem al servidor que el jugador ha sortit
            if (clientMap.containsKey(id)) {
                printAndLog("Player '" + id + "' has left.");
            } 
            else {
                printAndLog("The not-yet-identified player has left.");

            }

            // si el jugador ha arribat a estar acceptat
            if (clientMap.containsKey(id)){
                
                // actualitzem els stakes per quan el jugador torni
                if (ctrl != null) clientMap.get(id).setStakes(ctrl.getStakes());

                //eliminem el socket de la informació del client
                clientMap.get(id).setSocket(null);
            }

            //tanquem degudament el socket
            client.close();

            //es conclou l'operació amb un misatge d'èxit
            printAndLog("The socket has been successfully closed. Connection with player has ended.\n\n");

        //acaben les gestions d'errors possibles. Si alguna cosa falla mostrem el missatge pertinent
        } catch (IOException e) {
            printRedAndLog(e.getMessage());
        }
        
    }
    
    /**
     * Espera i processa la comanda start per començar la partida
     * 
     * @throws IOException 
     */
    private int manageStart() throws SocketException {
        
        
         // guardarà l'id del client
        int id = -1;
        
        // cada cop que salti el timeout es buidarà el buffer (per si un client
        // envia una comanda malament però la torna a enviar bé posteriorment,
        // evitar que es concatenin)
        client.setSoTimeout(1000);
        
        do {
            
            try {
            
                // llegeix la comanda del client
                ProtocolObject protocolObj = ctrl.readCommand(utils);

                if (protocolObj != null){

                    if (protocolObj instanceof ProtocolCommand){

                        ProtocolCommand command = (ProtocolCommand) protocolObj;

                        String commandString = command.toString();

                        printAndLog("C with unknown id: " + commandString);

                        // s'obtenen les parts de la comanda
                        ArrayList<Object> commandParts = command.getCommandParts();

                        // es mira si la comanda rebuda és correcta
                        boolean commandContainsStart = commandParts.size() == 2 && commandParts.get(0).equals(ProtocolController.START_COMMAND);

                        // es comprova que la comanda sigui un start tal i com s'espera
                        if (commandContainsStart) {

                            int candidateId;

                            // mirem si trobem l'id a la posició esperada de la comanda
                            if (commandParts.get(1) instanceof Integer){

                                candidateId = (Integer)commandParts.get(1);

                                // l'id ha de ser positiu, altrament comuniquem un error
                                if (candidateId <= 0){
                                    String errorMessage = "A client ID must be a positive integer!";
                                    printAndLog("S responding to a client with unknown id : "  + errorMessage);
                                    utils.writeProtocolError(new ProtocolError(errorMessage));
                                }
                                
                                // si l'id no existeix o correspon a un jugador que no està jugant actualment
                                else if (!clientMap.containsKey(candidateId) || !clientMap.get(candidateId).isPlaying()) {
                                    id = candidateId;

                                // aquest id correspon a un altre jugador en actiu; es notifica aquest fet
                                } else {
                                    
                                    String errorMessage = "A player with that ID is already playing! Try a different one!";
                                    printAndLog("S responding to a client with unknown id: "  + errorMessage);
                                    utils.writeProtocolError(new ProtocolError(errorMessage)); 
                                }
                            }

                        }  
                        
                        else {
                            
                            if (commandString.equals(ProtocolController.QUIT_COMMAND)) {
                                shouldExit = true;
                                return -1;
                            } else {
                                String errorMessage = "This command doesn't start with " + ProtocolController.START_COMMAND;
                                printAndLog("S responding to a client with unknown id : "  + errorMessage);
                                utils.writeProtocolError(new ProtocolError(errorMessage)); 
                            }

                        }

                    } 

                    // si hi ha hagut un error de protocol, l'indiquem
                    else if (protocolObj instanceof ProtocolError){
                        
                        printRedAndLog("[!] C with unknown id has introduced an unkown command!");

                        
                        String errorCommand = ((ProtocolError)protocolObj).obtainComand();
                        printAndLog("S responding to a client with unknown id : "  + errorCommand);
                        utils.writeProtocolError((ProtocolError)protocolObj);
                    }

                    
                }
            
            } 
            
            // hi ha hagut alguna irregularitat en la comanda
            catch (IOException e) {
                        
                // si la canonada del socket s'ha trencat vol dir que el client
                // ha marxat abruptament, cal sortir
                String exceptionMessage = e.getMessage();
                if (exceptionMessage.equals(ProtocolController.BROKEN_PIPE_MESSAGE)){
                    shouldExit = true;
                    return -1;
                }
            }
  
        } while (id <= 0);
        
        //s'afegeix el client a la taula de clients connectats; creant la seva unitat
        // d'informació o, si ja existia, actualizant-la amb el nou socket amb què
        // s'ha connectat
        if (clientMap.containsKey(id)){
            clientMap.get(id).setSocket(client);
        }
        else clientMap.put(id, new ServerClientInfo(client));
        
        return id;
    }
    
    /**
     * Notifica el fet de veure's forçat a acabar la connexió
     */
    private void notifyForcedToQuit() {
        String forcedToQuitMessage = "The game cannot continue (one of the players has less stakes than ante)";
        printAndLog(forcedToQuitMessage);
        utils.writeProtocolError(new ProtocolError(forcedToQuitMessage));
    }  

    
    /**
     * Mostra un missatge per pantalla i l'afegeix al log
     * @param message
     * @param markRed 
     */
    private void printAndLog(String message, boolean markRed) {
        
        if (markRed) System.err.println(message);
        
        else System.out.println(message);
        
        
        PrintWriter printWriter = null;

        try {
           File file = new File("Server"+Thread.currentThread().getName()+".log");
           FileWriter fileWriter = new FileWriter(file, true);
           printWriter = new PrintWriter(fileWriter);
           printWriter.println(message);
        } 
        
        catch (IOException e) {
            
            System.err.println(e);
        } 
        finally {
           if (printWriter != null) {
              printWriter.close();
           }
        }
    }
    
    
    /**
     * Mostra un missatge per pantalla i l'afegeix al log
     * @param message
     * @param markRed 
     */
    private void printAndLog(String message) {
        
        printAndLog(message, false);
    }

    /**
     * Mostra un missatge vermell per pantalla i l'afegeix al log
     * @param message 
     */
    private void printRedAndLog(String message) {
        printAndLog(message, true);    
    }

    
    /**
     * Crea l'arxiu de log
     */
    private void createLogFile() {
        
        try{
            logFile = new File("Server"+Thread.currentThread().getName()+".log");
            
            FileOutputStream stream = new FileOutputStream(logFile, false);
            byte[] bytes = "".getBytes();
            stream.write(bytes);
            stream.close();
            
        } catch (Exception e){
            System.err.println("There was a problem when creating the log file.");
        }
        
    }
}

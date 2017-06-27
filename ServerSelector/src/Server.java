/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import controller.ServerProtocolController;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import protocol.ProtocolCommand;
import controller.ProtocolController;
import protocol.ProtocolError;
import protocol.ProtocolObject;
import utils.ComUtilsBase;
import utils.ComUtilsSocketChannel;
import view.ServerClientInfo;

/**
 * Conjunt de dades sobre un client que el servidor utilitza i que manté fins i tot
 * si marxa, per quan torni
*/
class ServerClientData {
   
    /**
     * Controlador del protocol al servidor
     */
    private final ServerProtocolController controller;

    
    /**
     * Retorna el controlador
     * @return 
     */
    public ServerProtocolController getController() {
        return controller;
    }

    /**
     * Constructor
     * @param controller 
     */
    public ServerClientData() {
        this.controller = new ServerProtocolController();
    } 
    
}

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
    
    // valor indicatiu que no s'ha trobat l'id
    private static final int ID_NOT_FOUND = -1;
    
    // nombre d'instàncies de joc creades en total al servidor (cada cop que un client
    // es connecta s'incrementa)
    private int gameInstancesNum;
    
    
    /**
     * Server Constructor
     * @param port
     * @param isArtificialIntelligenceActive
     */
    public Server(int port, boolean isArtificialIntelligenceActive) {
        this.gameInstancesNum = 0;
        this.port = port;
        this.isArtificialIntelligenceActive = isArtificialIntelligenceActive;
        clientMap = new ConcurrentHashMap(MAX_CONNECTIONS);
    }
    
    
    /**
     * 
     * @throws IOException 
     */
    private void start() {
        
        // inicialitzacions el selector i el server
        Selector selector = null;
        ServerSocketChannel server = null;
        SelectionKey serverKey = null;
        
        // s'intenta crear el selector i el canal del servidor
        try {
        
            // es crea i s'obre el selector
            selector = Selector.open();     

            // s'obre el canal del servidor
            server = ServerSocketChannel.open();
            InetSocketAddress hostAddr = new InetSocketAddress(port);
            server.socket().bind(hostAddr);

            // es selecciona el mode 'no bloquejant'
            server.configureBlocking(false);
            
            // operacions admeses del canal
            int operations = server.validOps();

            // clau del servidor
            serverKey = server.register(selector, operations);
        
        } catch (IOException ex) {
            printRedAndLog("[!] Problemem creating or setting up the server: " + ex.getMessage());
        }
        
        
        // el servidor espera l'arribada de clients
        printAndLog("[!] Server ready and waiting for clients...");
        
        // gestiona síncronament l'arribada de nous clients i les peticions dels clients existents
        while (true) {
            
            // es comprova que el selector hagi estat creat correctament
            if (selector != null && server != null) {
               
                // s'intenta fer una selecció
                try {
                    
                    // es bloqueja aqui fins que algú es connecta o envia una comanda
                    selector.select();
                    
                } catch (IOException ex) {
                    printRedAndLog("Error using the selector: " + ex.getMessage());
                }

                // s'obté el conjunt de keys del selector, les quals seran iterades per a ser gestionades
                Set selectedKeys = selector.selectedKeys();
                Iterator it = selectedKeys.iterator();

                // s'iteren totes les keys del selector
                while (it.hasNext()) {

                    SelectionKey key = (SelectionKey) it.next();

                    // si la key es servidor i a més és acceptable involucra nova connexió
                    if (key == serverKey && key.isAcceptable()) {
                       
                        // el servidor accepta un client
                        SocketChannel client;
                        
                        // el servidor intenta acceptar un client
                        try {
                            
                            // el servidor accepta el client
                            client = (SocketChannel) server.accept();
                            
                            // es configura com a no-bloquejant
                            client.configureBlocking(false);
                            
                            // s'afegeix la nova connexió al servidor
                            SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ);
                            
                            // es passa un objecte al client
                            clientKey.attach(new ServerClientData());
                            
                            // un nou client s'ha connectat
                            gameInstancesNum++;

                            
                        } catch (IOException ex) {
                            printRedAndLog("Error when accepting a client: " + ex.getMessage());
                        }
                    }

                    // si la key és llegible aleshores el servidor ha d'atendre un client existent
                    else if (key.isReadable()) {

                        // s'obté el canal del client a partir de la clau
                        SocketChannel client = (SocketChannel) key.channel();

                        // es crea un buffer
                        ComUtilsSocketChannel utils = new ComUtilsSocketChannel(client);
 
                        // es processa el joc
                        // aqui es fa tot allò necessari corrsponent a la lògica del joc i despŕes
                        // es dóna la resposta al client
                        processClientData(key, client, utils);
                    }

                    // es destrueix l'objecte ja que l'iterador es crea a cada cicle del while true
                    it.remove();
                }
            }
        }
    }
    
    /**
     * Tanca la connexió amb un client
     * @param key
     * @param client
     * @param clients 
     */
    private void closeClient(SelectionKey key, SocketChannel client) {
        
        // es cancel·la la key
        key.cancel();

        // s'intenta tancar el socket del client
        try {
            client.close();
        } catch (IOException ex) {
            printRedAndLog("Problem closing the client's socket channel: " + ex.getMessage());
        }
        
    }

    /**
     * Retorna si la taula hash de clients té algun client amb el socket channel passat
     */
    private boolean hasClientMapAClientWithSocketChannel(SocketChannel socketChannel){
        
        for (ServerClientInfo info : clientMap.values()){
            if (info.getSocket() != null && info.getSocket().equals(socketChannel)){
                return true;
            }
        }
        return false;
    }
    

    /**
     * Retorna l'id d'un client a partir del seu socket channel (suposant que està
     * a la taula de clients, sinó un valor d'error)
     * @param socketChannel
     * @return 
     */
    private int obtainClientMapIdFromSocketChannel(SocketChannel socketChannel){
        
        for (Map.Entry<Integer, ServerClientInfo> entry : clientMap.entrySet()){
            
            if (entry != null && entry.getValue() != null && entry.getValue().getSocket() != null){
            
                if (entry.getValue().getSocket().equals(socketChannel)){
                    return entry.getKey();
                }
            }
            
        }
        return ID_NOT_FOUND;
    }
    
    /**
     * Processa les dades del client, gestionant l'acceptació pel joc i el joc en si
     * @param key 
     * @param clientId
     * @param utils
     */
    private void processClientData(SelectionKey key, SocketChannel client, ComUtilsSocketChannel utils) {
        
        
        int clientId = obtainClientMapIdFromSocketChannel(client);
        
        // s'obté l'objecte del joc amb aquest client
        ServerClientData clientData = (ServerClientData) key.attachment();
        
        ServerProtocolController ctrl = clientData.getController();
                
        // breu espera per moderar la velocitat d'intercanvi de dades entre client
        // i servidor automàtics (per no omplir el log tan ràpidament)
        briefWait();
        
        // es detecta si el jugador té una partida iniciada, és a dir, ja ha enviat START
        boolean isPlaying = clientId != ID_NOT_FOUND;
 
        // si no està jugant encara, es mira si envia la comanda d'inici per començar
        if (!isPlaying){
            
            // es mira si es rep una comanda d'inici vàlida del client per poder
            // acceptar el seu id
            clientId = manageStart(key, client, ctrl, utils);
            
            // es mira si s'ha de sortir
            if (ctrl.isForcedToQuit()){
                notifyForcedToQuit(utils);
                computeClientLeaving(key, client, ctrl);
            }
            
            if (clientId > 0){
                
                // inicialització del controlador del protocol, que reenvia algunes comandes al client
                for (String outCommandString : ctrl.start(clientMap.get(clientId).getStakes(), isArtificialIntelligenceActive)) {
                    printAndLog("S responding (" + clientId + "): " + outCommandString);
                    ProtocolCommand outCommand = ProtocolCommand.obtainProtocolCommandFromString(outCommandString);

                    boolean success = ctrl.writeCommand(outCommand, utils);

                    if (!success){
                        String illegalConditionsMessage = "Avorting game (illegal conditions, such as non-positive stakes)";
                        utils.writeProtocolError(new ProtocolError(illegalConditionsMessage));
                        printAndLog(illegalConditionsMessage, false);
                    }
                }
            }
            
            // es mira si s'ha de sortir
            if (ctrl.isForcedToQuit()){
                notifyForcedToQuit(utils);
                computeClientLeaving(key, client, ctrl);
            }
        }
        
        // si està jugant, es processa la lògica del joc
        else {
                      
            ProtocolObject protocolObj = null;

            try{

                //llegeix la comanda enviada pel client
                protocolObj = ctrl.readCommand(utils);
                
            } catch (IOException ex){}

            if (protocolObj != null){

                if (protocolObj instanceof ProtocolCommand){

                    ProtocolCommand command = (ProtocolCommand)protocolObj;

                    printAndLog("C (" + clientId + "): " + command);

                    // el controlador processa la comanda rebuda i envia les respostes
                    ArrayList<String> outCommands = ctrl.processCommand(command.toString());

                    if (outCommands != null && !outCommands.isEmpty()){

                        for (String outCommandString : outCommands) {

                            ProtocolCommand outCommand = ProtocolCommand.obtainProtocolCommandFromString(outCommandString);

                            printAndLog("S responding (" + clientId + "): " + outCommand);

                            boolean success = ctrl.writeCommand(outCommand, utils);
                            
                            if (!success){
                                String illegalConditionsMessage = "Avorting game (illegal conditions, such as non-positive stakes)";
                                utils.writeProtocolError(new ProtocolError(illegalConditionsMessage));
                                printAndLog(illegalConditionsMessage, false);
                            }
                            
                        }
                    }
                    
                    
                    // es mira si s'ha de sortir de manera forçosa
                    if (ctrl.isForcedToQuit()){
                        notifyForcedToQuit(utils);
                        computeClientLeaving(key, client, ctrl);
                    }
                }
                
                
                // si hi ha hagut un error de protocol, l'enviem
                else if (protocolObj instanceof ProtocolError){

                    printRedAndLog("[!] C (" + clientId + ") has introduced an unkown command!");

                    String errorCommand = ((ProtocolError)protocolObj).obtainComand();
                    printAndLog("S responding to a client with unknown id: "  + errorCommand);
                    utils.writeProtocolError((ProtocolError)protocolObj);
                }
            }
            
            
            // si l'objecte és nul s'assumeix sortida abrupta
            else{
                
                notifyForcedToQuit(utils);
                computeClientLeaving(key, client, ctrl);
            }
              
        }
        
        // es torna a adjuntar el joc a la clau
        key.attach(clientData);
    }
    
    /**
     * Notifica el fet de veure's forçat a acabar la connexió
     */
    private void notifyForcedToQuit(ComUtilsSocketChannel utils) {
        String forcedToQuitMessage = "The game cannot continue (player has quit or one of the players has less stakes than ante)";
        printAndLog(forcedToQuitMessage);
        utils.writeProtocolError(new ProtocolError(forcedToQuitMessage));
    }  
    
    // breu ralentització
    private void briefWait(){
        // operacions per ralentitzar breument l'execució
        for(int i = 0; i < 1000000; i++){
            int j = 0;
            System.out.print("");
            j = (int) (Math.atan2(i, j^2) * (int) Math.sqrt(j));
        }
    }
    
    /**
     * Espera i processa la comanda start per començar la partida
     * 
     * @throws IOException 
     */
    private int manageStart(SelectionKey key, SocketChannel client, ServerProtocolController ctrl, ComUtilsSocketChannel utils) {
         
         // guardarà l'id del client
        int id = -1;

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
            
            // si l'objecte és nul s'assumeix sortida abrupta
            else{
                
                notifyForcedToQuit(utils);
                computeClientLeaving(key, client, ctrl);
            }

        } 

        // hi ha hagut alguna irregularitat en la comanda
        catch (IOException e) {
                
            notifyForcedToQuit(utils);
            computeClientLeaving(key, client, ctrl);
            
            // si la canonada del socket s'ha trencat vol dir que el client
            // ha marxat abruptament, cal sortir
            String exceptionMessage = e.getMessage();
            if (exceptionMessage == null || exceptionMessage.equals(ProtocolController.BROKEN_PIPE_MESSAGE)){
                return -1;
            }
        }
  
        
        //s'afegeix el client a la taula de clients connectats; creant la seva unitat
        // d'informació o, si ja existia, actualizant-la amb el nou socket amb què
        // s'ha connectat
        if (clientMap.containsKey(id)){
            clientMap.get(id).setSelectionKey(key);
            clientMap.get(id).setSocket(client);  
        }
        else clientMap.put(id, new ServerClientInfo(client, key));
        
        return id;
    }
    
       /**
     * Mostra un missatge per pantalla i l'afegeix al log
     * @param message
     * @param markRed 
     */
    private void printAndLog(String message, boolean markRed) {
        
        message = "\n" + message;
        
        if (markRed) System.err.println(message);
        
        else System.out.println(message);
        
        
        PrintWriter printWriter = null;

        try {
           File file = new File("ServerGame-" + gameInstancesNum + ".log");
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
            
            if (ComUtilsBase.isNumeric(args[1])){
                
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
                        server.start();
                    }

                    else System.out.println(MAIN_USAGE_MESSAGE);
                }
            }
            
            else{
                System.out.println("The port must be a positive number!");
            }
        }
    }

    
    /**
     * Es computa la sortida d'un jugador, que haurà de ser retornat quan torni
     * @param key
     * @param client 
     */
    private void computeClientLeaving(SelectionKey key, SocketChannel client, ServerProtocolController ctrl) {
        
        if (hasClientMapAClientWithSocketChannel(client)){
            
            int id = obtainClientMapIdFromSocketChannel(client);
            
            // anul·lem socket i clau ja que quan torni caldrà donar-ne noves
            clientMap.get(id).setSocket(null);
            clientMap.get(id).setSelectionKey(null);
            
            try{
                // es processa la sortida del jugador
                ctrl.processCommand(ProtocolController.QUIT_COMMAND); 

                // informem al servidor que el jugador ha sortit
                printAndLog("Player '" + id + "' has left.");

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
        else{
            printAndLog("The not-yet-identified player has left.");
        }
        
        // tanca el socket i cancel·la la clau de selecció
        closeClient(key, client);
    }
    
}

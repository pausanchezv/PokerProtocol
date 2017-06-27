/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import poker.CardRank;
import poker.CardSuit;
import poker.Hand;
import protocol.ProtocolCommand;
import protocol.ProtocolError;
import protocol.ProtocolObject;
import utils.ComUtilsBase;
import static utils.ComUtilsBase.isNumeric;

/**
 * Controlador de protocol
 * @author psanchva9.alumnes
 */
public abstract class ProtocolController {
    
    //port de comunicació del protocol
    public static final int PROTOCOL_PORT = 1212;
    
    // separador
    public static final String SEPARATOR = " ";
    public static final char SEPARATOR_CHAR = ' ';
    

    // missatge de broken pipe
    public static final String BROKEN_PIPE_MESSAGE = "Broken Pipe";
    
    // Strings identificatius de qui és el dealer
    public static final String HOST_IS_DEALER = "0";
    public static final String NON_HOST_IS_DEALER = "1";
    public static final int DEALER_INDICATOR_LENGTH = HOST_IS_DEALER.length();
    
    // longitud obligatòria de les comandes
    public static final int COMMAND_LENGTH = 4;
    
    
    // cadascuna de les comandes que contempla el protocol
    public static final String START_COMMAND = "STRT";
    public static final String ANTE_COMMAND = "ANTE";
    public static final String STAKES_COMMAND = "STKS";
    public static final String ANTE_OK_COMMAND = "ANOK";
    public static final String QUIT_COMMAND = "QUIT";
    public static final String DEALER_COMMAND = "DEAL";
    public static final String HAND_COMMAND = "HAND";
    public static final String PASS_COMMAND = "PASS";
    public static final String BET_COMMAND = "BET_";
    public static final String CALL_COMMAND = "CALL";
    public static final String FOLD_COMMAND = "FOLD";
    public static final String RAISE_COMMAND = "RISE";
    public static final String DRAW_COMMAND = "DRAW";
    public static final String DRAW_SERVER_COMMAND = "DRWS";
    public static final String SHOWNDOWN_COMMAND = "SHOW";
    public static final String ERROR_COMMAND = "ERRO";

    // taula hash que relaciona cada comanda amb la sintaxi esperada
    public static  HashMap<String, String> commandSyntaxMap = obtainCommandSyntaxMap();
    
    
    /**
     * Retorna la sintaxi exacte de les comandes del protocol
     * 
     * @return 
     */
    private static HashMap<String, String> obtainCommandSyntaxMap() {
        
        commandSyntaxMap = new HashMap();
                
        commandSyntaxMap.put(START_COMMAND, START_COMMAND + "<SP><ID>");        
        commandSyntaxMap.put(ANTE_COMMAND, ANTE_COMMAND + "<SP><CHIPS>");        
        commandSyntaxMap.put(STAKES_COMMAND, STAKES_COMMAND + "<SP><CHIPS><SP><CHIPS>");        
        commandSyntaxMap.put(ANTE_OK_COMMAND, ANTE_OK_COMMAND);       
        commandSyntaxMap.put(QUIT_COMMAND, QUIT_COMMAND);        
        commandSyntaxMap.put(DEALER_COMMAND, DEALER_COMMAND + "<SP>'0'|'1'");      
        commandSyntaxMap.put(HAND_COMMAND, HAND_COMMAND + "<SP><CARD><SP><CARD><SP><CARD><SP><CARD><SP><CARD>");       
        commandSyntaxMap.put(PASS_COMMAND, PASS_COMMAND);       
        commandSyntaxMap.put(BET_COMMAND, BET_COMMAND + "<SP><CHIPS>");       
        commandSyntaxMap.put(CALL_COMMAND, CALL_COMMAND);       
        commandSyntaxMap.put(FOLD_COMMAND, FOLD_COMMAND);        
        commandSyntaxMap.put(RAISE_COMMAND, RAISE_COMMAND + "<SP><CHIPS>");   
        commandSyntaxMap.put(DRAW_COMMAND, DRAW_COMMAND + "<SP><'0'|'1'|'2'|'3'|'4'|'5'>(<SP><CARD>)[0-5]");
        commandSyntaxMap.put(DRAW_SERVER_COMMAND, DRAW_SERVER_COMMAND + "(<SP><CARD>)[0-5]<SP><'0'|'1'|'2'|'3'|'4'|'5'>");
        commandSyntaxMap.put(SHOWNDOWN_COMMAND, SHOWNDOWN_COMMAND + "<SP><CARD><SP><CARD><SP><CARD><SP><CARD><SP><CARD>");
        commandSyntaxMap.put(ERROR_COMMAND, ERROR_COMMAND + "<SP><D><D>(<C*>)");
        
        return commandSyntaxMap;
        
    }
    
    
       
    /**
     * Llegeix del socket una comanda, construint i retornant un objecte representatiu
     * de la informació que conté si segueix la sintaxi del protocol, i retornant un error
     * de protocol en cas contrari
     * en cas contrari
     * @param utils
     * @return 
     * @throws java.io.IOException 
     */
    public ProtocolObject readCommand(ComUtilsBase utils) throws IOException {   
        
        try {
            
            // sempre es comença llegint els 4 caràcters que delimiten una comanda;
            // si el que es llegeix correspon a un tipus conegut de comanda se segueix,
            // altrament es retorna un String buit
            
            String commandType = utils.read_string_of_length(COMMAND_LENGTH);
            commandType = commandType.toUpperCase();
        
            ArrayList<Object> commandParts = new ArrayList();
            commandParts.add(commandType);
            
            String unexpectedSyntaxMessage = "Unexpected arguments or syntax! Expected syntax: ";
            String unexpecteArgumentTypeMessage = "Unexpected argument type!";

            
            char separator;
            
            // llegim la resta de la informació de forma diferent segons el tipus
            // de comanda
            
            switch (commandType) {
                

                case START_COMMAND:
                    
                    
                    // hem de llegir el separador, en cas contrari es notifica un
                    // error de sintaxi
                    separator = utils.readSeparator();
 
                    if (separator != SEPARATOR_CHAR){
                       return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(START_COMMAND));

                    }
                                        
                    // hem de llegir l'ID, que ha de ser un enter positiu
                    int clientId = utils.read_int32();
                    
                    // si el valor no és un enter positiu, un error ho notificarà
                    if (clientId <= 0){
                        return new ProtocolError(START_COMMAND + " expects a positive integer as client id.");
                    }

                    // si tot és correcte, es retornarà un objecte comanda que inclourà
                    // l'id del client entre la seva informació
                    commandParts.add(clientId);
                    break;


                case ANTE_COMMAND:
                    
                    // hem de llegir el separador, en cas contrari es notifica un
                    // error de sintaxi
                    separator = utils.readSeparator();
 
                    if (separator != SEPARATOR_CHAR){
                       return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(ANTE_COMMAND));

                    }
                   
                    // hem de llegir l'ante, que ha de ser un enter positiu
                    int ante = utils.read_int32();
                    
                    // si el valor no és un enter positiu, un error ho notificarà
                    if (ante <= 0){
                        return new ProtocolError(ANTE_COMMAND + " expects a positive integer as ante.");
                    }

                    // si tot és correcte, es retornarà un objecte comanda que inclourà
                    // l'ante entre la seva informació
                    commandParts.add(ante);
                    break;



                case STAKES_COMMAND:

                    // hem de llegir el separador, en cas contrari es notifica un
                    // error de sintaxi
                    separator = utils.readSeparator();
 
                    if (separator != SEPARATOR_CHAR){
                       return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(STAKES_COMMAND));

                    }
                   
                    // hem de llegir els stakes del primer jugador, cosa que ha de ser un enter positiu
                    int stakes0 = utils.read_int32();
                    
                    // si el valor no és un enter positiu, un error ho notificarà
                    if (stakes0 <= 0){
                        return new ProtocolError(STAKES_COMMAND + " expects a positive integer as stake num.");
                    }
                    
                    // hem de llegir el separador, en cas contrari es notifica un
                    // error de sintaxi
                    separator = utils.readSeparator();
 
                    if (separator != SEPARATOR_CHAR){
                       return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(STAKES_COMMAND));
                    }
                   
                    // hem de llegir els stakes del segon jugador, cosa que ha de ser un enter positiu
                    int stakes1 = utils.read_int32();
                    
                    // si el valor no és un enter positiu, un error ho notificarà
                    if (stakes1 <= 0){
                        return new ProtocolError(STAKES_COMMAND + " expects a positive integer as stake num.");
                    }

                    // si tot és correcte, es retornarà un objecte comanda que inclourà
                    // els stakes de cada jugador entre la seva informació
                    commandParts.add(stakes0);
                    commandParts.add(stakes1);

                    break;

                case ANTE_OK_COMMAND:

                    // aquesta comanda no té res més que el seu nom, no cal llegir res just després
                    break;

                case QUIT_COMMAND:
                    
                    // aquesta comanda no té res més que el seu nom, no cal llegir res just després
                    break;

                    
                case DEALER_COMMAND:
                    
                    // hem de llegir el separador, en cas contrari es notifica un
                    // error de sintaxi
                    separator = utils.readSeparator();
 
                    if (separator != SEPARATOR_CHAR){
                       return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(DEALER_COMMAND));
                    }
                    
                    // s'ha de llegir un valor indicant qui és el dealer
                    String dealerIndicator = utils.read_string_of_length(DEALER_INDICATOR_LENGTH);
                    
                    // cal que el valor llegit sigui un dels possibles, altrament cal un
                    // missatge d'error
                    if (!dealerIndicator.equals(HOST_IS_DEALER) && !dealerIndicator.equals(NON_HOST_IS_DEALER)){
                        return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(DEALER_COMMAND));
                    }
                    
                    commandParts.add(dealerIndicator);
                    
                    break;

                
                case HAND_COMMAND:
                      
                    // han d'aparèixer 5 cartes, amb separadors al davant i un 
                    // format de carta vàlid (rang i pal)
                    for (int i = 0; i < Hand.NUM_CARDS; i++){
                        
                        // hem de llegir el separador, en cas contrari es notifica un
                        // error de sintaxi
                        separator = utils.readSeparator();

                        if (separator != SEPARATOR_CHAR){
                           return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(HAND_COMMAND));
                        }
                        
                        Object cardReadObj = readCard(utils);
                        
                        if (cardReadObj instanceof ProtocolError){
                            return (ProtocolError)cardReadObj;
                        }
                        
                        else if (cardReadObj instanceof String){
                            
                             String cardString = (String)cardReadObj;
                             
                             commandParts.add(cardString);
                             
                        }
                    }
                    
                    if (commandParts.size() != 6) { 
                        return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(HAND_COMMAND));
                    }
                    

                case PASS_COMMAND:
                    
                    // aquesta comanda no té res més que el seu nom, no cal llegir res just després
                    break;

                case BET_COMMAND:
                                        
                    // hem de llegir el separador, en cas contrari es notifica un
                    // error de sintaxi
                    separator = utils.readSeparator();
 
                    if (separator != SEPARATOR_CHAR){
                       return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(BET_COMMAND));

                    }
                   
                    // hem de llegir l'aposta, que ha de ser un enter positiu
                    int bet = utils.read_int32();
                    
                    // si el valor no és un enter positiu, un error ho notificarà
                    if (bet <= 0){
                        return new ProtocolError(BET_COMMAND + " expects a positive integer as ante.");
                    }
                    

                    // si tot és correcte, es retornarà un objecte comanda que inclourà
                    // el bet
                    commandParts.add(bet);
                    
                    break;

                case CALL_COMMAND:
                    
                    // aquesta comanda no té res més que el seu nom, no cal llegir res just després
                    break;

                case FOLD_COMMAND:
                    // aquesta comanda no té res més que el seu nom, no cal llegir res just després
                    break;

                case RAISE_COMMAND:
                    
                    // hem de llegir el separador, en cas contrari es notifica un
                    // error de sintaxi
                    separator = utils.readSeparator();
 
                    if (separator != SEPARATOR_CHAR){
                       return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(RAISE_COMMAND));
                    }
                   
                    // hem de llegir l'augment d'aposta, que ha de ser un enter positiu
                    int raise = utils.read_int32();
                    
                    // si el valor no és un enter positiu, un error ho notificarà
                    if (raise <= 0){
                        return new ProtocolError(RAISE_COMMAND + " expects a positive integer as raise.");
                    }
                    
                    // si tot és correcte, es retornarà un objecte comanda que inclourà
                    // el raise
                    commandParts.add(raise);
                    
                    break;

                case DRAW_COMMAND:
                    
                    // hem de llegir el separador, en cas contrari es notifica un
                    // error de sintaxi
                    separator = utils.readSeparator();
 
                    if (separator != SEPARATOR_CHAR){
                       return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(DRAW_COMMAND));
                    }
                    
                    // s'ha de llegir el nombre de cartes a canviar (entre 0 i 5)
                    int swapCardNum = Character.getNumericValue(utils.readSeparator());
                    if (swapCardNum > 5 || swapCardNum < 0){
                        return new ProtocolError(DRAW_COMMAND + " expects a swap card number between 0 and 5 (inclusive).");
                    }
                    
                    commandParts.add(swapCardNum);
                    
                    // han d'aparèixer tantes cartes com l'enter indiqui, amb
                    // separadors al davant i un format de carta vàlid (rang i pal)
                    for (int i = 0; i < swapCardNum; i++){
                        
                        // hem de llegir el separador, en cas contrari es notifica un
                        // error de sintaxi
                        separator = utils.readSeparator();

                        if (separator != SEPARATOR_CHAR){
                           return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(HAND_COMMAND));
                        }
                        
                        Object cardReadObj = readCard(utils);
                        
                        if (cardReadObj instanceof ProtocolError){
                            return (ProtocolError)cardReadObj;
                        }
                        
                        else if (cardReadObj instanceof String){
                            
                             String cardString = (String)cardReadObj;
                             
                             commandParts.add(cardString);
                             
                        }
                        
                    }
                    
                    if (commandParts.size() != swapCardNum + 2) { 
                        return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(HAND_COMMAND));
                    }
                    
                    break;

                case DRAW_SERVER_COMMAND:
                    ProtocolObject drawServerObj = readDrawServerCommandData(utils);
                    if (drawServerObj instanceof ProtocolCommand){
                        return drawServerObj;
                    }
                    else return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(DRAW_SERVER_COMMAND));


                case ERROR_COMMAND:
                    
                    // hem de llegir el separador, en cas contrari es notifica un
                    // error de sintaxi
                    separator = utils.readSeparator();

                    if (separator != SEPARATOR_CHAR){
                       return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(ERROR_COMMAND));
                    }
                    
                    // obtenim la longitud del missatge (sempre són dos caràcters)
                    String lenString = utils.read_string_of_length(2);
                    
                    if (isNumeric(lenString)){
                    
                        int len = Integer.parseInt(lenString);

                        String message = utils.read_string_of_length(len);
                        commandParts.add(len + message);
                    }
                    
                    break;

                case SHOWNDOWN_COMMAND:
                    
                    // han d'aparèixer 5 cartes, amb separadors al davant i un 
                    // format de carta vàlid (rang i pal)
                    for (int i = 0; i < Hand.NUM_CARDS; i++){
                        
                        // hem de llegir el separador, en cas contrari es notifica un
                        // error de sintaxi
                        separator = utils.readSeparator();

                        if (separator != SEPARATOR_CHAR){
                           return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(SHOWNDOWN_COMMAND));
                        }
                        
                        Object cardReadObj = readCard(utils);
                        
                        if (cardReadObj instanceof ProtocolError){
                            return (ProtocolError)cardReadObj;
                        }
                        
                        else if (cardReadObj instanceof String){
                            
                             String cardString = (String)cardReadObj;
                             
                             commandParts.add(cardString);
                             
                        }
                    }
                    
                    if (commandParts.size() != 6) { 
                        return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(SHOWNDOWN_COMMAND));
                    }

                    break;

                    
                // la comanda rebuda és desconeguda, ho notifiquem per error
                default:
                    return new ProtocolError("Unknown Command!");
            }
            
            // retornarem una objecte comanda vàlida que encapsula la informació
            // llegida
            ProtocolCommand command = new ProtocolCommand(commandParts);
            
            return command;
  

        } catch (SocketException ex) { 
            return null;
            
        } /*catch (IOException ex) {
            
            return new ProtocolError("A IOException took place");
        }   */  
    }

    protected Object readCard(ComUtilsBase utils) throws IOException {
        
        char firstRankChar = utils.readSeparator();
        String rankString = new String();
        
        String invalidCardMessage = "A valid card was expected but received an invalid one.";

        if (firstRankChar == '1') {

            char secondRankChar = utils.readSeparator();

             if (secondRankChar == '0') {
                 rankString = "10";
             } else {
                 return new ProtocolError(invalidCardMessage);
             }

        } 

        else {
            rankString += firstRankChar;
        }
        
        rankString = rankString.toUpperCase();

        if (!CardRank.obtainPossibleCardRankStrings().contains(rankString)) {
            return new ProtocolError(invalidCardMessage);

        }

        char suitChar = Character.toUpperCase(utils.readSeparator());

        if (!CardSuit.obtainPossibleCardSuitValues().contains(suitChar)) {
            return new ProtocolError(invalidCardMessage);
        }

        String cardString = rankString + suitChar;
        
        return cardString;
    }

    
    /**
     * Llegeix les dades d'una comanda DRAW_SERVER
     * @param utils
     * @return 
     */
    public ProtocolObject readDrawServerCommandData(ComUtilsBase utils) throws IOException {  
        // implementat en la subclasse del controlador del client
        return null;
    }
    
    
    

    
}

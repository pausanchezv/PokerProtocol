/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import java.util.ArrayList;
import poker.Card;
import poker.Poker;
import static poker.Poker.HOST_INDEX;
import static poker.Poker.NON_HOST_INDEX;
import poker_state_machine.BetState;
import poker_state_machine.DrawState;
import poker_state_machine.PassState;
import poker_state_machine.PreDrawState;
import poker_state_machine.PreparationState;
import protocol.ProtocolCommand;
import protocol.ProtocolObject;
import utils.ComUtilsBase;
import utils.ComUtilsSocketChannel;


/**
 * Controlador de protocol pel servidor
 * 
 * @author psanchva9.alumnes
 */
public class ServerProtocolController extends ProtocolController {
    
    // instància del poker de la partida
    protected Poker poker;

    
    
    /**
     * Constructor
     */
    public ServerProtocolController() {
    
        super();
    }
    
    /**
     * Inicialitza el controlador del protocol
     * @param isArtificialIntelligenceActive
     * @return 
     */
    public ArrayList<String> start(int[] stakes, boolean isArtificialIntelligenceActive) {
        
        // crea el joc si no exiteix
        if (poker == null) {
            poker = new Poker(stakes, isArtificialIntelligenceActive);
        }
        
        // inicialitza la partida
        return poker.start();
    }
    
    /**
     * Processa la comanda rebuda i retorna Strings representatius de les comandes 
     * de resposta
     * @param command
     * @return 
     */
    public ArrayList<String> processCommand(String command) {

        return (poker!= null && poker.getCurrentState() != null) ? poker.getCurrentState().processCommand(command, poker) : null;    
    }
    
    /**
     * Es llegeix una comanda i si escau es processen les dades sobre la lògica
     * accessible des del servidor
     * @param utils
     * @return
     * @throws IOException 
     */
    @Override
     public ProtocolObject readCommand(ComUtilsBase utils) throws IOException{
         
         ProtocolObject protocolObj = super.readCommand(utils);
         
         if (protocolObj != null){
             
             
            // si l'objecte de protocol llegit és una comanda, se'n processen les dades,
            // marcant que ens arriben del client (la comanda s'ha llegit pel socket)
            if (protocolObj instanceof ProtocolCommand){

                processGameData((ProtocolCommand)protocolObj, false);
            } 
         }
         
         return protocolObj;
     }
    
    
    /**
     * Processa dades de joc a partir d'una comanda
     * @param command 
     */
    void processGameData(ProtocolCommand command, boolean isServerCommand){
        
        // obtenim l'índex del jugador que ha enviat/es disposa a enviar la comanda
        int playerIndex = isServerCommand ? HOST_INDEX : NON_HOST_INDEX;

        String commandType = (String)command.getCommandParts().get(0);

        if (commandType != null){

            switch (commandType){

                case BET_COMMAND:
                    
                    if (poker.getCurrentState() instanceof PreparationState || poker.getCurrentState() instanceof DrawState || poker.getCurrentState() instanceof PassState){

                        int bet = (Integer)command.getCommandParts().get(1);

                        // anotem la quantitat de l'aposta i qui l'ha fet
                        poker.computeBet(bet, playerIndex);
                    }
                    break;
                    
                case PASS_COMMAND:
                    
                    if (poker.getCurrentState() instanceof BetState || poker.getCurrentState() instanceof PreparationState || poker.getCurrentState() instanceof DrawState || poker.getCurrentState() instanceof PassState){
                    
                        // computa la passada de torn
                        poker.computePass(playerIndex);
                    }
                    break;

                case FOLD_COMMAND:
                    
                    if (poker.getCurrentState() instanceof BetState){

                       poker.computeFold(playerIndex);
                    }
                       break;
                    

                    
                case CALL_COMMAND:

                    if (poker.getCurrentState() instanceof BetState){
                    
                    poker.computeCall(playerIndex);
                    }
                    break;
                    
                case RAISE_COMMAND:
                    
                    if (poker.getCurrentState() instanceof BetState){
                    
                    int raise = (Integer)command.getCommandParts().get(1);

                    poker.computeRaise(raise, playerIndex);
                    }
                    break;
                    
                    
                case DRAW_COMMAND:
                    
                    if (poker.getCurrentState() instanceof PreDrawState){
                    
                        int swapCardNum = (Integer)command.getCommandParts().get(1);

                        ArrayList<Card> swapCards = new ArrayList();

                        for (int i = 0; i < swapCardNum; i++){  
                            String cardString = (String)command.getCommandParts().get(2 + i);
                            swapCards.add(new Card(cardString.substring(0, cardString.length()-1), cardString.substring(cardString.length()-1, cardString.length()).charAt(0)));
                        }

                        poker.removePlayerCards(swapCards, NON_HOST_INDEX);
                    }
                    
                    break;

                default: break;
            }
        }
    }

    /**
     * Escriu pel socket les dades d'una comanda perquè les rebi al client,
     * no sense abans processar les dades necessàries sobre la lògica del servidor,
     * marcant que la comanda a enviar l'envia el servidor
     * @param command
     * @param utils 
     * @return  
     */
    public boolean writeCommand(ProtocolCommand command, ComUtilsSocketChannel utils) {
        
        //processGameData(command, true);
        
        return utils.writeProtocolCommand(command);
    }
    
    
    /**
     * Retorna si s'ha d'acabar a la força la partida amb el client
     * @return 
     */
    public boolean isForcedToQuit(){

        if (poker == null) return false;
        
        return poker.isForcedToQuit();
    }

    /**
     * Retorna els stakes del joc de Pòker
     * @return 
     */
    public int[] getStakes() {
        
        if (poker == null) return null;
        
        return poker.getStakes();
    }
     
}

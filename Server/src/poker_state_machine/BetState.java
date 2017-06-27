/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker_state_machine;

import java.util.ArrayList;
import static poker.ArtificialIntelligencePlayer.CALL_CODE;
import static poker.ArtificialIntelligencePlayer.FOLD_CODE;
import poker.Poker;
import controller.ProtocolController;
import static controller.ProtocolController.CALL_COMMAND;
import static controller.ProtocolController.FOLD_COMMAND;
import static controller.ProtocolController.RAISE_COMMAND;
import static controller.ProtocolController.SEPARATOR;

/**
 * Estat d'apostar del Pòker
 * @author aespinro11.alumnes
 */
public class BetState extends PokerState {
    
    
    /**
     * Constructor
     */
    public BetState() {

        super();
        
        commandsAndOutStatesMap.put(FOLD_COMMAND, EndState.class);
    }
    

    @Override
    public ArrayList<String> doAction(Poker pokerContext) {
        
        pokerContext.setCurrentState(this);

        // la comanda CALL porta a l'estat d'entre rondes si la ronda que d'apostes
        // actual és la primera, i al final del joc si ja és la segona
        if (pokerContext.getRound() == 1){
           commandsAndOutStatesMap.put(CALL_COMMAND, PreDrawState.class);
        }
        else if (pokerContext.getRound() == 2){
            commandsAndOutStatesMap.put(CALL_COMMAND, EndState.class);
        }
        
        // si és el torn de l'anfitrió aquest reacciona a l'aposta de l'altre jugador automàticament
        if (pokerContext.isHostTurn()) {
            
            ArrayList<String> commands = new ArrayList();
            
            // obtenim la comanda que reacciona a la darrera aposta realitzada
            String betResponseCommand = obtainBetResponseCommand(pokerContext);
            commands.add(betResponseCommand);
            
            // processem la comanda perquè es determini si cal canviar d'estat,
            // i en cas afirmatiu això es faci
            commands.addAll(processCommand(betResponseCommand, pokerContext));
            
            return commands;
        }
        
        return new ArrayList();
    }
    
    
    
    /**
     * Versió sobrescrita del processat d'una comanda
     * @param command
     * @param context
     * @return 
     */
    public ArrayList<String> processCommand(String command, Poker context) {
        
        ArrayList<String> commands = super.processCommand(command, context);
        
        String commandType = command.substring(0, ProtocolController.COMMAND_LENGTH);

        // si es rep un RAISE i és torn de l'anfitrió, cal respondre amb FOLD, 
        // CALL o RAISE
        if (context.isHostTurn() && commandType.equals(ProtocolController.RAISE_COMMAND)){
            
            // obtenim la comanda que reacciona a la darrera aposta realitzada
            String betResponseCommand = obtainBetResponseCommand(context);
            commands.add(betResponseCommand);
            
            // fem que la comanda tingui efecte sobre el joc processant-la
            commands.addAll(processCommand(betResponseCommand, context));
        }
        
        return commands;
    }

    
    /**
     * S'obté i retorna una comanda en forma d'String que representa la
     * resposta o reacció a la darrera aposta realitzada
     * @param pokerContext
     * @return 
     */
    private String obtainBetResponseCommand(Poker pokerContext) {
        
        // la lògica interpreta l'aposta anterior i torna un nombre que serà
        // un codi per fer CALL o FOLD o la quantitat que vol augmentar amb RAISE
        int betResponse = pokerContext.computeBetResponseFromHost();

        String betResponseCommand;

        switch (betResponse){

            case FOLD_CODE:
                 betResponseCommand = FOLD_COMMAND;
                 break;

            case CALL_CODE:
                 betResponseCommand = CALL_COMMAND;
                 break;

            default:
                betResponseCommand = RAISE_COMMAND + SEPARATOR + betResponse;
                break;
        }   
        
        return betResponseCommand;
    }
}

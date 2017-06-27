/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker_state_machine;

import java.util.ArrayList;
import poker.Poker;
import static controller.ProtocolController.ANTE_OK_COMMAND;
import static controller.ProtocolController.QUIT_COMMAND;
import static controller.ProtocolController.SEPARATOR;
import static controller.ProtocolController.SHOWNDOWN_COMMAND;
import static controller.ProtocolController.STAKES_COMMAND;

/**
 * Estat de fi de partida
 * @author psanchva9.alumnes
 */
public class EndState extends PokerState {
    
     /**
     * Constructor
     */
    public EndState() {

        super();
        
        commandsAndOutStatesMap.put(ANTE_OK_COMMAND, PreparationState.class);
        commandsAndOutStatesMap.put(QUIT_COMMAND, QuitState.class);
    }
    

    @Override
    public ArrayList<String> doAction(Poker pokerContext) {
        
        pokerContext.setCurrentState(this);
        
                
        ArrayList<String> commands = new ArrayList();
        
        // es computa la fi de la partida
        boolean showShowdown = pokerContext.computeEnd();
        
        // si cal, es mostra la mà de l'anfitrió amb SHOWDOWN
        if (showShowdown){
            
            String showndownCommand = SHOWNDOWN_COMMAND + SEPARATOR + pokerContext.getHostHandString();
            commands.add(showndownCommand);
        }
        
        
        String stakesCommand = STAKES_COMMAND;
        
        for (int i = 0; i < pokerContext.getStakes().length; i++) {
            // afegim els stakes de cada jugador (mai hauria de passar, però garantim
            // que enviarem un enter positiu en cas que la quantitat de la lògica no
            // ho fos, per seguir el protocol)
            stakesCommand += SEPARATOR + Math.max(1, pokerContext.getStakes()[i]);
        }

        commands.add(stakesCommand);
        
        return commands;
    }
    
    
    
    
}

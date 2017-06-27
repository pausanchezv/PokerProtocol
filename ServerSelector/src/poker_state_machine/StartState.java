/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker_state_machine;

import java.util.ArrayList;
import poker.Poker;
import static controller.ProtocolController.ANTE_COMMAND;
import static controller.ProtocolController.ANTE_OK_COMMAND;
import static controller.ProtocolController.QUIT_COMMAND;
import static controller.ProtocolController.SEPARATOR;
import static controller.ProtocolController.STAKES_COMMAND;

/**
 * Estat d'inici del Pòker
 * @author aespinro11.alumnes
 */
public class StartState extends PokerState {

    
    public StartState() {

        super();
        commandsAndOutStatesMap.put(ANTE_OK_COMMAND, PreparationState.class);
        commandsAndOutStatesMap.put(QUIT_COMMAND, QuitState.class);
    }
    
   
    

    @Override
    public ArrayList<String> doAction(Poker pokerContext) {
        
        pokerContext.setCurrentState(this);
        
        // l'estat inicial comença generant les comandes ANTE i STAKES
        
        String anteCommand = ANTE_COMMAND + SEPARATOR + String.valueOf(pokerContext.getAnte());
        
        String stakesCommand = STAKES_COMMAND;
        
        for (int i = 0; i < pokerContext.getStakes().length; i++) {
            // afegim els stakes de cada jugador (mai hauria de passar, però garantim
            // que enviarem un enter positiu en cas que la quantitat de la lògica no
            // ho fos, per seguir el protocol)
            stakesCommand += SEPARATOR + Math.max(1, pokerContext.getStakes()[i]);
        }
        
        ArrayList<String> commands = new ArrayList();
        commands.add(anteCommand);
        commands.add(stakesCommand);
        
        return commands;
         
    }
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker_state_machine;

import java.util.ArrayList;
import poker.Poker;
import static controller.ProtocolController.DRAW_COMMAND;

/**
 * Estat d'espera per començar el draw
 * @author pausanchezv
 */
public class PreDrawState extends PokerState {
    
    
    /**
     * Constructor
     */
    public PreDrawState() {

        super();
        
        commandsAndOutStatesMap.put(DRAW_COMMAND, DrawState.class);
    }

    @Override
    public ArrayList<String> doAction(Poker pokerContext) {
        
        pokerContext.setCurrentState(this);
        
        return new ArrayList<String>();
    }
    
}

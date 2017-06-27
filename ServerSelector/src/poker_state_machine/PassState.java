/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker_state_machine;

import java.util.ArrayList;
import poker.Poker;
import static poker.Poker.HOST_INDEX;
import static controller.ProtocolController.BET_COMMAND;
import static controller.ProtocolController.PASS_COMMAND;
import static controller.ProtocolController.SEPARATOR;

/**
 * Estat de passar de torn d'aposta
 * @author aespinro11.alumnes
 */
public class PassState extends PokerState {
    
    
    /**
     * Constructor
     */
    public PassState() {

        super();
        
        commandsAndOutStatesMap.put(BET_COMMAND, BetState.class);
    }
    

    @Override
    public ArrayList<String> doAction(Poker pokerContext) {
        
        pokerContext.setCurrentState(this);
        
        // la comanda PASS (venint d'un PASS previ) porta a l'estat de draw 
        // si encara no s'ha fet cap aposta, i al final del joc si ja ha transcorregut
        // tota una ronda
        if (pokerContext.getRound() == 1){
           commandsAndOutStatesMap.put(PASS_COMMAND, PreDrawState.class);
        }
        else if (pokerContext.getRound() == 2){
            commandsAndOutStatesMap.put(PASS_COMMAND, EndState.class);
        }
        
        ArrayList<String> commands = new ArrayList();
        
         // si és el torn de l'anfitrió aposta/passa automàticament
        if (pokerContext.isHostTurn()) {
            
            // si convé passar de torn, es passa
            if (pokerContext.shouldHostPass()){
                
                String passCommand = PASS_COMMAND;
                commands.add(passCommand);
                
                pokerContext.computePass(HOST_INDEX);

                commands.addAll(processCommand(passCommand, pokerContext));
            }
            
            // altrament, es determina la quantitat a apostar, i s'aposta
            else{
                
                int bet = pokerContext.obtainQuantityToBet();
                
                String betCommand = BET_COMMAND + SEPARATOR + bet;
                commands.add(betCommand);
                
                pokerContext.computeBet(bet, HOST_INDEX);

                BetState betState = new BetState();
                commands.addAll(betState.doAction(pokerContext));
            }
        }

        return commands;
    }

}

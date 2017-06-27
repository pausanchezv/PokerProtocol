/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker_state_machine;

import java.util.ArrayList;
import poker.Poker;


/**
 * Estat de marxar del joc
 * @author aespinro11.alumnes
 */
class QuitState extends PokerState {
    
    
    /**
     * Constructor
     */
    public QuitState() {

       super();
   }



   @Override
   public ArrayList<String> doAction(Poker pokerContext) {

       pokerContext.setCurrentState(this);

       // es computa la marxa del jugador no anfitrió
       pokerContext.computeNonHostExit();
       
       return new ArrayList();
   }
}

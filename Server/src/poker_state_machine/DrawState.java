/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker_state_machine;

import java.util.ArrayList;
import poker.Card;
import poker.Poker;
import static poker.Poker.HOST_INDEX;
import static poker.Poker.NON_HOST_INDEX;
import static controller.ProtocolController.BET_COMMAND;
import static controller.ProtocolController.DRAW_SERVER_COMMAND;
import static controller.ProtocolController.PASS_COMMAND;
import static controller.ProtocolController.SEPARATOR;

/**
 * Estat de draw
 * @author a
 */
public class DrawState extends PokerState {
    
    /**
     * Constructor
     */
    public DrawState() {

        super();
        
        commandsAndOutStatesMap.put(PASS_COMMAND, PassState.class);
        commandsAndOutStatesMap.put(BET_COMMAND, BetState.class);
    }

    /**
     * Versió sobrescrita de fer l'acció pròpia de l'estat
     * @param pokerContext
     * @return 
     */
    @Override
    public ArrayList<String> doAction(Poker pokerContext) {
        
        pokerContext.setCurrentState(this);
        
        ArrayList<String> commands = new ArrayList();

        // s'obtenen les cartes a enviar al jugador no anfitrió
        ArrayList<Card> newCards = pokerContext.giveNewCardsToPlayer(NON_HOST_INDEX);
        
        // preparem la primera part de la comanda, que inclou el codi i les cartes
        // a donar al no anfitrió
        String drawServerCommand = DRAW_SERVER_COMMAND;
        
        String cardsString = "";
        
        if (newCards != null && !newCards.isEmpty()){
        for (Card newCard : newCards){
            cardsString += newCard.toString() + SEPARATOR;
        }
        cardsString = cardsString.trim();
        drawServerCommand += SEPARATOR + cardsString;
        }


        // el jugador anfitrió gestiona el seu propi canvi de cartes, i indica quantes
        // ha canviat
        int hostSwapCardNun = pokerContext.swapHostCards();
        
        // s'afegeix el nombre de cartes canviades per l'anfitrió a la comanda, perquè
        // l'altre jugador ho pugui saber
        drawServerCommand += SEPARATOR + hostSwapCardNun;
        
        commands.add(drawServerCommand);
        
        pokerContext.prepareForSecondRound();
        
        // si és el torn de l'anfitrió aposta/passa automàticament
        if (pokerContext.isHostTurn()) {
            
            // si convé passar de torn, es passa
            if (pokerContext.shouldHostPass()){
                
                String passCommand = PASS_COMMAND;
                commands.add(passCommand);
                
                pokerContext.computePass(HOST_INDEX);

                PassState passState = new PassState();
                commands.addAll(passState.doAction(pokerContext));
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

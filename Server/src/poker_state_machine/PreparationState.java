/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker_state_machine;


import java.util.ArrayList;
import poker.Poker;
import static poker.Poker.HOST_INDEX;
import static controller.ProtocolController.*;
import protocol.ProtocolError;

/**
 * Estat de preparació abans de començar a apostar
 * @author aespinro11.alumnes
 */
public class PreparationState extends PokerState {
    
    
    /**
     * Constructor
     */
    public PreparationState() {

        super();
        
        commandsAndOutStatesMap.put(BET_COMMAND, BetState.class);
        commandsAndOutStatesMap.put(PASS_COMMAND, PassState.class);
    }

    @Override
    public ArrayList<String> doAction(Poker pokerContext) {
        
        pokerContext.setCurrentState(this);
        
        // en aquest punt la partida ha estat acceptada així que ja es pot descomptar
        // l'aposta inicial
        pokerContext.computeAnte();
        
        String dealerCommand = DEALER_COMMAND + SEPARATOR + pokerContext.setUpDealer();
        pokerContext.setUpCards();
        String handCommand = HAND_COMMAND + SEPARATOR + pokerContext.getNonHostHandString();
        
        ArrayList<String> commands = new ArrayList();
        commands.add(dealerCommand);
        commands.add(handCommand);
        
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
    
    
    /**
     * Donada una comanda avalua si es pot anar a un altre estat des de l'actual i si escau, es fa
     * 
     * @param command 
     * @param context 
     * @return  
     */
    @Override
    public ArrayList<String> processCommand(String command, Poker context) {
        
        // només permetem fer accions al jugador no anfitrió si no és el dealer
        if (context.isHostTheDealer()){
           return super.processCommand(command, context);
        }
       
        // si no es pot enviar una comanda perquè no és el torn de l'emissor s'indica
        String errorCommand = new ProtocolError("The sent comand is not allowed because it's not the turn of the sender.").obtainComand();
        ArrayList<String> errorCommands = new ArrayList();
        errorCommands.add(errorCommand);
        return errorCommands;

    }
    
    
    
}

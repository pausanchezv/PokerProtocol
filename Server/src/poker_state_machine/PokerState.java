/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker_state_machine;

import java.util.ArrayList;
import java.util.HashMap;
import poker.Poker;
import controller.ProtocolController;
import static controller.ProtocolController.ERROR_COMMAND;

/**
 * Classe abstracta mare dels estats del Pòker
 * @author aespinro11.alumnes
 */
public abstract class PokerState {
    
    
    // taula hash que vuncula una comanda a la classe de l'estat al qual porta
    // (des de l'estat actual)
    protected HashMap<String, Class> commandsAndOutStatesMap; 

    
    /**
     * Constructor
     */
    public PokerState() {
        this.commandsAndOutStatesMap = new HashMap();
        commandsAndOutStatesMap.put(ERROR_COMMAND, this.getClass());
    }
 
    /**
     * Canvia l'estat del context a aquest estat i realitza les tasques pròpies
     * de l'estat
     * @param pokerContext
     * @return 
     */
    public abstract ArrayList<String> doAction(Poker pokerContext);
    
    /**
     * Donada una comanda avalua si es pot anar a un altre estat des de l'actual i si escau, es fa
     * 
     * @param command 
     * @param context 
     * @return  
     */
    public ArrayList<String> processCommand(String command, Poker context) {
        
        ArrayList<String> commands = new ArrayList();
        
        String commandType = command.substring(0, ProtocolController.COMMAND_LENGTH);
        
        if (commandsAndOutStatesMap.containsKey(commandType)) {
            
            try {
                PokerState newState = (PokerState) Class.forName(commandsAndOutStatesMap.get(commandType).getName()).newInstance();
                commands = newState.doAction(context);
            
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
            
        }
        
        return commands;
    }

    /**
     * Mètode toString
     * @return toString
     */
    @Override
    public String toString(){
        return this.getClass().getSimpleName();
    }
    
}

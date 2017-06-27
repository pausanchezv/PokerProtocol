/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static controller.ProtocolController.DEALER_COMMAND;
import static controller.ProtocolController.DRAW_COMMAND;
import static controller.ProtocolController.DRAW_SERVER_COMMAND;
import static controller.ProtocolController.SEPARATOR;
import static utils.ComUtilsBase.isNumeric;

/**
 * Objecte que encapsula una comanda del protocol, i que es descriu com una llista
 * d'objectes que representen les seves diferents parts separades per espais, ja siguin
 * Strings o Integers, segons la comanda
 * @author a
 */
public final class ProtocolCommand extends ProtocolObject {
    
    
    // parts de la comanda, que en enviar-se se separen per espais
    private ArrayList<Object> commandParts;

    /**
     * Retorna les parts de la comanda
     * @return 
     */
    public ArrayList<Object> getCommandParts() {
        return commandParts;
    }

    
    /**
     * Constructor buit
     */
    public ProtocolCommand() {
        this.commandParts = new ArrayList();
    }
    

    /**
     * Constructor amb les parts
     * @param commandParts
    */
    public ProtocolCommand(ArrayList<Object> commandParts) {
        
        this.commandParts = new ArrayList();

        // afegim les parts només si compleixen certs requisits
        for (Object part : commandParts){
            
            if (part != null){
                
                boolean success = addCommandPart(part);
                
                if (!success) break;
            }
        }
    }
    
    
    /**
     * Afegeix una part a la llista de parts de la comanda, assegurant-se que sigui
     * un String o un enter; la primera part de la comanda ha de ser un String que 
     * identifiqui el tipus, i ha de pertànyer als tipus possibles del protocol;
     * retorna si hi ha hagut èxit
     * @param commandPart 
     * @return  
     */
    public boolean addCommandPart(Object commandPart){
        
        // la part ha de ser un String o un enter
        if (commandPart instanceof String || commandPart instanceof Integer){
            
            // la primera part de la comanda ha de ser un String que identifiqui el 
            // tipus, i ha de pertànyer als tipus possibles del protocol
            if (true){//!commandParts.isEmpty() || (commandPart instanceof String && ProtocolController.isStringAPossibleCommand((String)commandPart))){
            
                commandParts.add(commandPart);
                return true;
            }
        }  
        
        return false;
    }
    
    
    
    /**
     * Donat un String, crea una comanda a partir d'ell, o retorna null si troba
     * alguna anomalia
     * @param string
     * @return 
     */
    public static ProtocolCommand obtainProtocolCommandFromString(String string){
        
        List<String> stringParts = Arrays.asList(string.toUpperCase().trim().split(SEPARATOR));
        
        ProtocolCommand command = new ProtocolCommand();
        
        String commandType = stringParts.get(0);
        
        for (String stringPart : stringParts){
            
            Object part;
            
            if (!stringPart.equals("")){
            
                // converteix una part a entera si escau (si és una part estrictament
                // numèrica a menys que la comanda sigui DEAL, DRAW o DRAW_SERVER,
                // ja que en tal cas es vol com a caràcter)
                if (isNumeric(stringPart) && !commandType.equals(DEALER_COMMAND) && !commandType.equals(DRAW_COMMAND) && !commandType.equals(DRAW_SERVER_COMMAND)){
                    part = Integer.parseInt(stringPart);
                }

                // altrament la manté com a String
                else{
                    part = stringPart;
                }

                // cal que no es trobi cap irregularitat, o es cancel·larà l'operació
                // i es retornarà null
                boolean success = command.addCommandPart(part);

                if (!success){
                    return null;
                }
            }
            
            else return null;
        }
        
        return command;
    }

    @Override
    public String toString() {
        
        String str = "";
        
        for (Object part : commandParts){
            str += part.toString() + " ";
        }
        
        return str.trim();
    }
    
}

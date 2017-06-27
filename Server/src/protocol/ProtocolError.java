/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocol;

import controller.ProtocolController;

/**
 * Objecte que encapsula un error del protocol
 * @author psanchva9.alumnes
 */
public class ProtocolError extends ProtocolObject {
    
    
    // missatge d'error
    private String message;
    
    /**
     * Constructor: rep i assigna el missatge d'error
     * 
     * @param message 
     */
    public ProtocolError(String message) {
        this.message = message;
    }
    
    /**
     * Retorna el missatge d'error
     * 
     * @return 
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Modifica el missatge d'error
     * 
     * @param message 
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Retorna una comanda d'error amb el seu missatge
     * 
     * @return 
     */
    public String obtainComand() {
       
        int length = message.length();
        return ProtocolController.ERROR_COMMAND + ProtocolController.SEPARATOR + length + message;
    }
    
}

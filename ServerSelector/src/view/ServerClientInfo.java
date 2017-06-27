/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import poker.Poker;

/**
 * Informació que el servidor guarda d'un client i que persisteix si marxa i torna
 * a connectar-se per jugar posteriorment
 * @author a
 */
public class ServerClientInfo {
    
    // socket channel de connexió amb el client
    private SocketChannel socket;
    
    // clau de selecció
    private SelectionKey selectionKey;


    // stakes del servidor i el client (s'actualitzen quan acaba una partida)
    private int[] stakes;

    /**
     * Retorna el socket
     * @return 
     */
    public SocketChannel getSocket() {
        return socket;
    }
    
        
    /**
     * Modifica el socket
     * @param socket 
     */
    public void setSocket(SocketChannel socket) {
        this.socket = socket;
    }
    
    /**
     * Retorna la clau de selecció
     * @return 
     */
    public SelectionKey getSelectionKey() {
        return selectionKey;
    }
    
    /**
     * Modifica la clau de selecció
     * @param object 
     */
    public void setSelectionKey(SelectionKey object) {
        this.selectionKey = selectionKey;
    }

    
    
    /**
     * Retorna els stakes de cada jugador
     * @return 
     */
    public int[] getStakes() {
        return stakes;
    }

    /**
     * Modifica els stakes de servidor i client
     * @param stakes 
     */
    public void setStakes(int[] stakes) {
        this.stakes = stakes;
    }
    

    
    /**
     * Retorna si el client està jugant actualment
     * @return 
     */
    public boolean isPlaying(){
        // un jugador està a una partida si el socket no és nul
        return socket != null;
    }
    

    /**
     * Constructor
     * @param socket 
     * @param selectionKey 
     */
    public ServerClientInfo(SocketChannel socket, SelectionKey selectionKey) {
        
        this.socket = socket;
        this.selectionKey = selectionKey;
        stakes = new int[] {Poker.DEFAULT_STAKES, Poker.DEFAULT_STAKES};
    }



   
    
    
    
}

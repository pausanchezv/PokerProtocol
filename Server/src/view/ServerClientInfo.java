/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.net.Socket;
import poker.Poker;

/**
 * Informació que el servidor guarda d'un client i que persisteix si marxa i torna
 * a connectar-se per jugar posteriorment
 * @author a
 */
public class ServerClientInfo {
    
    // socket de connexió amb el client
    private Socket socket;

    // stakes del servidor i el client (s'actualitzen quan acaba una partida)
    private int[] stakes;

    /**
     * Retorna el socket
     * @return 
     */
    public Socket getSocket() {
        return socket;
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
     * Modifica el socket
     * @param socket 
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
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
     */
    public ServerClientInfo(Socket socket) {
        
        this.socket = socket;
        stakes = new int[] {Poker.DEFAULT_STAKES, Poker.DEFAULT_STAKES};
    }

   
    
    
    
}

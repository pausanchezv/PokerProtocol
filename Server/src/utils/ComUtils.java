/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;
 
import java.io.*;
import java.net.*;
 
  
/**
 *
 * @author pausanchezv
 */
public class ComUtils extends ComUtilsBase {



    
    // socket de ComUtils
    private final Socket socket;
      
    //Objectes per llegir i escriure dades
    private DataInputStream dis;
    private DataOutputStream dos;
      
    /**
     * Constructor per a sockets
     * 
     * @param socket
     * @throws IOException 
     */
    public ComUtils(Socket socket) throws IOException {
        this.socket = socket;
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
    }
      
    /**
     * Constructor per a fitxers
     * 
     * @param file
     * @throws IOException 
     */
    public ComUtils(File file) throws IOException{
        this.socket = null;
        dis = new DataInputStream(new FileInputStream(file));
        dos = new DataOutputStream(new FileOutputStream(file));
    }
    
    
    /**
     * Retorna el socket
     * @return 
     */
    public Socket getSocket() {
        return socket;
    }

      
    /**
     * Escriure un enter de 32 bits
     * 
     * @param number
     * @throws IOException 
     */
    @Override
    public void write_int32(int number) throws IOException {
          
        byte [] bytes = new byte[4];
          
        int32ToBytes(number, bytes, "be");
        dos.write(bytes, 0, 4);
    }
    

    
    /**
     * Escriu un String ocupant única i exclusivament el nombre de bytes mínim delimitat
     * per la longitud del propi String
     * @param str
     * @throws IOException 
     */
    @Override
    public void write_string_of_its_length(String str) throws IOException {
    
        int lenStr = str.length();
        
        byte [] bStr = new byte[lenStr];
          
        for (int i = 0; i < lenStr; i++) {
            bStr[i] = (byte) str.charAt(i);
        }
          
        dos.write(bStr, 0, lenStr);
        
    }
      
    /**
     * Escriure un string
     * 
     * @param str
     * @throws IOException 
     */
    @Override
    public void write_string(String str) throws IOException {
          
        int numBytes, lenStr;
        byte [] bStr = new byte[STRSIZE];
          
        lenStr = str.length();
          
        if (lenStr > STRSIZE) {
            numBytes = STRSIZE;
        } else {
            numBytes = lenStr;
        }
          
        for (int i = 0; i < numBytes; i++) {
            bStr[i] = (byte) str.charAt(i);
        }
          
        for (int i = numBytes; i < STRSIZE; i++) {
            bStr[i] = (byte) ' ';
        }
          
        dos.write(bStr, 0, STRSIZE);
    }
      
      

      
    /**
     * Llegir bytes
     * 
     * @param numBytes
     * @return
     * @throws IOException 
     */
    @Override
    protected byte [] read_bytes(int numBytes) throws IOException {
          
        int len = 0;
        byte [] bStr = new byte[numBytes];
          
        int bytesread = 0;
          
        do {
            bytesread = dis.read(bStr, len, numBytes - len);
              
            if (bytesread == -1) {
                throw new IOException("Broken Pipe");
            }
            len += bytesread;
              
        } while (len < numBytes);
                 
        return bStr;
    }
      
}
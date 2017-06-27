/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.IOException;
import java.util.Random;
import protocol.ProtocolCommand;
import static controller.ProtocolController.SEPARATOR_CHAR;
import static controller.ProtocolController.STAKES_COMMAND;
import protocol.ProtocolError;

/**
 * Base abstracta de ComUtils que imposa implementar mètodes a les subclasses
 * @author psanchva9.alumnes
 */
public abstract class ComUtilsBase {
 
    
          
    // mida per defecte d'puna cadena de caràcters
    public final int STRSIZE = 99;
    
    // màxim nombre de vegades seguides a intentar llegir el separador
    private final int MAX_READ_SEPARATOR_ITERATION_NUM = 10000;
      
    /**
     * Llegir un enter de 32 bits
     * 
     * @return
     * @throws IOException 
     */
    public int read_int32() throws IOException {
          
        byte [] bytes = new byte[4];
        bytes = read_bytes(4);
          
        return bytesToInt32(bytes, "be");
    }
    
    /**
     * Escriure un enter de 32 bits
     * @param number
     * @throws java.io.IOException
     */
    public abstract void write_int32(int number) throws IOException;
    
    
    /**
     * Converteix l'enter passat per paràmetre a bytes i retorna l'String que
     * representa aquests bytes com a caràcters
     * @param number
     * @return 
     */
    public String int32ToBytesString(int number){
        
        byte [] bytes = new byte[4];
          
        int32ToBytes(number, bytes, "be");
        
        int tempInt = bytesToInt32(bytes, "be");
        
        String str;
        char [] cStr = new char[4];
          
        for (int i = 0; i < 4; i++) {
            cStr[i] = (char) bytes[i];
        }
        
        str = String.valueOf(cStr);
          
        return str.trim();   
    }
    
    
    
        
    
    
    
    
    /**
     * Converteix l'String passat per paràmetre a bytes (un byte per caràcter) i 
     * transforma aquests bytes en un int32, que retorna
     * @param bytesString
     * @return 
     */
    public int bytesStringToInt32(String bytesString) {
        
        // primera part: conversió de l'String a bytes
        
        int numBytes, lenStr;
        byte [] bytes = new byte[4];
          
        lenStr = bytesString.length();
          
        if (lenStr > 4) {
            numBytes = 4;
        } else {
            numBytes = lenStr;
        }
          
        for (int i = 0; i < numBytes; i++) {
            bytes[i] = (byte) bytesString.charAt(i);
        }
          
        for (int i = numBytes; i < 4; i++) {
            bytes[i] = (byte) ' ';
        }
        
       
        // segona part: conversió dels bytes a int32
         return bytesToInt32(bytes, "be");
    }
    
    /**
     * Llegeix un string de la mida indicada
     * @param len
     * @return
     * @throws java.io.IOException
     */
    public String read_string_of_length(int len) throws IOException{
        
        if (len <= 0){
            return "";
        }
        
        
        String str;
        byte [] bStr = new byte[len];
        char [] cStr = new char[len];

        bStr = read_bytes(len);

        for (int i = 0; i < len; i++) {
            cStr[i] = (char) bStr[i];
        }
        str = String.valueOf(cStr);

        return str;

    }
   
      
    /**
     * Llegir un string de mida STRSIZE
     * 
     * @return
     * @throws IOException 
     */
    public String read_string() throws IOException {
          
        return read_string_of_length(STRSIZE);
    }
    
    /**
     * Escriu un String ocupant única i exclusivament el nombre de bytes mínim delimitat
     * per la longitud del propi String
     * @param str
     * @throws IOException 
     */
    public abstract void write_string_of_its_length(String str) throws IOException;
    
       
    /**
     * Escriure un string
     * 
     * @param str
     * @throws IOException 
     */
    public abstract void write_string(String str) throws IOException;
    
    
    
    
        /**
     * Passar d'enters a bytes
     * 
     * @param number
     * @param bytes
     * @param endianess
     * @return 
     */
    protected int int32ToBytes(int number, byte [] bytes, String endianess) {
          
        //En big endian els bytes van d'esquerra a dreta
        if("be".equals(endianess.toLowerCase())) {
            bytes[0] = (byte) ((number >> 24) & 0xFF);
            bytes[1] = (byte) ((number >> 16) & 0xFF);
            bytes[2] = (byte) ((number >> 8) & 0xFF);
            bytes[3] = (byte) (number & 0xFF);
        }
          
        //En little endian els bytes van de dreta a esquerra
        else {
            bytes[0] = (byte) (number & 0xFF);
            bytes[1] = (byte) ((number >> 8) & 0xFF);
            bytes[2] = (byte) ((number >> 16) & 0xFF);
            bytes[3] = (byte) ((number >> 24) & 0xFF);
        }
          
        return 4;
    }
      
    /**
     * Passar de bytes a enters
     * 
     * @param bytes
     * @param endianess
     * @return 
     */
    protected int bytesToInt32(byte [] bytes, String endianess) {
          
        int number;
          
        //Si és big endian els bytes van d'esquerra a dreta
        if("be".equals(endianess.toLowerCase())) {
            number=((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        }
          
        //Si és little endian els bytes van de dreta a esquerra
        else {
            number=(bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8) | ((bytes[2] & 0xFF) << 16) | ((bytes[3] & 0xFF) << 24);
        }
          
        return number;
    }
    
    
    /**
     * Llegir bytes
     * 
     * @param numBytes
     * @return
     * @throws IOException 
     */
    protected abstract byte [] read_bytes(int numBytes) throws IOException;
    

    /**
     * Llegir un string  mida variable size
     * 
     * @param size nombre de bytes especifica la longitud
     * @return
     * @throws IOException 
     */
    public String read_string_variable(int size) throws IOException {
          
        byte [] bHeader = new byte[size];
        char [] cHeader = new char[size];
        int numBytes = 0;
  
        // Llegim els bytes que indiquen la mida de l'string
        bHeader = read_bytes(size);
          
        // La mida de l'string ve en format text, per tant creem un string i el parsejem
        for(int i = 0; i < size; i++){
            cHeader[i] = (char) bHeader[i]; 
        }
          
        numBytes=Integer.parseInt(new String(cHeader));
  
        // Llegim l'string
        byte [] bStr = new byte[numBytes];
        char [] cStr = new char[numBytes];
          
        bStr = read_bytes(numBytes);
          
        for(int i=0; i<numBytes; i++) {
            cStr[i] = (char)bStr[i];
        }
          
        return String.valueOf(cStr);
    }
    
      
    /**
     * Comprova que un string estigui format només de dígits (d'una dimensió raonable,
     * compatible amb enters de 32 bits)
     * 
     * @param str
     * @return 
     */
    public static  boolean isNumeric(String str) {
        
        // descartem string buits i els massa grans
        if (str.equals("") || str.length() > 10) return false;
        
        for (char i : str.toCharArray()) {
            if (!Character.isDigit(i)) {
                return false;
            }
        }
        return true;
    }
   
    /**
     * Obté un nombre positiu a l'atzar, amb límit
     * @param bound
     * @return 
     */
    public static int obtainRandomPositiveInt(int bound) {
        // el màxim és per si obtenim un 0, que no és positiu
        return Math.max(1, Math.abs(new Random().nextInt(bound)));
    }
    
    public void writeChar(char c) throws IOException {
        String str = "" + c;
        write_string_of_its_length(str);
    }
      
    private char readChar() throws IOException {
        String str = read_string_of_length(1);
        return str.charAt(0);
    }
    
    public char readSeparator() throws IOException{
        
        char c = 0;
        int numIterations = 0;
        while (c == 0 && numIterations < MAX_READ_SEPARATOR_ITERATION_NUM){
            c = readChar();
            numIterations++;
            if (numIterations > 0.5 * MAX_READ_SEPARATOR_ITERATION_NUM){
                System.out.print(" ");
            }
        }
        
        return c;
    }
    

    public void writeProtocolError(ProtocolError protocolError) {
        
        try {
            write_string_of_its_length(protocolError.obtainComand());
            
        } 
        
        catch (IOException ex) {
            //Logger.getLogger(ComUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    /**
     * Escriu a l'Stream la comanda passada; per cadascuna de les seves parts, 
     * s'escriurà un String o un enter segons convingui, tot separant-les per espais;
     * retorna si hi hagut èxit o s'ha d'avortar la partida
     * @param command 
     * @return  
     */
    public boolean writeProtocolCommand(ProtocolCommand command) {
        
        try{
            
           
            // si anem a escriure stakes i algun dels dos nombres de fitxes no és
            // positiu incompliríem el protocol si l'escrivíssim: ho evitem mostrant
            // i enviant un error
            if (command != null && command.getCommandParts().size() == 3 && command.getCommandParts().get(0).equals(STAKES_COMMAND)){

                if ((Integer)command.getCommandParts().get(1) <= 0 || (Integer)command.getCommandParts().get(2) <= 0){
                    return false;
                }
            }


            for (int i = 0; i < command.getCommandParts().size(); i++){

                Object part = command.getCommandParts().get(i);

                // s'escriu cada part, com a enter o com a String

                if (part instanceof Integer){

                    try {
                        int intToWrite = (int)part;
                        // el protocol no admet nombres no positius; en el cas extrem
                        // que es volgués escriure un nombre no positiu, es transforma 
                        // en 1, per no incomplir el protocol
                        if (intToWrite <= 0) intToWrite = 1;
                        write_int32(intToWrite);

                    } catch (IOException ex) {}
                }

                else if (part instanceof String){

                    try {
                        write_string_of_its_length((String)part);

                    } catch (IOException ex) {}
                }

                // se separen les parts mitjançant el separador
                if (i < command.getCommandParts().size() - 1){

                    try {
                        writeChar(SEPARATOR_CHAR);

                    } catch (IOException ex) {}
                }
            }
            
            return true;
        
        } catch (Exception e){
            return false;
        }
    }   
    
}

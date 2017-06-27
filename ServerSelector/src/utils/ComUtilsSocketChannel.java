package utils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
 
  
/**
 * ComUtils per a comunicacions amb socket channel
 * @author pausanchezv
 */
public class ComUtilsSocketChannel extends ComUtilsBase {
      
    // buffer de bytes
    private ByteBuffer buffer;
    
    // socket de dades
    private final SocketChannel socket;
    
    /**
     * Constructor
     * @param socket 
     */
    public ComUtilsSocketChannel(SocketChannel socket) {
        
        this.socket = socket;
    }
    
      
    /**
     * Llegir un string
     * 
     * @return
     * @throws IOException 
     */
    @Override
    public String read_string() throws IOException {
          
        int readBytes = socket.read(buffer);
        
        if (readBytes == -1) throw new IOException();
        return getStringFromBuffer();
    }
    
      
    /**
     * Escriure un string
     * 
     * @param str
     * @throws IOException 
     */
    @Override
    public void write_string(String str) throws IOException {
        
        buffer = ByteBuffer.wrap(str.getBytes());
        socket.write(buffer);
    }
    
    /**
     * Retorna l'string del buffer
     * @return 
     */
    public String getStringFromBuffer() {
        return new String(buffer.array()).trim();
    }

    @Override
    public void write_int32(int number) throws IOException {
        
        byte [] bytes = new byte[4];
          
        int32ToBytes(number, bytes, "be");
        buffer = ByteBuffer.wrap(bytes);
        socket.write(buffer);
        
    }

    @Override
    public void write_string_of_its_length(String str) throws IOException {
        write_string(str);
    }

    @Override
    protected byte[] read_bytes(int numBytes) throws IOException {
        
        buffer = ByteBuffer.allocate(numBytes);
        if (socket.read(buffer) == -1) throw new IOException();
        return buffer.array();
    }

     
}
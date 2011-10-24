/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package penoplatinum.bluetooth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes text received from packets through a packettransporter to a file
 */
public class RemoteFileLogger {

    Thread fileThread;

    public RemoteFileLogger(IConnection conn, int Utils, String baseFilename, final File directory) {
        directory.mkdirs();

        final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd.HHmmss");

        
        




        int testNum = 0;

        final PacketTransporter pt = new PacketTransporter(conn);
        conn.RegisterTransporter(pt, penoplatinum.Utils.PACKETID_LOG);
        conn.RegisterTransporter(pt, penoplatinum.Utils.PACKETID_STARTLOG);


        fileThread = new Thread(new Runnable() {

            @Override
            public void run() {
                PrintStream fs = null;
                while (true) {
                    int id = pt.ReceivePacket();
                    Scanner scanner = new Scanner(pt.getReceiveStream()); //TODO: GC
                    
                    if (id == penoplatinum.Utils.PACKETID_LOG) {
                        String s;
                        s = scanner.nextLine();
                        fs.println(s);
                    } else if (id == penoplatinum.Utils.PACKETID_STARTLOG) {
                        String baseFilename = scanner.nextLine();
                        if (baseFilename.length() == 0)
                            baseFilename = "DEFAULT";
                        if (baseFilename.length()> 100)
                            baseFilename = baseFilename.substring(0,100);
                        
                        
                        File file = new File(directory.getAbsoluteFile() + "/" + baseFilename + format.format(new Date()) + ".txt");
                        try {
                            fs = new PrintStream(file);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(RemoteFileLogger.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }
            }
        });
        fileThread.setName("RemoteFileLogger");
        fileThread.setDaemon(true);


    }

    public void startLogging() {
        fileThread.start();

    }
}
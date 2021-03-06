import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class SecureFileTransfer {
    // send/receive tối đa 256 bytes 
    private static final int MAXBYTE = 256;

    public static void main(String[] args) throws Exception{

        Scanner scan = new Scanner(System.in);

        int selection = 0;

        while(selection != 3) {

            System.out.println("Welcome.");
            System.out.println("Please select operation.");
            System.out.println("1. Send File (to all users will connect)");
            System.out.println("2. Receive File");
            System.out.println("3. Exit");
            System.out.print("> ");

            selection = scan.nextInt();

            switch (selection) {
                case 1: {
                    System.out.print("Enter port for listening: ");

                    // Nhập port của bên gửi
                    int port = scan.nextInt();

                    System.out.print("Enter file path (C:/file.txt) for sending to all clients: ");

                    // Nhập file cần gửi
                    String filePath = scan.next();
                    
                    scan.nextLine();
                    outer:      
                    System.out.println("Enter key(key must be length = (16 ~ 128 || 24 ~ 192|| 32 ~ 256) crypto): ");
                    String key = scan.nextLine();
                    int keyLength = key.length();
                    if(keyLength != 16 && keyLength != 24 && keyLength != 32){
                         System.out.println("Please enter key must be length = (16 || 24 || 32)");
                        break;
                    }
                    
                    // Khởi tạo server socket
                    ServerSocket serverSocket = new ServerSocket(port);

                    // Khởi tạo logger cho tiến trình
                    Logger threadLogger = Logger.getLogger("serverLogger");
                    System.out.println("Server is reading file. Wait until finished!");
                    // Đọc file cần gửi 
                    String fileContent = readFile(filePath);

                    if (!fileContent.equalsIgnoreCase("")) {
                        // Lấy file name
                        String fileName = getFileName(filePath);

                        System.out.println("Server mode initiated. Serving clients on port " + port + ". Now you can get file from clients");

                        while (true) {
                            Socket clientSocket = serverSocket.accept();

                            // Khởi tạo 1 tiến trình cho client mới
                            Thread thread = new Thread(new ServerProtocol(clientSocket, threadLogger, fileName, fileContent, key));
                            thread.start();
                            threadLogger.info("Created and started new thread " + thread.getName() + " for client.");
                        }
                    } else {
                        System.err.println("File not found.");
                    }

                    break;
                }

                case 2:
                    //Nhập server 
                    System.out.println("Enter server for receiving.");
                    String server = scan.next();

                    //Nhập port của server
                    System.out.println("Enter port for listening.");
                    int servPort = scan.nextInt();

                    Socket clientSocket = new Socket(server, servPort);

                    DataInputStream fromServer = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream toServer = new DataOutputStream(clientSocket.getOutputStream());

                    String key = fromServer.readUTF();
                    System.out.println("chh_log key crypto -->" + key);

                    //
                    System.out.println("Waiting for file.");

                    try {
                        // Lấy đường dẫn file gửi từ server
                        String fName = fromServer.readUTF();

                        // Đọc file đã mã hóa từ server
                        String encryptedFile = "";
                        String line;
                        while (!(line = fromServer.readUTF()).equalsIgnoreCase("")) {

                            encryptedFile += line;

                            if (line.isEmpty()) {
                                break;
                            }
                        }
                        
                        System.out.println("ENCRYPTED FILE:"+encryptedFile);
                        //Lấy tên file
                        String fileLoc = fName.split(Pattern.quote(File.separator))[fName.split(Pattern.quote(File.separator)).length-1];
                        // Giải mã file
//                        System.out.println("File saved in:"+fileLoc);
                        String decryptedFile = decryptFile(encryptedFile, key);
                        System.out.println("DECRYPTED FILE:"+decryptedFile);

                        // Ghi đoạn văn đã giải mã vào file
                        writeFile(fileLoc, decryptedFile);

                        // Thông báo
                        System.out.println("File download complete. Saved in ./" + fileLoc + "\n");

                    } catch (Exception e) {
                        System.err.println("Error while creating/reading server socket: " + e);
                    }

                    break;
                case 3:
                    System.out.println("Bye bye.");
                    break;
                default:
                    System.out.println("Select 1, 2 or 3.");
                    break;
            }

        }

    }

    // Giải mã đoạn văn bản với secret key
    public static String decryptFile(String encryptedText, String secretKey) {
        return new String(new AES(secretKey.getBytes()).ECB_decrypt(Base64.getDecoder().decode(encryptedText)));
    }


    // Đọc file
    public static String readFile(String fileName) {
        InputStream inStream = null;
        String fileContent = "";

        try {
            inStream = new FileInputStream(fileName);


            int fileSize = inStream.available();
            for(int i = 0; i < fileSize; i++) {
                fileContent += (char) inStream.read();
            }

        } catch(Exception e) {
            System.err.println("File not found: " + fileName);
        } finally {
            try {
                if(inStream != null) {
                    inStream.close();
                }
            } catch(Exception ex) {
                System.err.println("Error while closing File I/O: " + ex);
            }
        }

        return fileContent;
    }

    // Ghi file
    public static void writeFile(String fileName, String fileContent) {
        OutputStream outStream = null;
        try {
            outStream = new FileOutputStream(fileName);

            byte[] fileContentBytes = fileContent.getBytes();

            outStream.write(fileContentBytes);

        } catch(Exception e) {
            System.err.println("Error while writing into file " + fileName + ": " + e);
        } finally {
            try {
                if(outStream != null) {
                    outStream.close();
                }
            } catch(Exception ex) {
                System.err.println("Error while closing File I/O: " + ex);
            }
        }
    }

    // Hàm lấy tên file từ path
    public static String getFileName(String filePath) {

        String[] split =  filePath.split("\\/");

        return split[split.length - 1];
    }

}

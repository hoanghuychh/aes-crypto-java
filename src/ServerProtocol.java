import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


// class để cho 1 server chạy multi thread
public class ServerProtocol implements Runnable {

    // Gửi max 256 byte
    private static final int MAXBYTE = 256;

    private Socket clientSocket;
    private Logger threadLogger;
    private String fileName;
    private String fileContent;
    private String key;
    private AES cipher;

    
    public ServerProtocol(Socket socket, Logger logger, String fName, String fContent, String key) {
        this.clientSocket = socket;
        this.threadLogger = logger;
        this.fileName = fName;
        this.fileContent = fContent;
        this.key = key;
        this.cipher = new AES(key.getBytes());
    }

    // Xử lý thread của client
    public void handleClient(Socket clientSocket, Logger threadLogger, String fileName, String fileContent) {

        try {
            DataOutputStream toClient = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream fromClient = new DataInputStream(clientSocket.getInputStream());

            threadLogger.log(Level.INFO, "Sending process started.");

            // Server khởi tạo p(prime number) và g (prime number's generator)
            // Server gửi p và g đến client
            // server chọn một số bí mật (a)
            // server tính A=g^a(modp)
            // server gửi A cho client.
            // server nhận B từ client.
            // server tính s=B^a(modp)
            // ->Server có khóa bí mật

//            AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
//            paramGen.init(1024, new SecureRandom());
//            AlgorithmParameters params = paramGen.generateParameters();
//            DHParameterSpec dhSpec = (DHParameterSpec)params.getParameterSpec(DHParameterSpec.class);
//
//            Random randomGenerator = new Random();
//
//            BigInteger a = new BigInteger(1024, randomGenerator); // Khóa bí mật a (private) (server)
//            BigInteger p = dhSpec.getP(); // prime number (public) (khởi tạo ở server)
//            BigInteger g = dhSpec.getG(); // primer number generator (public) (khởi tạo ở server)
//
//            BigInteger A = g.modPow(a, p); // tính khóa public của server(A=g^a(modp))
//
//            // Gửi p
//            toClient.writeUTF(p.toString());
//
//            // Gửi g
//            toClient.writeUTF(g.toString());
//
//            // Gửi A
//            toClient.writeUTF(A.toString());
//
//            // NhậnB
//            BigInteger B = new BigInteger(fromClient.readUTF());
//
//            // Tính khóa bí mật
//            BigInteger encryptionKeyServer = B.modPow(a, p);
//
//            System.out.println("Calculated key: " + encryptionKeyServer);
//
//            // khởi tạo AES key
//            Key key = generateKey(encryptionKeyServer.toByteArray());

            toClient.writeUTF(key);
            
            // Gửi tên file
            toClient.writeUTF(fileName);
            // Mã hóa nội dung file
            String encryptedFile = encryptFile(fileContent);

            byte[][] split;
            // chia nhỏ filethành các gói có sizê max là 256 bit
            if((split = chunkArray(encryptedFile.getBytes(), MAXBYTE)) != null) {

                for(int i = 0; i < split.length; i++) {
                    // Gửi packet
                    toClient.writeUTF(new String(split[i]));
                }
            }

            toClient.writeUTF("");

            toClient.flush();

            threadLogger.log(Level.INFO, "Sending process complete. " + split.length + " total packages sent.");


        } catch(Exception e) {
            threadLogger.log(Level.WARNING, "Error while creating output stream " + e);
        }

    }

    public String encryptFile(String plainText) {
        return Base64.getEncoder().encodeToString(cipher.ECB_encrypt(plainText.getBytes()));
    }

    public void run() {
        handleClient(clientSocket, threadLogger, fileName, fileContent);
    }

    public static byte[][] chunkArray(byte[] array, int chunkSize) {
        int numOfChunks = (int)Math.ceil((double)array.length / chunkSize);
        byte[][] output = new byte[numOfChunks][];

        for(int i = 0; i < numOfChunks; ++i) {
            int start = i * chunkSize;
            int length = Math.min(array.length - start, chunkSize);

            byte[] temp = new byte[length];

            System.arraycopy(array, start, temp, 0, length);

            output[i] = temp;
        }

        return output;
    }

    // Khởi tạo khóa từ các thông số đã có. Chương trình tự randam, mình khum có nhập.
    private static Key generateKey(byte[] sharedKey)
    {
        // Mã hóa AES 128 bit. Nên cái bytekey chỉ đượt 16 byte thoi..
        byte[] byteKey = new byte[16];
        for(int i = 0; i < 16; i++) {
            byteKey[i] = sharedKey[i];
        }

        // chuyển sang định dạng AES
        try {
            Key key = new SecretKeySpec(byteKey, "AES");

            return key;
        } catch(Exception e) {
            System.err.println("Error while generating key: " + e);
        }

        return null;
    }
}

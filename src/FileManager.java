//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.util.Base64;
//
//
//public class FileManager {
//
//    // Do chạy đa luồng nên dùng intance cho an toàn
//
//    // file input strram để đọc file
//    private FileInputStream inStream;
//
//    // file out stream để ghi file
//    private FileOutputStream outStream;
//
//
//
//
//    protected FileManager() {
//        try {
//
//        } catch(Exception e) {
//            System.err.println("Error while getting AES algorithm: " + e);
//        }
//    }
//
//    // Đọc file
//    public String readFile(String fileName) {
//
//        String fileContent = "";
//
//        try {
//            inStream = new FileInputStream(fileName);
//
//
//            int fileSize = inStream.available();
//            for(int i = 0; i < fileSize; i++) {
//                fileContent += (char) inStream.read();
//            }
//
//        } catch(Exception e) {
//            System.err.println("File not found: " + fileName);
//        } finally {
//            try {
//                if(inStream != null) {
//                    inStream.close();
//                }
//            } catch(Exception ex) {
//                System.err.println("Error while closing File I/O: " + ex);
//            }
//        }
//
//        return fileContent;
//    }
//
//    // Ghi file
//    public void writeFile(String fileName, String fileContent) {
//        try {
//            outStream = new FileOutputStream(fileName);
//
//            byte[] fileContentBytes = fileContent.getBytes();
//
//            outStream.write(fileContentBytes);
//
//        } catch(Exception e) {
//            System.err.println("Error while writing into file " + fileName + ": " + e);
//        } finally {
//            try {
//                if(outStream != null) {
//                    outStream.close();
//                }
//            } catch(Exception ex) {
//                System.err.println("Error while closing File I/O: " + ex);
//            }
//        }
//    }
//
//    // Hàm lấy tên file từ path
//    public String getFileName(String filePath) {
//
//        String[] split =  filePath.split("\\/");
//
//        return split[split.length - 1];
//    }
//
//    // Mã hóa đoạn văn bản với secretKey
////    public String encryptFile(String plainText, String secretKey) {
////        return null;
////    }
////
////    // Giải mã đoạn văn bản với secret key
////    public String decryptFile(String encryptedText, String secretKey) {
////        return null;
////    }
//
//
//}

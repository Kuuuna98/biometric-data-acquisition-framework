package com.kaist.iclab.util;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileToHashSha256 {

    public static String extractFileHashSHA256(String filename) throws Exception {

        String SHA = "";
        int buff = 30000;
        try {
            RandomAccessFile file = new RandomAccessFile(filename, "r");

            MessageDigest hashSum = MessageDigest.getInstance("SHA-256");

            byte[] buffer = new byte[buff];
            byte[] partialHash = null;

            long read = 0;

            // calculate the hash of the hole file for the test
            long offset = file.length();
            int unitsize;
            while (read < offset) {
                unitsize = (int) (((offset - read) >= buff) ? buff : (offset - read));
                file.read(buffer, 0, unitsize);

                hashSum.update(buffer, 0, unitsize);

                read += unitsize;
            }

            file.close();
            //partialHash = new byte[hashSum.getDigestLength()];
            partialHash = hashSum.digest();

            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < partialHash.length ; i++){
                sb.append(Integer.toString((partialHash[i]&0xff) + 0x100, 16).substring(1));
            }
            SHA = sb.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return SHA;
    }

    /*public static String extractStringHashSHA256(String str){
        String SHA = "";
        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(str.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++){
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }
            SHA = sb.toString();

        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            SHA = null;
        }
        return SHA;
    }*/

    public static void main(String[] args) throws Exception {
        //System.out.println(extractStringHashSHA256("sha256Test"));
        System.out.println(extractFileHashSHA256("sha256Test.iml"));
    }
}

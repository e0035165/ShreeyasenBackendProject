package org.utilities;


import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Component
public class FileUtils {

    public byte[] readFile(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        return fileInputStream.readAllBytes();
    }

    public String readFileInString(File file) throws IOException {
        return new String(this.readFile(file));
    }

    public String detectFileType(byte[] fileData, String fileName) {
        String contentType = URLConnection.guessContentTypeFromName(fileName);
        return contentType != null ? contentType : "application/octet-stream";
    }


    public void byteArrayToFileWithNIO(byte[] byteArray, String filePath) {
        try (FileChannel fileChannel = FileChannel.open(Paths.get(filePath),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.wrap(byteArray);
            fileChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package org.services;


import org.entity.Documents;
import org.repositories.DocumentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.utilities.FileUtils;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.utilities.KeyTracker;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DocumentService {
    @Autowired
    private DocumentRepo documentRepo;

    @Autowired
    private FileUtils fileUtils;

    @Value(value = "${resume_folder}")
    private String UPLOAD_DIR;

    public Documents saveFile(MultipartFile file, String title) throws IOException {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Create file path
        String filePath = UPLOAD_DIR + file.getOriginalFilename();

        // Save file to disk
        Path path = Paths.get(filePath);
        Files.write(path, file.getBytes());
        Documents document = new Documents();
        document.setTitle(title);
        document.setData(file.getBytes());
        document.setFilePath(path.toAbsolutePath().toString());
        document.setFileType(file.getContentType());
        document.setRelative_file_name(file.getOriginalFilename());
        return documentRepo.save(document);
    }

    public List<Documents> getRelevantDocuments(List<String> keywords) throws IOException {
        List<Documents>documentsList = documentRepo.findAll();List<Documents>relevantDocuments = new ArrayList<>();
        KeyTracker.setKeyWords(keywords);

        documentsList.stream()
                .forEach(
                        documents -> {
                            String rawValue = KeyTracker.numberProcessing(readResume(documents.getId()));
                            String processedValue = KeyTracker.cleanResume(rawValue);
                            Set<String> validatedKeys = KeyTracker.getAllKeys(processedValue);
                            if(validatedKeys.size()==KeyTracker.getKeyValidatedWords().size()) {
                                relevantDocuments.add(documents);
                            }
                        }
                );
        System.out.println("Files size: "+relevantDocuments.size());
        String[] srcFiles = new String[relevantDocuments.size()];
        for(int i=0;i< relevantDocuments.size();++i) {
            srcFiles[i] = relevantDocuments.get(i).getFilePath();
        }
        Arrays.stream(srcFiles).toList().forEach(System.out::println);
        zipFiles(srcFiles,"src/main/resources/compresssed.zip");

        return relevantDocuments;
    }

    public static void zipFiles(String[] srcFiles, String zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            byte[] buffer = new byte[1024];

            for (String srcFile : srcFiles) {
                File fileToZip = new File(srcFile);
                try (FileInputStream fis = new FileInputStream(fileToZip)) {
                    zos.putNextEntry(new ZipEntry(fileToZip.getName()));
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
            zos.close();
        }
        System.out.println("Files zipped successfully.");
    }


    public String readResume(Integer id) {
        Documents file = this.getFile(id);
        if(file==null)
            throw new RuntimeException("File is not in existence");

        String fileName = file.getFilePath();

        try (XWPFDocument doc = new XWPFDocument(
                Files.newInputStream(Paths.get(fileName)))) {

            XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(doc);
            String docText = xwpfWordExtractor.getText();
            //System.out.println(docText);

            // find number of words in the document
            long count = Arrays.stream(docText.split("\\s+")).count();
            //System.out.println("Total words: " + count);
            return docText;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Documents getFile(Integer id) {
        return documentRepo.findById(id).orElse(null);
    }

    public List<Documents> getAllFiles() {
        return documentRepo.findAll();
    }

    public void clearFiles() {
        File folder = new File(UPLOAD_DIR);
        folder.deleteOnExit();
        documentRepo.deleteAll();
    }
}

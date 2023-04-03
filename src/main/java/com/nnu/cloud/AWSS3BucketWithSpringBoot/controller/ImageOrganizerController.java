package com.nnu.cloud.AWSS3BucketWithSpringBoot.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/aws")
public class ImageOrganizerController {
    private AmazonS3 amazonS3;
    private final String IMAGE_DIRECTORY_NAME = "images/";
    private final String BUCKET_NAME = "yaseen-assignment-3-bucket";
    @Autowired
    public ImageOrganizerController(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            amazonS3.putObject(BUCKET_NAME, IMAGE_DIRECTORY_NAME + file.getOriginalFilename(), file.getInputStream(), null);
            return "File uploaded successfully!";
        } catch (IOException e) {
            return "Error uploading file.";
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable("fileName") String fileName) {
        S3Object s3Object = amazonS3.getObject(BUCKET_NAME, IMAGE_DIRECTORY_NAME + fileName);
        InputStream inputStream = s3Object.getObjectContent();
        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(fileName).build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/files")
    public List<String> getFileList() {
        try {
            ObjectListing objectListing = amazonS3.listObjects(BUCKET_NAME, IMAGE_DIRECTORY_NAME);
            List<String> files = new ArrayList<>();
            for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
                files.add(os.getKey().substring(os.getKey().lastIndexOf("/") + 1));
            }
            return files;
        } catch (Exception e){
            System.out.println(e.getStackTrace());
            return null;
        }
    }
}
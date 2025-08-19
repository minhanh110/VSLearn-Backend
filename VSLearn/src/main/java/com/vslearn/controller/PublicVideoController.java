package com.vslearn.controller;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/video")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class PublicVideoController {
    
    @Value("${gcp.storage.bucket.name}")
    private String bucketName;
    
    @Autowired
    private Storage storage;
    
    // Serve video files from Google Cloud Storage (public access)
    @GetMapping("/sentence")
    public ResponseEntity<String> getSentenceVideo(@RequestParam String objectName) {
        try {
            System.out.println("🔍 Public video request - Object name: " + objectName);
            System.out.println("🔍 Bucket name: " + bucketName);
            
            // Generate signed URL
            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            
            System.out.println("🔍 BlobId: " + blobId);
            System.out.println("🔍 BlobInfo: " + blobInfo);
            
            java.net.URL signedUrl = storage.signUrl(blobInfo, 2, java.util.concurrent.TimeUnit.HOURS, 
                Storage.SignUrlOption.withV4Signature());
            
            System.out.println("🔍 Signed Video URL: " + signedUrl);
            
            // Redirect to signed URL
            return ResponseEntity.status(302)
                .header("Location", signedUrl.toString())
                .build();
                
        } catch (Exception e) {
            System.out.println("❌ Error serving public video: " + e.getMessage());
            System.out.println("❌ Exception type: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
} 
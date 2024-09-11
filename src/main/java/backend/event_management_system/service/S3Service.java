package backend.event_management_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final String bucketName;
    private final S3Presigner presigner;

    public S3Service(@Value("${aws.accessKeyId}") String accessKeyId,
                     @Value("${aws.secretKey}") String accessSecretKey,
                     @Value("${aws.s3.region}") String bucketRegion,
                     @Value("${aws.s3.bucket}") String bucketName){
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKeyId, accessSecretKey);

        this.bucketName = bucketName;
        this.presigner = S3Presigner.builder()
                .region(Region.of(bucketRegion))
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .build();

        this.s3Client = S3Client.builder()
                    .region(Region.of(bucketRegion))
                    .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                    .build();
    }

    public String generatePresignedUrl(String objectKey){
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30))
                .getObjectRequest(getObjectRequest)
                .build();
        PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(getObjectPresignRequest);
                return presignedGetObjectRequest.url().toString();
    }

    public String uploadProfileImage(String username, MultipartFile file) {
        String key = "user-profile-images/" + generateUniqueFileName(username, file.getOriginalFilename());
        return uploadFile(key, file);
    }

    public String uploadEventFile(String eventName, MultipartFile file) {
        String key = "created-event/" + generateUniqueFileName(eventName, file.getOriginalFilename());
        return uploadFile(key, file);
    }

    private String uploadFile(String key, MultipartFile file) {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));
            return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(key)).toString();
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to s3: " + e.getMessage(), e);
        }
    }

    public void deleteEventFile(String fileUrl){
        try {
            URL url = new URL(fileUrl);
            String key = url.getPath().substring(1);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Invalid file url: " + fileUrl, exception);
        }
    }

    private String generateUniqueFileName(String prefix, String originalFilename) {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return prefix + "_" + timestamp + fileExtension;
    }

}

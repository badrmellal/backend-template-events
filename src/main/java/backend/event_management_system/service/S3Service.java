package backend.event_management_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final String bucketName;


    public S3Service(@Value("${aws.accessKeyId}") String accessKeyId,
                     @Value("${aws.secretKey}") String accessSecretKey,
                     @Value("${aws.s3.region}") String bucketRegion,
                     @Value("${aws.s3.bucket}") String bucketName){

        this.bucketName = bucketName;

        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKeyId, accessSecretKey);
            this.s3Client = S3Client.builder()
                    .region(Region.of(bucketRegion))
                    .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                    .build();
    }

    public String uploadProfileImage(String username, MultipartFile file){
        String key = "user-profile-images/" + username + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        try {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build(),
                    RequestBody.fromBytes(file.getBytes()));
            return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(key)).toString();
        } catch (IOException e) {
            throw new RuntimeException("Error uploading image to s3" + e.getMessage(), e);
        }
    }

    public String uploadEventFile(String eventName, MultipartFile file){
        String key = "created-event/" + eventName + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        try {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build(),
                    RequestBody.fromBytes(file.getBytes()));
            return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(key)).toString();
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to s3" + e.getMessage(), e);
        }
    }


}

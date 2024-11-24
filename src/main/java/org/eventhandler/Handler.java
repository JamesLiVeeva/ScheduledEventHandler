package org.eventhandler;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.AWSBatchClientBuilder;
import com.amazonaws.services.batch.model.SubmitJobRequest;
import com.amazonaws.services.batch.model.SubmitJobResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Handler implements RequestHandler<Object, String> {

    private final static String BUCKET_NAME = "human-resources-bucket-for-demo";
    private final static String FILE_SEPARATOR = "/";

    private final static String AWS_ACCESS_KEY = "AWS_Access_Key";
    private final static String AWS_ACCESS_SECRET = "AWS_Access_Secret";

    @Override
    public String handleRequest(Object event, Context context) {
        if(isNewFileUploaded()){
            System.out.println("New file provided!");
            triggerBatchJob();
            System.out.println("Batch job triggered!");
        }

        return "Lambda hr-import-process-trigger-function finished!";
    }

    private boolean isNewFileUploaded(){
        boolean hasNewFile = false;
        AmazonS3 s3Client = getS3Client();

        for (UserImportFileInfoEnum fileInfoEnum : UserImportFileInfoEnum.values()) {
            S3ObjectSummary targetFileToImport = findTargetFileToImport(fileInfoEnum, s3Client);
            if(targetFileToImport != null){
                hasNewFile = true;
                break;
            }
        }

        return hasNewFile;
    }

    private static ListObjectsV2Result listS3Objects(String path, AmazonS3 s3Client) {
        return s3Client.listObjectsV2(new ListObjectsV2Request().withBucketName(BUCKET_NAME).withPrefix(path));
    }

    private static AmazonS3 getS3Client() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(System.getenv(AWS_ACCESS_KEY), System.getenv(AWS_ACCESS_SECRET));
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();
    }

    private void triggerBatchJob(){
        String jobName = "hr-import-job-" + LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String jobQueue = "arn:aws:batch:ap-northeast-1:017820698978:job-queue/data-import-demo-job-queue";
        String jobDefinition = "arn:aws:batch:ap-northeast-1:017820698978:job-definition/user-data-import-job-definition:1";
        String shareIdentifier = "userDataImport";

        SubmitJobRequest submitJobRequest = new SubmitJobRequest()
                .withJobName(jobName)
                .withJobQueue(jobQueue)
                .withJobDefinition(jobDefinition)
                .withShareIdentifier(shareIdentifier)
                .withSchedulingPriorityOverride(0);

        SubmitJobResult submitJobResult = getBatchClient().submitJob(submitJobRequest);
        System.out.println("Job " + jobName + " submitted successfully with ID: " + submitJobResult.getJobId());
    }

    private static AWSBatch getBatchClient() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(System.getenv(AWS_ACCESS_KEY), System.getenv(AWS_ACCESS_SECRET));
        return AWSBatchClientBuilder.standard()
                .withRegion(Regions.AP_NORTHEAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    private S3ObjectSummary findTargetFileToImport(UserImportFileInfoEnum fileInfoEnum, AmazonS3 s3Client){
        ListObjectsV2Result objects = listS3Objects(fileInfoEnum.getFilePath(), s3Client);
        for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
            String objectSummaryKey = objectSummary.getKey();
            if(isTargetFileToProcess(fileInfoEnum, objectSummaryKey)){
                return objectSummary;
            }
        }
        return null;
    }

    private boolean isTargetFileToProcess(UserImportFileInfoEnum fileInfoEnum, String objectSummaryKey){
        boolean result = false;
        if(objectSummaryKey != null) {
            if(objectSummaryKey.contains(FILE_SEPARATOR)){
                objectSummaryKey = objectSummaryKey.substring(objectSummaryKey.lastIndexOf(FILE_SEPARATOR) + 1);
            }

            if(objectSummaryKey.toLowerCase().startsWith(fileInfoEnum.getFileNamePrefix().toLowerCase())
                    && objectSummaryKey.toLowerCase().endsWith(fileInfoEnum.getFileExtension().toLowerCase())){
                String dateInFileName = objectSummaryKey.substring(fileInfoEnum.getFileNamePrefix().length(),
                        objectSummaryKey.length() - fileInfoEnum.getFileExtension().length() - 1);
                try{
                    DateTimeFormatter.ofPattern(fileInfoEnum.getDatePattern()).parse(dateInFileName);
                    result = true;
                } catch (DateTimeParseException exp){
                    System.out.println("Fail to parse date for file: " + objectSummaryKey);
                }
            }
        }
        return result;
    }
}

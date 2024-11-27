# Lambda Event Handler

This is the Lambda event handler which is used to check if new employee file(s) are uploaded to S3 bucket on a scheduled time daily. 
If new file(s) are detected, AWS batch job will be triggered to process the files provided.

## More Information

This is a sub-project of the Employee Import Project: https://github.com/Annielz1223/HRImport/. 
You could get more information from this main project if needed.

## Dependency

- Java 17
- AWS S3
- AWS Lambda
- AWS Batch
- Gradle

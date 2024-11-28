# Lambda Event Handler

This is the Lambda event handler, which checks whether new employee file(s) are uploaded to the S3 bucket at a scheduled time daily. 
If new file(s) are detected, an AWS batch job will be triggered to process the provided files.

## More Information

See [Employee Import Repository](https://github.com/JamesLiVeeva/EmployeeImport) for more details.

## Dependency
- Java 17
- AWS S3
- AWS Lambda
- AWS Batch
- Gradle

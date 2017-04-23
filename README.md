# floating-average-crypto
## Floating average calculation for crypto currency.

This is mainly a project to try out different technologies with a (more or less) real use case.
Maybe it doesn't make sense ;)

### What is it for?
This code should be deployed as a lambda function in AWS.
During execution it will pull the top 100 crypto currencies from an API and store the values as ProtoBuf in S3.
Currently it is designed to calculate the 200 days average of each currency, hence it should run once a day.
So, yeah you have to wait 200 days before you actually have the average if you start from scratch...
It will notify any subscribers to an AWS SNSN topic if the current value of the currency deviates more or less than a percentage threshold.

### how to use
* have an AWS Account
* create an S3 bucket
* provide S3 bucket in the config.properties file as _aws.s3.bucket.name_
* mvn clean package
* create a Java 8 Lambda function, provide a SNS topic as Environment Variable _floatingAverageWarningTopic_
* upload the fat jar
* run for test
* configure AWS CloudWatch to run Lambda function once a day

Technology used in this one:
* [Java 8](https://java.com/de/)
* [Google Guice](https://github.com/google/guice) 
* [Google AutoValue](https://github.com/google/auto/tree/master/value) 
* [Google Protocol Buffers](https://developers.google.com/protocol-buffers/)
* [AWS S3](https://aws.amazon.com/s3/)
* [AWS SNS](https://aws.amazon.com/sns/)
* [UniRest](http://unirest.io/)

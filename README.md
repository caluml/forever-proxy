# Forever Proxy


This is a proxy that saves all responses. It can be useful for capturing traffic from IoT devices, or other devices where it is not possible to see/verify their behaviour.

The proxy functionality is from https://github.com/tkohegyi/mitmJavaProxy/

Current storage backends are
* Local filesystem
* S3

## Usage

1. Build. `mvn clean package`
2. Run. `java -jar target/forever-proxy-1.0-SNAPSHOT.jar`
3. Run your browser with a proxy set to 127.0.0.1:8080
4. If you don't want browser warnings, import the CA certificate from https://github.com/tkohegyi/mitmJavaProxy/tree/master/src/main/resources/sslSupport (*)
5. Browse to websites

\* Please note - <b>importing this CA certificate is unsafe, and could allow attackers on the web to spoof any HTTPS website</b>. Remember to delete it when you are finished

### Chrome
Start Chrome with `chrome --http-proxy=127.0.0.1:8080`

### Firefox
<a href="about:preferences#general">Menu, Settings, General</a>, Network Settings<br>

### curl
```bash
curl -v --cacert /path/to/cybervillainsCA.cer --proxy 127.0.0.1:8080 https://google.com
```

### S3 storage plugin
This requires 4 system properties to work:
* storage.s3.aws-access-key-id
* storage.s3.aws-secret-access-key
* storage.s3.region
* storage.s3.bucket

#### Setup
1. Create a bucket (consider using Versioning, Server-side encryption with AWS Key Management Service keys (SSE-KMS), and/or Lifecycle Management)
2. Create an IAM user (no console access required)
3. Generate an access key.
4. Create a policy for that user that only allows access to the bucket created previously. An example is below:
```JSON
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "Statement1",
      "Effect": "Allow",
      "Action": [
        "s3:PutObject"
      ],
      "Resource": [
        "arn:aws:s3:::bucket-name",
        "arn:aws:s3:::bucket-name/*"
      ]
    }
  ]
}
```

## TODO
* Store requests
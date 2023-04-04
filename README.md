# Cloud-AWS-S3-With-Spring-Boot-Assignment-3
* First, I create a bucket on AWS S3, then I create an access key.
* I install `AWS Toolkit` on Intellij, then set bucket access and secret keys and the region in `application.properties` file, which this tool will configure AWS configuration and make an access to the S3 service.
```
cloud.aws.credentials.accessKey= XXXXXXXXXXXX
cloud.aws.credentials.secretKey= XXXXXXXXXXXXXXXXXXXXXX
cloud.aws.region.static=eu-west-2
server.port=8082
```
* I set an AWS SDK S3 dependency on `pom.xml` and other required dependencies.
```
<dependency>
	<groupId>com.amazonaws</groupId>
	<artifactId>aws-java-sdk-s3</artifactId>
	<version>1.12.429</version>
</dependency>
```
* Configure amazons3 bean on `AmazonS3Configuration` class
```
@Bean
    public AmazonS3 amazonS3() {
        try {
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(region);
            return builder.build();
        } catch (Exception e){
            System.out.println(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }
```
### Create an endpoints 
* Upload endpoint: which is used to upload a file or image by send them as a parameter then store it in the bucket using `http://localhost/aws/upload`
```
@PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            amazonS3.putObject(BUCKET_NAME, IMAGE_DIRECTORY_NAME + file.getOriginalFilename(), file.getInputStream(), null);
            return "File uploaded successfully!";
        } catch (IOException e) {
            return "Error uploading file.";
        }
    }
```
* Download endpoint: which is used to download a file or image byte by byte by send it's name with the request using `http://localhost/aws/download/img.png`
```
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
```
* List all files endpoint: which is used to list all files or images in the `image/` directory using `http://localhost/aws/download/img.png`
```
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
```
* You can find a simple demo for app <a href="https://user-images.githubusercontent.com/59315877/229706326-1516a262-ce85-46f1-9d01-e535b51c2870.mp4">Here</a>


### To Deploy the application on an EC2 instance
* Create an EC2 instance
* Install java on the instance
* Install Tomcat server
* Build a spring boot app 
* Then move the app on it and set the requierd configs for maven
* Set requierd security group configs 
* Run the app
* Test the app

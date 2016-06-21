#  MSF4J FormParam Support
In JAX-RS, we can use @FormParam annotation to bind HTML form parameters value to a Java method. 
MSF4J supports below content types for FormParam
1. application/x-www-form-urlencoded
2. multipart/form-data

### For the application/x-www-form-urlencoded 
This is pretty much similar to the QueryParam. MSF4J reads the Request body and get the decode the encoded values and pass the values to the service.

E.g.
Sample service for application/x-www-form-urlencoded content type
```java
@POST
@Path("/formParam")
public Response testFormParam(@FormParam("age") int age, @FormParam("name") String name) {
   System.out.println("Name " + name);
   System.out.println("Age" + age);
   return Response.ok().entity("Name and age " + name + ", " + age).build();
}
```
Parameters should be based on http://docs.oracle.com/javaee/7/api/javax/ws/rs/FormParam.html

### For the multipart/form-data
```java
@POST
@Path("/formParam")
@Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
public Response testFormParam(@FormParam("age") int age, @FormParam("name") String name) {
   System.out.println("Name " + name);
   System.out.println("Age" + age);
   return Response.ok().entity("Name and age " + name + ", " + age).build();
}
```
If a user wants, MSF4J will not process the stream but rather directly pass an FormParamIterator which can be used to retrieve the parameter values. 
FormParam name must be ‘form’ and the type must be FormParamIterator

E.g.
Sample service for multipart/form-data content-type with file upload
```java
@POST
@Path("/simpleFormStreaming")
@Consumes(MediaType.MULTIPART_FORM_DATA)
public Response simpleFormStreaming(@Context FormParamIterator formParamIterator) {
   try {
       while (formParamIterator.hasNext()) {
           FormItem item = formParamIterator.next();
           if (item.isFormField()) {
               System.out.println(item.getFieldName() + " - " + StreamUtil.asString(item.openStream()));
           } else {
               Files.copy(item.openStream(), Paths.get(System.getProperty("java.io.tmpdir"), item.getName()));
           }
       }
   } catch (FileUploadException e) {
       log.error("Error while uploading the file " + e.getMessage(), e);
   } catch (IOException e) {
       log.error("Unable to upload the file " + e.getMessage(), e);
   }
   return Response.ok().entity("Request completed").build();
}
```
If you like use non streaming mode then you can directly get File objects in a file upload. Here rather than @FormParam you need to use *@FormDataParam* annotation. This annotation can be used with all FormParam supported data types plus File and bean types as well as InpuStreams.

If you want to upload set of files. Then a sample service would be like as follows:
```java
@POST
@Path("/multipleFiles")
@Consumes(MediaType.MULTIPART_FORM_DATA)
public Response multipleFiles(@FormDataParam("files") List<File> files) {
   files.forEach(file -> {
       try {
           Files.copy(file.toPath(), Paths.get(System.getProperty("java.io.tmpdir"), file.getName()));
       } catch (IOException e) {
           log.error("Error while Copying the file " + e.getMessage(), e);
       }
   });
   return Response.ok().entity("Request completed").build();
}
```
You can use more complex times with combination of primitives, beans and files as follows
```java
@POST
@Path("/complexForm")
@Consumes(MediaType.MULTIPART_FORM_DATA)
public Response complexForm(@FormDataParam("file") File file,
                       @FormDataParam("id") int id,
                       @FormDataParam("people") List<Person> personList,
                       @FormDataParam("company") Company animal) {
   System.out.println("First Person in List " + personList.get(0).getName());
   System.out.println("Id " + id);
   System.out.println("Company " + animal.getType());
   try {
       Files.copy(file.toPath(), Paths.get(System.getProperty("java.io.tmpdir"), file.getName()));
   } catch (IOException e) {
       log.error("Error while Copying the file " + e.getMessage(), e);
   }
   return Response.ok().entity("Request completed").build();
}
```

If you like to get the InputStream of a file then you can go ahead like below example. There FileInfo bean will hold the filename and the content type attributes of the particular inputstream. Note that attribute names must be equal when you use InpuStream. Here I’ve used ‘file’ for the both params.
```java
@POST
@Path("/streamFile")
@Consumes(MediaType.MULTIPART_FORM_DATA)
public Response multipleFiles(@FormDataParam("file") FileInfo fileInfo,
                             @FormDataParam("file") InputStream inputStream) {
   try {
       Files.copy(inputStream, Paths.get(System.getProperty("java.io.tmpdir"), fileInfo.getFileName()));
   } catch (IOException e) {
       log.error("Error while Copying the file " + e.getMessage(), e);
       return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
   } finally {
       IOUtils.closeQuietly(inputStream);
   }
   return Response.ok().entity("Request completed").build();
}
```

# FormParam Sample

This is the MSF4J sample which demonstrate how to use FormParam.

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

From this directory, run
```
java -jar target/formparam-*.jar
```

## How to test the sample

We will use the cURL command line tool for testing. You can use your preferred HTTP or REST client too.
Simple client is also available in the sample

## Sample CURL Command 
curl -X POST -H "Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW" -F "name=WSO2" -F "age=10" -F "image=@abc.png" "http://localhost:8080/formService/simpleFormStreaming"
package dk.tdc.chub;

//import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;
//ApiOperation:import io.swagger.annotations.ApiOperation;
//ConditionalOnProperty:import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//Generated:import javax.annotation.Generated;

//Generated:@Generated(value = "com.github.hervian.swagger.mojos.GenerateSwaggerDocMojo")
//ConditionalOnProperty:@ConditionalOnProperty(value = "swagger.ui.disabled", havingValue = "false", matchIfMissing = true) //Give Spring users an option to exclude swagger-ui from, say deployments to prod environment by setting swagger.ui.disabled=true in application-prod
//@Component
@Path("/doc") //TODO Insert configurable base path? If done, remember to update GenerateSwaggerDocMojo which has a hard coded reference to 'doc' when specifying the url of the swagger.json in the index.html
public class SwaggerDocResource {

  @Context
  UriInfo uriInfo;

  //ApiOperation:@ApiOperation(value = "hidden", hidden = true)
  @GET @Path("swagger-ui")
  public InputStream getSwaggerUi(){
    return getSwaggerUiHtml();
  }

  //ApiOperation:@ApiOperation(value = "hidden", hidden = true)
  @GET @Path("swagger-ui.html")
  public InputStream getSwaggerUiHtml(){
    System.out.println(uriInfo.getPath());
    for (PathSegment pathSegment: uriInfo.getPathSegments()){
      System.out.println(pathSegment.getPath());
    }
    String fileName = uriInfo.getPath().substring(uriInfo.getPath().lastIndexOf("/"));
    System.out.println("fileName = " + fileName);
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream resource4 = classloader.getResourceAsStream(String.format("swagger/ui/index.html"));
    System.out.println("resource4!=null: "+Boolean.valueOf(resource4!=null));
    return resource4;
  }

  //ApiOperation:@ApiOperation(value = "hidden", hidden = true)
  @GET @Path("swagger/swagger.json")
  public InputStream getSwaggerDoc(){
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    String path = uriInfo.getPath().substring(uriInfo.getPath().indexOf("/")+1);
    InputStream resource = classloader.getResourceAsStream("swagger/swagger.json");
    System.out.println("swagger.json resource!=null: "+Boolean.valueOf(resource!=null));
    return resource;
  }

  //ApiOperation:@ApiOperation(value = "hidden", hidden = true)
  @GET @Path("swagger/ui/{var:.+}") //https://www.logicbig.com/how-to/code-snippets/jcode-jax-rs-path-param-regex-match-all.html
  public InputStream getSwaggerUiFiles(){
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
   String path = uriInfo.getPath().substring(uriInfo.getPath().indexOf("/")+1);
    InputStream resource4 = classloader.getResourceAsStream(path);
    System.out.println("resource4!=null: "+Boolean.valueOf(resource4!=null));
    System.out.println(uriInfo.getPath());
   System.out.println(path);
    return resource4;
  }

}
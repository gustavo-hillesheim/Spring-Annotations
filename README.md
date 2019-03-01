# Spring-Annotations

Spring Annotations (also reffered to as HAS) is a Java library for generating controllers, services and repositories for CRUD operations and Authentication of Spring Boot applications. 

## @CRUD and @Endpoint Example
```
package com.example.has;

@Entity
@CRUD
public class Example {
  
  @Id
  private Long id;
  
  @Endpoint
  private String username;
  
  [Other fields]
  
  [Getters and Setters]
}
```

  The pre-requisites for running this piece of code without errors are:
  
    * The class must be annotated with javax.persistence.Entity
    * The class must have a field annotated iwth javax.persistence.Id
    
  This is all you need to generate all 4 CRUD operations for this entity.
  The @Endpoint annotation at `username` will create an endpoint on the controller for getting specifically this field's value.
  
### Customizations
  
  For @CRUD you can specify four different configurations that will be used to generate the classes:
    
  * `endpoint` will specify which will be the endpoint of the CRUD operations for this entity. If not set the class name converted to lower case will be used as default value.
  * `name` will specify the name used for generating the classes (Generated classes names follow this pattern: prefix + name + suffix, all of these being able to be customized, I will explain how to customize prefix and suffix at @HASConfiguration topic). If not set the class name will be used as default value.
  * `pagination` will define if the `getAll` method will be able to paginate or not the response. If not set true will be used as default value.
  * `filter` this configuration's value is another annotation (`@Filter`) of this library. This one have only two configurations:
    * `fields` will specify which fields will be used for filtering. if not set the `getAll` method won't be filterable.
    * `likeType` will determine how to treat String comparison on query using `like` operator, the four possible values are: START, END, BOTH and NONE. if not set BOTH will be used as default value.
        
  For @Endpoint you can only specify it's value. If not set the field's name to lower case will be used as default value.
  
## @Authentication Example
```
package com.example.has;

@Authentication(secret = "MySecret", encoder = MyEncoder.class)
public class Example {
}
```

  The pre-requistes for running this piece of code without errors are:
  
   * Creating an encoder class that extends `org.springframework.security.crypto.password.PasswordEncoder`, in this example named `MyEncoder`.
   * Creating an implementation of `org.springframework.security.core.userdetails.UserDetailsService` using Spring Boot conventions.
    
  This is all you need to generate a JWT Authenticated Spring Boot Application.
  A few details before we go into customizations: 
  
  * If you configure @Authentication this way your whole application will need authentication to be accessed, except for `/login`.
  * The default return for `/login` is a JSON containing:
    * `timestamp`: the date of the request in milliseconds.
    * `status`: the status of the response (OK or UNAUTHORIZED).
    * `token` or `error`: the token if the credentials were valid, otherwise will be "Could not  authenticate".
    * `message`: "Authenticated successfully" if the credentials were valid, otherwise will be the error raised.
  
### Customizations

  

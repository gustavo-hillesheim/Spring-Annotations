# Spring-Annotations

Spring Annotations (also reffered to as HAS) is a Java library for generating controllers, services and repositories for CRUD and Authentication operations of Spring Boot applications. 

## @CRUD and @Endpoint Example
```
package com.example.has;

@Entity
@CRUD
public class Test {
  
  @Id
  private Long id;
  
  @Endpoint
  private String username;
  
  [Other fields]
  
  [Getters and Setters]
}
```

  The only pre-requisites for running this code without errors are:
  
    * The class must be annotated with javax.persistence.Entity
    * The class must have a field annotated iwth javax.persistence.Id
    
  This is all you need to do to generate all 4 CRUD operations for this entity.
  The @Endpoint annotation at `username` will create an endpoint on the controller for getting specifically this field's value.
  
Customizations
  
  For @CRUD you can specify four different configurations that will be used to generate the classes:
    
  * `endpoint` will specify which will be the endpoint of the CRUD operations for this entity. If not set the class name converted to lower case will be used as default value.
  * `name` will specify the name used for generating the classes (Generated classes names follow this pattern: prefix + name + suffix, all of these being able to be customized, I will explain how to customize prefix and suffix at @HASConfiguration topic). If not set the class name will be used as default value.
  * `pagination` will define if the `getAll` method will be able to paginate or not the response. If not set true will be used as default value.
  * `filter` this configuration's value is another annotation (`@Filter`) of this library. This one have only two configurations:
    * `fields` will specify which fields will be used for filtering. if not set the `getAll` method won't be filterable.
    * `likeType` will determine how to treat String comparison on query using `like` operator, the four possible values are: START, END, BOTH and NONE.
        
  For @Endpoint you can only specify it's value.
  
## @Authentication Example

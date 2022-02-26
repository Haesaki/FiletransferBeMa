# Spring

1. 如何使用application.properties

   ```java
   // 1. 利用Environment对象注入Rest Controller或者Service类
   @Autowired
   private Environment env;
   String keyValue = env.getProperty(key);
   // 2. @Value 注解读取属性 后面的为default值 如果没有读取到的话
    @Value("${sc4u.account.directory-default-path:/sc4u/user/}")
    private String directoryPath;
   // 3. 利用@ConfigurationProperties() 读取应用程序属性
   @Component
   @ConfigurationProperties("app")
   public class AppProperties {
       private String title;
       private String description;
   }
   ```
   
   

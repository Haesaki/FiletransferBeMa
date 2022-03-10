# Spring

1. **如何使用application.properties**

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

2. **Model 和 Session
   有什么区别么 [StackOverflow](https://stackoverflow.com/questions/60331872/what-is-the-difference-between-the-model-and-session-in-spring)**

   Model和Session 都是存储信息的地方，生命周期不一样

    - Model：Request级别，Model的数据只能在该页面使用

      Spring中数据模型除了Model外还有ModelMap、RedirectAttributesModelMap、ModelAndView。后端将数据注入model中，传给前端取出。

      Model、ModelMap、RedirectAttributesModelMap无需手动创建，作为controller函数形参传入即可，返回URL时Spring自动将模型对象传到前端页面，ModelAndView需要在controller函数中手动创建，然后返回一个ModelAndView对象（包含视图和模型数据）。

    - Session：会话级别，在服务器和客户端交互的过程中一直保留

      由于HTTP协议是无状态的协议，所以服务端需要记录用户的状态时，就需要用某种机制来识具体的用户，这个机制就是Session。**将 user agent 和 server 之间一对一的交互，抽象为“会话”。**

      典型的场景比如购物车，当你点击下单按钮时，由于HTTP协议无状态，所以并不知道是哪个用户操作的，所以服务端要为特定的用户创建了特定的Session，用用于标识这个用户，并且跟踪用户，这样才知道购物车里面有几本书。

      这个Session是保存在服务端的，有一个唯一标识。在服务端保存Session的方法很多，内存、数据库、文件都有。集群的时候也要考虑Session的转移，在大型的网站，一般会有专门的Session服务器集群，用来保存用户会话，这个时候
      Session 信息都是放在内存的，使用一些缓存服务比如Memcached之类的来放 Session。

   思考一下服务端如何识别特定的客户？这个时候Cookie就登场了。

    - 每次HTTP请求的时候，客户端都会发送相应的Cookie信息到服务端。实际上大多数的应用都是用 Cookie 来实现Session跟踪的，第一次创建Session的时候，服务端会在HTTP协议中告诉客户端，需要在 Cookie
      里面记录一个Session ID，以后每次请求把这个会话ID发送到服务器，我就知道你是谁了。有人问，如果客户端的浏览器禁用了 Cookie
      怎么办？一般这种情况下，会使用一种叫做URL重写的技术来进行会话跟踪，即每次HTTP交互，URL后面都会被附加上一个诸如 sid=xxxxx 这样的参数，服务端据此来识别用户。

    - Cookie其实还可以用在一些方便用户的场景下，设想你某次登陆过一个网站，下次登录的时候不想再次输入账号了，怎么办？

      这个信息可以写到Cookie里面，访问网站的时候，网站页面的脚本可以读取这个信息，就自动帮你把用户名给填了，能够方便一下用户。这也是Cookie名称的由来，给用户的一点甜头。

    - **session 在服务器端，cookie 在客户端（浏览器）。**

      **session用来跟踪会话，cookie目的可以跟踪会话，也可以保存用户喜好或者保存用户名密码。**

3. 在spring 重定向的时候，保留在该controlle函数传入的attribute
   https://blog.csdn.net/yinbucheng/article/details/54097155

```java
   public String red(RedirectAttributes attributes){
           attributes.addFlashAttribute("test","hello");
           return "redirect:/test/test2";
           }
   ```

## 其他

[Regular Expressions For Regular Fork](https://refrf.dev/)

[Spring: Model 和 Session的区别](https://blog.csdn.net/LOG_IN_ME/article/details/106326636#:~:text=%E3%80%90%E9%80%9A%E4%BF%97%E3%80%91,%E8%83%BD%E5%9C%A8%E8%AF%A5%E9%A1%B5%E9%9D%A2%E4%BD%BF%E7%94%A8%E3%80%82) 

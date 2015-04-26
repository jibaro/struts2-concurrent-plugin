## struts2-bigpipe-plugin
### 简介
<hr>
   struts2-bigpipe-plugin是struts2的一款插件，插件功能是把一个页面分成很多个模块。每个模块执行业务代码并渲染有三种模式:
	<ul>
		<li>串行模式
		<li>服务器端并行模式
		<li>bigpipe模式
	</ul>
	<hr>
	<b>下面具体介绍一下每个模式</b>
	<p>
		1.串行模式是每个模块顺序执行
	</p>
	<p>
	  2.服务器端并行模式是每个模块并行执行（多线程），当所有的模块执行完之后拼成一个完整的html后再响应给客户端浏览器。
	</p>
	<p>
		3.bigpipe模式是facebook发明的一种浏览器-服务器之间的并发模式。跟服务器端并行模式一样，bigpipe模式也是模块并行执行，不同的是，bigpipe模式在每个模块执行完之后，马上给客户端浏览器响应。好处在于客户端能够立刻看到响应，提升用户体验。这个过程让我们不经想到Ajax，跟ajax区别在于bigpipe只有一个连接，一方面能够节约服务器连接资源，另一方面节省tcp连接的时间。
	</p>
### 使用文档
<hr>
<b>1.增加插件pom依赖，或者手动导入插件包</b></br>
<b>2.编写普通的struts2 Action组件</b>
<p>
	在插件的模型里面，action组件不再是执行业务逻辑代码的地方，理论上只用于为每个子模块创建参数上下文。具体执行业务逻辑由Pipe组件完成。
</p>
<pre>
<code>
	@View(ftlPath = "/index.ftl", type = ExecuteType.BIGPIPE)
public class IndexAction extends ActionSupport {

    @Param
    String name;
    @Autowired
    @Param
    public User user;

    @Override
    public String execute() throws Exception {
    //do prepare pipe execute context at here
        return "pipe";
    }
</code>
	</pre>
<p>
	<b>@View</b> 注释是插件里面的一个重要注释，在Action组件里面@View的功能有两个：<br>
	1.指定对应的页面的整体freemarker文件路径</br>
	2.指定此页面的模块的执行方式，枚举类型ExecuteType.SYNC、ExecuteType.CONCURRENT、ExecuteType.BIGPIPE分别对应上一节介绍的三种模式.模式切换只有这里需要改动，其他地方无需任何改动.
</p>
<p>
	<b>@Param</b> 指定的属性表示此参数会被此页面模块使用的到，也就是说会被注入到模块里相应的属性里。
</p>
<p>
其他地方和普通的struts2 action没有任何区别。
</p>
<br>
<b>3.编写模块组件</b>
<p>示例<p>
<pre><code>
@View(ftlPath = "demo/one.ftl", weight = Weight.HEIGHT, key = "pipeone")
public class PipeOne implements Pipe {
    @Param
    private String name;
    @Getter
    @Autowired
    private User user;
    @Getter
    private int age;
    @Getter
    private int time;
    @Override
    public void execute() {
        //do business logic at here
    }
}
</code></pre>

<p>示例说明<p>
<p>Pipe组件对应页面的一个模块，Pipe组件需要实现Pipe接口。Pipe接口定义只有一个execute()方法。</p>
<p>
	<b>@View</b> 注释有三个功能<br>
	<ul>
		<li>指定freemarker文件地址
		<li>指定在页面级freemarker中引用模块的key，默认是ftl文件名（不包括后缀）。例如上面示例中的模块key为one
		<li>指定此模块的权重。这里解释一下权重的概念：一个页面可能某些模块具有高权重，比如base info信息在整个页面必须首先渲染出来，而有些模块得在页面其他部分都渲染完之后在渲染也就是优先级是最低的。插件实现了三种优先级Weight.HEIGHT、Weight.NORMALL、Weight.LOW，默认是Normal。每个级别里面的模块之间没有优先级关系。Weight.HEIGHT模块可以确保在其他两个级别之前渲染。申明一点这里指的渲染是以用户视觉看到的来说。
	</ul>
</p>	
<p>
	<b>@Param</b> 会自动被注入的参数，参数值来源就是action中定义的带@Param参数。示例中@Getter是lombok插件的一个功能，一个参数被注释为@Getter，在编译的时候自动补全参数的getter()方法。模块中的含有getter方法和param注释的属性，都可以在ftl文件中引用。换句话说@getter参数是模块的输出，对应的@Param参数是模块的输入。
</p>
<p>
	另外考虑到在模块中使用到HttpServletRequest、HttpServletResponse、ServletContext、获取cookie值等是很常用的需求。插件考虑到这一点提供了PipeSupport类，模块pipe通过继承pipeSupport可以很方便的获得这些功能的支持，类似于ActionSupport。
</p>
<p><b>4.struts.xml配置</b></p>
<p>
	示例：
</p>
         
	<struts>
    <constant name="struts.objectFactory"
              value="org.apache.struts2.spring.StrutsSpringObjectFactory"/>
    <constant name="struts.concurrent.plugin.downgrade"
              value="MyPipeDowngrading"/>
    <constant name="struts.devMode" value="true"/>
    <package name="default" extends="struts-default" namespace="/">
        <result-types>
            <result-type name="bigpipe" class="org.le.view.BigPipeResult">
            </result-type>
        </result-types>
        <action name="hello" class="action.IndexAction">
            <result name="success" type="freemarker">/index.ftl</result>
            <result name="pipe" type="bigpipe">Pipes.PipeOne,Pipes.PipeTwo,
                Pipes.PipeThree,Pipes.PipeFour
            </result>
        </action>
    </package>
	</struts>
	
<p>
	在<result-types>中定义了一种新的resultType类型为bigpipe，然后在Action result定义中使用。示例中
	
		<result name="pipe" type="bigpipe">Pipes.PipeOne,Pipes.PipeTwo,
                Pipes.PipeThree,Pipes.PipeFour
        </result>  
        
result中定义了四个pipe，pipe的值为Pipe的全名。          

</p>
<p><b>5. action对应的ftl</b></p>

	<html>
	<head>
    	<title>struts2-bigpipe-plugin</title>
    	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	</head>
	<body>
	<h1>${name}</h1>
	<table border="1">
    	<tr>
        	<td width="25%"><div id="one">${one}</div></td>
	        <td width="25%"><div id="two">${two}</div></td>
    	    <td width="25%"><div id="three">${three}</div></td>
        	<td width="25%"><div id="four">${four}</div></td>
	    </tr>
	</table>
	<#--</body>-->
	<#--</html>-->
<p>
	示例中定义了四个模块，${key} 中定义的为pipe模块的key。
	
		<div id="one">
		
如果是bigpipe模式，则需要定义如上的div，id的值为pipe的key值。另外如果是bigpipe模式，则要去掉html中最后的< /body>和< /html>。框架会在全部模块都渲染完之后自动闭合html。
</p>
<p><b>6. 框架扩展功能-模块降级</b></p>
<p>
	由于框架把页面分成很多个模块，所以当一个模块执行抛出Exception的时候，并不会影响其他模块的执行，也不会导致整个页面请求的50X响应。在调试阶段一个页面的某个模块挂掉之后，会在页面的相应的位置显示异常堆栈信息。
</p>
<p>
	当然在线上环境的时候，肯定不容许页面某个地方显示异常信息，并且也不应该由于页面的某个模块挂掉导致整个页面挂掉。我相信大部分正式的网站都会有页面降级功能，但是毕竟页面降级代价高。为止，框架提供了一个接口用于模块降级。
</p>
	
	public class MyPipeDowngrading implements PipeDowngradeBackup{
    	private static Map<String , Object> backup = new HashMap<String, Object>();

    	@Override
    	public void backup(PipeProxy pipe, Object pipeResult) {
    	    backup.put(pipe.getFtlPath(), pipeResult);
	    }

    	@Override
	    public Object downgrade(PipeProxy pipe) {
    	    return backup.get(pipe.getFtlPath());
	    }
	}
<p>
	一共两个接口，backup(PipeProxy pipe, Object pipeResult) 每当模块渲染完之后都会调用此方法，用于备份，具体的降级策略由项目自己设定。当模块抛出异常时，则会调用Object downgrade(PipeProxy pipe)接口获取模块html，实现降级。示例中简单的用一个Map实现降级。
</p>
<p>
在非devMode模式下，模块执行抛异常之后，如果没有设置降级或者降级为命中则不显示此模块，这样对用户来说可能不感知的。
</p>
<p>
在struts.xml配置以下常量即可注册降级器

	<constant name="struts.concurrent.plugin.downgrade"value="MyPipeDowngrading"/>

</p>

### 结语
<hr>
此插件功能强大，使用起来十分方便，而且能够大幅度提高性能、用户体验，特别是bigpipe模式。并且对于页面开发可以起到页面代码解耦的功能。市面上有很多后端模块并行执行的框架，但大部分都是在struts层之下做的，多一层就多了很多的复杂度。我个人觉得这些功能应该在MVC层做的，也就是struts负责的。读者看完上面的教程之后，应该也会发现用一个struts插件实现这些功能是多么自然的事情。
<p>
插件还有很多不完善的地方，特别是bigpipe js部分。由于本人对前端知识不是特别了解，所以这部分还有很多改善的空间。也借此希望如果有兴趣的TX，一块开发完善此项目。
</p>



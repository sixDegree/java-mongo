package com.cj.mongo;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import spark.ModelAndView;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

// http://sparkjava.com
public class SparkTest {
	
	/*
	0: static file
	$ curl -i http://localhost:4567/1.txt
	HTTP/1.1 200 OK
	Date: Sat, 24 Nov 2018 13:05:19 GMT
	Content-Type: text/plain
	Content-Length: 12
	Server: Jetty(9.4.12.v20180830)
	
	Hello World!

	1:
	$ curl -i http://localhost:4567/hello/Tom
	HTTP/1.1 200 OK
	Date: Sat, 24 Nov 2018 12:28:07 GMT
	Content-Type: text/html;charset=utf-8
	Transfer-Encoding: chunked
	Server: Jetty(9.4.12.v20180830)
	
	Hello: Tom
	
	2:
	$ curl -i http://localhost:4567/ftl/Tom
	HTTP/1.1 200 OK
	Date: Sat, 24 Nov 2018 12:37:59 GMT
	Content-Type: text/html;charset=utf-8
	Transfer-Encoding: chunked
	Server: Jetty(9.4.12.v20180830)
	
	<html>
		<head>
			<title>Welcome!</title>
		</head>
		<body>
			<h1>Hello Tom</h1>
		</body>
	</html>
	
	3:
	$ curl -i http://localhost:4567/sparkftl/Tom
	HTTP/1.1 200 OK
	Date: Sat, 24 Nov 2018 12:37:59 GMT
	Content-Type: text/html;charset=utf-8
	Transfer-Encoding: chunked
	Server: Jetty(9.4.12.v20180830)
	
	<html>
		<head>
			<title>Welcome!</title>
		</head>
		<body>
			<h1>Hello Tom</h1>
		</body>
	</html>
	
	4:
	$ curl -H "Accept:applAccept:application/json" -i http://localhost:4567/type/Lucy
	HTTP/1.1 200 OK
	Date: Sat, 24 Nov 2018 13:10:06 GMT
	Content-Type: application/json
	Transfer-Encoding: chunked
	Server: Jetty(9.4.12.v20180830)
	
	{"name":"Lucy"}
	
	5:
	$curl -i http://localhost:4567/json/Tom
	HTTP/1.1 200 OK
	Date: Sat, 24 Nov 2018 13:05:11 GMT
	Content-Type: text/html;charset=utf-8
	Transfer-Encoding: chunked
	Server: Jetty(9.4.12.v20180830)

	{"name":"Tom"}
	*/
	public static void main(String[] args) {
		
		// static file
		Spark.staticFileLocation("/public"); // src/test/resource/public
		
		// 1: hello world
		Spark.get("/hello/:name", (request, response) -> {
            return "Hello: " + request.params(":name");
        });
		
		// 2: freemarker
		Configuration config = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
		config.setClassForTemplateLoading(SparkTest.class, "/");
		
		Spark.get("/ftl/:name", (req,res)->{
			StringWriter writer = new StringWriter();
			try {
				Template helloTemplate=config.getTemplate("hello.ftl");
				Map<String,Object> helloMap=new HashMap<String,Object>();
				helloMap.put("name",req.params(":name"));
				helloTemplate.process(helloMap, writer);
				System.out.println(writer);
			}  catch (Exception e) {
				Spark.halt(500);
				e.printStackTrace();
			}
			return writer;
		});
		
		// 3: freemarker
		Configuration freeMarkerConfiguration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
		FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine(freeMarkerConfiguration);
		freeMarkerConfiguration.setTemplateLoader(new ClassTemplateLoader(SparkTest.class, "/"));
		
		Spark.get("/sparkftl/:name",(req,res)->{
			res.status(200);
			res.type("text/html");
			Map<String,Object> helloMap=new HashMap<String,Object>();
			helloMap.put("name",req.params(":name"));
			return freeMarkerEngine.render(new ModelAndView(helloMap, "hello.ftl"));
		});
		
		// 4: accept
		Spark.get("/type/:name",(req,res)->{
			String accept=req.headers("Accept");
			System.out.println(accept);
			if(accept != null && accept.contains("application/json")){
				res.type("application/json");
				Map<String,Object> helloMap=new HashMap<String,Object>();
				helloMap.put("name",req.params(":name"));
				Gson gson = new Gson();
				return gson.toJson(helloMap);
			}else{
				return "Hello: " + req.params(":name");
			}
		});
		
		// 5: json
		Gson gson = new Gson();
		Spark.get("/json/:name", (req,res)->{
			Map<String,Object> helloMap=new HashMap<String,Object>();
			helloMap.put("name",req.params(":name"));
			return helloMap;
		},gson::toJson);
	
		// 6: path,before,after
		// curl -X POST -i http://localhost:4567/test/Tom
		// curl -i http://localhost:4567/test/Lucy
		Spark.path("/test", ()->{
			Spark.before("/Tom",(req,res)->{
				System.out.println("PreFilter...");
			});
			
			Spark.get("/:name", (req,res)->{
				return "Hello "+req.params("name");
			});
			
			Spark.post("/:name", (req,res)->{
				Map<String,Object> helloMap=new HashMap<String,Object>();
				helloMap.put("name",req.params(":name"));
				return helloMap;
			},gson::toJson);
			
			Spark.after("/*",(req,res)->{
				System.out.println("PostFilter...");
			});
			
		});
	}
}

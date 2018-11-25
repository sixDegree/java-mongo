package com.cj.mongo;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class FreemarkerTest {

	@Test
	public void HelloWorld(){
		Configuration config = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
		config.setClassForTemplateLoading(FreemarkerTest.class, "/");
		
		try {
			Template helloTemplate=config.getTemplate("hello.ftl");
			Map<String,Object> helloMap=new HashMap<String,Object>();
			helloMap.put("name","Tom");
			StringWriter writer = new StringWriter();
			helloTemplate.process(helloMap, writer);
			System.out.println(writer);
		}  catch (Exception e) {
			e.printStackTrace();
		}
	}
}

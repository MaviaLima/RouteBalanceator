package com.camel;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.UseAdviceWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import com.amazonaws.services.s3.AmazonS3;
import com.camel.MicroServiceCamelApplication;
import com.camel.services.SqsAWSService;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = MicroServiceCamelApplication.class)
@TestPropertySource(properties = {"folderPathIn=//target/inbox", "folderPathOut=//target/outbox", "rangeLines=2"})
@UseAdviceWith
public class RouteBalanceatorTests {
	@Autowired
    private CamelContext context;
 
    @Autowired
    private ProducerTemplate template;
 
    @EndpointInject(uri = "mock:s3")
    private MockEndpoint mocks3;
 
    @MockBean
    AmazonS3 s3Client;
 
    @MockBean
    private SqsAWSService sqsService;
    
    @Test
    public void onContextUpReturnS3Client() {
    	assertNotNull(s3Client);
    }
    
    @BeforeEach
    public void setUp() throws Exception {
    	deleteDirectory("target/outbox");
    	deleteDirectory("target/inbox");
    }
    

    private void deleteDirectory(String strPath) {
    	File dir = new File(strPath);
    	
    	for (File file: dir.listFiles()) {
    		if (!file.isDirectory()) {
    			if (file.delete()) {
    				System.out.println("file deleted successfully");
    			}else {
    				System.out.println("Failed to delete the file");
    			}
    		}
    		
    	}
    }
    
    @Test
    public void testMoveFile() throws Exception {
    	template.sendBodyAndHeader("file://target/inbox", "Hello World", Exchange.FILE_NAME, "hello.csv");
    	
    	Thread.sleep(2000);
    	
    	File target = new File("target/outbox");
    	assertTrue("File not moved", target.exists());
    }

    @Test
    public void testRoute() throws Exception {
    	
    	FluentProducerTemplate fluentTemplate = context.createFluentProducerTemplate();
    	Exchange exchange = fluentTemplate.withProcessor(e->{
    		e.getIn().setHeader(Exchange.FILE_NAME,"helloword.csv");
    		e.getIn().setBody("Hello World/n"+
    		"Hi linha2/n"+
    		"Hi linha 3/n"+
    		"Hi linha 4/n");
    	}).to("file://target/inbox")
    			.send();
    	
    	Object resposta = exchange.getIn().getBody();
    	assertNotNull(resposta);
    	assertTrue(resposta.toString().toLowerCase().contains("hello world"));
    }
 
    
    @Test
    public void WhenSendMessageToSQS_A() throws Exception {
       String resultExpected = "test";
 
       when(sqsService.getQueueUrl(Mockito.anyString())).thenReturn("url");
       when(sqsService.sendMessageToQueue(Mockito.anyString(), Mockito.anyString())).thenReturn(resultExpected);
 
       FluentProducerTemplate fluentTemplate = context.createFluentProducerTemplate();
       Exchange exchange = (Exchange) fluentTemplate.withProcessor(e-> {
           e.getIn().setHeader(Exchange.FILE_NAME, "helloworld.csv");            
       }).to("direct:a");
 
       String resultMessage = exchange.getIn().getBody(String.class);
       assertNotNull(resultMessage);
       assertTrue(resultExpected.equalsIgnoreCase(resultMessage));
   }

    
    @Test
    public void WhenSendMessageToSQS_B() throws Exception {
       String resultExpected = "test";
 
       when(sqsService.getQueueUrl(Mockito.anyString())).thenReturn("url");
       when(sqsService.sendMessageToQueue(Mockito.anyString(), Mockito.anyString())).thenReturn(resultExpected);
 
       FluentProducerTemplate fluentTemplate = context.createFluentProducerTemplate();
       Exchange exchange = (Exchange) fluentTemplate.withProcessor(e-> {
           e.getIn().setHeader(Exchange.FILE_NAME, "helloworld.csv");            
       }).to("direct:b");
 
       String resultMessage = exchange.getIn().getBody(String.class);
       assertNotNull(resultMessage);
       assertTrue(resultExpected.equalsIgnoreCase(resultMessage));
   }

    
    @Test
    public void WhenSendMessageToSQS_C() throws Exception {
       String resultExpected = "test";
 
       when(sqsService.getQueueUrl(Mockito.anyString())).thenReturn("url");
       when(sqsService.sendMessageToQueue(Mockito.anyString(), Mockito.anyString())).thenReturn(resultExpected);
 
       FluentProducerTemplate fluentTemplate = context.createFluentProducerTemplate();
       Exchange exchange = (Exchange) fluentTemplate.withProcessor(e-> {
           e.getIn().setHeader(Exchange.FILE_NAME, "helloworld.csv");            
       }).to("direct:c");
 
       String resultMessage = exchange.getIn().getBody(String.class);
       assertNotNull(resultMessage);
       assertTrue(resultExpected.equalsIgnoreCase(resultMessage));
   }

}

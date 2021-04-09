package com.camel;

import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.s3.S3Constants;
import org.apache.camel.impl.SimpleRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.camel.services.SqsAWSService;

@Component
public class RouteBalanceator extends RouteBuilder {

	@Autowired
	private SqsAWSService sqsService;

	@Value("${rangeLines}")
	private Integer rangeLines;

	@Value("${awsRegion}")
	private String region;

	String queueURL;

	@Value("${outBucket}")
	private String outBucket;

	@Value("${outQueue1}")
	private String outQueue1;

	@Value("${outQueue2}")
	private String outQueue2;

	@Value("${outQueue3}")
	private String outQueue3;

	@Value("${folderPathIn}")
	private String SOURCE_FOLDER;

	@Value("${folderPathOut}")
	private String DESTINATION_FOLDER;

	private AmazonS3 s3Client;
	SimpleRegistry simpleRegistry;

	@PostConstruct
	private void init() {
		s3Client = AmazonS3ClientBuilder.standard().withRegion(region).build();

		simpleRegistry = new SimpleRegistry();
		simpleRegistry.put("s3Client", s3Client);
	}

	@Bean(name = "s3Client")
	public AmazonS3 getAmazonS3Client() {
		return s3Client;
	}

	@Override
	    public void configure() throws Exception {

	        onException(Exception.class)
	            .routeId("camelException")
	            .log(LoggingLevel.ERROR, "Exception in Camel")
	            .handled(true)
	            .end();

	        from("file:" + SOURCE_FOLDER +"?delete=true")
	            .routeId("camelSplitFile")
	            .log("Start processing ...")
	            .multicast()
	            .marshal()
	            .string("UTF-8")
	            .split()
	            .tokenize("\n", rangeLines)
	            .process(e -> e.getIn().setHeader("uid", UUID.randomUUID().toString()))
	            	.setHeader(Exchange.FILE_NAME, 
	                   simple("${file:name.noext}-${in.header.uid}-${data:now:yyyyMMddHHmmssSSS}.${file:ext}"))
	            .to("file://"+ DESTINATION_FOLDER );

	        from("file://"+ DESTINATION_FOLDER )
	            .routeId("camelSendFileToS3")
	            .log("Start sending index ${header.CamelSplitIndex} splited file to s3 ...")
	            .setHeader(S3Constants.CONTENT_LENGTH, simple("${in.header.CamelFileLength}"))
	            .setHeader(S3Constants.KEY, simple("${in.header.CamelFileNameOnly}"))
	            .to("aws-s3://{{outBucket}}?deleteAfterWrite=false=false&AmazonS3Client=#s3Client")
	            .log("Start direct message to SQS by roundRobin ...")
	            .loadBalance().roundRobin().to("direct:a", "direct:b", "direct:c")
	            .end();


	        from("direct:a").routeId("camelSQSRouteA")
	            .log("Starting send ${in.reader.CamelFileName} to {{outQueue1}} queue")
	            .process(new Processor(){
	                public void process(Exchange exchange) throws Exception {
	                    processWithQueue(exchange, outQueue1);
	                }
	            })
	            .log("Filename succesfully send to {{outQueue1}} queue");

	        from("direct:b").routeId("camelSQSRouteB")
	            .log("Starting send ${in.reader.CamelFileName} to {{outQueue2}} queue")
	            .process(new Processor(){
	                public void process(Exchange exchange) throws Exception {
	                    processWithQueue(exchange, outQueue2);
	                }
	            })
	            .log("Filename succesfully send to {{outQueue2}} queue");

	            from("direct:c").routeId("camelSQSRouteC")
	            .log("Starting send ${in.reader.CamelFileName} to {{outQueue3}} queue")
	            .process(new Processor() {					
	                public void process(Exchange exchange) throws Exception {
	                    processWithQueue(exchange, outQueue3);
	                }
	            })
	            .log("Filename succesfully send to {{outQueue3}} queue");
	    }

	public void processWithQueue(Exchange exchange, String nameQueue) throws Exception {
		Map<String, Object> headers = exchange.getIn().getHeaders();
		String fileName = (String) headers.get("CamelFileName");
		String result = sqsService.sendMenssageToQueue(fileName, sqsService.getQueueURL(nameQueue));
		exchange.getOut().setBody(result);
	}
}

package com.camel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

@Configuration
public class AmazonSQSConfig {

	@Bean
	public AmazonSQS retClientSQS	{
	    return AmazonSQSClientBuilder
	            .standard()
	            .withRegion(Regions.SA_EAST_1)
	            .build();
	    }
}

package com.camel.helpers;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camel.RouteBalanceator;

public class RegisterLog implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(RouteBalanceator.class);

	public void process(Exchange exchange) throws Exception {
		String routeId = exchange.getFromRouteId();
		
		switch(routeId) {
		case "camelSplitFile":
			logger.info(routeId + ": Start processing ...");
			break;
			
		case "camelSendFileToS3":
			logger.info(routeId + ": Starting send file to s3 and round robin ...");
			break;
		case "camelSQSRouteA":
			logger.info(routeId + ": Starting send ${in.header.CamelFileName} to {{smsQueue1}} queue");
			break;
		case "camelSQSRouteB":
			logger.info(routeId + ": Starting send ${in.header.CamelFileName} to {{smsQueue2}} queue");
			break;
		case "camelSQSRouteC":
			logger.info(routeId + ": Starting send ${in.header.CamelFileName} to {{smsQueue3}} queue");
			break;
		case "camelException":
			logger.info("CamelProcessing BatchDecompor - Default message");
			break;			
		}		
	}
	
}

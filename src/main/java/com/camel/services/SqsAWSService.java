package com.camel.services;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

@Service
public class SqsAWSService {

	@Autowired
	private AmazonSQS sqsClient;
	
	public String getQueueUrl(String queueName) {
		return sqsClient.getQueueUrl(queueName).getQueueUrl();
	}
	
	public String sendMessageToQueue(String message, String queueUrl) throws Exception {
		SendMessageRequest sendMessageRequest = new SendMessageRequest()
				.withQueueUrl(queueUrl);
		
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		sendMessageRequest.setMessageBody(message);
		sendMessageRequest.setMessageGroupId(timeStamp);
		
		SendMessageResult sendMessageResult = sqsClient.sendMessage(sendMessageRequest);
		
		return sendMessageResult.getMessageId();
		
	}
	
}

package com.camel.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.camel.services.SqsAWSService;

@RunWith(JUnitPlatform.class)
@ExtendWith(MockitoExtension.class)
public class SqsAWSServiceTest {
	
	@InjectMocks
	private SqsAWSService sqsService;
	
	@Mock
	private AmazonSQS sqsClient;
	
	@Test
	public void returnValidUrl() {
		GetQueueUrlResult getQueueUrlResult = mock(GetQueueUrlResult.class);
		when(getQueueUrlResult.getQueueUrl()).thenReturn("url");
		when(sqsClient.getQueueUrl(Mockito.anyString())).thenReturn(getQueueUrlResult);
		
		assertEquals("url", sqsService.getQueueUrl("queue1"));
	}
	
	@Test
	public void returnMessageIdValid()throws Exception {
		
		SendMessageResult sendMessageResult = mock(SendMessageResult.class);
		when(sendMessageResult.getMessageId()).thenReturn("messageId");
		when(sqsClient.sendMessage((SendMessageRequest) Mockito.any())).thenReturn(sendMessageResult);
		
		String resultMessage = sqsService.sendMessageToQueue("message", "queue");
		
		assertEquals("messageId", resultMessage);
		
	}

}

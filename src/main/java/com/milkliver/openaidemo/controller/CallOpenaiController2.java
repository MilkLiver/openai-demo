package com.milkliver.openaidemo.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.messages.Message;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.runs.CreateThreadAndRunRequest;
import com.theokanning.openai.runs.Run;
import com.theokanning.openai.runs.RunCreateRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.ThreadRequest;
import com.theokanning.openai.threads.Thread;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@Controller
public class CallOpenaiController2 {
	private static final Logger log = LoggerFactory.getLogger(CallOpenaiController2.class);

	private static String token="TOKEN";
	
	@ResponseBody
	@RequestMapping(value = "/openai_msg4")
	private String openai_msg4(@RequestBody String message) {
		System.out.println(message);

		Duration duration=Duration.ofSeconds(10);
		
		ObjectMapper mapper = OpenAiService.defaultObjectMapper();
		OkHttpClient client = OpenAiService.defaultClient(token, duration)
		        .newBuilder()
//		        .interceptor(HttpLoggingInterceptor())
		        .build();
		Retrofit retrofit = OpenAiService.defaultRetrofit(client, mapper);
		OpenAiApi api = retrofit.create(OpenAiApi.class);
//		api.createThreadAndRun(null);
		
		OpenAiService service = new OpenAiService("TOKEN");
		
		MessageRequest mr1=MessageRequest.builder()
				.role("user")
				.content("這網銀登入不了")
				.build();
		
		List<MessageRequest> messages=new ArrayList<MessageRequest>();
		messages.add(mr1);
		
		ThreadRequest tr=ThreadRequest.builder().messages(messages).build();
		Thread th=service.createThread(tr);
		
		RunCreateRequest runcreq=RunCreateRequest.builder()
				.assistantId("asst_BPtZedvH22wo4mpKShG7qRJx")
				.model("gpt-4-turbo")
				.build();
		
		Run run=service.createRun(th.getId(), runcreq);
		
		System.out.println(run.getCompletedAt());
		
		Thread resth = service.retrieveThread(th.getId());
		while(run.getCompletedAt()==null) {
			try {
				java.lang.Thread.sleep(1000);
				run=service.retrieveRun(th.getId(), run.getId());
				System.out.println(run.getCompletedAt());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		List<Message> thMsgsList = service.listMessages(resth.getId()).getData();
		System.out.println(service.listMessages(resth.getId()).toString());
		System.out.println(thMsgsList.get(0).getContent());
		System.out.println(thMsgsList.get(thMsgsList.size()-1).getContent());
//		thMsgsList.get(0).getContent()
		System.out.println(service.listMessages(resth.getId()).toString());
		Run respRun=service.retrieveRun(resth.getId(), run.getId());
		
		return respRun.toString();
//		return "";
	}
	
}

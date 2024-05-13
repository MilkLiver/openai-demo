package com.milkliver.openaidemo.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletion;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage.Role;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CallOpenaiController {
	private static final Logger log = LoggerFactory.getLogger(CallOpenaiController.class);

//	private final OpenAiChatClient chatClient = new OpenAiChatClient(
//			new OpenAiApi("TOKEN"));

	@ResponseBody
	@RequestMapping(value = "/test")
	private String test() {
		return "testOwO";
	}

	@ResponseBody
	@RequestMapping(value = "/openai_msg")
	private String openai_msg(@RequestBody String message) {
		System.out.println(message);

//		StringBuilder execRes = new StringBuilder();
//		String execLine;
//		StringBuilder execStrSb = new StringBuilder();
//		while ((execLine = request.getReader().readLine()) != null) {
//			execStrSb.append(execLine);
//		}

		OpenAiApi openAiApi = new OpenAiApi("Token");

		OpenAiChatClient chatClient = new OpenAiChatClient(openAiApi);

		ChatResponse response = chatClient.call(new Prompt(message));
		return response.toString();

//		OpenAiChatOptions.builder()

//		ChatResponse response = chatClient.call(
//			    new Prompt(
//			        "Generate the names of 5 famous pirates.",
//			        OpenAiChatOptions.builder()
//			            .withModel("gpt-4")
//			            .withTemperature((float) 0.4)
//			        .build()
//			    ));
//		return "testOwO2";
	}

	@ResponseBody
	@RequestMapping(value = "/openai_msg2")
	private String openai_msg2(@RequestBody String message) {
		System.out.println(message);

		OpenAiApi openAiApi = new OpenAiApi("Token");

		ChatCompletionMessage chatCompletionMessage = new ChatCompletionMessage(message, Role.USER);

		// Sync request
		ResponseEntity<ChatCompletion> response = openAiApi
				.chatCompletionEntity(new ChatCompletionRequest(List.of(chatCompletionMessage), "gpt-4", 0.8f, false));

		return response.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/openai_msg3")
	private String openai_msg3(@RequestBody String message) {
		System.out.println(message);

		OpenAiApi openAiApi = new OpenAiApi("Token");

		OpenAiChatClient chatClient = new OpenAiChatClient(openAiApi);
		
		ChatCompletionMessage chatCompletionMessage = new ChatCompletionMessage(message, Role.USER);

		Prompt prompt = new Prompt(message);

		ChatResponse response = chatClient.call(prompt);
//		ChatResponse response = chatClient.call(new Prompt(message));

		return response.toString();
	}
	
}

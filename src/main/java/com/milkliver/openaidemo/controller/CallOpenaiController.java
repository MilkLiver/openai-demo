package com.milkliver.openaidemo.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milkliver.openaidemo.ai.chat.OpenAiCall;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.messages.Message;
import com.theokanning.openai.messages.MessageContent;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.runs.Run;
import com.theokanning.openai.runs.RunCreateRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.ThreadRequest;
import com.theokanning.openai.threads.Thread;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@Controller
public class CallOpenaiController {
	private static final Logger log = LoggerFactory.getLogger(CallOpenaiController.class);

	@Autowired
	OpenAiCall openAiCall;

	@ResponseBody
	@RequestMapping(value = "/openai_msg")
//	private String openai_msg(@RequestHeader String apikey, @RequestBody String reqPayload) {
	private String openai_msg(@RequestBody String reqPayload) {
//		log.info("apikey: " + apikey);
		log.info("reqPayload: " + reqPayload);

		ObjectMapper objectMapper = new ObjectMapper();
		Map reqPayloadMap = null;
		String res = null;
		try {
			reqPayloadMap = objectMapper.readValue(reqPayload, new TypeReference<Map>() {
			});

			if (reqPayloadMap.get("asst") == null || reqPayloadMap.get("asst").toString().trim().equals("")) {
				return "asst is empty";
			}
			if (reqPayloadMap.get("model") == null || reqPayloadMap.get("model").toString().trim().equals("")) {
				return "model is empty";
			}
			if (reqPayloadMap.get("query") == null || reqPayloadMap.get("query").toString().trim().equals("")) {
				return "query is empty";
			}
			String asst = reqPayloadMap.get("asst").toString();
			String model = reqPayloadMap.get("model").toString();
			String query = reqPayloadMap.get("query").toString();
			log.info("asst: " + asst);
			log.info("model: " + model);
			log.info("query: " + query);

			res = openAiCall.callWithAssistant(asst, model, query);

		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			log.info(this.getClass().getName() + " error");
			return e.getMessage();
		}

		return res;
	}

}

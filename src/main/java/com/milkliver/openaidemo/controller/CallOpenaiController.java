package com.milkliver.openaidemo.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milkliver.openaidemo.ai.chat.ChatAiRespProcessing;
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

	@Autowired
	ChatAiRespProcessing chatAiRespProcessing;

	@Value("${tag.name}")
	String TAG_NAME;

	// just for test
	@ResponseBody
	@RequestMapping(value = "/test")
	private String test() {
		log.info(this.getClass().getName() + " ...");

		try {

			log.info("TAG_NAME: " + TAG_NAME + " this is test api ~");

		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			log.error(this.getClass().getName() + " error");
			return e.getMessage();
		}
		log.info(this.getClass().getName() + " finish");
		return "TAG_NAME: " + TAG_NAME;
	}

	// ask openai and return message
	@ResponseBody
	@RequestMapping(value = "/openai_msg")
	private String openai_msg(@RequestHeader(name = "apikey", required = false) String apikey,
			@RequestBody String reqPayload) {
		log.info(this.getClass().getName() + " ...");
		log.info("apikey: " + apikey);
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
			log.error(this.getClass().getName() + " error");
			return e.getMessage();
		}
		log.info(this.getClass().getName() + " finish");
		return res;
	}

	// return openai assistant list
	@ResponseBody
	@RequestMapping(value = "/openaiListAsst", produces = "application/json")
	private String openaiListAsst(@RequestHeader(name = "apikey", required = false) String apikey,
			@RequestParam(name = "limit", required = false, defaultValue = "99") Integer limitNum) {
		log.info(this.getClass().getName() + " ...");

		ObjectMapper objectMapper = new ObjectMapper();
		Map resPayloadMap = new HashMap();
		List<Map<String, Object>> asstList = new ArrayList<Map<String, Object>>();
		String res = null;
		try {
			asstList = openAiCall.getAssistants(limitNum);
			resPayloadMap.put("returnCode", "0000");
			resPayloadMap.put("object", "list");
			resPayloadMap.put("data", asstList);
			res = objectMapper.writeValueAsString(resPayloadMap);
			log.info(this.getClass().getName() + " finish");
		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			log.error(this.getClass().getName() + " error");
			return res;
		}

		return res;
	}

	// return input string tags
	@ResponseBody
	@RequestMapping(value = "/openTags", produces = "application/json")
	private String openTags(@RequestHeader(name = "apikey", required = false) String apikey,
			@RequestBody String reqPayload) {

		log.info(this.getClass().getName() + " ...");
		log.info("apikey: " + apikey);
		log.info("reqPayload: " + reqPayload);

		ObjectMapper objectMapper = new ObjectMapper();
		Map reqPayloadMap = null;
		Map respPayloadMap = new HashMap();
//		respPayloadMap.put("data", new HashMap());
		String res = null;
		try {
			reqPayloadMap = objectMapper.readValue(reqPayload, new TypeReference<Map>() {
			});

			if (reqPayloadMap.get("asst") == null || reqPayloadMap.get("asst").toString().trim().equals("")) {
				respPayloadMap.put("returnCode", "E998");
//				return "asst is empty";
			} else if (reqPayloadMap.get("model") == null || reqPayloadMap.get("model").toString().trim().equals("")) {
				respPayloadMap.put("returnCode", "E998");
//				return "model is empty";
			} else if (reqPayloadMap.get("query") == null || reqPayloadMap.get("query").toString().trim().equals("")) {
				respPayloadMap.put("returnCode", "E998");
//				return "query is empty";
			} else {
				String asst = reqPayloadMap.get("asst").toString();
				String model = reqPayloadMap.get("model").toString();
				String query = reqPayloadMap.get("query").toString();
				log.info("asst: " + asst);
				log.info("model: " + model);
				log.info("query: " + query);

				Map respTagsMap = chatAiRespProcessing.identityMsgTags(asst, model, query);
				if (respTagsMap != null && respTagsMap.keySet().size() > 0) {
					respPayloadMap = respTagsMap;
					respPayloadMap.put("returnCode", "0000");
				} else {
					respPayloadMap.put("returnCode", "E996");
				}
			}

			res = objectMapper.writeValueAsString(respPayloadMap);

		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			respPayloadMap.put("returnCode", "E999");
			log.error(this.getClass().getName() + " error");
			try {
				log.info("respPayloadMap: " + respPayloadMap.toString());
				res = objectMapper.writeValueAsString(respPayloadMap);
			} catch (JsonProcessingException e1) {
				log.error(e1.getMessage());
				return "server error E999";
			}
		}
		log.info(this.getClass().getName() + " finish");

		return res;
	}

	// return tags but string format
	@ResponseBody
	@RequestMapping(value = "/openAnswer", produces = "application/json")
	private String openAnswer(@RequestHeader(name = "apikey", required = false) String apikey,
			@RequestBody String reqPayload) {

		log.info(this.getClass().getName() + " ...");
		log.info("apikey: " + apikey);
		log.info("reqPayload: " + reqPayload);

		ObjectMapper objectMapper = new ObjectMapper();
		Map reqPayloadMap = null;
		Map respPayloadMap = new HashMap();
//		respPayloadMap.put("data", new HashMap());
		String res = null;
		try {
			reqPayloadMap = objectMapper.readValue(reqPayload, new TypeReference<Map>() {
			});

			if (reqPayloadMap.get("asst") == null || reqPayloadMap.get("asst").toString().trim().equals("")) {
				respPayloadMap.put("returnCode", "E998");
				res = objectMapper.writeValueAsString(respPayloadMap);
//				return "asst is empty";
			} else if (reqPayloadMap.get("model") == null || reqPayloadMap.get("model").toString().trim().equals("")) {
				respPayloadMap.put("returnCode", "E998");
				res = objectMapper.writeValueAsString(respPayloadMap);
//				return "model is empty";
			} else if (reqPayloadMap.get("query") == null || reqPayloadMap.get("query").toString().trim().equals("")) {
				respPayloadMap.put("returnCode", "E998");
				res = objectMapper.writeValueAsString(respPayloadMap);
//				return "query is empty";
			} else {
				String asst = reqPayloadMap.get("asst").toString();
				String model = reqPayloadMap.get("model").toString();
				String query = reqPayloadMap.get("query").toString();
				log.info("asst: " + asst);
				log.info("model: " + model);
				log.info("query: " + query);

				String resStr = openAiCall.callWithAssistant(asst, model, query);
				respPayloadMap.put("data", resStr);
				res = objectMapper.writeValueAsString(respPayloadMap);

			}
		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			respPayloadMap.put("returnCode", "E999");
			log.error(this.getClass().getName() + " error");
			try {
				log.info("respPayloadMap: " + respPayloadMap.toString());
				res = objectMapper.writeValueAsString(respPayloadMap);
			} catch (JsonProcessingException e1) {
				log.error(e1.getMessage());
				return "server error E999";
			}
		}
		log.info(this.getClass().getName() + " finish");
		return res;
	}

}

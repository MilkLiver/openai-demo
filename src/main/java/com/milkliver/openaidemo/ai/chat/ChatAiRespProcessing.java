package com.milkliver.openaidemo.ai.chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChatAiRespProcessing {
	private static final Logger log = LoggerFactory.getLogger(ChatAiRespProcessing.class);

	@Autowired
	OpenAiCall openAiCall;

	public Map<String, List<String>> identityMsgTags(String asst, String model, String query) {
		log.info(this.getClass().getName() + " ...");
		Map<String, List<String>> respTagsMap = new HashMap<String, List<String>>();
		try {
			ObjectMapper objectMapper = new ObjectMapper();

			String tagsStr = openAiCall.callWithAssistant(OpenAiCall.openaiReturnType.TAG ,asst, model, query);

			Map<String, List<String>> tagsMap = new HashMap<String, List<String>>();

			tagsMap = objectMapper.readValue(tagsStr, new TypeReference<Map<String, List<String>>>() {
			});

			respTagsMap.put("data", tagsMap.get("tags"));
		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			respTagsMap = null;
			log.error(this.getClass().getName() + " error");
		}
		log.info(this.getClass().getName() + " finish");
		return respTagsMap;
	}
}

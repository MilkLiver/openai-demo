package com.milkliver.openaidemo.ai.chat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.Assistant;
import com.theokanning.openai.assistants.Tool;
import com.theokanning.openai.messages.Message;
import com.theokanning.openai.messages.MessageContent;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.runs.Run;
import com.theokanning.openai.runs.RunCreateRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.Thread;
import com.theokanning.openai.threads.ThreadRequest;

@Service
public class OpenAiCall {
	private static final Logger log = LoggerFactory.getLogger(OpenAiCall.class);

	@Value("${openai.token}")
	String OPENAI_TOKEN;

	public String callWithAssistant(String asst, String model, String query) {
		log.info(this.getClass().getName() + " ...");
		try {
			OpenAiService service = new OpenAiService(OPENAI_TOKEN);

			MessageRequest mr1 = MessageRequest.builder().role("user").content(query).build();

			List<MessageRequest> messages = new ArrayList<MessageRequest>();
			messages.add(mr1);

			ThreadRequest tr = ThreadRequest.builder().messages(messages).build();
			Thread th = service.createThread(tr);

			RunCreateRequest runcreq = RunCreateRequest.builder().assistantId(asst).model(model).build();

			Run run = service.createRun(th.getId(), runcreq);

			Thread resth = service.retrieveThread(th.getId());
			while (run.getCompletedAt() == null) {
				java.lang.Thread.sleep(1000);
				run = service.retrieveRun(th.getId(), run.getId());
			}
			log.info(run.getCompletedAt().toString());

			List<Message> thMsgsList = service.listMessages(resth.getId()).getData();

			MessageContent resMsgCont = (MessageContent) thMsgsList.get(0).getContent().get(0);
			log.info(this.getClass().getName() + " finish");
			return resMsgCont.getText().getValue();
		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			log.error(this.getClass().getName() + " error");
			return e.getMessage();
		}

	}

	public List<Map<String, Object>> getAssistants(Integer limitNum) {
		log.info(this.getClass().getName() + " ...");
		List<Map<String, Object>> asstList = new ArrayList<Map<String, Object>>();
		try {
			OpenAiService service = new OpenAiService(OPENAI_TOKEN);

			ListSearchParameters openaiLsp = ListSearchParameters.builder().limit(limitNum.intValue()).build();

			OpenAiResponse<Assistant> getAsstRes = service.listAssistants(openaiLsp);
			List<Assistant> resAsstList = getAsstRes.getData();
			for (Assistant asst : resAsstList) {
				Map asstMap = new HashMap();
				asstMap.put("id", asst.getId());
				asstMap.put("object", asst.getObject());
				asstMap.put("createdAt", asst.getCreatedAt());
				asstMap.put("name", asst.getName());
				asstMap.put("model", asst.getModel());
				asstMap.put("description", asst.getDescription());
				asstMap.put("instructions", asst.getInstructions());

				List<Map<String, Object>> asstToolList = new ArrayList<Map<String, Object>>();
				for (Tool tool : asst.getTools()) {
					Map toolMap = new HashMap();
					toolMap.put("type", tool.getType().name());
					asstToolList.add(toolMap);
				}
				asstMap.put("tools", asstToolList);
				asstMap.put("file_ids", asst.getFileIds());
				asstMap.put("metadata", asst.getMetadata());
				asstList.add(asstMap);
			}

			log.info(this.getClass().getName() + " finish");
			return asstList;
		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			log.error(this.getClass().getName() + " error");
			return asstList;
		}

	}

}

package com.milkliver.openaidemo.ai.chat;

import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.Assistant;
import com.theokanning.openai.assistants.Tool;
import com.theokanning.openai.messages.Message;
import com.theokanning.openai.messages.MessageContent;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.runs.RunCreateRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.Thread;
import com.theokanning.openai.threads.ThreadRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class OpenAiCall {
	private static final Logger log = LoggerFactory.getLogger(OpenAiCall.class);

	@Value("${openai.token}")
	String OPENAI_TOKEN;

	public static enum openaiReturnType {
		TAG, MSG, JSONMSG
	}

	public String callWithAssistant(openaiReturnType type, String asst, String model, String query) {
		log.info(this.getClass().getName() + " ...");
		try {
			OpenAiService service = new OpenAiService(OPENAI_TOKEN);

			MessageRequest mr1 = MessageRequest.builder().role("user").content(query).build();

			List<MessageRequest> messages = new ArrayList<MessageRequest>();
			messages.add(mr1);

			ThreadRequest tr = ThreadRequest.builder().messages(messages).build();
			Thread th = service.createThread(tr);

			RunCreateRequest runcreq = RunCreateRequest.builder().assistantId(asst).model(model).build();

			// threads/thread_abc123/runs
			URL url = new URL("https://api.openai.com/v1/threads/" + th.getId() + "/runs");
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestMethod("POST");

			httpConn.setRequestProperty("Authorization", "Bearer " + OPENAI_TOKEN);
			httpConn.setRequestProperty("Content-Type", "application/json");
			httpConn.setRequestProperty("OpenAI-Beta", "assistants=v2");

			httpConn.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
			writer.write("{\n    \"assistant_id\": \"" + asst + "\"\n  }");
			writer.flush();
			writer.close();
			httpConn.getOutputStream().close();

			InputStream responseStream = httpConn.getResponseCode() / 100 == 2 ? httpConn.getInputStream()
					: httpConn.getErrorStream();
			Scanner s = new Scanner(responseStream).useDelimiter("\\A");
			String response = s.hasNext() ? s.next() : "";
			log.info(response);
			JSONObject jsonObject = new JSONObject(response);

			var runId = jsonObject.get("id");
			var runCompletedAt = jsonObject.get("completed_at");

			// Run run = service.createRun(th.getId(), runcreq);

			URL url2 = new URL("https://api.openai.com/v1/threads/" + th.getId());
			HttpURLConnection httpConn2 = (HttpURLConnection) url2.openConnection();
			httpConn2.setRequestMethod("GET");

			httpConn2.setRequestProperty("Content-Type", "application/json");
			httpConn2.setRequestProperty("Authorization", "Bearer " + OPENAI_TOKEN);
			httpConn2.setRequestProperty("OpenAI-Beta", "assistants=v2");

			InputStream responseStream2 = httpConn2.getResponseCode() / 100 == 2 ? httpConn2.getInputStream()
					: httpConn2.getErrorStream();
			Scanner s2 = new Scanner(responseStream2).useDelimiter("\\A");
			String response2 = s2.hasNext() ? s2.next() : "";
			log.info(response2);

			Thread resth = service.retrieveThread(th.getId());
			while (runCompletedAt == JSONObject.NULL) {
				java.lang.Thread.sleep(1000);
				URL url3 = new URL("https://api.openai.com/v1/threads/" + th.getId() + "/runs/" + runId);
				HttpURLConnection httpConn3 = (HttpURLConnection) url3.openConnection();
				httpConn3.setRequestMethod("GET");
				httpConn3.setRequestProperty("Authorization", "Bearer " + OPENAI_TOKEN);
				httpConn3.setRequestProperty("OpenAI-Beta", "assistants=v2");
				InputStream responseStream3 = httpConn3.getResponseCode() / 100 == 2 ? httpConn3.getInputStream()
						: httpConn3.getErrorStream();
				Scanner s3 = new Scanner(responseStream3).useDelimiter("\\A");
				String response3 = s3.hasNext() ? s3.next() : "";
				JSONObject jsonObject3 = new JSONObject(response3);
				if (jsonObject3.has("completed_at")) {
					runCompletedAt = jsonObject3.get("completed_at");
				}
			}
//			log.info(run.getCompletedAt().toString());

			List<Message> thMsgsList = service.listMessages(resth.getId()).getData();

			MessageContent resMsgCont = (MessageContent) thMsgsList.get(0).getContent().get(0);
			log.info(this.getClass().getName() + " finish");
			service.shutdownExecutor();

			if (type != null && type.equals(openaiReturnType.TAG) || type.equals(openaiReturnType.MSG)) {
				return resMsgCont.getText().getValue();
			} else {
				// 轉json
				return resMsgCont.getText().getValue().replace("【4:0†source】", "").replace("【4:1†source】", "")
						.replace("【4:2†source】", "").replace("【4:3†source】", "").replace("【4:4†source】", "")
						.replace("【4:5†source】", "");
			}

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
package cn.cerc.jdb.jiguang;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.cerc.jdb.core.IHandle;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.PushPayload.Builder;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.Notification;

public class JiguangPush {
	private static final Logger log = Logger.getLogger(JiguangPush.class);
	private JiguangSession session;
	// 消息id，回调时使用
	private String msgId;
	// 消息标题，仅安卓机型有效，IOS设备忽略，默认为应用标题
	private String title;
	// 消息内容
	private String message;
	// 附加参数
	private Map<String, String> params = new LinkedHashMap<>();

	public JiguangPush() {

	}

	public JiguangPush(IHandle handle) {
		this.session = (JiguangSession) handle.getProperty(JiguangSession.sessionId);
	}

	// 发送给所有设备
	public void send() {
		// 发送给安卓
		send(ClientType.Android, null);
		// 发送给IOS
		send(ClientType.IOS, null);
	}

	// 发送给指定设备
	public void send(ClientType clientType, String clientId) {
		if (msgId == null)
			throw new RuntimeException("msgId is null");
		addParam("msgId", msgId);

		Builder builder = PushPayload.newBuilder();

		// 发送给指定的设备
		if (clientId != null)
			builder.setAudience(Audience.alias(clientId));
		else
			builder.setAudience(Audience.all());

		// 发送给指定的设备类型
		if (clientType == ClientType.Android) {
			builder.setPlatform(Platform.android());
			builder.setNotification(Notification.android(message, this.title, params));
			sendMessage(builder.build());
		} else if (clientType == ClientType.IOS) {
			builder.setPlatform(Platform.ios());
			builder.setNotification(Notification.ios(message, params));
			sendMessage(builder.build());
		} else
			throw new RuntimeException("暂不支持的设备类别：" + clientType.ordinal());
	}

	// 发送一条讯息
	private void sendMessage(PushPayload payload) {
		try {
			PushResult result = session.getClient().sendPush(payload);
			log.info("Got result - " + result);

		} catch (APIConnectionException e) {
			// Connection error, should retry later
			log.error("Connection error, should retry later", e);

		} catch (APIRequestException e) {
			// Should review the error, and fix the request
			log.error("Should review the error, and fix the request", e);
			log.info("HTTP Status: " + e.getStatus());
			log.info("Error Code: " + e.getErrorCode());
			log.info("Error Message: " + e.getErrorMessage());
		}
	}

	// 增加附加参数
	public void addParam(String key, String value) {
		params.put(key, value);
	}

	public String getMsgId() {
		return msgId;
	}

	public JiguangPush setMsgId(String msgId) {
		this.msgId = msgId;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public JiguangPush setTitle(String title) {
		this.title = title;
		return this;
	}

	public JiguangSession getSession() {
		return session;
	}

	public void setSession(JiguangSession session) {
		this.session = session;
	}

	public String getMessage() {
		return message;
	}

	public JiguangPush setMessage(String message) {
		this.message = message;
		return this;
	}

}

package com.clickntap.api;

import java.util.HashMap;
import java.util.Map;

import com.clickntap.smart.SmartContext;
import com.clickntap.tool.mail.Mail;
import com.clickntap.utils.ConstUtils;

public class Mailer extends com.clickntap.tool.mail.Mailer {

	public Mail newMail(String key, SmartContext ctx) throws Exception {
		Mail mail = newMail(key);
		Map<String, Object> mailCtx = new HashMap<String, Object>();
		mailCtx.put(ConstUtils.THIS, ctx);
		setup(mail, mailCtx);
		return mail;
	}

}

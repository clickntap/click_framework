package com.clickntap.tool.mail;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clickntap.utils.ConstUtils;

public class Mail {
	private static final String MIXED = "mixed";
	private static final String RELATED = "related";
	private static final String ALTERNATIVE = "alternative";
	private static final String CONTENT_ID = "Content-Id";
	private static final String CONTENT_TYPE = "Content-Type";
	private static Log log = LogFactory.getLog(Mail.class);
	protected String key = ConstUtils.EMPTY;
	protected String host = ConstUtils.EMPTY;
	protected String port = null;
	protected String from = null;
	protected String username = null;
	protected String password = null;
	protected List<String> to = new ArrayList<String>();
	protected List<String> cc = new ArrayList<String>();
	protected List<String> bcc = new ArrayList<String>();
	protected List<DataSource> attachs = new ArrayList<DataSource>();
	protected List<String> resources = new ArrayList<String>();
	protected String subject = ConstUtils.EMPTY;
	protected List<Body> bodies = new ArrayList<Body>();
	protected Boolean starttls;

	public Mail() {
		this(false);
	}

	public Mail(Boolean starttl) {
		this.starttls = starttl;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setFrom(String email) {
		this.from = email;
	}

	public void addTo(String email) {
		this.to.add(email);
	}

	public void addTos(List<String> to) {
		this.to.addAll(to);
	}

	public void resetTo() {
		this.to = new ArrayList<String>();
	}

	public void addCc(String mail) {
		this.cc.add(mail);
	}

	public void addCcs(List<String> cc) {
		this.cc.addAll(cc);
	}

	public void resetCc() {
		this.cc = new ArrayList<String>();
	}

	public void addBcc(String mail) {
		this.bcc.add(mail);
	}

	public void addBccs(List<String> bcc) {
		this.bcc.addAll(bcc);
	}

	public void resetBcc() {
		this.bcc = new ArrayList<String>();
	}

	public void addAttach(String file) {
		addAttach(new FileDataSource(file));
	}

	public void addAttach(DataSource dataSource) {
		this.attachs.add(dataSource);
	}

	public void addAttachs(List<String> attachs) {
		for (String file : attachs) {
			addAttach(file);
		}
	}

	public void resetAttachs() {
		this.attachs = new ArrayList<DataSource>();
	}

	public void addResource(String file) {
		this.resources.add(file);
	}

	public void addResources(List<String> resources) {
		this.resources.addAll(resources);
	}

	public void resetResources() {
		this.resources = new ArrayList<String>();
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void addBody(String content, String contentType) {
		bodies.add(new Body(content, contentType));
	}

	public void addBodies(List<Body> bodies) {
		this.bodies.addAll(bodies);
	}

	public void resetBodies() {
		bodies = new ArrayList<Body>();
	}

	public void send(boolean synchronous) throws Exception {
		if (synchronous)
			send();
		else
			sendAsynchronous();
	}

	public void send() throws Exception {
		MimeMessage msg;
		java.util.Properties p = new java.util.Properties();

		SmtpAuthenticator authenticator = null;

		if (host.equals("localhost"))
			p.put("mail.host", host);
		else {
			p.put("mail.smtp.host", host);
			p.put("mail.smtp.localhost", host);
		}
		if (port != null)
			p.put("mail.smtp.port", port);
		if (username != null) {
			if (starttls)
				p.put("mail.smtp.starttls.enable", "true");
			p.put("mail.smtp.auth", "true");
			authenticator = new SmtpAuthenticator(username, password);
		}

		Session session = Session.getInstance(p, authenticator);

		msg = new MimeMessage(session);
		if (from != null)
			msg.setFrom(new InternetAddress(from));
		msg.setSubject(subject);

		for (int i = 0; i < to.size(); i++)
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress((String) to.get(i)));

		for (int i = 0; i < cc.size(); i++)
			msg.addRecipient(Message.RecipientType.CC, new InternetAddress((String) cc.get(i)));

		for (int i = 0; i < bcc.size(); i++)
			msg.addRecipient(Message.RecipientType.BCC, new InternetAddress((String) bcc.get(i)));
		MimeBodyPart mbp = null;
		DataSource fds = null;
		Body body = null;
		if ((attachs.size() + resources.size() + bodies.size()) > 1) {
			Multipart mp = null;
			Multipart mpBodies = null;
			Multipart mpResources = null;

			String mpType = ConstUtils.EMPTY;

			if (attachs.size() > 0)
				mpType = MIXED;
			else if (resources.size() > 0)
				mpType = RELATED;
			else if (bodies.size() > 1)
				mpType = ALTERNATIVE;

			mp = new MimeMultipart(mpType);

			if (resources.size() > 0 && !mpType.equals(RELATED))
				mpResources = new MimeMultipart(RELATED);

			if (bodies.size() > 1 && !mpType.equals(ALTERNATIVE))
				mpBodies = new MimeMultipart(ALTERNATIVE);

			for (int i = 0; i < bodies.size(); i++) {
				body = (Body) bodies.get(i);
				mbp = new MimeBodyPart();
				mbp.setContent(body.getContent(), body.getContentType());

				if (mpBodies != null)
					mpBodies.addBodyPart(mbp);
				else if (mpResources != null)
					mpResources.addBodyPart(mbp);
				else
					mp.addBodyPart(mbp);
			}
			if (mpBodies != null) {
				mbp = new MimeBodyPart();
				mbp.setContent(mpBodies);
				if (mpResources != null)
					mpResources.addBodyPart(mbp);
				else
					mp.addBodyPart(mbp);
			}

			for (int i = 0; i < resources.size(); i++) {

				FileNameMap fileNameMap = URLConnection.getFileNameMap();
				String contentType = fileNameMap.getContentTypeFor((String) resources.get(i));
				File f = new File((String) resources.get(i));
				fds = new FileDataSource(f);
				mbp = new MimeBodyPart();
				mbp.setDataHandler(new DataHandler(fds));
				mbp.setHeader(CONTENT_ID, ConstUtils.LT + fds.getName() + ConstUtils.GT);
				mbp.setHeader(CONTENT_TYPE, contentType);
				if (mpResources != null)
					mpResources.addBodyPart(mbp);
				else
					mp.addBodyPart(mbp);
			}
			if (mpResources != null) {
				mbp = new MimeBodyPart();
				mbp.setContent(mpResources);
				mp.addBodyPart(mbp);
			}

			for (int i = 0; i < attachs.size(); i++) {
				fds = attachs.get(i);
				mbp = new MimeBodyPart();
				mbp.setDataHandler(new DataHandler(fds));
				mbp.setFileName(fds.getName());
				mp.addBodyPart(mbp);
			}

			msg.setContent(mp);
		} else if (bodies.size() == 1) {
			body = (Body) bodies.get(0);
			msg.setContent(body.getContent(), body.getContentType());
		}
		msg.setSentDate(new java.util.Date());
		Transport.send(msg);
	}

	public void sendAsynchronous() {
		Thread t = new MailerThread();
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	public class MailerThread extends Thread {
		public void run() {
			try {
				send();
			} catch (Exception e) {
				log.error("mail", e);
			}
		}
	}

	public class SmtpAuthenticator extends Authenticator {
		protected PasswordAuthentication passwordAuthentication = null;

		public SmtpAuthenticator(String username, String password) {
			passwordAuthentication = new PasswordAuthentication(username, password);
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return passwordAuthentication;
		}

	}

}

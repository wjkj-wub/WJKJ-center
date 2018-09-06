package com.miqtech.master.thirdparty.util;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 发送邮件工具类
 */
public class EmailUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailUtil.class);
	private static final String CONFIG_HOST = "smtp.163.com";
	private static final String CONFIG_USERNAME = "wy_notify@163.com";
	private static final String CONFIG_PASSWORD = "yligtgchodjlqsxr";
	private static JavaMailSenderImpl javaMailSenderImpl;

	private EmailUtil() {
	}

	/**
	 * 初始化寄件器
	 */
	private static JavaMailSenderImpl initSender() {
		if (javaMailSenderImpl == null) {
			javaMailSenderImpl = new JavaMailSenderImpl();
			javaMailSenderImpl.setHost(CONFIG_HOST);
			javaMailSenderImpl.setUsername(CONFIG_USERNAME);
			javaMailSenderImpl.setPassword(CONFIG_PASSWORD);

			Properties properties = javaMailSenderImpl.getJavaMailProperties();
			properties.setProperty("mail.smtp.auth", "true");
			properties.setProperty("mail.smtp.timeout", "25000");
			properties.setProperty("mail.smtp.port", "465");
			properties.setProperty("mail.smtp.socketFactory.port", "465");
			properties.setProperty("mail.smtp.socketFactory.fallback", "false");
			properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		}

		return javaMailSenderImpl;
	}

	/**
	 * 发送邮件
	 * @param to：收件人地址
	 * @param from：发件人地址
	 * @param subject：邮件主题
	 * @param text：邮件正文
	 */
	public static void send(String to, String subject, String text) {
		JavaMailSender sender = initSender();//获取JavaMailSender bean
		SimpleMailMessage mail = new SimpleMailMessage();//发送简单text格式的邮件
		try {
			mail.setTo(to);//收件人
			mail.setFrom(CONFIG_USERNAME);//发件人
			mail.setSubject(subject);//邮件主题
			mail.setText(text);//邮件内容
			sender.send(mail);
		} catch (MailException e) {
			LOGGER.error("邮件发送异常：" + e);
		}
	}

}

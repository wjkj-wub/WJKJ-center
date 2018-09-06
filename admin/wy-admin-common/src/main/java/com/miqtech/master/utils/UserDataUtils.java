package com.miqtech.master.utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;

public class UserDataUtils {

	public static final int[] dataPerHour = { 1000, 30000, 100000, 100000, 200000, 2000, 3000, 4000, 50000, 60000,
			1000, 30000, 100000, 100000, 200000, 2000, 3000, 4000, 50000, 60000, 70000, 1000, 30000, 100000 };

	public static final String[] pwds = { "123456", "123456789", "12345678", "11111111", "00000000", "123123123",
			"1234567890", "88888888", "111111111", "147258369", "987654321", "aaaaaaaa", "1111111111", "66666666",
			"a123456789", "11223344", "1qaz2wsx", "xiazhili", "789456123", "password", "87654321", "qqqqqqqq",
			"000000000", "qwertyuiop", "qq123456", "iloveyou", "31415926", "12344321", "0000000000", "asdfghjkl",
			"1q2w3e4r", "123456abc", "0123456789", "123654789", "12121212", "qazwsxedc", "abcd1234", "12341234",
			"110110110", "asdasdasd", "22222222", "123456", "123321123", "abc123456", "a12345678", "123456123",
			"a1234567", "1234qwer", "qwertyui", "123456789a", "aa123456", "asdfasdf", "99999999", "999999999",
			"123456aa", "123456123456", "520520520", "963852741", "741852963", "55555555", "33333333", "qwer1234",
			"asd123456", "77777777", "qweasdzxc", "code8925", "11112222", "ms0083jxj", "zzzzzzzz", "111222333",
			"qweqweqwe", "3.1415926", "123456qq", "147852369", "521521521", "asdf1234", "123698745", "1123581321",
			"asdfghjk", "q1w2e3r4", "12345678a", "qazxcv", "woaini1314", "1234abcd", "123qweasd", "1qazxsw2",
			"woaiwojia", "321321321", "05962514787", "123456987", "kingcom5", "zxcvbnm123", "5845201314", "584131421",
			"0987654321", "wwwwwwww", "11111111111111111111", "12345600", "11235813", "1q2w3e4r5t" };
	public static final Joiner join = Joiner.on(",");

	public static void main(String[] args) throws IOException {
		File userData = new File("d:\\userdata3.txt");
		Date date = new Date();
		List<String> readLines = FileUtils.readLines(new File("c:\\phones3.txt"), "UTF-8");
		if (CollectionUtils.isNotEmpty(readLines)) {
			for (String phone : readLines) {
				if (StringUtils.isNotBlank(phone)) {
					StringBuffer sb = new StringBuffer();
					String nickname = StringUtils.replace(phone, StringUtils.substring(phone, 3, 7), "****");
					int nextInt = RandomUtils.nextInt(300);
					Date addDays = DateUtils.addDays(date, -nextInt);
					int nextHour = RandomUtils.nextInt(24);
					addDays = DateUtils.addHours(addDays, nextHour);
					int nextMinute = RandomUtils.nextInt(60);
					addDays = DateUtils.addMinutes(addDays, nextMinute);
					int nextSecond = RandomUtils.nextInt(60);
					addDays = DateUtils.addSeconds(addDays, nextSecond);
					sb.append(phone).append(",").append(EncodeUtils.base64Md5(pwds[new Random().nextInt(pwds.length)]))
							.append(",").append(nickname).append(",")
							.append(DateUtils.dateToString(addDays, "yyyy-MM-dd hh:mm:ss")).append(",")
							.append(IdentityUtils.uuid()).append("\n");
					FileUtils.writeStringToFile(userData, sb.toString(), true);
				}
			}
		}
	}
}

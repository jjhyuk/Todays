package com.example.jang.application1.login;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Util { // 이메일정규식
 public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
 public static boolean validateEmail(String emailStr) { Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr); return matcher.find(); }
 // 비밀번호정규식
 public static final Pattern VALID_PASSWOLD_REGEX_ALPHA_NUM = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{8,16}$"); // 4자리 ~ 16자리까지 가능
 public static boolean validatePassword(String pwStr) { Matcher matcher = VALID_PASSWOLD_REGEX_ALPHA_NUM.matcher(pwStr); return matcher.matches(); }
 //Util.validatePassword("가나다라"); // => false Util.validatePassword("aaa"); // => false
}


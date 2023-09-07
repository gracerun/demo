package com.gracerun.authentication.web;

import ch.qos.logback.classic.Level;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gracerun.security.authentication.bean.LoginRequest;
import com.gracerun.security.authentication.constant.LoginConstant;
import com.gracerun.util.HttpBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class LoginTest {

    protected static final String AUTH_HOST = "http://localhost:6010";
    protected static final Map<String, String> header = new HashMap<>();

    protected static final RequestConfig config = RequestConfig.custom()
            .setSocketTimeout(300000).setConnectTimeout(10000).setConnectionRequestTimeout(2000).build();

    protected static final String fileName = "config.properties";
    protected static final String Authorization = LoginConstant.HEADER_X_AUTH_TOKEN;

    protected static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @SneakyThrows
    @Test
    public void login() {
        if (StringUtils.hasText(header.get(Authorization))) {
            return;
        }
        LoginRequest loginDto = new LoginRequest();
        loginDto.setLoginType(LoginConstant.PASSWORD);
        loginDto.setUsername("test");
        loginDto.setPassword("test");
        log.info(JSON.toJSONString(loginDto));
        String response = HttpBuilder.post(AUTH_HOST + "/login").setJsonParam(loginDto)
                .setHeader(header).setLevel(Level.INFO).setConfig(config)
                .execute();
        JSONObject jsonObject = JSON.parseObject(response);
        String token = jsonObject.getString("token");
        header.put(Authorization, token);
        IOUtils.closeQuietly();
//        writeToken(token);
    }

    @SneakyThrows
    public static void logout() {
        HttpBuilder.create(HttpDelete.METHOD_NAME).setUrl(AUTH_HOST + "/auth/logout").setHeader(header).execute();
    }

    public static void loadToken() throws IOException {
        final File file = getFile();
        try (InputStream inputStream = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            String token = properties.getProperty("token");
            Assert.hasText(token, "加载token失败");
            log.info("加载token成功:{}", token);
            header.put(Authorization, token);
        }
    }

    public static void writeToken(String token) throws IOException {
        final File file = getFile();
        try (InputStream inputStream = new FileInputStream(file);
             FileOutputStream outputStream = new FileOutputStream(file)
        ) {
            Properties properties = new Properties();
            properties.load(inputStream);
            properties.setProperty("token", token);
            properties.store(outputStream, "更新token");
        }
    }

    public static File getFile() {
        String rootPath = System.getProperty("user.dir");
        log.info(rootPath);
        File file = new File(rootPath, "/src/test/resources/" + fileName);
        log.info(file.getAbsolutePath());
        return file;
    }

    public static String getDate() {
        return new DateTime().toString(DATETIME_FORMAT);
    }

}
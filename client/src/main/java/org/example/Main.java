package org.example;

import cn.hutool.json.JSONObject;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Please specify the path to the LLVM file.");
            return;
        } else if (args.length < 2) {
            System.out.println("Please specify the path to the input file.");
            return;
        }

        final String apiUrl = "https://shocking-shadow-g44p4qpgjqw4f9jg6-8080.app.github.dev/api/run";

        String llvmFilePath = args[0];
        String inputFilePath = args[1];

        // 检查文件是否存在
        File llvmFile = new File(llvmFilePath);
        if (!llvmFile.exists() || !llvmFile.isFile()) {
            System.out.println("The specified LLVM file does not exist.");
            return;
        }

        String inputData;

        File inputFile = new File(inputFilePath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            System.out.println("The specified input file does not exist.");
            return;
        }
        try {
            inputData = Files.readString(inputFile.toPath());
        } catch (IOException e) {
            System.out.println("Failed to read input file.");
            return;
        }

        File outputFile = null;
        if (args.length == 3) {
            String outputFilePath = args[2];
            outputFile = new File(outputFilePath);
        }

        // 创建 HTTP 客户端并发送请求
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(apiUrl);

            // 创建请求体
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", llvmFile, ContentType.DEFAULT_BINARY, llvmFile.getName());
            builder.addTextBody("input", inputData, ContentType.TEXT_PLAIN);

            HttpEntity multipart = builder.build();
            httpPost.setEntity(multipart);

            // 执行请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                HttpEntity responseEntity = response.getEntity();
                final JSONObject jsonObject = new JSONObject(EntityUtils.toString(responseEntity));

                if (statusCode == 200) {
                    String output = jsonObject.getStr("output");
                    Integer exitCode = jsonObject.getInt("exitCode");
                    if (exitCode != 0) {
                        System.out.println("LLVM code error");
                        System.out.println(output);
                    } else {
                        if (outputFile != null) {
                            try {
                                Files.writeString(outputFile.toPath(), output);
                            } catch (IOException e) {
                                System.out.println("Failed to write output file.");
                            }
                        } else {
                            System.out.println(output);
                        }
                    }
                } else {
                    System.out.println("Request failed with status code: " + statusCode);
                    System.out.println("Message: " + jsonObject.getStr("message"));
                }
            }
        } catch (IOException | ParseException e) {
            System.out.println("An error occurred while sending the request.");
        }
    }
}
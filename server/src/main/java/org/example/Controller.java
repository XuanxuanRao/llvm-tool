package org.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class Controller {

    private static final String LIB_LL_PATH = "/workspaces/codespaces-blank/llvm-tool/lib.ll";

    @PostMapping("/run")
    public RunResult runLLVM(@RequestParam MultipartFile file, @RequestParam String input) {
        // 临时目录
        String tempDir = System.getProperty("java.io.tmpdir");
        Path llvmFilePath = Paths.get(tempDir, file.getOriginalFilename());
        Path outputPath = Paths.get(tempDir, "out.ll");

        try {
            // 保存上传的文件
            Files.write(llvmFilePath, file.getBytes());

            // 1. 执行 llvm-link
            ProcessBuilder linkProcessBuilder = new ProcessBuilder(
                    "llvm-link", llvmFilePath.toString(), LIB_LL_PATH, "-S", "-o", outputPath.toString()
            );
            linkProcessBuilder.redirectErrorStream(true); // 合并错误流
            Process linkProcess = linkProcessBuilder.start();
            String linkOutput = readProcessOutput(linkProcess);
            int linkExitCode = linkProcess.waitFor();

            if (linkExitCode != 0) {
                return RunResult.success(linkExitCode, linkOutput);
            }

            // 2. 执行 lli
            log.info("Run lli with input {}", input);
            ProcessBuilder runProcessBuilder = new ProcessBuilder(
                    "lli", outputPath.toString()
            );
            runProcessBuilder.redirectErrorStream(true); // 合并错误流
            Process runProcess = runProcessBuilder.start();
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
                writer.write(input);
                writer.flush();
            }
            // 读取 lli 的输出
            String runOutput = readProcessOutput(runProcess);
            int runExitCode = runProcess.waitFor();
            if (runExitCode != 0) {
                return RunResult.success(runExitCode, runOutput);
            }

            return RunResult.success(0, runOutput);

        } catch (Exception e) {
            return RunResult.error("An error occurred: " + e.getMessage());
        } finally {
            // 删除临时文件
            try {
                Files.deleteIfExists(llvmFilePath);
                Files.deleteIfExists(outputPath);
            } catch (IOException ignored) {
            }
        }
    }


    // 读取进程输出
    private String readProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString();
        }
    }

}

package com.itgr.zhaojCodeSandbox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.itgr.zhaojCodeSandbox.model.ExecuteMessage;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Java 代码沙箱模板方法的实现
 *
 * @author ygking
 */
@Component
public class JavaDockerCodeSandbox extends JavaCodeSandboxTemplate {

    private static final long TIME_OUT = 5000L;

    public static final boolean FIRST_INIT = true;

    /**
     * 3.把编译好的文件上传到 docker 容器环境内
     *
     * @param userCodeFile
     * @param inputList
     * @param userCodeParentPath
     * @return
     */
    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList, String userCodeParentPath) {
        //获取默认的 docker Clint
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();

        // 拉取镜像
        String image = "openjdk:8-alpine";
        if (FIRST_INIT) {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("下载镜像：" + item.getStatus());
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd
                        .exec(pullImageResultCallback)
                        .awaitCompletion();
            } catch (InterruptedException e) {
                System.out.println("拉取镜像异常");
                throw new RuntimeException(e);
            }
        }

        System.out.println("下载完成!");

        //创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        // 创建容器配置
        HostConfig hostConfig = new HostConfig();
        hostConfig
                .withMemory(100 * 1000 * 1000L)
                .withCpuCount(1L);
        // 编译后的文件复制到挂载目录
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/userCode")));
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .withReadonlyRootfs(true)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();
        System.out.println(createContainerResponse);
        String containerId = createContainerResponse.getId();

        // 4.启动容器
        dockerClient.startContainerCmd(containerId).exec();

        // 执行命令并获取结果
        List<ExecuteMessage> executeMessageList = new ArrayList<>();    //获取执行列表
        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            // 创建命令
            String[] inputArgArray = inputArgs.split(" ");
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/userCode", "Main"}, inputArgArray);
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .exec();
            // 执行命令
            System.out.println("创建执行命令：" + execCreateCmdResponse);

            ExecuteMessage executeMessage = new ExecuteMessage();
            final String[] message = {null};
            final String[] errorMessage = {null};
            long time = 0L;
            final boolean[] timeout = {true};

            String execId = execCreateCmdResponse.getId();
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {


                @Override
                public void onComplete() {
                    // 如果程序完成，则表示未超时
                    timeout[0] = true;
                    super.onComplete();
                }

                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        errorMessage[0] = new String(frame.getPayload(), StandardCharsets.UTF_8);
                        System.out.println("输出异常结果：" + new String(frame.getPayload(), StandardCharsets.UTF_8));
                    } else {
                        message[0] = new String(frame.getPayload(), StandardCharsets.UTF_8);
                        System.out.println("输出结果：" + new String(frame.getPayload(), StandardCharsets.UTF_8));

                    }
                    super.onNext(frame);
                }
            };

            final long[] maxMemory = {0L};
            final CountDownLatch latch = new CountDownLatch(1);

            // 获取内存占用
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {

                @Override
                public void onNext(Statistics statistics) {
                    System.out.println("内存占用：" + statistics.getMemoryStats().getUsage());
                    Long usage = statistics.getMemoryStats().getUsage();
                    if (usage != null) {
                        synchronized (maxMemory) {
                            maxMemory[0] = Math.max(usage, maxMemory[0]);
                        }
                        latch.countDown(); // 内存获取完成后释放锁
                    }
                }

                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                    latch.countDown(); // 出现错误时释放锁
                }

                @Override
                public void onComplete() {

                }

                @Override
                public void close() throws IOException {

                }
            });

            statsCmd.exec(statisticsResultCallback);

            try {
                stopWatch.start();
                dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
                        .awaitCompletion(TIME_OUT, TimeUnit.MICROSECONDS);
                stopWatch.stop();
                time = stopWatch.getLastTaskTimeMillis();
                latch.await(); // 等待内存统计完成
            } catch (InterruptedException e) {
                System.out.println("错误信息：" + e.getMessage());
                throw new RuntimeException(e);
            } finally {
                statsCmd.close();
            }
            executeMessage.setMessage(message[0]);
            executeMessage.setErrorMessage(errorMessage[0]);
            executeMessage.setTime(time);
            executeMessage.setMemory(maxMemory[0]);
            executeMessageList.add(executeMessage);
        }
        // 确保在所有操作完成后停止和移除容器
        try {
            dockerClient.stopContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId).exec();
        } catch (Exception e) {
            System.out.println("清理容器时出错：" + e.getMessage());
        }
        return executeMessageList;
    }
}

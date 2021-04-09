package dev.jianmu.api.grpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.api.dto.DockerTask;
import dev.jianmu.application.service.ParameterApplication;
import dev.jianmu.application.service.TaskInstanceApplication;
import dev.jianmu.application.service.WorkerApplication;
import dev.jianmu.infrastructure.messagequeue.TaskInstanceQueue;
import dev.jianmu.infrastructure.storage.StorageException;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.task.aggregate.InstanceStatus;
import dev.jianmu.task.aggregate.TaskInstance;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @class: WorkerStreamServiceImpl
 * @description: Grpc接口实现类
 * @author: Ethan Liu
 * @create: 2021-03-30 14:24
 **/
@GrpcService
public class WorkerStreamServiceImpl extends WorkerStreamServiceGrpc.WorkerStreamServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(WorkerStreamServiceImpl.class);
    private final WorkerApplication workerApplication;
    private final TaskInstanceApplication taskInstanceApplication;
    private final ParameterApplication parameterApplication;
    private final TaskInstanceQueue taskInstanceQueue;
    private final ObjectMapper objectMapper;
    private final StorageService storageService;

    private final ConcurrentHashMap<String, Thread> threadMap = new ConcurrentHashMap<>();

    @Inject
    public WorkerStreamServiceImpl(WorkerApplication workerApplication, TaskInstanceApplication taskInstanceApplication, ParameterApplication parameterApplication, TaskInstanceQueue taskInstanceQueue, ObjectMapper objectMapper, StorageService storageService) {
        this.workerApplication = workerApplication;
        this.taskInstanceApplication = taskInstanceApplication;
        this.parameterApplication = parameterApplication;
        this.taskInstanceQueue = taskInstanceQueue;
        this.objectMapper = objectMapper;
        this.storageService = storageService;
    }

    @Override
    public void registry(OnlineReq request, StreamObserver<ServerResp> responseObserver) {
        String workerId = request.getWorkerId();
        logger.info("get worker (id: {}) registry request", workerId);
        this.workerApplication.online(workerId);
        responseObserver.onNext(
                ServerResp.newBuilder()
                        .setResult(true)
                        .setErrorMsg("200 OK")
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void leave(OffLineReq request, StreamObserver<ServerResp> responseObserver) {
        String workerId = request.getWorkerId();
        logger.info("get worker (id: {}) leave request", workerId);
        this.workerApplication.offline(workerId);
        responseObserver.onNext(
                ServerResp.newBuilder()
                        .setResult(true)
                        .setErrorMsg("200 OK")
                        .build()
        );
        responseObserver.onCompleted();
    }

    private String getDto(TaskInstance taskInstance)  {
        var p = this.parameterApplication.findTaskParameters(taskInstance.getId());
        List<String> entrypoint = null;
        List<String> args = null;
        List<DockerTask.TasksEntity.VolumeMountsEntity> volume_mounts = null;
        List<DockerTask.VolumesEntity> volumes = null;
        try {
            entrypoint = this.objectMapper.readValue(p.getLeft().getOrDefault("entrypoint", "[]"), new TypeReference<List<String>>() {});
            args = this.objectMapper.readValue(p.getLeft().getOrDefault("command", "[]"), new TypeReference<List<String>>() {});
            volume_mounts = this.objectMapper.readValue(p.getLeft().getOrDefault("volume_mounts", "[]"), new TypeReference<List<DockerTask.TasksEntity.VolumeMountsEntity>>() {});
            volumes = this.objectMapper.readValue(p.getLeft().getOrDefault("volumes", "{}"), new TypeReference<List<DockerTask.VolumesEntity>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("some thing wrong", e);
        }
        DockerTask dockerTask = DockerTask.builder()
                .Tasks(List.of(
                        DockerTask.TasksEntity.builder()
                                .id(taskInstance.getId())
                                .name(taskInstance.getName())
                                .image(p.getLeft().get("image"))
                                .network(p.getLeft().get("network"))
                                .working_dir(p.getLeft().get("working_dir"))
                                .entrypoint(entrypoint)
                                .args(args)
                                .volume_mounts(volume_mounts)
                                .environment(p.getRight())
                                .build()
                ))
                .volumes(volumes)
                .build();
        try {
            return objectMapper.writeValueAsString(dockerTask);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("DTO转换失败");
        }
    }

    @Override
    public void subscribeTask(SubReq request, StreamObserver<Task> responseObserver) {
        for (; ; ) {
            try {
                Thread thread = this.threadMap.get(request.getWorkerId());
                if (thread != null && !Thread.currentThread().equals(thread)) {
                    logger.info("old subscriber kill, worker id: {} thread name: {}", request.getWorkerId(), thread.getName());
                    thread.interrupt();
                }
                this.threadMap.put(request.getWorkerId(), Thread.currentThread());
                TaskInstance taskInstance = this.taskInstanceQueue.take();
                logger.info("get task instance here: id {}, key {}", taskInstance.getId(), taskInstance.getDefKey());
                var dto = this.getDto(taskInstance);
                logger.info(dto);
                Task task = Task.newBuilder()
                        .setId(taskInstance.getId())
                        .setName(taskInstance.getName())
                        .setDto(dto)
                        .build();
                responseObserver.onNext(task);
            } catch (InterruptedException e) {
                responseObserver.onCompleted();
                break;
            }
        }
    }

    @Override
    public void updateTask(TaskResult request, StreamObserver<ServerResp> responseObserver) {
        String taskId = request.getTaskId();
        logger.info("get task (id: {}) update request", taskId);
        TaskResult.Status status = request.getTaskStatus();
        logger.info("update task status: {}", status);
        Instant instant = Instant.ofEpochSecond(request.getWorkerTimestamp().getSeconds(),
                request.getWorkerTimestamp().getNanos());
        logger.info("update task time is: {}", instant.atZone(ZoneId.systemDefault()).toLocalDateTime());
        // TODO 需要生成任务记录
        switch (status) {
            case RUNNING:
                this.taskInstanceApplication.updateStatus(taskId, InstanceStatus.RUNNING);
                break;
            case FAILED:
                this.taskInstanceApplication.updateStatus(taskId, InstanceStatus.EXECUTION_FAILED);
                break;
            case SUCCEEDED:
                this.taskInstanceApplication.updateStatus(taskId, InstanceStatus.EXECUTION_SUCCEEDED);
                break;
            default:
                logger.info("should never get here");
        }
        responseObserver.onNext(
                ServerResp.newBuilder()
                        .setResult(true)
                        .setErrorMsg("200 OK")
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<TaskOutput> uploadTaskLog(StreamObserver<ServerResp> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(TaskOutput taskOutput) {
                logger.info("get task (id: {}) output", taskOutput.getTaskId());
                try (var writer = storageService.writeLog(taskOutput.getTaskId())) {
                    writer.write(taskOutput.getLine());
                    writer.flush();
                } catch (IOException e) {
                    throw new StorageException("Could not write log file", e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("get throwable is: ", throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(
                        ServerResp.newBuilder()
                                .setResult(true)
                                .setErrorMsg("200 OK")
                                .build()
                );
                responseObserver.onCompleted();
            }
        };
    }
}

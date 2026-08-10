package com.api.jira.apis.listener;

import com.api.jira.apis.task.model.TaskUpdateEventDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TaskEventListener {
    private final SimpMessagingTemplate messagingTemplate;

    public TaskEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Async("customExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskEvent(TaskUpdateEventDto event){
        // Send the event to the WebSocket topic
        messagingTemplate.convertAndSend("/topic/task-updates", event);
    }
}

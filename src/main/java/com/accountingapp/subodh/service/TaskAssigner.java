/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.accountingapp.subodh.service;

import com.accountingapp.subodh.constant.ExpertStatus;
import com.accountingapp.subodh.constant.TaskStatus;
import com.accountingapp.subodh.entity.Customer;
import com.accountingapp.subodh.entity.Expert;
import com.accountingapp.subodh.entity.Task;
import com.accountingapp.subodh.repository.ExpertRepository;
import com.accountingapp.subodh.repository.TaskRepository;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 *
 * @author subodh
 */
@Component
public class TaskAssigner {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ExpertRepository expertRepository;

    private final Queue<Task> pendingTasksQueue = new LinkedList<>();

    @Scheduled(fixedDelay = 5000)
    public void assignTasksToExperts() {
        System.out.println("scheduled job");
        List<Expert> availableExperts = expertRepository
                .findAvailableExpertsSortedByAvailability(
                        ExpertStatus.AVAILABLE,
                        LocalDateTime.now()
                );
        for (Expert expert : availableExperts) {
            Task task = getPendingTask();
            if (task == null) {
                break;
            }
            if(LocalDateTime.now().plusHours(task.getEstimatedTime()).isAfter(task.getDeadline())){
                task.setStatus(TaskStatus.EXPIRED);
                taskRepository.save(task);
                break;
            }
            Customer taskCustomer = task.getCustomer();
            List<Task> customerTasks = taskRepository
                    .findTasksByCustomerAndStatus(taskCustomer, 
                            TaskStatus.PENDING);
            Long customerEstTime = getEstCustomerTasks(customerTasks);
            if (expert.getLastTaskEndTime() != null 
                    && expert.getLastTaskEndTime()
                            .plusHours(customerEstTime)
                            .isAfter(LocalDateTime.now()
                                    .plusHours(expert
                                            .getRemainingWorkHours()))) {
                // expert is not available yet, add task to queue
                pendingTasksQueue.add(task);
            } else {
                expert.setLastTaskEndTime(LocalDateTime.now()
                        .plusHours(customerEstTime));
                expert.setStatus(ExpertStatus.BUSY);
                expertRepository.save(expert);
                for(Task custTask:customerTasks){
                    custTask.setExpert(expert);
                    custTask.setStatus(TaskStatus.IN_PROGRESS);
                    
                    taskRepository.save(custTask);
                    
                }
                
            }
        }
    }
    
    private Long getEstCustomerTasks(List<Task> tasks){
        Long est = (long)0;
        for(Task task: tasks){
            est += task.getEstimatedTime();
        }
        return est;
    }

    private Task getPendingTask() {
        if (!pendingTasksQueue.isEmpty()) {
            return pendingTasksQueue.poll();
        }
        List<Task> pendingTasksList = taskRepository
                .findTopByStatusOrderByDeadlineAsc(TaskStatus.PENDING);
        if (!pendingTasksList.isEmpty()) {
            return pendingTasksList.get(0);
        }
        return null;
    }
    
    @Scheduled(cron = "0 0 0 * * ?") // runs at midnight every day
    public void resetExpertDailyWorkHours() {
        List<Expert> experts = expertRepository.findAll();
        for (Expert expert : experts) {
            expert.setRemainingWorkHours(8);
            expert.setLastTaskEndTime(LocalDateTime.now());
            expertRepository.save(expert);
        }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.accountingapp.subodh.controller;

import com.accountingapp.subodh.constant.ExpertStatus;
import com.accountingapp.subodh.constant.TaskStatus;
import com.accountingapp.subodh.entity.Customer;
import com.accountingapp.subodh.entity.Expert;
import com.accountingapp.subodh.entity.Task;
import com.accountingapp.subodh.repository.CustomerRepository;
import com.accountingapp.subodh.repository.ExpertRepository;
import com.accountingapp.subodh.repository.TaskRepository;
import com.accountingapp.subodh.request.CreateTaskRequest;
import com.accountingapp.subodh.request.CreateUserRequest;
import com.accountingapp.subodh.request.FetchRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author subodh
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api")
public class AccountingController {

  @Autowired
  private CustomerRepository customerRepository;
  
  @Autowired
  private ExpertRepository expertRepository;
  
  @Autowired
  private TaskRepository taskRepository;

  //---------------------- LOGIN APIs ----------------------------------------
  // Endpoint for expert login
  @PostMapping("/expert/login")
  public ResponseEntity<?> 
        expertLogin(@RequestBody FetchRequest loginUserRequest) {
    Optional<Expert> foundExpert = expertRepository
            .findById(loginUserRequest.getId());
    if (foundExpert.isPresent())
        return ResponseEntity.ok(foundExpert.get());
    else
        return ResponseEntity.badRequest().body("LOGIN ERROR.");
  }
        
  // Endpoint for customer login
  @PostMapping("/customer/login")
  public ResponseEntity<?> 
        customerLogin(@RequestBody FetchRequest loginUserRequest) {
    Optional<Customer> foundCustomer = customerRepository
            .findById(loginUserRequest.getId());
    if (foundCustomer.isPresent())
        return ResponseEntity.ok(foundCustomer.get());
    else
        return ResponseEntity.badRequest().body("LOGIN ERROR.");
  }
  
  //---------------------- CUSTOMER APIs ---------------------------------------
  // Endpoint for customer create
  @PostMapping("/customer/create")
  public ResponseEntity<?> 
        createCustomer(@RequestBody CreateUserRequest user) {
    Customer customer = new Customer();
    customer.setEmail(user.getEmail());
    customer.setName(user.getName());
    Customer saveCustomer = customerRepository.save(customer);
    return ResponseEntity.ok(saveCustomer);
  }

  // Endpoint for customer to raise a task request
  @PostMapping("/customer/task/create")
  public ResponseEntity<?> createTask(@RequestBody CreateTaskRequest taskRequest) {
    
    
    Customer customer = customerRepository.findById(taskRequest.getCustomerId()).get();
    List<Task> customerPendingTasks = taskRepository
            .findTasksByCustomerAndStatus(customer, TaskStatus.PENDING);
    Long sum = taskRequest.getEstimatedTime();
    for(Task t: customerPendingTasks){
        sum += t.getEstimatedTime();
    }
    if(sum >= 8) {
        return ResponseEntity.badRequest().body("DAILY LIMIT TASK EXCEEDED.");
    }
    boolean isCustomerTaskInProgress = taskRepository
            .findTasksByCustomerAndStatus(customer, TaskStatus.IN_PROGRESS)
            .isEmpty();
    Task task = new Task();
    task.setStatus(TaskStatus.PENDING);
    task.setCustomer(customer);
    task.setDeadline(LocalDateTime.now().plusDays(taskRequest.getDeadline()));
    task.setEstimatedTime(taskRequest.getEstimatedTime());
    task.setTaskType(taskRequest.getTaskType());
    Task saveTask = taskRepository.save(task);
    return ResponseEntity.ok(saveTask);
  }
  
  // Endpoint for customer to view his tasks
  @PostMapping("/customer/tasks")
  public ResponseEntity<?> viewMyTasks(@RequestBody FetchRequest customerRequest) {
    try{Customer customer = customerRepository.findById(customerRequest.getId()).get();
    List<Task> tasks = taskRepository.findTasksByCustomer(customer);
    return ResponseEntity.ok(tasks);
    }catch(Exception ex){
        return ResponseEntity.badRequest().body(ex.toString());
    }
  }

  // Endpoint for customer to view the status of a task
  @GetMapping("/customer/task-details")
  public ResponseEntity<Task> getTaskStatus(@RequestBody FetchRequest taskRequest) {
    Optional<Task> foundTask = taskRepository.findById(taskRequest.getId());
    if (foundTask.isPresent()) {
      Task task = foundTask.get();
      return ResponseEntity.ok(task);
    } else {
      return ResponseEntity.badRequest().body(null);
    }
  }
  
  //---------------------- EXPERT APIs ----------------------------------------
  // Endpoint for expert create
  @PostMapping("/expert/create")
  public ResponseEntity<?> 
        createExpert(@RequestBody CreateUserRequest user) {
    Expert expert = new Expert();
    expert.setEmail(user.getEmail());
    expert.setName(user.getName());
    expert.setLastTaskEndTime(LocalDateTime.now());
    expert.setRemainingWorkHours(8);
    expert.setStatus(ExpertStatus.AVAILABLE);
    Expert saveExpert = expertRepository.save(expert);
    return ResponseEntity.ok(saveExpert);
  }
        
  // Endpoint for expert to view all tasks
  @GetMapping("/expert/tasks")
  public ResponseEntity<?> getAllTasks() {
    List<Task> tasks = taskRepository.findAll();
    return ResponseEntity.ok(tasks);
  }

  // Endpoint for expert to resolve a task
  @PutMapping("/expert/resolve")
  public ResponseEntity<?> resolveTask(@RequestBody FetchRequest taskRequest) {
    Optional<Task> foundTask = taskRepository.findById(taskRequest.getId());
    if (foundTask.isPresent()) {
      Task task = foundTask.get();
      task.setStatus(TaskStatus.COMPLETED);
      Task saveTask = taskRepository.save(task);
      List<Task> uncompletedTasks = taskRepository.findTasksByExpertAndStatus(
                task.getExpert(), TaskStatus.IN_PROGRESS);
      if(uncompletedTasks.isEmpty()){
        task.getExpert().setStatus(ExpertStatus.AVAILABLE);
      }
      return ResponseEntity.ok(saveTask);
    } else {
      return ResponseEntity.badRequest().body("Invalid task ID.");
    }
  }

  // Endpoint for expert to view queued tasks
  @GetMapping("/expert/tasks/queued")
  public ResponseEntity<List<Task>> getQueuedTasks() {
    List<Task> tasks = taskRepository.findByStatus(TaskStatus.PENDING);
    return ResponseEntity.ok(tasks);
  }

  // Endpoint for expert to view the most important expiring task across customers
  @GetMapping("/expert/tasks/important-tasks")
  public ResponseEntity<?> getMostImportantTask() {
    List<Task> tasks = taskRepository
            .findTopByStatusOrderByDeadlineAsc(TaskStatus.PENDING);
    return ResponseEntity.ok(tasks);
  }
  
  // Endpoint for expert to view his tasks
  @PostMapping("/expert/assigned-tasks")
  public ResponseEntity<?> viewAssignedTasks(@RequestBody FetchRequest expertRequest) {
    Expert expert = expertRepository.findById(expertRequest.getId()).get();
    List<Task> tasks = taskRepository.findTasksByExpert(expert);
    return ResponseEntity.ok(tasks);
    
  }

}

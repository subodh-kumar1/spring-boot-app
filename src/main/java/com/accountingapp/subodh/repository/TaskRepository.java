/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.accountingapp.subodh.repository;

import com.accountingapp.subodh.constant.TaskStatus;
import com.accountingapp.subodh.entity.Customer;
import com.accountingapp.subodh.entity.Expert;
import com.accountingapp.subodh.entity.Task;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author subodh
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>{

    @Query("SELECT t FROM Task t WHERE t.customer = :customer")
    List<Task> findTasksByCustomer(@Param("customer") Customer customer);
    
    @Query("SELECT t FROM Task t WHERE t.expert = :expert")
    List<Task> findTasksByExpert(@Param("expert") Expert expert);
    
    @Query("SELECT t FROM Task t WHERE t.customer = :customer AND t.status = :taskStatus")
    List<Task> findTasksByCustomerAndStatus(@Param("customer") Customer customer, TaskStatus taskStatus);
    
    @Modifying
    @Query("UPDATE Task t "
            + "SET t.status = :status WHERE t.id = :taskId")
    void updateTaskStatus(@Param("taskId") Long taskId, 
            @Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.status = :taskStatus")
    public List<Task> findByStatus(TaskStatus taskStatus);

    @Query("SELECT t FROM Task t "
            + "WHERE t.status = :taskStatus ORDER BY t.deadline ASC")
    public List<Task> findTopByStatusOrderByDeadlineAsc(TaskStatus taskStatus);
    
}

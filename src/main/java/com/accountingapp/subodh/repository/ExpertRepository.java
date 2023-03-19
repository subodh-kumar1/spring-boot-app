/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.accountingapp.subodh.repository;

import com.accountingapp.subodh.constant.ExpertStatus;
import com.accountingapp.subodh.entity.Expert;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author subodh
 */
@Repository
public interface ExpertRepository extends JpaRepository<Expert, Long>{
    List<Expert> findByStatus(ExpertStatus status);
    
    @Query(value = "SELECT e FROM Expert e "
            + "WHERE e.status = :status "
            + "AND e.lastTaskEndTime <= :time "
            + "ORDER BY e.lastTaskEndTime ASC")
    List<Expert> findAvailableExpertsSortedByAvailability(
            @Param("status") ExpertStatus status, 
            @Param("time") LocalDateTime time);
}

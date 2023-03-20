/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.accountingapp.subodh.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author subodh
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTaskRequest {
    private Long estimatedTime;
    private String taskType;

    private Long deadline;

    private Long customerId;
}

package com.ketankp.springboot_flowable_integration.service;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class AITApproveService implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) {
        delegateExecution.getId();
        System.out.println("Approved Yeah");
    }
}

package com.ketankp.springboot_flowable_integration.controller;

import com.ketankp.springboot_flowable_integration.dto.Approval;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("flowable")
public class FlowableController {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @GetMapping("checker/{user}")
    public List<Map<String, Object>> getTaskForUser(@PathVariable("user") String user){
        List<Task> task = taskService.createTaskQuery().taskCandidateUser(user).list();
        List<Map<String, Object>> result = new ArrayList<>();
        for(Task obj:task){
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("taskId",obj.getId());
            map.put("assignee",obj.getAssignee());
            Map<String, Object> variables = taskService.getVariables(obj.getId());
            map.put("dummy",variables.get("dummy"));
            result.add(map);
        }
        return result;
    }

    @GetMapping("maker/{user}")
    public List<Map<String, Object>> getTaskForMaker(@PathVariable("user") String user){
        List<Task> task = taskService.createTaskQuery().taskOwner(user).list();
        List<Map<String, Object>> result = new ArrayList<>();
        for(Task obj:task){
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("taskId",obj.getId());
            Map<String, Object> variables = taskService.getVariables(obj.getId());
            map.put("dummy",variables.get("dummy"));
            result.add(map);
        }
        return result;
    }

    @PostMapping("")
    public void startProcess() {
        Map<String,Object> map = new HashMap<>();
        map.put("dummy", "dummyValue");
        ProcessInstance id = runtimeService.startProcessInstanceByKey("aitRequest",map);
        List<Task> task = taskService.createTaskQuery().processInstanceId(id.getProcessInstanceId()).list();
        for(Task obj : task){
            taskService.addCandidateUser(obj.getId(),"user1");
            taskService.addCandidateUser(obj.getId(),"user2");
            taskService.setOwner(obj.getId(),"user3");
        }
    }

    @PostMapping("/approve")
    public HistoricTaskInstance approve(@RequestBody Approval approval) {
        Task task = taskService.createTaskQuery().taskId(approval.getId()).taskCandidateUser(approval.getUser()).singleResult();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("approved", approval.getStatus());
        if(approval.getStatus()){
            taskService.setVariable(task.getId(),"approvalStatus","Approved");
        }else{
            taskService.setVariable(task.getId(),"approvalStatus","Rejected");
        }

        taskService.claim(approval.getId(), approval.getUser());
        taskService.complete(approval.getId(), variables);
        return historyService.createHistoricTaskInstanceQuery()
                .taskId(approval.getId()).includeTaskLocalVariables().includeProcessVariables().singleResult();
    }
}

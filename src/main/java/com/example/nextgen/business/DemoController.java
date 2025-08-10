package com.example.nextgen.business;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/chat")
@Tag(name = "聊天接口", description = "处理聊天相关接口")
public class DemoController {

    @Autowired
    FinancialAnalysisWorkflow workflow;

    @GetMapping("demo")
    @Operation(summary = "演示接口", description = "执行金融分析工作流的演示接口")
    public String demo() {
        String parallelProcessingWorkflow = workflow.createAndExecuteWorkflow();
        return "demo";
    }
}
package com.example.nextgen.business;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/chat")
public class DemoController {

    public DemoController(FinancialAnalysisWorkflow workflow) {
        this.workflow = workflow;
    }

    FinancialAnalysisWorkflow workflow;

    @GetMapping("demo")
    public String demo() {
        String parallelProcessingWorkflow = workflow.createAndExecuteWorkflow();
        return "demo";
    }
}

package com.sunnao.spring.ddd.template.domain.system.file.model.aggregate;

import com.sunnao.spring.ddd.template.domain.system.file.model.param.CreateFileParam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileAggregateTest {

    @Test
    void createShouldNotRequireStorageType() throws Exception {
        CreateFileParam param = new CreateFileParam();
        param.setOriginalName("report.pdf");
        param.setPath("2026/07/10/report.pdf");
        param.setSize(128L);
        param.setContentType("application/pdf");
        param.setOperatorId(1L);

        FileAggregate aggregate = FileAggregate.create(param);

        assertEquals("report.pdf", aggregate.getFileEntity().getOriginalName());
        assertEquals("2026/07/10/report.pdf", aggregate.getFileEntity().getPath());
    }
}

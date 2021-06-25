package com.cathay.bcsRegression.service.impl;

import com.itextpdf.text.DocumentException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;

@RunWith(SpringRunner.class)
@SpringBootTest
//@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class PdfServiceTest {
    @Autowired
    private PdfService pdfService;

    @Test
    public void helloWordTest() throws DocumentException, FileNotFoundException {
        String file = "c:/temp/hello.pdf";
        pdfService.createHelloPdf(file);
    }

}
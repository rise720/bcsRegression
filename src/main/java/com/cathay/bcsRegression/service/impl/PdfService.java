package com.cathay.bcsRegression.service.impl;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author 0100065352
 */
@Service
public class PdfService {

    public void createHelloPdf(String path) throws FileNotFoundException, DocumentException {
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance( doc, new FileOutputStream(path));
        doc.open();
        doc.add(new Paragraph( "Hello World! Hello People! " +
                "Hello Sky! Hello Sun! Hello Moon! Hello Stars!"));
        doc.close();
    }
}

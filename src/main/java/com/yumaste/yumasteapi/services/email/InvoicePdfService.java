package com.yumaste.yumasteapi.services.email;


import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.yumaste.yumasteapi.models.DettaglioOrdine;
import com.yumaste.yumasteapi.models.Ordine;
import com.yumaste.yumasteapi.repositories.DettaglioOrdineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    private final DettaglioOrdineRepository dettaglioOrdineRepository;


    public byte[] generateInvoice(Ordine ordine) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();

        // Header
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        document.add(new Paragraph("FATTURA", titleFont));
        document.add(new Paragraph("Numero ordine: #" + ordine.getId()));
        document.add(new Paragraph("Data: " + ordine.getDataOrdine().toString()));
        document.add(Chunk.NEWLINE);

        // Dettagli cliente
        document.add(new Paragraph("Cliente: " + ordine.getUtente().getNome() + " " + ordine.getUtente().getCognome()));
        document.add(new Paragraph("Email: " + ordine.getUtente().getEmail()));
        document.add(Chunk.NEWLINE);

        // Tabella prodotti
        PdfPTable table = new PdfPTable(3); // Prodotto | Quantità | Prezzo
        table.setWidthPercentage(100);
        table.addCell("Prodotto");
        table.addCell("Quantità");
        table.addCell("Prezzo");


        List<DettaglioOrdine> dettagli = dettaglioOrdineRepository.findByOrdine_Id(ordine.getId());

        for (DettaglioOrdine box : dettagli) {
            table.addCell(box.getBox().getNome());
            table.addCell(String.valueOf(box.getQuantita()));
            table.addCell("€ " + box.getPrezzoUnitario());
        }
        document.add(table);

        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("TOTALE: € " + ordine.getTotalePrezzo(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));

        document.close();
        return baos.toByteArray();
    }
}
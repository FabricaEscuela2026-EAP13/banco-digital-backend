package co.edu.udea.bancodigital.services;

import co.edu.udea.bancodigital.dtos.responses.CertificadoBancarioDTO;
import co.edu.udea.bancodigital.dtos.responses.ReporteMovimientosDTO;
import co.edu.udea.bancodigital.exception.ReporteGenerationException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class ReporteGeneratorService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public byte[] generarPDF(CertificadoBancarioDTO certificado) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("CERTIFICADO BANCARIO")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setBold());

            document.add(new Paragraph("Banco Digital S.A.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12));

            document.add(new Paragraph("\n"));

            Table table = new Table(2);
            table.addCell("Nombre del Cliente:");
            table.addCell(certificado.getNombreCompleto());
            table.addCell("Fecha de Consulta:");
            table.addCell(certificado.getFechaConsulta().format(DATE_FORMATTER));

            document.add(table);

            document.add(new Paragraph("\n"));

            Table detailsTable = new Table(2);
            detailsTable.addCell("Producto:");
            detailsTable.addCell(certificado.getNombreProducto());
            detailsTable.addCell("Número de Cuenta:");
            detailsTable.addCell(certificado.getNumeroCuenta());
            detailsTable.addCell("Fecha de Inicio:");
            detailsTable.addCell(certificado.getFechaInicio().format(DATETIME_FORMATTER));
            detailsTable.addCell("Balance Actual:");
            detailsTable.addCell(formatearSaldo(certificado.getBalance()));
            detailsTable.addCell("Estado de Cuenta:");
            detailsTable.addCell(certificado.getEstadoCuenta());

            document.add(detailsTable);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF: {}", e.getMessage(), e);
            throw new ReporteGenerationException("Error generando PDF del certificado", e);
        }
    }

    public byte[] generarCSV(CertificadoBancarioDTO certificado) {
        try {
            StringWriter sw = new StringWriter();
            CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder()
                    .setHeader("Campo", "Valor")
                    .build());

            printer.printRecord("Nombre del Cliente", certificado.getNombreCompleto());
            printer.printRecord("Fecha de Consulta", certificado.getFechaConsulta().format(DATE_FORMATTER));
            printer.printRecord("Producto", certificado.getNombreProducto());
            printer.printRecord("Número de Cuenta", certificado.getNumeroCuenta());
            printer.printRecord("Fecha de Inicio", certificado.getFechaInicio().format(DATETIME_FORMATTER));
            printer.printRecord("Balance Actual", formatearSaldo(certificado.getBalance()));
            printer.printRecord("Estado de Cuenta", certificado.getEstadoCuenta());

            printer.flush();
            return sw.toString().getBytes();
        } catch (Exception e) {
            log.error("Error generando CSV: {}", e.getMessage(), e);
            throw new ReporteGenerationException("Error generando CSV del certificado", e);
        }
    }

    private String formatearSaldo(java.math.BigDecimal saldo) {
        return "$" + String.format("%,.2f", saldo);
    }

    public byte[] generarPDFReporte(ReporteMovimientosDTO reporte) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("REPORTE DE MOVIMIENTOS")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setBold());

            document.add(new Paragraph("Banco Digital S.A.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12));

            document.add(new Paragraph("\n"));

            Table headerTable = new Table(2);
            headerTable.addCell("Cliente:");
            headerTable.addCell(reporte.getNombreCliente());
            headerTable.addCell("Número de Cuenta:");
            headerTable.addCell(reporte.getNumeroCuenta());
            headerTable.addCell("Período:");
            headerTable.addCell(reporte.getFechaInicio().format(DATE_FORMATTER) + " a " +
                    reporte.getFechaFin().format(DATE_FORMATTER));
            headerTable.addCell("Fecha de Consulta:");
            headerTable.addCell(reporte.getFechaConsulta().format(DATE_FORMATTER));
            headerTable.addCell("Saldo Final:");
            headerTable.addCell(formatearSaldo(reporte.getSaldoFinal()));

            document.add(headerTable);
            document.add(new Paragraph("\n"));

            if (reporte.getTransacciones() == null || reporte.getTransacciones().isEmpty()) {
                document.add(new Paragraph("No hay movimientos en el período seleccionado")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setItalic());
            } else {
                document.add(new Paragraph("MOVIMIENTOS")
                        .setBold()
                        .setFontSize(12));

                Table movementsTable = new Table(5);
                movementsTable.addCell("Fecha");
                movementsTable.addCell("Tipo");
                movementsTable.addCell("Monto");
                movementsTable.addCell("Estado");
                movementsTable.addCell("Descripción");

                for (var transaccion : reporte.getTransacciones()) {
                    movementsTable.addCell(transaccion.getFechaHora().format(DATETIME_FORMATTER));
                    movementsTable.addCell(transaccion.getTipo().getNombre());
                    movementsTable.addCell(formatearSaldo(transaccion.getMonto()));
                    movementsTable.addCell(transaccion.getEstado().getNombre());
                    movementsTable.addCell(transaccion.getDescripcion() != null ? transaccion.getDescripcion() : "-");
                }

                document.add(movementsTable);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF de reporte: {}", e.getMessage(), e);
            throw new ReporteGenerationException("Error generando PDF del reporte", e);
        }
    }

    public byte[] generarCSVReporte(ReporteMovimientosDTO reporte) {
        try {
            StringWriter sw = new StringWriter();
            CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

            printer.printRecord("REPORTE DE MOVIMIENTOS");
            printer.printRecord("");
            printer.printRecord("Cliente", reporte.getNombreCliente());
            printer.printRecord("Número de Cuenta", reporte.getNumeroCuenta());
            printer.printRecord("Período", reporte.getFechaInicio().format(DATE_FORMATTER) + " a " + reporte.getFechaFin().format(DATE_FORMATTER));
            printer.printRecord("Fecha de Consulta", reporte.getFechaConsulta().format(DATE_FORMATTER));
            printer.printRecord("Saldo Final", formatearSaldo(reporte.getSaldoFinal()));
            printer.printRecord("");
            printer.printRecord("MOVIMIENTOS");
            printer.printRecord("Fecha", "Tipo", "Monto", "Estado", "Descripción");

            if (reporte.getTransacciones() != null && !reporte.getTransacciones().isEmpty()) {
                for (var transaccion : reporte.getTransacciones()) {
                    printer.printRecord(
                            transaccion.getFechaHora().format(DATETIME_FORMATTER),
                            transaccion.getTipo().getNombre(),
                            transaccion.getMonto(),
                            transaccion.getEstado().getNombre(),
                            transaccion.getDescripcion() != null ? transaccion.getDescripcion() : ""
                    );
                }
            }

            printer.flush();
            return sw.toString().getBytes();
        } catch (Exception e) {
            log.error("Error generando CSV de reporte: {}", e.getMessage(), e);
            throw new ReporteGenerationException("Error generando CSV del reporte", e);
        }
    }
}

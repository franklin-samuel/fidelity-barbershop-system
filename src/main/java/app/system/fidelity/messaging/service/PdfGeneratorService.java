package app.system.fidelity.messaging.service;

import app.system.fidelity.domain.BarberMonthlyPerformance;
import app.system.fidelity.domain.MonthlyReport;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Slf4j
@Service
public class PdfGeneratorService {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(LOCALE_BR);
    private static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance(LOCALE_BR);
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(52, 73, 94);

    static {
        PERCENT_FORMAT.setMinimumFractionDigits(2);
        PERCENT_FORMAT.setMaximumFractionDigits(2);
    }

    public byte[] generateMonthlyReportPdf(final MonthlyReport report) {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            final PdfWriter writer = new PdfWriter(baos);
            final PdfDocument pdfDoc = new PdfDocument(writer);
            final Document document = new Document(pdfDoc);

            addTitle(document, report);

            addExecutiveSummary(document, report);

            addRevenueSection(document, report);

            addBarbersPerformanceSection(document, report);

            addInsightsSection(document, report);

            document.close();

            log.info("PDF do relatório mensal gerado com sucesso para {}", report.getReportMonth());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Erro ao gerar PDF do relatório mensal", e);
            throw new RuntimeException("Falha ao gerar PDF do relatório mensal", e);
        }
    }

    private void addTitle(final Document document, final MonthlyReport report) {
        final String monthName = report.getReportMonth()
                .getMonth()
                .getDisplayName(TextStyle.FULL, LOCALE_BR);
        final int year = report.getReportMonth().getYear();

        final Paragraph title = new Paragraph("RELATÓRIO MENSAL")
                .setFontSize(24)
                .setBold()
                .setFontColor(HEADER_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);

        final Paragraph subtitle = new Paragraph(String.format("%s de %d",
                monthName.substring(0, 1).toUpperCase() + monthName.substring(1), year))
                .setFontSize(18)
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);

        document.add(title);
        document.add(subtitle);
    }

    private void addExecutiveSummary(final Document document, final MonthlyReport report) {
        addSectionTitle(document, "Resumo Executivo");

        final Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        addSummaryRow(table, "Receita Total", CURRENCY_FORMAT.format(report.getTotalRevenue()));

        final String growthText = formatGrowth(report.getRevenueGrowthPercentage(), report.getRevenueGrowthAbsolute());
        addSummaryRow(table, "Crescimento", growthText);

        addSummaryRow(table, "Total de Atendimentos", String.valueOf(report.getTotalAppointments()));

        addSummaryRow(table, "Ticket Médio", CURRENCY_FORMAT.format(report.getAverageTicket()));

        addSummaryRow(table, "Novos Clientes", String.valueOf(report.getNewCustomers()));

        document.add(table);
    }

    private void addRevenueSection(final Document document, final MonthlyReport report) {
        addSectionTitle(document, "Análise de Receitas");

        final Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        addSummaryRow(table, "Receita de Serviços", CURRENCY_FORMAT.format(report.getServicesRevenue()));

        addSummaryRow(table, "Receita de Produtos", CURRENCY_FORMAT.format(report.getProductsRevenue()));

        final BigDecimal servicesPercent = calculatePercentage(report.getServicesRevenue(), report.getTotalRevenue());
        addSummaryRow(table, "% Serviços", PERCENT_FORMAT.format(servicesPercent.divide(BigDecimal.valueOf(100))));

        final BigDecimal productsPercent = calculatePercentage(report.getProductsRevenue(), report.getTotalRevenue());
        addSummaryRow(table, "% Produtos", PERCENT_FORMAT.format(productsPercent.divide(BigDecimal.valueOf(100))));

        document.add(table);
    }

    private void addBarbersPerformanceSection(final Document document, final MonthlyReport report) {
        addSectionTitle(document, "Desempenho dos Barbeiros");

        if (report.getBarbersPerformance() == null || report.getBarbersPerformance().isEmpty()) {
            document.add(new Paragraph("Nenhum dado de barbeiros disponível para este período.")
                    .setFontSize(10)
                    .setMarginBottom(20));
            return;
        }

        final Table table = new Table(UnitValue.createPercentArray(new float[]{30, 25, 20, 25}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        addTableHeader(table, "Barbeiro");
        addTableHeader(table, "Receita");
        addTableHeader(table, "Atendimentos");
        addTableHeader(table, "Ticket Médio");

        for (final BarberMonthlyPerformance performance : report.getBarbersPerformance()) {
            addTableCell(table, performance.getBarberName());
            addTableCell(table, CURRENCY_FORMAT.format(performance.getTotalRevenue()));
            addTableCell(table, String.valueOf(performance.getAppointmentsCount()));
            addTableCell(table, CURRENCY_FORMAT.format(performance.getAverageTicket()));
        }

        document.add(table);
    }

    private void addInsightsSection(final Document document, final MonthlyReport report) {
        addSectionTitle(document, "Insights e Recomendações");

        if (report.getWeakDaysInsights() == null || report.getWeakDaysInsights().isEmpty()) {
            document.add(new Paragraph("Nenhum insight disponível para este período.")
                    .setFontSize(10)
                    .setMarginBottom(20));
            return;
        }

        for (final String insight : report.getWeakDaysInsights()) {
            final Paragraph p = new Paragraph("• " + insight)
                    .setFontSize(10)
                    .setMarginBottom(5);
            document.add(p);
        }

        document.add(new Paragraph("").setMarginBottom(20));
    }

    private void addSectionTitle(final Document document, final String title) {
        final Paragraph sectionTitle = new Paragraph(title)
                .setFontSize(14)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginTop(10)
                .setMarginBottom(10);
        document.add(sectionTitle);
    }

    private void addSummaryRow(final Table table, final String label, final String value) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFontSize(10).setBold())
                .setBackgroundColor(new DeviceRgb(236, 240, 241))
                .setPadding(8));

        table.addCell(new Cell()
                .add(new Paragraph(value).setFontSize(10))
                .setPadding(8));
    }

    private void addTableHeader(final Table table, final String text) {
        table.addHeaderCell(new Cell()
                .add(new Paragraph(text).setFontSize(10).setBold())
                .setBackgroundColor(HEADER_COLOR)
                .setFontColor(ColorConstants.WHITE)
                .setPadding(8)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private void addTableCell(final Table table, final String text) {
        table.addCell(new Cell()
                .add(new Paragraph(text).setFontSize(9))
                .setPadding(6)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private String formatGrowth(final BigDecimal percentage, final BigDecimal absolute) {
        final String sign = absolute.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return String.format("%s%s (%s%s)",
                sign,
                CURRENCY_FORMAT.format(absolute),
                sign,
                PERCENT_FORMAT.format(percentage.divide(BigDecimal.valueOf(100))));
    }

    private BigDecimal calculatePercentage(final BigDecimal part, final BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return part.multiply(BigDecimal.valueOf(100))
                .divide(total, 2, java.math.RoundingMode.HALF_UP);
    }
}
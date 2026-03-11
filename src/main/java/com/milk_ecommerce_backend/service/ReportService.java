package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.dto.SalesReportResponse;
import com.milk_ecommerce_backend.model.Order;
import com.milk_ecommerce_backend.repository.OrderRepository;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private OrderRepository orderRepository;

    // =============================
    // JSON REPORT (Admin dashboard)
    // =============================

    public SalesReportResponse getSalesReport(LocalDateTime from, LocalDateTime to) {

        Timestamp fromTs = Timestamp.valueOf(from);
        Timestamp toTs = Timestamp.valueOf(to);

        List<Order> paidOrders =
                orderRepository.findByOrderStatusAndOrderDateBetween("PAID", fromTs, toTs);

        BigDecimal revenue = BigDecimal.ZERO;

        for (Order o : paidOrders) {
            if (o.getTotalAmount() != null) {
                revenue = revenue.add(o.getTotalAmount());
            }
        }

        SalesReportResponse res = new SalesReportResponse();
        res.setTotalOrders(paidOrders.size());
        res.setTotalRevenue(revenue.doubleValue());
        res.setDailySales(buildDailySales(paidOrders, from.toLocalDate(), to.toLocalDate()));

        return res;
    }

    // =============================
    // DAILY SALES GRAPH
    // =============================

    private List<Map<String, Object>> buildDailySales(
            List<Order> orders,
            LocalDate from,
            LocalDate to
    ) {

        Map<LocalDate, BigDecimal> map = new LinkedHashMap<>();

        LocalDate d = from;
        while (!d.isAfter(to)) {
            map.put(d, BigDecimal.ZERO);
            d = d.plusDays(1);
        }

        for (Order o : orders) {

            if (o.getOrderDate() != null && o.getTotalAmount() != null) {

                LocalDate day =
                        o.getOrderDate()
                                .toLocalDateTime()
                                .toLocalDate();

                if (map.containsKey(day)) {

                    map.put(
                            day,
                            map.get(day).add(o.getTotalAmount())
                    );
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<LocalDate, BigDecimal> e : map.entrySet()) {

            Map<String, Object> row = new HashMap<>();

            row.put("date", e.getKey().toString());
            row.put("amount", e.getValue().doubleValue());

            result.add(row);
        }

        return result;
    }

    // =============================
    // CSV EXPORT
    // =============================

    public byte[] exportSalesCsv(LocalDateTime from, LocalDateTime to) {

        Timestamp fromTs = Timestamp.valueOf(from);
        Timestamp toTs = Timestamp.valueOf(to);

        List<Order> orders =
                orderRepository.findByOrderStatusAndOrderDateBetween("PAID", fromTs, toTs);

        StringBuilder sb = new StringBuilder();

        sb.append("OrderId,UserId,TotalAmount,Status,OrderDate\n");

        for (Order o : orders) {

            sb.append(o.getId()).append(",");

            sb.append(
                    o.getUser() != null
                            ? o.getUser().getId()
                            : ""
            ).append(",");

            BigDecimal amt =
                    o.getTotalAmount() != null
                            ? o.getTotalAmount()
                            : BigDecimal.ZERO;

            sb.append(amt).append(",");

            sb.append(
                    o.getOrderStatus() != null
                            ? o.getOrderStatus()
                            : ""
            ).append(",");

            sb.append(
                    o.getOrderDate() != null
                            ? o.getOrderDate()
                            : ""
            ).append("\n");
        }

        return sb.toString().getBytes();
    }

    // =============================
    // EXCEL EXPORT
    // =============================

    public byte[] exportSalesXlsx(LocalDateTime from, LocalDateTime to) {

        Timestamp fromTs = Timestamp.valueOf(from);
        Timestamp toTs = Timestamp.valueOf(to);

        List<Order> orders =
                orderRepository.findByOrderStatusAndOrderDateBetween("PAID", fromTs, toTs);

        try (
                Workbook wb = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {

            Sheet sheet = wb.createSheet("Sales Report");

            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("OrderId");
            header.createCell(1).setCellValue("UserId");
            header.createCell(2).setCellValue("TotalAmount");
            header.createCell(3).setCellValue("Status");
            header.createCell(4).setCellValue("OrderDate");

            int rowNum = 1;

            for (Order o : orders) {

                Row r = sheet.createRow(rowNum++);

                r.createCell(0).setCellValue(o.getId());

                r.createCell(1).setCellValue(
                        o.getUser() != null
                                ? o.getUser().getId()
                                : 0
                );

                BigDecimal amt =
                        o.getTotalAmount() != null
                                ? o.getTotalAmount()
                                : BigDecimal.ZERO;

                r.createCell(2).setCellValue(amt.doubleValue());

                r.createCell(3).setCellValue(
                        o.getOrderStatus() != null
                                ? o.getOrderStatus()
                                : ""
                );

                r.createCell(4).setCellValue(
                        o.getOrderDate() != null
                                ? o.getOrderDate().toString()
                                : ""
                );
            }

            wb.write(out);

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Excel export failed: " + e.getMessage(),
                    e
            );
        }
    }

    // =============================
    // PDF EXPORT
    // =============================

    public byte[] exportSalesPdf(LocalDateTime from, LocalDateTime to) {

        Timestamp fromTs = Timestamp.valueOf(from);
        Timestamp toTs = Timestamp.valueOf(to);

        List<Order> orders =
                orderRepository.findByOrderStatusAndOrderDateBetween("PAID", fromTs, toTs);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4);

            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont =
                    new Font(Font.HELVETICA, 16, Font.BOLD);

            document.add(
                    new Paragraph("Sales Report (PAID Orders)", titleFont)
            );

            document.add(
                    new Paragraph("From: " + from + "  To: " + to)
            );

            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);

            table.setWidthPercentage(100);

            table.addCell("OrderId");
            table.addCell("UserId");
            table.addCell("TotalAmount");
            table.addCell("Status");
            table.addCell("OrderDate");

            for (Order o : orders) {

                table.addCell(String.valueOf(o.getId()));

                table.addCell(
                        o.getUser() != null
                                ? String.valueOf(o.getUser().getId())
                                : ""
                );

                BigDecimal amt =
                        o.getTotalAmount() != null
                                ? o.getTotalAmount()
                                : BigDecimal.ZERO;

                table.addCell(amt.toPlainString());

                table.addCell(
                        o.getOrderStatus() != null
                                ? o.getOrderStatus()
                                : ""
                );

                table.addCell(
                        o.getOrderDate() != null
                                ? o.getOrderDate().toString()
                                : ""
                );
            }

            document.add(table);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "PDF export failed: " + e.getMessage(),
                    e
            );
        }
    }
}
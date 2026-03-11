package com.milk_ecommerce_backend.controller;

import com.milk_ecommerce_backend.dto.SalesReportResponse;
import com.milk_ecommerce_backend.service.ReportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    private LocalDateTime startOfDay(LocalDate d) { return d.atStartOfDay(); }
    private LocalDateTime endOfDay(LocalDate d) { return d.atTime(23, 59, 59); }

    // ✅ JSON report (graph + totals)
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/sales")
    public SalesReportResponse salesReport(
            @RequestParam String from,
            @RequestParam String to
    ) {
        LocalDate f = LocalDate.parse(from);
        LocalDate t = LocalDate.parse(to);
        return reportService.getSalesReport(startOfDay(f), endOfDay(t));
    }

    // ✅ CSV download
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/sales/csv")
    public ResponseEntity<byte[]> salesCsv(@RequestParam String from, @RequestParam String to) {

        byte[] data = reportService.exportSalesCsv(startOfDay(LocalDate.parse(from)), endOfDay(LocalDate.parse(to)));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales-report.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(data);
    }

    // ✅ Excel download
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/sales/xlsx")
    public ResponseEntity<byte[]> salesXlsx(@RequestParam String from, @RequestParam String to) {

        byte[] data = reportService.exportSalesXlsx(startOfDay(LocalDate.parse(from)), endOfDay(LocalDate.parse(to)));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales-report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    // ✅ PDF download
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/sales/pdf")
    public ResponseEntity<byte[]> salesPdf(@RequestParam String from, @RequestParam String to) {

        byte[] data = reportService.exportSalesPdf(startOfDay(LocalDate.parse(from)), endOfDay(LocalDate.parse(to)));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
}
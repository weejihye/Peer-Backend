package peer.backend.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import peer.backend.mongo.entity.UserTracking;
import peer.backend.mongo.repository.UserTrackingRepository;

@RequiredArgsConstructor
@Service
public class ExcelService {

    private final UserTrackingRepository userTrackingRepository;

    public ByteArrayInputStream getTrackingExcel() throws IOException {
        List<UserTracking> userTrackingList = this.userTrackingRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Sheet sheet = workbook.createSheet("UserTracking");
        createUserTrackingSheet(userTrackingList, sheet, headerCellStyle);

        workbook.write(out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    private void createUserTrackingSheet(List<UserTracking> list, Sheet sheet,
        CellStyle headerCellStyle) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"userId", "userEmail", "registrationDate", "unRegistrationDate",
            "intraId", "ftOAuthRegistered", "peerMemberDate", "accumulatedWallet",
            "monthlyAccumulatedWallet", "status", "reportCount"};

        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerCellStyle);
        }

        int idx = 1;
        for (UserTracking userTracking : list) {
            Row bodyRow = sheet.createRow(idx++);
            Cell bodyCell = bodyRow.createCell(0);
            bodyCell.setCellValue(userTracking.getUserId());
            bodyCell = bodyRow.createCell(1);
            bodyCell.setCellValue(userTracking.getUserEmail());
            bodyCell = bodyRow.createCell(2);
            bodyCell.setCellValue(userTracking.getRegistrationDate().toString());
            bodyCell = bodyRow.createCell(3);
            bodyCell.setCellValue(userTracking.getUnRegistrationDate());
            bodyCell = bodyRow.createCell(4);
            bodyCell.setCellValue(userTracking.getIntraId());
            bodyCell = bodyRow.createCell(5);
            bodyCell.setCellValue(userTracking.isFtOAuthRegistered());
            bodyCell = bodyRow.createCell(6);
            bodyCell.setCellValue(userTracking.getPeerMemberDate());
            bodyCell = bodyRow.createCell(7);
            bodyCell.setCellValue(userTracking.getAccumulatedWallet());
            bodyCell = bodyRow.createCell(8);
            bodyCell.setCellValue(userTracking.getMonthlyAccumulatedWallet());
            bodyCell = bodyRow.createCell(9);
            bodyCell.setCellValue(userTracking.getStatus().getValue());
            bodyCell = bodyRow.createCell(10);
            bodyCell.setCellValue(userTracking.getReportCount());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, (sheet.getColumnWidth(i) + 1024));
        }
    }
}

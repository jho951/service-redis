package com.drawer.application;

import com.common.web.PageResponse;
import com.drawer.interfaces.dto.response.DrawerDetailResponse;
import com.drawer.interfaces.dto.response.DrawerListItemResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class DrawerExportService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 상세 DTO 기반 단건 워크북 생성 */
    public byte[] makeSingleWorkbookFromDto(DrawerDetailResponse d) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            CellStyle header = wb.createCellStyle();
            XSSFFont bold = wb.createFont(); bold.setBold(true); header.setFont(bold);

            // meta
            Sheet meta = wb.createSheet("meta");
            int r = 0;
            r = row(meta, r, header, "id", toStr(d.getId()));
            r = row(meta, r, header, "title", d.getTitle());
            r = row(meta, r, header, "version", toStr(d.getVersion()));
            r = row(meta, r, header, "createdAt", ts(d.getCreatedAt()));
            r = row(meta, r, header, "updatedAt", ts(d.getUpdatedAt()));
            r = row(meta, r, header, "deletedAt", ts(d.getDeletedAt()));
            autoSize(meta, 2);

            // payload
            Sheet payload = wb.createSheet("payload");
            Row h = payload.createRow(0);
            Cell c0 = h.createCell(0); c0.setCellValue("drawer_id"); c0.setCellStyle(header);
            Cell c1 = h.createCell(1); c1.setCellValue("vectorJson"); c1.setCellStyle(header);

            Row data = payload.createRow(1);
            data.createCell(0).setCellValue(toStr(d.getId()));
            CellStyle wrap = wb.createCellStyle(); wrap.setWrapText(true);
            Cell jsonCell = data.createCell(1);
            jsonCell.setCellValue(d.getVectorJson() != null ? d.getVectorJson() : "");
            jsonCell.setCellStyle(wrap);

            payload.setColumnWidth(0, 18 * 256);
            payload.setColumnWidth(1, 80 * 256);

            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("엑셀 생성 실패", e);
        }
    }

    /** 페이지 DTO 기반 목록 워크북 생성 */
    public byte[] makeListWorkbookFromDto(PageResponse<DrawerListItemResponse> page) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            CellStyle header = wb.createCellStyle();
            XSSFFont bold = wb.createFont(); bold.setBold(true); header.setFont(bold);

            // list_meta
            Sheet meta = wb.createSheet("list_meta");
            String[] cols = {"id","title","version","createdAt","updatedAt","deletedAt"};
            Row head = meta.createRow(0);
            for (int i=0; i<cols.length; i++) {
                Cell cell = head.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(header);
            }
            int r = 1;
            for (DrawerListItemResponse d : page.getItems()) {
                Row row = meta.createRow(r++);
                row.createCell(0).setCellValue(toStr(d.getId()));
                row.createCell(1).setCellValue(nullSafe(d.getTitle()));
                row.createCell(2).setCellValue(toStr(d.getVersion()));
                row.createCell(3).setCellValue(ts(d.getCreatedAt()));
                row.createCell(4).setCellValue(ts(d.getUpdatedAt()));
                row.createCell(5).setCellValue(ts(d.getDeletedAt()));
            }
            for (int i=0;i<cols.length;i++) meta.autoSizeColumn(i);

            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("엑셀 생성 실패", e);
        }
    }

    /* helpers */
    private int row(Sheet s, int r, CellStyle header, String key, String val) {
        Row row = s.createRow(r);
        Cell k = row.createCell(0); k.setCellValue(key); k.setCellStyle(header);
        row.createCell(1).setCellValue(nullSafe(val));
        return r + 1;
    }
    private void autoSize(Sheet s, int cols) { for (int i=0;i<cols;i++) s.autoSizeColumn(i); }
    private String ts(java.time.LocalDateTime t) { return t == null ? "" : ISO.format(t); }
    private String toStr(Object o) { return o == null ? "" : String.valueOf(o); }
    private String nullSafe(String s) { return s == null ? "" : s; }
}
